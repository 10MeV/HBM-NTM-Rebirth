package com.hbm.entity.mob;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for the Meltdown Elemental.
 */
@Deprecated(forRemoval = false)
public class EntityRADBeast extends com.hbm.ntm.entity.mob.EntityRADBeast implements api.hbm.entity.IRadiationImmune {
    public EntityRADBeast(EntityType<? extends com.hbm.ntm.entity.mob.EntityRADBeast> type, Level level) {
        super(type, level);
    }

    public EntityRADBeast(Level level) {
        this(ModEntityTypes.RAD_BEAST.get(), level);
    }
}
