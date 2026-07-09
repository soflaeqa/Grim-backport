package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.category.Category;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Java 8 source shadow for Grim's legacy/v1 category router.
 *
 * In the Java 8 backport DataStoreLifecycle still constructs this class before
 * installing the v2 routes:
 *
 *   new CategoryRouter(startupRouterMap)
 *
 * The class was missing from the final runtime jar, causing:
 *
 *   NoClassDefFoundError: ac/grim/grimac/internal/storage/core/CategoryRouter
 *
 * This implementation intentionally keeps a broad compatibility surface so both
 * old DataStoreImpl call sites and the v2 cutover bridge can resolve it.
 */
public final class CategoryRouter {
    private final Map<Category, Backend> routes;
    private final Map<String, Backend> routesById;

    @SuppressWarnings("unchecked")
    public CategoryRouter(@NotNull Map<?, ? extends Backend> routes) {
        Objects.requireNonNull(routes, "routes");

        IdentityHashMap<Category, Backend> byIdentity = new IdentityHashMap<Category, Backend>();
        LinkedHashMap<String, Backend> byId = new LinkedHashMap<String, Backend>();

        for (Map.Entry<?, ? extends Backend> entry : routes.entrySet()) {
            Object rawKey = entry.getKey();
            Backend backend = entry.getValue();

            if (rawKey instanceof Category && backend != null) {
                Category category = (Category) rawKey;
                byIdentity.put(category, backend);
                byId.put(category.id(), backend);
            }
        }

        this.routes = Collections.unmodifiableMap(byIdentity);
        this.routesById = Collections.unmodifiableMap(byId);
    }

    public static @NotNull CategoryRouter empty() {
        return new CategoryRouter(Collections.emptyMap());
    }

    public boolean isEmpty() {
        return routes.isEmpty();
    }

    public int size() {
        return routes.size();
    }

    public boolean contains(@NotNull Category category) {
        return routes.containsKey(category) || routesById.containsKey(category.id());
    }

    public boolean containsCategory(@NotNull Category category) {
        return contains(category);
    }

    public boolean isRouted(@NotNull Category category) {
        return contains(category);
    }

    public @NotNull Set<Category> categories() {
        return routes.keySet();
    }

    public @NotNull Map<Category, Backend> routes() {
        return routes;
    }

    public @NotNull Map<Category, Backend> asMap() {
        return routes;
    }

    public @NotNull Optional<Backend> backendFor(@NotNull Category category) {
        Backend backend = find(category);
        return backend == null ? Optional.empty() : Optional.of(backend);
    }

    public @NotNull Optional<Backend> get(@NotNull Category category) {
        return backendFor(category);
    }

    public @Nullable Backend find(@NotNull Category category) {
        Objects.requireNonNull(category, "category");

        Backend backend = routes.get(category);
        if (backend != null) {
            return backend;
        }

        return routesById.get(category.id());
    }

    public @NotNull Backend require(@NotNull Category category) {
        Backend backend = find(category);
        if (backend == null) {
            throw new IllegalStateException("no backend routed for category " + category.id());
        }
        return backend;
    }

    public @NotNull Backend route(@NotNull Category category) {
        return require(category);
    }

    public @NotNull Backend backend(@NotNull Category category) {
        return require(category);
    }

    public @NotNull Backend backendForOrThrow(@NotNull Category category) {
        return require(category);
    }

    @Override
    public String toString() {
        return "CategoryRouter" + routesById.keySet();
    }
}
