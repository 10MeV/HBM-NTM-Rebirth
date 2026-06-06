package com.hbm.ntm.energy;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbmBatteryItemCapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<IEnergyStorage> energy;

    public HbmBatteryItemCapabilityProvider(ItemStack stack, HbmBatteryItem battery) {
        this.energy = LazyOptional.of(() -> new ForgeBatteryItemAdapter(stack, battery));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        return LazyOptional.empty();
    }
}
