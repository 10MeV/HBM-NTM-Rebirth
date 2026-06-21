package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

public final class SilexRecipeRuntime {
    private static final List<Entry> ENTRIES = createEntries();

    public static Optional<SilexRecipe> find(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        for (Entry entry : ENTRIES) {
            if (entry.matches(stack)) {
                return Optional.of(entry.recipe());
            }
        }
        return Optional.empty();
    }

    public static Optional<SilexRecipe> findFluidSource(FluidType fluid) {
        if (fluid == HbmFluids.UF6) {
            return find(legacyStack("ingot_uranium"));
        }
        if (fluid == HbmFluids.PUF6) {
            return find(legacyStack("ingot_plutonium"));
        }
        if (fluid == HbmFluids.DEATH) {
            return find(legacyStack("powder_impure_osmiridium"));
        }
        if (fluid == HbmFluids.VITRIOL) {
            return recipeForFluidOutputs(1_000, 300, LaserWavelength.IR,
                    weighted("powder_bromine", 5),
                    weighted("powder_iodine", 5),
                    weighted("powder_iron", 5),
                    weighted("sulfur", 15));
        }
        if (fluid == HbmFluids.REDMUD) {
            return recipeForFluidOutputs(300, 50, LaserWavelength.VISIBLE,
                    weighted("powder_aluminium", 10),
                    weighted("powder_neodymium_tiny", 5, 3),
                    weighted("powder_boron_tiny", 5, 3),
                    weighted("nugget_zirconium", 5),
                    weighted("powder_iron", 20),
                    weighted("powder_titanium", 15),
                    weighted("powder_sodium", 10));
        }
        if (fluid == HbmFluids.FULLERENE) {
            return recipeForFluidOutputs(1_000, 1_000, LaserWavelength.VISIBLE,
                    weighted("powder_ash", 1));
        }
        return Optional.empty();
    }

    public static boolean isValidInput(ItemStack stack) {
        return find(stack).isPresent();
    }

