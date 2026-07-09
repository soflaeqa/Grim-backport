package ac.grim.grimac.utils.item;

import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemBehaviourRegistry {

    private static final Map<ItemType, ItemBehaviour> ITEM_MAPPING = createItemMapping();

    private static Map<ItemType, ItemBehaviour> createItemMapping() {
        Map<ItemType, ItemBehaviour> map = new HashMap<>();
        map.put(ItemTypes.GOAT_HORN, AlwaysUseItem.INSTANCE);
        map.put(ItemTypes.SHIELD, AlwaysUseItem.INSTANCE);
        map.put(ItemTypes.SPYGLASS, AlwaysUseItem.INSTANCE);
        map.put(ItemTypes.CROSSBOW, UnsupportedItem.INSTANCE);
        map.put(ItemTypes.BOW, UnsupportedItem.INSTANCE);
        map.put(ItemTypes.TRIDENT, TridentItem.INSTANCE);
        return Collections.unmodifiableMap(map);
    }

    private static final boolean RELIABLE_COMPONENT_SYSTEM = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_4);

    public static @NotNull ItemBehaviour getItemBehaviour(GrimPlayer player, ItemType type) {
        if (!RELIABLE_COMPONENT_SYSTEM || player.getClientVersion().isOlderThan(ClientVersion.V_1_21_4)) {
            return LegacyItem.INSTANCE;
        }

        return ITEM_MAPPING.getOrDefault(type, ItemBehaviour.INSTANCE);
    }

}
