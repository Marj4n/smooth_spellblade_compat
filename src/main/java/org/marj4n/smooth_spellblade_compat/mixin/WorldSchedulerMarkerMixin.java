package org.marj4n.smooth_spellblade_compat.mixin;

import net.minecraft.world.World;
import org.marj4n.smooth_spellblade_compat.compat.LegacyRuntimeContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = World.class, priority = 100)
public abstract class WorldSchedulerMarkerMixin implements net.spell_engine.internals.WorldScheduler {
    @Inject(method = "<init>", at = @At("RETURN"), require = 0)
    private void smoothSpellbladeCompat$rememberWorld(CallbackInfo ci) {
        LegacyRuntimeContext.note((World) (Object) this);
    }
}
