package org.marj4n.smooth_spellblade_compat.mixin;

import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.Spell$Cast;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Spell.class, priority = 100)
public abstract class SpellLegacyFieldsMixin {
    public Spell$Cast cast;
    public Spell.Impact[] impact = new Spell.Impact[0];
}
