package ac.grim.grimac.utils.data;

import com.github.retrooper.packetevents.util.Vector3i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class MainSupportingBlockData {
    public static final MainSupportingBlockData AIR_OFF_GROUND = new MainSupportingBlockData(null, false);
    public static final MainSupportingBlockData AIR_ON_GROUND = new MainSupportingBlockData(null, true);

    private final @Nullable Vector3i blockPos;
    private final boolean onGround;

    public MainSupportingBlockData(@Nullable Vector3i blockPos, boolean onGround) {
        this.blockPos = blockPos;
        this.onGround = onGround;
    }

    public @Nullable Vector3i blockPos() {
        return blockPos;
    }

    public boolean onGround() {
        return onGround;
    }

    @Contract(pure = true)
    public boolean lastOnGroundAndNoBlock() {
        return blockPos == null && onGround;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MainSupportingBlockData)) return false;
        MainSupportingBlockData that = (MainSupportingBlockData) o;
        return onGround == that.onGround && Objects.equals(blockPos, that.blockPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos, onGround);
    }

    @Override
    public String toString() {
        return "MainSupportingBlockData[blockPos=" + blockPos + ", onGround=" + onGround + "]";
    }
}
