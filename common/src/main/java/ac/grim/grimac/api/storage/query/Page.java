package ac.grim.grimac.api.storage.query;

import java.util.Collections;
import java.util.List;

public final class Page<R> {
    private final List<R> items;
    private final Cursor nextCursor;

    public Page(List<R> items, Cursor nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public static <R> Page<R> empty() {
        return new Page<R>(Collections.<R>emptyList(), null);
    }

    public boolean hasMore() {
        return nextCursor != null;
    }

    public List<R> items() {
        return items;
    }

    public Cursor nextCursor() {
        return nextCursor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        Page<?> page = (Page<?>) o;
        if (items == null ? page.items != null : !items.equals(page.items)) return false;
        return nextCursor == null ? page.nextCursor == null : nextCursor.equals(page.nextCursor);
    }

    @Override
    public int hashCode() {
        int result = items == null ? 0 : items.hashCode();
        result = 31 * result + (nextCursor == null ? 0 : nextCursor.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Page[items=" + items + ", nextCursor=" + nextCursor + "]";
    }
}
