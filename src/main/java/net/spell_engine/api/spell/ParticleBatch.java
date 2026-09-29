package net.spell_engine.api.spell;
import net.spell_engine.api.spell.fx.ParticleGroup;
import org.jetbrains.annotations.Nullable;
public class ParticleBatch {
 public String particle_id; public Origin origin=Origin.CENTER; public enum Origin{FEET,CENTER,LAUNCH_POINT}
 public Rotation rotation=null; public enum Rotation{LOOK; @Nullable public static Rotation from(int ordinal){return ordinal<0||ordinal>=values().length?null:values()[ordinal];}}
 public float roll=0,roll_offset=0; public Shape shape=Shape.SPHERE; public enum Shape{CIRCLE,PILLAR,PIPE,SPHERE,CONE,LINE}
 public float count=1,min_speed=0,max_speed=1,angle=0; public static final float EXTENT_TRESHOLD=1000; public float extent=0,pre_spawn_travel=0; public boolean invert=false;
 public ParticleBatch(){}
 public ParticleBatch(String id,Shape shape,Origin origin,Rotation rotation,float roll,float rollOffset,float count,float min,float max,float angle,float extent,float pre,boolean invert){this.particle_id=id;this.shape=shape;this.origin=origin;this.rotation=rotation;this.roll=roll;this.roll_offset=rollOffset;this.count=count;this.min_speed=min;this.max_speed=max;this.angle=angle;this.extent=extent;this.pre_spawn_travel=pre;this.invert=invert;}
 @Deprecated public ParticleBatch(String id,Shape shape,Origin origin,Rotation rotation,float count,float min,float max,float angle){this(id,shape,origin,rotation,count,min,max,angle,0);}
 @Deprecated public ParticleBatch(String id,Shape shape,Origin origin,Rotation rotation,float count,float min,float max,float angle,float extent){this(id,shape,origin,rotation,0,0,count,min,max,angle,extent,0,false);}
 public ParticleBatch(ParticleBatch o){this(o.particle_id,o.shape,o.origin,o.rotation,o.roll,o.roll_offset,o.count,o.min_speed,o.max_speed,o.angle,o.extent,o.pre_spawn_travel,o.invert);}
 public ParticleGroup toModern(){
   var g=new ParticleGroup(); g.id=particle_id; var b=g.batch;
   b.shape=switch(shape){case CIRCLE->ParticleGroup.Shape.CIRCLE;case PILLAR->ParticleGroup.Shape.PILLAR;case PIPE->ParticleGroup.Shape.PIPE;case SPHERE->ParticleGroup.Shape.SPHERE;case CONE->ParticleGroup.Shape.CONE;case LINE->ParticleGroup.Shape.LINE;};
   switch(origin){case FEET->{b.anchor=ParticleGroup.Anchor.ENTITY;b.vertical_origin=0.1F;} case CENTER->{b.anchor=ParticleGroup.Anchor.ENTITY;b.vertical_origin=0.5F;} case LAUNCH_POINT->b.anchor=ParticleGroup.Anchor.LAUNCH_POINT;}
   if(rotation==Rotation.LOOK)b.alignment=ParticleGroup.Alignment.LOOK; b.roll_per_tick=roll; b.roll_offset=roll_offset; b.count=count; b.min_speed=min_speed; b.max_speed=max_speed; b.angle=angle;
   if(Math.abs(extent)>=EXTENT_TRESHOLD){b.width_factor=0;b.extent=Math.max(0,Math.abs(extent)-EXTENT_TRESHOLD);}else b.extent=extent;
   b.pre_travel=pre_spawn_travel;b.invert=invert; return g;
 }
}
