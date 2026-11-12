package dev.venti.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class NumberSetting extends Setting<Float> {
    private final float fallback;
    private final float min;
    private final float max;
    private float step;
    private float value;

    public NumberSetting(String name, float value, float min, float max) {
        super(name);
        this.fallback = value;
        this.value = value;
        this.min = min;
        this.max = max;
    }

    public NumberSetting step(float step) {
        this.step = step;
        return this;
    }

    public float min() {
        return this.min;
    }

    public float max() {
        return this.max;
    }

    public float stepSize() {
        return this.step;
    }

    @Override
    public Float get() {
        return this.value;
    }

    @Override
    public void set(Float value) {
        float next = Math.max(this.min, Math.min(this.max, value));
        if (this.step > 0.0f) {
            next = this.min + Math.round((next - this.min) / this.step) * this.step;
            next = Math.max(this.min, Math.min(this.max, next));
        }
        if (next != this.value) {
            this.value = next;
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
        this.value = Math.max(this.min, Math.min(this.max, json.getAsFloat()));
    }
}
