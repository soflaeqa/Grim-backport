package ac.grim.grimac.api.storage.model;

import java.util.Objects;
import java.util.Arrays;

public final class ViolationRecord {
    private final java.util.UUID id;
    private final java.util.UUID sessionId;
    private final java.util.UUID playerUuid;
    private final int checkId;
    private final double vl;
    private final long occurredEpochMs;
    private final byte[] verboseData;
    private final VerboseFormat verboseFormat;

    public ViolationRecord(java.util.UUID id, java.util.UUID sessionId, java.util.UUID playerUuid, int checkId, double vl, long occurredEpochMs, byte[] verboseData, VerboseFormat verboseFormat) {
        this.id = id;
        this.sessionId = sessionId;
        this.playerUuid = playerUuid;
        this.checkId = checkId;
        this.vl = vl;
        this.occurredEpochMs = occurredEpochMs;
        this.verboseData = verboseData;
        this.verboseFormat = verboseFormat;
    }

    public ViolationRecord(java.util.UUID id, java.util.UUID sessionId, java.util.UUID playerUuid, int checkId, double vl, long occurredEpochMs, byte[] verboseData) {
        this(id, sessionId, playerUuid, checkId, vl, occurredEpochMs, verboseData, null);
    }

    public ViolationRecord(java.util.UUID id, java.util.UUID sessionId, java.util.UUID playerUuid, int checkId, double vl, long occurredEpochMs, String verbose, VerboseFormat verboseFormat) {
        this(id, sessionId, playerUuid, checkId, vl, occurredEpochMs, verbose == null ? null : verbose.getBytes(java.nio.charset.StandardCharsets.UTF_8), verboseFormat);
    }

    public java.util.UUID id() { return id; }
    public java.util.UUID sessionId() { return sessionId; }
    public java.util.UUID playerUuid() { return playerUuid; }
    public int checkId() { return checkId; }
    public double vl() { return vl; }
    public long occurredEpochMs() { return occurredEpochMs; }
    public byte[] verboseData() { return verboseData; }
    public VerboseFormat verboseFormat() { return verboseFormat; }

    public String verbose() { return verboseData == null ? null : new String(verboseData, java.nio.charset.StandardCharsets.UTF_8); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ViolationRecord)) return false;
        ViolationRecord that = (ViolationRecord) o;
        return Objects.equals(id, that.id) && Objects.equals(sessionId, that.sessionId) && Objects.equals(playerUuid, that.playerUuid) && checkId == that.checkId && Double.compare(that.vl, vl) == 0 && occurredEpochMs == that.occurredEpochMs && Arrays.equals(verboseData, that.verboseData) && Objects.equals(verboseFormat, that.verboseFormat);
    }
    @Override public int hashCode() {
        int result = Objects.hash(id, sessionId, playerUuid, checkId, vl, occurredEpochMs, verboseFormat);
        result = 31 * result + Arrays.hashCode(verboseData);
        return result;
    }
    @Override public String toString() { return "ViolationRecord[id=" + id + ", sessionId=" + sessionId + ", playerUuid=" + playerUuid + ", checkId=" + checkId + ", vl=" + vl + ", occurredEpochMs=" + occurredEpochMs + ", verboseData=" + Arrays.toString(verboseData) + ", verboseFormat=" + verboseFormat + "]"; }
}