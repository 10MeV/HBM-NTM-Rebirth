package com.hbm.ntm.recipe;

import com.hbm.ntm.util.AchievementHandler;
import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Runtime helpers for legacy anvil construction recipes.
 */
public final class AnvilConstructionRecipeRuntime {
    private AnvilConstructionRecipeRuntime() {
    }

    public static List<AnvilConstructionRecipe> recipes(Level level) {
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.ANVIL_CONSTRUCTION.type().get()).stream()
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    public static List<AnvilConstructionRecipe> recipesForTier(Level level, int tier) {
        return recipes(level).stream()
                .filter(recipe -> recipe.isTierValid(tier))
                .toList();
    }

    public static Optional<AnvilConstructionRecipe> recipeById(Level level, ResourceLocation id) {
        if (id == null) {
            return Optional.empty();
        }
        return recipes(level).stream()
                .filter(recipe -> recipe.getId().equals(id))
                .findFirst();
    }

    public static Optional<AnvilConstructionRecipe> recipeByIndex(Level level, int tier, int index) {
        List<AnvilConstructionRecipe> available = recipesForTier(level, tier);
        if (index < 0 || index >= available.size()) {
            return Optional.empty();
        }
        return Optional.of(available.get(index));
    }

    public static int recipeIndex(Level level, int tier, AnvilConstructionRecipe recipe) {
        if (recipe == null) {
            return -1;
        }
        List<AnvilConstructionRecipe> available = recipesForTier(level, tier);
        for (int index = 0; index < available.size(); index++) {
            if (available.get(index).getId().equals(recipe.getId())) {
                return index;
            }
        }
        return -1;
    }

    public static List<AnvilConstructionRecipe> search(Level level, int tier, @Nullable String query) {
        String needle = normalize(query);
        if (needle.isEmpty()) {
            return recipesForTier(level, tier);
        }
        return recipesForTier(level, tier).stream()
                .filter(recipe -> matchesSearch(recipe, needle))
                .toList();
    }

    public static boolean canCraft(Player player, AnvilConstructionRecipe recipe, int tier) {
        return player != null
                && recipe != null
                && recipe.isTierValid(tier)
                && HbmInventoryUtil.doesPlayerHaveAStacks(player, recipe.inputs(), false);
    }

    public static CraftResult craft(Player player, AnvilConstructionRecipe recipe, int tier, boolean batch) {
        if (player == null || recipe == null || !recipe.isTierValid(tier)) {
            return CraftResult.failed();
        }

        int attempts = batchIterations(recipe, batch);
        if (attempts <= 0) {
            return CraftResult.failed();
        }

        int crafted = 0;
        List<ItemStack> granted = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            if (!HbmInventoryUtil.doesPlayerHaveAStacks(player, recipe.inputs(), true)) {
                break;
            }
            crafted++;
            for (HbmItemOutput output : recipe.outputs()) {
                ItemStack stack = output.collapse(player.getRandom());
                if (!stack.isEmpty()) {
                    HbmItemStackUtil.giveOrDrop(player, stack);
                    granted.add(stack.copy());
                }
            }
            AchievementHandler.fire(player, recipe.outputs().get(0).representativeStack());
        }
        return crafted <= 0 ? CraftResult.failed() : CraftResult.success(crafted, granted);
    }

    public static int batchIterations(AnvilConstructionRecipe recipe, boolean batch) {
        if (recipe == null) {
            return 0;
        }
        if (!batch) {
            return 1;
        }
        if (recipe.outputs().size() > 1) {
            return 64;
        }
        ItemStack output = recipe.outputs().get(0).representativeStack();
        if (output.isEmpty()) {
            return 0;
        }
        int count = Math.max(1, output.getCount());
        return output.getMaxStackSize() / count;
    }

    public static boolean matchesSearch(AnvilConstructionRecipe recipe, String normalizedQuery) {
        if (recipe == null) {
            return false;
        }
        String needle = normalize(normalizedQuery);
        if (needle.isEmpty() || recipe.getId().toString().toLowerCase(Locale.ROOT).contains(needle)) {
            return true;
        }
        for (HbmIngredient input : recipe.inputs()) {
            if (input.diagnosticName().toLowerCase(Locale.ROOT).contains(needle)
                    || input.displayStacks().stream().anyMatch(stack -> stackNameContains(stack, needle))) {
                return true;
            }
        }
        for (HbmItemOutput output : recipe.outputs()) {
            if (output.displayStacks().stream().anyMatch(stack -> stackNameContains(stack, needle))) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(@Nullable String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean stackNameContains(ItemStack stack, String needle) {
        return !stack.isEmpty() && stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle);
    }

    public record CraftResult(boolean craftedAny, int iterations, List<ItemStack> granted) {
        public CraftResult {
            iterations = Math.max(0, iterations);
            granted = granted == null ? List.of() : List.copyOf(granted);
        }

        public static CraftResult failed() {
            return new CraftResult(false, 0, List.of());
        }

        public static CraftResult success(int iterations, List<ItemStack> granted) {
            return new CraftResult(iterations > 0, iterations, granted);
        }
    }
}
