package ac.grim.grimac.internal.storage.backend.sqlite.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.event.CheckCatalogEvent;
import ac.grim.grimac.api.storage.event.PlayerIdentityEvent;
import ac.grim.grimac.api.storage.event.ServerStartupEvent;
import ac.grim.grimac.api.storage.event.SessionEvent;
import ac.grim.grimac.api.storage.event.SettingEvent;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.EntityOps;
import ac.grim.grimac.api.storage.kind.ops.EventStreamOps;
import ac.grim.grimac.api.storage.model.CheckCatalogRecord;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.ServerStartupRecord;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.SettingRecord;
import ac.grim.grimac.api.storage.model.SettingScope;
import ac.grim.grimac.api.storage.model.VerboseFormat;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.internal.storage.core.DataStoreImpl;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Java 8 typed SQLite adapter for Grim's built-in v2 DataKinds.
 *
 * It intentionally stores real columns instead of the old payload-only debug
 * table, so operators can inspect rows and /grim history has data to read.
 */
final class Java8SqliteV2KindAdapter implements KindAdapter<DataKind<?, ?>> {
    interface ConnectionSupplier {
        Connection get() throws BackendException;
    }

    private final ConnectionSupplier connections;

    Java8SqliteV2KindAdapter(ConnectionSupplier connections) {
        this.connections = connections;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NotNull Class<DataKind<?, ?>> kindType() {
        return (Class) DataKind.class;
    }

    @Override
    public @NotNull EnumSet<Capability> subcapabilities() {
        return EnumSet.allOf(Capability.class);
    }

    @Override
    public void ensureStore(@NotNull StoreId id, @NotNull DataKind<?, ?> kind) throws BackendException {
        String table = table(id, kind);
        ensureGeneric(table);
        String name = kind.name();
        if ("violations".equals(name)) ensureViolations(table);
        else if ("sessions".equals(name)) ensureSessions(table);
        else if ("checks".equals(name)) ensureChecks(table);
        else if ("players".equals(name)) ensurePlayers(table);
        else if ("settings".equals(name)) ensureSettings(table);
        else if ("server-startups".equals(name)) ensureStartups(table);
    }

    @Override
    public void dropStore(@NotNull StoreId id, @NotNull DataKind<?, ?> kind) throws BackendException {
        try (Statement statement = connection().createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS " + table(id, kind));
        } catch (SQLException e) {
            throw new BackendException("failed to drop SQLite v2 store " + id + "/" + kind.name(), e);
        }
    }

    @Override
    public <E> @NotNull StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull DataKind<?, ?> kind, @NotNull Category<E> category) {
        final String table = table(id, kind);
        return new StorageEventHandler<E>() {
            @Override
            public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
                writeAny(table, event);
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R execute(@NotNull StoreId id, @NotNull DataKind<?, ?> kind, @NotNull Operation<R> op) throws BackendException {
        String table = table(id, kind);

        if (op instanceof DataStoreImpl.QueryOperation) {
            Object query = ((DataStoreImpl.QueryOperation<?>) op).query();
            return (R) executeQuery(table, query);
        }

        if (op instanceof EntityOps.UpsertOp) {
            writeAny(table, ((EntityOps.UpsertOp<?>) op).record());
            return null;
        }

        if (op instanceof EntityOps.GetByIdOp) {
            EntityOps.GetByIdOp<?, ?> get = (EntityOps.GetByIdOp<?, ?>) op;
            return (R) getById(table, get.category(), get.id());
        }

        if (op instanceof EntityOps.FindByIndexOp) {
            EntityOps.FindByIndexOp<?> find = (EntityOps.FindByIndexOp<?>) op;
            return (R) findByIndex(table, find.category(), find.indexName(), find.key(), find.cursor(), find.pageSize());
        }

        if (op instanceof EntityOps.CountByIndexOp) {
            EntityOps.CountByIndexOp count = (EntityOps.CountByIndexOp) op;
            return (R) Long.valueOf(countByIndex(table, count.category(), count.indexName(), count.key()));
        }

        if (op instanceof EventStreamOps.CountOp) {
            EventStreamOps.CountOp count = (EventStreamOps.CountOp) op;
            return (R) Long.valueOf(countPartition(table, count.partition(), count.key()));
        }

        if (op instanceof EventStreamOps.CountDistinctOp) {
            EventStreamOps.CountDistinctOp count = (EventStreamOps.CountDistinctOp) op;
            return (R) Long.valueOf(countDistinct(table, count.partition(), count.key(), count.field()));
        }

        if (op instanceof EventStreamOps.CountManyOp) {
            EventStreamOps.CountManyOp<?> many = (EventStreamOps.CountManyOp<?>) op;
            return (R) countMany(table, many.partition(), many.keys());
        }

        if (op instanceof EventStreamOps.PageOp) {
            EventStreamOps.PageOp<?> page = (EventStreamOps.PageOp<?>) op;
            return (R) pagePartition(table, page.category(), page.partition(), page.key(), page.cursor(), page.pageSize());
        }

        if (op instanceof EventStreamOps.DeleteOlderThanOp) {
            deleteOlderThan(table, ((EventStreamOps.DeleteOlderThanOp) op).cutoffEpochMs());
            return null;
        }

        if (op instanceof EventStreamOps.DeleteByPartitionOp) {
            EventStreamOps.DeleteByPartitionOp del = (EventStreamOps.DeleteByPartitionOp) op;
            deletePartition(table, del.partition(), del.key());
            return null;
        }

        return null;
    }

    @Override
    public @NotNull List<Migration<DataKind<?, ?>>> migrations(@NotNull DataKind<?, ?> kind) {
        return Collections.emptyList();
    }

    private Object executeQuery(String table, Object query) throws BackendException {
        if (query instanceof Queries.ListSessionsByPlayer) {
            Queries.ListSessionsByPlayer q = (Queries.ListSessionsByPlayer) query;
            return listSessionsByPlayer(table, q.player(), q.cursor(), q.pageSize());
        }
        if (query instanceof Queries.GetSessionById) {
            Queries.GetSessionById q = (Queries.GetSessionById) query;
            SessionRecord record = selectSessionById(table, q.sessionId());
            if (record == null) return Page.empty();
            return new Page<SessionRecord>(Collections.singletonList(record), null);
        }
        if (query instanceof Queries.ListViolationsInSession) {
            Queries.ListViolationsInSession q = (Queries.ListViolationsInSession) query;
            return listViolationsInSession(table, q.sessionId(), q.cursor(), q.pageSize());
        }
        if (query instanceof Queries.GetPlayerIdentity) {
            Queries.GetPlayerIdentity q = (Queries.GetPlayerIdentity) query;
            PlayerIdentity identity = selectPlayerByUuid(table, q.uuid());
            return pageSingle(identity);
        }
        if (query instanceof Queries.GetPlayerIdentityByName) {
            Queries.GetPlayerIdentityByName q = (Queries.GetPlayerIdentityByName) query;
            PlayerIdentity identity = selectPlayerByName(table, q.name());
            return pageSingle(identity);
        }
        if (query instanceof Queries.ListPlayersByNamePrefix) {
            Queries.ListPlayersByNamePrefix q = (Queries.ListPlayersByNamePrefix) query;
            return listPlayersByPrefix(table, q.lowerPrefix(), q.limit());
        }
        if (query instanceof Queries.GetSetting) {
            Queries.GetSetting q = (Queries.GetSetting) query;
            SettingRecord record = selectSetting(table, q.scope(), q.scopeKey(), q.key());
            return pageSingle(record);
        }
        return Page.empty();
    }

    private static <T> Page<T> pageSingle(T value) {
        return value == null ? Page.<T>empty() : new Page<T>(Collections.singletonList(value), null);
    }

    private void writeAny(String table, Object value) throws BackendException {
        if (value == null) return;
        if (value instanceof ViolationEvent) { writeViolation(table, (ViolationEvent) value); return; }
        if (value instanceof ViolationRecord) { writeViolation(table, (ViolationRecord) value); return; }
        if (value instanceof SessionEvent) { writeSession(table, (SessionEvent) value); return; }
        if (value instanceof SessionRecord) { writeSession(table, (SessionRecord) value); return; }
        if (value instanceof CheckCatalogEvent) { writeCheck(table, (CheckCatalogEvent) value); return; }
        if (value instanceof CheckCatalogRecord) { writeCheck(table, (CheckCatalogRecord) value); return; }
        if (value instanceof PlayerIdentityEvent) { writePlayer(table, (PlayerIdentityEvent) value); return; }
        if (value instanceof PlayerIdentity) { writePlayer(table, (PlayerIdentity) value); return; }
        if (value instanceof SettingEvent) { writeSetting(table, (SettingEvent) value); return; }
        if (value instanceof SettingRecord) { writeSetting(table, (SettingRecord) value); return; }
        if (value instanceof ServerStartupEvent) { writeStartup(table, (ServerStartupEvent) value); return; }
        if (value instanceof ServerStartupRecord) { writeStartup(table, (ServerStartupRecord) value); return; }
        writePayloadFallback(table, value);
    }

    private void ensureGeneric(String table) throws BackendException {
        try (Statement s = connection().createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (" +
                    "seq INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "record_id TEXT," +
                    "kind_name TEXT," +
                    "event_class TEXT," +
                    "created_epoch_ms INTEGER," +
                    "payload TEXT" +
                    ")");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS " + table + "_created_idx ON " + table + " (created_epoch_ms)");
        } catch (SQLException e) {
            throw new BackendException("failed to ensure generic SQLite v2 table " + table, e);
        }
    }

    private void ensureViolations(String t) throws BackendException {
        add(t, "id TEXT"); add(t, "session_id TEXT"); add(t, "player_uuid TEXT"); add(t, "check_id INTEGER");
        add(t, "vl REAL"); add(t, "occurred_epoch_ms INTEGER"); add(t, "verbose_data BLOB"); add(t, "verbose_format INTEGER");
        index(t, "id", true, "id"); index(t, "session", false, "session_id", "occurred_epoch_ms", "seq");
        index(t, "player", false, "player_uuid", "occurred_epoch_ms", "seq"); index(t, "check", false, "check_id");
    }

    private void ensureSessions(String t) throws BackendException {
        add(t, "session_id TEXT"); add(t, "player_uuid TEXT"); add(t, "server_name TEXT");
        add(t, "started_epoch_ms INTEGER"); add(t, "last_activity_epoch_ms INTEGER"); add(t, "closed_at_epoch_ms INTEGER");
        add(t, "grim_version TEXT"); add(t, "client_brand TEXT"); add(t, "client_version INTEGER");
        add(t, "server_version_string TEXT"); add(t, "instance_id TEXT"); add(t, "startup_id TEXT");
        index(t, "session_id", true, "session_id"); index(t, "player_started", false, "player_uuid", "started_epoch_ms", "session_id");
    }

    private void ensureChecks(String t) throws BackendException {
        add(t, "stable_key TEXT"); add(t, "check_id INTEGER"); add(t, "display TEXT"); add(t, "description TEXT");
        add(t, "introduced_version TEXT"); add(t, "introduced_at INTEGER");
        index(t, "stable_key", true, "stable_key"); index(t, "check_id", true, "check_id");
    }

    private void ensurePlayers(String t) throws BackendException {
        add(t, "uuid TEXT"); add(t, "current_name TEXT"); add(t, "lower_name TEXT");
        add(t, "first_seen_epoch_ms INTEGER"); add(t, "last_seen_epoch_ms INTEGER");
        index(t, "uuid", true, "uuid"); index(t, "lower_name", false, "lower_name");
    }

    private void ensureSettings(String t) throws BackendException {
        add(t, "scope TEXT"); add(t, "scope_key TEXT"); add(t, "setting_key TEXT"); add(t, "value_blob BLOB"); add(t, "updated_epoch_ms INTEGER");
        index(t, "setting_unique", true, "scope", "scope_key", "setting_key");
    }

    private void ensureStartups(String t) throws BackendException {
        add(t, "startup_id TEXT"); add(t, "instance_id TEXT"); add(t, "server_name TEXT"); add(t, "grim_version TEXT");
        add(t, "server_version_string TEXT"); add(t, "hostname TEXT"); add(t, "started_epoch_ms INTEGER");
        add(t, "last_heartbeat_epoch_ms INTEGER"); add(t, "closed_at_epoch_ms INTEGER"); add(t, "close_reason TEXT"); add(t, "verbose_manifest BLOB");
        index(t, "startup_id", true, "startup_id"); index(t, "instance_started", false, "instance_id", "started_epoch_ms");
    }

    private void add(String table, String definition) throws BackendException {
        try (Statement s = connection().createStatement()) {
            s.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + definition);
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.toLowerCase().contains("duplicate column")) return;
            throw new BackendException("failed to add SQLite column " + definition + " to " + table, e);
        }
    }

