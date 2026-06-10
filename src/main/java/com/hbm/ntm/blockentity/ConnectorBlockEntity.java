package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LegacyConnectorBlock;
import com.hbm.ntm.energy.HbmLegacyPowerNodeShapes;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ConnectorBlockEntity extends HbmLegacyConnectorBlockEntity {
    public ConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_CONNECTOR.get(), pos, state);
    }

    @Override
    public double getMaxWireLength() {
        return kind() == LegacyConnectorBlock.Kind.SUPER
                ? HbmLegacyPowerNodeShapes.LARGE_PYLON_MAX_WIRE_LENGTH
                : HbmLegacyPowerNodeShapes.CONNECTOR_MAX_WIRE_LENGTH;
    }

    @Override
    protected List<Vec3> getWireMounts() {
        if (kind() != LegacyConnectorBlock.Kind.SUPER) {
            return super.getWireMounts();
        }
        Direction direction = getLegacyFacing();
        return List.of(new Vec3(
                worldPosition.getX() + 0.5D + direction.getStepX() * 0.375D,
                worldPosition.getY() + 0.5D + direction.getStepY() * 0.375D,
                worldPosition.getZ() + 0.5D + direction.getStepZ() * 0.375D));
    }

    private LegacyConnectorBlock.Kind kind() {
        return getBlockState().getBlock() instanceof LegacyConnectorBlock connector
                ? connector.kind()
                : LegacyConnectorBlock.Kind.NORMAL;
    }
}
