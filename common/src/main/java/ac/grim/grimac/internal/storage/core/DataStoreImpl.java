package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.DataStoreMetrics;
import ac.grim.grimac.api.storage.DeletionReport;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.instance.ServerOwnershipGate;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.EventStreamOps;
import ac.grim.grimac.api.storage.query.DeleteCriteria;
import ac.grim.grimac.api.storage.query.Deletes;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Query;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java 8 DataStoreImpl for the v2 routed backend surface.
 *
 * This implementation still stays synchronous/simple for the Java 8 backport,
 * but reads, counts and writes now delegate to the real KindAdapter instead of
 * returning empty pages.
 */
public final class DataStoreImpl implements DataStore {
    private final CategoryRouter router;
    private final Object writePath;
    private final Logger logger;
    private final ConcurrentMap<Category<?>, StorageEventHandler<Object>> handlers = new ConcurrentHashMap<Category<?>, StorageEventHandler<Object>>();
    private final AtomicLong sequence = new AtomicLong();
    private final AtomicLong submittedTotal = new AtomicLong();
    private final AtomicLong droppedOnErrorTotal = new AtomicLong();

    private volatile V2Routes v2Routes = V2Routes.empty();
    private volatile ServerOwnershipGate ownershipGate = ServerOwnershipGate.disabled();
    private volatile boolean started;

    public DataStoreImpl(@NotNull CategoryRouter router, @NotNull Object writePath, @NotNull Logger logger) {
        this.router = router;
        this.writePath = writePath;
        this.logger = logger;
    }

    public @NotNull DataStoreImpl withV2Routes(@NotNull V2Routes routes) {
        this.v2Routes = routes == null ? V2Routes.empty() : routes;
        return this;
    }

    public @NotNull V2Routes v2Routes() { return v2Routes; }

    public @NotNull DataStoreImpl withOwnershipGate(@NotNull ServerOwnershipGate gate) {
        this.ownershipGate = gate == null ? ServerOwnershipGate.disabled() : gate;
        return this;
    }

