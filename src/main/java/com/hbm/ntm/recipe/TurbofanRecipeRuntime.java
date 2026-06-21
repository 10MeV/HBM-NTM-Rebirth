package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import java.util.List;

public final class TurbofanRecipeRuntime {
    private static final int DISPLAY_AMOUNT = 1_000;

    public static List<DisplayRecipe> displayRecipes() {
        return HbmFluids.niceOrder().stream()
                .map(TurbofanRecipeRuntime::displayRecipe)
                .flatMap(List::stream)
                .toList();
    }

    private static List<DisplayRecipe> displayRecipe(FluidType type) {
        CombustibleFluidTrait combustible = type.getTrait(CombustibleFluidTrait.class);
        if (combustible == null || combustible.getGrade() != CombustibleFluidTrait.FuelGrade.AERO) {
            return List.of();
        }
        return List.of(new DisplayRecipe(new HbmFluidStack(type, DISPLAY_AMOUNT),
                combustible.getCombustionEnergyPerBucket()));
    }

    public record DisplayRecipe(HbmFluidStack fuel, long powerPerBucket) {
        public long powerPerMb() {
            return powerPerBucket / 1_000L;
        }
    }

    private TurbofanRecipeRuntime() {
    }
}
