package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

/**
 * Shared 1.7.10-style remote fluid port helpers for machines that are not
 * full fluid network nodes.
 */
public final class HbmFluidPortMachine {
    private HbmFluidPortMachine() {
    }

    public static int refreshReceiverPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, HbmFluidReceiver receiver) {
        if (level == null || level.isClientSide || receivingTanks == null || receiver == null) {
            return 0;
        }
        int touched = 0;
        Set<FluidType> seenTypes = new HashSet<>();
        for (HbmFluidTank tank : receivingTanks) {
            if (tank != null && tank.getTankType() != HbmFluids.NONE && seenTypes.add(tank.getTankType())) {
                touched += HbmFluidUtil.subscribeReceiverToPorts(level, origin, ports, tank.getTankType(), receiver);
            }
        }
        return touched;
    }

    public static int refreshProviderPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> sendingTanks, HbmFluidProvider provider) {
        if (level == null || level.isClientSide || sendingTanks == null || provider == null) {
            return 0;
        }
        int touched = 0;
        Set<FluidKey> seenKeys = new HashSet<>();
        for (HbmFluidTank tank : sendingTanks) {
            if (tank != null
                    && tank.getTankType() != HbmFluids.NONE
                    && tank.getFill() > 0
                    && seenKeys.add(new FluidKey(tank.getTankType(), tank.getPressure()))) {
                touched += HbmFluidUtil.tryProvideToPorts(level, origin, ports, tank.getTankType(), tank.getPressure(), provider);
            }
        }
        return touched;
    }

    public static int refreshTransceiverPorts(Level level, BlockPos origin, Iterable<FluidPort> ports,
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            HbmStandardFluidTransceiver transceiver) {
        return refreshReceiverPorts(level, origin, ports, receivingTanks, transceiver)
                + refreshProviderPorts(level, origin, ports, sendingTanks, transceiver);
    }

    private record FluidKey(FluidType type, int pressure) {
    }
}
