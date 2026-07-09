package ac.grim.grimac.api.storage.history;

import java.util.Objects;

public final class CheckCount {
    private final int checkId;
    private final String stableKey;
    private final String displayName;
    private final String description;
    private final int count;

    public CheckCount(int checkId, String stableKey, String displayName, String description, int count) {
        this.checkId = checkId;
        this.stableKey = stableKey;
        this.displayName = displayName;
        this.description = description;
        this.count = count;
    }

    public int checkId() { return checkId; }
    public String stableKey() { return stableKey; }
    public String displayName() { return displayName; }
    public String description() { return description; }
    public int count() { return count; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckCount)) return false;
        CheckCount that = (CheckCount) o;
        return checkId == that.checkId && count == that.count
                && Objects.equals(stableKey, that.stableKey)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkId, stableKey, displayName, description, count);
    }

    @Override
    public String toString() {
        return "CheckCount[checkId=" + checkId + ", stableKey=" + stableKey
                + ", displayName=" + displayName + ", description=" + description
                + ", count=" + count + "]";
    }
}
