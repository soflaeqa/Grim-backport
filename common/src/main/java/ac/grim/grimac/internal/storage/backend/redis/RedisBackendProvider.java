package ac.grim.grimac.internal.storage.backend.redis;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.internal.storage.backend.NoopBackend;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public final class RedisBackendProvider implements BackendProvider {
    public static final String ID = "redis";
    @Override public @NotNull String id() { return ID; }
    @Override public @NotNull Class<? extends BackendConfig> configType() { return RedisBackendConfig.class; }
    @Override public @NotNull BackendConfig readConfig(@NotNull BackendConfigSource src) {
        String user = src.getString("user", "");
        String pw = src.getString("password", "");
        return new RedisBackendConfig(src.getString("host", "localhost"), src.getInt("port", 6379),
                src.getInt("database", 0), user.isEmpty() ? null : user, pw.isEmpty() ? null : pw,
                src.getString("key-prefix", ""), src.getInt("timeout-ms", 2000),
                src.getInt("batch-flush-cap", 256), src.getBoolean("warn-on-history", true),
                1, Collections.<String, Integer>emptyMap(), TableNames.readFrom(src));
    }
    @Override public @NotNull Backend create(@NotNull BackendConfig config) { return new NoopBackend(ID, config); }
}
