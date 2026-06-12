package com.hbm.util;

import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.fluid.FluidType;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy package facade for stable, non-computer compat utilities.
 */
@Deprecated(forRemoval = false)
public final class Compat {
    public static final String MOD_AE2 = com.hbm.ntm.compat.Compat.MOD_AE2;
    public static final String MOD_JEI = com.hbm.ntm.compat.Compat.MOD_JEI;
    public static final String MOD_ENERGY_CONTROL = com.hbm.ntm.compat.Compat.MOD_ENERGY_CONTROL;
    public static final String MOD_GTCEU = com.hbm.ntm.compat.Compat.MOD_GTCEU;
    public static final String MOD_GREGTECH = com.hbm.ntm.compat.Compat.MOD_GREGTECH;
    public static final String MOD_GT6 = com.hbm.ntm.compat.Compat.MOD_GT6;
    public static final String MOD_REACTORCRAFT = com.hbm.ntm.compat.Compat.MOD_REACTORCRAFT;
    public static final String MOD_REC = com.hbm.ntm.compat.Compat.MOD_REC;
    public static final String MOD_ET_FUTURUM = com.hbm.ntm.compat.Compat.MOD_ET_FUTURUM;
    public static final String MOD_EF = com.hbm.ntm.compat.Compat.MOD_EF;
    public static final String MOD_GALACTICRAFT = com.hbm.ntm.compat.Compat.MOD_GALACTICRAFT;
    public static final String MOD_GCC = com.hbm.ntm.compat.Compat.MOD_GCC;
    public static final String MOD_ADVANCED_ROCKETRY = com.hbm.ntm.compat.Compat.MOD_ADVANCED_ROCKETRY;
    public static final String MOD_AR = com.hbm.ntm.compat.Compat.MOD_AR;
    public static final String MOD_TCONSTRUCT = com.hbm.ntm.compat.Compat.MOD_TCONSTRUCT;
    public static final String MOD_TIC = com.hbm.ntm.compat.Compat.MOD_TIC;
    public static final String MOD_THAUMCRAFT = com.hbm.ntm.compat.Compat.MOD_THAUMCRAFT;
    public static final String MOD_TC = com.hbm.ntm.compat.Compat.MOD_TC;
    public static final String MOD_ENDLESS_IDS = com.hbm.ntm.compat.Compat.MOD_ENDLESS_IDS;
    public static final String MOD_EIDS = com.hbm.ntm.compat.Compat.MOD_EIDS;
    public static final String MOD_ANGELICA = com.hbm.ntm.compat.Compat.MOD_ANGELICA;
    public static final String MOD_ANG = com.hbm.ntm.compat.Compat.MOD_ANG;
    public static final String MOD_RAILCRAFT = com.hbm.ntm.compat.Compat.MOD_RAILCRAFT;
    public static final String MOD_RC = com.hbm.ntm.compat.Compat.MOD_RC;
    public static final String MOD_TORCHERINO = com.hbm.ntm.compat.Compat.MOD_TORCHERINO;
    public static final String MOD_TOR = com.hbm.ntm.compat.Compat.MOD_TOR;

    public enum ReikaIsotope {
        C14, U235, U238, Pu239, Pu244, Th232, Rn222, Ra226, Sr90, Po210, Cs134, Xe135, Zr93, Mo99,
        Cs137, Tc99, I131, Pm147, I129, Sm151, Ru106, Kr85, Pd107, Se79, Gd155, Sb125, Sn126,
        Xe136, I135, Xe131, Ru103, Pm149, Rh105;

        public float getRad() {
            return com.hbm.ntm.compat.Compat.reikaIsotopeRad(name(), 0.0F);
        }
    }

    public static boolean isModLoaded(String modId) {
        return com.hbm.ntm.compat.Compat.isModLoaded(modId);
    }

    public static boolean isAnyModLoaded(String... modIds) {
        return com.hbm.ntm.compat.Compat.isAnyModLoaded(modIds);
    }

    public static List<String> loadedModIds(List<String> modIds) {
        return com.hbm.ntm.compat.Compat.loadedModIds(modIds);
    }

