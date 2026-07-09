package ac.grim.grimac.api.storage.registry;

import ac.grim.grimac.api.storage.kind.DataKind;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Java 8 runtime replacement for GrimAPI's migration step interface.
 *
 * @param <K> the data kind type this migration applies to
 */
@ApiStatus.Experimental
public interface Migration<K extends DataKind<?, ?>> {

    int fromVersion();

    int toVersion();

    void apply(@NotNull MigrationContext ctx, @NotNull StoreId id, @NotNull K kind) throws Exception;
}
