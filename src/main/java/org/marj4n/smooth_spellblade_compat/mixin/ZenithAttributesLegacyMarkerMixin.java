package org.marj4n.smooth_spellblade_compat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Applies the compat plugin's post-transform pass to Zenith Attributes Renewed's
 * legacy Spell Power bridge. Zenith Attributes 1.0.3 still reads the removed
 * SpellSchool.attribute field while Spell Power 1.6.x stores the attribute via
 * attributeEntry / ownedAttribute instead.
 */
@Pseudo
@Mixin(targets = {
        "dev.shadowsoffire.attributeslib.compat.SpellPowerCompat"
}, priority = 100, remap = false)
public abstract class ZenithAttributesLegacyMarkerMixin { }
