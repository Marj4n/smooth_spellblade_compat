package org.marj4n.smooth_spellblade_compat.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.SpellInfo;
import net.spell_engine.entity.SpellProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = SpellProjectile.class, priority = 100)
public abstract class SpellProjectileLegacyMixin {
    @Shadow public abstract RegistryEntry<Spell> getSpellEntry();

    public Spell getSpell() {
        var entry = getSpellEntry();
        return entry == null ? null : entry.value();
    }

    public SpellInfo getSpellInfo() {
        var entry = getSpellEntry();
        if (entry == null || entry.getKey().isEmpty()) return null;
        return new SpellInfo(entry.value(), entry.getKey().get().getValue());
    }
}
