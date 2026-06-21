package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.ZirnoxFuelRuntime;
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

public final class ZirnoxFuelRecipeCategory implements IRecipeCategory<ZirnoxFuelRuntime.DisplayRod> {
    private static final int WIDTH = 132;
    private static final int HEIGHT = 58;

    private final RecipeType<ZirnoxFuelRuntime.DisplayRod> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    ZirnoxFuelRecipeCategory(RecipeType<ZirnoxFuelRuntime.DisplayRod> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<ZirnoxFuelRuntime.DisplayRod> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.reactor_zirnox", "ZIRNOX Reactor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ZirnoxFuelRuntime.DisplayRod recipe, IFocusGroup focuses) {
        builder.addInputSlot(12, 20)
                .addItemStack(recipe.input())
                .setStandardSlotBackground();
        builder.addOutputSlot(104, 20)
                .addItemStack(recipe.product())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(ZirnoxFuelRuntime.DisplayRod recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 58, 20);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ZirnoxFuelRuntime.DisplayRod recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.add(Component.literal(recipe.breeding() ? "Breeding rod" : "Fuel rod"));
        tooltip.add(Component.literal("Heat: " + recipe.heat()));
    }
}
