package com.hbm.ntm.entity.effect;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class QuasarEntity extends BlackHoleEntity {
    public QuasarEntity(EntityType<? extends QuasarEntity> type, Level level) {
        super(type, level);
    }

    public QuasarEntity(Level level) {
        this(ModEntityTypes.QUASAR.get(), level);
    }

    public QuasarEntity(Level level, float size) {
        this(level);
        setSize(size);
    }
}
