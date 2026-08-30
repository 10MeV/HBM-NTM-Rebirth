package com.hbm.ntm.entity.train;

import com.hbm.ntm.energy.HbmChargeableItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 1.7.10 EntityRailCarElectric charge slot and client-visible power counter. */
public abstract class LegacyRailCarElectricEntity extends LegacyRailCarRidableEntity {
    private static final EntityDataAccessor<Integer> POWER = SynchedEntityData.defineId(
            LegacyRailCarElectricEntity.class, EntityDataSerializers.INT);

    protected LegacyRailCarElectricEntity(EntityType<? extends LegacyRailCarElectricEntity> type, Level level) {
        super(type, level);
    }

    public abstract int getMaxPower();
    public abstract int getPowerConsumption();
    public boolean hasChargeSlot() { return false; }
    public int getChargeSlot() { return 0; }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(POWER, 0);
    }

    public int getPower() {
        return entityData.get(POWER);
    }

    public void setPower(int power) {
        entityData.set(POWER, Mth.clamp(power, 0, getMaxPower()));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("power", getPower());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("power")) {
            setPower(tag.getInt("power"));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || !hasChargeSlot()) {
            return;
        }
        ItemStack stack = getItem(getChargeSlot());
        if (stack.getItem() instanceof HbmChargeableItem battery) {
            long requested = Math.min((long) getMaxPower() - getPower(),
                    Math.min(battery.getDischargeRate(stack), battery.getCharge(stack)));
            if (requested > 0L) {
                long transferred = battery.dischargeBattery(stack, requested);
                if (transferred > 0L) {
                    setPower(getPower() + (int) Math.min(Integer.MAX_VALUE, transferred));
                }
            }
        } else if (stack.is(ModItems.BATTERY_CREATIVE.get())) {
            setPower(getMaxPower());
        }
    }
}
