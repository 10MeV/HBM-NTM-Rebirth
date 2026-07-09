package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Legacy 1.7.10 package bridge for the hot hazard block particle behavior.
 */
@Deprecated(forRemoval = false)
public class BlockHotHazard extends BlockHazard {
    public BlockHotHazard(String legacyName, BlockBehaviour.Properties properties, ExtDisplayEffect effect) {
        super(legacyName, properties);
        setDisplayEffect(effect);
    }

    @Override
    public BlockHotHazard makeBeaconable() {
        super.makeBeaconable();
        return this;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (level.isRainingAt(pos.above())) {
            level.addParticle(ParticleTypes.CLOUD,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + 1.0D,
                    pos.getZ() + random.nextFloat(),
                    0.0D, 0.0D, 0.0D);
        }

        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) {
                continue;
            }

            BlockPos neighborPos = pos.relative(direction);
            if (!level.getBlockState(neighborPos).getFluidState().is(FluidTags.WATER)) {
                continue;
            }

            double x = pos.getX() + 0.5D + direction.getStepX() + random.nextDouble() - 0.5D;
            double y = pos.getY() + 0.5D + direction.getStepY() + random.nextDouble() - 0.5D;
            double z = pos.getZ() + 0.5D + direction.getStepZ() + random.nextDouble() - 0.5D;

            if (direction.getStepX() != 0) {
                x = pos.getX() + 0.5D + direction.getStepX() * 0.5D
                        + random.nextDouble() * 0.125D * direction.getStepX();
            }
            if (direction.getStepY() != 0) {
                y = pos.getY() + 0.5D + direction.getStepY() * 0.5D
                        + random.nextDouble() * 0.125D * direction.getStepY();
            }
            if (direction.getStepZ() != 0) {
                z = pos.getZ() + 0.5D + direction.getStepZ() * 0.5D
                        + random.nextDouble() * 0.125D * direction.getStepZ();
            }

            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
