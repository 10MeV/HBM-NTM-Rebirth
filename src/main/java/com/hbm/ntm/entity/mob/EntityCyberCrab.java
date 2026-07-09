package com.hbm.ntm.entity.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class EntityCyberCrab extends com.hbm.entity.mob.EntityCyberCrab {
    public EntityCyberCrab(EntityType<? extends EntityCyberCrab> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return com.hbm.entity.mob.EntityCyberCrab.createAttributes();
    }
}
