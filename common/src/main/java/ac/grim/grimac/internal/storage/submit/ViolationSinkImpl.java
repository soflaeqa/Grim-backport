package ac.grim.grimac.internal.storage.submit;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.event.ViolationEvent;
import ac.grim.grimac.api.storage.submit.SubmitResult;
import ac.grim.grimac.api.storage.submit.ViolationSink;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Java 8 compatibility implementation for the violation hot-path sink.
 *
 * This class is created and stopped directly by DataStoreLifecycle:
 *   new ViolationSinkImpl(dataStore)
 *   violationSink.shutDown()
 */
public final class ViolationSinkImpl implements ViolationSink {
    private final DataStore dataStore;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public ViolationSinkImpl(@NotNull DataStore dataStore) {
        this.dataStore = Objects.requireNonNull(dataStore, "dataStore");
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public @NotNull SubmitResult record(@NotNull Consumer configurer) {
        if (configurer == null || shuttingDown.get()) {
            return SubmitResult.DROPPED_SHUTTING_DOWN;
        }

        try {
            dataStore.submit(Categories.VIOLATION, event -> {
                ViolationEvent violation = (ViolationEvent) event;

                configurer.accept(violation);

                if (violation.id() == null) {
                    violation.id(UUID.randomUUID());
                }

                if (violation.occurredEpochMs() <= 0L) {
                    violation.occurredEpochMs(System.currentTimeMillis());
                }
            });

            return SubmitResult.QUEUED;
        } catch (Throwable ignored) {
            return SubmitResult.DROPPED_SHUTTING_DOWN;
        }
    }

    public void shutDown() {
        shuttingDown.set(true);
    }
}
