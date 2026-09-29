package net.spell_engine.api.item;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
public final class AttributeResolver { private AttributeResolver(){} public static EntityAttribute get(Identifier id){ return Registries.ATTRIBUTE.get(id); } }
