package dev.venti.mixin;

import dev.venti.modules.render.NoRenderModule;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.fog.LavaFogModifier;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps the lava fog off the camera.
 *
 * <p>1.21.11 applies fog through modifier objects, and each one decides whether it applies through
 * {@code shouldApply}. Declining it leaves the fog as it was before lava came into view: you still
 * see the orange tint and the lava is still there, you just are not blinded by it.</p>
 */
@Mixin(LavaFogModifier.class)
public abstract class LavaFogModifierMixin {
    @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
    private void venti$hideLavaFog(CameraSubmersionType type, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideLavaFog()) {
            cir.setReturnValue(false);
        }
    }
}
