package com.hbm.ntm.entity.cart;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class NtmEmptyMinecartEntity extends NtmMinecartEntity {
    public NtmEmptyMinecartEntity(EntityType<? extends NtmEmptyMinecartEntity> type, Level level) {
        super(type, level);
    }

    public NtmEmptyMinecartEntity(Level level, double x, double y, double z, NtmMinecartBase base) {
        super(ModEntityTypes.NTM_CART_ORE.get(), level, x, y, z, base);
    }

    @Override
    public NtmMinecartType cartType() {
        return NtmMinecartType.EMPTY;
    }
}
