package org.marj4n.smooth_spellblade_compat.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.container.SpellContainer;
import net.spell_engine.internals.container.SpellAssignments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

/**
 * Accepts Spell Engine V1's bare spell-assignment format in addition to the
 * modern { "spell_container": { ... } } wrapper.
 */
@Mixin(value = SpellAssignments.class, priority = 2000)
public abstract class SpellAssignmentsLegacyMixin {
    @Inject(method = "parseAssignment", at = @At("HEAD"), cancellable = true, require = 0)
    private static void smoothSpellbladeCompat$parseLegacyAssignment(
            Identifier fileId,
            JsonElement root,
            CallbackInfoReturnable<SpellAssignments.Assignment> cir
    ) {
        if (root == null || !root.isJsonObject()) return;
        JsonObject object = root.getAsJsonObject();

        // Let Spell Engine parse native 1.10 assignment files itself.
        if (object.has(SpellAssignments.Assignment.CONTAINER_KEY)
                || object.has(SpellAssignments.Assignment.CHOICE_KEY)) {
            return;
        }

        // Only claim files that actually look like the V1 container schema.
        if (!object.has("is_proxy")
                && !object.has("spell_ids")
                && !object.has("pool")
                && !object.has("max_spell_count")) {
            return;
        }

        boolean proxy = object.has("is_proxy") && object.get("is_proxy").getAsBoolean();
        int maxSpellCount = object.has("max_spell_count")
                ? object.get("max_spell_count").getAsInt()
                : 0;
        String pool = object.has("pool") && !object.get("pool").isJsonNull()
                ? object.get("pool").getAsString()
                : "";

        var spellIds = new ArrayList<String>();
        if (object.has("spell_ids") && object.get("spell_ids").isJsonArray()) {
            for (var element : object.getAsJsonArray("spell_ids")) {
                if (element.isJsonPrimitive()) spellIds.add(element.getAsString());
            }
        }

        // V1 proxy containers merged MAGIC sources by default (the old `content` default).
        // V1 non-proxy containers cast only their own assigned spells.
        var access = proxy
                ? SpellContainer.ContentType.MAGIC
                : SpellContainer.ContentType.CONTAINED;

        var container = new SpellContainer(
                access,
                "",
                pool,
                "",
                maxSpellCount,
                spellIds,
                0
        );
        cir.setReturnValue(new SpellAssignments.Assignment(container, null));
    }
}
