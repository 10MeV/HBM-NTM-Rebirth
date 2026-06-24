package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.PWRFuelRuntime;
import java.util.Locale;
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

public final class PWRFuelRecipeCategory implements IRecipeCategory<PWRFuelRuntime.DisplayFuel> {
    private static final int WIDTH = 132;
    private static final int HEIGHT = 58;

    private final RecipeType<PWRFuelRuntime.DisplayFuel> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    PWRFuelRecipeCategory(RecipeType<PWRFuelRuntime.DisplayFuel> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<PWRFuelRuntime.DisplayFuel> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.pwr_controller", "PWR Controller");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PWRFuelRuntime.DisplayFuel recipe, IFocusGroup focuses) {
        builder.addInputSlot(10, 26)
                .addItemStack(recipe.input())
                .setStandardSlotBackground();
        builder.addOutputSlot(104, 26)
                .addItemStack(recipe.hot())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(PWRFuelRuntime.DisplayFuel recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 58, 26);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, PWRFuelRuntime.DisplayFuel recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        PWRFuelRuntime.Type type = recipe.type();
        tooltip.add(Component.literal("Reactor output: hot PWR fuel"));
        tooltip.add(Component.literal("Heat per flux: " + format(type.heatEmission()) + " TU"));
        tooltip.add(Component.literal("Reaction function: " + type.curve().fuelLabel()));
        tooltip.add(Component.literal("Fuel type: " + type.curve().dangerLabel()));
        tooltip.add(Component.literal("Yield: " + String.format(Locale.US, "%,d", type.yield())));
    }

    private static String format(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
