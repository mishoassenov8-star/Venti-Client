package dev.venti.modules.movement;

import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Keeps the sprint state up from the tick loop.
 *
 * <p>Same observable behaviour as vanilla toggle sprint: it only applies while you are moving
 * forward, you are not using an item and you are not sneaking.</p>
 */
public final class SprintModule extends Module {
    public SprintModule() {
        super("sprint", "Sprint", "Keeps you sprinting while you move forward, like vanilla Toggle Sprint.", ModuleCategory.MISC);
    }

    @Override
    protected void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null || mc.currentScreen != null) {
            return;
        }
        if (player.forwardSpeed > 0.0f && !player.isUsingItem() && !player.isSneaking()) {
            player.setSprinting(true);
        }
    }
}
