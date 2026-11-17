package dev.venti.modules.render;

import dev.venti.config.ColorSetting;
import dev.venti.config.ModeSetting;
import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import dev.venti.module.Modules;
import net.minecraft.client.gui.DrawContext;

/**
 * Draws a configurable crosshair and hides vanilla's while it is on.
 *
 * <p>Vanilla's crosshair is cancelled in {@code InGameHudCrosshairMixin}; the replacement is drawn
 * by a HUD element registered in {@code VentiMod}, so it sits in the same place but with the gap,
 * length, thickness and colour this module wants. The attack indicator is drawn by the same
 * vanilla method, so it is hidden too while the module is on.</p>
 */
public final class CrosshairModule extends Module {
    private final ModeSetting mode = this.add("mode", new ModeSetting("Mode", "Cross", "Cross", "Dot", "Cross + Dot"));
    private final ColorSetting color = this.add("color", new ColorSetting("Color", 0xFFFFFFFF));
    private final NumberSetting gap = this.add("gap", new NumberSetting("Gap", 2.0f, 0.0f, 12.0f).step(1.0f));
    private final NumberSetting length = this.add("length", new NumberSetting("Length", 6.0f, 1.0f, 20.0f).step(1.0f));
    private final NumberSetting thickness = this.add("thickness", new NumberSetting("Thickness", 2.0f, 1.0f, 6.0f).step(1.0f));

    public CrosshairModule() {
        super("crosshair", "Custom Crosshair", "Draws a configurable crosshair in place of the vanilla one.", ModuleCategory.RENDER);
        this.length.visibleWhen(() -> !this.mode.is("Dot"));
        this.gap.visibleWhen(() -> !this.mode.is("Dot"));
    }

    public static CrosshairModule active() {
        CrosshairModule module = Modules.get(CrosshairModule.class);
        return module != null && module.isEnabled() ? module : null;
    }

    /** Called from the HUD element registered in {@code VentiMod}. */
    public void render(DrawContext context) {
        int centerX = context.getScaledWindowWidth() / 2;
        int centerY = context.getScaledWindowHeight() / 2;
        int argb = this.color.argb();
        int thickness = Math.max(1, Math.round(this.thickness.get()));
        int half = thickness / 2;

        if (!this.mode.is("Dot")) {
            int gap = Math.round(this.gap.get());
            int length = Math.round(this.length.get());
            context.fill(centerX - gap - length, centerY - half, centerX - gap, centerY - half + thickness, argb);
            context.fill(centerX + gap, centerY - half, centerX + gap + length, centerY - half + thickness, argb);
            context.fill(centerX - half, centerY - gap - length, centerX - half + thickness, centerY - gap, argb);
            context.fill(centerX - half, centerY + gap, centerX - half + thickness, centerY + gap + length, argb);
        }

        if (this.mode.is("Dot") || this.mode.is("Cross + Dot")) {
            int dot = Math.max(2, thickness);
            context.fill(centerX - dot / 2, centerY - dot / 2, centerX - dot / 2 + dot, centerY - dot / 2 + dot, argb);
        }
    }
}
