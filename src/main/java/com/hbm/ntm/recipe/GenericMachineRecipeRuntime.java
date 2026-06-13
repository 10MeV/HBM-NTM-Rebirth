package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidRecipeIO;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class GenericMachineRecipeRuntime {
    public static final String NULL_RECIPE = "null";

    private GenericMachineRecipeRuntime() {
    }

    public static List<GenericMachineRecipe> recipes(Level level, GenericMachineRecipe.Machine machine) {
        return level.getRecipeManager().getAllRecipesFor(machine.type()).stream()
                .filter(recipe -> recipe.getMachine() == machine)
                .sorted(GenericMachineRecipe.LEGACY_ORDER)
                .toList();
    }

    public static Index index(Level level, GenericMachineRecipe.Machine machine) {
        return Index.build(recipes(level, machine));
    }

    public static Audit audit(Level level, GenericMachineRecipe.Machine machine) {
        return index(level, machine).audit();
    }

    public static List<String> recipeNames(Level level, GenericMachineRecipe.Machine machine) {
        return index(level, machine).recipeNames();
    }

    public static boolean hasRecipe(Level level, GenericMachineRecipe.Machine machine, String internalName) {
        return findByInternalName(level, machine, internalName) != null;
    }

    @Nullable
    public static GenericMachineRecipe findByInternalName(Level level, GenericMachineRecipe.Machine machine, String internalName) {
        if (internalName == null || internalName.isBlank() || NULL_RECIPE.equals(internalName)) {
            return null;
        }
        return index(level, machine).byInternalName(internalName);
    }

    public static List<GenericMachineRecipe> findByPool(Level level, GenericMachineRecipe.Machine machine, String pool) {
        if (pool == null || pool.isBlank()) {
            return List.of();
        }
        return index(level, machine).byPool().getOrDefault(pool, List.of());
    }

    public static List<GenericMachineRecipe> search(Level level, GenericMachineRecipe.Machine machine, String query) {
        return index(level, machine).recipes().stream()
                .filter(recipe -> recipe.matchesSearch(query))
                .toList();
    }

    @Nullable
    public static GenericMachineRecipe findAutoSwitchRecipe(Level level, GenericMachineRecipe.Machine machine,
            GenericMachineRecipe currentRecipe, ItemStack firstInput) {
        String group = currentRecipe.getAutoSwitchGroup();
        if (group == null || firstInput.isEmpty()) {
            return null;
        }
        return index(level, machine).recipes().stream()
                .filter(recipe -> recipe != currentRecipe)
                .filter(recipe -> group.equals(recipe.getAutoSwitchGroup()))
                .filter(recipe -> !recipe.getItemInputs().isEmpty())
                .filter(recipe -> recipe.getItemInputs().get(0).test(firstInput, true))
                .findFirst()
                .orElse(null);
    }

    public static boolean canProcess(GenericMachineRecipe recipe, ItemStackHandler items, int[] inputSlots, int[] outputSlots,
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks) {
        return hasItemInputs(recipe, items, inputSlots)
                && hasFluidInputs(recipe, inputTanks)
                && canFitItemOutputs(recipe, items, inputSlots, outputSlots)
                && canFitFluidOutputs(recipe, outputTanks);
    }

    public static ProcessingResult update(Level level, GenericMachineRecipe.Machine machine, String selectedRecipe,
            double progress, ItemStack blueprint, HbmEnergyStorage energy, ItemStackHandler items, int[] inputSlots,
            int[] outputSlots, List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks,
            ProcessingFactors factors, boolean extraCondition, int defaultTankCapacity) {
        return update(level, machine, selectedRecipe, progress, blueprint, energy, items, inputSlots, outputSlots,
                inputTanks, outputTanks, factors, extraCondition, defaultTankCapacity, null);
    }

    public static ProcessingResult update(Level level, GenericMachineRecipe.Machine machine, String selectedRecipe,
            double progress, ItemStack blueprint, HbmEnergyStorage energy, ItemStackHandler items, int[] inputSlots,
            int[] outputSlots, List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks,
            ProcessingFactors factors, boolean extraCondition, int defaultTankCapacity, @Nullable BlockPos pollutionPos) {
        return update(level, machine, selectedRecipe, progress, blueprint, energy, items, inputSlots, outputSlots,
                inputTanks, outputTanks, factors, extraCondition, defaultTankCapacity, pollutionPos,
                PollutionSink.DIRECT);
    }

    public static ProcessingResult update(Level level, GenericMachineRecipe.Machine machine, String selectedRecipe,
            double progress, ItemStack blueprint, HbmEnergyStorage energy, ItemStackHandler items, int[] inputSlots,
            int[] outputSlots, List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks,
            ProcessingFactors factors, boolean extraCondition, int defaultTankCapacity,
            @Nullable BlockPos pollutionPos, @Nullable PollutionSink pollutionSink) {
        GenericMachineRecipe recipe = findByInternalName(level, machine, selectedRecipe);
        if (recipe != null && !GenericMachineRecipeSelector.isAllowedByBlueprint(recipe, blueprint)) {
            return new ProcessingResult(NULL_RECIPE, 0.0D, false, true, null, false);
        }
        setupTanks(recipe, inputTanks, outputTanks, defaultTankCapacity);

        if (recipe == null) {
            return new ProcessingResult(normalize(selectedRecipe), 0.0D, false, false, null, false);
        }

        GenericMachineRecipe switchedRecipe = inputSlots.length > 0
                ? findAutoSwitchRecipe(level, machine, recipe, items.getStackInSlot(inputSlots[0]))
                : null;
        if (switchedRecipe != null) {
            setupTanks(switchedRecipe, inputTanks, outputTanks, defaultTankCapacity);
            return new ProcessingResult(switchedRecipe.getInternalName(), 0.0D, false, true, switchedRecipe, false);
        }

        boolean canProcess = extraCondition
                && canProcess(recipe, items, inputSlots, outputSlots, inputTanks, outputTanks)
                && hasPower(energy, recipe, factors.powerMultiplier());
        if (!canProcess) {
            return new ProcessingResult(recipe.getInternalName(), 0.0D, false, progress != 0.0D, recipe, false);
        }

        energy.setPower(energy.getPower() - requiredPower(recipe, factors.powerMultiplier()));
        applyTickPollution(level, pollutionPos, recipe, pollutionSink);
        double nextProgress = progress + Math.min(factors.speedMultiplier() / Math.max(recipe.getDuration(), 1), 1.0D);
        boolean completed = false;
        if (nextProgress >= 1.0D) {
            List<ItemStack> inputRemainders = inputRemainders(recipe, items, inputSlots);
            consumeInputs(recipe, items, inputSlots, inputTanks);
            produceOutputs(recipe, inputRemainders, items, outputSlots, outputTanks);
            completed = true;
            if (canProcess(recipe, items, inputSlots, outputSlots, inputTanks, outputTanks)
                    && hasPower(energy, recipe, factors.powerMultiplier())) {
                nextProgress -= 1.0D;
            } else {
                nextProgress = 0.0D;
            }
        }

        return new ProcessingResult(recipe.getInternalName(), nextProgress, true, true, recipe, completed);
    }

    private static void applyTickPollution(Level level, @Nullable BlockPos pos, GenericMachineRecipe recipe,
            @Nullable PollutionSink pollutionSink) {
        if (pos == null || level.getGameTime() % 20 != 0) {
            return;
        }
        PollutionSink sink = pollutionSink == null ? PollutionSink.DIRECT : pollutionSink;
        recipe.getExtraData().pollution().ifPresent(pollution -> {
            float amount = pollution.amount();
            if (!Float.isFinite(amount) || amount == 0.0F) {
                return;
            }
            sink.apply(level, pos, pollution.type(), amount);
        });
    }

    public static void setupTanks(@Nullable GenericMachineRecipe recipe, List<HbmFluidTank> inputTanks,
            List<HbmFluidTank> outputTanks, int defaultCapacity) {
        if (recipe == null) {
            return;
        }
        HbmFluidRecipeIO.setupRecipeTanks(
                recipe.getFluidInputs(), recipe.getFluidOutputs(), inputTanks, outputTanks, defaultCapacity);
    }

    public static boolean isItemValidForCurrentRecipe(GenericMachineRecipe recipe, GenericMachineRecipe.Machine machine,
            Level level, int slot, ItemStack stack, int[] inputSlots) {
        if (recipe == null || recipe.getItemInputs().isEmpty()) {
            return false;
        }
        boolean inputSlot = false;
        for (int candidate : inputSlots) {
            if (candidate == slot) {
                inputSlot = true;
                break;
            }
        }
        if (!inputSlot) {
            return false;
        }
        List<HbmIngredient> inputs = recipe.getItemInputs();
        for (HbmIngredient input : inputs) {
            if (input.test(stack, true)) {
                return true;
            }
        }
        if (recipe.getAutoSwitchGroup() == null || inputSlots.length == 0) {
            return false;
        }
        return index(level, machine).recipes().stream()
                .filter(candidate -> recipe.getAutoSwitchGroup().equals(candidate.getAutoSwitchGroup()))
                .filter(candidate -> !candidate.getItemInputs().isEmpty())
                .anyMatch(candidate -> candidate.getItemInputs().stream().anyMatch(input -> input.test(stack, true)));
    }

    public static boolean isSlotClogged(GenericMachineRecipe recipe, GenericMachineRecipe.Machine machine, Level level,
            ItemStackHandler items, int slot, int[] inputSlots) {
        boolean inputSlot = false;
        for (int candidate : inputSlots) {
            if (candidate == slot) {
                inputSlot = true;
                break;
            }
        }
        if (!inputSlot) {
            return false;
        }
        ItemStack stack = items.getStackInSlot(slot);
        return !stack.isEmpty() && !isItemValidForCurrentRecipe(recipe, machine, level, slot, stack, inputSlots);
    }

    public static void consumeInputs(GenericMachineRecipe recipe, ItemStackHandler items, int[] inputSlots,
            List<HbmFluidTank> inputTanks) {
        ItemInputMatchPlan itemPlan = matchItemInputs(recipe, items, inputSlots);
        if (itemPlan == null) {
            throw new IllegalStateException("Generic machine inputs no longer match after canProcess: " + recipe.getId());
        }
        List<HbmIngredient> itemInputs = recipe.getItemInputs();
        int[] matchedSlots = itemPlan.matchedSlots();
        int itemCount = Math.min(itemInputs.size(), matchedSlots.length);
        for (int i = 0; i < itemCount; i++) {
            items.extractItem(matchedSlots[i], itemInputs.get(i).count(), false);
        }

        List<HbmFluidStack> fluidInputs = recipe.getFluidInputs();
        HbmFluidRecipeIO.FluidStackSetTransferReport fluidReport =
                HbmFluidRecipeIO.consumeInputsReport(fluidInputs, inputTanks, false);
        if (!fluidReport.complete()) {
            throw new IllegalStateException("Generic machine fluid inputs no longer match after canProcess: " + recipe.getId());
        }
    }

    public static void produceOutputs(GenericMachineRecipe recipe, ItemStackHandler items, int[] outputSlots,
            List<HbmFluidTank> outputTanks) {
        produceOutputs(recipe, List.of(), items, outputSlots, outputTanks);
    }

    private static void produceOutputs(GenericMachineRecipe recipe, List<ItemStack> inputRemainders,
            ItemStackHandler items, int[] outputSlots, List<HbmFluidTank> outputTanks) {
        List<ItemStack> itemOutputs = collapseItemOutputs(recipe, RandomSource.create());
        if (!inputRemainders.isEmpty()) {
            List<ItemStack> combined = new ArrayList<>(itemOutputs);
            combined.addAll(inputRemainders);
            itemOutputs = combined;
        }
        int itemCount = itemOutputs.size();
        if (itemCount > outputSlots.length) {
            throw new IllegalStateException("Generic machine output no longer fits after canProcess: " + recipe.getId());
        }
        for (int i = 0; i < itemCount; i++) {
            ItemStack output = itemOutputs.get(i);
            if (output.isEmpty()) {
                continue;
            }
            int outputSlot = outputSlots[i];
            ItemStack current = items.getStackInSlot(outputSlot);
            if (current.isEmpty()) {
                items.setStackInSlot(outputSlot, output.copy());
            } else if (ItemStack.isSameItemSameTags(current, output)
                    && current.getCount() + output.getCount() <= Math.min(current.getMaxStackSize(), items.getSlotLimit(outputSlot))) {
                ItemStack merged = current.copy();
                merged.grow(output.getCount());
                items.setStackInSlot(outputSlot, merged);
            } else {
                throw new IllegalStateException("Generic machine output no longer fits after canProcess: " + recipe.getId());
            }
        }

        List<HbmFluidStack> fluidOutputs = recipe.getFluidOutputs();
        HbmFluidRecipeIO.FluidStackSetTransferReport fluidReport =
                HbmFluidRecipeIO.produceOutputsReport(fluidOutputs, outputTanks, false);
        if (!fluidReport.complete()) {
            throw new IllegalStateException("Generic machine fluid output no longer fits after canProcess: " + recipe.getId());
        }
    }

    public static List<ItemStack> collapseItemOutputs(GenericMachineRecipe recipe, RandomSource random) {
        return recipe.getItemOutputEntries().stream()
                .map(output -> output.collapse(random))
                .toList();
    }

    private static boolean hasItemInputs(GenericMachineRecipe recipe, ItemStackHandler items, int[] inputSlots) {
        return matchItemInputs(recipe, items, inputSlots) != null;
    }

    @Nullable
    private static ItemInputMatchPlan matchItemInputs(GenericMachineRecipe recipe, ItemStackHandler items, int[] inputSlots) {
        List<HbmIngredient> itemInputs = recipe.getItemInputs();
        int itemCount = Math.min(itemInputs.size(), inputSlots.length);
        if (itemInputs.size() > inputSlots.length) {
            return null;
        }
        int[] matchedSlots = new int[itemCount];
        Arrays.fill(matchedSlots, -1);

        boolean[] usedSlots = new boolean[inputSlots.length];
        return matchItemInputsRecursive(itemInputs, items, inputSlots, matchedSlots, usedSlots, 0)
                ? new ItemInputMatchPlan(matchedSlots)
                : null;
    }

    private static boolean matchItemInputsRecursive(List<HbmIngredient> itemInputs, ItemStackHandler items,
            int[] inputSlots, int[] matchedSlots, boolean[] usedSlots, int inputIndex) {
        if (inputIndex >= itemInputs.size()) {
            return true;
        }
        HbmIngredient input = itemInputs.get(inputIndex);
        for (int slotIndex = 0; slotIndex < inputSlots.length; slotIndex++) {
            if (usedSlots[slotIndex]) {
                continue;
            }
            int inputSlot = inputSlots[slotIndex];
            ItemStack stack = items.getStackInSlot(inputSlot);
            if (stack.isEmpty() || !input.test(stack)) {
                continue;
            }
            usedSlots[slotIndex] = true;
            matchedSlots[inputIndex] = inputSlot;
            if (matchItemInputsRecursive(itemInputs, items, inputSlots, matchedSlots, usedSlots, inputIndex + 1)) {
                return true;
            }
            matchedSlots[inputIndex] = -1;
            usedSlots[slotIndex] = false;
        }
        return false;
    }

    private static boolean hasPower(HbmEnergyStorage energy, GenericMachineRecipe recipe, double powerMultiplier) {
        return energy.getPower() >= requiredPower(recipe, powerMultiplier);
    }

    private static long requiredPower(GenericMachineRecipe recipe, double powerMultiplier) {
        if (powerMultiplier == 1.0D) {
            return recipe.getPower();
        }
        return (long) (recipe.getPower() * powerMultiplier);
    }

    private static String normalize(String selectedRecipe) {
        return selectedRecipe == null || selectedRecipe.isBlank() ? NULL_RECIPE : selectedRecipe;
    }

    public record ProcessingFactors(double speedMultiplier, double powerMultiplier) {
        public ProcessingFactors {
            speedMultiplier = Math.max(0.0D, speedMultiplier);
            powerMultiplier = Math.max(0.0D, powerMultiplier);
        }

        public static ProcessingFactors normal() {
            return new ProcessingFactors(1.0D, 1.0D);
        }
    }

    public record ProcessingResult(String selectedRecipe, double progress, boolean didProcess, boolean changed,
                                   @Nullable GenericMachineRecipe recipe, boolean completed) {
    }

    private record PendingItemOutput(ItemStack stack, boolean oneOf) {
    }

    private static boolean hasFluidInputs(GenericMachineRecipe recipe, List<HbmFluidTank> inputTanks) {
        return HbmFluidRecipeIO.inspectInputs(recipe.getFluidInputs(), inputTanks).complete();
    }

    private static boolean canFitItemOutputs(GenericMachineRecipe recipe, ItemStackHandler items, int[] inputSlots,
            int[] outputSlots) {
        List<HbmItemOutput> outputs = recipe.getItemOutputEntries();
        List<PendingItemOutput> pending = new ArrayList<>();
        for (HbmItemOutput output : outputs) {
            pending.add(new PendingItemOutput(output.representativeStack(),
                    output.oneOf() && output.entries().size() > 1));
        }
        inputRemainders(recipe, items, inputSlots).stream()
                .map(stack -> new PendingItemOutput(stack, false))
                .forEach(pending::add);
        if (pending.size() > outputSlots.length) {
            return pending.stream().allMatch(output -> output.stack().isEmpty());
        }
        for (int i = 0; i < pending.size(); i++) {
            int outputSlot = outputSlots[i];
            ItemStack current = items.getStackInSlot(outputSlot);
            PendingItemOutput output = pending.get(i);
            ItemStack single = output.stack();
            if (single.isEmpty()) {
                continue;
            }
            if (current.isEmpty()) {
                continue;
            }
            if (output.oneOf()) {
                return false;
            }
            if (!ItemStack.isSameItemSameTags(current, single)
                    || current.getCount() + single.getCount() > Math.min(current.getMaxStackSize(), items.getSlotLimit(outputSlot))) {
                return false;
            }
        }
        return true;
    }

    private static List<ItemStack> inputRemainders(GenericMachineRecipe recipe, ItemStackHandler items, int[] inputSlots) {
        ItemInputMatchPlan itemPlan = matchItemInputs(recipe, items, inputSlots);
        if (itemPlan == null) {
            return List.of();
        }
        List<ItemStack> remainders = new ArrayList<>();
        List<HbmIngredient> inputs = recipe.getItemInputs();
        int[] matchedSlots = itemPlan.matchedSlots();
        int itemCount = Math.min(inputs.size(), matchedSlots.length);
        for (int i = 0; i < itemCount; i++) {
            remainders.addAll(inputs.get(i).remainingItems(items.getStackInSlot(matchedSlots[i])));
        }
        return remainders;
    }

    private static boolean canFitFluidOutputs(GenericMachineRecipe recipe, List<HbmFluidTank> outputTanks) {
        return HbmFluidRecipeIO.inspectOutputs(recipe.getFluidOutputs(), outputTanks).complete();
    }

    public record Index(List<GenericMachineRecipe> recipes,
                        Map<String, GenericMachineRecipe> byInternalName,
                        Map<String, List<GenericMachineRecipe>> duplicates,
                        Map<String, List<GenericMachineRecipe>> byPool,
                        Map<LegacyBlueprintPools.Kind, List<GenericMachineRecipe>> byPoolKind) {
        private static Index build(List<GenericMachineRecipe> recipes) {
            Map<String, List<GenericMachineRecipe>> grouped = recipes.stream()
                    .collect(Collectors.groupingBy(GenericMachineRecipe::getInternalName, LinkedHashMap::new, Collectors.toList()));
            Map<String, GenericMachineRecipe> byName = new LinkedHashMap<>();
            Map<String, List<GenericMachineRecipe>> duplicates = new LinkedHashMap<>();
            for (Map.Entry<String, List<GenericMachineRecipe>> entry : grouped.entrySet()) {
                byName.put(entry.getKey(), entry.getValue().get(0));
                if (entry.getValue().size() > 1) {
                    duplicates.put(entry.getKey(), List.copyOf(entry.getValue()));
                }
            }

            Map<String, List<GenericMachineRecipe>> byPool = new LinkedHashMap<>();
            for (GenericMachineRecipe recipe : recipes) {
                for (String pool : recipe.getPools()) {
                    byPool.computeIfAbsent(pool, ignored -> new java.util.ArrayList<>()).add(recipe);
                }
            }
            byPool.replaceAll((pool, poolRecipes) -> List.copyOf(poolRecipes));
            Map<LegacyBlueprintPools.Kind, List<GenericMachineRecipe>> byPoolKind = new LinkedHashMap<>();
            for (GenericMachineRecipe recipe : recipes) {
                for (String pool : recipe.getPools()) {
                    byPoolKind.computeIfAbsent(LegacyBlueprintPools.kind(pool), ignored -> new java.util.ArrayList<>()).add(recipe);
                }
            }
            byPoolKind.replaceAll((kind, poolRecipes) -> List.copyOf(poolRecipes));
            return new Index(
                    List.copyOf(recipes),
                    Collections.unmodifiableMap(new LinkedHashMap<>(byName)),
                    Collections.unmodifiableMap(new LinkedHashMap<>(duplicates)),
                    Collections.unmodifiableMap(new LinkedHashMap<>(byPool)),
                    Collections.unmodifiableMap(new LinkedHashMap<>(byPoolKind)));
        }

        @Nullable
        public GenericMachineRecipe byInternalName(String internalName) {
            return byInternalName.get(internalName);
        }

        public List<String> recipeNames() {
            return List.copyOf(byInternalName.keySet());
        }

        public Audit audit() {
            List<GenericMachineRecipe> emptyOutputs = recipes.stream()
                    .filter(recipe -> recipe.getItemOutputs().isEmpty() && recipe.getFluidOutputs().isEmpty())
                    .toList();
            List<GenericMachineRecipe> emptyInputs = recipes.stream()
                    .filter(recipe -> recipe.getItemInputs().isEmpty() && recipe.getFluidInputs().isEmpty())
                    .toList();
            List<GenericMachineRecipe> invalidDurations = recipes.stream()
                    .filter(recipe -> recipe.getDuration() <= 0)
                    .toList();
            List<GenericMachineRecipe> invalidWeightedOutputs = recipes.stream()
                    .filter(GenericMachineRecipeRuntime::hasInvalidWeightedOutput)
                    .toList();
            List<GenericMachineRecipe> overLimit = recipes.stream()
                    .filter(GenericMachineRecipeRuntime::exceedsMachineLimits)
                    .toList();
            List<GenericMachineRecipe> oversizedItemInputs = recipes.stream()
                    .filter(GenericMachineRecipeRuntime::hasOversizedItemInput)
                    .toList();
            List<GenericMachineRecipe> unresolvedItemInputs = recipes.stream()
                    .filter(GenericMachineRecipeRuntime::hasUnresolvedItemInput)
                    .toList();
            List<UnresolvedItemInput> unresolvedItemInputDetails = new ArrayList<>();
            Map<String, UnresolvedInputGroup> unresolvedItemInputGroups = new LinkedHashMap<>();
            for (GenericMachineRecipe recipe : recipes) {
                List<HbmIngredient> inputs = recipe.getItemInputs();
                for (int slot = 0; slot < inputs.size(); slot++) {
                    HbmIngredient input = inputs.get(slot);
                    if (!input.unresolvedDisplayInput()) {
                        continue;
                    }
                    UnresolvedItemInput detail = new UnresolvedItemInput(recipe, slot, input);
                    unresolvedItemInputDetails.add(detail);
                    String key = input.diagnosticKey();
                    unresolvedItemInputGroups
                            .computeIfAbsent(key, ignored -> new UnresolvedInputGroup(key, input.diagnosticName(), new ArrayList<>()))
                            .entries()
                            .add(detail);
                }
            }
            unresolvedItemInputGroups.replaceAll((key, group) ->
                    new UnresolvedInputGroup(key, group.diagnosticName(), List.copyOf(group.entries())));
            return new Audit(duplicates, emptyInputs, emptyOutputs, invalidDurations, invalidWeightedOutputs,
                    overLimit, oversizedItemInputs, unresolvedItemInputs, List.copyOf(unresolvedItemInputDetails),
                    Collections.unmodifiableMap(new LinkedHashMap<>(unresolvedItemInputGroups)));
        }
    }

    public record Audit(Map<String, List<GenericMachineRecipe>> duplicateInternalNames,
                        List<GenericMachineRecipe> emptyInputs,
                        List<GenericMachineRecipe> emptyOutputs,
                        List<GenericMachineRecipe> invalidDurations,
                        List<GenericMachineRecipe> invalidWeightedOutputs,
                        List<GenericMachineRecipe> overLimit,
                        List<GenericMachineRecipe> oversizedItemInputs,
                        List<GenericMachineRecipe> unresolvedItemInputs,
                        List<UnresolvedItemInput> unresolvedItemInputDetails,
                        Map<String, UnresolvedInputGroup> unresolvedItemInputGroups) {
        public boolean hasProblems() {
            return !duplicateInternalNames.isEmpty()
                    || !emptyInputs.isEmpty()
                    || !emptyOutputs.isEmpty()
                    || !invalidDurations.isEmpty()
                    || !invalidWeightedOutputs.isEmpty()
                    || !overLimit.isEmpty()
                    || !oversizedItemInputs.isEmpty()
                    || !unresolvedItemInputs.isEmpty();
        }

        public int problemCount() {
            return duplicateInternalNames.values().stream().mapToInt(List::size).sum()
                    + emptyInputs.size()
                    + emptyOutputs.size()
                    + invalidDurations.size()
                    + invalidWeightedOutputs.size()
                    + overLimit.size()
                    + oversizedItemInputs.size()
                    + unresolvedItemInputs.size();
        }
    }

    public record UnresolvedItemInput(GenericMachineRecipe recipe, int inputIndex, HbmIngredient ingredient) {
    }

    public record UnresolvedInputGroup(String key, String diagnosticName, List<UnresolvedItemInput> entries) {
        public int recipeCount() {
            return entries.stream()
                    .map(entry -> entry.recipe().getId())
                    .collect(Collectors.toSet())
                    .size();
        }
    }

    private static boolean hasInvalidWeightedOutput(GenericMachineRecipe recipe) {
        for (HbmItemOutput output : recipe.getItemOutputEntries()) {
            if (!output.hasValidWeightedChoices()) {
                return true;
            }
        }
        return false;
    }

    private static boolean exceedsMachineLimits(GenericMachineRecipe recipe) {
        GenericMachineRecipe.Machine machine = recipe.getMachine();
        return recipe.getItemInputs().size() > machine.inputItemLimit()
                || recipe.getFluidInputs().size() > machine.inputFluidLimit()
                || recipe.getItemOutputEntries().size() > machine.outputItemLimit()
                || recipe.getFluidOutputs().size() > machine.outputFluidLimit();
    }

    private static boolean hasOversizedItemInput(GenericMachineRecipe recipe) {
        return recipe.getItemInputs().stream().anyMatch(HbmIngredient::exceedsStackLimit);
    }

    private static boolean hasUnresolvedItemInput(GenericMachineRecipe recipe) {
        return recipe.getItemInputs().stream().anyMatch(HbmIngredient::unresolvedDisplayInput);
    }

    @FunctionalInterface
    public interface PollutionSink {
        PollutionSink DIRECT = PollutionManager::applyPollutionDelta;

        boolean apply(Level level, BlockPos pos, PollutionType type, float amount);
    }

    private record ItemInputMatchPlan(int[] matchedSlots) {
        private ItemInputMatchPlan {
            matchedSlots = matchedSlots.clone();
        }

        @Override
        public int[] matchedSlots() {
            return matchedSlots.clone();
        }
    }
}
