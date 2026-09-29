package org.marj4n.smooth_spellblade_compat.mixin;

import net.spell_engine.api.spell.ParticleBatch;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.Spell$Release$Target;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Spell.Release.class, priority = 100)
public abstract class SpellReleaseLegacyFieldsMixin {
    public Spell$Release$Target target;
    public ParticleBatch[] particles = new ParticleBatch[0];
}
