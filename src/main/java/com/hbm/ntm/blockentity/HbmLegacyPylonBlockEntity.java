package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmLegacyPowerNodeShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class HbmLegacyPylonBlockEntity extends HbmLegacyWireNodeBlockEntity {
    protected HbmLegacyPylonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public HbmLegacyPowerNodeShapes.WireConnectionType getWireConnectionType() {
        return HbmLegacyPowerNodeShapes.WireConnectionType.SINGLE;
    }

    @Override
    public double getMaxWireLength() {
        return HbmLegacyPowerNodeShapes.PYLON_MAX_WIRE_LENGTH;
    }

    @Override
    protected List<Vec3> getWireMounts() {
        return HbmLegacyPowerNodeShapes.pylonMounts(worldPosition);
    }

    @Override
    protected HbmEnergyNode createEnergyNode() {
        return HbmLegacyPowerNodeShapes.pylon(worldPosition, getWireConnections());
    }
}
