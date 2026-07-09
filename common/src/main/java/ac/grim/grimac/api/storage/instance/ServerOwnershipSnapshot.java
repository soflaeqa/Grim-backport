package ac.grim.grimac.api.storage.instance;

import java.util.Objects;
import java.util.UUID;

public final class ServerOwnershipSnapshot {
    public static final long OPEN = 0L;

    private final UUID persistentId;
    private final UUID ownerStartupId;
    private final UUID fence;
    private final long leaseExpiresAtEpochMs;
    private final long lastRenewedAtEpochMs;
    private final long closedAtEpochMs;
    private final String closeReason;
    private final String serverName;
    private final String hostname;
    private final String grimVersion;
    private final String serverVersionString;

    public ServerOwnershipSnapshot(UUID persistentId, UUID ownerStartupId, UUID fence,
                                   long leaseExpiresAtEpochMs, long lastRenewedAtEpochMs,
                                   long closedAtEpochMs, String closeReason,
                                   String serverName, String hostname,
                                   String grimVersion, String serverVersionString) {
        this.persistentId = persistentId;
        this.ownerStartupId = ownerStartupId;
        this.fence = fence;
        this.leaseExpiresAtEpochMs = leaseExpiresAtEpochMs;
        this.lastRenewedAtEpochMs = lastRenewedAtEpochMs;
        this.closedAtEpochMs = closedAtEpochMs;
        this.closeReason = closeReason;
        this.serverName = serverName;
        this.hostname = hostname;
        this.grimVersion = grimVersion;
        this.serverVersionString = serverVersionString;
    }

    public boolean activeAt(long epochMs) {
        return closedAtEpochMs == OPEN && leaseExpiresAtEpochMs > epochMs;
    }

    public UUID persistentId() { return persistentId; }
    public UUID ownerStartupId() { return ownerStartupId; }
    public UUID fence() { return fence; }
    public long leaseExpiresAtEpochMs() { return leaseExpiresAtEpochMs; }
    public long lastRenewedAtEpochMs() { return lastRenewedAtEpochMs; }
    public long closedAtEpochMs() { return closedAtEpochMs; }
    public String closeReason() { return closeReason; }
    public String serverName() { return serverName; }
    public String hostname() { return hostname; }
    public String grimVersion() { return grimVersion; }
    public String serverVersionString() { return serverVersionString; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerOwnershipSnapshot)) return false;
        ServerOwnershipSnapshot that = (ServerOwnershipSnapshot) o;
        return leaseExpiresAtEpochMs == that.leaseExpiresAtEpochMs
                && lastRenewedAtEpochMs == that.lastRenewedAtEpochMs
                && closedAtEpochMs == that.closedAtEpochMs
                && Objects.equals(persistentId, that.persistentId)
                && Objects.equals(ownerStartupId, that.ownerStartupId)
                && Objects.equals(fence, that.fence)
                && Objects.equals(closeReason, that.closeReason)
                && Objects.equals(serverName, that.serverName)
                && Objects.equals(hostname, that.hostname)
                && Objects.equals(grimVersion, that.grimVersion)
                && Objects.equals(serverVersionString, that.serverVersionString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persistentId, ownerStartupId, fence, leaseExpiresAtEpochMs,
                lastRenewedAtEpochMs, closedAtEpochMs, closeReason, serverName,
                hostname, grimVersion, serverVersionString);
    }

    @Override
    public String toString() {
        return "ServerOwnershipSnapshot[persistentId=" + persistentId
                + ", ownerStartupId=" + ownerStartupId
                + ", fence=" + fence
                + ", leaseExpiresAtEpochMs=" + leaseExpiresAtEpochMs
                + ", lastRenewedAtEpochMs=" + lastRenewedAtEpochMs
                + ", closedAtEpochMs=" + closedAtEpochMs
                + ", closeReason=" + closeReason
                + ", serverName=" + serverName
                + ", hostname=" + hostname
                + ", grimVersion=" + grimVersion
                + ", serverVersionString=" + serverVersionString + ']';
    }
}
