package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmLegacyPowerNodeShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class HbmLegacyConnectorBlockEntity extends HbmLegacyWireNodeBlockEntity {
    protected HbmLegacyConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public HbmLegacyPowerNodeShapes.WireConnectionType getWireConnectionType() {
        return HbmLegacyPowerNodeShapes.WireConnectionType.SINGLE;
    }

    @Override
    public double getMaxWireLength() {
        return HbmLegacyPowerNodeShapes.CONNECTOR_MAX_WIRE_LENGTH;
    }

    @Override
    protected List<Vec3> getWireMounts() {
        return HbmLegacyPowerNodeShapes.connectorMounts(worldPosition);
    }

    @Override
    protected HbmEnergyNode createEnergyNode() {
        return HbmLegacyPowerNodeShapes.connector(worldPosition, getAttachedConnectionSide(), getWireConnections());
    }

    @Override
    public boolean canConnectEnergy(@Nullable Direction side) {
        return side != null && side == getAttachedConnectionSide();
    }

    protected Direction getAttachedConnectionSide() {
        return getLegacyFacing().getOpposite();
    }
}
