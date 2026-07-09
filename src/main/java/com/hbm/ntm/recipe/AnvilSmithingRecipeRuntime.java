package com.hbm.ntm.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Runtime helpers for legacy two-slot anvil smithing recipes.
 */
public final class AnvilSmithingRecipeRuntime {
    private AnvilSmithingRecipeRuntime() {
    }

    public static List<AnvilSmithingRecipe> recipes(Level level) {
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.ANVIL_SMITHING.type().get()).stream()
                .sorted(Comparator
                        .comparingInt(AnvilSmithingRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }

    public static Optional<MatchedRecipe> match(Level level, Container input, int tier) {
        if (input == null || input.getContainerSize() < 2) {
            return Optional.empty();
        }
        ItemStack left = input.getItem(0);
        ItemStack right = input.getItem(1);
        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }
        return recipes(level).stream()
                .map(recipe -> new MatchedRecipe(recipe, recipe.match(left, right, tier)))
                .filter(matched -> matched.match().matches())
                .findFirst();
    }

    public static ItemStack result(Level level, Container input, int tier) {
        return match(level, input, tier)
                .map(matched -> matched.recipe().result(input.getItem(0), input.getItem(1)))
                .orElse(ItemStack.EMPTY);
    }

    public static boolean consume(Level level, Container input, int tier) {
        Optional<MatchedRecipe> matched = match(level, input, tier);
        if (matched.isEmpty()) {
            return false;
        }
        AnvilSmithingRecipe recipe = matched.get().recipe();
        AnvilSmithingRecipe.Match match = matched.get().match();
        input.removeItem(0, match.consumeLeft(recipe));
        input.removeItem(1, match.consumeRight(recipe));
        input.setChanged();
        return true;
    }

    public record MatchedRecipe(AnvilSmithingRecipe recipe, AnvilSmithingRecipe.Match match) {
    }
}
