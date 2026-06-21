package com.hbm.ntm.compat.jei;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record SawmillJeiRecipe(List<ItemStack> inputs, ItemStack output,
        @Nullable ItemStack bonusOutput, @Nullable String bonusChance) {
    public SawmillJeiRecipe {
        inputs = inputs == null ? List.of() : inputs.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
        output = output == null ? ItemStack.EMPTY : output.copy();
        bonusOutput = bonusOutput == null ? null : bonusOutput.copy();
    }
}
