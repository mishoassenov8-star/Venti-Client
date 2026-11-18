package dev.venti.modules.render;

import dev.venti.config.BooleanSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import dev.venti.module.Modules;

/**
 * Switches off individual overlays, screen effects and entity renders.
 *
 * <p>Every toggle is its own hook and every one of them only <em>removes</em> drawing: a cancelled
 * overlay is simply not drawn, a cancelled fog modifier leaves the fog as it was, and a hidden
 * falling block is not rendered at all. Nothing here adds geometry or reaches through walls, and
 * with the module off no mixin body does anything.</p>
 *
 * <p>The two that are off by default are the ones that hide something the game tells you about
 * the world — falling blocks and fire on entities — so you have to ask for them.</p>
 */
public final class NoRenderModule extends Module {
    private final BooleanSetting fireOverlay = this.add("fire-overlay", new BooleanSetting("Fire Overlay", true));
    private final BooleanSetting totemAnimation = this.add("totem-animation", new BooleanSetting("Totem Animation", true));
    private final BooleanSetting pumpkinOverlay = this.add("pumpkin-overlay", new BooleanSetting("Pumpkin Blur", true));
    private final BooleanSetting powderSnow = this.add("powder-snow", new BooleanSetting("Powder Snow", true));
    private final BooleanSetting nausea = this.add("nausea", new BooleanSetting("Nausea", true));
    private final BooleanSetting portal = this.add("portal", new BooleanSetting("Portal", true));
    private final BooleanSetting blindnessDarkness = this.add("blindness-darkness", new BooleanSetting("Blindness & Darkness", true));
    private final BooleanSetting spawnerMob = this.add("spawner-mob", new BooleanSetting("Spawner Mob", true));
    private final BooleanSetting fallingBlocks = this.add("falling-blocks", new BooleanSetting("Falling Blocks", false));
    private final BooleanSetting weather = this.add("weather", new BooleanSetting("Weather", true));
    private final BooleanSetting lavaFog = this.add("lava-fog", new BooleanSetting("Lava Fog", true));
    private final BooleanSetting entityFire = this.add("entity-fire", new BooleanSetting("Entity Fire", false));

    public NoRenderModule() {
        super("no-render", "No Render", "Hide selected overlays, screen effects and entity renders.", ModuleCategory.RENDER);
    }

    public static NoRenderModule active() {
        NoRenderModule module = Modules.get(NoRenderModule.class);
        return module != null && module.isEnabled() ? module : null;
    }

    public boolean hideFireOverlay() {
        return this.fireOverlay.get();
    }

    public boolean hideTotemAnimation() {
        return this.totemAnimation.get();
    }

    public boolean hidePumpkinBlur() {
        return this.pumpkinOverlay.get();
    }

    public boolean hidePowderSnow() {
        return this.powderSnow.get();
    }

    public boolean hideNausea() {
        return this.nausea.get();
    }

    public boolean hidePortal() {
        return this.portal.get();
    }

    public boolean hideBlindnessDarkness() {
        return this.blindnessDarkness.get();
    }

    public boolean hideSpawnerMob() {
        return this.spawnerMob.get();
    }

    public boolean hideFallingBlocks() {
        return this.fallingBlocks.get();
    }

    public boolean hideWeather() {
        return this.weather.get();
    }

    public boolean hideLavaFog() {
        return this.lavaFog.get();
    }

    public boolean hideEntityFire() {
        return this.entityFire.get();
    }
}
