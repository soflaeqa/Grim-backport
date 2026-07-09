package ac.grim.grimac.internal.storage.retention;

import ac.grim.grimac.api.storage.DataStore;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Java 8 compatibility placeholder for the datastore retention sweeper.
 *
 * DataStoreLifecycle creates this service during storage startup. The full
 * upstream implementation periodically deletes old history rows according to
 * retention settings. This placeholder is intentionally passive: it keeps
 * storage startup from failing, but does not delete historical rows.
 */
public final class RetentionSweeper {
    private final DataStore dataStore;
    private final Object retentionConfig;
    private final Logger logger;

    public RetentionSweeper(@NotNull DataStore dataStore,
                            @NotNull Object retentionConfig,
                            @NotNull Logger logger) {
        this.dataStore = Objects.requireNonNull(dataStore, "dataStore");
        this.retentionConfig = Objects.requireNonNull(retentionConfig, "retentionConfig");
        this.logger = Objects.requireNonNull(logger, "logger");
    }
}
