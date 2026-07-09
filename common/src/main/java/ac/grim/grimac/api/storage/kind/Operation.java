package ac.grim.grimac.api.storage.kind;

import org.jetbrains.annotations.ApiStatus;

/**
 * Java 8 source shadow for the v2 storage operation marker.
 *
 * The v2 storage API uses typed operation objects from
 * ac.grim.grimac.api.storage.kind.ops.*. Those operation classes already
 * implement/extend this interface in your backport, but the interface itself
 * was missing from the final runtime.
 *
 * @param <R> result type returned by this operation
 */
@ApiStatus.Experimental
public interface Operation<R> {
}
