package ac.grim.grimac.api.storage.model;

import java.util.Objects;

public final class CheckCatalogRecord {
    private final String stableKey;
    private final int checkId;
    private final String display;
    private final String description;
    private final String introducedVersion;
    private final long introducedAt;

    public CheckCatalogRecord(String stableKey, int checkId, String display, String description, String introducedVersion, long introducedAt) {
        this.stableKey = stableKey;
        this.checkId = checkId;
        this.display = display;
        this.description = description;
        this.introducedVersion = introducedVersion;
        this.introducedAt = introducedAt;
    }

    public String stableKey() { return stableKey; }
    public int checkId() { return checkId; }
    public String display() { return display; }
    public String description() { return description; }
    public String introducedVersion() { return introducedVersion; }
    public long introducedAt() { return introducedAt; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckCatalogRecord)) return false;
        CheckCatalogRecord that = (CheckCatalogRecord) o;
        return Objects.equals(stableKey, that.stableKey) && checkId == that.checkId && Objects.equals(display, that.display) && Objects.equals(description, that.description) && Objects.equals(introducedVersion, that.introducedVersion) && introducedAt == that.introducedAt;
    }
    @Override public int hashCode() {
        return Objects.hash(stableKey, checkId, display, description, introducedVersion, introducedAt);
    }
    @Override public String toString() { return "CheckCatalogRecord[stableKey=" + stableKey + ", checkId=" + checkId + ", display=" + display + ", description=" + description + ", introducedVersion=" + introducedVersion + ", introducedAt=" + introducedAt + "]"; }
}