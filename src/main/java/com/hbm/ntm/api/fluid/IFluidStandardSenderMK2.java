package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy-name bridge for the standard Fluid MK2 sender implementation.
 */
@Deprecated(forRemoval = false)
public interface IFluidStandardSenderMK2 extends IFluidProviderMK2, HbmStandardFluidSender {
    @Override
    List<HbmFluidTank> getSendingTanks();

    @Override
    default long getFluidAvailable(FluidType type, int pressure) {
        return HbmStandardFluidSender.super.getFluidAvailable(type, pressure);
    }

    @Override
    default void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
    }

    @Override
    default int[] getProvidingPressureRange(FluidType type) {
        return HbmStandardFluidSender.super.getProvidingPressureRange(type);
    }

    default boolean tryProvide(HbmFluidTank tank, Level level, BlockPos connectorPos, Direction directionFromSender) {
        if (tank == null) {
            return false;
        }
        return tryProvide(tank.getTankType(), tank.getPressure(), level, connectorPos, directionFromSender);
    }

    default boolean tryProvide(FluidType type, Level level, BlockPos connectorPos, Direction directionFromSender) {
        return tryProvide(type, 0, level, connectorPos, directionFromSender);
    }

    default boolean tryProvide(FluidType type, int pressure, Level level, BlockPos connectorPos, Direction directionFromSender) {
        if (level == null || connectorPos == null || directionFromSender == null) {
            return false;
        }
        BlockPos origin = connectorPos.relative(directionFromSender.getOpposite());
        return HbmFluidUtil.tryProvideToPorts(level, origin,
                List.of(HbmFluidPortLayouts.adjacent(directionFromSender)),
                type, pressure, this) > 0;
    }

    default int tryProvideToAll(HbmFluidTank tank, BlockEntity blockEntity) {
        return blockEntity == null ? 0 : tryProvideToAll(tank, blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    default int tryProvideToAll(HbmFluidTank tank, Level level, BlockPos pos) {
        if (tank == null || level == null || pos == null) {
            return 0;
        }
        return HbmFluidUtil.tryProvideToPorts(level, pos, HbmFluidPortLayouts.allAdjacent(),
                tank.getTankType(), tank.getPressure(), this);
    }

    default boolean sendFluid(HbmFluidTank tank, Level level, BlockPos connectorPos, Direction directionFromSender) {
        return tryProvide(tank, level, connectorPos, directionFromSender);
    }

    default int sendFluidToAll(HbmFluidTank tank, BlockEntity blockEntity) {
        return tryProvideToAll(tank, blockEntity);
    }

    default int sendFluidToAll(HbmFluidTank tank, Level level, BlockPos pos) {
        return tryProvideToAll(tank, level, pos);
    }
}
