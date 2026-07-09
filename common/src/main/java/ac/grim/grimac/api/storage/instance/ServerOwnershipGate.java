package ac.grim.grimac.api.storage.instance;

import java.util.Objects;
import java.util.UUID;

public final class ServerOwnershipGate {
    private final boolean enforced;
    private volatile boolean open;
    private volatile long deadlineNanos;
    private volatile UUID startupId;
    private volatile UUID fence;
    private volatile String closeReason;

    public ServerOwnershipGate(boolean enforced) {
        this.enforced = enforced;
        this.open = !enforced;
    }

    public static ServerOwnershipGate disabled() {
        return new ServerOwnershipGate(false);
    }

    public boolean enforced() {
        return enforced;
    }

    public void open(UUID startupId, UUID fence, long ttlMs, long safetyMarginMs) {
        this.startupId = startupId;
        this.fence = fence;
        this.deadlineNanos = deadlineFromNow(ttlMs, safetyMarginMs);
        this.closeReason = null;
        this.open = true;
    }

    public void extend(UUID startupId, UUID fence, long ttlMs, long safetyMarginMs) {
        if (!Objects.equals(this.startupId, startupId) || !Objects.equals(this.fence, fence)) {
            close("ownership identifiers changed");
            return;
        }
        this.deadlineNanos = deadlineFromNow(ttlMs, safetyMarginMs);
        this.closeReason = null;
        this.open = true;
    }

    public void close(String reason) {
        this.open = false;
        this.deadlineNanos = 0L;
        this.closeReason = reason;
    }

    public boolean allowWrites() {
        if (!enforced) return true;
        if (!open) return false;
        if (deadlineNanos <= 0L) return false;
        if (System.nanoTime() >= deadlineNanos) {
            close("ownership lease expired");
            return false;
        }
        return true;
    }

    public String closeReason() {
        return closeReason;
    }

    private static long deadlineFromNow(long ttlMs, long safetyMarginMs) {
        long safeTtlMs = Math.max(0L, ttlMs - Math.max(0L, safetyMarginMs));
        return System.nanoTime() + safeTtlMs * 1000000L;
    }
}
