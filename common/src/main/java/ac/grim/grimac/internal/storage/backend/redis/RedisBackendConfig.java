package ac.grim.grimac.internal.storage.backend.redis;

import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.config.TableNames;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@ApiStatus.Internal
public final class RedisBackendConfig implements BackendConfig {
    private final @NotNull String host;
    private final int port;
    private final int database;
    private final @Nullable String user;
    private final @Nullable String password;
    private final @NotNull String keyPrefix;
    private final int timeoutMs;
    private final int batchFlushCap;
    private final boolean warnOnHistory;
    private final int writerThreadsDefault;
    private final @NotNull Map<String, Integer> writerThreadsPerCategory;
    private final @NotNull TableNames tableNames;

    public RedisBackendConfig(@NotNull String host,
                              int port,
                              int database,
                              @Nullable String user,
                              @Nullable String password,
                              @NotNull String keyPrefix,
                              int timeoutMs,
                              int batchFlushCap,
                              boolean warnOnHistory,
                              int writerThreadsDefault,
                              @NotNull Map<String, Integer> writerThreadsPerCategory,
                              @NotNull TableNames tableNames) {
        if (batchFlushCap <= 0) batchFlushCap = 256;
        if (timeoutMs <= 0) timeoutMs = 2000;
        if (writerThreadsDefault <= 0) writerThreadsDefault = 1;
        if (writerThreadsPerCategory == null) writerThreadsPerCategory = Collections.emptyMap();
        if (tableNames == null) tableNames = TableNames.DEFAULTS;
        if (keyPrefix == null) keyPrefix = "";

        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        this.keyPrefix = keyPrefix;
        this.timeoutMs = timeoutMs;
        this.batchFlushCap = batchFlushCap;
        this.warnOnHistory = warnOnHistory;
        this.writerThreadsDefault = writerThreadsDefault;
        this.writerThreadsPerCategory = writerThreadsPerCategory;
        this.tableNames = tableNames;
    }

    public RedisBackendConfig(@NotNull String host, int port, int database,
                              @Nullable String user, @Nullable String password,
                              @NotNull String keyPrefix, int timeoutMs, int batchFlushCap,
                              boolean warnOnHistory, @NotNull TableNames tableNames) {
        this(host, port, database, user, password, keyPrefix, timeoutMs, batchFlushCap,
                warnOnHistory, 1, Collections.<String, Integer>emptyMap(), tableNames);
    }

    public @NotNull String host() { return host; }
    public int port() { return port; }
    public int database() { return database; }
    public @Nullable String user() { return user; }
    public @Nullable String password() { return password; }
    public @NotNull String keyPrefix() { return keyPrefix; }
    public int timeoutMs() { return timeoutMs; }
    public int batchFlushCap() { return batchFlushCap; }
    public boolean warnOnHistory() { return warnOnHistory; }
    public int writerThreadsDefault() { return writerThreadsDefault; }
    public @NotNull Map<String, Integer> writerThreadsPerCategory() { return writerThreadsPerCategory; }
    public @NotNull TableNames tableNames() { return tableNames; }

    public int writerThreadsFor(@NotNull String categoryId) {
        Integer specific = writerThreadsPerCategory.get(categoryId);
        return specific != null ? specific : writerThreadsDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedisBackendConfig)) return false;
        RedisBackendConfig that = (RedisBackendConfig) o;
        return port == that.port && database == that.database && timeoutMs == that.timeoutMs &&
                batchFlushCap == that.batchFlushCap && warnOnHistory == that.warnOnHistory &&
                writerThreadsDefault == that.writerThreadsDefault && Objects.equals(host, that.host) &&
                Objects.equals(user, that.user) && Objects.equals(password, that.password) &&
                Objects.equals(keyPrefix, that.keyPrefix) && Objects.equals(writerThreadsPerCategory, that.writerThreadsPerCategory) &&
                Objects.equals(tableNames, that.tableNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, database, user, password, keyPrefix, timeoutMs, batchFlushCap,
                warnOnHistory, writerThreadsDefault, writerThreadsPerCategory, tableNames);
    }

    @Override
    public String toString() {
        return "RedisBackendConfig[" +
                "host=" + host +
                ", port=" + port +
                ", database=" + database +
                ", user=" + user +
                ", password=" + password +
                ", keyPrefix=" + keyPrefix +
                ", timeoutMs=" + timeoutMs +
                ", batchFlushCap=" + batchFlushCap +
                ", warnOnHistory=" + warnOnHistory +
                ", writerThreadsDefault=" + writerThreadsDefault +
                ", writerThreadsPerCategory=" + writerThreadsPerCategory +
                ", tableNames=" + tableNames +
                ']';
    }
}
