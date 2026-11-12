# Venti Client — private work repo

Private workspace for Venti Client modules. Nothing in this repository is published.

This is a **buildable Fabric mod** containing three modules plus the module/settings
framework they depend on.

## Build

Requires a JDK 21 or newer. Gradle itself needs **Java 25** (Fabric Loom 1.19.4 refuses
to run on anything older); the mod still compiles and runs at Java 21.

```bash
# one-time: generate the wrapper (the binary wrapper jar is not committed)
gradle wrapper --gradle-version 9.9.1

JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

Or skip the wrapper and run `gradle build` with a local Gradle 9.9.1.

Output: `build/libs/venti-client-0.2.0.jar` (plus a sources jar).

If your Gradle JVM is JDK 25 but the toolchain JDK 21 is installed somewhere unusual,
point Gradle at it:

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

These match the shipped `venti.jar` (`1.0.0+mc1.21.11`), so the ported framework behaves
the same as the real one.

## What is in here

| Path | What it is |
| --- | --- |
| `src/main/java/dev/venti/VentiMod.java` | Client entrypoint: registers the three modules, starts/stops them with the client, ticks them |
| `src/main/java/dev/venti/module/` | `Module`, `ModuleCategory`, `Modules` — the framework, reconstructed from the jar |
| `src/main/java/dev/venti/config/` | All nine setting types plus `Settings` persistence |
| `src/main/java/dev/venti/modules/nametags/` | Name Tags — vanilla depth-tested label pass only, never through walls |
| `src/main/java/dev/venti/modules/coords/` | Copy Coords — clipboard dump of your position |
| `src/main/java/dev/venti/modules/health/` | Low Health — chat warning with a cooldown |
| `docs/wiring.md` | How the nametag hook goes into your own renderer |
| `docs/recovered-api.md` | The framework API as it exists in the shipped jar |
| `tools/intermediary_to_yarn.py` | The one-shot renamer used to convert the decompiled intermediary names to Yarn |

## Provenance

The framework and settings classes were reconstructed from the shipped `venti.jar`
(decompiled with CFR 0.152; 263 classes → 133 source files). The decompiled tree itself
is **not** committed. Decompiled code references Minecraft through intermediary names
(`class_310`, `method_1551`), while a Fabric project compiles against Yarn — so
`tools/intermediary_to_yarn.py` translates the exact set of members this code touches.
Running it again is safe and idempotent.

## Status

`build` succeeds: `compileJava`, `jar` and `remapJar` all pass, and the jar contains
`dev/venti/**` plus a `fabric.mod.json` with the `dev.venti.VentiMod` entrypoint. It has
**not** been run in a Minecraft client: that needs a launcher, a world and a second
player to look at.

## Deliberately not included

Target ESP, auto eat, legit auto tool, goliath RTP, chat spam and chat macros are not
ported and will not be added here.
