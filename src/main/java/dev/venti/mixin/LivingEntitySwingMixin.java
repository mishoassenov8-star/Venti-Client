package dev.venti.mixin;

import dev.venti.modules.misc.SlowSwingModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scales how many ticks the swing animation lasts, for the local player only.
 *
 * <p>{@code getHandSwingDuration} feeds both the progress curve and the item-in-hand swing, so
 * changing this one return value changes the whole first-person animation without touching
 * packets or combat timing. Other entities keep the vanilla duration.</p>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {
    @Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true)
    private void venti$swingDuration(CallbackInfoReturnable<Integer> cir) {
        SlowSwingModule module = SlowSwingModule.active();
        if (module == null || (Object) this != MinecraftClient.getInstance().player) {
            return;
        }
        cir.setReturnValue(module.duration(cir.getReturnValueI()));
    }
}
