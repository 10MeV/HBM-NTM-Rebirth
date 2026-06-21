package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PARfcBlockEntity extends PABlockEntity implements PAParticleUser {
    public static final long USAGE = 250_000L;
    public static final int MOMENTUM_GAIN = 100;
    public static final int DEFOCUS_GAIN = 100;

    public PARfcBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PA_RFC.get(), pos, state, ParticleAcceleratorBlock.Variant.RFC, 1, 1_000_000L);
    }

    @Override
    public long getUsage() {
        return USAGE;
    }

    @Override
    public List<EnergyPort> energyPorts() {
        return ports().stream().map(port -> energyPort(port.offset(), port.direction())).toList();
    }

    @Override
    public List<FluidPort> fluidPorts() {
        return ports();
    }

    private List<FluidPort> ports() {
        Direction side = facing().getClockWise();
        return List.of(
                fluidPort(rel(side, 3).above(2), Direction.UP),
                fluidPort(rel(side, -3).above(2), Direction.UP),
                fluidPort(BlockPos.ZERO.above(2), Direction.UP),
                fluidPort(rel(side, 3).below(2), Direction.DOWN),
                fluidPort(rel(side, -3).below(2), Direction.DOWN),
                fluidPort(BlockPos.ZERO.below(2), Direction.DOWN));
    }

    @Override
    public boolean canParticleEnter(PASourceBlockEntity.Particle particle, Direction dir, BlockPos entryPos) {
        Direction beam = beamSide(facing());
        return worldPosition.relative(beam.getOpposite(), 4).equals(entryPos) && beam == dir;
    }

    @Override
    public void onParticleEnter(PASourceBlockEntity.Particle particle, Direction dir) {
        if (!isCool()) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NOCOOL);
        }
        if (getPower() < USAGE) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NOPOWER);
        }
        if (particle.invalid()) {
            return;
        }
        particle.addDistance(9);
        particle.addMomentum(MOMENTUM_GAIN);
        particle.defocus(DEFOCUS_GAIN);
        setPower(getPower() - USAGE);
    }

    @Override
    public BlockPos getParticleExitPos(PASourceBlockEntity.Particle particle) {
        return worldPosition.relative(beamSide(facing()), 5);
    }
}
