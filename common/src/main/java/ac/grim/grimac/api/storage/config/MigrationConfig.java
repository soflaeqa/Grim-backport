package ac.grim.grimac.api.storage.config;

import java.util.Objects;

public final class MigrationConfig {
    private final boolean skip;
    private final long maxDurationMs;

    public MigrationConfig(boolean skip, long maxDurationMs) {
        this.skip = skip;
        this.maxDurationMs = maxDurationMs;
    }

    public static MigrationConfig defaults() {
        return new MigrationConfig(false, 0L);
    }

    public boolean skip() {
        return skip;
    }

    public long maxDurationMs() {
        return maxDurationMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MigrationConfig)) return false;
        MigrationConfig that = (MigrationConfig) o;
        return skip == that.skip && maxDurationMs == that.maxDurationMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skip, maxDurationMs);
    }

    @Override
    public String toString() {
        return "MigrationConfig[skip=" + skip + ", maxDurationMs=" + maxDurationMs + "]";
    }
}
