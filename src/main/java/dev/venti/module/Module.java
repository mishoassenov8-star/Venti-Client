package dev.venti.module;

import dev.venti.config.BooleanSetting;
import dev.venti.config.Setting;
import dev.venti.config.Settings;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Module {
    protected static final Logger LOGGER = LoggerFactory.getLogger("Venti");

    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final Settings settings;
    private final BooleanSetting notifications;
    private boolean hold;
    private boolean running;

    protected Module(String id, String name, String description, ModuleCategory category) {
        this(id, name, description, category, false);
    }

    protected Module(String id, String name, String description, ModuleCategory category, boolean enabledByDefault) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.settings = new Settings(id, enabledByDefault);
        this.notifications = this.settings.add("notifications", new BooleanSetting("Notifications", true));
    }

    protected final <S extends Setting<?>> S add(String key, S setting) {
        return this.settings.add(key, setting);
    }

    public final String id() {
        return this.id;
    }

    public final String name() {
        return this.name;
    }

    public final String description() {
        return this.description;
    }

    public final ModuleCategory category() {
        return this.category;
    }

    public final Settings settings() {
        return this.settings;
    }

    public final boolean isEnabled() {
        return this.settings.enabled;
    }

    /** A hold module is on only while its bind is held; it is never saved or announced. */
    public final boolean hold() {
        return this.hold;
    }

    protected final void setHold(boolean hold) {
        this.hold = hold;
    }

    final void setHeld(boolean held) {
        if (this.settings.enabled == held) {
            return;
        }
        this.settings.enabled = held;
        this.sync();
    }

    public final void setEnabled(boolean enabled) {
        if (this.hold) {
            return;
        }
        if (this.settings.enabled == enabled) {
            return;
        }
        this.settings.enabled = enabled;
        this.settings.save();
        this.sync();
        if (this.notifications.get()) {
            this.chat(Text.literal(enabled ? "enabled" : "disabled")
                    .formatted(enabled ? Formatting.GREEN : Formatting.RED));
        }
    }

    public final void toggle() {
        this.setEnabled(!this.settings.enabled);
    }

    final void sync() {
        if (this.settings.enabled && !this.running) {
            this.running = true;
            this.safely("enable", this::onEnable);
        } else if (!this.settings.enabled && this.running) {
            this.running = false;
            this.safely("disable", this::onDisable);
        }
    }

    final void shutdown() {
        if (this.running) {
            this.running = false;
            this.safely("shut down", this::onDisable);
        }
    }

    final void tick() {
        if (this.running) {
            this.safely("tick", this::onTick);
        }
    }

    private void safely(String what, Runnable step) {
        try {
            step.run();
        } catch (LinkageError | RuntimeException e) {
            LOGGER.error("[Venti] {} failed to {}", this.name, what, e);
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    protected void onTick() {
    }

    protected final void chat(Text message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (this.notifications.get() && mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(
                    Text.literal("[Venti] ")
                            .formatted(Formatting.GOLD)
                            .append(Text.literal(this.name + " ").formatted(Formatting.GRAY))
                            .append(message));
        }
    }
}
