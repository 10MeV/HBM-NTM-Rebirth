package com.hbm.ntm.explosion.vnt.interfaces;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

@FunctionalInterface
public interface ExplosionEffect {
    void doEffect(ExplosionVnt explosion, ServerLevel level, Vec3 position, float size, Set<BlockPos> affectedBlocks);
}
