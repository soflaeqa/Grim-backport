package ac.grim.grimac.api.storage.check;

import java.util.Objects;

public final class CheckCatalogRow {
    private final int checkId;
    private final String stableKey;
    private final String display;
    private final String description;
    private final String introducedVersion;
    private final long introducedAt;

    public CheckCatalogRow(int checkId, String stableKey, String display, String description, String introducedVersion, long introducedAt) {
        this.checkId = checkId;
        this.stableKey = stableKey;
        this.display = display;
        this.description = description;
        this.introducedVersion = introducedVersion;
        this.introducedAt = introducedAt;
    }

    public int checkId() {
        return checkId;
    }

    public String stableKey() {
        return stableKey;
    }

    public String display() {
        return display;
    }

    public String description() {
        return description;
    }

    public String introducedVersion() {
        return introducedVersion;
    }

    public long introducedAt() {
        return introducedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckCatalogRow)) return false;
        CheckCatalogRow that = (CheckCatalogRow) o;
        return checkId == that.checkId
                && introducedAt == that.introducedAt
                && Objects.equals(stableKey, that.stableKey)
                && Objects.equals(display, that.display)
                && Objects.equals(description, that.description)
                && Objects.equals(introducedVersion, that.introducedVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkId, stableKey, display, description, introducedVersion, introducedAt);
    }

    @Override
    public String toString() {
        return "CheckCatalogRow[" +
                "checkId=" + checkId +
                ", stableKey=" + stableKey +
                ", display=" + display +
                ", description=" + description +
                ", introducedVersion=" + introducedVersion +
                ", introducedAt=" + introducedAt +
                ']';
    }
}
