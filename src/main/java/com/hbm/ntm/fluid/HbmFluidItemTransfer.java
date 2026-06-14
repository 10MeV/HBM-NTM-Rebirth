package com.hbm.ntm.fluid;

import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItem;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
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

    public static FluidIdentifierStackReport identifyFluidFromStackReport(ItemStack stack, Level level, BlockPos pos) {
        ItemStack before = stack == null ? ItemStack.EMPTY : stack.copy();
        if (before.isEmpty() || !(before.getItem() instanceof IFluidIdentifierItem identifier)) {
            return new FluidIdentifierStackReport(before, HbmFluids.NONE, before.isEmpty(), false, true);
        }
        FluidType selected = identifier.getIdentifiedFluid(level, pos == null ? BlockPos.ZERO : pos, before);
        return new FluidIdentifierStackReport(before, selected, false, true, selected == null);
    }

    public static boolean setTankTypeFromIdentifierStack(ItemStack stack, HbmFluidTank tank, Level level, BlockPos pos) {
        return setTankTypeFromIdentifierStackReport(stack, tank, level, pos).changed();
    }

    public static FluidIdentifierTankReport setTankTypeFromIdentifierStackReport(ItemStack stack, HbmFluidTank tank,
            Level level, BlockPos pos) {
        FluidIdentifierStackReport identifier = identifyFluidFromStackReport(stack, level, pos);
        HbmFluidTank.TankState before = tank == null ? emptyTankState() : tank.snapshot();
        if (!identifier.identifierItem() || identifier.selectedNone() || tank == null) {
            return FluidIdentifierTankReport.of(identifier, tank == null, before, before, false);
        }
        tank.setTankType(identifier.selectedType());
        return FluidIdentifierTankReport.of(identifier, false, before, tank.snapshot(), true);
    }

    public static boolean setTankTypeFromIdentifierSlot(IItemHandlerModifiable items, int inputSlot, int outputSlot,
            HbmFluidTank tank, Level level, BlockPos pos, int pressure, boolean forcePressure) {
        return setTankTypeFromIdentifierSlotReport(items, inputSlot, outputSlot, tank, level, pos, pressure,
                forcePressure).changed();
    }

    public static FluidIdentifierSlotReport setTankTypeFromIdentifierSlotReport(IItemHandlerModifiable items,
            int inputSlot, int outputSlot, HbmFluidTank tank, Level level, BlockPos pos, int pressure,
            boolean forcePressure) {
        boolean inputSlotValid = isValidSlot(items, inputSlot);
        boolean outputSlotValid = isValidSlot(items, outputSlot);
        HbmFluidTank.TankState before = tank == null ? emptyTankState() : tank.snapshot();
        ItemStack inputBefore = inputSlotValid ? items.getStackInSlot(inputSlot).copy() : ItemStack.EMPTY;
        ItemStack outputBefore = outputSlotValid ? items.getStackInSlot(outputSlot).copy() : ItemStack.EMPTY;
        int targetPressure = HbmFluidTank.clampPressure(pressure);
        if (!isValidSlot(items, inputSlot) || !isValidSlot(items, outputSlot) || tank == null) {
            return FluidIdentifierSlotReport.of(inputSlot, outputSlot, inputSlotValid, outputSlotValid, tank == null,
                    inputBefore, inputBefore, outputBefore, outputBefore, before, before, HbmFluids.NONE,
                    targetPressure, forcePressure, false, false, false, false, false, false, false);
        }
        ItemStack input = items.getStackInSlot(inputSlot);
        if (input.isEmpty() || !(input.getItem() instanceof IFluidIdentifierItem identifier)) {
            return FluidIdentifierSlotReport.of(inputSlot, outputSlot, true, true, false,
                    inputBefore, inputBefore, outputBefore, outputBefore, before, before, HbmFluids.NONE,
                    targetPressure, forcePressure, input.isEmpty(), false, false, false, false, false, false);
        }
        if (inputSlot != outputSlot && !items.getStackInSlot(outputSlot).isEmpty()) {
            return FluidIdentifierSlotReport.of(inputSlot, outputSlot, true, true, false,
                    inputBefore, inputBefore, outputBefore, outputBefore, before, before, HbmFluids.NONE,
                    targetPressure, forcePressure, false, true, false, false, false, false, false);
        }
        FluidType selected = identifier.getIdentifiedFluid(level, pos == null ? BlockPos.ZERO : pos, input);
        boolean sameType = selected != null && tank.getTankType() == selected;
        boolean pressureChanged = forcePressure && tank.getPressure() != targetPressure;
        if (selected == null || (sameType && !pressureChanged)) {
            return FluidIdentifierSlotReport.of(inputSlot, outputSlot, true, true, false,
                    inputBefore, inputBefore, outputBefore, outputBefore, before, before,
                    selected == null ? HbmFluids.NONE : selected, targetPressure, forcePressure, false, false,
                    selected == null, sameType, false, false, false);
        }
        if (!sameType) {
            tank.setTankType(selected);
        }
        if (forcePressure) {
            tank.withPressure(pressure);
        }
        boolean movedStack = false;
        if (inputSlot != outputSlot) {
            items.setStackInSlot(outputSlot, input.copy());
            items.setStackInSlot(inputSlot, ItemStack.EMPTY);
            movedStack = true;
        }
        ItemStack inputAfter = items.getStackInSlot(inputSlot).copy();
        ItemStack outputAfter = items.getStackInSlot(outputSlot).copy();
        return FluidIdentifierSlotReport.of(inputSlot, outputSlot, true, true, false,
                inputBefore, inputAfter, outputBefore, outputAfter, before, tank.snapshot(), selected,
                targetPressure, forcePressure, false, false, false, sameType, pressureChanged, movedStack, true);
    }

    public static boolean loadTankFromSlot(IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return loadTankFromSlot(items, inputSlot, outputSlot, tank, Integer.MAX_VALUE, false);
    }

    public static TankSlotTransferResult loadTankFromSlotReport(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return loadTankFromSlotReport(items, inputSlot, outputSlot, tank, Integer.MAX_VALUE, false);
    }

    public static TankSlotTransferResult loadTankFromSlotReport(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank,
            int maxAmount, boolean simulate) {
        return TankSlotTransfer.load(inputSlot, outputSlot, tank, maxAmount).processResult(items, simulate);
    }

    public static TankSlotTransferResult previewLoadTankFromSlot(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return loadTankFromSlotReport(items, inputSlot, outputSlot, tank, Integer.MAX_VALUE, true);
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

    public static TankSlotTransferResult unloadTankToSlotReport(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return unloadTankToSlotReport(items, inputSlot, outputSlot, tank, Integer.MAX_VALUE, false);
    }

    public static TankSlotTransferResult unloadTankToSlotReport(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank,
            int maxAmount, boolean simulate) {
        return TankSlotTransfer.unload(inputSlot, outputSlot, tank, maxAmount).processResult(items, simulate);
    }

    public static TankSlotTransferResult previewUnloadTankToSlot(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return unloadTankToSlotReport(items, inputSlot, outputSlot, tank, Integer.MAX_VALUE, true);
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
        if (simulate) {
            TransferResult preview = fillItemFromTank(stack.copy(), copyTank(tank), maxAmount, false, useStandardContainers);
            return preview.moved() ? new TransferResult(stack, preview.amount()) : TransferResult.empty(stack);
        }
        int amount = Math.min(maxAmount, tank.getFill());
        ItemStack working = stack.copy();
        if (useStandardContainers) {
            TransferResult standardResult = fillStandardContainerFromTank(working, tank, amount, simulate);
            if (standardResult.moved()) {
                return standardResult;
            }
        }
        int totalFilled = 0;
        ArmorModFluidTransferReport armorReport = fillArmorModsFromTankReport(working, tank, amount, false);
        if (armorReport.moved()) {
            working = armorReport.armorAfter();
            totalFilled += armorReport.movedAmount();
        }
        int remaining = Math.min(amount - totalFilled, tank.getFill());
        if (remaining > 0) {
            int filled = fillHbmItem(working, tank.getTankType(), remaining, false);
            if (filled <= 0) {
                TransferResult forgeResult = fillForgeItem(working, tank.getTankType(), remaining, false);
                working = forgeResult.stack();
                filled = forgeResult.amount();
            }
            if (filled > 0) {
                tank.drain(filled, false);
                totalFilled += filled;
            }
        }
        if (totalFilled <= 0) {
            return TransferResult.empty(stack);
        }
        return new TransferResult(working, totalFilled);
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
        if (simulate) {
            TransferResult preview = drainItemToTank(stack.copy(), copyTank(tank), maxAmount, false, useStandardContainers);
            return preview.moved() ? new TransferResult(stack, preview.amount()) : TransferResult.empty(stack);
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
        int totalDrained = 0;
        ArmorModFluidTransferReport armorReport = drainArmorModsToTankReport(working, tank, maxAmount, false);
        if (armorReport.moved()) {
            working = armorReport.armorAfter();
            totalDrained += armorReport.movedAmount();
        }
        HbmFluidStack available = getHbmItemFluid(working);
        int drained = 0;
        if (totalDrained < maxAmount && !available.isEmpty() && tank.canAccept(available.type(), available.pressure())) {
            int amount = Math.min(maxAmount - totalDrained, Math.min(available.amount(), tank.getSpace()));
            drained = drainHbmItem(working, available.type(), amount, false);
            if (drained > 0) {
                tank.fill(available.type(), drained, available.pressure(), false);
            }
        }
        if (drained <= 0 && totalDrained < maxAmount) {
            TransferResult forgeResult = drainForgeItemToTank(working, tank, maxAmount - totalDrained, false);
            working = forgeResult.stack();
            drained = forgeResult.amount();
        }
        if (drained > 0) {
            totalDrained += drained;
        }
        if (totalDrained <= 0) {
            return TransferResult.empty(stack);
        }
        return new TransferResult(working, totalDrained);
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
        HbmFluidStack armorModFluid = getArmorModFluid(stack);
        if (!armorModFluid.isEmpty()) {
            return armorModFluid;
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
                .map(handler -> {
                    int filled = handler.fill(forgeFluid, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
                    ItemStack container = !simulate && filled > 0 ? handler.getContainer() : stack;
                    return new TransferResult(container, filled);
                })
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

    public static TransferResult fillArmorModsFromTank(ItemStack armor, HbmFluidTank tank, int maxAmount,
            boolean simulate) {
        ArmorModFluidTransferReport report = fillArmorModsFromTankReport(armor, tank, maxAmount, simulate);
        return report.moved()
                ? new TransferResult(simulate ? safeStackCopy(armor) : report.armorAfter(), report.movedAmount())
                : TransferResult.empty(safeStackCopy(armor));
    }

    public static ArmorModFluidTransferReport fillArmorModsFromTankReport(ItemStack armor, HbmFluidTank tank,
            int maxAmount, boolean simulate) {
        return transferArmorMods(armor, tank, maxAmount, simulate, TankSlotTransfer.Direction.TANK_TO_ITEM);
    }

    public static TransferResult drainArmorModsToTank(ItemStack armor, HbmFluidTank tank, int maxAmount,
            boolean simulate) {
        ArmorModFluidTransferReport report = drainArmorModsToTankReport(armor, tank, maxAmount, simulate);
        return report.moved()
                ? new TransferResult(simulate ? safeStackCopy(armor) : report.armorAfter(), report.movedAmount())
                : TransferResult.empty(safeStackCopy(armor));
    }

    public static ArmorModFluidTransferReport drainArmorModsToTankReport(ItemStack armor, HbmFluidTank tank,
            int maxAmount, boolean simulate) {
        return transferArmorMods(armor, tank, maxAmount, simulate, TankSlotTransfer.Direction.ITEM_TO_TANK);
    }

    private static ArmorModFluidTransferReport transferArmorMods(ItemStack armor, HbmFluidTank tank, int maxAmount,
            boolean simulate, TankSlotTransfer.Direction direction) {
        ItemStack armorBefore = safeStackCopy(armor);
        HbmFluidTank.TankState tankBefore = tank == null ? emptyTankState() : tank.snapshot();
        boolean validArmor = hasFillableArmorMods(armorBefore);
        if (!validArmor || tank == null || maxAmount <= 0
                || (direction == TankSlotTransfer.Direction.TANK_TO_ITEM && tank.isEmpty())) {
            return ArmorModFluidTransferReport.empty(direction, simulate, validArmor, tank == null, maxAmount,
                    armorBefore, tankBefore);
        }

        ItemStack workingArmor = simulate ? armorBefore.copy() : armor;
        HbmFluidTank workingTank = simulate ? copyTank(tank) : tank;
        List<ArmorModFluidTransferEntry> entries = new ArrayList<>();
        int moved = 0;
        int movedModules = 0;
        ItemStack[] mods = ArmorModHandler.pryMods(workingArmor);
        for (int slot = 0; slot < mods.length; slot++) {
            ItemStack mod = mods[slot];
            if (mod.isEmpty()) {
                continue;
            }
            ItemStack modBefore = mod.copy();
            boolean fillable = isFillableArmorMod(mod);
            boolean compatible = false;
            FluidType type = HbmFluids.NONE;
            int request = 0;
            int movedThisMod = 0;
            if (fillable && moved < maxAmount) {
                if (direction == TankSlotTransfer.Direction.TANK_TO_ITEM) {
                    type = workingTank.getTankType();
                    compatible = !workingTank.isEmpty()
                            && ((IFillableItem) mod.getItem()).acceptsFluid(type, mod);
                    if (compatible) {
                        request = Math.min(maxAmount - moved, workingTank.getFill());
                        movedThisMod = fillHbmItem(mod, type, request, false);
                        if (movedThisMod > 0) {
                            workingTank.drain(movedThisMod, false);
                        }
                    }
                } else {
                    HbmFluidStack available = getHbmItemFluid(mod);
                    type = available.type();
                    compatible = !available.isEmpty()
                            && workingTank.canAccept(available.type(), available.pressure())
                            && workingTank.getSpace() > 0;
                    if (compatible) {
                        request = Math.min(maxAmount - moved, Math.min(available.amount(), workingTank.getSpace()));
                        movedThisMod = drainHbmItem(mod, available.type(), request, false);
                        if (movedThisMod > 0) {
                            workingTank.fill(available.type(), movedThisMod, available.pressure(), false);
                        }
                    }
                }
                if (movedThisMod > 0) {
                    ArmorModHandler.applyMod(workingArmor, mod);
                    moved += movedThisMod;
                    movedModules++;
                }
            }
            entries.add(new ArmorModFluidTransferEntry(slot, fillable, compatible, type, request, movedThisMod,
                    modBefore, mod.copy()));
        }
        return new ArmorModFluidTransferReport(direction, simulate, validArmor, false, maxAmount, moved, movedModules,
                armorBefore, workingArmor, tankBefore, workingTank.snapshot(), List.copyOf(entries));
    }

    public static HbmFluidStack getArmorModFluid(ItemStack armor) {
        if (!hasFillableArmorMods(armor)) {
            return new HbmFluidStack(HbmFluids.NONE, 0);
        }
        for (ItemStack mod : ArmorModHandler.pryMods(armor)) {
            if (!isFillableArmorMod(mod)) {
                continue;
            }
            HbmFluidStack fluid = getItemFluid(mod);
            if (!fluid.isEmpty()) {
                return fluid;
            }
        }
        return new HbmFluidStack(HbmFluids.NONE, 0);
    }

    private static boolean hasFillableArmorMods(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ArmorItem && ArmorModHandler.hasMods(stack);
    }

    private static boolean isFillableArmorMod(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ArmorModItem && stack.getItem() instanceof IFillableItem;
    }

    private static ItemStack safeStackCopy(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
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

    private static HbmFluidTank.TankState emptyTankState() {
        return new HbmFluidTank.TankState(HbmFluids.NONE, 0, 0, 0);
    }

    public static boolean processTransfers(IItemHandlerModifiable items, Iterable<TankSlotTransfer> transfers) {
        return processTransfers(items, transfers, false);
    }

    public static boolean processTransfers(IItemHandlerModifiable items, Iterable<TankSlotTransfer> transfers, boolean simulate) {
        return processTransferReport(items, transfers, simulate).moved();
    }

    public static TransferBatchReport processTransferReport(IItemHandlerModifiable items, Iterable<TankSlotTransfer> transfers) {
        return processTransferReport(items, transfers, false);
    }

    public static TransferBatchReport processTransferReport(IItemHandlerModifiable items, Iterable<TankSlotTransfer> transfers, boolean simulate) {
        if (items == null || transfers == null) {
            return TransferBatchReport.empty(simulate);
        }
        TransferBatchReport.Builder builder = new TransferBatchReport.Builder(simulate);
        for (TankSlotTransfer transfer : transfers) {
            if (transfer != null) {
                builder.add(transfer.processResult(items, simulate));
            }
        }
        return builder.build();
    }

    public static List<TankSlotTransfer> loadTransfers(int inputSlotStart, int outputSlotStart, HbmFluidTank... tanks) {
        return loadTransfers(inputSlotStart, outputSlotStart, 1, tanks);
    }

    public static List<TankSlotTransfer> loadTransfers(int inputSlotStart, int outputSlotStart, int slotStride,
            HbmFluidTank... tanks) {
        return slotTransfers(inputSlotStart, outputSlotStart, slotStride, TankSlotTransfer.Direction.ITEM_TO_TANK,
                true, tanks);
    }

    public static List<TankSlotTransfer> loadTransfers(int inputSlotStart, int outputSlotStart,
            Iterable<HbmFluidTank> tanks) {
        return loadTransfers(inputSlotStart, outputSlotStart, 1, tanks);
    }

    public static List<TankSlotTransfer> loadTransfers(int inputSlotStart, int outputSlotStart, int slotStride,
            Iterable<HbmFluidTank> tanks) {
        return slotTransfers(inputSlotStart, outputSlotStart, slotStride, TankSlotTransfer.Direction.ITEM_TO_TANK,
                true, tanks);
    }

    public static List<TankSlotTransfer> loadTransfersIncludingEmptyTypes(int inputSlotStart, int outputSlotStart,
            Iterable<HbmFluidTank> tanks) {
        return slotTransfers(inputSlotStart, outputSlotStart, 1, TankSlotTransfer.Direction.ITEM_TO_TANK, false, tanks);
    }

    public static List<TankSlotTransfer> unloadTransfers(int inputSlotStart, int outputSlotStart,
            HbmFluidTank... tanks) {
        return unloadTransfers(inputSlotStart, outputSlotStart, 1, tanks);
    }

    public static List<TankSlotTransfer> unloadTransfers(int inputSlotStart, int outputSlotStart, int slotStride,
            HbmFluidTank... tanks) {
        return slotTransfers(inputSlotStart, outputSlotStart, slotStride, TankSlotTransfer.Direction.TANK_TO_ITEM,
                true, tanks);
    }

    public static List<TankSlotTransfer> unloadTransfers(int inputSlotStart, int outputSlotStart,
            Iterable<HbmFluidTank> tanks) {
        return unloadTransfers(inputSlotStart, outputSlotStart, 1, tanks);
    }

    public static List<TankSlotTransfer> unloadTransfers(int inputSlotStart, int outputSlotStart, int slotStride,
            Iterable<HbmFluidTank> tanks) {
        return slotTransfers(inputSlotStart, outputSlotStart, slotStride, TankSlotTransfer.Direction.TANK_TO_ITEM,
                true, tanks);
    }

    public static List<TankSlotTransfer> unloadTransfersIncludingEmptyTypes(int inputSlotStart, int outputSlotStart,
            Iterable<HbmFluidTank> tanks) {
        return slotTransfers(inputSlotStart, outputSlotStart, 1, TankSlotTransfer.Direction.TANK_TO_ITEM, false, tanks);
    }

    @SafeVarargs
    public static List<TankSlotTransfer> combineTransfers(Iterable<TankSlotTransfer>... transferGroups) {
        List<TankSlotTransfer> transfers = new ArrayList<>();
        if (transferGroups == null) {
            return transfers;
        }
        for (Iterable<TankSlotTransfer> group : transferGroups) {
            if (group == null) {
                continue;
            }
            for (TankSlotTransfer transfer : group) {
                if (transfer != null) {
                    transfers.add(transfer);
                }
            }
        }
        return transfers;
    }

    private static List<TankSlotTransfer> slotTransfers(int inputSlotStart, int outputSlotStart, int slotStride,
            TankSlotTransfer.Direction direction, boolean skipNone, HbmFluidTank... tanks) {
        List<TankSlotTransfer> transfers = new ArrayList<>();
        if (tanks == null) {
            return transfers;
        }
        int stride = Math.max(1, slotStride);
        for (int i = 0; i < tanks.length; i++) {
            addSlotTransfer(transfers, inputSlotStart + i * stride, outputSlotStart + i * stride,
                    direction, skipNone, tanks[i]);
        }
        return transfers;
    }

    private static List<TankSlotTransfer> slotTransfers(int inputSlotStart, int outputSlotStart, int slotStride,
            TankSlotTransfer.Direction direction, boolean skipNone, Iterable<HbmFluidTank> tanks) {
        List<TankSlotTransfer> transfers = new ArrayList<>();
        if (tanks == null) {
            return transfers;
        }
        int stride = Math.max(1, slotStride);
        int index = 0;
        for (HbmFluidTank tank : tanks) {
            addSlotTransfer(transfers, inputSlotStart + index * stride, outputSlotStart + index * stride,
                    direction, skipNone, tank);
            index++;
        }
        return transfers;
    }

    private static void addSlotTransfer(List<TankSlotTransfer> transfers, int inputSlot, int outputSlot,
            TankSlotTransfer.Direction direction, boolean skipNone, HbmFluidTank tank) {
        if (tank == null || (skipNone && tank.getTankType() == HbmFluids.NONE)) {
            return;
        }
        transfers.add(direction == TankSlotTransfer.Direction.ITEM_TO_TANK
                ? TankSlotTransfer.load(inputSlot, outputSlot, tank)
                : TankSlotTransfer.unload(inputSlot, outputSlot, tank));
    }

    public record TransferResult(ItemStack stack, int amount) {
        public boolean moved() {
            return amount > 0;
        }

        private static TransferResult empty(ItemStack stack) {
            return new TransferResult(stack, 0);
        }
    }

    public record ArmorModFluidTransferReport(
            TankSlotTransfer.Direction direction,
            boolean simulate,
            boolean validArmor,
            boolean missingTank,
            int maxAmount,
            int movedAmount,
            int movedModules,
            ItemStack armorBefore,
            ItemStack armorAfter,
            HbmFluidTank.TankState tankBefore,
            HbmFluidTank.TankState tankAfter,
            List<ArmorModFluidTransferEntry> entries) {
        public ArmorModFluidTransferReport {
            direction = direction == null ? TankSlotTransfer.Direction.TANK_TO_ITEM : direction;
            maxAmount = Math.max(0, maxAmount);
            movedAmount = Math.max(0, movedAmount);
            movedModules = Math.max(0, movedModules);
            armorBefore = safeStackCopy(armorBefore);
            armorAfter = safeStackCopy(armorAfter);
            tankBefore = tankBefore == null ? emptyTankState() : tankBefore;
            tankAfter = tankAfter == null ? tankBefore : tankAfter;
            entries = entries == null ? List.of() : List.copyOf(entries);
        }

        public boolean moved() {
            return movedAmount > 0;
        }

        public TransferResult toTransferResult() {
            return moved() ? new TransferResult(armorAfter, movedAmount) : TransferResult.empty(armorBefore);
        }

        private static ArmorModFluidTransferReport empty(TankSlotTransfer.Direction direction, boolean simulate,
                boolean validArmor, boolean missingTank, int maxAmount, ItemStack armorBefore,
                HbmFluidTank.TankState tankBefore) {
            return new ArmorModFluidTransferReport(direction, simulate, validArmor, missingTank, maxAmount, 0, 0,
                    armorBefore, armorBefore, tankBefore, tankBefore, List.of());
        }
    }

    public record ArmorModFluidTransferEntry(
            int slot,
            boolean fillable,
            boolean tankCompatible,
            FluidType fluidType,
            int requestedAmount,
            int movedAmount,
            ItemStack modBefore,
            ItemStack modAfter) {
        public ArmorModFluidTransferEntry {
            fluidType = fluidType == null ? HbmFluids.NONE : fluidType;
            requestedAmount = Math.max(0, requestedAmount);
            movedAmount = Math.max(0, movedAmount);
            modBefore = safeStackCopy(modBefore);
            modAfter = safeStackCopy(modAfter);
        }

        public boolean moved() {
            return movedAmount > 0;
        }
    }

    public record TransferBatchReport(boolean simulate, int attemptedTransfers, int movedTransfers, int movedAmount,
                                      java.util.List<TankSlotTransferResult> results) {
        public boolean moved() {
            return movedTransfers > 0;
        }

        private static TransferBatchReport empty(boolean simulate) {
            return new TransferBatchReport(simulate, 0, 0, 0, java.util.List.of());
        }

        private static final class Builder {
            private final boolean simulate;
            private final java.util.List<TankSlotTransferResult> results = new java.util.ArrayList<>();
            private int movedTransfers;
            private int movedAmount;

            private Builder(boolean simulate) {
                this.simulate = simulate;
            }

            private void add(TankSlotTransferResult result) {
                if (result == null) {
                    return;
                }
                results.add(result);
                if (result.moved()) {
                    movedTransfers++;
                    movedAmount += result.movedAmount();
                }
            }

            private TransferBatchReport build() {
                return new TransferBatchReport(simulate, results.size(), movedTransfers, movedAmount, java.util.List.copyOf(results));
            }
        }
    }

    public record TankSlotTransferResult(
            int inputSlot,
            int outputSlot,
            TankSlotTransfer.Direction direction,
            boolean moved,
            int movedAmount,
            int tankBefore,
            int tankAfter,
            int tankCapacity,
            FluidType tankType,
            int tankPressure,
            int maxAmount,
            boolean inputSlotValid,
            boolean outputSlotValid,
            boolean standardPressure,
            boolean inputEmpty,
            FluidType inputFluidType,
            int inputFluidAmount,
            int inputFluidPressure,
            ItemStack inputBefore,
            ItemStack inputAfter,
            ItemStack outputBefore,
            ItemStack outputAfter) {
        private static TankSlotTransferResult of(TankSlotTransfer transfer, boolean moved, int movedAmount,
                int tankBefore, int tankAfter, ItemStack inputBefore, ItemStack inputAfter,
                ItemStack outputBefore, ItemStack outputAfter, boolean inputSlotValid, boolean outputSlotValid) {
            HbmFluidTank tank = transfer.tank();
            HbmFluidStack inputFluid = safeItemFluid(inputBefore);
            return new TankSlotTransferResult(
                    transfer.inputSlot(),
                    transfer.outputSlot(),
                    transfer.direction(),
                    moved,
                    Math.max(0, movedAmount),
                    tankBefore,
                    tankAfter,
                    tank == null ? 0 : tank.getMaxFill(),
                    tank == null ? HbmFluids.NONE : tank.getTankType(),
                    tank == null ? 0 : tank.getPressure(),
                    transfer.maxAmount(),
                    inputSlotValid,
                    outputSlotValid,
                    tank != null && HbmForgeFluidInterop.isStandardPressure(tank.getPressure()),
                    inputBefore == null || inputBefore.isEmpty(),
                    inputFluid.type(),
                    inputFluid.amount(),
                    inputFluid.pressure(),
                    safeCopy(inputBefore),
                    safeCopy(inputAfter),
                    safeCopy(outputBefore),
                    safeCopy(outputAfter));
        }

        private static ItemStack safeCopy(ItemStack stack) {
            return stack == null ? ItemStack.EMPTY : stack.copy();
        }

        private static HbmFluidStack safeItemFluid(ItemStack stack) {
            return stack == null || stack.isEmpty() ? new HbmFluidStack(HbmFluids.NONE, 0) : getItemFluid(stack);
        }
    }

    public record FluidIdentifierSlotReport(
            int inputSlot,
            int outputSlot,
            boolean inputSlotValid,
            boolean outputSlotValid,
            boolean missingTank,
            ItemStack inputBefore,
            ItemStack inputAfter,
            ItemStack outputBefore,
            ItemStack outputAfter,
            HbmFluidTank.TankState tankBefore,
            HbmFluidTank.TankState tankAfter,
            FluidType selectedType,
            int targetPressure,
            boolean forcePressure,
            boolean inputEmpty,
            boolean outputBlocked,
            boolean selectedNone,
            boolean sameType,
            boolean pressureChanged,
            boolean movedIdentifierStack,
            boolean changed) {
        public FluidIdentifierSlotReport {
            inputBefore = safeStackCopy(inputBefore);
            inputAfter = safeStackCopy(inputAfter);
            outputBefore = safeStackCopy(outputBefore);
            outputAfter = safeStackCopy(outputAfter);
            tankBefore = tankBefore == null ? emptyTankState() : tankBefore;
            tankAfter = tankAfter == null ? tankBefore : tankAfter;
            selectedType = selectedType == null ? HbmFluids.NONE : selectedType;
            targetPressure = HbmFluidTank.clampPressure(targetPressure);
        }

        private static ItemStack safeStackCopy(ItemStack stack) {
            return stack == null ? ItemStack.EMPTY : stack.copy();
        }

        private static FluidIdentifierSlotReport of(int inputSlot, int outputSlot, boolean inputSlotValid,
                boolean outputSlotValid, boolean missingTank, ItemStack inputBefore, ItemStack inputAfter,
                ItemStack outputBefore, ItemStack outputAfter, HbmFluidTank.TankState tankBefore,
                HbmFluidTank.TankState tankAfter, FluidType selectedType, int targetPressure, boolean forcePressure,
                boolean inputEmpty, boolean outputBlocked, boolean selectedNone, boolean sameType,
                boolean pressureChanged, boolean movedIdentifierStack, boolean changed) {
            boolean tankChanged = tankBefore != null && tankAfter != null
                    && (tankBefore.type() != tankAfter.type()
                            || tankBefore.fillMb() != tankAfter.fillMb()
                            || tankBefore.capacityMb() != tankAfter.capacityMb()
                            || tankBefore.pressure() != tankAfter.pressure());
            return new FluidIdentifierSlotReport(inputSlot, outputSlot, inputSlotValid, outputSlotValid, missingTank,
                    inputBefore, inputAfter, outputBefore, outputAfter, tankBefore, tankAfter, selectedType,
                    targetPressure, forcePressure, inputEmpty, outputBlocked, selectedNone, sameType,
                    pressureChanged, movedIdentifierStack, changed || tankChanged || movedIdentifierStack);
        }
    }

    public record FluidIdentifierStackReport(
            ItemStack stack,
            FluidType selectedType,
            boolean inputEmpty,
            boolean identifierItem,
            boolean selectedNone) {
        public FluidIdentifierStackReport {
            stack = safeStackCopy(stack);
            selectedType = selectedType == null ? HbmFluids.NONE : selectedType;
        }

        private static ItemStack safeStackCopy(ItemStack stack) {
            return stack == null ? ItemStack.EMPTY : stack.copy();
        }
    }

    public record FluidIdentifierTankReport(
            FluidIdentifierStackReport identifier,
            boolean missingTank,
            HbmFluidTank.TankState tankBefore,
            HbmFluidTank.TankState tankAfter,
            boolean sameType,
            boolean changed) {
        public FluidIdentifierTankReport {
            identifier = identifier == null
                    ? new FluidIdentifierStackReport(ItemStack.EMPTY, HbmFluids.NONE, true, false, true)
                    : identifier;
            tankBefore = tankBefore == null ? emptyTankState() : tankBefore;
            tankAfter = tankAfter == null ? tankBefore : tankAfter;
        }

        private static FluidIdentifierTankReport of(FluidIdentifierStackReport identifier, boolean missingTank,
                HbmFluidTank.TankState tankBefore, HbmFluidTank.TankState tankAfter, boolean attemptedChange) {
            boolean sameType = !missingTank
                    && identifier != null
                    && tankBefore != null
                    && identifier.identifierItem()
                    && !identifier.selectedNone()
                    && tankBefore.type() == identifier.selectedType();
            boolean tankChanged = tankBefore != null && tankAfter != null
                    && (tankBefore.type() != tankAfter.type()
                            || tankBefore.fillMb() != tankAfter.fillMb()
                            || tankBefore.capacityMb() != tankAfter.capacityMb()
                            || tankBefore.pressure() != tankAfter.pressure());
            return new FluidIdentifierTankReport(identifier, missingTank, tankBefore, tankAfter, sameType,
                    attemptedChange && tankChanged);
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

        private TankSlotTransferResult processResult(IItemHandlerModifiable items, boolean simulate) {
            boolean inputSlotValid = isValidSlot(items, inputSlot);
            boolean outputSlotValid = isValidSlot(items, outputSlot);
            int tankBefore = tank == null ? 0 : tank.getFill();
            ItemStack inputBefore = inputSlotValid ? items.getStackInSlot(inputSlot) : ItemStack.EMPTY;
            ItemStack outputBefore = outputSlotValid ? items.getStackInSlot(outputSlot) : ItemStack.EMPTY;
            boolean moved = process(items, simulate);
            int tankAfter = tank == null ? tankBefore : tank.getFill();
            ItemStack inputAfter = inputSlotValid ? items.getStackInSlot(inputSlot) : inputBefore;
            ItemStack outputAfter = outputSlotValid ? items.getStackInSlot(outputSlot) : outputBefore;
            int movedAmount = moved ? estimateMovedAmount(tankBefore, tankAfter, inputBefore) : 0;
            return TankSlotTransferResult.of(this, moved, movedAmount, tankBefore, tankAfter,
                    inputBefore, inputAfter, outputBefore, outputAfter, inputSlotValid, outputSlotValid);
        }

        private int estimateMovedAmount(int tankBefore, int tankAfter, ItemStack inputBefore) {
            int delta = tankAfter - tankBefore;
            if (delta != 0) {
                return Math.abs(delta);
            }
            return movedBySimulationFallback(inputBefore);
        }

        private int movedBySimulationFallback(ItemStack inputBefore) {
            if (tank == null || inputBefore == null || inputBefore.isEmpty()) {
                return 0;
            }
            if (inputBefore.getItem() instanceof HbmInfiniteFluidItem infinite) {
                return Math.min(maxAmount, infinite.getAmount());
            }
            ItemStack single = inputBefore.copy();
            single.setCount(1);
            HbmFluidTank tankCopy = copyTank(tank);
            TransferResult result = direction == Direction.ITEM_TO_TANK
                    ? drainItemToTank(single, tankCopy, maxAmount, true)
                    : fillItemFromTank(single, tankCopy, maxAmount, true);
            if (result.moved()) {
                return result.amount();
            }
            return Math.min(maxAmount, switch (direction) {
                case ITEM_TO_TANK -> tank.getSpace();
                case TANK_TO_ITEM -> tank.getFill();
            });
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
