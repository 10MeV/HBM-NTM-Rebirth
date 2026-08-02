package com.hbm.ntm.entity.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Modern networked carrier for 1.7.10 {@code EntityItemBuoyant}. */
public final class BuoyantItemEntity extends ItemEntity {
    public BuoyantItemEntity(EntityType<? extends BuoyantItemEntity> type, Level level) {
        super(type, level);
    }

    public BuoyantItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(com.hbm.ntm.registry.ModEntityTypes.BUOYANT_ITEM.get(), level);
        setPos(x, y, z);
        setItem(stack);
    }

    @Override
    public void tick() {
        BlockPos below = BlockPos.containing(getX(), getY() - 0.0625D, getZ());
        if (level().getFluidState(below).is(FluidTags.WATER) && !level().getFluidState(below).isSource()) {
            setDeltaMovement(getDeltaMovement().add(0.0D, 0.045D, 0.0D));
        }
        super.tick();
    }
}
