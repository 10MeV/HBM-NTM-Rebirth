package com.hbm.ntm.compat.jei;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.FoundryMoldItem.Mold;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayList;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class CrucibleCastingRecipeCategory
        implements HbmJeiRecipeCategory<CrucibleCastingRecipeCategory.DisplayRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 65;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_foundry.png");

    private final RecipeType<DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;

    CrucibleCastingRecipeCategory(RecipeType<DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 5, 11, WIDTH, HEIGHT);
    }

    @Override
    public RecipeType<DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Crucible Casting");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(48, 24)
                .addItemStack(recipe.input());
        builder.addInputSlot(75, 6)
                .addItemStack(recipe.mold());
        builder.addInputSlot(75, 42)
                .addItemStack(recipe.basin());
        builder.addOutputSlot(102, 24)
                .addItemStack(recipe.output());
    }

    @Override
    public void draw(DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
    }

    public static List<DisplayRecipe> recipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (NTMMaterial material : Mats.orderedList) {
            if (material.smeltable != SmeltingBehavior.SMELTABLE) {
                continue;
            }
            for (Mold mold : FoundryMoldItem.molds()) {
                ItemStack output = mold.getOutput(material);
                ItemStack basin = basin(mold);
                if (output.isEmpty() || basin.isEmpty()) {
                    continue;
                }
                ItemStack input = FoundryScrapsItem.create(new MaterialStack(material, mold.cost()), true);
                if (!input.isEmpty()) {
                    recipes.add(new DisplayRecipe(input, FoundryMoldItem.stackFor(mold), basin, output,
                            material.id, mold.id()));
                }
            }
        }
        recipes.sort(Comparator.comparingInt(DisplayRecipe::materialId)
                .thenComparingInt(DisplayRecipe::moldId));
        return List.copyOf(recipes);
    }

    private static ItemStack basin(Mold mold) {
        return switch (mold.size()) {
            case 0 -> new ItemStack(ModBlocks.FOUNDRY_MOLD.get());
            case 1 -> new ItemStack(ModBlocks.FOUNDRY_BASIN.get());
            default -> ItemStack.EMPTY;
        };
    }

    public record DisplayRecipe(ItemStack input, ItemStack mold, ItemStack basin, ItemStack output,
                                int materialId, int moldId) {
        public DisplayRecipe {
            input = input.copy();
            mold = mold.copy();
            basin = basin.copy();
            output = output.copy();
        }
    }
}
