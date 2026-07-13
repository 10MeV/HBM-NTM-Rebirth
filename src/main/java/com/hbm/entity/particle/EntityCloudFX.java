package com.hbm.entity.particle;

import com.hbm.ntm.entity.effect.LegacyVentCloudEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityCloudFX extends LegacyVentCloudEntity {
    public EntityCloudFX(EntityType<? extends EntityCloudFX> type, Level level) { super(type, level, 900, 300); }
    public EntityCloudFX(Level level) { super(ModEntityTypes.CLOUD_FX.get(), level, 900, 300); }
    public EntityCloudFX(Level level, double x, double y, double z, double mx, double my, double mz) {
        super(ModEntityTypes.CLOUD_FX.get(), level, x, y, z, mx, my, mz, 900, 300);
    }
    @Override public boolean isChlorine() { return false; }
    @Override public boolean isPink() { return false; }
}
