package ac.grim.grimac.api.storage.kind.ops;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public final class CounterOps {
    private CounterOps() {}

    public interface Op<R> extends Operation<R> {}

    public static final class GetOp<K> implements Op<Long> {
        private final Category<?> category;
        private final K key;
        public GetOp(Category<?> category, K key) { this.category = category; this.key = key; }
        public Category<?> category() { return category; }
        public K key() { return key; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof GetOp)) return false; GetOp<?> that = (GetOp<?>) o; return Objects.equals(category, that.category) && Objects.equals(key, that.key); }
        @Override public int hashCode() { return Objects.hash(category, key); }
        @Override public String toString() { return "GetOp[category=" + category + ", key=" + key + ']'; }
    }

    public static final class GetManyOp<K> implements Op<Map<K, Long>> {
        private final Category<?> category;
        private final Collection<K> keys;
        public GetManyOp(Category<?> category, Collection<K> keys) { this.category = category; this.keys = keys; }
        public Category<?> category() { return category; }
        public Collection<K> keys() { return keys; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof GetManyOp)) return false; GetManyOp<?> that = (GetManyOp<?>) o; return Objects.equals(category, that.category) && Objects.equals(keys, that.keys); }
        @Override public int hashCode() { return Objects.hash(category, keys); }
        @Override public String toString() { return "GetManyOp[category=" + category + ", keys=" + keys + ']'; }
    }

    public static final class IncrementByOp<K> implements Op<Long> {
        private final Category<?> category;
        private final K key;
        private final long delta;
        public IncrementByOp(Category<?> category, K key, long delta) { this.category = category; this.key = key; this.delta = delta; }
        public Category<?> category() { return category; }
        public K key() { return key; }
        public long delta() { return delta; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof IncrementByOp)) return false; IncrementByOp<?> that = (IncrementByOp<?>) o; return delta == that.delta && Objects.equals(category, that.category) && Objects.equals(key, that.key); }
        @Override public int hashCode() { return Objects.hash(category, key, delta); }
        @Override public String toString() { return "IncrementByOp[category=" + category + ", key=" + key + ", delta=" + delta + ']'; }
    }

    public static final class SetIfHigherOp<K> implements Op<Long> {
        private final Category<?> category;
        private final K key;
        private final long value;
        public SetIfHigherOp(Category<?> category, K key, long value) { this.category = category; this.key = key; this.value = value; }
        public Category<?> category() { return category; }
        public K key() { return key; }
        public long value() { return value; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof SetIfHigherOp)) return false; SetIfHigherOp<?> that = (SetIfHigherOp<?>) o; return value == that.value && Objects.equals(category, that.category) && Objects.equals(key, that.key); }
        @Override public int hashCode() { return Objects.hash(category, key, value); }
        @Override public String toString() { return "SetIfHigherOp[category=" + category + ", key=" + key + ", value=" + value + ']'; }
    }
}
