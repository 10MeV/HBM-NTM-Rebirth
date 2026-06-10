package com.hbm.ntm.blockentity;

import com.hbm.ntm.config.CoolingTowerConfig;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SmallCoolingTowerBlockEntity extends CoolingTowerBlockEntity {
    private static final List<FluidPort> PORTS = cardinalPorts(3);

    public SmallCoolingTowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SMALL_COOLING_TOWER.get(), pos, state, CoolingTowerConfig.smallInputTankSize(),
                CoolingTowerConfig.smallOutputTankSize());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SmallCoolingTowerBlockEntity tower) {
        tickTower(level, pos, state, tower);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SmallCoolingTowerBlockEntity tower) {
        tickSmallTowerClient(level, pos, tower);
    }

    @Override
    protected Iterable<FluidPort> ports() {
        return PORTS;
    }

    @Override
    protected int configuredInputCapacity() {
        return CoolingTowerConfig.smallInputTankSize();
    }

    @Override
    protected int configuredOutputCapacity() {
        return CoolingTowerConfig.smallOutputTankSize();
    }
}
