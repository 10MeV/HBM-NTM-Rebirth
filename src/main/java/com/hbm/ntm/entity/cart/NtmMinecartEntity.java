package com.hbm.ntm.entity.cart;

import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public abstract class NtmMinecartEntity extends Minecart {
    private static final EntityDataAccessor<Integer> DATA_BASE =
            SynchedEntityData.defineId(NtmMinecartEntity.class, EntityDataSerializers.INT);

    protected NtmMinecartEntity(EntityType<? extends NtmMinecartEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    protected NtmMinecartEntity(EntityType<? extends NtmMinecartEntity> type, Level level,
            double x, double y, double z, NtmMinecartBase base) {
        this(type, level);
        setPos(x, y, z);
        xo = x;
        yo = y;
        zo = z;
        setBase(base);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_BASE, NtmMinecartBase.STEEL.legacyId());
    }

    public void setBase(NtmMinecartBase base) {
        entityData.set(DATA_BASE, base.legacyId());
    }

    public NtmMinecartBase getBase() {
        return NtmMinecartBase.byLegacyId(entityData.get(DATA_BASE));
    }

    public abstract NtmMinecartType cartType();

    public ItemStack getCartItem() {
        return getBase().cartStack(cartType());
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.RIDEABLE;
    }

    @Override
    protected Item getDropItem() {
        return getBase().cartItem(cartType());
    }

    @Override
    public void destroy(DamageSource damageSource) {
        kill();
        if (level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack stack = getCartItem();
            if (hasCustomName()) {
                stack.setHoverName(getCustomName());
            }
            spawnAtLocation(stack);
        }
    }

    @Override
    public ItemStack getPickResult() {
        return getCartItem();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("base", getBase().legacyId());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("base", net.minecraft.nbt.Tag.TAG_INT)) {
            setBase(NtmMinecartBase.byLegacyId(tag.getInt("base")));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
