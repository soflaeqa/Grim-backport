package ac.grim.grimac.api.storage.history;

import java.util.Objects;
import java.util.UUID;

public final class SessionSummary {
    private final UUID sessionId;
    private final UUID playerUuid;
    private final int sessionOrdinal;
    private final long startedEpochMs;
    private final long lastActivityEpochMs;
    private final long closedEpochMs;
    private final String grimVersion;
    private final String serverName;
    private final int clientVersion;
    private final String clientBrand;
    private final long violationCount;
    private final int uniqueCheckCount;
    private final boolean endedUnexpectedly;

    public SessionSummary(UUID sessionId, UUID playerUuid, int sessionOrdinal,
                          long startedEpochMs, long lastActivityEpochMs,
                          String grimVersion, String serverName, int clientVersion, String clientBrand,
                          long violationCount, int uniqueCheckCount) {
        this(sessionId, playerUuid, sessionOrdinal, startedEpochMs, lastActivityEpochMs,
                lastActivityEpochMs, grimVersion, serverName, clientVersion, clientBrand,
                violationCount, uniqueCheckCount, false);
    }

    public SessionSummary(UUID sessionId, UUID playerUuid, int sessionOrdinal,
                          long startedEpochMs, long lastActivityEpochMs,
                          String grimVersion, String serverName, int clientVersion, String clientBrand,
                          long violationCount, int uniqueCheckCount, boolean endedUnexpectedly) {
        this(sessionId, playerUuid, sessionOrdinal, startedEpochMs, lastActivityEpochMs,
                lastActivityEpochMs, grimVersion, serverName, clientVersion, clientBrand,
                violationCount, uniqueCheckCount, endedUnexpectedly);
    }

    public SessionSummary(UUID sessionId, UUID playerUuid, int sessionOrdinal,
                          long startedEpochMs, long lastActivityEpochMs, long closedEpochMs,
                          String grimVersion, String serverName, int clientVersion, String clientBrand,
                          long violationCount, int uniqueCheckCount) {
        this(sessionId, playerUuid, sessionOrdinal, startedEpochMs, lastActivityEpochMs,
                closedEpochMs, grimVersion, serverName, clientVersion, clientBrand,
                violationCount, uniqueCheckCount,
                closedEpochMs == lastActivityEpochMs && closedEpochMs > 0L);
    }

    public SessionSummary(UUID sessionId, UUID playerUuid, int sessionOrdinal,
                          long startedEpochMs, long lastActivityEpochMs, long closedEpochMs,
                          String grimVersion, String serverName, int clientVersion, String clientBrand,
                          long violationCount, int uniqueCheckCount, boolean endedUnexpectedly) {
        this.sessionId = sessionId;
        this.playerUuid = playerUuid;
        this.sessionOrdinal = sessionOrdinal;
        this.startedEpochMs = startedEpochMs;
        this.lastActivityEpochMs = lastActivityEpochMs;
        this.closedEpochMs = closedEpochMs;
        this.grimVersion = grimVersion;
        this.serverName = serverName;
        this.clientVersion = clientVersion;
        this.clientBrand = clientBrand;
        this.violationCount = violationCount;
        this.uniqueCheckCount = uniqueCheckCount;
        this.endedUnexpectedly = endedUnexpectedly;
    }

    public UUID sessionId() { return sessionId; }
    public UUID playerUuid() { return playerUuid; }
    public int sessionOrdinal() { return sessionOrdinal; }
    public long startedEpochMs() { return startedEpochMs; }
    public long lastActivityEpochMs() { return lastActivityEpochMs; }
    public long closedEpochMs() { return closedEpochMs; }
    public String grimVersion() { return grimVersion; }
    public String serverName() { return serverName; }
    public int clientVersion() { return clientVersion; }
    public String clientBrand() { return clientBrand; }
    public long violationCount() { return violationCount; }
    public int uniqueCheckCount() { return uniqueCheckCount; }
    public boolean endedUnexpectedly() { return endedUnexpectedly; }
    public long durationMs() { return Math.max(0L, lastActivityEpochMs - startedEpochMs); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionSummary)) return false;
        SessionSummary that = (SessionSummary) o;
        return sessionOrdinal == that.sessionOrdinal && startedEpochMs == that.startedEpochMs
                && lastActivityEpochMs == that.lastActivityEpochMs && closedEpochMs == that.closedEpochMs
                && clientVersion == that.clientVersion && violationCount == that.violationCount
                && uniqueCheckCount == that.uniqueCheckCount && endedUnexpectedly == that.endedUnexpectedly
                && Objects.equals(sessionId, that.sessionId) && Objects.equals(playerUuid, that.playerUuid)
                && Objects.equals(grimVersion, that.grimVersion) && Objects.equals(serverName, that.serverName)
                && Objects.equals(clientBrand, that.clientBrand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, playerUuid, sessionOrdinal, startedEpochMs, lastActivityEpochMs,
                closedEpochMs, grimVersion, serverName, clientVersion, clientBrand,
                violationCount, uniqueCheckCount, endedUnexpectedly);
    }
}
