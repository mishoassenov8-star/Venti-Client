package dev.venti.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public final class ActionSetting extends Setting<Runnable> {
    private final Runnable action;

    public ActionSetting(String name, Runnable action) {
        super(name);
        this.action = action;
    }

    public void run() {
        this.action.run();
    }

    @Override
    public Runnable get() {
        return this.action;
    }

    @Override
    public void set(Runnable value) {
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public void reset() {
    }

    @Override
    JsonElement toJson() {
        return JsonNull.INSTANCE;
    }

    @Override
    void fromJson(JsonElement json) {
    }
}
