package ac.grim.grimac.api.storage.query;

import java.util.UUID;

public final class Deletes {
    private Deletes() {
    }

    public static ByPlayer byPlayer(UUID uuid) {
        return new ByPlayer(uuid);
    }

    public static OlderThan olderThan(long maxAgeMs) {
        return new OlderThan(maxAgeMs);
    }

    public static final class ByPlayer implements DeleteCriteria {
        private final UUID uuid;
        public ByPlayer(UUID uuid) { this.uuid = uuid; }
        public UUID uuid() { return uuid; }
        public boolean equals(Object o) { return this == o || (o instanceof ByPlayer && (uuid == null ? ((ByPlayer)o).uuid == null : uuid.equals(((ByPlayer)o).uuid))); }
        public int hashCode() { return uuid == null ? 0 : uuid.hashCode(); }
        public String toString() { return "ByPlayer[uuid=" + uuid + "]"; }
    }

    public static final class OlderThan implements DeleteCriteria {
        private final long maxAgeMs;
        public OlderThan(long maxAgeMs) { this.maxAgeMs = maxAgeMs; }
        public long maxAgeMs() { return maxAgeMs; }
        public boolean equals(Object o) { return this == o || (o instanceof OlderThan && maxAgeMs == ((OlderThan)o).maxAgeMs); }
        public int hashCode() { return (int) (maxAgeMs ^ (maxAgeMs >>> 32)); }
        public String toString() { return "OlderThan[maxAgeMs=" + maxAgeMs + "]"; }
    }
}
