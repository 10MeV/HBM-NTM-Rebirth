package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.recipe.ParticleAcceleratorRecipeRegistry;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PADetectorBlockEntity extends PABlockEntity implements PAParticleUser {
    public static final int SLOT_CONTAINER_1 = 1;
    public static final int SLOT_CONTAINER_2 = 2;
    public static final int SLOT_OUTPUT_1 = 3;
    public static final int SLOT_OUTPUT_2 = 4;
    public static final long USAGE = 100_000L;

    public PADetectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PA_DETECTOR.get(), pos, state, ParticleAcceleratorBlock.Variant.DETECTOR,
                5, 1_000_000L);
    }

    @Override
    public long getUsage() {
        return USAGE;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return super.isItemValid(slot, stack) || slot == SLOT_CONTAINER_1 || slot == SLOT_CONTAINER_2;
    }

    @Override
    public List<EnergyPort> energyPorts() {
        return fluidPorts().stream().map(port -> energyPort(port.offset(), port.direction())).toList();
    }

    @Override
    public List<FluidPort> fluidPorts() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        Direction out = side.getOpposite();
        BlockPos far = rel(side, -5);
        return List.of(
                fluidPort(far, out),
                fluidPort(far.above(), out),
                fluidPort(far.below(), out),
                fluidPort(far.offset(rel(facing, 1)), out),
                fluidPort(far.offset(rel(facing, -1)), out));
    }

    @Override
    public boolean canParticleEnter(PASourceBlockEntity.Particle particle, Direction dir, BlockPos entryPos) {
        Direction beam = beamSide(facing());
        return worldPosition.relative(beam.getOpposite(), 4).equals(entryPos) && beam == dir;
    }

    @Override
    public void onParticleEnter(PASourceBlockEntity.Particle particle, Direction dir) {
        particle.crash(PASourceBlockEntity.PAState.SUCCESS);
        if (particle.defocus() > 0) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_DEFOCUS);
            return;
        }
        if (getPower() < USAGE) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NOPOWER);
            return;
        }
        if (!isCool()) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NOCOOL);
            return;
        }
        setPower(getPower() - USAGE);

        ParticleAcceleratorRecipeRegistry.Recipe recipe =
                ParticleAcceleratorRecipeRegistry.getOutput(particle.input1(), particle.input2());
        if (recipe == null) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NORECIPE);
            return;
        }
        if (particle.momentum() < recipe.momentum()) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_UNDERSPEED);
            return;
        }
        if (!canAccept(recipe)) {
            return;
        }
        addOutput(SLOT_OUTPUT_1, recipe.output1Stack());
        addOutput(SLOT_OUTPUT_2, recipe.output2Stack());
        setChanged();
    }

    public boolean canAccept(ParticleAcceleratorRecipeRegistry.Recipe recipe) {
        return canFit(SLOT_OUTPUT_1, recipe.output1Stack()) && canFit(SLOT_OUTPUT_2, recipe.output2Stack());
    }

    private boolean canFit(int slot, ItemStack output) {
        if (output.isEmpty()) {
            return true;
        }
        ItemStack current = items.getStackInSlot(slot);
        return current.isEmpty() || ItemStack.isSameItemSameTags(current, output)
                && current.getCount() + output.getCount() <= current.getMaxStackSize();
    }

    private void addOutput(int slot, ItemStack output) {
        if (output.isEmpty()) {
            return;
        }
        ItemStack current = items.getStackInSlot(slot);
        if (current.isEmpty()) {
            items.setStackInSlot(slot, output.copy());
        } else if (ItemStack.isSameItemSameTags(current, output)) {
            current.grow(output.getCount());
        }
    }

    @Override
    public BlockPos getParticleExitPos(PASourceBlockEntity.Particle particle) {
        return null;
    }
}
