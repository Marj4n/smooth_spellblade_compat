package org.marj4n.smooth_spellblade_compat.compat;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import org.jetbrains.annotations.Nullable;

public final class ModernSpellEntries {
    private ModernSpellEntries() { }

    @Nullable
    public static RegistryEntry<Spell> find(World world, Identifier id) {
        if (world == null || id == null) return null;
        LegacyRuntimeContext.note(world);
        return SpellRegistry.from(world)
                .getEntry(RegistryKey.of(SpellRegistry.KEY, id))
                .orElse(null);
    }
}
