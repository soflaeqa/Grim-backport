package com.mongodb.client;

import java.util.Collections;
import java.util.Iterator;

public interface FindIterable<T> extends Iterable<T> {
    default FindIterable<T> limit(int limit) {
        return this;
    }

    default T first() {
        return null;
    }

    @Override
    default Iterator<T> iterator() {
        return Collections.<T>emptyList().iterator();
    }
}
