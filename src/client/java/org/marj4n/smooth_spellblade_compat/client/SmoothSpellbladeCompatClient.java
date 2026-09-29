package org.marj4n.smooth_spellblade_compat.client;

import net.fabricmc.api.ClientModInitializer;

/** Client bootstrap. All actual compatibility hooks are installed through mixins. */
public final class SmoothSpellbladeCompatClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Intentionally empty.
    }
}
