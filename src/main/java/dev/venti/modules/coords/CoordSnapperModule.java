package dev.venti.modules.coords;

import dev.venti.config.BooleanSetting;
import dev.venti.config.ModeSetting;
import dev.venti.config.NumberSetting;
import dev.venti.module.Module;
import dev.venti.module.ModuleCategory;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Copies the coordinates you look at, or the block you stand on, to the clipboard on your bind.
 *
 * <p>Looked At / Own Position target, 16-512 block raycast range, Stop at Fluids switch, three
 * formats and an optional dimension suffix. Pressing the bind is the trigger, and the module
 * switches itself off afterwards like Copy Coords.</p>
 *
 * <p>There is no upload path: coordinates never leave the client.</p>
 */
public final class CoordSnapperModule extends Module {
    private final ModeSetting target = this.add("target", new ModeSetting("Target", "Looked At", "Looked At", "Own Position"));
    private final NumberSetting range = this.add("range", new NumberSetting("Range", 256.0f, 16.0f, 512.0f).step(1.0f));
    private final BooleanSetting stopAtFluids = this.add("stop-at-fluids", new BooleanSetting("Stop at Fluids", false));
    private final ModeSetting format = this.add("format", new ModeSetting("Format", "X Y Z", "X Y Z", "X, Y, Z", "/tp Command"));
    private final BooleanSetting includeDimension = this.add("include-dimension", new BooleanSetting("Include Dimension", true));
    private final BooleanSetting confirm = this.add("confirm", new BooleanSetting("Chat Confirmation", true));

    public CoordSnapperModule() {
        super("coord-snapper", "Coord Snapper", "Copies the coordinates you look at or stand on, on your bind.", ModuleCategory.MISC);
        this.range.visibleWhen(() -> this.target.is("Looked At"));
        this.stopAtFluids.visibleWhen(() -> this.target.is("Looked At"));
    }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        BlockPos pos;
        if (this.target.is("Looked At")) {
            HitResult hit = player.raycast(this.range.get(), 1.0f, this.stopAtFluids.get());
            if (!(hit instanceof BlockHitResult block)) {
                if (this.confirm.get()) {
                    this.chat(Text.literal("nothing in range"));
                }
                this.setEnabled(false);
                return;
            }
            pos = block.getBlockPos();
        } else {
            pos = player.getBlockPos();
        }
        String text = this.format(pos);
        if (this.includeDimension.get() && mc.world != null) {
            text = text + " (" + mc.world.getRegistryKey().getValue() + ")";
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            if (this.confirm.get()) {
                this.chat(Text.literal("copied " + text));
            }
        } catch (RuntimeException e) {
            LOGGER.warn("[Venti] Coord Snapper could not use the clipboard", e);
            this.chat(Text.literal("clipboard unavailable: " + text));
        }
        this.setEnabled(false);
    }

    private String format(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return switch (this.format.get()) {
            case "X, Y, Z" -> String.format(Locale.ROOT, "%d, %d, %d", x, y, z);
            case "/tp Command" -> String.format(Locale.ROOT, "/tp %d %d %d", x, y, z);
            default -> String.format(Locale.ROOT, "%d %d %d", x, y, z);
        };
    }
}
