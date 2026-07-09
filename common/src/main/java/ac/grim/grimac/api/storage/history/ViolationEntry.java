package ac.grim.grimac.api.storage.history;

import java.util.Objects;
import java.util.UUID;

public final class ViolationEntry {
    private final UUID sessionId;
    private final long offsetFromSessionStartMs;
    private final int checkId;
    private final String stableKey;
    private final String displayName;
    private final String description;
    private final double vl;
    private final String verbose;

    public ViolationEntry(UUID sessionId, long offsetFromSessionStartMs, int checkId,
                          String stableKey, String displayName, String description,
                          double vl, String verbose) {
        this.sessionId = sessionId;
        this.offsetFromSessionStartMs = offsetFromSessionStartMs;
        this.checkId = checkId;
        this.stableKey = stableKey;
        this.displayName = displayName;
        this.description = description;
        this.vl = vl;
        this.verbose = verbose;
    }

    public ViolationEntry(long offsetFromSessionStartMs, int checkId,
                          String stableKey, String displayName, String description,
                          double vl, String verbose) {
        this(null, offsetFromSessionStartMs, checkId, stableKey, displayName, description, vl, verbose);
    }

    public ViolationEntry(int checkId, String stableKey, String displayName, String description,
                          double vl, String verbose, long offsetFromSessionStartMs) {
        this(null, offsetFromSessionStartMs, checkId, stableKey, displayName, description, vl, verbose);
    }

    public UUID sessionId() { return sessionId; }
    public long offsetFromSessionStartMs() { return offsetFromSessionStartMs; }
    public int checkId() { return checkId; }
    public String stableKey() { return stableKey; }
    public String displayName() { return displayName; }
    public String description() { return description == null ? "" : description; }
    public double vl() { return vl; }
    public String verbose() { return verbose; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ViolationEntry)) return false;
        ViolationEntry that = (ViolationEntry) o;
        return offsetFromSessionStartMs == that.offsetFromSessionStartMs
                && checkId == that.checkId
                && Double.compare(that.vl, vl) == 0
                && Objects.equals(sessionId, that.sessionId)
                && Objects.equals(stableKey, that.stableKey)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description)
                && Objects.equals(verbose, that.verbose);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, offsetFromSessionStartMs, checkId, stableKey, displayName, description, vl, verbose);
    }

    @Override
    public String toString() {
        return "ViolationEntry[sessionId=" + sessionId + ", offsetFromSessionStartMs=" + offsetFromSessionStartMs
                + ", checkId=" + checkId + ", stableKey=" + stableKey + ", displayName=" + displayName
                + ", description=" + description + ", vl=" + vl + ", verbose=" + verbose + "]";
    }
}
