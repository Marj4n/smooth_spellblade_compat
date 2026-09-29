package org.marj4n.smooth_spellblade_compat.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Guards attribute reads performed while a LivingEntity is still inside its constructor.
 *
 * Extra Spell Attributes 1.4.0 injects dissolution logic into LivingEntity#getHealth().
 * Create/Porting Lib can query entity dimensions from the base Entity constructor, before
 * LivingEntity's AttributeContainer has been assigned. In that short construction window,
 * vanilla getAttributeValue() dereferences a null AttributeContainer and crashes.
 *
 * Returning the attribute's default value is equivalent to "no modifiers have been applied"
 * and is only used while the container is unavailable. Once construction completes, vanilla
 * behavior is untouched.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAttributeGuardMixin {

    @Inject(
            method = "getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D",
            at = @At("HEAD"),
            cancellable = true
    )
    private void smoothSpellbladeCompat$guardUninitializedAttributes(
            EntityAttribute attribute,
            CallbackInfoReturnable<Double> cir
    ) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self.getAttributes() == null) {
            cir.setReturnValue(attribute != null ? attribute.getDefaultValue() : 0.0D);
        }
    }
}
