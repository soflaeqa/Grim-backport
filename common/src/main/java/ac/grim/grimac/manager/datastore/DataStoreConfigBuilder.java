package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.backend.BackendRegistry;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.config.DataStoreConfig;
import ac.grim.grimac.api.storage.config.DuplicatePersistentUuidAction;
import ac.grim.grimac.api.storage.config.HistoryConfig;
import ac.grim.grimac.api.storage.config.MigrationConfig;
import ac.grim.grimac.api.storage.config.OwnershipConfig;
import ac.grim.grimac.api.storage.config.RetentionRule;
import ac.grim.grimac.api.storage.config.SessionConfig;
import ac.grim.grimac.api.storage.config.WaitStrategyType;
import ac.grim.grimac.api.storage.config.WritePathConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Builds a {@link DataStoreConfig} from the shared {@link ConfigManager}.
 */
public final class DataStoreConfigBuilder {
    private static final String NS = "database.";

    private final ConfigManager config;
    private final BackendRegistry registry;
    private final Path dataFolder;

    public DataStoreConfigBuilder(@NotNull BackendRegistry registry,
                                  @NotNull Path dataFolder,
                                  @NotNull ConfigManager config) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.dataFolder = Objects.requireNonNull(dataFolder, "dataFolder");
        this.config = Objects.requireNonNull(config, "config");
    }

    public boolean enabled() {
        return config.getBooleanElse(NS + "enabled", true);
    }

    public @NotNull DataStoreConfig build() {
        Map<Category<?>, String> routing = readRouting();
        Map<String, BackendConfig> backends = readBackends(routing);

        SessionConfig session = new SessionConfig(
                config.getLongElse(NS + "session.gap-ms", 600000L),
                config.getBooleanElse(NS + "session.scope-per-server", true),
                config.getLongElse(NS + "session.heartbeat-interval-ms", 30000L));

        OwnershipConfig ownership = readOwnership();
        WritePathConfig writePath = readWritePath();
        Map<Category<?>, RetentionRule> retention = readRetention();
        MigrationConfig migration = new MigrationConfig(
                config.getBooleanElse(NS + "migration.skip", false),
                config.getLongElse(NS + "migration.max-duration-ms", 0L));

        List<String> defaultChain = new ArrayList<String>();
        defaultChain.add("local-cache");
        defaultChain.add("offline-mode-uuid");
        List<String> chain = config.getStringListElse(NS + "name-resolution.chain", defaultChain);

        HistoryConfig history = new HistoryConfig(
                config.getIntElse(NS + "history.entries-per-page", 15),
                config.getLongElse(NS + "history.group-interval-ms", 30000L));

        String serverName = config.getStringElse(NS + "server-name", "Unknown");
        return new DataStoreConfig(
                routing, backends, session, ownership, writePath, retention,
                migration, chain, history, serverName);
    }

    private @NotNull OwnershipConfig readOwnership() {
        String actionRaw = config.getStringElse(
                NS + "ownership.duplicate-persistent-uuid-action", "disable-storage");
        DuplicatePersistentUuidAction action;
        try {
            action = DuplicatePersistentUuidAction.valueOf(
                    actionRaw.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "database.ownership.duplicate-persistent-uuid-action must be one of "
                            + "disable-storage, fail-startup, allow-unsafe (got '" + actionRaw + "')");
        }

        return new OwnershipConfig(
                config.getBooleanElse(NS + "ownership.enforce-persistent-uuid-ownership", true),
                action,
                config.getLongElse(NS + "ownership.lease-ttl-ms", 20000L),
                config.getLongElse(NS + "ownership.renew-interval-ms", 10000L),
                config.getLongElse(NS + "ownership.startup-wait-ms", 20000L),
                config.getLongElse(NS + "ownership.safety-margin-ms", 5000L),
                config.getLongElse(NS + "ownership.stale-startup-ttl-ms", 30000L),
                config.getLongElse(NS + "ownership.recovery-sweep-interval-ms", 10000L),
                config.getBooleanElse(NS + "ownership.cleanup-other-servers", true));
    }

    private Map<Category<?>, String> readRouting() {
        Map<String, Object> raw = config.getMapElse(NS + "routing", Collections.<String, Object>emptyMap());
        Map<Category<?>, String> out = new LinkedHashMap<Category<?>, String>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            Category<?> cat = categoryFor(e.getKey());
            if (cat == null) continue;
            out.put(cat, Objects.toString(e.getValue(), "none"));
        }
        return out;
    }

    private Map<String, BackendConfig> readBackends(Map<Category<?>, String> routing) {
        Map<String, BackendConfig> out = new LinkedHashMap<String, BackendConfig>();
        for (String backendId : routing.values()) {
            if (backendId.equals("none") || out.containsKey(backendId)) continue;

            BackendProvider provider = registry.lookup(backendId);
            if (provider == null) {
                throw new IllegalArgumentException("no backend provider registered for id '"
                        + backendId + "' referenced in routing (registered: "
                        + registry.registeredIds() + ")");
            }
            out.put(backendId, provider.readConfig(new PrefixedSource(config, backendId)));
        }
        return out;
    }

    private WritePathConfig readWritePath() {
        int capacity = config.getIntElse(NS + "write-path.queue-capacity", 16384);
        if (capacity <= 0 || Integer.bitCount(capacity) != 1) {
            throw new IllegalArgumentException(
                    "database.write-path.queue-capacity must be a positive power of two (got "
                            + capacity + "). Example values: 4096, 8192, 16384, 32768.");
        }

        String waitRaw = config.getStringElse(NS + "write-path.wait-strategy", "BLOCKING");
        WaitStrategyType wait;
        try {
            wait = WaitStrategyType.valueOf(waitRaw.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "database.write-path.wait-strategy must be one of BLOCKING, TIMEOUT_BLOCKING, SLEEPING, "
                            + "YIELDING, BUSY_SPIN (got '" + waitRaw + "')");
        }

        return new WritePathConfig(
                capacity,
                config.getIntElse(NS + "write-path.batch-size", 256),
                config.getLongElse(NS + "write-path.flush-interval-ms", 1000L),
                config.getLongElse(NS + "write-path.warn-rate-ms", 10000L),
                config.getLongElse(NS + "write-path.shutdown-drain-timeout-ms", 5000L),
                wait);
    }

    private Map<Category<?>, RetentionRule> readRetention() {
        Map<String, Object> raw = config.getMapElse(NS + "retention", Collections.<String, Object>emptyMap());
        Map<Category<?>, RetentionRule> out = new LinkedHashMap<Category<?>, RetentionRule>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            Category<?> cat = categoryFor(e.getKey());
            if (cat == null) continue;

            String base = NS + "retention." + e.getKey() + ".";
            boolean enabled = config.getBooleanElse(base + "enabled", false);
            long days = config.getLongElse(base + "max-age-days", 0L);
            out.put(cat, new RetentionRule(enabled, days));
        }
        return out;
    }

    private static Category<?> categoryFor(String id) {
        if ("violation".equals(id)) {
            return Categories.VIOLATION;
        }
        if ("session".equals(id)) {
            return Categories.SESSION;
        }
        if ("player-identity".equals(id)) {
            return Categories.PLAYER_IDENTITY;
        }
        if ("setting".equals(id)) {
            return Categories.SETTING;
        }
        if ("blob".equals(id)) {
            return Categories.BLOB;
        }
        return null;
    }

    /**
     * Adapts the shared ConfigManager into a per-backend BackendConfigSource by
     * prepending the backend-id prefix on every read.
     */
    private static final class PrefixedSource implements BackendConfigSource {
        private final ConfigManager delegate;
        private final String prefix;

        PrefixedSource(ConfigManager delegate, String backendId) {
            this.delegate = delegate;
            this.prefix = backendId + ".";
        }

        @Override
        public @NotNull String getString(@NotNull String key, @NotNull String defaultValue) {
            return delegate.getStringElse(prefix + key, defaultValue);
        }

        @Override
        public int getInt(@NotNull String key, int defaultValue) {
            return delegate.getIntElse(prefix + key, defaultValue);
        }

        @Override
        public long getLong(@NotNull String key, long defaultValue) {
            return delegate.getLongElse(prefix + key, defaultValue);
        }

        @Override
        public boolean getBoolean(@NotNull String key, boolean defaultValue) {
            return delegate.getBooleanElse(prefix + key, defaultValue);
        }

        @Override
        public @NotNull List<String> getStringList(@NotNull String key, @NotNull List<String> defaultValue) {
            return delegate.getStringListElse(prefix + key, defaultValue);
        }
    }
}
