package com.hbm.ntm.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;

/**
 * JEI 15.20-compatible category background contract.
 *
 * <p>JEI no longer draws {@code IRecipeCategory#getBackground()} automatically.
 * This adapter retains the legacy category layouts while using the replacement
 * width, height, and draw hooks.</p>
 */
interface HbmJeiRecipeCategory<T> extends IRecipeCategory<T> {
    IDrawable getRecipeBackground();

    @Override
    default int getWidth() {
        return getRecipeBackground().getWidth();
    }

    @Override
    default int getHeight() {
        return getRecipeBackground().getHeight();
    }

    @Override
    default void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                      double mouseX, double mouseY) {
        drawBackground(guiGraphics);
    }

    default void drawBackground(GuiGraphics guiGraphics) {
        getRecipeBackground().draw(guiGraphics);
    }
}
