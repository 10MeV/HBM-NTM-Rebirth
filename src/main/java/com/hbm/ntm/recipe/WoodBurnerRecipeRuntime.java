package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fuel.LegacyBurnTimeModule;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

public final class WoodBurnerRecipeRuntime {
    private static final LegacyBurnTimeModule BURN_MODULE = new LegacyBurnTimeModule()
            .setLogTimeMod(4)
            .setWoodTimeMod(2);
    private static final String[] LEGACY_SOLID_INPUTS = {
            "powder_sawdust", "briquette_wood", "biomass", "biomass_compressed",
            "powder_coal", "briquette_coal", "briquette_lignite", "coke", "lignite",
            "solid_fuel", "solid_fuel_presto", "solid_fuel_presto_triplet"
    };

    private WoodBurnerRecipeRuntime() {
    }

    public static LegacyBurnTimeModule burnModule() {
        return BURN_MODULE;
    }

    public static List<SolidFuelRecipe> solidFuelRecipes() {
        List<SolidFuelRecipe> recipes = new ArrayList<>();
        addVanilla(recipes, new ItemStack(Items.OAK_LOG));
        addVanilla(recipes, new ItemStack(Items.OAK_PLANKS));
        addVanilla(recipes, new ItemStack(Items.CHARCOAL));
        addVanilla(recipes, new ItemStack(Items.COAL));
        for (String name : LEGACY_SOLID_INPUTS) {
            RegistryObject<net.minecraft.world.item.Item> item = ModItems.legacyItem(name);
            if (item != null) {
                addVanilla(recipes, new ItemStack(item.get()));
            }
        }
        return List.copyOf(recipes);
    }

    private static void addVanilla(List<SolidFuelRecipe> recipes, ItemStack stack) {
        int burn = BURN_MODULE.getBurnTime(stack);
        if (burn > 0) {
            recipes.add(new SolidFuelRecipe(stack, burn));
        }
    }

    public static List<LiquidFuelRecipe> liquidFuelRecipes() {
        return HbmFluids.all().stream()
                .map(type -> new LiquidFuelRecipe(type,
                        type.getTrait(FlammableFluidTrait.class)))
                .filter(recipe -> recipe.trait() != null && recipe.trait().getHeatEnergyPerBucket() > 0)
                .toList();
    }

    public static List<DisplayRecipe> displayRecipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (SolidFuelRecipe recipe : solidFuelRecipes()) {
            recipes.add(DisplayRecipe.solid(recipe));
        }
        for (LiquidFuelRecipe recipe : liquidFuelRecipes()) {
            recipes.add(DisplayRecipe.liquid(recipe));
        }
        return List.copyOf(recipes);
    }

    public record SolidFuelRecipe(ItemStack input, int burnTime) {
        public SolidFuelRecipe {
            input = input == null ? ItemStack.EMPTY : input.copy();
            burnTime = Math.max(1, burnTime);
        }
    }

    public record LiquidFuelRecipe(FluidType type, FlammableFluidTrait trait) {
        public HbmFluidStack displayFluid() {
            return new HbmFluidStack(type, 1_000, 0);
        }

        public long powerPerBucket() {
            return trait == null ? 0L : trait.getHeatEnergyPerBucket() / 2L;
        }
    }

    public record DisplayRecipe(SolidFuelRecipe solid, LiquidFuelRecipe liquid) {
        public static DisplayRecipe solid(SolidFuelRecipe recipe) {
            return new DisplayRecipe(recipe, null);
        }

        public static DisplayRecipe liquid(LiquidFuelRecipe recipe) {
            return new DisplayRecipe(null, recipe);
        }

        public boolean isSolid() {
            return solid != null;
        }
    }
}