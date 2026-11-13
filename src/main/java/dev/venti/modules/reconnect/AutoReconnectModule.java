package dev.venti.modules.reconnect;

import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import java.util.Locale;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public final class AutoReconnectModule extends Module {
    private final NumberSetting delay = this.add("delay", new NumberSetting("Delay", 5.0f, 3.0f, 60.0f).step(1.0f));

    private ServerInfo lastServer;
    private Screen activeScreen;
    private ButtonWidget reconnectButton;
    private int ticksLeft;
    private boolean held;

    public AutoReconnectModule() {
        super("auto-reconnect", "Auto Reconnect", "Reconnects to the last server after an unexpected disconnect.", ModuleCategory.MISC);
        ScreenEvents.AFTER_INIT.register(this::onScreenInit);
    }

    @Override
    protected void onEnable() {
        this.lastServer = null;
        this.activeScreen = null;
        this.reconnectButton = null;
        this.ticksLeft = 0;
        this.held = false;
    }

    @Override
    protected void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) {
            return;
        }
        ServerInfo server = mc.isInSingleplayer() ? null : mc.getCurrentServerEntry();
        this.lastServer = server == null || server.isLocal() ? null : server;
    }

    private void onScreenInit(MinecraftClient client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof TitleScreen) {
            this.lastServer = null;
            this.activeScreen = null;
            return;
        }
        if (!(screen instanceof DisconnectedScreen) || this.lastServer == null) {
            return;
        }
        ServerInfo server = this.lastServer;
        if (screen == this.activeScreen) {
            Screens.getButtons(screen).remove(this.reconnectButton);
        } else {
            this.activeScreen = screen;
            this.ticksLeft = Math.round(this.delay.get() * 20.0f);
            this.held = isBan(screen);
        }
        boolean auto = this.isEnabled() && !this.held;
        this.reconnectButton = ButtonWidget.builder(label(auto, this.ticksLeft), button -> {
                    button.active = false;
                    reconnect(client, server);
                })
                .dimensions(scaledWidth - 150 - 10, scaledHeight - 20 - 10, 150, 20)
                .build();
        Screens.getButtons(screen).add(this.reconnectButton);
        if (auto) {
            ScreenEvents.afterTick(screen).register(ticked -> {
                this.ticksLeft--;
                if (this.ticksLeft <= 0) {
                    this.reconnectButton.active = false;
                    reconnect(client, server);
                    return;
                }
                this.reconnectButton.setMessage(label(true, this.ticksLeft));
            });
        }
    }

    private static boolean isBan(Screen screen) {
        String reason = screen.getTitle().getString().toLowerCase(Locale.ROOT);
        return reason.contains("banned") || reason.contains("blacklisted");
    }

    private static Text label(boolean auto, int ticksLeft) {
        return Text.literal(auto ? String.format(Locale.ROOT, "Reconnect (%.1f)", ticksLeft / 20.0) : "Reconnect");
    }

    private static void reconnect(MinecraftClient client, ServerInfo server) {
        ConnectScreen.connect(new TitleScreen(), client, ServerAddress.parse(server.address), server, false, null);
    }
}
