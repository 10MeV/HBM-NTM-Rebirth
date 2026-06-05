package com.hbm.ntm.fluid;

import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ForgeRecipeFluidHandlerAdapter extends ForgeFluidHandlerAdapter {
    private final List<HbmFluidTank> recipeInputTanks;
    private final Runnable onChanged;

    public ForgeRecipeFluidHandlerAdapter(List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks,
            int inputPressure, Runnable onChanged) {
        super(inputTanks, outputTanks, inputPressure, true, true, onChanged);
        this.recipeInputTanks = List.copyOf(inputTanks == null ? List.of() : inputTanks);
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
}
