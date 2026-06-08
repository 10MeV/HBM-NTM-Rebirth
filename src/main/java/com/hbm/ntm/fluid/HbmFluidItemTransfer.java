package com.hbm.ntm.fluid;

import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Random;

public final class HbmFluidItemTransfer {
    private static final Random RANDOM = new Random();

    public static boolean setTankTypeFromIdentifierSlot(IItemHandlerModifiable items, int inputSlot,
            HbmFluidTank tank, Level level, BlockPos pos) {
        return setTankTypeFromIdentifierSlot(items, inputSlot, inputSlot, tank, level, pos, 0, false);
    }

    public static boolean setTankTypeFromIdentifierSlot(IItemHandlerModifiable items, int inputSlot, int outputSlot,
            HbmFluidTank tank, Level level, BlockPos pos) {
        return setTankTypeFromIdentifierSlot(items, inputSlot, outputSlot, tank, level, pos, 0, false);
    }

    public static boolean setTankTypeFromIdentifierSlot(IItemHandlerModifiable items, int inputSlot,
            HbmFluidTank tank, Level level, BlockPos pos, int pressure, boolean forcePressure) {
        return setTankTypeFromIdentifierSlot(items, inputSlot, inputSlot, tank, level, pos, pressure, forcePressure);
    }

    public static boolean setTankTypeFromIdentifierSlot(IItemHandlerModifiable items, int inputSlot, int outputSlot,
            HbmFluidTank tank, Level level, BlockPos pos, int pressure, boolean forcePressure) {
        if (!isValidSlot(items, inputSlot) || !isValidSlot(items, outputSlot) || tank == null) {
            return false;
        }
        ItemStack input = items.getStackInSlot(inputSlot);
        if (input.isEmpty() || !(input.getItem() instanceof IFluidIdentifierItem identifier)) {
            return false;
        }
        if (inputSlot != outputSlot && !items.getStackInSlot(outputSlot).isEmpty()) {
            return false;
        }
        FluidType selected = identifier.getIdentifiedFluid(level, pos == null ? BlockPos.ZERO : pos, input);
        if (selected == null || tank.getTankType() == selected) {
            return false;
        }
        tank.setTankType(selected);
        if (forcePressure) {
            tank.withPressure(pressure);
        }
        if (inputSlot != outputSlot) {
            items.setStackInSlot(outputSlot, input.copy());
            items.setStackInSlot(inputSlot, ItemStack.EMPTY);
        }
        return true;
    }

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
        if (input.getItem() instanceof HbmInfiniteFluidItem infinite) {
            return drainInfiniteItemToTank(infinite, tank, simulate);
        }
        if (!HbmForgeFluidInterop.isStandardPressure(tank.getPressure())) {
            return false;
        }
        if (usesDiscreteContainerSlots(input, inputSlot, outputSlot)) {
            return transferContainerItem(items, inputSlot, outputSlot, tank, maxAmount, TransferDirection.ITEM_TO_TANK, simulate);
        }
        if (input.getItem() instanceof IFillableItem) {
            if (input.getCount() != 1) {
                return false;
            }
            TransferResult result = drainItemToTank(input, tank, maxAmount, simulate, false);
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
        if (input.getItem() instanceof HbmInfiniteFluidItem infinite) {
            return fillTankToInfiniteItem(infinite, tank, simulate);
        }
        if (!HbmForgeFluidInterop.isStandardPressure(tank.getPressure())) {
            return false;
        }
        if (usesDiscreteContainerSlots(input, inputSlot, outputSlot)) {
            return transferContainerItem(items, inputSlot, outputSlot, tank, maxAmount, TransferDirection.TANK_TO_ITEM, simulate);
        }
        if (input.getItem() instanceof IFillableItem) {
            if (input.getCount() != 1) {
                return false;
            }
            TransferResult result = fillItemFromTank(input, tank, maxAmount, simulate, false);
            if (result.moved() && !simulate) {
                items.setStackInSlot(inputSlot, result.stack());
            }
            return result.moved();
        }
        return transferContainerItem(items, inputSlot, outputSlot, tank, maxAmount, TransferDirection.TANK_TO_ITEM, simulate);
    }

    public static TransferResult fillItemFromTank(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate) {
        return fillItemFromTank(stack, tank, maxAmount, simulate, true);
    }

