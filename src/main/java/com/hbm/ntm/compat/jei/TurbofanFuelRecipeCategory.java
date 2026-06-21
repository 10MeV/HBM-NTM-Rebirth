package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.TurbofanRecipeRuntime;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public final class TurbofanFuelRecipeCategory
        implements IRecipeCategory<TurbofanRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 132;
    private static final int HEIGHT = 52;

    private final RecipeType<TurbofanRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;

    TurbofanFuelRecipeCategory(RecipeType<TurbofanRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
    }

    @Override
    public RecipeType<TurbofanRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_turbofan", "Turbofan");
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
    public void setRecipe(IRecipeLayoutBuilder builder, TurbofanRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        JeiFluidSlots.addFluidSlot(builder, recipe.fuel(), true, 12, 18);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, TurbofanRecipeRuntime.DisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.add(Component.literal("Fuel grade: Aviation"));
        tooltip.add(Component.literal("Power: " + recipe.powerPerMb() + " HE/mB"));
        tooltip.add(Component.literal("Base use: 1 mB/t"));
        tooltip.add(Component.literal("Afterburner increases use and output"));
    }
}
