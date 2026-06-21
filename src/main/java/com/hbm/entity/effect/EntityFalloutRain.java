package com.hbm.entity.effect;

import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.level.Level;

/**
 * Old-package source migration facade for the fallout rain effect entity.
 */
@Deprecated(forRemoval = false)
public class EntityFalloutRain extends FalloutRainEntity {
    public EntityFalloutRain(Level level) {
        super(ModEntityTypes.FALLOUT_RAIN.get(), level);
    }

    public EntityFalloutRain(Level level, int scale) {
        this(level);
        setScale(scale);
    }

    public static EntityFalloutRain create(Level level, double x, double y, double z, int scale) {
        EntityFalloutRain entity = new EntityFalloutRain(level, scale);
        entity.setPos(x, y, z);
        return entity;
    }
}
