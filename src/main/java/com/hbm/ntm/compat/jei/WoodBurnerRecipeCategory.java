package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.WoodBurnerRecipeRuntime;
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

public final class WoodBurnerRecipeCategory implements IRecipeCategory<WoodBurnerRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 138;
    private static final int HEIGHT = 64;

    private final RecipeType<WoodBurnerRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    WoodBurnerRecipeCategory(RecipeType<WoodBurnerRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<WoodBurnerRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_wood_burner", "Wood Burner");
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
    public void setRecipe(IRecipeLayoutBuilder builder, WoodBurnerRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        if (recipe.isSolid()) {
            builder.addInputSlot(12, 23)
                    .addItemStack(recipe.solid().input())
                    .setStandardSlotBackground();
        } else {
            JeiFluidSlots.addFluidSlot(builder, recipe.liquid().displayFluid(), true, 12, 23);
        }
    }

    @Override
    public void draw(WoodBurnerRecipeRuntime.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 58, 23);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, WoodBurnerRecipeRuntime.DisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (recipe.isSolid()) {
            tooltip.add(Component.literal("Power: 100 HE/t"));
            tooltip.add(Component.literal("Burn time: " + recipe.solid().burnTime() + " ticks"));
        } else {
            tooltip.add(Component.literal("Power per bucket: " + recipe.liquid().powerPerBucket() + " HE"));
        }
    }
}