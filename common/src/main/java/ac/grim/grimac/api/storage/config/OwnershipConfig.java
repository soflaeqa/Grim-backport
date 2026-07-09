package ac.grim.grimac.api.storage.config;

import java.util.Objects;

public final class OwnershipConfig {
    private final boolean enforcePersistentUuidOwnership;
    private final DuplicatePersistentUuidAction duplicatePersistentUuidAction;
    private final long leaseTtlMs;
    private final long renewIntervalMs;
    private final long startupWaitMs;
    private final long safetyMarginMs;
    private final long staleStartupTtlMs;
    private final long recoverySweepIntervalMs;
    private final boolean cleanupOtherServers;

    public OwnershipConfig(boolean enforcePersistentUuidOwnership,
                           DuplicatePersistentUuidAction duplicatePersistentUuidAction,
                           long leaseTtlMs,
                           long renewIntervalMs,
                           long startupWaitMs,
                           long safetyMarginMs,
                           long staleStartupTtlMs,
                           long recoverySweepIntervalMs,
                           boolean cleanupOtherServers) {
        this.enforcePersistentUuidOwnership = enforcePersistentUuidOwnership;
        this.duplicatePersistentUuidAction = duplicatePersistentUuidAction == null
                ? DuplicatePersistentUuidAction.DISABLE_STORAGE
                : duplicatePersistentUuidAction;
        this.leaseTtlMs = Math.max(1L, leaseTtlMs);
        this.renewIntervalMs = Math.max(1L, renewIntervalMs);
        this.startupWaitMs = Math.max(0L, startupWaitMs);
        this.safetyMarginMs = Math.max(0L, safetyMarginMs);
        this.staleStartupTtlMs = Math.max(1L, staleStartupTtlMs);
        this.recoverySweepIntervalMs = Math.max(1L, recoverySweepIntervalMs);
        this.cleanupOtherServers = cleanupOtherServers;
    }

    public static OwnershipConfig defaults() {
        return new OwnershipConfig(true,
                DuplicatePersistentUuidAction.DISABLE_STORAGE,
                20_000L,
                10_000L,
                20_000L,
                5_000L,
                30_000L,
                10_000L,
                true);
    }

    public boolean enforcePersistentUuidOwnership() {
        return enforcePersistentUuidOwnership;
    }

    public DuplicatePersistentUuidAction duplicatePersistentUuidAction() {
        return duplicatePersistentUuidAction;
    }

    public long leaseTtlMs() {
        return leaseTtlMs;
    }

    public long renewIntervalMs() {
        return renewIntervalMs;
    }

    public long startupWaitMs() {
        return startupWaitMs;
    }

    public long safetyMarginMs() {
        return safetyMarginMs;
    }

    public long staleStartupTtlMs() {
        return staleStartupTtlMs;
    }

    public long recoverySweepIntervalMs() {
        return recoverySweepIntervalMs;
    }

    public boolean cleanupOtherServers() {
        return cleanupOtherServers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OwnershipConfig)) return false;
        OwnershipConfig that = (OwnershipConfig) o;
        return enforcePersistentUuidOwnership == that.enforcePersistentUuidOwnership
                && leaseTtlMs == that.leaseTtlMs
                && renewIntervalMs == that.renewIntervalMs
                && startupWaitMs == that.startupWaitMs
                && safetyMarginMs == that.safetyMarginMs
                && staleStartupTtlMs == that.staleStartupTtlMs
                && recoverySweepIntervalMs == that.recoverySweepIntervalMs
                && cleanupOtherServers == that.cleanupOtherServers
                && duplicatePersistentUuidAction == that.duplicatePersistentUuidAction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enforcePersistentUuidOwnership, duplicatePersistentUuidAction, leaseTtlMs,
                renewIntervalMs, startupWaitMs, safetyMarginMs, staleStartupTtlMs,
                recoverySweepIntervalMs, cleanupOtherServers);
    }

    @Override
    public String toString() {
        return "OwnershipConfig[enforcePersistentUuidOwnership=" + enforcePersistentUuidOwnership
                + ", duplicatePersistentUuidAction=" + duplicatePersistentUuidAction
                + ", leaseTtlMs=" + leaseTtlMs
                + ", renewIntervalMs=" + renewIntervalMs
                + ", startupWaitMs=" + startupWaitMs
                + ", safetyMarginMs=" + safetyMarginMs
                + ", staleStartupTtlMs=" + staleStartupTtlMs
                + ", recoverySweepIntervalMs=" + recoverySweepIntervalMs
                + ", cleanupOtherServers=" + cleanupOtherServers + "]";
    }
}
