package com.hbm.ntm.fluid;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fluids.FluidStack;

public class ForgeRecipeFluidHandlerAdapter extends ForgeFluidHandlerAdapter {
    private final List<HbmFluidTank> recipeVisibleTanks;
    private final List<HbmFluidTank> recipeInputTanks;
    private final List<HbmFluidTank> recipeOutputTanks;
    private final Runnable onChanged;

    public ForgeRecipeFluidHandlerAdapter(List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks,
            int inputPressure, Runnable onChanged) {
        super(inputTanks, outputTanks, inputPressure, true, true, onChanged);
        this.recipeInputTanks = List.copyOf(inputTanks == null ? List.of() : inputTanks);
        this.recipeOutputTanks = List.copyOf(outputTanks == null ? List.of() : outputTanks);
        this.recipeVisibleTanks = mergeVisibleTanks(this.recipeInputTanks, this.recipeOutputTanks);
        this.onChanged = onChanged == null ? () -> {
        } : onChanged;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (tank < 0 || tank >= recipeInputTanks.size() || stack == null || stack.isEmpty()) {
            return false;
        }
        HbmFluidTank hbmTank = recipeInputTanks.get(tank);
        FluidType type = HbmFluidForgeMappings.fromForge(stack);
        return hbmTank.getTankType() != HbmFluids.NONE && hbmTank.canAccept(type, hbmTank.getPressure());
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank < 0 || tank >= recipeVisibleTanks.size()) {
            return FluidStack.EMPTY;
        }
        HbmFluidTank hbmTank = recipeVisibleTanks.get(tank);
        return HbmFluidForgeMappings.toForge(hbmTank.getTankType(), hbmTank.getFill());
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidType type = HbmFluidForgeMappings.fromForgeExport(resource);
        if (type == HbmFluids.NONE) {
            return FluidStack.EMPTY;
        }
        int drained = drainMatchingRecipeOutput(type, resource.getAmount(), action.simulate());
        return drained <= 0 ? FluidStack.EMPTY : new FluidStack(resource.getFluid(), drained);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        FluidType type = HbmFluids.NONE;
        for (HbmFluidTank tank : recipeOutputTanks) {
            FluidType tankType = tank.getTankType();
            if (tank.getFill() <= 0 || !HbmFluidForgeMappings.canExport(tankType)) {
                continue;
            }
            type = tankType;
            break;
        }
        if (type == HbmFluids.NONE) {
            return FluidStack.EMPTY;
        }
        int drained = drainMatchingRecipeOutput(type, maxDrain, action.simulate());
        return drained <= 0 ? FluidStack.EMPTY : HbmFluidForgeMappings.toForge(type, drained);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty()) {
            return 0;
        }
        FluidType type = HbmFluidForgeMappings.fromForge(resource);
        if (type == HbmFluids.NONE) {
            return 0;
        }
        int remaining = resource.getAmount();
        int filled = 0;
        boolean simulate = action.simulate();
        for (HbmFluidTank tank : recipeInputTanks) {
            if (remaining <= 0) {
                break;
            }
            if (tank.getTankType() == HbmFluids.NONE) {
                continue;
            }
            int accepted = tank.fill(type, remaining, tank.getPressure(), simulate);
            filled += accepted;
            remaining -= accepted;
        }
        if (!simulate && filled > 0) {
            onChanged.run();
        }
        return filled;
    }

    private int drainMatchingRecipeOutput(FluidType type, int amount, boolean simulate) {
        int remaining = amount;
        int drained = 0;
        for (HbmFluidTank tank : recipeOutputTanks) {
            if (remaining <= 0) {
                break;
            }
            if (tank.getTankType() != type || tank.getFill() <= 0 || !HbmFluidForgeMappings.canExport(type)) {
                continue;
            }
            int taken = tank.drain(remaining, simulate);
            drained += taken;
            remaining -= taken;
        }
        if (!simulate && drained > 0) {
            onChanged.run();
        }
        return drained;
    }

    private static List<HbmFluidTank> mergeVisibleTanks(List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks) {
        List<HbmFluidTank> visible = new ArrayList<>();
        for (HbmFluidTank tank : inputTanks) {
            if (tank != null && !visible.contains(tank)) {
                visible.add(tank);
            }
        }
        for (HbmFluidTank tank : outputTanks) {
            if (tank != null && !visible.contains(tank)) {
                visible.add(tank);
            }
        }
        return List.copyOf(visible);
    }
}
