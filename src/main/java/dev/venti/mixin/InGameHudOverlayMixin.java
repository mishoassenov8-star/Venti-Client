package dev.venti.mixin;

import dev.venti.modules.render.NoRenderModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the HUD overlays: pumpkin blur, powder snow, nausea and the portal tint.
 *
 * <p>Pumpkin blur and powder snow both go through {@code renderOverlay}, which is shared with every
 * other full-screen overlay — including the ones mods and other equipment register — so the hook
 * only cancels the two textures it knows by name and leaves the rest alone. Nausea and the portal
 * have their own methods.</p>
 */
@Mixin(InGameHud.class)
public abstract class InGameHudOverlayMixin {
    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void venti$hideTextureOverlays(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
        NoRenderModule module = NoRenderModule.active();
        if (module == null) {
            return;
        }
        String path = texture.getPath();
        if (module.hidePumpkinBlur() && path.contains("pumpkin")) {
            ci.cancel();
        } else if (module.hidePowderSnow() && path.contains("powder_snow")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
    private void venti$hideNausea(DrawContext context, float opacity, CallbackInfo ci) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hideNausea()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void venti$hidePortal(DrawContext context, float opacity, CallbackInfo ci) {
        NoRenderModule module = NoRenderModule.active();
        if (module != null && module.hidePortal()) {
            ci.cancel();
        }
    }
}
