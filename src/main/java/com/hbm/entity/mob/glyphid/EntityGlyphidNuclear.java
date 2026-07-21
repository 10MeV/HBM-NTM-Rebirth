package com.hbm.entity.mob.glyphid;

import com.hbm.entity.logic.EntityWaypoint;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.mob.EntityParasiteMaggot;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.*;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Boss Glyphid allowed only as the source pollution penalty. */
public class EntityGlyphidNuclear extends EntityGlyphid {
    public int deathTicks;
    private boolean hasDeathWaypoint;
    public EntityGlyphidNuclear(EntityType<? extends EntityGlyphidNuclear> type, Level level) {
        super(type, level);
        // User-defined exception boundary: this pollution punishment carrier
        // must not turn into a boss reward path.
        xpReward = 0;
    }
    public EntityGlyphidNuclear(Level level) { this(ModEntityTypes.GLYPHID_NUCLEAR.get(), level); }
    public static AttributeSupplier.Builder createAttributes() { return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,100).add(Attributes.MOVEMENT_SPEED,.8).add(Attributes.ATTACK_DAMAGE,50).add(Attributes.FOLLOW_RANGE,16); }
    @Override public ResourceLocation getSkin(){ return new ResourceLocation(HbmNtm.MOD_ID,"textures/entity/glyphid_nuclear.png"); }
    @Override public float getScale(){ return 2F; }
    @Override protected float getArmorThresholdMultiplier(){ return 10F; }
    @Override protected float getArmorResistanceMultiplier(){ return .5F; }
    @Override protected boolean isArmorBroken(float a){ return random.nextInt(100)<=Math.min(Math.pow(a*.12,2),100); }
    @Override public boolean doesInfectedSpawnMaggots(){ return false; }
    @Override public boolean isNuclearGlyphid(){ return true; }
    @Override protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        // Do not inherit ordinary Glyphid meat drops: Nuclear Glyphid has no drops.
    }
    @Override public void tick(){ super.tick(); if(!level().isClientSide()&&tickCount%20==0){ if(isAtDestination()&&getCurrentTask()==TASK_FOLLOW)setCurrentTask(TASK_IDLE,null); if(getCurrentTask()==TASK_BUILD_HIVE&&getTarget()==null)addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,3)); if(getCurrentTask()==TASK_TERRAFORM)setHealth(0); } }
    @Override public void communicate(int task, EntityWaypoint waypoint){ int r=waypoint==null?4:waypoint.radius; for(Entity e:level().getEntities(this,getBoundingBox().inflate(r))){ if(e instanceof EntityGlyphidScout s&&s.getCurrentTask()!=task)s.setCurrentTask(task,waypoint); } }
    @Override protected void tickDeath(){
        // Legacy onDeathUpdate runs on both sides.  The client-side counter is the renderer's
        // state contract; it is deliberately not a separately tracked value in 1.7.10.
        ++deathTicks;
        if(!level().isClientSide()) {
            if(!hasDeathWaypoint){communicate(TASK_INITIATE_RETREAT,null);hasDeathWaypoint=true;}
            if(deathTicks==90){
                for(Entity e:level().getEntities(this,getBoundingBox().inflate(8))){
                    // Keep the legacy target bug: every nearby Glyphid merely causes another
                    // application to the dying Nuclear Glyphid, rather than receiving the buffs.
                    if(e instanceof EntityGlyphid){
                        addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,20,6));
                        addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,300,1));
                    }
                }
            }
            if(deathTicks==100){
                ExplosionVnt vnt = new ExplosionVnt(level(), getX(), getY(), getZ(), 25, this);
                if(getSubtype()==TYPE_INFECTED){
                    for(int i=0;i<15+random.nextInt(6);i++){
                        float x=(i%2-.5F)*.5F,z=(i/2-.5F)*.5F;
                        EntityParasiteMaggot m=new EntityParasiteMaggot(ModEntityTypes.PARASITE_MAGGOT.get(),level());
                        m.moveTo(getX()+x,getY()+.5,getZ()+z,random.nextFloat()*360,0);
                        m.setDeltaMovement(x,0,z);
                        level().addFreshEntity(m);
                    }
                }else{
                    vnt.setBlockAllocator(new BlockAllocatorStandard(24));
                    vnt.setBlockProcessor(new BlockProcessorStandard()
                            .withBlockEffect(new BlockMutatorDebris(ModBlocks.VOLCANIC_LAVA_BLOCK.get()))
                            .setNoDrop());
                }
                vnt.setEntityProcessor(new EntityProcessorStandard())
                        .setPlayerProcessor(new PlayerProcessorStandard())
                        .explode();
                // The source's polaroid-ID override has no modern global-polaroid owner yet;
                // retain its independent one-percent branch here rather than inventing one.
                ParticleUtil.spawnMuke(level(),getX(),getY()+.5,getZ(),random.nextInt(100)==0);
                LegacySoundPlayer.playSoundEffect(level(),getX(),getY(),getZ(),"hbm:weapon.mukeExplosion",15,1);
                discard();
            } else if(deathTicks%10==0) LegacySoundPlayer.playSoundEffect(level(),getX(),getY(),getZ(),"hbm:weapon.fstbmbPing",5,1);
        } else if(deathTicks==100) {
            discard();
        }
    }
}
