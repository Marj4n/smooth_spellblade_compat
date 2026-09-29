# Compatibility audit — Spellblades 2.4.0 -> Spell Engine 1.10.7

## Data migration

- 80 migrated spell definitions in `data/spellbladenext/spell/`
- 10 V1 spell pools converted to `data/spellbladenext/tags/spell/`
- 16 V1 spell assignments converted to the modern wrapped `spell_container` format
- Proxy assignment semantics preserve V1's default MAGIC content filtering; non-proxy assignments map to `CONTAINED`

## Critical runtime bridges

- V1 global `SpellRegistry.getSpell` -> modern world dynamic registry, with pre-world bootstrap from bundled migrated JSON
- V1 `SpellContainerHelper.getEquipped` -> modern `SpellContainerSource.activeContainerOf(player)`; this matches the historical V1 implementation that returned the merged player container
- V1 cooldown manager Identifier API -> modern RegistryEntry API
- V1 `CustomSpellHandler.register` -> modern custom delivery registry
- V1 `WorldScheduler` -> modern scheduler interface
- old release animation/sound descriptor reads -> compatibility accessors
- old SpellProjectile constructor -> modern projectile factory
- old 4-argument SpellWeaponItem constructor -> modern 2-argument constructor bytecode adaptation
- old required `AnimationHelper` mixin target provided before Spellblades' default-priority mixins

## Spell data observations

School distribution:

- frost: 17
- fire: 16
- arcane: 15
- healing: 14
- physical melee: 10
- lightning: 6
- armor: 2

Delivery migration:

- CUSTOM: 39
- direct/default: 28
- PROJECTILE: 10
- METEOR: 2
- CLOUD: 1

Legacy custom-delivery IDs that do not have an obvious top-level `CustomSpellHandler.register(...)` registration in the audited Spellblades source/JAR behavior and therefore need runtime verification:

- `spellbladenext:box`
- `spellbladenext:gem_barrage`
- `spellbladenext:gem_barrage2`
- `spellbladenext:grandstanding`
- `spellbladenext:lightningsmite`
- `spellbladenext:lightningstep`
- `spellbladenext:spellstrike2`
- `spellbladenext:thesis`
- `spellbladenext:xslash`

`spellbladenext:combustion` has an old Java custom-handler registration although its legacy JSON does not set the same custom-impact flag as the main custom-delivery group; it also needs gameplay verification.

## Build verification

The project is configured for Loom 1.17.21 / Gradle 9.6.1 and Java 17 bytecode. In the artifact-generation environment, Gradle cannot download its distribution because outbound DNS/network access for the shell is unavailable, so no compiled JAR is claimed in this package. Run `gradlew clean build` locally to perform the real compiler/remap check.
