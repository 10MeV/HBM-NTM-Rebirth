package com.hbm.ntm.compat;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.radiation.RadiationConstants;
import com.hbm.ntm.radiation.HazmatRegistry;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Modern replacement for small, stable pieces of the 1.7.10 compat utility class.
 */
public final class Compat {
    public static final String MOD_AE2 = "ae2";
    public static final String MOD_JEI = "jei";
    public static final String MOD_ENERGY_CONTROL = "energycontrol";
    public static final String MOD_GTCEU = "gtceu";
    public static final String MOD_GREGTECH = "gregtech";
    public static final String MOD_GT6 = MOD_GREGTECH;
    public static final String MOD_REACTORCRAFT = "reactorcraft";
    public static final String MOD_ET_FUTURUM = "etfuturum";
    public static final String MOD_EF = MOD_ET_FUTURUM;
    public static final String MOD_GALACTICRAFT = "galacticraftcore";
    public static final String MOD_GCC = MOD_GALACTICRAFT;
    public static final String MOD_ADVANCED_ROCKETRY = "advancedrocketry";
    public static final String MOD_AR = MOD_ADVANCED_ROCKETRY;
    public static final String MOD_TCONSTRUCT = "tconstruct";
    public static final String MOD_TIC = MOD_TCONSTRUCT;
    public static final String MOD_RAILCRAFT = "railcraft";
    public static final String MOD_RC = MOD_RAILCRAFT;
    public static final String MOD_TORCHERINO = "torcherino";
    public static final String MOD_TOR = MOD_TORCHERINO;

    private static final HazardClass[] LEGACY_FULL_PACKAGE = new HazardClass[] {
            HazardClass.PARTICLE_COARSE,
            HazardClass.PARTICLE_FINE,
            HazardClass.GAS_LUNG,
            HazardClass.BACTERIA,
            HazardClass.GAS_BLISTERING,
            HazardClass.GAS_MONOXIDE,
            HazardClass.LIGHT,
            HazardClass.SAND
    };
    private static final float REIKA_GEN_S = 10_000.0F;
    private static final float REIKA_GEN_H = 2_000.0F;
    private static final float REIKA_GEN_10D = 100.0F;
    private static final float REIKA_GEN_1Y = 50.0F;
    private static final float REIKA_GEN_10Y = 30.0F;
    private static final float REIKA_GEN_100Y = 10.0F;
    private static final float REIKA_GEN_10K = 6.25F;
    private static final float REIKA_GEN_100K = 5.0F;
    private static final float REIKA_GEN_1M = 2.5F;
    private static final float REIKA_GEN_10M = 1.5F;
    private static final float REIKA_GEN_100M = 1.0F;

    public enum ReikaIsotope {
        C14(REIKA_GEN_10K),
        U235(RadiationConstants.U235),
        U238(RadiationConstants.U238),
        Pu239(RadiationConstants.PU239),
        Pu244(REIKA_GEN_100M),
        Th232(RadiationConstants.TH232),
        Rn222(REIKA_GEN_10D),
        Ra226(RadiationConstants.RA226),
        Sr90(REIKA_GEN_10Y),
        Po210(RadiationConstants.PO210),
        Cs134(REIKA_GEN_1Y),
        Xe135(RadiationConstants.XE135),
        Zr93(REIKA_GEN_1M),
        Mo99(REIKA_GEN_10D),
        Cs137(RadiationConstants.CS137),
        Tc99(RadiationConstants.TC99),
        I131(150.0F),
        Pm147(REIKA_GEN_1Y),
        I129(REIKA_GEN_10M),
        Sm151(REIKA_GEN_100Y),
        Ru106(REIKA_GEN_1Y),
        Kr85(REIKA_GEN_10Y),
        Pd107(REIKA_GEN_10M),
        Se79(REIKA_GEN_100K),
        Gd155(REIKA_GEN_1Y),
        Sb125(REIKA_GEN_1Y),
        Sn126(REIKA_GEN_100K),
        Xe136(0.0F),
        I135(REIKA_GEN_H),
        Xe131(REIKA_GEN_10D),
        Ru103(REIKA_GEN_S),
        Pm149(REIKA_GEN_10D),
        Rh105(REIKA_GEN_H);

        private final float rads;

        ReikaIsotope(float rads) {
            this.rads = rads;
        }

        public float getRad() {
            return rads;
        }
    }

    public static boolean isModLoaded(String modId) {
        return modId != null && !modId.isBlank() && ModList.get().isLoaded(modId);
    }

    public static boolean isAnyModLoaded(String... modIds) {
        return modIds != null && Arrays.stream(modIds).anyMatch(Compat::isModLoaded);
    }

    public static List<String> loadedModIds(List<String> modIds) {
        if (modIds == null || modIds.isEmpty()) {
            return List.of();
        }
        return modIds.stream()
                .filter(Compat::isModLoaded)
                .toList();
    }

