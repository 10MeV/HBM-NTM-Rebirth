package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.MixerRecipe;
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

public final class MixerRecipeCategory implements IRecipeCategory<MixerRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 72;

    private final RecipeType<MixerRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    MixerRecipeCategory(RecipeType<MixerRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<MixerRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_mixer", "Industrial Mixer");
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
    public void setRecipe(IRecipeLayoutBuilder builder, MixerRecipe recipe, IFocusGroup focuses) {
        recipe.input1().ifPresent(input -> JeiFluidSlots.addFluidSlot(builder, input, true, 4, 8));
        recipe.input2().ifPresent(input -> JeiFluidSlots.addFluidSlot(builder, input, true, 28, 8));
        recipe.solidInput().ifPresent(input -> builder.addInputSlot(16, 36)
                .addItemStacks(input.displayStacks())
                .setStandardSlotBackground());
        JeiFluidSlots.addFluidSlot(builder, recipe.output(), false, 130, 22);
    }

    @Override
    public void draw(MixerRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 24);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, MixerRecipe recipe, IRecipeSlotsView recipeSlotsView,
            double mouseX, double mouseY) {
        if (mouseY >= 54) {
            tooltip.add(Component.literal("Duration: " + recipe.duration() + " ticks"));
        }
    }
}
