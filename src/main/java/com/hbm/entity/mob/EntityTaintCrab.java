package com.hbm.entity.mob;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for the taint crab entity.
 */
@Deprecated(forRemoval = false)
public class EntityTaintCrab extends com.hbm.ntm.entity.mob.EntityTaintCrab {
    public EntityTaintCrab(EntityType<? extends com.hbm.ntm.entity.mob.EntityTaintCrab> type, Level level) {
        super(type, level);
    }

    public EntityTaintCrab(Level level) {
        this(ModEntityTypes.TAINT_CRAB.get(), level);
    }
}
