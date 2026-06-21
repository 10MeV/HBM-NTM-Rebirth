package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.BlastFurnaceRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import java.util.List;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class BlastFurnaceRecipeCategory implements IRecipeCategory<BlastFurnaceRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 72;

    private final RecipeType<BlastFurnaceRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    BlastFurnaceRecipeCategory(RecipeType<BlastFurnaceRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<BlastFurnaceRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_blast_furnace", "Blast Furnace");
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
    public void setRecipe(IRecipeLayoutBuilder builder, BlastFurnaceRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.inputs().size(); i++) {
            HbmIngredient input = recipe.inputs().get(i);
            builder.addInputSlot(4 + i * 22, 18)
                    .addItemStacks(input.displayStacks())
                    .setStandardSlotBackground();
        }
        for (int i = 0; i < recipe.outputs().size(); i++) {
            HbmItemOutput output = recipe.outputs().get(i);
            List<ItemStack> stacks = output.displayStacks();
            if (!stacks.isEmpty()) {
                builder.addOutputSlot(130 + i * 22, 18)
                        .addItemStacks(stacks)
                        .setOutputSlotBackground();
            }
        }
    }

    @Override
    public void draw(BlastFurnaceRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 18);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, BlastFurnaceRecipe recipe, IRecipeSlotsView recipeSlotsView,
            double mouseX, double mouseY) {
        if (mouseY >= 54) {
            tooltip.add(Component.literal("Duration: " + recipe.duration() + " ticks"));
        }
    }
}
