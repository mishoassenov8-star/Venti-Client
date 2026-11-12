package dev.venti.module;

public enum ModuleCategory {
    RENDER("Render"),
    HUD("HUD"),
    MISC("Misc");

    public final String label;

    ModuleCategory(String label) {
        this.label = label;
    }
}
