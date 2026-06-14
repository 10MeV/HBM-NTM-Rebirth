package api.hbm.energymk2;

import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.util.fauxpointtwelve.DirPos;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for Energy MK2 nodespace access.
 */
@Deprecated(forRemoval = false)
public final class Nodespace {
    public static HbmEnergyNode getNode(Level level, int x, int y, int z) {
        return com.hbm.ntm.energy.Nodespace.getNode(level, x, y, z);
    }

    public static HbmEnergyNode getNode(Level level, BlockPos pos) {
        return com.hbm.ntm.energy.Nodespace.getNode(level, pos);
    }

    public static HbmEnergyNode createNode(Level level, HbmEnergyNode node) {
        return com.hbm.ntm.energy.Nodespace.createNode(level, node);
    }

    public static void destroyNode(Level level, int x, int y, int z) {
        com.hbm.ntm.energy.Nodespace.destroyNode(level, x, y, z);
    }

    public static void destroyNode(Level level, BlockPos pos) {
        com.hbm.ntm.energy.Nodespace.destroyNode(level, pos);
    }

    public static void destroyNode(Level level, HbmEnergyNode node) {
        com.hbm.ntm.energy.Nodespace.destroyNode(level, node);
    }

    /**
     * Source-migration wrapper for legacy Energy MK2 power nodes.
     */
    @Deprecated(forRemoval = false)
    public static class PowerNode extends com.hbm.ntm.energy.Nodespace.PowerNode {
        public PowerNode(BlockPos... positions) {
            super(positions);
        }

        public PowerNode(com.hbm.ntm.util.fauxpointtwelve.BlockPos... positions) {
            super(positions);
        }

        public PowerNode(Set<BlockPos> positions, Set<Direction> connections) {
            super(positions, connections);
        }

        @Override
        public PowerNode setConnections(Direction... connections) {
            super.setConnections(connections);
            return this;
        }

        @Override
        public PowerNode setConnections(DirPos... connections) {
            super.setConnections(connections);
            return this;
        }

        @Override
        public PowerNode setConnections(com.hbm.ntm.world.DirPos... connections) {
            super.setConnections(connections);
            return this;
        }

        @Override
        public PowerNode addConnection(DirPos connection) {
            super.addConnection(connection);
            return this;
        }

        @Override
        public PowerNode addConnection(com.hbm.ntm.world.DirPos connection) {
            super.addConnection(connection);
            return this;
        }
    }

    private Nodespace() {
    }
}
