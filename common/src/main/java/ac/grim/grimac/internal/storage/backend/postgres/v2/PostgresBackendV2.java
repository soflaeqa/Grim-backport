package ac.grim.grimac.internal.storage.backend.postgres.v2;

import ac.grim.grimac.internal.storage.backend.NoopBackendV2;
import ac.grim.grimac.internal.storage.backend.postgres.PostgresBackendConfig;

public final class PostgresBackendV2 extends NoopBackendV2 {
    public PostgresBackendV2(PostgresBackendConfig config) {
        super("postgres", config);
    }
}
