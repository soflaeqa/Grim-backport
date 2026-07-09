package ac.grim.grimac.internal.storage.history;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.category.EventStreamCategory;
import ac.grim.grimac.api.storage.event.ServerStartupEvent;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.history.CheckBucket;
import ac.grim.grimac.api.storage.history.CheckCount;
import ac.grim.grimac.api.storage.history.HistoryService;
import ac.grim.grimac.api.storage.history.SessionDetail;
import ac.grim.grimac.api.storage.history.SessionSummary;
import ac.grim.grimac.api.storage.history.ViolationEntry;
import ac.grim.grimac.api.storage.model.ServerStartupRecord;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.ServerStartupRecord;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.api.storage.kind.ops.EntityOps;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import ac.grim.grimac.internal.storage.verbose.VerboseRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Java 8 history service backed by the routed DataStore queries.
 */
@ApiStatus.Internal
public final class HistoryServiceImpl implements HistoryService {
    private final DataStore store;
    private final CheckRegistry checks;
    private final int defaultPageSize;
    private final long defaultGroupIntervalMs;
    private VerboseRegistry verboseRegistry;

    public HistoryServiceImpl(@NotNull DataStore store, @NotNull CheckRegistry checks,
                              int defaultPageSize, long defaultGroupIntervalMs) {
        this(store, checks, defaultPageSize, defaultGroupIntervalMs, null);
    }

    public HistoryServiceImpl(@NotNull DataStore store, @NotNull CheckRegistry checks,
                              int defaultPageSize, long defaultGroupIntervalMs,
                              @Nullable VerboseRegistry verboseRegistry) {
        this.store = store;
        this.checks = checks;
        this.defaultPageSize = defaultPageSize <= 0 ? 15 : defaultPageSize;
        this.defaultGroupIntervalMs = defaultGroupIntervalMs <= 0L ? 30000L : defaultGroupIntervalMs;
        this.verboseRegistry = verboseRegistry;
    }

    public HistoryServiceImpl withV2Violations(@NotNull EventStreamCategory<ViolationEvent, ViolationRecord> v2) { return this; }
    public HistoryServiceImpl withV2Startups(@NotNull Category<ServerStartupEvent> v2) { return this; }
    public HistoryServiceImpl withVerboseRegistry(@NotNull VerboseRegistry registry) { this.verboseRegistry = registry; return this; }

    @Override
    public @NotNull CompletionStage<@NotNull Page<SessionSummary>> listSessions(@NotNull UUID player, @Nullable Cursor cursor, int pageSize) {
        try {
            final int limit = pageSize <= 0 ? defaultPageSize : pageSize;
            Page<SessionRecord> page = store.query(Categories.SESSION, Queries.listSessionsByPlayer(player, limit, cursor))
                    .toCompletableFuture().join();
            int base = offset(cursor);
            List<SessionSummary> out = new ArrayList<SessionSummary>();
            List<SessionRecord> sessions = page.items();
            for (int i = 0; i < sessions.size(); i++) {
                out.add(summary(sessions.get(i), base + i + 1));
            }
            return CompletableFuture.completedFuture(new Page<SessionSummary>(out, page.nextCursor()));
        } catch (Throwable t) {
            return failed(t);
        }
    }

    public @NotNull CompletionStage<@NotNull Page<SessionSummary>> listSessionsPaged(@NotNull UUID player, int pageIndex1Based, int pageSize) {
        int limit = pageSize <= 0 ? defaultPageSize : pageSize;
        int offset = Math.max(0, pageIndex1Based - 1) * limit;
        return listSessions(player, new Cursor(String.valueOf(offset)), limit);
    }

    @Override
    public @NotNull CompletionStage<@Nullable SessionDetail> getSessionDetail(@NotNull UUID player, @NotNull UUID sessionId) {
        try {
            Page<SessionRecord> sessionPage = store.query(Categories.SESSION, Queries.getSessionById(sessionId))
                    .toCompletableFuture().join();
            if (sessionPage.items().isEmpty()) return CompletableFuture.completedFuture(null);
            SessionRecord session = sessionPage.items().get(0);
            if (session.playerUuid() != null && !player.equals(session.playerUuid())) return CompletableFuture.completedFuture(null);
            int ordinal = sessionOrdinal(player, sessionId);
            return CompletableFuture.completedFuture(detail(session, ordinal));
        } catch (Throwable t) {
            return failed(t);
        }
    }

    public @NotNull CompletionStage<@Nullable SessionDetail> getSessionDetailByOrdinal(@NotNull UUID player, int sessionOrdinal) {
        try {
            if (sessionOrdinal <= 0) return CompletableFuture.completedFuture(null);
            Page<SessionRecord> page = store.query(Categories.SESSION,
                    Queries.listSessionsByPlayer(player, 1, new Cursor(String.valueOf(sessionOrdinal - 1))))
                    .toCompletableFuture().join();
            if (page.items().isEmpty()) return CompletableFuture.completedFuture(null);
            return CompletableFuture.completedFuture(detail(page.items().get(0), sessionOrdinal));
        } catch (Throwable t) {
            return failed(t);
        }
    }

    @Override
    public @NotNull CompletionStage<@NotNull Long> countSessions(@NotNull UUID player) {
        return store.countSessionsByPlayer(player);
    }

