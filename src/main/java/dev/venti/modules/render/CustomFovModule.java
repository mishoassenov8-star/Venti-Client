package dev.venti.modules.render;

import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import dev.venti.module.Modules;
import net.minecraft.client.MinecraftClient;

/**
 * Replaces the field of view with a fixed value the player picks.
 *
 * <p>The value is substituted in {@code GameRendererFovMixin} after vanilla has worked out its own
 * FOV, so sprinting, flying, underwater and spyglass modifiers are all replaced rather than added
 * to. The panorama passes keep their own projection. Changing the value rebuilds the chunk meshes,
 * because the culling frustum is built from the old projection.</p>
 */
public final class CustomFovModule extends Module {
    private final NumberSetting fov = this.add("fov", new NumberSetting("FOV", 110.0f, 30.0f, 160.0f).step(1.0f));

    public CustomFovModule() {
        super("custom-fov", "Custom Fov", "Changes your field of view (FOV).", ModuleCategory.RENDER);
        this.fov.onChange(this::rebuildChunks);
    }

    public static CustomFovModule active() {
        CustomFovModule module = Modules.get(CustomFovModule.class);
        return module != null && module.isEnabled() ? module : null;
    }

    public float fov() {
        return this.fov.get();
    }

    @Override
    protected void onEnable() {
        this.rebuildChunks();
    }

    @Override
    protected void onDisable() {
        this.rebuildChunks();
    }

    private void rebuildChunks() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.worldRenderer == null) {
            return;
        }
        mc.execute(mc.worldRenderer::reload);
    }
}
