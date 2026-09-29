package org.marj4n.smooth_spellblade_compat.compat;

import net.spell_engine.api.spell.Sound;
import net.spell_engine.api.spell.Spell;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Accessors used by bytecode-rewritten V1 field reads whose descriptors changed in V2. */
public final class LegacySpellAccess {
    private static final Map<Spell.Release, String> RELEASE_ANIMATIONS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private LegacySpellAccess() { }

    public static void rememberReleaseAnimation(Spell.Release release, String animation) {
        if (release != null && animation != null) RELEASE_ANIMATIONS.put(release, animation);
    }

    public static String releaseAnimation(Spell.Release release) {
        if (release == null) return null;
        String legacy = RELEASE_ANIMATIONS.get(release);
        if (legacy != null) return legacy;
        return release.animation != null ? release.animation.id : null;
    }

    public static Sound releaseSound(Spell.Release release) {
        return release == null ? null : Sound.fromModern(release.sound);
    }
}
