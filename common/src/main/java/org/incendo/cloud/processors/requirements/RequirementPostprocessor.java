package org.incendo.cloud.processors.requirements;

import org.incendo.cloud.execution.postprocessor.CommandPostprocessingContext;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.services.type.ConsumerService;

import java.util.Objects;

public final class RequirementPostprocessor<C, R extends Requirement<C, R>> implements CommandPostprocessor<C> {
    private final CloudKey<Requirements<C, R>> requirementKey;
    private final RequirementFailureHandler<C, R> failureHandler;

    public static <C, R extends Requirement<C, R>> RequirementPostprocessor<C, R> of(
            CloudKey<Requirements<C, R>> requirementKey,
            RequirementFailureHandler<C, R> failureHandler
    ) {
        return new RequirementPostprocessor<C, R>(requirementKey, failureHandler);
    }

    private RequirementPostprocessor(CloudKey<Requirements<C, R>> requirementKey, RequirementFailureHandler<C, R> failureHandler) {
        this.requirementKey = Objects.requireNonNull(requirementKey, "requirementKey");
        this.failureHandler = Objects.requireNonNull(failureHandler, "failureHandler");
    }

    @Override
    public void accept(CommandPostprocessingContext<C> context) {
        Requirements<C, R> requirements = context.command().commandMeta().getOrDefault(requirementKey, null);
        if (requirements == null) {
            return;
        }
        for (R requirement : requirements) {
            if (requirement.evaluateRequirement(context.commandContext())) {
                continue;
            }
            failureHandler.handleFailure(context.commandContext(), requirement);
            ConsumerService.interrupt();
        }
    }
}
