package api.hbm.fluidmk2;

import com.hbm.ntm.fluid.FluidType;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Legacy 1.7.10 package bridge for Fluid MK2 network nodes.
 */
@Deprecated(forRemoval = false)
public class FluidNode extends com.hbm.ntm.api.fluid.FluidNode {
    public FluidNode(FluidType type, BlockPos... positions) {
        super(type, positions);
    }

    public FluidNode(FluidType type, com.hbm.ntm.util.fauxpointtwelve.BlockPos... positions) {
        super(type, positions);
    }

    public FluidNode(FluidType type, Set<BlockPos> positions, Set<Direction> connections) {
        super(type, positions, connections);
    }

    @Override
    public FluidNode setConnections(Direction... connections) {
        return new FluidNode(getFluidType(), getPositions(), directions(connections));
    }

    @Override
    public FluidNode setConnections(com.hbm.ntm.util.fauxpointtwelve.DirPos... connections) {
        return new FluidNode(getFluidType(), getPositions(), directions(connections));
    }

    @Override
    public FluidNode setConnections(com.hbm.ntm.world.DirPos... connections) {
        return new FluidNode(getFluidType(), getPositions(), worldDirections(connections));
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
