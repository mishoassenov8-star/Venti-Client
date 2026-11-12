package dev.venti.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class StringSetting extends Setting<String> {
    private final String fallback;
    private String value;
    private String[] fileExtensions;

    public StringSetting(String name, String value) {
        super(name);
        this.fallback = value;
        this.value = value;
    }

    public StringSetting files(String... extensions) {
        this.fileExtensions = extensions;
        return this;
    }

    public boolean isFile() {
        return this.fileExtensions != null;
    }

    public String[] fileExtensions() {
        return this.fileExtensions;
    }

    @Override
    public String get() {
        return this.value;
    }

    @Override
    public void set(String value) {
        String next = value == null ? "" : value;
        if (!this.value.equals(next)) {
            this.value = next;
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
        this.value = json.getAsString();
    }
}
