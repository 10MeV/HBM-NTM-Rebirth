package com.hbm.ntm.explosion.vnt.interfaces;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

@FunctionalInterface
public interface BlockProcessor {
    void process(ExplosionVnt explosion, ServerLevel level, Vec3 position, Set<BlockPos> affectedBlocks);
}
