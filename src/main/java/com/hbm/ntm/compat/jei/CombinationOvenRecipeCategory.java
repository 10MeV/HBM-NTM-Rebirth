package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.CombinationOvenRecipe;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class CombinationOvenRecipeCategory implements IRecipeCategory<CombinationOvenRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 72;

    private final RecipeType<CombinationOvenRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    CombinationOvenRecipeCategory(RecipeType<CombinationOvenRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<CombinationOvenRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.furnace_combination", "Combination Oven");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CombinationOvenRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(4, 18)
                .addItemStacks(recipe.input().displayStacks())
                .setStandardSlotBackground();
        recipe.outputItem().ifPresent(output -> {
            List<ItemStack> stacks = output.displayStacks();
            if (!stacks.isEmpty()) {
                builder.addOutputSlot(130, 8)
                        .addItemStacks(stacks)
                        .setOutputSlotBackground();
            }
        });
        recipe.outputFluid().ifPresent(output -> JeiFluidSlots.addFluidSlot(builder, output, false, 130, 34));
    }

    @Override
    public void draw(CombinationOvenRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 20);
    }
}
