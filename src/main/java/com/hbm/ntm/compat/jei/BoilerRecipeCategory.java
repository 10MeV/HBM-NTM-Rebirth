package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.BoilerRecipeRuntime;
import com.hbm.ntm.registry.ModBlocks;
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

public final class BoilerRecipeCategory implements IRecipeCategory<BoilerRecipeRuntime.DisplayRecipe> {
    private final RecipeType<BoilerRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final List<ItemStack> catalysts;

    BoilerRecipeCategory(RecipeType<BoilerRecipeRuntime.DisplayRecipe> type, ItemLike catalyst,
            IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalysts = List.of(new ItemStack(catalyst), new ItemStack(ModBlocks.MACHINE_INDUSTRIAL_BOILER.get()));
    }

    @Override
    public RecipeType<BoilerRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_boiler", "Boiler");
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BoilerRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground,
                List.of(List.of(LegacyNeiUniversalLayout.fluidIcon(recipe.input()))));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground,
                List.of(List.of(LegacyNeiUniversalLayout.fluidIcon(recipe.output()))));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalysts);
    }
}
