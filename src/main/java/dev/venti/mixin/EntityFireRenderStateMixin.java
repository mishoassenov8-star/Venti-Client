package dev.venti.mixin;

import dev.venti.modules.render.NoRenderModule;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops the flames being drawn on burning entities.
 *
 * <p>Every entity's render state passes through {@code getAndUpdateRenderState}, which carries the
 * {@code onFire} flag the renderer uses to draw them. Clearing it on the way out means the entity
 * is drawn exactly as if it were not burning; the fire itself, its damage and its particles are
 * untouched. Off by default, because whether someone is on fire is worth seeing.</p>
 */
@Mixin(EntityRenderManager.class)
public abstract class EntityFireRenderStateMixin {
    @Inject(method = "getAndUpdateRenderState", at = @At("RETURN"))
    private void venti$hideEntityFire(net.minecraft.entity.Entity entity, float tickProgress,
                                      CallbackInfoReturnable<EntityRenderState> cir) {
        NoRenderModule module = NoRenderModule.active();
        EntityRenderState state = cir.getReturnValue();
        if (module != null && state != null && module.hideEntityFire()) {
            state.onFire = false;
        }
    }
}
