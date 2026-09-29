#!/usr/bin/env python3
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources/data/spellbladenext"
SPELL_DIR = RES / "spell"
TAG_DIR = RES / "tags/spell"
ASSIGN_DIR = RES / "spell_assignments"

errors = []
warnings = []


def load_json(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        errors.append(f"Invalid JSON {path.relative_to(ROOT)}: {exc}")
        return None


spells = {}
for path in sorted(SPELL_DIR.glob("*.json")):
    data = load_json(path)
    if data is not None:
        spells[f"spellbladenext:{path.stem}"] = data

for path in sorted(TAG_DIR.glob("*.json")):
    data = load_json(path)
    if not isinstance(data, dict):
        continue
    for spell_id in data.get("values", []):
        if isinstance(spell_id, str) and spell_id.startswith("spellbladenext:") and spell_id not in spells:
            errors.append(f"Tag {path.name} references missing spell {spell_id}")

for path in sorted(ASSIGN_DIR.glob("*.json")):
    data = load_json(path)
    if not isinstance(data, dict):
        continue
    container = data.get("spell_container")
    if not isinstance(container, dict):
        errors.append(f"Assignment {path.name} is missing spell_container wrapper")
        continue
    if container.get("access") not in {"MAGIC", "ARCHERY", "CONTAINED", "ANY", "TAG", "NONE"}:
        errors.append(f"Assignment {path.name} has invalid access={container.get('access')!r}")
    for spell_id in container.get("spell_ids", []):
        if isinstance(spell_id, str) and spell_id.startswith("spellbladenext:") and spell_id not in spells:
            errors.append(f"Assignment {path.name} references missing spell {spell_id}")

custom = []
legacy_mana = []
for spell_id, data in spells.items():
    delivery = data.get("deliver") or {}
    if delivery.get("type") == "CUSTOM":
        custom.append(spell_id)
    cost = data.get("cost") or {}
    if "legacy_rpgmana" in cost:
        legacy_mana.append(spell_id)

mixin_cfg = load_json(ROOT / "src/main/resources/smooth_spellblade_compat.mixins.json")
if isinstance(mixin_cfg, dict):
    package = mixin_cfg.get("package", "")
    base = ROOT / "src/main/java" / Path(*package.split("."))
    for name in mixin_cfg.get("mixins", []):
        expected = base / f"{name}.java"
        if not expected.exists():
            errors.append(f"Mixin source missing: {expected.relative_to(ROOT)}")

fabric = load_json(ROOT / "src/main/resources/fabric.mod.json")
if isinstance(fabric, dict) and fabric.get("id") != "smooth_spellblade_compat":
    errors.append("fabric.mod.json has unexpected mod id")

print(f"spells={len(spells)}")
print(f"spell_tags={len(list(TAG_DIR.glob('*.json')))}")
print(f"assignments={len(list(ASSIGN_DIR.glob('*.json')))}")
print(f"custom_deliveries={len(custom)}")
print(f"legacy_rpgmana={len(legacy_mana)}")
print(f"errors={len(errors)}")
print(f"warnings={len(warnings)}")
for message in errors:
    print("ERROR:", message)
for message in warnings:
    print("WARN:", message)

raise SystemExit(1 if errors else 0)
