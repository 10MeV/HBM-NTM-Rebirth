package com.hbm.ntm.entity.cart;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class NtmSemtexMinecartEntity extends NtmMinecartEntity {
    public NtmSemtexMinecartEntity(EntityType<? extends NtmSemtexMinecartEntity> type, Level level) {
        super(type, level);
    }

    public NtmSemtexMinecartEntity(Level level, double x, double y, double z, NtmMinecartBase base) {
        super(ModEntityTypes.NTM_CART_SEMTEX.get(), level, x, y, z, base);
    }

    @Override
    public NtmMinecartType cartType() {
        return NtmMinecartType.SEMTEX;
    }
}
