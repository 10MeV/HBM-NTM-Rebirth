package com.hbm.ntm.fluid;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ForgeFluidHandlerAdapter implements IFluidHandler {
    private final List<HbmFluidTank> tanks;
    private final List<HbmFluidTank> inputTanks;
    private final List<HbmFluidTank> outputTanks;
    private final int inputPressure;
    private final boolean canFill;
    private final boolean canDrain;
    private final Runnable onChanged;

    public ForgeFluidHandlerAdapter(List<HbmFluidTank> tanks) {
        this(tanks, 0, true, true);
    }

    public ForgeFluidHandlerAdapter(List<HbmFluidTank> tanks, int inputPressure) {
        this(tanks, inputPressure, true, true);
    }

    public ForgeFluidHandlerAdapter(List<HbmFluidTank> tanks, int inputPressure, boolean canFill, boolean canDrain) {
        this(tanks, inputPressure, canFill, canDrain, () -> {
        });
    }

    public ForgeFluidHandlerAdapter(List<HbmFluidTank> tanks, int inputPressure, boolean canFill, boolean canDrain, Runnable onChanged) {
        this(tanks, tanks, inputPressure, canFill, canDrain, onChanged);
    }

    public ForgeFluidHandlerAdapter(List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int inputPressure,
            boolean canFill, boolean canDrain, Runnable onChanged) {
        this.inputTanks = List.copyOf(inputTanks == null ? List.of() : inputTanks);
        this.outputTanks = List.copyOf(outputTanks == null ? List.of() : outputTanks);
        this.tanks = mergeVisibleTanks(this.inputTanks, this.outputTanks);
        this.inputPressure = HbmFluidTank.clampPressure(inputPressure);
        this.canFill = canFill;
        this.canDrain = canDrain;
        this.onChanged = onChanged == null ? () -> {
        } : onChanged;
    }

    @Override
    public int getTanks() {
        return tanks.size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (!isValidTank(tank)) {
            return FluidStack.EMPTY;
        }
        HbmFluidTank hbmTank = tanks.get(tank);
        if (!HbmForgeFluidInterop.canExposeToForge(hbmTank)) {
            return FluidStack.EMPTY;
        }
        return HbmFluidForgeMappings.toForge(hbmTank.getTankType(), hbmTank.getFill());
    }

    @Override
    public int getTankCapacity(int tank) {
        return isValidTank(tank) ? tanks.get(tank).getMaxFill() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (!canFill || !isValidTank(tank) || stack == null || stack.isEmpty()) {
            return false;
        }
        HbmFluidTank hbmTank = tanks.get(tank);
        if (!HbmForgeFluidInterop.canFillFromForge(hbmTank, inputPressure)) {
            return false;
        }
        if (!inputTanks.contains(hbmTank)) {
            return false;
        }
        FluidType type = HbmFluidForgeMappings.fromForge(stack);
        return type != HbmFluids.NONE && hbmTank.canAccept(type, inputPressure);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!canFill || resource == null || resource.isEmpty()) {
            return 0;
        }
        FluidType type = HbmFluidForgeMappings.fromForge(resource);
        if (type == HbmFluids.NONE) {
            return 0;
        }
        int remaining = resource.getAmount();
        int filled = 0;
        boolean simulate = action.simulate();
        for (HbmFluidTank tank : inputTanks) {
            if (!HbmForgeFluidInterop.canFillFromForge(tank, inputPressure)) {
                continue;
            }
            if (remaining <= 0) {
                break;
            }
            int accepted = tank.fill(type, remaining, inputPressure, simulate);
            filled += accepted;
            remaining -= accepted;
        }
        if (!simulate && filled > 0) {
            onChanged.run();
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!canDrain || resource == null || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidType type = HbmFluidForgeMappings.fromForge(resource);
        if (type == HbmFluids.NONE) {
            return FluidStack.EMPTY;
        }
        int drained = drainMatching(type, resource.getAmount(), action.simulate());
        return drained <= 0 ? FluidStack.EMPTY : new FluidStack(resource.getFluid(), drained);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (!canDrain || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        for (HbmFluidTank tank : outputTanks) {
            FluidType type = tank.getTankType();
            if (!HbmForgeFluidInterop.canExposeToForge(tank)) {
                continue;
            }
            int drained = tank.drain(maxDrain, action.simulate());
            if (!action.simulate() && drained > 0) {
                onChanged.run();
            }
            return HbmFluidForgeMappings.toForge(type, drained);
        }
        return FluidStack.EMPTY;
    }

    private int drainMatching(FluidType type, int amount, boolean simulate) {
        int remaining = amount;
        int drained = 0;
        for (HbmFluidTank tank : outputTanks) {
            if (remaining <= 0) {
                break;
            }
            if (tank.getTankType() != type || !HbmForgeFluidInterop.canExposeToForge(tank)) {
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

    private boolean isValidTank(int tank) {
        return tank >= 0 && tank < tanks.size();
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
