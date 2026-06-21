package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.SilexRecipeRuntime;
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

public final class SilexRecipeCategory implements IRecipeCategory<SilexRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 90;

    private final RecipeType<SilexRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    SilexRecipeCategory(RecipeType<SilexRecipeRuntime.DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<SilexRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_silex", "SILEX");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SilexRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        int inputY = recipe.itemInput().isEmpty() ? 28 : 18;
        if (!recipe.itemInput().isEmpty()) {
            builder.addInputSlot(4, 8)
                    .addItemStack(recipe.itemInput())
                    .setStandardSlotBackground();
        }
        if (recipe.fluidInput().type() != HbmFluids.NONE && recipe.fluidInput().amount() > 0) {
            JeiFluidSlots.addFluidSlot(builder, recipe.fluidInput(), true, 28, inputY);
        }
        int index = 0;
        for (SilexRecipeRuntime.WeightedOutput output : recipe.recipe().outputs()) {
            ItemStack stack = output.stack();
            if (!stack.isEmpty()) {
                builder.addOutputSlot(104 + (index % 3) * 20, 8 + (index / 3) * 22)
                        .addItemStack(stack)
                        .setOutputSlotBackground();
                index++;
            }
        }
    }

    @Override
    public void draw(SilexRecipeRuntime.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 72, 26);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, SilexRecipeRuntime.DisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseY >= 62) {
            tooltip.add(Component.literal("Laser: " + recipe.recipe().laserStrength().name()));
            tooltip.add(Component.literal(recipe.directFluidSource()
                    ? "Source fluid consumed per output: " + recipe.recipe().fluidConsumed() + " mB"
                    : "Peroxide loaded per item: " + recipe.recipe().fluidProduced() + " mB"));
            if (!recipe.directFluidSource()) {
                tooltip.add(Component.literal("Loaded fluid consumed per output: "
                        + recipe.recipe().fluidConsumed() + " mB"));
            }
            for (SilexRecipeRuntime.WeightedOutput output : recipe.recipe().outputs()) {
                if (!output.stack().isEmpty()) {
                    tooltip.add(Component.literal(output.stack().getHoverName().getString()
                            + " weight " + output.weight()));
                }
            }
        }
    }
}