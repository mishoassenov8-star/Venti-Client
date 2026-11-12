package dev.venti.modules.health;

import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_1657;

/** Warns in chat once your health drops below a threshold, with a cooldown so it does not spam. */
public final class LowHealthModule extends Module {
    private final NumberSetting threshold = this.add("threshold", new NumberSetting("Health", 6.0f, 1.0f, 20.0f).step(0.5f));
    private final NumberSetting cooldown = this.add("cooldown", new NumberSetting("Cooldown (s)", 5.0f, 1.0f, 30.0f).step(1.0f));

    private long lastWarning;
    private boolean wasLow;

    public LowHealthModule() {
        super("low-health", "Low Health", "Warns you when your health gets low.", ModuleCategory.MISC);
    }

    @Override
    protected void onEnable() {
        this.lastWarning = 0L;
        this.wasLow = false;
    }

    @Override
    protected void onTick() {
        class_310 mc = class_310.method_1551();
        class_1657 player = mc.field_1724;
        if (player == null || !player.method_5805()) {
            return;
        }
        float health = player.method_6032();
        boolean low = health > 0.0f && health <= this.threshold.get();
        long now = System.currentTimeMillis();
        if (low && (!this.wasLow || now - this.lastWarning >= (long)(this.cooldown.get() * 1000.0f))) {
            this.lastWarning = now;
            this.chat(class_2561.method_43470(String.format(Locale.ROOT, "low health: %.1f", health)));
        }
        this.wasLow = low;
    }
}
