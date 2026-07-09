package ac.grim.grimac.api.storage.kind.ops;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public final class EventStreamOps {
    private EventStreamOps() {}

    public interface Op<R> extends Operation<R> {}

    public static final class RangeByTimeOp<R> implements Op<Page<R>> {
        private final Category<?> category;
        private final long fromEpochMs;
        private final long toEpochMs;
        private final Cursor cursor;
        private final int pageSize;
        public RangeByTimeOp(Category<?> category, long fromEpochMs, long toEpochMs, Cursor cursor, int pageSize) { this.category = category; this.fromEpochMs = fromEpochMs; this.toEpochMs = toEpochMs; this.cursor = cursor; this.pageSize = pageSize; }
        public Category<?> category() { return category; }
        public long fromEpochMs() { return fromEpochMs; }
        public long toEpochMs() { return toEpochMs; }
        public Cursor cursor() { return cursor; }
        public int pageSize() { return pageSize; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof RangeByTimeOp)) return false; RangeByTimeOp<?> that = (RangeByTimeOp<?>) o; return fromEpochMs == that.fromEpochMs && toEpochMs == that.toEpochMs && pageSize == that.pageSize && Objects.equals(category, that.category) && Objects.equals(cursor, that.cursor); }
        @Override public int hashCode() { return Objects.hash(category, fromEpochMs, toEpochMs, cursor, pageSize); }
        @Override public String toString() { return "RangeByTimeOp[category=" + category + ", fromEpochMs=" + fromEpochMs + ", toEpochMs=" + toEpochMs + ", cursor=" + cursor + ", pageSize=" + pageSize + ']'; }
    }

    public static final class PageOp<R> implements Op<Page<R>> {
        private final Category<?> category;
        private final String partition;
        private final Object key;
        private final Cursor cursor;
        private final int pageSize;
        public PageOp(Category<?> category, String partition, Object key, Cursor cursor, int pageSize) { this.category = category; this.partition = partition; this.key = key; this.cursor = cursor; this.pageSize = pageSize; }
        public Category<?> category() { return category; }
        public String partition() { return partition; }
        public Object key() { return key; }
        public Cursor cursor() { return cursor; }
        public int pageSize() { return pageSize; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof PageOp)) return false; PageOp<?> that = (PageOp<?>) o; return pageSize == that.pageSize && Objects.equals(category, that.category) && Objects.equals(partition, that.partition) && Objects.equals(key, that.key) && Objects.equals(cursor, that.cursor); }
        @Override public int hashCode() { return Objects.hash(category, partition, key, cursor, pageSize); }
        @Override public String toString() { return "PageOp[category=" + category + ", partition=" + partition + ", key=" + key + ", cursor=" + cursor + ", pageSize=" + pageSize + ']'; }
    }

    public static final class CountOp implements Op<Long> {
        private final Category<?> category;
        private final String partition;
        private final Object key;
        public CountOp(Category<?> category, String partition, Object key) { this.category = category; this.partition = partition; this.key = key; }
        public Category<?> category() { return category; }
        public String partition() { return partition; }
        public Object key() { return key; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof CountOp)) return false; CountOp that = (CountOp) o; return Objects.equals(category, that.category) && Objects.equals(partition, that.partition) && Objects.equals(key, that.key); }
        @Override public int hashCode() { return Objects.hash(category, partition, key); }
        @Override public String toString() { return "CountOp[category=" + category + ", partition=" + partition + ", key=" + key + ']'; }
    }

    public static final class CountManyOp<K> implements Op<Map<K, Long>> {
        private final Category<?> category;
        private final String partition;
        private final Collection<K> keys;
        public CountManyOp(Category<?> category, String partition, Collection<K> keys) { this.category = category; this.partition = partition; this.keys = keys; }
        public Category<?> category() { return category; }
        public String partition() { return partition; }
        public Collection<K> keys() { return keys; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof CountManyOp)) return false; CountManyOp<?> that = (CountManyOp<?>) o; return Objects.equals(category, that.category) && Objects.equals(partition, that.partition) && Objects.equals(keys, that.keys); }
        @Override public int hashCode() { return Objects.hash(category, partition, keys); }
        @Override public String toString() { return "CountManyOp[category=" + category + ", partition=" + partition + ", keys=" + keys + ']'; }
    }

    public static final class CountDistinctOp implements Op<Long> {
        private final Category<?> category;
        private final String partition;
        private final Object key;
        private final String field;
        public CountDistinctOp(Category<?> category, String partition, Object key, String field) { this.category = category; this.partition = partition; this.key = key; this.field = field; }
        public Category<?> category() { return category; }
        public String partition() { return partition; }
        public Object key() { return key; }
        public String field() { return field; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof CountDistinctOp)) return false; CountDistinctOp that = (CountDistinctOp) o; return Objects.equals(category, that.category) && Objects.equals(partition, that.partition) && Objects.equals(key, that.key) && Objects.equals(field, that.field); }
        @Override public int hashCode() { return Objects.hash(category, partition, key, field); }
        @Override public String toString() { return "CountDistinctOp[category=" + category + ", partition=" + partition + ", key=" + key + ", field=" + field + ']'; }
    }

    public static final class DeleteOlderThanOp implements Op<Void> {
        private final Category<?> category;
        private final long cutoffEpochMs;
        public DeleteOlderThanOp(Category<?> category, long cutoffEpochMs) { this.category = category; this.cutoffEpochMs = cutoffEpochMs; }
        public Category<?> category() { return category; }
        public long cutoffEpochMs() { return cutoffEpochMs; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof DeleteOlderThanOp)) return false; DeleteOlderThanOp that = (DeleteOlderThanOp) o; return cutoffEpochMs == that.cutoffEpochMs && Objects.equals(category, that.category); }
        @Override public int hashCode() { return Objects.hash(category, cutoffEpochMs); }
        @Override public String toString() { return "DeleteOlderThanOp[category=" + category + ", cutoffEpochMs=" + cutoffEpochMs + ']'; }
    }

    public static final class DeleteByPartitionOp implements Op<Void> {
        private final Category<?> category;
        private final String partition;
        private final Object key;
        public DeleteByPartitionOp(Category<?> category, String partition, Object key) { this.category = category; this.partition = partition; this.key = key; }
        public Category<?> category() { return category; }
        public String partition() { return partition; }
        public Object key() { return key; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof DeleteByPartitionOp)) return false; DeleteByPartitionOp that = (DeleteByPartitionOp) o; return Objects.equals(category, that.category) && Objects.equals(partition, that.partition) && Objects.equals(key, that.key); }
        @Override public int hashCode() { return Objects.hash(category, partition, key); }
        @Override public String toString() { return "DeleteByPartitionOp[category=" + category + ", partition=" + partition + ", key=" + key + ']'; }
    }
}
