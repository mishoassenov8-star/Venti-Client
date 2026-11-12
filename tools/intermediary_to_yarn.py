#!/usr/bin/env python3
"""Rewrite the intermediary names that appear in the ported Venti sources to Yarn names.

The ported code started life as decompiled bytecode, so it referenced Minecraft through
intermediary names (class_310, method_1551, ...). A Fabric project compiles against Yarn,
so those references are translated once here. Run from the project root:

    python3 tools/intermediary_to_yarn.py src/main/java

The script is idempotent: running it again changes nothing.
"""
import pathlib
import sys

REPLACEMENTS = [
    # classes (fully qualified first, then bare)
    ("net.minecraft.class_310", "net.minecraft.client.MinecraftClient"),
    ("net.minecraft.class_1657", "net.minecraft.entity.player.PlayerEntity"),
    ("net.minecraft.class_1297", "net.minecraft.entity.Entity"),
    ("net.minecraft.class_2561", "net.minecraft.text.Text"),
    ("net.minecraft.class_124", "net.minecraft.util.Formatting"),
    # MinecraftClient / window / hud
    ("method_1551(", "getInstance("),
    ("field_1724", "player"),
    ("field_1705", "inGameHud"),
    ("field_1687", "world"),
    ("field_1755", "currentScreen"),
    ("method_22683(", "getWindow("),
    ("method_4490(", "getHandle("),
    ("method_1569(", "isRunning("),
    # chat / text
    ("method_1743(", "getChatHud("),
    ("method_1812(", "addMessage("),
    ("method_43470(", "literal("),
    ("method_27692(", "formatted("),
    ("method_10852(", "append("),
    ("field_1060", "GREEN"),
    ("field_1061", "RED"),
    ("field_1076", "GOLD"),
    ("field_1068", "GRAY"),
    # entity
    ("method_5805(", "isAlive("),
    ("method_31481(", "isRemoved("),
    ("method_5767(", "isInvisible("),
    ("method_23317(", "getX("),
    ("method_23318(", "getY("),
    ("method_23321(", "getZ("),
    ("method_24515(", "getBlockPos("),
    ("method_10263(", "getX("),
    ("method_10264(", "getY("),
    ("method_10260(", "getZ("),
    ("method_27983(", "getRegistryKey("),
    ("method_29177(", "getValue("),
    ("method_6032(", "getHealth("),
    ("method_6067(", "getMaxHealth("),
    # bare (unqualified) references after the qualified ones above have been handled
    ("class_310", "MinecraftClient"),
    ("class_1657", "PlayerEntity"),
    ("class_1297", "Entity"),
    ("class_2561", "Text"),
    ("class_124", "Formatting"),
]


def rewrite(path: pathlib.Path) -> bool:
    original = path.read_text(encoding="utf-8")
    text = original
    for old, new in REPLACEMENTS:
        text = text.replace(old, new)
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def main() -> int:
    root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "src/main/java")
    changed = [p for p in sorted(root.rglob("*.java")) if rewrite(p)]
    for path in changed:
        print(f"rewrote {path}")
    print(f"{len(changed)} file(s) rewritten")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
