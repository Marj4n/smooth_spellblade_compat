package org.marj4n.smooth_spellblade_compat.mixin;

import net.spell_engine.api.spell.ParticleBatch;
import net.spell_engine.api.spell.Spell;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Spell.Impact.class, priority = 100)
public abstract class SpellImpactLegacyFieldsMixin {
    public ParticleBatch[] particles = new ParticleBatch[0];
}
