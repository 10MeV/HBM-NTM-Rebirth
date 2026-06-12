package com.hbm.ntm.energy;

import com.hbm.ntm.util.fauxpointtwelve.DirPos;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Legacy-name facade for Energy MK2 nodespace access.
 */
@Deprecated(forRemoval = false)
public final class Nodespace {
    public static HbmEnergyNode getNode(Level level, int x, int y, int z) {
        return HbmEnergyNodespace.getNode(level, new BlockPos(x, y, z));
    }

    public static HbmEnergyNode getNode(Level level, BlockPos pos) {
        return HbmEnergyNodespace.getNode(level, pos);
    }

    public static HbmEnergyNode createNode(Level level, HbmEnergyNode node) {
        return HbmEnergyNodespace.createNode(level, node);
    }

    public static void destroyNode(Level level, int x, int y, int z) {
        HbmEnergyNodespace.destroyNode(level, new BlockPos(x, y, z));
    }

    public static void destroyNode(Level level, BlockPos pos) {
        HbmEnergyNodespace.destroyNode(level, pos);
    }

    public static class PowerNode extends HbmEnergyNode {
        public PowerNode(BlockPos... positions) {
            this(positions(positions), EnumSet.allOf(Direction.class));
        }

        public PowerNode(com.hbm.ntm.util.fauxpointtwelve.BlockPos... positions) {
            this(legacyPositions(positions), EnumSet.allOf(Direction.class));
        }

        public PowerNode(Set<BlockPos> positions, Set<Direction> connections) {
            super(positions(positions), connections(connections));
        }

        public PowerNode setConnections(Direction... connections) {
            return new PowerNode(getPositions(), directions(connections));
        }

        public PowerNode setConnections(DirPos... connections) {
            return new PowerNode(getPositions(), directions(connections));
        }

        public PowerNode setConnections(com.hbm.ntm.world.DirPos... connections) {
            return new PowerNode(getPositions(), worldDirections(connections));
        }
    }

    private static Set<BlockPos> positions(Set<BlockPos> positions) {
        if (positions == null || positions.isEmpty()) {
            return Set.of(BlockPos.ZERO);
        }
        LinkedHashSet<BlockPos> result = new LinkedHashSet<>();
        for (BlockPos pos : positions) {
            if (pos != null) {
                result.add(pos.immutable());
            }
        }
        return result.isEmpty() ? Set.of(BlockPos.ZERO) : result;
    }

    private static Set<BlockPos> positions(BlockPos... positions) {
        if (positions == null || positions.length == 0) {
            return Set.of(BlockPos.ZERO);
        }
        LinkedHashSet<BlockPos> result = new LinkedHashSet<>();
        for (BlockPos pos : positions) {
            if (pos != null) {
                result.add(pos.immutable());
            }
        }
        return result.isEmpty() ? Set.of(BlockPos.ZERO) : result;
    }

    private static Set<BlockPos> legacyPositions(com.hbm.ntm.util.fauxpointtwelve.BlockPos... positions) {
        if (positions == null || positions.length == 0) {
            return Set.of(BlockPos.ZERO);
        }
        LinkedHashSet<BlockPos> result = new LinkedHashSet<>();
        for (com.hbm.ntm.util.fauxpointtwelve.BlockPos pos : positions) {
            if (pos != null) {
                result.add(pos.immutable());
            }
        }
        return result.isEmpty() ? Set.of(BlockPos.ZERO) : result;
    }

    private static Set<Direction> connections(Set<Direction> connections) {
        if (connections == null || connections.isEmpty()) {
            return EnumSet.noneOf(Direction.class);
        }
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (Direction direction : connections) {
            if (direction != null) {
                result.add(direction);
            }
        }
        return result;
    }

    private static Set<Direction> directions(Direction... connections) {
        if (connections == null || connections.length == 0) {
            return EnumSet.noneOf(Direction.class);
        }
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (Direction direction : connections) {
            if (direction != null) {
                result.add(direction);
            }
        }
        return result;
    }

    private static Set<Direction> directions(DirPos... connections) {
        if (connections == null || connections.length == 0) {
            return EnumSet.noneOf(Direction.class);
        }
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (DirPos pos : connections) {
            if (pos != null && pos.getDir() != null) {
                result.add(pos.getDir());
            }
        }
        return result;
    }

    private static Set<Direction> worldDirections(com.hbm.ntm.world.DirPos... connections) {
        if (connections == null || connections.length == 0) {
            return EnumSet.noneOf(Direction.class);
        }
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (com.hbm.ntm.world.DirPos pos : connections) {
            if (pos != null && pos.getDir() != null) {
                result.add(pos.getDir());
            }
        }
        return result;
    }

    private Nodespace() {
    }
}
