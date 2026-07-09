package com.hbm.ntm.block;

import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class LegacyImpactDirtBlock extends Block {
    public LegacyImpactDirtBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    public void neighborChanged(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, Block block,
            BlockPos neighborPos, boolean moving) {
        super.neighborChanged(state, level, pos, block, neighborPos, moving);
        for (BlockPos target : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(target).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        TomImpactSavedData data = TomImpactSavedData.forLevel(level);
        BlockPos above = pos.above();
        int light = Math.max(level.getBrightness(LightLayer.BLOCK, above),
                (int) (level.getMaxLocalRawBrightness(above) * (1.0F - data.dust())));
        if (light >= 9 && data.fire() == 0.0F) {
            level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
