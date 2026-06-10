package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy-name bridge for Energy MK2 conductors.
 */
@Deprecated(forRemoval = false)
public interface IEnergyConductorMK2 extends IEnergyConnectorMK2 {
    default HbmEnergyNode createNode() {
        if (this instanceof BlockEntity blockEntity) {
            return createNode(blockEntity.getLevel(), blockEntity.getBlockPos());
        }
        return new HbmEnergyNode(BlockPos.ZERO);
    }

    default HbmEnergyNode createNode(Level level, BlockPos pos) {
        if (pos == null) {
            return new HbmEnergyNode(BlockPos.ZERO);
        }
        return HbmEnergyNode.withStandardLegacyConnections(pos);
    }
}
