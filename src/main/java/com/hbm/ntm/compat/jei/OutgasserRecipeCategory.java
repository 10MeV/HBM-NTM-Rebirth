package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.OutgasserRecipe;
import com.hbm.ntm.registry.ModBlocks;
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

import java.util.List;

public final class OutgasserRecipeCategory implements IRecipeCategory<OutgasserRecipe> {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 58;

    private final RecipeType<OutgasserRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    OutgasserRecipeCategory(RecipeType<OutgasserRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<OutgasserRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.rbmk_outgasser", "RBMK Irradiation Channel");
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
    public void setRecipe(IRecipeLayoutBuilder builder, OutgasserRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = recipe.input().displayStacks();
        if (!inputs.isEmpty()) {
            builder.addInputSlot(10, 20)
                    .addItemStacks(inputs)
                    .setStandardSlotBackground();
        }

        int outputX = 104;
        if (recipe.solidOutput().isPresent()) {
            builder.addOutputSlot(outputX, 20)
                    .addItemStack(recipe.solidOutput().get())
                    .setOutputSlotBackground();
            outputX += 20;
        }
        final int fluidOutputX = outputX;
        recipe.fluidOutput().ifPresent(fluid -> JeiFluidSlots.addFluidSlot(builder, fluid, false, fluidOutputX, 20));

        if (recipe.fusionOnly()) {
            builder.addInputSlot(60, 2)
                    .addItemStack(new ItemStack(ModBlocks.FUSION_BREEDER.get()))
                    .setStandardSlotBackground();
        }
    }

    @Override
    public void draw(OutgasserRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 56, 20);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, OutgasserRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (recipe.fusionOnly()) {
            tooltip.add(Component.translatableWithFallback("jei.hbm_ntm_rebirth.outgasser.fusion_only",
                    "Requires Fusion Breeder"));
        }
    }
}
