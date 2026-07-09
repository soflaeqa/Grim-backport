package ac.grim.grimac.api.storage.model;

import java.util.Objects;

public final class BlobRef {
    private final String backendId;
    private final String key;
    private final long sizeBytes;
    private final String contentType;

    public BlobRef(String backendId, String key, long sizeBytes, String contentType) {
        this.backendId = backendId;
        this.key = key;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
    }

    public String backendId() { return backendId; }
    public String key() { return key; }
    public long sizeBytes() { return sizeBytes; }
    public String contentType() { return contentType; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlobRef)) return false;
        BlobRef that = (BlobRef) o;
        return Objects.equals(backendId, that.backendId) && Objects.equals(key, that.key) && sizeBytes == that.sizeBytes && Objects.equals(contentType, that.contentType);
    }
    @Override public int hashCode() {
        return Objects.hash(backendId, key, sizeBytes, contentType);
    }
    @Override public String toString() { return "BlobRef[backendId=" + backendId + ", key=" + key + ", sizeBytes=" + sizeBytes + ", contentType=" + contentType + "]"; }
}