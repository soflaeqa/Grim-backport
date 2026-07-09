package ac.grim.grimac.api.storage.model;

import java.util.Objects;
import java.util.Arrays;

public final class VerboseSchemaRecord {
    private final String schemaKey;
    private final int flavor;
    private final int checkId;
    private final int version;
    private final byte[] layout;
    private final long introducedAt;

    public VerboseSchemaRecord(String schemaKey, int flavor, int checkId, int version, byte[] layout, long introducedAt) {
        this.schemaKey = schemaKey;
        this.flavor = flavor;
        this.checkId = checkId;
        this.version = version;
        this.layout = layout;
        this.introducedAt = introducedAt;
    }

    public String schemaKey() { return schemaKey; }
    public int flavor() { return flavor; }
    public int checkId() { return checkId; }
    public int version() { return version; }
    public byte[] layout() { return layout; }
    public long introducedAt() { return introducedAt; }

    public static String keyOf(int flavor, int checkId, int version) { return flavor + ":" + checkId + ":" + version; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerboseSchemaRecord)) return false;
        VerboseSchemaRecord that = (VerboseSchemaRecord) o;
        return Objects.equals(schemaKey, that.schemaKey) && flavor == that.flavor && checkId == that.checkId && version == that.version && Arrays.equals(layout, that.layout) && introducedAt == that.introducedAt;
    }
    @Override public int hashCode() {
        int result = Objects.hash(schemaKey, flavor, checkId, version, introducedAt);
        result = 31 * result + Arrays.hashCode(layout);
        return result;
    }
    @Override public String toString() { return "VerboseSchemaRecord[schemaKey=" + schemaKey + ", flavor=" + flavor + ", checkId=" + checkId + ", version=" + version + ", layout=" + Arrays.toString(layout) + ", introducedAt=" + introducedAt + "]"; }
}