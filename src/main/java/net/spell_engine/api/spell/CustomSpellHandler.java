package net.spell_engine.api.spell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.casting.SpellCast;
import org.marj4n.smooth_spellblade_compat.compat.LegacyRuntimeContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import it.unimi.dsi.fastutil.Function;

/** Legacy custom spell delivery API used by Spellblades 2.4. */
public final class CustomSpellHandler {
    public static final Map<Identifier, Function<Data, Boolean>> handlers = new HashMap<>();

    public record Data(
            PlayerEntity caster,
            List<Entity> targets,
            ItemStack itemStack,
            SpellCast.Action action,
            float progress,
            SpellHelper.ImpactContext impactContext
    ) { }

    private CustomSpellHandler() { }

    public static void register(Identifier id, Function<Data, Boolean> handler) {
        handlers.put(id, handler);
        SpellHandlers.registerCustomDelivery(id, (world, entry, caster, targets, context, location) -> {
            if (!(caster instanceof PlayerEntity player)) return false;
            try (var ignored = LegacyRuntimeContext.enter(world)) {
                var entities = new ArrayList<>(targets.stream()
                        .map(net.spell_engine.internals.SpellExecution.DeliveryTarget::entity)
                        .toList());
                var action = context.isChanneled() ? SpellCast.Action.CHANNEL : SpellCast.Action.RELEASE;
                var oldContext = SpellHelper.ImpactContext.fromModern(context);
                var result = handler.get(new Data(
                        player,
                        entities,
                        player.getMainHandStack(),
                        action,
                        context.charge(),
                        oldContext
                ));
                return Boolean.TRUE.equals(result);
            }
        });
    }
}
