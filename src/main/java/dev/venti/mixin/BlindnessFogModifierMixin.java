package dev.venti.mixin;

import dev.venti.modules.render.NoRenderModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.BlindnessEffectFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes the screen fog and the dimming that blindness puts on the camera.
 *
 * <p>Blindness has two pieces in 1.21.11: {@code applyStartEndModifier} pulls the fog right up to
 * your face, and {@code applyDarknessModifier} darkens what is left. Cancelling the first and
 * returning 1.0 — the value vanilla uses for "no darkening" — from the second leaves the effect
 * itself alone: it still ticks on you, the HUD icon is still there, and the server still counts it.
 * Only what you see changes.</p>
 */
@Mixin(BlindnessEffectFogModifier.class)
public abstract class BlindnessFogModifierMixin {
    @Inject(method = "applyStartEndModifier", at = @At("HEAD"), cancellable = true)
    private void venti$hideBlindnessFog(FogData fog, Camera camera, ClientWorld world, float renderDistance,
                                        RenderTickCounter tickCounter, CallbackInfo ci) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideBlindnessDarkness()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyDarknessModifier", at = @At("HEAD"), cancellable = true)
    private void venti$undimBlindness(LivingEntity entity, float darkness, float tickProgress,
                                      CallbackInfoReturnable<Float> cir) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideBlindnessDarkness()) {
            cir.setReturnValue(1.0f);
        }
    }
}
