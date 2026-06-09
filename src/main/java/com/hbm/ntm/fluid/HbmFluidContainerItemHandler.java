package com.hbm.ntm.fluid;

import com.hbm.ntm.item.HbmFluidContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class HbmFluidContainerItemHandler implements IFluidHandlerItem {
    private final HbmFluidContainerItem item;
    private ItemStack container;

    public HbmFluidContainerItemHandler(ItemStack container, HbmFluidContainerItem item) {
        this.container = container;
        this.item = item;
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
        if (tank != 0 || container.getCount() != 1) {
            return FluidStack.EMPTY;
        }
        if (!HbmForgeFluidInterop.canExposeItemToForge(item, container)) {
            return FluidStack.EMPTY;
        }
        FluidType type = item.getFirstFluidType(container);
        return HbmFluidForgeMappings.toForge(type, item.getFill(container));
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? item.getCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (tank != 0 || container.getCount() != 1 || stack.isEmpty()) {
            return false;
        }
        if (!HbmForgeFluidInterop.canExposeItemToForge(item, container)) {
            return false;
        }
        FluidType type = HbmFluidForgeMappings.fromForge(stack);
        return type != HbmFluids.NONE && item.acceptsFluid(type, container);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty()) {
            return 0;
        }
        if (!HbmForgeFluidInterop.canExposeItemToForge(item, container)) {
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
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (container.getCount() != 1 || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (!HbmForgeFluidInterop.canExposeItemToForge(item, container)) {
            return FluidStack.EMPTY;
        }
        FluidType type = HbmFluidForgeMappings.fromForgeExport(resource);
        if (type == HbmFluids.NONE || item.getFirstFluidType(container) != type) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        if (!HbmForgeFluidInterop.canExposeItemToForge(item, container)) {
            return FluidStack.EMPTY;
        }
        FluidType type = item.getFirstFluidType(container);
        if (type == HbmFluids.NONE || !HbmFluidForgeMappings.canExport(type) || !item.providesFluid(type, container)) {
            return FluidStack.EMPTY;
        }
        ItemStack target = action.execute() ? container : container.copy();
        int drained = item.tryEmpty(type, maxDrain, target);
        if (drained <= 0) {
            return FluidStack.EMPTY;
        }
        if (action.execute()) {
            container = item.getFill(target) <= 0 ? HbmFluidContainerRegistry.getEmptyContainer(target) : target;
        }
        return HbmFluidForgeMappings.toForge(type, drained);
    }
}
