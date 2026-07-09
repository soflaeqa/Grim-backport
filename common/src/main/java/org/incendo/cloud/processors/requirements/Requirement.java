package org.incendo.cloud.processors.requirements;

import org.incendo.cloud.context.CommandContext;

import java.util.Collections;
import java.util.List;

public interface Requirement<C, R extends Requirement<C, R>> {
    boolean evaluateRequirement(CommandContext<C> commandContext);

    default List<R> parents() {
        return Collections.emptyList();
    }
}
