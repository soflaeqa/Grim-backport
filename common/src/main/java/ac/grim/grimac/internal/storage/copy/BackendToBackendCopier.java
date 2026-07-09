package ac.grim.grimac.internal.storage.copy;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongConsumer;

/**
 * One-shot v1-to-v1 copier. Copies the check catalog, then streams SESSION +
 * VIOLATION + PLAYER_IDENTITY records from a source {@link Backend} into a
 * destination via its {@link Backend#bulkImport} escape hatch.
 */
@ApiStatus.Internal
public final class BackendToBackendCopier {

    public static final class Result {
        private final long sessions;
        private final long violations;
        private final long players;
        private final long elapsedMs;

        public Result(long sessions, long violations, long players, long elapsedMs) {
            this.sessions = sessions;
            this.violations = violations;
            this.players = players;
            this.elapsedMs = elapsedMs;
        }

        public long sessions() {
            return sessions;
        }

        public long violations() {
            return violations;
        }

        public long players() {
            return players;
        }

        public long elapsedMs() {
            return elapsedMs;
        }
    }

    private final Backend src;
    private final Backend dst;
    private final int pageSize;

    public BackendToBackendCopier(@NotNull Backend src, @NotNull Backend dst) {
        this(src, dst, 500);
    }

    public BackendToBackendCopier(@NotNull Backend src, @NotNull Backend dst, int pageSize) {
        if (src == dst) throw new IllegalArgumentException("source and destination cannot be the same backend");
        this.src = src;
        this.dst = dst;
        this.pageSize = Math.max(50, pageSize);
    }

    /**
     * Run a full copy. {@code onBatch} fires after each flushed batch with the
     * running total of violations copied so callers can paint progress lines.
     */
    public Result run(LongConsumer onBatch) throws BackendException {
        long start = System.currentTimeMillis();
        copyCheckCatalog();
        Set<UUID> players = collectPlayersBySessionWalk();

        List<PlayerIdentity> identities = new ArrayList<PlayerIdentity>(players.size());
        for (UUID u : players) {
            Page<PlayerIdentity> p = src.read(Categories.PLAYER_IDENTITY, Queries.getPlayerIdentity(u));
            if (!p.items().isEmpty()) identities.add(p.items().get(0));
        }
        if (!identities.isEmpty()) dst.bulkImport(Categories.PLAYER_IDENTITY, identities);

        long copiedSessions = 0;
        long copiedViolations = 0;
        List<SessionRecord> sessionBatch = new ArrayList<SessionRecord>(pageSize);
        List<ViolationRecord> violationBatch = new ArrayList<ViolationRecord>(pageSize);

        for (UUID player : players) {
            Cursor sCursor = null;
            while (true) {
                Page<SessionRecord> sp = src.read(Categories.SESSION,
                        Queries.listSessionsByPlayer(player, pageSize, sCursor));
                for (SessionRecord s : sp.items()) {
                    sessionBatch.add(s);
                    if (sessionBatch.size() >= pageSize) {
                        dst.bulkImport(Categories.SESSION, sessionBatch);
                        copiedSessions += sessionBatch.size();
                        sessionBatch.clear();
                    }
                    Cursor vCursor = null;
                    while (true) {
                        Page<ViolationRecord> vp = src.read(Categories.VIOLATION,
                                Queries.listViolationsInSession(s.sessionId(), pageSize, vCursor));
                        for (ViolationRecord v : vp.items()) {
                            violationBatch.add(v);
                            if (violationBatch.size() >= pageSize) {
                                dst.bulkImport(Categories.VIOLATION, violationBatch);
                                copiedViolations += violationBatch.size();
                                violationBatch.clear();
                                onBatch.accept(copiedViolations);
                            }
                        }
                        vCursor = vp.nextCursor();
                        if (vCursor == null) break;
                    }
                }
                sCursor = sp.nextCursor();
                if (sCursor == null) break;
            }
        }
        if (!sessionBatch.isEmpty()) {
            dst.bulkImport(Categories.SESSION, sessionBatch);
            copiedSessions += sessionBatch.size();
        }
        if (!violationBatch.isEmpty()) {
            dst.bulkImport(Categories.VIOLATION, violationBatch);
            copiedViolations += violationBatch.size();
            onBatch.accept(copiedViolations);
        }
        return new Result(copiedSessions, copiedViolations, identities.size(),
                System.currentTimeMillis() - start);
    }

    private void copyCheckCatalog() throws BackendException {
        for (CheckCatalogRow row : src.checkCatalog().loadAll()) {
            dst.checkCatalog().upsert(row);
        }
    }

    private Set<UUID> collectPlayersBySessionWalk() throws BackendException {
        Set<UUID> out = new LinkedHashSet<UUID>();
        if (src instanceof ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackend) {
            ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackend sq =
                    (ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackend) src;
            try (java.sql.Connection c = java.sql.DriverManager.getConnection(sq.jdbcUrl());
                 java.sql.Statement s = c.createStatement();
                 java.sql.ResultSet rs = s.executeQuery("SELECT uuid FROM " + sq.tableNames().players())) {
                while (rs.next()) {
                    byte[] bytes = rs.getBytes(1);
                    if (bytes != null && bytes.length == 16) {
                        java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(bytes);
                        out.add(new UUID(bb.getLong(), bb.getLong()));
                    }
                }
            } catch (java.sql.SQLException e) {
                throw new BackendException("failed to enumerate source players", e);
            }
            return out;
        }
        if (src instanceof ac.grim.grimac.internal.storage.backend.memory.InMemoryBackend) {
            ac.grim.grimac.internal.storage.backend.memory.InMemoryBackend mem =
                    (ac.grim.grimac.internal.storage.backend.memory.InMemoryBackend) src;
            out.addAll(mem.knownPlayerUuids());
            return out;
        }
        throw new BackendException("backend " + src.id() + " does not expose a player-enumeration hatch; cannot use as copy source");
    }

    public void dropSource() throws BackendException {
        if (src instanceof ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackend) {
            ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackend sq =
                    (ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackend) src;
            synchronized (sq.writeMutexForCopier()) {
                java.sql.Connection conn = sq.writeConnection();
                if (conn == null) throw new BackendException("source backend not initialised");
                boolean wasAutoCommit = true;
                try {
                    wasAutoCommit = conn.getAutoCommit();
                    if (wasAutoCommit) conn.setAutoCommit(false);
                    try (java.sql.Statement s = conn.createStatement()) {
                        ac.grim.grimac.api.storage.config.TableNames t = sq.tableNames();
                        s.executeUpdate("DELETE FROM " + t.violations());
                        s.executeUpdate("DELETE FROM " + t.sessions());
                        s.executeUpdate("DELETE FROM " + t.players());
                    }
                    conn.commit();
                } catch (java.sql.SQLException e) {
                    try { conn.rollback(); } catch (java.sql.SQLException ignore) {}
                    throw new BackendException("failed to drop source data", e);
                } finally {
                    try { if (wasAutoCommit) conn.setAutoCommit(true); }
                    catch (java.sql.SQLException ignore) {}
                }
            }
            return;
        }
        if (src instanceof ac.grim.grimac.internal.storage.backend.memory.InMemoryBackend) {
            ac.grim.grimac.internal.storage.backend.memory.InMemoryBackend mem =
                    (ac.grim.grimac.internal.storage.backend.memory.InMemoryBackend) src;
            mem.wipeAllForCopier();
            return;
        }
        throw new BackendException("backend " + src.id() + " does not support in-place wipe from the copier");
    }
}
