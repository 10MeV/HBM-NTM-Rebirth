package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class HydrotreaterBlockEntity extends LegacyRemoteFluidMachineBlockEntity {
    private static final long MAX_POWER = 1_000_000L;

    public HydrotreaterBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                tank(HbmFluids.OIL, 64_000),
                tank(HbmFluids.HYDROGEN, 64_000, 1),
                tank(HbmFluids.OIL_DS, 24_000),
                tank(HbmFluids.SOURGAS, 24_000));
    }

    private HydrotreaterBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank hydrogenTank, HbmFluidTank desulfurizedOilTank, HbmFluidTank sourGasTank) {
        super(ModBlockEntities.HYDROTREATER.get(), pos, state, MAX_POWER,
                List.of(inputTank, hydrogenTank, desulfurizedOilTank, sourGasTank),
                List.of(inputTank, hydrogenTank),
                List.of(desulfurizedOilTank, sourGasTank),
                true);
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fixedSurroundingPorts();
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return energyPortsFromOffsets(List.of(
                new BlockPos(2, 0, 1),
                new BlockPos(2, 0, -1),
                new BlockPos(-2, 0, 1),
                new BlockPos(-2, 0, -1),
                new BlockPos(1, 0, 2),
                new BlockPos(-1, 0, 2),
                new BlockPos(1, 0, -2),
                new BlockPos(-1, 0, -2)));
    }
}
