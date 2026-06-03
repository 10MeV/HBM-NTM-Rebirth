package com.hbm.ntm.fluid;

import java.util.Map;
import org.jetbrains.annotations.Nullable;

public final class LegacyOilFluidRecipes {
    private static final Map<FluidType, PairRecipe> CRACKING = Map.ofEntries(
            Map.entry(HbmFluids.OIL, pair(HbmFluids.CRACKOIL, 80, HbmFluids.PETROLEUM, 20)),
            Map.entry(HbmFluids.BITUMEN, pair(HbmFluids.OIL, 80, HbmFluids.AROMATICS, 20)),
            Map.entry(HbmFluids.SMEAR, pair(HbmFluids.NAPHTHA, 60, HbmFluids.PETROLEUM, 40)),
            Map.entry(HbmFluids.GAS, pair(HbmFluids.PETROLEUM, 30, HbmFluids.UNSATURATEDS, 20)),
            Map.entry(HbmFluids.DIESEL, pair(HbmFluids.KEROSENE, 40, HbmFluids.PETROLEUM, 30)),
            Map.entry(HbmFluids.DIESEL_CRACK, pair(HbmFluids.KEROSENE, 40, HbmFluids.PETROLEUM, 30)),
            Map.entry(HbmFluids.KEROSENE, pair(HbmFluids.PETROLEUM, 60, HbmFluids.NONE, 0)),
            Map.entry(HbmFluids.WOODOIL, pair(HbmFluids.HEATINGOIL, 40, HbmFluids.AROMATICS, 10)),
            Map.entry(HbmFluids.XYLENE, pair(HbmFluids.AROMATICS, 80, HbmFluids.PETROLEUM, 20)),
            Map.entry(HbmFluids.HEATINGOIL_VACUUM, pair(HbmFluids.HEATINGOIL, 80, HbmFluids.REFORMGAS, 20)),
            Map.entry(HbmFluids.REFORMATE, pair(HbmFluids.UNSATURATEDS, 40, HbmFluids.REFORMGAS, 60)),
            Map.entry(HbmFluids.BIOGAS, pair(HbmFluids.PETROLEUM, 20, HbmFluids.AROMATICS, 20)));

    private static final Map<FluidType, PairRecipe> FRACTIONING = Map.ofEntries(
            Map.entry(HbmFluids.HEAVYOIL, pair(HbmFluids.BITUMEN, 30, HbmFluids.SMEAR, 70)),
            Map.entry(HbmFluids.HEAVYOIL_VACUUM, pair(HbmFluids.SMEAR, 40, HbmFluids.HEATINGOIL_VACUUM, 60)),
            Map.entry(HbmFluids.SMEAR, pair(HbmFluids.HEATINGOIL, 60, HbmFluids.LUBRICANT, 40)),
            Map.entry(HbmFluids.NAPHTHA, pair(HbmFluids.HEATINGOIL, 40, HbmFluids.DIESEL, 60)),
            Map.entry(HbmFluids.NAPHTHA_DS, pair(HbmFluids.XYLENE, 60, HbmFluids.DIESEL_REFORM, 40)),
            Map.entry(HbmFluids.NAPHTHA_CRACK, pair(HbmFluids.HEATINGOIL, 30, HbmFluids.DIESEL_CRACK, 70)),
            Map.entry(HbmFluids.LIGHTOIL, pair(HbmFluids.DIESEL, 40, HbmFluids.KEROSENE, 60)),
            Map.entry(HbmFluids.LIGHTOIL_DS, pair(HbmFluids.DIESEL_REFORM, 60, HbmFluids.KEROSENE_REFORM, 40)),
            Map.entry(HbmFluids.LIGHTOIL_CRACK, pair(HbmFluids.KEROSENE, 70, HbmFluids.PETROLEUM, 30)),
            Map.entry(HbmFluids.COALOIL, pair(HbmFluids.COALGAS, 30, HbmFluids.OIL, 70)),
            Map.entry(HbmFluids.COALCREOSOTE, pair(HbmFluids.COALOIL, 10, HbmFluids.BITUMEN, 90)),
            Map.entry(HbmFluids.REFORMATE, pair(HbmFluids.AROMATICS, 40, HbmFluids.XYLENE, 60)),
            Map.entry(HbmFluids.LIGHTOIL_VACUUM, pair(HbmFluids.KEROSENE, 70, HbmFluids.REFORMGAS, 30)),
            Map.entry(HbmFluids.EGG, pair(HbmFluids.CHOLESTEROL, 50, HbmFluids.RADIOSOLVENT, 50)),
            Map.entry(HbmFluids.OIL_COKER, pair(HbmFluids.CRACKOIL, 30, HbmFluids.HEATINGOIL, 70)),
            Map.entry(HbmFluids.NAPHTHA_COKER, pair(HbmFluids.NAPHTHA_CRACK, 75, HbmFluids.LIGHTOIL_CRACK, 25)),
            Map.entry(HbmFluids.GAS_COKER, pair(HbmFluids.AROMATICS, 25, HbmFluids.CARBONDIOXIDE, 75)),
            Map.entry(HbmFluids.CHLOROCALCITE_MIX, pair(HbmFluids.CHLOROCALCITE_CLEANED, 50, HbmFluids.COLLOID, 50)),
            Map.entry(HbmFluids.BAUXITE_SOLUTION, pair(HbmFluids.REDMUD, 50, HbmFluids.SODIUM_ALUMINATE, 50)));

