package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.Codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Entity<ID, E, R> implements DataKind<E, R> {
    private final String name;
    private final Class<E> eventType;
    private final Supplier<E> newEvent;
    private final Class<R> recordType;
    private final Codec<R> codec;
    private final Function<E, R> eventToRecord;
    private final Class<ID> idType;
    private final Function<R, ID> idOf;
    private final List<IndexSpec> secondaryIndexes;
    private final EnumSet<Capability> requiredCapabilities;
    private final EnumSet<Capability> optionalCapabilities;

    public Entity(String name, Class<E> eventType, Supplier<E> newEvent, Class<R> recordType,
                  Codec<R> codec, Function<E, R> eventToRecord, Class<ID> idType,
                  Function<R, ID> idOf, List<IndexSpec> secondaryIndexes,
                  EnumSet<Capability> requiredCapabilities, EnumSet<Capability> optionalCapabilities) {
        this.name = name;
        this.eventType = eventType;
        this.newEvent = newEvent;
        this.recordType = recordType;
        this.codec = codec;
        this.eventToRecord = eventToRecord;
        this.idType = idType;
        this.idOf = idOf;
        this.secondaryIndexes = secondaryIndexes == null ? Collections.<IndexSpec>emptyList() : Collections.unmodifiableList(new ArrayList<IndexSpec>(secondaryIndexes));
        this.requiredCapabilities = copy(requiredCapabilities);
        this.optionalCapabilities = copy(optionalCapabilities);
    }

    public static <ID, E, R> Builder<ID, E, R> builder() { return new Builder<ID, E, R>(); }
    private static EnumSet<Capability> copy(EnumSet<Capability> set) { return set == null ? EnumSet.noneOf(Capability.class) : EnumSet.copyOf(set); }

    public String name() { return name; }
    public Class<E> eventType() { return eventType; }
    public Supplier<E> newEvent() { return newEvent; }
    public Class<R> recordType() { return recordType; }
    public Codec<R> codec() { return codec; }
    public Function<E, R> eventToRecord() { return eventToRecord; }
    public Class<ID> idType() { return idType; }
    public Function<R, ID> idOf() { return idOf; }
    public List<IndexSpec> secondaryIndexes() { return secondaryIndexes; }
    public EnumSet<Capability> requiredCapabilities() { return copy(requiredCapabilities); }
    public EnumSet<Capability> optionalCapabilities() { return copy(optionalCapabilities); }

    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof Entity)) return false; Entity<?, ?, ?> entity = (Entity<?, ?, ?>) o; return Objects.equals(name, entity.name) && Objects.equals(eventType, entity.eventType) && Objects.equals(newEvent, entity.newEvent) && Objects.equals(recordType, entity.recordType) && Objects.equals(codec, entity.codec) && Objects.equals(eventToRecord, entity.eventToRecord) && Objects.equals(idType, entity.idType) && Objects.equals(idOf, entity.idOf) && Objects.equals(secondaryIndexes, entity.secondaryIndexes) && Objects.equals(requiredCapabilities, entity.requiredCapabilities) && Objects.equals(optionalCapabilities, entity.optionalCapabilities); }
    @Override public int hashCode() { return Objects.hash(name, eventType, newEvent, recordType, codec, eventToRecord, idType, idOf, secondaryIndexes, requiredCapabilities, optionalCapabilities); }
    @Override public String toString() { return "Entity[name=" + name + ", eventType=" + eventType + ", recordType=" + recordType + ']'; }

    public static final class Builder<ID, E, R> {
        private String name;
        private Class<E> eventType;
        private Supplier<E> newEvent;
        private Class<R> recordType;
        private Codec<R> codec;
        private Function<E, R> eventToRecord;
        private Class<ID> idType;
        private Function<R, ID> idOf;
        private final List<IndexSpec> indexes = new ArrayList<IndexSpec>();
        private final EnumSet<Capability> required = EnumSet.noneOf(Capability.class);
        private final EnumSet<Capability> optional = EnumSet.noneOf(Capability.class);
        public Builder<ID, E, R> name(String name) { this.name = name; return this; }
        public Builder<ID, E, R> event(Class<E> eventType, Supplier<E> newEvent) { this.eventType = eventType; this.newEvent = newEvent; return this; }
        public Builder<ID, E, R> record(Class<R> recordType) { this.recordType = recordType; return this; }
        public Builder<ID, E, R> codec(Codec<R> codec) { this.codec = codec; return this; }
        public Builder<ID, E, R> eventToRecord(Function<E, R> eventToRecord) { this.eventToRecord = eventToRecord; return this; }
        public Builder<ID, E, R> id(Class<ID> idType, Function<R, ID> idOf) { this.idType = idType; this.idOf = idOf; return this; }
        public Builder<ID, E, R> secondaryIndex(IndexSpec index) { if (index != null) this.indexes.add(index); return this; }
        public Builder<ID, E, R> secondaryIndex(String name, String... fields) { this.indexes.add(IndexSpec.of(name, fields)); return this; }
        public Builder<ID, E, R> requireCapability(Capability... capabilities) { if (capabilities != null) Collections.addAll(required, capabilities); return this; }
        public Builder<ID, E, R> optionalCapability(Capability... capabilities) { if (capabilities != null) Collections.addAll(optional, capabilities); return this; }
        public Entity<ID, E, R> build() { return new Entity<ID, E, R>(name, eventType, newEvent, recordType, codec, eventToRecord, idType, idOf, indexes, required, optional); }
    }
}
