package ac.grim.grimac.api.events;

import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.events.VerboseSuppliers;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.function.Supplier;

public class FlagEvent extends Event implements GrimUserEvent, Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final GrimUser user;
    private final AbstractCheck check;
    private final Supplier<String> verboseSupplier;
    private boolean cancelled;

    public FlagEvent(GrimUser user, AbstractCheck check, String verbose) {
        this(user, check, VerboseSuppliers.constant(verbose));
    }

    public FlagEvent(GrimUser user, AbstractCheck check, Supplier<String> verboseSupplier) {
        super(true);
        this.user = user;
        this.check = check;
        this.verboseSupplier = VerboseSuppliers.memoize(verboseSupplier);
    }

    public String getVerbose() {
        return verboseSupplier.get();
    }

    public Supplier<String> verboseSupplier() {
        return verboseSupplier;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public double getViolations() {
        return check.getViolations();
    }

    public boolean isSetback() {
        return check.getViolations() > check.getSetbackVL();
    }

    @Override
    public GrimUser getUser() {
        return user;
    }

    public AbstractCheck getCheck() {
        return check;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
