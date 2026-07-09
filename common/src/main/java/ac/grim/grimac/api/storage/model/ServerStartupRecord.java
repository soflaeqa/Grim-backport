package ac.grim.grimac.api.storage.model;

import java.util.Objects;
import java.util.Arrays;

public final class ServerStartupRecord {
    private final java.util.UUID startupId;
    private final java.util.UUID instanceId;
    private final String serverName;
    private final String grimVersion;
    private final String serverVersionString;
    private final String hostname;
    private final long startedEpochMs;
    private final long lastHeartbeatEpochMs;
    private final long closedAtEpochMs;
    private final String closeReason;
    private final byte[] verboseManifest;

    public ServerStartupRecord(java.util.UUID startupId, java.util.UUID instanceId, String serverName, String grimVersion, String serverVersionString, String hostname, long startedEpochMs, long lastHeartbeatEpochMs, long closedAtEpochMs, String closeReason, byte[] verboseManifest) {
        this.startupId = startupId;
        this.instanceId = instanceId;
        this.serverName = serverName;
        this.grimVersion = grimVersion;
        this.serverVersionString = serverVersionString;
        this.hostname = hostname;
        this.startedEpochMs = startedEpochMs;
        this.lastHeartbeatEpochMs = lastHeartbeatEpochMs;
        this.closedAtEpochMs = closedAtEpochMs;
        this.closeReason = closeReason;
        this.verboseManifest = verboseManifest;
    }

    public java.util.UUID startupId() { return startupId; }
    public java.util.UUID instanceId() { return instanceId; }
    public String serverName() { return serverName; }
    public String grimVersion() { return grimVersion; }
    public String serverVersionString() { return serverVersionString; }
    public String hostname() { return hostname; }
    public long startedEpochMs() { return startedEpochMs; }
    public long lastHeartbeatEpochMs() { return lastHeartbeatEpochMs; }
    public long closedAtEpochMs() { return closedAtEpochMs; }
    public String closeReason() { return closeReason; }
    public byte[] verboseManifest() { return verboseManifest; }

    public static final long OPEN = 0L;

    public boolean isClosed() { return closedAtEpochMs != OPEN; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerStartupRecord)) return false;
        ServerStartupRecord that = (ServerStartupRecord) o;
        return Objects.equals(startupId, that.startupId) && Objects.equals(instanceId, that.instanceId) && Objects.equals(serverName, that.serverName) && Objects.equals(grimVersion, that.grimVersion) && Objects.equals(serverVersionString, that.serverVersionString) && Objects.equals(hostname, that.hostname) && startedEpochMs == that.startedEpochMs && lastHeartbeatEpochMs == that.lastHeartbeatEpochMs && closedAtEpochMs == that.closedAtEpochMs && Objects.equals(closeReason, that.closeReason) && Arrays.equals(verboseManifest, that.verboseManifest);
    }
    @Override public int hashCode() {
        int result = Objects.hash(startupId, instanceId, serverName, grimVersion, serverVersionString, hostname, startedEpochMs, lastHeartbeatEpochMs, closedAtEpochMs, closeReason);
        result = 31 * result + Arrays.hashCode(verboseManifest);
        return result;
    }
    @Override public String toString() { return "ServerStartupRecord[startupId=" + startupId + ", instanceId=" + instanceId + ", serverName=" + serverName + ", grimVersion=" + grimVersion + ", serverVersionString=" + serverVersionString + ", hostname=" + hostname + ", startedEpochMs=" + startedEpochMs + ", lastHeartbeatEpochMs=" + lastHeartbeatEpochMs + ", closedAtEpochMs=" + closedAtEpochMs + ", closeReason=" + closeReason + ", verboseManifest=" + Arrays.toString(verboseManifest) + "]"; }
}