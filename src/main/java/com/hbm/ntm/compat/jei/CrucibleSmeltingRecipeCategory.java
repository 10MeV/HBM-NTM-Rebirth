package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.CrucibleSmeltingRecipeRuntime;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class CrucibleSmeltingRecipeCategory
        implements HbmJeiRecipeCategory<CrucibleSmeltingRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 65;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_crucible_smelting.png");

    private final RecipeType<CrucibleSmeltingRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final ItemStack catalyst;

    CrucibleSmeltingRecipeCategory(RecipeType<CrucibleSmeltingRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 11, WIDTH, HEIGHT);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<CrucibleSmeltingRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Crucible Smelting");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CrucibleSmeltingRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        builder.addInputSlot(48, 24)
                .addItemStacks(recipe.inputs());
        builder.addSlot(RecipeIngredientRole.CATALYST, 75, 42)
                .addItemStack(catalyst);
        addOutputSlots(builder, recipe.outputs());
    }

    @Override
    public void draw(CrucibleSmeltingRecipeRuntime.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
    }

    private static void addOutputSlots(IRecipeLayoutBuilder builder, List<ItemStack> outputs) {
        for (int i = 0; i < outputs.size(); i++) {
            builder.addOutputSlot(102 + (i % 3) * 18, 6 + (i / 3) * 18)
                    .addItemStack(outputs.get(i));
        }
    }
}
