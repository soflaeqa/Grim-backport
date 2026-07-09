package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.category.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DataStoreConfig {
    private final Map<Category<?>, String> routing;
    private final Map<String, BackendConfig> backends;
    private final SessionConfig session;
    private final OwnershipConfig ownership;
    private final WritePathConfig writePath;
    private final Map<Category<?>, RetentionRule> retention;
    private final MigrationConfig migration;
    private final List<String> nameResolutionChain;
    private final HistoryConfig history;
    private final String serverName;

    public DataStoreConfig(Map<Category<?>, String> routing,
                           Map<String, BackendConfig> backends,
                           SessionConfig session,
                           OwnershipConfig ownership,
                           WritePathConfig writePath,
                           Map<Category<?>, RetentionRule> retention,
                           MigrationConfig migration,
                           List<String> nameResolutionChain,
                           HistoryConfig history,
                           String serverName) {
        this.routing = routing == null
                ? Collections.<Category<?>, String>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(routing));
        this.backends = backends == null
                ? Collections.<String, BackendConfig>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(backends));
        this.session = session == null ? SessionConfig.defaults() : session;
        this.ownership = ownership == null ? OwnershipConfig.defaults() : ownership;
        this.writePath = writePath == null ? WritePathConfig.defaults() : writePath;
        this.retention = retention == null
                ? Collections.<Category<?>, RetentionRule>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(retention));
        this.migration = migration == null ? MigrationConfig.defaults() : migration;
        this.nameResolutionChain = nameResolutionChain == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(nameResolutionChain));
        this.history = history == null ? HistoryConfig.defaults() : history;
        this.serverName = serverName == null ? "Unknown" : serverName;
    }

    public Map<Category<?>, String> routing() {
        return routing;
    }

    public Map<String, BackendConfig> backends() {
        return backends;
    }

    public SessionConfig session() {
        return session;
    }

    public OwnershipConfig ownership() {
        return ownership;
    }

    public WritePathConfig writePath() {
        return writePath;
    }

    public Map<Category<?>, RetentionRule> retention() {
        return retention;
    }

    public MigrationConfig migration() {
        return migration;
    }

    public List<String> nameResolutionChain() {
        return nameResolutionChain;
    }

    public HistoryConfig history() {
        return history;
    }

    public String serverName() {
        return serverName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataStoreConfig)) return false;
        DataStoreConfig that = (DataStoreConfig) o;
        return Objects.equals(routing, that.routing)
                && Objects.equals(backends, that.backends)
                && Objects.equals(session, that.session)
                && Objects.equals(ownership, that.ownership)
                && Objects.equals(writePath, that.writePath)
                && Objects.equals(retention, that.retention)
                && Objects.equals(migration, that.migration)
                && Objects.equals(nameResolutionChain, that.nameResolutionChain)
                && Objects.equals(history, that.history)
                && Objects.equals(serverName, that.serverName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routing, backends, session, ownership, writePath, retention,
                migration, nameResolutionChain, history, serverName);
    }

    @Override
    public String toString() {
        return "DataStoreConfig[routing=" + routing
                + ", backends=" + backends
                + ", session=" + session
                + ", ownership=" + ownership
                + ", writePath=" + writePath
                + ", retention=" + retention
                + ", migration=" + migration
                + ", nameResolutionChain=" + nameResolutionChain
                + ", history=" + history
                + ", serverName=" + serverName + "]";
    }
}
