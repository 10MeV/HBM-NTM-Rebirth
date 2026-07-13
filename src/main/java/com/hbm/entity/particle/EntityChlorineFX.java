package com.hbm.entity.particle;

import com.hbm.ntm.entity.effect.LegacyVentCloudEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityChlorineFX extends LegacyVentCloudEntity {
    public EntityChlorineFX(EntityType<? extends EntityChlorineFX> type, Level level) { super(type, level, 700, 100); }
    public EntityChlorineFX(Level level) { super(ModEntityTypes.CHLORINE_FX.get(), level, 700, 100); }
    public EntityChlorineFX(Level level, double x, double y, double z, double mx, double my, double mz) {
        super(ModEntityTypes.CHLORINE_FX.get(), level, x, y, z, mx, my, mz, 700, 100);
    }
    @Override public boolean isChlorine() { return true; }
    @Override public boolean isPink() { return false; }
}
