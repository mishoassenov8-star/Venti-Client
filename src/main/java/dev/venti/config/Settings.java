package dev.venti.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Settings {
    private static final Logger LOGGER = LoggerFactory.getLogger("Venti");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<Setting<?>> all = new ArrayList<>();
    private final Path file;

    public boolean enabled;
    public int bind = -1;
    public float hudX = Float.NaN;
    public float hudY = Float.NaN;
    public boolean hudRight = true;
    public float hudScale = 1.0f;

    public Settings(String id, boolean enabledByDefault) {
        this.file = FabricLoader.getInstance().getConfigDir().resolve("venti").resolve("modules").resolve(id + ".json");
        this.enabled = enabledByDefault;
    }

    public <S extends Setting<?>> S add(String key, S setting) {
        setting.bind(key);
        this.all.add(setting);
        return setting;
    }

    public List<Setting<?>> all() {
        return this.all;
    }

    public void resetAll() {
        for (Setting<?> setting : this.all) {
            setting.reset();
        }
    }

    public void load() {
        if (!Files.isRegularFile(this.file)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(this.file, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("enabled")) {
                this.enabled = root.get("enabled").getAsBoolean();
            }
            if (root.has("bind")) {
                this.bind = root.get("bind").getAsInt();
            }
            if (root.has("hud")) {
                JsonObject hud = root.getAsJsonObject("hud");
                this.hudX = hud.has("x") ? hud.get("x").getAsFloat() : Float.NaN;
                this.hudY = hud.has("y") ? hud.get("y").getAsFloat() : Float.NaN;
                this.hudScale = hud.has("scale") ? hud.get("scale").getAsFloat() : 1.0f;
                this.hudRight = !hud.has("right") || hud.get("right").getAsBoolean();
            }
            JsonObject values = root.has("settings") ? root.getAsJsonObject("settings") : new JsonObject();
            for (Setting<?> setting : this.all) {
                JsonElement value = values.get(setting.key());
                if (value == null) {
                    continue;
                }
                try {
                    setting.fromJson(value);
                } catch (RuntimeException e) {
                    LOGGER.warn("[Venti] Ignoring bad value for {}: {}", setting.key(), value);
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("[Venti] Could not read {}: {}", this.file, e.toString());
        }
    }

    public void save() {
        JsonObject root = new JsonObject();
        root.addProperty("enabled", this.enabled);
        root.addProperty("bind", this.bind);
        JsonObject hud = new JsonObject();
        if (!Float.isNaN(this.hudX)) {
            hud.addProperty("x", this.hudX);
            hud.addProperty("y", this.hudY);
            hud.addProperty("right", this.hudRight);
        }
        hud.addProperty("scale", this.hudScale);
        root.add("hud", hud);
        JsonObject values = new JsonObject();
        for (Setting<?> setting : this.all) {
            values.add(setting.key(), setting.toJson());
        }
        root.add("settings", values);
        try {
            Files.createDirectories(this.file.getParent());
            Files.writeString(this.file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("[Venti] Could not save {}: {}", this.file, e.toString());
        }
    }
}
