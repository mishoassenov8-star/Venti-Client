package dev.venti.modules.render;

import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import dev.venti.module.Modules;
import org.lwjgl.glfw.GLFW;

/**
 * Hold-to-zoom: the field of view is divided by the chosen factor while the key is held down.
 *
 * <p>The module is a hold bind, so it is only on while the key is down and the screen is closed.
 * Nothing is written to the config for a hold, and the factor eases towards its target in log
 * space so the view does not snap. The division itself happens in
 * {@code GameRendererFovMixin}, on the value vanilla already computed.</p>
 */
public final class ZoomModule extends Module {
    private static final float IDLE_FACTOR = 1.0f;

    private final NumberSetting zoom = this.add("zoom", new NumberSetting("Zoom", 4.0f, 1.5f, 20.0f).step(0.5f));
    private final NumberSetting smoothness = this.add("smoothness", new NumberSetting("Smoothness", 60.0f, 0.0f, 100.0f).step(5.0f));

    private float factor = IDLE_FACTOR;
    private long lastNanos;

    public ZoomModule() {
        super("zoom", "Zoom", "Hold the key to zoom in.", ModuleCategory.RENDER);
        this.setHold(true);
        this.settings().bind = GLFW.GLFW_KEY_C;
    }

    public static ZoomModule active() {
        ZoomModule module = Modules.get(ZoomModule.class);
        return module != null && module.isEnabled() ? module : null;
    }

    /** Current zoom factor, eased towards the target. Values above 1 mean "divide the FOV by this". */
    public float factor() {
        long now = System.nanoTime();
        float seconds = this.lastNanos == 0L ? 0.0f : Math.min(0.1f, (now - this.lastNanos) / 1.0E9f);
        this.lastNanos = now;

        float target = this.isEnabled() ? this.zoom.get() : IDLE_FACTOR;
        float smooth = this.smoothness.get() / 100.0f;
        if (smooth <= 0.0f || this.factor == target) {
            this.factor = target;
            return this.factor;
        }

        float rate = 30.0f - 25.0f * smooth;
        float blend = 1.0f - (float) Math.exp(-seconds * rate);
        double from = Math.log(this.factor);
        double to = Math.log(target);
        this.factor = (float) Math.exp(from + (to - from) * blend);
        if (Math.abs(this.factor - target) < 0.002f) {
            this.factor = target;
        }
        return this.factor;
    }
}
