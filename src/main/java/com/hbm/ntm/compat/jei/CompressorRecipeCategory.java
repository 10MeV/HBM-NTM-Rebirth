package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluidCompressorRecipes;
import com.hbm.ntm.fluid.HbmFluidStack;
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

public final class CompressorRecipeCategory implements IRecipeCategory<HbmFluidCompressorRecipes.RecipeEntry> {
    private final RecipeType<HbmFluidCompressorRecipes.RecipeEntry> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final List<ItemStack> catalysts;

    CompressorRecipeCategory(RecipeType<HbmFluidCompressorRecipes.RecipeEntry> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalysts = List.of(new ItemStack(catalyst), new ItemStack(ModBlocks.MACHINE_COMPRESSOR_COMPACT.get()));
    }

    @Override
    public RecipeType<HbmFluidCompressorRecipes.RecipeEntry> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_compressor", "Compressor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, HbmFluidCompressorRecipes.RecipeEntry recipe,
            IFocusGroup focuses) {
        HbmFluidStack input = new HbmFluidStack(recipe.inputType(), recipe.recipe().inputAmount(),
                recipe.inputPressure());
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground,
                List.of(List.of(LegacyNeiUniversalLayout.fluidIcon(input))));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground,
                List.of(List.of(LegacyNeiUniversalLayout.fluidIcon(recipe.recipe().output()))));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalysts);
    }
}
