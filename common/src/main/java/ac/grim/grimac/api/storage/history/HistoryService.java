package ac.grim.grimac.api.storage.history;

import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApiStatus.Experimental
public interface HistoryService {
    @NotNull CompletionStage<@NotNull Page<SessionSummary>> listSessions(
            @NotNull UUID player,
            @Nullable Cursor cursor,
            int pageSize);

    @NotNull CompletionStage<@Nullable SessionDetail> getSessionDetail(
            @NotNull UUID player,
            @NotNull UUID sessionId);

    @NotNull CompletionStage<@NotNull Long> countSessions(@NotNull UUID player);
}
