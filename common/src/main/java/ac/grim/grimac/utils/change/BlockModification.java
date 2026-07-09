package ac.grim.grimac.utils.change;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BlockModification {
    private final WrappedBlockState oldBlockContents;
    private final WrappedBlockState newBlockContents;
    private final Vector3i location;
    private final int tick;
    private final Cause cause;

    public BlockModification(WrappedBlockState oldBlockContents,
                             WrappedBlockState newBlockContents,
                             Vector3i location,
                             int tick,
                             Cause cause) {
        this.oldBlockContents = oldBlockContents;
        this.newBlockContents = newBlockContents;
        this.location = location;
        this.tick = tick;
        this.cause = cause;
    }

    public WrappedBlockState oldBlockContents() {
        return oldBlockContents;
    }

    public WrappedBlockState newBlockContents() {
        return newBlockContents;
    }

    public Vector3i location() {
        return location;
    }

    public int tick() {
        return tick;
    }

    public Cause cause() {
        return cause;
    }

    @Override
    public @NotNull String toString() {
        return String.format(
                "BlockModification{location=%s, old=%s, new=%s, tick=%d, cause=%s}",
                location, oldBlockContents, newBlockContents, tick, cause
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockModification)) return false;

        BlockModification that = (BlockModification) o;
        return tick == that.tick
                && Objects.equals(oldBlockContents, that.oldBlockContents)
                && Objects.equals(newBlockContents, that.newBlockContents)
                && Objects.equals(location, that.location)
                && cause == that.cause;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldBlockContents, newBlockContents, location, tick, cause);
    }

    public enum Cause {
        START_DIGGING,
        APPLY_BLOCK_CHANGES,
        HANDLE_NETTY_SYNC_TRANSACTION,
        OTHER
    }
}
