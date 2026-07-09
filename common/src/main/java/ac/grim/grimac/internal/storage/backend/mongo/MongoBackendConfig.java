package ac.grim.grimac.internal.storage.backend.mongo;

import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.config.TableNames;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@ApiStatus.Internal
public final class MongoBackendConfig implements BackendConfig {
    private final @NotNull String connectionString;
    private final @NotNull String database;
    private final int batchFlushCap;
    private final int writerThreadsDefault;
    private final @NotNull Map<String, Integer> writerThreadsPerCategory;
    private final @NotNull TableNames tableNames;

    public MongoBackendConfig(@NotNull String connectionString,
                              @NotNull String database,
                              int batchFlushCap,
                              int writerThreadsDefault,
                              @NotNull Map<String, Integer> writerThreadsPerCategory,
                              @NotNull TableNames tableNames) {
        if (batchFlushCap <= 0) batchFlushCap = 256;
        if (writerThreadsDefault <= 0) writerThreadsDefault = 1;
        if (writerThreadsPerCategory == null) writerThreadsPerCategory = Collections.emptyMap();
        if (tableNames == null) tableNames = TableNames.DEFAULTS;

        this.connectionString = connectionString;
        this.database = database;
        this.batchFlushCap = batchFlushCap;
        this.writerThreadsDefault = writerThreadsDefault;
        this.writerThreadsPerCategory = writerThreadsPerCategory;
        this.tableNames = tableNames;
    }

    public MongoBackendConfig(@NotNull String connectionString, @NotNull String database,
                              int batchFlushCap, @NotNull TableNames tableNames) {
        this(connectionString, database, batchFlushCap, 1, Collections.<String, Integer>emptyMap(), tableNames);
    }

    public @NotNull String connectionString() { return connectionString; }
    public @NotNull String database() { return database; }
    public int batchFlushCap() { return batchFlushCap; }
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
        if (!(o instanceof MongoBackendConfig)) return false;
        MongoBackendConfig that = (MongoBackendConfig) o;
        return batchFlushCap == that.batchFlushCap && writerThreadsDefault == that.writerThreadsDefault &&
                Objects.equals(connectionString, that.connectionString) && Objects.equals(database, that.database) &&
                Objects.equals(writerThreadsPerCategory, that.writerThreadsPerCategory) && Objects.equals(tableNames, that.tableNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionString, database, batchFlushCap, writerThreadsDefault, writerThreadsPerCategory, tableNames);
    }

    @Override
    public String toString() {
        return "MongoBackendConfig[" +
                "connectionString=" + connectionString +
                ", database=" + database +
                ", batchFlushCap=" + batchFlushCap +
                ", writerThreadsDefault=" + writerThreadsDefault +
                ", writerThreadsPerCategory=" + writerThreadsPerCategory +
                ", tableNames=" + tableNames +
                ']';
    }
}
