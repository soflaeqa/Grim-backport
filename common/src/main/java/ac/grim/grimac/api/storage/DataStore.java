package ac.grim.grimac.api.storage;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.query.DeleteCriteria;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Query;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

@ApiStatus.Experimental
public interface DataStore {
    <E> void submit(@NotNull Category<E> cat, @NotNull Consumer<E> configurer);

    @NotNull <R> CompletionStage<Page<R>> query(@NotNull Category<?> cat, @NotNull Query<R> query);

    @NotNull
    default <R> CompletionStage<R> execute(@NotNull Operation<R> op) {
        throw new UnsupportedOperationException("execute(Operation) requires a V2-backed DataStoreImpl");
    }

    @NotNull <E> CompletionStage<Void> delete(@NotNull Category<E> cat, @NotNull DeleteCriteria criteria);

    @NotNull CompletionStage<DeletionReport> forgetPlayer(@NotNull UUID uuid);

    @NotNull CompletionStage<Long> countViolationsInSession(@NotNull UUID sessionId);

    @NotNull CompletionStage<Long> countUniqueChecksInSession(@NotNull UUID sessionId);

    @NotNull CompletionStage<Long> countSessionsByPlayer(@NotNull UUID player);

    @NotNull DataStoreMetrics metrics();

    void flushAndClose(long drainTimeoutMs);
}
