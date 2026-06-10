package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy-name bridge for the standard Fluid MK2 receiver implementation.
 */
@Deprecated(forRemoval = false)
public interface IFluidStandardReceiverMK2 extends IFluidReceiverMK2, HbmStandardFluidReceiver {
    @Override
    List<HbmFluidTank> getReceivingTanks();

    @Override
    default long getDemand(FluidType type, int pressure) {
        return HbmStandardFluidReceiver.super.getDemand(type, pressure);
    }

    @Override
    default long transferFluid(FluidType type, int pressure, long amount) {
        return HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
    }

    @Override
    default int[] getReceivingPressureRange(FluidType type) {
        return HbmStandardFluidReceiver.super.getReceivingPressureRange(type);
    }

    default int subscribeToAllAround(FluidType type, BlockEntity blockEntity) {
        return blockEntity == null ? 0 : subscribeToAllAround(type, blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    default int subscribeToAllAround(FluidType type, Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0;
        }
        return com.hbm.ntm.fluid.HbmFluidUtil.subscribeReceiverToPorts(
                level, pos, HbmFluidPortLayouts.allAdjacent(), type, this);
    }

    default boolean tryUnsubscribe(FluidType type, Level level, BlockPos connectorPos) {
        if (level == null || connectorPos == null) {
            return false;
        }
        HbmFluidNode node = HbmFluidNodespace.getNode(level, connectorPos, type);
        HbmFluidNet fluidNet = node == null ? null : node.getFluidNet();
        if (fluidNet == null || !fluidNet.isSubscribed(this)) {
            return false;
        }
        fluidNet.removeReceiver(this);
        return true;
    }

    default int unsubscribeToAllAround(FluidType type, BlockEntity blockEntity) {
        return blockEntity == null ? 0 : unsubscribeToAllAround(type, blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    default int unsubscribeToAllAround(FluidType type, Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0;
        }
        int unsubscribed = 0;
        for (Direction direction : Direction.values()) {
            if (tryUnsubscribe(type, level, pos.relative(direction))) {
                unsubscribed++;
            }
        }
        return unsubscribed;
    }
}
