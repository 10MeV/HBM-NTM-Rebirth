package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.OreSlopperRecipeRuntime;
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

public final class OreSlopperRecipeCategory implements IRecipeCategory<OreSlopperRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 84;

    private final RecipeType<OreSlopperRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    OreSlopperRecipeCategory(RecipeType<OreSlopperRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<OreSlopperRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_ore_slopper",
                "Bedrock Ore Processor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, OreSlopperRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        builder.addInputSlot(6, 12)
                .addItemStack(recipe.input())
                .setStandardSlotBackground();
        JeiFluidSlots.addFluidSlot(builder, recipe.water(), true, 6, 44);
        JeiFluidSlots.addFluidSlot(builder, recipe.slop(), false, 124, 44);
        addPossibleOutputs(builder, recipe.possibleOutputs());
    }

    @Override
    public void draw(OreSlopperRecipeRuntime.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 76, 28);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, OreSlopperRecipeRuntime.DisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.add(Component.literal("Water: " + OreSlopperRecipeRuntime.WATER_USED + " mB per cycle"));
        tooltip.add(Component.literal("Base power: " + recipe.baseConsumption() + " HE/t"));
        tooltip.add(Component.literal("Ore outputs follow the raw bedrock ore composition."));
    }

    private static void addPossibleOutputs(IRecipeLayoutBuilder builder, List<ItemStack> outputs) {
        for (int i = 0; i < outputs.size(); i++) {
            builder.addOutputSlot(102 + (i % 3) * 18, 8 + (i / 3) * 18)
                    .addItemStack(outputs.get(i))
                    .setOutputSlotBackground();
        }
    }
}