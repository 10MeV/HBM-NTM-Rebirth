package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyNuclearWasteBlock extends RadiatingHazardBlock {
    public LegacyNuclearWasteBlock(String legacyName, Properties properties) {
        super(legacyName, properties);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction direction = Direction.values()[random.nextInt(6)];
        BlockPos target = pos.relative(direction);
        if (random.nextInt(2) == 0 && level.isEmptyBlock(target)) {
            level.setBlock(target, ModBlocks.GAS_RADON_DENSE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        super.tick(state, level, pos, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            if (!level.isEmptyBlock(target)) {
                continue;
            }

            double x = pos.getX() + 0.5D + direction.getStepX() + random.nextDouble() * 3.0D - 1.5D;
            double y = pos.getY() + 0.5D + direction.getStepY() + random.nextDouble() * 3.0D - 1.5D;
            double z = pos.getZ() + 0.5D + direction.getStepZ() + random.nextDouble() * 3.0D - 1.5D;

            if (direction.getStepX() != 0) {
                x = pos.getX() + 0.5D + direction.getStepX() * 0.5D + random.nextDouble() * direction.getStepX();
            }
            if (direction.getStepY() != 0) {
                y = pos.getY() + 0.5D + direction.getStepY() * 0.5D + random.nextDouble() * direction.getStepY();
            }
            if (direction.getStepZ() != 0) {
                z = pos.getZ() + 0.5D + direction.getStepZ() * 0.5D + random.nextDouble() * direction.getStepZ();
            }

            level.addParticle(ModParticleTypes.RADIATION_FOG.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
