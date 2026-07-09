package ac.grim.grimac.utils.data;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public final class BlockPlaceSnapshot {
    private final PacketWrapper<?> wrapper;
    private final boolean sneaking;

    public BlockPlaceSnapshot(PacketWrapper<?> wrapper, boolean sneaking) {
        this.wrapper = wrapper;
        this.sneaking = sneaking;
    }

    public PacketWrapper<?> wrapper() {
        return wrapper;
    }

    public boolean sneaking() {
        return sneaking;
    }
}
