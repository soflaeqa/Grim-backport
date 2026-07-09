package org.incendo.cloud.processors.requirements;

import java.util.Collection;

public interface RequirementHolder<C, R extends Requirement<C, R>> {
    Collection<R> requirements();
}
