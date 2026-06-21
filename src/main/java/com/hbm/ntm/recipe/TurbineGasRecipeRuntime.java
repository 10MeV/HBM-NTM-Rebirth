package com.hbm.ntm.recipe;

import com.hbm.ntm.blockentity.TurbineGasBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import java.util.List;

public final class TurbineGasRecipeRuntime {
    private static final int DISPLAY_AMOUNT = 1_000;

    public static List<DisplayRecipe> displayRecipes() {
        return HbmFluids.niceOrder().stream()
                .map(TurbineGasRecipeRuntime::displayRecipe)
                .flatMap(List::stream)
                .toList();
    }

    private static List<DisplayRecipe> displayRecipe(FluidType type) {
        CombustibleFluidTrait combustible = type.getTrait(CombustibleFluidTrait.class);
        if (combustible == null || combustible.getGrade() != CombustibleFluidTrait.FuelGrade.GAS) {
            return List.of();
        }
        return List.of(new DisplayRecipe(new HbmFluidStack(type, DISPLAY_AMOUNT),
                combustible.getCombustionEnergyPerBucket(),
                TurbineGasBlockEntity.maxFuelConsumption(type),
                fluidBurnTemp(type)));
    }

    private static int fluidBurnTemp(FluidType type) {
        CombustibleFluidTrait combustible = type.getTrait(CombustibleFluidTrait.class);
        double fuel = combustible == null ? 0.0D : combustible.getCombustionEnergyPerBucket();
        return (int) Math.floor(800.0D - Math.pow(Math.E, -fuel / 100_000.0D) * 300.0D);
    }

    public record DisplayRecipe(HbmFluidStack fuel, long powerPerBucket, double maxConsumptionMbPerTick,
            int maxBurnTemperature) {
        public long powerPerMb() {
            return powerPerBucket / 1_000L;
        }
    }

    private TurbineGasRecipeRuntime() {
    }
}
