package com.hbm.ntm.block;

import com.hbm.ntm.radiation.ChunkRadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class RadiatingFallingBlock extends FallingBlock {
    private final float chunkRadiationPerTick;

    public RadiatingFallingBlock(Properties properties, float chunkRadiationPerTick) {
        super(properties);
        this.chunkRadiationPerTick = chunkRadiationPerTick;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getBlock() != oldState.getBlock() && chunkRadiationPerTick > 0.0F) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (chunkRadiationPerTick > 0.0F) {
            ChunkRadiationManager.incrementRadiation(level, pos, chunkRadiationPerTick);
            level.scheduleTick(pos, this, 20);
        }
        super.tick(state, level, pos, random);
    }
}