    public static String optionalModSummary(List<String> modIds) {
        return com.hbm.ntm.compat.Compat.optionalModSummary(modIds);
    }

    @Nullable
    public static Item tryLoadItem(String id) {
        return com.hbm.ntm.compat.Compat.tryLoadItem(id);
    }

    @Nullable
    public static Item tryLoadItem(String namespace, String path) {
        return com.hbm.ntm.compat.Compat.tryLoadItem(namespace, path);
    }

    @Nullable
    public static Item tryLoadItem(ResourceLocation id) {
        return com.hbm.ntm.compat.Compat.tryLoadItem(id);
    }

    public static ItemStack tryLoadItemStack(String id) {
        return com.hbm.ntm.compat.Compat.tryLoadItemStack(id);
    }

    public static ItemStack tryLoadItemStack(String id, int count) {
        return com.hbm.ntm.compat.Compat.tryLoadItemStack(id, count);
    }

    public static ItemStack tryLoadItemStack(String namespace, String path) {
        return com.hbm.ntm.compat.Compat.tryLoadItemStack(namespace, path);
    }

    public static ItemStack tryLoadItemStack(String namespace, String path, int count) {
        return com.hbm.ntm.compat.Compat.tryLoadItemStack(namespace, path, count);
    }

    public static ItemStack tryLoadItemStack(ResourceLocation id) {
        return com.hbm.ntm.compat.Compat.tryLoadItemStack(id);
    }

    public static ItemStack tryLoadItemStack(ResourceLocation id, int count) {
        return com.hbm.ntm.compat.Compat.tryLoadItemStack(id, count);
    }

    @Nullable
    public static Block tryLoadBlock(String id) {
        return com.hbm.ntm.compat.Compat.tryLoadBlock(id);
    }

    @Nullable
    public static Block tryLoadBlock(String namespace, String path) {
        return com.hbm.ntm.compat.Compat.tryLoadBlock(namespace, path);
    }

    @Nullable
    public static Block tryLoadBlock(ResourceLocation id) {
        return com.hbm.ntm.compat.Compat.tryLoadBlock(id);
    }

    public static ItemStack tryLoadBlockStack(String id) {
        return com.hbm.ntm.compat.Compat.tryLoadBlockStack(id);
    }

    public static ItemStack tryLoadBlockStack(String id, int count) {
        return com.hbm.ntm.compat.Compat.tryLoadBlockStack(id, count);
    }

    public static ItemStack tryLoadBlockStack(String namespace, String path) {
        return com.hbm.ntm.compat.Compat.tryLoadBlockStack(namespace, path);
    }

    public static ItemStack tryLoadBlockStack(String namespace, String path, int count) {
        return com.hbm.ntm.compat.Compat.tryLoadBlockStack(namespace, path, count);
    }

    public static ItemStack tryLoadBlockStack(ResourceLocation id) {
        return com.hbm.ntm.compat.Compat.tryLoadBlockStack(id);
    }

    public static ItemStack tryLoadBlockStack(ResourceLocation id, int count) {
        return com.hbm.ntm.compat.Compat.tryLoadBlockStack(id, count);
    }

    public static ItemStack getPreferredItemOutput(List<ItemStack> candidates) {
        return com.hbm.ntm.compat.Compat.getPreferredItemOutput(candidates);
    }

    public static ItemStack getPreferredItemOutput(ItemStack... candidates) {
        return com.hbm.ntm.compat.Compat.getPreferredItemOutput(candidates);
    }

    public static ItemStack getPreferredOreOutput(List<ItemStack> candidates) {
        return com.hbm.ntm.compat.Compat.getPreferredOreOutput(candidates);
    }

    public static ItemStack getPreferredOreOutput(ItemStack... candidates) {
        return com.hbm.ntm.compat.Compat.getPreferredOreOutput(candidates);
    }

    public static ItemStack getPreferredItemOutput(List<ItemStack> candidates, String... preferredNamespaces) {
        return com.hbm.ntm.compat.Compat.getPreferredItemOutput(candidates, preferredNamespaces);
    }

    public static ItemStack getPreferredItemOutput(List<ItemStack> candidates, List<String> preferredNamespaces) {
        return com.hbm.ntm.compat.Compat.getPreferredItemOutput(candidates, preferredNamespaces);
    }

