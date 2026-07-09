package ac.grim.grimac.api.storage.tx;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
@FunctionalInterface
public interface TxBody<T> {
    T run(@NotNull Transaction tx) throws Exception;
}
