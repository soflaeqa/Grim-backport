package ac.grim.grimac.internal.storage.backend.sqlite.v2;

import ac.grim.grimac.api.storage.backend.BackendContext;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.instance.ServerOwnershipAdapter;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.internal.storage.backend.NoopBackendV2;
import ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackendConfig;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Optional;

/**
 * Java 8 port of the v2 SQLite backend surface used by this Grim backport.
 *
 * This is no longer the generic payload-only compatibility writer: it creates
 * typed Grim history tables for sessions, violations, checks, players,
 * settings and startups, and exposes a real ownership adapter.
 */
public final class SqliteBackendV2 extends NoopBackendV2 {
    private final SqliteBackendConfig config;
    private final Java8SqliteV2KindAdapter adapter;
    private final ServerOwnershipAdapter ownershipAdapter;
    private Connection connection;

    public SqliteBackendV2(SqliteBackendConfig config) {
        super("sqlite", config);
        this.config = config;
        this.adapter = new Java8SqliteV2KindAdapter(this::requireConnection);
        this.ownershipAdapter = new SqliteServerOwnershipAdapter(this::requireConnection);
    }

    @Override
    public void init(@NotNull BackendContext ctx) throws BackendException {
        super.init(ctx);
        try {
            File file = resolveSqliteFile(config.path(), ctx.dataDirectory());
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
                throw new BackendException("failed to create SQLite directory: " + parent);
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
            connection.setAutoCommit(true);

            executeQuietPragma("PRAGMA journal_mode=" + safePragma(config.journalMode()));
            executeQuietPragma("PRAGMA synchronous=" + safePragma(config.synchronousMode()));
            executeQuietPragma("PRAGMA busy_timeout=" + Math.max(0, config.busyTimeoutMs()));
            executeQuietPragma("PRAGMA cache_size=" + Math.max(0, config.cachePages()));
        } catch (SQLException e) {
            throw new BackendException("failed to open Java 8 SQLite v2 backend", e);
        }
    }

    @Override
    public void flush() throws BackendException {
        // Auto-commit mode. The write handlers do not buffer rows.
    }

    @Override
    public void close() throws BackendException {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException e) {
            throw new BackendException("failed to close Java 8 SQLite v2 backend", e);
        } finally {
            connection = null;
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <K extends DataKind<?, ?>> @NotNull Optional<KindAdapter<K>> adapterFor(@NotNull K kind) {
        return Optional.of((KindAdapter<K>) adapter);
    }

    @Override
    public @NotNull Optional<ServerOwnershipAdapter> ownershipAdapter() {
        return Optional.of(ownershipAdapter);
    }

    Connection requireConnection() throws BackendException {
        if (connection == null) {
            throw new BackendException("Java 8 SQLite v2 backend is not initialized");
        }
        return connection;
    }

    private void executeQuietPragma(String sql) {
        if (connection == null) return;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException ignored) {
            // Tuning pragma only. Do not fail startup if SQLite rejects it.
        }
    }

    private static File resolveSqliteFile(String configuredPath, Path dataDirectory) {
        String path = configuredPath == null || configuredPath.trim().isEmpty()
                ? "data/history.v1.db"
                : configuredPath.trim();
        File file = new File(path);
        if (file.isAbsolute()) return file;
        if (dataDirectory != null) return dataDirectory.resolve(path).toFile();
        return file;
    }

    static String sanitize(String value) {
        if (value == null || value.length() == 0) return "unknown";
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
                    (c >= '0' && c <= '9') || c == '_') {
                out.append(Character.toLowerCase(c));
            } else {
                out.append('_');
            }
        }
        if (out.length() == 0) return "unknown";
        if (out.charAt(0) >= '0' && out.charAt(0) <= '9') out.insert(0, '_');
        return out.toString();
    }

    static String tableName(ac.grim.grimac.api.storage.registry.StoreId id, DataKind<?, ?> kind) {
        return "grim_v2_" + sanitize(id.toString()) + "_" + sanitize(kind.name());
    }

    private static String safePragma(String value) {
        if (value == null || value.trim().isEmpty()) return "NORMAL";
        String upper = value.trim().toUpperCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(upper.length());
        for (int i = 0; i < upper.length(); i++) {
            char c = upper.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') out.append(c);
        }
        return out.length() == 0 ? "NORMAL" : out.toString();
    }
}
