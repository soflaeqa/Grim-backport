package ac.grim.grimac.platform.bukkit.manager;

import ac.grim.grimac.platform.api.manager.ItemResetHandler;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.platform.bukkit.utils.reflection.PaperUtils;
import ac.grim.grimac.utils.reflection.ReflectionUtils;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class BukkitItemResetHandler implements ItemResetHandler {
    private static final Consumer<Player> resetItemUsage;
    private static final Predicate<Player> isUsingItem;
    private static final Function<Player, InteractionHand> getItemUsageHand;

    @Override
    public void resetItemUsage(@Nullable PlatformPlayer player) {
        if (player != null) resetItemUsage.accept((Player) player.getNative());
    }

    @Override
    public @Nullable InteractionHand getItemUsageHand(@Nullable PlatformPlayer player) {
        return player == null ? null : getItemUsageHand.apply((Player) player.getNative());
    }

    @Override
    public boolean isUsingItem(@Nullable PlatformPlayer player) {
        return player != null && isUsingItem.test((Player) player.getNative());
    }

    static {
        final ServerVersion version = PacketEvents.getAPI().getServerManager().getVersion();
        final boolean legacy = version.isOlderThanOrEquals(ServerVersion.V_1_8_8);
        try {
            final Method getHandle;
            final String nmsPackage;

            Class<?> craftLivingEntity = ReflectionUtils.getClass("org.bukkit.craftbukkit.entity.CraftLivingEntity");
            if (craftLivingEntity != null) {
                getHandle = craftLivingEntity.getMethod("getHandle");
                nmsPackage = null;
            } else {
                String packageName = Bukkit.getServer().getClass().getPackage().getName();
                nmsPackage = packageName.split("\\.")[3];
                final String className = legacy ? "CraftHumanEntity" : "CraftLivingEntity";
                getHandle = Class.forName("org.bukkit.craftbukkit." + nmsPackage + ".entity." + className).getMethod("getHandle");
            }

            final boolean obfuscated = nmsPackage != null;
            final Class<?> clazz = getHandle.getReturnType();

            if (version.isNewerThanOrEquals(ServerVersion.V_1_10)) {
                isUsingItem = Player::isHandRaised;
            } else {
                Method method = clazz.getMethod(getUsingItemMethodName(Objects.requireNonNull(nmsPackage), version));
                isUsingItem = player -> {
                    try {
                        return (boolean) method.invoke(getHandle.invoke(player));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            if (legacy) {
                getItemUsageHand = player -> isUsingItem.test(player) ? InteractionHand.MAIN_HAND : null;
            } else if (PaperUtils.PAPER && version.isNewerThanOrEquals(ServerVersion.V_1_16_5)) {
                getItemUsageHand = player -> player.isHandRaised()
                        ? player.getHandRaised() == EquipmentSlot.OFF_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND
                        : null;
            } else {
                Method method = clazz.getMethod(nmsPackage != null ? getItemUsageHandMethodName(Objects.requireNonNull(nmsPackage), version) : "getUsedItemHand");
                getItemUsageHand = player -> {
                    try {
                        return isUsingItem.test(player)
                                ? ((Enum<?>) method.invoke(getHandle.invoke(player))).ordinal() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND
                                : null;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
            }

            Method setLivingEntityFlag;
            if (version.isNewerThanOrEquals(ServerVersion.V_1_19)) {
                String name = obfuscated ? "c" : "setLivingEntityFlag";
                setLivingEntityFlag = clazz.getDeclaredMethod(name, int.class, boolean.class);
                setLivingEntityFlag.setAccessible(true);
            } else {
                setLivingEntityFlag = null;
            }

            if (PaperUtils.PAPER && version.isNewerThan(ServerVersion.V_1_17)) {
                resetItemUsage = setLivingEntityFlag == null ? LivingEntity::clearActiveItem : player -> {
                    try {
                        setLivingEntityFlag.invoke(getHandle.invoke(player), 1, false);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                    player.clearActiveItem();
                };
            } else {
                Method method = clazz.getMethod(obfuscated ? getResetItemUsageMethodName(Objects.requireNonNull(nmsPackage), version) : "stopUsingItem");
                if (legacy) {
                    // 1.8.8
                    resetItemUsage = player -> {
                        try {
                            method.invoke(getHandle.invoke(player));
                            // in 1.8 we need to resync item usage manually,
                            // only do so if the player is using an item
                            if (isUsingItem.test(player)) player.updateInventory();
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };
                } else if (setLivingEntityFlag == null) {
                    // 1.9-1.18.2
                    resetItemUsage = player -> {
                        try {
                            method.invoke(getHandle.invoke(player));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };
                } else {
                    // 1.19+
                    resetItemUsage = player -> {
                        try {
                            Object handle = getHandle.invoke(player);
                            setLivingEntityFlag.invoke(handle, 1, false);
                            method.invoke(handle);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };
                }
            }
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }

    private static String getUsingItemMethodName(String nmsPackage, ServerVersion version) {
        switch (nmsPackage) {
            case "v1_8_R3":
                return "bS";
            case "v1_9_R1":
                return "cs";
            case "v1_9_R2":
                return "ct";
            default:
                throw unsupported(version);
        }
    }

    private static String getItemUsageHandMethodName(String nmsPackage, ServerVersion version) {
        switch (nmsPackage) {
            case "v1_9_R1":
                return "ct";
            case "v1_9_R2":
                return "cu";
            case "v1_10_R1":
                return "cy";
            case "v1_11_R1":
                return "cz";
            case "v1_12_R1":
                return "cH";
            case "v1_13_R1":
            case "v1_13_R2":
            case "v1_14_R1":
                return "cU";
            case "v1_15_R1":
            case "v1_16_R1":
            case "v1_16_R2":
            case "v1_16_R3":
            case "v1_17_R1":
                return "getRaisedHand";
            case "v1_18_R1":
                return "eM";
            case "v1_18_R2":
                return "eN";
            case "v1_19_R1":
                return "eU";
            case "v1_19_R2":
                return "fa";
            case "v1_19_R3":
                return "ff";
            case "v1_20_R1":
                return "fj";
            case "v1_20_R2":
                return "fn";
            case "v1_20_R3":
                return "fo";
            case "v1_20_R4":
                return "fw";
            case "v1_21_R1":
                return "fs";
            case "v1_21_R2":
            case "v1_21_R3":
            case "v1_21_R4":
                return "fA";
            case "v1_21_R5":
                return "fH";
            case "v1_21_R6":
                return "fP";
            case "v1_21_R7":
                return "ga";
            default:
                throw unsupported(version);
        }
    }

    private static String getResetItemUsageMethodName(String nmsPackage, ServerVersion version) {
        switch (nmsPackage) {
            case "v1_8_R3":
                return "bV";
            case "v1_9_R1":
                return "cz";
            case "v1_9_R2":
                return "cA";
            case "v1_10_R1":
                return "cE";
            case "v1_11_R1":
                return "cF";
            case "v1_12_R1":
                return "cN";
            case "v1_13_R1":
            case "v1_13_R2":
                return "da";
            case "v1_14_R1":
                return "dp";
            case "v1_15_R1":
                return "dH";
            case "v1_16_R1":
            case "v1_16_R2":
            case "v1_16_R3":
            case "v1_17_R1":
                return "clearActiveItem";
            case "v1_18_R1":
                return "eR";
            case "v1_18_R2":
                return "eS";
            case "v1_19_R1":
                return "eZ";
            case "v1_19_R2":
                return "ff";
            case "v1_19_R3":
                return "fk";
            case "v1_20_R1":
                return "fo";
            case "v1_20_R2":
                return "fs";
            case "v1_20_R3":
                return "ft";
            case "v1_20_R4":
                return "fB";
            case "v1_21_R1":
                return "fx";
            case "v1_21_R2":
            case "v1_21_R3":
            case "v1_21_R4":
                return "fF";
            case "v1_21_R5":
                return "fM";
            case "v1_21_R6":
                return "fU";
            case "v1_21_R7":
                return "gf";
            default:
                throw unsupported(version);
        }
    }

    private static IllegalStateException unsupported(ServerVersion version) {
        return new IllegalStateException("You are using an unsupported server version! (" + version.getReleaseName() + ")");
    }
}
