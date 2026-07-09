package org.incendo.cloud.processors.requirements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public interface Requirements<C, R extends Requirement<C, R>> extends Iterable<R> {
    static <C, R extends Requirement<C, R>> List<R> extractRequirements(List<R> requirements) {
        Objects.requireNonNull(requirements, "requirements");
        List<R> extracted = new ArrayList<R>();
        for (R requirement : requirements) {
            Objects.requireNonNull(requirement, "requirement");
            for (R parent : extractRequirements(requirement.parents())) {
                if (!extracted.contains(parent)) {
                    extracted.add(parent);
                }
            }
            if (!extracted.contains(requirement)) {
                extracted.add(requirement);
            }
        }
        return Collections.unmodifiableList(extracted);
    }

    static <C, R extends Requirement<C, R>> Requirements<C, R> empty() {
        return new RequirementsImpl<C, R>(Collections.<R>emptyList());
    }

    static <C, R extends Requirement<C, R>> Requirements<C, R> of(List<R> requirements) {
        return new RequirementsImpl<C, R>(extractRequirements(requirements));
    }

    @SafeVarargs
    static <C, R extends Requirement<C, R>> Requirements<C, R> of(R... requirements) {
        return of(Arrays.asList(requirements));
    }

    default Requirements<C, R> with(R requirement) {
        List<R> copy = new ArrayList<R>(requirements());
        copy.add(requirement);
        return of(copy);
    }

    List<R> requirements();

    @Override
    default Iterator<R> iterator() {
        return requirements().iterator();
    }
}
