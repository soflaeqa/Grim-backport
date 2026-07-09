package ac.grim.grimac.internal.storage.backend.mongo;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.internal.storage.backend.NoopBackend;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public final class MongoBackendProvider implements BackendProvider {
    public static final String ID = "mongo";
    @Override public @NotNull String id() { return ID; }
    @Override public @NotNull Class<? extends BackendConfig> configType() { return MongoBackendConfig.class; }
    @Override public @NotNull BackendConfig readConfig(@NotNull BackendConfigSource src) {
        return new MongoBackendConfig(src.getString("connection-string", "mongodb://localhost:27017"),
                src.getString("database", "grim"), src.getInt("batch-flush-cap", 256),
                1, Collections.<String, Integer>emptyMap(), TableNames.readFrom(src));
    }
    @Override public @NotNull Backend create(@NotNull BackendConfig config) { return new NoopBackend(ID, config); }
}
