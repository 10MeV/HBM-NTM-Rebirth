package com.hbm.ntm.recipe;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class ElectrolyserRecipeRuntime {
    private static final List<FluidRecipe> DEFAULT_FLUID_RECIPES = List.of(
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
    private static final Map<FluidType, FluidRecipe> RUNTIME_FLUID_RECIPES = new LinkedHashMap<>();

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
        return fluidRecipes(null);
    }

    public static List<FluidRecipe> fluidRecipes(@Nullable RecipeManager recipeManager) {
        Map<FluidType, FluidRecipe> recipes = new LinkedHashMap<>();
        if (recipeManager != null) {
            for (ElectrolyserFluidRecipe recipe : recipeManager.getAllRecipesFor(ModRecipes.ELECTROLYZER_FLUID.type().get())) {
                FluidRecipe entry = fromDatapack(recipe);
                recipes.put(entry.input(), entry);
            }
        }
        for (Map.Entry<FluidType, FluidRecipe> entry : RUNTIME_FLUID_RECIPES.entrySet()) {
            recipes.putIfAbsent(entry.getKey(), entry.getValue());
        }
        if (recipes.isEmpty()) {
            for (FluidRecipe recipe : DEFAULT_FLUID_RECIPES) {
                recipes.put(recipe.input(), recipe);
            }
        }
        return new ArrayList<>(recipes.values());
    }

    public static List<MetalRecipe> metalRecipes() {
        return metalRecipes(null);
    }

    public static List<MetalRecipe> metalRecipes(@Nullable RecipeManager recipeManager) {
        List<MetalRecipe> recipes = new ArrayList<>();
        if (recipeManager != null) {
            for (ElectrolyserMetalRecipe recipe : recipeManager.getAllRecipesFor(ModRecipes.ELECTROLYZER_METAL.type().get())) {
                recipes.add(fromDatapack(recipe));
            }
        }
        if (recipes.isEmpty()) {
            recipes.addAll(METAL_RECIPES);
        }
        return List.copyOf(recipes);
    }

    public static List<DisplayRecipe> displayRecipes() {
        return displayRecipes(null);
    }

    public static List<DisplayRecipe> displayRecipes(@Nullable RecipeManager recipeManager) {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (FluidRecipe recipe : fluidRecipes(recipeManager)) {
            recipes.add(DisplayRecipe.fluid(recipe));
        }
        for (MetalRecipe recipe : metalRecipes(recipeManager)) {
            if (!recipe.inputStack().isEmpty()) {
                recipes.add(DisplayRecipe.metal(recipe));
            }
        }
        return List.copyOf(recipes);
    }

    @Nullable
    public static FluidRecipe fluidForInput(FluidType input) {
        FluidRecipe recipe = RUNTIME_FLUID_RECIPES.get(input);
        if (recipe != null) {
            return recipe;
        }
        return defaultFluidForInput(input);
    }

    @Nullable
    public static FluidRecipe fluidForInput(@Nullable Level level, FluidType input) {
        if (level != null) {
            List<ElectrolyserFluidRecipe> datapackRecipes =
                    level.getRecipeManager().getAllRecipesFor(ModRecipes.ELECTROLYZER_FLUID.type().get());
            FluidRecipe recipe = findDatapack(datapackRecipes, input);
            if (recipe != null) {
                return recipe;
            }
            recipe = RUNTIME_FLUID_RECIPES.get(input);
            if (recipe != null || !datapackRecipes.isEmpty()) {
                return recipe;
            }
        }
        return fluidForInput(input);
    }

    public static FluidRecipe registerFluid(HbmFluidStack input, HbmFluidStack output1, HbmFluidStack output2,
            int duration, ItemStack... byproducts) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Electrolyser fluid input cannot be empty");
        }
        FluidRecipe recipe = new FluidRecipe(input.type(), input.amount(),
                output1 == null ? HbmFluids.NONE : output1.type(),
                output1 == null ? 0 : output1.amount(),
                output2 == null ? HbmFluids.NONE : output2.type(),
                output2 == null ? 0 : output2.amount(), duration, byproductList(byproducts));
        RUNTIME_FLUID_RECIPES.put(recipe.input(), recipe);
        return recipe;
    }

    @Nullable
    public static MetalRecipe metalForInput(ItemStack stack) {
        return metalForInput(null, stack);
    }

    @Nullable
    public static MetalRecipe metalForInput(@Nullable Level level, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        RecipeManager recipeManager = level == null ? null : level.getRecipeManager();
        for (MetalRecipe recipe : metalRecipes(recipeManager)) {
            if (recipe.matches(stack)) {
                return recipe;
            }
        }
        return null;
    }

    private static FluidRecipe fluid(FluidType input, int amount, FluidType output1Type, int output1Amount,
            FluidType output2Type, int output2Amount, int duration, ItemStack... byproducts) {
        return new FluidRecipe(input, amount, output1Type, output1Amount, output2Type, output2Amount, duration,
                byproductList(byproducts));
    }

    private static List<ItemStack> byproductList(ItemStack... byproducts) {
        List<ItemStack> outputs = new ArrayList<>();
        if (byproducts != null) {
            for (ItemStack byproduct : byproducts) {
                if (!byproduct.isEmpty()) {
                    outputs.add(byproduct);
                }
            }
        }
        return List.copyOf(outputs);
    }

    @Nullable
    private static FluidRecipe defaultFluidForInput(FluidType input) {
        for (FluidRecipe recipe : DEFAULT_FLUID_RECIPES) {
            if (recipe.input() == input) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    private static FluidRecipe findDatapack(List<ElectrolyserFluidRecipe> recipes, FluidType input) {
        for (ElectrolyserFluidRecipe recipe : recipes) {
            FluidRecipe entry = fromDatapack(recipe);
            if (entry.input() == input) {
                return entry;
            }
        }
        return null;
    }

    private static FluidRecipe fromDatapack(ElectrolyserFluidRecipe recipe) {
        HbmFluidStack input = recipe.input();
        HbmFluidStack output1 = recipe.output1();
        HbmFluidStack output2 = recipe.output2();
        return new FluidRecipe(input.type(), input.amount(), output1.type(), output1.amount(),
                output2.type(), output2.amount(), recipe.duration(), recipe.byproducts());
    }

    private static MetalRecipe fromDatapack(ElectrolyserMetalRecipe recipe) {
        return new MetalRecipe("", recipe.input(), recipe.output1(), recipe.output2(), recipe.duration(),
                recipe.byproducts());
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
        return new MetalRecipe(inputName, null, output1, output2, duration, List.copyOf(outputs));
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

    public record MetalRecipe(String inputName, @Nullable HbmIngredient input, MaterialStack output1,
                              @Nullable MaterialStack output2, int duration, List<ItemStack> byproducts) {
        public MetalRecipe {
            inputName = inputName == null ? "" : inputName;
            output1 = output1 == null ? null : output1.copy();
            output2 = output2 == null ? null : output2.copy();
            duration = Math.max(1, duration);
            byproducts = byproducts == null ? List.of() : List.copyOf(byproducts);
        }

        public ItemStack inputStack() {
            if (input != null) {
                List<ItemStack> stacks = input.displayStacks();
                return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0).copy();
            }
            RegistryObject<Item> item = ModItems.legacyItem(inputName);
            return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
        }

        private boolean matches(ItemStack stack) {
            if (input != null) {
                return input.test(stack);
            }
            RegistryObject<Item> item = ModItems.legacyItem(inputName);
            return item != null && stack.is(item.get());
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
