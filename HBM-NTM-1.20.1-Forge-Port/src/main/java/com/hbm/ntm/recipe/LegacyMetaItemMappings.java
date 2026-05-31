package com.hbm.ntm.recipe;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class LegacyMetaItemMappings {
    public static final ResourceLocation BATTERY_PACK = hbm("battery_pack");
    public static final ResourceLocation BATTERY_SC = hbm("battery_sc");
    public static final ResourceLocation CIRCUIT = hbm("circuit");

    private static final Map<ResourceLocation, List<RegistryObject<Item>>> ITEM_VARIANTS = new LinkedHashMap<>();

    static {
        register(BATTERY_PACK,
                ModItems.BATTERY_REDSTONE,
                ModItems.BATTERY_LEAD,
                ModItems.BATTERY_LITHIUM,
                ModItems.BATTERY_SODIUM,
                ModItems.BATTERY_SCHRABIDIUM,
                ModItems.BATTERY_QUANTUM,
                ModItems.CAPACITOR_COPPER,
                ModItems.CAPACITOR_GOLD,
                ModItems.CAPACITOR_NIOBIUM,
                ModItems.CAPACITOR_TANTALUM,
                ModItems.CAPACITOR_BISMUTH,
                ModItems.CAPACITOR_SPARK);
        register(BATTERY_SC,
                ModItems.BATTERY_SC_EMPTY,
                ModItems.BATTERY_SC_WASTE,
                ModItems.BATTERY_SC_RA226,
                ModItems.BATTERY_SC_TC99,
                ModItems.BATTERY_SC_CO60,
                ModItems.BATTERY_SC_PU238,
                ModItems.BATTERY_SC_PO210,
                ModItems.BATTERY_SC_AU198,
                ModItems.BATTERY_SC_PB209,
                ModItems.BATTERY_SC_AM241);
        registerList(CIRCUIT, ModItems.CIRCUIT_ITEMS);
    }

    @SafeVarargs
    public static void register(ResourceLocation legacyId, RegistryObject<Item>... variantsByMeta) {
        registerList(legacyId, List.of(variantsByMeta));
    }

    public static void registerList(ResourceLocation legacyId, List<RegistryObject<Item>> variantsByMeta) {
        if (ITEM_VARIANTS.containsKey(legacyId)) {
            throw new IllegalStateException("Duplicate legacy item mapping family: " + legacyId);
        }
        ITEM_VARIANTS.put(legacyId, List.copyOf(variantsByMeta));
    }

    public static Optional<RegistryObject<Item>> item(ResourceLocation legacyId, int legacyMeta) {
        List<RegistryObject<Item>> variants = ITEM_VARIANTS.get(legacyId);
        if (variants == null || legacyMeta < 0 || legacyMeta >= variants.size()) {
            return Optional.empty();
        }
        return Optional.of(variants.get(legacyMeta));
    }

    public static RegistryObject<Item> requireItem(ResourceLocation legacyId, int legacyMeta) {
        return item(legacyId, legacyMeta)
                .orElseThrow(() -> new IllegalStateException("Missing legacy item mapping: " + legacyId + " meta " + legacyMeta));
    }

    public static Optional<ItemStack> stack(ResourceLocation legacyId, int legacyMeta, int count) {
        return item(legacyId, legacyMeta).map(item -> new ItemStack(item.get(), Math.max(1, count)));
    }

    public static List<ItemStack> stacks(ResourceLocation legacyId, int count) {
        int safeCount = Math.max(1, count);
        return variants(legacyId).stream()
                .map(item -> new ItemStack(item.get(), safeCount))
                .toList();
    }

    public static Optional<ItemLike> itemLike(ResourceLocation legacyId, int legacyMeta) {
        return item(legacyId, legacyMeta).map(RegistryObject::get);
    }

    public static List<RegistryObject<Item>> variants(ResourceLocation legacyId) {
        List<RegistryObject<Item>> variants = ITEM_VARIANTS.get(legacyId);
        return variants == null ? List.of() : variants;
    }

    public static int variantCount(ResourceLocation legacyId) {
        return variants(legacyId).size();
    }

    public static Set<ResourceLocation> legacyIds() {
        return Collections.unmodifiableSet(ITEM_VARIANTS.keySet());
    }

    public static Map<ResourceLocation, List<RegistryObject<Item>>> mappings() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(ITEM_VARIANTS));
    }

    private static ResourceLocation hbm(String path) {
        return new ResourceLocation("hbm", path);
    }

    private LegacyMetaItemMappings() {
    }
}
