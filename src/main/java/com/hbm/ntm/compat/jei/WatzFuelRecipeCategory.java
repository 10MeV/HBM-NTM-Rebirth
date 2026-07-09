package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.WatzFuelRuntime;
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

public final class WatzFuelRecipeCategory implements IRecipeCategory<WatzFuelRuntime.DisplayPellet> {
    private static final int WIDTH = LegacyNeiUniversalLayout.WIDTH;
    private static final int HEIGHT = LegacyNeiUniversalLayout.HEIGHT;

    private final RecipeType<WatzFuelRuntime.DisplayPellet> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    WatzFuelRecipeCategory(RecipeType<WatzFuelRuntime.DisplayPellet> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<WatzFuelRuntime.DisplayPellet> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.watz", "Watz Reactor");
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WatzFuelRuntime.DisplayPellet recipe,
            IFocusGroup focuses) {
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, List.of(List.of(recipe.input())));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, List.of(List.of(recipe.depleted())));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }
}
