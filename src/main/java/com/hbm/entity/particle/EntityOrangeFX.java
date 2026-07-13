package com.hbm.entity.particle;

import com.hbm.ntm.explosion.ExplosionChaos;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class EntityOrangeFX extends com.hbm.ntm.entity.effect.LegacyVentCloudEntity {
    public EntityOrangeFX(EntityType<? extends EntityOrangeFX> type, Level level) { super(type, level, 900, 300); }
    public EntityOrangeFX(Level level) { super(ModEntityTypes.ORANGE_FX.get(), level, 900, 300); }
    public EntityOrangeFX(Level level, double x, double y, double z, double mx, double my, double mz) { super(ModEntityTypes.ORANGE_FX.get(), level, x, y, z, mx, my, mz, 900, 300); }
    @Override public boolean isChlorine() { return false; }
    @Override public boolean isPink() { return false; }
    @Override public void tick() {
        xo=getX(); yo=getY(); zo=getZ();
        if (getMaxAge() < 900) setMaxAge(900 + random.nextInt(301));
        if (!level().isClientSide && random.nextInt(50)==0) ExplosionChaos.poison(level(), (int)getX(), (int)getY(), (int)getZ(), 2);
        if (++tickCount >= getMaxAge()) { discard(); return; }
        Vec3 motion=getDeltaMovement().scale(.86D).add(0,-.1D,0);
        for(int i=0;i<4;i++) { Vec3 step=motion.scale(.25D); setPos(getX()+step.x,getY()+step.y,getZ()+step.z); BlockPos hit=BlockPos.containing(getX(),getY(),getZ()); if(!level().getBlockState(hit).isAir()) { discard(); if(!level().isClientSide) for(int x=-1;x<2;x++)for(int y=-1;y<2;y++)for(int z=-1;z<2;z++){BlockPos p=hit.offset(x,y,z); if(level().getBlockState(p).is(Blocks.GRASS_BLOCK)) level().setBlock(p,Blocks.DIRT.defaultBlockState(),3); else ExplosionNukeGeneric.solinium(level(),p);} } }
        setDeltaMovement(motion);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { discard(); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
}
