package dev.venti.mixin;

import dev.venti.modules.render.BlockOverlayModule;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Swaps the colour and line width of the vanilla selection outline.
 *
 * <p>{@code renderTargetBlockOutline} calls {@code drawBlockOutline} twice: once for the
 * high-contrast pass and once for the normal one. Only the normal call is touched (ordinal 1), so
 * with Minecraft's own high-contrast outline enabled that pass still draws as vanilla. Arguments
 * 6 and 7 are the ARGB colour and the line width; everything else, including the shape and the
 * depth-tested line layer, stays vanilla's.</p>
 */
@Mixin(WorldRenderer.class)
public abstract class BlockOverlayOutlineMixin {
    @ModifyArgs(
            method = "renderTargetBlockOutline",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;DDDLnet/minecraft/client/render/state/OutlineRenderState;IF)V",
                    ordinal = 1))
    private void venti$blockOverlay(Args args) {
        BlockOverlayModule module = BlockOverlayModule.active();
        if (module == null) {
            return;
        }
        args.set(6, module.outlineColor());
        args.set(7, module.thickness());
    }
}
