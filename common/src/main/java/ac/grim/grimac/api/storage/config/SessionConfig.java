package ac.grim.grimac.api.storage.config;

import java.util.Objects;

public final class SessionConfig {
    private final long gapMs;
    private final boolean scopePerServer;
    private final long heartbeatIntervalMs;

    public SessionConfig(long gapMs, boolean scopePerServer, long heartbeatIntervalMs) {
        this.gapMs = gapMs;
        this.scopePerServer = scopePerServer;
        this.heartbeatIntervalMs = heartbeatIntervalMs;
    }

    public static SessionConfig defaults() {
        return new SessionConfig(600_000L, true, 30_000L);
    }

    public long gapMs() {
        return gapMs;
    }

    public boolean scopePerServer() {
        return scopePerServer;
    }

    public long heartbeatIntervalMs() {
        return heartbeatIntervalMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionConfig)) return false;
        SessionConfig that = (SessionConfig) o;
        return gapMs == that.gapMs && scopePerServer == that.scopePerServer && heartbeatIntervalMs == that.heartbeatIntervalMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gapMs, scopePerServer, heartbeatIntervalMs);
    }

    @Override
    public String toString() {
        return "SessionConfig[gapMs=" + gapMs + ", scopePerServer=" + scopePerServer + ", heartbeatIntervalMs=" + heartbeatIntervalMs + "]";
    }
}
