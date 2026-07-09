package ac.grim.grimac.internal.storage.backend.redis.v2;

import ac.grim.grimac.internal.storage.backend.NoopBackendV2;
import ac.grim.grimac.internal.storage.backend.redis.RedisBackendConfig;

public final class RedisBackendV2 extends NoopBackendV2 {
    public RedisBackendV2(RedisBackendConfig config) {
        super("redis", config);
    }
}
