package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.CyclotronRecipeRuntime;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class CyclotronRecipeCategory
        implements HbmJeiRecipeCategory<CyclotronRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 65;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_cyclotron.png");

    private final RecipeType<CyclotronRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableAnimated progress;

    CyclotronRecipeCategory(RecipeType<CyclotronRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 11, WIDTH, HEIGHT);
        this.progress = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 100, 119, 24, 16), 48, StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<CyclotronRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Cyclotron");
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
    public IDrawable getRecipeBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CyclotronRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        builder.addInputSlot(21, 24)
                .addItemStacks(singles(recipe.particleInputs()));
        builder.addInputSlot(75, 24)
                .addItemStacks(singles(recipe.targetInputs()));
        builder.addOutputSlot(129, 24)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(CyclotronRecipeRuntime.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
        progress.draw(guiGraphics, 44, 24);
    }

    private static List<ItemStack> singles(List<ItemStack> stacks) {
        return stacks.stream()
                .map(CyclotronRecipeCategory::single)
                .toList();
    }

    private static ItemStack single(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }
}
