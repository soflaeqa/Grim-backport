package ac.grim.grimac.api.storage.model;

import java.util.Objects;

public final class SessionBlobRecord {
    private final java.util.UUID sessionId;
    private final java.util.UUID playerUuid;
    private final BlobRef blobRef;
    private final String kind;
    private final String codec;
    private final long startOffsetMs;
    private final long durationMs;
    private final String label;
    private final String metadata;
    private final boolean truncated;

    public SessionBlobRecord(java.util.UUID sessionId, java.util.UUID playerUuid, BlobRef blobRef, String kind, String codec, long startOffsetMs, long durationMs, String label, String metadata, boolean truncated) {
        this.sessionId = sessionId;
        this.playerUuid = playerUuid;
        this.blobRef = blobRef;
        this.kind = kind;
        this.codec = codec;
        this.startOffsetMs = startOffsetMs;
        this.durationMs = durationMs;
        this.label = label;
        this.metadata = metadata;
        this.truncated = truncated;
    }

    public java.util.UUID sessionId() { return sessionId; }
    public java.util.UUID playerUuid() { return playerUuid; }
    public BlobRef blobRef() { return blobRef; }
    public String kind() { return kind; }
    public String codec() { return codec; }
    public long startOffsetMs() { return startOffsetMs; }
    public long durationMs() { return durationMs; }
    public String label() { return label; }
    public String metadata() { return metadata; }
    public boolean truncated() { return truncated; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionBlobRecord)) return false;
        SessionBlobRecord that = (SessionBlobRecord) o;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(playerUuid, that.playerUuid) && Objects.equals(blobRef, that.blobRef) && Objects.equals(kind, that.kind) && Objects.equals(codec, that.codec) && startOffsetMs == that.startOffsetMs && durationMs == that.durationMs && Objects.equals(label, that.label) && Objects.equals(metadata, that.metadata) && truncated == that.truncated;
    }
    @Override public int hashCode() {
        return Objects.hash(sessionId, playerUuid, blobRef, kind, codec, startOffsetMs, durationMs, label, metadata, truncated);
    }
    @Override public String toString() { return "SessionBlobRecord[sessionId=" + sessionId + ", playerUuid=" + playerUuid + ", blobRef=" + blobRef + ", kind=" + kind + ", codec=" + codec + ", startOffsetMs=" + startOffsetMs + ", durationMs=" + durationMs + ", label=" + label + ", metadata=" + metadata + ", truncated=" + truncated + "]"; }
}