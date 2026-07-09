package ac.grim.grimac.utils.chunks;

import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;

import java.util.Arrays;
import java.util.Objects;

public final class Column {
    private final int x;
    private final int z;
    private final BaseChunk[] chunks;
    private final int transaction;

    public Column(int x, int z, BaseChunk[] chunks, int transaction) {
        this.x = x;
        this.z = z;
        this.chunks = chunks;
        this.transaction = transaction;
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    public BaseChunk[] chunks() {
        return chunks;
    }

    public int transaction() {
        return transaction;
    }

    // This ability was removed in 1.17 because of the extended world height
    // Therefore, the size of the chunks are ALWAYS 16!
    public void mergeChunks(BaseChunk[] toMerge) {
        for (int i = 0; i < 16; i++) {
            if (toMerge[i] != null) chunks[i] = toMerge[i];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Column)) return false;
        Column column = (Column) o;
        return x == column.x
                && z == column.z
                && transaction == column.transaction
                && Arrays.equals(chunks, column.chunks);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(x, z, transaction);
        result = 31 * result + Arrays.hashCode(chunks);
        return result;
    }

    @Override
    public String toString() {
        return "Column[x=" + x + ", z=" + z + ", chunks=" + Arrays.toString(chunks)
                + ", transaction=" + transaction + "]";
    }
}
