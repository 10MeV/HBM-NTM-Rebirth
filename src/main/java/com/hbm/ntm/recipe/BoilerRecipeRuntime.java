package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingStep;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import java.util.ArrayList;
import java.util.List;

public final class BoilerRecipeRuntime {

    private BoilerRecipeRuntime() {
    }

    public static List<DisplayRecipe> displayRecipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        for (FluidType type : HbmFluids.niceOrder()) {
            HeatableFluidTrait trait = type.getTrait(HeatableFluidTrait.class);
            if (trait == null || trait.getEfficiency(HeatingType.BOILER) <= 0.0D) {
                continue;
            }
            HeatingStep step = trait.getFirstStep();
            if (step == null || step.amountRequired() <= 0 || step.amountProduced() <= 0) {
                continue;
            }
            recipes.add(new DisplayRecipe(
                    new HbmFluidStack(type, step.amountRequired()),
                    new HbmFluidStack(step.producedType(), step.amountProduced()),
                    step.heatRequired(),
                    trait.getEfficiency(HeatingType.BOILER)));
        }
        return List.copyOf(recipes);
    }

    public record DisplayRecipe(HbmFluidStack input, HbmFluidStack output, int heatRequired, double efficiency) {
        public DisplayRecipe {
            input = input == null ? new HbmFluidStack(HbmFluids.NONE, 0) : input;
            output = output == null ? new HbmFluidStack(HbmFluids.NONE, 0) : output;
            heatRequired = Math.max(0, heatRequired);
            efficiency = Math.max(0.0D, efficiency);
        }
    }
}
