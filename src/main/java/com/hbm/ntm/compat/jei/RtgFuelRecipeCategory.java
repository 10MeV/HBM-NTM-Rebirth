package com.hbm.ntm.compat.jei;

import com.hbm.ntm.blockentity.RtgBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.RtgPelletRuntime;
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

public final class RtgFuelRecipeCategory implements IRecipeCategory<RtgBlockEntity.FuelSpec> {
    private final RecipeType<RtgBlockEntity.FuelSpec> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final List<ItemStack> catalysts;

    RtgFuelRecipeCategory(RecipeType<RtgBlockEntity.FuelSpec> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalysts = List.of(
                new ItemStack(ModBlocks.MACHINE_RTG_GREY.get()),
                new ItemStack(ModBlocks.MACHINE_DIFURNACE_RTG.get()));
    }

    @Override
    public RecipeType<RtgBlockEntity.FuelSpec> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("RTG");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RtgBlockEntity.FuelSpec recipe, IFocusGroup focuses) {
        ItemStack decayOutput = RtgPelletRuntime.decayItem(recipe.input());
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, List.of(List.of(recipe.input())));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, List.of(List.of(decayOutput)));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalysts);
    }
}
