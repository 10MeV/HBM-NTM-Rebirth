package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluidStack;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public final class HbmOilRecipeCategory implements IRecipeCategory<HbmOilRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 72;

    private final RecipeType<HbmOilRecipe> type;
    private final Component title;
    private final IDrawableStatic background;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    HbmOilRecipeCategory(RecipeType<HbmOilRecipe> type, Component title, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.title = title;
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<HbmOilRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
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
    public void setRecipe(IRecipeLayoutBuilder builder, HbmOilRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.itemInputs().size(); i++) {
            addItemInput(builder, recipe.itemInputs().get(i), 4 + i * 18, 4);
        }
        for (int i = 0; i < recipe.fluidInputs().size(); i++) {
            addFluidSlot(builder, recipe.fluidInputs().get(i), true, 4 + i * 18, 30);
        }

        int fluidOutputStart = Math.max(108, 148 - recipe.fluidOutputs().size() * 18);
        for (int i = 0; i < recipe.fluidOutputs().size(); i++) {
            addFluidSlot(builder, recipe.fluidOutputs().get(i), false, fluidOutputStart + i * 18, 30);
        }
        int itemOutputStart = Math.max(108, 148 - recipe.itemOutputs().size() * 18);
        for (int i = 0; i < recipe.itemOutputs().size(); i++) {
            ItemStack stack = recipe.itemOutputs().get(i);
            if (!stack.isEmpty()) {
                builder.addOutputSlot(itemOutputStart + i * 18, 4)
                        .addItemStack(stack)
                        .setOutputSlotBackground();
            }
        }
    }

    @Override
    public void draw(HbmOilRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 28);
    }

    private static void addItemInput(IRecipeLayoutBuilder builder, Ingredient ingredient, int x, int y) {
        builder.addInputSlot(x, y)
                .addItemStacks(List.of(ingredient.getItems()))
                .setStandardSlotBackground();
    }

    private static void addFluidSlot(IRecipeLayoutBuilder builder, HbmFluidStack hbmStack,
            boolean input, int x, int y) {
        FluidStack forgeStack = HbmFluidForgeMappings.toForge(hbmStack.type(), hbmStack.amount());
        if (forgeStack.isEmpty()) {
            return;
        }
        Fluid fluid = forgeStack.getFluid();
        if (input) {
            builder.addInputSlot(x, y)
                    .addFluidStack(fluid, forgeStack.getAmount())
                    .setFluidRenderer(Math.max(1, forgeStack.getAmount()), false, 16, 16)
                    .setStandardSlotBackground();
        } else {
            builder.addOutputSlot(x, y)
                    .addFluidStack(fluid, forgeStack.getAmount())
                    .setFluidRenderer(Math.max(1, forgeStack.getAmount()), false, 16, 16)
                    .setOutputSlotBackground();
        }
    }
}
