package net.spell_engine.internals;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.SpellInfo;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.cost.Ammo;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.delivery.LaunchGeometry;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.TargetHelper$TargetingMode;
import net.spell_power.api.SpellPower;
import org.jetbrains.annotations.Nullable;
import org.marj4n.smooth_spellblade_compat.compat.LegacyRuntimeContext;
import org.marj4n.smooth_spellblade_compat.compat.ModernSpellEntries;

import java.util.List;

/** Legacy SpellHelper surface used by Spellblades 2.4, forwarded into Spell Engine 1.10. */
public final class SpellHelper {
    private SpellHelper() { }

    public record ImpactContext(
            float channel,
            float distance,
            @Nullable Vec3d position,
            SpellPower.Result power,
            TargetHelper$TargetingMode targetingMode
    ) {
        public ImpactContext() { this(1F, 1F, null, null, TargetHelper$TargetingMode.DIRECT); }
        public ImpactContext channeled(float value) { return new ImpactContext(value, distance, position, power, targetingMode); }
        public ImpactContext distance(float value) { return new ImpactContext(channel, value, position, power, targetingMode); }
        public ImpactContext position(Vec3d value) { return new ImpactContext(channel, distance, value, power, targetingMode); }
        public ImpactContext power(SpellPower.Result value) { return new ImpactContext(channel, distance, position, value, targetingMode); }
        public ImpactContext target(TargetHelper$TargetingMode value) { return new ImpactContext(channel, distance, position, power, value); }
        public boolean hasOffset() { return position != null; }
        public Vec3d knockbackDirection(Vec3d targetPosition) { return targetPosition.subtract(position).normalize(); }
        public boolean isChanneled() { return channel != 1F; }
        public float total() { return channel * distance; }

        public SpellExecution.ImpactContext toModern() {
            var focus = targetingMode == TargetHelper$TargetingMode.AREA
                    ? SpellTarget.FocusMode.AREA : SpellTarget.FocusMode.DIRECT;
            return new SpellExecution.ImpactContext(channel, distance, position, power, focus, 0);
        }

        public static ImpactContext fromModern(SpellExecution.ImpactContext context) {
            var mode = context.focusMode() == SpellTarget.FocusMode.AREA
                    ? TargetHelper$TargetingMode.AREA : TargetHelper$TargetingMode.DIRECT;
            return new ImpactContext(context.channel(), context.distance(), context.position(), context.power(), mode);
        }
    }

    public record AmmoResult(boolean satisfied, ItemStack ammo) { }

    public static AmmoResult ammoForSpell(PlayerEntity player, Spell spell, ItemStack itemStack) {
        LegacyRuntimeContext.note(player.getWorld());
        var result = Ammo.ammoForSpell(player, spell, itemStack);
        ItemStack ammo = null;
        if (result.item() != null) {
            if (result.item().item() != null) ammo = result.item().item().getDefaultStack();
            else if (!result.sources().isEmpty()) ammo = result.sources().get(0).itemStack().copyWithCount(1);
        }
        return new AmmoResult(result.satisfied(), ammo);
    }

    public static float getCooldownDuration(LivingEntity caster, Spell spell) {
        var world = caster.getWorld();
        LegacyRuntimeContext.note(world);
        RegistryEntry<Spell> entry = entryFor(world, spell);
        return entry != null ? SpellParameters.getCooldownDuration(caster, entry) : 0F;
    }

    public static TargetHelper$TargetingMode impactTargetingMode(Spell spell) {
        try {
            var field = spell.release.getClass().getField("target");
            Object target = field.get(spell.release);
            if (target != null) {
                var typeField = target.getClass().getField("type");
                Object type = typeField.get(target);
                if (type != null && "AREA".equals(type.toString())) return TargetHelper$TargetingMode.AREA;
            }
        } catch (Throwable ignored) { }
        return spell.target != null && spell.target.type == Spell.Target.Type.AREA
                ? TargetHelper$TargetingMode.AREA : TargetHelper$TargetingMode.DIRECT;
    }

    public static void imposeCooldown(PlayerEntity player, Identifier spellId, Spell spell, float progress) {
        LegacyRuntimeContext.note(player.getWorld());
        var entry = ModernSpellEntries.find(player.getWorld(), spellId);
        if (entry == null) return;
        float seconds = SpellParameters.getCooldownDuration(player, entry);
        if (spell != null && spell.cost != null && spell.cost.cooldown != null
                && spell.cost.cooldown.proportional) {
            seconds *= Math.max(0F, Math.min(progress, 1F));
        }
        int ticks = Math.max(0, Math.round(seconds * 20F));
        if (ticks > 0) ((SpellCaster.Player) player).getCooldownManager().set(entry, ticks, true);
    }

    public static Vec3d launchPoint(LivingEntity caster) {
        return LaunchGeometry.launchPoint(caster);
    }

    public static boolean performImpacts(World world, LivingEntity caster, @Nullable Entity target, Entity aoeSource,
                                         SpellInfo spellInfo, ImpactContext context) {
        return performImpacts(world, caster, target, aoeSource, spellInfo, context, true);
    }

    public static boolean performImpacts(World world, LivingEntity caster, @Nullable Entity target, Entity aoeSource,
                                         SpellInfo spellInfo, ImpactContext context, boolean additionalTargetLookup) {
        LegacyRuntimeContext.note(world);
        var entry = ModernSpellEntries.find(world, spellInfo.id());
        if (entry == null) return false;
        return SpellImpacts.performImpacts(
                world, caster, target, aoeSource,
                entry, entry.value().impacts, context.toModern(), additionalTargetLookup, null);
    }

    public static void performSpell(World world, PlayerEntity player, Identifier spellId, List<Entity> targets,
                                    SpellCast.Action action, float progress) {
        LegacyRuntimeContext.note(world);
        var entry = ModernSpellEntries.find(world, spellId);
        if (entry == null) return;
        SpellExecution.performSpell(world, player, entry, SpellTarget.SearchResult.of(targets), action, progress);
    }

    @Nullable
    private static RegistryEntry<Spell> entryFor(World world, Spell spell) {
        Identifier id = SpellRegistry.idOf(spell);
        if (id != null) return ModernSpellEntries.find(world, id);
        var registry = net.spell_engine.api.spell.registry.SpellRegistry.from(world);
        for (var entry : registry.getIndexedEntries()) {
            if (entry.value() == spell) return entry;
        }
        return null;
    }
}
