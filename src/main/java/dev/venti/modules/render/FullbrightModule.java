package dev.venti.modules.render;

import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import dev.venti.module.Modules;

/**
 * Brightens the client lightmap gamma and never dims it below vanilla.
 *
 * <p>One setting and a 1.0 floor. The work happens in {@code FullbrightLightmapMixin}, which
 * raises the gamma argument inside {@code LightmapTextureManager#update}.</p>
 */
public final class FullbrightModule extends Module {
    private final NumberSetting gamma = this.add("gamma", new NumberSetting("Gamma", 8.0f, 1.0f, 16.0f).step(0.5f));

    public FullbrightModule() {
        super("fullbright", "Fullbright", "Brighten dark areas by overriding the client lightmap gamma.", ModuleCategory.RENDER);
    }

    public static FullbrightModule active() {
        FullbrightModule module = Modules.get(FullbrightModule.class);
        return module != null && module.isEnabled() ? module : null;
    }

    public float gamma() {
        return Math.max(1.0f, this.gamma.get());
    }
}
