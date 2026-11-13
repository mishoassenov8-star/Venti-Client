package dev.venti.modules.weather;

import dev.venti.config.BooleanSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

/**
 * Reports rain starting and stopping in chat, with an optional chime.
 *
 * <p>The 20-tick debounce is deliberate: a server that flickers between raining and clear for a
 * second does not spam you.</p>
 */
public final class WeatherNotifierModule extends Module {
    private static final int STABLE_TICKS = 20;

    private final BooleanSetting sound = this.add("sound", new BooleanSetting("Sound", true));

    private ClientWorld level;
    private boolean raining;
    private int changedTicks;

    public WeatherNotifierModule() {
        super("weather-notifier", "Weather Notifier", "Tells you when it starts or stops raining.", ModuleCategory.MISC);
    }

    @Override
    protected void onEnable() {
        this.level = null;
        this.raining = false;
        this.changedTicks = 0;
    }

    @Override
    protected void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld current = mc.world;
        if (current != this.level) {
            this.level = current;
            this.raining = false;
            this.changedTicks = 0;
        }
        if (current == null) {
            this.changedTicks = 0;
            return;
        }
        boolean now = current.isRaining();
        if (now == this.raining) {
            this.changedTicks = 0;
            return;
        }
        if (++this.changedTicks < STABLE_TICKS) {
            return;
        }
        this.raining = now;
        this.changedTicks = 0;
        this.chat(Text.literal(now ? "it started raining" : "it stopped raining"));
        if (this.sound.get() && mc.player != null) {
            mc.player.playSound(SoundEvents.UI_TOAST_IN, 1.0f, 1.0f);
        }
    }
}
