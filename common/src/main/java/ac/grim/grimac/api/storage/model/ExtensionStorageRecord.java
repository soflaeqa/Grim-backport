package ac.grim.grimac.api.storage.model;

import java.util.Objects;
import java.util.Arrays;

public final class ExtensionStorageRecord {
    private final String categoryId;
    private final String key;
    private final byte[] value;
    private final long createdAtEpochMs;
    private final long expiresAtEpochMs;
    private final String metadata;

    public ExtensionStorageRecord(String categoryId, String key, byte[] value, long createdAtEpochMs, long expiresAtEpochMs, String metadata) {
        this.categoryId = categoryId;
        this.key = key;
        this.value = value;
        this.createdAtEpochMs = createdAtEpochMs;
        this.expiresAtEpochMs = expiresAtEpochMs;
        this.metadata = metadata;
    }

    public String categoryId() { return categoryId; }
    public String key() { return key; }
    public byte[] value() { return value; }
    public long createdAtEpochMs() { return createdAtEpochMs; }
    public long expiresAtEpochMs() { return expiresAtEpochMs; }
    public String metadata() { return metadata; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionStorageRecord)) return false;
        ExtensionStorageRecord that = (ExtensionStorageRecord) o;
        return Objects.equals(categoryId, that.categoryId) && Objects.equals(key, that.key) && Arrays.equals(value, that.value) && createdAtEpochMs == that.createdAtEpochMs && expiresAtEpochMs == that.expiresAtEpochMs && Objects.equals(metadata, that.metadata);
    }
    @Override public int hashCode() {
        int result = Objects.hash(categoryId, key, createdAtEpochMs, expiresAtEpochMs, metadata);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }
    @Override public String toString() { return "ExtensionStorageRecord[categoryId=" + categoryId + ", key=" + key + ", value=" + Arrays.toString(value) + ", createdAtEpochMs=" + createdAtEpochMs + ", expiresAtEpochMs=" + expiresAtEpochMs + ", metadata=" + metadata + "]"; }
}