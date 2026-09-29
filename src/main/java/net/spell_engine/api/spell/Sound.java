package net.spell_engine.api.spell;
import java.util.Random;
public final class Sound {
 private String id; private float volume=1,pitch=1,randomness=0.1F; private static final Random RNG=new Random();
 public Sound(){} public Sound(String id){this.id=id;} public Sound(String id,float volume,float pitch,float randomness){this.id=id;this.volume=volume;this.pitch=pitch;this.randomness=randomness;}
 public String id(){return id;} public float volume(){return volume;} public float pitch(){return pitch;} public float randomness(){return randomness;}
 public float randomizedPitch(){ return randomness>0 ? pitch-randomness+RNG.nextFloat()*(randomness*2F) : pitch; }
 public net.spell_engine.api.spell.fx.Sound toModern(){ return new net.spell_engine.api.spell.fx.Sound(id,volume,pitch,randomness); }
 public static Sound fromModern(net.spell_engine.api.spell.fx.Sound sound){ return sound==null?null:new Sound(sound.id(),sound.volume(),sound.pitch(),sound.randomness()); }
}
