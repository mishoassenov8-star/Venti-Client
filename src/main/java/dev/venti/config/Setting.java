package dev.venti.config;

import com.google.gson.JsonElement;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private final String name;
    private String key;
    private BooleanSupplier visibility = () -> true;
    private Runnable onChange;

    protected Setting(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public String key() {
        return this.key;
    }

    void bind(String key) {
        this.key = key;
    }

    public boolean visible() {
        return this.visibility.getAsBoolean();
    }

    public Setting<T> visibleWhen(BooleanSupplier condition) {
        this.visibility = condition;
        return this;
    }

    public Setting<T> onChange(Runnable listener) {
        this.onChange = listener;
        return this;
    }

    protected void changed() {
        if (this.onChange != null) {
            this.onChange.run();
        }
    }

    public abstract T get();

    public abstract void set(T value);

    public abstract boolean isDefault();

    public abstract void reset();

    abstract JsonElement toJson();

    abstract void fromJson(JsonElement json);
}
