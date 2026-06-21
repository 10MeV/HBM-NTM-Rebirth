package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.BedrockOreItem;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreGrade;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreType;
import com.hbm.ntm.registry.ModItems;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class OreSlopperRecipeRuntime {
    public static final int WATER_USED = 1_000;
    public static final int SLOP_PRODUCED = 1_000;
    public static final long BASE_CONSUMPTION = 200L;

    private static final DisplayRecipe DISPLAY = new DisplayRecipe(
            new ItemStack(ModItems.BEDROCK_ORE_BASE.get()),
            new HbmFluidStack(HbmFluids.WATER, WATER_USED, 0),
            new HbmFluidStack(HbmFluids.SLOP, SLOP_PRODUCED, 0),
            Arrays.stream(BedrockOreType.values())
                    .map(type -> BedrockOreItem.make(BedrockOreGrade.BASE, type))
                    .toList(),
            BASE_CONSUMPTION);

    private OreSlopperRecipeRuntime() {
    }

    public static List<DisplayRecipe> displayRecipes() {
        return List.of(DISPLAY);
    }

    public record DisplayRecipe(ItemStack input, HbmFluidStack water, HbmFluidStack slop,
                                List<ItemStack> possibleOutputs, long baseConsumption) {
        public DisplayRecipe {
            input = input == null ? ItemStack.EMPTY : input.copy();
            possibleOutputs = possibleOutputs == null ? List.of()
                    : possibleOutputs.stream().map(ItemStack::copy).toList();
        }
    }
}