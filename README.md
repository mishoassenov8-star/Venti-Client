# Venti Client — private work repo

Private workspace for Venti Client modules. Nothing in this repository is published.

This is a **buildable Fabric mod**: the module/settings framework recovered from the shipped
client, modules written for Venti, and three mixins.

## Build

Requires a JDK 21 or newer. Gradle itself needs **Java 25** (Fabric Loom 1.19.4 refuses
to run on anything older); the mod still compiles and runs at Java 21.

```bash
# one-time: generate the wrapper (the binary wrapper jar is not committed)
gradle wrapper --gradle-version 9.9.1

JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

Or skip the wrapper and run `gradle build` with a local Gradle 9.9.1.

Output: `build/libs/venti-client-0.6.0.jar` (plus a sources jar).

If your Gradle JVM is JDK 25 but the toolchain JDK 21 is installed somewhere unusual:

```bash
./gradlew build -Porg.gradle.java.installations.paths=/path/to/jdk-21
```

## Versions

| | |
| --- | --- |
| Minecraft | 1.21.11 |
| Yarn mappings | 1.21.11+build.6 |
| Fabric Loader | 0.16.14 |
| Fabric API | 0.141.6+1.21.11 |
| Loom | 1.19.4 |

These match the shipped `venti.jar` (`1.0.0+mc1.21.11`).

## Modules

Written for Venti:

| Module | Category | What it does |
| --- | --- | --- |
| Name Tags | Render | Vanilla-style tags for nearby players, gated through the shipped `EntityRenderer#hasLabel` mixin, so a tag is drawn only by the depth-tested vanilla pass — never through walls |
| Copy Coords | Misc | Clipboard dump of your position, then switches itself off |
| Low Health | Misc | Chat warning with a cooldown |

Other modules:

| Module | Category | What it does |
| --- | --- | --- |
| Weather Notifier | Misc | Chat warning when it starts or stops raining, with a 20-tick debounce so a flickering server does not spam you, plus an optional chime |
| Sprint | Misc | Keeps you sprinting while you move forward — only while moving forward, not using an item, not sneaking. Same behaviour as vanilla Toggle Sprint |
| Coord Snapper | Misc | Copies the block you look at, or the one you stand on, to the clipboard. Looked At / Own Position targets, 16-512 range, Stop at Fluids, three formats and a dimension suffix. **Never uploads anything** |
| Fullbright | Render | Gamma setting 1-16, default 8, with a 1.0 floor — it brightens, never dims. Raises the gamma argument inside the lightmap update with core Mixin's `@ModifyArg` |
| Time Changer | Render | -20000..20000 client-only override via `@Redirect` on `Properties#setTimeOfDay`. The server clock and other players are untouched; disabling writes the last server time back |

## Mixins

One config, three mixins, `defaultRequire: 1` so a target that stops matching fails loudly:

| Mixin | Target |
| --- | --- |
| `NametagRendererMixin` | `EntityRenderer#hasLabel` — cancels labels for players that fail the Name Tags rule |
| `FullbrightLightmapMixin` | `LightmapTextureManager#update` — raises the gamma argument |
| `TimeChangerClientWorldMixin` | `ClientWorld#setTime` — substitutes the forced time-of-day |

Loom remaps the annotations during `remapJar`, so **no refmap is needed**. Verified in the
built jar: `class_897/method_3921`, `class_765/method_3313` and
`class_638/method_29089` targeting `class_638$class_5271.method_165`. `docs/wiring.md` shows
the `javap` command to re-check and the recipe for adding another mixin.

## Layout

| Path | What it is |
| --- | --- |
| `src/main/java/dev/venti/VentiMod.java` | Client entrypoint: registers every module, starts/stops them with the client, ticks them |
| `src/main/java/dev/venti/module/` | `Module`, `ModuleCategory`, `Modules` — the framework, reconstructed from the jar |
| `src/main/java/dev/venti/config/` | All nine setting types plus `Settings` persistence |
| `src/main/java/dev/venti/mixin/` | The three mixins |
| `src/main/java/dev/venti/modules/` | `nametags`, `coords`, `health`, `weather`, `movement`, `render` |
| `docs/wiring.md` | Mixin setup, the nametag gate, how to add a mixin |
| `docs/recovered-api.md` | The framework API as it exists in the shipped jar |
| `tools/intermediary_to_yarn.py` | The one-shot renamer used to convert the decompiled intermediary names to Yarn |

## Provenance

(decompiled with CFR 0.152; 263 classes → 133 source files). The decompiled tree is not
committed. Decompiled code references Minecraft through intermediary names (`class_310`,
`method_1551`), while a Fabric project compiles against Yarn — `tools/intermediary_to_yarn.py`
translates the exact set of members this code touches, and is safe to run again.

## Status

`build` succeeds: `compileJava`, `jar` and `remapJar` all pass, and all three mixins are
remapped into the jar. None of it has been run in a Minecraft client yet.

## Deliberately not included

Target ESP, auto eat, legit auto tool, goliath RTP, chat spam and chat macros are not ported
and will not be added here. Combat/aura modules, ESP/Xray and flight are also out.

Auto Reconnect was ported in 0.3.0 and removed again at the owner's request; it is not part
of the mod.

Movement automation is out as well:
Auto Clicker, Fast Use, No Jump Delay, Fly, Speed and similar are not ported. They play the
game for you rather than showing you something about it.

Toggle-sprint style modules stay. Sprint holds the sprint state while you move forward, which
is a plain convenience setting rather than automation that plays the game for you.

This repo does not post your coordinates anywhere. Coord Snapper copies to the clipboard and
stops there: a keypress that silently sends your position off the machine is not something it
will do.