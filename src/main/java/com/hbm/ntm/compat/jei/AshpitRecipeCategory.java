package com.hbm.ntm.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public final class AshpitRecipeCategory implements IRecipeCategory<AshpitJeiRecipe> {
    private static final int WIDTH = 140;
    private static final int HEIGHT = 60;

    private final RecipeType<AshpitJeiRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    AshpitRecipeCategory(RecipeType<AshpitJeiRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<AshpitJeiRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_ashpit", "Ashpit");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AshpitJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(8, 22)
                .addItemStacks(recipe.machines())
                .setStandardSlotBackground();
        if (recipe.hasFluidInput()) {
            JeiFluidSlots.addFluidSlot(builder, recipe.fluidInput(), true, 38, 22);
        } else {
            builder.addInputSlot(38, 22)
                    .addItemStacks(recipe.itemInputs())
                    .setStandardSlotBackground();
        }
        builder.addOutputSlot(114, 22)
                .addItemStack(recipe.output())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(AshpitJeiRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 72, 22);
    }
}
