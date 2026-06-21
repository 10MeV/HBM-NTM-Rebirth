package com.hbm.ntm.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface PAParticleUser {
    boolean canParticleEnter(PASourceBlockEntity.Particle particle, Direction dir, BlockPos entryPos);

    void onParticleEnter(PASourceBlockEntity.Particle particle, Direction dir);

    BlockPos getParticleExitPos(PASourceBlockEntity.Particle particle);
}
