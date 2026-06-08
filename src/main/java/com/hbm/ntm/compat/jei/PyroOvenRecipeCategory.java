package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.PyroOvenRecipe;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public final class PyroOvenRecipeCategory implements IRecipeCategory<PyroOvenRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 72;

    private final RecipeType<PyroOvenRecipe> type;
    private final IDrawableStatic background;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    PyroOvenRecipeCategory(RecipeType<PyroOvenRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<PyroOvenRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_pyrooven", "Pyrolysis Oven");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PyroOvenRecipe recipe, IFocusGroup focuses) {
        recipe.inputItem().ifPresent(input -> builder.addInputSlot(4, 4)
                .addItemStacks(input.displayStacks())
                .setStandardSlotBackground());
        recipe.inputFluid().ifPresent(input -> addFluidSlot(builder, input, true, 4, 30));
        recipe.outputItem().ifPresent(output -> {
            List<ItemStack> stacks = output.displayStacks();
            if (!stacks.isEmpty()) {
                builder.addOutputSlot(130, 4)
                        .addItemStacks(stacks)
                        .setOutputSlotBackground();
            }
        });
        recipe.outputFluid().ifPresent(output -> addFluidSlot(builder, output, false, 130, 30));
    }

    @Override
    public void draw(PyroOvenRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 28);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, PyroOvenRecipe recipe, IRecipeSlotsView recipeSlotsView,
            double mouseX, double mouseY) {
        if (mouseY >= 54) {
            tooltip.add(Component.literal("Duration: " + recipe.duration() + " ticks"));
        }
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
