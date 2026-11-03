# Smooth Spellblade Compat

Fabric 1.20.1 compatibility layer for Spellblades/Spellblade Next 2.4.0 on Spell Engine 1.10.7 and Spell Power 1.6.x. Bridges legacy APIs, spell delivery and migrated spell data while retaining the original Spellblades mod.

## Build

Use JDK 21; output targets Java 17.

```sh
./gradlew build
```

Install the regular JAR from `build/libs/` with Fabric API, Spellblades, Spell Engine, Spell Power and Extra Spell Attributes. Exact versions are in `gradle.properties`, `build.gradle` and mod metadata. This addon does not replace Spellblades.

## Source layout

`src/main/java/org/marj4n` contains compatibility adapters; legacy `net.spell_engine` packages are intentional API facades. `src/client` contains client adapters; resource folders contain migrated spells, assignments and tags. Preserve those public names and descriptors when editing.

The unscoped legacy world fallback is weak; nested spell-delivery scopes remain strong until closed. Migrated mana/custom-delivery and visual behavior still need per-spell coverage; startup or build success is not proof of complete gameplay equivalence. The unassigned Bulwark draft is retained in source but excluded from active migration: upstream 2.4.0 never registers its status effect, so exposing it would crash spell delivery. Rebuke reuses the existing upstream gladius projectile geometry; the upstream distribution does not contain its referenced spear model.

## License

By Marj4n. Upstream Spell Engine and Spellblades retain their original authorship and notices. See [LICENSE.txt](LICENSE.txt).

## Reuse permissions

Reusing or modifying Marj4n-owned source code or assets requires prior written permission. Contact Marj4n through [nivekts.vercel.app](https://nivekts.vercel.app/) with the intended use. See [LICENSE.txt](LICENSE.txt) for gameplay permissions and exceptions. Third-party licenses, existing grants and GitHub platform rights remain applicable.

Migrated Spellblades material retains cleannrooster's MIT license, reproduced in `LICENSE.txt`. The permission requirement above applies only to Marj4n-owned material and does not restrict those MIT rights.