    public static List<DisplayRecipe> displayRecipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            recipes.add(DisplayRecipe.itemSource(entry.input(), entry.recipe()));
        }
        addFluidDisplayRecipe(recipes, HbmFluids.UF6);
        addFluidDisplayRecipe(recipes, HbmFluids.PUF6);
        addFluidDisplayRecipe(recipes, HbmFluids.DEATH);
        addFluidDisplayRecipe(recipes, HbmFluids.VITRIOL);
        addFluidDisplayRecipe(recipes, HbmFluids.REDMUD);
        addFluidDisplayRecipe(recipes, HbmFluids.FULLERENE);
        return List.copyOf(recipes);
    }

    private static void addFluidDisplayRecipe(List<DisplayRecipe> recipes, FluidType fluid) {
        findFluidSource(fluid).ifPresent(recipe -> recipes.add(DisplayRecipe.fluidSource(fluid, recipe)));
    }

    private static List<Entry> createEntries() {
        List<Entry> entries = new ArrayList<>();
        add(entries, "ingot_uranium", new SilexRecipe(900, 100, LaserWavelength.VISIBLE,
                outputs(weighted("nugget_u235", 1), weighted("nugget_u238", 11))));
        add(entries, "powder_uranium", new SilexRecipe(900, 100, LaserWavelength.VISIBLE,
                outputs(weighted("nugget_u235", 1), weighted("nugget_u238", 11))));
        add(entries, "ingot_plutonium", new SilexRecipe(900, 100, LaserWavelength.UV,
                outputs(weighted("nugget_pu238", 3), weighted("nugget_pu239", 4), weighted("nugget_pu240", 2))));
        add(entries, "powder_plutonium", new SilexRecipe(900, 100, LaserWavelength.UV,
                outputs(weighted("nugget_pu238", 3), weighted("nugget_pu239", 4), weighted("nugget_pu240", 2))));
        add(entries, "ingot_pu_mix", new SilexRecipe(900, 100, LaserWavelength.UV,
                outputs(weighted("nugget_pu239", 6), weighted("nugget_pu240", 3))));
        add(entries, "ingot_am_mix", new SilexRecipe(900, 100, LaserWavelength.UV,
                outputs(weighted("nugget_am241", 3), weighted("nugget_am242", 6))));
        add(entries, "ingot_schraranium", new SilexRecipe(900, 100, LaserWavelength.UV,
                outputs(weighted("nugget_schrabidium", 4),
                        weighted("nugget_uranium", 3),
                        weighted("nugget_neptunium", 2))));
        SilexRecipe australiumRecipe = new SilexRecipe(900, 100, LaserWavelength.VISIBLE,
                outputs(weighted("nugget_australium_lesser", 5),
                        weighted("nugget_australium_greater", 1)));
        add(entries, "ingot_australium", australiumRecipe);
        add(entries, "powder_australium", australiumRecipe);
        add(entries, "crystal_schraranium", new SilexRecipe(900, 100, LaserWavelength.GAMMA,
                outputs(weighted("nugget_schrabidium", 5),
                        weighted("nugget_uranium", 2),
                        weighted("nugget_neptunium", 2))));
        addBlock(entries, "ore_tikite", new SilexRecipe(900, 100, LaserWavelength.UV,
                outputs(weighted("powder_plutonium", 2),
                        weighted("powder_cobalt", 3),
                        weighted("powder_niobium", 3),
                        weighted("powder_nitan_mix", 2))));
        add(entries, "crystal_trixite", new SilexRecipe(1_200, 100, LaserWavelength.UV,
                outputs(weighted("powder_plutonium", 2),
                        weighted("powder_cobalt", 3),
                        weighted("powder_niobium", 3),
                        weighted("powder_nitan_mix", 1),
                        weighted("powder_spark_mix", 1))));
        add(entries, "powder_lapis", new SilexRecipe(100, 100, LaserWavelength.IR,
                outputs(weighted("sulfur", 4),
                        weighted("powder_aluminium", 3),
                        weighted("powder_cobalt", 3))));
        entries.add(new Entry(new ItemStack(Items.LAPIS_LAZULI), new SilexRecipe(100, 100, LaserWavelength.IR,
                outputs(weighted("sulfur", 4),
                        weighted("powder_aluminium", 3),
                        weighted("powder_cobalt", 3)))));
        add(entries, "powder_impure_osmiridium", new SilexRecipe(1_000, 1_000, LaserWavelength.DRX,
                outputs(weighted("powder_impure_osmiridium", 1))));
        entries.add(new Entry(new ItemStack(Items.GRAVEL), new SilexRecipe(1_000, 250, LaserWavelength.VISIBLE,
                outputs(new WeightedOutput(new ItemStack(Items.FLINT), 80),
                        weighted("powder_boron", 5),
                        weighted("powder_lithium", 10),
                        weighted("fluorite", 5)))));
        return List.copyOf(entries);
    }

    private static Optional<SilexRecipe> recipeForFluidOutputs(int produced, int consumed, LaserWavelength wavelength,
            WeightedOutput... outputs) {
        List<WeightedOutput> resolved = outputs(outputs);
        return resolved.isEmpty() ? Optional.empty() : Optional.of(new SilexRecipe(produced, consumed, wavelength, resolved));
    }

    private static void add(List<Entry> entries, String legacyName, SilexRecipe recipe) {
        ItemStack stack = legacyStack(legacyName);
        if (!stack.isEmpty() && !recipe.outputs().isEmpty()) {
            entries.add(new Entry(stack, recipe));
        }
    }

    private static void addBlock(List<Entry> entries, String legacyName, SilexRecipe recipe) {
        ItemStack stack = legacyBlockStack(legacyName);
        if (!stack.isEmpty() && !recipe.outputs().isEmpty()) {
            entries.add(new Entry(stack, recipe));
        }
    }

    private static List<WeightedOutput> outputs(WeightedOutput... outputs) {
        List<WeightedOutput> resolved = new ArrayList<>();
        for (WeightedOutput output : outputs) {
            if (output != null && !output.stack().isEmpty() && output.weight() > 0) {
                resolved.add(output);
            }
        }
        return List.copyOf(resolved);
    }

    private static WeightedOutput weighted(String legacyName, int weight) {
        return weighted(legacyName, weight, 1);
    }

    private static WeightedOutput weighted(String legacyName, int weight, int count) {
        ItemStack stack = legacyStack(legacyName);
        if (!stack.isEmpty()) {
            stack.setCount(Math.max(1, count));
        }
        return new WeightedOutput(stack, weight);
    }

    private static ItemStack legacyStack(String legacyName) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
    }

    private static ItemStack legacyBlockStack(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        return block == null ? ItemStack.EMPTY : new ItemStack(block.get());
    }

    private record Entry(ItemStack input, SilexRecipe recipe) {
        private boolean matches(ItemStack stack) {
            return ItemStack.isSameItemSameTags(input, stack);
        }
    }

    public record SilexRecipe(int fluidProduced, int fluidConsumed, LaserWavelength laserStrength,
                              List<WeightedOutput> outputs) {
        public SilexRecipe {
            outputs = outputs == null ? List.of() : List.copyOf(outputs);
        }

        public int totalWeight() {
            int total = 0;
            for (WeightedOutput output : outputs) {
                total += output.weight();
            }
            return Math.max(total, 1);
        }

        public ItemStack selectOutput(int index) {
            int normalized = Math.floorMod(index, totalWeight());
            int weight = 0;
            for (WeightedOutput output : outputs) {
                weight += output.weight();
                if (normalized < weight) {
                    return output.stack().copy();
                }
            }
            return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).stack().copy();
        }
    }

    public record WeightedOutput(ItemStack stack, int weight) {
        public WeightedOutput {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
            weight = Math.max(0, weight);
        }
    }

    public record DisplayRecipe(ItemStack itemInput, HbmFluidStack fluidInput, SilexRecipe recipe,
                                boolean directFluidSource) {
        public DisplayRecipe {
            itemInput = itemInput == null ? ItemStack.EMPTY : itemInput.copy();
            fluidInput = fluidInput == null ? new HbmFluidStack(HbmFluids.NONE, 0, 0) : fluidInput;
        }

        private static DisplayRecipe itemSource(ItemStack input, SilexRecipe recipe) {
            return new DisplayRecipe(input,
                    new HbmFluidStack(HbmFluids.PEROXIDE, recipe.fluidProduced(), 0),
                    recipe, false);
        }

        private static DisplayRecipe fluidSource(FluidType fluid, SilexRecipe recipe) {
            return new DisplayRecipe(ItemStack.EMPTY,
                    new HbmFluidStack(fluid, recipe.fluidConsumed(), 0),
                    recipe, true);
        }
    }

    private SilexRecipeRuntime() {
    }
}
