package ac.grim.grimac.api.storage.check;

import java.util.Objects;

public final class CheckCatalogRepairResult {
    private final int mappingsApplied;
    private final long violationsUpdated;
    private final long catalogVersionsUpdated;

    public CheckCatalogRepairResult(int mappingsApplied, long violationsUpdated, long catalogVersionsUpdated) {
        this.mappingsApplied = mappingsApplied;
        this.violationsUpdated = violationsUpdated;
        this.catalogVersionsUpdated = catalogVersionsUpdated;
    }

    public int mappingsApplied() {
        return mappingsApplied;
    }

    public long violationsUpdated() {
        return violationsUpdated;
    }

    public long catalogVersionsUpdated() {
        return catalogVersionsUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckCatalogRepairResult)) return false;
        CheckCatalogRepairResult that = (CheckCatalogRepairResult) o;
        return mappingsApplied == that.mappingsApplied
                && violationsUpdated == that.violationsUpdated
                && catalogVersionsUpdated == that.catalogVersionsUpdated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingsApplied, violationsUpdated, catalogVersionsUpdated);
    }

    @Override
    public String toString() {
        return "CheckCatalogRepairResult[" +
                "mappingsApplied=" + mappingsApplied +
                ", violationsUpdated=" + violationsUpdated +
                ", catalogVersionsUpdated=" + catalogVersionsUpdated +
                ']';
    }
}
