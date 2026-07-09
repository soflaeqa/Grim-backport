package ac.grim.grimac.api.storage.tx;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@ApiStatus.Experimental
public interface Transaction {
    <E> void submit(@NotNull Category<E> cat, @NotNull Consumer<E> configurer);
    <R> R execute(@NotNull Operation<R> op) throws Exception;
    void rollback();
}
