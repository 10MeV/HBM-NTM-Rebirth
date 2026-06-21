package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.ExposureChamberRecipe;
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

public final class ExposureChamberRecipeCategory implements IRecipeCategory<ExposureChamberRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 72;

    private final RecipeType<ExposureChamberRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    ExposureChamberRecipeCategory(RecipeType<ExposureChamberRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<ExposureChamberRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_exposure_chamber",
                "Exposure Chamber");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ExposureChamberRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(18, 26)
                .addItemStacks(recipe.particle().displayStacks())
                .setStandardSlotBackground();
        builder.addInputSlot(52, 26)
                .addItemStacks(recipe.ingredient().displayStacks())
                .setStandardSlotBackground();
        builder.addOutputSlot(128, 26)
                .addItemStack(recipe.output())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(ExposureChamberRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 28);
    }
}
