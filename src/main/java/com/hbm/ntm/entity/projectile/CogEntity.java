package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CogEntity extends MachinePartProjectileEntity {
    public CogEntity(net.minecraft.world.entity.EntityType<? extends CogEntity> type, Level level) {
        super(type, level);
    }

    public CogEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.COG.get(), level, x, y, z);
    }

    @Override
    protected ItemStack getPickupStack() {
        return new ItemStack(getMeta() == 1 ? ModItems.GEAR_LARGE_STEEL.get() : ModItems.GEAR_LARGE.get());
    }
}
