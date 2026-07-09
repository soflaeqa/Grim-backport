package ac.grim.grimac.api.events;

import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.GrimUser;
import org.bukkit.event.HandlerList;

public class CompletePredictionEvent extends FlagEvent {
    private static final HandlerList handlers = new HandlerList();
    private final double offset;
    private boolean cancelled;

    public CompletePredictionEvent(GrimUser user, AbstractCheck check, String verbose, double offset) {
        super(user, check, verbose);
        this.offset = offset;
    }

    public double getOffset() {
        return offset;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
