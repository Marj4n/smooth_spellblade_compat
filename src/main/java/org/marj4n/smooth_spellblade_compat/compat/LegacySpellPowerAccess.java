package org.marj4n.smooth_spellblade_compat.compat;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_power.api.SpellSchool;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility bridge for Spell Power API changes between the API used by
 * Spellblades 2.4.0 / Extra Spell Attributes 1.4.0 and Spell Power 1.6.x
 * on Minecraft 1.20.1.
 */
public final class LegacySpellPowerAccess {

    private LegacySpellPowerAccess() {
    }

    /**
     * Spell Power V1 constructor used by Spellblades:
     *
     * (Archetype, Identifier, int, RegistryKey<DamageType>, EntityAttribute)
     *
     * Modern Spell Power uses RegistryEntry<EntityAttribute> for an
     * externally-owned attribute.
     */
    public static SpellSchool createSchool(
            SpellSchool.Archetype archetype,
            Identifier id,
            int color,
            RegistryKey<DamageType> damageType,
            EntityAttribute attribute
    ) {
        if (attribute != null) {
            RegistryEntry<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(attribute);
            return new SpellSchool(archetype, id, color, damageType, entry);
        }

        // Legacy fallback for callers that provide no attribute.
        return new SpellSchool(
                archetype,
                id,
                color,
                damageType,
                (EntityAttribute) null,
                null
        );
    }

    /**
     * Spell Power V1 exposed SpellSchool.attribute as a public
     * EntityAttribute field. Modern Spell Power stores either a registry entry
     * or an internally-owned attribute. Present the old view to transformed
     * Spellblades / Extra Spell Attributes bytecode.
     */
    @Nullable
    public static EntityAttribute attribute(SpellSchool school) {
        if (school == null) {
            return null;
        }

        if (school.attributeEntry != null) {
            return school.attributeEntry.value();
        }

        return school.ownedAttribute();
    }
}
