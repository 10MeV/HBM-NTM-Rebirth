package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class GenericMachineRecipeRuntime {
    public static final String NULL_RECIPE = "null";

    private GenericMachineRecipeRuntime() {
    }

    public static List<GenericMachineRecipe> recipes(Level level, GenericMachineRecipe.Machine machine) {
        return level.getRecipeManager().getAllRecipesFor(machine.type()).stream()
                .filter(recipe -> recipe.getMachine() == machine)
                .toList();
    }

    public static List<String> recipeNames(Level level, GenericMachineRecipe.Machine machine) {
        return recipes(level, machine).stream()
                .map(GenericMachineRecipe::getInternalName)
                .sorted()
                .toList();
    }

    public static boolean hasRecipe(Level level, GenericMachineRecipe.Machine machine, String internalName) {
        return findByInternalName(level, machine, internalName) != null;
    }

    @Nullable
    public static GenericMachineRecipe findByInternalName(Level level, GenericMachineRecipe.Machine machine, String internalName) {
        if (internalName == null || internalName.isBlank() || NULL_RECIPE.equals(internalName)) {
            return null;
        }
        return recipes(level, machine).stream()
                .filter(recipe -> recipe.getInternalName().equals(internalName))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static GenericMachineRecipe findAutoSwitchRecipe(Level level, GenericMachineRecipe.Machine machine,
            GenericMachineRecipe currentRecipe, ItemStack firstInput) {
        String group = currentRecipe.getAutoSwitchGroup();
        if (group == null || firstInput.isEmpty()) {
            return null;
        }
        return recipes(level, machine).stream()
                .filter(recipe -> recipe != currentRecipe)
                .filter(recipe -> group.equals(recipe.getAutoSwitchGroup()))
                .filter(recipe -> !recipe.getItemInputs().isEmpty())
                .filter(recipe -> recipe.getItemInputs().get(0).test(firstInput))
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
        List<GenericMachineRecipe.ItemInput> itemInputs = recipe.getItemInputs();
        int itemCount = Math.min(itemInputs.size(), inputSlots.length);
        for (int i = 0; i < itemCount; i++) {
            items.extractItem(inputSlots[i], itemInputs.get(i).count(), false);
        }

        List<HbmFluidStack> fluidInputs = recipe.getFluidInputs();
        int fluidCount = Math.min(fluidInputs.size(), inputTanks.size());
        for (int i = 0; i < fluidCount; i++) {
            inputTanks.get(i).drain(fluidInputs.get(i).amount(), false);
        }
    }

    public static void produceOutputs(GenericMachineRecipe recipe, ItemStackHandler items, int[] outputSlots,
            List<HbmFluidTank> outputTanks) {
        List<ItemStack> itemOutputs = recipe.getItemOutputs();
        int itemCount = Math.min(itemOutputs.size(), outputSlots.length);
        for (int i = 0; i < itemCount; i++) {
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

    private static boolean hasItemInputs(GenericMachineRecipe recipe, ItemStackHandler items, int[] inputSlots) {
        List<GenericMachineRecipe.ItemInput> itemInputs = recipe.getItemInputs();
        if (itemInputs.size() > inputSlots.length) {
            return false;
        }
        for (int i = 0; i < itemInputs.size(); i++) {
            ItemStack stack = items.getStackInSlot(inputSlots[i]);
            GenericMachineRecipe.ItemInput input = itemInputs.get(i);
            if (!input.test(stack)) {
                return false;
            }
        }
        return true;
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
        List<ItemStack> outputs = recipe.getItemOutputs();
        if (outputs.size() > outputSlots.length) {
            return false;
        }
        for (int i = 0; i < outputs.size(); i++) {
            if (!items.insertItem(outputSlots[i], outputs.get(i).copy(), true).isEmpty()) {
                return false;
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
}
