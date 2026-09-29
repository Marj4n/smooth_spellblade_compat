package org.marj4n.smooth_spellblade_compat.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.Spell$Release$Target$Area;
import net.spell_engine.internals.target.EntityRelation;
import net.spell_engine.internals.target.EntityRelations;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.TargetHelper;
import net.spell_engine.utils.TargetHelper$Intent;
import net.spell_engine.utils.TargetHelper$Relation;
import net.spell_engine.utils.TargetHelper$TargetingMode;

import java.util.List;
import java.util.function.Predicate;

/**
 * Runtime bridge for TargetHelper methods removed after the legacy Spell Engine API.
 *
 * Kept outside a Mixin deliberately: Mixin 0.8.7 rejects adding new non-private
 * static methods to TargetHelper as ordinary mixin methods. Spellblades call sites
 * are rewritten to this class by SmoothSpellbladeCompatMixinPlugin instead.
 */
public final class LegacyTargetAccess {
    private LegacyTargetAccess() { }

    public static TargetHelper$Relation getRelation(LivingEntity attacker, Entity target) {
        EntityRelation relation = EntityRelations.getRelation(attacker, target);
        return switch (relation) {
            case ALLY -> TargetHelper$Relation.FRIENDLY;
            case FRIENDLY -> TargetHelper$Relation.SEMI_FRIENDLY;
            case NEUTRAL -> TargetHelper$Relation.NEUTRAL;
            case HOSTILE -> TargetHelper$Relation.HOSTILE;
            case MIXED -> TargetHelper$Relation.MIXED;
        };
    }

    public static boolean actionAllowed(
            TargetHelper$TargetingMode mode,
            TargetHelper$Intent intent,
            LivingEntity attacker,
            Entity target
    ) {
        SpellTarget.FocusMode focus = mode == TargetHelper$TargetingMode.AREA
                ? SpellTarget.FocusMode.AREA
                : SpellTarget.FocusMode.DIRECT;
        SpellTarget.Intent modernIntent = intent == TargetHelper$Intent.HELPFUL
                ? SpellTarget.Intent.HELPFUL
                : SpellTarget.Intent.HARMFUL;
        return EntityRelations.actionAllowed(focus, modernIntent, attacker, target);
    }

    public static boolean allowedToHurt(Entity first, Entity second) {
        return EntityRelations.allowedToHurt(first, second);
    }

    public static List<Entity> targetsFromArea(
            Entity caster,
            Vec3d origin,
            float range,
            Spell$Release$Target$Area oldArea,
            Predicate<Entity> predicate
    ) {
        Spell.Target.Area area = new Spell.Target.Area();
        if (oldArea != null) {
            area.horizontal_range_multiplier = oldArea.horizontal_range_multiplier;
            area.vertical_range_multiplier = oldArea.vertical_range_multiplier;
            area.angle_degrees = oldArea.angle_degrees;
        }
        return TargetHelper.targetsFromArea(
                caster.getWorld(),
                caster,
                origin,
                caster.getRotationVector(),
                range,
                area,
                predicate
        );
    }
}
