package com.hbm.ntm.recipe;

import com.hbm.ntm.item.NuclearWasteItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class LegacyMetaItemMappings {
    public static final ResourceLocation BATTERY_PACK = hbm("battery_pack");
    public static final ResourceLocation BATTERY_SC = hbm("battery_sc");
    public static final ResourceLocation CIRCUIT = hbm("circuit");
    public static final ResourceLocation CIRCUIT_STAR_PIECE = hbm("circuit_star_piece");
    public static final ResourceLocation CIRCUIT_STAR_COMPONENT = hbm("circuit_star_component");
    public static final ResourceLocation INGOT_METAL = hbm("ingot_metal");
    public static final ResourceLocation CHEMICAL_DYE = hbm("chemical_dye");
    public static final ResourceLocation BLUEPRINT_FOLDER = hbm("blueprint_folder");
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
    public static final ResourceLocation PLANT_FLOWER = hbm("plant_flower");
    public static final ResourceLocation PLANT_TALL = hbm("plant_tall");
    public static final ResourceLocation PARTS_LEGENDARY = hbm("parts_legendary");
    public static final ResourceLocation PART_GENERIC = hbm("part_generic");
    public static final ResourceLocation ITEM_EXPENSIVE = hbm("item_expensive");
    public static final ResourceLocation ORE_BYPRODUCT = hbm("ore_byproduct");
    public static final ResourceLocation STAMP_BOOK = hbm("stamp_book");
    public static final ResourceLocation PAGE_OF = hbm("page_of_");
    public static final ResourceLocation CASING = hbm("casing");
    public static final ResourceLocation GRENADE_SHELL = hbm("grenade_shell");
    public static final ResourceLocation GRENADE_FUZE = hbm("grenade_fuze");
    public static final ResourceLocation GRENADE_FILLING = hbm("grenade_filling");
    public static final ResourceLocation GRENADE_EXTRA = hbm("grenade_extra");
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
    public static final ResourceLocation AMMO_SECRET = hbm("ammo_secret");
    public static final ResourceLocation FUSION_COMPONENT = hbm("fusion_component");
    public static final ResourceLocation WEAPON_MOD_SPECIAL = hbm("weapon_mod_special");
    public static final ResourceLocation CANNED_CONSERVE = hbm("canned_conserve");
    public static final ResourceLocation APPLE_LEAD = hbm("apple_lead");
    public static final ResourceLocation APPLE_SCHRABIDIUM = hbm("apple_schrabidium");
    public static final ResourceLocation ITEM_SECRET = hbm("item_secret");
    public static final ResourceLocation INGOT_STEEL_DUSTED = hbm("ingot_steel_dusted");
    public static final ResourceLocation MOTOR = hbm("motor");
    public static final ResourceLocation MOTOR_DESH = hbm("motor_desh");
    public static final ResourceLocation REACTOR_CORE = hbm("reactor_core");
    public static final ResourceLocation INGOT_CFT = hbm("ingot_cft");
    public static final ResourceLocation ROD_QUAD_EMPTY = hbm("rod_quad_empty");
    public static final ResourceLocation NUCLEAR_WASTE_LONG = hbm("nuclear_waste_long");
    public static final ResourceLocation NUCLEAR_WASTE_LONG_TINY = hbm("nuclear_waste_long_tiny");
    public static final ResourceLocation NUCLEAR_WASTE_LONG_DEPLETED = hbm("nuclear_waste_long_depleted");
    public static final ResourceLocation NUCLEAR_WASTE_LONG_DEPLETED_TINY = hbm("nuclear_waste_long_depleted_tiny");
    public static final ResourceLocation NUCLEAR_WASTE_SHORT = hbm("nuclear_waste_short");
    public static final ResourceLocation NUCLEAR_WASTE_SHORT_TINY = hbm("nuclear_waste_short_tiny");
    public static final ResourceLocation NUCLEAR_WASTE_SHORT_DEPLETED = hbm("nuclear_waste_short_depleted");
    public static final ResourceLocation NUCLEAR_WASTE_SHORT_DEPLETED_TINY = hbm("nuclear_waste_short_depleted_tiny");
    public static final ResourceLocation TILE_SAND_MIX = hbm("tile.sand_mix");
    public static final ResourceLocation TILE_GLASS_QUARTZ = hbm("tile.glass_quartz");
    public static final ResourceLocation TILE_STONE_RESOURCE = hbm("tile.stone_resource");

    private static final Map<ResourceLocation, LinkedHashMap<Integer, RegistryObject<Item>>> ITEM_VARIANTS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, LinkedHashMap<Integer, ItemLike>> VANILLA_META_VARIANTS = new LinkedHashMap<>();

    static {
        registerVanillaMeta(new ResourceLocation("minecraft", "log"), Map.of(
                3, Items.JUNGLE_LOG));
        registerVanillaMeta(new ResourceLocation("minecraft", "coal"), Map.of(
                1, Items.CHARCOAL));
        registerVanillaMeta(new ResourceLocation("minecraft", "dye"), Map.of(
                4, Items.LAPIS_LAZULI));
        registerVanillaMeta(new ResourceLocation("minecraft", "fish"), Map.of(
                0, Items.COD,
                1, Items.SALMON,
                2, Items.TROPICAL_FISH,
                3, Items.PUFFERFISH));

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
        registerList(CIRCUIT_STAR_PIECE, ModItems.CIRCUIT_STAR_PIECE_ITEMS);
        registerList(CIRCUIT_STAR_COMPONENT, ModItems.CIRCUIT_STAR_COMPONENT_ITEMS);
        registerList(INGOT_METAL, ModItems.INGOT_METAL_ITEMS);
        registerList(CHEMICAL_DYE, ModItems.CHEMICAL_DYE_ITEMS);
        register(BLUEPRINT_FOLDER,
                ModItems.BLUEPRINT_FOLDER,
                ModItems.BLUEPRINT_FOLDER_DISCOVER,
                ModItems.BLUEPRINT_FOLDER_SECRET);
        registerSparse(PLATE_CAST, Map.of(
                39, requireLegacyItem("plate_cast_combine_steel"),
                46, requireLegacyItem("plate_cast_bismuth_bronze"),
                47, requireLegacyItem("plate_cast_arsenic_bronze"),
                7_400, requireLegacyItem("plate_cast_tungsten")));
        registerSparse(PLATE_WELDED, sparseMap(
                2_600, requireLegacyItem("plate_welded_iron"),
                30, requireLegacyItem("plate_welded_steel"),
                2_900, requireLegacyItem("plate_welded_copper"),
                2_200, requireLegacyItem("plate_welded_titanium"),
                4_000, requireLegacyItem("plate_welded_zirconium"),
                1_300, requireLegacyItem("plate_welded_aluminium"),
                36, requireLegacyItem("plate_welded_tcalloy"),
                43, requireLegacyItem("plate_welded_cdalloy"),
                7_400, requireLegacyItem("plate_welded_tungsten"),
                39, requireLegacyItem("plate_welded_combine_steel"),
                7_699, requireLegacyItem("plate_welded_osmiridium")));
        registerSparse(WIRE_FINE, sparseMap(
                699, requireLegacyItem("wire_fine_carbon"),
                1_300, requireLegacyItem("wire_fine_aluminium"),
                2_900, requireLegacyItem("wire_fine_copper"),
                30, requireLegacyItem("wire_fine_steel"),
                31, requireLegacyItem("wire_fine_mingrade"),
                38, requireLegacyItem("wire_fine_magnetized_tungsten"),
                4_000, requireLegacyItem("wire_fine_zirconium"),
                7_400, requireLegacyItem("wire_fine_tungsten"),
                7_900, requireLegacyItem("wire_gold"),
                8_200, requireLegacyItem("wire_fine_lead"),
                12_626, requireLegacyItem("wire_fine_schrabidium")));
        registerSparse(WIRE_DENSE, Map.of(
                2_200, requireLegacyItem("wire_dense_titanium"),
                2_900, requireLegacyItem("wire_dense_copper"),
                4_100, requireLegacyItem("wire_dense_niobium"),
                7_900, requireLegacyItem("wire_dense_gold"),
                31, requireLegacyItem("wire_dense_mingrade"),
                38, requireLegacyItem("wire_dense_magnetized_tungsten"),
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
        register(GRENADE_SHELL,
                requireLegacyItem("grenade_shell_frag"),
                requireLegacyItem("grenade_shell_stick"),
                requireLegacyItem("grenade_shell_tech"),
                requireLegacyItem("grenade_shell_nuke"));
        register(GRENADE_FUZE,
                requireLegacyItem("grenade_fuze_s3"),
                requireLegacyItem("grenade_fuze_s7"),
                requireLegacyItem("grenade_fuze_s15"),
                requireLegacyItem("grenade_fuze_impact"),
                requireLegacyItem("grenade_fuze_airburst"));
        register(GRENADE_FILLING,
                requireLegacyItem("grenade_filling_powder"),
                requireLegacyItem("grenade_filling_he"),
                requireLegacyItem("grenade_filling_demo"),
                requireLegacyItem("grenade_filling_inc"),
                requireLegacyItem("grenade_filling_wp"),
                requireLegacyItem("grenade_filling_cluster"),
                requireLegacyItem("grenade_filling_emp"),
                requireLegacyItem("grenade_filling_plasma"),
                requireLegacyItem("grenade_filling_laser"),
                requireLegacyItem("grenade_filling_cluster_heavy"),
                requireLegacyItem("grenade_filling_nuclear"),
                requireLegacyItem("grenade_filling_nuclear_demo"),
                requireLegacyItem("grenade_filling_schrab"));
        register(GRENADE_EXTRA,
                requireLegacyItem("grenade_extra_glue"),
                requireLegacyItem("grenade_extra_proxy_fuze"),
                requireLegacyItem("grenade_extra_frag_sleeve"),
                requireLegacyItem("grenade_extra_triplex"));
        registerSparse(AMMO_STANDARD, Map.ofEntries(
                Map.entry(0, requireLegacyItem("ammo_standard_stone")),
                Map.entry(1, requireLegacyItem("ammo_standard_stone_ap")),
                Map.entry(2, requireLegacyItem("ammo_standard_stone_iron")),
                Map.entry(3, requireLegacyItem("ammo_standard_stone_shot")),
                Map.entry(4, requireLegacyItem("ammo_standard_m357_bp")),
                Map.entry(5, requireLegacyItem("ammo_standard_m357_sp")),
                Map.entry(6, requireLegacyItem("ammo_standard_m357_fmj")),
                Map.entry(7, requireLegacyItem("ammo_standard_m357_jhp")),
                Map.entry(8, requireLegacyItem("ammo_standard_m357_ap")),
                Map.entry(9, requireLegacyItem("ammo_standard_m357_express")),
                Map.entry(10, requireLegacyItem("ammo_standard_m44_bp")),
                Map.entry(11, requireLegacyItem("ammo_standard_m44_sp")),
                Map.entry(12, requireLegacyItem("ammo_standard_m44_fmj")),
                Map.entry(13, requireLegacyItem("ammo_standard_m44_jhp")),
                Map.entry(14, requireLegacyItem("ammo_standard_m44_ap")),
                Map.entry(15, requireLegacyItem("ammo_standard_m44_express")),
                Map.entry(16, requireLegacyItem("ammo_standard_p22_sp")),
                Map.entry(17, requireLegacyItem("ammo_standard_p22_fmj")),
                Map.entry(18, requireLegacyItem("ammo_standard_p22_jhp")),
                Map.entry(19, requireLegacyItem("ammo_standard_p22_ap")),
                Map.entry(20, requireLegacyItem("ammo_standard_p9_sp")),
                Map.entry(21, requireLegacyItem("ammo_standard_p9_fmj")),
                Map.entry(22, requireLegacyItem("ammo_standard_p9_jhp")),
                Map.entry(23, requireLegacyItem("ammo_standard_p9_ap")),
                Map.entry(24, requireLegacyItem("ammo_standard_r556_sp")),
                Map.entry(25, requireLegacyItem("ammo_standard_r556_fmj")),
                Map.entry(26, requireLegacyItem("ammo_standard_r556_jhp")),
                Map.entry(27, requireLegacyItem("ammo_standard_r556_ap")),
                Map.entry(28, requireLegacyItem("ammo_standard_r762_sp")),
                Map.entry(29, requireLegacyItem("ammo_standard_r762_fmj")),
                Map.entry(30, requireLegacyItem("ammo_standard_r762_jhp")),
                Map.entry(31, requireLegacyItem("ammo_standard_r762_ap")),
                Map.entry(32, requireLegacyItem("ammo_standard_r762_du")),
                Map.entry(33, requireLegacyItem("ammo_standard_bmg50_sp")),
                Map.entry(34, requireLegacyItem("ammo_standard_bmg50_fmj")),
                Map.entry(35, requireLegacyItem("ammo_standard_bmg50_jhp")),
                Map.entry(36, requireLegacyItem("ammo_standard_bmg50_ap")),
                Map.entry(37, requireLegacyItem("ammo_standard_bmg50_du")),
                Map.entry(38, requireLegacyItem("ammo_standard_b75")),
                Map.entry(39, requireLegacyItem("ammo_standard_b75_inc")),
                Map.entry(40, requireLegacyItem("ammo_standard_b75_exp")),
                Map.entry(41, requireLegacyItem("ammo_standard_g12_bp")),
                Map.entry(42, requireLegacyItem("ammo_standard_g12_bp_magnum")),
                Map.entry(43, requireLegacyItem("ammo_standard_g12_bp_slug")),
                Map.entry(44, requireLegacyItem("ammo_standard_g12")),
                Map.entry(45, requireLegacyItem("ammo_standard_g12_slug")),
                Map.entry(46, requireLegacyItem("ammo_standard_g12_flechette")),
                Map.entry(47, requireLegacyItem("ammo_standard_g12_magnum")),
                Map.entry(48, requireLegacyItem("ammo_standard_g12_explosive")),
                Map.entry(49, requireLegacyItem("ammo_standard_g12_phosphorus")),
                Map.entry(50, requireLegacyItem("ammo_standard_g26_flare")),
                Map.entry(51, requireLegacyItem("ammo_standard_g26_flare_supply")),
                Map.entry(52, requireLegacyItem("ammo_standard_g26_flare_weapon")),
                Map.entry(53, requireLegacyItem("ammo_standard_g40_he")),
                Map.entry(54, requireLegacyItem("ammo_standard_g40_heat")),
                Map.entry(55, requireLegacyItem("ammo_standard_g40_demo")),
                Map.entry(56, requireLegacyItem("ammo_standard_g40_inc")),
                Map.entry(57, requireLegacyItem("ammo_standard_g40_phosphorus")),
                Map.entry(58, requireLegacyItem("ammo_standard_rocket_he")),
                Map.entry(59, requireLegacyItem("ammo_standard_rocket_heat")),
                Map.entry(60, requireLegacyItem("ammo_standard_rocket_demo")),
                Map.entry(61, requireLegacyItem("ammo_standard_rocket_inc")),
                Map.entry(62, requireLegacyItem("ammo_standard_rocket_phosphorus")),
                Map.entry(63, requireLegacyItem("ammo_standard_flame_diesel")),
                Map.entry(64, requireLegacyItem("ammo_standard_flame_gas")),
                Map.entry(65, requireLegacyItem("ammo_standard_flame_napalm")),
                Map.entry(66, requireLegacyItem("ammo_standard_flame_balefire")),
                Map.entry(67, requireLegacyItem("ammo_standard_capacitor")),
                Map.entry(68, requireLegacyItem("ammo_standard_capacitor_overcharge")),
                Map.entry(69, requireLegacyItem("ammo_standard_capacitor_ir")),
                Map.entry(70, requireLegacyItem("ammo_standard_tau_uranium")),
                Map.entry(71, requireLegacyItem("ammo_standard_coil_tungsten")),
                Map.entry(72, requireLegacyItem("ammo_standard_coil_ferrouranium")),
                Map.entry(73, requireLegacyItem("ammo_standard_nuke_standard")),
                Map.entry(74, requireLegacyItem("ammo_standard_nuke_demo")),
                Map.entry(75, requireLegacyItem("ammo_standard_nuke_high")),
                Map.entry(76, requireLegacyItem("ammo_standard_nuke_tots")),
                Map.entry(77, requireLegacyItem("ammo_standard_nuke_hive")),
                Map.entry(78, requireLegacyItem("ammo_standard_g10")),
                Map.entry(79, requireLegacyItem("ammo_standard_g10_shrapnel")),
                Map.entry(80, requireLegacyItem("ammo_standard_g10_du")),
                Map.entry(81, requireLegacyItem("ammo_standard_g10_slug")),
                Map.entry(82, requireLegacyItem("ammo_standard_r762_he")),
                Map.entry(83, requireLegacyItem("ammo_standard_bmg50_he")),
                Map.entry(84, requireLegacyItem("ammo_standard_g10_explosive")),
                Map.entry(85, requireLegacyItem("ammo_standard_p45_sp")),
                Map.entry(86, requireLegacyItem("ammo_standard_p45_fmj")),
                Map.entry(87, requireLegacyItem("ammo_standard_p45_jhp")),
                Map.entry(88, requireLegacyItem("ammo_standard_p45_ap")),
                Map.entry(89, requireLegacyItem("ammo_standard_p45_du")),
                Map.entry(90, requireLegacyItem("ammo_standard_ct_hook")),
                Map.entry(91, requireLegacyItem("ammo_standard_ct_mortar")),
                Map.entry(92, requireLegacyItem("ammo_standard_ct_mortar_charge")),
                Map.entry(93, requireLegacyItem("ammo_standard_nuke_balefire")),
                Map.entry(94, requireLegacyItem("ammo_standard_bmg50_sm"))));
        registerSparse(AMMO_SECRET, Map.of(
                0, requireLegacyItem("ammo_secret_folly_sm"),
                1, requireLegacyItem("ammo_secret_folly_nuke"),
                5, requireLegacyItem("ammo_secret_p35_800"),
                6, requireLegacyItem("ammo_secret_bmg50_black"),
                7, requireLegacyItem("ammo_secret_p35_800_bl")));
        registerSparse(WEAPON_MOD_SPECIAL, Map.of(
                0, requireLegacyItem("weapon_mod_special_silencer"),
                1, requireLegacyItem("weapon_mod_special_scope"),
                7, requireLegacyItem("weapon_mod_special_furniture_black")));
        registerSparse(CANNED_CONSERVE, Map.ofEntries(
                Map.entry(0, requireLegacyItem("canned_beef")),
                Map.entry(1, requireLegacyItem("canned_tuna")),
                Map.entry(2, requireLegacyItem("canned_mystery")),
                Map.entry(3, requireLegacyItem("canned_pashtet")),
                Map.entry(4, requireLegacyItem("canned_cheese")),
                Map.entry(5, requireLegacyItem("canned_slime")),
                Map.entry(6, requireLegacyItem("canned_milk")),
                Map.entry(7, requireLegacyItem("canned_ass")),
                Map.entry(8, requireLegacyItem("canned_pizza")),
                Map.entry(9, requireLegacyItem("canned_tube")),
                Map.entry(10, requireLegacyItem("canned_tomato")),
                Map.entry(11, requireLegacyItem("canned_asbestos")),
                Map.entry(12, requireLegacyItem("canned_bhole")),
                Map.entry(13, requireLegacyItem("canned_hotdogs")),
                Map.entry(14, requireLegacyItem("canned_leftovers")),
                Map.entry(15, requireLegacyItem("canned_yogurt")),
                Map.entry(16, requireLegacyItem("canned_stew")),
                Map.entry(17, requireLegacyItem("canned_chinese")),
                Map.entry(18, requireLegacyItem("canned_oil")),
                Map.entry(19, requireLegacyItem("canned_fist")),
                Map.entry(20, requireLegacyItem("canned_spam")),
                Map.entry(21, requireLegacyItem("canned_fried")),
                Map.entry(22, requireLegacyItem("canned_napalm")),
                Map.entry(23, requireLegacyItem("canned_diesel")),
                Map.entry(24, requireLegacyItem("canned_kerosene")),
                Map.entry(25, requireLegacyItem("canned_recursion")),
                Map.entry(26, requireLegacyItem("canned_bark"))));
        register(APPLE_LEAD,
                ModItems.APPLE_LEAD,
                ModItems.APPLE_LEAD_INGOT,
                ModItems.APPLE_LEAD_BLOCK);
        register(APPLE_SCHRABIDIUM,
                ModItems.APPLE_SCHRABIDIUM,
                ModItems.APPLE_SCHRABIDIUM_INGOT,
                ModItems.APPLE_SCHRABIDIUM_BLOCK);
        registerSparse(ITEM_SECRET, Map.of(
                2, requireLegacyItem("item_secret_selenium_steel")));
        registerSparse(MOTOR, Map.of(
                0, ModItems.MOTOR));
        registerSparse(MOTOR_DESH, Map.of(
                -1, requireLegacyItem("motor_desh")));
        registerSparse(REACTOR_CORE, Map.of(
                -1, requireLegacyItem("reactor_core")));
        registerSparse(INGOT_CFT, Map.of(
                -1, requireLegacyItem("ingot_cft")));
        registerList(NUCLEAR_WASTE_LONG, repeated(requireLegacyItem("nuclear_waste_long"), 5));
        registerList(NUCLEAR_WASTE_LONG_TINY, repeated(requireLegacyItem("nuclear_waste_long_tiny"), 5));
        registerList(NUCLEAR_WASTE_LONG_DEPLETED, repeated(requireLegacyItem("nuclear_waste_long_depleted"), 5));
        registerList(NUCLEAR_WASTE_LONG_DEPLETED_TINY,
                repeated(requireLegacyItem("nuclear_waste_long_depleted_tiny"), 5));
        registerList(NUCLEAR_WASTE_SHORT, repeated(requireLegacyItem("nuclear_waste_short"), 8));
        registerList(NUCLEAR_WASTE_SHORT_TINY, repeated(requireLegacyItem("nuclear_waste_short_tiny"), 8));
        registerList(NUCLEAR_WASTE_SHORT_DEPLETED, repeated(requireLegacyItem("nuclear_waste_short_depleted"), 8));
        registerList(NUCLEAR_WASTE_SHORT_DEPLETED_TINY,
                repeated(requireLegacyItem("nuclear_waste_short_depleted_tiny"), 8));
        registerDamageBacked("rbmk_pellet_ueu", 10);
        registerDamageBacked("rbmk_pellet_meu", 10);
        registerDamageBacked("rbmk_pellet_heu233", 10);
        registerDamageBacked("rbmk_pellet_heu235", 10);
        registerDamageBacked("rbmk_pellet_uzh", 10);
        registerDamageBacked("rbmk_pellet_thmeu", 10);
        registerDamageBacked("rbmk_pellet_lep", 10);
        registerDamageBacked("rbmk_pellet_mep", 10);
        registerDamageBacked("rbmk_pellet_hep239", 10);
        registerDamageBacked("rbmk_pellet_hep241", 10);
        registerDamageBacked("rbmk_pellet_men", 10);
        registerDamageBacked("rbmk_pellet_hen", 10);
        registerDamageBacked("rbmk_pellet_mox", 10);
        registerDamageBacked("rbmk_pellet_leaus", 10);
        registerDamageBacked("rbmk_pellet_heaus", 10);
        registerDamageBacked("rbmk_pellet_les", 10);
        registerDamageBacked("rbmk_pellet_mes", 10);
        registerDamageBacked("rbmk_pellet_hes", 10);
        registerDamageBacked("rbmk_pellet_balefire", 5);
        registerDamageBacked("rbmk_pellet_balefire_gold", 5);
        registerDamageBacked("rbmk_pellet_flashlead", 5);
        registerDamageBacked("rbmk_pellet_po210be", 5);
        registerDamageBacked("rbmk_pellet_pu238be", 10);
        registerDamageBacked("rbmk_pellet_ra226be", 5);
        registerDamageBacked("rbmk_pellet_drx", 10);
        registerDamageBacked("rbmk_pellet_zfb_bismuth", 10);
        registerDamageBacked("rbmk_pellet_zfb_pu241", 10);
        registerDamageBacked("rbmk_pellet_zfb_am_mix", 10);
        registerList(DRILLBIT, ModItems.DRILLBIT_ITEMS);
        registerList(PISTON_SET, ModItems.PISTON_SET_ITEMS);
        registerList(ARC_ELECTRODE, ModItems.ARC_ELECTRODE_ITEMS.subList(0, 4));
        registerList(ARC_ELECTRODE_BURNT, ModItems.ARC_ELECTRODE_ITEMS.subList(4, 8));
        registerList(PA_COIL, ModItems.PA_COIL_ITEMS);
        registerSparse(HOLOTAPE_IMAGE, Map.ofEntries(
                Map.entry(0, requireLegacyItem("holotape_image_digamma")),
                Map.entry(1, requireLegacyItem("holotape_image_restored")),
                Map.entry(2, requireLegacyItem("holotape_image_fe_hall")),
                Map.entry(3, requireLegacyItem("holotape_image_fe_corridor")),
                Map.entry(4, requireLegacyItem("holotape_image_fe_server")),
                Map.entry(5, requireLegacyItem("holotape_image_feh_dome")),
                Map.entry(6, requireLegacyItem("holotape_image_feh_boat")),
                Map.entry(7, requireLegacyItem("holotape_image_feh_lsc")),
                Map.entry(8, requireLegacyItem("holotape_image_f3_rc")),
                Map.entry(9, requireLegacyItem("holotape_image_f3_iv")),
                Map.entry(10, requireLegacyItem("holotape_image_f3_wm")),
                Map.entry(11, requireLegacyItem("holotape_image_nv_crater")),
                Map.entry(12, requireLegacyItem("holotape_image_nv_divide")),
                Map.entry(13, requireLegacyItem("holotape_image_nv_bm")),
                Map.entry(14, requireLegacyItem("holotape_image_o_1")),
                Map.entry(15, requireLegacyItem("holotape_image_o_2")),
                Map.entry(16, requireLegacyItem("holotape_image_o_3")),
                Map.entry(17, requireLegacyItem("holotape_image_challenge"))));
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
        registerList(INGOT_STEEL_DUSTED, ModItems.INGOT_STEEL_DUSTED_ITEMS);
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

    @SuppressWarnings("unchecked")
    private static Map<Integer, RegistryObject<Item>> sparseMap(Object... metaItemPairs) {
        if (metaItemPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Sparse legacy item map requires meta/item pairs");
        }
        LinkedHashMap<Integer, RegistryObject<Item>> variants = new LinkedHashMap<>();
        for (int i = 0; i < metaItemPairs.length; i += 2) {
            variants.put((Integer) metaItemPairs[i], (RegistryObject<Item>) metaItemPairs[i + 1]);
        }
        return variants;
    }

    public static Optional<RegistryObject<Item>> item(ResourceLocation legacyId, int legacyMeta) {
        Map<Integer, RegistryObject<Item>> variants = ITEM_VARIANTS.get(legacyId);
        if (variants == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(variants.get(legacyMeta));
    }

    /**
     * Returns whether the old item ID/meta pair has a modern counterpart. Unlike {@link #item(ResourceLocation, int)},
     * this also covers old vanilla metadata which has no {@link RegistryObject} carrier in the port.
     */
    public static boolean hasMapping(ResourceLocation legacyId, int legacyMeta) {
        return vanillaStack(legacyId, legacyMeta, 1).isPresent() || item(legacyId, legacyMeta).isPresent();
    }

    public static RegistryObject<Item> requireItem(ResourceLocation legacyId, int legacyMeta) {
        return item(legacyId, legacyMeta)
                .orElseThrow(() -> new IllegalStateException("Missing legacy item mapping: " + legacyId + " meta " + legacyMeta));
    }

    public static Optional<ItemStack> stack(ResourceLocation legacyId, int legacyMeta, int count) {
        return stack(legacyId, legacyMeta, count, true);
    }

    public static Optional<ItemStack> stackPreservingCount(ResourceLocation legacyId, int legacyMeta, int count) {
        return stack(legacyId, legacyMeta, count, false);
    }

    private static Optional<ItemStack> stack(ResourceLocation legacyId, int legacyMeta, int count, boolean clampCount) {
        int stackCount = clampCount ? Math.max(1, count) : count;
        Optional<ItemStack> vanillaStack = vanillaStack(legacyId, legacyMeta, stackCount);
        if (vanillaStack.isPresent()) {
            return vanillaStack;
        }
        return item(legacyId, legacyMeta).map(item -> {
            Item resolved = item.get();
            if (resolved instanceof NuclearWasteItem) {
                ItemStack stack = NuclearWasteItem.stack(resolved, legacyMeta, stackCount);
                if (!clampCount) {
                    stack.setCount(count);
                }
                return stack;
            }
            ItemStack stack = new ItemStack(resolved, stackCount);
            if (isDamageValueBacked(legacyId)) {
                stack.setDamageValue(Math.max(0, legacyMeta));
            }
            return stack;
        });
    }

    public static Optional<LegacyStackIdentity> legacyIdentity(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        for (Map.Entry<ResourceLocation, LinkedHashMap<Integer, ItemLike>> family : VANILLA_META_VARIANTS.entrySet()) {
            for (Map.Entry<Integer, ItemLike> variant : family.getValue().entrySet()) {
                if (stack.getItem() == variant.getValue().asItem()) {
                    return Optional.of(new LegacyStackIdentity(family.getKey(), variant.getKey()));
                }
            }
        }

        List<LegacyStackIdentity> matches = new ArrayList<>();
        for (Map.Entry<ResourceLocation, LinkedHashMap<Integer, RegistryObject<Item>>> family : ITEM_VARIANTS.entrySet()) {
            for (Map.Entry<Integer, RegistryObject<Item>> variant : family.getValue().entrySet()) {
                if (stack.getItem() == variant.getValue().get()) {
                    matches.add(new LegacyStackIdentity(family.getKey(), variant.getKey()));
                }
            }
        }
        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        }

        List<LegacyStackIdentity> damageMatches = matches.stream()
                .filter(identity -> isDamageValueBacked(identity.legacyId())
                        && identity.legacyMeta() == stack.getDamageValue())
                .toList();
        return damageMatches.size() == 1 ? Optional.of(damageMatches.get(0)) : Optional.empty();
    }

    public static List<ItemStack> stacks(ResourceLocation legacyId, int count) {
        Map<Integer, ItemLike> vanillaVariants = VANILLA_META_VARIANTS.get(legacyId);
        if (vanillaVariants != null) {
            int safeCount = Math.max(1, count);
            return vanillaVariants.keySet().stream()
                    .map(meta -> vanillaStack(legacyId, meta, safeCount).orElse(ItemStack.EMPTY))
                    .filter(stack -> !stack.isEmpty())
                    .toList();
        }
        Map<Integer, RegistryObject<Item>> variants = ITEM_VARIANTS.get(legacyId);
        if (variants == null) {
            return List.of();
        }
        int safeCount = Math.max(1, count);
        return variants.keySet().stream()
                .map(meta -> stack(legacyId, meta, safeCount).orElse(ItemStack.EMPTY))
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    public static Optional<ItemLike> itemLike(ResourceLocation legacyId, int legacyMeta) {
        Optional<ItemStack> vanillaStack = vanillaStack(legacyId, legacyMeta, 1);
        if (vanillaStack.isPresent()) {
            return Optional.of(vanillaStack.get().getItem());
        }
        return item(legacyId, legacyMeta).map(RegistryObject::get);
    }

    public static List<RegistryObject<Item>> variants(ResourceLocation legacyId) {
        Map<Integer, RegistryObject<Item>> variants = ITEM_VARIANTS.get(legacyId);
        return variants == null ? List.of() : List.copyOf(variants.values());
    }

    public static int variantCount(ResourceLocation legacyId) {
        Map<Integer, ItemLike> vanillaVariants = VANILLA_META_VARIANTS.get(legacyId);
        return vanillaVariants != null ? vanillaVariants.size() : variants(legacyId).size();
    }

    public static boolean isDamageValueBacked(ResourceLocation legacyId) {
        Map<Integer, RegistryObject<Item>> variants = ITEM_VARIANTS.get(legacyId);
        if (variants == null || variants.size() <= 1) {
            return false;
        }
        RegistryObject<Item> first = null;
        for (RegistryObject<Item> variant : variants.values()) {
            if (first == null) {
                first = variant;
            } else if (first != variant) {
                return false;
            }
        }
        return true;
    }

    public static Set<ResourceLocation> legacyIds() {
        LinkedHashSet<ResourceLocation> ids = new LinkedHashSet<>(VANILLA_META_VARIANTS.keySet());
        ids.addAll(ITEM_VARIANTS.keySet());
        return Collections.unmodifiableSet(ids);
    }

    /**
     * Returns resolved modern stacks keyed by their original metadata. This is the carrier-neutral view for commands,
     * diagnostics and importers; {@link #mappingsByMeta()} intentionally remains a RegistryObject-only compatibility API.
     */
    public static Map<Integer, ItemStack> stacksByMeta(ResourceLocation legacyId, int count) {
        Map<Integer, ItemLike> vanillaVariants = VANILLA_META_VARIANTS.get(legacyId);
        LinkedHashMap<Integer, ItemStack> stacks = new LinkedHashMap<>();
        if (vanillaVariants != null) {
            vanillaVariants.keySet().stream().sorted()
                    .forEach(meta -> vanillaStack(legacyId, meta, count).ifPresent(stack -> stacks.put(meta, stack)));
        } else {
            Map<Integer, RegistryObject<Item>> variants = ITEM_VARIANTS.get(legacyId);
            if (variants != null) {
                variants.keySet().stream().sorted()
                        .forEach(meta -> stack(legacyId, meta, count).ifPresent(stack -> stacks.put(meta, stack)));
            }
        }
        return Collections.unmodifiableMap(stacks);
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

    private static void registerVanillaMeta(ResourceLocation legacyId, Map<Integer, ? extends ItemLike> variantsByMeta) {
        if (VANILLA_META_VARIANTS.containsKey(legacyId)) {
            throw new IllegalStateException("Duplicate vanilla legacy item mapping family: " + legacyId);
        }
        LinkedHashMap<Integer, ItemLike> variants = new LinkedHashMap<>();
        variantsByMeta.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> variants.put(entry.getKey(), entry.getValue()));
        VANILLA_META_VARIANTS.put(legacyId, variants);
    }

    private static Optional<ItemStack> vanillaStack(ResourceLocation legacyId, int legacyMeta, int count) {
        Map<Integer, ItemLike> variants = VANILLA_META_VARIANTS.get(legacyId);
        if (variants == null) {
            return Optional.empty();
        }
        ItemLike item = variants.get(legacyMeta);
        return item == null ? Optional.empty() : Optional.of(new ItemStack(item, count));
    }

    private static void registerDamageBacked(String legacyName, int variants) {
        registerList(hbm(legacyName), repeated(requireLegacyItem(legacyName), variants));
    }

    private static List<RegistryObject<Item>> repeated(RegistryObject<Item> item, int count) {
        java.util.ArrayList<RegistryObject<Item>> variants = new java.util.ArrayList<>(Math.max(0, count));
        for (int meta = 0; meta < count; meta++) {
            variants.add(item);
        }
        return List.copyOf(variants);
    }

    private static ResourceLocation hbm(String path) {
        return new ResourceLocation("hbm", path);
    }

    public record LegacyStackIdentity(ResourceLocation legacyId, int legacyMeta) {
    }

    private LegacyMetaItemMappings() {
    }
}
