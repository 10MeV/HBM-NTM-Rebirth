package com.hbm.ntm.compat.jei;

import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.recipe.CrucibleRecipeRuntime;
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

public final class CrucibleRecipeCategory implements HbmJeiRecipeCategory<CrucibleRecipeRuntime.Recipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 65;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_crucible.png");

    private final RecipeType<CrucibleRecipeRuntime.Recipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final ItemStack catalyst;

    CrucibleRecipeCategory(RecipeType<CrucibleRecipeRuntime.Recipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 11, WIDTH, HEIGHT);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<CrucibleRecipeRuntime.Recipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Crucible Alloying");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CrucibleRecipeRuntime.Recipe recipe, IFocusGroup focuses) {
        addMaterialSlots(builder, RecipeIngredientRole.INPUT, recipe.input(), 12);
        builder.addSlot(RecipeIngredientRole.CATALYST, 75, 42)
                .addItemStack(catalyst);
        addMaterialSlots(builder, RecipeIngredientRole.OUTPUT, recipe.output(), 102);
    }

    @Override
    public void draw(CrucibleRecipeRuntime.Recipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
    }

    private static void addMaterialSlots(IRecipeLayoutBuilder builder, RecipeIngredientRole role,
            List<MaterialStack> materials, int xBase) {
        for (int i = 0; i < materials.size(); i++) {
            ItemStack stack = FoundryScrapsItem.create(materials.get(i), true);
            if (!stack.isEmpty()) {
                builder.addSlot(role, xBase + (i % 3) * 18, 6 + (i / 3) * 18)
                        .addItemStack(stack);
            }
        }
    }
}
