package ac.grim.grimac.internal.storage.backend.sqlite.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.instance.OwnershipClaimResult;
import ac.grim.grimac.api.storage.instance.OwnershipRenewResult;
import ac.grim.grimac.api.storage.instance.ServerOwnershipAdapter;
import ac.grim.grimac.api.storage.instance.ServerOwnershipMetadata;
import ac.grim.grimac.api.storage.instance.ServerOwnershipSnapshot;
import ac.grim.grimac.api.storage.registry.StoreId;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

final class SqliteServerOwnershipAdapter implements ServerOwnershipAdapter {
    interface ConnectionSupplier {
        Connection get() throws BackendException;
    }

    private final ConnectionSupplier connections;

    SqliteServerOwnershipAdapter(ConnectionSupplier connections) {
        this.connections = connections;
    }

    @Override
    public void ensureStore(StoreId storeId) throws BackendException {
        String table = table(storeId);

        try (Statement statement = connection().createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + table + " (" +
                            "persistent_id TEXT PRIMARY KEY," +
                            "owner_startup_id TEXT NOT NULL," +
                            "fence TEXT NOT NULL," +
                            "lease_expires_at_epoch_ms INTEGER NOT NULL," +
                            "last_renewed_at_epoch_ms INTEGER NOT NULL," +
                            "closed_at_epoch_ms INTEGER NOT NULL DEFAULT 0," +
                            "close_reason TEXT," +
                            "server_name TEXT," +
                            "hostname TEXT," +
                            "grim_version TEXT," +
                            "server_version_string TEXT" +
                            ")"
            );
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS " + table + "_lease_idx ON " + table +
                            " (lease_expires_at_epoch_ms, closed_at_epoch_ms)"
            );
        } catch (SQLException e) {
            throw new BackendException("failed to ensure SQLite server ownership store", e);
        }
    }

    @Override
    public long dbNowEpochMs() throws BackendException {
        return System.currentTimeMillis();
    }

    @Override
    public OwnershipClaimResult claimOwnership(StoreId storeId,
                                               UUID persistentId,
                                               UUID startupId,
                                               UUID fence,
                                               long leaseTtlMs,
                                               ServerOwnershipMetadata metadata) throws BackendException {
        String table = table(storeId);
        long now = dbNowEpochMs();
        long leaseExpires = now + Math.max(1L, leaseTtlMs);

        ServerOwnershipSnapshot previous = readSnapshot(table, persistentId);

        if (previous != null && previous.activeAt(now)
                && !startupId.equals(previous.ownerStartupId())) {
            return OwnershipClaimResult.denied(persistentId, startupId, fence, now, previous);
        }

        ServerOwnershipSnapshot current = new ServerOwnershipSnapshot(
                persistentId,
                startupId,
                fence,
                leaseExpires,
                now,
                ServerOwnershipSnapshot.OPEN,
                null,
                metadata == null ? null : metadata.serverName(),
                metadata == null ? null : metadata.hostname(),
                metadata == null ? null : metadata.grimVersion(),
                metadata == null ? null : metadata.serverVersionString()
        );

        upsert(table, current);
        return new OwnershipClaimResult(true, persistentId, startupId, fence, now, leaseExpires, previous, current);
    }

    @Override
    public OwnershipRenewResult renewOwnership(StoreId storeId,
                                               UUID persistentId,
                                               UUID startupId,
                                               UUID fence,
                                               long leaseTtlMs) throws BackendException {
        String table = table(storeId);
        long now = dbNowEpochMs();
        long leaseExpires = now + Math.max(1L, leaseTtlMs);

        String sql = "UPDATE " + table + " SET " +
                "lease_expires_at_epoch_ms=?, last_renewed_at_epoch_ms=? " +
                "WHERE persistent_id=? AND owner_startup_id=? AND fence=? AND closed_at_epoch_ms=0";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, leaseExpires);
            ps.setLong(2, now);
            ps.setString(3, persistentId.toString());
            ps.setString(4, startupId.toString());
            ps.setString(5, fence.toString());

            int changed = ps.executeUpdate();
            if (changed > 0) {
                return OwnershipRenewResult.renewed(persistentId, startupId, fence, now, leaseExpires);
            }
            return OwnershipRenewResult.lost(persistentId, startupId, fence, now);
        } catch (SQLException e) {
            throw new BackendException("failed to renew SQLite server ownership", e);
        }
    }

    @Override
    public boolean closeOwnership(StoreId storeId,
                                  UUID persistentId,
                                  UUID startupId,
                                  UUID fence,
                                  String reason) throws BackendException {
        String table = table(storeId);
        long now = dbNowEpochMs();

        String sql = "UPDATE " + table + " SET closed_at_epoch_ms=?, close_reason=? " +
                "WHERE persistent_id=? AND owner_startup_id=? AND fence=? AND closed_at_epoch_ms=0";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, now);
            ps.setString(2, reason);
            ps.setString(3, persistentId.toString());
            ps.setString(4, startupId.toString());
            ps.setString(5, fence.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new BackendException("failed to close SQLite server ownership", e);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Optional readOwnership(StoreId storeId, UUID persistentId) throws BackendException {
        return Optional.ofNullable(readSnapshot(table(storeId), persistentId));
    }

    private void upsert(String table, ServerOwnershipSnapshot s) throws BackendException {
        String sql = "INSERT OR REPLACE INTO " + table + " (" +
                "persistent_id, owner_startup_id, fence, lease_expires_at_epoch_ms, " +
                "last_renewed_at_epoch_ms, closed_at_epoch_ms, close_reason, " +
                "server_name, hostname, grim_version, server_version_string" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, s.persistentId().toString());
            ps.setString(2, s.ownerStartupId().toString());
            ps.setString(3, s.fence().toString());
            ps.setLong(4, s.leaseExpiresAtEpochMs());
            ps.setLong(5, s.lastRenewedAtEpochMs());
            ps.setLong(6, s.closedAtEpochMs());
            ps.setString(7, s.closeReason());
            ps.setString(8, s.serverName());
            ps.setString(9, s.hostname());
            ps.setString(10, s.grimVersion());
            ps.setString(11, s.serverVersionString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new BackendException("failed to upsert SQLite server ownership", e);
        }
    }

    private ServerOwnershipSnapshot readSnapshot(String table, UUID persistentId) throws BackendException {
        String sql = "SELECT persistent_id, owner_startup_id, fence, lease_expires_at_epoch_ms, " +
                "last_renewed_at_epoch_ms, closed_at_epoch_ms, close_reason, server_name, hostname, " +
                "grim_version, server_version_string FROM " + table + " WHERE persistent_id=?";

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, persistentId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new ServerOwnershipSnapshot(
                        UUID.fromString(rs.getString("persistent_id")),
                        UUID.fromString(rs.getString("owner_startup_id")),
                        UUID.fromString(rs.getString("fence")),
                        rs.getLong("lease_expires_at_epoch_ms"),
                        rs.getLong("last_renewed_at_epoch_ms"),
                        rs.getLong("closed_at_epoch_ms"),
                        rs.getString("close_reason"),
                        rs.getString("server_name"),
                        rs.getString("hostname"),
                        rs.getString("grim_version"),
                        rs.getString("server_version_string")
                );
            }
        } catch (SQLException e) {
            throw new BackendException("failed to read SQLite server ownership", e);
        }
    }

    private Connection connection() throws BackendException {
        return connections.get();
    }

    private static String table(StoreId storeId) {
        return "grim_v2_" + SqliteBackendV2.sanitize(storeId.toString());
    }
}
