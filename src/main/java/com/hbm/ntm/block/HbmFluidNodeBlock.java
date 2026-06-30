package com.hbm.ntm.block;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidConnectorBlock;
import com.hbm.ntm.fluid.HbmFluidNodeHost;
import com.hbm.ntm.client.ClientGeometryInvalidationBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public abstract class HbmFluidNodeBlock extends BaseEntityBlock implements HbmFluidConnectorBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    protected HbmFluidNodeBlock(Properties properties) {
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
        if (level.isClientSide) {
            ClientGeometryInvalidationBridge.scheduleWithNeighbors(pos);
        }
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
        if (level.isClientSide) {
            ClientGeometryInvalidationBridge.schedule(pos);
        }
        updateConnectionState(level, pos);
        refreshNode(level, pos);
        refreshNeighborNodes(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level.isClientSide) {
            ClientGeometryInvalidationBridge.scheduleWithNeighbors(pos);
        }
        if (!state.is(newState.getBlock())) {
            removeNode(level, pos);
            updateNeighborConnectionStates(level, pos);
            refreshNeighborNodes(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean canConnectFluid(BlockGetter level, BlockPos pos, FluidType type, @Nullable Direction side) {
        return side != null
                && level.getBlockEntity(pos) instanceof com.hbm.ntm.fluid.HbmFluidConnector connector
                && connector.canConnectFluid(type, side);
    }

    protected BlockState getConnectionState(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState result = state;
        FluidType type = getConnectionFluidType(level, pos);
        for (Direction direction : Direction.values()) {
            result = result.setValue(propertyFor(direction),
                    HbmFluidConnectionUtil.canConnect(level, pos, type, getFluidConnector(level, pos), direction));
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

    public void updateFluidConnectionGraph(Level level, BlockPos pos) {
        updateConnectionState(level, pos);
        updateNeighborConnectionStates(level, pos);
        refreshNode(level, pos);
        refreshNeighborNodes(level, pos);
    }

    protected void refreshNode(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HbmFluidNodeHost host) {
            host.refreshFluidNode();
        }
    }

    protected void removeNode(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HbmFluidNodeHost host) {
            host.removeFluidNode();
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
            if (level.getBlockState(neighborPos).getBlock() instanceof HbmFluidNodeBlock nodeBlock) {
                nodeBlock.updateConnectionState(level, neighborPos);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    protected FluidType getConnectionFluidType(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FluidTypedBlockEntity typed ? typed.getFluidType() : null;
    }

    protected com.hbm.ntm.fluid.HbmFluidConnector getFluidConnector(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof com.hbm.ntm.fluid.HbmFluidConnector connector ? connector : null;
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

    public interface FluidTypedBlockEntity {
        FluidType getFluidType();
    }
}

