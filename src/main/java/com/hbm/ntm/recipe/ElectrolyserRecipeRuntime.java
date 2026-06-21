package com.hbm.ntm.recipe;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class ElectrolyserRecipeRuntime {
    private static final List<FluidRecipe> FLUID_RECIPES = List.of(
            fluid(HbmFluids.WATER, 2_000, HbmFluids.HYDROGEN, 200, HbmFluids.OXYGEN, 200, 10),
            fluid(HbmFluids.HEAVYWATER, 2_000, HbmFluids.DEUTERIUM, 200, HbmFluids.OXYGEN, 200, 10),
            fluid(HbmFluids.VITRIOL, 1_000, HbmFluids.SULFURIC_ACID, 500, HbmFluids.CHLORINE, 500, 20,
                    stack("powder_iron"), stack("ingot_mercury")),
            fluid(HbmFluids.SLOP, 1_000, HbmFluids.MERCURY, 250, HbmFluids.NONE, 0, 20,
                    stack("niter", 2), stack("powder_limestone", 2), stack("sulfur")),
            fluid(HbmFluids.REDMUD, 450, HbmFluids.MERCURY, 150, HbmFluids.LYE, 50, 20,
                    stack("powder_titanium", 3), stack("powder_iron", 3), stack("powder_aluminium", 2)),
            fluid(HbmFluids.ALUMINA, 200, HbmFluids.CARBONDIOXIDE, 100, HbmFluids.NONE, 0, 40,
                    stack("powder_aluminium", 7), stack("fluorite", 2)),
            fluid(HbmFluids.POTASSIUM_CHLORIDE, 250, HbmFluids.CHLORINE, 125, HbmFluids.NONE, 0, 20,
                    stack("dust")),
            fluid(HbmFluids.CALCIUM_CHLORIDE, 250, HbmFluids.CHLORINE, 125,
                    HbmFluids.CALCIUM_SOLUTION, 125, 20));

    private static final List<MetalRecipe> METAL_RECIPES = List.of(
            metal("crystal_iron",
                    stack(Mats.MAT_IRON, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_TITANIUM, MaterialShapes.INGOT.q(2)),
                    600, item("powder_lithium_tiny", 3)),
            metal("crystal_gold",
                    stack(Mats.MAT_GOLD, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_LEAD, MaterialShapes.INGOT.q(2)),
                    600, item("powder_lithium_tiny", 3), item("ingot_mercury", 2)),
            metal("crystal_uranium",
                    stack(Mats.MAT_URANIUM, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_RADIUM, MaterialShapes.NUGGET.q(4)),
                    600, item("powder_lithium_tiny", 3)),
            metal("crystal_thorium",
                    stack(Mats.MAT_THORIUM, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_URANIUM, MaterialShapes.INGOT.q(2)),
                    600, item("powder_lithium_tiny", 3)),
            metal("crystal_plutonium",
                    stack(Mats.MAT_PLUTONIUM, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_POLONIUM, MaterialShapes.INGOT.q(2)),
                    600, item("powder_lithium_tiny", 3)),
            metal("crystal_titanium",
                    stack(Mats.MAT_TITANIUM, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_IRON, MaterialShapes.INGOT.q(2)),
                    600, item("powder_lithium_tiny", 3)),
            metal("crystal_copper",
                    stack(Mats.MAT_COPPER, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_LEAD, MaterialShapes.NUGGET.q(4)),
                    600, item("powder_lithium_tiny", 3), item("sulfur", 2)),
            metal("crystal_tungsten",
                    stack(Mats.MAT_TUNGSTEN, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_IRON, MaterialShapes.INGOT.q(2)),
                    600, item("powder_lithium_tiny", 3)),
            metal("crystal_aluminium",
                    stack(Mats.MAT_ALUMINIUM, MaterialShapes.INGOT.q(2)),
                    stack(Mats.MAT_IRON, MaterialShapes.INGOT.q(2)),
                    600, item("chunk_ore_cryolite", 4), item("powder_lithium_tiny", 3)),
            metal("crystal_beryllium",
                    stack(Mats.MAT_BERYLLIUM, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_LEAD, MaterialShapes.NUGGET.q(4)),
                    600, item("powder_lithium_tiny", 3), item("powder_quartz", 2)),
            metal("crystal_lead",
                    stack(Mats.MAT_LEAD, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_GOLD, MaterialShapes.INGOT.q(2)),
                    600, item("powder_lithium_tiny", 3)),
            metal("crystal_schraranium",
                    stack(Mats.MAT_SCHRABIDIUM, MaterialShapes.NUGGET.q(5)),
                    stack(Mats.MAT_URANIUM, MaterialShapes.NUGGET.q(2)),
                    600, item("nugget_neptunium", 2)),
            metal("crystal_schrabidium",
                    stack(Mats.MAT_SCHRABIDIUM, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_PLUTONIUM, MaterialShapes.INGOT.q(2)),
                    600, item("powder_lithium_tiny", 3)),
            metal("crystal_rare",
                    stack(Mats.MAT_ZIRCONIUM, MaterialShapes.NUGGET.q(6)),
                    stack(Mats.MAT_BORON, MaterialShapes.NUGGET.q(2)),
                    600, item("powder_desh_mix", 3)),
            metal("crystal_trixite",
                    stack(Mats.MAT_PLUTONIUM, MaterialShapes.INGOT.q(3)),
                    stack(Mats.MAT_COBALT, MaterialShapes.INGOT.q(4)),
                    600, item("powder_niobium", 4), item("powder_nitan_mix", 2)),
            metal("crystal_lithium",
                    stack(Mats.MAT_LITHIUM, MaterialShapes.INGOT.q(6)),
                    stack(Mats.MAT_BORON, MaterialShapes.INGOT.q(2)),
                    600, item("powder_quartz", 2), item("fluorite", 2)),
            metal("crystal_starmetal",
                    stack(Mats.MAT_DURA, MaterialShapes.INGOT.q(4)),
                    stack(Mats.MAT_COBALT, MaterialShapes.INGOT.q(4)),
                    600, item("powder_astatine", 3), item("ingot_mercury", 8)),
            metal("crystal_cobalt",
                    stack(Mats.MAT_COBALT, MaterialShapes.INGOT.q(3)),
                    stack(Mats.MAT_IRON, MaterialShapes.INGOT.q(4)),
                    600, item("powder_copper", 4), item("powder_lithium_tiny", 3)));

    private ElectrolyserRecipeRuntime() {
    }

    public static List<FluidRecipe> fluidRecipes() {
        return FLUID_RECIPES;
    }

    public static List<MetalRecipe> metalRecipes() {
        return METAL_RECIPES;
    }

    public static List<DisplayRecipe> displayRecipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (FluidRecipe recipe : FLUID_RECIPES) {
            recipes.add(DisplayRecipe.fluid(recipe));
        }
        for (MetalRecipe recipe : METAL_RECIPES) {
            if (!recipe.inputStack().isEmpty()) {
                recipes.add(DisplayRecipe.metal(recipe));
            }
        }
        return List.copyOf(recipes);
    }

    @Nullable
    public static FluidRecipe fluidForInput(FluidType input) {
        for (FluidRecipe recipe : FLUID_RECIPES) {
            if (recipe.input() == input) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    public static MetalRecipe metalForInput(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        for (MetalRecipe recipe : METAL_RECIPES) {
            RegistryObject<Item> item = ModItems.legacyItem(recipe.inputName());
            if (item != null && stack.is(item.get())) {
                return recipe;
            }
        }
        return null;
    }

    private static FluidRecipe fluid(FluidType input, int amount, FluidType output1Type, int output1Amount,
            FluidType output2Type, int output2Amount, int duration, ItemStack... byproducts) {
        List<ItemStack> outputs = new ArrayList<>();
        if (byproducts != null) {
            for (ItemStack byproduct : byproducts) {
                if (!byproduct.isEmpty()) {
                    outputs.add(byproduct);
                }
            }
        }
        return new FluidRecipe(input, amount, output1Type, output1Amount, output2Type, output2Amount, duration,
                List.copyOf(outputs));
    }

    private static ItemStack stack(String legacyName) {
        return stack(legacyName, 1);
    }

    private static ItemStack stack(String legacyName, int count) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get(), count);
    }

    private static MetalRecipe metal(String inputName, MaterialStack output1,
            @Nullable MaterialStack output2, int duration, ItemStack... byproducts) {
        List<ItemStack> outputs = new ArrayList<>();
        if (byproducts != null) {
            for (ItemStack byproduct : byproducts) {
                if (!byproduct.isEmpty()) {
                    outputs.add(byproduct);
                }
            }
        }
        return new MetalRecipe(inputName, output1, output2, duration, List.copyOf(outputs));
    }

    private static MaterialStack stack(com.hbm.inventory.material.NTMMaterial material, int amount) {
        return new MaterialStack(material, amount);
    }

    private static ItemStack item(String legacyName, int count) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get(), count);
    }

    public record FluidRecipe(FluidType input, int amount, FluidType output1Type, int output1Amount,
                              FluidType output2Type, int output2Amount, int duration,
                              List<ItemStack> byproducts) {
        public FluidRecipe {
            amount = Math.max(1, amount);
            output1Amount = Math.max(0, output1Amount);
            output2Amount = Math.max(0, output2Amount);
            duration = Math.max(1, duration);
            byproducts = byproducts == null ? List.of() : List.copyOf(byproducts);
        }
    }

    public record MetalRecipe(String inputName, MaterialStack output1, @Nullable MaterialStack output2,
                              int duration, List<ItemStack> byproducts) {
        public MetalRecipe {
            output1 = output1 == null ? null : output1.copy();
            output2 = output2 == null ? null : output2.copy();
            duration = Math.max(1, duration);
            byproducts = byproducts == null ? List.of() : List.copyOf(byproducts);
        }

        public ItemStack inputStack() {
            RegistryObject<Item> item = ModItems.legacyItem(inputName);
            return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
        }
    }

    public record DisplayRecipe(@Nullable FluidRecipe fluid, @Nullable MetalRecipe metal) {
        public static DisplayRecipe fluid(FluidRecipe recipe) {
            return new DisplayRecipe(recipe, null);
        }

        public static DisplayRecipe metal(MetalRecipe recipe) {
            return new DisplayRecipe(null, recipe);
        }

        public boolean isFluid() {
            return fluid != null;
        }
    }
}