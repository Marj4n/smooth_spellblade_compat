package net.spell_engine.internals;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.marj4n.smooth_spellblade_compat.compat.ModernSpellEntries;

/** Binary bridge for the pre-1.10 Spell Engine cooldown manager. */
public final class SpellCooldownManager {
    private final World world;
    private final net.spell_engine.internals.cost.SpellCooldownManager delegate;

    public SpellCooldownManager(World world, net.spell_engine.internals.cost.SpellCooldownManager delegate) {
        this.world = world;
        this.delegate = delegate;
    }

    public boolean isCoolingDown(Identifier spellId) {
        var entry = ModernSpellEntries.find(world, spellId);
        return entry != null && delegate.isCoolingDown(entry);
    }

    public void set(Identifier spellId, int duration) { set(spellId, duration, true); }

    public void set(Identifier spellId, int duration, boolean force) {
        var entry = ModernSpellEntries.find(world, spellId);
        if (entry != null) delegate.set(entry, duration, force);
    }

    public void remove(Identifier spellId) { delegate.remove(spellId); }
    public void reset(@Nullable Identifier spellId) { delegate.reset(spellId); }
}
