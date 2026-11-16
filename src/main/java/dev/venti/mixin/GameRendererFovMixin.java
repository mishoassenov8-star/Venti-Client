package dev.venti.mixin;

import dev.venti.modules.render.CustomFovModule;
import dev.venti.modules.render.ZoomModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Rewrites the field of view vanilla already computed, and only that value.
 *
 * <p>{@code getFov(Camera, float, boolean)} is the single place the projection's FOV comes from,
 * so both Custom FOV and Zoom are applied here instead of being re-derived from scratch. The
 * panorama passes are left alone because they build their own cube-map projection. With neither
 * module on, the mixin hands back the original value unchanged.</p>
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererFovMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void venti$applyFov(Camera camera, float tickDelta, boolean changingFov,
                                CallbackInfoReturnable<Float> cir) {
        GameRenderer renderer = (GameRenderer) (Object) this;
        if (renderer.isRenderingPanorama()) {
            return;
        }

        float fov = cir.getReturnValueF();
        CustomFovModule custom = CustomFovModule.active();
        if (custom != null) {
            fov = custom.fov();
        }

        ZoomModule zoom = ZoomModule.active();
        if (zoom != null) {
            float factor = zoom.factor();
            if (factor > 1.0001f) {
                fov /= factor;
            }
        }

        cir.setReturnValue(fov);
    }
}
