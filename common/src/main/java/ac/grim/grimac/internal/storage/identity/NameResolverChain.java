package ac.grim.grimac.internal.storage.identity;

import ac.grim.grimac.api.storage.identity.NameResolver;
import ac.grim.grimac.api.storage.identity.NameResolverLink;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Ordered list of {@link NameResolverLink}s. Consulted sequentially; the first
 * non-empty result wins. Empty means "don't know, try the next link".
 */
@ApiStatus.Internal
public final class NameResolverChain implements NameResolver {

    private final List<NameResolverLink> links;

    public NameResolverChain(List<NameResolverLink> links) {
        this.links = Collections.unmodifiableList(new ArrayList<NameResolverLink>(links));
    }

    @Override
    public CompletionStage<Optional<UUID>> resolveByName(String name) {
        return chain(0, new LinkAccess<UUID>() {
            @Override
            public CompletionStage<Optional<UUID>> apply(NameResolverLink link) {
                return link.resolveByName(name);
            }
        });
    }

    @Override
    public CompletionStage<Optional<String>> resolveByUuid(UUID uuid) {
        return chain(0, new LinkAccess<String>() {
            @Override
            public CompletionStage<Optional<String>> apply(NameResolverLink link) {
                return link.resolveByUuid(uuid);
            }
        });
    }

    @Override
    public CompletionStage<List<UUID>> allHistoricalUsersOfName(String name) {
        return resolveByName(name).thenApply(opt ->
                opt.<List<UUID>>map(Collections::singletonList).orElseGet(Collections::emptyList));
    }

    private <T> CompletionStage<Optional<T>> chain(final int idx, final LinkAccess<T> access) {
        if (idx >= links.size()) return CompletableFuture.completedFuture(Optional.<T>empty());
        return access.apply(links.get(idx))
                .thenCompose(result -> result.isPresent()
                        ? CompletableFuture.completedFuture(result)
                        : chain(idx + 1, access));
    }

    public List<NameResolverLink> links() {
        return new ArrayList<NameResolverLink>(links);
    }

    private interface LinkAccess<T> {
        CompletionStage<Optional<T>> apply(NameResolverLink link);
    }
}
