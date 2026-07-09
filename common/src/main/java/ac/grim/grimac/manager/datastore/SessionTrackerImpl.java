package ac.grim.grimac.manager.datastore;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.model.SessionRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concrete {@link SessionTracker}. The disabled-datastore path uses
 * {@link SessionTracker#NOOP} instead.
 */
public final class SessionTrackerImpl implements SessionTracker {

    private final DataStore store;
    private final long heartbeatIntervalMs;
    private final @Nullable UUID startupId;
    private final Map<UUID, State> states = new ConcurrentHashMap<>();

    public SessionTrackerImpl(@NotNull DataStore store, @NotNull String serverName, long heartbeatIntervalMs) {
        this(store, serverName, heartbeatIntervalMs, null);
    }

    public SessionTrackerImpl(
            @NotNull DataStore store,
            @NotNull String serverName,
            long heartbeatIntervalMs,
            @Nullable UUID startupId) {
        this.store = store;
        this.heartbeatIntervalMs = heartbeatIntervalMs;
        this.startupId = startupId;
    }

    @Override
    public @NotNull UUID observeActivity(
            @NotNull UUID playerUuid,
            long now,
            @NotNull ClientMeta meta) {
        // Lock-free CAS retry loop. get/putIfAbsent/replace hold a bin lock only for the CAS itself — no user code under the lock. UUID.randomUUID() (~1µs) runs unlocked. The loop keeps a fresh in-memory session_id on race-with-close (unconditional put would have re-inserted the closed session's id and a quick reconnect would inherit it).
        UUID candidateSessionId = null;
        while (true) {
            State current = states.get(playerUuid);
            if (current == null) {
                if (candidateSessionId == null) candidateSessionId = UUID.randomUUID();
                State fresh = new State(candidateSessionId, now, now, now, meta);
                if (states.putIfAbsent(playerUuid, fresh) == null) {
                    emit(fresh, playerUuid, now, null); // closed_at stays OPEN while alive
                    return fresh.sessionId;
                }
                // Lost the insert race — someone inserted between get and putIfAbsent. Retry as update.
                continue;
            }
            State next = new State(current.sessionId, current.startedEpochMs, now, now,
                    mergeMeta(current.cachedMeta, meta));
            if (states.replace(playerUuid, current, next)) {
                emit(next, playerUuid, now, null);
                return next.sessionId;
            }
            // CAS lost — state changed (close removed it, or another observe replaced it). Retry.
        }
    }

    @Override
    public void pollHeartbeat(@NotNull UUID playerUuid, long now) {
        if (heartbeatIntervalMs <= 0) return;
        State current = states.get(playerUuid);
        if (current == null) return;
        if (now - current.lastEmittedEpochMs < heartbeatIntervalMs) return;
        State next = new State(current.sessionId, current.startedEpochMs, now, now, current.cachedMeta);
        // CAS the new state in. If another thread (rare — pollData runs on a
        // single tick scheduler per player) beat us, just skip — they'll emit.
        if (states.replace(playerUuid, current, next)) {
            emit(next, playerUuid, now, null);
        }
    }

    @Override
    public void close(@NotNull UUID playerUuid, long now, @NotNull ClientMeta meta) {
        State prev = states.remove(playerUuid);
        if (prev == null) return;
        // Emit last_activity from the prev state (the last actual observation),
        // closed_at = now (the disconnect timestamp). They diverge by design so
        // crash recovery can stamp closed_at = last_activity for orphaned rows.
        State closed = new State(prev.sessionId, prev.startedEpochMs, prev.lastActivityEpochMs, now,
                mergeMeta(prev.cachedMeta, meta));
        emit(closed, playerUuid, prev.lastActivityEpochMs, now);
    }

    @Override
    public @Nullable UUID currentSessionId(@NotNull UUID playerUuid) {
        State s = states.get(playerUuid);
        return s == null ? null : s.sessionId;
    }

    private void emit(State s, UUID playerUuid, long now, @Nullable Long closedAt) {
        final UUID sessionId = s.sessionId;
        final long started = s.startedEpochMs;
        final ClientMeta meta = s.cachedMeta;
        store.submit(Categories.SESSION, e -> e
                .sessionId(sessionId)
                .playerUuid(playerUuid)
                .startedEpochMs(started)
                .lastActivityEpochMs(now)
                .closedAtEpochMs(closedAt == null ? SessionRecord.OPEN : closedAt)
                .clientBrand(meta.clientBrand())
                .clientVersion(meta.clientVersion())
                .startupId(startupId));
    }

    private static ClientMeta mergeMeta(ClientMeta current, ClientMeta incoming) {
        return new ClientMeta(
                latest(incoming.grimVersion(), current.grimVersion()),
                current.clientBrand() != null ? current.clientBrand() : incoming.clientBrand(),
                incoming.clientVersion() >= 0 ? incoming.clientVersion() : current.clientVersion(),
                latest(incoming.serverVersion(), current.serverVersion()));
    }

    private static <T> T latest(T incoming, T current) {
        return incoming != null ? incoming : current;
    }

    private static final class State {
        private final UUID sessionId;
        private final long startedEpochMs;
        private final long lastActivityEpochMs;
        private final long lastEmittedEpochMs;
        private final ClientMeta cachedMeta;

        private State(UUID sessionId,
                      long startedEpochMs,
                      long lastActivityEpochMs,
                      long lastEmittedEpochMs,
                      ClientMeta cachedMeta) {
            this.sessionId = sessionId;
            this.startedEpochMs = startedEpochMs;
            this.lastActivityEpochMs = lastActivityEpochMs;
            this.lastEmittedEpochMs = lastEmittedEpochMs;
            this.cachedMeta = cachedMeta;
        }

        public UUID sessionId() {
            return sessionId;
        }

        public long startedEpochMs() {
            return startedEpochMs;
        }

        public long lastActivityEpochMs() {
            return lastActivityEpochMs;
        }

        public long lastEmittedEpochMs() {
            return lastEmittedEpochMs;
        }

        public ClientMeta cachedMeta() {
            return cachedMeta;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof State)) return false;
            State state = (State) o;
            return startedEpochMs == state.startedEpochMs
                    && lastActivityEpochMs == state.lastActivityEpochMs
                    && lastEmittedEpochMs == state.lastEmittedEpochMs
                    && Objects.equals(sessionId, state.sessionId)
                    && Objects.equals(cachedMeta, state.cachedMeta);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sessionId, startedEpochMs, lastActivityEpochMs, lastEmittedEpochMs, cachedMeta);
        }

        @Override
        public String toString() {
            return "State[sessionId=" + sessionId
                    + ", startedEpochMs=" + startedEpochMs
                    + ", lastActivityEpochMs=" + lastActivityEpochMs
                    + ", lastEmittedEpochMs=" + lastEmittedEpochMs
                    + ", cachedMeta=" + cachedMeta + "]";
        }
    }
}
