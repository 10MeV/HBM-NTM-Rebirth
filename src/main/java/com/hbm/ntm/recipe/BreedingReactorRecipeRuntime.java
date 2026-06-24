package com.hbm.ntm.recipe;

import com.hbm.ntm.registry.ModItems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public final class BreedingReactorRecipeRuntime {
    private static final Map<Item, BreederRecipe> RECIPES = new HashMap<>();

    static {
        rod("lithium", "tritium", 200);
        rod("co", "co60", 100);
        rod("ra226", "ac227", 300);
        rod("th232", "thf", 500);
        rod("u235", "np237", 300);
        rod("np237", "pu238", 200);
        rod("pu238", "pu239", 1000);
        rod("u238", "rgp", 300);
        rod("uranium", "rgp", 200);
        rod("rgp", "waste", 200);
        recipe("meteorite_sword_etched", "meteorite_sword_bred", 1000);
    }

    private BreedingReactorRecipeRuntime() {
    }

    public static BreederRecipe recipeFor(ItemStack stack) {
        return stack.isEmpty() ? null : RECIPES.get(stack.getItem());
    }

    public static BreederRecipe recipeFor(Level level, ItemStack stack) {
        if (level != null && !stack.isEmpty()) {
            for (BreedingReactorRecipe recipe : level.getRecipeManager()
                    .getAllRecipesFor(ModRecipes.BREEDING_REACTOR.type().get())) {
                if (recipe.matches(stack)) {
                    return recipe.asRuntimeRecipe();
                }
            }
        }
        return recipeFor(stack);
    }

    public static boolean isInput(ItemStack stack) {
        return recipeFor(stack) != null;
    }

    public static boolean isInput(Level level, ItemStack stack) {
        return recipeFor(level, stack) != null;
    }

    public static List<DisplayRecipe> displayRecipes() {
        return RECIPES.entrySet().stream()
                .map(entry -> new DisplayRecipe(new ItemStack(entry.getKey()), entry.getValue()))
                .toList();
    }

    public static List<DisplayRecipe> displayRecipes(Level level) {
        if (level == null) {
            return displayRecipes();
        }
        List<DisplayRecipe> dynamic = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.BREEDING_REACTOR.type().get()).stream()
                .flatMap(recipe -> recipe.input().displayStacks().stream()
                        .map(input -> new DisplayRecipe(input, recipe.asRuntimeRecipe())))
                .toList();
        if (dynamic.isEmpty()) {
            return displayRecipes();
        }
        return java.util.stream.Stream.concat(dynamic.stream(), displayRecipes().stream()).toList();
    }

    private static void rod(String input, String output, int flux) {
        recipe("rod_" + input, "rod_" + output, flux);
        recipe("rod_dual_" + input, "rod_dual_" + output, flux * 2);
        recipe("rod_quad_" + input, "rod_quad_" + output, flux * 3);
    }

    private static void recipe(String input, String output, int flux) {
        RegistryObject<Item> inputItem = ModItems.legacyItem(input);
        RegistryObject<Item> outputItem = ModItems.legacyItem(output);
        if (inputItem != null && outputItem != null) {
            RECIPES.put(inputItem.get(), new BreederRecipe(new ItemStack(outputItem.get()), flux));
        }
    }

    public record BreederRecipe(ItemStack output, int flux) {
    }

    public record DisplayRecipe(ItemStack input, BreederRecipe recipe) {
    }
}
