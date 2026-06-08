package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.PoweredRedCableBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredRedCableBlockEntity extends HbmEnergyNodeBlockEntity {
    public PoweredRedCableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static PoweredRedCableBlockEntity switchEntity(BlockPos pos, BlockState state) {
        return new PoweredRedCableBlockEntity(ModBlockEntities.CABLE_SWITCH.get(), pos, state);
    }

    public static PoweredRedCableBlockEntity detectorEntity(BlockPos pos, BlockState state) {
        return new PoweredRedCableBlockEntity(ModBlockEntities.CABLE_DETECTOR.get(), pos, state);
    }

    @Override
    protected boolean shouldCreateEnergyNode() {
        return getBlockState().getBlock() instanceof PoweredRedCableBlock cable && cable.isActive(getBlockState());
    }

    @Override
    public boolean canConnectEnergy(Direction side) {
        return shouldCreateEnergyNode() && super.canConnectEnergy(side);
    }
}
