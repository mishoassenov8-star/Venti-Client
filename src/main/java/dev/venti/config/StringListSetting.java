package dev.venti.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;

public final class StringListSetting extends Setting<List<String>> {
    private final List<String> fallback;
    private final List<String> value;

    public StringListSetting(String name, String... values) {
        super(name);
        this.fallback = List.of(values);
        this.value = new ArrayList<>(this.fallback);
    }

    @Override
    public List<String> get() {
        return this.value;
    }

    @Override
    public void set(List<String> value) {
        this.value.clear();
        this.value.addAll(value);
        this.changed();
    }

    public void edited() {
        this.changed();
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
        JsonArray array = new JsonArray();
        this.value.forEach(array::add);
        return array;
    }

    @Override
    void fromJson(JsonElement json) {
        this.value.clear();
        json.getAsJsonArray().forEach(line -> this.value.add(line.getAsString()));
    }
}
