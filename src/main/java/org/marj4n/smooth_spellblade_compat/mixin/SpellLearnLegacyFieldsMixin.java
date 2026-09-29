package org.marj4n.smooth_spellblade_compat.mixin;

import net.spell_engine.api.spell.Spell;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Spell.Learn.class, priority = 100)
public abstract class SpellLearnLegacyFieldsMixin {
    public int tier = 0;
}
