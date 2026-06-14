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
    public static final ResourceLocation ARC_ELECTRODE = hbm("arc_electrode");
    public static final ResourceLocation ARC_ELECTRODE_BURNT = hbm("arc_electrode_burnt");
    public static final ResourceLocation PA_COIL = hbm("pa_coil");
    public static final ResourceLocation PELLET_RTG_DEPLETED = hbm("pellet_rtg_depleted");
    public static final ResourceLocation HOLOTAPE_IMAGE = hbm("holotape_image");
    public static final ResourceLocation ROD = hbm("rod");
    public static final ResourceLocation ROD_DUAL = hbm("rod_dual");
    public static final ResourceLocation ROD_QUAD = hbm("rod_quad");
    public static final ResourceLocation ROD_ZIRNOX = hbm("rod_zirnox");
    public static final ResourceLocation PWR_FUEL = hbm("pwr_fuel");
    public static final ResourceLocation PWR_FUEL_HOT = hbm("pwr_fuel_hot");
    public static final ResourceLocation PWR_FUEL_DEPLETED = hbm("pwr_fuel_depleted");
    public static final ResourceLocation WATZ_PELLET = hbm("watz_pellet");
    public static final ResourceLocation AMMO_STANDARD = hbm("ammo_standard");

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
                47, requireLegacyItem("plate_cast_arsenic_bronze"),
                7_400, requireLegacyItem("plate_cast_tungsten")));
        registerSparse(PLATE_WELDED, Map.of(
                36, requireLegacyItem("plate_welded_tcalloy"),
                43, requireLegacyItem("plate_welded_cdalloy")));
        registerSparse(WIRE_FINE, Map.of(
                1_300, requireLegacyItem("wire_fine_aluminium"),
                2_900, requireLegacyItem("wire_fine_copper"),
                31, requireLegacyItem("wire_fine_mingrade"),
                7_400, requireLegacyItem("wire_fine_tungsten"),
                7_900, requireLegacyItem("wire_gold")));
        registerSparse(WIRE_DENSE, Map.of(
                2_200, requireLegacyItem("wire_dense_titanium"),
                2_900, requireLegacyItem("wire_dense_copper"),
                4_100, requireLegacyItem("wire_dense_niobium"),
                7_900, requireLegacyItem("wire_dense_gold"),
                31, requireLegacyItem("wire_dense_mingrade"),
                48, requireLegacyItem("wire_dense_bscco"),
                6_000, requireLegacyItem("wire_dense_neodymium")));
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
        registerSparse(AMMO_STANDARD, Map.ofEntries(
                Map.entry(5, requireLegacyItem("ammo_standard_m357_sp")),
                Map.entry(6, requireLegacyItem("ammo_standard_m357_fmj")),
                Map.entry(11, requireLegacyItem("ammo_standard_m44_sp")),
                Map.entry(12, requireLegacyItem("ammo_standard_m44_fmj")),
                Map.entry(20, requireLegacyItem("ammo_standard_p9_sp")),
                Map.entry(21, requireLegacyItem("ammo_standard_p9_fmj")),
                Map.entry(22, requireLegacyItem("ammo_standard_p9_jhp")),
                Map.entry(28, requireLegacyItem("ammo_standard_r762_sp")),
                Map.entry(32, requireLegacyItem("ammo_standard_r762_du")),
                Map.entry(33, requireLegacyItem("ammo_standard_bmg50_sp")),
                Map.entry(34, requireLegacyItem("ammo_standard_bmg50_fmj")),
                Map.entry(35, requireLegacyItem("ammo_standard_bmg50_jhp")),
                Map.entry(36, requireLegacyItem("ammo_standard_bmg50_ap")),
                Map.entry(37, requireLegacyItem("ammo_standard_bmg50_du")),
                Map.entry(41, requireLegacyItem("ammo_standard_g12_bp")),
                Map.entry(42, requireLegacyItem("ammo_standard_g12_bp_magnum")),
                Map.entry(43, requireLegacyItem("ammo_standard_g12_bp_slug")),
                Map.entry(44, requireLegacyItem("ammo_standard_g12")),
                Map.entry(45, requireLegacyItem("ammo_standard_g12_slug")),
                Map.entry(46, requireLegacyItem("ammo_standard_g12_flechette")),
                Map.entry(47, requireLegacyItem("ammo_standard_g12_magnum")),
                Map.entry(48, requireLegacyItem("ammo_standard_g12_explosive")),
                Map.entry(49, requireLegacyItem("ammo_standard_g12_phosphorus")),
                Map.entry(51, requireLegacyItem("ammo_standard_g26_flare_supply")),
                Map.entry(52, requireLegacyItem("ammo_standard_g26_flare_weapon")),
                Map.entry(53, requireLegacyItem("ammo_standard_g40_he")),
                Map.entry(58, requireLegacyItem("ammo_standard_rocket_he")),
                Map.entry(73, requireLegacyItem("ammo_standard_nuke_standard")),
                Map.entry(74, requireLegacyItem("ammo_standard_nuke_demo")),
                Map.entry(75, requireLegacyItem("ammo_standard_nuke_high")),
                Map.entry(76, requireLegacyItem("ammo_standard_nuke_tots")),
                Map.entry(83, requireLegacyItem("ammo_standard_bmg50_he")),
                Map.entry(94, requireLegacyItem("ammo_standard_bmg50_sm"))));
        registerSparse(FUEL_ADDITIVE, Map.of(
                0, requireLegacyItem("fuel_additive_antiknock"),
                1, requireLegacyItem("fuel_additive_deicer")));
        registerList(DRILLBIT, ModItems.DRILLBIT_ITEMS);
        registerList(PISTON_SET, ModItems.PISTON_SET_ITEMS);
        registerList(ARC_ELECTRODE, ModItems.ARC_ELECTRODE_ITEMS.subList(0, 4));
        registerList(ARC_ELECTRODE_BURNT, ModItems.ARC_ELECTRODE_ITEMS.subList(4, 8));
        registerList(PA_COIL, ModItems.PA_COIL_ITEMS);
        register(PELLET_RTG_DEPLETED,
                requireLegacyItem("pellet_rtg_depleted_bismuth"),
                requireLegacyItem("pellet_rtg_depleted_mercury"),
                requireLegacyItem("pellet_rtg_depleted_neptunium"),
                requireLegacyItem("pellet_rtg_depleted_lead"),
                requireLegacyItem("pellet_rtg_depleted_zirconium"),
                requireLegacyItem("pellet_rtg_depleted_nickel"));
        registerSparse(HOLOTAPE_IMAGE, Map.of(
                1, requireLegacyItem("holotape_image_restored")));
        register(ROD,
                requireLegacyItem("rod_lithium"),
                requireLegacyItem("rod_tritium"),
                requireLegacyItem("rod_co"),
                requireLegacyItem("rod_co60"),
                requireLegacyItem("rod_th232"),
                requireLegacyItem("rod_thf"),
                requireLegacyItem("rod_u235"),
                requireLegacyItem("rod_np237"),
                requireLegacyItem("rod_u238"),
                requireLegacyItem("rod_pu238"),
                requireLegacyItem("rod_pu239"),
                requireLegacyItem("rod_rgp"),
                requireLegacyItem("rod_waste"),
                requireLegacyItem("rod_lead"),
                requireLegacyItem("rod_uranium"),
                requireLegacyItem("rod_ra226"),
                requireLegacyItem("rod_ac227"));
        register(ROD_DUAL,
                requireLegacyItem("rod_dual_lithium"),
                requireLegacyItem("rod_dual_tritium"),
                requireLegacyItem("rod_dual_co"),
                requireLegacyItem("rod_dual_co60"),
                requireLegacyItem("rod_dual_th232"),
                requireLegacyItem("rod_dual_thf"),
                requireLegacyItem("rod_dual_u235"),
                requireLegacyItem("rod_dual_np237"),
                requireLegacyItem("rod_dual_u238"),
                requireLegacyItem("rod_dual_pu238"),
                requireLegacyItem("rod_dual_pu239"),
                requireLegacyItem("rod_dual_rgp"),
                requireLegacyItem("rod_dual_waste"),
                requireLegacyItem("rod_dual_lead"),
                requireLegacyItem("rod_dual_uranium"),
                requireLegacyItem("rod_dual_ra226"),
                requireLegacyItem("rod_dual_ac227"));
        register(ROD_QUAD,
                requireLegacyItem("rod_quad_lithium"),
                requireLegacyItem("rod_quad_tritium"),
                requireLegacyItem("rod_quad_co"),
                requireLegacyItem("rod_quad_co60"),
                requireLegacyItem("rod_quad_th232"),
                requireLegacyItem("rod_quad_thf"),
                requireLegacyItem("rod_quad_u235"),
                requireLegacyItem("rod_quad_np237"),
                requireLegacyItem("rod_quad_u238"),
                requireLegacyItem("rod_quad_pu238"),
                requireLegacyItem("rod_quad_pu239"),
                requireLegacyItem("rod_quad_rgp"),
                requireLegacyItem("rod_quad_waste"),
                requireLegacyItem("rod_quad_lead"),
                requireLegacyItem("rod_quad_uranium"),
                requireLegacyItem("rod_quad_ra226"),
                requireLegacyItem("rod_quad_ac227"));
        register(ROD_ZIRNOX,
                requireLegacyItem("rod_zirnox_natural_uranium_fuel"),
                requireLegacyItem("rod_zirnox_uranium_fuel"),
                requireLegacyItem("rod_zirnox_th232"),
                requireLegacyItem("rod_zirnox_thorium_fuel"),
                requireLegacyItem("rod_zirnox_mox_fuel"),
                requireLegacyItem("rod_zirnox_plutonium_fuel"),
                requireLegacyItem("rod_zirnox_u233_fuel"),
                requireLegacyItem("rod_zirnox_u235_fuel"),
                requireLegacyItem("rod_zirnox_les_fuel"),
                requireLegacyItem("rod_zirnox_lithium"),
                requireLegacyItem("rod_zirnox_zfb_mox"));
        registerList(PWR_FUEL, ModItems.PWR_FUEL_ITEMS);
        registerList(PWR_FUEL_HOT, ModItems.PWR_FUEL_HOT_ITEMS);
        registerList(PWR_FUEL_DEPLETED, ModItems.PWR_FUEL_DEPLETED_ITEMS);
        registerList(WATZ_PELLET, ModItems.WATZ_PELLET_ITEMS);
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
