package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

public record AshpitJeiRecipe(List<ItemStack> machines, List<ItemStack> itemInputs,
        HbmFluidStack fluidInput, ItemStack output) {

    public AshpitJeiRecipe {
        machines = copyStacks(machines);
        itemInputs = copyStacks(itemInputs);
        fluidInput = fluidInput == null ? new HbmFluidStack(HbmFluids.NONE, 0) : fluidInput;
        output = output == null ? ItemStack.EMPTY : output.copy();
    }

    public boolean hasFluidInput() {
        return !fluidInput.isEmpty();
    }

    public static List<AshpitJeiRecipe> recipes() {
        List<AshpitJeiRecipe> recipes = new ArrayList<>();
        List<ItemStack> ovens = List.of(
                new ItemStack(ModBlocks.HEATER_FIREBOX.get()),
                new ItemStack(ModBlocks.HEATER_OVEN.get()));
        List<ItemStack> chimneys = List.of(
                new ItemStack(ModBlocks.CHIMNEY_BRICK.get()),
                new ItemStack(ModBlocks.CHIMNEY_INDUSTRIAL.get()));
        List<ItemStack> industrialChimney = List.of(new ItemStack(ModBlocks.CHIMNEY_INDUSTRIAL.get()));

        addItemRecipe(recipes, ovens, List.of(
                new ItemStack(Items.COAL),
                item("lignite"),
                item("coke_coal")), "powder_ash_coal");
        addItemRecipe(recipes, ovens, List.of(
                new ItemStack(Items.OAK_LOG),
                new ItemStack(Items.ACACIA_LOG),
                new ItemStack(Items.OAK_PLANKS),
                new ItemStack(Items.OAK_SAPLING)), "powder_ash_wood");
        addItemRecipe(recipes, ovens, List.of(
                item("solid_fuel"),
                item("scrap"),
                item("dust"),
                item("rocket_fuel")), "powder_ash_misc");

        for (var smoke : List.of(HbmFluids.SMOKE, HbmFluids.SMOKE_LEADED, HbmFluids.SMOKE_POISON)) {
            addFluidRecipe(recipes, chimneys, new HbmFluidStack(smoke, 2_000), "powder_ash_fly");
            addFluidRecipe(recipes, industrialChimney, new HbmFluidStack(smoke, 8_000), "powder_ash_soot");
        }
        return List.copyOf(recipes);
    }

    private static void addItemRecipe(List<AshpitJeiRecipe> recipes, List<ItemStack> machines,
            List<ItemStack> inputs, String outputName) {
        List<ItemStack> cleanInputs = inputs.stream()
                .filter(stack -> !stack.isEmpty())
                .toList();
        ItemStack output = item(outputName);
        if (!cleanInputs.isEmpty() && !output.isEmpty()) {
            recipes.add(new AshpitJeiRecipe(machines, cleanInputs, new HbmFluidStack(HbmFluids.NONE, 0), output));
        }
    }

    private static void addFluidRecipe(List<AshpitJeiRecipe> recipes, List<ItemStack> machines,
            HbmFluidStack input, String outputName) {
        ItemStack output = item(outputName);
        if (!input.isEmpty() && !output.isEmpty()) {
            recipes.add(new AshpitJeiRecipe(machines, List.of(), input, output));
        }
    }

    private static ItemStack item(String legacyName) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        return stacks == null ? List.of() : stacks.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }
}
