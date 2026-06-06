package com.hbm.ntm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public final class ConnectionUtil {
    private ConnectionUtil() {
    }

    public static boolean canConnectEnergy(BlockGetter level, BlockPos pos, Direction cableSide) {
        if (level == null || pos == null || cableSide == null) {
            return false;
        }
        if (pos.getY() < level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        Direction machineSide = cableSide.getOpposite();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof EnergyConnectorBlock connector && connector.canConnectEnergy(level, pos, machineSide)) {
            return true;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnergyConnector connector && connector.canConnectEnergy(machineSide)) {
            return true;
        }
        return blockEntity != null && blockEntity.getCapability(ForgeCapabilities.ENERGY, machineSide).isPresent();
    }

    public interface EnergyConnector {
        boolean canConnectEnergy(Direction side);
    }

    public interface EnergyConnectorBlock {
        boolean canConnectEnergy(BlockGetter level, BlockPos pos, Direction side);
    }
}
