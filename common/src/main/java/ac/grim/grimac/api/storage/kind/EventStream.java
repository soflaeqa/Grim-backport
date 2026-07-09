package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.Codec;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class EventStream<E, R> implements DataKind<E, R> {
    private final String name;
    private final Class<E> eventType;
    private final Supplier<E> newEvent;
    private final Class<R> recordType;
    private final Codec<R> codec;
    private final Function<E, R> eventToRecord;
    private final String timestampField;
    private final List<String> partitionFields;
    private final Duration retention;
    private final Granularity granularity;
    private final EnumSet<Capability> requiredCapabilities;
    private final EnumSet<Capability> optionalCapabilities;

    public EventStream(String name, Class<E> eventType, Supplier<E> newEvent, Class<R> recordType,
                       Codec<R> codec, Function<E, R> eventToRecord, String timestampField,
                       List<String> partitionFields, Duration retention, Granularity granularity,
                       EnumSet<Capability> requiredCapabilities, EnumSet<Capability> optionalCapabilities) {
        this.name = name;
        this.eventType = eventType;
        this.newEvent = newEvent;
        this.recordType = recordType;
        this.codec = codec;
        this.eventToRecord = eventToRecord;
        this.timestampField = timestampField;
        this.partitionFields = partitionFields == null ? Collections.<String>emptyList() : Collections.unmodifiableList(partitionFields);
        this.retention = retention;
        this.granularity = granularity;
        this.requiredCapabilities = copy(requiredCapabilities);
        this.optionalCapabilities = copy(optionalCapabilities);
    }

    public static <E, R> Builder<E, R> builder() { return new Builder<E, R>(); }
    private static EnumSet<Capability> copy(EnumSet<Capability> set) { return set == null ? EnumSet.noneOf(Capability.class) : EnumSet.copyOf(set); }

    public String name() { return name; }
    public Class<E> eventType() { return eventType; }
    public Supplier<E> newEvent() { return newEvent; }
    public Class<R> recordType() { return recordType; }
    public Codec<R> codec() { return codec; }
    public Function<E, R> eventToRecord() { return eventToRecord; }
    public String timestampField() { return timestampField; }
    public List<String> partitionFields() { return partitionFields; }
    public Duration retention() { return retention; }
    public Granularity granularity() { return granularity; }
    public EnumSet<Capability> requiredCapabilities() { return copy(requiredCapabilities); }
    public EnumSet<Capability> optionalCapabilities() { return copy(optionalCapabilities); }

    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof EventStream)) return false; EventStream<?, ?> that = (EventStream<?, ?>) o; return Objects.equals(name, that.name) && Objects.equals(eventType, that.eventType) && Objects.equals(newEvent, that.newEvent) && Objects.equals(recordType, that.recordType) && Objects.equals(codec, that.codec) && Objects.equals(eventToRecord, that.eventToRecord) && Objects.equals(timestampField, that.timestampField) && Objects.equals(partitionFields, that.partitionFields) && Objects.equals(retention, that.retention) && granularity == that.granularity && Objects.equals(requiredCapabilities, that.requiredCapabilities) && Objects.equals(optionalCapabilities, that.optionalCapabilities); }
    @Override public int hashCode() { return Objects.hash(name, eventType, newEvent, recordType, codec, eventToRecord, timestampField, partitionFields, retention, granularity, requiredCapabilities, optionalCapabilities); }
    @Override public String toString() { return "EventStream[name=" + name + ", eventType=" + eventType + ", recordType=" + recordType + ']'; }

    public static final class Builder<E, R> {
        private String name;
        private Class<E> eventType;
        private Supplier<E> newEvent;
        private Class<R> recordType;
        private Codec<R> codec;
        private Function<E, R> eventToRecord;
        private String timestampField;
        private List<String> partitionFields = Collections.emptyList();
        private Duration retention;
        private Granularity granularity;
        private final EnumSet<Capability> required = EnumSet.noneOf(Capability.class);
        private final EnumSet<Capability> optional = EnumSet.noneOf(Capability.class);
        public Builder<E, R> name(String name) { this.name = name; return this; }
        public Builder<E, R> event(Class<E> eventType, Supplier<E> newEvent) { this.eventType = eventType; this.newEvent = newEvent; return this; }
        public Builder<E, R> record(Class<R> recordType) { this.recordType = recordType; return this; }
        public Builder<E, R> codec(Codec<R> codec) { this.codec = codec; return this; }
        public Builder<E, R> eventToRecord(Function<E, R> eventToRecord) { this.eventToRecord = eventToRecord; return this; }
        public Builder<E, R> timestamp(String timestampField) { this.timestampField = timestampField; return this; }
        public Builder<E, R> partition(String... partitionFields) { this.partitionFields = partitionFields == null ? Collections.<String>emptyList() : Arrays.asList(partitionFields); return this; }
        public Builder<E, R> retention(Duration retention) { this.retention = retention; return this; }
        public Builder<E, R> granularity(Granularity granularity) { this.granularity = granularity; return this; }
        public Builder<E, R> requireCapability(Capability... capabilities) { if (capabilities != null) Collections.addAll(required, capabilities); return this; }
        public Builder<E, R> optionalCapability(Capability... capabilities) { if (capabilities != null) Collections.addAll(optional, capabilities); return this; }
        public EventStream<E, R> build() { return new EventStream<E, R>(name, eventType, newEvent, recordType, codec, eventToRecord, timestampField, partitionFields, retention, granularity, required, optional); }
    }
}
