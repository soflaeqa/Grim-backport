package ac.grim.grimac.internal.storage.backend.mongo.v2;

import ac.grim.grimac.internal.storage.backend.NoopBackendV2;
import ac.grim.grimac.internal.storage.backend.mongo.MongoBackendConfig;

public final class MongoBackendV2 extends NoopBackendV2 {
    public MongoBackendV2(MongoBackendConfig config) {
        super("mongo", config);
    }
}
