# Mixins and wiring

## Mixin infrastructure

The mod ships one mixin config, `src/main/resources/venti.mixins.json`, registered from
`fabric.mod.json`:

```json
"mixins": ["venti.mixins.json"]
```

```json
{
  "required": true,
  "package": "dev.venti.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [],
  "client": ["NametagRendererMixin"],
  "injectors": { "defaultRequire": 1 }
}
```

Loom handles the annotation processor and the remap. **No refmap is needed**: in the remapped
jar the `@Mixin` target becomes `class_897` and `method = "hasLabel"` becomes `method_3921`,
i.e. Loom rewrites the annotation to intermediary names during `remapJar`. You can confirm it
yourself:

```bash
javap -v -p -classpath build/libs/venti-client-0.5.0.jar dev.venti.mixin.NametagRendererMixin \
  | grep -E 'method=\[|value=\['
```

To add a mixin, drop it in `dev.venti.mixin`, list it under `client` in `venti.mixins.json`,
and keep `defaultRequire: 1` so a target that stops matching fails loudly instead of silently
doing nothing.

## Name tag hook

`dev.venti.mixin.NametagRendererMixin` is the mod's only rendering hook, and it can only
subtract tags:

```java
@Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
private void venti$gateNameTags(Entity entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
    if (NameTagsModule.shouldHide(entity)) {
        cir.setReturnValue(false);
    }
}
```

`hasLabel(T, double)` is vanilla's own "does this entity get a name tag" check, and the pass
that follows it is depth tested. Cancelling there means a tag is drawn exactly as vanilla draws
it — including being hidden behind a wall — while `NameTagsModule` can take a tag away (out of
range, self, invisible, removed). Non-players and a disabled module return `false`, so vanilla
behaviour is untouched.

There is deliberately **no** hook on the render call itself. Adding one is how a client ends up
with a `RenderLayer.getTextSeeThrough(...)` variant that draws tags through terrain, which is
the exact thing this module exists to avoid.

## Registering modules

Modules are created once in `dev.venti.VentiMod#onInitializeClient`:

```java
Modules.register(new NameTagsModule());
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
