package com.hbm.ntm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Arrays;
import java.util.List;

/**
 * Legacy-name facade for stable optional-mod compatibility helpers.
 */
@Deprecated(forRemoval = false)
public final class Compat {
    public static final String MOD_GT6 = com.hbm.ntm.compat.Compat.MOD_GT6;
    public static final String MOD_GCC = com.hbm.ntm.compat.Compat.MOD_GCC;
    public static final String MOD_AR = com.hbm.ntm.compat.Compat.MOD_AR;
    public static final String MOD_EF = com.hbm.ntm.compat.Compat.MOD_EF;
    public static final String MOD_REC = com.hbm.ntm.compat.Compat.MOD_REC;
    public static final String MOD_TIC = com.hbm.ntm.compat.Compat.MOD_TIC;
    public static final String MOD_RC = com.hbm.ntm.compat.Compat.MOD_RC;
    public static final String MOD_TC = com.hbm.ntm.compat.Compat.MOD_TC;
    public static final String MOD_EIDS = com.hbm.ntm.compat.Compat.MOD_EIDS;
    public static final String MOD_ANG = com.hbm.ntm.compat.Compat.MOD_ANG;
    public static final String MOD_TOR = com.hbm.ntm.compat.Compat.MOD_TOR;
    public static final String MOD_OC = "opencomputers";

    public static com.hbm.ntm.compat.Compat.CompatHazmatReport lastCompatHazmatReport;

    private Compat() {
    }

    public static boolean isModLoaded(String modid) {
        return com.hbm.ntm.compat.Compat.isModLoaded(modid);
    }

    public static Item tryLoadItem(String domain, String name) {
        return com.hbm.ntm.compat.Compat.tryLoadItem(domain, name);
    }

    public static Item tryLoadItem(String id) {
        return com.hbm.ntm.compat.Compat.tryLoadItem(id);
    }

    public static ItemStack tryLoadItemStack(String domain, String name) {
        return com.hbm.ntm.compat.Compat.tryLoadItemStack(domain, name);
    }

    public static ItemStack tryLoadItemStack(String domain, String name, int count) {
        return com.hbm.ntm.compat.Compat.tryLoadItemStack(domain, name, count);
    }

    public static Block tryLoadBlock(String domain, String name) {
        return com.hbm.ntm.compat.Compat.tryLoadBlock(domain, name);
    }

    public static Block tryLoadBlock(String id) {
        return com.hbm.ntm.compat.Compat.tryLoadBlock(id);
    }

    public static ItemStack getPreferredOreOutput(List<ItemStack> oreList) {
        return com.hbm.ntm.compat.Compat.getPreferredOreOutput(oreList);
    }

    public static ItemStack getPreferredOreOutput(ItemStack... stacks) {
        return com.hbm.ntm.compat.Compat.getPreferredOreOutput(stacks);
    }

    public static ItemStack getPreferredItemOutput(List<ItemStack> stacks) {
        return com.hbm.ntm.compat.Compat.getPreferredItemOutput(stacks);
    }

    public static ItemStack getPreferredItemOutput(ItemStack... stacks) {
        return com.hbm.ntm.compat.Compat.getPreferredItemOutput(stacks);
    }

    public static List<ItemStack> scrapeItemFromME(ItemStack meDrive) {
        return List.of();
    }

    public static com.hbm.ntm.compat.Compat.CompatHazmatReport registerCompatHazmat() {
        lastCompatHazmatReport = com.hbm.ntm.compat.Compat.registerCompatHazmat();
        return lastCompatHazmatReport;
    }

    public static void registerCompatFluidContainers() {
    }

    public static void handleRailcraftNonsense() {
    }

    public static Class<?> getChunkBiomeHook() {
        return null;
    }

    public static short[] getBiomeShortArray(Object instance) {
        return null;
    }

    public static BlockEntity getTileStandard(Level level, int x, int y, int z) {
        return com.hbm.ntm.compat.Compat.getTileStandard(level, x, y, z);
    }

    public static BlockEntity getTileStandard(Level level, BlockPos pos) {
        return com.hbm.ntm.compat.Compat.getTileStandard(level, pos);
    }

    public static void blacklistAccelerator(Class<?> clazz) {
    }

    public static ResourceLocation resource(String namespace, String path) {
        return com.hbm.ntm.compat.Compat.resource(namespace, path);
    }

    public static ResourceLocation resource(String id) {
        return com.hbm.ntm.compat.Compat.resource(id);
    }

    public static List<String> loadedModIds(String... modIds) {
        return com.hbm.ntm.compat.Compat.loadedModIds(Arrays.asList(modIds));
    }

    public enum ReikaIsotope {
        C14,
        U235,
        U238,
        Pu239,
        Pu244,
        Th232,
        Rn222,
        Ra226,
        Sr90,
        Po210,
        Cs134,
        Xe135,
        Zr93,
        Mo99,
        Cs137,
        Tc99,
        I131,
        Pm147,
        I129,
        Sm151,
        Ru106,
        Kr85,
        Pd107,
        Se79,
        Gd155,
        Sb125,
        Sn126,
        Xe136,
        I135,
        Xe131,
        Ru103,
        Pm149,
        Rh105;

        public float getRad() {
            return com.hbm.ntm.compat.Compat.reikaIsotopeRad(name(), 0.0F);
        }
    }
}
