package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.SolderingStationRecipe;
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

public final class SolderingStationRecipeCategory implements IRecipeCategory<SolderingStationRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 96;

    private final RecipeType<SolderingStationRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    SolderingStationRecipeCategory(RecipeType<SolderingStationRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<SolderingStationRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_soldering_station",
                "Soldering Station");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SolderingStationRecipe recipe, IFocusGroup focuses) {
        addInputs(builder, recipe.toppings(), 4, 4);
        addInputs(builder, recipe.pcb(), 4, 26);
        addInputs(builder, recipe.solder(), 4, 48);
        recipe.fluid().ifPresent(input -> addFluidSlot(builder, input, true, 72, 56));
        builder.addOutputSlot(140, 26)
                .addItemStack(recipe.output())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(SolderingStationRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 94, 28);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, SolderingStationRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseY >= 74) {
            tooltip.add(Component.literal("Duration: " + recipe.duration() + " ticks"));
            tooltip.add(Component.literal("Consumption: " + recipe.consumption() + " HE/t"));
        }
    }

    private static void addInputs(IRecipeLayoutBuilder builder, java.util.List<HbmIngredient> inputs,
            int x, int y) {
        for (int i = 0; i < inputs.size(); i++) {
            builder.addInputSlot(x + i * 18, y)
                    .addItemStacks(inputs.get(i).displayStacks())
                    .setStandardSlotBackground();
        }
    }

    private static void addFluidSlot(IRecipeLayoutBuilder builder, HbmFluidStack hbmStack,
            boolean input, int x, int y) {
        JeiFluidSlots.addFluidSlot(builder, hbmStack, input, x, y);
    }
}
