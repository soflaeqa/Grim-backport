package org.bson;

import java.util.HashMap;
import java.util.Map;

public class Document extends HashMap<String, Object> {
    public Document() {
        super();
    }

    public Document(String key, Object value) {
        super();
        put(key, value);
    }

    public Document(Map<String, ?> map) {
        super();
        if (map != null) {
            putAll(map);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (clazz != null && clazz.isInstance(value)) {
            return (T) value;
        }
        if (clazz == Number.class && value instanceof Number) {
            return (T) value;
        }
        return null;
    }

    public String getString(String key) {
        Object value = get(key);
        return value == null ? null : String.valueOf(value);
    }
}
