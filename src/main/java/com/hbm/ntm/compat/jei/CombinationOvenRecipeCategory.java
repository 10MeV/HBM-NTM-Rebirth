package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.CombinationOvenRecipe;
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

public final class CombinationOvenRecipeCategory implements HbmJeiRecipeCategory<CombinationOvenRecipe> {
    private final RecipeType<CombinationOvenRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    CombinationOvenRecipeCategory(RecipeType<CombinationOvenRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<CombinationOvenRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.furnace_combination", "Combination Oven");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CombinationOvenRecipe recipe, IFocusGroup focuses) {
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground, List.of(recipe.input().displayStacks()));

        List<List<ItemStack>> outputs = new ArrayList<>();
        recipe.outputItem().ifPresent(output -> {
            List<ItemStack> stacks = output.displayStacks();
            if (!stacks.isEmpty()) {
                outputs.add(stacks);
            }
        });
        recipe.outputFluid().ifPresent(output -> outputs.add(List.of(LegacyNeiUniversalLayout.fluidIcon(output))));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, outputs);
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }
}
