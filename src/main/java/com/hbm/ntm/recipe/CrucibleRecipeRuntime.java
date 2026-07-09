package com.hbm.ntm.recipe;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class CrucibleRecipeRuntime {
    public static final String NULL_RECIPE = GenericMachineRecipeRuntime.NULL_RECIPE;

    private static final int NUGGET = MaterialShapes.NUGGET.q(1);
    private static final int INGOT = MaterialShapes.INGOT.q(1);
    private static final List<Recipe> RECIPES = registerDefaults();
    private static final Map<String, Recipe> BY_NAME = indexByName(RECIPES);

    private CrucibleRecipeRuntime() {
    }

    public static List<Recipe> recipes() {
        return RECIPES;
    }

    public static List<Recipe> recipes(@Nullable Level level) {
        return level == null ? recipes() : recipes(level.getRecipeManager());
    }

    public static List<Recipe> recipes(@Nullable RecipeManager recipeManager) {
        if (recipeManager == null) {
            return recipes();
        }
        List<Recipe> recipes = recipeManager.getAllRecipesFor(ModRecipes.CRUCIBLE.type().get()).stream()
                .sorted(Comparator.comparingInt(CrucibleRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .map(CrucibleRecipe::runtimeRecipe)
                .toList();
        return recipes.isEmpty() ? RECIPES : recipes;
    }

    @Nullable
    public static Recipe find(String internalName) {
        String normalized = normalize(internalName);
        return NULL_RECIPE.equals(normalized) ? null : BY_NAME.get(normalized);
    }

    @Nullable
    public static Recipe find(@Nullable Level level, String internalName) {
        return level == null ? find(internalName) : find(level.getRecipeManager(), internalName);
    }

    @Nullable
    public static Recipe find(@Nullable RecipeManager recipeManager, String internalName) {
        String normalized = normalize(internalName);
        if (NULL_RECIPE.equals(normalized)) {
            return null;
        }
        for (Recipe recipe : recipes(recipeManager)) {
            if (recipe.internalName().equals(normalized)) {
                return recipe;
            }
        }
        return null;
    }

    public static String normalize(@Nullable String recipe) {
        return recipe == null || recipe.isBlank() ? NULL_RECIPE : recipe;
    }

    public static boolean isNullSelection(@Nullable String recipe) {
        return NULL_RECIPE.equals(normalize(recipe));
    }

    public static boolean canSelect(@Nullable String recipe) {
        return isNullSelection(recipe) || find(recipe) != null;
    }

    public static boolean canSelect(@Nullable Level level, @Nullable String recipe) {
        return isNullSelection(recipe) || find(level, recipe) != null;
    }

    public static boolean matchesSearch(Recipe recipe, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String needle = query.toLowerCase(Locale.ROOT);
        if (recipe.internalName().toLowerCase(Locale.ROOT).contains(needle)) {
            return true;
        }
        if (recipe.fallbackName().toLowerCase(Locale.ROOT).contains(needle)) {
            return true;
        }
        for (MaterialStack stack : recipe.input()) {
            if (materialMatches(stack, needle)) {
                return true;
            }
        }
        for (MaterialStack stack : recipe.output()) {
            if (materialMatches(stack, needle)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRecipeMaterial(Recipe recipe, NTMMaterial material) {
        if (recipe == null || material == null) {
            return false;
        }
        return containsMaterial(recipe.input(), material) || containsMaterial(recipe.output(), material);
    }

    public static boolean isRecipeOutput(Recipe recipe, NTMMaterial material) {
        return recipe != null && material != null && containsMaterial(recipe.output(), material);
    }

    public static int recipeInputAmount(Recipe recipe, NTMMaterial material) {
        return amountOf(recipe.input(), material);
    }

    public static int recipeInputAmount(Recipe recipe) {
        int total = 0;
        for (MaterialStack stack : recipe.input()) {
            total += stack.amount;
        }
        return total;
    }

    public static boolean canFitRecipeMaterial(Recipe recipe, List<MaterialStack> recipeStack,
            MaterialStack incoming, int capacity) {
        if (recipe == null || incoming == null || incoming.isEmpty()) {
            return false;
        }
        int recipeAmount = totalAmount(recipeStack);
        if (recipeAmount + incoming.amount > capacity) {
            return false;
        }
        if (isRecipeOutput(recipe, incoming.material)) {
            return true;
        }
        int recipeInputRequired = recipeInputAmount(recipe, incoming.material);
        if (recipeInputRequired <= 0) {
            return false;
        }
        int recipeContent = recipeInputAmount(recipe);
        int materialMaximum = recipeInputRequired * capacity / Math.max(1, recipeContent);
        return amountOf(recipeStack, incoming.material) + incoming.amount <= materialMaximum;
    }

    public static boolean canProcess(Recipe recipe, List<MaterialStack> recipeStack, int capacity) {
        if (recipe == null) {
            return false;
        }
        for (MaterialStack input : recipe.input()) {
            if (amountOf(recipeStack, input.material) < input.amount) {
                return false;
            }
        }
        return true;
    }

    public static boolean process(Recipe recipe, List<MaterialStack> recipeStack, int capacity) {
        if (!canProcess(recipe, recipeStack, capacity)) {
            return false;
        }
        for (MaterialStack input : recipe.input()) {
            remove(recipeStack, input.material, input.amount);
        }
        for (MaterialStack output : recipe.output()) {
            add(recipeStack, output.copy());
        }
        cleanup(recipeStack);
        return true;
    }

    public static List<Component> displayLines(Recipe recipe) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatableWithFallback(recipe.translationKey(), recipe.fallbackName())
                .withStyle(ChatFormatting.YELLOW));
        lines.add(Component.translatableWithFallback("gui.recipe.input", "Input:")
                .withStyle(ChatFormatting.BOLD));
        for (MaterialStack stack : recipe.input()) {
            lines.add(materialLine(stack));
        }
        lines.add(Component.translatableWithFallback("gui.recipe.output", "Output:")
                .withStyle(ChatFormatting.BOLD));
        for (MaterialStack stack : recipe.output()) {
            lines.add(materialLine(stack));
        }
        return lines;
    }

    private static Component materialLine(MaterialStack stack) {
        return Component.translatable(stack.material.getUnlocalizedName())
                .append(": " + Mats.formatAmount(stack.amount, false));
    }

    private static boolean materialMatches(MaterialStack stack, String needle) {
        if (stack == null || stack.material == null) {
            return false;
        }
        for (String name : stack.material.names) {
            if (name.toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsMaterial(List<MaterialStack> stacks, NTMMaterial material) {
        for (MaterialStack stack : stacks) {
            if (stack.material == material) {
                return true;
            }
        }
        return false;
    }

    private static int amountOf(List<MaterialStack> stacks, NTMMaterial material) {
        int amount = 0;
        for (MaterialStack stack : stacks) {
            if (stack.material == material) {
                amount += stack.amount;
            }
        }
        return amount;
    }

    private static int totalAmount(List<MaterialStack> stacks) {
        int total = 0;
        if (stacks != null) {
            for (MaterialStack stack : stacks) {
                if (stack != null && !stack.isEmpty()) {
                    total += stack.amount;
                }
            }
        }
        return total;
    }

    private static void remove(List<MaterialStack> stacks, NTMMaterial material, int amount) {
        int remaining = amount;
        for (MaterialStack stack : stacks) {
            if (stack.material != material || remaining <= 0) {
                continue;
            }
            int moved = Math.min(stack.amount, remaining);
            stack.amount -= moved;
            remaining -= moved;
        }
    }

    private static void add(List<MaterialStack> stacks, MaterialStack material) {
        if (material == null || material.isEmpty()) {
            return;
        }
        for (MaterialStack stack : stacks) {
            if (stack.material == material.material) {
                stack.amount += material.amount;
                return;
            }
        }
        stacks.add(material.copy());
    }

    private static void cleanup(List<MaterialStack> stacks) {
        stacks.removeIf(stack -> stack == null || stack.isEmpty());
        stacks.sort(Comparator.comparingInt(stack -> stack.material.id));
    }

    private static Map<String, Recipe> indexByName(List<Recipe> recipes) {
        Map<String, Recipe> map = new LinkedHashMap<>();
        for (Recipe recipe : recipes) {
            map.put(recipe.internalName(), recipe);
        }
        return Map.copyOf(map);
    }

    private static List<Recipe> registerDefaults() {
        List<Recipe> recipes = new ArrayList<>();
        add(recipes, "crucible.steel", "Steel Production", ModItems.STEEL_INGOT,
                in(ms(Mats.MAT_IRON, NUGGET * 2), ms(Mats.MAT_CARBON, NUGGET * 3), ms(Mats.MAT_FLUX, NUGGET)),
                out(ms(Mats.MAT_STEEL, NUGGET * 2)), 20, 0);
        addBlock(recipes, "crucible.hematite", "Iron Production from Hematite", "stone_resource_hematite",
                in(ms(Mats.MAT_HEMATITE, INGOT * 2), ms(Mats.MAT_FLUX, NUGGET * 2)),
                out(ms(Mats.MAT_IRON, INGOT), ms(Mats.MAT_SLAG, NUGGET * 3)), 6, 1);
        addBlock(recipes, "crucible.malachite", "Copper Production from Malachite", "stone_resource_malachite",
                in(ms(Mats.MAT_MALACHITE, INGOT * 2), ms(Mats.MAT_FLUX, NUGGET * 2)),
                out(ms(Mats.MAT_COPPER, INGOT), ms(Mats.MAT_SLAG, NUGGET * 3)), 6, 2);
        add(recipes, "crucible.redcopper", "Red Copper Production", "ingot_red_copper",
                in(ms(Mats.MAT_COPPER, NUGGET), ms(Mats.MAT_REDSTONE, NUGGET)),
                out(ms(Mats.MAT_MINGRADE, NUGGET * 2)), 2, 3);
        add(recipes, "crucible.hss", "High-Speed Steel Production", "ingot_dura_steel",
                in(ms(Mats.MAT_STEEL, NUGGET * 5), ms(Mats.MAT_TUNGSTEN, NUGGET * 3), ms(Mats.MAT_COBALT, NUGGET)),
                out(ms(Mats.MAT_DURA, NUGGET * 9)), 9, 4);
        add(recipes, "crucible.ferro", "Ferrouranium Production", "ingot_ferrouranium",
                in(ms(Mats.MAT_STEEL, NUGGET * 2), ms(Mats.MAT_U238, NUGGET)),
                out(ms(Mats.MAT_FERRO, NUGGET * 3)), 3, 5);
        add(recipes, "crucible.tcalloy", "Technetium Steel Production", "ingot_tcalloy",
                in(ms(Mats.MAT_STEEL, NUGGET * 8), ms(Mats.MAT_TECHNETIUM, NUGGET)),
                out(ms(Mats.MAT_TCALLOY, INGOT)), 9, 6);
        add(recipes, "crucible.cdalloy", "Cadmium Steel Production", "ingot_cdalloy",
                in(ms(Mats.MAT_STEEL, NUGGET * 8), ms(Mats.MAT_CADMIUM, NUGGET)),
                out(ms(Mats.MAT_CDALLOY, INGOT)), 9, 7);
        add(recipes, "crucible.bbronze", "Bismuth Bronze Production", "ingot_bismuth_bronze",
                in(ms(Mats.MAT_COPPER, NUGGET * 8), ms(Mats.MAT_BISMUTH, NUGGET), ms(Mats.MAT_FLUX, NUGGET * 3)),
                out(ms(Mats.MAT_BBRONZE, INGOT), ms(Mats.MAT_SLAG, NUGGET * 3)), 9, 8);
        add(recipes, "crucible.abronze", "Arsenic Bronze Production", "ingot_arsenic_bronze",
                in(ms(Mats.MAT_COPPER, NUGGET * 8), ms(Mats.MAT_ARSENIC, NUGGET), ms(Mats.MAT_FLUX, NUGGET * 3)),
                out(ms(Mats.MAT_ABRONZE, INGOT), ms(Mats.MAT_SLAG, NUGGET * 3)), 9, 9);
        add(recipes, "crucible.cmb", "CMB Steel Production", "ingot_combine_steel",
                in(ms(Mats.MAT_MAGTUNG, NUGGET * 6), ms(Mats.MAT_MUD, NUGGET * 3)),
                out(ms(Mats.MAT_CMB, INGOT)), 3, 10);
        add(recipes, "crucible.magtung", "Magnetized Tungsten Production", "ingot_magnetized_tungsten",
                in(ms(Mats.MAT_TUNGSTEN, INGOT), ms(Mats.MAT_SCHRABIDIUM, NUGGET)),
                out(ms(Mats.MAT_MAGTUNG, INGOT)), 3, 11);
        add(recipes, "crucible.bscco", "BSCCO Production", "ingot_bscco",
                in(ms(Mats.MAT_BISMUTH, NUGGET * 2), ms(Mats.MAT_STRONTIUM, NUGGET * 2),
                        ms(Mats.MAT_CALCIUM, NUGGET * 2), ms(Mats.MAT_COPPER, NUGGET * 3)),
                out(ms(Mats.MAT_BSCCO, INGOT)), 3, 12);
        return List.copyOf(recipes);
    }

    private static void add(List<Recipe> recipes, String name, String fallbackName, Supplier<? extends ItemLike> icon,
            List<MaterialStack> input, List<MaterialStack> output, int frequency, int sourceOrder) {
        recipes.add(new Recipe(name, fallbackName, () -> {
            ItemLike itemLike = icon.get();
            return new ItemStack(itemLike);
        }, List.copyOf(input), List.copyOf(output),
                Math.max(1, frequency), sourceOrder));
    }

    private static void add(List<Recipe> recipes, String name, String fallbackName, String legacyIcon,
            List<MaterialStack> input, List<MaterialStack> output, int frequency, int sourceOrder) {
        RegistryObject<? extends net.minecraft.world.item.Item> icon = ModItems.legacyItem(legacyIcon);
        if (icon != null) {
            add(recipes, name, fallbackName, icon, input, output, frequency, sourceOrder);
        }
    }

    private static void addBlock(List<Recipe> recipes, String name, String fallbackName, String legacyBlock,
            List<MaterialStack> input, List<MaterialStack> output, int frequency, int sourceOrder) {
        RegistryObject<? extends net.minecraft.world.level.block.Block> icon = ModBlocks.legacyBlock(legacyBlock);
        if (icon != null) {
            add(recipes, name, fallbackName, icon, input, output, frequency, sourceOrder);
        }
    }

    private static List<MaterialStack> in(MaterialStack... stacks) {
        return List.of(stacks);
    }

    private static List<MaterialStack> out(MaterialStack... stacks) {
        return List.of(stacks);
    }

    private static MaterialStack ms(NTMMaterial material, int amount) {
        return new MaterialStack(material, amount);
    }

    public record Recipe(String internalName, String fallbackName, Supplier<ItemStack> iconFactory, List<MaterialStack> input,
                         List<MaterialStack> output, int frequency, int sourceOrder) {
        public Recipe {
            iconFactory = iconFactory == null ? () -> ItemStack.EMPTY : iconFactory;
            input = List.copyOf(input);
            output = List.copyOf(output);
            frequency = Math.max(1, frequency);
        }

        public ItemStack icon() {
            return iconFactory.get().copy();
        }

        public String translationKey() {
            return internalName;
        }
    }
}
