package com.hbm.ntm.fluid;

import com.hbm.ntm.api.fluid.IFillableItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public final class HbmFluidItemTransfer {
    public static boolean loadTankFromSlot(IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return loadTankFromSlot(items, inputSlot, outputSlot, tank, Integer.MAX_VALUE, false);
    }

    public static boolean loadTankFromSlot(IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank, int maxAmount, boolean simulate) {
        if (!isValidSlot(items, inputSlot) || !isValidSlot(items, outputSlot) || tank == null || maxAmount <= 0) {
            return false;
        }
        ItemStack input = items.getStackInSlot(inputSlot);
        if (input.isEmpty()) {
            return false;
        }
        if (input.getItem() instanceof IFillableItem) {
            TransferResult result = drainItemToTank(input, tank, maxAmount, simulate);
            if (result.moved() && !simulate) {
                items.setStackInSlot(inputSlot, result.stack());
            }
            return result.moved();
        }
        return transferContainerItem(items, inputSlot, outputSlot, tank, maxAmount, TransferDirection.ITEM_TO_TANK, simulate);
    }

    public static boolean unloadTankToSlot(IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return unloadTankToSlot(items, inputSlot, outputSlot, tank, Integer.MAX_VALUE, false);
    }

    public static boolean unloadTankToSlot(IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank, int maxAmount, boolean simulate) {
        if (!isValidSlot(items, inputSlot) || !isValidSlot(items, outputSlot) || tank == null || maxAmount <= 0) {
            return false;
        }
        ItemStack input = items.getStackInSlot(inputSlot);
        if (input.isEmpty()) {
            return false;
        }
        if (input.getItem() instanceof IFillableItem) {
            TransferResult result = fillItemFromTank(input, tank, maxAmount, simulate);
            if (result.moved() && !simulate) {
                items.setStackInSlot(inputSlot, result.stack());
            }
            return result.moved();
        }
        return transferContainerItem(items, inputSlot, outputSlot, tank, maxAmount, TransferDirection.TANK_TO_ITEM, simulate);
    }

    public static TransferResult fillItemFromTank(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate) {
        if (stack.isEmpty() || tank == null || tank.isEmpty() || maxAmount <= 0 || !HbmFluidForgeMappings.canExport(tank.getTankType())) {
            return TransferResult.empty(stack);
        }
        int amount = Math.min(maxAmount, tank.getFill());
        ItemStack working = stack.copy();
        int filled = fillHbmItem(working, tank.getTankType(), amount, simulate);
        if (filled <= 0) {
            TransferResult forgeResult = fillForgeItem(working, tank.getTankType(), amount, simulate);
            working = forgeResult.stack();
            filled = forgeResult.amount();
        }
        if (filled <= 0) {
            return TransferResult.empty(stack);
        }
        if (!simulate) {
            tank.drain(filled, false);
        }
        return new TransferResult(working, filled);
    }

    public static TransferResult drainItemToTank(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate) {
        if (stack.isEmpty() || tank == null || maxAmount <= 0) {
            return TransferResult.empty(stack);
        }
        ItemStack working = stack.copy();
        HbmFluidStack available = getHbmItemFluid(working);
        int drained = 0;
        if (!available.isEmpty() && tank.canAccept(available.type(), available.pressure())) {
            int amount = Math.min(maxAmount, Math.min(available.amount(), tank.getSpace()));
            drained = drainHbmItem(working, available.type(), amount, simulate);
            if (drained > 0 && !simulate) {
                tank.fill(available.type(), drained, available.pressure(), false);
            }
        }
        if (drained <= 0) {
            TransferResult forgeResult = drainForgeItemToTank(working, tank, maxAmount, simulate);
            working = forgeResult.stack();
            drained = forgeResult.amount();
        }
        if (drained <= 0) {
            return TransferResult.empty(stack);
        }
        return new TransferResult(working, drained);
    }

    public static HbmFluidStack getItemFluid(ItemStack stack) {
        if (stack.isEmpty()) {
            return new HbmFluidStack(HbmFluids.NONE, 0);
        }
        HbmFluidStack hbmFluid = getHbmItemFluid(stack);
        if (!hbmFluid.isEmpty()) {
            return hbmFluid;
        }
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(HbmFluidItemTransfer::getForgeItemFluid)
                .orElse(new HbmFluidStack(HbmFluids.NONE, 0));
    }

    private static int fillHbmItem(ItemStack stack, FluidType type, int amount, boolean simulate) {
        if (!(stack.getItem() instanceof IFillableItem fillable) || !fillable.acceptsFluid(type, stack)) {
            return 0;
        }
        ItemStack target = simulate ? stack.copy() : stack;
        int remainder = fillable.tryFill(type, amount, target);
        return Math.max(0, amount - Math.max(0, remainder));
    }

    private static TransferResult fillForgeItem(ItemStack stack, FluidType type, int amount, boolean simulate) {
        FluidStack forgeFluid = HbmFluidForgeMappings.toForge(type, amount);
        if (forgeFluid.isEmpty()) {
            return TransferResult.empty(stack);
        }
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> handler.fill(forgeFluid, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE))
                .map(filled -> new TransferResult(currentContainer(stack), filled))
                .orElse(TransferResult.empty(stack));
    }

    private static HbmFluidStack getHbmItemFluid(ItemStack stack) {
        if (!(stack.getItem() instanceof IFillableItem fillable)) {
            return new HbmFluidStack(HbmFluids.NONE, 0);
        }
        FluidType type = fillable.getFirstFluidType(stack);
        if (type == null || type == HbmFluids.NONE || !fillable.providesFluid(type, stack)) {
            return new HbmFluidStack(HbmFluids.NONE, 0);
        }
        return new HbmFluidStack(type, fillable.getFill(stack));
    }

    private static int drainHbmItem(ItemStack stack, FluidType type, int amount, boolean simulate) {
        if (!(stack.getItem() instanceof IFillableItem fillable) || !fillable.providesFluid(type, stack)) {
            return 0;
        }
        ItemStack target = simulate ? stack.copy() : stack;
        return fillable.tryEmpty(type, amount, target);
    }

    private static TransferResult drainForgeItemToTank(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> {
                    FluidStack simulated = handler.drain(maxAmount, IFluidHandler.FluidAction.SIMULATE);
                    FluidType type = HbmFluidForgeMappings.fromForge(simulated);
                    if (type == HbmFluids.NONE || !tank.canAccept(type, tank.getPressure())) {
                        return TransferResult.empty(stack);
                    }
                    int accepted = tank.fill(type, simulated.getAmount(), tank.getPressure(), true);
                    if (accepted <= 0) {
                        return TransferResult.empty(stack);
                    }
                    if (!simulate) {
                        FluidStack drained = handler.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                        int filled = tank.fill(type, drained.getAmount(), tank.getPressure(), false);
                        return new TransferResult(handler.getContainer(), filled);
                    }
                    return new TransferResult(stack, accepted);
                })
                .orElse(TransferResult.empty(stack));
    }

    private static HbmFluidStack getForgeItemFluid(IFluidHandlerItem handler) {
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack stack = handler.getFluidInTank(i);
            FluidType type = HbmFluidForgeMappings.fromForge(stack);
            if (type != HbmFluids.NONE && stack.getAmount() > 0) {
                return new HbmFluidStack(type, stack.getAmount());
            }
        }
        return new HbmFluidStack(HbmFluids.NONE, 0);
    }

    private static ItemStack currentContainer(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(IFluidHandlerItem::getContainer)
                .orElse(stack);
    }

    private static boolean transferContainerItem(IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank,
            int maxAmount, TransferDirection direction, boolean simulate) {
        ItemStack input = items.getStackInSlot(inputSlot);
        if (input.isEmpty()) {
            return false;
        }
        if (inputSlot == outputSlot && input.getCount() > 1) {
            return false;
        }

        ItemStack single = input.copy();
        single.setCount(1);
        HbmFluidTank previewTank = copyTank(tank);
        TransferResult preview = direction.transfer(single, previewTank, maxAmount, false);
        if (!preview.moved() || !canMoveToOutput(items, outputSlot, preview.stack())) {
            return false;
        }
        if (simulate) {
            return true;
        }

        ItemStack actualSingle = input.copy();
        actualSingle.setCount(1);
        TransferResult actual = direction.transfer(actualSingle, tank, maxAmount, false);
        if (!actual.moved() || !canMoveToOutput(items, outputSlot, actual.stack())) {
            return false;
        }
        shrinkInput(items, inputSlot);
        addToOutput(items, outputSlot, actual.stack());
        return true;
    }

    private static boolean canMoveToOutput(IItemHandlerModifiable items, int outputSlot, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemStack output = items.getStackInSlot(outputSlot);
        if (output.isEmpty()) {
            return stack.getCount() <= Math.min(stack.getMaxStackSize(), items.getSlotLimit(outputSlot));
        }
        return ItemHandlerHelper.canItemStacksStack(output, stack)
                && output.getCount() + stack.getCount() <= Math.min(output.getMaxStackSize(), items.getSlotLimit(outputSlot));
    }

    private static void shrinkInput(IItemHandlerModifiable items, int inputSlot) {
        ItemStack input = items.getStackInSlot(inputSlot);
        if (input.getCount() <= 1) {
            items.setStackInSlot(inputSlot, ItemStack.EMPTY);
            return;
        }
        ItemStack remaining = input.copy();
        remaining.shrink(1);
        items.setStackInSlot(inputSlot, remaining);
    }

    private static void addToOutput(IItemHandlerModifiable items, int outputSlot, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack output = items.getStackInSlot(outputSlot);
        if (output.isEmpty()) {
            items.setStackInSlot(outputSlot, stack.copy());
            return;
        }
        ItemStack merged = output.copy();
        merged.grow(stack.getCount());
        items.setStackInSlot(outputSlot, merged);
    }

    private static HbmFluidTank copyTank(HbmFluidTank tank) {
        HbmFluidTank copy = new HbmFluidTank(tank.getTankType(), tank.getMaxFill()).withPressure(tank.getPressure());
        copy.setFill(tank.getFill());
        return copy;
    }

    private static boolean isValidSlot(IItemHandlerModifiable items, int slot) {
        return items != null && slot >= 0 && slot < items.getSlots();
    }

    public record TransferResult(ItemStack stack, int amount) {
        public boolean moved() {
            return amount > 0;
        }

        private static TransferResult empty(ItemStack stack) {
            return new TransferResult(stack, 0);
        }
    }

    private enum TransferDirection {
        ITEM_TO_TANK {
            @Override
            TransferResult transfer(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate) {
                return drainItemToTank(stack, tank, maxAmount, simulate);
            }
        },
        TANK_TO_ITEM {
            @Override
            TransferResult transfer(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate) {
                return fillItemFromTank(stack, tank, maxAmount, simulate);
            }
        };

        abstract TransferResult transfer(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate);
    }

    private HbmFluidItemTransfer() {
    }
}