    public void start() { this.started = true; }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E> void submit(@NotNull Category<E> cat, @NotNull Consumer<E> configurer) {
        if (cat == null || configurer == null) return;
        if (!allowWrites()) return;

        E event;
        try {
            event = cat.newEvent().get();
            configurer.accept(event);
            prepareEvent(event);
        } catch (Throwable t) {
            droppedOnErrorTotal.incrementAndGet();
            logger.log(Level.WARNING, "[grim-datastore] failed to build storage event for category " + safeCategoryId(cat), t);
            return;
        }
        write(cat, event);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R> @NotNull CompletionStage<Page<R>> query(@NotNull Category<?> cat, @NotNull Query<R> query) {
        try {
            if (cat == null || query == null) return (CompletionStage) CompletableFuture.completedFuture(Page.empty());
            V2Routes.Route route = v2Routes.routeFor(cat);
            if (route == null) return (CompletionStage) CompletableFuture.completedFuture(Page.empty());
            Object out = ((KindAdapter) route.adapter()).execute(route.storeId(), route.kind(), new QueryOperation(cat, query));
            if (out instanceof Page) return CompletableFuture.completedFuture((Page<R>) out);
            return (CompletionStage) CompletableFuture.completedFuture(Page.empty());
        } catch (Throwable t) {
            return failed(t);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R> @NotNull CompletionStage<R> execute(@NotNull Operation<R> op) {
        try {
            Category<?> category = categoryOf(op);
            if (category != null) {
                V2Routes.Route route = v2Routes.routeFor(category);
                if (route != null) {
                    Object out = ((KindAdapter) route.adapter()).execute(route.storeId(), route.kind(), op);
                    return CompletableFuture.completedFuture((R) out);
                }
            }
            return CompletableFuture.completedFuture((R) executeFallback(op));
        } catch (Throwable t) {
            return failed(t);
        }
    }

    @Override
    public <E> @NotNull CompletionStage<Void> delete(@NotNull Category<E> cat, @NotNull DeleteCriteria criteria) {
        try {
            if (cat == null || criteria == null) return CompletableFuture.completedFuture(null);
            if (criteria instanceof Deletes.ByPlayer) {
                UUID uuid = ((Deletes.ByPlayer) criteria).uuid();
                return execute(new EventStreamOps.DeleteByPartitionOp(cat, "player", uuid));
            }
            if (criteria instanceof Deletes.OlderThan) {
                long cutoff = System.currentTimeMillis() - ((Deletes.OlderThan) criteria).maxAgeMs();
                return execute(new EventStreamOps.DeleteOlderThanOp(cat, cutoff));
            }
            return CompletableFuture.completedFuture(null);
        } catch (Throwable t) {
            return failed(t);
        }
    }

    @Override
    public @NotNull CompletionStage<DeletionReport> forgetPlayer(@NotNull UUID uuid) {
        try {
            delete(Categories.VIOLATION, Deletes.byPlayer(uuid)).toCompletableFuture().join();
            delete(Categories.SESSION, Deletes.byPlayer(uuid)).toCompletableFuture().join();
            delete(Categories.PLAYER_IDENTITY, Deletes.byPlayer(uuid)).toCompletableFuture().join();
            return CompletableFuture.completedFuture(DeletionReport.EMPTY);
        } catch (Throwable t) {
            return failed(t);
        }
    }

    @Override
    public @NotNull CompletionStage<Long> countViolationsInSession(@NotNull UUID sessionId) {
        return execute(new EventStreamOps.CountOp(Categories.VIOLATION, "session", sessionId));
    }

    @Override
    public @NotNull CompletionStage<Long> countUniqueChecksInSession(@NotNull UUID sessionId) {
        return execute(new EventStreamOps.CountDistinctOp(Categories.VIOLATION, "session", sessionId, "check"));
    }

    @Override
    public @NotNull CompletionStage<Long> countSessionsByPlayer(@NotNull UUID player) {
        return execute(new EventStreamOps.CountOp(Categories.SESSION, "player", player));
    }

    @Override
    public @NotNull DataStoreMetrics metrics() {
        return new DataStoreMetrics() {
            @Override public long queuedCount() { return 0L; }
            @Override public long submittedTotal() { return DataStoreImpl.this.submittedTotal.get(); }
            @Override public long droppedOnOverflowTotal() { return 0L; }
            @Override public long droppedOnErrorTotal() { return DataStoreImpl.this.droppedOnErrorTotal.get(); }
            @Override public long writeBatchLatencyMsEma() { return 0L; }
            @Override public long readLatencyMsEma() { return 0L; }
        };
    }

    @Override
    public void flushAndClose(long drainTimeoutMs) {
        started = false;
        handlers.clear();
    }

    private boolean allowWrites() {
        ServerOwnershipGate gate = ownershipGate;
        if (gate == null || gate.allowWrites()) return true;
        droppedOnErrorTotal.incrementAndGet();
        String reason = gate.closeReason();
        logger.warning("[grim-datastore] write dropped because persistent ownership gate is closed" + (reason == null ? "" : ": " + reason));
        return false;
    }

    private void write(@NotNull Category<?> category, Object event) {
        if (event == null) return;
        StorageEventHandler<Object> handler;
        try { handler = handlerFor(category); }
        catch (Throwable t) {
            droppedOnErrorTotal.incrementAndGet();
            logger.log(Level.WARNING, "[grim-datastore] failed to resolve write handler for category " + safeCategoryId(category), t);
            return;
        }
        if (handler == null) {
            droppedOnErrorTotal.incrementAndGet();
            logger.warning("[grim-datastore] no route for category " + safeCategoryId(category) + "; write dropped");
            return;
        }
        try {
            handler.onEvent(event, sequence.getAndIncrement(), true);
            submittedTotal.incrementAndGet();
        } catch (Throwable t) {
            droppedOnErrorTotal.incrementAndGet();
            logger.log(Level.WARNING, "[grim-datastore] write failed for category " + safeCategoryId(category), t);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private StorageEventHandler<Object> handlerFor(@NotNull Category<?> category) throws BackendException {
        StorageEventHandler<Object> cached = handlers.get(category);
        if (cached != null) return cached;
        V2Routes.Route route = v2Routes.routeFor(category);
        if (route == null) return null;
        KindAdapter adapter = route.adapter();
        StorageEventHandler<Object> created = (StorageEventHandler<Object>) adapter.writeHandler(route.storeId(), route.kind(), category);
        StorageEventHandler<Object> existing = handlers.putIfAbsent(category, created);
        return existing == null ? created : existing;
    }

    private Object executeFallback(Operation<?> op) {
        String simpleName = op == null ? "" : op.getClass().getSimpleName();
        if ("GetByIdOp".equals(simpleName)) return Optional.empty();
        if ("GetManyOp".equals(simpleName)) return Collections.emptyList();
        if (simpleName.contains("Find") || simpleName.contains("Page")) return Page.empty();
        if (simpleName.contains("Count")) return 0L;
        return null;
    }

    private static void prepareEvent(Object event) {
        if (event == null) return;
        Object id = invokeNoArg(event, "id");
        if (id == null) invokeOneArg(event, "id", UUID.randomUUID());
        Object occurred = invokeNoArg(event, "occurredEpochMs");
        if (occurred instanceof Number && ((Number) occurred).longValue() <= 0L) {
            invokeOneArg(event, "occurredEpochMs", System.currentTimeMillis());
        }
    }

    private static Category<?> categoryOf(Object op) {
        Object category = invokeNoArg(op, "category");
        return category instanceof Category ? (Category<?>) category : null;
    }

    private static Object invokeNoArg(Object target, String methodName) {
        if (target == null) return null;
        try { Method method = target.getClass().getMethod(methodName); return method.invoke(target); }
        catch (Throwable ignored) { return null; }
    }

    private static void invokeOneArg(Object target, String methodName, Object value) {
        if (target == null) return;
        Method[] methods = target.getClass().getMethods();
        for (Method method : methods) {
            if (!methodName.equals(method.getName()) || method.getParameterTypes().length != 1) continue;
            Class<?> type = wrap(method.getParameterTypes()[0]);
            if (value == null || type.isAssignableFrom(value.getClass())) {
                try { method.invoke(target, value); } catch (Throwable ignored) {}
                return;
            }
        }
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == long.class) return Long.class;
        if (type == int.class) return Integer.class;
        if (type == double.class) return Double.class;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == float.class) return Float.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static String safeCategoryId(Category<?> category) {
        try { return category == null ? "null" : category.id(); }
        catch (Throwable ignored) { return String.valueOf(category); }
    }

    private static <T> CompletableFuture<T> failed(Throwable t) {
        CompletableFuture<T> future = new CompletableFuture<T>();
        future.completeExceptionally(t);
        return future;
    }

    public static final class QueryOperation<R> implements Operation<Page<R>> {
        private final Category<?> category;
        private final Query<R> query;
        public QueryOperation(Category<?> category, Query<R> query) { this.category = category; this.query = query; }
        public Category<?> category() { return category; }
        public Query<R> query() { return query; }
    }
}
