package dev.venti.modules.coords;

import dev.venti.config.BooleanSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

/** Copies your current position to the clipboard when the module is toggled on, then turns itself off. */
public final class CopyCoordsModule extends Module {
    private final BooleanSetting withDimension = this.add("with-dimension", new BooleanSetting("Include Dimension", true));
    private final BooleanSetting asWaypoint = this.add("as-waypoint", new BooleanSetting("Waypoint Format", false));

    public CopyCoordsModule() {
        super("copy-coords", "Copy Coords", "Puts your current coordinates on the clipboard.", ModuleCategory.MISC);
    }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        String text;
        if (this.asWaypoint.get()) {
            text = String.format(Locale.ROOT, "[%d, %d, %d]", player.getBlockPos().getX(), player.getBlockPos().getY(), player.getBlockPos().getZ());
        } else {
            text = String.format(Locale.ROOT, "%.1f %.1f %.1f", player.getX(), player.getY(), player.getZ());
        }
        if (this.withDimension.get() && mc.world != null) {
            text = text + " (" + mc.world.getRegistryKey().getValue() + ")";
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            this.chat(Text.literal("copied " + text));
        } catch (RuntimeException e) {
            LOGGER.warn("[Venti] Copy Coords could not use the clipboard", e);
            this.chat(Text.literal("clipboard unavailable: " + text));
        }
        this.setEnabled(false);
    }
}
