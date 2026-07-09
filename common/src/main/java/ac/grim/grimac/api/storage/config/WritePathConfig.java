package ac.grim.grimac.api.storage.config;

import java.util.Objects;

public final class WritePathConfig {
    private final int queueCapacity;
    private final int batchSize;
    private final long flushIntervalMs;
    private final long warnRateMs;
    private final long shutdownDrainTimeoutMs;
    private final WaitStrategyType waitStrategy;

    public WritePathConfig(int queueCapacity, int batchSize, long flushIntervalMs,
                           long warnRateMs, long shutdownDrainTimeoutMs,
                           WaitStrategyType waitStrategy) {
        if (queueCapacity <= 0 || Integer.bitCount(queueCapacity) != 1) {
            throw new IllegalArgumentException("queueCapacity must be a positive power of two: " + queueCapacity);
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive: " + batchSize);
        }
        if (flushIntervalMs < 0L) {
            throw new IllegalArgumentException("flushIntervalMs must be non-negative: " + flushIntervalMs);
        }
        if (warnRateMs < 0L) {
            throw new IllegalArgumentException("warnRateMs must be non-negative: " + warnRateMs);
        }
        if (shutdownDrainTimeoutMs < 0L) {
            throw new IllegalArgumentException("shutdownDrainTimeoutMs must be non-negative: " + shutdownDrainTimeoutMs);
        }
        this.queueCapacity = queueCapacity;
        this.batchSize = batchSize;
        this.flushIntervalMs = flushIntervalMs;
        this.warnRateMs = warnRateMs;
        this.shutdownDrainTimeoutMs = shutdownDrainTimeoutMs;
        this.waitStrategy = waitStrategy == null ? WaitStrategyType.BLOCKING : waitStrategy;
    }

    public static WritePathConfig defaults() {
        return new WritePathConfig(16_384, 256, 1_000L, 10_000L, 5_000L, WaitStrategyType.BLOCKING);
    }

    public int queueCapacity() {
        return queueCapacity;
    }

    public int batchSize() {
        return batchSize;
    }

    public long flushIntervalMs() {
        return flushIntervalMs;
    }

    public long warnRateMs() {
        return warnRateMs;
    }

    public long shutdownDrainTimeoutMs() {
        return shutdownDrainTimeoutMs;
    }

    public WaitStrategyType waitStrategy() {
        return waitStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WritePathConfig)) return false;
        WritePathConfig that = (WritePathConfig) o;
        return queueCapacity == that.queueCapacity
                && batchSize == that.batchSize
                && flushIntervalMs == that.flushIntervalMs
                && warnRateMs == that.warnRateMs
                && shutdownDrainTimeoutMs == that.shutdownDrainTimeoutMs
                && waitStrategy == that.waitStrategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(queueCapacity, batchSize, flushIntervalMs, warnRateMs, shutdownDrainTimeoutMs, waitStrategy);
    }

    @Override
    public String toString() {
        return "WritePathConfig[queueCapacity=" + queueCapacity
                + ", batchSize=" + batchSize
                + ", flushIntervalMs=" + flushIntervalMs
                + ", warnRateMs=" + warnRateMs
                + ", shutdownDrainTimeoutMs=" + shutdownDrainTimeoutMs
                + ", waitStrategy=" + waitStrategy + "]";
    }
}
