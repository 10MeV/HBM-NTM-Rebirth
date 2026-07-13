package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.MixerRecipe;
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

public final class MixerRecipeCategory implements HbmJeiRecipeCategory<MixerRecipe> {
    private final RecipeType<MixerRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    MixerRecipeCategory(RecipeType<MixerRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<MixerRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_mixer", "Industrial Mixer");
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
    public void setRecipe(IRecipeLayoutBuilder builder, MixerRecipe recipe, IFocusGroup focuses) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        recipe.input1().ifPresent(input -> inputs.add(List.of(LegacyNeiUniversalLayout.fluidIcon(input))));
        recipe.input2().ifPresent(input -> inputs.add(List.of(LegacyNeiUniversalLayout.fluidIcon(input))));
        recipe.solidInput().ifPresent(input -> inputs.add(input.displayStacks()));

        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, inputs);
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground,
                List.of(List.of(LegacyNeiUniversalLayout.fluidIcon(recipe.output()))));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }
}
