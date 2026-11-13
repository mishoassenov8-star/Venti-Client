# Mixins and wiring

## Mixin infrastructure

One config, `src/main/resources/venti.mixins.json`, registered from `fabric.mod.json`:

```json
"mixins": ["venti.mixins.json"]
```

```json
{
  "required": true,
  "package": "dev.venti.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [],
  "client": [
    "NametagRendererMixin",
    "FullbrightLightmapMixin",
    "TimeChangerClientWorldMixin"
  ],
  "injectors": { "defaultRequire": 1 }
}
```

Loom handles the annotation processor and the remap. **No refmap is needed**: during
`remapJar`, Loom rewrites the annotations to intermediary names. Verified in the built jar:

| Mixin | Remapped target |
| --- | --- |
| `NametagRendererMixin` | `class_897`, method `method_3921` |
| `FullbrightLightmapMixin` | `class_765`, method `method_3313` |
| `TimeChangerClientWorldMixin` | `class_638`, method `method_29089`, redirecting `class_638$class_5271.method_165(J)V` |

Check it yourself with:

```bash
javap -v -p -classpath build/libs/venti-client-0.6.0.jar dev.venti.mixin.FullbrightLightmapMixin \
  | grep -E 'method=\[|value=\[|target=\['
```

To add a mixin: drop it in `dev.venti.mixin`, list it under `client` in `venti.mixins.json`,
keep `defaultRequire: 1` so a target that stops matching fails loudly, and build.

**Core Mixin only.** Fullbright and Time Changer are wired with core Mixin (`@ModifyArg`,
`@Redirect`) rather than MixinExtras, so no extra dependency is needed and the annotation
processor stays the default one.

## Name tag gate

`NametagRendererMixin` can only subtract tags:

```java
@Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
private void venti$gateNameTags(Entity entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
    if (NameTagsModule.shouldHide(entity)) {
        cir.setReturnValue(false);
    }
}
```

`hasLabel` is vanilla's own "does this entity get a name tag" check, and the pass that follows
it is depth tested. Cancelling there means a tag is drawn exactly as vanilla draws it — hidden
behind a wall included — while the module can only take a tag away. There is deliberately no
hook on the render call itself: that is how a client ends up with a see-through layer.

## Fullbright

```java
@ModifyArg(method = "update",
        at = @At(value = "INVOKE",
                 target = "Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",
                 ordinal = 6),
        index = 0)
```

`ordinal = 6` is the gamma slot in `LightmapTextureManager#update` for Minecraft 1.21.11. If a
future Minecraft reorders those floats the method stops matching and the mixin fails loudly
rather than brightening some other value.

## Time Changer

```java
@Redirect(method = "setTime",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/world/ClientWorld$Properties;setTimeOfDay(J)V"))
```

The client keeps receiving normal time updates; only the value written into its own world
properties is substituted. The server clock, the scoreboard and what other players see are
untouched. Disabling writes the last server time back.

## Registering modules

Modules are created once in `dev.venti.VentiMod#onInitializeClient`:

```java
Modules.register(new FullbrightModule());
```

Registration also loads that module's config from `<config>/venti/modules/<id>.json`.

## Building

```bash
gradle wrapper --gradle-version 9.9.1
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

Gradle needs Java 25 because Loom 1.19.4 requires it; the mod targets Java 21 and Minecraft
1.21.11 with Yarn `1.21.11+build.6`.

If you pull these files into your own client instead, keep your own mappings. The recovered
code was written against intermediary names and converted with `tools/intermediary_to_yarn.py`;
the translated members are listed in that script, so you can invert or extend the mapping there
rather than editing by hand.
