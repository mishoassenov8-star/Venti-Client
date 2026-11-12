package dev.venti.modules.nametags;

import dev.venti.config.BooleanSetting;
import dev.venti.config.ColorSetting;
import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import dev.venti.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Draws a name tag above nearby players.
 *
 * <p>The tag is only ever drawn by the vanilla label pass, which is depth tested: a tag
 * behind a wall is occluded exactly like the vanilla one. There is no "no depth" pass,
 * no glow-through-block mode and no off-screen fallback, so nothing here can reveal a
 * player you could not already see.</p>
 */
public final class NameTagsModule extends Module {
    private final NumberSetting scale = this.add("scale", new NumberSetting("Scale", 1.0f, 0.5f, 2.5f).step(0.05f));
    private final NumberSetting maxDistance = this.add("max-distance", new NumberSetting("Max Distance", 64.0f, 8.0f, 256.0f).step(1.0f));
    private final BooleanSetting showHealth = this.add("show-health", new BooleanSetting("Show Health", true));
    private final BooleanSetting showDistance = this.add("show-distance", new BooleanSetting("Show Distance", false));
    private final BooleanSetting background = this.add("background", new BooleanSetting("Background", true));
    private final ColorSetting nameColor = this.add("name-color", new ColorSetting("Name Color", 0xFFFFFFFF));
    private final ColorSetting friendColor = this.add("friend-color", new ColorSetting("Friend Color", 0xFF55FFFF));

    public NameTagsModule() {
        super("nametags", "Name Tags", "Vanilla-style name tags for nearby players. Never drawn through walls.", ModuleCategory.RENDER);
    }

    /** Returns the module when it is on, otherwise {@code null}. */
    public static NameTagsModule active() {
        NameTagsModule module = Modules.get(NameTagsModule.class);
        return module != null && module.isEnabled() ? module : null;
    }

    public static boolean shouldRender(Entity entity) {
        NameTagsModule module = active();
        return module != null && module.visible(entity);
    }

    public float scale() {
        return this.scale.get();
    }

    public float maxDistance() {
        return this.maxDistance.get();
    }

    public boolean health() {
        return this.showHealth.get();
    }

    public boolean distance() {
        return this.showDistance.get();
    }

    public boolean background() {
        return this.background.get();
    }

    public int nameColor() {
        return this.nameColor.argb();
    }

    public int friendColor() {
        return this.friendColor.argb();
    }

    /**
     * Vanilla-forged visibility: the entity has to be something the vanilla label pass would
     * consider in the first place (a live, loaded, visible player), and it has to be inside the
     * configured distance. Everything else is left to the vanilla renderer's own depth test.
     */
    private boolean visible(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            return false;
        }
        if (!player.isAlive() || player.isRemoved() || player.isInvisible()) {
            return false;
        }
        PlayerEntity self = MinecraftClient.getInstance().player;
        if (self == null || player == self) {
            return false;
        }
        double dx = player.getX() - self.getX();
        double dy = player.getY() - self.getY();
        double dz = player.getZ() - self.getZ();
        double limit = this.maxDistance.get();
        return dx * dx + dy * dy + dz * dz <= limit * limit;
    }
}
