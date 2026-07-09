package ac.grim.grimac.internal.storage.identity;

import ac.grim.grimac.api.storage.identity.NameResolverLink;
import org.jetbrains.annotations.ApiStatus;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Offline-mode UUID derivation.
 */
@ApiStatus.Internal
public final class OfflineModeUuidLink implements NameResolverLink {

    public static final String ID = "offline-mode-uuid";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public CompletionStage<Optional<UUID>> resolveByName(String name) {
        UUID derived = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        return CompletableFuture.completedFuture(Optional.of(derived));
    }

    @Override
    public CompletionStage<Optional<String>> resolveByUuid(UUID uuid) {
        return CompletableFuture.completedFuture(Optional.<String>empty());
    }
}
