package com.hbm.ntm.compat;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.radiation.HazmatRegistry;
import java.util.List;
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
    public static final String MOD_GALACTICRAFT = "galacticraftcore";
    public static final String MOD_TCONSTRUCT = "tconstruct";
    public static final String MOD_RAILCRAFT = "railcraft";

    public static boolean isModLoaded(String modId) {
        return modId != null && !modId.isBlank() && ModList.get().isLoaded(modId);
    }

    @Nullable
    public static Item tryLoadItem(String namespace, String path) {
        ResourceLocation id = resource(namespace, path);
        if (id == null) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        return item == null || item == Items.AIR ? null : item;
    }

    @Nullable
    public static Block tryLoadBlock(String namespace, String path) {
        ResourceLocation id = resource(namespace, path);
        if (id == null) {
            return null;
        }
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        return block == null || block == Blocks.AIR ? null : block;
    }

    public static ItemStack getPreferredItemOutput(List<ItemStack> candidates) {
        return getPreferredItemOutput(candidates, List.of(HbmNtm.MOD_ID, "minecraft"));
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

    public static CompatHazmatReport registerCompatHazmat() {
        double p90 = 1.0D;
        double p99 = 2.0D;
        int resistanceEntries = 0;
        int protectionEntries = 0;

        resistanceEntries += tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.radiation.head", p90 * HazmatRegistry.HELMET);
        resistanceEntries += tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.radiation.chest", p90 * HazmatRegistry.CHEST);
        resistanceEntries += tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.radiation.legs", p90 * HazmatRegistry.LEGS);
        resistanceEntries += tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.radiation.boots", p90 * HazmatRegistry.BOOTS);

        resistanceEntries += tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.universal.head", p99 * HazmatRegistry.HELMET);
        resistanceEntries += tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.universal.chest", p99 * HazmatRegistry.CHEST);
        resistanceEntries += tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.universal.legs", p99 * HazmatRegistry.LEGS);
        resistanceEntries += tryRegisterHazmat(MOD_GT6, "gt.armor.hazmat.universal.boots", p99 * HazmatRegistry.BOOTS);

        resistanceEntries += tryRegisterHazmat(MOD_REACTORCRAFT, "reactorcraft_item_hazhelmet", p99 * HazmatRegistry.HELMET);
        resistanceEntries += tryRegisterHazmat(MOD_REACTORCRAFT, "reactorcraft_item_hazchest", p99 * HazmatRegistry.CHEST);
        resistanceEntries += tryRegisterHazmat(MOD_REACTORCRAFT, "reactorcraft_item_hazlegs", p99 * HazmatRegistry.LEGS);
        resistanceEntries += tryRegisterHazmat(MOD_REACTORCRAFT, "reactorcraft_item_hazboots", p99 * HazmatRegistry.BOOTS);

        resistanceEntries += tryRegisterHazmat(MOD_ET_FUTURUM, "netherite_helmet", p90 * HazmatRegistry.HELMET);
        resistanceEntries += tryRegisterHazmat(MOD_ET_FUTURUM, "netherite_chestplate", p90 * HazmatRegistry.CHEST);
        resistanceEntries += tryRegisterHazmat(MOD_ET_FUTURUM, "netherite_leggings", p90 * HazmatRegistry.LEGS);
        resistanceEntries += tryRegisterHazmat(MOD_ET_FUTURUM, "netherite_boots", p90 * HazmatRegistry.BOOTS);

        protectionEntries += tryRegisterProtection(MOD_GT6, "gt.armor.hazmat.universal.head", HazardClass.values());
        protectionEntries += tryRegisterProtection(MOD_GT6, "gt.armor.hazmat.biochemgas.head", HazardClass.values());
        protectionEntries += tryRegisterProtection(MOD_GT6, "gt.armor.hazmat.radiation.head", HazardClass.values());

        return new CompatHazmatReport(resistanceEntries, protectionEntries);
    }

    private static int tryRegisterHazmat(String namespace, String path, double resistance) {
        Item item = tryLoadItem(namespace, path);
        if (item == null) {
            return 0;
        }
        HazmatRegistry.registerHazmat(item, resistance);
        return 1;
    }

    private static int tryRegisterProtection(String namespace, String path, HazardClass... protections) {
        Item item = tryLoadItem(namespace, path);
        if (item == null) {
            return 0;
        }
        HazmatRegistry.registerProtection(item, protections);
        return 1;
    }

    @Nullable
    public static ResourceLocation resource(String namespace, String path) {
        if (namespace == null || namespace.isBlank() || path == null || path.isBlank()) {
            return null;
        }
        return ResourceLocation.tryParse(namespace + ":" + path);
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
