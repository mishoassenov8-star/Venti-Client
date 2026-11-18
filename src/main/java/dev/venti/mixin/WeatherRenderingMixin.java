package dev.venti.mixin;

import dev.venti.modules.render.NoRenderModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.render.state.WeatherRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops rain and snow from being drawn.
 *
 * <p>Two hooks: {@code renderPrecipitation} draws the falling streaks and snowflakes, and
 * {@code addParticlesAndSound} adds the splash particles around the camera. That second method is
 * also where vanilla plays the occasional rain sound, so while this toggle is on you lose those
 * too — they are part of the same call and splitting them is not worth a second hook.</p>
 */
@Mixin(WeatherRendering.class)
public abstract class WeatherRenderingMixin {
    @Inject(method = "renderPrecipitation", at = @At("HEAD"), cancellable = true)
    private void venti$hidePrecipitation(VertexConsumerProvider vertexConsumers, Vec3d pos,
                                         WeatherRenderState state, CallbackInfo ci) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideWeather()) {
            ci.cancel();
        }
    }

    @Inject(method = "addParticlesAndSound", at = @At("HEAD"), cancellable = true)
    private void venti$hideRainSplashes(ClientWorld world, Camera camera, int ticks, ParticlesMode particlesMode,
                                        int radius, CallbackInfo ci) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideWeather()) {
            ci.cancel();
        }
    }
}
