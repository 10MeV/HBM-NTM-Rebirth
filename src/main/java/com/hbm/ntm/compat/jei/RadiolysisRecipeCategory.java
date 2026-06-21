package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.RadiolysisRecipes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public final class RadiolysisRecipeCategory implements IRecipeCategory<RadiolysisRecipes.DisplayRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 72;

    private final RecipeType<RadiolysisRecipes.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    RadiolysisRecipeCategory(RecipeType<RadiolysisRecipes.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<RadiolysisRecipes.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_radiolysis", "Radiolysis Chamber");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RadiolysisRecipes.DisplayRecipe recipe,
            IFocusGroup focuses) {
        JeiFluidSlots.addFluidSlot(builder, recipe.input(), true, 8, 26);
        JeiFluidSlots.addFluidSlot(builder, recipe.left(), false, 118, 16);
        JeiFluidSlots.addFluidSlot(builder, recipe.right(), false, 140, 36);
    }

    @Override
    public void draw(RadiolysisRecipes.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 76, 28);
    }
}