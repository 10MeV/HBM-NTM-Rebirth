package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;
import java.util.Set;

public final class HbmEnergyConnectionUtil {
    private HbmEnergyConnectionUtil() {
    }

    public static Set<Direction> collectNodeConnections(BlockGetter level, BlockPos pos, HbmEnergyConnector connector) {
        EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (canConnect(level, pos, connector, direction)) {
                connections.add(direction);
            }
        }
        return connections;
    }

    public static Set<Direction> collectBlockConnections(BlockGetter level, BlockPos pos, HbmEnergyConnectorBlock connector) {
        EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (canConnect(level, pos, connector, direction)) {
                connections.add(direction);
            }
        }
        return connections;
    }

    public static boolean canConnect(BlockGetter level, BlockPos pos, HbmEnergyConnector connector, Direction direction) {
        if (level == null || pos == null || connector == null || direction == null || !connector.canConnectEnergy(direction)) {
            return false;
        }

        BlockPos neighborPos = pos.relative(direction);
        Direction neighborSide = direction.getOpposite();
        BlockEntity neighborEntity = level.getBlockEntity(neighborPos);
        if (neighborEntity instanceof HbmEnergyConnector neighborConnector
                && neighborConnector.canConnectEnergy(neighborSide)) {
            return true;
        }

        if (level.getBlockState(neighborPos).getBlock() instanceof HbmEnergyConnectorBlock connectorBlock) {
            return connectorBlock.canConnectEnergy(level, neighborPos, neighborSide);
        }

        return false;
    }

    public static boolean canConnect(BlockGetter level, BlockPos pos, HbmEnergyConnectorBlock connector, Direction direction) {
        if (level == null || pos == null || connector == null || direction == null || !connector.canConnectEnergy(level, pos, direction)) {
            return false;
        }

        BlockPos neighborPos = pos.relative(direction);
        Direction neighborSide = direction.getOpposite();
        BlockEntity neighborEntity = level.getBlockEntity(neighborPos);
        if (neighborEntity instanceof HbmEnergyConnector neighborConnector
                && neighborConnector.canConnectEnergy(neighborSide)) {
            return true;
        }

        if (level.getBlockState(neighborPos).getBlock() instanceof HbmEnergyConnectorBlock connectorBlock) {
            return connectorBlock.canConnectEnergy(level, neighborPos, neighborSide);
        }

        return false;
    }
}
