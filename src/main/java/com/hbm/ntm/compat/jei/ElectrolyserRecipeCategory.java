package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.recipe.ElectrolyserRecipeRuntime;
import com.hbm.ntm.recipe.ElectrolyserRecipeRuntime.FluidRecipe;
import com.hbm.ntm.recipe.ElectrolyserRecipeRuntime.MetalRecipe;
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

public final class ElectrolyserRecipeCategory implements IRecipeCategory<ElectrolyserRecipeRuntime.DisplayRecipe> {
    private final RecipeType<ElectrolyserRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slotBackground;
    private final IDrawableStatic machineBackground;
    private final ItemStack catalyst;

    ElectrolyserRecipeCategory(RecipeType<ElectrolyserRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = LegacyNeiUniversalLayout.background(guiHelper);
        this.slotBackground = LegacyNeiUniversalLayout.slotBackground(guiHelper);
        this.machineBackground = LegacyNeiUniversalLayout.machineBackground(guiHelper);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<ElectrolyserRecipeRuntime.DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_electrolyser", "Electrolyser");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ElectrolyserRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        if (recipe.isFluid()) {
            FluidRecipe fluid = recipe.fluid();
            LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground,
                    List.of(List.of(fluidIcon(fluid.input(), fluid.amount()))));
            LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, fluidOutputs(fluid));
            LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
            return;
        }
        MetalRecipe metal = recipe.metal();
        ItemStack input = metal.inputStack();
        if (!input.isEmpty()) {
            LegacyNeiUniversalLayout.addInputSlots(builder, slotBackground,
                    List.of(List.of(input), List.of(fluidIcon(HbmFluids.NITRIC_ACID, 100))));
        }
        LegacyNeiUniversalLayout.addOutputSlots(builder, slotBackground, metalOutputs(metal));
        LegacyNeiUniversalLayout.addMachineCatalyst(builder, machineBackground, catalyst);
    }

    private static List<List<ItemStack>> fluidOutputs(FluidRecipe recipe) {
        List<List<ItemStack>> outputs = new ArrayList<>();
        if (recipe.output1Type() != HbmFluids.NONE && recipe.output1Amount() > 0) {
            outputs.add(List.of(fluidIcon(recipe.output1Type(), recipe.output1Amount())));
        }
        if (recipe.output2Type() != HbmFluids.NONE && recipe.output2Amount() > 0) {
            outputs.add(List.of(fluidIcon(recipe.output2Type(), recipe.output2Amount())));
        }
        addByproducts(outputs, recipe.byproducts());
        return outputs;
    }

    private static List<List<ItemStack>> metalOutputs(MetalRecipe recipe) {
        List<List<ItemStack>> outputs = new ArrayList<>();
        ItemStack output1 = FoundryScrapsItem.create(recipe.output1(), true);
        if (!output1.isEmpty()) {
            outputs.add(List.of(output1));
        }
        ItemStack output2 = FoundryScrapsItem.create(recipe.output2(), true);
        if (!output2.isEmpty()) {
            outputs.add(List.of(output2));
        }
        addByproducts(outputs, recipe.byproducts());
        return outputs;
    }

    private static void addByproducts(List<List<ItemStack>> outputs, List<ItemStack> byproducts) {
        for (ItemStack byproduct : byproducts) {
            if (!byproduct.isEmpty()) {
                outputs.add(List.of(byproduct.copy()));
            }
        }
    }

    private static ItemStack fluidIcon(com.hbm.ntm.fluid.FluidType type, int amount) {
        return LegacyNeiUniversalLayout.fluidIcon(new HbmFluidStack(type, amount, 0));
    }
}
