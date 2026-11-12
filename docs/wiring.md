# Wiring these modules into Venti

## 1. Drop the files in

Copy the three package folders from `src/main/java/dev/venti/modules/` into your
project's `src/main/java/dev/venti/modules/`:

```
dev/venti/modules/nametags/NameTagsModule.java
dev/venti/modules/coords/CopyCoordsModule.java
dev/venti/modules/health/LowHealthModule.java
```

They need no new dependencies: `Module`, `ModuleCategory`, `Modules`, `BooleanSetting`,
`NumberSetting` and `ColorSetting` all already exist in the client.

## 2. Register them

In `dev.venti.VentiClient#onInitializeClient`, next to the existing registrations:

```java
Modules.register(new NameTagsModule());
Modules.register(new CopyCoordsModule());
Modules.register(new LowHealthModule());
```

Copy Coords and Low Health need nothing else. They run off `onEnable` / `onTick`, which
the framework already calls, and their bind is set from the GUI.

## 3. Hook Name Tags into the label pass

`NameTagsModule` only decides *whether* a tag may be drawn. The actual drawing stays in
vanilla's entity-label renderer, which is depth tested, so a tag behind a wall is
occluded by the wall. That is the whole "not through walls" guarantee: there is no
second, depth-disabled pass anywhere in the module.

In your `dev/venti/mixin/character/MixinCharacterLivingEntityRenderer` (or a fresh mixin
on `LivingEntityRenderer`), gate the label call:

```java
if (entity instanceof PlayerEntity && !NameTagsModule.shouldRender(entity)) {
    ci.cancel();          // skip the label for this entity this frame
}
```

and apply `scale()`, `health()`, `distance()`, `background()`, `nameColor()` from the
module when you build the label text and matrix. `NameTagsModule.active()` returns
`null` when the module is off; `shouldRender` already handles that.

Do **not** wrap that call in a `RenderLayer.getTextSeeThrough(...)` / no-depth layer to
"fix" tags disappearing behind blocks. They are supposed to disappear behind blocks;
that is the requested behaviour.

## 4. First build

Nothing here has been compiled. Signatures follow the shipped `venti.jar` for
Minecraft 1.21.11, so the likeliest first-build fixes are:

* `class_1657` / `method_6032` / `method_6067` names if you build against a different
  Minecraft version than the jar was compiled for.
* `CopyCoordsModule` uses `class_1657#method_24515()` for block coordinates; if your
  mappings differ, fall back to `Math.floor` on the doubles.
