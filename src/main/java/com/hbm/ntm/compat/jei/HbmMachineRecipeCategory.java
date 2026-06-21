package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public final class HbmMachineRecipeCategory implements IRecipeCategory<GenericMachineRecipe> {
    private static final int WIDTH = 168;
    private static final int HEIGHT = 108;

    private final RecipeType<GenericMachineRecipe> type;
    private final GenericMachineRecipe.Machine machine;
    private final IDrawable icon;
    private final IDrawableStatic arrow;

    HbmMachineRecipeCategory(RecipeType<GenericMachineRecipe> type, GenericMachineRecipe.Machine machine,
            ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.machine = machine;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<GenericMachineRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return switch (machine) {
            case ASSEMBLY_MACHINE -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_assembly_machine", "Assembly Machine");
            case CHEMICAL_PLANT -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_chemical_plant", "Chemical Plant");
            case PUREX -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_purex", "PUREX");
            case PRECASS -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_precass", "Precision Assembly Machine");
            case ARC_WELDER -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_arc_welder", "Arc Welder");
            case ARC_FURNACE -> Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_arc_furnace", "Electric Arc Furnace");
            case FUSION_REACTOR -> Component.translatableWithFallback("container.fusionTorus", "Fusion Reactor Vessel");
            case PLASMA_FORGE -> Component.translatableWithFallback("container.machinePlasmaForge", "Plasma Forge");
            default -> Component.literal(machine.name());
        };
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
    public void setRecipe(IRecipeLayoutBuilder builder, GenericMachineRecipe recipe, IFocusGroup focuses) {
        int itemInputCount = recipe.getItemInputs().size();
        for (int i = 0; i < itemInputCount; i++) {
            HbmIngredient input = recipe.getItemInputs().get(i);
            builder.addInputSlot(4 + (i % 4) * 18, 4 + (i / 4) * 18)
                    .addItemStacks(input.displayStacks())
                    .setStandardSlotBackground();
        }

        List<HbmFluidStack> fluidInputs = recipe.getFluidInputs();
        for (int i = 0; i < fluidInputs.size(); i++) {
            addFluidSlot(builder, fluidInputs.get(i), true, 4 + i * 18, 62);
        }

        int itemOutputBaseX = 130;
        for (int i = 0; i < recipe.getDisplayItemOutputs().size(); i++) {
            List<ItemStack> stacks = recipe.getDisplayItemOutputs().get(i);
            builder.addOutputSlot(itemOutputBaseX, 4 + i * 18)
                    .addItemStacks(stacks)
                    .setOutputSlotBackground();
        }

        List<HbmFluidStack> fluidOutputs = recipe.getFluidOutputs();
        for (int i = 0; i < fluidOutputs.size(); i++) {
            addFluidSlot(builder, fluidOutputs.get(i), false, itemOutputBaseX + i * 18, 62);
        }
    }

    @Override
    public void draw(GenericMachineRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 80, 36);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, GenericMachineRecipe recipe, IRecipeSlotsView recipeSlotsView,
            double mouseX, double mouseY) {
        if (mouseY >= 82) {
            tooltip.addAll(recipe.getDisplayLines());
        }
    }

    private static void addFluidSlot(IRecipeLayoutBuilder builder, HbmFluidStack hbmStack,
            boolean input, int x, int y) {
        FluidStack forgeStack = HbmFluidForgeMappings.toForge(hbmStack.type(), hbmStack.amount());
        if (forgeStack.isEmpty()) {
            return;
        }
        Fluid fluid = forgeStack.getFluid();
        if (input) {
            builder.addInputSlot(x, y)
                    .addFluidStack(fluid, forgeStack.getAmount())
                    .setFluidRenderer(Math.max(1, forgeStack.getAmount()), false, 16, 16)
                    .setStandardSlotBackground();
        } else {
            builder.addOutputSlot(x, y)
                    .addFluidStack(fluid, forgeStack.getAmount())
                    .setFluidRenderer(Math.max(1, forgeStack.getAmount()), false, 16, 16)
                    .setOutputSlotBackground();
        }
    }
}
