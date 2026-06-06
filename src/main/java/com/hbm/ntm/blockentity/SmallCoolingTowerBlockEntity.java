package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SmallCoolingTowerBlockEntity extends CoolingTowerBlockEntity {
    private static final int INPUT_CAPACITY = 1_000;
    private static final int OUTPUT_CAPACITY = 1_000;
    private static final List<FluidPort> PORTS = cardinalPorts(3);

    public SmallCoolingTowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SMALL_COOLING_TOWER.get(), pos, state, INPUT_CAPACITY, OUTPUT_CAPACITY);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SmallCoolingTowerBlockEntity tower) {
        tickTower(level, pos, state, tower);
    }

    @Override
    protected Iterable<FluidPort> ports() {
        return PORTS;
    }
}