    private SessionSummary summary(SessionRecord s, int ordinal) {
        long violations = store.countViolationsInSession(s.sessionId()).toCompletableFuture().join();
        long unique = store.countUniqueChecksInSession(s.sessionId()).toCompletableFuture().join();
        ServerStartupRecord startup = startupFor(s);
        return new SessionSummary(
                s.sessionId(), s.playerUuid(), ordinal,
                s.startedEpochMs(), s.lastActivityEpochMs(), s.closedAtEpochMs(),
                firstNonNull(s.grimVersion(), startup == null ? null : startup.grimVersion()),
                firstNonNull(s.serverName(), startup == null ? null : startup.serverName()),
                s.clientVersion(), s.clientBrand(),
                violations, (int) unique,
                s.closedAtEpochMs() == s.lastActivityEpochMs() && s.closedAtEpochMs() > 0L);
    }

    private SessionDetail detail(SessionRecord s, int ordinal) {
        List<ViolationRecord> records = allViolations(s.sessionId());
        List<ViolationEntry> entries = new ArrayList<ViolationEntry>();
        Map<Long, Map<Integer, Integer>> buckets = new LinkedHashMap<Long, Map<Integer, Integer>>();
        Set<Integer> unique = new LinkedHashSet<Integer>();

        for (ViolationRecord r : records) {
            long offset = Math.max(0L, r.occurredEpochMs() - s.startedEpochMs());
            unique.add(r.checkId());
            entries.add(new ViolationEntry(
                    s.sessionId(),
                    offset,
                    r.checkId(),
                    checks.stableKeyFor(r.checkId()).orElse(null),
                    checks.displayFor(r.checkId()).orElse("#" + r.checkId()),
                    checks.descriptionFor(r.checkId()).orElse(""),
                    r.vl(),
                    renderVerbose(r)));

            long bucketStart = (offset / defaultGroupIntervalMs) * defaultGroupIntervalMs;
            Map<Integer, Integer> counts = buckets.get(bucketStart);
            if (counts == null) {
                counts = new LinkedHashMap<Integer, Integer>();
                buckets.put(bucketStart, counts);
            }
            Integer old = counts.get(r.checkId());
            counts.put(r.checkId(), old == null ? 1 : old + 1);
        }

        List<CheckBucket> bucketList = new ArrayList<CheckBucket>();
        for (Map.Entry<Long, Map<Integer, Integer>> bucket : buckets.entrySet()) {
            List<CheckCount> checkCounts = new ArrayList<CheckCount>();
            for (Map.Entry<Integer, Integer> count : bucket.getValue().entrySet()) {
                int checkId = count.getKey();
                checkCounts.add(new CheckCount(
                        checkId,
                        checks.stableKeyFor(checkId).orElse(null),
                        checks.displayFor(checkId).orElse("#" + checkId),
                        checks.descriptionFor(checkId).orElse(""),
                        count.getValue()));
            }
            Collections.sort(checkCounts, new Comparator<CheckCount>() {
                @Override public int compare(CheckCount a, CheckCount b) { return Integer.compare(b.count(), a.count()); }
            });
            bucketList.add(new CheckBucket(bucket.getKey(), checkCounts));
        }

        ServerStartupRecord startup = startupFor(s);
        return new SessionDetail(
                s.sessionId(), s.playerUuid(), ordinal,
                s.startedEpochMs(), s.lastActivityEpochMs(),
                firstNonNull(s.grimVersion(), startup == null ? null : startup.grimVersion()),
                firstNonNull(s.serverName(), startup == null ? null : startup.serverName()),
                s.clientVersion(), s.clientBrand(),
                defaultGroupIntervalMs, unique.size(), bucketList, entries);
    }

    @SuppressWarnings("unchecked")
    private ServerStartupRecord startupFor(SessionRecord session) {
        if (session == null || session.startupId() == null) {
            return null;
        }
        try {
            java.util.Optional<ServerStartupRecord> result = store.execute(
                    new EntityOps.GetByIdOp<UUID, ServerStartupRecord>(
                            Categories.SERVER_STARTUP, session.startupId()))
                    .toCompletableFuture().join();
            return result.orElse(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String firstNonNull(String primary, String fallback) {
        return primary != null && primary.length() > 0 ? primary : fallback;
    }

    private List<ViolationRecord> allViolations(UUID sessionId) {
        List<ViolationRecord> out = new ArrayList<ViolationRecord>();
        Cursor cursor = null;
        do {
            Page<ViolationRecord> page = store.query(Categories.VIOLATION,
                    Queries.listViolationsInSession(sessionId, 500, cursor))
                    .toCompletableFuture().join();
            out.addAll(page.items());
            cursor = page.nextCursor();
        } while (cursor != null);
        return out;
    }

    private int sessionOrdinal(UUID player, UUID sessionId) {
        int offset = 0;
        Cursor cursor = null;
        do {
            Page<SessionRecord> page = store.query(Categories.SESSION, Queries.listSessionsByPlayer(player, 100, cursor))
                    .toCompletableFuture().join();
            for (SessionRecord s : page.items()) {
                offset++;
                if (sessionId.equals(s.sessionId())) return offset;
            }
            cursor = page.nextCursor();
        } while (cursor != null);
        return 1;
    }

    private String renderVerbose(ViolationRecord r) {
        String text = r.verbose();
        if (text == null) return "";
        return text;
    }

    private static int offset(Cursor cursor) {
        if (cursor == null || cursor.token() == null) return 0;
        String token = cursor.token();
        if (token.startsWith("offset:")) token = token.substring(7);
        try { return Math.max(0, Integer.parseInt(token)); }
        catch (NumberFormatException e) { return 0; }
    }

    private static <T> CompletableFuture<T> failed(Throwable t) {
        CompletableFuture<T> future = new CompletableFuture<T>();
        future.completeExceptionally(t);
        return future;
    }
}
