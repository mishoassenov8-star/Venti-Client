package dev.venti.modules.render;

import dev.venti.config.BooleanSetting;
import dev.venti.config.ColorSetting;
import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import dev.venti.module.Modules;

/**
 * Recolours and thickens the outline vanilla draws around the block you look at.
 *
 * <p>This is the vanilla selection outline, not a second one: {@code BlockOverlayOutlineMixin}
 * rewrites the colour and line-width arguments on the {@code drawBlockOutline} call that already
 * runs for the targeted block, so the shape follows whatever the block's actual outline is, the
 * breaking overlay still works, and the result is depth tested exactly like vanilla — it cannot
 * show a block through a wall. With the module off nothing is substituted.</p>
 */
public final class BlockOverlayModule extends Module {
    private final ColorSetting color = this.add("color", new ColorSetting("Color", 0xFF4FC3F7));
    private final NumberSetting thickness = this.add("thickness", new NumberSetting("Thickness", 2.0f, 1.0f, 6.0f).step(0.5f));
    private final BooleanSetting pulse = this.add("pulse", new BooleanSetting("Pulse", true));

    public BlockOverlayModule() {
        super("block-overlay", "Block Overlay", "A custom outline on the block you look at.", ModuleCategory.RENDER);
    }

    public static BlockOverlayModule active() {
        BlockOverlayModule module = Modules.get(BlockOverlayModule.class);
        return module != null && module.isEnabled() ? module : null;
    }

    /** The ARGB colour to hand to the outline, breathing in alpha when Pulse is on. */
    public int outlineColor() {
        int argb = this.color.argb();
        if (!this.pulse.get()) {
            return argb;
        }
        int alpha = (argb >>> 24) & 0xFF;
        double wave = 0.55 + 0.45 * ((Math.sin(System.currentTimeMillis() / 380.0) + 1.0) / 2.0);
        int pulsed = Math.max(0, Math.min(255, (int) (alpha * wave)));
        return (pulsed << 24) | (argb & 0x00FFFFFF);
    }

    public float thickness() {
        return Math.max(1.0f, this.thickness.get());
    }
}
