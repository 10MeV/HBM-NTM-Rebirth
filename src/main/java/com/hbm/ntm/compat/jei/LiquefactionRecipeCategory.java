package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.LiquefactionRecipe;
import java.util.List;
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

public final class LiquefactionRecipeCategory implements IRecipeCategory<LiquefactionRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 54;

    private final RecipeType<LiquefactionRecipe> type;
    private final IDrawableStatic background;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    LiquefactionRecipeCategory(RecipeType<LiquefactionRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<LiquefactionRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_liquefactor", "Liquefactor");
    }

    @Override
    public IDrawable getBackground() {
        return background;
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
    public void setRecipe(IRecipeLayoutBuilder builder, LiquefactionRecipe recipe, IFocusGroup focuses) {
        if (!recipe.getIngredients().isEmpty()) {
            builder.addInputSlot(4, 18)
                    .addItemStacks(List.of(recipe.getIngredients().get(0).getItems()))
                    .setStandardSlotBackground();
        }
        JeiFluidSlots.addFluidSlot(builder, recipe.getOutputFluid(), false, 130, 18);
    }

    @Override
    public void draw(LiquefactionRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 18);
    }
}
