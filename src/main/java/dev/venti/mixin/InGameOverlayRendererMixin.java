package dev.venti.mixin;

import dev.venti.modules.render.NoRenderModule;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the fire overlay and the totem pop animation.
 *
 * <p>{@code renderFireOverlay} is the vanilla pass that draws flames across the screen while you
 * burn; cancelling it removes that pass only. {@code setFloatingItem} is where vanilla hands the
 * popped totem to the overlay renderer, so cancelling there means the float animation never
 * starts — the totem's own particles and sound are untouched.</p>
 */
@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void venti$hideFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                               Sprite sprite, CallbackInfo ci) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideFireOverlay()) {
            ci.cancel();
        }
    }

    @Inject(method = "setFloatingItem", at = @At("HEAD"), cancellable = true)
    private void venti$hideTotemAnimation(ItemStack stack, Random random, CallbackInfo ci) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideTotemAnimation()) {
            ci.cancel();
        }
    }
}
