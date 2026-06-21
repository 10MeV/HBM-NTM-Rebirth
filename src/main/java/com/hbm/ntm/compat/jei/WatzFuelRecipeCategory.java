package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.WatzFuelRuntime;
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

public final class WatzFuelRecipeCategory implements IRecipeCategory<WatzFuelRuntime.DisplayPellet> {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 70;

    private final RecipeType<WatzFuelRuntime.DisplayPellet> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    WatzFuelRecipeCategory(RecipeType<WatzFuelRuntime.DisplayPellet> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<WatzFuelRuntime.DisplayPellet> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.watz", "Watz Reactor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, WatzFuelRuntime.DisplayPellet recipe,
            IFocusGroup focuses) {
        builder.addInputSlot(12, 26)
                .addItemStack(recipe.input())
                .setStandardSlotBackground();
        builder.addOutputSlot(116, 26)
                .addItemStack(recipe.depleted())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(WatzFuelRuntime.DisplayPellet recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 62, 26);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, WatzFuelRuntime.DisplayPellet recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        WatzFuelRuntime.Type type = recipe.type();
        tooltip.add(Component.literal("Passive flux: " + format(type.passive())));
        tooltip.add(Component.literal("Heat per flux: " + format(type.heatEmission())));
        tooltip.add(Component.literal("Mud content: " + format(type.mudContent())));
        if (type.burnFunc() != null) {
            tooltip.add(Component.literal("Burn curve: " + type.burnFunc().fuelLabel()));
        }
        if (type.absorbFunc() != null) {
            tooltip.add(Component.literal("Absorb curve: " + type.absorbFunc().fuelLabel()));
        }
    }

    private static String format(double value) {
        return String.format(Locale.US, "%.3f", value);
    }
}
