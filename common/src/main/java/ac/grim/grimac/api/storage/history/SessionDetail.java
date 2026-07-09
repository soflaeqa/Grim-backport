package ac.grim.grimac.api.storage.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class SessionDetail {
    private final UUID sessionId;
    private final UUID playerUuid;
    private final int sessionOrdinal;
    private final long startedEpochMs;
    private final long lastActivityEpochMs;
    private final String grimVersion;
    private final String serverName;
    private final int clientVersion;
    private final String clientBrand;
    private final long bucketSizeMs;
    private final int uniqueCheckCount;
    private final List<CheckBucket> buckets;
    private final List<ViolationEntry> violations;

    public SessionDetail(UUID sessionId, UUID playerUuid, int sessionOrdinal,
                         long startedEpochMs, long lastActivityEpochMs,
                         String grimVersion, String serverName, int clientVersion, String clientBrand,
                         long bucketSizeMs, int uniqueCheckCount,
                         List<CheckBucket> buckets, List<ViolationEntry> violations) {
        this.sessionId = sessionId;
        this.playerUuid = playerUuid;
        this.sessionOrdinal = sessionOrdinal;
        this.startedEpochMs = startedEpochMs;
        this.lastActivityEpochMs = lastActivityEpochMs;
        this.grimVersion = grimVersion;
        this.serverName = serverName;
        this.clientVersion = clientVersion;
        this.clientBrand = clientBrand;
        this.bucketSizeMs = bucketSizeMs;
        this.uniqueCheckCount = uniqueCheckCount;
        this.buckets = buckets == null ? Collections.<CheckBucket>emptyList()
                : Collections.unmodifiableList(new ArrayList<CheckBucket>(buckets));
        this.violations = violations == null ? Collections.<ViolationEntry>emptyList()
                : Collections.unmodifiableList(new ArrayList<ViolationEntry>(violations));
    }

    public UUID sessionId() { return sessionId; }
    public UUID playerUuid() { return playerUuid; }
    public int sessionOrdinal() { return sessionOrdinal; }
    public long startedEpochMs() { return startedEpochMs; }
    public long lastActivityEpochMs() { return lastActivityEpochMs; }
    public String grimVersion() { return grimVersion; }
    public String serverName() { return serverName; }
    public int clientVersion() { return clientVersion; }
    public String clientBrand() { return clientBrand; }
    public long bucketSizeMs() { return bucketSizeMs; }
    public int uniqueCheckCount() { return uniqueCheckCount; }
    public List<CheckBucket> buckets() { return buckets; }
    public List<ViolationEntry> violations() { return violations; }
    public long durationMs() { return Math.max(0L, lastActivityEpochMs - startedEpochMs); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionDetail)) return false;
        SessionDetail that = (SessionDetail) o;
        return sessionOrdinal == that.sessionOrdinal && startedEpochMs == that.startedEpochMs
                && lastActivityEpochMs == that.lastActivityEpochMs && clientVersion == that.clientVersion
                && bucketSizeMs == that.bucketSizeMs && uniqueCheckCount == that.uniqueCheckCount
                && Objects.equals(sessionId, that.sessionId) && Objects.equals(playerUuid, that.playerUuid)
                && Objects.equals(grimVersion, that.grimVersion) && Objects.equals(serverName, that.serverName)
                && Objects.equals(clientBrand, that.clientBrand) && Objects.equals(buckets, that.buckets)
                && Objects.equals(violations, that.violations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, playerUuid, sessionOrdinal, startedEpochMs, lastActivityEpochMs,
                grimVersion, serverName, clientVersion, clientBrand, bucketSizeMs,
                uniqueCheckCount, buckets, violations);
    }
}
