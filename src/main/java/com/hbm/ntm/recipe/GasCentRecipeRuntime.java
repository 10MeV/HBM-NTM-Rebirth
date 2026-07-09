package com.hbm.ntm.recipe;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.GasCentBlockEntity.PseudoFluidType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class GasCentRecipeRuntime {
    private static final Comparator<GasCentRecipe> RECIPE_ORDER = Comparator
            .comparingInt(GasCentRecipe::sourceOrder)
            .thenComparing(recipe -> recipe.getId().toString());
    private static final List<GasCentRecipe> FALLBACK_RECIPES = createFallbackRecipes();

    public static List<GasCentRecipe> recipes(@Nullable RecipeManager recipeManager) {
        if (recipeManager != null) {
            return recipeManager.getAllRecipesFor(ModRecipes.GAS_CENT.type().get()).stream()
                    .sorted(RECIPE_ORDER)
                    .toList();
        }
        return FALLBACK_RECIPES;
    }

    public static List<GasCentRecipe> recipes() {
        return recipes(null);
    }

    private static List<GasCentRecipe> createFallbackRecipes() {
        List<GasCentRecipe> recipes = new ArrayList<>();
        add(recipes, "uf6_full_chain", HbmFluids.UF6, 1_200, true, 4, PseudoFluidType.NUF6,
                PseudoFluidType.NONE, 0,
                out("nugget_u238", 11), out("nugget_u235", 1), out("fluorite", 4));
        add(recipes, "uf6_fuel_chain", HbmFluids.UF6, 1_200, false, 2, PseudoFluidType.LEUF6,
                PseudoFluidType.NONE, 1,
                out("nugget_u238", 6), out("nugget_uranium_fuel", 6), out("fluorite", 4));
        add(recipes, "puf6", HbmFluids.PUF6, 900, false, 1, PseudoFluidType.PF6,
                PseudoFluidType.NONE, 2,
                out("nugget_pu238", 3), out("nugget_pu_mix", 6), out("fluorite", 3));
        add(recipes, "watz_mud", HbmFluids.WATZ, 1_000, false, 2, PseudoFluidType.MUD,
                PseudoFluidType.NONE, 3,
                out("powder_iron", 1), out("powder_lead", 1), out("nuclear_waste_tiny", 1),
                out("dust", 2));
        return List.copyOf(recipes.stream().sorted(RECIPE_ORDER).toList());
    }

    private static void add(List<GasCentRecipe> recipes, String name, FluidType fluid, int amount, boolean highSpeed,
            int centrifuges, PseudoFluidType inputType, PseudoFluidType outputType, int sourceOrder,
            ItemStack... outputs) {
        recipes.add(new GasCentRecipe(new ResourceLocation(HbmNtm.MOD_ID, "gas_cent/" + name),
                new HbmFluidStack(fluid, amount), List.of(outputs), highSpeed, centrifuges, inputType, outputType,
                sourceOrder));
    }

    private static ItemStack out(String legacyName, int count) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        if (item == null) {
            throw new IllegalStateException("Missing gas centrifuge fallback item: " + legacyName);
        }
        return new ItemStack(item.get(), count);
    }

    private GasCentRecipeRuntime() {
    }
}