    public static String optionalModSummary(List<String> modIds) {
        if (modIds == null || modIds.isEmpty()) {
            return "none";
        }
        return modIds.stream()
                .map(modId -> modId + "=" + isModLoaded(modId))
                .reduce((left, right) -> left + ", " + right)
                .orElse("none");
    }

    @Nullable
    public static Item tryLoadItem(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return tryLoadItem(ResourceLocation.tryParse(id));
    }

    @Nullable
    public static Item tryLoadItem(String namespace, String path) {
        ResourceLocation id = resource(namespace, path);
        return tryLoadItem(id);
    }

    @Nullable
    public static Item tryLoadItem(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        return item == null || item == Items.AIR ? null : item;
    }

    public static ItemStack tryLoadItemStack(String id) {
        return tryLoadItemStack(id, 1);
    }

    public static ItemStack tryLoadItemStack(String id, int count) {
        Item item = tryLoadItem(id);
        return item == null ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, count));
    }

    public static ItemStack tryLoadItemStack(String namespace, String path) {
        return tryLoadItemStack(namespace, path, 1);
    }

    public static ItemStack tryLoadItemStack(String namespace, String path, int count) {
        Item item = tryLoadItem(namespace, path);
        return item == null ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, count));
    }

    public static ItemStack tryLoadItemStack(ResourceLocation id) {
        return tryLoadItemStack(id, 1);
    }

    public static ItemStack tryLoadItemStack(ResourceLocation id, int count) {
        Item item = tryLoadItem(id);
        return item == null ? ItemStack.EMPTY : new ItemStack(item, Math.max(1, count));
    }

    @Nullable
    public static Block tryLoadBlock(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return tryLoadBlock(ResourceLocation.tryParse(id));
    }

    @Nullable
    public static Block tryLoadBlock(String namespace, String path) {
        ResourceLocation id = resource(namespace, path);
        return tryLoadBlock(id);
    }

    @Nullable
    public static Block tryLoadBlock(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        return block == null || block == Blocks.AIR ? null : block;
    }

    public static ItemStack tryLoadBlockStack(String id) {
        return tryLoadBlockStack(id, 1);
    }

    public static ItemStack tryLoadBlockStack(String id, int count) {
        Block block = tryLoadBlock(id);
        return block == null ? ItemStack.EMPTY : new ItemStack(block, Math.max(1, count));
    }

    public static ItemStack tryLoadBlockStack(String namespace, String path) {
        return tryLoadBlockStack(namespace, path, 1);
    }

    public static ItemStack tryLoadBlockStack(String namespace, String path, int count) {
        Block block = tryLoadBlock(namespace, path);
        return block == null ? ItemStack.EMPTY : new ItemStack(block, Math.max(1, count));
    }

    public static ItemStack tryLoadBlockStack(ResourceLocation id) {
        return tryLoadBlockStack(id, 1);
    }

    public static ItemStack tryLoadBlockStack(ResourceLocation id, int count) {
        Block block = tryLoadBlock(id);
        return block == null ? ItemStack.EMPTY : new ItemStack(block, Math.max(1, count));
    }

    public static ItemStack getPreferredItemOutput(List<ItemStack> candidates) {
        return getPreferredItemOutput(candidates, List.of(HbmNtm.MOD_ID, "minecraft"));
    }

    public static ItemStack getPreferredOreOutput(List<ItemStack> candidates) {
        return getPreferredItemOutput(candidates);
    }

    public static ItemStack getPreferredItemOutput(List<ItemStack> candidates, List<String> preferredNamespaces) {
        if (candidates == null || candidates.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int bestPreference = Integer.MAX_VALUE;
        ItemStack best = ItemStack.EMPTY;
        for (ItemStack candidate : candidates) {
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            int preference = namespacePreference(candidate, preferredNamespaces);
            if (best.isEmpty() || preference < bestPreference) {
                best = candidate;
                bestPreference = preference;
            }
        }
        return best.isEmpty() ? ItemStack.EMPTY : best.copy();
    }

    @Nullable
    public static BlockEntity getTileStandard(Level level, BlockPos pos) {
        if (level == null || pos == null || !level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return null;
        }
        return level.getBlockEntity(pos);
    }

    @Nullable
    public static BlockEntity getTileStandard(Level level, int x, int y, int z) {
        return getTileStandard(level, new BlockPos(x, y, z));
    }

    @Nullable
    public static ResourceLocation itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
    }

    @Nullable
    public static String itemNamespace(ItemStack stack) {
        ResourceLocation id = itemId(stack);
        return id == null ? null : id.getNamespace();
    }

    public static boolean isItemFromMod(ItemStack stack, String modId) {
        String namespace = itemNamespace(stack);
        return namespace != null && modId != null && namespace.equals(modId);
    }

    @Nullable
    public static ResourceLocation blockId(Block block) {
        if (block == null || block == Blocks.AIR) {
            return null;
        }
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    @Nullable
    public static ResourceLocation blockId(BlockState state) {
        return state == null ? null : blockId(state.getBlock());
    }

    @Nullable
    public static String blockNamespace(Block block) {
        ResourceLocation id = blockId(block);
        return id == null ? null : id.getNamespace();
    }

    @Nullable
    public static String blockNamespace(BlockState state) {
        ResourceLocation id = blockId(state);
        return id == null ? null : id.getNamespace();
    }

    public static boolean isBlockFromMod(Block block, String modId) {
        String namespace = blockNamespace(block);
        return namespace != null && modId != null && namespace.equals(modId);
    }

    public static boolean isBlockFromMod(BlockState state, String modId) {
        String namespace = blockNamespace(state);
        return namespace != null && modId != null && namespace.equals(modId);
    }

    @Nullable
    public static ReikaIsotope reikaIsotope(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        for (ReikaIsotope isotope : ReikaIsotope.values()) {
            if (isotope.name().equalsIgnoreCase(name)) {
                return isotope;
            }
        }
        return null;
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
        if (type == HbmFluids.HOTSTEAM) {
            return 1;
        }
        if (type == HbmFluids.SUPERHOTSTEAM) {
            return 2;
        }
        if (type == HbmFluids.ULTRAHOTSTEAM) {
            return 3;
        }
        return 0;
    }

    public static FluidType intToSteamType(int compressionLevel) {
        return switch (compressionLevel) {
            case 1 -> HbmFluids.HOTSTEAM;
            case 2 -> HbmFluids.SUPERHOTSTEAM;
            case 3 -> HbmFluids.ULTRAHOTSTEAM;
            default -> HbmFluids.STEAM;
        };
    }

    public static CompatHazmatReport registerCompatHazmat() {
        double p90 = 1.0D;
        double p99 = 2.0D;
        int resistanceEntries = 0;
        int protectionEntries = 0;

        resistanceEntries += tryRegisterHazmatSet(MOD_GT6,
                "gt.armor.hazmat.radiation.head",
                "gt.armor.hazmat.radiation.chest",
                "gt.armor.hazmat.radiation.legs",
                "gt.armor.hazmat.radiation.boots",
                p90);
        resistanceEntries += tryRegisterHazmatSet(MOD_GT6,
                "gt.armor.hazmat.universal.head",
                "gt.armor.hazmat.universal.chest",
                "gt.armor.hazmat.universal.legs",
                "gt.armor.hazmat.universal.boots",
                p99);
        resistanceEntries += tryRegisterHazmatSet(MOD_REACTORCRAFT,
                "reactorcraft_item_hazhelmet",
                "reactorcraft_item_hazchest",
                "reactorcraft_item_hazlegs",
                "reactorcraft_item_hazboots",
                p99);
        resistanceEntries += tryRegisterHazmatSet(MOD_ET_FUTURUM,
                "netherite_helmet",
                "netherite_chestplate",
                "netherite_leggings",
                "netherite_boots",
                p90);

        protectionEntries += tryRegisterProtection(MOD_GT6, "gt.armor.hazmat.universal.head", LEGACY_FULL_PACKAGE);
        protectionEntries += tryRegisterProtection(MOD_GT6, "gt.armor.hazmat.biochemgas.head", LEGACY_FULL_PACKAGE);
        protectionEntries += tryRegisterProtection(MOD_GT6, "gt.armor.hazmat.radiation.head", LEGACY_FULL_PACKAGE);

        return new CompatHazmatReport(resistanceEntries, protectionEntries);
    }

    private static int tryRegisterHazmatSet(String namespace, String helmet, String chest, String legs, String boots,
                                            double materialResistance) {
        return HazmatRegistry.registerExternalArmorSet(
                resource(namespace, helmet),
                resource(namespace, chest),
                resource(namespace, legs),
                resource(namespace, boots),
                materialResistance);
    }

    private static int tryRegisterProtection(String namespace, String path, HazardClass... protections) {
        Item item = tryLoadItem(namespace, path);
        if (item == null) {
            return 0;
        }
        HazmatRegistry.registerExternalProtection(item, protections);
        return 1;
    }

    @Nullable
    public static ResourceLocation resource(String namespace, String path) {
        if (namespace == null || namespace.isBlank() || path == null || path.isBlank()) {
            return null;
        }
        return ResourceLocation.tryParse(namespace.toLowerCase(Locale.ROOT) + ":" + path);
    }

    private static int namespacePreference(ItemStack stack, List<String> preferredNamespaces) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || preferredNamespaces == null) {
            return Integer.MAX_VALUE;
        }
        for (int index = 0; index < preferredNamespaces.size(); index++) {
            if (id.getNamespace().equals(preferredNamespaces.get(index))) {
                return index;
            }
        }
        return Integer.MAX_VALUE;
    }

    private Compat() {
    }

    public record CompatHazmatReport(int resistanceEntries, int protectionEntries) {
        public int totalEntries() {
            return resistanceEntries + protectionEntries;
        }
    }
}
