package ac.grim.grimac.api.storage.tx;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@ApiStatus.Experimental
public interface TxScope {
    @NotNull String tag();

    final class Player implements TxScope {
        private final UUID uuid;
        public Player(@NotNull UUID uuid) { this.uuid = uuid; }
        public @NotNull UUID uuid() { return uuid; }
        @Override public @NotNull String tag() { return "player:" + uuid; }
    }

    final class Server implements TxScope {
        private final String name;
        public Server(@NotNull String name) { this.name = name; }
        public @NotNull String name() { return name; }
        @Override public @NotNull String tag() { return "server:" + name; }
    }

    final class Custom implements TxScope {
        private final String value;
        public Custom(@NotNull String value) { this.value = value; }
        public @NotNull String value() { return value; }
        @Override public @NotNull String tag() { return value; }
    }
}
