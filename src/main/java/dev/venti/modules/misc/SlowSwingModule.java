package dev.venti.modules.misc;

import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import dev.venti.module.Modules;

/**
 * Stretches the local player's hand swing animation.
 *
 * <p>The setting is a speed multiplier: 0.25 makes vanilla's swing last four times as long, 1.0
 * leaves it alone and 2.0 halves it. Only the local player is touched, so other players keep
 * vanilla timing. The hook is {@code LivingEntitySwingMixin} on {@code getHandSwingDuration}.</p>
 */
public final class SlowSwingModule extends Module {
    private final NumberSetting speed = this.add("speed", new NumberSetting("Speed", 0.25f, 0.1f, 2.0f).step(0.05f));

    public SlowSwingModule() {
        super("slow-swing", "Slow Swing", "Control hand swing animation speed.", ModuleCategory.MISC);
    }

    public static SlowSwingModule active() {
        SlowSwingModule module = Modules.get(SlowSwingModule.class);
        return module != null && module.isEnabled() ? module : null;
    }

    public int duration(int vanilla) {
        float multiplier = Math.max(0.1f, this.speed.get());
        return Math.max(1, Math.round(vanilla / multiplier));
    }
}
