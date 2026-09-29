package org.marj4n.smooth_spellblade_compat.compat;

import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Keeps the current spell-delivery world available to legacy static Spell Engine APIs. */
public final class LegacyRuntimeContext {
    private static final ThreadLocal<World> CURRENT_WORLD = new ThreadLocal<>();
    private static volatile World lastWorld;

    private LegacyRuntimeContext() { }

    public static Scope enter(World world) {
        World previous = CURRENT_WORLD.get();
        CURRENT_WORLD.set(world);
        lastWorld = world;
        return () -> {
            if (previous == null) CURRENT_WORLD.remove();
            else CURRENT_WORLD.set(previous);
        };
    }

    public static void note(World world) {
        if (world != null) lastWorld = world;
    }

    @Nullable
    public static World world() {
        World current = CURRENT_WORLD.get();
        return current != null ? current : lastWorld;
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override void close();
    }
}
