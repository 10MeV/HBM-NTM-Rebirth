package com.hbm.ntm.entity.cart;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class NtmPowderMinecartEntity extends NtmMinecartEntity {
    public NtmPowderMinecartEntity(EntityType<? extends NtmPowderMinecartEntity> type, Level level) {
        super(type, level);
    }

    public NtmPowderMinecartEntity(Level level, double x, double y, double z, NtmMinecartBase base) {
        super(ModEntityTypes.NTM_CART_POWDER.get(), level, x, y, z, base);
    }

    @Override
    public NtmMinecartType cartType() {
        return NtmMinecartType.POWDER;
    }
}
