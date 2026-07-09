package com.hbm.ntm.recipe;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class CyclotronRecipeRuntime {
    private static final Comparator<CyclotronRecipe> RECIPE_ORDER = Comparator
            .comparingInt(CyclotronRecipe::sourceOrder)
            .thenComparing(recipe -> recipe.getId().toString());
    private static final List<Entry> FALLBACK_ENTRIES = createFallbackEntries();

    public static Optional<CyclotronRecipe> find(@Nullable Level level, ItemStack particle, ItemStack input) {
        if (particle.isEmpty() || input.isEmpty()) {
            return Optional.empty();
        }
        List<CyclotronRecipe> recipes = recipes(level == null ? null : level.getRecipeManager());
        for (CyclotronRecipe recipe : recipes) {
            if (recipe.matches(particle, input)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    public static Optional<CyclotronRecipe> find(ItemStack particle, ItemStack input) {
        return find(null, particle, input);
    }

    public static boolean isValidParticle(@Nullable Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return recipes(level == null ? null : level.getRecipeManager()).stream()
                .anyMatch(recipe -> recipe.particle().test(stack, true));
    }

    public static boolean isValidParticle(ItemStack stack) {
        return isValidParticle(null, stack);
    }

    public static boolean isValidInput(@Nullable Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return recipes(level == null ? null : level.getRecipeManager()).stream()
                .anyMatch(recipe -> recipe.input().test(stack, true));
    }

    public static boolean isValidInput(ItemStack stack) {
        return isValidInput(null, stack);
    }

    public static List<DisplayRecipe> displayRecipes(@Nullable RecipeManager recipeManager) {
        return recipes(recipeManager).stream()
                .sorted(RECIPE_ORDER)
                .map(CyclotronRecipeRuntime::displayRecipe)
                .filter(recipe -> !recipe.particleInputs().isEmpty()
                        && !recipe.targetInputs().isEmpty()
                        && !recipe.output().isEmpty())
                .toList();
    }

    public static List<DisplayRecipe> displayRecipes() {
        return displayRecipes(null);
    }

    private static List<CyclotronRecipe> recipes(@Nullable RecipeManager recipeManager) {
        if (recipeManager != null) {
            return recipeManager.getAllRecipesFor(ModRecipes.CYCLOTRON.type().get()).stream()
                    .sorted(RECIPE_ORDER)
                    .toList();
        }
        return FALLBACK_ENTRIES.stream()
                .map(Entry::recipe)
                .toList();
    }

    private static DisplayRecipe displayRecipe(CyclotronRecipe recipe) {
        return new DisplayRecipe(recipe.particle().displayStacks(), recipe.input().displayStacks(),
                recipe.output(), recipe.antimatterMb());
    }

    private static List<Entry> createFallbackEntries() {
        List<Entry> entries = new ArrayList<>();

        add(entries, "lithium_to_beryllium", "part_lithium", HbmIngredient.legacyOre("dustLithium", 1),
                "powder_beryllium", 50);
        add(entries, "lithium_to_boron", "part_lithium", HbmIngredient.legacyOre("dustBeryllium", 1),
                "powder_boron", 50);
        add(entries, "lithium_to_coal", "part_lithium", HbmIngredient.legacyOre("dustBoron", 1),
                "powder_coal", 50);
        add(entries, "lithium_to_red_phosphorus", "part_lithium", HbmIngredient.legacyOre("dustNetherQuartz", 1),
                "powder_fire", 50);
        add(entries, "lithium_to_sulfur", "part_lithium", HbmIngredient.legacyOre("dustPhosphorus", 1),
                "sulfur", 50);
        add(entries, "lithium_to_cobalt", "part_lithium", HbmIngredient.legacyOre("dustIron", 1),
                "powder_cobalt", 50);
        add(entries, "lithium_to_zirconium", "part_lithium", ingredient("powder_strontium"),
                "powder_zirconium", 50);
        add(entries, "lithium_to_mercury", "part_lithium", HbmIngredient.legacyOre("dustGold", 1),
                "ingot_mercury", 50);
        add(entries, "lithium_to_astatine", "part_lithium", HbmIngredient.legacyOre("dustPolonium", 1),
                "powder_astatine", 50);
        add(entries, "lithium_to_cerium", "part_lithium", HbmIngredient.legacyOre("dustLanthanium", 1),
                "powder_cerium", 50);
        add(entries, "lithium_to_thorium", "part_lithium", HbmIngredient.legacyOre("dustActinium", 1),
                "powder_thorium", 50);
        add(entries, "lithium_to_neptunium", "part_lithium", HbmIngredient.legacyOre("dustUranium", 1),
                "powder_neptunium", 50);
        add(entries, "lithium_to_plutonium", "part_lithium", HbmIngredient.legacyOre("dustNp237", 1),
                "powder_plutonium", 50);

        add(entries, "beryllium_to_boron", "part_beryllium", HbmIngredient.legacyOre("dustLithium", 1),
                "powder_boron", 25);
        add(entries, "beryllium_to_sulfur", "part_beryllium", HbmIngredient.legacyOre("dustNetherQuartz", 1),
                "sulfur", 25);
        add(entries, "beryllium_to_iron", "part_beryllium", HbmIngredient.legacyOre("dustTitanium", 1),
                "powder_iron", 25);
        add(entries, "beryllium_to_copper", "part_beryllium", HbmIngredient.legacyOre("dustCobalt", 1),
                "powder_copper", 25);
        add(entries, "beryllium_to_niobium", "part_beryllium", ingredient("powder_strontium"),
                "powder_niobium", 25);
        add(entries, "beryllium_to_neodymium", "part_beryllium", ingredient("powder_cerium"),
                "powder_neodymium", 25);
        add(entries, "beryllium_to_uranium", "part_beryllium", HbmIngredient.legacyOre("dustThorium", 1),
                "powder_uranium", 25);

        add(entries, "carbon_to_aluminium", "part_carbon", HbmIngredient.legacyOre("dustBoron", 1),
                "powder_aluminium", 10);
        add(entries, "carbon_to_titanium", "part_carbon", HbmIngredient.legacyOre("dustSulfur", 1),
                "powder_titanium", 10);
        add(entries, "carbon_to_cobalt", "part_carbon", HbmIngredient.legacyOre("dustTitanium", 1),
                "powder_cobalt", 10);
        add(entries, "carbon_to_lanthanium", "part_carbon", ingredient("powder_caesium"),
                "powder_lanthanium", 10);
        add(entries, "carbon_to_gold", "part_carbon", ingredient("powder_neodymium"),
                "powder_gold", 10);
        add(entries, "carbon_to_polonium", "part_carbon", ingredient("ingot_mercury"),
                "powder_polonium", 10);
        add(entries, "carbon_to_ra226", "part_carbon", HbmIngredient.legacyOre("dustLead", 1),
                "powder_ra226", 10);
        add(entries, "carbon_to_actinium", "part_carbon", ingredient("powder_astatine"),
                "powder_actinium", 10);

        add(entries, "copper_to_quartz", "part_copper", HbmIngredient.legacyOre("dustBeryllium", 1),
                "powder_quartz", 15);
        add(entries, "copper_to_bromine", "part_copper", HbmIngredient.legacyOre("dustCoal", 1),
                "powder_bromine", 15);
        add(entries, "copper_to_strontium", "part_copper", HbmIngredient.legacyOre("dustTitanium", 1),
                "powder_strontium", 15);
        add(entries, "copper_to_niobium", "part_copper", HbmIngredient.legacyOre("dustIron", 1),
                "powder_niobium", 15);
        add(entries, "copper_to_iodine", "part_copper", ingredient("powder_bromine"),
                "powder_iodine", 15);
        add(entries, "copper_to_neodymium", "part_copper", ingredient("powder_strontium"),
                "powder_neodymium", 15);
        add(entries, "copper_to_caesium", "part_copper", ingredient("powder_niobium"),
                "powder_caesium", 15);
        add(entries, "copper_to_polonium", "part_copper", ingredient("powder_iodine"),
                "powder_polonium", 15);
        add(entries, "copper_to_actinium", "part_copper", ingredient("powder_caesium"),
                "powder_actinium", 15);
        add(entries, "copper_to_uranium", "part_copper", HbmIngredient.legacyOre("dustGold", 1),
                "powder_uranium", 15);

        add(entries, "plutonium_to_tennessine_from_phosphorus", "part_plutonium",
                HbmIngredient.legacyOre("dustPhosphorus", 1), "powder_tennessine", 100);
        add(entries, "plutonium_to_tennessine", "part_plutonium", HbmIngredient.legacyOre("dustPlutonium", 1),
                "powder_tennessine", 100);
        add(entries, "plutonium_to_australium", "part_plutonium", ingredient("powder_tennessine"),
                "powder_australium", 100);
        add(entries, "plutonium_to_schrabidium_nugget", "part_plutonium", ingredient("pellet_charged"),
                "nugget_schrabidium", 1000);

        return List.copyOf(entries);
    }

    private static void add(List<Entry> entries, String name, String particleName, HbmIngredient input,
            String outputName, int antimatter) {
        ResourceLocation id = new ResourceLocation(HbmNtm.MOD_ID, "cyclotron/" + name);
        CyclotronRecipe recipe = new CyclotronRecipe(id, ingredient(particleName), input, stack(outputName),
                antimatter, entries.size());
        entries.add(new Entry(recipe));
    }

    private static HbmIngredient ingredient(String legacyName) {
        return HbmIngredient.of(item(legacyName), 1);
    }

    private static ItemLike item(String legacyName) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        if (item == null) {
            throw new IllegalStateException("Missing Cyclotron fallback item: " + legacyName);
        }
        return item.get();
    }

    private static ItemStack stack(String legacyName) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        if (item == null) {
            throw new IllegalStateException("Missing Cyclotron fallback output: " + legacyName);
        }
        return new ItemStack(item.get());
    }

    private record Entry(CyclotronRecipe recipe) {
    }

    public record DisplayRecipe(List<ItemStack> particleInputs, List<ItemStack> targetInputs,
            ItemStack output, int antimatterMb) {
        public DisplayRecipe {
            particleInputs = particleInputs == null ? List.of() : particleInputs.stream()
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
            targetInputs = targetInputs == null ? List.of() : targetInputs.stream()
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
            output = output == null ? ItemStack.EMPTY : output.copy();
            antimatterMb = Math.max(0, antimatterMb);
        }
    }

    private CyclotronRecipeRuntime() {
    }
}
