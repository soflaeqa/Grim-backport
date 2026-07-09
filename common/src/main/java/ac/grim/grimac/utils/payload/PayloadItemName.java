package ac.grim.grimac.utils.payload;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Payload wrapper for serverbound {@code MC|ItemName}, replaced by the {@link WrapperPlayClientNameItem NAME_ITEM} packet in 1.13
 */
public final class PayloadItemName implements Payload {
    private final @NotNull String itemName;

    public PayloadItemName(@NotNull String itemName) {
        this.itemName = Objects.requireNonNull(itemName, "itemName");
    }

    public PayloadItemName(byte[] data) {
        this(Payload.wrapper(data).readString());
    }

    public @NotNull String itemName() {
        return itemName;
    }

    @Override
    public void write(PacketWrapper<?> wrapper) {
        wrapper.writeString(itemName);
    }

    @Override
    public String toString() {
        return "PayloadItemName[itemName=" + itemName + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PayloadItemName)) return false;
        PayloadItemName that = (PayloadItemName) o;
        return itemName.equals(that.itemName);
    }

    @Override
    public int hashCode() {
        return itemName.hashCode();
    }
}
