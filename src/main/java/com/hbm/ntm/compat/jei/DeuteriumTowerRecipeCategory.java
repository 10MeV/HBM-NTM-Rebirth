package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.DeuteriumTowerRecipeRuntime;
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

public final class DeuteriumTowerRecipeCategory implements HbmJeiRecipeCategory<DeuteriumTowerRecipeRuntime.DisplayRecipe> {
    private final RecipeType<DeuteriumTowerRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final List<ItemStack> catalysts;

    DeuteriumTowerRecipeCategory(RecipeType<DeuteriumTowerRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalysts = List.of(new ItemStack(catalyst), new ItemStack(ModBlocks.MACHINE_DEUTERIUM_TOWER.get()));
    }

    @Override
    public RecipeType<DeuteriumTowerRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_deuterium_extractor",
                "Deuterium Extractor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DeuteriumTowerRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground,
                List.of(List.of(LegacyNeiUniversalLayout.fluidIcon(recipe.input()))));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground,
                List.of(List.of(LegacyNeiUniversalLayout.fluidIcon(recipe.output()))));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalysts);
    }
}
