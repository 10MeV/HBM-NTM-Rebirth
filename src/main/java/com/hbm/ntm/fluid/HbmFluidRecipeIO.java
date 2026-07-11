package com.hbm.ntm.fluid;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public final class HbmFluidRecipeIO {
    private HbmFluidRecipeIO() {
    }

    public static RecipeTankSetupReport setupRecipeTanks(
            List<HbmFluidStack> inputStacks, List<HbmFluidStack> outputStacks,
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int defaultCapacity) {
        List<HbmFluidStack> safeInputs = safeStacks(inputStacks);
        List<HbmFluidStack> safeOutputs = safeStacks(outputStacks);
        List<HbmFluidTank> safeInputTanks = safeTanks(inputTanks);
        List<HbmFluidTank> safeOutputTanks = safeTanks(outputTanks);
        List<TankConformReport> inputReports = new ArrayList<>();
        List<TankConformReport> outputReports = new ArrayList<>();
        for (int i = 0; i < safeInputTanks.size(); i++) {
            inputReports.add(conformTankReport(
                    i, safeInputTanks.get(i), i < safeInputs.size() ? safeInputs.get(i) : null, defaultCapacity));
        }
        for (int i = 0; i < safeOutputTanks.size(); i++) {
            outputReports.add(conformTankReport(
                    i, safeOutputTanks.get(i), i < safeOutputs.size() ? safeOutputs.get(i) : null, defaultCapacity));
        }
        return new RecipeTankSetupReport(inputReports, outputReports,
                Math.max(0, safeInputs.size() - safeInputTanks.size()),
                Math.max(0, safeOutputs.size() - safeOutputTanks.size()));
    }

    public static RecipeTankSetupReport setupLegacyFixedRecipeTanks(
            @Nullable List<HbmFluidStack> inputStacks, @Nullable List<HbmFluidStack> outputStacks,
            @Nullable List<HbmFluidTank> inputTanks, @Nullable List<HbmFluidTank> outputTanks) {
        return setupRecipeTanks(inputStacks, outputStacks, inputTanks, outputTanks, 0);
    }

    public static boolean setupRecipeTanksChanged(
            @Nullable List<HbmFluidStack> inputStacks, @Nullable List<HbmFluidStack> outputStacks,
            @Nullable List<HbmFluidTank> inputTanks, @Nullable List<HbmFluidTank> outputTanks, int defaultCapacity) {
        return setupRecipeTankGroupChanged(inputStacks, inputTanks, defaultCapacity)
                | setupRecipeTankGroupChanged(outputStacks, outputTanks, defaultCapacity);
    }

    public static boolean setupLegacyFixedRecipeTanksChanged(
            @Nullable List<HbmFluidStack> inputStacks, @Nullable List<HbmFluidStack> outputStacks,
            @Nullable List<HbmFluidTank> inputTanks, @Nullable List<HbmFluidTank> outputTanks) {
        return setupRecipeTanksChanged(inputStacks, outputStacks, inputTanks, outputTanks, 0);
    }

    public static boolean processLegacyFixedRecipeIo(
            List<HbmFluidStack> inputStacks, List<HbmFluidStack> outputStacks,
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, boolean simulate) {
        return processRecipeIo(inputStacks, outputStacks, inputTanks, outputTanks, simulate);
    }

    public static RecipeFluidIoProcessReport processLegacyFixedRecipeIoReport(
            List<HbmFluidStack> inputStacks, List<HbmFluidStack> outputStacks,
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, boolean simulate) {
        return processRecipeIoReport(inputStacks, outputStacks, inputTanks, outputTanks, simulate);
    }

    public static HbmFluidStack requirementFromTank(HbmFluidTank tank, int amount) {
        HbmFluidTank.TankState state = snapshot(tank);
        return new HbmFluidStack(state.type(), amount, state.pressure());
    }

    public static TankConformReport conformTankReport(
            HbmFluidTank tank, @Nullable HbmFluidStack stack, int defaultCapacity) {
        return conformTankReport(0, tank, stack, defaultCapacity);
    }

    public static TankConformReport conformTankReport(
            int index, HbmFluidTank tank, @Nullable HbmFluidStack stack, int defaultCapacity) {
        HbmFluidTank.TankState before = snapshot(tank);
        if (tank == null) {
            return new TankConformReport(index, stack, defaultCapacity, true, before, before, 0, false);
        }
        int overflow = 0;
        if (stack == null || stack.isEmpty()) {
            tank.resetTankReport();
            return new TankConformReport(index, stack, defaultCapacity, false, before, tank.snapshot(), overflow, true);
        }
        tank.setTankTypeReport(stack.type());
        tank.withPressureReport(stack.pressure());
        if (defaultCapacity > 0) {
            int targetCapacity = Math.max(Math.max(tank.getFill(), stack.amount() * 2), defaultCapacity);
            overflow = tank.changeTankSizeReport(targetCapacity).overflowMb();
        }
        return new TankConformReport(index, stack, defaultCapacity, false, before, tank.snapshot(), overflow, false);
    }

    public static FluidStackSetCheckReport inspectInputs(List<HbmFluidStack> requiredStacks,
            List<HbmFluidTank> inputTanks) {
        List<HbmFluidStack> required = safeStacks(requiredStacks);
        List<HbmFluidTank> tanks = safeTanks(inputTanks);
        int checked = Math.min(required.size(), tanks.size());
        List<FluidStackTankCheck> details = new ArrayList<>();
        for (int i = 0; i < checked; i++) {
            details.add(FluidStackTankCheck.input(i, required.get(i), tanks.get(i)));
        }
        return new FluidStackSetCheckReport(details, required.size(), tanks.size(), Math.max(0, required.size() - checked));
    }

    public static FluidStackSetCheckReport inspectOutputs(List<HbmFluidStack> outputStacks,
            List<HbmFluidTank> outputTanks) {
        List<HbmFluidStack> outputs = safeStacks(outputStacks);
        List<HbmFluidTank> tanks = safeTanks(outputTanks);
        int checked = Math.min(outputs.size(), tanks.size());
        List<FluidStackTankCheck> details = new ArrayList<>();
        for (int i = 0; i < checked; i++) {
            details.add(FluidStackTankCheck.output(i, outputs.get(i), tanks.get(i)));
        }
        return new FluidStackSetCheckReport(details, outputs.size(), tanks.size(), Math.max(0, outputs.size() - checked));
    }

    public static FluidStackSetTransferReport consumeInputsReport(List<HbmFluidStack> requiredStacks,
            List<HbmFluidTank> inputTanks, boolean simulate) {
        FluidStackSetCheckReport check = inspectInputs(requiredStacks, inputTanks);
        List<HbmFluidStack> required = safeStacks(requiredStacks);
        List<HbmFluidTank> tanks = safeTanks(inputTanks);
        int checked = Math.min(required.size(), tanks.size());
        List<FluidStackTankTransfer> transfers = new ArrayList<>();
        for (int i = 0; i < checked; i++) {
            HbmFluidStack stack = required.get(i);
            HbmFluidTank tank = tanks.get(i);
            HbmFluidTank.TankState state = snapshot(tank);
            HbmFluidTank.TankMutationReport mutation = stack.isEmpty()
                    ? new HbmFluidTank.TankMutationReport(
                            "drain", simulate, state, state, 0, 0, 0, 0, false)
                    : tank == null
                            ? new HbmFluidTank.TankMutationReport(
                                    "drain", simulate, state, state, stack.amount(), 0, 0, stack.amount(), false)
                            : tank.drainReport(stack.amount(), simulate);
            transfers.add(new FluidStackTankTransfer(i, stack, mutation));
        }
        return new FluidStackSetTransferReport(check, transfers, simulate);
    }

    public static boolean consumeInputs(List<HbmFluidStack> requiredStacks, List<HbmFluidTank> inputTanks,
            boolean simulate) {
        return canConsumeInputs(requiredStacks, inputTanks)
                && (simulate || consumeInputsUnchecked(requiredStacks, inputTanks));
    }

    public static FluidStackSetTransferReport produceOutputsReport(List<HbmFluidStack> outputStacks,
            List<HbmFluidTank> outputTanks, boolean simulate) {
        FluidStackSetCheckReport check = inspectOutputs(outputStacks, outputTanks);
        List<HbmFluidStack> outputs = safeStacks(outputStacks);
        List<HbmFluidTank> tanks = safeTanks(outputTanks);
        int checked = Math.min(outputs.size(), tanks.size());
        List<FluidStackTankTransfer> transfers = new ArrayList<>();
        for (int i = 0; i < checked; i++) {
            HbmFluidStack stack = outputs.get(i);
            HbmFluidTank tank = tanks.get(i);
            HbmFluidTank.TankState state = snapshot(tank);
            HbmFluidTank.TankMutationReport mutation = stack.isEmpty()
                    ? new HbmFluidTank.TankMutationReport(
                            "fill", simulate, state, state, 0, 0, 0, 0, false)
                    : tank == null
                            ? new HbmFluidTank.TankMutationReport(
                                    "fill", simulate, state, state, stack.amount(), 0, 0, stack.amount(), false)
                            : tank.fillReport(stack.type(), stack.amount(), stack.pressure(), simulate);
            transfers.add(new FluidStackTankTransfer(i, stack, mutation));
        }
        return new FluidStackSetTransferReport(check, transfers, simulate);
    }

    public static boolean produceOutputs(List<HbmFluidStack> outputStacks, List<HbmFluidTank> outputTanks,
            boolean simulate) {
        return canProduceOutputs(outputStacks, outputTanks)
                && (simulate || produceOutputsUnchecked(outputStacks, outputTanks));
    }

    public static RecipeFluidIoCheckReport inspectRecipeIo(List<HbmFluidStack> inputStacks,
            List<HbmFluidStack> outputStacks, List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks) {
        return new RecipeFluidIoCheckReport(
                inspectInputs(inputStacks, inputTanks),
                inspectOutputs(outputStacks, outputTanks));
    }

    public static RecipeFluidIoProcessReport previewRecipeIo(List<HbmFluidStack> inputStacks,
            List<HbmFluidStack> outputStacks, List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks) {
        return processRecipeIoReport(inputStacks, outputStacks, inputTanks, outputTanks, true);
    }

    public static boolean canProcessRecipeIo(List<HbmFluidStack> inputStacks,
            List<HbmFluidStack> outputStacks, List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks) {
        return canConsumeInputs(inputStacks, inputTanks)
                && canProduceOutputs(outputStacks, outputTanks);
    }

    public static boolean processRecipeIo(List<HbmFluidStack> inputStacks,
            List<HbmFluidStack> outputStacks, List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks,
            boolean simulate) {
        if (!canProcessRecipeIo(inputStacks, outputStacks, inputTanks, outputTanks)) {
            return false;
        }
        if (simulate) {
            return true;
        }
        return consumeInputsUnchecked(inputStacks, inputTanks)
                && produceOutputsUnchecked(outputStacks, outputTanks);
    }

    public static RecipeFluidIoProcessReport processRecipeIoReport(List<HbmFluidStack> inputStacks,
            List<HbmFluidStack> outputStacks, List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks,
            boolean simulate) {
        RecipeFluidIoCheckReport check = inspectRecipeIo(inputStacks, outputStacks, inputTanks, outputTanks);
        if (!check.complete()) {
            return RecipeFluidIoProcessReport.skipped(check, simulate);
        }
        FluidStackSetTransferReport inputs = consumeInputsReport(inputStacks, inputTanks, simulate);
        FluidStackSetTransferReport outputs = produceOutputsReport(outputStacks, outputTanks, simulate);
        return new RecipeFluidIoProcessReport(check, inputs, outputs, simulate, false);
    }

    public static boolean canConsumeInputs(List<HbmFluidStack> requiredStacks, List<HbmFluidTank> inputTanks) {
        int checked = Math.min(size(requiredStacks), size(inputTanks));
        for (int i = 0; i < checked; i++) {
            HbmFluidStack stack = stackAt(requiredStacks, i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            HbmFluidTank tank = tankAt(inputTanks, i);
            if (tank == null
                    || tank.getTankType() != stack.type()
                    || tank.getPressure() != HbmFluidTank.clampPressure(stack.pressure())
                    || tank.getFill() < stack.amount()) {
                return false;
            }
        }
        return true;
    }

    public static boolean canProduceOutputs(List<HbmFluidStack> outputStacks, List<HbmFluidTank> outputTanks) {
        int checked = Math.min(size(outputStacks), size(outputTanks));
        for (int i = 0; i < checked; i++) {
            HbmFluidStack stack = stackAt(outputStacks, i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            HbmFluidTank tank = tankAt(outputTanks, i);
            if (tank == null || !tank.canAccept(stack.type(), stack.pressure()) || tank.getSpace() < stack.amount()) {
                return false;
            }
        }
        return true;
    }

    private static HbmFluidTank.TankState snapshot(@Nullable HbmFluidTank tank) {
        return tank == null ? new HbmFluidTank.TankState(HbmFluids.NONE, 0, 0, 0) : tank.snapshot();
    }

    private static boolean setupRecipeTankGroupChanged(@Nullable List<HbmFluidStack> stacks,
            @Nullable List<HbmFluidTank> tanks, int defaultCapacity) {
        if (tanks == null || tanks.isEmpty()) {
            return false;
        }
        boolean changed = false;
        for (int i = 0; i < tanks.size(); i++) {
            changed |= conformTankChanged(tanks.get(i), stackAt(stacks, i), defaultCapacity);
        }
        return changed;
    }

    public static boolean conformTankChanged(@Nullable HbmFluidTank tank, @Nullable HbmFluidStack stack,
            int defaultCapacity) {
        if (stack == null || stack.isEmpty()) {
            return conformTankChanged(tank, HbmFluids.NONE, 0, 0, defaultCapacity);
        }
        return conformTankChanged(tank, stack.type(), stack.amount(), stack.pressure(), defaultCapacity);
    }

    public static boolean conformTankChanged(@Nullable HbmFluidTank tank, @Nullable FluidType type, int amount,
            int pressure, int defaultCapacity) {
        if (tank == null) {
            return false;
        }
        FluidType beforeType = tank.getTankType();
        int beforeFill = tank.getFill();
        int beforeCapacity = tank.getMaxFill();
        int beforePressure = tank.getPressure();
        FluidType targetType = type == null ? HbmFluids.NONE : type;
        if (targetType == HbmFluids.NONE || amount <= 0) {
            tank.resetTank();
        } else {
            tank.setTankType(targetType);
            tank.withPressure(pressure);
            if (defaultCapacity > 0) {
                long recipeCapacity = (long) amount * 2L;
                long target = Math.max(Math.max((long) tank.getFill(), recipeCapacity), defaultCapacity);
                tank.changeTankSize((int) Math.min(Integer.MAX_VALUE, target));
            }
        }
        return beforeType != tank.getTankType()
                || beforeFill != tank.getFill()
                || beforeCapacity != tank.getMaxFill()
                || beforePressure != tank.getPressure();
    }

    public static boolean canConsumeInput(@Nullable HbmFluidTank tank, @Nullable HbmFluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return true;
        }
        return tank != null
                && tank.getTankType() == stack.type()
                && tank.getPressure() == HbmFluidTank.clampPressure(stack.pressure())
                && tank.getFill() >= stack.amount();
    }

    public static boolean canConsumeInput(@Nullable HbmFluidTank tank, int amount) {
        return amount <= 0 || (tank != null && tank.getFill() >= amount);
    }

    public static boolean consumeInput(@Nullable HbmFluidTank tank, @Nullable HbmFluidStack stack, boolean simulate) {
        if (!canConsumeInput(tank, stack)) {
            return false;
        }
        return stack == null || stack.isEmpty() || simulate || tank.drain(stack.amount(), false) == stack.amount();
    }

    public static boolean consumeInput(@Nullable HbmFluidTank tank, int amount, boolean simulate) {
        if (!canConsumeInput(tank, amount)) {
            return false;
        }
        return amount <= 0 || simulate || tank.drain(amount, false) == amount;
    }

    public static boolean canProduceOutput(@Nullable HbmFluidTank tank, @Nullable HbmFluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return true;
        }
        return canProduceOutput(tank, stack.type(), stack.amount(), stack.pressure());
    }

    public static boolean canProduceOutput(@Nullable HbmFluidTank tank, @Nullable FluidType type, int amount,
            int pressure) {
        FluidType outputType = type == null ? HbmFluids.NONE : type;
        return outputType == HbmFluids.NONE || amount <= 0
                || (tank != null && tank.canAccept(outputType, pressure) && tank.getSpace() >= amount);
    }

    public static boolean produceOutput(@Nullable HbmFluidTank tank, @Nullable HbmFluidStack stack, boolean simulate) {
        if (!canProduceOutput(tank, stack)) {
            return false;
        }
        return stack == null || stack.isEmpty() || simulate
                || tank.fill(stack.type(), stack.amount(), stack.pressure(), false) == stack.amount();
    }

    public static boolean produceOutput(@Nullable HbmFluidTank tank, @Nullable FluidType type, int amount,
            int pressure, boolean simulate) {
        if (!canProduceOutput(tank, type, amount, pressure)) {
            return false;
        }
        FluidType outputType = type == null ? HbmFluids.NONE : type;
        return outputType == HbmFluids.NONE || amount <= 0 || simulate
                || tank.fill(outputType, amount, pressure, false) == amount;
    }

    private static boolean consumeInputsUnchecked(List<HbmFluidStack> requiredStacks, List<HbmFluidTank> inputTanks) {
        int checked = Math.min(size(requiredStacks), size(inputTanks));
        for (int i = 0; i < checked; i++) {
            HbmFluidStack stack = stackAt(requiredStacks, i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            HbmFluidTank tank = tankAt(inputTanks, i);
            if (tank == null || tank.drain(stack.amount(), false) != stack.amount()) {
                return false;
            }
        }
        return true;
    }

    private static boolean produceOutputsUnchecked(List<HbmFluidStack> outputStacks, List<HbmFluidTank> outputTanks) {
        int checked = Math.min(size(outputStacks), size(outputTanks));
        for (int i = 0; i < checked; i++) {
            HbmFluidStack stack = stackAt(outputStacks, i);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            HbmFluidTank tank = tankAt(outputTanks, i);
            if (tank == null || tank.fill(stack.type(), stack.amount(), stack.pressure(), false) != stack.amount()) {
                return false;
            }
        }
        return true;
    }

    private static int size(@Nullable List<?> list) {
        return list == null ? 0 : list.size();
    }

    @Nullable
    private static HbmFluidStack stackAt(@Nullable List<HbmFluidStack> stacks, int index) {
        return stacks == null || index < 0 || index >= stacks.size() ? null : stacks.get(index);
    }

    @Nullable
    private static HbmFluidTank tankAt(@Nullable List<HbmFluidTank> tanks, int index) {
        return tanks == null || index < 0 || index >= tanks.size() ? null : tanks.get(index);
    }

    private static List<HbmFluidStack> safeStacks(@Nullable List<HbmFluidStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }
        List<HbmFluidStack> result = new ArrayList<>();
        for (HbmFluidStack stack : stacks) {
            result.add(stack == null ? new HbmFluidStack(HbmFluids.NONE, 0) : stack);
        }
        return List.copyOf(result);
    }

    private static List<HbmFluidTank> safeTanks(@Nullable List<HbmFluidTank> tanks) {
        if (tanks == null || tanks.isEmpty()) {
            return List.of();
        }
        List<HbmFluidTank> result = new ArrayList<>();
        for (HbmFluidTank tank : tanks) {
            result.add(tank);
        }
        return result;
    }

    public record RecipeTankSetupReport(
            List<TankConformReport> inputTanks,
            List<TankConformReport> outputTanks,
            int ignoredInputStacks,
            int ignoredOutputStacks) {
        public RecipeTankSetupReport {
            inputTanks = inputTanks == null ? List.of() : List.copyOf(inputTanks);
            outputTanks = outputTanks == null ? List.of() : List.copyOf(outputTanks);
            ignoredInputStacks = Math.max(0, ignoredInputStacks);
            ignoredOutputStacks = Math.max(0, ignoredOutputStacks);
        }

        public boolean changed() {
            return inputTanks.stream().anyMatch(TankConformReport::changed)
                    || outputTanks.stream().anyMatch(TankConformReport::changed);
        }
    }

    public record TankConformReport(
            int index,
            @Nullable HbmFluidStack targetStack,
            int defaultCapacity,
            boolean missingTank,
            HbmFluidTank.TankState before,
            HbmFluidTank.TankState after,
            int overflowMb,
            boolean reset) {
        public TankConformReport {
            defaultCapacity = Math.max(0, defaultCapacity);
            before = before == null ? new HbmFluidTank.TankState(HbmFluids.NONE, 0, 0, 0) : before;
            after = after == null ? before : after;
            overflowMb = Math.max(0, overflowMb);
        }

        public boolean changed() {
            return before.type() != after.type()
                    || before.fillMb() != after.fillMb()
                    || before.capacityMb() != after.capacityMb()
                    || before.pressure() != after.pressure();
        }
    }

    public record FluidStackSetCheckReport(
            List<FluidStackTankCheck> details,
            int requestedStacks,
            int availableTanks,
            int ignoredStacks) {
        public FluidStackSetCheckReport {
            details = details == null ? List.of() : List.copyOf(details);
            requestedStacks = Math.max(0, requestedStacks);
            availableTanks = Math.max(0, availableTanks);
            ignoredStacks = Math.max(0, ignoredStacks);
        }

        public boolean complete() {
            return details.stream().allMatch(FluidStackTankCheck::satisfied);
        }
    }

    public record FluidStackTankCheck(
            int index,
            HbmFluidStack stack,
            HbmFluidTank.TankState tank,
            boolean matchesType,
            boolean matchesPressure,
            int requestedMb,
            int availableMb,
            int missingMb,
            boolean satisfied) {
        public FluidStackTankCheck {
            stack = stack == null ? new HbmFluidStack(HbmFluids.NONE, 0) : stack;
            tank = tank == null ? new HbmFluidTank.TankState(HbmFluids.NONE, 0, 0, 0) : tank;
            requestedMb = Math.max(0, requestedMb);
            availableMb = Math.max(0, availableMb);
            missingMb = Math.max(0, missingMb);
        }

        private static FluidStackTankCheck input(int index, HbmFluidStack stack, HbmFluidTank tank) {
            HbmFluidTank.TankState state = snapshot(tank);
            if (stack == null || stack.isEmpty()) {
                return new FluidStackTankCheck(index, stack, state, true, true, 0, 0, 0, true);
            }
            boolean type = state.type() == stack.type();
            boolean pressure = state.pressure() == HbmFluidTank.clampPressure(stack.pressure());
            int missing = Math.max(0, stack.amount() - state.fillMb());
            return new FluidStackTankCheck(index, stack, state, type, pressure, stack.amount(), state.fillMb(),
                    missing, type && pressure && missing == 0);
        }

        private static FluidStackTankCheck output(int index, HbmFluidStack stack, HbmFluidTank tank) {
            HbmFluidTank.TankState state = snapshot(tank);
            if (stack == null || stack.isEmpty()) {
                return new FluidStackTankCheck(index, stack, state, true, true, 0, 0, 0, true);
            }
            HbmFluidTank.TankMutationReport fill = tank == null
                    ? new HbmFluidTank.TankMutationReport(
                            "fill", true, state, state, stack.amount(), 0, 0, stack.amount(), false)
                    : tank.fillReport(stack.type(), stack.amount(), stack.pressure(), true);
            boolean type = state.type() == stack.type() || state.type() == HbmFluids.NONE;
            boolean pressure = state.pressure() == HbmFluidTank.clampPressure(stack.pressure());
            int missing = Math.max(0, stack.amount() - fill.movedMb());
            return new FluidStackTankCheck(index, stack, state, type, pressure, stack.amount(), fill.movedMb(),
                    missing, type && pressure && missing == 0);
        }
    }

    public record FluidStackSetTransferReport(
            FluidStackSetCheckReport check,
            List<FluidStackTankTransfer> transfers,
            boolean simulated) {
        public FluidStackSetTransferReport {
            check = check == null ? new FluidStackSetCheckReport(List.of(), 0, 0, 0) : check;
            transfers = transfers == null ? List.of() : List.copyOf(transfers);
        }

        public boolean complete() {
            return check.complete()
                    && transfers.stream().allMatch(FluidStackTankTransfer::satisfied);
        }

        public int movedMb() {
            int moved = 0;
            for (FluidStackTankTransfer transfer : transfers) {
                moved += transfer.movedMb();
            }
            return moved;
        }
    }

    public record FluidStackTankTransfer(
            int index,
            HbmFluidStack stack,
            HbmFluidTank.TankMutationReport mutation) {
        public FluidStackTankTransfer {
            stack = stack == null ? new HbmFluidStack(HbmFluids.NONE, 0) : stack;
            mutation = mutation == null
                    ? new HbmFluidTank.TankMutationReport(
                            "unknown", true, new HbmFluidTank.TankState(HbmFluids.NONE, 0, 0, 0),
                            new HbmFluidTank.TankState(HbmFluids.NONE, 0, 0, 0), 0, 0, 0, 0, false)
                    : mutation;
        }

        public int movedMb() {
            return mutation.movedMb();
        }

        public boolean satisfied() {
            return stack.isEmpty() || mutation.movedMb() == stack.amount();
        }
    }

    public record RecipeFluidIoCheckReport(
            FluidStackSetCheckReport inputCheck,
            FluidStackSetCheckReport outputCheck) {
        public RecipeFluidIoCheckReport {
            inputCheck = inputCheck == null ? new FluidStackSetCheckReport(List.of(), 0, 0, 0) : inputCheck;
            outputCheck = outputCheck == null ? new FluidStackSetCheckReport(List.of(), 0, 0, 0) : outputCheck;
        }

        public boolean complete() {
            return inputCheck.complete() && outputCheck.complete();
        }

        public int ignoredStacks() {
            return inputCheck.ignoredStacks() + outputCheck.ignoredStacks();
        }
    }

    public record RecipeFluidIoProcessReport(
            RecipeFluidIoCheckReport check,
            FluidStackSetTransferReport inputTransfer,
            FluidStackSetTransferReport outputTransfer,
            boolean simulated,
            boolean skipped) {
        public RecipeFluidIoProcessReport {
            check = check == null
                    ? new RecipeFluidIoCheckReport(null, null)
                    : check;
            inputTransfer = inputTransfer == null
                    ? new FluidStackSetTransferReport(check.inputCheck(), List.of(), simulated)
                    : inputTransfer;
            outputTransfer = outputTransfer == null
                    ? new FluidStackSetTransferReport(check.outputCheck(), List.of(), simulated)
                    : outputTransfer;
        }

        private static RecipeFluidIoProcessReport skipped(RecipeFluidIoCheckReport check, boolean simulated) {
            return new RecipeFluidIoProcessReport(check, null, null, simulated, true);
        }

        public boolean complete() {
            return !skipped && inputTransfer.complete() && outputTransfer.complete();
        }

        public int movedMb() {
            return inputTransfer.movedMb() + outputTransfer.movedMb();
        }
    }
}
