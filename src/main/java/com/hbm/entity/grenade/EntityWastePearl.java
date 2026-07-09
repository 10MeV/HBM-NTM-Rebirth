package com.hbm.entity.grenade;

import com.hbm.ntm.entity.projectile.WastePearlEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Old-package source migration facade for the waste pearl fallout/radon projectile.
 */
@Deprecated(forRemoval = false)
public class EntityWastePearl extends WastePearlEntity {
    public EntityWastePearl(EntityType<? extends WastePearlEntity> type, Level level) {
        super(type, level);
    }

    public EntityWastePearl(Level level) {
        super(ModEntityTypes.WASTE_PEARL.get(), level);
    }

    public EntityWastePearl(Level level, LivingEntity thrower) {
        super(level, thrower);
    }

    public EntityWastePearl(Level level, double x, double y, double z) {
        super(level, x, y, z);
    }
}
