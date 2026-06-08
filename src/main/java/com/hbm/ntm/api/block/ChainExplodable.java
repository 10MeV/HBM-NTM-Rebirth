package com.hbm.ntm.api.block;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface ChainExplodable {
    void explodeEntity(Level level, Vec3 position, @Nullable Entity source);
}
