package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.viaversion.ViaVersionUtil;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import org.jetbrains.annotations.NotNull;

public class PreViaCheckManagerListener extends PacketListenerAbstract {
    // Minecraft 26.3. PacketEvents 2.13.x does not know this protocol and
    // falls back to its newest known version (26.2), which makes raw pre-Via
    // packets get decoded with the wrong packet map.
    private static final int FIRST_UNSUPPORTED_RAW_PROTOCOL = 777;

    public PreViaCheckManagerListener() {
        super(PacketListenerPriority.LOW);
    }

    @Override
    public boolean isPreVia() {
        return true;
    }

    private static boolean shouldSkipRawPreVia(GrimPlayer player) {
        return ViaVersionUtil.isProtocolOrNewer(player.uuid, FIRST_UNSUPPORTED_RAW_PROTOCOL);
    }

    @Override
    public void onPacketReceive(final @NotNull PacketReceiveEvent event) {
        // Allow checks to listen to configuration packets
        if (event.getConnectionState() == ConnectionState.CONFIGURATION) {
            final GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null || shouldSkipRawPreVia(player)) return;
            player.checkManager.onPreViaPacketReceive(event);
        }

        if (event.getConnectionState() != ConnectionState.PLAY) return;
        final GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
        if (player == null || shouldSkipRawPreVia(player)) return;

        player.checkManager.onPreViaPacketReceive(event);
    }

    @Override
    public void onPacketSend(final @NotNull PacketSendEvent event) {
        // Allow checks to listen to configuration packets
        if (event.getConnectionState() == ConnectionState.CONFIGURATION) {
            final GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null || shouldSkipRawPreVia(player)) return;
            player.checkManager.onPreViaPacketSend(event);
        }

        if (event.getConnectionState() != ConnectionState.PLAY) return;
        final GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
        if (player == null) return;

        // Keep the lightweight state bookkeeping below, but do not feed raw
        // 26.3+ bytes into PE 2.13's pre-Via check wrappers. The normal
        // CheckManagerListener still receives the Via-translated packets.
        final boolean skipRawPreVia = shouldSkipRawPreVia(player);
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Server.OPEN_WINDOW || packetType == PacketType.Play.Server.OPEN_HORSE_WINDOW) {
            player.sendTransaction();
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.serverOpenedInventoryThisTick = true);
        }
        if (packetType == PacketType.Play.Server.BUNDLE) {
            player.packetStateData.sendingBundlePacket = !player.packetStateData.sendingBundlePacket;
        }

        if (!skipRawPreVia) {
            player.checkManager.onPreViaPacketSend(event);
        }
    }
}
