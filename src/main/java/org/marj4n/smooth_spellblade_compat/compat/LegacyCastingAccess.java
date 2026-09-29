package org.marj4n.smooth_spellblade_compat.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.spell_engine.internals.SpellCooldownManager;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.casting.SpellCasterEntity;

public final class LegacyCastingAccess {
    private LegacyCastingAccess() { }

    public static SpellCooldownManager cooldownManager(SpellCasterEntity caster) {
        var modernCaster = (SpellCaster.Player) caster;
        var player = (PlayerEntity) caster;
        LegacyRuntimeContext.note(player.getWorld());
        return new SpellCooldownManager(player.getWorld(), modernCaster.getCooldownManager());
    }

    public static void setSpellCastProcess(SpellCasterEntity caster, SpellCast.Process process) {
        var interactor = ((SpellCaster.Player) caster).getInteractor();
        if (process == null) interactor.requestClear();
        else interactor.setProcess(process);
    }
}
