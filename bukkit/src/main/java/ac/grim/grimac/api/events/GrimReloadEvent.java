package ac.grim.grimac.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GrimReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final boolean success;

    public GrimReloadEvent(boolean success) {
        super(true);
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
