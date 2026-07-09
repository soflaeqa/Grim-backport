package ac.grim.grimac.api.storage.model;

import java.util.Objects;
import java.util.Arrays;

public final class SettingRecord {
    private final SettingScope scope;
    private final String scopeKey;
    private final String key;
    private final byte[] value;
    private final long updatedEpochMs;

    public SettingRecord(SettingScope scope, String scopeKey, String key, byte[] value, long updatedEpochMs) {
        this.scope = scope;
        this.scopeKey = scopeKey;
        this.key = key;
        this.value = value;
        this.updatedEpochMs = updatedEpochMs;
    }

    public SettingScope scope() { return scope; }
    public String scopeKey() { return scopeKey; }
    public String key() { return key; }
    public byte[] value() { return value; }
    public long updatedEpochMs() { return updatedEpochMs; }

    public String asString() { return value == null ? null : new String(value, java.nio.charset.StandardCharsets.UTF_8); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettingRecord)) return false;
        SettingRecord that = (SettingRecord) o;
        return Objects.equals(scope, that.scope) && Objects.equals(scopeKey, that.scopeKey) && Objects.equals(key, that.key) && Arrays.equals(value, that.value) && updatedEpochMs == that.updatedEpochMs;
    }
    @Override public int hashCode() {
        int result = Objects.hash(scope, scopeKey, key, updatedEpochMs);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }
    @Override public String toString() { return "SettingRecord[scope=" + scope + ", scopeKey=" + scopeKey + ", key=" + key + ", value=" + Arrays.toString(value) + ", updatedEpochMs=" + updatedEpochMs + "]"; }
}