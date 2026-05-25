package com.hbm.ntm.compat;

import com.hbm.ntm.api.tile.InfoProviderEC;
import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.blockentity.HbmFluidBlockEntity;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.multiblock.MultiblockHelper;
import java.util.ArrayList;
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
    public static final String D_OUTPUT_MB = "outputmb";
    public static final String D_OUTPUT_TU = "outputTU";
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
    public static final String L_DURABILITY = "durability";
    public static final String S_TANK = "tank";
    public static final String S_TANK2 = "tank2";
    public static final String S_TANK3 = "tank3";
    public static final String S_TANK4 = "tank4";
    public static final String S_TANK5 = "tank5";
    @Deprecated public static final String I_PISTONS = "pistons";
    public static final String S_CHUNKRAD = "chunkRad";
    public static final String B_ACTIVE = "active";

    public static boolean isElectricItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof HbmBatteryItem;
    }

    public static double dischargeItem(ItemStack stack, double needed) {
        if (needed <= 0.0D || stack.isEmpty() || !(stack.getItem() instanceof HbmBatteryItem battery)) {
            return 0.0D;
        }
        long requested = Math.min(battery.getDischargeRate(stack), Math.min(battery.getCharge(stack), (long) Math.min(needed, Long.MAX_VALUE)));
        return battery.dischargeBattery(stack, requested);
    }

    public static void getEnergyData(BlockEntity blockEntity, CompoundTag data) {
        data.putString(KEY_EUTYPE, "HE");
        if (blockEntity instanceof HbmEnergyHandler handler) {
            data.putLong(L_ENERGY_HE, handler.getPower());
            data.putLong(L_CAPACITY_HE, handler.getMaxPower());
        }
    }

    public static void getExtraData(BlockEntity blockEntity, CompoundTag data) {
        if (blockEntity instanceof InfoProviderEC provider) {
            provider.provideExtraInfo(data);
        }
    }

    public static int getHeat(BlockEntity blockEntity) {
        BlockEntity resolved = findTileEntity(blockEntity);
        return resolved instanceof HeatSource heatSource ? heatSource.getHeatStored() : -1;
    }

    public static List<Object[]> getAllTanks(BlockEntity blockEntity) {
        BlockEntity resolved = findTileEntity(blockEntity);
        if (!(resolved instanceof HbmFluidBlockEntity fluidBlockEntity)) {
            return null;
        }
        List<Object[]> list = new ArrayList<>();
        for (HbmFluidTank tank : fluidBlockEntity.getAllTanks()) {
            FluidType type = tank.getTankType();
            if (type == HbmFluids.NONE || tank.getFill() <= 0) {
                continue;
            }
            list.add(toFluidInfo(tank));
        }
        return list.isEmpty() ? null : list;
    }

    private static Object[] toFluidInfo(HbmFluidTank tank) {
        return new Object[]{tank.getTankType().getName(), tank.getFill(), tank.getMaxFill()};
    }

    public static BlockEntity findTileEntity(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return null;
        }
        return MultiblockHelper.resolveCoreBlockEntity(blockEntity);
    }

    public static BlockEntity findTileEntity(Level level, BlockPos pos) {
        return level == null || pos == null ? null : findTileEntity(level.getBlockEntity(pos));
    }

    public static ResourceLocation getFluidTexture(String name) {
        FluidType type = HbmFluids.fromName(name);
        return type == null || type == HbmFluids.NONE ? null : type.getTexture();
    }

    private CompatEnergyControl() {
    }
}
