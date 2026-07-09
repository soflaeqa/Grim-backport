package ac.grim.grimac.utils.data;

import java.util.Objects;

public final class IntToObjectPair<T> {
    private final int integer;
    private final T value;

    public IntToObjectPair(int integer, T value) {
        this.integer = integer;
        this.value = value;
    }

    public int integer() {
        return integer;
    }

    public int first() {
        return integer;
    }

    public int key() {
        return integer;
    }

    public int intValue() {
        return integer;
    }

    public T value() {
        return value;
    }

    public T second() {
        return value;
    }

    public T object() {
        return value;
    }

    public T right() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof IntToObjectPair)) {
            return false;
        }

        IntToObjectPair<?> that = (IntToObjectPair<?>) o;
        return integer == that.integer && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(integer, value);
    }

    @Override
    public String toString() {
        return "IntToObjectPair[integer=" + integer + ", value=" + value + "]";
    }
}
