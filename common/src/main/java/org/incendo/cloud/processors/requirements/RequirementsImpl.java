package org.incendo.cloud.processors.requirements;

import java.util.List;
import java.util.Objects;

final class RequirementsImpl<C, R extends Requirement<C, R>> implements Requirements<C, R> {
    private final List<R> requirements;

    RequirementsImpl(List<R> requirements) {
        this.requirements = Objects.requireNonNull(requirements, "requirements");
    }

    @Override
    public List<R> requirements() {
        return requirements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequirementsImpl<?, ?> that = (RequirementsImpl<?, ?>) o;
        return Objects.equals(requirements, that.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirements);
    }

    @Override
    public String toString() {
        return "RequirementsImpl[requirements=" + requirements + ']';
    }
}
