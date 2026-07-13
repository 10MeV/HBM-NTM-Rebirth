package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.RadiolysisRecipes;
import com.hbm.ntm.registry.ModItems;
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

public final class RadiolysisRecipeCategory implements HbmJeiRecipeCategory<RadiolysisRecipes.DisplayRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 65;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_radiolysis.png");

    private final RecipeType<RadiolysisRecipes.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableAnimated progress;

    RadiolysisRecipeCategory(RecipeType<RadiolysisRecipes.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 11, WIDTH, HEIGHT);
        this.progress = guiHelper.createAnimatedDrawable(
                guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 87, 64, 28), 60, StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<RadiolysisRecipes.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Radiolysis");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RadiolysisRecipes.DisplayRecipe recipe,
            IFocusGroup focuses) {
        builder.addInputSlot(34, 25)
                .addItemStack(fluidIconOrNothing(recipe.input()));
        builder.addOutputSlot(118, 16)
                .addItemStack(fluidIconOrNothing(recipe.left()));
        builder.addOutputSlot(118, 34)
                .addItemStack(fluidIconOrNothing(recipe.right()));
    }

    @Override
    public void draw(RadiolysisRecipes.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
        progress.draw(guiGraphics, 52, 19);
    }

    private static ItemStack fluidIconOrNothing(HbmFluidStack stack) {
        if (stack == null || stack.type() == HbmFluids.NONE || stack.amount() <= 0) {
            return new ItemStack(ModItems.NOTHING.get());
        }
        return LegacyNeiUniversalLayout.fluidIcon(stack);
    }
}
