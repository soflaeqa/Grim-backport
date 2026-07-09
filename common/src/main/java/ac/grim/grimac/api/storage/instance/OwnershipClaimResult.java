package ac.grim.grimac.api.storage.instance;

import java.util.Objects;
import java.util.UUID;

public final class OwnershipClaimResult {
    private final boolean claimed;
    private final UUID persistentId;
    private final UUID startupId;
    private final UUID fence;
    private final long dbNowEpochMs;
    private final long leaseExpiresAtEpochMs;
    private final ServerOwnershipSnapshot previousOwner;
    private final ServerOwnershipSnapshot currentOwner;

    public OwnershipClaimResult(boolean claimed, UUID persistentId, UUID startupId, UUID fence,
                                long dbNowEpochMs, long leaseExpiresAtEpochMs,
                                ServerOwnershipSnapshot previousOwner, ServerOwnershipSnapshot currentOwner) {
        this.claimed = claimed;
        this.persistentId = persistentId;
        this.startupId = startupId;
        this.fence = fence;
        this.dbNowEpochMs = dbNowEpochMs;
        this.leaseExpiresAtEpochMs = leaseExpiresAtEpochMs;
        this.previousOwner = previousOwner;
        this.currentOwner = currentOwner;
    }

    public static OwnershipClaimResult claimed(UUID persistentId, UUID startupId, UUID fence,
                                               long dbNowEpochMs, long leaseExpiresAtEpochMs,
                                               ServerOwnershipSnapshot previousOwner) {
        ServerOwnershipSnapshot currentOwner = new ServerOwnershipSnapshot(
                persistentId,
                startupId,
                fence,
                leaseExpiresAtEpochMs,
                dbNowEpochMs,
                ServerOwnershipSnapshot.OPEN,
                null,
                previousOwner == null ? null : previousOwner.serverName(),
                previousOwner == null ? null : previousOwner.hostname(),
                previousOwner == null ? null : previousOwner.grimVersion(),
                previousOwner == null ? null : previousOwner.serverVersionString());
        return new OwnershipClaimResult(true, persistentId, startupId, fence,
                dbNowEpochMs, leaseExpiresAtEpochMs, previousOwner, currentOwner);
    }

    public static OwnershipClaimResult denied(UUID persistentId, UUID startupId, UUID fence,
                                              long dbNowEpochMs, ServerOwnershipSnapshot currentOwner) {
        long leaseExpiresAt = currentOwner == null ? 0L : currentOwner.leaseExpiresAtEpochMs();
        return new OwnershipClaimResult(false, persistentId, startupId, fence,
                dbNowEpochMs, leaseExpiresAt, currentOwner, currentOwner);
    }

    public boolean claimed() { return claimed; }
    public UUID persistentId() { return persistentId; }
    public UUID startupId() { return startupId; }
    public UUID fence() { return fence; }
    public long dbNowEpochMs() { return dbNowEpochMs; }
    public long leaseExpiresAtEpochMs() { return leaseExpiresAtEpochMs; }
    public ServerOwnershipSnapshot previousOwner() { return previousOwner; }
    public ServerOwnershipSnapshot currentOwner() { return currentOwner; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OwnershipClaimResult)) return false;
        OwnershipClaimResult that = (OwnershipClaimResult) o;
        return claimed == that.claimed
                && dbNowEpochMs == that.dbNowEpochMs
                && leaseExpiresAtEpochMs == that.leaseExpiresAtEpochMs
                && Objects.equals(persistentId, that.persistentId)
                && Objects.equals(startupId, that.startupId)
                && Objects.equals(fence, that.fence)
                && Objects.equals(previousOwner, that.previousOwner)
                && Objects.equals(currentOwner, that.currentOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claimed, persistentId, startupId, fence, dbNowEpochMs,
                leaseExpiresAtEpochMs, previousOwner, currentOwner);
    }

    @Override
    public String toString() {
        return "OwnershipClaimResult[claimed=" + claimed
                + ", persistentId=" + persistentId
                + ", startupId=" + startupId
                + ", fence=" + fence
                + ", dbNowEpochMs=" + dbNowEpochMs
                + ", leaseExpiresAtEpochMs=" + leaseExpiresAtEpochMs
                + ", previousOwner=" + previousOwner
                + ", currentOwner=" + currentOwner + ']';
    }
}
