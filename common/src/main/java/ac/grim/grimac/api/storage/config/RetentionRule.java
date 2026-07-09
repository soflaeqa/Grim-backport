package ac.grim.grimac.api.storage.config;

import java.util.Objects;

public final class RetentionRule {
    public static final RetentionRule DISABLED = new RetentionRule(false, 0L);

    private final boolean enabled;
    private final long maxAgeDays;

    public RetentionRule(boolean enabled, long maxAgeDays) {
        this.enabled = enabled;
        this.maxAgeDays = maxAgeDays;
    }

    public long maxAgeMs() {
        return maxAgeDays * 24L * 60L * 60L * 1000L;
    }

    public boolean enabled() {
        return enabled;
    }

    public long maxAgeDays() {
        return maxAgeDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RetentionRule)) return false;
        RetentionRule that = (RetentionRule) o;
        return enabled == that.enabled && maxAgeDays == that.maxAgeDays;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, maxAgeDays);
    }

    @Override
    public String toString() {
        return "RetentionRule[enabled=" + enabled + ", maxAgeDays=" + maxAgeDays + "]";
    }
}
