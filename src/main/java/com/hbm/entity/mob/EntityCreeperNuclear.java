package com.hbm.entity.mob;

import api.hbm.entity.IRadiationImmune;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for the nuclear creeper entity.
 *
 * <p>The modern entity owns the runtime behavior; this subclass restores the
 * old FQCN and old radiation-immune marker for source migrations without
 * adding a second entity registration.
 */
@Deprecated(forRemoval = false)
public class EntityCreeperNuclear extends com.hbm.ntm.entity.mob.EntityCreeperNuclear implements IRadiationImmune {
    public EntityCreeperNuclear(EntityType<? extends com.hbm.ntm.entity.mob.EntityCreeperNuclear> type,
            Level level) {
        super(type, level);
    }

    public EntityCreeperNuclear(Level level) {
        this(ModEntityTypes.NUCLEAR_CREEPER.get(), level);
    }
}
