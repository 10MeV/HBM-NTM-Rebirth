package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.BoilerRecipeRuntime;
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

public final class BoilerRecipeCategory implements IRecipeCategory<BoilerRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 132;
    private static final int HEIGHT = 58;

    private final RecipeType<BoilerRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    BoilerRecipeCategory(RecipeType<BoilerRecipeRuntime.DisplayRecipe> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<BoilerRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_boiler", "Boiler");
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
    public void setRecipe(IRecipeLayoutBuilder builder, BoilerRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        JeiFluidSlots.addFluidSlot(builder, recipe.input(), true, 12, 21);
        JeiFluidSlots.addFluidSlot(builder, recipe.output(), false, 96, 21);
    }

    @Override
    public void draw(BoilerRecipeRuntime.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 54, 21);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, BoilerRecipeRuntime.DisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.add(Component.literal("Heat: " + recipe.heatRequired() + " TU"));
        tooltip.add(Component.literal("Boiler efficiency: " + recipe.efficiency()));
    }
}
