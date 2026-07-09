package ac.grim.grimac.utils.common.arguments;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public final class SystemArgument<T> {
    private final String key;
    private final Class<T> clazz;
    private final T value;
    private final boolean set;
    private final Visibility visibility;

    public SystemArgument(String key, Class<T> clazz, T value, boolean set, Visibility visibility) {
        this.key = key;
        this.clazz = clazz;
        this.value = value;
        this.set = set;
        this.visibility = visibility == null ? Visibility.VISIBLE : visibility;
    }

    public String key() {
        return key;
    }

    public String getKey() {
        return key;
    }

    public Class<T> clazz() {
        return clazz;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public T value() {
        return value;
    }

    public T getValue() {
        return value;
    }

    public boolean set() {
        return set;
    }

    public boolean isSet() {
        return set;
    }

    public Visibility visibility() {
        return visibility;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public boolean matches(Predicate<T> predicate) {
        return predicate != null && predicate.test(value);
    }

    public <K> K mapValue(Function<T, K> mapper, K otherwise) {
        try {
            return value == null || mapper == null ? otherwise : mapper.apply(value);
        } catch (Exception ignored) {
            return otherwise;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemArgument)) return false;
        SystemArgument<?> that = (SystemArgument<?>) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "SystemArgument[key=" + key + ", value=" + value + ", set=" + set + "]";
    }

    public enum Visibility {
        VISIBLE,
        HIDDEN,
        SECRET
    }
}
