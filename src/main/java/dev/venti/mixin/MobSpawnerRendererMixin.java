package dev.venti.mixin;

import dev.venti.modules.render.NoRenderModule;
import net.minecraft.client.render.block.entity.MobSpawnerBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.MobSpawnerBlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the small spinning mob inside a spawner.
 *
 * <p>That mob is all this renderer draws — the block itself comes from the block model renderer —
 * so cancelling here leaves the spawner block looking exactly like vanilla, just without the
 * miniature. The descriptor is spelled out because the class has a second, bridge-shaped
 * {@code render} that must not be confused with it.</p>
 */
@Mixin(MobSpawnerBlockEntityRenderer.class)
public abstract class MobSpawnerRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/render/block/entity/state/MobSpawnerBlockEntityRenderState;"
                    + "Lnet/minecraft/client/util/math/MatrixStack;"
                    + "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;"
                    + "Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void venti$hideSpawnerMob(MobSpawnerBlockEntityRenderState state, MatrixStack matrices,
                                      OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideSpawnerMob()) {
            ci.cancel();
        }
    }
}
