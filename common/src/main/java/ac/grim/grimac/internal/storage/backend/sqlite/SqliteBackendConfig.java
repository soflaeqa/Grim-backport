package ac.grim.grimac.internal.storage.backend.sqlite;

import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.config.TableNames;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
public final class SqliteBackendConfig implements BackendConfig {
    private final @NotNull String path;
    private final @NotNull String journalMode;
    private final @NotNull String synchronousMode;
    private final int busyTimeoutMs;
    private final int cachePages;
    private final int batchFlushCap;
    private final @NotNull TableNames tableNames;

    public SqliteBackendConfig(@NotNull String path,
                               @NotNull String journalMode,
                               @NotNull String synchronousMode,
                               int busyTimeoutMs,
                               int cachePages,
                               int batchFlushCap,
                               @NotNull TableNames tableNames) {
        if (batchFlushCap <= 0) batchFlushCap = 256;
        if (tableNames == null) tableNames = TableNames.DEFAULTS;

        this.path = path;
        this.journalMode = journalMode;
        this.synchronousMode = synchronousMode;
        this.busyTimeoutMs = busyTimeoutMs;
        this.cachePages = cachePages;
        this.batchFlushCap = batchFlushCap;
        this.tableNames = tableNames;
    }

    public static SqliteBackendConfig defaults(String path) {
        return new SqliteBackendConfig(path, "WAL", "NORMAL", 5000, 10000, 256, TableNames.DEFAULTS);
    }

    public @NotNull String path() { return path; }
    public @NotNull String journalMode() { return journalMode; }
    public @NotNull String synchronousMode() { return synchronousMode; }
    public int busyTimeoutMs() { return busyTimeoutMs; }
    public int cachePages() { return cachePages; }
    public int batchFlushCap() { return batchFlushCap; }
    public @NotNull TableNames tableNames() { return tableNames; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SqliteBackendConfig)) return false;
        SqliteBackendConfig that = (SqliteBackendConfig) o;
        return busyTimeoutMs == that.busyTimeoutMs && cachePages == that.cachePages && batchFlushCap == that.batchFlushCap &&
                Objects.equals(path, that.path) && Objects.equals(journalMode, that.journalMode) &&
                Objects.equals(synchronousMode, that.synchronousMode) && Objects.equals(tableNames, that.tableNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, journalMode, synchronousMode, busyTimeoutMs, cachePages, batchFlushCap, tableNames);
    }

    @Override
    public String toString() {
        return "SqliteBackendConfig[" +
                "path=" + path +
                ", journalMode=" + journalMode +
                ", synchronousMode=" + synchronousMode +
                ", busyTimeoutMs=" + busyTimeoutMs +
                ", cachePages=" + cachePages +
                ", batchFlushCap=" + batchFlushCap +
                ", tableNames=" + tableNames +
                ']';
    }
}
