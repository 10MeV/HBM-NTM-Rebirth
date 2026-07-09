package com.hbm.ntm.recipe;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class CargoShellCraftingRecipe extends CustomRecipe {
    private static final String TAG_CARGO = "cargo";

    public CargoShellCraftingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !assemble(container, RegistryAccess.EMPTY).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack shell = ItemStack.EMPTY;
        ItemStack cargo = ItemStack.EMPTY;
        int itemCount = 0;
        int shellCount = 0;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.hasCraftingRemainingItem()) {
                return ItemStack.EMPTY;
            }

            itemCount++;
            if (isEmptyCargoShell(stack)) {
                shellCount++;
                if (shellCount > 1) {
                    return ItemStack.EMPTY;
                }
                shell = stack.copyWithCount(1);
            } else {
                if (!cargo.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                cargo = stack.copyWithCount(1);
            }
        }

        if (itemCount != 2 || shellCount != 1 || shell.isEmpty() || cargo.isEmpty()) {
            return ItemStack.EMPTY;
        }

        shell.getOrCreateTag().put(TAG_CARGO, cargo.save(new CompoundTag()));
        return shell;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CARGO_SHELL_CRAFTING.get();
    }

    private static boolean isEmptyCargoShell(ItemStack stack) {
        return stack.is(ModItems.AMMO_ARTY_CARGO.get()) && !stack.hasTag();
    }
}
