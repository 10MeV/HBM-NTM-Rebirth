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
    public static final ResourceLocation PLATE_CAST = hbm("plate_cast");
    public static final ResourceLocation PLATE_WELDED = hbm("plate_welded");
    public static final ResourceLocation WIRE_FINE = hbm("wire_fine");
    public static final ResourceLocation WIRE_DENSE = hbm("wire_dense");
    public static final ResourceLocation PIPE = hbm("pipe");
    public static final ResourceLocation COKE = hbm("coke");
    public static final ResourceLocation BRIQUETTE = hbm("briquette");
    public static final ResourceLocation OIL_TAR = hbm("oil_tar");
    public static final ResourceLocation POWDER_ASH = hbm("powder_ash");
    public static final ResourceLocation CHUNK_ORE = hbm("chunk_ore");
    public static final ResourceLocation PLANT_ITEM = hbm("plant_item");
    public static final ResourceLocation PARTS_LEGENDARY = hbm("parts_legendary");
    public static final ResourceLocation PART_GENERIC = hbm("part_generic");
    public static final ResourceLocation ITEM_EXPENSIVE = hbm("item_expensive");
    public static final ResourceLocation ORE_BYPRODUCT = hbm("ore_byproduct");
    public static final ResourceLocation STAMP_BOOK = hbm("stamp_book");
    public static final ResourceLocation PAGE_OF = hbm("page_of_");
    public static final ResourceLocation CASING = hbm("casing");
    public static final ResourceLocation FUEL_ADDITIVE = hbm("fuel_additive");
    public static final ResourceLocation DRILLBIT = hbm("drillbit");
    public static final ResourceLocation PISTON_SET = hbm("piston_set");

    private static final Map<ResourceLocation, LinkedHashMap<Integer, RegistryObject<Item>>> ITEM_VARIANTS = new LinkedHashMap<>();

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
        registerSparse(PLATE_CAST, Map.of(
                39, requireLegacyItem("plate_cast_combine_steel"),
                46, requireLegacyItem("plate_cast_bismuth_bronze"),
                47, requireLegacyItem("plate_cast_arsenic_bronze")));
        registerSparse(PLATE_WELDED, Map.of(
                36, requireLegacyItem("plate_welded_tcalloy"),
                43, requireLegacyItem("plate_welded_cdalloy")));
        registerSparse(WIRE_FINE, Map.of(
                2_900, requireLegacyItem("wire_fine_copper"),
                31, requireLegacyItem("wire_fine_mingrade"),
                7_400, requireLegacyItem("wire_fine_tungsten"),
                7_900, requireLegacyItem("wire_gold")));
        registerSparse(WIRE_DENSE, Map.of(
                4_100, requireLegacyItem("wire_dense_niobium"),
                7_900, requireLegacyItem("wire_dense_gold"),
                48, requireLegacyItem("wire_dense_bscco")));
        registerSparse(PIPE, Map.of(
                30, requireLegacyItem("pipes_steel")));
        registerSparse(COKE, Map.of(
                0, requireLegacyItem("coke_coal"),
                1, requireLegacyItem("coke_lignite"),
                2, requireLegacyItem("coke_petroleum")));
        registerSparse(BRIQUETTE, Map.of(
                0, requireLegacyItem("briquette_coal"),
                1, requireLegacyItem("briquette_lignite"),
                2, requireLegacyItem("briquette_wood")));
        registerSparse(OIL_TAR, Map.of(
                0, requireLegacyItem("oil_tar_crude"),
                1, requireLegacyItem("oil_tar_crack"),
                2, requireLegacyItem("oil_tar_coal"),
                3, requireLegacyItem("oil_tar_wood"),
                4, requireLegacyItem("oil_tar_wax"),
                5, requireLegacyItem("oil_tar_paraffin")));
        registerSparse(POWDER_ASH, Map.of(
                0, requireLegacyItem("powder_ash_wood"),
                1, requireLegacyItem("powder_ash_coal"),
                2, requireLegacyItem("powder_ash_misc"),
                3, requireLegacyItem("powder_ash_fly"),
                4, requireLegacyItem("powder_ash_soot"),
                5, requireLegacyItem("powder_ash_fullerene")));
        registerSparse(CHUNK_ORE, Map.of(
                0, requireLegacyItem("chunk_ore_rare"),
                1, requireLegacyItem("chunk_ore_malachite"),
                2, requireLegacyItem("chunk_ore_cryolite"),
                3, requireLegacyItem("chunk_ore_moonstone")));
        registerSparse(PLANT_ITEM, Map.of(
                0, requireLegacyItem("plant_item_tobacco"),
                1, requireLegacyItem("plant_item_rope"),
                2, requireLegacyItem("plant_item_mustardwillow")));
        registerSparse(PARTS_LEGENDARY, Map.of(
                0, requireLegacyItem("parts_legendary_tier1"),
                1, requireLegacyItem("parts_legendary_tier2"),
                2, requireLegacyItem("parts_legendary_tier3")));
        registerSparse(PART_GENERIC, Map.of(
                0, requireLegacyItem("part_generic_piston_pneumatic"),
                1, requireLegacyItem("part_generic_piston_hydraulic"),
                2, requireLegacyItem("part_generic_piston_electric"),
                3, requireLegacyItem("part_generic_lde"),
                4, requireLegacyItem("part_generic_hde"),
                5, requireLegacyItem("part_generic_glass_polarized")));
        registerList(ITEM_EXPENSIVE, ModItems.EXPENSIVE_MODE_ITEMS);
        registerList(ORE_BYPRODUCT, ModItems.ORE_BYPRODUCT_ITEMS);
        registerList(STAMP_BOOK, ModItems.STAMP_BOOK_ITEMS);
        registerList(PAGE_OF, ModItems.PAGE_OF_ITEMS);
        registerSparse(CASING, Map.of(
                0, requireLegacyItem("casing_small"),
                1, requireLegacyItem("casing_large"),
                2, requireLegacyItem("casing_small_steel"),
                3, requireLegacyItem("casing_large_steel"),
                4, requireLegacyItem("casing_shotshell"),
                5, requireLegacyItem("casing_buckshot"),
                6, requireLegacyItem("casing_buckshot_advanced")));
        registerSparse(FUEL_ADDITIVE, Map.of(
                0, requireLegacyItem("fuel_additive_antiknock"),
                1, requireLegacyItem("fuel_additive_deicer")));
        registerList(DRILLBIT, ModItems.DRILLBIT_ITEMS);
        registerList(PISTON_SET, ModItems.PISTON_SET_ITEMS);
    }

    @SafeVarargs
    public static void register(ResourceLocation legacyId, RegistryObject<Item>... variantsByMeta) {
        registerList(legacyId, List.of(variantsByMeta));
    }

    public static void registerList(ResourceLocation legacyId, List<RegistryObject<Item>> variantsByMeta) {
        if (ITEM_VARIANTS.containsKey(legacyId)) {
            throw new IllegalStateException("Duplicate legacy item mapping family: " + legacyId);
        }
        LinkedHashMap<Integer, RegistryObject<Item>> variants = new LinkedHashMap<>();
        for (int meta = 0; meta < variantsByMeta.size(); meta++) {
            variants.put(meta, variantsByMeta.get(meta));
        }
        ITEM_VARIANTS.put(legacyId, variants);
    }

    public static void registerSparse(ResourceLocation legacyId, Map<Integer, RegistryObject<Item>> variantsByMeta) {
        if (ITEM_VARIANTS.containsKey(legacyId)) {
            throw new IllegalStateException("Duplicate legacy item mapping family: " + legacyId);
        }
        LinkedHashMap<Integer, RegistryObject<Item>> variants = new LinkedHashMap<>();
        variantsByMeta.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> variants.put(entry.getKey(), entry.getValue()));
        ITEM_VARIANTS.put(legacyId, variants);
    }

    public static Optional<RegistryObject<Item>> item(ResourceLocation legacyId, int legacyMeta) {
        Map<Integer, RegistryObject<Item>> variants = ITEM_VARIANTS.get(legacyId);
        if (variants == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(variants.get(legacyMeta));
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
        Map<Integer, RegistryObject<Item>> variants = ITEM_VARIANTS.get(legacyId);
        return variants == null ? List.of() : List.copyOf(variants.values());
    }

    public static int variantCount(ResourceLocation legacyId) {
        return variants(legacyId).size();
    }

    public static Set<ResourceLocation> legacyIds() {
        return Collections.unmodifiableSet(ITEM_VARIANTS.keySet());
    }

    public static Map<ResourceLocation, List<RegistryObject<Item>>> mappings() {
        Map<ResourceLocation, List<RegistryObject<Item>>> mappings = new LinkedHashMap<>();
        ITEM_VARIANTS.forEach((legacyId, variants) -> mappings.put(legacyId, List.copyOf(variants.values())));
        return Collections.unmodifiableMap(mappings);
    }

    public static Map<ResourceLocation, Map<Integer, RegistryObject<Item>>> mappingsByMeta() {
        Map<ResourceLocation, Map<Integer, RegistryObject<Item>>> mappings = new LinkedHashMap<>();
        ITEM_VARIANTS.forEach((legacyId, variants) -> mappings.put(legacyId, Collections.unmodifiableMap(new LinkedHashMap<>(variants))));
        return Collections.unmodifiableMap(mappings);
    }

    private static RegistryObject<Item> requireLegacyItem(String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        if (item == null) {
            throw new IllegalStateException("Missing modern item for legacy meta mapping: " + name);
        }
        return item;
    }

    private static ResourceLocation hbm(String path) {
        return new ResourceLocation("hbm", path);
    }

    private LegacyMetaItemMappings() {
    }
}
