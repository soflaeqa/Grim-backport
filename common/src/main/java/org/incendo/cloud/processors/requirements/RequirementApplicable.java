package org.incendo.cloud.processors.requirements;

import org.incendo.cloud.Command;
import org.incendo.cloud.key.CloudKey;

import java.util.List;
import java.util.Objects;

public final class RequirementApplicable<C, R extends Requirement<C, R>> implements Command.Builder.Applicable<C> {
    private final CloudKey<Requirements<C, R>> requirementKey;
    private final Requirements<C, R> requirements;

    public static <C, R extends Requirement<C, R>> RequirementApplicableFactory<C, R> factory(CloudKey<Requirements<C, R>> requirementKey) {
        return new RequirementApplicableFactory<C, R>(requirementKey);
    }

    private RequirementApplicable(CloudKey<Requirements<C, R>> requirementKey, Requirements<C, R> requirements) {
        this.requirementKey = Objects.requireNonNull(requirementKey, "requirementKey");
        this.requirements = Objects.requireNonNull(requirements, "requirements");
    }

    @Override
    public Command.Builder<C> applyToCommandBuilder(Command.Builder<C> builder) {
        return builder.meta(requirementKey, requirements);
    }

    public static final class RequirementApplicableFactory<C, R extends Requirement<C, R>> {
        private final CloudKey<Requirements<C, R>> requirementKey;

        private RequirementApplicableFactory(CloudKey<Requirements<C, R>> requirementKey) {
            this.requirementKey = Objects.requireNonNull(requirementKey, "requirementKey");
        }

        public RequirementApplicable<C, R> create(Requirements<C, R> requirements) {
            return new RequirementApplicable<C, R>(requirementKey, Objects.requireNonNull(requirements, "requirements"));
        }

        public RequirementApplicable<C, R> create(List<R> requirements) {
            Objects.requireNonNull(requirements, "requirements");
            return new RequirementApplicable<C, R>(requirementKey, Requirements.<C, R>of(requirements));
        }

        @SafeVarargs
        public final RequirementApplicable<C, R> create(R... requirements) {
            Objects.requireNonNull(requirements, "requirements");
            return new RequirementApplicable<C, R>(requirementKey, Requirements.<C, R>of(requirements));
        }
    }
}
