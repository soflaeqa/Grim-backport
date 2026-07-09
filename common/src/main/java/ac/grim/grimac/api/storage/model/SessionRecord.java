package ac.grim.grimac.api.storage.model;

import java.util.Objects;

public final class SessionRecord {
    private final java.util.UUID sessionId;
    private final java.util.UUID playerUuid;
    private final String serverName;
    private final long startedEpochMs;
    private final long lastActivityEpochMs;
    private final long closedAtEpochMs;
    private final String grimVersion;
    private final String clientBrand;
    private final int clientVersion;
    private final String serverVersionString;
    private final java.util.UUID instanceId;
    private final java.util.UUID startupId;
    private final java.util.List<SessionBlobRecord> sessionBlobs;

    public SessionRecord(java.util.UUID sessionId, java.util.UUID playerUuid, String serverName, long startedEpochMs, long lastActivityEpochMs, long closedAtEpochMs, String grimVersion, String clientBrand, int clientVersion, String serverVersionString, java.util.UUID instanceId, java.util.UUID startupId, java.util.List<SessionBlobRecord> sessionBlobs) {
        this.sessionId = sessionId;
        this.playerUuid = playerUuid;
        this.serverName = serverName;
        this.startedEpochMs = startedEpochMs;
        this.lastActivityEpochMs = lastActivityEpochMs;
        this.closedAtEpochMs = closedAtEpochMs;
        this.grimVersion = grimVersion;
        this.clientBrand = clientBrand;
        this.clientVersion = clientVersion;
        this.serverVersionString = serverVersionString;
        this.instanceId = instanceId;
        this.startupId = startupId;
        this.sessionBlobs = sessionBlobs;
    }

    public SessionRecord(java.util.UUID sessionId, java.util.UUID playerUuid, String serverName, long startedEpochMs, long lastActivityEpochMs, Long closedAtEpochMs, String grimVersion, String clientBrand, int clientVersion, String serverVersionString, java.util.List<SessionBlobRecord> sessionBlobs) {
        this(sessionId, playerUuid, serverName, startedEpochMs, lastActivityEpochMs, closedAtEpochMs == null ? OPEN : closedAtEpochMs.longValue(), grimVersion, clientBrand, clientVersion, serverVersionString, null, null, sessionBlobs);
    }

    public SessionRecord(java.util.UUID sessionId, java.util.UUID playerUuid, String serverName, long startedEpochMs, long lastActivityEpochMs, long closedAtEpochMs, String grimVersion, String clientBrand, int clientVersion, String serverVersionString, java.util.UUID instanceId, java.util.List<SessionBlobRecord> sessionBlobs) {
        this(sessionId, playerUuid, serverName, startedEpochMs, lastActivityEpochMs, closedAtEpochMs, grimVersion, clientBrand, clientVersion, serverVersionString, instanceId, null, sessionBlobs);
    }

    public java.util.UUID sessionId() { return sessionId; }
    public java.util.UUID playerUuid() { return playerUuid; }
    public String serverName() { return serverName; }
    public long startedEpochMs() { return startedEpochMs; }
    public long lastActivityEpochMs() { return lastActivityEpochMs; }
    public long closedAtEpochMs() { return closedAtEpochMs; }
    public String grimVersion() { return grimVersion; }
    public String clientBrand() { return clientBrand; }
    public int clientVersion() { return clientVersion; }
    public String serverVersionString() { return serverVersionString; }
    public java.util.UUID instanceId() { return instanceId; }
    public java.util.UUID startupId() { return startupId; }
    public java.util.List<SessionBlobRecord> sessionBlobs() { return sessionBlobs; }

    public static final long OPEN = 0L;

    public boolean isClosed() { return closedAtEpochMs != OPEN; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionRecord)) return false;
        SessionRecord that = (SessionRecord) o;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(playerUuid, that.playerUuid) && Objects.equals(serverName, that.serverName) && startedEpochMs == that.startedEpochMs && lastActivityEpochMs == that.lastActivityEpochMs && closedAtEpochMs == that.closedAtEpochMs && Objects.equals(grimVersion, that.grimVersion) && Objects.equals(clientBrand, that.clientBrand) && clientVersion == that.clientVersion && Objects.equals(serverVersionString, that.serverVersionString) && Objects.equals(instanceId, that.instanceId) && Objects.equals(startupId, that.startupId) && Objects.equals(sessionBlobs, that.sessionBlobs);
    }
    @Override public int hashCode() {
        return Objects.hash(sessionId, playerUuid, serverName, startedEpochMs, lastActivityEpochMs, closedAtEpochMs, grimVersion, clientBrand, clientVersion, serverVersionString, instanceId, startupId, sessionBlobs);
    }
    @Override public String toString() { return "SessionRecord[sessionId=" + sessionId + ", playerUuid=" + playerUuid + ", serverName=" + serverName + ", startedEpochMs=" + startedEpochMs + ", lastActivityEpochMs=" + lastActivityEpochMs + ", closedAtEpochMs=" + closedAtEpochMs + ", grimVersion=" + grimVersion + ", clientBrand=" + clientBrand + ", clientVersion=" + clientVersion + ", serverVersionString=" + serverVersionString + ", instanceId=" + instanceId + ", startupId=" + startupId + ", sessionBlobs=" + sessionBlobs + "]"; }
}