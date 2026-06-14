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
        super(safeTanks(inputTanks), safeTanks(outputTanks), inputPressure, true, true, onChanged);
        this.recipeInputTanks = safeTanks(inputTanks);
        this.recipeOutputTanks = safeTanks(outputTanks);
        this.recipeVisibleTanks = mergeVisibleTanks(this.recipeInputTanks, this.recipeOutputTanks);
        this.onChanged = onChanged == null ? () -> {
        } : onChanged;
    }

    public static ForgeRecipeFluidHandlerAdapter create(List<HbmFluidTank> inputTanks,
            List<HbmFluidTank> outputTanks, int inputPressure, Runnable onChanged) {
        return new ForgeRecipeFluidHandlerAdapter(inputTanks, outputTanks, inputPressure, onChanged);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (tank < 0 || tank >= recipeInputTanks.size() || stack == null || stack.isEmpty()) {
            return false;
        }
        HbmFluidTank hbmTank = recipeInputTanks.get(tank);
        if (hbmTank == null) {
            return false;
        }
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
        return drainRecipeReport(resource, action).drainedStack();
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return drainRecipeReport(maxDrain, action).drainedStack();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return fillRecipeReport(resource, action).acceptedMb();
    }

    public RecipeForgeFillReport previewRecipeFillReport(FluidStack resource) {
        return fillRecipeReport(resource, FluidAction.SIMULATE);
    }

    public RecipeForgeFillReport fillRecipeReport(FluidStack resource, FluidAction action) {
        boolean simulate = action == null || action.simulate();
        if (resource == null || resource.isEmpty()) {
            return new RecipeForgeFillReport(simulate, false, false, HbmFluids.NONE, 0, 0, List.of());
        }
        FluidType type = HbmFluidForgeMappings.fromForge(resource);
        if (type == HbmFluids.NONE) {
            return new RecipeForgeFillReport(simulate, true, false, HbmFluids.NONE,
                    resource.getAmount(), 0, List.of());
        }
        int remaining = resource.getAmount();
        int filled = 0;
        List<RecipeTankTransferDetail> details = new ArrayList<>();
        for (HbmFluidTank tank : recipeInputTanks) {
            if (remaining <= 0) {
                break;
            }
            HbmFluidTank.TankState before = state(tank);
            boolean eligible = tank != null && tank.getTankType() != HbmFluids.NONE;
            HbmFluidTank.TankMutationReport mutation = eligible
                    ? tank.fillReport(type, remaining, tank.getPressure(), simulate)
                    : new HbmFluidTank.TankMutationReport(
                            "fill", simulate, before, before, remaining, 0, 0, Math.max(0, remaining), false);
            int accepted = mutation.movedMb();
            details.add(new RecipeTankTransferDetail(
                    recipeVisibleTanks.indexOf(tank), recipeInputTanks.indexOf(tank), -1, eligible, mutation));
            if (accepted <= 0) {
                continue;
            }
            filled += accepted;
            remaining -= accepted;
        }
        if (!simulate && filled > 0) {
            onChanged.run();
        }
        return new RecipeForgeFillReport(simulate, true, true, type, resource.getAmount(), filled, details);
    }

    public RecipeForgeDrainReport previewRecipeDrainReport(FluidStack resource) {
        return drainRecipeReport(resource, FluidAction.SIMULATE);
    }

    public RecipeForgeDrainReport previewRecipeDrainReport(int maxDrain) {
        return drainRecipeReport(maxDrain, FluidAction.SIMULATE);
    }

    public RecipeForgeDrainReport drainRecipeReport(FluidStack resource, FluidAction action) {
        boolean simulate = action == null || action.simulate();
        boolean requestPresent = resource != null && !resource.isEmpty();
        FluidType type = requestPresent ? HbmFluidForgeMappings.fromForgeExport(resource) : HbmFluids.NONE;
        boolean exportMapped = type != HbmFluids.NONE;
        int requested = requestPresent ? resource.getAmount() : 0;
        if (!requestPresent || !exportMapped || requested <= 0) {
            return new RecipeForgeDrainReport(simulate, requestPresent, exportMapped, type, requested,
                    0, FluidStack.EMPTY, List.of());
        }
        RecipeDrainMatchReport match = drainMatchingRecipeOutputReport(type, requested, simulate);
        FluidStack drained = match.drainedMb() <= 0 ? FluidStack.EMPTY : new FluidStack(resource.getFluid(), match.drainedMb());
        return new RecipeForgeDrainReport(simulate, true, true, type, requested, match.drainedMb(), drained,
                match.details());
    }

    public RecipeForgeDrainReport drainRecipeReport(int maxDrain, FluidAction action) {
        boolean simulate = action == null || action.simulate();
        if (maxDrain <= 0) {
            return new RecipeForgeDrainReport(simulate, maxDrain > 0, false, HbmFluids.NONE,
                    Math.max(0, maxDrain), 0, FluidStack.EMPTY, List.of());
        }
        FluidType type = HbmFluids.NONE;
        for (HbmFluidTank tank : recipeOutputTanks) {
            if (tank == null) {
                continue;
            }
            FluidType tankType = tank.getTankType();
            if (tank.getFill() <= 0 || !HbmFluidForgeMappings.canExport(tankType)) {
                continue;
            }
            type = tankType;
            break;
        }
        if (type == HbmFluids.NONE) {
            return new RecipeForgeDrainReport(simulate, true, false, type, maxDrain,
                    0, FluidStack.EMPTY, List.of());
        }
        RecipeDrainMatchReport match = drainMatchingRecipeOutputReport(type, maxDrain, simulate);
        FluidStack drained = match.drainedMb() <= 0 ? FluidStack.EMPTY : HbmFluidForgeMappings.toForge(type, match.drainedMb());
        return new RecipeForgeDrainReport(simulate, true, true, type, maxDrain, match.drainedMb(), drained,
                match.details());
    }

    private RecipeDrainMatchReport drainMatchingRecipeOutputReport(FluidType type, int amount, boolean simulate) {
        int remaining = amount;
        int drained = 0;
        List<RecipeTankTransferDetail> details = new ArrayList<>();
        for (HbmFluidTank tank : recipeOutputTanks) {
            if (remaining <= 0) {
                break;
            }
            HbmFluidTank.TankState before = state(tank);
            boolean eligible = tank != null && tank.getTankType() == type && tank.getFill() > 0
                    && HbmFluidForgeMappings.canExport(type);
            HbmFluidTank.TankMutationReport mutation = eligible
                    ? tank.drainReport(remaining, simulate)
                    : new HbmFluidTank.TankMutationReport(
                            "drain", simulate, before, before, remaining, 0, 0, Math.max(0, remaining), false);
            int taken = mutation.movedMb();
            details.add(new RecipeTankTransferDetail(
                    recipeVisibleTanks.indexOf(tank), -1, recipeOutputTanks.indexOf(tank), eligible, mutation));
            if (taken <= 0) {
                continue;
            }
            drained += taken;
            remaining -= taken;
        }
        if (!simulate && drained > 0) {
            onChanged.run();
        }
        return new RecipeDrainMatchReport(drained, details);
    }

    private static HbmFluidTank.TankState state(HbmFluidTank tank) {
        return tank == null ? new HbmFluidTank.TankState(HbmFluids.NONE, 0, 0, 0) : tank.snapshot();
    }

    private static List<HbmFluidTank> safeTanks(List<HbmFluidTank> tanks) {
        if (tanks == null || tanks.isEmpty()) {
            return List.of();
        }
        List<HbmFluidTank> result = new ArrayList<>();
        for (HbmFluidTank tank : tanks) {
            if (tank != null) {
                result.add(tank);
            }
        }
        return List.copyOf(result);
    }

    public record RecipeForgeFillReport(
            boolean simulated,
            boolean requestPresent,
            boolean importMapped,
            FluidType hbmType,
            int requestedMb,
            int acceptedMb,
            List<RecipeTankTransferDetail> details) {
        public RecipeForgeFillReport {
            hbmType = hbmType == null ? HbmFluids.NONE : hbmType;
            requestedMb = Math.max(0, requestedMb);
            acceptedMb = Math.max(0, acceptedMb);
            details = details == null ? List.of() : List.copyOf(details);
        }

        public boolean moved() {
            return acceptedMb > 0;
        }
    }

    public record RecipeForgeDrainReport(
            boolean simulated,
            boolean requestPresent,
            boolean exportMapped,
            FluidType hbmType,
            int requestedMb,
            int drainedMb,
            FluidStack drainedStack,
            List<RecipeTankTransferDetail> details) {
        public RecipeForgeDrainReport {
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

    public record RecipeTankTransferDetail(
            int visibleTankIndex,
            int inputTankIndex,
            int outputTankIndex,
            boolean eligible,
            HbmFluidTank.TankMutationReport mutation) {
        public RecipeTankTransferDetail {
            inputTankIndex = inputTankIndex < 0 ? -1 : inputTankIndex;
            outputTankIndex = outputTankIndex < 0 ? -1 : outputTankIndex;
            mutation = mutation == null
                    ? new HbmFluidTank.TankMutationReport(
                            "unknown", true, state(null), state(null), 0, 0, 0, 0, false)
                    : mutation;
        }

        public int movedMb() {
            return mutation.movedMb();
        }
    }

    private record RecipeDrainMatchReport(int drainedMb, List<RecipeTankTransferDetail> details) {
        private RecipeDrainMatchReport {
            drainedMb = Math.max(0, drainedMb);
            details = details == null ? List.of() : List.copyOf(details);
        }
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
