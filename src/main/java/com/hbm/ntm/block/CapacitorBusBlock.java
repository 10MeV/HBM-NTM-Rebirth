package com.hbm.ntm.block;

import com.hbm.ntm.energy.IEnergyConnectorBlock;
import com.hbm.ntm.util.ConnectionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CapacitorBusBlock extends DirectionalBlock
        implements IEnergyConnectorBlock, ConnectionUtil.EnergyConnectorBlock {
    public CapacitorBusBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean canConnect(BlockGetter level, BlockPos pos, @Nullable Direction side) {
        return side != null && level.getBlockState(pos).getValue(FACING) == side;
    }

    @Override
    public boolean canConnectEnergy(BlockGetter level, BlockPos pos, Direction side) {
        return canConnect(level, pos, side);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }
}