    private static TransferResult fillItemFromTank(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate,
            boolean useStandardContainers) {
        if (stack.isEmpty() || tank == null || tank.isEmpty()
                || !HbmForgeFluidInterop.isStandardPressure(tank.getPressure()) || maxAmount <= 0) {
            return TransferResult.empty(stack);
        }
        int amount = Math.min(maxAmount, tank.getFill());
        ItemStack working = stack.copy();
        if (useStandardContainers) {
            TransferResult standardResult = fillStandardContainerFromTank(working, tank, amount, simulate);
            if (standardResult.moved()) {
                return standardResult;
            }
        }
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
        return drainItemToTank(stack, tank, maxAmount, simulate, true);
    }

    private static TransferResult drainItemToTank(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate,
            boolean useStandardContainers) {
        if (stack.isEmpty() || tank == null || !HbmForgeFluidInterop.isStandardPressure(tank.getPressure())
                || maxAmount <= 0) {
            return TransferResult.empty(stack);
        }
        ItemStack working = stack.copy();
        if (useStandardContainers) {
            TransferResult standardResult = drainStandardContainerToTank(working, tank, maxAmount, simulate);
            if (standardResult.moved()) {
                return standardResult;
            }
            if (isFiniteHbmContainer(working) && HbmFluidContainerRegistry.getFluidType(working) != HbmFluids.NONE) {
                return TransferResult.empty(stack);
            }
        }
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
        if (stack.getItem() instanceof HbmInfiniteFluidItem infinite) {
            FluidType type = infinite.getType();
            return type == null || type == HbmFluids.NONE ? new HbmFluidStack(HbmFluids.NONE, 0) : new HbmFluidStack(type, infinite.getAmount(), 0);
        }
        HbmFluidStack hbmFluid = getHbmItemFluid(stack);
        if (!hbmFluid.isEmpty()) {
            return hbmFluid;
        }
        FluidType registeredType = HbmFluidContainerRegistry.getFluidType(stack);
        int registeredAmount = HbmFluidContainerRegistry.getFluidContent(stack, registeredType);
        if (registeredType != HbmFluids.NONE && registeredAmount > 0) {
            return new HbmFluidStack(registeredType, registeredAmount, getContainerPressure(stack));
        }
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(HbmFluidItemTransfer::getForgeItemFluid)
                .orElse(new HbmFluidStack(HbmFluids.NONE, 0));
    }

    private static int fillHbmItem(ItemStack stack, FluidType type, int amount, boolean simulate) {
        if (stack.getItem() instanceof HbmInfiniteFluidItem) {
            return 0;
        }
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
        if (stack.getItem() instanceof HbmInfiniteFluidItem) {
            return new HbmFluidStack(HbmFluids.NONE, 0);
        }
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
        if (stack.getItem() instanceof HbmInfiniteFluidItem) {
            return 0;
        }
        if (!(stack.getItem() instanceof IFillableItem fillable) || !fillable.providesFluid(type, stack)) {
            return 0;
        }
        ItemStack target = simulate ? stack.copy() : stack;
        return fillable.tryEmpty(type, amount, target);
    }

    private static boolean drainInfiniteItemToTank(HbmInfiniteFluidItem item, HbmFluidTank tank, boolean simulate) {
        FluidType type = item.getType();
        if (tank.getTankType() == HbmFluids.NONE || type == HbmFluids.NONE || !item.allowPressure(tank.getPressure())) {
            return false;
        }
        if (type != null && tank.getTankType() != type) {
            return false;
        }
        if (simulate) {
            return tank.getFill() < tank.getMaxFill();
        }
        if (item.getChance() > 1 && !simulate && RANDOM.nextInt(item.getChance()) != 0) {
            return false;
        }
        int previous = tank.getFill();
        if (type == null) {
            tank.setFill(Math.min(tank.getFill() + item.getAmount(), tank.getMaxFill()));
        } else {
            tank.fill(type, item.getAmount(), tank.getPressure(), false);
        }
        return tank.getFill() > previous;
    }

