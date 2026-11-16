package dev.venti.mixin;

import dev.venti.modules.render.FullbrightModule;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Raises the gamma argument fed to the lightmap.
 *
 * <p>Core Mixin's {@code @ModifyArg} needs no extra dependency. The target is the seventh
 * {@code putFloat} on the {@code Std140Builder} inside {@code LightmapTextureManager#update(float)},
 * which is the gamma slot for Minecraft 1.21.11. If Minecraft ever reorders those floats this
 * method stops matching and {@code defaultRequire: 1} fails the mixin loudly instead of silently
 * brightening the wrong value.</p>
 */
@Mixin(LightmapTextureManager.class)
public abstract class FullbrightLightmapMixin {
    @ModifyArg(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
                    ordinal = 6),
            index = 0)
    private float venti$fullbrightGamma(float gamma) {
        FullbrightModule module = FullbrightModule.active();
        if (module == null) {
            return gamma;
        }
        return Math.max(gamma, module.gamma());
    }
}
