package com.hbm.ntm.fluid;

import com.hbm.ntm.item.HbmFluidContainerItem;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbmFluidContainerItemCapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<IFluidHandlerItem> fluidHandler;

    public HbmFluidContainerItemCapabilityProvider(ItemStack stack, HbmFluidContainerItem item) {
        this.fluidHandler = LazyOptional.of(() -> new HbmFluidContainerItemHandler(stack, item));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            return fluidHandler.cast();
        }
        return LazyOptional.empty();
    }
}
