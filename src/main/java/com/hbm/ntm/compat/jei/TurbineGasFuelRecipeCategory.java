package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.TurbineGasRecipeRuntime;
import java.util.Locale;
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

public final class TurbineGasFuelRecipeCategory
        implements IRecipeCategory<TurbineGasRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 138;
    private static final int HEIGHT = 58;

    private final RecipeType<TurbineGasRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;

    TurbineGasFuelRecipeCategory(RecipeType<TurbineGasRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
    }

    @Override
    public RecipeType<TurbineGasRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_turbinegas", "Gas Turbine");
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
    public void setRecipe(IRecipeLayoutBuilder builder, TurbineGasRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        JeiFluidSlots.addFluidSlot(builder, recipe.fuel(), true, 12, 21);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, TurbineGasRecipeRuntime.DisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.add(Component.literal("Fuel grade: Gaseous"));
        tooltip.add(Component.literal("Power: " + recipe.powerPerMb() + " HE/mB"));
        tooltip.add(Component.literal("Max use: " + format(recipe.maxConsumptionMbPerTick()) + " mB/t"));
        tooltip.add(Component.literal("Max burn temp: " + recipe.maxBurnTemperature() + " C"));
    }

    private static String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
