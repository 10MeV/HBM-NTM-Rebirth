package com.hbm.ntm.compat.jei;

import com.hbm.ntm.recipe.OreSlopperRecipeRuntime;
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

public final class OreSlopperRecipeCategory implements IRecipeCategory<OreSlopperRecipeRuntime.DisplayRecipe> {
    private final RecipeType<OreSlopperRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    OreSlopperRecipeCategory(RecipeType<OreSlopperRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<OreSlopperRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_ore_slopper",
                "Bedrock Ore Processor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, OreSlopperRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground,
                List.of(List.of(LegacyNeiUniversalLayout.fluidIcon(recipe.water())), List.of(recipe.input())));

        List<List<ItemStack>> outputs = new ArrayList<>();
        for (ItemStack output : recipe.possibleOutputs()) {
            if (!output.isEmpty()) {
                outputs.add(List.of(output.copy()));
            }
        }
        outputs.add(List.of(LegacyNeiUniversalLayout.fluidIcon(recipe.slop())));
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, outputs);
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }
}
