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
import java.util.function.Predicate;

public class BlockAllocatorBulkie implements BlockAllocator {
    private final double maximumResistance;
    private final int resolution;
    private final Predicate<BlockState> immuneBlock;

    public BlockAllocatorBulkie(double maximumResistance) {
        this(maximumResistance, 16);
    }

    public BlockAllocatorBulkie(double maximumResistance, int resolution) {
        this(maximumResistance, resolution, state -> false);
    }

    public BlockAllocatorBulkie(double maximumResistance, int resolution, Predicate<BlockState> immuneBlock) {
        this.maximumResistance = maximumResistance;
        this.resolution = Math.max(2, resolution);
        this.immuneBlock = immuneBlock;
    }

    @Override
    public Set<BlockPos> allocate(ExplosionVnt explosion, ServerLevel level, Vec3 position, float size) {
        Set<BlockPos> affectedBlocks = new LinkedHashSet<>();

        for (int i = 0; i < resolution; ++i) {
            for (int j = 0; j < resolution; ++j) {
                for (int k = 0; k < resolution; ++k) {
                    if (i == 0 || i == resolution - 1 || j == 0 || j == resolution - 1 || k == 0 || k == resolution - 1) {
                        allocateRay(explosion, level, position, i, j, k, affectedBlocks);
                    }
                }
            }
        }

        return affectedBlocks;
    }

    private void allocateRay(ExplosionVnt explosion, ServerLevel level, Vec3 position, int i, int j, int k, Set<BlockPos> affectedBlocks) {
        double dirX = (float) i / ((float) resolution - 1.0F) * 2.0F - 1.0F;
        double dirY = (float) j / ((float) resolution - 1.0F) * 2.0F - 1.0F;
        double dirZ = (float) k / ((float) resolution - 1.0F) * 2.0F - 1.0F;
        double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX /= length;
        dirY /= length;
        dirZ /= length;

        double currentX = position.x;
        double currentY = position.y;
        double currentZ = position.z;
        double distance = 0.0D;

        for (float stepSize = 0.3F; distance <= explosion.size();) {
            double deltaX = currentX - position.x;
            double deltaY = currentY - position.y;
            double deltaZ = currentZ - position.z;
            distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            BlockPos blockPos = BlockPos.containing(currentX, currentY, currentZ);
            if (!level.isInWorldBounds(blockPos)) {
                break;
            }

            BlockState state = level.getBlockState(blockPos);
            if (!state.isAir()) {
                FluidState fluidState = level.getFluidState(blockPos);
                Optional<Float> resistance = explosion.damageCalculator().getBlockExplosionResistance(
                        explosion.compat(), level, blockPos, state, fluidState);
                if (resistance.isPresent() && (maximumResistance < resistance.get() || immuneBlock.test(state))) {
                    break;
                }
            }

            if (explosion.damageCalculator().shouldBlockExplode(explosion.compat(), level, blockPos, state, explosion.size())) {
                affectedBlocks.add(blockPos.immutable());
            }

            currentX += dirX * stepSize;
            currentY += dirY * stepSize;
            currentZ += dirZ * stepSize;
        }
    }
}
