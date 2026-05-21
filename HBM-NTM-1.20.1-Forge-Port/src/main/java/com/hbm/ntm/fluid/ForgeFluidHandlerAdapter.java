package com.hbm.ntm.fluid;

import java.util.List;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ForgeFluidHandlerAdapter implements IFluidHandler {
    private final List<HbmFluidTank> tanks;
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
        this.tanks = List.copyOf(tanks);
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
        FluidType type = HbmFluidForgeMappings.fromForge(stack);
        return type != HbmFluids.NONE && tanks.get(tank).canAccept(type, inputPressure);
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
        for (HbmFluidTank tank : tanks) {
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
        for (HbmFluidTank tank : tanks) {
            FluidType type = tank.getTankType();
            if (tank.getFill() <= 0 || !HbmFluidForgeMappings.canExport(type)) {
                continue;
            }
            int drained = tank.drain(maxDrain, action.simulate());
            return HbmFluidForgeMappings.toForge(type, drained);
        }
        return FluidStack.EMPTY;
    }

    private int drainMatching(FluidType type, int amount, boolean simulate) {
        int remaining = amount;
        int drained = 0;
        for (HbmFluidTank tank : tanks) {
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

    private boolean isValidTank(int tank) {
        return tank >= 0 && tank < tanks.size();
    }
}
