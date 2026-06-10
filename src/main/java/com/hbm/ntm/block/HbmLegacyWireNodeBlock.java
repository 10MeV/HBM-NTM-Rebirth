package com.hbm.ntm.block;

import com.hbm.ntm.energy.HbmEnergyNodeHost;
import com.hbm.ntm.energy.HbmLegacyWireNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public abstract class HbmLegacyWireNodeBlock extends BaseEntityBlock {
    protected HbmLegacyWireNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(oldState.getBlock())) {
            refreshNode(level, pos);
            refreshNeighborNodes(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        refreshNode(level, pos);
        refreshNeighborNodes(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof HbmLegacyWireNode wireNode) {
                wireNode.disconnectAllWires();
            } else if (blockEntity instanceof HbmEnergyNodeHost host) {
                host.removeEnergyNode();
            }
            refreshNeighborNodes(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected void refreshNode(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HbmEnergyNodeHost host) {
            host.refreshEnergyNode();
        }
    }

    protected void refreshNeighborNodes(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        for (Direction direction : Direction.values()) {
            refreshNode(level, pos.relative(direction));
        }
    }
}
