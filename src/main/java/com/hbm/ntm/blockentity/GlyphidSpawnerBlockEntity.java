package com.hbm.ntm.blockentity;

import com.hbm.config.MobConfig;
import com.hbm.entity.mob.glyphid.*;
import com.hbm.ntm.block.LegacyGlyphidSpawnerBlock;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Exact eight-carrier Glyphid swarm runtime. */
public class GlyphidSpawnerBlockEntity extends BlockEntity {
    private boolean initialSpawn = true;
    private static final List<EntityType<? extends EntityGlyphid>> MAP = List.of(ModEntityTypes.GLYPHID.get(),ModEntityTypes.GLYPHID_BOMBARDIER.get(),ModEntityTypes.GLYPHID_BRAWLER.get(),ModEntityTypes.GLYPHID_DIGGER.get(),ModEntityTypes.GLYPHID_BLASTER.get(),ModEntityTypes.GLYPHID_BEHEMOTH.get(),ModEntityTypes.GLYPHID_BRENDA.get(),ModEntityTypes.GLYPHID_NUCLEAR.get());
    public GlyphidSpawnerBlockEntity(BlockPos p, BlockState s){super(ModBlockEntities.GLYPHID_SPAWNER.get(),p,s);}
    public static void serverTick(Level l,BlockPos p,BlockState s,GlyphidSpawnerBlockEntity be){
        if(l.isClientSide||l.getDifficulty().getId()==0)return;
        if(!(l instanceof ServerLevel serverLevel))return;
        MobConfig.syncFromModern();
        if(!be.initialSpawn&&l.getGameTime()%MobConfig.swarmCooldown!=0)return;
        be.initialSpawn=false;
        // TileEntityGlpyhidSpawner persists this flag in its NBT on every legacy
        // chunk save.  A modern BlockEntity must mark itself dirty here or a
        // first swarm can be replayed after a restart.
        be.setChanged();
        int globalGlyphids=0;
        for(net.minecraft.world.entity.Entity entity:serverLevel.getAllEntities()){
            if(entity instanceof EntityGlyphid && ++globalGlyphids>=MobConfig.spawnMax)return;
        }
        int subtype=s.getValue(LegacyGlyphidSpawnerBlock.VARIANT);
        if(subtype!=EntityGlyphid.TYPE_RADIOACTIVE&&l.getEntitiesOfClass(EntityGlyphid.class,new net.minecraft.world.phys.AABB(p.offset(-5,1,-5),p.offset(6,7,6))).size()>3)return;
        float soot=PollutionManager.getPollution(l,p,PollutionType.SOOT);
        for(EntityGlyphid glyphid:be.createSwarm(l,soot,subtype))be.trySpawn(l,p,glyphid);
        if(subtype!=EntityGlyphid.TYPE_RADIOACTIVE&&l.random.nextInt(MobConfig.scoutSwarmSpawnChance+1)==0&&soot>=MobConfig.scoutThreshold){EntityGlyphidScout scout=ModEntityTypes.GLYPHID_SCOUT.get().create(l);if(scout!=null){scout.setSubtype(subtype==EntityGlyphid.TYPE_INFECTED?EntityGlyphid.TYPE_INFECTED:EntityGlyphid.TYPE_NORMAL);be.trySpawn(l,p,scout);}}
    }
    private List<EntityGlyphid> createSwarm(Level l,float soot,int subtype){
        Random random=new Random();
        List<EntityGlyphid> swarm=new ArrayList<>();
        int swarmAmount=(int)Math.min(MobConfig.baseSwarmSize*Math.max(MobConfig.swarmScalingMult*(soot/MobConfig.sootStep),1),10);
        for(int cap=100;swarm.size()<=swarmAmount&&cap>=0;cap--){
            for(int i=0;i<MAP.size();i++){
                int[] c=chanceAt(i);
                int chance=(int)(c[0]+(c[1]-c[1]/Math.max((soot+1)/3,1)));
                if(soot>=c[2]&&random.nextInt(100)<=chance){
                    EntityGlyphid glyphid=MAP.get(i).create(l);
                    if(glyphid!=null){glyphid.setSubtype(subtype);swarm.add(glyphid);}
                }
            }
        }
        return swarm;
    }
    private static int[] chanceAt(int index){return switch(index){case 0->MobConfig.glyphidChance;case 1->MobConfig.bombardierChance;case 2->MobConfig.brawlerChance;case 3->MobConfig.diggerChance;case 4->MobConfig.blasterChance;case 5->MobConfig.behemothChance;case 6->MobConfig.brendaChance;case 7->MobConfig.johnsonChance;default->throw new IllegalArgumentException("Unknown Glyphid spawn-map index: "+index);};}
    private void trySpawn(Level l,BlockPos p,EntityGlyphid g){double x=g.getRandom().nextGaussian()*3,z=g.getRandom().nextGaussian()*3;for(int i=0;i<7;i++){g.moveTo(p.getX()+.5+x,p.getY()-2+i,p.getZ()+.5+z,l.random.nextFloat()*360,0);if(g.getCanSpawnHere()){l.addFreshEntity(g);return;}}}
    @Override protected void saveAdditional(CompoundTag t){super.saveAdditional(t);t.putBoolean("initialSpawn",initialSpawn);} @Override public void load(CompoundTag t){super.load(t);initialSpawn=t.getBoolean("initialSpawn");}
}
