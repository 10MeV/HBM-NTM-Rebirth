package com.hbm.entity.effect;

import com.hbm.ntm.entity.effect.MistEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Old-package source migration facade for the fluid mist effect entity.
 */
@Deprecated(forRemoval = false)
public class EntityMist extends MistEntity {
    public EntityMist(EntityType<? extends MistEntity> type, Level level) {
        super(type, level);
    }

    public EntityMist(Level level) {
        super(ModEntityTypes.MIST.get(), level);
    }

    public EntityMist setType(FluidType fluid) {
        return setFluidType(fluid);
    }

    @Override
    public EntityMist setFluidType(FluidType fluid) {
        super.setFluidType(fluid);
        return this;
    }

    @Override
    public EntityMist setArea(float width, float height) {
        super.setArea(width, height);
        return this;
    }

    @Override
    public EntityMist setDuration(int duration) {
        super.setDuration(duration);
        return this;
    }

    public static EntityMist create(Level level, double x, double y, double z, FluidType fluid,
            float width, float height, int duration) {
        EntityMist entity = new EntityMist(level);
        entity.setPos(x, y, z);
        entity.setType(fluid);
        entity.setArea(width, height);
        entity.setDuration(duration);
        return entity;
    }
}
