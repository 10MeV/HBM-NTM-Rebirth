package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluidCompressorRecipes;
import com.hbm.ntm.fluid.HbmFluidStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public final class CompressorRecipeCategory implements IRecipeCategory<HbmFluidCompressorRecipes.RecipeEntry> {
    private static final int WIDTH = 138;
    private static final int HEIGHT = 64;

    private final RecipeType<HbmFluidCompressorRecipes.RecipeEntry> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    CompressorRecipeCategory(RecipeType<HbmFluidCompressorRecipes.RecipeEntry> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<HbmFluidCompressorRecipes.RecipeEntry> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_compressor", "Compressor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, HbmFluidCompressorRecipes.RecipeEntry recipe,
            IFocusGroup focuses) {
        JeiFluidSlots.addFluidSlot(builder,
                new HbmFluidStack(recipe.inputType(), recipe.recipe().inputAmount(), recipe.inputPressure()),
                true, 12, 23);
        JeiFluidSlots.addFluidSlot(builder, recipe.recipe().output(), false, 110, 23);
    }

    @Override
    public void draw(HbmFluidCompressorRecipes.RecipeEntry recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 58, 23);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, HbmFluidCompressorRecipes.RecipeEntry recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.add(Component.literal("Input pressure: " + recipe.inputPressure() + " PU"));
        tooltip.add(Component.literal("Output pressure: " + recipe.recipe().output().pressure() + " PU"));
        tooltip.add(Component.literal("Duration: " + recipe.recipe().duration() + " ticks"));
    }
}
