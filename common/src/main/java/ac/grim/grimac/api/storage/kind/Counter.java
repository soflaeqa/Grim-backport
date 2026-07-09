package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.Codec;
import ac.grim.grimac.api.storage.codec.Codecs;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

public final class Counter<K> implements DataKind<CounterEvent<K>, Long> {
    private final String name;
    private final Class<K> keyType;
    private final Codec<K> keyCodec;
    private final EnumSet<Capability> requiredCapabilities;
    private final EnumSet<Capability> optionalCapabilities;

    public Counter(String name, Class<K> keyType, Codec<K> keyCodec, EnumSet<Capability> requiredCapabilities, EnumSet<Capability> optionalCapabilities) {
        this.name = name;
        this.keyType = keyType;
        this.keyCodec = keyCodec;
        this.requiredCapabilities = copy(requiredCapabilities);
        this.optionalCapabilities = copy(optionalCapabilities);
    }

    public static <K> Builder<K> builder() { return new Builder<K>(); }
    @SuppressWarnings({"unchecked", "rawtypes"}) public Class<CounterEvent<K>> eventType() { return (Class) CounterEvent.class; }
    @SuppressWarnings({"unchecked", "rawtypes"}) public Class<Long> recordType() { return (Class) Long.class; }
    public Codec<Long> codec() { return Codecs.of(Long.class); }
    private static EnumSet<Capability> copy(EnumSet<Capability> set) { return set == null ? EnumSet.noneOf(Capability.class) : EnumSet.copyOf(set); }
    public String name() { return name; }
    public Class<K> keyType() { return keyType; }
    public Codec<K> keyCodec() { return keyCodec; }
    public EnumSet<Capability> requiredCapabilities() { return copy(requiredCapabilities); }
    public EnumSet<Capability> optionalCapabilities() { return copy(optionalCapabilities); }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof Counter)) return false; Counter<?> counter = (Counter<?>) o; return Objects.equals(name, counter.name) && Objects.equals(keyType, counter.keyType) && Objects.equals(keyCodec, counter.keyCodec) && Objects.equals(requiredCapabilities, counter.requiredCapabilities) && Objects.equals(optionalCapabilities, counter.optionalCapabilities); }
    @Override public int hashCode() { return Objects.hash(name, keyType, keyCodec, requiredCapabilities, optionalCapabilities); }
    @Override public String toString() { return "Counter[name=" + name + ", keyType=" + keyType + ']'; }

    public static final class Builder<K> {
        private String name;
        private Class<K> keyType;
        private Codec<K> keyCodec;
        private final EnumSet<Capability> required = EnumSet.noneOf(Capability.class);
        private final EnumSet<Capability> optional = EnumSet.noneOf(Capability.class);
        public Builder<K> name(String name) { this.name = name; return this; }
        public Builder<K> key(Class<K> keyType, Codec<K> keyCodec) { this.keyType = keyType; this.keyCodec = keyCodec; return this; }
        public Builder<K> requireCapability(Capability... capabilities) { if (capabilities != null) Collections.addAll(required, capabilities); return this; }
        public Counter<K> build() { return new Counter<K>(name, keyType, keyCodec, required, optional); }
    }
}
