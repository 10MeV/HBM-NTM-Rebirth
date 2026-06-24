package com.hbm.ntm.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;
import java.util.Set;

public final class HbmFluidConnectionUtil {
    public static Set<Direction> collectNodeConnections(BlockGetter level, BlockPos pos, FluidType type, HbmFluidConnector connector) {
        EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (canConnect(level, pos, type, connector, direction)) {
                connections.add(direction);
            }
        }
        return connections;
    }

    public static boolean canConnect(BlockGetter level, BlockPos pos, FluidType type, HbmFluidConnector connector, Direction direction) {
        if (level == null || pos == null || connector == null || direction == null || !connector.canConnectFluid(type, direction)) {
            return false;
        }

        BlockPos neighborPos = pos.relative(direction);
        Direction neighborSide = direction.getOpposite();
        BlockEntity neighborEntity = level.getBlockEntity(neighborPos);
        if (neighborEntity instanceof HbmFluidConnector neighborConnector
                && neighborConnector.canConnectFluid(type, neighborSide)) {
            return true;
        }

        if (level.getBlockState(neighborPos).getBlock() instanceof HbmFluidConnectorBlock connectorBlock) {
            return connectorBlock.canConnectFluid(level, neighborPos, type, neighborSide);
        }

        if (HbmFluidUtil.hasRemoteVisualPortConnection(level, pos, type, direction)) {
            return true;
        }

        return false;
    }

    private HbmFluidConnectionUtil() {
    }
}
