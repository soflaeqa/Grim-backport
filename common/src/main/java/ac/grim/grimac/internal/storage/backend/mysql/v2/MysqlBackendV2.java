package ac.grim.grimac.internal.storage.backend.mysql.v2;

import ac.grim.grimac.internal.storage.backend.NoopBackendV2;
import ac.grim.grimac.internal.storage.backend.mysql.MysqlBackendConfig;

public final class MysqlBackendV2 extends NoopBackendV2 {
    public MysqlBackendV2(MysqlBackendConfig config) {
        super("mysql", config);
    }
}
