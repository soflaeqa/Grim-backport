package ac.grim.grimac.api.storage.registry;

import java.util.Objects;

public final class StoreId {
    private final String namespace;
    private final String name;

    public StoreId(String namespace, String name) {
        if (namespace == null || namespace.isEmpty()) {
            throw new IllegalArgumentException("namespace");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name");
        }
        if (!isLegal(namespace)) {
            throw new IllegalArgumentException("Illegal namespace: " + namespace);
        }
        if (!isLegal(name)) {
            throw new IllegalArgumentException("Illegal name: " + name);
        }

        this.namespace = namespace;
        this.name = name;
    }

    public static StoreId grim(String name) {
        return new StoreId("grim", name);
    }

    public String qualified() {
        return namespace + ":" + name;
    }

    @Override
    public String toString() {
        return qualified();
    }

    private static boolean isLegal(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                return false;
            }
        }
        return true;
    }

    public String namespace() {
        return namespace;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoreId)) return false;
        StoreId storeId = (StoreId) o;
        return namespace.equals(storeId.namespace) && name.equals(storeId.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }
}
