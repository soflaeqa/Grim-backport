package ac.grim.grimac.utils.worldborder;

import ac.grim.grimac.utils.math.GrimMath;

import java.util.Objects;

public final class StaticBorderExtent implements BorderExtent {
    private final double size;

    public StaticBorderExtent(double size) {
        this.size = size;
    }

    public double size() {
        return size;
    }

    @Override
    public double getMinX(double centerX, double absoluteMaxSize) {
        return GrimMath.clamp(centerX - size / 2.0, -absoluteMaxSize, absoluteMaxSize);
    }

    @Override
    public double getMaxX(double centerX, double absoluteMaxSize) {
        return GrimMath.clamp(centerX + size / 2.0, -absoluteMaxSize, absoluteMaxSize);
    }

    @Override
    public double getMinZ(double centerZ, double absoluteMaxSize) {
        return GrimMath.clamp(centerZ - size / 2.0, -absoluteMaxSize, absoluteMaxSize);
    }

    @Override
    public double getMaxZ(double centerZ, double absoluteMaxSize) {
        return GrimMath.clamp(centerZ + size / 2.0, -absoluteMaxSize, absoluteMaxSize);
    }

    @Override
    public BorderExtent tick() {
        return this;
    }

    @Override
    public BorderExtent update() {
        return this;
    }

    @Override
    public String toString() {
        return "StaticBorderExtent[size=" + size + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaticBorderExtent)) return false;
        StaticBorderExtent that = (StaticBorderExtent) o;
        return Double.compare(that.size, size) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size);
    }
}
