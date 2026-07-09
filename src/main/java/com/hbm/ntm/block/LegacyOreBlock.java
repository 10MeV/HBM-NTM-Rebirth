package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class LegacyOreBlock extends RadiatingHazardBlock {
    private final String legacyName;

    public LegacyOreBlock(String legacyName, Properties properties) {
        super(legacyName, properties);
        this.legacyName = legacyName;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide || !"ore_oil".equals(legacyName)) {
            return;
        }

        Block oilEmpty = ModBlocks.legacyBlock("ore_oil_empty").get();
        BlockPos below = pos.below();
        if (level.getBlockState(below).is(oilEmpty)) {
            level.setBlock(pos, oilEmpty.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(below, state, Block.UPDATE_ALL);
        }
    }
}
