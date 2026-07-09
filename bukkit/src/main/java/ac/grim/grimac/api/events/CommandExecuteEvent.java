package ac.grim.grimac.api.events;

import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.GrimUser;
import org.bukkit.event.HandlerList;

import java.util.function.Supplier;

public class CommandExecuteEvent extends FlagEvent {
    private static final HandlerList handlers = new HandlerList();
    private final String command;

    public CommandExecuteEvent(GrimUser user, AbstractCheck check, String verbose, String command) {
        super(user, check, verbose);
        this.command = command;
    }

    public CommandExecuteEvent(GrimUser user, AbstractCheck check, Supplier<String> verboseSupplier, String command) {
        super(user, check, verboseSupplier);
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
