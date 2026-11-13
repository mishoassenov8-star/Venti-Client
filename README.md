# Venti Client — private work repo

Private workspace for Venti Client modules. Nothing in this repository is published.

This is a **buildable Fabric mod**: the module/settings framework recovered from the shipped
client, plus modules written for Venti.

## Build

Requires a JDK 21 or newer. Gradle itself needs **Java 25** (Fabric Loom 1.19.4 refuses
to run on anything older); the mod still compiles and runs at Java 21.

```bash
# one-time: generate the wrapper (the binary wrapper jar is not committed)
gradle wrapper --gradle-version 9.9.1

JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

Or skip the wrapper and run `gradle build` with a local Gradle 9.9.1.

Output: `build/libs/venti-client-0.3.0.jar` (plus a sources jar).

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
| Name Tags | Render | Vanilla-style tags for nearby players, drawn only by the depth-tested vanilla label pass — never through walls |
| Copy Coords | Misc | Clipboard dump of your position, then switches itself off |
| Low Health | Misc | Chat warning with a cooldown |

Other modules:

| Module | Category | What it does |
| --- | --- | --- |
| Weather Notifier | Misc | Chat warning when it starts or stops raining, with a 20-tick debounce so a flickering server does not spam you, plus an optional chime |
| Sprint | Misc | Keeps you sprinting while you move forward — only while moving forward, not using an item, not sneaking. Same behaviour as vanilla Toggle Sprint |
| Auto Reconnect | Misc | Same countdown button on the disconnect screen and the same ban/blacklist guard. Clears the saved server on the title screen so a deliberate quit is not undone |

## Layout

| Path | What it is |
| --- | --- |
| `src/main/java/dev/venti/VentiMod.java` | Client entrypoint: registers every module, starts/stops them with the client, ticks them |
| `src/main/java/dev/venti/module/` | `Module`, `ModuleCategory`, `Modules` — the framework, reconstructed from the jar |
| `src/main/java/dev/venti/config/` | All nine setting types plus `Settings` persistence |
| `src/main/java/dev/venti/modules/` | `nametags`, `coords`, `health`, `weather`, `movement`, `reconnect` |
| `docs/wiring.md` | How the nametag hook goes into your own renderer |
| `docs/recovered-api.md` | The framework API as it exists in the shipped jar |
| `tools/intermediary_to_yarn.py` | The one-shot renamer used to convert the decompiled intermediary names to Yarn |

## Provenance

(decompiled with CFR 0.152; 263 classes → 133 source files). The decompiled tree is not
committed. Decompiled code references Minecraft through intermediary names (`class_310`,
`method_1551`), while a Fabric project compiles against Yarn — `tools/intermediary_to_yarn.py`
translates the exact set of members this code touches, and is safe to run again.

## Status

`build` succeeds: `compileJava`, `jar` and `remapJar` all pass. None of it has been run in a
Minecraft client yet.

## Deliberately not included

Target ESP, auto eat, legit auto tool, goliath RTP, chat spam and chat macros are not ported
and will not be added here. Combat/aura modules, ESP/Xray and flight are also out.
