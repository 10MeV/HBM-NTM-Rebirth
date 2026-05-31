package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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
                && canFitItemOutputs(recipe, items, outputSlots)
                && canFitFluidOutputs(recipe, outputTanks);
    }

    public static void consumeInputs(GenericMachineRecipe recipe, ItemStackHandler items, int[] inputSlots,
            List<HbmFluidTank> inputTanks) {
        ItemInputMatchPlan itemPlan = matchItemInputs(recipe, items, inputSlots);
        if (itemPlan == null) {
            throw new IllegalStateException("Generic machine inputs no longer match after canProcess: " + recipe.getId());
        }
        List<HbmIngredient> itemInputs = recipe.getItemInputs();
        int[] matchedSlots = itemPlan.matchedSlots();
        for (int i = 0; i < itemInputs.size(); i++) {
            items.extractItem(matchedSlots[i], itemInputs.get(i).count(), false);
        }

        List<HbmFluidStack> fluidInputs = recipe.getFluidInputs();
        int fluidCount = Math.min(fluidInputs.size(), inputTanks.size());
        for (int i = 0; i < fluidCount; i++) {
            inputTanks.get(i).drain(fluidInputs.get(i).amount(), false);
        }
    }

    public static void produceOutputs(GenericMachineRecipe recipe, ItemStackHandler items, int[] outputSlots,
            List<HbmFluidTank> outputTanks) {
        List<ItemStack> itemOutputs = collapseItemOutputs(recipe, RandomSource.create());
        int itemCount = Math.min(itemOutputs.size(), outputSlots.length);
        for (int i = 0; i < itemCount; i++) {
            if (itemOutputs.get(i).isEmpty()) {
                continue;
            }
            ItemStack remaining = items.insertItem(outputSlots[i], itemOutputs.get(i).copy(), false);
            if (!remaining.isEmpty()) {
                throw new IllegalStateException("Generic machine output no longer fits after canProcess: " + recipe.getId());
            }
        }

        List<HbmFluidStack> fluidOutputs = recipe.getFluidOutputs();
        int fluidCount = Math.min(fluidOutputs.size(), outputTanks.size());
        for (int i = 0; i < fluidCount; i++) {
            HbmFluidStack output = fluidOutputs.get(i);
            int filled = outputTanks.get(i).fill(output.type(), output.amount(), output.pressure(), false);
            if (filled != output.amount()) {
                throw new IllegalStateException("Generic machine fluid output no longer fits after canProcess: " + recipe.getId());
            }
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
        if (itemInputs.size() > inputSlots.length) {
            return null;
        }
        List<IndexedIngredient> remaining = new ArrayList<>(itemInputs.size());
        for (int i = 0; i < itemInputs.size(); i++) {
            remaining.add(new IndexedIngredient(i, itemInputs.get(i)));
        }
        int[] matchedSlots = new int[itemInputs.size()];
        Arrays.fill(matchedSlots, -1);

        for (int inputSlot : inputSlots) {
            ItemStack stack = items.getStackInSlot(inputSlot);
            if (stack.isEmpty()) {
                continue;
            }

            boolean hasMatch = false;
            Iterator<IndexedIngredient> iterator = remaining.iterator();
            while (iterator.hasNext()) {
                IndexedIngredient candidate = iterator.next();
                if (candidate.ingredient().test(stack)) {
                    matchedSlots[candidate.index()] = inputSlot;
                    iterator.remove();
                    hasMatch = true;
                    break;
                }
            }
            if (!hasMatch) {
                return null;
            }
        }
        return remaining.isEmpty() ? new ItemInputMatchPlan(matchedSlots) : null;
    }

    private static boolean hasFluidInputs(GenericMachineRecipe recipe, List<HbmFluidTank> inputTanks) {
        List<HbmFluidStack> fluidInputs = recipe.getFluidInputs();
        if (fluidInputs.size() > inputTanks.size()) {
            return false;
        }
        for (int i = 0; i < fluidInputs.size(); i++) {
            HbmFluidStack input = fluidInputs.get(i);
            HbmFluidTank tank = inputTanks.get(i);
            if (tank.getTankType() != input.type()
                    || tank.getPressure() != HbmFluidTank.clampPressure(input.pressure())
                    || tank.getFill() < input.amount()) {
                return false;
            }
        }
        return true;
    }

    private static boolean canFitItemOutputs(GenericMachineRecipe recipe, ItemStackHandler items, int[] outputSlots) {
        List<HbmItemOutput> outputs = recipe.getItemOutputEntries();
        if (outputs.size() > outputSlots.length) {
            return false;
        }
        for (int i = 0; i < outputs.size(); i++) {
            for (HbmItemOutput.Entry entry : outputs.get(i).entries()) {
                if (!items.insertItem(outputSlots[i], entry.stack(), true).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean canFitFluidOutputs(GenericMachineRecipe recipe, List<HbmFluidTank> outputTanks) {
        List<HbmFluidStack> outputs = recipe.getFluidOutputs();
        if (outputs.size() > outputTanks.size()) {
            return false;
        }
        for (int i = 0; i < outputs.size(); i++) {
            HbmFluidStack output = outputs.get(i);
            if (outputTanks.get(i).fill(output.type(), output.amount(), output.pressure(), true) != output.amount()) {
                return false;
            }
        }
        return true;
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
            return byInternalName.keySet().stream().sorted().toList();
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

    private record IndexedIngredient(int index, HbmIngredient ingredient) {
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
