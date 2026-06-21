package com.hbm.ntm.compat.jei;

import com.hbm.ntm.blockentity.RtgBlockEntity;
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

public final class RtgFuelRecipeCategory implements IRecipeCategory<RtgBlockEntity.FuelSpec> {
    private static final int WIDTH = 132;
    private static final int HEIGHT = 58;

    private final RecipeType<RtgBlockEntity.FuelSpec> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    RtgFuelRecipeCategory(RecipeType<RtgBlockEntity.FuelSpec> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<RtgBlockEntity.FuelSpec> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_rtg_grey", "RT Generator");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RtgBlockEntity.FuelSpec recipe, IFocusGroup focuses) {
        builder.addInputSlot(12, 20)
                .addItemStack(recipe.input())
                .setStandardSlotBackground();
    }

    @Override
    public void draw(RtgBlockEntity.FuelSpec recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 58, 20);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RtgBlockEntity.FuelSpec recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.add(Component.literal("Heat: " + recipe.heat()));
        tooltip.add(Component.literal("Power: " + recipe.powerPerTick() + " HE/t per pellet"));
    }
}
