package ac.grim.grimac.api.storage.instance;

import java.util.Objects;
import java.util.UUID;

public final class OwnershipRenewResult {
    private final boolean renewed;
    private final UUID persistentId;
    private final UUID startupId;
    private final UUID fence;
    private final long dbNowEpochMs;
    private final long leaseExpiresAtEpochMs;

    public OwnershipRenewResult(boolean renewed, UUID persistentId, UUID startupId, UUID fence,
                                long dbNowEpochMs, long leaseExpiresAtEpochMs) {
        this.renewed = renewed;
        this.persistentId = persistentId;
        this.startupId = startupId;
        this.fence = fence;
        this.dbNowEpochMs = dbNowEpochMs;
        this.leaseExpiresAtEpochMs = leaseExpiresAtEpochMs;
    }

    public static OwnershipRenewResult renewed(UUID persistentId, UUID startupId, UUID fence,
                                               long dbNowEpochMs, long leaseExpiresAtEpochMs) {
        return new OwnershipRenewResult(true, persistentId, startupId, fence, dbNowEpochMs, leaseExpiresAtEpochMs);
    }

    public static OwnershipRenewResult lost(UUID persistentId, UUID startupId, UUID fence, long dbNowEpochMs) {
        return new OwnershipRenewResult(false, persistentId, startupId, fence, dbNowEpochMs, 0L);
    }

    public boolean renewed() { return renewed; }
    public UUID persistentId() { return persistentId; }
    public UUID startupId() { return startupId; }
    public UUID fence() { return fence; }
    public long dbNowEpochMs() { return dbNowEpochMs; }
    public long leaseExpiresAtEpochMs() { return leaseExpiresAtEpochMs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OwnershipRenewResult)) return false;
        OwnershipRenewResult that = (OwnershipRenewResult) o;
        return renewed == that.renewed
                && dbNowEpochMs == that.dbNowEpochMs
                && leaseExpiresAtEpochMs == that.leaseExpiresAtEpochMs
                && Objects.equals(persistentId, that.persistentId)
                && Objects.equals(startupId, that.startupId)
                && Objects.equals(fence, that.fence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(renewed, persistentId, startupId, fence, dbNowEpochMs, leaseExpiresAtEpochMs);
    }

    @Override
    public String toString() {
        return "OwnershipRenewResult[renewed=" + renewed
                + ", persistentId=" + persistentId
                + ", startupId=" + startupId
                + ", fence=" + fence
                + ", dbNowEpochMs=" + dbNowEpochMs
                + ", leaseExpiresAtEpochMs=" + leaseExpiresAtEpochMs + ']';
    }
}
