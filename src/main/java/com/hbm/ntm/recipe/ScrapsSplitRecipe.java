package com.hbm.ntm.recipe;

import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.item.FoundryScrapsItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ScrapsSplitRecipe extends CustomRecipe {
    public ScrapsSplitRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !assemble(container, RegistryAccess.EMPTY).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack input = singleStack(container);
        if (input.isEmpty() || !(input.getItem() instanceof FoundryScrapsItem)) {
            return ItemStack.EMPTY;
        }

        MaterialStack material = FoundryScrapsItem.getMaterial(input);
        if (material == null || material.amount < 2) {
            return ItemStack.EMPTY;
        }

        ItemStack output = FoundryScrapsItem.create(new MaterialStack(material.material, material.amount / 2));
        if (output.isEmpty()) {
            return ItemStack.EMPTY;
        }
        output.setCount(2);
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SCRAPS_SPLIT.get();
    }

    private static ItemStack singleStack(CraftingContainer container) {
        ItemStack found = ItemStack.EMPTY;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!found.isEmpty()) {
                return ItemStack.EMPTY;
            }
            found = stack;
        }
        return found;
    }
}
