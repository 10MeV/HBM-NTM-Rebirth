package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.OutgasserRecipe;
import com.hbm.ntm.registry.ModBlocks;
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

public final class OutgasserRecipeCategory implements IRecipeCategory<OutgasserRecipe> {
    private static final int WIDTH = LegacyNeiUniversalLayout.WIDTH;
    private static final int HEIGHT = LegacyNeiUniversalLayout.HEIGHT;

    private final RecipeType<OutgasserRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final List<ItemStack> defaultCatalysts;
    private final List<ItemStack> fusionOnlyCatalyst;

    OutgasserRecipeCategory(RecipeType<OutgasserRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.defaultCatalysts = List.of(new ItemStack(ModBlocks.RBMK_OUTGASSER.get()),
                new ItemStack(ModBlocks.FUSION_BREEDER.get()));
        this.fusionOnlyCatalyst = List.of(new ItemStack(ModBlocks.FUSION_BREEDER.get()));
    }

    @Override
    public RecipeType<OutgasserRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.rbmk_outgasser", "RBMK Irradiation Channel");
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
    public void setRecipe(IRecipeLayoutBuilder builder, OutgasserRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = recipe.input().displayStacks();
        if (!inputs.isEmpty()) {
            LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, List.of(inputs));
        }

        List<List<ItemStack>> outputs = new ArrayList<>();
        if (recipe.solidOutput().isPresent()) {
            outputs.add(List.of(recipe.solidOutput().get()));
        }
        recipe.fluidOutput().ifPresent(fluid -> outputs.add(List.of(LegacyNeiUniversalLayout.fluidIcon(fluid))));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, outputs);

        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground,
                recipe.fusionOnly() ? fusionOnlyCatalyst : defaultCatalysts);
    }
}
