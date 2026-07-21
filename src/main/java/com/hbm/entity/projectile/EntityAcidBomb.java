package com.hbm.entity.projectile;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Old-package source migration facade for the Glyphid acid-bomb projectile. */
@Deprecated(forRemoval = false)
public class EntityAcidBomb extends com.hbm.ntm.entity.projectile.EntityAcidBomb {
    public EntityAcidBomb(EntityType<? extends com.hbm.ntm.entity.projectile.EntityAcidBomb> type, Level level) {
        super(type, level);
    }

    public EntityAcidBomb(Level level) {
        super(ModEntityTypes.ACID_BOMB.get(), level);
    }

    public EntityAcidBomb(Level level, double x, double y, double z) {
        super(level, x, y, z);
    }
}