    private static final Map<FluidType, TripleRecipe> HYDROTREATING = Map.ofEntries(
            Map.entry(HbmFluids.OIL, triple(stack(HbmFluids.HYDROGEN, 5, 1), stack(HbmFluids.OIL_DS, 90),
                    stack(HbmFluids.SOURGAS, 15))),
            Map.entry(HbmFluids.CRACKOIL, triple(stack(HbmFluids.HYDROGEN, 5, 1), stack(HbmFluids.CRACKOIL_DS, 90),
                    stack(HbmFluids.SOURGAS, 15))),
            Map.entry(HbmFluids.GAS, triple(stack(HbmFluids.HYDROGEN, 5, 1), stack(HbmFluids.PETROLEUM, 80),
                    stack(HbmFluids.SOURGAS, 15))),
            Map.entry(HbmFluids.DIESEL_CRACK, triple(stack(HbmFluids.HYDROGEN, 10, 1), stack(HbmFluids.DIESEL, 80),
                    stack(HbmFluids.SOURGAS, 30))),
            Map.entry(HbmFluids.DIESEL_CRACK_REFORM, triple(stack(HbmFluids.HYDROGEN, 10, 1),
                    stack(HbmFluids.DIESEL_REFORM, 80), stack(HbmFluids.SOURGAS, 30))),
            Map.entry(HbmFluids.COALOIL, triple(stack(HbmFluids.HYDROGEN, 10, 1), stack(HbmFluids.COALGAS, 80),
                    stack(HbmFluids.SOURGAS, 15))));

