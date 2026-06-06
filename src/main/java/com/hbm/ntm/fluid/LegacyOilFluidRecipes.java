package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    private static final Map<FluidType, RefineryRecipe> REFINERY = linkedMap(List.of(
            entry(HbmFluids.HOTOIL, refinery(
                    stack(HbmFluids.HEAVYOIL, 50),
                    stack(HbmFluids.NAPHTHA, 25),
                    stack(HbmFluids.LIGHTOIL, 15),
                    stack(HbmFluids.PETROLEUM, 10),
                    item("sulfur"))),
            entry(HbmFluids.HOTCRACKOIL, refinery(
                    stack(HbmFluids.NAPHTHA_CRACK, 40),
                    stack(HbmFluids.LIGHTOIL_CRACK, 30),
                    stack(HbmFluids.AROMATICS, 15),
                    stack(HbmFluids.UNSATURATEDS, 15),
                    item("oil_tar_crack"))),
            entry(HbmFluids.HOTOIL_DS, refinery(
                    stack(HbmFluids.HEAVYOIL, 30),
                    stack(HbmFluids.NAPHTHA_DS, 35),
                    stack(HbmFluids.LIGHTOIL_DS, 20),
                    stack(HbmFluids.UNSATURATEDS, 15),
                    item("oil_tar_paraffin"))),
            entry(HbmFluids.HOTCRACKOIL_DS, refinery(
                    stack(HbmFluids.NAPHTHA_DS, 35),
                    stack(HbmFluids.LIGHTOIL_DS, 35),
                    stack(HbmFluids.AROMATICS, 15),
                    stack(HbmFluids.UNSATURATEDS, 15),
                    item("oil_tar_paraffin")))));

    private static Map<FluidType, SolidificationRecipe> solidification;
    private static Map<FluidType, CokerRecipe> coking;

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

    @Nullable
    public static RefineryRecipe getRefinery(FluidType input) {
        return REFINERY.get(input);
    }

    @Nullable
    public static SolidificationRecipe getSolidification(FluidType input) {
        return solidification().get(input);
    }

    @Nullable
    public static CokerRecipe getCoking(FluidType input) {
        return coking().get(input);
    }

    public static List<Map.Entry<FluidType, RefineryRecipe>> refineryRecipes() {
        return List.copyOf(REFINERY.entrySet());
    }

    public static List<Map.Entry<FluidType, PairRecipe>> crackingRecipes() {
        return List.copyOf(CRACKING.entrySet());
    }

    public static List<Map.Entry<FluidType, PairRecipe>> fractioningRecipes() {
        return List.copyOf(FRACTIONING.entrySet());
    }

    public static List<Map.Entry<FluidType, TripleRecipe>> hydrotreatingRecipes() {
        return List.copyOf(HYDROTREATING.entrySet());
    }

    public static List<Map.Entry<FluidType, TripleRecipe>> reformingRecipes() {
        return List.copyOf(REFORMING.entrySet());
    }

    public static List<Map.Entry<FluidType, VacuumRecipe>> vacuumRecipes() {
        return List.copyOf(VACUUM.entrySet());
    }

    public static List<Map.Entry<FluidType, SolidificationRecipe>> solidificationRecipes() {
        return List.copyOf(solidification().entrySet());
    }

    public static List<Map.Entry<FluidType, CokerRecipe>> cokingRecipes() {
        return List.copyOf(coking().entrySet());
    }

    private static Map<FluidType, SolidificationRecipe> solidification() {
        if (solidification == null) {
            solidification = buildSolidificationRecipes();
        }
        return solidification;
    }

    private static Map<FluidType, CokerRecipe> coking() {
        if (coking == null) {
            coking = buildCokingRecipes();
        }
        return coking;
    }

    private static Map<FluidType, SolidificationRecipe> buildSolidificationRecipes() {
        LinkedHashMap<FluidType, SolidificationRecipe> recipes = new LinkedHashMap<>();
        putSolid(recipes, HbmFluids.WATER, 1_000, () -> new ItemStack(Items.ICE));
        putSolid(recipes, HbmFluids.LAVA, 1_000, () -> new ItemStack(Items.OBSIDIAN));
        putSolid(recipes, HbmFluids.MERCURY, 125, item("ingot_mercury"));
        putSolid(recipes, HbmFluids.BIOGAS, 250, item("biomass_compressed", 4));
        putSolid(recipes, HbmFluids.SALIENT, 1_280, item("bio_wafer", 8));
        putSolid(recipes, HbmFluids.ENDERJUICE, 100, () -> new ItemStack(Items.ENDER_PEARL));
        putSolid(recipes, HbmFluids.WATZ, 1_000, item("ingot_mud"));
        putSolid(recipes, HbmFluids.REDMUD, 450, () -> new ItemStack(Items.IRON_INGOT));
        putSolid(recipes, HbmFluids.SODIUM, 100, item("powder_sodium"));
        putSolid(recipes, HbmFluids.LEAD, 100, item("ingot_lead"));
        putSolid(recipes, HbmFluids.SLOP, 250, () -> new ItemStack(ModBlocks.legacyBlock("ore_oil_sand").get()));
        putSolid(recipes, HbmFluids.OIL, 200, item("oil_tar_crude"));
        putSolid(recipes, HbmFluids.CRACKOIL, 200, item("oil_tar_crack"));
        putSolid(recipes, HbmFluids.COALOIL, 200, item("oil_tar_coal"));
        putSolid(recipes, HbmFluids.HEAVYOIL, 150, item("oil_tar_crude"));
        putSolid(recipes, HbmFluids.HEAVYOIL_VACUUM, 150, item("oil_tar_crude"));
        putSolid(recipes, HbmFluids.BITUMEN, 100, item("oil_tar_crude"));
        putSolid(recipes, HbmFluids.COALCREOSOTE, 200, item("oil_tar_coal"));
        putSolid(recipes, HbmFluids.WOODOIL, 1_000, item("oil_tar_wood"));
        putSolid(recipes, HbmFluids.LUBRICANT, 100, item("oil_tar_paraffin"));
        putSolid(recipes, HbmFluids.BALEFIRE, 250, item("solid_fuel_bf"));

        putSolidAuto(recipes, HbmFluids.SMEAR);
        putSolidAuto(recipes, HbmFluids.HEATINGOIL);
        putSolidAuto(recipes, HbmFluids.HEATINGOIL_VACUUM);
        putSolidAuto(recipes, HbmFluids.RECLAIMED);
        putSolidAuto(recipes, HbmFluids.PETROIL);
        putSolidAuto(recipes, HbmFluids.NAPHTHA);
        putSolidAuto(recipes, HbmFluids.NAPHTHA_CRACK);
        putSolidAuto(recipes, HbmFluids.DIESEL);
        putSolidAuto(recipes, HbmFluids.DIESEL_REFORM);
        putSolidAuto(recipes, HbmFluids.DIESEL_CRACK);
        putSolidAuto(recipes, HbmFluids.DIESEL_CRACK_REFORM);
        putSolidAuto(recipes, HbmFluids.LIGHTOIL);
        putSolidAuto(recipes, HbmFluids.LIGHTOIL_CRACK);
        putSolidAuto(recipes, HbmFluids.LIGHTOIL_VACUUM);
        putSolidAuto(recipes, HbmFluids.KEROSENE);
        putSolidAuto(recipes, HbmFluids.KEROSENE_REFORM);
        putSolidAuto(recipes, HbmFluids.SOURGAS);
        putSolidAuto(recipes, HbmFluids.REFORMGAS);
        putSolidAuto(recipes, HbmFluids.SYNGAS);
        putSolidAuto(recipes, HbmFluids.PETROLEUM);
        putSolidAuto(recipes, HbmFluids.LPG);
        putSolidAuto(recipes, HbmFluids.BIOFUEL);
        putSolidAuto(recipes, HbmFluids.AROMATICS);
        putSolidAuto(recipes, HbmFluids.UNSATURATEDS);
        putSolidAuto(recipes, HbmFluids.REFORMATE);
        putSolidAuto(recipes, HbmFluids.XYLENE);
        putSolidAuto(recipes, HbmFluids.BALEFIRE, 24_000_000L, item("solid_fuel_bf"));
        return Collections.unmodifiableMap(recipes);
    }

    private static Map<FluidType, CokerRecipe> buildCokingRecipes() {
        LinkedHashMap<FluidType, CokerRecipe> recipes = new LinkedHashMap<>();
        putCokerAuto(recipes, HbmFluids.HEAVYOIL, HbmFluids.OIL_COKER);
        putCokerAuto(recipes, HbmFluids.HEAVYOIL_VACUUM, HbmFluids.REFORMATE);
        putCokerAuto(recipes, HbmFluids.COALCREOSOTE, HbmFluids.NAPHTHA_COKER);
        putCokerAuto(recipes, HbmFluids.SMEAR, HbmFluids.OIL_COKER);
        putCokerAuto(recipes, HbmFluids.HEATINGOIL, HbmFluids.OIL_COKER);
        putCokerAuto(recipes, HbmFluids.HEATINGOIL_VACUUM, HbmFluids.OIL_COKER);
        putCokerAuto(recipes, HbmFluids.RECLAIMED, HbmFluids.NAPHTHA_COKER);
        putCokerAuto(recipes, HbmFluids.NAPHTHA, HbmFluids.NAPHTHA_COKER);
        putCokerAuto(recipes, HbmFluids.NAPHTHA_DS, HbmFluids.NAPHTHA_COKER);
        putCokerAuto(recipes, HbmFluids.NAPHTHA_CRACK, HbmFluids.NAPHTHA_COKER);
        putCokerAuto(recipes, HbmFluids.DIESEL, HbmFluids.NAPHTHA_COKER);
        putCokerAuto(recipes, HbmFluids.DIESEL_REFORM, HbmFluids.NAPHTHA_COKER);
        putCokerAuto(recipes, HbmFluids.DIESEL_CRACK, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.DIESEL_CRACK_REFORM, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.LIGHTOIL, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.LIGHTOIL_DS, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.LIGHTOIL_CRACK, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.LIGHTOIL_VACUUM, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.BIOFUEL, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.AROMATICS, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.REFORMATE, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.XYLENE, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.FISHOIL, HbmFluids.MERCURY);
        putCokerAuto(recipes, HbmFluids.SUNFLOWEROIL, HbmFluids.GAS_COKER);
        putCokerAuto(recipes, HbmFluids.WOODOIL, 340_000L, () -> new ItemStack(Items.CHARCOAL), HbmFluids.GAS_COKER);
        putCoker(recipes, HbmFluids.WATZ, 4_000, item("ingot_mud", 4), null);
        putCoker(recipes, HbmFluids.REDMUD, 450, () -> new ItemStack(Items.IRON_INGOT), stack(HbmFluids.MERCURY, 50));
        putCoker(recipes, HbmFluids.BITUMEN, 16_000, item("coke_petroleum"), stack(HbmFluids.OIL_COKER, 1_600));
        putCoker(recipes, HbmFluids.LUBRICANT, 12_000, item("coke_petroleum"), stack(HbmFluids.OIL_COKER, 1_200));
        putCoker(recipes, HbmFluids.CALCIUM_SOLUTION, 125, item("powder_calcium"), stack(HbmFluids.SPENTSTEAM, 100));
        putCoker(recipes, HbmFluids.SOURGAS, 1_000, item("sulfur"), stack(HbmFluids.GAS_COKER, 150));
        putCoker(recipes, HbmFluids.SLOP, 1_000, item("powder_limestone"), stack(HbmFluids.COLLOID, 250));
        putCoker(recipes, HbmFluids.VITRIOL, 4_000, item("powder_iron"), stack(HbmFluids.SULFURIC_ACID, 500));
        return Collections.unmodifiableMap(recipes);
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

    private static RefineryRecipe refinery(HbmFluidStack heavy, HbmFluidStack naphtha, HbmFluidStack light,
            HbmFluidStack petroleum, Supplier<ItemStack> solid) {
        return new RefineryRecipe(heavy, naphtha, light, petroleum, solid);
    }

    private static Supplier<ItemStack> item(String legacyName) {
        return item(legacyName, 1);
    }

    private static Supplier<ItemStack> item(String legacyName, int count) {
        return () -> {
            var item = ModItems.legacyItem(legacyName);
            return item == null ? ItemStack.EMPTY : new ItemStack(item.get(), Math.max(1, count));
        };
    }

    private static <T> Map.Entry<FluidType, T> entry(FluidType type, T recipe) {
        return Map.entry(type, recipe);
    }

    private static <T> Map<FluidType, T> linkedMap(List<Map.Entry<FluidType, T>> entries) {
        LinkedHashMap<FluidType, T> map = new LinkedHashMap<>();
        for (Map.Entry<FluidType, T> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(map);
    }

    private static void putSolid(Map<FluidType, SolidificationRecipe> recipes, FluidType input, int amount,
            Supplier<ItemStack> output) {
        recipes.put(input, new SolidificationRecipe(amount, output));
    }

    private static void putSolidAuto(Map<FluidType, SolidificationRecipe> recipes, FluidType input) {
        putSolidAuto(recipes, input, 1_440_000L, item("solid_fuel"));
    }

    private static void putSolidAuto(Map<FluidType, SolidificationRecipe> recipes, FluidType input, long tuPerFuel,
            Supplier<ItemStack> output) {
        long heat = heatEnergy(input);
        if (heat <= 0L) {
            return;
        }
        int amount = autoAmount(tuPerFuel, heat, 1.25D, 1);
        putSolid(recipes, input, amount, output);
    }

    private static void putCoker(Map<FluidType, CokerRecipe> recipes, FluidType input, int amount,
            Supplier<ItemStack> output, @Nullable HbmFluidStack byproduct) {
        recipes.put(input, new CokerRecipe(amount, output, byproduct));
    }

    private static void putCokerAuto(Map<FluidType, CokerRecipe> recipes, FluidType input, FluidType byproductType) {
        putCokerAuto(recipes, input, 820_000L, item("coke_petroleum"), byproductType);
    }

    private static void putCokerAuto(Map<FluidType, CokerRecipe> recipes, FluidType input, long tuPerFuel,
            Supplier<ItemStack> output, @Nullable FluidType byproductType) {
        long heat = Math.max(heatEnergy(input), combustionEnergy(input));
        if (heat <= 0L) {
            return;
        }
        int amount = autoAmount(tuPerFuel, heat, 1.0D, 0);
        HbmFluidStack byproduct = byproductType == null ? null : stack(byproductType, Math.max(10, amount / 10));
        putCoker(recipes, input, amount, output, byproduct);
    }

    private static int autoAmount(long tuPerFuel, long heatPerBucket, double penalty, int min) {
        int amount = (int) (tuPerFuel * 1_000L * penalty / heatPerBucket);
        if (amount > 10_000) {
            amount -= amount % 1_000;
        } else if (amount > 1_000) {
            amount -= amount % 100;
        } else if (amount > 100) {
            amount -= amount % 10;
        }
        return Math.max(amount, min);
    }

    private static long heatEnergy(FluidType type) {
        FlammableFluidTrait trait = type.getTrait(FlammableFluidTrait.class);
        return trait == null ? 0L : trait.getHeatEnergyPerBucket();
    }

    private static long combustionEnergy(FluidType type) {
        CombustibleFluidTrait trait = type.getTrait(CombustibleFluidTrait.class);
        return trait == null ? 0L : trait.getCombustionEnergyPerBucket();
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

    public record RefineryRecipe(HbmFluidStack heavyOil, HbmFluidStack naphtha, HbmFluidStack lightOil,
            HbmFluidStack petroleum, Supplier<ItemStack> solid) {
        public HbmFluidStack[] outputs() {
            return new HbmFluidStack[] { heavyOil, naphtha, lightOil, petroleum };
        }

        public ItemStack solidStack() {
            return solid.get().copy();
        }
    }

    public record SolidificationRecipe(int inputAmount, Supplier<ItemStack> output) {
        public ItemStack outputStack() {
            return output.get().copy();
        }
    }

    public record CokerRecipe(int inputAmount, Supplier<ItemStack> output, @Nullable HbmFluidStack byproduct) {
        public ItemStack outputStack() {
            return output.get().copy();
        }
    }
}
