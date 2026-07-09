package ac.grim.grimac.internal.storage.backend;

import ac.grim.grimac.api.storage.backend.AdminAdapter;
import ac.grim.grimac.api.storage.backend.ApiVersion;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendContext;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.BackendV2;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.SearchAdapter;
import ac.grim.grimac.api.storage.backend.TxAdapter;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.kind.DataKind;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Java 8 no-op v2 backend used when the Java 17 grim-internal backend
 * implementations are excluded from the shaded Bukkit plugin.
 */
public class NoopBackendV2 implements BackendV2 {
    private final String id;
    private final BackendConfig config;

    public NoopBackendV2(String id, BackendConfig config) {
        this.id = id == null ? "noop" : id;
        this.config = config;
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public @NotNull ApiVersion apiVersion() {
        return ApiVersion.CURRENT;
    }

    @Override
    public @NotNull EnumSet<Capability> capabilities() {
        return EnumSet.allOf(Capability.class);
    }

    @Override
    public void init(@NotNull BackendContext ctx) throws BackendException {
    }

    @Override
    public void flush() throws BackendException {
    }

    @Override
    public void close() throws BackendException {
    }

    @Override
    public <K extends DataKind<?, ?>> @NotNull Optional<KindAdapter<K>> adapterFor(@NotNull K kind) {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<SearchAdapter> searchAdapter() {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<TxAdapter> txAdapter() {
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<AdminAdapter> adminAdapter() {
        return Optional.empty();
    }

    @Override
    public <X> @NotNull Optional<X> unwrap(@NotNull Class<X> type) {
        return Optional.empty();
    }

    public BackendConfig config() {
        return config;
    }
}
