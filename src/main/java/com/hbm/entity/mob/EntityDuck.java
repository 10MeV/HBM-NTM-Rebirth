package com.hbm.entity.mob;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for the ordinary Duck entity.
 *
 * <p>Quackos remains intentionally excluded as boss content; this bridge only
 * restores the old Duck FQCN while delegating to the modern Duck runtime.
 */
@Deprecated(forRemoval = false)
public class EntityDuck extends com.hbm.ntm.entity.mob.EntityDuck {
    public EntityDuck(EntityType<? extends com.hbm.ntm.entity.mob.EntityDuck> type, Level level) {
        super(type, level);
    }

    public EntityDuck(Level level) {
        this(ModEntityTypes.DUCK.get(), level);
    }
}
