package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class CatalyticReformerBlockEntity extends LegacyRemoteFluidMachineBlockEntity {
    private static final long MAX_POWER = 1_000_000L;

    public CatalyticReformerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                tank(HbmFluids.NAPHTHA, 64_000),
                tank(HbmFluids.REFORMATE, 24_000),
                tank(HbmFluids.PETROLEUM, 24_000),
                tank(HbmFluids.HYDROGEN, 24_000));
    }

    private CatalyticReformerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank reformateTank, HbmFluidTank petroleumTank, HbmFluidTank hydrogenTank) {
        super(ModBlockEntities.CATALYTIC_REFORMER.get(), pos, state, MAX_POWER,
                List.of(inputTank, reformateTank, petroleumTank, hydrogenTank),
                List.of(inputTank),
                List.of(reformateTank, petroleumTank, hydrogenTank),
                true);
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                FluidPort.of(facing.getStepX() * 2 + rot.getStepX(), 0,
                        facing.getStepZ() * 2 + rot.getStepZ(), facing),
                FluidPort.of(facing.getStepX() * 2 - rot.getStepX(), 0,
                        facing.getStepZ() * 2 - rot.getStepZ(), facing),
                FluidPort.of(-facing.getStepX() * 2 + rot.getStepX(), 0,
                        -facing.getStepZ() * 2 + rot.getStepZ(), facing.getOpposite()),
                FluidPort.of(-facing.getStepX() * 2 - rot.getStepX(), 0,
                        -facing.getStepZ() * 2 - rot.getStepZ(), facing.getOpposite()),
                FluidPort.of(rot.getStepX() * 3, 0, rot.getStepZ() * 3, rot),
                FluidPort.of(-rot.getStepX() * 3, 0, -rot.getStepZ() * 3, rot.getOpposite()));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                EnergyPort.of(facing.getStepX() * 2 + rot.getStepX(), 0,
                        facing.getStepZ() * 2 + rot.getStepZ(), facing),
                EnergyPort.of(facing.getStepX() * 2 - rot.getStepX(), 0,
                        facing.getStepZ() * 2 - rot.getStepZ(), facing),
                EnergyPort.of(-facing.getStepX() * 2 + rot.getStepX(), 0,
                        -facing.getStepZ() * 2 + rot.getStepZ(), facing.getOpposite()),
                EnergyPort.of(-facing.getStepX() * 2 - rot.getStepX(), 0,
                        -facing.getStepZ() * 2 - rot.getStepZ(), facing.getOpposite()),
                EnergyPort.of(rot.getStepX() * 3, 0, rot.getStepZ() * 3, rot),
                EnergyPort.of(-rot.getStepX() * 3, 0, -rot.getStepZ() * 3, rot.getOpposite()));
    }
}
