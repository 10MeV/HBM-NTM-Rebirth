package com.hbm.entity.mob;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for the Tesla crab entity.
 */
@Deprecated(forRemoval = false)
public class EntityTeslaCrab extends com.hbm.ntm.entity.mob.EntityTeslaCrab {
    public EntityTeslaCrab(EntityType<? extends com.hbm.ntm.entity.mob.EntityTeslaCrab> type, Level level) {
        super(type, level);
    }

    public EntityTeslaCrab(Level level) {
        this(ModEntityTypes.TESLA_CRAB.get(), level);
    }
}
