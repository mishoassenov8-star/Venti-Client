# Venti Client — private work repo

Private workspace for Venti Client modules. Nothing in this repository is published.

## What is here

| Path | What it is |
| --- | --- |
| `src/main/java/dev/venti/modules/nametags/` | Name Tags — vanilla depth-tested label pass only, never through walls |
| `src/main/java/dev/venti/modules/coords/` | Copy Coords — clipboard dump of your position |
| `src/main/java/dev/venti/modules/health/` | Low Health — chat warning with cooldown |
| `docs/wiring.md` | Where each file goes and the registration edits |
| `docs/recovered-api.md` | The module/settings API recovered from the shipped jar |

## Provenance

These files were written against the API recovered by decompiling the shipped
`venti.jar` (CFR 0.152, `dev.venti.*`, 263 classes → 133 source files). The recovered
reference tree is **not** committed here; it stays local. Module ids, category values,
setting keys and hook names below match the real classes:

* `dev.venti.module.Module` — `(id, name, description, category[, enabledByDefault])`,
  `add(key, setting)`, `onEnable/onDisable/onTick`, `chat(Text)`, `isEnabled()`, `setEnabled(boolean)`
* `dev.venti.module.ModuleCategory` — `RENDER`, `HUD`, `MISC`
* `dev.venti.module.Modules` — `register(module)`, `get(Class)`, `all()`
* `dev.venti.config` — `Settings`, `BooleanSetting`, `NumberSetting`, `ModeSetting`,
  `ColorSetting`, `StringSetting`, `StringListSetting`, `ActionSetting`
* Per-module config: `<config>/venti/modules/<id>.json`

## Status

Written, not compiled. There is no buildable Gradle/Loom project in this repo yet, so
none of these files have been through a compiler. Expect to fix a signature or two on
the first real build, most likely around `method_*` names if the target Minecraft
version differs from 1.21.11.

## Deliberately not included

Target ESP, auto eat, legit auto tool, goliath RTP, chat spam and chat macros are not
ported and will not be added here.
