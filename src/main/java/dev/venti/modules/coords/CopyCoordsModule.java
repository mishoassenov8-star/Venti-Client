package dev.venti.modules.coords;

import dev.venti.config.BooleanSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_1657;

/** Copies your current position to the clipboard when the module is toggled on, then turns itself off. */
public final class CopyCoordsModule extends Module {
    private final BooleanSetting withDimension = this.add("with-dimension", new BooleanSetting("Include Dimension", true));
    private final BooleanSetting asWaypoint = this.add("as-waypoint", new BooleanSetting("Waypoint Format", false));

    public CopyCoordsModule() {
        super("copy-coords", "Copy Coords", "Puts your current coordinates on the clipboard.", ModuleCategory.MISC);
    }

    @Override
    protected void onEnable() {
        class_310 mc = class_310.method_1551();
        class_1657 player = mc.field_1724;
        if (player == null) {
            return;
        }
        String text;
        if (this.asWaypoint.get()) {
            var pos = player.method_24515();
            text = String.format(Locale.ROOT, "[%d, %d, %d]", pos.method_10263(), pos.method_10264(), pos.method_10260());
        } else {
            text = String.format(Locale.ROOT, "%.1f %.1f %.1f", player.method_23317(), player.method_23318(), player.method_23321());
        }
        if (this.withDimension.get() && mc.field_1687 != null) {
            text = text + " (" + mc.field_1687.method_27983().method_29177() + ")";
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            this.chat(class_2561.method_43470("copied " + text));
        } catch (RuntimeException e) {
            LOGGER.warn("[Venti] Copy Coords could not use the clipboard", e);
            this.chat(class_2561.method_43470("clipboard unavailable: " + text));
        }
        this.setEnabled(false);
    }
}
