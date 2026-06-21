package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SawbladeEntity extends MachinePartProjectileEntity {
    public SawbladeEntity(net.minecraft.world.entity.EntityType<? extends SawbladeEntity> type, Level level) {
        super(type, level);
    }

    public SawbladeEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.SAWBLADE.get(), level, x, y, z);
    }

    @Override
    protected ItemStack getPickupStack() {
        return new ItemStack(ModItems.SAWBLADE.get());
    }
}
