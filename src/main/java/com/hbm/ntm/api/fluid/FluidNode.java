package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidNode;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Legacy-name facade for Fluid MK2 network nodes.
 */
@Deprecated(forRemoval = false)
public class FluidNode extends HbmFluidNode {
    public FluidNode(FluidType type, BlockPos... positions) {
        this(type, positions(positions), EnumSet.allOf(Direction.class));
    }

    public FluidNode(FluidType type, com.hbm.ntm.util.fauxpointtwelve.BlockPos... positions) {
        this(type, legacyPositions(positions), EnumSet.allOf(Direction.class));
    }

    public FluidNode(FluidType type, Set<BlockPos> positions, Set<Direction> connections) {
        super(positions(positions), type, connections(connections));
    }

    public FluidNode(FluidType type, BlockPos position, Set<Direction> connections) {
        this(type, positions(position), connections);
    }

    public FluidNode setConnections(Direction... connections) {
        return new FluidNode(getFluidType(), getPositions(), directions(connections));
    }

    public FluidNode setConnections(com.hbm.ntm.util.fauxpointtwelve.DirPos... connections) {
        return new FluidNode(getFluidType(), getPositions(), directions(connections));
    }

    public FluidNode setConnections(com.hbm.ntm.world.DirPos... connections) {
        return new FluidNode(getFluidType(), getPositions(), worldDirections(connections));
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

    private static Set<Direction> directions(com.hbm.ntm.util.fauxpointtwelve.DirPos... connections) {
        if (connections == null || connections.length == 0) {
            return EnumSet.noneOf(Direction.class);
        }
        EnumSet<Direction> result = EnumSet.noneOf(Direction.class);
        for (com.hbm.ntm.util.fauxpointtwelve.DirPos pos : connections) {
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
}
