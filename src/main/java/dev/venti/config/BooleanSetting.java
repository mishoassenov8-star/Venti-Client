package dev.venti.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BooleanSetting extends Setting<Boolean> {
    private final boolean fallback;
    private boolean value;

    public BooleanSetting(String name, boolean value) {
        super(name);
        this.fallback = value;
        this.value = value;
    }

    @Override
    public Boolean get() {
        return this.value;
    }

    @Override
    public void set(Boolean value) {
        if (this.value != value) {
            this.value = value;
            this.changed();
        }
    }

    @Override
    public boolean isDefault() {
        return this.value == this.fallback;
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
        this.value = json.getAsBoolean();
    }
}
