package ac.grim.grimac.internal.storage.history;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.category.EventStreamCategory;
import ac.grim.grimac.api.storage.event.ServerStartupEvent;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.history.HistoryService;
import ac.grim.grimac.api.storage.history.SessionDetail;
import ac.grim.grimac.api.storage.history.SessionSummary;
import ac.grim.grimac.api.storage.model.ServerStartupRecord;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.internal.storage.checks.CheckRegistry;
import ac.grim.grimac.internal.storage.verbose.VerboseRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Java 8-compatible fallback history service.
 *
 * Storage/history is not critical for live anticheat checks, so this minimal
 * implementation keeps Grim bootable on Java 8 while the Java 17 grim-internal
 * dependency is excluded from the shaded plugin.
 */
@ApiStatus.Internal
public final class HistoryServiceImpl implements HistoryService {
    public HistoryServiceImpl(@NotNull DataStore store, @NotNull CheckRegistry checks,
                              int defaultPageSize, long defaultGroupIntervalMs) {
    }

    public HistoryServiceImpl(@NotNull DataStore store, @NotNull CheckRegistry checks,
                              int defaultPageSize, long defaultGroupIntervalMs,
                              @Nullable VerboseRegistry verboseRegistry) {
    }

    public HistoryServiceImpl withV2Violations(@NotNull EventStreamCategory<ViolationEvent, ViolationRecord> v2) {
        return this;
    }

    public HistoryServiceImpl withV2Startups(@NotNull Category<ServerStartupEvent> v2) {
        return this;
    }

    public HistoryServiceImpl withVerboseRegistry(@NotNull VerboseRegistry registry) {
        return this;
    }

    @Override
    public @NotNull CompletionStage<@NotNull Page<SessionSummary>> listSessions(
            @NotNull UUID player, @Nullable Cursor cursor, int pageSize) {
        return CompletableFuture.completedFuture(new Page<SessionSummary>(Collections.<SessionSummary>emptyList(), null));
    }

    public @NotNull CompletionStage<@NotNull Page<SessionSummary>> listSessionsPaged(
            @NotNull UUID player, int pageIndex1Based, int pageSize) {
        return CompletableFuture.completedFuture(new Page<SessionSummary>(Collections.<SessionSummary>emptyList(), null));
    }

    @Override
    public @NotNull CompletionStage<@Nullable SessionDetail> getSessionDetail(
            @NotNull UUID player, @NotNull UUID sessionId) {
        return CompletableFuture.completedFuture(null);
    }

    public @NotNull CompletionStage<@Nullable SessionDetail> getSessionDetailByOrdinal(
            @NotNull UUID player, int sessionOrdinal) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletionStage<@NotNull Long> countSessions(@NotNull UUID player) {
        return CompletableFuture.completedFuture(0L);
    }
}
