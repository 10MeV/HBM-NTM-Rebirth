package com.hbm.ntm.util;

import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy-name facade for the EnergyControl data bridge.
 */
public final class CompatEnergyControl {
    public static final String KEY_EUTYPE = com.hbm.ntm.compat.CompatEnergyControl.KEY_EUTYPE;

    public static final String L_ENERGY_HE = com.hbm.ntm.compat.CompatEnergyControl.L_ENERGY_HE;
    public static final String L_ENERGY_TU = com.hbm.ntm.compat.CompatEnergyControl.L_ENERGY_TU;
    public static final String L_ENERGY_ = com.hbm.ntm.compat.CompatEnergyControl.L_ENERGY_;
    public static final String L_CAPACITY_HE = com.hbm.ntm.compat.CompatEnergyControl.L_CAPACITY_HE;
    public static final String L_CAPACITY_TU = com.hbm.ntm.compat.CompatEnergyControl.L_CAPACITY_TU;
    public static final String L_CAPACITY_ = com.hbm.ntm.compat.CompatEnergyControl.L_CAPACITY_;
    public static final String D_CONSUMPTION_HE = com.hbm.ntm.compat.CompatEnergyControl.D_CONSUMPTION_HE;
    public static final String D_CONSUMPTION_MB = com.hbm.ntm.compat.CompatEnergyControl.D_CONSUMPTION_MB;
    @Deprecated public static final String S_CONSUMPTION_ = com.hbm.ntm.compat.CompatEnergyControl.S_CONSUMPTION_;
    public static final String D_OUTPUT_HE = com.hbm.ntm.compat.CompatEnergyControl.D_OUTPUT_HE;
    public static final String D_OUTPUT_MB = com.hbm.ntm.compat.CompatEnergyControl.D_OUTPUT_MB;
    public static final String D_OUTPUT_TU = com.hbm.ntm.compat.CompatEnergyControl.D_OUTPUT_TU;
    public static final String L_DIFF_HE = com.hbm.ntm.compat.CompatEnergyControl.L_DIFF_HE;
    @Deprecated public static final String I_TEMP_K = com.hbm.ntm.compat.CompatEnergyControl.I_TEMP_K;
    public static final String D_TURBINE_PERCENT = com.hbm.ntm.compat.CompatEnergyControl.D_TURBINE_PERCENT;
    public static final String I_TURBINE_SPEED = com.hbm.ntm.compat.CompatEnergyControl.I_TURBINE_SPEED;
    public static final String L_COREHEAT_C = com.hbm.ntm.compat.CompatEnergyControl.L_COREHEAT_C;
    public static final String L_HULLHEAT_C = com.hbm.ntm.compat.CompatEnergyControl.L_HULLHEAT_C;
    public static final String S_LEVEL_PERCENT = com.hbm.ntm.compat.CompatEnergyControl.S_LEVEL_PERCENT;
    @Deprecated public static final String L_HEATL = com.hbm.ntm.compat.CompatEnergyControl.L_HEATL;
    public static final String D_HEAT_C = com.hbm.ntm.compat.CompatEnergyControl.D_HEAT_C;
    public static final String D_MAXHEAT_C = com.hbm.ntm.compat.CompatEnergyControl.D_MAXHEAT_C;
    public static final String L_PRESSURE_BAR = com.hbm.ntm.compat.CompatEnergyControl.L_PRESSURE_BAR;
    public static final String L_FUEL = com.hbm.ntm.compat.CompatEnergyControl.L_FUEL;
    @Deprecated public static final String S_FUELTEXT = com.hbm.ntm.compat.CompatEnergyControl.S_FUELTEXT;
    @Deprecated public static final String S_DEPLETED = com.hbm.ntm.compat.CompatEnergyControl.S_DEPLETED;
    public static final String D_DEPLETION_PERCENT = com.hbm.ntm.compat.CompatEnergyControl.D_DEPLETION_PERCENT;
    public static final String D_XENON_PERCENT = com.hbm.ntm.compat.CompatEnergyControl.D_XENON_PERCENT;
    public static final String D_SKIN_C = com.hbm.ntm.compat.CompatEnergyControl.D_SKIN_C;
    public static final String D_CORE_C = com.hbm.ntm.compat.CompatEnergyControl.D_CORE_C;
    public static final String D_MELT_C = com.hbm.ntm.compat.CompatEnergyControl.D_MELT_C;
    public static final String I_PROGRESS = com.hbm.ntm.compat.CompatEnergyControl.I_PROGRESS;
    public static final String I_FLUX = com.hbm.ntm.compat.CompatEnergyControl.I_FLUX;
    public static final String I_WATER = com.hbm.ntm.compat.CompatEnergyControl.I_WATER;
    public static final String L_DURABILITY = com.hbm.ntm.compat.CompatEnergyControl.L_DURABILITY;
    public static final String S_TANK = com.hbm.ntm.compat.CompatEnergyControl.S_TANK;
    public static final String S_TANK2 = com.hbm.ntm.compat.CompatEnergyControl.S_TANK2;
    public static final String S_TANK3 = com.hbm.ntm.compat.CompatEnergyControl.S_TANK3;
    public static final String S_TANK4 = com.hbm.ntm.compat.CompatEnergyControl.S_TANK4;
    public static final String S_TANK5 = com.hbm.ntm.compat.CompatEnergyControl.S_TANK5;
    @Deprecated public static final String I_PISTONS = com.hbm.ntm.compat.CompatEnergyControl.I_PISTONS;
    public static final String S_CHUNKRAD = com.hbm.ntm.compat.CompatEnergyControl.S_CHUNKRAD;
    public static final String B_ACTIVE = com.hbm.ntm.compat.CompatEnergyControl.B_ACTIVE;

