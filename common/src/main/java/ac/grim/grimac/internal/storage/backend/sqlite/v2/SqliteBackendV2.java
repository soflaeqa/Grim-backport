package ac.grim.grimac.internal.storage.backend.sqlite.v2;

import ac.grim.grimac.internal.storage.backend.NoopBackendV2;
import ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackendConfig;

public final class SqliteBackendV2 extends NoopBackendV2 {
    public SqliteBackendV2(SqliteBackendConfig config) {
        super("sqlite", config);
    }
}
