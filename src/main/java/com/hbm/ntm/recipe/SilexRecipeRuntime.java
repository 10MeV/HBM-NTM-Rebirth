package com.hbm.ntm.recipe;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class SilexRecipeRuntime {
    private static final Comparator<SilexRecipe> RECIPE_ORDER = Comparator
            .comparingInt(SilexRecipe::sourceOrder)
            .thenComparing(recipe -> recipe.getId().toString());
    private static final List<SilexRecipe> FALLBACK_RECIPES = createFallbackRecipes();

    public static Optional<SilexRecipe> find(@Nullable Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return recipes(level == null ? null : level.getRecipeManager()).stream()
                .filter(recipe -> recipe.matches(stack))
                .findFirst();
    }

    public static Optional<SilexRecipe> find(ItemStack stack) {
        return find(null, stack);
    }

    public static Optional<SilexRecipe> findFluidSource(@Nullable Level level, FluidType fluid) {
        if (fluid == null || fluid == HbmFluids.NONE) {
            return Optional.empty();
        }
        return recipes(level == null ? null : level.getRecipeManager()).stream()
                .filter(recipe -> recipe.matchesFluid(fluid))
                .findFirst();
    }

    public static Optional<SilexRecipe> findFluidSource(FluidType fluid) {
        return findFluidSource(null, fluid);
    }

    public static boolean isValidInput(@Nullable Level level, ItemStack stack) {
        return find(level, stack).isPresent();
    }

    public static boolean isValidInput(ItemStack stack) {
        return isValidInput(null, stack);
    }

    public static List<DisplayRecipe> displayRecipes(@Nullable RecipeManager recipeManager) {
        return recipes(recipeManager).stream()
                .sorted(RECIPE_ORDER)
                .map(SilexRecipeRuntime::displayRecipe)
                .filter(recipe -> (!recipe.itemInputs().isEmpty()
                        || recipe.fluidInput().type() != HbmFluids.NONE && recipe.fluidInput().amount() > 0)
                        && !recipe.recipe().outputs().isEmpty())
                .toList();
    }

    public static List<DisplayRecipe> displayRecipes() {
        return displayRecipes(null);
    }

    private static List<SilexRecipe> recipes(@Nullable RecipeManager recipeManager) {
        if (recipeManager != null) {
            return recipeManager.getAllRecipesFor(ModRecipes.SILEX.type().get()).stream()
                    .sorted(RECIPE_ORDER)
                    .toList();
        }
        return FALLBACK_RECIPES;
    }

    private static DisplayRecipe displayRecipe(SilexRecipe recipe) {
        if (recipe.hasFluidSource()) {
            return DisplayRecipe.fluidSource(recipe.fluidSource(), recipe);
        }
        HbmIngredient itemSource = recipe.itemSource();
        return DisplayRecipe.itemSource(itemSource == null ? List.of() : itemSource.displayStacks(), recipe);
    }

    private static List<SilexRecipe> createFallbackRecipes() {
        List<SilexRecipe> recipes = new ArrayList<>();
        addItem(recipes, "ingot_uranium", HbmIngredient.legacyOre("ingotUranium", 1), 900, 100,
                LaserWavelength.VISIBLE, 0, out("nugget_u235", 1), out("nugget_u238", 11));
        addItem(recipes, "dust_uranium", HbmIngredient.legacyOre("dustUranium", 1), 900, 100,
                LaserWavelength.VISIBLE, 1, out("nugget_u235", 1), out("nugget_u238", 11));
        addFluid(recipes, "uf6", HbmFluids.UF6, 900, 100, LaserWavelength.VISIBLE, 2,
                out("nugget_u235", 1), out("nugget_u238", 11));
        addItem(recipes, "ingot_pu_mix", ingredient("ingot_pu_mix"), 900, 100, LaserWavelength.VISIBLE, 3,
                out("nugget_pu239", 6), out("nugget_pu240", 3));
        addItem(recipes, "ingot_am_mix", ingredient("ingot_am_mix"), 900, 100, LaserWavelength.VISIBLE, 4,
                out("nugget_am241", 3), out("nugget_am242", 6));
        addItem(recipes, "ingot_plutonium", HbmIngredient.legacyOre("ingotPlutonium", 1), 900, 100,
                LaserWavelength.VISIBLE, 5, out("nugget_pu238", 3), out("nugget_pu239", 4),
                out("nugget_pu240", 2));
        addItem(recipes, "dust_plutonium", HbmIngredient.legacyOre("dustPlutonium", 1), 900, 100,
                LaserWavelength.VISIBLE, 6, out("nugget_pu238", 3), out("nugget_pu239", 4),
                out("nugget_pu240", 2));
        addFluid(recipes, "puf6", HbmFluids.PUF6, 900, 100, LaserWavelength.VISIBLE, 7,
                out("nugget_pu238", 3), out("nugget_pu239", 4), out("nugget_pu240", 2));
        addItem(recipes, "ingot_schraranium", ingredient("ingot_schraranium"), 900, 100,
                LaserWavelength.VISIBLE, 8, out("nugget_schrabidium", 4), out("nugget_uranium", 3),
                out("nugget_neptunium", 2));
        addItem(recipes, "ingot_australium", ingredient("ingot_australium"), 900, 100,
                LaserWavelength.VISIBLE, 9, out("nugget_australium_lesser", 5),
                out("nugget_australium_greater", 1));
        addItem(recipes, "powder_australium", ingredient("powder_australium"), 900, 100,
                LaserWavelength.VISIBLE, 10, out("nugget_australium_lesser", 5),
                out("nugget_australium_greater", 1));
        addItem(recipes, "crystal_schraranium", ingredient("crystal_schraranium"), 900, 100,
                LaserWavelength.UV, 11, out("nugget_schrabidium", 5), out("nugget_uranium", 2),
                out("nugget_neptunium", 2));
        addItem(recipes, "ore_tikite", HbmIngredient.of(block("ore_tikite"), 1), 900, 100,
                LaserWavelength.UV, 12, out("powder_plutonium", 2), out("powder_cobalt", 3),
                out("powder_niobium", 3), out("powder_nitan_mix", 2));
        addItem(recipes, "crystal_trixite", ingredient("crystal_trixite"), 1_200, 100,
                LaserWavelength.UV, 13, out("powder_plutonium", 2), out("powder_cobalt", 3),
                out("powder_niobium", 3), out("powder_nitan_mix", 1), out("powder_spark_mix", 1));
        addItem(recipes, "powder_lapis", ingredient("powder_lapis"), 100, 100, LaserWavelength.IR, 14,
                out("sulfur", 4), out("powder_aluminium", 3), out("powder_cobalt", 3));
        addItem(recipes, "lapis_lazuli", HbmIngredient.of(Items.LAPIS_LAZULI, 1), 100, 100,
                LaserWavelength.IR, 15, out("sulfur", 4), out("powder_aluminium", 3),
                out("powder_cobalt", 3));
        addFluid(recipes, "death", HbmFluids.DEATH, 1_000, 1_000, LaserWavelength.GAMMA, 16,
                out("powder_impure_osmiridium", 1));
        addFluid(recipes, "vitriol", HbmFluids.VITRIOL, 1_000, 300, LaserWavelength.IR, 17,
                out("powder_bromine", 5), out("powder_iodine", 5), out("powder_iron", 5),
                out("sulfur", 15));
        addFluid(recipes, "redmud", HbmFluids.REDMUD, 300, 50, LaserWavelength.VISIBLE, 18,
                out("powder_aluminium", 10), out("powder_neodymium_tiny", 5, 3),
                out("powder_boron_tiny", 5, 3), out("nugget_zirconium", 5), out("powder_iron", 20),
                out("powder_titanium", 15), out("powder_sodium", 10));
        addItem(recipes, "gravel", HbmIngredient.of(Items.GRAVEL, 1), 1_000, 250, LaserWavelength.VISIBLE, 19,
                new SilexRecipe.WeightedOutput(new ItemStack(Items.FLINT), 80), out("powder_boron", 5),
                out("powder_lithium", 10), out("fluorite", 5));
        addFluid(recipes, "fullerene", HbmFluids.FULLERENE, 1_000, 1_000, LaserWavelength.VISIBLE, 20,
                out("powder_ash_fullerene", 1));
        return List.copyOf(recipes);
    }

    private static void addItem(List<SilexRecipe> recipes, String name, HbmIngredient source, int fluidProduced,
            int fluidConsumed, LaserWavelength laserStrength, int sourceOrder, SilexRecipe.WeightedOutput... outputs) {
        recipes.add(new SilexRecipe(new ResourceLocation(HbmNtm.MOD_ID, "silex/" + name), source, HbmFluids.NONE,
                fluidProduced, fluidConsumed, laserStrength, List.of(outputs), sourceOrder));
    }

    private static void addFluid(List<SilexRecipe> recipes, String name, FluidType source, int fluidProduced,
            int fluidConsumed, LaserWavelength laserStrength, int sourceOrder, SilexRecipe.WeightedOutput... outputs) {
        recipes.add(new SilexRecipe(new ResourceLocation(HbmNtm.MOD_ID, "silex/" + name), null, source,
                fluidProduced, fluidConsumed, laserStrength, List.of(outputs), sourceOrder));
    }

    private static HbmIngredient ingredient(String legacyName) {
        return HbmIngredient.of(item(legacyName), 1);
    }

    private static SilexRecipe.WeightedOutput out(String legacyName, int weight) {
        return out(legacyName, weight, 1);
    }

    private static SilexRecipe.WeightedOutput out(String legacyName, int weight, int count) {
        return new SilexRecipe.WeightedOutput(new ItemStack(item(legacyName), Math.max(1, count)), weight);
    }

    private static ItemLike item(String legacyName) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        if (item == null) {
            throw new IllegalStateException("Missing SILEX fallback item: " + legacyName);
        }
        return item.get();
    }

    private static ItemLike block(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        if (block == null) {
            throw new IllegalStateException("Missing SILEX fallback block: " + legacyName);
        }
        return block.get();
    }

    public record DisplayRecipe(List<ItemStack> itemInputs, HbmFluidStack fluidInput, SilexRecipe recipe,
                                boolean directFluidSource) {
        public DisplayRecipe {
            itemInputs = itemInputs == null ? List.of() : itemInputs.stream()
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
            fluidInput = fluidInput == null ? new HbmFluidStack(HbmFluids.NONE, 0, 0) : fluidInput;
        }

        private static DisplayRecipe itemSource(List<ItemStack> inputs, SilexRecipe recipe) {
            return new DisplayRecipe(inputs,
                    new HbmFluidStack(HbmFluids.PEROXIDE, recipe.fluidProduced(), 0),
                    recipe, false);
        }

        private static DisplayRecipe fluidSource(FluidType fluid, SilexRecipe recipe) {
            return new DisplayRecipe(List.of(),
                    new HbmFluidStack(fluid, recipe.fluidConsumed(), 0),
                    recipe, true);
        }
    }

    private SilexRecipeRuntime() {
    }
}
