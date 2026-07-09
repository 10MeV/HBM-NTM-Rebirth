package com.hbm.ntm.recipe;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class RotaryFurnaceRecipeRuntime {
    private static final Comparator<RotaryFurnaceRecipe> RECIPE_ORDER = Comparator
            .comparingInt(RotaryFurnaceRecipe::sourceOrder)
            .thenComparing(recipe -> recipe.getId().toString());
    private static final List<RotaryFurnaceRecipe> FALLBACK_RECIPES = createFallbackRecipes();

    private RotaryFurnaceRecipeRuntime() {
    }

    public static List<Recipe> recipes() {
        return recipes((RecipeManager) null);
    }

    public static List<Recipe> recipes(@Nullable RecipeManager recipeManager) {
        if (recipeManager != null) {
            return recipeManager.getAllRecipesFor(ModRecipes.ROTARY_FURNACE.type().get()).stream()
                    .sorted(RECIPE_ORDER)
                    .map(Recipe::fromDatapack)
                    .toList();
        }
        return FALLBACK_RECIPES.stream()
                .map(Recipe::fromDatapack)
                .toList();
    }

    @Nullable
    public static Recipe find(@Nullable Level level, ItemStack first, ItemStack second, ItemStack third) {
        RecipeManager recipeManager = level == null ? null : level.getRecipeManager();
        for (Recipe recipe : recipes(recipeManager)) {
            if (recipe.matches(first, second, third)) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    public static Recipe find(ItemStack first, ItemStack second, ItemStack third) {
        return find(null, first, second, third);
    }

    private static List<RotaryFurnaceRecipe> createFallbackRecipes() {
        List<RotaryFurnaceRecipe> recipes = new ArrayList<>();
        add(recipes, "steel_from_coal", Mats.MAT_STEEL, MaterialShapes.INGOT.q(1), 100, 100, null, 0,
                HbmIngredient.legacyOre("ingotIron", 1), HbmIngredient.of(Items.COAL, 1));
        add(recipes, "steel_from_coke", Mats.MAT_STEEL, MaterialShapes.INGOT.q(1), 100, 100, null, 1,
                HbmIngredient.legacyOre("ingotIron", 1), HbmIngredient.legacyOre("gemAnyCoke", 1));
        add(recipes, "steel_fragments_from_coal", Mats.MAT_STEEL, MaterialShapes.INGOT.q(2), 200, 25,
                null, 2, HbmIngredient.legacyOre("bedrockorefragmentIron", 9), HbmIngredient.of(Items.COAL, 1));
        add(recipes, "steel_fragments_from_coke", Mats.MAT_STEEL, MaterialShapes.INGOT.q(3), 200, 25,
                null, 3, HbmIngredient.legacyOre("bedrockorefragmentIron", 9),
                HbmIngredient.legacyOre("gemAnyCoke", 1));
        add(recipes, "steel_fragments_from_coke_flux", Mats.MAT_STEEL, MaterialShapes.INGOT.q(4), 400, 25,
                null, 4, HbmIngredient.legacyOre("bedrockorefragmentIron", 9),
                HbmIngredient.legacyOre("gemAnyCoke", 1), ingredient("powder_flux", 1));
        add(recipes, "desh_from_lightoil", Mats.MAT_DESH, MaterialShapes.INGOT.q(1), 100, 200,
                new HbmFluidStack(HbmFluids.LIGHTOIL, 100), 5, ingredient("powder_desh_ready", 1));
        add(recipes, "gunmetal", Mats.MAT_GUNMETAL, MaterialShapes.INGOT.q(4), 200, 100, null, 6,
                HbmIngredient.of(Items.COPPER_INGOT, 3), HbmIngredient.legacyOre("ingotAluminum", 1));
        add(recipes, "weaponsteel_flux", Mats.MAT_WEAPONSTEEL, MaterialShapes.INGOT.q(1), 200, 400,
                new HbmFluidStack(HbmFluids.GAS_COKER, 100), 7,
                HbmIngredient.legacyOre("ingotSteel", 1), ingredient("powder_flux", 2));
        add(recipes, "saturnite", Mats.MAT_SATURN, MaterialShapes.INGOT.q(2), 200, 400,
                new HbmFluidStack(HbmFluids.REFORMGAS, 250), 8,
                HbmIngredient.legacyOre("dustDuraSteel", 4), HbmIngredient.legacyOre("dustCopper", 1));
        add(recipes, "saturnite_borax", Mats.MAT_SATURN, MaterialShapes.INGOT.q(4), 200, 300,
                new HbmFluidStack(HbmFluids.REFORMGAS, 250), 9,
                HbmIngredient.legacyOre("dustDuraSteel", 4), HbmIngredient.legacyOre("dustCopper", 1),
                HbmIngredient.legacyOre("dustBorax", 1));
        add(recipes, "aluminium_from_sodium_aluminate", Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(2),
                100, 400, new HbmFluidStack(HbmFluids.SODIUM_ALUMINATE, 150), 10);
        add(recipes, "aluminium_flux", Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(3), 40, 200,
                new HbmFluidStack(HbmFluids.SODIUM_ALUMINATE, 150), 11, ingredient("powder_flux", 2));
        return List.copyOf(recipes);
    }

    private static void add(List<RotaryFurnaceRecipe> recipes, String name,
            com.hbm.inventory.material.NTMMaterial material, int amount, int duration, int steam,
            @Nullable HbmFluidStack fluid, int sourceOrder, HbmIngredient... inputs) {
        recipes.add(new RotaryFurnaceRecipe(new ResourceLocation(HbmNtm.MOD_ID, "rotary_furnace/" + name),
                new MaterialStack(material, amount), duration, steam, fluid, List.of(inputs), sourceOrder));
    }

    private static HbmIngredient ingredient(String legacyName, int count) {
        return HbmIngredient.of(item(legacyName), count);
    }

    private static ItemLike item(String legacyName) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        if (item == null) {
            throw new IllegalStateException("Missing Rotary Furnace fallback item: " + legacyName);
        }
        return item.get();
    }

    public record Recipe(MaterialStack output, int duration, int steam, @Nullable HbmFluidStack fluid,
                         List<HbmIngredient> ingredients, int sourceOrder, ResourceLocation id) {
        public Recipe {
            output = output == null ? null : output.copy();
            duration = Math.max(1, duration);
            steam = Math.max(0, steam);
            fluid = fluid == null || fluid.isEmpty() ? null : fluid;
            ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
            sourceOrder = Math.max(0, sourceOrder);
        }

        public static Recipe fromDatapack(RotaryFurnaceRecipe recipe) {
            return new Recipe(recipe.output(), recipe.duration(), recipe.steam(), recipe.fluid(),
                    recipe.inputs(), recipe.sourceOrder(), recipe.getId());
        }

        public boolean matches(ItemStack first, ItemStack second, ItemStack third) {
            List<HbmIngredient> remaining = new ArrayList<>(ingredients);
            for (ItemStack stack : List.of(first, second, third)) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                boolean found = false;
                for (HbmIngredient ingredient : List.copyOf(remaining)) {
                    if (ingredient.test(stack)) {
                        remaining.remove(ingredient);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return remaining.isEmpty();
        }
    }
}
