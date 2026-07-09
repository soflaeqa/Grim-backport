package ac.grim.grimac.api.storage.model;

import java.util.Objects;

public final class PlayerIdentity {
    private final java.util.UUID uuid;
    private final String currentName;
    private final long firstSeenEpochMs;
    private final long lastSeenEpochMs;

    public PlayerIdentity(java.util.UUID uuid, String currentName, long firstSeenEpochMs, long lastSeenEpochMs) {
        this.uuid = uuid;
        this.currentName = currentName;
        this.firstSeenEpochMs = firstSeenEpochMs;
        this.lastSeenEpochMs = lastSeenEpochMs;
    }

    public java.util.UUID uuid() { return uuid; }
    public String currentName() { return currentName; }
    public long firstSeenEpochMs() { return firstSeenEpochMs; }
    public long lastSeenEpochMs() { return lastSeenEpochMs; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerIdentity)) return false;
        PlayerIdentity that = (PlayerIdentity) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(currentName, that.currentName) && firstSeenEpochMs == that.firstSeenEpochMs && lastSeenEpochMs == that.lastSeenEpochMs;
    }
    @Override public int hashCode() {
        return Objects.hash(uuid, currentName, firstSeenEpochMs, lastSeenEpochMs);
    }
    @Override public String toString() { return "PlayerIdentity[uuid=" + uuid + ", currentName=" + currentName + ", firstSeenEpochMs=" + firstSeenEpochMs + ", lastSeenEpochMs=" + lastSeenEpochMs + "]"; }
}