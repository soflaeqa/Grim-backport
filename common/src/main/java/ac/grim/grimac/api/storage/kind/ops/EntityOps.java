package ac.grim.grimac.api.storage.kind.ops;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EntityOps {
    private EntityOps() {}

    public interface Op<R> extends Operation<R> {}

    public static final class UpsertOp<R> implements Op<Void> {
        private final Category<?> category;
        private final R record;
        public UpsertOp(Category<?> category, R record) { this.category = category; this.record = record; }
        public Category<?> category() { return category; }
        public R record() { return record; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof UpsertOp)) return false; UpsertOp<?> that = (UpsertOp<?>) o; return Objects.equals(category, that.category) && Objects.equals(record, that.record); }
        @Override public int hashCode() { return Objects.hash(category, record); }
        @Override public String toString() { return "UpsertOp[category=" + category + ", record=" + record + ']'; }
    }

    public static final class GetByIdOp<ID, R> implements Op<Optional<R>> {
        private final Category<?> category;
        private final ID id;
        public GetByIdOp(Category<?> category, ID id) { this.category = category; this.id = id; }
        public Category<?> category() { return category; }
        public ID id() { return id; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof GetByIdOp)) return false; GetByIdOp<?, ?> that = (GetByIdOp<?, ?>) o; return Objects.equals(category, that.category) && Objects.equals(id, that.id); }
        @Override public int hashCode() { return Objects.hash(category, id); }
        @Override public String toString() { return "GetByIdOp[category=" + category + ", id=" + id + ']'; }
    }

    public static final class GetManyOp<ID, R> implements Op<List<R>> {
        private final Category<?> category;
        private final Collection<ID> ids;
        public GetManyOp(Category<?> category, Collection<ID> ids) { this.category = category; this.ids = ids; }
        public Category<?> category() { return category; }
        public Collection<ID> ids() { return ids; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof GetManyOp)) return false; GetManyOp<?, ?> that = (GetManyOp<?, ?>) o; return Objects.equals(category, that.category) && Objects.equals(ids, that.ids); }
        @Override public int hashCode() { return Objects.hash(category, ids); }
        @Override public String toString() { return "GetManyOp[category=" + category + ", ids=" + ids + ']'; }
    }

    public static final class FindByIndexOp<R> implements Op<Page<R>> {
        private final Category<?> category;
        private final String indexName;
        private final Object key;
        private final Cursor cursor;
        private final int pageSize;
        public FindByIndexOp(Category<?> category, String indexName, Object key, Cursor cursor, int pageSize) { this.category = category; this.indexName = indexName; this.key = key; this.cursor = cursor; this.pageSize = pageSize; }
        public Category<?> category() { return category; }
        public String indexName() { return indexName; }
        public Object key() { return key; }
        public Cursor cursor() { return cursor; }
        public int pageSize() { return pageSize; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof FindByIndexOp)) return false; FindByIndexOp<?> that = (FindByIndexOp<?>) o; return pageSize == that.pageSize && Objects.equals(category, that.category) && Objects.equals(indexName, that.indexName) && Objects.equals(key, that.key) && Objects.equals(cursor, that.cursor); }
        @Override public int hashCode() { return Objects.hash(category, indexName, key, cursor, pageSize); }
        @Override public String toString() { return "FindByIndexOp[category=" + category + ", indexName=" + indexName + ", key=" + key + ", cursor=" + cursor + ", pageSize=" + pageSize + ']'; }
    }

    public static final class PrefixIndexOp<R> implements Op<Page<R>> {
        private final Category<?> category;
        private final String indexName;
        private final String prefix;
        private final Cursor cursor;
        private final int pageSize;
        public PrefixIndexOp(Category<?> category, String indexName, String prefix, Cursor cursor, int pageSize) { this.category = category; this.indexName = indexName; this.prefix = prefix; this.cursor = cursor; this.pageSize = pageSize; }
        public Category<?> category() { return category; }
        public String indexName() { return indexName; }
        public String prefix() { return prefix; }
        public Cursor cursor() { return cursor; }
        public int pageSize() { return pageSize; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof PrefixIndexOp)) return false; PrefixIndexOp<?> that = (PrefixIndexOp<?>) o; return pageSize == that.pageSize && Objects.equals(category, that.category) && Objects.equals(indexName, that.indexName) && Objects.equals(prefix, that.prefix) && Objects.equals(cursor, that.cursor); }
        @Override public int hashCode() { return Objects.hash(category, indexName, prefix, cursor, pageSize); }
        @Override public String toString() { return "PrefixIndexOp[category=" + category + ", indexName=" + indexName + ", prefix=" + prefix + ", cursor=" + cursor + ", pageSize=" + pageSize + ']'; }
    }

    public static final class DeleteByIdOp<ID> implements Op<Void> {
        private final Category<?> category;
        private final ID id;
        public DeleteByIdOp(Category<?> category, ID id) { this.category = category; this.id = id; }
        public Category<?> category() { return category; }
        public ID id() { return id; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof DeleteByIdOp)) return false; DeleteByIdOp<?> that = (DeleteByIdOp<?>) o; return Objects.equals(category, that.category) && Objects.equals(id, that.id); }
        @Override public int hashCode() { return Objects.hash(category, id); }
        @Override public String toString() { return "DeleteByIdOp[category=" + category + ", id=" + id + ']'; }
    }

    public static final class DeleteByIndexOp implements Op<Void> {
        private final Category<?> category;
        private final String indexName;
        private final Object key;
        public DeleteByIndexOp(Category<?> category, String indexName, Object key) { this.category = category; this.indexName = indexName; this.key = key; }
        public Category<?> category() { return category; }
        public String indexName() { return indexName; }
        public Object key() { return key; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof DeleteByIndexOp)) return false; DeleteByIndexOp that = (DeleteByIndexOp) o; return Objects.equals(category, that.category) && Objects.equals(indexName, that.indexName) && Objects.equals(key, that.key); }
        @Override public int hashCode() { return Objects.hash(category, indexName, key); }
        @Override public String toString() { return "DeleteByIndexOp[category=" + category + ", indexName=" + indexName + ", key=" + key + ']'; }
    }

    public static final class CountByIndexOp implements Op<Long> {
        private final Category<?> category;
        private final String indexName;
        private final Object key;
        public CountByIndexOp(Category<?> category, String indexName, Object key) { this.category = category; this.indexName = indexName; this.key = key; }
        public Category<?> category() { return category; }
        public String indexName() { return indexName; }
        public Object key() { return key; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof CountByIndexOp)) return false; CountByIndexOp that = (CountByIndexOp) o; return Objects.equals(category, that.category) && Objects.equals(indexName, that.indexName) && Objects.equals(key, that.key); }
        @Override public int hashCode() { return Objects.hash(category, indexName, key); }
        @Override public String toString() { return "CountByIndexOp[category=" + category + ", indexName=" + indexName + ", key=" + key + ']'; }
    }
}
