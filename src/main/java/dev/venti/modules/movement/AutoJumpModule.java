package dev.venti.modules.movement;

import dev.venti.config.ModeSetting;
import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * <p>Same two modes: {@code Jump} uses the vanilla jump, {@code LowHop} replaces the vertical
 * velocity with the configured height so you clear a block without the full jump arc. Same
 * {@code Jump If} gate too — Always, Walking, or only while sprinting.</p>
 */
public final class AutoJumpModule extends Module {
    private final ModeSetting mode = this.add("mode", new ModeSetting("Mode", "Jump", "Jump", "LowHop"));
    private final ModeSetting jumpIf = this.add("jump-if", new ModeSetting("Jump If", "Always", "Sprinting", "Walking", "Always"));
    private final NumberSetting velocityHeight = this.add("velocity-height", new NumberSetting("Velocity Height", 0.25f, 0.0f, 2.0f).step(0.05f));

    public AutoJumpModule() {
        super("auto-jump", "Auto Jump", "Automatically jumps.", ModuleCategory.MISC);
    }

    @Override
    protected void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null || mc.currentScreen != null) {
            return;
        }
        if (!player.isOnGround() || player.isUsingItem() || !this.shouldJump(player)) {
            return;
        }
        if (this.mode.is("Jump")) {
            player.jump();
            return;
        }
        Vec3d velocity = player.getVelocity();
        player.setVelocity(velocity.x, this.velocityHeight.get(), velocity.z);
    }

    private boolean shouldJump(PlayerEntity player) {
        boolean moving = player.forwardSpeed != 0.0f || player.sidewaysSpeed != 0.0f;
        return switch (this.jumpIf.get()) {
            case "Sprinting" -> player.isSprinting() && moving;
            case "Walking" -> moving;
            default -> true;
        };
    }
}
