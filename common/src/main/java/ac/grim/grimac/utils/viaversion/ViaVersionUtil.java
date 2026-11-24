package ac.grim.grimac.utils.viaversion;

import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.reflection.ReflectionUtils;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class ViaVersionUtil {
    public static final boolean isAvailable = ReflectionUtils.hasClass("com.viaversion.viaversion.api.Via");

    static {
        if (!isAvailable && ReflectionUtils.hasClass("us.myles.ViaVersion.api.Via")) {
            LogUtil.error("Using unsupported ViaVersion 4.0 API, update ViaVersion to 5.0");
        }
    }

    /**
     * Returns the real protocol announced to ViaVersion, or -1 when it is not
     * available yet. This is intentionally separate from PacketEvents'
     * ClientVersion: PE 2.13.x maps unknown protocols above its newest known
     * version to that newest version, so protocol 777 otherwise looks like 26.2.
     */
    public static int getProtocolVersion(UUID uuid) {
        if (!isAvailable || uuid == null) return -1;

        try {
            // Prefer the connection object: Grim already uses this Via 5.x API,
            // and it can be available before the platform player lookup is fully ready.
            UserConnection connection = Via.getManager().getConnectionManager().getConnectedClient(uuid);
            if (connection != null && connection.getProtocolInfo() != null) {
                return connection.getProtocolInfo().protocolVersion().getVersion();
            }

            return Via.getAPI().getPlayerVersion(uuid);
        } catch (Throwable ignored) {
            // Via can briefly have no connection entry during login/disconnect.
            return -1;
        }
    }

    public static boolean isProtocolOrNewer(UUID uuid, int protocolVersion) {
        int actual = getProtocolVersion(uuid);
        return actual >= protocolVersion;
    }
}
