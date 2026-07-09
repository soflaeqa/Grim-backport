package ac.grim.grimac.api.events;

import ac.grim.grimac.api.GrimUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GrimJoinEvent extends Event implements GrimUserEvent {
    private static final HandlerList handlers = new HandlerList();
    private final GrimUser user;

    public GrimJoinEvent(GrimUser user) {
        super(true);
        this.user = user;
    }

    @Override
    public GrimUser getUser() {
        return user;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
