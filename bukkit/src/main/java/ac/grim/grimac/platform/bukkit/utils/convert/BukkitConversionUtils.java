package ac.grim.grimac.platform.bukkit.utils.convert;

import ac.grim.grimac.platform.api.permissions.PermissionDefaultValue;
import ac.grim.grimac.platform.bukkit.world.BukkitPlatformWorld;
import ac.grim.grimac.utils.math.Location;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class BukkitConversionUtils {
    @Contract("null -> null; !null -> new")
    public static org.bukkit.Location toBukkitLocation(Location location) {
        if (location == null) return null;
        return new org.bukkit.Location(((BukkitPlatformWorld) location.getWorld()).bukkitWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    /**
     * Converts this enum to a Bukkit PermissionDefault.
     *
     * @return The corresponding Bukkit PermissionDefault.
     */
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static @Nullable PermissionDefault toBukkitPermissionDefault(@Nullable PermissionDefaultValue permissionDefaultValue) {
        if (permissionDefaultValue == null) return null;
        switch (permissionDefaultValue) {
            case TRUE:
                return PermissionDefault.TRUE;
            case FALSE:
                return PermissionDefault.FALSE;
            case OP:
                return PermissionDefault.OP;
            case NOT_OP:
                return PermissionDefault.NOT_OP;
            default:
                throw new IllegalStateException("Unknown permission default value: " + permissionDefaultValue);
        }
    }

    public static BlockFace fromBukkitFace(org.bukkit.block.BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.SOUTH;
            case WEST:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.EAST;
            case UP:
                return BlockFace.UP;
            case DOWN:
                return BlockFace.DOWN;
            default:
                return BlockFace.OTHER;
        }
    }
}
