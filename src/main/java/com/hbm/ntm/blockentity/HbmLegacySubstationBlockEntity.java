package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmLegacyPowerNodeShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class HbmLegacySubstationBlockEntity extends HbmLegacyWireNodeBlockEntity {
    protected HbmLegacySubstationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public HbmLegacyPowerNodeShapes.WireConnectionType getWireConnectionType() {
        return HbmLegacyPowerNodeShapes.WireConnectionType.QUAD;
    }

    @Override
    public double getMaxWireLength() {
        return HbmLegacyPowerNodeShapes.SUBSTATION_MAX_WIRE_LENGTH;
    }

    @Override
    protected List<Vec3> getWireMounts() {
        return HbmLegacyPowerNodeShapes.substationMounts(worldPosition, getLegacyHorizontalFacing());
    }

    @Override
    public Vec3 getWireConnectionPoint() {
        return HbmLegacyPowerNodeShapes.substationConnectionPoint(worldPosition);
    }

    @Override
    protected HbmEnergyNode createEnergyNode() {
        return HbmLegacyPowerNodeShapes.substation(worldPosition, getWireConnections());
    }
}
