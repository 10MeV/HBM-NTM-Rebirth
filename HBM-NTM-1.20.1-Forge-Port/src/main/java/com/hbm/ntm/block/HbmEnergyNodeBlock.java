package com.hbm.ntm.block;

import com.hbm.ntm.energy.HbmEnergyConnectionUtil;
import com.hbm.ntm.energy.HbmEnergyConnectorBlock;
import com.hbm.ntm.energy.HbmEnergyNodeHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public abstract class HbmEnergyNodeBlock extends BaseEntityBlock implements HbmEnergyConnectorBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    protected HbmEnergyNodeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getConnectionState(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(oldState.getBlock())) {
            updateConnectionState(level, pos);
            updateNeighborConnectionStates(level, pos);
            refreshNode(level, pos);
            refreshNeighborNodes(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        updateConnectionState(level, pos);
        refreshNode(level, pos);
        refreshNeighborNodes(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            removeNode(level, pos);
            updateNeighborConnectionStates(level, pos);
            refreshNeighborNodes(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean canConnectEnergy(BlockGetter level, BlockPos pos, @Nullable Direction side) {
        return side != null;
    }

    public void updateEnergyConnectionGraph(Level level, BlockPos pos) {
        updateConnectionState(level, pos);
        updateNeighborConnectionStates(level, pos);
        refreshNode(level, pos);
        refreshNeighborNodes(level, pos);
    }

    protected BlockState getConnectionState(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState result = state;
        for (Direction direction : Direction.values()) {
            result = result.setValue(propertyFor(direction), HbmEnergyConnectionUtil.canConnect(level, pos, this, direction));
        }
        return result;
    }

    protected void updateConnectionState(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.is(this)) {
            return;
        }
        BlockState updated = getConnectionState(state, level, pos);
        if (!updated.equals(state)) {
            level.setBlock(pos, updated, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
        }
    }

    protected void refreshNode(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HbmEnergyNodeHost host) {
            host.refreshEnergyNode();
        }
    }

    protected void removeNode(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HbmEnergyNodeHost host) {
            host.removeEnergyNode();
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

    protected void updateNeighborConnectionStates(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (level.getBlockState(neighborPos).getBlock() instanceof HbmEnergyNodeBlock nodeBlock) {
                nodeBlock.updateConnectionState(level, neighborPos);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    protected static BooleanProperty propertyFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }
}

