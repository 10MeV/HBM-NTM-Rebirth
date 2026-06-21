package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.item.PACoilItem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PAQuadrupoleBlockEntity extends PABlockEntity implements PAParticleUser {
    public static final int SLOT_COIL = 1;
    public static final long USAGE = 100_000L;
    public static final int FOCUS_GAIN = 100;

    public PAQuadrupoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PA_QUADRUPOLE.get(), pos, state, ParticleAcceleratorBlock.Variant.QUADRUPOLE,
                2, 2_500_000L);
    }

    @Override
    public long getUsage() {
        return USAGE;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return super.isItemValid(slot, stack) || slot == SLOT_COIL && stack.getItem() instanceof PACoilItem;
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
        Direction forward = facing();
        return List.of(
                fluidPort(BlockPos.ZERO.above(2), Direction.UP),
                fluidPort(BlockPos.ZERO.below(2), Direction.DOWN),
                fluidPort(rel(forward, 2), forward),
                fluidPort(rel(forward, -2), forward.getOpposite()));
    }

    @Override
    public boolean canParticleEnter(PASourceBlockEntity.Particle particle, Direction dir, BlockPos entryPos) {
        Direction beam = beamSide(facing());
        return worldPosition.relative(beam.getOpposite()).equals(entryPos) && beam == dir;
    }

    @Override
    public void onParticleEnter(PASourceBlockEntity.Particle particle, Direction dir) {
        PACoilItem.Type coil = coilType();
        int mult = coil != null && coil.quadMin() > particle.momentum() ? 10 : 1;
        if (!isCool()) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NOCOOL);
        }
        if (getPower() < USAGE * mult) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NOPOWER);
        }
        if (coil == null) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NOCOIL);
        }
        if (coil != null && coil.quadMax() < particle.momentum()) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_OVERSPEED);
        }
        if (particle.invalid()) {
            return;
        }
        particle.addDistance(3);
        particle.focus(FOCUS_GAIN);
        setPower(getPower() - USAGE * mult);
    }

    @Override
    public BlockPos getParticleExitPos(PASourceBlockEntity.Particle particle) {
        return worldPosition.relative(beamSide(facing()), 2);
    }

    @SuppressWarnings("deprecation")
    public PACoilItem.Type coilType() {
        ItemStack stack = items.getStackInSlot(SLOT_COIL);
        return stack.getItem() instanceof PACoilItem coil ? coil.type() : null;
    }
}
