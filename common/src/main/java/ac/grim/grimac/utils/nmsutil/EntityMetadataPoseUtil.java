package ac.grim.grimac.utils.nmsutil;

import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.enums.Pose;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@UtilityClass
public class EntityMetadataPoseUtil {

    private static final boolean SERVER_HAS_POSE = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_14);
    private static final int POSE_METADATA_INDEX = 6;

    public static boolean usesPoseMetadata(@Nullable PacketEntity entity) {
        return entity != null && usesPoseMetadata(entity.getType());
    }

    public static boolean usesPoseMetadata(@Nullable EntityType entityType) {
        return SERVER_HAS_POSE && (entityType == EntityTypes.PLAYER || entityType == EntityTypes.MANNEQUIN);
    }

    public static @Nullable Pose getPoseFromMetadata(List<EntityData<?>> entityMetadata) {
        EntityData<?> poseData = WatchableIndexUtil.getIndex(entityMetadata, POSE_METADATA_INDEX);
        if (poseData == null || !(poseData.getValue() instanceof EntityPose)) {
            return null;
        }

        EntityPose pose = (EntityPose) poseData.getValue();
        return mapEntityPose(pose);
    }

    private static Pose mapEntityPose(EntityPose entityPose) {
        switch (entityPose) {
            case FALL_FLYING:
                return Pose.FALL_FLYING;
            case SLEEPING:
                return Pose.SLEEPING;
            case SWIMMING:
                return Pose.SWIMMING;
            case SPIN_ATTACK:
                return Pose.SPIN_ATTACK;
            case CROUCHING:
                return Pose.CROUCHING;
            case LONG_JUMPING:
                return Pose.LONG_JUMPING;
            case DYING:
                return Pose.DYING;
            default:
                return Pose.STANDING;
        }
    }
}
