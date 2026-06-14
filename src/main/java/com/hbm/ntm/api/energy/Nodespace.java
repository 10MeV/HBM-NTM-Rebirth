package com.hbm.ntm.api.energy;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Modern API namespace alias for Energy MK2 nodespace access.
 */
@Deprecated(forRemoval = false)
public final class Nodespace {
    public static com.hbm.ntm.energy.HbmEnergyNode getNode(Level level, int x, int y, int z) {
        return com.hbm.ntm.energy.Nodespace.getNode(level, x, y, z);
    }

    public static com.hbm.ntm.energy.HbmEnergyNode getNode(Level level, BlockPos pos) {
        return com.hbm.ntm.energy.Nodespace.getNode(level, pos);
    }

    public static com.hbm.ntm.energy.HbmEnergyNode createNode(Level level,
            com.hbm.ntm.energy.HbmEnergyNode node) {
        return com.hbm.ntm.energy.Nodespace.createNode(level, node);
    }

    public static void destroyNode(Level level, int x, int y, int z) {
        com.hbm.ntm.energy.Nodespace.destroyNode(level, x, y, z);
    }

    public static void destroyNode(Level level, BlockPos pos) {
        com.hbm.ntm.energy.Nodespace.destroyNode(level, pos);
    }

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
    }

    private Nodespace() {
    }
}
