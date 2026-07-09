package ac.grim.grimac.internal.storage.identity;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

/**
 * Thin facade for submitting player-identity upserts.
 */
@ApiStatus.Internal
public final class PlayerIdentityService {

    private final DataStore store;

    public PlayerIdentityService(DataStore store) {
        this.store = store;
    }

    public void observe(final UUID uuid, final String name, final long epochMs) {
        store.submit(Categories.PLAYER_IDENTITY, e -> e
                .uuid(uuid)
                .currentName(name)
                .firstSeenEpochMs(epochMs)
                .lastSeenEpochMs(epochMs));
    }
}
