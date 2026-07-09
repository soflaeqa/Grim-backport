package ac.grim.grimac.api.storage.kind;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class IndexSpec {
    private final String name;
    private final List<String> fields;
    private final boolean unique;
    private final boolean caseInsensitivePrefix;

    public IndexSpec(String name, List<String> fields, boolean unique, boolean caseInsensitivePrefix) {
        this.name = name;
        this.fields = fields == null ? Collections.<String>emptyList() : Collections.unmodifiableList(fields);
        this.unique = unique;
        this.caseInsensitivePrefix = caseInsensitivePrefix;
    }

    public static IndexSpec of(String name, String... fields) {
        return new IndexSpec(name, fields == null ? Collections.<String>emptyList() : Arrays.asList(fields), false, false);
    }

    public static IndexSpec unique(String name, String... fields) {
        return new IndexSpec(name, fields == null ? Collections.<String>emptyList() : Arrays.asList(fields), true, false);
    }

    public String name() { return name; }
    public List<String> fields() { return fields; }
    public boolean unique() { return unique; }
    public boolean caseInsensitivePrefix() { return caseInsensitivePrefix; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexSpec)) return false;
        IndexSpec that = (IndexSpec) o;
        return unique == that.unique && caseInsensitivePrefix == that.caseInsensitivePrefix
                && Objects.equals(name, that.name) && Objects.equals(fields, that.fields);
    }
    @Override public int hashCode() { return Objects.hash(name, fields, unique, caseInsensitivePrefix); }
    @Override public String toString() { return "IndexSpec[name=" + name + ", fields=" + fields + ", unique=" + unique + ", caseInsensitivePrefix=" + caseInsensitivePrefix + ']'; }
}
