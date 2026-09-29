package org.marj4n.smooth_spellblade_compat.mixin;

import net.spell_engine.utils.AnimationHelper;
import org.spongepowered.asm.mixin.Mixin;

/**
 * High-priority marker. The config plugin injects the removed String-based
 * sendAnimation overload into AnimationHelper during preApply, before
 * Spellblades' required AnimationHelperMixin is processed.
 */
@Mixin(value = AnimationHelper.class, priority = 2000)
public abstract class AnimationHelperLegacyMixin { }
