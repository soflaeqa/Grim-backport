package ac.grim.grimac.api.storage.category;

import ac.grim.grimac.api.storage.event.BlobEvent;
import ac.grim.grimac.api.storage.event.CheckCatalogEvent;
import ac.grim.grimac.api.storage.event.PlayerIdentityEvent;
import ac.grim.grimac.api.storage.event.ServerStartupEvent;
import ac.grim.grimac.api.storage.event.SessionEvent;
import ac.grim.grimac.api.storage.event.SettingEvent;
import ac.grim.grimac.api.storage.event.VerboseSchemaEvent;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.model.CheckCatalogRecord;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.ServerStartupRecord;
import ac.grim.grimac.api.storage.model.SessionBlobRecord;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.SettingRecord;
import ac.grim.grimac.api.storage.model.VerboseSchemaRecord;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.function.Supplier;

@ApiStatus.Experimental
public final class Categories {
    public static final Category<ViolationEvent> VIOLATION = new Builtin<ViolationEvent>(
            "violation", ViolationEvent.class, ViolationEvent::new, ViolationRecord.class,
            EnumSet.of(Capability.INDEXED_KV, Capability.TIMESERIES_APPEND, Capability.HISTORY), AccessPattern.TIMESERIES);

    public static final Category<SessionEvent> SESSION = new Builtin<SessionEvent>(
            "session", SessionEvent.class, SessionEvent::new, SessionRecord.class,
            EnumSet.of(Capability.INDEXED_KV, Capability.HISTORY), AccessPattern.INDEXED_KV);

    public static final Category<ServerStartupEvent> SERVER_STARTUP = new Builtin<ServerStartupEvent>(
            "server-startup", ServerStartupEvent.class, ServerStartupEvent::new, ServerStartupRecord.class,
            EnumSet.of(Capability.KIND_ENTITY), AccessPattern.INDEXED_KV);

    public static final Category<VerboseSchemaEvent> VERBOSE_SCHEMA = new Builtin<VerboseSchemaEvent>(
            "verbose-schema", VerboseSchemaEvent.class, VerboseSchemaEvent::new, VerboseSchemaRecord.class,
            EnumSet.of(Capability.KIND_ENTITY), AccessPattern.INDEXED_KV);

    public static final Category<CheckCatalogEvent> CHECK_CATALOG = new Builtin<CheckCatalogEvent>(
            "check-catalog", CheckCatalogEvent.class, CheckCatalogEvent::new, CheckCatalogRecord.class,
            EnumSet.of(Capability.KIND_ENTITY), AccessPattern.INDEXED_KV);

    public static final Category<PlayerIdentityEvent> PLAYER_IDENTITY = new Builtin<PlayerIdentityEvent>(
            "player-identity", PlayerIdentityEvent.class, PlayerIdentityEvent::new, PlayerIdentity.class,
            EnumSet.of(Capability.INDEXED_KV, Capability.PLAYER_IDENTITY), AccessPattern.INDEXED_KV);

    public static final Category<SettingEvent> SETTING = new Builtin<SettingEvent>(
            "setting", SettingEvent.class, SettingEvent::new, SettingRecord.class,
            EnumSet.of(Capability.INDEXED_KV, Capability.SETTINGS), AccessPattern.INDEXED_KV);

    public static final Category<BlobEvent> BLOB = new Builtin<BlobEvent>(
            "blob", BlobEvent.class, BlobEvent::new, SessionBlobRecord.class,
            EnumSet.of(Capability.BLOB), AccessPattern.BLOB_REF);

    private Categories() {}

    private static final class Builtin<E> implements Category<E> {
        private final String id;
        private final Class<E> eventType;
        private final Supplier<E> newEvent;
        private final Class<?> queryResultType;
        private final EnumSet<Capability> requiredCapabilities;
        private final AccessPattern accessPattern;

        private Builtin(@NotNull String id,
                        @NotNull Class<E> eventType,
                        @NotNull Supplier<E> newEvent,
                        @NotNull Class<?> queryResultType,
                        @NotNull EnumSet<Capability> requiredCapabilities,
                        @NotNull AccessPattern accessPattern) {
            this.id = id;
            this.eventType = eventType;
            this.newEvent = newEvent;
            this.queryResultType = queryResultType;
            this.requiredCapabilities = requiredCapabilities;
            this.accessPattern = accessPattern;
        }

        @Override public @NotNull String id() { return id; }
        @Override public @NotNull Class<E> eventType() { return eventType; }
        @Override public @NotNull Supplier<E> newEvent() { return newEvent; }
        @Override public @NotNull Class<?> queryResultType() { return queryResultType; }
        @Override public @NotNull EnumSet<Capability> requiredCapabilities() { return requiredCapabilities; }
        @Override public @NotNull AccessPattern accessPattern() { return accessPattern; }
    }
}
