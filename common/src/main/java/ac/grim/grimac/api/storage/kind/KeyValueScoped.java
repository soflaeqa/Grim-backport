package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.api.storage.codec.Codecs;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

public final class KeyValueScoped<S, V> implements DataKind<KeyValueEvent<S, V>, KeyValueRecord<S, V>> {
    private final String name;
    private final Class<S> scopeType;
    private final Class<V> valueType;
    private final Codec<V> valueCodec;
    private final EnumSet<Capability> requiredCapabilities;
    private final EnumSet<Capability> optionalCapabilities;

    public KeyValueScoped(String name, Class<S> scopeType, Class<V> valueType, Codec<V> valueCodec, EnumSet<Capability> requiredCapabilities, EnumSet<Capability> optionalCapabilities) {
        this.name = name;
        this.scopeType = scopeType;
        this.valueType = valueType;
        this.valueCodec = valueCodec;
        this.requiredCapabilities = copy(requiredCapabilities);
        this.optionalCapabilities = copy(optionalCapabilities);
    }

    public static <S, V> Builder<S, V> builder() { return new Builder<S, V>(); }
    @SuppressWarnings({"unchecked", "rawtypes"}) public Class<KeyValueEvent<S, V>> eventType() { return (Class) KeyValueEvent.class; }
    @SuppressWarnings({"unchecked", "rawtypes"}) public Class<KeyValueRecord<S, V>> recordType() { return (Class) KeyValueRecord.class; }
    @SuppressWarnings({"unchecked", "rawtypes"}) public Codec<KeyValueRecord<S, V>> codec() { return (Codec) Codecs.of(KeyValueRecord.class); }
    private static EnumSet<Capability> copy(EnumSet<Capability> set) { return set == null ? EnumSet.noneOf(Capability.class) : EnumSet.copyOf(set); }
    public String name() { return name; }
    public Class<S> scopeType() { return scopeType; }
    public Class<V> valueType() { return valueType; }
    public Codec<V> valueCodec() { return valueCodec; }
    public EnumSet<Capability> requiredCapabilities() { return copy(requiredCapabilities); }
    public EnumSet<Capability> optionalCapabilities() { return copy(optionalCapabilities); }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof KeyValueScoped)) return false; KeyValueScoped<?, ?> that = (KeyValueScoped<?, ?>) o; return Objects.equals(name, that.name) && Objects.equals(scopeType, that.scopeType) && Objects.equals(valueType, that.valueType) && Objects.equals(valueCodec, that.valueCodec) && Objects.equals(requiredCapabilities, that.requiredCapabilities) && Objects.equals(optionalCapabilities, that.optionalCapabilities); }
    @Override public int hashCode() { return Objects.hash(name, scopeType, valueType, valueCodec, requiredCapabilities, optionalCapabilities); }
    @Override public String toString() { return "KeyValueScoped[name=" + name + ", scopeType=" + scopeType + ", valueType=" + valueType + ']'; }

    public static final class Builder<S, V> {
        private String name;
        private Class<S> scopeType;
        private Class<V> valueType;
        private Codec<V> valueCodec;
        private final EnumSet<Capability> required = EnumSet.noneOf(Capability.class);
        private final EnumSet<Capability> optional = EnumSet.noneOf(Capability.class);
        public Builder<S, V> name(String name) { this.name = name; return this; }
        public Builder<S, V> scope(Class<S> scopeType) { this.scopeType = scopeType; return this; }
        public Builder<S, V> value(Class<V> valueType, Codec<V> valueCodec) { this.valueType = valueType; this.valueCodec = valueCodec; return this; }
        public Builder<S, V> requireCapability(Capability... capabilities) { if (capabilities != null) Collections.addAll(required, capabilities); return this; }
        public Builder<S, V> optionalCapability(Capability... capabilities) { if (capabilities != null) Collections.addAll(optional, capabilities); return this; }
        public KeyValueScoped<S, V> build() { return new KeyValueScoped<S, V>(name, scopeType, valueType, valueCodec, required, optional); }
    }
}
