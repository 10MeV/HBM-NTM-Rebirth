package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CokerBlockEntity extends LegacyRemoteFluidMachineBlockEntity {
    public CokerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                tank(HbmFluids.HEAVYOIL, 16_000),
                tank(HbmFluids.OIL_COKER, 8_000));
    }

    private CokerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank, HbmFluidTank outputTank) {
        super(ModBlockEntities.COKER.get(), pos, state, 0L,
                List.of(inputTank, outputTank),
                List.of(inputTank),
                List.of(outputTank),
                false);
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fixedSurroundingPorts();
    }
}
