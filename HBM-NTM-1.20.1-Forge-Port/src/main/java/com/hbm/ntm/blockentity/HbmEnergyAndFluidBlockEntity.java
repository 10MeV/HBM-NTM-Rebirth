package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class HbmEnergyAndFluidBlockEntity extends HbmFluidNetworkBlockEntity {
    private static final String TAG_ENERGY = "Energy";

    protected final HbmEnergyStorage energy;
    private final LazyOptional<IEnergyStorage> forgeEnergy;
    private final LazyOptional<IEnergyStorage> forgeEnergyInput;
    private final LazyOptional<IEnergyStorage> forgeEnergyOutput;

    protected HbmEnergyAndFluidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            HbmEnergyStorage energy, List<HbmFluidTank> tanks) {
        super(type, pos, state, tanks);
        this.energy = energy;
        this.forgeEnergy = LazyOptional.of(() -> new ForgeEnergyAdapter(this.energy));
        this.forgeEnergyInput = LazyOptional.of(() -> new ForgeEnergyAdapter(this.energy, true, false));
        this.forgeEnergyOutput = LazyOptional.of(() -> new ForgeEnergyAdapter(this.energy, false, true));
    }

    public static <T extends HbmEnergyAndFluidBlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, blockEntity);
    }

    public long getPower() {
        return energy.getPower();
    }

    public long getMaxPower() {
        return energy.getMaxPower();
    }

    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.BOTH;
    }

    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of();
    }

    protected int tryProvideEnergyToPorts() {
        return level == null || level.isClientSide
                ? 0
                : HbmEnergyUtil.tryProvideToPorts(level, worldPosition, getEnergyPorts(), energy);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_ENERGY, energy.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        forgeEnergy.invalidate();
        forgeEnergyInput.invalidate();
        forgeEnergyOutput.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            HbmEnergySideMode mode = getEnergySideMode(side);
            if (mode.canReceive() && mode.canExtract()) {
                return forgeEnergy.cast();
            }
            if (mode.canReceive()) {
                return forgeEnergyInput.cast();
            }
            if (mode.canExtract()) {
                return forgeEnergyOutput.cast();
            }
        }
        return super.getCapability(capability, side);
    }
}
