package ac.grim.grimac.api.storage.instance;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.registry.StoreId;

import java.util.Optional;
import java.util.UUID;

public interface ServerOwnershipAdapter {
    void ensureStore(StoreId storeId) throws BackendException;

    long dbNowEpochMs() throws BackendException;

    OwnershipClaimResult claimOwnership(StoreId storeId, UUID persistentId, UUID startupId, UUID fence,
                                         long leaseTtlMs, ServerOwnershipMetadata metadata) throws BackendException;

    OwnershipRenewResult renewOwnership(StoreId storeId, UUID persistentId, UUID startupId, UUID fence,
                                         long leaseTtlMs) throws BackendException;

    boolean closeOwnership(StoreId storeId, UUID persistentId, UUID startupId, UUID fence,
                           String reason) throws BackendException;

    Optional<ServerOwnershipSnapshot> readOwnership(StoreId storeId, UUID persistentId) throws BackendException;
}
