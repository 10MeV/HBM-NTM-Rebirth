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

public abstract class HbmLegacyMediumPylonBlockEntity extends HbmLegacyWireNodeBlockEntity {
    protected HbmLegacyMediumPylonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public HbmLegacyPowerNodeShapes.WireConnectionType getWireConnectionType() {
        return HbmLegacyPowerNodeShapes.WireConnectionType.TRIPLE;
    }

    @Override
    public double getMaxWireLength() {
        return HbmLegacyPowerNodeShapes.MEDIUM_PYLON_MAX_WIRE_LENGTH;
    }

    @Override
    protected List<Vec3> getWireMounts() {
        return HbmLegacyPowerNodeShapes.mediumPylonMounts(worldPosition, getLegacyHorizontalFacing());
    }

    @Override
    protected HbmEnergyNode createEnergyNode() {
        return HbmLegacyPowerNodeShapes.mediumPylon(worldPosition, getTransformerConnectionSide(), getWireConnections());
    }

    @Override
    public boolean canConnectEnergy(@Nullable Direction side) {
        return side != null && side == getTransformerConnectionSide();
    }

    protected boolean hasTransformerPort() {
        return false;
    }

    @Nullable
    protected Direction getTransformerConnectionSide() {
        return hasTransformerPort() ? getLegacyHorizontalFacing().getOpposite() : null;
    }
}
