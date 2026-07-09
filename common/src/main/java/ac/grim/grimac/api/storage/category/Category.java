package ac.grim.grimac.api.storage.category;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.function.Supplier;

@ApiStatus.Experimental
public interface Category<E> {
    @NotNull String id();
    @NotNull Class<E> eventType();
    @NotNull Supplier<E> newEvent();
    @NotNull Class<?> queryResultType();
    @NotNull EnumSet<Capability> requiredCapabilities();
    @NotNull AccessPattern accessPattern();
}
