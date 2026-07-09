package ac.grim.grimac.api.storage.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CheckBucket {
    private final long bucketStartOffsetMs;
    private final List<CheckCount> checks;

    public CheckBucket(long bucketStartOffsetMs, List<CheckCount> checks) {
        this.bucketStartOffsetMs = bucketStartOffsetMs;
        this.checks = checks == null ? Collections.<CheckCount>emptyList()
                : Collections.unmodifiableList(new ArrayList<CheckCount>(checks));
    }

    public long bucketStartOffsetMs() { return bucketStartOffsetMs; }
    public List<CheckCount> checks() { return checks; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CheckBucket)) return false;
        CheckBucket that = (CheckBucket) o;
        return bucketStartOffsetMs == that.bucketStartOffsetMs && Objects.equals(checks, that.checks);
    }

    @Override
    public int hashCode() { return Objects.hash(bucketStartOffsetMs, checks); }

    @Override
    public String toString() {
        return "CheckBucket[bucketStartOffsetMs=" + bucketStartOffsetMs + ", checks=" + checks + "]";
    }
}
