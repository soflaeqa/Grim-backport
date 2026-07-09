package ac.grim.grimac.utils.data;

import java.util.Objects;

public final class ShortToLongPair {
    private final short first;
    private final long second;

    public ShortToLongPair(short first, long second) {
        this.first = first;
        this.second = second;
    }

    public short first() {
        return first;
    }

    public long second() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortToLongPair)) return false;
        ShortToLongPair that = (ShortToLongPair) o;
        return first == that.first && second == that.second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "ShortToLongPair[first=" + first + ", second=" + second + "]";
    }
}