    public static ItemStack getCraftingMaterial() {
        return new ItemStack(ModItems.STEEL_INGOT.get());
    }

    public static boolean isElectricItem(ItemStack stack) {
        return com.hbm.ntm.compat.CompatEnergyControl.isElectricItem(stack);
    }

    public static double dischargeItem(ItemStack stack, double needed) {
        return com.hbm.ntm.compat.CompatEnergyControl.dischargeItem(stack, needed);
    }

    public static void getEnergyData(BlockEntity blockEntity, CompoundTag data) {
        com.hbm.ntm.compat.CompatEnergyControl.getEnergyData(blockEntity, data);
    }

    public static void getExtraData(BlockEntity blockEntity, CompoundTag data) {
        com.hbm.ntm.compat.CompatEnergyControl.getExtraData(blockEntity, data);
    }

    public static int getHeat(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatEnergyControl.getHeat(blockEntity);
    }

    public static List<Object[]> getAllTanks(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatEnergyControl.getAllTanks(blockEntity);
    }

    public static List<Object[]> getAllTanks(BlockEntity blockEntity, boolean includeEmpty) {
        return com.hbm.ntm.compat.CompatEnergyControl.getAllTanks(blockEntity, includeEmpty);
    }

    public static List<Object[]> getExternalFluidInfo(BlockEntity blockEntity, boolean includeEmpty) {
        return com.hbm.ntm.compat.CompatEnergyControl.getExternalFluidInfo(blockEntity, includeEmpty);
    }

    public static boolean hasEnergy(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatEnergyControl.hasEnergy(blockEntity);
    }

    public static boolean hasTanks(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatEnergyControl.hasTanks(blockEntity);
    }

    public static long getBufferedPower(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatEnergyControl.getBufferedPower(blockEntity);
    }

    public static long getMaxPower(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatEnergyControl.getMaxPower(blockEntity);
    }

    public static int getEnergyPriority(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatEnergyControl.getEnergyPriority(blockEntity);
    }

    public static BlockEntity findTileEntity(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatEnergyControl.findTileEntity(blockEntity);
    }

    public static BlockEntity findTileEntity(Level level, BlockPos pos) {
        return com.hbm.ntm.compat.CompatEnergyControl.findTileEntity(level, pos);
    }

    public static BlockEntity findTileEntity(Level level, int x, int y, int z) {
        return findTileEntity(level, new BlockPos(x, y, z));
    }

    public static ResourceLocation getFluidTexture(String name) {
        return com.hbm.ntm.compat.CompatEnergyControl.getFluidTexture(name);
    }

    private CompatEnergyControl() {
    }
}
