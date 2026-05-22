package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockAllocator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class BlockAllocatorWater implements BlockAllocator {
    private final int resolution;

    public BlockAllocatorWater(int resolution) {
        this.resolution = Math.max(2, resolution);
    }

    @Override
    public Set<BlockPos> allocate(ExplosionVnt explosion, ServerLevel level, Vec3 position, float size) {
        Set<BlockPos> affectedBlocks = new LinkedHashSet<>();

        for (int i = 0; i < resolution; ++i) {
            for (int j = 0; j < resolution; ++j) {
                for (int k = 0; k < resolution; ++k) {
                    if (i == 0 || i == resolution - 1 || j == 0 || j == resolution - 1 || k == 0 || k == resolution - 1) {
                        allocateRay(explosion, level, position, size, i, j, k, affectedBlocks);
                    }
                }
            }
        }

        return affectedBlocks;
    }

    private void allocateRay(ExplosionVnt explosion, ServerLevel level, Vec3 position, float size, int i, int j, int k,
            Set<BlockPos> affectedBlocks) {
        double dirX = (float) i / ((float) resolution - 1.0F) * 2.0F - 1.0F;
        double dirY = (float) j / ((float) resolution - 1.0F) * 2.0F - 1.0F;
        double dirZ = (float) k / ((float) resolution - 1.0F) * 2.0F - 1.0F;
        double distance = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX /= distance;
        dirY /= distance;
        dirZ /= distance;

        float powerRemaining = size * (0.7F + level.random.nextFloat() * 0.6F);
        double currentX = position.x;
        double currentY = position.y;
        double currentZ = position.z;

        for (float stepSize = 0.3F; powerRemaining > 0.0F; powerRemaining -= stepSize * 0.75F) {
            BlockPos blockPos = BlockPos.containing(currentX, currentY, currentZ);
            if (!level.isInWorldBounds(blockPos)) {
                break;
            }

            BlockState state = level.getBlockState(blockPos);
            FluidState fluidState = level.getFluidState(blockPos);
            boolean liquid = !fluidState.isEmpty();
            if (!state.isAir() && !liquid) {
                Optional<Float> resistance = explosion.damageCalculator().getBlockExplosionResistance(
                        explosion.compat(), level, blockPos, state, fluidState);
                if (resistance.isPresent()) {
                    powerRemaining -= (resistance.get() + 0.3F) * stepSize;
                }
            }

            if (powerRemaining > 0.0F && !liquid && explosion.damageCalculator().shouldBlockExplode(
                    explosion.compat(), level, blockPos, state, powerRemaining)) {
                affectedBlocks.add(blockPos.immutable());
            }

            currentX += dirX * stepSize;
            currentY += dirY * stepSize;
            currentZ += dirZ * stepSize;
        }
    }
}
