package org.marj4n.smooth_spellblade_compat.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.entity.SpellProjectile;
import net.spell_engine.internals.SpellHelper;

public final class LegacyProjectileFactory {
    private LegacyProjectileFactory() { }

    public static SpellProjectile create(
            World world,
            LivingEntity caster,
            double x, double y, double z,
            SpellProjectile.Behaviour behaviour,
            Identifier spellId,
            Entity target,
            SpellHelper.ImpactContext context,
            Spell.ProjectileData.Perks perks
    ) {
        LegacyRuntimeContext.note(world);
        var entry = ModernSpellEntries.find(world, spellId);
        if (entry == null) {
            throw new IllegalStateException("Smooth Spellblade Compat: missing spell " + spellId + " in modern registry");
        }
        var projectile = new SpellProjectile(
                world, caster, x, y, z, behaviour,
                entry,
                context == null ? new net.spell_engine.internals.SpellExecution.ImpactContext() : context.toModern(),
                perks
        );
        if (target != null) projectile.setFollowedTarget(target);
        return projectile;
    }
}
