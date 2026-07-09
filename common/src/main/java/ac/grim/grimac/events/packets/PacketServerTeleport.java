package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.manager.SetbackTeleportUtil;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerVehicleMove;
import ac.grim.grimac.utils.data.IntToObjectPair;
import ac.grim.grimac.utils.data.RotationData;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Location;

public class PacketServerTeleport extends PacketListenerAbstract {
    private static final boolean STUPID_TELEPORT_SYSTEM = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2);

    public PacketServerTeleport() {
        super(PacketListenerPriority.LOW);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            WrapperPlayServerPlayerPositionAndLook teleport = new WrapperPlayServerPlayerPositionAndLook(event);
            Vector3d pos = new Vector3d(teleport.getX(), teleport.getY(), teleport.getZ());

            if (player.getSetbackTeleportUtil().getRequiredSetBack() == null) {
                player.x = teleport.getX();
                player.y = teleport.getY();
                player.z = teleport.getZ();
                player.yaw = teleport.getYaw();
                player.pitch = teleport.getPitch();

                player.lastX = teleport.getX();
                player.lastY = teleport.getY();
                player.lastZ = teleport.getZ();
                player.lastYaw = teleport.getYaw();
                player.lastPitch = teleport.getPitch();

                player.pollData();
            }

            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) || player.inVehicle()) {
                boolean relativeX = teleport.isRelativeFlag(RelativeFlag.X);
                boolean relativeY = teleport.isRelativeFlag(RelativeFlag.Y);
                boolean relativeZ = teleport.isRelativeFlag(RelativeFlag.Z);

                if (relativeX) {
                    pos = pos.add(new Vector3d(player.x, 0, 0));
                    teleport.setRelative(RelativeFlag.X, false);
                }
                if (relativeY) {
                    pos = pos.add(new Vector3d(0, player.y, 0));
                    teleport.setRelative(RelativeFlag.Y, false);
                }
                if (relativeZ) {
                    pos = pos.add(new Vector3d(0, 0, player.z));
                    teleport.setRelative(RelativeFlag.Z, false);
                }

                if (relativeX || relativeY || relativeZ) {
                    teleport.setX(pos.getX());
                    teleport.setY(pos.getY());
                    teleport.setZ(pos.getZ());
                    event.markForReEncode(true);
                }
            }

            if (STUPID_TELEPORT_SYSTEM && player.inVehicle()) {
                boolean relativeDeltaX = teleport.isRelativeFlag(RelativeFlag.DELTA_X);
                boolean relativeDeltaY = teleport.isRelativeFlag(RelativeFlag.DELTA_Y);
                boolean relativeDeltaZ = teleport.isRelativeFlag(RelativeFlag.DELTA_Z);

                if (relativeDeltaX) {
                    teleport.setRelative(RelativeFlag.DELTA_X, false);
                }
                if (relativeDeltaY) {
                    teleport.setRelative(RelativeFlag.DELTA_Y, false);
                }
                if (relativeDeltaZ) {
                    teleport.setRelative(RelativeFlag.DELTA_Z, false);
                }

                if (relativeDeltaX || relativeDeltaY || relativeDeltaZ) {
                    teleport.setDeltaMovement(Vector3d.zero());
                    event.markForReEncode(true);
                }
            }

            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)
                    && player.compensatedEntities != null
                    && player.compensatedEntities.serverPlayerVehicle != null) {
                SetbackTeleportUtil setbackTeleportUtil = player.getSetbackTeleportUtil();
                if (setbackTeleportUtil != null
                        && setbackTeleportUtil.lastKnownGoodPosition != null
                        && setbackTeleportUtil.lastKnownGoodPosition.getPos() != null) {
                    pos = setbackTeleportUtil.lastKnownGoodPosition.getPos();
                }
            }

            player.sendTransaction();
            int lastTransactionSent = player.lastTransactionSent.get();
            event.getTasksAfterSend().add(player::pollData);

            if (teleport.isDismountVehicle()) {
                event.getTasksAfterSend().add(() -> player.compensatedEntities.self.eject());
            }

            if (event.getServerVersion().isOlderThan(ServerVersion.V_1_8)) {
                pos = pos.withY(pos.getY() - 1.62D);
            }

            Location target = new Location(null, pos.getX(), pos.getY(), pos.getZ(), teleport.getYaw(), teleport.getPitch());
            player.getSetbackTeleportUtil().addSentTeleport(target, teleport.getDeltaMovement(), lastTransactionSent, teleport.getRelativeFlags(), true, teleport.getTeleportId());
        }

        if (event.getPacketType() == PacketType.Play.Server.PLAYER_ROTATION) {
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            WrapperPlayServerPlayerRotation packet = new WrapperPlayServerPlayerRotation(event);

            if (!Float.isFinite(packet.getPitch())) {
                packet.setPitch(0);
                event.markForReEncode(true);
            }
            if (!Float.isFinite(packet.getYaw())) {
                packet.setYaw(0);
                event.markForReEncode(true);
            }

            player.sendTransaction();
            player.pendingRotations.add(new RotationData(packet.getYaw(),
                    packet.isRelativePitch() ? packet.getPitch() : GrimMath.clamp(packet.getPitch() % 360.0F, -90.0F, 90.0F),
                    packet.isRelativeYaw(), packet.isRelativePitch(), player.getLastTransactionSent()));
            event.getTasksAfterSend().add(player::pollData);
        }

        if (event.getPacketType() == PacketType.Play.Server.VEHICLE_MOVE) {
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            player.sendTransaction();
            event.getTasksAfterSend().add(player::pollData);
            player.vehicleData.vehicleTeleports.add(new IntToObjectPair<Vector3d>(player.lastTransactionSent.get(), new WrapperPlayServerVehicleMove(event).getPosition()));
        }
    }
}
