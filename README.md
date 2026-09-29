# Smooth Spellblade Compat

Compatibility/forward-port layer for running **Spellblades and Such / Spellblade Next 2.4.0** on the modern **Spell Engine 1.10.7** stack used by Smooth Odyssey.

## Target stack

- Minecraft 1.20.1
- Fabric Loader 0.19.5+
- Fabric API 0.92.12+1.20.1
- Spell Engine 1.10.7+1.20.1-fabric
- Spell Power 1.6.0+1.20.1
- Extra Spell Attributes 1.4.0
- Spellblade Next / Spellblades and Such 2.4.0+1.20.1
- Java 17 bytecode (Gradle/Loom can run on JDK 21)

## What this addon does

Spellblades 2.4.0 was compiled against the legacy Spell Engine API. Spell Engine 1.10 moved/decomposed several APIs and changed the spell data format. This project provides a focused compatibility layer instead of modifying the Spellblades jar in-place.

Implemented in this alpha:

- legacy `net.spell_engine.internals.SpellRegistry` facade over the modern dynamic spell registry;
- legacy `SpellHelper`, `SpellContainerHelper`, cooldown and scheduler surfaces used by Spellblades;
- legacy spell binary structures (`Spell$Cast`, `Spell$Release$Target*`, `SpellInfo`, `SpellPool`, particles/sounds);
- compatibility bridges for old Spell Engine item/config helper APIs used during Spellblades bootstrap;
- bytecode adaptation for members whose owner/name stayed the same while their descriptor changed;
- early legacy overload for `AnimationHelper.sendAnimation(...)`, required by Spellblades' own required mixin;
- legacy SpellProjectile constructor/getter bridge;
- old custom spell delivery registration forwarded to `SpellHandlers.registerCustomDelivery`;
- old assignment parser fallback plus converted modern assignments;
- V1 spellbook fallback containers registered into Spell Engine 1.10's assignment system;
- 80 Spellblades spell JSONs migrated to `data/spellbladenext/spell/`;
- 10 legacy spell pools migrated to modern `tags/spell` registry tags;
- 16 legacy spell assignments migrated to Spell Engine 1.10 assignment format.

## Build

```powershell
.\gradlew clean build
```

Expected output is under `build/libs/`.

## Runtime installation

Keep the original Spellblades 2.4.0 jar installed. This addon is a bridge and does not replace it.

Required runtime mods are declared in `fabric.mod.json`.

## Alpha status / known behavior gaps

This is a compatibility forward-port, not an upstream source rewrite. The current goal is to get Spellblades 2.4.0 through class loading/mixin application and preserve as much V1 behavior as possible through modern APIs.

The migrated data still has behavior that needs runtime verification:

- 32 spell definitions preserve the old `rpgmana` value as `legacy_rpgmana`; Spell Engine 1.10 core does not consume that field directly.
- 39 spell definitions use modern `CUSTOM` delivery. The old Spellblades Java bootstrap registers handlers for 31 primary IDs; several additional custom-delivery IDs require runtime/source-level verification.
- visual migration is conservative. Old particle/model semantics that changed in Spell Engine 1.10 may need per-spell tuning after the first successful game boot.

See `docs/COMPAT_AUDIT.md` for the exact audit status.

## crash-chain patch

Added Spell Power 1.6 bridge required by Spellblades 2.4.0:

- rewrites the removed V1 `SpellSchool(..., EntityAttribute)` constructor to a modern external-attribute school factory
- rewrites reads of removed `SpellSchool.attribute` to the modern `attributeEntry` / `ownedAttribute()` view
- adds `CustomSpellSchools` to the post-mixin bytecode marker (the reason 0.1.0 missed the constructor crash)
- adds remaining Spellblades classes known to read the removed `SpellSchool.attribute` field

Observed crash fixed by this patch:
`NoSuchMethodError: SpellSchool.<init>(Archetype, Identifier, int, RegistryKey, EntityAttribute)`


## runtime compatibility fix

- Bridges Spell Power 1.6.x removal of `SpellSchool.attribute` for legacy callers.
- Runs the post-mixin bytecode adapter on Minecraft `AttributeContainer` after Extra Spell Attributes 1.4.0 injects its legacy field reads.
- Fixes the world-entry tooltip crash caused by `extraspellattributes$getAttributeValueCONVERTFROM`.
- Uses the Minecraft 1.20.1 `Registry#getEntry(EntityAttribute)` return type directly.


### TargetHelper runtime fix
- Removed invalid public-static TargetHelper mixin bridge rejected by Mixin 0.8.7.
- Rewrites legacy TargetHelper getRelation/actionAllowed/allowedToHurt/targetsFromArea call sites to LegacyTargetAccess.
- Added Spellblades effect.Challenged and entity.Magister to the post-transform coverage.
- Replaced legacy SoundHelper public-static mixin with call-site bridge.
- AnimationHelper legacy String overload is now injected by the config plugin before Spellblades' required mixin applies, avoiding the same Mixin 0.8.7 visibility trap.

## mutable legacy target lists

Spell Engine 1.10 may provide immutable delivery target lists, while Spellblades 2.4 legacy custom handlers assume `CustomSpellHandler.Data.targets()` is mutable (for example `eviscerate` removes the caster from the list). The compatibility bridge now copies modern delivery targets into a mutable `ArrayList` before invoking every legacy custom handler.

## 1.0.1 - Extra Spell Attributes construction guard

- Fixes a server-start crash caused by Extra Spell Attributes 1.4.0 reading `DISSOLUTION` while a `LivingEntity` is still being constructed.
- Handles the Create / Porting Lib entity-size callback path and Moonlight / Supplementaries `FakePlayer` startup path.
- When the `AttributeContainer` is not initialized yet, `LivingEntity#getAttributeValue(...)` temporarily returns the attribute default value instead of dereferencing `null`.
- The guard only changes the short entity-construction window; normal attribute calculations are untouched after initialization.

Observed crash fixed by this patch:
`NullPointerException: LivingEntity.getAttributes() is null -> extraspellattributes$getHealthDissolution`

## 1.0.2 - Zenith Attributes Renewed compatibility

- Added an optional compatibility transform for **Zenith Attributes Renewed 1.0.3** (`zenith_attributes`).
- Fixed a client crash while rendering the Attributes GUI with Spell Power 1.6.x.
- Redirects Zenith Attributes' legacy `SpellSchool.attribute` field access through `LegacySpellPowerAccess.attribute(...)`.
- Keeps the Zenith integration optional; the marker mixin is only applied when `zenith_attributes` is installed.
- Retains the 1.0.1 Extra Spell Attributes early-construction guard and all previous Spellblades compatibility fixes.
