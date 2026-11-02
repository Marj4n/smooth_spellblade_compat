package org.marj4n.smooth_spellblade_compat.compat;

import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.lang.ref.WeakReference;

/**
 * Scoped delivery keeps its World strongly reachable until close. The unscoped
 * legacy fallback observes a World; it must not own a closed world or server.
 */
public final class LegacyRuntimeContext {
    private static final ThreadLocal<World> CURRENT_WORLD = new ThreadLocal<>();
    private static volatile WeakReference<World> lastWorld = new WeakReference<>(null);

    private LegacyRuntimeContext() { }

    public static Scope enter(World world) {
        World previous = CURRENT_WORLD.get();
        CURRENT_WORLD.set(world);
        remember(world);
        return () -> {
            if (previous == null) CURRENT_WORLD.remove();
            else CURRENT_WORLD.set(previous);
        };
    }

    public static void note(World world) {
        if (world != null) remember(world);
    }

    private static void remember(World world) {
        // Reuse the wrapper for repeated lookups in the same live world.
        if (lastWorld.get() != world) lastWorld = new WeakReference<>(world);
    }

    @Nullable
    public static World world() {
        World current = CURRENT_WORLD.get();
        return current != null ? current : lastWorld.get();
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override void close();
    }
}
