package dev.venti.mixin;

import dev.venti.modules.nametags.NameTagsModule;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The only nametag hook in the mod, and it only ever subtracts.
 *
 * <p>{@code hasLabel} is vanilla's own "should this entity get a name tag" check, and the pass
 * that follows it is depth tested. Cancelling here means a tag is drawn exactly as vanilla would
 * draw it — including being hidden behind a wall — and the module can only take tags away, never
 * add one you could not already see. There is deliberately no hook on the render call itself,
 * so nothing can sneak a no-depth layer in later.</p>
 */
@Mixin(EntityRenderer.class)
public abstract class NametagRendererMixin {
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void venti$gateNameTags(Entity entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (NameTagsModule.shouldHide(entity)) {
            cir.setReturnValue(false);
        }
    }
}
