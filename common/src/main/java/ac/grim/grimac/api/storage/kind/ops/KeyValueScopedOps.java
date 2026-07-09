package ac.grim.grimac.api.storage.kind.ops;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class KeyValueScopedOps {
    private KeyValueScopedOps() {}

    public interface Op<R> extends Operation<R> {}

    public static final class GetOp<S, V> implements Op<Optional<V>> {
        private final Category<?> category;
        private final S scope;
        private final String scopeKey;
        private final String key;
        public GetOp(Category<?> category, S scope, String scopeKey, String key) { this.category = category; this.scope = scope; this.scopeKey = scopeKey; this.key = key; }
        public Category<?> category() { return category; }
        public S scope() { return scope; }
        public String scopeKey() { return scopeKey; }
        public String key() { return key; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof GetOp)) return false; GetOp<?, ?> that = (GetOp<?, ?>) o; return Objects.equals(category, that.category) && Objects.equals(scope, that.scope) && Objects.equals(scopeKey, that.scopeKey) && Objects.equals(key, that.key); }
        @Override public int hashCode() { return Objects.hash(category, scope, scopeKey, key); }
        @Override public String toString() { return "GetOp[category=" + category + ", scope=" + scope + ", scopeKey=" + scopeKey + ", key=" + key + ']'; }
    }

    public static final class GetAllOp<S, V> implements Op<Map<String, V>> {
        private final Category<?> category;
        private final S scope;
        private final String scopeKey;
        public GetAllOp(Category<?> category, S scope, String scopeKey) { this.category = category; this.scope = scope; this.scopeKey = scopeKey; }
        public Category<?> category() { return category; }
        public S scope() { return scope; }
        public String scopeKey() { return scopeKey; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof GetAllOp)) return false; GetAllOp<?, ?> that = (GetAllOp<?, ?>) o; return Objects.equals(category, that.category) && Objects.equals(scope, that.scope) && Objects.equals(scopeKey, that.scopeKey); }
        @Override public int hashCode() { return Objects.hash(category, scope, scopeKey); }
        @Override public String toString() { return "GetAllOp[category=" + category + ", scope=" + scope + ", scopeKey=" + scopeKey + ']'; }
    }

    public static final class PutOp<S, V> implements Op<Void> {
        private final Category<?> category;
        private final S scope;
        private final String scopeKey;
        private final String key;
        private final V value;
        public PutOp(Category<?> category, S scope, String scopeKey, String key, V value) { this.category = category; this.scope = scope; this.scopeKey = scopeKey; this.key = key; this.value = value; }
        public Category<?> category() { return category; }
        public S scope() { return scope; }
        public String scopeKey() { return scopeKey; }
        public String key() { return key; }
        public V value() { return value; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof PutOp)) return false; PutOp<?, ?> that = (PutOp<?, ?>) o; return Objects.equals(category, that.category) && Objects.equals(scope, that.scope) && Objects.equals(scopeKey, that.scopeKey) && Objects.equals(key, that.key) && Objects.equals(value, that.value); }
        @Override public int hashCode() { return Objects.hash(category, scope, scopeKey, key, value); }
        @Override public String toString() { return "PutOp[category=" + category + ", scope=" + scope + ", scopeKey=" + scopeKey + ", key=" + key + ", value=" + value + ']'; }
    }

    public static final class PutAllOp<S, V> implements Op<Void> {
        private final Category<?> category;
        private final S scope;
        private final String scopeKey;
        private final Map<String, V> values;
        public PutAllOp(Category<?> category, S scope, String scopeKey, Map<String, V> values) { this.category = category; this.scope = scope; this.scopeKey = scopeKey; this.values = values; }
        public Category<?> category() { return category; }
        public S scope() { return scope; }
        public String scopeKey() { return scopeKey; }
        public Map<String, V> values() { return values; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof PutAllOp)) return false; PutAllOp<?, ?> that = (PutAllOp<?, ?>) o; return Objects.equals(category, that.category) && Objects.equals(scope, that.scope) && Objects.equals(scopeKey, that.scopeKey) && Objects.equals(values, that.values); }
        @Override public int hashCode() { return Objects.hash(category, scope, scopeKey, values); }
        @Override public String toString() { return "PutAllOp[category=" + category + ", scope=" + scope + ", scopeKey=" + scopeKey + ", values=" + values + ']'; }
    }

    public static final class RemoveOp<S> implements Op<Void> {
        private final Category<?> category;
        private final S scope;
        private final String scopeKey;
        private final String key;
        public RemoveOp(Category<?> category, S scope, String scopeKey, String key) { this.category = category; this.scope = scope; this.scopeKey = scopeKey; this.key = key; }
        public Category<?> category() { return category; }
        public S scope() { return scope; }
        public String scopeKey() { return scopeKey; }
        public String key() { return key; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof RemoveOp)) return false; RemoveOp<?> that = (RemoveOp<?>) o; return Objects.equals(category, that.category) && Objects.equals(scope, that.scope) && Objects.equals(scopeKey, that.scopeKey) && Objects.equals(key, that.key); }
        @Override public int hashCode() { return Objects.hash(category, scope, scopeKey, key); }
        @Override public String toString() { return "RemoveOp[category=" + category + ", scope=" + scope + ", scopeKey=" + scopeKey + ", key=" + key + ']'; }
    }

    public static final class RemoveAllOp<S> implements Op<Void> {
        private final Category<?> category;
        private final S scope;
        private final String scopeKey;
        public RemoveAllOp(Category<?> category, S scope, String scopeKey) { this.category = category; this.scope = scope; this.scopeKey = scopeKey; }
        public Category<?> category() { return category; }
        public S scope() { return scope; }
        public String scopeKey() { return scopeKey; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof RemoveAllOp)) return false; RemoveAllOp<?> that = (RemoveAllOp<?>) o; return Objects.equals(category, that.category) && Objects.equals(scope, that.scope) && Objects.equals(scopeKey, that.scopeKey); }
        @Override public int hashCode() { return Objects.hash(category, scope, scopeKey); }
        @Override public String toString() { return "RemoveAllOp[category=" + category + ", scope=" + scope + ", scopeKey=" + scopeKey + ']'; }
    }

    public static final class CountOp<S> implements Op<Long> {
        private final Category<?> category;
        private final S scope;
        private final String scopeKey;
        public CountOp(Category<?> category, S scope, String scopeKey) { this.category = category; this.scope = scope; this.scopeKey = scopeKey; }
        public Category<?> category() { return category; }
        public S scope() { return scope; }
        public String scopeKey() { return scopeKey; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof CountOp)) return false; CountOp<?> that = (CountOp<?>) o; return Objects.equals(category, that.category) && Objects.equals(scope, that.scope) && Objects.equals(scopeKey, that.scopeKey); }
        @Override public int hashCode() { return Objects.hash(category, scope, scopeKey); }
        @Override public String toString() { return "CountOp[category=" + category + ", scope=" + scope + ", scopeKey=" + scopeKey + ']'; }
    }
}
