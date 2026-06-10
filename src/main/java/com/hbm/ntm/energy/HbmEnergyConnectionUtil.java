package com.hbm.ntm.energy;

import com.hbm.ntm.world.DirPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;
import java.util.List;
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

    /**
     * Modern equivalent of 1.7.10 Library.canConnect(...): {@code cableSide} is the side
     * from the cable/conductor toward {@code targetPos}; the target receives the opposite side.
     * This intentionally checks only HBM MK2 connector contracts and does not fall back to FE.
     */
    public static boolean canConnectLegacy(BlockGetter level, BlockPos targetPos, Direction cableSide) {
        if (level == null || targetPos == null || cableSide == null) {
            return false;
        }
        if (targetPos.getY() < level.getMinBuildHeight() || targetPos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }

        Direction targetSide = cableSide.getOpposite();
        if (level.getBlockState(targetPos).getBlock() instanceof HbmEnergyConnectorBlock connectorBlock
                && connectorBlock.canConnectEnergy(level, targetPos, targetSide)) {
            return true;
        }

        BlockEntity blockEntity = level.getBlockEntity(targetPos);
        return blockEntity instanceof HbmEnergyConnector connector
                && connector.canConnectEnergy(targetSide);
    }

    public static boolean canConnect(BlockGetter level, BlockPos pos, HbmEnergyConnector connector, Direction direction) {
        if (level == null || pos == null || connector == null || direction == null || !connector.canConnectEnergy(direction)) {
            return false;
        }
        return canConnectLegacy(level, pos.relative(direction), direction);
    }

    public static boolean canConnect(BlockGetter level, BlockPos pos, HbmEnergyConnectorBlock connector, Direction direction) {
        if (level == null || pos == null || connector == null || direction == null || !connector.canConnectEnergy(level, pos, direction)) {
            return false;
        }
        return canConnectLegacy(level, pos.relative(direction), direction);
    }

    public static List<DirPos> standardLegacyConnectionPoints(BlockPos pos) {
        if (pos == null) {
            return List.of();
        }
        return List.of(
                new DirPos(pos.relative(Direction.EAST), Direction.EAST),
                new DirPos(pos.relative(Direction.WEST), Direction.WEST),
                new DirPos(pos.relative(Direction.UP), Direction.UP),
                new DirPos(pos.relative(Direction.DOWN), Direction.DOWN),
                new DirPos(pos.relative(Direction.SOUTH), Direction.SOUTH),
                new DirPos(pos.relative(Direction.NORTH), Direction.NORTH));
    }
}
