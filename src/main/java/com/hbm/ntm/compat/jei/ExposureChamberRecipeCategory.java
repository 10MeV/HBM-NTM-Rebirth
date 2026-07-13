package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.ExposureChamberRecipe;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ExposureChamberRecipeCategory implements HbmJeiRecipeCategory<ExposureChamberRecipe> {
    private final RecipeType<ExposureChamberRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    ExposureChamberRecipeCategory(RecipeType<ExposureChamberRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<ExposureChamberRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_exposure_chamber",
                "Exposure Chamber");
    }

    @Override
    public int getWidth() {
        return LegacyNeiUniversalLayout.WIDTH;
    }

    @Override
    public int getHeight() {
        return LegacyNeiUniversalLayout.HEIGHT;
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
    public void setRecipe(IRecipeLayoutBuilder builder, ExposureChamberRecipe recipe, IFocusGroup focuses) {
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, List.of(
                withCount(recipe.ingredient().displayStacks(), 8),
                recipe.particle().displayStacks()));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, List.of(List.of(withCount(recipe.output(), 8))));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }

    private static List<ItemStack> withCount(List<ItemStack> stacks, int count) {
        List<ItemStack> copies = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copies.add(withCount(stack, count));
        }
        return copies;
    }

    private static ItemStack withCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }
}
