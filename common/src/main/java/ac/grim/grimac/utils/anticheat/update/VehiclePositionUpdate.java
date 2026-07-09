package ac.grim.grimac.utils.anticheat.update;

import com.github.retrooper.packetevents.util.Vector3d;

import java.util.Objects;

public final class VehiclePositionUpdate {
    private final Vector3d from;
    private final Vector3d to;
    private final float xRot;
    private final float yRot;
    private final boolean onGround;
    private final boolean isTeleport;

    public VehiclePositionUpdate(Vector3d from, Vector3d to, float xRot, float yRot, boolean onGround, boolean isTeleport) {
        this.from = from;
        this.to = to;
        this.xRot = xRot;
        this.yRot = yRot;
        this.onGround = onGround;
        this.isTeleport = isTeleport;
    }

    public Vector3d from() {
        return from;
    }

    public Vector3d to() {
        return to;
    }

    public float xRot() {
        return xRot;
    }

    public float yRot() {
        return yRot;
    }

    public boolean onGround() {
        return onGround;
    }

    public boolean isTeleport() {
        return isTeleport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehiclePositionUpdate)) return false;
        VehiclePositionUpdate that = (VehiclePositionUpdate) o;
        return Float.compare(that.xRot, xRot) == 0
                && Float.compare(that.yRot, yRot) == 0
                && onGround == that.onGround
                && isTeleport == that.isTeleport
                && Objects.equals(from, that.from)
                && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, xRot, yRot, onGround, isTeleport);
    }

    @Override
    public String toString() {
        return "VehiclePositionUpdate[from=" + from + ", to=" + to
                + ", xRot=" + xRot + ", yRot=" + yRot
                + ", onGround=" + onGround + ", isTeleport=" + isTeleport + "]";
    }
}
