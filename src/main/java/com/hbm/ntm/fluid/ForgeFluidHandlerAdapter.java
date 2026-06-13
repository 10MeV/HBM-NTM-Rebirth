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

    public List<HbmFluidTank> getVisibleTanks() {
        return tanks;
    }

    public List<HbmFluidTank> getInputTanks() {
        return inputTanks;
    }

    public List<HbmFluidTank> getOutputTanks() {
        return outputTanks;
    }

    public AdapterSnapshot createSnapshot() {
        return new AdapterSnapshot(
                HbmFluidGuiHelper.snapshotTanks(tanks),
                HbmFluidGuiHelper.snapshotTanks(inputTanks),
                HbmFluidGuiHelper.snapshotTanks(outputTanks),
                inputPressure,
                canFill,
                canDrain);
    }

    public static AdapterSnapshot emptySnapshot() {
        HbmFluidGuiHelper.TankSetSnapshot emptyTanks = HbmFluidGuiHelper.snapshotTanks(List.of());
        return new AdapterSnapshot(emptyTanks, emptyTanks, emptyTanks, 0, false, false);
    }

    public int previewFill(FluidStack resource) {
        return fill(resource, FluidAction.SIMULATE);
    }

    public ForgeFillReport previewFillReport(FluidStack resource) {
        return fillReport(resource, FluidAction.SIMULATE);
    }

    public ForgeFillReport fillReport(FluidStack resource, FluidAction action) {
        boolean simulate = action == null || action.simulate();
        boolean stackPresent = resource != null && !resource.isEmpty();
        FluidType type = stackPresent ? HbmFluidForgeMappings.fromForge(resource) : HbmFluids.NONE;
        boolean importMapped = type != HbmFluids.NONE;
        int requested = stackPresent ? resource.getAmount() : 0;
        if (!canFill || !stackPresent || !importMapped || requested <= 0) {
            return new ForgeFillReport(simulate, canFill, stackPresent, importMapped, type, requested, 0, List.of());
        }

        int remaining = requested;
        int filled = 0;
        List<TankFillDetail> details = new ArrayList<>();
        for (HbmFluidTank tank : inputTanks) {
            if (remaining <= 0) {
                break;
            }
            int before = tank.getFill();
            FluidType typeBefore = tank.getTankType();
            boolean eligible = HbmForgeFluidInterop.canFillFromForge(tank, inputPressure)
                    && tank.canAccept(type, inputPressure);
            int accepted = eligible ? tank.fill(type, remaining, inputPressure, simulate) : 0;
            int after = tank.getFill();
            FluidType typeAfter = tank.getTankType();
            filled += accepted;
            remaining -= accepted;
            details.add(TankFillDetail.of(tanks.indexOf(tank), tank, eligible, accepted,
                    typeBefore, typeAfter, before, after));
        }
        if (!simulate && filled > 0) {
            onChanged.run();
        }
        return new ForgeFillReport(simulate, canFill, true, true, type, requested, filled, details);
    }

    public FluidStack previewDrain(FluidStack resource) {
        return drain(resource, FluidAction.SIMULATE);
    }

    public ForgeDrainReport previewDrainReport(FluidStack resource) {
        return drainReport(resource, FluidAction.SIMULATE);
    }

    public FluidStack previewDrain(int maxDrain) {
        return drain(maxDrain, FluidAction.SIMULATE);
    }

    public ForgeDrainReport previewDrainReport(int maxDrain) {
        return drainReport(maxDrain, FluidAction.SIMULATE);
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
        return fillReport(resource, action).acceptedMb();
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return drainReport(resource, action).drainedStack();
    }

    public ForgeDrainReport drainReport(FluidStack resource, FluidAction action) {
        boolean simulate = action == null || action.simulate();
        boolean stackPresent = resource != null && !resource.isEmpty();
        FluidType type = stackPresent ? HbmFluidForgeMappings.fromForgeExport(resource) : HbmFluids.NONE;
        boolean exportMapped = type != HbmFluids.NONE;
        int requested = stackPresent ? resource.getAmount() : 0;
        if (!canDrain || !stackPresent || !exportMapped || requested <= 0) {
            return new ForgeDrainReport(simulate, canDrain, stackPresent, exportMapped, type, requested,
                    0, FluidStack.EMPTY, List.of());
        }
        DrainMatchReport match = drainMatchingReport(type, requested, simulate);
        FluidStack drainedStack = match.drainedMb() <= 0 ? FluidStack.EMPTY : new FluidStack(resource.getFluid(), match.drainedMb());
        return new ForgeDrainReport(simulate, canDrain, true, true, type, requested,
                match.drainedMb(), drainedStack, match.details());
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return drainReport(maxDrain, action).drainedStack();
    }

    public ForgeDrainReport drainReport(int maxDrain, FluidAction action) {
        boolean simulate = action == null || action.simulate();
        FluidType type = HbmFluids.NONE;
        boolean stackPresent = maxDrain > 0;
        if (!canDrain || maxDrain <= 0) {
            return new ForgeDrainReport(simulate, canDrain, stackPresent, false, type, Math.max(0, maxDrain),
                    0, FluidStack.EMPTY, List.of());
        }
        for (HbmFluidTank tank : outputTanks) {
            if (!HbmForgeFluidInterop.canExposeToForge(tank)) {
                continue;
            }
            type = tank.getTankType();
            break;
        }
        if (type == HbmFluids.NONE) {
            return new ForgeDrainReport(simulate, canDrain, true, false, type, maxDrain,
                    0, FluidStack.EMPTY, List.of());
        }
        DrainMatchReport match = drainMatchingReport(type, maxDrain, simulate);
        FluidStack drainedStack = match.drainedMb() <= 0 ? FluidStack.EMPTY : HbmFluidForgeMappings.toForge(type, match.drainedMb());
        return new ForgeDrainReport(simulate, canDrain, true, true, type, maxDrain,
                match.drainedMb(), drainedStack, match.details());
    }

    private int drainMatching(FluidType type, int amount, boolean simulate) {
        return drainMatchingReport(type, amount, simulate).drainedMb();
    }

    private DrainMatchReport drainMatchingReport(FluidType type, int amount, boolean simulate) {
        int remaining = amount;
        int drained = 0;
        List<TankDrainDetail> details = new ArrayList<>();
        for (HbmFluidTank tank : outputTanks) {
            if (remaining <= 0) {
                break;
            }
            int before = tank.getFill();
            FluidType typeBefore = tank.getTankType();
            boolean eligible = tank.getTankType() == type && HbmForgeFluidInterop.canExposeToForge(tank);
            int taken = eligible ? tank.drain(remaining, simulate) : 0;
            int after = tank.getFill();
            FluidType typeAfter = tank.getTankType();
            drained += taken;
            remaining -= taken;
            details.add(TankDrainDetail.of(tanks.indexOf(tank), tank, eligible, taken,
                    typeBefore, typeAfter, before, after));
        }
        if (!simulate && drained > 0) {
            onChanged.run();
        }
        return new DrainMatchReport(drained, details);
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

    public record AdapterSnapshot(
            HbmFluidGuiHelper.TankSetSnapshot visibleTanks,
            HbmFluidGuiHelper.TankSetSnapshot inputTanks,
            HbmFluidGuiHelper.TankSetSnapshot outputTanks,
            int inputPressure,
            boolean canFill,
            boolean canDrain) {
    }

    public record ForgeFillReport(
            boolean simulated,
            boolean adapterCanFill,
            boolean stackPresent,
            boolean importMapped,
            FluidType hbmType,
            int requestedMb,
            int acceptedMb,
            List<TankFillDetail> details) {
        public ForgeFillReport {
            hbmType = hbmType == null ? HbmFluids.NONE : hbmType;
            requestedMb = Math.max(0, requestedMb);
            acceptedMb = Math.max(0, acceptedMb);
            details = details == null ? List.of() : List.copyOf(details);
        }

        public boolean moved() {
            return acceptedMb > 0;
        }
    }

    public record ForgeDrainReport(
            boolean simulated,
            boolean adapterCanDrain,
            boolean requestPresent,
            boolean exportMapped,
            FluidType hbmType,
            int requestedMb,
            int drainedMb,
            FluidStack drainedStack,
            List<TankDrainDetail> details) {
        public ForgeDrainReport {
            hbmType = hbmType == null ? HbmFluids.NONE : hbmType;
            requestedMb = Math.max(0, requestedMb);
            drainedMb = Math.max(0, drainedMb);
            drainedStack = drainedStack == null ? FluidStack.EMPTY : drainedStack.copy();
            details = details == null ? List.of() : List.copyOf(details);
        }

        public boolean moved() {
            return drainedMb > 0;
        }
    }

    public record TankFillDetail(
            int visibleTankIndex,
            FluidType typeBefore,
            FluidType typeAfter,
            int pressure,
            int fillBefore,
            int fillAfter,
            int capacity,
            boolean eligible,
            int acceptedMb) {
        private static TankFillDetail of(int visibleTankIndex, HbmFluidTank tank, boolean eligible, int acceptedMb,
                FluidType typeBefore, FluidType typeAfter, int fillBefore, int fillAfter) {
            return new TankFillDetail(
                    visibleTankIndex,
                    typeBefore == null ? HbmFluids.NONE : typeBefore,
                    typeAfter == null ? HbmFluids.NONE : typeAfter,
                    tank == null ? 0 : tank.getPressure(),
                    fillBefore,
                    fillAfter,
                    tank == null ? 0 : tank.getMaxFill(),
                    eligible,
                    Math.max(0, acceptedMb));
        }
    }

    public record TankDrainDetail(
            int visibleTankIndex,
            FluidType typeBefore,
            FluidType typeAfter,
            int pressure,
            int fillBefore,
            int fillAfter,
            int capacity,
            boolean eligible,
            int drainedMb) {
        private static TankDrainDetail of(int visibleTankIndex, HbmFluidTank tank, boolean eligible, int drainedMb,
                FluidType typeBefore, FluidType typeAfter, int fillBefore, int fillAfter) {
            return new TankDrainDetail(
                    visibleTankIndex,
                    typeBefore == null ? HbmFluids.NONE : typeBefore,
                    typeAfter == null ? HbmFluids.NONE : typeAfter,
                    tank == null ? 0 : tank.getPressure(),
                    fillBefore,
                    fillAfter,
                    tank == null ? 0 : tank.getMaxFill(),
                    eligible,
                    Math.max(0, drainedMb));
        }
    }

    private record DrainMatchReport(int drainedMb, List<TankDrainDetail> details) {
        private DrainMatchReport {
            drainedMb = Math.max(0, drainedMb);
            details = details == null ? List.of() : List.copyOf(details);
        }
    }
}
