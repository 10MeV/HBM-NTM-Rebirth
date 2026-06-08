package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ItemProcessingRecipeRuntime {
    private static final Comparator<ItemProcessingRecipe> ORDER =
            Comparator.comparing(recipe -> recipe.getId().toString());

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
                .findFirst()
                .orElse(null);
    }

    private ItemProcessingRecipeRuntime() {
    }
}
