package dev.venti.modules.render;

import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import dev.venti.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

/**
 * Client-only time override in the -20000..20000 range.
 *
 * <p>Catches the server's time update and substitutes the forced value, so the server keeps its
 * own clock and nothing about it changes server-side. The interception is in
 * {@code TimeChangerClientWorldMixin}.</p>
 *
 * <p>When the module turns off, the last server time it saw is written back, so the world
 * resumes roughly where it was rather than staying frozen at the forced value.</p>
 */
public final class TimeChangerModule extends Module {
    private final NumberSetting time = this.add("time", new NumberSetting("Time", 0.0f, -20000.0f, 20000.0f).step(1.0f));

    private ClientWorld trackedLevel;
    private long serverDayTime;
    private boolean sawServerTime;

    public TimeChangerModule() {
        super("time-changer", "Time Changer", "Makes you able to set a custom time.", ModuleCategory.RENDER);
    }

    public static TimeChangerModule active() {
        TimeChangerModule module = Modules.get(TimeChangerModule.class);
        return module != null && module.isEnabled() ? module : null;
    }

    @Override
    protected void onEnable() {
        this.trackedLevel = null;
        this.serverDayTime = 0L;
        this.sawServerTime = false;
    }

    @Override
    protected void onDisable() {
        ClientWorld level = MinecraftClient.getInstance().world;
        if (level != null && level == this.trackedLevel && this.sawServerTime) {
            level.setTime(level.getTime(), this.serverDayTime, false);
        }
        this.trackedLevel = null;
        this.sawServerTime = false;
    }

    /** Called from the mixin. Returns the time the client should show instead of the server's. */
    public long intercept(ClientWorld level, long dayTime) {
        this.trackedLevel = level;
        this.serverDayTime = dayTime;
        this.sawServerTime = true;
        return this.forcedTime();
    }

    public long forcedTime() {
        return (long) this.time.get().floatValue();
    }
}
