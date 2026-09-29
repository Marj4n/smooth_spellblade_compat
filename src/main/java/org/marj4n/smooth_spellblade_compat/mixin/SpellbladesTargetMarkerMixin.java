package org.marj4n.smooth_spellblade_compat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.spell_engine.internals.SpellContainerHelper;
import net.spell_engine.internals.SpellHelper;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Runs after Spellblades' and Extra Spell Attributes' common mixins so merged
 * legacy bytecode is adapted too.
 *
 * AttributeContainer is intentionally included here: Extra Spell Attributes
 * 1.4.0 injects code into it that still reads the removed
 * SpellSchool.attribute field. With this marker at priority 100, the config
 * plugin gets a postApply pass after ESA's default-priority mixin and rewrites
 * those field reads to LegacySpellPowerAccess.attribute(...).
 */
@Mixin(value = {
        PlayerEntity.class,
        LivingEntity.class,
        ItemEntity.class,
        ItemStack.class,
        Entity.class,
        AttributeContainer.class,
        SpellHelper.class,
        SpellContainerHelper.class
}, priority = 100)
public abstract class SpellbladesTargetMarkerMixin { }
