package dev.venti.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Arrays;
import java.util.List;

public final class ModeSetting extends Setting<String> {
    private final String fallback;
    private String[] modes;
    private String value;

    public ModeSetting(String name, String value, String... modes) {
        super(name);
        this.fallback = value;
        this.value = value;
        this.modes = modes;
    }

    public String[] modes() {
        return this.modes;
    }

    public void setModes(List<String> modes) {
        this.modes = modes.toArray(new String[0]);
        if (!modes.contains(this.value)) {
            this.value = modes.contains(this.fallback) || modes.isEmpty() ? this.fallback : modes.get(0);
            this.changed();
        }
    }

    public boolean is(String mode) {
        return this.value.equals(mode);
    }

    @Override
    public String get() {
        return this.value;
    }

    @Override
    public void set(String value) {
        if (Arrays.asList(this.modes).contains(value) && !this.value.equals(value)) {
            this.value = value;
            this.changed();
        }
    }

    @Override
    public boolean isDefault() {
        return this.value.equals(this.fallback);
    }

    @Override
    public void reset() {
        this.set(this.fallback);
    }

    @Override
    JsonElement toJson() {
        return new JsonPrimitive(this.value);
    }

    @Override
    void fromJson(JsonElement json) {
        String read = json.getAsString();
        if (Arrays.asList(this.modes).contains(read)) {
            this.value = read;
        }
    }
}
