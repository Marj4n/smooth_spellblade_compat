package org.marj4n.smooth_spellblade_compat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/** Applies the config plugin's post-transform compatibility pass to Spellblades' own classes. */
@Pseudo
@Mixin(targets = {
        "com.spellbladenext.Spellblades",
        "com.spellbladenext.SpellbladesClient",
        "com.spellbladenext.CustomSpellSchools",
        "com.spellbladenext.client.entity.MagisterRenderer",
        "com.spellbladenext.effect.Collapse",
        "com.spellbladenext.effect.Challenged",
        "com.spellbladenext.effect.Fervor",
        "com.spellbladenext.effect.Inexorable",
        "com.spellbladenext.effect.Slamming",
        "com.spellbladenext.effect.Spellstrike",
        "com.spellbladenext.entity.Archmagus",
        "com.spellbladenext.entity.Magus",
        "com.spellbladenext.entity.Magister",
        "com.spellbladenext.entity.CycloneEntity",
        "com.spellbladenext.entity.ai.ArchmagusJumpBack",
        "com.spellbladenext.entity.ai.BackUp",
        "com.spellbladenext.entity.ai.MagusAttackGoal",
        "com.spellbladenext.entity.ai.MagusDivebombGoal",
        "com.spellbladenext.entity.ai.MagusJumpBack",
        "com.spellbladenext.entity.ai.MagusSwirlGoal",
        "com.spellbladenext.entity.ai.MagusThrowGoal",
        "com.spellbladenext.entity.ai.SpellAttack",
        "com.spellbladenext.items.Claymore",
        "com.spellbladenext.items.EviscerateOil",
        "com.spellbladenext.items.FinalStrikeOil",
        "com.spellbladenext.items.FlickerStrikeOil",
        "com.spellbladenext.items.RandomSpellOil",
        "com.spellbladenext.items.SmiteOil",
        "com.spellbladenext.items.SpellOil",
        "com.spellbladenext.items.ThesisBook",
        "com.spellbladenext.items.TheAvatar",
        "com.spellbladenext.items.WhirlwindOil",
        "com.spellbladenext.items.attacks.Attacks",
        "com.spellbladenext.items.Spellblade"
}, priority = 100, remap = false)
public abstract class SpellbladesLegacyMarkerMixin { }
