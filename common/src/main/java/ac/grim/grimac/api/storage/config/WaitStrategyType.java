package ac.grim.grimac.api.storage.config;

public enum WaitStrategyType {
    BLOCKING,
    TIMEOUT_BLOCKING,
    SLEEPING,
    YIELDING,
    BUSY_SPIN
}
