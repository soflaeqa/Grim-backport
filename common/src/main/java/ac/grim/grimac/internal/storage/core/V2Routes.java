package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.backend.BackendV2;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.api.storage.registry.StoreId;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * v2 routing table populated at startup: for each {@link Category} that
 * routes through the new {@link BackendV2} path, holds the destination
 * backend + the typed {@link KindAdapter} + the registered {@link StoreId}.
 * <p>
 * Java 8 replacement for the original version whose nested Route type was a record.
 */
@ApiStatus.Internal
public final class V2Routes {

    /**
     * Identity-keyed: we deliberately key by Category INSTANCE reference,
     * not by id-equality. Two distinct Category objects with the same id
     * are distinct routes.
     */
    private final @NotNull Map<Category<?>, Route<?>> routes;

    private V2Routes(@NotNull IdentityHashMap<Category<?>, Route<?>> routes) {
        this.routes = routes;
    }

    public static @NotNull Builder builder() { return new Builder(); }

    public static @NotNull V2Routes empty() {
        return new V2Routes(new IdentityHashMap<Category<?>, Route<?>>());
    }

    public boolean contains(@NotNull Category<?> category) {
        return routes.containsKey(category);
    }

    @SuppressWarnings("unchecked")
    public <K extends DataKind<?, ?>> @Nullable Route<K> routeFor(@NotNull Category<?> cat) {
        return (Route<K>) routes.get(cat);
    }

    public boolean isEmpty() { return routes.isEmpty(); }

    public static final class Route<K extends DataKind<?, ?>> {
        private final @NotNull StoreId storeId;
        private final @NotNull BackendV2 backend;
        private final @NotNull KindAdapter<K> adapter;
        private final @NotNull K kind;

        public Route(@NotNull StoreId storeId,
                     @NotNull BackendV2 backend,
                     @NotNull KindAdapter<K> adapter,
                     @NotNull K kind) {
            this.storeId = Objects.requireNonNull(storeId, "storeId");
            this.backend = Objects.requireNonNull(backend, "backend");
            this.adapter = Objects.requireNonNull(adapter, "adapter");
            this.kind = Objects.requireNonNull(kind, "kind");
        }

        public @NotNull StoreId storeId() { return storeId; }
        public @NotNull BackendV2 backend() { return backend; }
        public @NotNull KindAdapter<K> adapter() { return adapter; }
        public @NotNull K kind() { return kind; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Route)) return false;
            Route<?> route = (Route<?>) o;
            return storeId.equals(route.storeId)
                    && backend.equals(route.backend)
                    && adapter.equals(route.adapter)
                    && kind.equals(route.kind);
        }

        @Override
        public int hashCode() {
            return Objects.hash(storeId, backend, adapter, kind);
        }

        @Override
        public String toString() {
            return "Route[storeId=" + storeId
                    + ", backend=" + backend
                    + ", adapter=" + adapter
                    + ", kind=" + kind
                    + ']';
        }
    }

    public static final class Builder {
        private final Map<Category<?>, Route<?>> routes = new IdentityHashMap<Category<?>, Route<?>>();

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <E, R, K extends DataKind<E, R>> @NotNull Builder register(
                @NotNull Category<E> category,
                @NotNull StoreId storeId,
                @NotNull K kind,
                @NotNull BackendV2 backend) {
            if (routes.containsKey(category)) {
                Route<?> existing = routes.get(category);
                throw new IllegalStateException(
                    "duplicate v2 route registration for category " + category.id()
                        + " (existing: storeId=" + existing.storeId() + " kind=" + existing.kind().name()
                        + " backend=" + existing.backend().id() + "; attempted: storeId=" + storeId
                        + " kind=" + kind.name() + " backend=" + backend.id()
                        + ") — two backends claiming the same category is a wiring bug");
            }
            KindAdapter<K> adapter = (KindAdapter<K>) backend.adapterFor(kind).orElseThrow(
                () -> new IllegalArgumentException(
                    "backend " + backend.id() + " does not advertise an adapter for kind " + kind.name()));
            routes.put(category, new Route(storeId, backend, adapter, kind));
            return this;
        }

        public @NotNull V2Routes build() {
            IdentityHashMap<Category<?>, Route<?>> snapshot = new IdentityHashMap<Category<?>, Route<?>>(routes.size());
            snapshot.putAll(routes);
            return new V2Routes(snapshot);
        }
    }
}
