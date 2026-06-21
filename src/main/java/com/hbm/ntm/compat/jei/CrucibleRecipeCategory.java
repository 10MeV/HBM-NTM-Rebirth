package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.CrucibleRecipeRuntime;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public final class CrucibleRecipeCategory implements IRecipeCategory<CrucibleRecipeRuntime.Recipe> {
    private static final int WIDTH = 180;
    private static final int HEIGHT = 100;

    private final RecipeType<CrucibleRecipeRuntime.Recipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    CrucibleRecipeCategory(RecipeType<CrucibleRecipeRuntime.Recipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<CrucibleRecipeRuntime.Recipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_crucible", "Crucible");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CrucibleRecipeRuntime.Recipe recipe, IFocusGroup focuses) {
        builder.addOutputSlot(150, 36)
                .addItemStack(recipe.icon())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(CrucibleRecipeRuntime.Recipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 108, 36);
        int y = 8;
        for (Component line : CrucibleRecipeRuntime.displayLines(recipe)) {
            guiGraphics.drawString(Minecraft.getInstance().font, line, 4, y, 0x404040, false);
            y += 10;
            if (y > 88) {
                break;
            }
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, CrucibleRecipeRuntime.Recipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.addAll(CrucibleRecipeRuntime.displayLines(recipe));
        tooltip.add(Component.literal("Frequency: " + recipe.frequency()));
    }
}