package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.FusionFluidBreederRecipe;
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

public final class FusionFluidBreederRecipeCategory implements IRecipeCategory<FusionFluidBreederRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 54;

    private final RecipeType<FusionFluidBreederRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    FusionFluidBreederRecipeCategory(RecipeType<FusionFluidBreederRecipe> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<FusionFluidBreederRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("container.fusionBreeder", "Fusion Reactor Breeder");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FusionFluidBreederRecipe recipe, IFocusGroup focuses) {
        JeiFluidSlots.addFluidSlot(builder, recipe.input(), true, 4, 18);
        JeiFluidSlots.addFluidSlot(builder, recipe.output(), false, 130, 18);
    }

    @Override
    public void draw(FusionFluidBreederRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 18);
    }
}
