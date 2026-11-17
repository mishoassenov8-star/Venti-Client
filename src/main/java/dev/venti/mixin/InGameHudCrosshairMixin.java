package dev.venti.mixin;

import dev.venti.modules.render.CrosshairModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides vanilla's crosshair while the Custom Crosshair module draws its own.
 *
 * <p>{@code renderCrosshair} also draws the attack indicator, so that is hidden with it; the module
 * replaces the crosshair only, not the indicator. With the module off the method is untouched.</p>
 */
@Mixin(InGameHud.class)
public abstract class InGameHudCrosshairMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void venti$hideVanillaCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (CrosshairModule.active() != null) {
            ci.cancel();
        }
    }
}
