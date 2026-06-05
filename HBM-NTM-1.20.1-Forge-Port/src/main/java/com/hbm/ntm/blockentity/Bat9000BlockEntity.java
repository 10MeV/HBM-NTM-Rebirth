package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class Bat9000BlockEntity extends LegacyBigTankBlockEntity {
    private static final int TANK_CAPACITY = 2_048_000;
    private static final List<FluidPort> FLUID_PORTS = List.of(
            FluidPort.of(1, 0, 3, Direction.SOUTH),
            FluidPort.of(-1, 0, 3, Direction.SOUTH),
            FluidPort.of(1, 0, -3, Direction.NORTH),
            FluidPort.of(-1, 0, -3, Direction.NORTH),
            FluidPort.of(3, 0, 1, Direction.EAST),
            FluidPort.of(-3, 0, 1, Direction.WEST),
            FluidPort.of(3, 0, -1, Direction.EAST),
            FluidPort.of(-3, 0, -1, Direction.WEST));

    public Bat9000BlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, ModBlockEntities.BAT9000.get(), new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.bat9000");
    }
}
