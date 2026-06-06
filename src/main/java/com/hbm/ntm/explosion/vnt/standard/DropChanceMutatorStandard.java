package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.DropChanceMutator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DropChanceMutatorStandard implements DropChanceMutator {
    private final float chance;

    public DropChanceMutatorStandard(float chance) {
        this.chance = chance;
    }

    @Override
    public float mutateDropChance(ExplosionVnt explosion, BlockState state, BlockPos pos, float chance) {
        return this.chance;
    }
}
