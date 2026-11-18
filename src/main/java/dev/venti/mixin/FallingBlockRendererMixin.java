package dev.venti.mixin;

import dev.venti.modules.render.NoRenderModule;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.FallingBlockEntityRenderer;
import net.minecraft.entity.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Skips the render of sand, gravel and anvils while they fall.
 *
 * <p>{@code shouldRender} is vanilla's own culling check, so answering false means the entity is
 * never rendered at all rather than drawn and covered up. Off by default: a falling block is
 * information about the world, not an overlay in your face.</p>
 */
@Mixin(FallingBlockEntityRenderer.class)
public abstract class FallingBlockRendererMixin {
    @Inject(
            method = "shouldRender(Lnet/minecraft/entity/FallingBlockEntity;"
                    + "Lnet/minecraft/client/render/Frustum;DDD)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void venti$hideFallingBlocks(FallingBlockEntity entity, Frustum frustum,
                                         double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideFallingBlocks()) {
            cir.setReturnValue(false);
        }
    }
}
