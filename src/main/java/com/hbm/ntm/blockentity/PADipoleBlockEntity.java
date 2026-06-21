package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.item.PACoilItem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PADipoleBlockEntity extends PABlockEntity implements PAParticleUser {
    public static final int SLOT_COIL = 1;
    public static final long USAGE = 100_000L;
    private static final String TAG_DIR_LOWER = "dirLower";
    private static final String TAG_DIR_UPPER = "dirUpper";
    private static final String TAG_DIR_REDSTONE = "dirRedstone";
    private static final String TAG_THRESHOLD = "threshold";

    private int dirLower;
    private int dirUpper;
    private int dirRedstone;
    private int threshold;

    public PADipoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PA_DIPOLE.get(), pos, state, ParticleAcceleratorBlock.Variant.DIPOLE,
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
        List<EnergyPort> ports = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            ports.add(energyPort(new BlockPos(direction.getStepX(), 2, direction.getStepZ()), Direction.UP));
            ports.add(energyPort(new BlockPos(direction.getStepX(), -2, direction.getStepZ()), Direction.DOWN));
        }
        return ports;
    }

    @Override
    public List<FluidPort> fluidPorts() {
        List<FluidPort> ports = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            ports.add(fluidPort(new BlockPos(direction.getStepX(), 2, direction.getStepZ()), Direction.UP));
            ports.add(fluidPort(new BlockPos(direction.getStepX(), -2, direction.getStepZ()), Direction.DOWN));
        }
        return ports;
    }

    @Override
    public boolean canParticleEnter(PASourceBlockEntity.Particle particle, Direction dir, BlockPos entryPos) {
        return entryPos.getY() == worldPosition.getY()
                && (entryPos.getX() == worldPosition.getX() || entryPos.getZ() == worldPosition.getZ());
    }

    @Override
    public void onParticleEnter(PASourceBlockEntity.Particle particle, Direction dir) {
        PACoilItem.Type coil = coilType();
        boolean inline = dir == getExitDir(particle);
        int mult = 1;
        if (coil != null && !inline) {
            if (coil.diMin() > particle.momentum()) {
                mult *= 10;
            }
            if (coil.diDistMin() > particle.distanceTraveled()) {
                mult *= 10;
            }
        }
        if (!isCool()) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NOCOOL);
        }
        if (getPower() < USAGE * mult) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NOPOWER);
        }
        if (coil == null) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_NOCOIL);
        }
        if (coil != null && coil.diMax() < particle.momentum() && !inline) {
            particle.crash(PASourceBlockEntity.PAState.CRASH_OVERSPEED);
        }
        if (particle.invalid()) {
            return;
        }
        if (inline) {
            particle.addDistance(3);
        } else {
            particle.resetDistance();
        }
        setPower(getPower() - USAGE * mult);
    }

    @Override
    public BlockPos getParticleExitPos(PASourceBlockEntity.Particle particle) {
        Direction exit = getExitDir(particle);
        particle.setDir(exit);
        return worldPosition.relative(exit, 2);
    }

    public Direction getExitDir(PASourceBlockEntity.Particle particle) {
        return ditToDirection(particle.momentum() < threshold ? dirLower : checkRedstone() ? dirRedstone : dirUpper);
    }

    public boolean checkRedstone() {
        if (level == null) {
            return false;
        }
        for (EnergyPort port : energyPorts()) {
            if (level.hasNeighborSignal(worldPosition.offset(port.offset()))) {
                return true;
            }
        }
        return false;
    }

    public PACoilItem.Type coilType() {
        ItemStack stack = items.getStackInSlot(SLOT_COIL);
        return stack.getItem() instanceof PACoilItem coil ? coil.type() : null;
    }

    @Override
    protected void loadPa(CompoundTag tag) {
        dirLower = tag.getInt(TAG_DIR_LOWER);
        dirUpper = tag.getInt(TAG_DIR_UPPER);
        dirRedstone = tag.getInt(TAG_DIR_REDSTONE);
        threshold = tag.getInt(TAG_THRESHOLD);
    }

    @Override
    protected void savePa(CompoundTag tag) {
        tag.putInt(TAG_DIR_LOWER, dirLower);
        tag.putInt(TAG_DIR_UPPER, dirUpper);
        tag.putInt(TAG_DIR_REDSTONE, dirRedstone);
        tag.putInt(TAG_THRESHOLD, threshold);
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if (tag.contains("lower")) {
            dirLower = (dirLower + 1) & 3;
        }
        if (tag.contains("upper")) {
            dirUpper = (dirUpper + 1) & 3;
        }
        if (tag.contains("redstone")) {
            dirRedstone = (dirRedstone + 1) & 3;
        }
        if (tag.contains("threshold")) {
            threshold = Mth.clamp(tag.getInt("threshold"), 0, 999_999_999);
        }
    }

    public int getDirLower() {
        return dirLower;
    }

    public int getDirUpper() {
        return dirUpper;
    }

    public int getDirRedstone() {
        return dirRedstone;
    }

    public int getThreshold() {
        return threshold;
    }

    public static Direction ditToDirection(int dir) {
        return switch (dir & 3) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    public static String dirName(int dir) {
        return ditToDirection(dir).getName().toLowerCase(Locale.ROOT);
    }
}
