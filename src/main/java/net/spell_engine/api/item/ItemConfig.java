package net.spell_engine.api.item;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import java.util.*;
public class ItemConfig {
 public static class Attribute { public String id; public float value; public EntityAttributeModifier.Operation operation; public Attribute(){} public Attribute(String id,float value,EntityAttributeModifier.Operation op){this.id=id;this.value=value;this.operation=op;} public static Attribute bonus(Identifier id,float v){return new Attribute(id.toString(),v,EntityAttributeModifier.Operation.ADDITION);} public static Attribute multiply(Identifier id,float v){return new Attribute(id.toString(),v,EntityAttributeModifier.Operation.MULTIPLY_BASE);} public static ArrayList<Attribute> bonuses(List<Identifier> ids,float v){var out=new ArrayList<Attribute>(); for(var id:ids) out.add(bonus(id,v)); return out;} }
 public Map<String,Weapon> weapons=new HashMap<>();
 public static class Weapon { public float attack_damage,attack_speed; public ArrayList<Attribute> attributes=new ArrayList<>(); public Weapon(){} public Weapon(float d,float s){attack_damage=d;attack_speed=s;} public Weapon add(Attribute a){attributes.add(a);return this;} }
 public Map<String,ArmorSet> armor_sets=new HashMap<>();
 public static class ArmorSet { public float armor_toughness,knockback_resistance; public Piece head=new Piece(),chest=new Piece(),legs=new Piece(),feet=new Piece(); public static class Piece { public int armor; public ArrayList<Attribute> attributes=new ArrayList<>(); public Piece(){} public Piece(int a){armor=a;} public Piece add(Attribute a){attributes.add(a);return this;} public Piece addAll(List<Attribute> a){attributes.addAll(a);return this;} } public static ArmorSet with(Piece h,Piece c,Piece l,Piece f){var s=new ArmorSet();s.head=h;s.chest=c;s.legs=l;s.feet=f;return s;} }
}
