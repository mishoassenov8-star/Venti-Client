package dev.venti.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class ColorSetting extends Setting<Integer> {
    private final int fallback;
    private int argb;

    public ColorSetting(String name, int argb) {
        super(name);
        this.fallback = argb;
        this.argb = argb;
    }

    public int argb() {
        return this.argb;
    }

    @Override
    public Integer get() {
        return this.argb;
    }

    @Override
    public void set(Integer argb) {
        if (this.argb != argb) {
            this.argb = argb;
            this.changed();
        }
    }

    @Override
    public boolean isDefault() {
        return this.argb == this.fallback;
    }

    @Override
    public void reset() {
        this.set(this.fallback);
    }

    @Override
    JsonElement toJson() {
        return new JsonPrimitive(String.format("#%08X", this.argb));
    }

    @Override
    void fromJson(JsonElement json) {
        this.argb = (int) Long.parseLong(json.getAsString().replace("#", ""), 16);
    }
}
