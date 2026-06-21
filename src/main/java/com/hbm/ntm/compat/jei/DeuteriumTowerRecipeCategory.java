package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.DeuteriumTowerRecipeRuntime;
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

public final class DeuteriumTowerRecipeCategory implements IRecipeCategory<DeuteriumTowerRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 138;
    private static final int HEIGHT = 62;

    private final RecipeType<DeuteriumTowerRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    DeuteriumTowerRecipeCategory(RecipeType<DeuteriumTowerRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<DeuteriumTowerRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_deuterium_tower",
                "Deuterium Tower");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DeuteriumTowerRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        JeiFluidSlots.addFluidSlot(builder, recipe.input(), true, 12, 22);
        JeiFluidSlots.addFluidSlot(builder, recipe.output(), false, 108, 22);
    }

    @Override
    public void draw(DeuteriumTowerRecipeRuntime.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 58, 22);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, DeuteriumTowerRecipeRuntime.DisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.add(Component.literal("Ratio: " + DeuteriumTowerRecipeRuntime.WATER_PER_HEAVY_WATER
                + " mB water -> 1 mB heavy water"));
        tooltip.add(Component.literal("Power: " + recipe.powerPerOperation() + " HE per operation"));
    }
}