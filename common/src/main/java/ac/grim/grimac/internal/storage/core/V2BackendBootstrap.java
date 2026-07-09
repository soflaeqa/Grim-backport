package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.BackendV2;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.MigrationContext;
import ac.grim.grimac.api.storage.registry.StoreId;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java 8 source shadow for the v2 bootstrap helper.
 *
 * <p>The upstream class is compiled with newer Java features in the dependency
 * cache. This implementation keeps the same public surface used by common while
 * avoiding records and other post-Java-8 bytecode.</p>
 */
public final class V2BackendBootstrap {
    private V2BackendBootstrap() {
    }

    public static final class Binding<K extends DataKind<?, ?>> {
        private final StoreId storeId;
        private final K kind;

        public Binding(@NotNull StoreId storeId, @NotNull K kind) {
            this.storeId = Objects.requireNonNull(storeId, "storeId");
            this.kind = Objects.requireNonNull(kind, "kind");
        }

        public @NotNull StoreId storeId() {
            return storeId;
        }

        /** Compatibility alias for possible upstream call sites. */
        public @NotNull StoreId store() {
            return storeId;
        }

        public @NotNull K kind() {
            return kind;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Binding)) return false;
            Binding<?> binding = (Binding<?>) o;
            return storeId.equals(binding.storeId) && kind.equals(binding.kind);
        }

        @Override
        public int hashCode() {
            return Objects.hash(storeId, kind);
        }

        @Override
        public String toString() {
            return "Binding[storeId=" + storeId + ", kind=" + kind + ']';
        }
    }

    public static final class Result {
        private final boolean ok;
        private final List<String> failures;

        public Result(boolean ok, @NotNull List<String> failures) {
            this.ok = ok;
            this.failures = Collections.unmodifiableList(
                    new ArrayList<>(Objects.requireNonNull(failures, "failures")));
        }

        public boolean ok() {
            return ok;
        }

        public @NotNull List<String> failures() {
            return failures;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result)) return false;
            Result result = (Result) o;
            return ok == result.ok && failures.equals(result.failures);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ok, failures);
        }

        @Override
        public String toString() {
            return "Result[ok=" + ok + ", failures=" + failures + ']';
        }
    }

    public static @NotNull Result install(@NotNull Map<Category<?>, Binding<?>> bindings,
                                          @NotNull BackendV2 backend,
                                          @NotNull MigrationContext migrationContext,
                                          @NotNull V2Routes.Builder routesBuilder,
                                          @NotNull Logger logger) {
        Objects.requireNonNull(bindings, "bindings");
        Objects.requireNonNull(backend, "backend");
        Objects.requireNonNull(migrationContext, "migrationContext");
        Objects.requireNonNull(routesBuilder, "routesBuilder");
        Objects.requireNonNull(logger, "logger");

        List<String> failures = new ArrayList<>();
        for (Map.Entry<Category<?>, Binding<?>> entry : bindings.entrySet()) {
            installOne(entry.getKey(), entry.getValue(), backend, migrationContext, routesBuilder, logger, failures);
        }
        return new Result(failures.isEmpty(), failures);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void installOne(Category<?> category,
                                   Binding<?> binding,
                                   BackendV2 backend,
                                   MigrationContext migrationContext,
                                   V2Routes.Builder routesBuilder,
                                   Logger logger,
                                   List<String> failures) {
        if (category == null || binding == null) {
            failures.add("null v2 bootstrap binding");
            return;
        }

        DataKind kind = binding.kind();
        StoreId storeId = binding.storeId();
        KindAdapter adapter;
        try {
            Optional optional = backend.adapterFor(kind);
            if (!optional.isPresent()) {
                failures.add("backend '" + backend.id() + "' has no adapter for kind '" + kind.name() + "'");
                return;
            }
            adapter = (KindAdapter) optional.get();
        } catch (RuntimeException e) {
            failures.add("failed to resolve adapter for kind '" + safeKindName(kind) + "': " + e.getMessage());
            logger.log(Level.WARNING, "[grim-datastore] failed to resolve v2 adapter", e);
            return;
        }

        try {
            adapter.ensureStore(storeId, kind);
        } catch (BackendException e) {
            failures.add("failed to ensure store '" + storeId + "' for kind '" + safeKindName(kind) + "': " + e.getMessage());
            logger.log(Level.WARNING, "[grim-datastore] failed to ensure v2 store " + storeId, e);
            return;
        } catch (RuntimeException e) {
            failures.add("failed to ensure store '" + storeId + "' for kind '" + safeKindName(kind) + "': " + e.getMessage());
            logger.log(Level.WARNING, "[grim-datastore] failed to ensure v2 store " + storeId, e);
            return;
        }

        try {
            List migrations = adapter.migrations(kind);
            if (migrations != null) {
                for (Object rawMigration : migrations) {
                    if (rawMigration instanceof Migration) {
                        ((Migration) rawMigration).apply(migrationContext, storeId, kind);
                    }
                }
            }
        } catch (Exception e) {
            failures.add("failed to apply migrations for store '" + storeId + "': " + e.getMessage());
            logger.log(Level.WARNING, "[grim-datastore] failed to apply v2 migrations for " + storeId, e);
            return;
        }

        if (!registerRoute(routesBuilder, category, backend, storeId, kind, adapter)) {
            failures.add("failed to register v2 route for category '" + category + "' and store '" + storeId + "'");
        }
    }

    private static String safeKindName(DataKind<?, ?> kind) {
        try {
            return kind == null ? "null" : kind.name();
        } catch (RuntimeException ignored) {
            return String.valueOf(kind);
        }
    }

    private static boolean registerRoute(V2Routes.Builder builder,
                                         Category<?> category,
                                         BackendV2 backend,
                                         StoreId storeId,
                                         DataKind<?, ?> kind,
                                         KindAdapter<?> adapter) {
        String[] preferredNames = new String[] {
                "put", "route", "add", "addRoute", "bind", "binding", "register", "registerRoute", "addBinding"
        };
        Object[] candidates = new Object[] { category, backend, storeId, kind, adapter };

        for (String name : preferredNames) {
            Method[] methods = builder.getClass().getMethods();
            for (Method method : methods) {
                if (name.equals(method.getName()) && tryInvoke(builder, method, candidates)) {
                    return true;
                }
            }
        }

        Method[] methods = builder.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals("build")) continue;
            if (tryInvoke(builder, method, candidates)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryInvoke(Object target, Method method, Object[] candidates) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0 || parameterTypes.length > candidates.length) return false;

        Object[] args = new Object[parameterTypes.length];
        boolean[] used = new boolean[candidates.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            int index = findCandidate(parameterTypes[i], candidates, used);
            if (index < 0) return false;
            args[i] = candidates[index];
            used[index] = true;
        }

        try {
            method.setAccessible(true);
            method.invoke(target, args);
            return true;
        } catch (IllegalAccessException ignored) {
            return false;
        } catch (IllegalArgumentException ignored) {
            return false;
        } catch (InvocationTargetException ignored) {
            return false;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static int findCandidate(Class<?> parameterType, Object[] candidates, boolean[] used) {
        for (int i = 0; i < candidates.length; i++) {
            if (used[i]) continue;
            Object candidate = candidates[i];
            if (candidate == null) continue;
            if (wrap(parameterType).isAssignableFrom(candidate.getClass())) return i;
        }
        return -1;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }
}
