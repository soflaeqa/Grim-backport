package ac.grim.grimac.internal.storage.backend.sqlite;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.internal.storage.backend.NoopBackend;
import org.jetbrains.annotations.NotNull;

public final class SqliteBackendProvider implements BackendProvider {
    public static final String ID = "sqlite";
    @Override public @NotNull String id() { return ID; }
    @Override public @NotNull Class<? extends BackendConfig> configType() { return SqliteBackendConfig.class; }
    @Override public @NotNull BackendConfig readConfig(@NotNull BackendConfigSource src) {
        return new SqliteBackendConfig(
                src.getString("path", "data/history.v1.db"),
                src.getString("journal-mode", "WAL"),
                src.getString("synchronous-mode", "NORMAL"),
                src.getInt("busy-timeout-ms", 5000),
                src.getInt("cache-pages", 10000),
                src.getInt("batch-flush-cap", 256),
                TableNames.readFrom(src));
    }
    @Override public @NotNull Backend create(@NotNull BackendConfig config) { return new NoopBackend(ID, config); }
}
