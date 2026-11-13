package dev.venti.mixin;

import dev.venti.modules.render.FullbrightModule;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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
