package ac.grim.grimac.internal.storage.backend.postgres;

import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.internal.storage.backend.sql.HikariPoolSettings;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@ApiStatus.Internal
public final class PostgresBackendConfig implements BackendConfig {
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 5;

    private final @NotNull String host;
    private final int port;
    private final @NotNull String database;
    private final @NotNull String user;
    private final @Nullable String password;
    private final @NotNull String extraJdbcParams;
    private final int batchFlushCap;
    private final int writerThreadsDefault;
    private final @NotNull Map<String, Integer> writerThreadsPerCategory;
    private final @NotNull HikariPoolSettings poolSettings;
    private final @NotNull TableNames tableNames;

    public PostgresBackendConfig(@NotNull String host,
                                 int port,
                                 @NotNull String database,
                                 @NotNull String user,
                                 @Nullable String password,
                                 @NotNull String extraJdbcParams,
                                 int batchFlushCap,
                                 int writerThreadsDefault,
                                 @NotNull Map<String, Integer> writerThreadsPerCategory,
                                 @NotNull HikariPoolSettings poolSettings,
                                 @NotNull TableNames tableNames) {
        if (batchFlushCap <= 0) batchFlushCap = 256;
        if (writerThreadsDefault <= 0) writerThreadsDefault = 1;
        if (writerThreadsPerCategory == null) writerThreadsPerCategory = Collections.emptyMap();
        if (poolSettings == null) poolSettings = HikariPoolSettings.defaults(DEFAULT_MAXIMUM_POOL_SIZE);
        if (tableNames == null) tableNames = TableNames.DEFAULTS;

        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        this.extraJdbcParams = extraJdbcParams;
        this.batchFlushCap = batchFlushCap;
        this.writerThreadsDefault = writerThreadsDefault;
        this.writerThreadsPerCategory = writerThreadsPerCategory;
        this.poolSettings = poolSettings;
        this.tableNames = tableNames;
    }

    public PostgresBackendConfig(@NotNull String host, int port, @NotNull String database,
                                 @NotNull String user, @Nullable String password,
                                 @NotNull String extraJdbcParams, int batchFlushCap,
                                 int writerThreadsDefault,
                                 @NotNull Map<String, Integer> writerThreadsPerCategory,
                                 @NotNull TableNames tableNames) {
        this(host, port, database, user, password, extraJdbcParams, batchFlushCap,
                writerThreadsDefault, writerThreadsPerCategory,
                HikariPoolSettings.defaults(DEFAULT_MAXIMUM_POOL_SIZE), tableNames);
    }

    public PostgresBackendConfig(@NotNull String host, int port, @NotNull String database,
                                 @NotNull String user, @Nullable String password,
                                 @NotNull String extraJdbcParams, int batchFlushCap,
                                 @NotNull TableNames tableNames) {
        this(host, port, database, user, password, extraJdbcParams, batchFlushCap,
                1, Collections.<String, Integer>emptyMap(), HikariPoolSettings.defaults(DEFAULT_MAXIMUM_POOL_SIZE), tableNames);
    }

    public @NotNull String host() { return host; }
    public int port() { return port; }
    public @NotNull String database() { return database; }
    public @NotNull String user() { return user; }
    public @Nullable String password() { return password; }
    public @NotNull String extraJdbcParams() { return extraJdbcParams; }
    public int batchFlushCap() { return batchFlushCap; }
    public int writerThreadsDefault() { return writerThreadsDefault; }
    public @NotNull Map<String, Integer> writerThreadsPerCategory() { return writerThreadsPerCategory; }
    public @NotNull HikariPoolSettings poolSettings() { return poolSettings; }
    public @NotNull TableNames tableNames() { return tableNames; }

    public int writerThreadsFor(@NotNull String categoryId) {
        Integer specific = writerThreadsPerCategory.get(categoryId);
        return specific != null ? specific : writerThreadsDefault;
    }

    public String jdbcUrl() {
        StringBuilder sb = new StringBuilder("jdbc:postgresql://").append(host).append(':').append(port)
                .append('/').append(database);
        if (!extraJdbcParams.isEmpty()) sb.append('?').append(extraJdbcParams);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostgresBackendConfig)) return false;
        PostgresBackendConfig that = (PostgresBackendConfig) o;
        return port == that.port && batchFlushCap == that.batchFlushCap && writerThreadsDefault == that.writerThreadsDefault &&
                Objects.equals(host, that.host) && Objects.equals(database, that.database) && Objects.equals(user, that.user) &&
                Objects.equals(password, that.password) && Objects.equals(extraJdbcParams, that.extraJdbcParams) &&
                Objects.equals(writerThreadsPerCategory, that.writerThreadsPerCategory) && Objects.equals(poolSettings, that.poolSettings) &&
                Objects.equals(tableNames, that.tableNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, database, user, password, extraJdbcParams, batchFlushCap,
                writerThreadsDefault, writerThreadsPerCategory, poolSettings, tableNames);
    }

    @Override
    public String toString() {
        return "PostgresBackendConfig[" +
                "host=" + host +
                ", port=" + port +
                ", database=" + database +
                ", user=" + user +
                ", password=" + password +
                ", extraJdbcParams=" + extraJdbcParams +
                ", batchFlushCap=" + batchFlushCap +
                ", writerThreadsDefault=" + writerThreadsDefault +
                ", writerThreadsPerCategory=" + writerThreadsPerCategory +
                ", poolSettings=" + poolSettings +
                ", tableNames=" + tableNames +
                ']';
    }
}
