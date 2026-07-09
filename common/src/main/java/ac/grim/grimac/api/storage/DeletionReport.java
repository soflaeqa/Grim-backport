package ac.grim.grimac.api.storage;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public final class DeletionReport {
    public static final DeletionReport EMPTY = new DeletionReport(0, 0, 0, 0, 0);

    private final int sessionsDeleted;
    private final int violationsDeleted;
    private final int settingsDeleted;
    private final int identitiesDeleted;
    private final int blobsDeleted;

    public DeletionReport(int sessionsDeleted, int violationsDeleted, int settingsDeleted, int identitiesDeleted, int blobsDeleted) {
        this.sessionsDeleted = sessionsDeleted;
        this.violationsDeleted = violationsDeleted;
        this.settingsDeleted = settingsDeleted;
        this.identitiesDeleted = identitiesDeleted;
        this.blobsDeleted = blobsDeleted;
    }

    public int sessionsDeleted() { return sessionsDeleted; }
    public int violationsDeleted() { return violationsDeleted; }
    public int settingsDeleted() { return settingsDeleted; }
    public int identitiesDeleted() { return identitiesDeleted; }
    public int blobsDeleted() { return blobsDeleted; }

    public DeletionReport plus(DeletionReport other) {
        return new DeletionReport(
                sessionsDeleted + other.sessionsDeleted,
                violationsDeleted + other.violationsDeleted,
                settingsDeleted + other.settingsDeleted,
                identitiesDeleted + other.identitiesDeleted,
                blobsDeleted + other.blobsDeleted);
    }

    public int total() {
        return sessionsDeleted + violationsDeleted + settingsDeleted + identitiesDeleted + blobsDeleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeletionReport)) return false;
        DeletionReport that = (DeletionReport) o;
        return sessionsDeleted == that.sessionsDeleted
                && violationsDeleted == that.violationsDeleted
                && settingsDeleted == that.settingsDeleted
                && identitiesDeleted == that.identitiesDeleted
                && blobsDeleted == that.blobsDeleted;
    }

    @Override
    public int hashCode() {
        int result = sessionsDeleted;
        result = 31 * result + violationsDeleted;
        result = 31 * result + settingsDeleted;
        result = 31 * result + identitiesDeleted;
        result = 31 * result + blobsDeleted;
        return result;
    }

    @Override
    public String toString() {
        return "DeletionReport[" +
                "sessionsDeleted=" + sessionsDeleted +
                ", violationsDeleted=" + violationsDeleted +
                ", settingsDeleted=" + settingsDeleted +
                ", identitiesDeleted=" + identitiesDeleted +
                ", blobsDeleted=" + blobsDeleted +
                ']';
    }
}
