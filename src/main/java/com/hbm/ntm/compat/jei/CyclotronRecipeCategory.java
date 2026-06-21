package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.CyclotronRecipeRuntime;
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

public final class CyclotronRecipeCategory
        implements IRecipeCategory<CyclotronRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 72;

    private final RecipeType<CyclotronRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    CyclotronRecipeCategory(RecipeType<CyclotronRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<CyclotronRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_cyclotron", "Cyclotron");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CyclotronRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        builder.addInputSlot(20, 26)
                .addItemStacks(recipe.particleInputs())
                .setStandardSlotBackground();
        builder.addInputSlot(54, 26)
                .addItemStacks(recipe.targetInputs())
                .setStandardSlotBackground();
        builder.addOutputSlot(128, 26)
                .addItemStack(recipe.output())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(CyclotronRecipeRuntime.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 28);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, CyclotronRecipeRuntime.DisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseY >= 54 && recipe.antimatterMb() > 0) {
            tooltip.add(Component.literal("Antimatter: " + recipe.antimatterMb() + " mB"));
        }
    }
}
