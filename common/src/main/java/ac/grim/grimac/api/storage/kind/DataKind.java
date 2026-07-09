package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

@ApiStatus.Experimental
public interface DataKind<E, R> {
    @NotNull String name();

    @NotNull Class<E> eventType();

    @NotNull Class<R> recordType();

    @NotNull Codec<R> codec();

    @NotNull EnumSet<Capability> requiredCapabilities();

    @NotNull EnumSet<Capability> optionalCapabilities();
}
