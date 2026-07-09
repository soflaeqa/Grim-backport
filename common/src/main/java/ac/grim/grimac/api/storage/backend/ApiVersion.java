package ac.grim.grimac.api.storage.backend;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public final class ApiVersion implements Comparable<ApiVersion> {
    public static final ApiVersion V1_0 = new ApiVersion(1, 0);
    public static final ApiVersion V2_0 = new ApiVersion(2, 0);
    public static final ApiVersion CURRENT = V2_0;

    private final int major;
    private final int minor;

    public ApiVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int major() { return major; }
    public int minor() { return minor; }

    public boolean isCompatibleWith(ApiVersion required) {
        if (this.major != required.major) return false;
        return this.minor >= required.minor;
    }

    @Override
    public int compareTo(ApiVersion other) {
        int m = Integer.compare(this.major, other.major);
        return m != 0 ? m : Integer.compare(this.minor, other.minor);
    }

    @Override
    public String toString() {
        return "v" + major + "." + minor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiVersion)) return false;
        ApiVersion that = (ApiVersion) o;
        return major == that.major && minor == that.minor;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        return result;
    }
}
