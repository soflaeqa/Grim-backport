package org.incendo.cloud.processors.requirements;

import org.incendo.cloud.context.CommandContext;

public interface RequirementFailureHandler<C, R extends Requirement<C, R>> {
    static <C, R extends Requirement<C, R>> RequirementFailureHandler<C, R> noOp() {
        return new RequirementFailureHandler<C, R>() {
            @Override
            public void handleFailure(CommandContext<C> commandContext, R requirement) {
            }
        };
    }

    void handleFailure(CommandContext<C> commandContext, R requirement);
}
