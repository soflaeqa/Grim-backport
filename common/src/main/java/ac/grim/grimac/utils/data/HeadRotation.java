package ac.grim.grimac.utils.data;

public final class HeadRotation {
    private final float yaw;
    private final float pitch;

    public HeadRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }
}
