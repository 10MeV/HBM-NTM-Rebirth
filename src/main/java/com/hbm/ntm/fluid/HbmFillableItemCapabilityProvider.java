package com.hbm.ntm.fluid;

import com.hbm.ntm.api.fluid.IFillableItem;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbmFillableItemCapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<IFluidHandlerItem> fluidHandler;

    public HbmFillableItemCapabilityProvider(ItemStack stack, IFillableItem item, int capacity) {
        this.fluidHandler = LazyOptional.of(() -> new Handler(stack, item, capacity));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            return fluidHandler.cast();
        }
        return LazyOptional.empty();
    }

    private static final class Handler implements IFluidHandlerItem {
        private final IFillableItem item;
        private final int capacity;
        private ItemStack container;

        private Handler(ItemStack container, IFillableItem item, int capacity) {
            this.container = container;
            this.item = item;
            this.capacity = Math.max(0, capacity);
        }

        @Override
        public @NotNull ItemStack getContainer() {
            return container;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (tank != 0 || !canHandleSingleStack()) {
                return FluidStack.EMPTY;
            }
            FluidType type = item.getFirstFluidType(container);
            if (type == null || type == HbmFluids.NONE || !item.providesFluid(type, container)) {
                return FluidStack.EMPTY;
            }
            return HbmFluidForgeMappings.toForge(type, item.getFill(container));
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? capacity : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            if (tank != 0 || stack.isEmpty() || !canHandleSingleStack()) {
                return false;
            }
            FluidType type = HbmFluidForgeMappings.fromForge(stack);
            return type != HbmFluids.NONE && item.acceptsFluid(type, container);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!canHandleSingleStack() || resource.isEmpty()) {
                return 0;
            }
            FluidType type = HbmFluidForgeMappings.fromForge(resource);
            if (type == HbmFluids.NONE || !item.acceptsFluid(type, container)) {
                return 0;
            }
            ItemStack target = action.execute() ? container : container.copy();
            int remainder = item.tryFill(type, resource.getAmount(), target);
            int filled = Math.max(0, resource.getAmount() - Math.max(0, remainder));
            if (action.execute() && filled > 0) {
                container = target;
            }
            return filled;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            if (!canHandleSingleStack() || resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            FluidType type = HbmFluidForgeMappings.fromForge(resource);
            if (type == HbmFluids.NONE || !HbmFluidForgeMappings.canExport(type) || !item.providesFluid(type, container)) {
                return FluidStack.EMPTY;
            }
            return drain(type, resource.getAmount(), action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            if (!canHandleSingleStack() || maxDrain <= 0) {
                return FluidStack.EMPTY;
            }
            FluidType type = item.getFirstFluidType(container);
            if (type == null || type == HbmFluids.NONE || !HbmFluidForgeMappings.canExport(type)
                    || !item.providesFluid(type, container)) {
                return FluidStack.EMPTY;
            }
            return drain(type, maxDrain, action);
        }

        private @NotNull FluidStack drain(FluidType type, int maxDrain, IFluidHandler.FluidAction action) {
            int available = Math.max(0, item.getFill(container));
            int requested = Math.min(maxDrain, available);
            if (requested <= 0) {
                return FluidStack.EMPTY;
            }
            ItemStack target = action.execute() ? container : container.copy();
            int drained = item.tryEmpty(type, requested, target);
            if (drained <= 0) {
                return FluidStack.EMPTY;
            }
            if (action.execute()) {
                container = target;
            }
            return HbmFluidForgeMappings.toForge(type, drained);
        }

        private boolean canHandleSingleStack() {
            return !container.isEmpty() && container.getCount() == 1;
        }
    }
}
