package ac.grim.grimac.api.storage.kind;

import java.util.Objects;

public final class KeyValueRecord<S, V> {
    private final S scope;
    private final String scopeKey;
    private final String key;
    private final V value;
    private final long updatedEpochMs;

    public KeyValueRecord(S scope, String scopeKey, String key, V value, long updatedEpochMs) {
        this.scope = scope;
        this.scopeKey = scopeKey;
        this.key = key;
        this.value = value;
        this.updatedEpochMs = updatedEpochMs;
    }

    public S scope() { return scope; }
    public String scopeKey() { return scopeKey; }
    public String key() { return key; }
    public V value() { return value; }
    public long updatedEpochMs() { return updatedEpochMs; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValueRecord)) return false;
        KeyValueRecord<?, ?> that = (KeyValueRecord<?, ?>) o;
        return updatedEpochMs == that.updatedEpochMs
                && Objects.equals(scope, that.scope)
                && Objects.equals(scopeKey, that.scopeKey)
                && Objects.equals(key, that.key)
                && Objects.equals(value, that.value);
    }
    @Override public int hashCode() { return Objects.hash(scope, scopeKey, key, value, updatedEpochMs); }
    @Override public String toString() { return "KeyValueRecord[scope=" + scope + ", scopeKey=" + scopeKey + ", key=" + key + ", value=" + value + ", updatedEpochMs=" + updatedEpochMs + ']'; }
}
