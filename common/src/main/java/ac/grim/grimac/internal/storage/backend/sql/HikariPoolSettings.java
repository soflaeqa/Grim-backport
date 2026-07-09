package ac.grim.grimac.internal.storage.backend.sql;

import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
public final class HikariPoolSettings {
    public static final long DEFAULT_MAXIMUM_LIFETIME_MS = 1800000L;
    public static final long DEFAULT_KEEPALIVE_TIME_MS = 0L;
    public static final long DEFAULT_CONNECTION_TIMEOUT_MS = 5000L;

    private final int maximumPoolSize;
    private final int minimumIdle;
    private final long maximumLifetimeMs;
    private final long keepaliveTimeMs;
    private final long connectionTimeoutMs;

    public HikariPoolSettings(int maximumPoolSize,
                              int minimumIdle,
                              long maximumLifetimeMs,
                              long keepaliveTimeMs,
                              long connectionTimeoutMs) {
        if (maximumPoolSize < 1) {
            throw new IllegalArgumentException("pool-settings.maximum-pool-size must be at least 1");
        }
        if (minimumIdle < 0) {
            throw new IllegalArgumentException("pool-settings.minimum-idle must not be negative");
        }
        if (minimumIdle > maximumPoolSize) {
            throw new IllegalArgumentException("pool-settings.minimum-idle must not exceed maximum-pool-size");
        }
        if (maximumLifetimeMs != 0L && maximumLifetimeMs < 30000L) {
            throw new IllegalArgumentException("pool-settings.maximum-lifetime-ms must be 0 or at least 30000");
        }
        if (keepaliveTimeMs != 0L && keepaliveTimeMs < 30000L) {
            throw new IllegalArgumentException("pool-settings.keepalive-time-ms must be 0 or at least 30000");
        }
        if (maximumLifetimeMs != 0L && keepaliveTimeMs >= maximumLifetimeMs) {
            throw new IllegalArgumentException("pool-settings.keepalive-time-ms must be less than maximum-lifetime-ms");
        }
        if (connectionTimeoutMs != 0L && connectionTimeoutMs < 250L) {
            throw new IllegalArgumentException("pool-settings.connection-timeout-ms must be 0 or at least 250");
        }

        this.maximumPoolSize = maximumPoolSize;
        this.minimumIdle = minimumIdle;
        this.maximumLifetimeMs = maximumLifetimeMs;
        this.keepaliveTimeMs = keepaliveTimeMs;
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int maximumPoolSize() { return maximumPoolSize; }
    public int minimumIdle() { return minimumIdle; }
    public long maximumLifetimeMs() { return maximumLifetimeMs; }
    public long keepaliveTimeMs() { return keepaliveTimeMs; }
    public long connectionTimeoutMs() { return connectionTimeoutMs; }

    public static @NotNull HikariPoolSettings defaults(int defaultMaximumPoolSize) {
        return new HikariPoolSettings(
                defaultMaximumPoolSize,
                defaultMaximumPoolSize,
                DEFAULT_MAXIMUM_LIFETIME_MS,
                DEFAULT_KEEPALIVE_TIME_MS,
                DEFAULT_CONNECTION_TIMEOUT_MS);
    }

    public static @NotNull HikariPoolSettings readFrom(
            @NotNull BackendConfigSource source,
            int defaultMaximumPoolSize) {
        int maximumPoolSize = source.getInt("pool-settings.maximum-pool-size", defaultMaximumPoolSize);
        return new HikariPoolSettings(
                maximumPoolSize,
                source.getInt("pool-settings.minimum-idle", maximumPoolSize),
                source.getLong("pool-settings.maximum-lifetime-ms", DEFAULT_MAXIMUM_LIFETIME_MS),
                source.getLong("pool-settings.keepalive-time-ms", DEFAULT_KEEPALIVE_TIME_MS),
                source.getLong("pool-settings.connection-timeout-ms", DEFAULT_CONNECTION_TIMEOUT_MS));
    }

    public void applyTo(@NotNull HikariConfig config) {
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setMaxLifetime(maximumLifetimeMs);
        config.setKeepaliveTime(keepaliveTimeMs);
        config.setConnectionTimeout(connectionTimeoutMs);
    }

    public int connectionTimeoutSeconds() {
        if (connectionTimeoutMs == 0L) {
            return 0;
        }
        return (int) Math.max(1L, (connectionTimeoutMs + 999L) / 1000L);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HikariPoolSettings)) return false;
        HikariPoolSettings that = (HikariPoolSettings) o;
        return maximumPoolSize == that.maximumPoolSize && minimumIdle == that.minimumIdle &&
                maximumLifetimeMs == that.maximumLifetimeMs && keepaliveTimeMs == that.keepaliveTimeMs &&
                connectionTimeoutMs == that.connectionTimeoutMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maximumPoolSize, minimumIdle, maximumLifetimeMs, keepaliveTimeMs, connectionTimeoutMs);
    }

    @Override
    public String toString() {
        return "HikariPoolSettings[" +
                "maximumPoolSize=" + maximumPoolSize +
                ", minimumIdle=" + minimumIdle +
                ", maximumLifetimeMs=" + maximumLifetimeMs +
                ", keepaliveTimeMs=" + keepaliveTimeMs +
                ", connectionTimeoutMs=" + connectionTimeoutMs +
                ']';
    }
}
