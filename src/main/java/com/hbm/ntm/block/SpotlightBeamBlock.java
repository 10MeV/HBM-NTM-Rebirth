package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.SpotlightBeamBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SpotlightBeamBlock extends LegacyBeamBaseBlock {
    public SpotlightBeamBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof SpotlightBeamBlockEntity beam) {
            for (Direction direction : beam.directions()) {
                LegacySpotlightBlock.unpropagateBeam(level, pos, direction);
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean moving) {
        if (level.isClientSide || neighborBlock instanceof SpotlightBeamBlock) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof SpotlightBeamBlockEntity beam) {
            for (Direction direction : beam.directions()) {
                LegacySpotlightBlock.backPropagate(level, pos, direction);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpotlightBeamBlockEntity(pos, state);
    }
}
