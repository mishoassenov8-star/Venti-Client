package dev.venti.mixin;

import dev.venti.modules.render.TimeChangerModule;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientWorld.class)
public abstract class TimeChangerClientWorldMixin {
    @Redirect(
            method = "setTime",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/world/ClientWorld$Properties;setTimeOfDay(J)V"))
    private void venti$forceTimeOfDay(ClientWorld.Properties properties, long dayTime) {
        TimeChangerModule module = TimeChangerModule.active();
        if (module == null) {
            properties.setTimeOfDay(dayTime);
            return;
        }
        properties.setTimeOfDay(module.intercept((ClientWorld) (Object) this, dayTime));
    }
}
