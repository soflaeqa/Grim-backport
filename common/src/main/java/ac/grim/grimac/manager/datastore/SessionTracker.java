package ac.grim.grimac.manager.datastore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Tracks a current session id per connected player. A session is bounded by
 * the player's connection lifetime: opens on the first activity after join,
 * closes when {@link #close} is called from the disconnect path.
 */
public interface SessionTracker {
    /**
     * Ensure the player has an open session and extend its activity to {@code now}.
     */
    @NotNull UUID observeActivity(@NotNull UUID playerUuid, long now, @NotNull ClientMeta meta);

    /**
     * Periodic heartbeat from {@code pollData}. Throttles internally.
     */
    void pollHeartbeat(@NotNull UUID playerUuid, long now);

    /**
     * Final activity heartbeat for a disconnecting player + drop the in-memory state.
     */
    void close(@NotNull UUID playerUuid, long now, @NotNull ClientMeta meta);

    /**
     * Returns the current session id for a player, or {@code null}.
     */
    @Nullable UUID currentSessionId(@NotNull UUID playerUuid);

    /**
     * Optional client/server metadata stamped on every session upsert.
     */
    final class ClientMeta {
        private final String grimVersion;
        private final String clientBrand;
        private final int clientVersion;
        private final String serverVersion;

        public ClientMeta(String grimVersion, String clientBrand, int clientVersion, String serverVersion) {
            this.grimVersion = grimVersion;
            this.clientBrand = clientBrand;
            this.clientVersion = clientVersion;
            this.serverVersion = serverVersion;
        }

        public String grimVersion() {
            return grimVersion;
        }

        public String clientBrand() {
            return clientBrand;
        }

        public int clientVersion() {
            return clientVersion;
        }

        public String serverVersion() {
            return serverVersion;
        }

        public static ClientMeta empty() {
            return new ClientMeta(null, null, -1, null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClientMeta)) return false;
            ClientMeta that = (ClientMeta) o;
            return clientVersion == that.clientVersion
                    && Objects.equals(grimVersion, that.grimVersion)
                    && Objects.equals(clientBrand, that.clientBrand)
                    && Objects.equals(serverVersion, that.serverVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(grimVersion, clientBrand, clientVersion, serverVersion);
        }

        @Override
        public String toString() {
            return "ClientMeta[grimVersion=" + grimVersion
                    + ", clientBrand=" + clientBrand
                    + ", clientVersion=" + clientVersion
                    + ", serverVersion=" + serverVersion + "]";
        }
    }

    /**
     * No-op tracker returned when datastore is disabled or init failed.
     */
    SessionTracker NOOP = new SessionTracker() {
        @Override
        public @NotNull UUID observeActivity(@NotNull UUID playerUuid, long now, @NotNull ClientMeta meta) {
            return UUID.randomUUID();
        }

        @Override
        public void pollHeartbeat(@NotNull UUID playerUuid, long now) {
        }

        @Override
        public void close(@NotNull UUID playerUuid, long now, @NotNull ClientMeta meta) {
        }

        @Override
        public @Nullable UUID currentSessionId(@NotNull UUID playerUuid) {
            return null;
        }
    };
}
