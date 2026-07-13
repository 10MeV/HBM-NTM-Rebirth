package com.hbm.entity.particle;

import com.hbm.ntm.entity.effect.LegacyVentCloudEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityPinkCloudFX extends LegacyVentCloudEntity {
    public EntityPinkCloudFX(EntityType<? extends EntityPinkCloudFX> type, Level level) { super(type, level, 900, 300); }
    public EntityPinkCloudFX(Level level) { super(ModEntityTypes.PINK_CLOUD_FX.get(), level, 900, 300); }
    public EntityPinkCloudFX(Level level, double x, double y, double z, double mx, double my, double mz) {
        super(ModEntityTypes.PINK_CLOUD_FX.get(), level, x, y, z, mx, my, mz, 900, 300);
    }
    @Override public boolean isChlorine() { return false; }
    @Override public boolean isPink() { return true; }
}
