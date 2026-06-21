package com.hbm.ntm.compat.jei;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.ElectrolyserRecipeRuntime;
import com.hbm.ntm.recipe.ElectrolyserRecipeRuntime.FluidRecipe;
import com.hbm.ntm.recipe.ElectrolyserRecipeRuntime.MetalRecipe;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ElectrolyserRecipeCategory implements IRecipeCategory<ElectrolyserRecipeRuntime.DisplayRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 82;

    private final RecipeType<ElectrolyserRecipeRuntime.DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    ElectrolyserRecipeCategory(RecipeType<ElectrolyserRecipeRuntime.DisplayRecipe> type,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
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
    public void setRecipe(IRecipeLayoutBuilder builder, ElectrolyserRecipeRuntime.DisplayRecipe recipe,
            IFocusGroup focuses) {
        if (recipe.isFluid()) {
            FluidRecipe fluid = recipe.fluid();
            JeiFluidSlots.addFluidSlot(builder, new HbmFluidStack(fluid.input(), fluid.amount(), 0), true, 6, 26);
            JeiFluidSlots.addFluidSlot(builder,
                    new HbmFluidStack(fluid.output1Type(), fluid.output1Amount(), 0), false, 116, 12);
            if (fluid.output2Type() != HbmFluids.NONE && fluid.output2Amount() > 0) {
                JeiFluidSlots.addFluidSlot(builder,
                        new HbmFluidStack(fluid.output2Type(), fluid.output2Amount(), 0), false, 140, 34);
            }
            addByproducts(builder, fluid.byproducts(), 92, 56);
            return;
        }
        MetalRecipe metal = recipe.metal();
        ItemStack input = metal.inputStack();
        if (!input.isEmpty()) {
            builder.addInputSlot(6, 26)
                    .addItemStack(input)
                    .setStandardSlotBackground();
        }
        addByproducts(builder, metal.byproducts(), 92, 56);
    }

    @Override
    public void draw(ElectrolyserRecipeRuntime.DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 74, 28);
        if (!recipe.isFluid()) {
            MetalRecipe metal = recipe.metal();
            guiGraphics.drawString(Minecraft.getInstance().font, materialLine(metal.output1()), 112, 12,
                    0x404040, false);
            if (metal.output2() != null) {
                guiGraphics.drawString(Minecraft.getInstance().font, materialLine(metal.output2()), 112, 24,
                        0x404040, false);
            }
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ElectrolyserRecipeRuntime.DisplayRecipe recipe,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int duration = recipe.isFluid() ? recipe.fluid().duration() : recipe.metal().duration();
        tooltip.add(Component.literal("Duration: " + duration + " ticks"));
        if (!recipe.isFluid()) {
            tooltip.add(materialLine(recipe.metal().output1()));
            if (recipe.metal().output2() != null) {
                tooltip.add(materialLine(recipe.metal().output2()));
            }
        }
    }

    private static void addByproducts(IRecipeLayoutBuilder builder, List<ItemStack> byproducts, int x, int y) {
        for (int i = 0; i < byproducts.size(); i++) {
            builder.addOutputSlot(x + i * 18, y)
                    .addItemStack(byproducts.get(i))
                    .setOutputSlotBackground();
        }
    }

    private static Component materialLine(MaterialStack stack) {
        if (stack == null) {
            return Component.empty();
        }
        return Component.translatable(stack.material.getUnlocalizedName())
                .append(": " + Mats.formatAmount(stack.amount, false));
    }
}