    private void index(String table, String suffix, boolean unique, String... columns) throws BackendException {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE ");
        if (unique) sql.append("UNIQUE ");
        sql.append("INDEX IF NOT EXISTS ").append(table).append('_').append(suffix).append("_idx ON ").append(table).append(" (");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) sql.append(',');
            sql.append(columns[i]);
        }
        sql.append(')');
        try (Statement s = connection().createStatement()) { s.executeUpdate(sql.toString()); }
        catch (SQLException e) { throw new BackendException("failed to create SQLite index on " + table, e); }
    }

    private void writeViolation(String t, ViolationEvent e) throws BackendException {
        UUID id = e.id() == null ? UUID.randomUUID() : e.id();
        writeViolation0(t, id, e.sessionId(), e.playerUuid(), e.checkId(), e.vl(),
                e.occurredEpochMs() <= 0L ? System.currentTimeMillis() : e.occurredEpochMs(),
                e.verboseData(), e.verboseFormat());
    }

    private void writeViolation(String t, ViolationRecord r) throws BackendException {
        writeViolation0(t, r.id(), r.sessionId(), r.playerUuid(), r.checkId(), r.vl(), r.occurredEpochMs(), r.verboseData(), r.verboseFormat());
    }

    private void writeViolation0(String t, UUID id, UUID sid, UUID player, int checkId, double vl, long occurred, byte[] verbose, VerboseFormat format) throws BackendException {
        String sql = "INSERT OR REPLACE INTO " + t + " (record_id,kind_name,event_class,created_epoch_ms,payload,id,session_id,player_uuid,check_id,vl,occurred_epoch_ms,verbose_data,verbose_format) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, str(id)); ps.setString(2, "violations"); ps.setString(3, ViolationEvent.class.getName()); ps.setLong(4, System.currentTimeMillis());
            ps.setString(5, verbose == null ? null : new String(verbose, StandardCharsets.UTF_8));
            ps.setString(6, str(id)); ps.setString(7, str(sid)); ps.setString(8, str(player)); ps.setInt(9, checkId); ps.setDouble(10, vl); ps.setLong(11, occurred);
            ps.setBytes(12, verbose); ps.setInt(13, format == null ? VerboseFormat.TEXT.code() : format.code()); ps.executeUpdate();
        } catch (SQLException e) { throw new BackendException("failed to write violation", e); }
    }

    private void writeSession(String t, SessionEvent e) throws BackendException {
        writeSession0(t, e.sessionId(), e.playerUuid(), e.serverName(), e.startedEpochMs(), e.lastActivityEpochMs(), e.closedAtEpochMs(),
                e.grimVersion(), e.clientBrand(), e.clientVersion(), e.serverVersionString(), e.instanceId(), e.startupId());
    }

    private void writeSession(String t, SessionRecord r) throws BackendException {
        writeSession0(t, r.sessionId(), r.playerUuid(), r.serverName(), r.startedEpochMs(), r.lastActivityEpochMs(), r.closedAtEpochMs(),
                r.grimVersion(), r.clientBrand(), r.clientVersion(), r.serverVersionString(), r.instanceId(), r.startupId());
    }

    private void writeSession0(String t, UUID sid, UUID player, String server, long started, long last, long closed, String grimVersion, String brand, int clientVersion, String serverVersion, UUID instance, UUID startup) throws BackendException {
        String sql = "INSERT OR REPLACE INTO " + t + " (record_id,kind_name,event_class,created_epoch_ms,payload,session_id,player_uuid,server_name,started_epoch_ms,last_activity_epoch_ms,closed_at_epoch_ms,grim_version,client_brand,client_version,server_version_string,instance_id,startup_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, str(sid)); ps.setString(2, "sessions"); ps.setString(3, SessionEvent.class.getName()); ps.setLong(4, System.currentTimeMillis()); ps.setString(5, null);
            ps.setString(6, str(sid)); ps.setString(7, str(player)); ps.setString(8, server); ps.setLong(9, started); ps.setLong(10, last); ps.setLong(11, closed);
            ps.setString(12, grimVersion); ps.setString(13, brand); ps.setInt(14, clientVersion); ps.setString(15, serverVersion); ps.setString(16, str(instance)); ps.setString(17, str(startup)); ps.executeUpdate();
        } catch (SQLException e) { throw new BackendException("failed to write session", e); }
    }

    private void writeCheck(String t, CheckCatalogEvent e) throws BackendException { writeCheck0(t, e.stableKey(), e.checkId(), e.display(), e.description(), e.introducedVersion(), e.introducedAt()); }
    private void writeCheck(String t, CheckCatalogRecord r) throws BackendException { writeCheck0(t, r.stableKey(), r.checkId(), r.display(), r.description(), r.introducedVersion(), r.introducedAt()); }
    private void writeCheck0(String t, String stable, int id, String display, String desc, String version, long at) throws BackendException {
        String sql = "INSERT OR REPLACE INTO " + t + " (record_id,kind_name,event_class,created_epoch_ms,payload,stable_key,check_id,display,description,introduced_version,introduced_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, stable); ps.setString(2, "checks"); ps.setString(3, CheckCatalogEvent.class.getName()); ps.setLong(4, System.currentTimeMillis()); ps.setString(5, display);
            ps.setString(6, stable); ps.setInt(7, id); ps.setString(8, display); ps.setString(9, desc); ps.setString(10, version); ps.setLong(11, at); ps.executeUpdate();
        } catch (SQLException e) { throw new BackendException("failed to write check catalog", e); }
    }

    private void writePlayer(String t, PlayerIdentityEvent e) throws BackendException { writePlayer0(t, e.uuid(), e.currentName(), e.firstSeenEpochMs(), e.lastSeenEpochMs()); }
    private void writePlayer(String t, PlayerIdentity p) throws BackendException { writePlayer0(t, p.uuid(), p.currentName(), p.firstSeenEpochMs(), p.lastSeenEpochMs()); }
    private void writePlayer0(String t, UUID uuid, String name, long first, long last) throws BackendException {
        PlayerIdentity old = selectPlayerByUuid(t, uuid);
        if (old != null) { if (first <= 0 || (old.firstSeenEpochMs() > 0 && old.firstSeenEpochMs() < first)) first = old.firstSeenEpochMs(); if (last < old.lastSeenEpochMs()) last = old.lastSeenEpochMs(); }
        String sql = "INSERT OR REPLACE INTO " + t + " (record_id,kind_name,event_class,created_epoch_ms,payload,uuid,current_name,lower_name,first_seen_epoch_ms,last_seen_epoch_ms) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, str(uuid)); ps.setString(2, "players"); ps.setString(3, PlayerIdentityEvent.class.getName()); ps.setLong(4, System.currentTimeMillis()); ps.setString(5, name);
            ps.setString(6, str(uuid)); ps.setString(7, name); ps.setString(8, name == null ? null : name.toLowerCase(java.util.Locale.ROOT)); ps.setLong(9, first); ps.setLong(10, last); ps.executeUpdate();
        } catch (SQLException e) { throw new BackendException("failed to write player identity", e); }
    }

    private void writeSetting(String t, SettingEvent e) throws BackendException { writeSetting0(t, e.scope(), e.scopeKey(), e.key(), e.value(), e.updatedEpochMs()); }
    private void writeSetting(String t, SettingRecord r) throws BackendException { writeSetting0(t, r.scope(), r.scopeKey(), r.key(), r.value(), r.updatedEpochMs()); }
    private void writeSetting0(String t, SettingScope scope, String scopeKey, String key, byte[] value, long updated) throws BackendException {
        String sql = "INSERT OR REPLACE INTO " + t + " (record_id,kind_name,event_class,created_epoch_ms,payload,scope,scope_key,setting_key,value_blob,updated_epoch_ms) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, scope + ":" + scopeKey + ":" + key); ps.setString(2, "settings"); ps.setString(3, SettingEvent.class.getName()); ps.setLong(4, System.currentTimeMillis()); ps.setString(5, null);
            ps.setString(6, scope == null ? null : scope.name()); ps.setString(7, scopeKey); ps.setString(8, key); ps.setBytes(9, value); ps.setLong(10, updated); ps.executeUpdate();
        } catch (SQLException e) { throw new BackendException("failed to write setting", e); }
    }

    private void writeStartup(String t, ServerStartupEvent e) throws BackendException { writeStartup0(t, e.startupId(), e.instanceId(), e.serverName(), e.grimVersion(), e.serverVersionString(), e.hostname(), e.startedEpochMs(), e.lastHeartbeatEpochMs(), e.closedAtEpochMs(), e.closeReason(), e.verboseManifest()); }
    private void writeStartup(String t, ServerStartupRecord r) throws BackendException { writeStartup0(t, r.startupId(), r.instanceId(), r.serverName(), r.grimVersion(), r.serverVersionString(), r.hostname(), r.startedEpochMs(), r.lastHeartbeatEpochMs(), r.closedAtEpochMs(), r.closeReason(), r.verboseManifest()); }
    private void writeStartup0(String t, UUID startup, UUID instance, String serverName, String grimVersion, String serverVersion, String host, long started, long heartbeat, long closed, String reason, byte[] manifest) throws BackendException {
        String sql = "INSERT OR REPLACE INTO " + t + " (record_id,kind_name,event_class,created_epoch_ms,payload,startup_id,instance_id,server_name,grim_version,server_version_string,hostname,started_epoch_ms,last_heartbeat_epoch_ms,closed_at_epoch_ms,close_reason,verbose_manifest) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, str(startup)); ps.setString(2, "server-startups"); ps.setString(3, ServerStartupEvent.class.getName()); ps.setLong(4, System.currentTimeMillis()); ps.setString(5, null);
            ps.setString(6, str(startup)); ps.setString(7, str(instance)); ps.setString(8, serverName); ps.setString(9, grimVersion); ps.setString(10, serverVersion); ps.setString(11, host); ps.setLong(12, started); ps.setLong(13, heartbeat); ps.setLong(14, closed); ps.setString(15, reason); ps.setBytes(16, manifest); ps.executeUpdate();
        } catch (SQLException e) { throw new BackendException("failed to write startup", e); }
    }

    private void writePayloadFallback(String t, Object value) throws BackendException {
        try (PreparedStatement ps = connection().prepareStatement("INSERT INTO " + t + " (record_id,kind_name,event_class,created_epoch_ms,payload) VALUES (?,?,?,?,?)")) {
            ps.setString(1, null); ps.setString(2, "unknown"); ps.setString(3, value.getClass().getName()); ps.setLong(4, System.currentTimeMillis()); ps.setString(5, String.valueOf(value)); ps.executeUpdate();
        } catch (SQLException e) { throw new BackendException("failed to write fallback payload", e); }
    }

    private Object getById(String t, Category<?> category, Object id) throws BackendException {
        if (category == Categories.SESSION) return Optional.ofNullable(selectSessionById(t, uuid(id)));
        if (category == Categories.SERVER_STARTUP) return Optional.ofNullable(selectStartupById(t, uuid(id)));
        if (category == Categories.PLAYER_IDENTITY) return Optional.ofNullable(selectPlayerByUuid(t, uuid(id)));
        if (category == Categories.CHECK_CATALOG) return Optional.ofNullable(selectCheckById(t, intValue(id)));
        if (category == Categories.SETTING) return Optional.empty();
        return Optional.empty();
    }

    private Object findByIndex(String t, Category<?> category, String index, Object key, Cursor cursor, int pageSize) throws BackendException {
        if (category == Categories.SESSION && is(index, "player", "player_uuid")) return listSessionsByPlayer(t, uuid(key), cursor, pageSize);
        if (category == Categories.PLAYER_IDENTITY && is(index, "name", "lower_name")) return pageSingle(selectPlayerByName(t, String.valueOf(key)));
        return Page.empty();
    }

    private long countByIndex(String t, Category<?> category, String index, Object key) throws BackendException {
        if (category == Categories.SESSION && is(index, "player", "player_uuid")) return countWhere(t, "player_uuid", str(key));
        if (category == Categories.VIOLATION && is(index, "session", "session_id")) return countWhere(t, "session_id", str(key));
        return 0L;
    }

    private Page<?> pagePartition(String t, Category<?> category, String partition, Object key, Cursor cursor, int pageSize) throws BackendException {
        if (category == Categories.VIOLATION && is(partition, "session", "session_id")) return listViolationsInSession(t, uuid(key), cursor, pageSize);
        if (category == Categories.SESSION && is(partition, "player", "player_uuid")) return listSessionsByPlayer(t, uuid(key), cursor, pageSize);
        return Page.empty();
    }

    private long countPartition(String t, String partition, Object key) throws BackendException {
        if (is(partition, "session", "session_id")) return countWhere(t, "session_id", str(key));
        if (is(partition, "player", "player_uuid")) return countWhere(t, "player_uuid", str(key));
        return 0L;
    }

    private long countDistinct(String t, String partition, Object key, String field) throws BackendException {
        String where = is(partition, "session", "session_id") ? "session_id" : is(partition, "player", "player_uuid") ? "player_uuid" : null;
        String column = is(field, "check", "check_id") ? "check_id" : field;
        if (where == null || column == null) return 0L;
        try (PreparedStatement ps = connection().prepareStatement("SELECT COUNT(DISTINCT " + column + ") FROM " + t + " WHERE " + where + "=?")) {
            ps.setString(1, str(key));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0L; }
        } catch (SQLException e) { throw new BackendException("failed to count distinct", e); }
    }

    private Map<Object, Long> countMany(String t, String partition, Iterable<?> keys) throws BackendException {
        Map<Object, Long> out = new LinkedHashMap<Object, Long>();
        for (Object key : keys) out.put(key, countPartition(t, partition, key));
        return out;
    }

    private void deleteOlderThan(String t, long cutoff) throws BackendException { executeUpdate("DELETE FROM " + t + " WHERE occurred_epoch_ms>0 AND occurred_epoch_ms<?", cutoff); }
    private void deletePartition(String t, String partition, Object key) throws BackendException { if (is(partition, "player", "player_uuid")) executeUpdate("DELETE FROM " + t + " WHERE player_uuid=?", str(key)); else if (is(partition, "session", "session_id")) executeUpdate("DELETE FROM " + t + " WHERE session_id=?", str(key)); }

    private Page<SessionRecord> listSessionsByPlayer(String t, UUID player, Cursor cursor, int pageSize) throws BackendException {
        int offset = offset(cursor); int limit = saneLimit(pageSize);
        List<SessionRecord> items = new ArrayList<SessionRecord>();
        String sql = "SELECT * FROM " + t + " WHERE player_uuid=? ORDER BY started_epoch_ms DESC, session_id DESC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, str(player)); ps.setInt(2, limit + 1); ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) items.add(readSession(rs)); }
        } catch (SQLException e) { throw new BackendException("failed to list sessions", e); }
        return page(items, offset, limit);
    }

    private Page<ViolationRecord> listViolationsInSession(String t, UUID session, Cursor cursor, int pageSize) throws BackendException {
        int offset = offset(cursor); int limit = saneLimit(pageSize);
        List<ViolationRecord> items = new ArrayList<ViolationRecord>();
        String sql = "SELECT * FROM " + t + " WHERE session_id=? ORDER BY occurred_epoch_ms ASC, seq ASC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, str(session)); ps.setInt(2, limit + 1); ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) items.add(readViolation(rs)); }
        } catch (SQLException e) { throw new BackendException("failed to list violations", e); }
        return page(items, offset, limit);
    }

    private <T> Page<T> page(List<T> items, int offset, int limit) {
        Cursor next = null;
        if (items.size() > limit) { items = new ArrayList<T>(items.subList(0, limit)); next = new Cursor(String.valueOf(offset + limit)); }
        return new Page<T>(items, next);
    }

    private ServerStartupRecord selectStartupById(String t, UUID startupId) throws BackendException {
        try (PreparedStatement ps = connection().prepareStatement("SELECT * FROM " + t + " WHERE startup_id=?")) {
            ps.setString(1, str(startupId));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readStartup(rs) : null;
            }
        } catch (SQLException e) {
            throw new BackendException("failed to get server startup", e);
        }
    }

    private SessionRecord selectSessionById(String t, UUID sid) throws BackendException {
        try (PreparedStatement ps = connection().prepareStatement("SELECT * FROM " + t + " WHERE session_id=?")) {
            ps.setString(1, str(sid)); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? readSession(rs) : null; }
        } catch (SQLException e) { throw new BackendException("failed to get session", e); }
    }

    private PlayerIdentity selectPlayerByUuid(String t, UUID uuid) throws BackendException {
        try (PreparedStatement ps = connection().prepareStatement("SELECT * FROM " + t + " WHERE uuid=?")) {
            ps.setString(1, str(uuid)); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? readPlayer(rs) : null; }
        } catch (SQLException e) { throw new BackendException("failed to get player", e); }
    }

    private PlayerIdentity selectPlayerByName(String t, String name) throws BackendException {
        try (PreparedStatement ps = connection().prepareStatement("SELECT * FROM " + t + " WHERE lower_name=? ORDER BY last_seen_epoch_ms DESC LIMIT 1")) {
            ps.setString(1, name == null ? null : name.toLowerCase(java.util.Locale.ROOT)); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? readPlayer(rs) : null; }
        } catch (SQLException e) { throw new BackendException("failed to get player by name", e); }
    }

    private Page<PlayerIdentity> listPlayersByPrefix(String t, String prefix, int limit) throws BackendException {
        List<PlayerIdentity> out = new ArrayList<PlayerIdentity>();
        try (PreparedStatement ps = connection().prepareStatement("SELECT * FROM " + t + " WHERE lower_name LIKE ? ORDER BY last_seen_epoch_ms DESC LIMIT ?")) {
            ps.setString(1, (prefix == null ? "" : prefix.toLowerCase(java.util.Locale.ROOT)) + "%"); ps.setInt(2, saneLimit(limit));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(readPlayer(rs)); }
        } catch (SQLException e) { throw new BackendException("failed to list players", e); }
        return new Page<PlayerIdentity>(out, null);
    }

    private SettingRecord selectSetting(String t, SettingScope scope, String scopeKey, String key) throws BackendException {
        try (PreparedStatement ps = connection().prepareStatement("SELECT * FROM " + t + " WHERE scope=? AND scope_key=? AND setting_key=?")) {
            ps.setString(1, scope == null ? null : scope.name()); ps.setString(2, scopeKey); ps.setString(3, key);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? readSetting(rs) : null; }
        } catch (SQLException e) { throw new BackendException("failed to get setting", e); }
    }

    private CheckCatalogRecord selectCheckById(String t, int id) throws BackendException {
        try (PreparedStatement ps = connection().prepareStatement("SELECT * FROM " + t + " WHERE check_id=?")) {
            ps.setInt(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? readCheck(rs) : null; }
        } catch (SQLException e) { throw new BackendException("failed to get check", e); }
    }

    private long countWhere(String t, String column, String value) throws BackendException {
        try (PreparedStatement ps = connection().prepareStatement("SELECT COUNT(*) FROM " + t + " WHERE " + column + "=?")) {
            ps.setString(1, value); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0L; }
        } catch (SQLException e) { throw new BackendException("failed to count rows", e); }
    }

    private void executeUpdate(String sql, Object arg) throws BackendException {
        try (PreparedStatement ps = connection().prepareStatement(sql)) { bind(ps, 1, arg); ps.executeUpdate(); }
        catch (SQLException e) { throw new BackendException("failed to execute update", e); }
    }

    private SessionRecord readSession(ResultSet rs) throws SQLException {
        return new SessionRecord(uuid(rs.getString("session_id")), uuid(rs.getString("player_uuid")), rs.getString("server_name"),
                rs.getLong("started_epoch_ms"), rs.getLong("last_activity_epoch_ms"), rs.getLong("closed_at_epoch_ms"),
                rs.getString("grim_version"), rs.getString("client_brand"), rs.getInt("client_version"), rs.getString("server_version_string"),
                uuid(rs.getString("instance_id")), uuid(rs.getString("startup_id")), Collections.emptyList());
    }

    private ViolationRecord readViolation(ResultSet rs) throws SQLException {
        return new ViolationRecord(uuid(rs.getString("id")), uuid(rs.getString("session_id")), uuid(rs.getString("player_uuid")),
                rs.getInt("check_id"), rs.getDouble("vl"), rs.getLong("occurred_epoch_ms"), rs.getBytes("verbose_data"), VerboseFormat.fromCode(rs.getInt("verbose_format")));
    }

    private PlayerIdentity readPlayer(ResultSet rs) throws SQLException { return new PlayerIdentity(uuid(rs.getString("uuid")), rs.getString("current_name"), rs.getLong("first_seen_epoch_ms"), rs.getLong("last_seen_epoch_ms")); }
    private SettingRecord readSetting(ResultSet rs) throws SQLException { return new SettingRecord(SettingScope.valueOf(rs.getString("scope")), rs.getString("scope_key"), rs.getString("setting_key"), rs.getBytes("value_blob"), rs.getLong("updated_epoch_ms")); }
    private CheckCatalogRecord readCheck(ResultSet rs) throws SQLException { return new CheckCatalogRecord(rs.getString("stable_key"), rs.getInt("check_id"), rs.getString("display"), rs.getString("description"), rs.getString("introduced_version"), rs.getLong("introduced_at")); }

    private ServerStartupRecord readStartup(ResultSet rs) throws SQLException {
        return new ServerStartupRecord(
                uuid(rs.getString("startup_id")),
                uuid(rs.getString("instance_id")),
                rs.getString("server_name"),
                rs.getString("grim_version"),
                rs.getString("server_version_string"),
                rs.getString("hostname"),
                rs.getLong("started_epoch_ms"),
                rs.getLong("last_heartbeat_epoch_ms"),
                rs.getLong("closed_at_epoch_ms"),
                rs.getString("close_reason"),
                rs.getBytes("verbose_manifest"));
    }

    private Connection connection() throws BackendException { return connections.get(); }
    private static String table(StoreId id, DataKind<?, ?> kind) { return SqliteBackendV2.tableName(id, kind); }
    private static boolean is(String value, String... names) { if (value == null) return false; for (String n : names) if (value.equalsIgnoreCase(n)) return true; return false; }
    private static String str(Object v) { return v == null ? null : String.valueOf(v); }
    private static UUID uuid(Object v) { if (v == null) return null; if (v instanceof UUID) return (UUID) v; String s = String.valueOf(v); return s.isEmpty() || "null".equals(s) ? null : UUID.fromString(s); }
    private static int intValue(Object v) { return v instanceof Number ? ((Number) v).intValue() : Integer.parseInt(String.valueOf(v)); }
    private static int offset(Cursor cursor) { if (cursor == null || cursor.token() == null) return 0; String t = cursor.token(); if (t.startsWith("offset:")) t = t.substring(7); try { return Math.max(0, Integer.parseInt(t)); } catch (NumberFormatException e) { return 0; } }
    private static int saneLimit(int pageSize) { if (pageSize <= 0) return 20; return Math.min(pageSize, 1000); }
    private static void bind(PreparedStatement ps, int i, Object v) throws SQLException { if (v instanceof Number) ps.setLong(i, ((Number) v).longValue()); else ps.setString(i, str(v)); }
}
