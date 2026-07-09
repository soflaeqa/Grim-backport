package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow.WindowClickType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;

@CheckData(name = "BadPacketsP", stableKey = "grim.badpackets.invalid_click", description = "Invalid window click packet", experimental = true)
public class BadPacketsP extends Check implements PacketCheck {
    private static final Verbose V =
            Verbose.of("clickType={clicktype_lower}, button={sint}[, container={sint}]");

    private int containerType = -1;
    private int containerId = -1;

    public BadPacketsP(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
            WrapperPlayServerOpenWindow window = new WrapperPlayServerOpenWindow(event);
            this.containerType = window.getType();
            this.containerId = window.getContainerId();
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);
            WindowClickType clickType = wrapper.getWindowClickType();
            int button = wrapper.getButton();

            boolean flag;

            switch (clickType) {
                case PICKUP:
                case QUICK_MOVE:
                case CLONE:
                    flag = button > 2 || button < 0;
                    break;

                case THROW:
                    flag = button > 1 || button < 0;
                    break;

                default:
                    flag = false;
                    break;
            }

            // Allowing this to false flag to debug and find issues faster
            if (flag) {
                boolean hasContainer = wrapper.getWindowId() == containerId;
                if (flag(V.write(verbose()).uint(VerboseCodecs.enumId(clickType)).sint(button).bool(hasContainer).sint(containerType))
                        && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
