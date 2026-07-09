package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ItemProcessingRecipeRuntime {
    private static final Comparator<ItemProcessingRecipe> ORDER =
            Comparator.comparingInt(ItemProcessingRecipe::sourceOrder)
                    .thenComparing(recipe -> recipe.getId().toString());

    public static List<ItemProcessingRecipe> recipes(Level level, ItemProcessingRecipe.Machine machine) {
        return level.getRecipeManager().getAllRecipesFor(machine.type()).stream()
                .filter(recipe -> recipe.machine() == machine)
                .sorted(ORDER)
                .toList();
    }

    @Nullable
    public static ItemProcessingRecipe find(Level level, ItemProcessingRecipe.Machine machine, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return recipes(level, machine).stream()
                .filter(recipe -> recipe.matches(stack))
                .sorted(matchOrder(stack))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static ItemProcessingRecipe find(Level level, ItemProcessingRecipe.Machine machine, ItemStack stack,
            FluidType fluidType) {
        if (stack.isEmpty()) {
            return null;
        }
        return recipes(level, machine).stream()
                .filter(recipe -> recipe.matches(stack, fluidType))
                .sorted(matchOrder(stack))
                .findFirst()
                .orElse(null);
    }

    public static int inputAmount(Level level, ItemProcessingRecipe.Machine machine, ItemStack stack) {
        ItemProcessingRecipe recipe = find(level, machine, stack);
        return recipe == null ? 0 : Math.max(1, recipe.input().count());
    }

    private static Comparator<ItemProcessingRecipe> matchOrder(ItemStack stack) {
        return Comparator.comparingInt((ItemProcessingRecipe recipe) -> matchPriority(recipe, stack))
                .thenComparing(ORDER);
    }

    private static int matchPriority(ItemProcessingRecipe recipe, ItemStack stack) {
        HbmIngredient input = recipe.input();
        if (input.legacyWildcard()) {
            return 2;
        }
        if (input.legacyOreName() != null || input.isTagIngredient()) {
            return 1;
        }
        return 0;
    }

    private ItemProcessingRecipeRuntime() {
    }
}