    @Nullable
    public static BlockEntity getTileStandard(Level level, BlockPos pos) {
        return com.hbm.ntm.compat.Compat.getTileStandard(level, pos);
    }

    @Nullable
    public static BlockEntity getTileStandard(Level level, int x, int y, int z) {
        return com.hbm.ntm.compat.Compat.getTileStandard(level, x, y, z);
    }

    @Nullable
    public static ResourceLocation itemId(Item item) {
        return com.hbm.ntm.compat.Compat.itemId(item);
    }

    @Nullable
    public static ResourceLocation itemId(ItemStack stack) {
        return com.hbm.ntm.compat.Compat.itemId(stack);
    }

    @Nullable
    public static String itemNamespace(Item item) {
        return com.hbm.ntm.compat.Compat.itemNamespace(item);
    }

    @Nullable
    public static String itemNamespace(ItemStack stack) {
        return com.hbm.ntm.compat.Compat.itemNamespace(stack);
    }

    public static boolean isItemFromMod(ItemStack stack, String modId) {
        return com.hbm.ntm.compat.Compat.isItemFromMod(stack, modId);
    }

    public static boolean isItemFromMod(Item item, String modId) {
        return com.hbm.ntm.compat.Compat.isItemFromMod(item, modId);
    }

    @Nullable
    public static ResourceLocation blockId(Block block) {
        return com.hbm.ntm.compat.Compat.blockId(block);
    }

    @Nullable
    public static ResourceLocation blockId(BlockState state) {
        return com.hbm.ntm.compat.Compat.blockId(state);
    }

    @Nullable
    public static String blockNamespace(Block block) {
        return com.hbm.ntm.compat.Compat.blockNamespace(block);
    }

    @Nullable
    public static String blockNamespace(BlockState state) {
        return com.hbm.ntm.compat.Compat.blockNamespace(state);
    }

    public static boolean isBlockFromMod(Block block, String modId) {
        return com.hbm.ntm.compat.Compat.isBlockFromMod(block, modId);
    }

    public static boolean isBlockFromMod(BlockState state, String modId) {
        return com.hbm.ntm.compat.Compat.isBlockFromMod(state, modId);
    }

    @Nullable
    public static ReikaIsotope reikaIsotope(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return Arrays.stream(ReikaIsotope.values())
                .filter(isotope -> isotope.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static float reikaIsotopeRad(String name, float fallback) {
        ReikaIsotope isotope = reikaIsotope(name);
        return isotope == null ? fallback : isotope.getRad();
    }

    public static List<String> reikaIsotopeNames() {
        return Arrays.stream(ReikaIsotope.values())
                .map(Enum::name)
                .toList();
    }

    public static int steamTypeToInt(FluidType type) {
        return com.hbm.ntm.compat.Compat.steamTypeToInt(type);
    }

    public static FluidType intToSteamType(int compressionLevel) {
        return com.hbm.ntm.compat.Compat.intToSteamType(compressionLevel);
    }

    public static com.hbm.ntm.compat.Compat.CompatHazmatReport registerCompatHazmat() {
        return com.hbm.ntm.compat.Compat.registerCompatHazmat();
    }

    public static ResourceLocation resource(String id) {
        return com.hbm.ntm.compat.Compat.resource(id);
    }

    public static ResourceLocation resource(String namespace, String path) {
        return com.hbm.ntm.compat.Compat.resource(namespace, path);
    }

    @Deprecated(forRemoval = false)
    public static void registerCompatHazmat(String modId, String itemPath, double resistance) {
        Item item = tryLoadItem(modId, itemPath);
        if (item != null) {
            com.hbm.ntm.radiation.HazmatRegistry.registerExternalHazmat(item, resistance);
        }
    }

    @Deprecated(forRemoval = false)
    public static void registerCompatHazmat(String modId, String itemPath, HazardClass... classes) {
        Item item = tryLoadItem(modId, itemPath);
        if (item != null) {
            com.hbm.ntm.radiation.ArmorRegistry.registerProtection(item, classes);
        }
    }

    private Compat() {
    }
}