    private static boolean fillTankToInfiniteItem(HbmInfiniteFluidItem item, HbmFluidTank tank, boolean simulate) {
        FluidType type = item.getType();
        if (tank.getTankType() == HbmFluids.NONE) {
            return false;
        }
        if (!item.allowPressure(tank.getPressure())) {
            return false;
        }
        if (type != null && type != HbmFluids.NONE && tank.getTankType() != type) {
            return false;
        }
        if (simulate) {
            return tank.getFill() > 0;
        }
        if (item.getChance() > 1 && !simulate && RANDOM.nextInt(item.getChance()) != 0) {
            return false;
        }
        int previous = tank.getFill();
        tank.drain(item.getAmount(), false);
        return tank.getFill() < previous;
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

    private static TransferResult fillStandardContainerFromTank(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate) {
        if (!HbmForgeFluidInterop.isStandardPressure(tank.getPressure())) {
            return TransferResult.empty(stack);
        }
        HbmFluidContainerRegistry.ContainerEntry entry = HbmFluidContainerRegistry.getContainer(tank.getTankType(), stack);
        if (entry == null || entry.content() <= 0 || maxAmount < entry.content() || tank.getFill() < entry.content()) {
            return TransferResult.empty(stack);
        }
        ItemStack full = copyHoverName(stack, entry.copyFullContainer());
        if (!simulate) {
            tank.drain(entry.content(), false);
        }
        return new TransferResult(full, entry.content());
    }

    private static TransferResult drainStandardContainerToTank(ItemStack stack, HbmFluidTank tank, int maxAmount, boolean simulate) {
        FluidType type = HbmFluidContainerRegistry.getFluidType(stack);
        int content = HbmFluidContainerRegistry.getFluidContent(stack, type);
        ItemStack empty = HbmFluidContainerRegistry.getEmptyContainer(stack);
        int pressure = getContainerPressure(stack);
        if (type == HbmFluids.NONE
                || content <= 0
                || maxAmount < content
                || tank.getSpace() < content
                || !tank.canAccept(type, pressure)) {
            return TransferResult.empty(stack);
        }
        if (!simulate) {
            tank.fill(type, content, pressure, false);
        }
        return new TransferResult(copyHoverName(stack, empty), content);
    }

    private static int getContainerPressure(ItemStack stack) {
        return stack.getItem() instanceof com.hbm.ntm.item.HbmFluidContainerItem container ? container.getPressure(stack) : 0;
    }

    private static ItemStack copyHoverName(ItemStack source, ItemStack target) {
        if (source.hasCustomHoverName() && !target.isEmpty()) {
            target.setHoverName(source.getHoverName());
        }
        return target;
    }

    private static boolean usesDiscreteContainerSlots(ItemStack stack, int inputSlot, int outputSlot) {
        return inputSlot != outputSlot && isFiniteHbmContainer(stack);
    }

    private static boolean isFiniteHbmContainer(ItemStack stack) {
        return stack.getItem() instanceof com.hbm.ntm.item.HbmFluidContainerItem
                && !(stack.getItem() instanceof HbmInfiniteFluidItem);
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

    public static boolean processTransfers(IItemHandlerModifiable items, Iterable<TankSlotTransfer> transfers) {
        return processTransfers(items, transfers, false);
    }

    public static boolean processTransfers(IItemHandlerModifiable items, Iterable<TankSlotTransfer> transfers, boolean simulate) {
        if (items == null || transfers == null) {
            return false;
        }
        boolean changed = false;
        for (TankSlotTransfer transfer : transfers) {
            if (transfer != null) {
                changed |= transfer.process(items, simulate);
            }
        }
        return changed;
    }

    public record TransferResult(ItemStack stack, int amount) {
        public boolean moved() {
            return amount > 0;
        }

        private static TransferResult empty(ItemStack stack) {
            return new TransferResult(stack, 0);
        }
    }

    public record TankSlotTransfer(int inputSlot, int outputSlot, HbmFluidTank tank, int maxAmount, Direction direction) {
        public TankSlotTransfer {
            maxAmount = maxAmount <= 0 ? Integer.MAX_VALUE : maxAmount;
            direction = direction == null ? Direction.ITEM_TO_TANK : direction;
        }

        public static TankSlotTransfer load(int inputSlot, int outputSlot, HbmFluidTank tank) {
            return load(inputSlot, outputSlot, tank, Integer.MAX_VALUE);
        }

        public static TankSlotTransfer load(int inputSlot, int outputSlot, HbmFluidTank tank, int maxAmount) {
            return new TankSlotTransfer(inputSlot, outputSlot, tank, maxAmount, Direction.ITEM_TO_TANK);
        }

        public static TankSlotTransfer unload(int inputSlot, int outputSlot, HbmFluidTank tank) {
            return unload(inputSlot, outputSlot, tank, Integer.MAX_VALUE);
        }

        public static TankSlotTransfer unload(int inputSlot, int outputSlot, HbmFluidTank tank, int maxAmount) {
            return new TankSlotTransfer(inputSlot, outputSlot, tank, maxAmount, Direction.TANK_TO_ITEM);
        }

        private boolean process(IItemHandlerModifiable items, boolean simulate) {
            return direction == Direction.ITEM_TO_TANK
                    ? loadTankFromSlot(items, inputSlot, outputSlot, tank, maxAmount, simulate)
                    : unloadTankToSlot(items, inputSlot, outputSlot, tank, maxAmount, simulate);
        }

        public enum Direction {
            ITEM_TO_TANK,
            TANK_TO_ITEM
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
