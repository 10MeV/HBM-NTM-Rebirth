package com.hbm.entity.particle;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class EntityFogFX extends Entity {
    public EntityFogFX(EntityType<? extends EntityFogFX> type, Level level) { super(type, level); setNoGravity(true); }
    public EntityFogFX(Level level) { this(ModEntityTypes.NUCLEAR_FOG.get(), level); }
    @Override public void tick() { xo=getX();yo=getY();zo=getZ(); if(++tickCount>=400){discard();return;} setDeltaMovement(getDeltaMovement().scale(.96D)); }
    @Override protected void defineSynchedData() { }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { tickCount=tag.getInt("age"); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putInt("age",tickCount); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
