package com.hbm.ntm.compat;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
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
