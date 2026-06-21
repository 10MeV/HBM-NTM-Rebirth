package com.hbm.ntm.compat.jei;

import com.hbm.ntm.blockentity.GasCentBlockEntity;
import com.hbm.ntm.fluid.HbmFluidStack;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public record GasCentJeiRecipe(HbmFluidStack inputFluid, List<ItemStack> outputs,
        boolean highSpeed, int centrifuges, GasCentBlockEntity.PseudoFluidType inputType,
        GasCentBlockEntity.PseudoFluidType outputType) {
    public GasCentJeiRecipe {
        outputs = outputs == null ? List.of() : outputs.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }
}
