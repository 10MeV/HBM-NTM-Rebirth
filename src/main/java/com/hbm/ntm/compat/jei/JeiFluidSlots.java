package com.hbm.ntm.compat.jei;

import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluidStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

final class JeiFluidSlots {
    static void addFluidSlot(IRecipeLayoutBuilder builder, HbmFluidStack hbmStack, boolean input, int x, int y) {
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

    private JeiFluidSlots() {
    }
}
