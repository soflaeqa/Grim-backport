package ac.grim.grimac.utils.common.arguments;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.Function;

public final class SystemArgumentFactory {
    private final String namespace;
    private final Function<ArgumentOptions.Builder, ArgumentOptions.Builder> optionModifier;
    private final boolean supportEnv;

    private SystemArgumentFactory(String namespace,
                                  Function<ArgumentOptions.Builder, ArgumentOptions.Builder> optionModifier,
                                  boolean supportEnv) {
        this.namespace = namespace;
        this.optionModifier = optionModifier;
        this.supportEnv = supportEnv;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> SystemArgument<T> create(ArgumentOptions.Builder<T> builder) {
        ArgumentOptions.Builder<T> modifiedBuilder = builder;
        if (optionModifier != null) {
            modifiedBuilder = (ArgumentOptions.Builder<T>) optionModifier.apply((ArgumentOptions.Builder) builder);
        }

        ArgumentOptions<T> options = modifiedBuilder.build();
        String key = options.getKey();
        String raw = System.getProperty(key);

        if (raw == null && supportEnv) {
            raw = System.getenv(toEnvKey(key));
            if (raw == null && namespace != null && !namespace.isEmpty()) {
                raw = System.getenv(toEnvKey(namespace + "_" + key));
            }
        }

        boolean set = raw != null;
        T value = null;

        if (set) {
            value = parse(raw, options.getClazz());
        }

        if (value == null) {
            value = options.getDefaultSupplier().get();
        }

        if (value != null || options.isNullable()) {
            value = options.getModifier().apply(value);
        }

        if (!options.getVerifier().test(value)) {
            value = options.getDefaultSupplier().get();
            set = false;
        }

        return new SystemArgument<T>(key, options.getClazz(), value, set, options.getVisibility());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> T parse(String raw, Class<T> clazz) {
        if (raw == null) return null;
        String trimmed = raw.trim();

        try {
            if (clazz == String.class) {
                return (T) raw;
            }
            if (clazz == Boolean.class || clazz == Boolean.TYPE) {
                return (T) Boolean.valueOf(trimmed);
            }
            if (clazz == Integer.class || clazz == Integer.TYPE) {
                return (T) Integer.valueOf(trimmed);
            }
            if (clazz == Long.class || clazz == Long.TYPE) {
                return (T) Long.valueOf(trimmed);
            }
            if (clazz == Double.class || clazz == Double.TYPE) {
                return (T) Double.valueOf(trimmed);
            }
            if (clazz == Float.class || clazz == Float.TYPE) {
                return (T) Float.valueOf(trimmed);
            }
            if (Enum.class.isAssignableFrom(clazz)) {
                return (T) Enum.valueOf((Class<? extends Enum>) clazz.asSubclass(Enum.class), trimmed.toUpperCase(Locale.ROOT));
            }

            Method valueOf = clazz.getMethod("valueOf", String.class);
            return (T) valueOf.invoke(null, trimmed);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String toEnvKey(String key) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                out.append(Character.toUpperCase(c));
            } else {
                out.append('_');
            }
        }
        return out.toString();
    }

    public static final class Builder {
        private final String namespace;
        private Function<ArgumentOptions.Builder, ArgumentOptions.Builder> optionModifier = new Function<ArgumentOptions.Builder, ArgumentOptions.Builder>() {
            @Override
            public ArgumentOptions.Builder apply(ArgumentOptions.Builder builder) {
                return builder;
            }
        };
        private boolean supportEnv;

        private Builder(String namespace) {
            this.namespace = namespace;
        }

        public static Builder of(String namespace) {
            return new Builder(namespace);
        }

        public Builder optionModifier(Function<ArgumentOptions.Builder, ArgumentOptions.Builder> optionModifier) {
            this.optionModifier = optionModifier;
            return this;
        }

        public Builder supportEnv() {
            this.supportEnv = true;
            return this;
        }

        public SystemArgumentFactory build() {
            return new SystemArgumentFactory(namespace, optionModifier, supportEnv);
        }
    }
}
