package dev.venti.mixin;

import dev.venti.modules.render.NoRenderModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.DarknessEffectFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes the pulsing darkness the warden puts on the camera.
 *
 * <p>Same two pieces as blindness: the fog modifier and the extra dimming, both cancelled while
 * the toggle is on. Darkness still applies on the server — the warden still senses you — this only
 * takes the blackout off your screen.</p>
 */
@Mixin(DarknessEffectFogModifier.class)
public abstract class DarknessFogModifierMixin {
    @Inject(method = "applyStartEndModifier", at = @At("HEAD"), cancellable = true)
    private void venti$hideDarknessFog(FogData fog, Camera camera, ClientWorld world, float renderDistance,
                                       RenderTickCounter tickCounter, CallbackInfo ci) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideBlindnessDarkness()) {
            ci.cancel();
        }
    }

    @Inject(method = "applyDarknessModifier", at = @At("HEAD"), cancellable = true)
    private void venti$undimDarkness(LivingEntity entity, float darkness, float tickProgress,
                                     CallbackInfoReturnable<Float> cir) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideBlindnessDarkness()) {
            cir.setReturnValue(1.0f);
        }
    }
}
