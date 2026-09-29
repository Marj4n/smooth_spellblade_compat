package org.marj4n.smooth_spellblade_compat.compat;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.spell_engine.api.spell.Sound;
import net.spell_engine.utils.SoundHelper;

/** Bridges Spell Engine V1 SoundHelper calls to the modern fx.Sound API. */
public final class LegacySoundAccess {
    private LegacySoundAccess() { }

    public static void playSound(World world, Entity entity, Sound sound) {
        if (sound != null) {
            SoundHelper.playSound(world, entity, sound.toModern());
        }
    }
}
