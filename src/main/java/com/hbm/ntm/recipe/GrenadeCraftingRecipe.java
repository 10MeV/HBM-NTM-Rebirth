package com.hbm.ntm.recipe;

import com.hbm.ntm.item.UniversalGrenadeItem;
import com.hbm.ntm.item.UniversalGrenadeItem.Extra;
import com.hbm.ntm.item.UniversalGrenadeItem.Filling;
import com.hbm.ntm.item.UniversalGrenadeItem.Fuze;
import com.hbm.ntm.item.UniversalGrenadeItem.Shell;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class GrenadeCraftingRecipe extends CustomRecipe {
    public GrenadeCraftingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !assemble(container, level.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        Shell shell = null;
        Filling filling = null;
        Fuze fuze = null;
        Extra extra = null;
        int itemCount = 0;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            itemCount++;
            if (itemCount > 4) {
                return ItemStack.EMPTY;
            }

            Item item = stack.getItem();
            Shell shellPart = UniversalGrenadeItem.shellFromItem(item);
            if (shellPart != null) {
                if (shell != null) {
                    return ItemStack.EMPTY;
                }
                shell = shellPart;
                continue;
            }

            Filling fillingPart = UniversalGrenadeItem.fillingFromItem(item);
            if (fillingPart != null) {
                if (filling != null) {
                    return ItemStack.EMPTY;
                }
                filling = fillingPart;
                continue;
            }

            Fuze fuzePart = UniversalGrenadeItem.fuzeFromItem(item);
            if (fuzePart != null) {
                if (fuze != null) {
                    return ItemStack.EMPTY;
                }
                fuze = fuzePart;
                continue;
            }

            Extra extraPart = UniversalGrenadeItem.extraFromItem(item);
            if (extraPart != null) {
                if (extra != null) {
                    return ItemStack.EMPTY;
                }
                extra = extraPart;
                continue;
            }

            return ItemStack.EMPTY;
        }

        if (shell == null || filling == null || fuze == null || !filling.compatible(shell)) {
            return ItemStack.EMPTY;
        }
        return UniversalGrenadeItem.make(shell, filling, fuze, extra);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.GRENADE_CRAFTING.get();
    }
}