    private static final Map<FluidType, TripleRecipe> REFORMING = Map.ofEntries(
            Map.entry(HbmFluids.HEATINGOIL, triple(stack(HbmFluids.NAPHTHA, 50), stack(HbmFluids.PETROLEUM, 15),
                    stack(HbmFluids.HYDROGEN, 10))),
            Map.entry(HbmFluids.NAPHTHA, triple(stack(HbmFluids.REFORMATE, 50), stack(HbmFluids.PETROLEUM, 15),
                    stack(HbmFluids.HYDROGEN, 10))),
            Map.entry(HbmFluids.NAPHTHA_CRACK, triple(stack(HbmFluids.REFORMATE, 50), stack(HbmFluids.AROMATICS, 10),
                    stack(HbmFluids.HYDROGEN, 5))),
            Map.entry(HbmFluids.NAPHTHA_COKER, triple(stack(HbmFluids.REFORMATE, 50), stack(HbmFluids.REFORMGAS, 10),
                    stack(HbmFluids.HYDROGEN, 5))),
            Map.entry(HbmFluids.LIGHTOIL, triple(stack(HbmFluids.AROMATICS, 50), stack(HbmFluids.REFORMGAS, 10),
                    stack(HbmFluids.HYDROGEN, 15))),
            Map.entry(HbmFluids.LIGHTOIL_CRACK, triple(stack(HbmFluids.AROMATICS, 50), stack(HbmFluids.REFORMGAS, 5),
                    stack(HbmFluids.HYDROGEN, 20))),
            Map.entry(HbmFluids.PETROLEUM, triple(stack(HbmFluids.UNSATURATEDS, 85), stack(HbmFluids.REFORMGAS, 10),
                    stack(HbmFluids.HYDROGEN, 5))),
            Map.entry(HbmFluids.SOURGAS, triple(stack(HbmFluids.SULFURIC_ACID, 75), stack(HbmFluids.PETROLEUM, 10),
                    stack(HbmFluids.HYDROGEN, 15))),
            Map.entry(HbmFluids.CHOLESTEROL, triple(stack(HbmFluids.ESTRADIOL, 50), stack(HbmFluids.REFORMGAS, 35),
                    stack(HbmFluids.HYDROGEN, 15))));

    private static final Map<FluidType, VacuumRecipe> VACUUM = Map.ofEntries(
            Map.entry(HbmFluids.OIL, vacuum(
                    stack(HbmFluids.HEAVYOIL_VACUUM, 40),
                    stack(HbmFluids.REFORMATE, 25),
                    stack(HbmFluids.LIGHTOIL_VACUUM, 20),
                    stack(HbmFluids.SOURGAS, 15))),
            Map.entry(HbmFluids.OIL_DS, vacuum(
                    stack(HbmFluids.HEAVYOIL_VACUUM, 40),
                    stack(HbmFluids.REFORMATE, 25),
                    stack(HbmFluids.LIGHTOIL_VACUUM, 20),
                    stack(HbmFluids.REFORMGAS, 15))));

    private LegacyOilFluidRecipes() {
    }

    @Nullable
    public static PairRecipe getCracking(FluidType input) {
        return CRACKING.get(input);
    }

    @Nullable
    public static PairRecipe getFractioning(FluidType input) {
        return FRACTIONING.get(input);
    }

    @Nullable
    public static TripleRecipe getHydrotreating(FluidType input) {
        return HYDROTREATING.get(input);
    }

    @Nullable
    public static TripleRecipe getReforming(FluidType input) {
        return REFORMING.get(input);
    }

    @Nullable
    public static VacuumRecipe getVacuum(FluidType input) {
        return VACUUM.get(input);
    }

    private static PairRecipe pair(FluidType leftType, int leftAmount, FluidType rightType, int rightAmount) {
        return new PairRecipe(new HbmFluidStack(leftType, leftAmount), new HbmFluidStack(rightType, rightAmount));
    }

    private static HbmFluidStack stack(FluidType type, int amount) {
        return new HbmFluidStack(type, amount);
    }

    private static HbmFluidStack stack(FluidType type, int amount, int pressure) {
        return new HbmFluidStack(type, amount, pressure);
    }

    private static TripleRecipe triple(HbmFluidStack first, HbmFluidStack second, HbmFluidStack third) {
        return new TripleRecipe(first, second, third);
    }

    private static VacuumRecipe vacuum(HbmFluidStack heavy, HbmFluidStack reformate, HbmFluidStack light,
            HbmFluidStack gas) {
        return new VacuumRecipe(heavy, reformate, light, gas);
    }

    public record PairRecipe(HbmFluidStack left, HbmFluidStack right) {
    }

    public record TripleRecipe(HbmFluidStack first, HbmFluidStack second, HbmFluidStack third) {
    }

    public record VacuumRecipe(HbmFluidStack heavyOil, HbmFluidStack reformate, HbmFluidStack lightOil,
            HbmFluidStack gas) {
        public HbmFluidStack[] outputs() {
            return new HbmFluidStack[] { heavyOil, reformate, lightOil, gas };
        }
    }
}
