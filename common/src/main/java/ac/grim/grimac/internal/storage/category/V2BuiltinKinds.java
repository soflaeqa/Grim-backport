package ac.grim.grimac.internal.storage.category;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.api.storage.kind.DataKind;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Objects;

/**
 * Java 8 source shadow for Grim's built-in v2 storage kind registry.
 *
 * This class is referenced by DataStoreLifecycle, but in the Java 8 backport it may be
 * missing from the final Bukkit jar because the upstream implementation lives in the
 * newer grim-internal storage layer.
 *
 * Important:
 * This class fixes the ClassNotFound/NoClassDefFoundError for V2BuiltinKinds.
 * It does not by itself turn a no-op backend into a real SQLite/MySQL backend.
 */
public final class V2BuiltinKinds {
    private static final DataKind VIOLATIONS = new BuiltinKind("violations", Categories.VIOLATION);
    private static final DataKind CHECKS = new BuiltinKind("checks", Categories.CHECK_CATALOG);
    private static final DataKind SESSIONS = new BuiltinKind("sessions", Categories.SESSION);
    private static final DataKind PLAYERS = new BuiltinKind("players", Categories.PLAYER_IDENTITY);
    private static final DataKind SETTINGS = new BuiltinKind("settings", Categories.SETTING);
    private static final DataKind SERVER_STARTUPS = new BuiltinKind("server-startups", Categories.SERVER_STARTUP);

    private V2BuiltinKinds() {
    }

    public static @NotNull DataKind violations() {
        return VIOLATIONS;
    }

    public static @NotNull DataKind checks() {
        return CHECKS;
    }

    public static @NotNull DataKind sessions() {
        return SESSIONS;
    }

    public static @NotNull DataKind players() {
        return PLAYERS;
    }

    public static @NotNull DataKind settings() {
        return SETTINGS;
    }

    public static @NotNull DataKind serverStartups() {
        return SERVER_STARTUPS;
    }

    private static final class BuiltinKind implements DataKind {
        private final String name;
        private final Category category;

        private BuiltinKind(@NotNull String name, @NotNull Category category) {
            this.name = Objects.requireNonNull(name, "name");
            this.category = Objects.requireNonNull(category, "category");
        }

        @Override
        public @NotNull String name() {
            return name;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public @NotNull Class eventType() {
            return category.eventType();
        }

        @Override
        @SuppressWarnings("rawtypes")
        public @NotNull Class recordType() {
            return category.queryResultType();
        }

        @Override
        @SuppressWarnings("rawtypes")
        public @NotNull Codec codec() {
            /*
             * Do not call Codecs.of(...) here.
             *
             * In the Java 8 backport the codec factory may still be missing from the shaded
             * runtime. Calling Codecs.of(...) inside this registry would move the crash from
             * "missing V2BuiltinKinds" to "no codec provider installed".
             *
             * A real backend/adapter must supply or request the codec when it is actually
             * capable of storing this kind. The current NoopBackendV2 never calls this method.
             */
            return NullCodecHolder.NULL_CODEC;
        }

        @Override
        public @NotNull EnumSet<Capability> requiredCapabilities() {
            return copyCapabilities(category.requiredCapabilities());
        }

        @Override
        public @NotNull EnumSet<Capability> optionalCapabilities() {
            return EnumSet.noneOf(Capability.class);
        }

        @Override
        public String toString() {
            return "BuiltinKind[name=" + name + ", category=" + category.id() + ']';
        }
    }

    private static @NotNull EnumSet<Capability> copyCapabilities(@NotNull EnumSet<Capability> source) {
        if (source.isEmpty()) {
            return EnumSet.noneOf(Capability.class);
        }
        return EnumSet.copyOf(source);
    }

    /**
     * Minimal non-null Codec object so DataKind.codec() obeys the API contract without
     * triggering the Java 17 grim-internal codec factory during startup.
     *
     * The actual methods are intentionally supplied through a dynamic proxy because the
     * exact Codec interface shape can change between GrimAPI minor versions.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class NullCodecHolder {
        private static final Codec NULL_CODEC = (Codec) java.lang.reflect.Proxy.newProxyInstance(
                Codec.class.getClassLoader(),
                new Class[]{Codec.class},
                (proxy, method, args) -> {
                    String name = method.getName();

                    if ("toString".equals(name)) {
                        return "NullCodec";
                    }
                    if ("hashCode".equals(name)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(name)) {
                        return proxy == (args == null ? null : args[0]);
                    }

                    Class<?> returnType = method.getReturnType();
                    if (returnType == Void.TYPE) {
                        return null;
                    }
                    if (returnType == Boolean.TYPE) {
                        return false;
                    }
                    if (returnType == Byte.TYPE) {
                        return (byte) 0;
                    }
                    if (returnType == Short.TYPE) {
                        return (short) 0;
                    }
                    if (returnType == Integer.TYPE) {
                        return 0;
                    }
                    if (returnType == Long.TYPE) {
                        return 0L;
                    }
                    if (returnType == Float.TYPE) {
                        return 0.0F;
                    }
                    if (returnType == Double.TYPE) {
                        return 0.0D;
                    }
                    if (returnType == Character.TYPE) {
                        return '\0';
                    }

                    throw new UnsupportedOperationException(
                            "NullCodec from Java 8 V2BuiltinKinds patch cannot perform codec operation: "
                                    + method);
                });
    }
}
