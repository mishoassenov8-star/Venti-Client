# Recovered Venti module API

Recovered by decompiling the shipped `venti.jar` (mod id `venti`, version
`1.0.0+mc1.21.11`, entrypoint `dev.venti.VentiClient`, 263 classes, CFR 0.152).
The `class_NNN` / `method_NNNN` names are Minecraft intermediaries; everything else is
Venti's own code.

## `dev.venti.module.Module`

```java
protected Module(String id, String name, String description, ModuleCategory category)
protected Module(String id, String name, String description, ModuleCategory category, boolean enabledByDefault)

protected final <S extends Setting<?>> S add(String key, S setting)

protected void onEnable()   // called when the module flips on
protected void onDisable()  // called when it flips off, and on client shutdown
protected void onTick()     // called every client tick while enabled

public final String id(); name(); description(); ModuleCategory category(); Settings settings();
public final boolean isEnabled();
public final void setEnabled(boolean enabled);   // saves config, fires hooks, chats if notifications on
public final void toggle();
protected final void chat(Text message);         // "[Venti] <name> ..." in chat
protected static final Logger LOGGER;
```

Every module gets an automatic `notifications` boolean setting (default `true`).

## `dev.venti.module.ModuleCategory`

`RENDER("Render")`, `HUD("HUD")`, `MISC("Misc")`.

## `dev.venti.module.Modules`

```java
public static <M extends Module> M register(M module);  // also loads its config
public static List<Module> all();
public static <M extends Module> M get(Class<M> type);  // null when absent
public static void startAll(); stopAll();
public static void tick(MinecraftClient client);        // handles binds, then onTick
```

## `dev.venti.config`

`Setting<T>` base: `name()`, `key()`, `get()`, `set(T)`, `isDefault()`, `reset()`,
`visibleWhen(BooleanSupplier)`, `onChange(Runnable)`. Concrete types:

| Class | Constructor | Notes |
| --- | --- | --- |
| `BooleanSetting` | `(name, boolean)` | |
| `NumberSetting` | `(name, value, min, max)` | `.step(float)` snaps values |
| `ModeSetting` | `(name, value, String... modes)` | `.is("Mode")`, `.setModes(List)` |
| `ColorSetting` | `(name, argb)` | `.argb()` |
| `StringSetting` | `(name, value)` | `.files(ext...)` marks it a file picker |
| `StringListSetting` | `(name, String... values)` | |
| `ActionSetting` | `(name, Runnable)` | `.run()` for buttons |

`Settings` (one per module) holds `enabled`, `bind` (GLFW key), HUD position
(`hudX`, `hudY`, `hudRight`, `hudScale`) and persists to
`<config>/venti/modules/<id>.json` via `load()` / `save()`.

## Existing modules in the jar

`custom-sky`, `hitfx`, `character`, `spotify-hud`, `discord`, `custom-totem`.
There is no nametag, coords or health module in the shipped jar — the modules in this
repo are new.
