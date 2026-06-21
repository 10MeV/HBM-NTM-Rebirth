package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.AmmoPressRecipe;
import java.util.Comparator;
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

public final class AmmoPressRecipeCategory implements IRecipeCategory<AmmoPressRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 64;

    private final RecipeType<AmmoPressRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    AmmoPressRecipeCategory(RecipeType<AmmoPressRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<AmmoPressRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_ammo_press", "Ammunition Press");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AmmoPressRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < AmmoPressRecipe.INPUT_SLOTS; i++) {
            List<ItemStack> stacks = recipe.displayInputs(i);
            if (stacks.isEmpty()) {
                continue;
            }
            builder.addInputSlot(4 + (i % 3) * 18, 4 + (i / 3) * 18)
                    .addItemStacks(stacks)
                    .setStandardSlotBackground();
        }
        builder.addOutputSlot(130, 22)
                .addItemStack(recipe.output())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(AmmoPressRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 82, 22);
    }

    static List<AmmoPressRecipe> sorted(List<AmmoPressRecipe> recipes) {
        return recipes.stream()
                .sorted(Comparator.comparingInt(AmmoPressRecipe::sourceOrder)
                        .thenComparing(recipe -> recipe.getId().toString()))
                .toList();
    }
}
