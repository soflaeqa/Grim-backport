package ac.grim.grimac.api.storage.config;

import java.util.Objects;

public final class HistoryConfig {
    private final int entriesPerPage;
    private final long groupIntervalMs;

    public HistoryConfig(int entriesPerPage, long groupIntervalMs) {
        this.entriesPerPage = entriesPerPage;
        this.groupIntervalMs = groupIntervalMs;
    }

    public static HistoryConfig defaults() {
        return new HistoryConfig(15, 30_000L);
    }

    public int entriesPerPage() {
        return entriesPerPage;
    }

    public long groupIntervalMs() {
        return groupIntervalMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoryConfig)) return false;
        HistoryConfig that = (HistoryConfig) o;
        return entriesPerPage == that.entriesPerPage && groupIntervalMs == that.groupIntervalMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entriesPerPage, groupIntervalMs);
    }

    @Override
    public String toString() {
        return "HistoryConfig[entriesPerPage=" + entriesPerPage + ", groupIntervalMs=" + groupIntervalMs + "]";
    }
}
