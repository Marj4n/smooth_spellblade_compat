package net.spell_engine.internals;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.spell_engine.api.item.SpellItemData;
import net.spell_engine.api.spell.SpellContainer;
import net.spell_engine.internals.container.SpellContainerSource;

import java.util.ArrayList;

/** Legacy container facade backed by Spell Engine 1.10's container API. */
public final class SpellContainerHelper {
    private SpellContainerHelper() { }

    public static SpellContainer containerFromItemStack(ItemStack itemStack) {
        return SpellContainer.fromModern(
                net.spell_engine.api.spell.container.SpellContainerHelper.containerFromItemStack(itemStack));
    }

    /**
     * Exact V1 behavior: getEquipped(ItemStack, player) ignored the supplied stack and returned
     * the merged spell container available to the player. Spell Engine 1.10 already maintains
     * that merged result in SpellContainerSource.
     */
    public static SpellContainer getEquipped(ItemStack itemStack, PlayerEntity player) {
        if (player == null) return containerFromItemStack(itemStack);
        return SpellContainer.fromModern(SpellContainerSource.activeContainerOf(player));
    }

    /** Kept for source/binary consumers from the same V1 API generation. */
    public static SpellContainer getEquipped(SpellContainer starterContainer, PlayerEntity player) {
        if (player == null) return starterContainer;
        return SpellContainer.fromModern(SpellContainerSource.activeContainerOf(player));
    }

    public static SpellContainer getAvailable(PlayerEntity player) {
        return player == null ? null : SpellContainer.fromModern(SpellContainerSource.activeContainerOf(player));
    }

    public static void addContainerToItemStack(SpellContainer container, ItemStack itemStack) {
        if (container != null && itemStack != null && !itemStack.isEmpty()) {
            SpellItemData.setSpellContainer(itemStack, container.toModern());
        }
    }

    public static void addSpell(Identifier spellId, ItemStack itemStack) {
        var modern = net.spell_engine.api.spell.container.SpellContainerHelper.containerFromItemStack(itemStack);
        if (modern == null) return;
        var ids = new ArrayList<>(modern.spell_ids());
        if (!ids.contains(spellId.toString())) ids.add(spellId.toString());
        SpellItemData.setSpellContainer(itemStack, modern.copyWith(ids));
    }

    public static boolean contains(SpellContainer container, Identifier spellId) {
        return container != null && container.spell_ids != null && container.spell_ids.contains(spellId.toString());
    }

    public static Identifier getPoolId(SpellContainer container) {
        return container == null || container.pool == null || container.pool.isBlank()
                ? null : new Identifier(container.pool);
    }
}
