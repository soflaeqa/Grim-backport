package ac.grim.grimac.internal.storage.backend.mysql;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.internal.storage.backend.NoopBackend;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public final class MysqlBackendProvider implements BackendProvider {
    public static final String ID = "mysql";
    @Override public @NotNull String id() { return ID; }
    @Override public @NotNull Class<? extends BackendConfig> configType() { return MysqlBackendConfig.class; }
    @Override public @NotNull BackendConfig readConfig(@NotNull BackendConfigSource src) {
        String pw = src.getString("password", "");
        return new MysqlBackendConfig(
                src.getString("host", "localhost"), src.getInt("port", 3306),
                src.getString("database", "grim"), src.getString("user", "root"),
                pw.isEmpty() ? null : pw, src.getString("extra-jdbc-params", ""),
                src.getInt("batch-flush-cap", 256), 1,
                Collections.<String, Integer>emptyMap(), TableNames.readFrom(src));
    }
    @Override public @NotNull Backend create(@NotNull BackendConfig config) { return new NoopBackend(ID, config); }
}
