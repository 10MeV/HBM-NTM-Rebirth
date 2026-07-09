package com.hbm.blocks.generic;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Legacy 1.7.10 package bridge for nuclear waste blocks.
 */
@Deprecated(forRemoval = false)
public class BlockNuclearWaste extends BlockHazard {
    public BlockNuclearWaste(String legacyName, BlockBehaviour.Properties properties) {
        super(legacyName, properties);
        setDisplayEffect(ExtDisplayEffect.RADFOG);
    }

    @Override
    public BlockNuclearWaste makeBeaconable() {
        super.makeBeaconable();
        return this;
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
}
