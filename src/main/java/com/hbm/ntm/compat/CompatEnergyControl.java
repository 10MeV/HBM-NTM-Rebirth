package com.hbm.ntm.compat;

import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.api.tile.InfoProviderEC;
import com.hbm.ntm.energy.HbmChargeableItem;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class CompatEnergyControl {
    public static final String KEY_EUTYPE = "euType";

    public static final String L_ENERGY_HE = "energy";
    public static final String L_ENERGY_TU = "energyTU";
    public static final String L_ENERGY_ = "energy_";
    public static final String L_CAPACITY_HE = "capacity";
    public static final String L_CAPACITY_TU = "capacityTU";
    public static final String L_CAPACITY_ = "capacity_";
    public static final String D_CONSUMPTION_HE = "consumptionHE";
    public static final String D_CONSUMPTION_MB = "consumption";
    @Deprecated public static final String S_CONSUMPTION_ = "consumption_";
    public static final String D_OUTPUT_HE = "output";
    public static final String D_OUTPUT_TARGET_HE = "target";
    public static final String D_OUTPUT_MB = "outputmb";
    public static final String D_OUTPUT_TU = "outputTU";
    public static final String D_EFFICIENCY = "efficiency";
    public static final String I_THROTTLE = "throttle";
    public static final String I_STATE = "state";
    public static final String B_AUTO_MODE = "autoMode";
    public static final String L_DIFF_HE = "diff";
    @Deprecated public static final String I_TEMP_K = "temp";
    public static final String D_TURBINE_PERCENT = "turbine";
    public static final String I_TURBINE_SPEED = "speed";
    public static final String L_COREHEAT_C = "core";
    public static final String L_HULLHEAT_C = "hull";
    public static final String S_LEVEL_PERCENT = "level";
    @Deprecated public static final String L_HEATL = "heatL";
    public static final String D_HEAT_C = "heat";
    public static final String D_MAXHEAT_C = "maxHeat";
    public static final String L_PRESSURE_BAR = "bar";
    public static final String L_FUEL = "fuel";
    @Deprecated public static final String S_FUELTEXT = "fuelText";
    @Deprecated public static final String S_DEPLETED = "depleted";
    public static final String D_DEPLETION_PERCENT = "depletion";
    public static final String D_XENON_PERCENT = "xenon";
    public static final String D_SKIN_C = "skin";
    public static final String D_CORE_C = "c_heat";
    public static final String D_MELT_C = "melt";
    public static final String I_PROGRESS = "progress";
    public static final String I_FLUX = "flux";
    public static final String I_WATER = "water";
    public static final String S_PWR_COOLANT = "pwrCoolant";
    public static final String S_PWR_HOT_COOLANT = "pwrHotCoolant";
    public static final String L_PLASMA_TU = "plasma";
    public static final String L_KLYSTRON_TU = "klystron";
    public static final String D_NEUTRON_FLUX = "neutrons";
    public static final String D_FUSION_CONSUMPTION_PERCENT = "fusionConsumption";
    public static final String D_BONUS_PERCENT = "bonus";
    public static final String S_FUSION_RECIPE_TANK_PREFIX = "fusionRecipeTank";
    public static final String S_FUSION_COOLANT = "fusionCoolant";
    public static final String S_FUSION_HOT_COOLANT = "fusionHotCoolant";
    public static final String S_FUSION_WATER = "fusionWater";
    public static final String S_FUSION_STEAM = "fusionSteam";
    public static final String S_FUSION_INPUT = "fusionInput";
    public static final String S_FUSION_OUTPUT = "fusionOutput";
    public static final String S_FUSION_AIR = "fusionAir";
    public static final String S_FUSION_COLD_COOLANT = "fusionColdCoolant";
    public static final String D_FUSION_PROGRESS_RAW = "fusionProgress";
    public static final String D_FUSION_BONUS_RAW = "fusionBonus";
    public static final String D_FUSION_CONSUMPTION_RAW = "fusionConsumptionRaw";
    public static final String S_FUSION_INPUT_ITEM = "fusionInputItem";
    public static final String I_FUSION_INPUT_COUNT = "fusionInputCount";
    public static final String S_FUSION_OUTPUT_ITEM = "fusionOutputItem";
    public static final String I_FUSION_OUTPUT_COUNT = "fusionOutputCount";
    public static final String S_ZIRNOX_WATER = "zirnoxWater";
    public static final String S_ZIRNOX_STEAM = "zirnoxSteam";
    public static final String S_ZIRNOX_CO2 = "zirnoxCarbonDioxide";
    public static final String L_ICF_HEATING_RATE_TU = "icfHeatingRate";
    public static final String L_ICF_LASER_TU = "icfLaser";
    public static final String L_ICF_MAX_LASER_TU = "icfMaxLaser";
    public static final String S_ICF_COOLANT = "icfCoolant";
    public static final String S_ICF_HOT_COOLANT = "icfHotCoolant";
    public static final String S_ICF_STELLAR_FLUX = "icfStellarFlux";
    public static final String L_ICF_PELLET_DEPLETION = "icfPelletDepletion";
    public static final String L_ICF_PELLET_MAX_DEPLETION = "icfPelletMaxDepletion";
    public static final String L_ICF_PELLET_FUSING_DIFFICULTY = "icfPelletFusingDifficulty";
    public static final String S_ICF_PELLET_PRIMARY = "icfPelletPrimary";
    public static final String S_ICF_PELLET_SECONDARY = "icfPelletSecondary";
    public static final String I_RESEARCH_HEAT_RAW = "researchHeat";
    public static final String D_RESEARCH_ROD_LEVEL_RAW = "researchRodLevel";
    public static final String D_RESEARCH_ROD_TARGET_RAW = "researchRodTarget";
    public static final String I_BREEDING_FLUX_RAW = "breedingFlux";
    public static final String D_BREEDING_PROGRESS_RAW = "breedingProgress";
    public static final String S_RBMK_BOILER_FEED = "rbmkBoilerFeed";
    public static final String S_RBMK_BOILER_STEAM = "rbmkBoilerSteam";
    public static final String S_RBMK_HEATER_FEED = "rbmkHeaterFeed";
    public static final String S_RBMK_HEATER_OUTPUT = "rbmkHeaterOutput";
    public static final String S_RBMK_COOLER_COLD = "rbmkCoolerCold";
    public static final String S_RBMK_COOLER_WARM = "rbmkCoolerWarm";
    public static final String S_RBMK_OUTGASSER_GAS = "rbmkOutgasserGas";
    public static final String I_LASER_LENGTH = "laserLength";
    public static final String I_CELL_COUNT = "cells";
    public static final String I_EMITTER_COUNT = "emitters";
    public static final String I_CAPACITOR_COUNT = "capacitors";
    public static final String I_TURBOCHARGER_COUNT = "turbochargers";
    public static final String I_MUON = "muon";
    public static final String D_ROD_LEVEL_PERCENT = "rodLevel";
    public static final String D_ROD_TARGET_PERCENT = "rodTarget";
    public static final String I_FUEL_AMOUNT = "fuelAmount";
    public static final String D_PROCESS_PROGRESS = "process";
    public static final String D_PROCESS_TIME = "processTime";
    public static final String L_CORE_CAPACITY_C = "coreCapacity";
    public static final String L_HULL_CAPACITY_C = "hullCapacity";
    public static final String L_DURABILITY = "durability";
    public static final String S_TANK = "tank";
    public static final String S_TANK2 = "tank2";
    public static final String S_TANK3 = "tank3";
    public static final String S_TANK4 = "tank4";
    public static final String S_TANK5 = "tank5";
    public static final String S_TURBINE_INPUT = "turbineInput";
    public static final String S_TURBINE_OUTPUT = "turbineOutput";
    public static final String S_STEAM_ENGINE_STEAM = "steamEngineSteam";
    public static final String S_STEAM_ENGINE_SPENT = "steamEngineSpent";
    public static final String S_COMBUSTION_FUEL = "combustionFuel";
    public static final String S_DIESEL_FUEL = "dieselFuel";
    public static final String S_GAS_CENT_FEED = "gasCentFeed";
    public static final String S_GAS_FLARE_FUEL = "gasFlareFuel";
    public static final String S_LIQUEFACTOR_OUTPUT = "liquefactorOutput";
    public static final String S_SOLIDIFIER_INPUT = "solidifierInput";
    public static final String S_RADIOLYSIS_INPUT = "radiolysisInput";
    public static final String S_RADIOLYSIS_OUTPUT_1 = "radiolysisOutput1";
    public static final String S_RADIOLYSIS_OUTPUT_2 = "radiolysisOutput2";
    public static final String S_GAS_TURBINE_FUEL = "gasTurbineFuel";
    public static final String S_GAS_TURBINE_LUBRICANT = "gasTurbineLubricant";
    public static final String S_GAS_TURBINE_WATER = "gasTurbineWater";
    public static final String S_GAS_TURBINE_STEAM = "gasTurbineSteam";
    @Deprecated public static final String I_PISTONS = "pistons";
    public static final String S_CHUNKRAD = "chunkRad";
    public static final String B_ACTIVE = "active";

    public static boolean isElectricItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof HbmChargeableItem;
    }

    public static double dischargeItem(ItemStack stack, double needed) {
        if (needed <= 0.0D || stack.isEmpty() || !(stack.getItem() instanceof HbmChargeableItem battery)) {
            return 0.0D;
        }
        long requested = Math.min(battery.getDischargeRate(stack),
                Math.min(battery.getCharge(stack), (long) Math.min(needed, Long.MAX_VALUE)));
        return battery.dischargeBattery(stack, requested);
    }

    public static void putTankAmountInfo(CompoundTag data, String key, HbmFluidTank tank) {
        data.putInt(key, tank.getFill());
        data.putInt(key + "Max", tank.getMaxFill());
    }

    public static void putTypedTankInfo(CompoundTag data, String key, HbmFluidTank tank) {
        putTankAmountInfo(data, key, tank);
        data.putString(key + "Type", tank.getTankType().getUnlocalizedName());
    }

    public static void getEnergyData(BlockEntity blockEntity, CompoundTag data) {
        data.putString(KEY_EUTYPE, "HE");
        BlockEntity resolved = findTileEntity(blockEntity);
        if (resolved instanceof HbmEnergyHandler handler) {
            handler.provideInfoForEnergyControl(data);
        }
    }

    public static void getExtraData(BlockEntity blockEntity, CompoundTag data) {
        BlockEntity resolved = findTileEntity(blockEntity);
        if (resolved instanceof InfoProviderEC provider) {
            provider.provideExtraInfo(data);
        }
    }

    public static int getHeat(BlockEntity blockEntity) {
        BlockEntity resolved = findTileEntity(blockEntity);
        return resolved instanceof HeatSource heatSource ? heatSource.getHeatStored() : -1;
    }

    /**
     * Returns legacy EnergyControl tank rows: [fluidName, fill, capacity].
     */
    public static List<Object[]> getAllTanks(BlockEntity blockEntity) {
        List<Object[]> list = CompatExternal.getAllTanks(blockEntity).stream()
                .filter(tank -> tank.getTankType() != HbmFluids.NONE)
                .filter(tank -> tank.getFill() > 0)
                .map(CompatEnergyControl::toFluidInfo)
                .toList();
        return list.isEmpty() ? null : list;
    }

    /**
     * Returns all EnergyControl tank rows, including empty tanks.
     */
    public static List<Object[]> getAllTanks(BlockEntity blockEntity, boolean includeEmpty) {
        if (!includeEmpty) {
            return getAllTanks(blockEntity);
        }
        List<Object[]> list = CompatExternal.getAllTanks(blockEntity).stream()
                .filter(tank -> tank.getTankType() != HbmFluids.NONE)
                .map(CompatEnergyControl::toFluidInfo)
                .toList();
        return list.isEmpty() ? null : list;
    }

    /**
     * Returns legacy CompatExternal fluid rows: [fluidName, fluidId, color, fill, capacity].
     */
    public static List<Object[]> getExternalFluidInfo(BlockEntity blockEntity, boolean includeEmpty) {
        List<Object[]> list = CompatExternal.getFluidInfoFromTile(blockEntity).stream()
                .filter(row -> includeEmpty || (row.length >= 4 && row[3] instanceof Integer fill && fill > 0))
                .toList();
        return list.isEmpty() ? null : list;
    }

    public static boolean hasEnergy(BlockEntity blockEntity) {
        return findTileEntity(blockEntity) instanceof HbmEnergyHandler;
    }

    public static boolean hasTanks(BlockEntity blockEntity) {
        return !CompatExternal.getAllTanks(blockEntity).isEmpty();
    }

    public static long getBufferedPower(BlockEntity blockEntity) {
        return CompatExternal.getBufferedPowerFromTile(blockEntity);
    }

    public static long getMaxPower(BlockEntity blockEntity) {
        return CompatExternal.getMaxPowerFromTile(blockEntity);
    }

    public static int getEnergyPriority(BlockEntity blockEntity) {
        return CompatExternal.getEnergyPriorityFromTile(blockEntity);
    }

    private static Object[] toFluidInfo(HbmFluidTank tank) {
        FluidType type = tank.getTankType();
        return new Object[]{type.getName(), tank.getFill(), tank.getMaxFill()};
    }

    public static BlockEntity findTileEntity(BlockEntity blockEntity) {
        return CompatExternal.getOperationalCoreFromTile(blockEntity);
    }

    public static BlockEntity findTileEntity(Level level, BlockPos pos) {
        return CompatExternal.getOperationalCoreFromPos(level, pos);
    }

    public static ResourceLocation getFluidTexture(String name) {
        FluidType type = HbmFluidJsonUtil.readFluidReference(name);
        return type == null || type == HbmFluids.NONE ? null : type.getTexture();
    }

    private CompatEnergyControl() {
    }
}
