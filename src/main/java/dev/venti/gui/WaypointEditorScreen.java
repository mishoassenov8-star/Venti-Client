package dev.venti.gui;

import dev.venti.modules.render.WaypointsModule;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Lists the saved waypoints with a Delete button per row, and adds the spot you stand on.
 *
 * <p>Opened from the Waypoints bind, which is a bind action rather than a toggle, so pressing the
 * key opens this screen and leaves the module's on/off state alone. Changes are written straight
 * to the module config, so closing the screen keeps them.</p>
 */
public final class WaypointEditorScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int VISIBLE_ROWS = 6;
    private static final int LIST_WIDTH = 240;
    private static final int DELETE_WIDTH = 64;

    private final WaypointsModule module;
    private int page;

    public WaypointEditorScreen(WaypointsModule module) {
        super(Text.literal("Waypoints"));
        this.module = module;
    }

    @Override
    protected void init() {
        List<String> entries = this.module.entries();
        int pages = Math.max(1, (entries.size() + VISIBLE_ROWS - 1) / VISIBLE_ROWS);
        this.page = Math.max(0, Math.min(this.page, pages - 1));

        int left = this.width / 2 - 155;
        int top = this.height / 2 - 80;
        int first = this.page * VISIBLE_ROWS;
        int last = Math.min(entries.size(), first + VISIBLE_ROWS);
        for (int i = first; i < last; i++) {
            int index = i;
            int y = top + (i - first) * ROW_HEIGHT;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(entries.get(i)), button -> {
            }).dimensions(left, y, LIST_WIDTH, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), button -> {
                this.module.removeEntry(index);
                this.rebuild();
            }).dimensions(left + LIST_WIDTH + 6, y, DELETE_WIDTH, 20).build());
        }

        int footer = top + VISIBLE_ROWS * ROW_HEIGHT + 6;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add Here"), button -> {
            this.module.addHere();
            this.rebuild();
        }).dimensions(left, footer, 148, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> this.close())
                .dimensions(left + 154, footer, 150, 20).build());

        if (pages > 1) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> {
                this.page--;
                this.rebuild();
            }).dimensions(left + LIST_WIDTH + 6 + DELETE_WIDTH + 6, top, 20, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> {
                this.page++;
                this.rebuild();
            }).dimensions(left + LIST_WIDTH + 6 + DELETE_WIDTH + 6, footer, 20, 20).build());
        }
    }

    private void rebuild() {
        this.clearChildren();
        this.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 100, 0xFFFFFFFF);
        if (this.module.entries().isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("No waypoints yet"),
                    this.width / 2, this.height / 2 - 60, 0xFF9E9E9E);
        } else {
            List<String> entries = this.module.entries();
            int pages = Math.max(1, (entries.size() + VISIBLE_ROWS - 1) / VISIBLE_ROWS);
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("Page " + (this.page + 1) + " / " + pages + "  (" + entries.size() + " saved)"),
                    this.width / 2, this.height / 2 + 80, 0xFF9E9E9E);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
