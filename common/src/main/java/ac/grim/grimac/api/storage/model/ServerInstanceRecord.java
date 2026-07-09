package ac.grim.grimac.api.storage.model;

import java.util.Objects;

public final class ServerInstanceRecord {
    private final java.util.UUID instanceId;
    private final String serverName;
    private final long startedEpochMs;
    private final long lastHeartbeatEpochMs;
    private final String hostname;
    private final String grimVersion;
    private final boolean drainMode;

    public ServerInstanceRecord(java.util.UUID instanceId, String serverName, long startedEpochMs, long lastHeartbeatEpochMs, String hostname, String grimVersion, boolean drainMode) {
        this.instanceId = instanceId;
        this.serverName = serverName;
        this.startedEpochMs = startedEpochMs;
        this.lastHeartbeatEpochMs = lastHeartbeatEpochMs;
        this.hostname = hostname;
        this.grimVersion = grimVersion;
        this.drainMode = drainMode;
    }

    public java.util.UUID instanceId() { return instanceId; }
    public String serverName() { return serverName; }
    public long startedEpochMs() { return startedEpochMs; }
    public long lastHeartbeatEpochMs() { return lastHeartbeatEpochMs; }
    public String hostname() { return hostname; }
    public String grimVersion() { return grimVersion; }
    public boolean drainMode() { return drainMode; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerInstanceRecord)) return false;
        ServerInstanceRecord that = (ServerInstanceRecord) o;
        return Objects.equals(instanceId, that.instanceId) && Objects.equals(serverName, that.serverName) && startedEpochMs == that.startedEpochMs && lastHeartbeatEpochMs == that.lastHeartbeatEpochMs && Objects.equals(hostname, that.hostname) && Objects.equals(grimVersion, that.grimVersion) && drainMode == that.drainMode;
    }
    @Override public int hashCode() {
        return Objects.hash(instanceId, serverName, startedEpochMs, lastHeartbeatEpochMs, hostname, grimVersion, drainMode);
    }
    @Override public String toString() { return "ServerInstanceRecord[instanceId=" + instanceId + ", serverName=" + serverName + ", startedEpochMs=" + startedEpochMs + ", lastHeartbeatEpochMs=" + lastHeartbeatEpochMs + ", hostname=" + hostname + ", grimVersion=" + grimVersion + ", drainMode=" + drainMode + "]"; }
}