package ac.grim.grimac.internal.storage.backend.memory;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.internal.storage.backend.NoopBackend;
import org.jetbrains.annotations.NotNull;

public final class InMemoryBackendProvider implements BackendProvider {
    public static final String ID = "memory";
    @Override public @NotNull String id() { return ID; }
    @Override public @NotNull Class<? extends BackendConfig> configType() { return InMemoryBackendConfig.class; }
    @Override public @NotNull BackendConfig readConfig(@NotNull BackendConfigSource src) { return new InMemoryBackendConfig(); }
    @Override public @NotNull Backend create(@NotNull BackendConfig config) { return new NoopBackend(ID, config); }
}
