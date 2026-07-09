package ac.grim.grimac.utils.math;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Vec2 {
    public static final Vec2 ZERO = new Vec2(0.0F, 0.0F);

    private final float x;
    private final float y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    @Contract("_ -> new")
    public @NotNull Vec2 scale(float scalar) {
        return new Vec2(this.x * scalar, this.y * scalar);
    }

    @Contract(pure = true)
    public float dot(@NotNull Vec2 vec) {
        return this.x * vec.x + this.y * vec.y;
    }

    @Contract("_ -> new")
    public @NotNull Vec2 add(@NotNull Vec2 vec) {
        return new Vec2(this.x + vec.x, this.y + vec.y);
    }

    @Contract("_ -> new")
    public @NotNull Vec2 add(float vec) {
        return new Vec2(this.x + vec, this.y + vec);
    }

    @Contract(pure = true)
    public boolean equals(@NotNull Vec2 vec) {
        return this.x == vec.x && this.y == vec.y;
    }

    public @NotNull Vec2 normalized() {
        float length = GrimMath.sqrt(this.x * this.x + this.y * this.y);
        return length < 1.0E-4F ? ZERO : new Vec2(this.x / length, this.y / length);
    }

    @Contract(pure = true)
    public float length() {
        return GrimMath.sqrt(this.x * this.x + this.y * this.y);
    }

    @Contract(pure = true)
    public float lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    @Contract(pure = true)
    public float distanceToSqr(@NotNull Vec2 vec) {
        float dx = vec.x - this.x;
        float dy = vec.y - this.y;
        return dx * dx + dy * dy;
    }

    @Contract(" -> new")
    public @NotNull Vec2 negated() {
        return new Vec2(-this.x, -this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec2)) return false;
        Vec2 vec2 = (Vec2) o;
        return Float.compare(vec2.x, x) == 0 && Float.compare(vec2.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        return result;
    }

    @Override
    public String toString() {
        return "Vec2[x=" + x + ", y=" + y + "]";
    }
}
