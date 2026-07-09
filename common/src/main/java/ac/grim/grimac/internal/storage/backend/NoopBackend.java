package ac.grim.grimac.internal.storage.backend;

import ac.grim.grimac.api.storage.backend.ApiVersion;
import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendContext;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRepairResult;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.api.storage.query.DeleteCriteria;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Java 8 fallback backend used to keep Grim's storage SPI loadable after
 * removing the Java 17 grim-internal backend jar from the shaded plugin.
 * It accepts live writes and keeps only the check catalog in memory.
 */
public final class NoopBackend implements Backend {
    private final String id;
    private final BackendConfig config;
    private final CheckCatalogPersistence checkCatalog = new MemoryCheckCatalog();

    public NoopBackend(String id, BackendConfig config) {
        this.id = id == null ? "noop" : id;
        this.config = config;
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    @Override
    public @NotNull ApiVersion getApiVersion() {
        return ApiVersion.CURRENT;
    }

    @Override
    public @NotNull EnumSet<Capability> capabilities() {
        return EnumSet.allOf(Capability.class);
    }

    @Override
    public @NotNull Set<Category<?>> supportedCategories() {
        LinkedHashSet<Category<?>> set = new LinkedHashSet<Category<?>>();
        set.add(Categories.VIOLATION);
        set.add(Categories.SESSION);
        set.add(Categories.SERVER_STARTUP);
        set.add(Categories.VERBOSE_SCHEMA);
        set.add(Categories.CHECK_CATALOG);
        set.add(Categories.PLAYER_IDENTITY);
        set.add(Categories.SETTING);
        set.add(Categories.BLOB);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public void init(@NotNull BackendContext ctx) throws BackendException {
        // No persistent connection to initialise.
    }

    @Override
    public @NotNull CheckCatalogPersistence checkCatalog() {
        return checkCatalog;
    }

    @Override
    public @NotNull CheckCatalogRepairResult repairCheckCatalog(@NotNull Map<Integer, Integer> legacyToCatalogCheckIds,
                                                                @Nullable String introducedVersionReplacement) throws BackendException {
        return new CheckCatalogRepairResult(0, 0L, 0L);
    }

    @Override
    public void flush() throws BackendException {
    }

    @Override
    public void close() throws BackendException {
    }

    @Override
    public @NotNull <E> StorageEventHandler<E> eventHandlerFor(@NotNull Category<E> cat) throws BackendException {
        return new StorageEventHandler<E>() {
            @Override
            public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
                // Swallow live storage events. Anti-cheat checks run outside this backend.
            }
        };
    }

    @Override
    public @NotNull <R> Page<R> read(@NotNull Category<?> cat, @NotNull Query<R> query) throws BackendException {
        return Page.empty();
    }

    @Override
    public <E> void delete(@NotNull Category<E> cat, @NotNull DeleteCriteria criteria) throws BackendException {
    }

    @Override
    public <R> void bulkImport(@NotNull Category<?> cat, @NotNull List<R> records) throws BackendException {
    }

    @Override
    public long countViolationsInSession(@NotNull UUID sessionId) throws BackendException {
        return 0L;
    }

    @Override
    public long countUniqueChecksInSession(@NotNull UUID sessionId) throws BackendException {
        return 0L;
    }

    @Override
    public long countSessionsByPlayer(@NotNull UUID player) throws BackendException {
        return 0L;
    }

    @Override
    public long markCrashedSessions() throws BackendException {
        return 0L;
    }

    public BackendConfig config() {
        return config;
    }

    private static final class MemoryCheckCatalog implements CheckCatalogPersistence {
        private final AtomicInteger nextId = new AtomicInteger(1);
        private final Map<String, CheckCatalogRow> byKey = new LinkedHashMap<String, CheckCatalogRow>();
        private final Map<Integer, CheckCatalogRow> byId = new LinkedHashMap<Integer, CheckCatalogRow>();

        @Override
        public synchronized Iterable<CheckCatalogRow> loadAll() {
            return new ArrayList<CheckCatalogRow>(byId.values());
        }

        @Override
        public synchronized int insert(String stableKey, @Nullable String display, @Nullable String description,
                                       @Nullable String introducedVersion, long introducedAt) {
            CheckCatalogRow existing = byKey.get(stableKey);
            if (existing != null) return existing.checkId();
            int id = nextId.getAndIncrement();
            CheckCatalogRow row = new CheckCatalogRow(id, stableKey, display, description, introducedVersion, introducedAt);
            byKey.put(stableKey, row);
            byId.put(id, row);
            return id;
        }

        @Override
        public synchronized void upsert(CheckCatalogRow row) {
            if (row == null) return;
            byKey.put(row.stableKey(), row);
            byId.put(row.checkId(), row);
            while (nextId.get() <= row.checkId()) nextId.incrementAndGet();
        }

        @Override
        public synchronized void updateDisplayAndDescription(int checkId, @Nullable String display, @Nullable String description) {
            CheckCatalogRow old = byId.get(checkId);
            if (old == null) return;
            CheckCatalogRow row = new CheckCatalogRow(old.checkId(), old.stableKey(), display, description, old.introducedVersion(), old.introducedAt());
            byId.put(checkId, row);
            byKey.put(row.stableKey(), row);
        }
    }
}
