package com.hbm.ntm.compat.jei;

import com.hbm.ntm.client.screen.AnvilScreen;
import com.hbm.ntm.menu.AnvilMenu;
import com.hbm.ntm.recipe.AnvilConstructionRecipe;
import com.hbm.ntm.registry.ModMenuTypes;
import java.util.Optional;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

final class AnvilConstructionRecipeTransferHandler
        implements IRecipeTransferHandler<AnvilMenu, AnvilConstructionRecipe> {
    @Override
    public Class<? extends AnvilMenu> getContainerClass() {
        return AnvilMenu.class;
    }

    @Override
    public Optional<MenuType<AnvilMenu>> getMenuType() {
        return Optional.of(ModMenuTypes.ANVIL.get());
    }

    @Override
    public RecipeType<AnvilConstructionRecipe> getRecipeType() {
        return HbmJeiPlugin.ANVIL_CONSTRUCTION;
    }

    @Override
    public IRecipeTransferError transferRecipe(AnvilMenu container, AnvilConstructionRecipe recipe,
            IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer && Minecraft.getInstance().screen instanceof AnvilScreen screen
                && screen.getMenu() == container) {
            screen.focusRecipe(recipe);
        }
        return null;
    }
}
