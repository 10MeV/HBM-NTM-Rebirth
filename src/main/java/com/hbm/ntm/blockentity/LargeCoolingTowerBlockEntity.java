package com.hbm.ntm.blockentity;

import com.hbm.ntm.config.CoolingTowerConfig;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LargeCoolingTowerBlockEntity extends CoolingTowerBlockEntity {
    private static final List<FluidPort> PORTS = largeTowerPorts();

    public LargeCoolingTowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LARGE_COOLING_TOWER.get(), pos, state, CoolingTowerConfig.largeInputTankSize(),
                CoolingTowerConfig.largeOutputTankSize());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LargeCoolingTowerBlockEntity tower) {
        tickTower(level, pos, state, tower);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LargeCoolingTowerBlockEntity tower) {
        tickLargeTowerClient(level, pos, tower);
    }

    @Override
    protected Iterable<FluidPort> ports() {
        return PORTS;
    }

    @Override
    protected int configuredInputCapacity() {
        return CoolingTowerConfig.largeInputTankSize();
    }

    @Override
    protected int configuredOutputCapacity() {
        return CoolingTowerConfig.largeOutputTankSize();
    }
}
