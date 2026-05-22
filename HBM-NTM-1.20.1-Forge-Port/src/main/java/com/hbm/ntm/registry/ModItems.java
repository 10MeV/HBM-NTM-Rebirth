package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmCreativeBatteryItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.DigammaDiagnosticItem;
import com.hbm.ntm.item.EffectPillItem;
import com.hbm.ntm.item.GeigerCounterItem;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.item.RadawayItem;
import com.hbm.ntm.item.ToolboxItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HbmNtm.MOD_ID);
    private static final Map<String, RegistryObject<Item>> ITEMS_BY_LEGACY_NAME = new LinkedHashMap<>();

    // Legacy 1.7.10 ID: ModItems.ingot_uranium / texture items/ingot_uranium.png
    public static final RegistryObject<Item> URANIUM_INGOT = ingot("ingot_uranium");
    public static final RegistryObject<Item> URANIUM_233_INGOT = ingot("ingot_u233");
    public static final RegistryObject<Item> URANIUM_235_INGOT = ingot("ingot_u235");
    public static final RegistryObject<Item> URANIUM_238_INGOT = ingot("ingot_u238");
    public static final RegistryObject<Item> PLUTONIUM_INGOT = ingot("ingot_plutonium");
    public static final RegistryObject<Item> PLUTONIUM_238_INGOT = ingot("ingot_pu238");
    public static final RegistryObject<Item> PLUTONIUM_239_INGOT = ingot("ingot_pu239");
    public static final RegistryObject<Item> PLUTONIUM_240_INGOT = ingot("ingot_pu240");
    public static final RegistryObject<Item> PLUTONIUM_241_INGOT = ingot("ingot_pu241");
    public static final RegistryObject<Item> NEPTUNIUM_INGOT = ingot("ingot_neptunium");
    public static final RegistryObject<Item> POLONIUM_INGOT = ingot("ingot_polonium");
    public static final RegistryObject<Item> THORIUM_232_INGOT = ingot("ingot_th232");
    public static final RegistryObject<Item> TITANIUM_INGOT = ingot("ingot_titanium");
    public static final RegistryObject<Item> TUNGSTEN_INGOT = ingot("ingot_tungsten");
    public static final RegistryObject<Item> COPPER_INGOT = ingot("ingot_copper");
    public static final RegistryObject<Item> LEAD_INGOT = ingot("ingot_lead");
    public static final RegistryObject<Item> STEEL_INGOT = ingot("ingot_steel");
    public static final RegistryObject<Item> COBALT_INGOT = ingot("ingot_cobalt");
    public static final RegistryObject<Item> ALUMINIUM_INGOT = ingot("ingot_aluminium");
    public static final RegistryObject<Item> BERYLLIUM_INGOT = ingot("ingot_beryllium");
    public static final RegistryObject<Item> SCHRABIDIUM_INGOT = ingot("ingot_schrabidium");
    public static final RegistryObject<Item> ADVANCED_ALLOY_INGOT = ingot("ingot_advanced_alloy");

    public static final RegistryObject<Item> STEEL_PLATE = part("plate_steel");
    public static final RegistryObject<Item> IRON_PLATE = part("plate_iron");
    public static final RegistryObject<Item> COPPER_PLATE = part("plate_copper");
    public static final RegistryObject<Item> LEAD_PLATE = part("plate_lead");
    public static final RegistryObject<Item> TITANIUM_PLATE = part("plate_titanium");
    public static final RegistryObject<Item> ALUMINIUM_PLATE = part("plate_aluminium");

    public static final RegistryObject<Item> URANIUM_POWDER = part("powder_uranium");
    public static final RegistryObject<Item> PLUTONIUM_POWDER = part("powder_plutonium");
    public static final RegistryObject<Item> THORIUM_POWDER = part("powder_thorium");
    public static final RegistryObject<Item> TITANIUM_POWDER = part("powder_titanium");
    public static final RegistryObject<Item> TUNGSTEN_POWDER = part("powder_tungsten");
    public static final RegistryObject<Item> COPPER_POWDER = part("powder_copper");
    public static final RegistryObject<Item> IRON_POWDER = part("powder_iron");
    public static final RegistryObject<Item> STEEL_POWDER = part("powder_steel");
    public static final RegistryObject<Item> LEAD_POWDER = part("powder_lead");

    public static final RegistryObject<Item> COPPER_COIL = part("coil_copper");
    public static final RegistryObject<Item> TUNGSTEN_COIL = part("coil_tungsten");
    public static final RegistryObject<Item> GOLD_COIL = part("coil_gold");
    public static final RegistryObject<Item> MOTOR = part("motor");

    public static final RegistryObject<Item> IRON_PLATE_STAMP = ITEMS.register("stamp_iron_plate",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.PLATE));
    public static final RegistryObject<Item> IRON_FLAT_STAMP = ITEMS.register("stamp_iron_flat",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.FLAT));
    public static final RegistryObject<Item> IRON_WIRE_STAMP = ITEMS.register("stamp_iron_wire",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.WIRE));
    public static final RegistryObject<Item> IRON_CIRCUIT_STAMP = ITEMS.register("stamp_iron_circuit",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.CIRCUIT));

    public static final RegistryObject<Item> GEIGER_COUNTER = ITEMS.register("geiger_counter",
            () -> new GeigerCounterItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIGAMMA_DIAGNOSTIC = ITEMS.register("digamma_diagnostic",
            () -> new DigammaDiagnosticItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RADAWAY = ITEMS.register("radaway",
            () -> new RadawayItem(new Item.Properties().stacksTo(16), 14, 9));
    public static final RegistryObject<Item> RADAWAY_STRONG = ITEMS.register("radaway_strong",
            () -> new RadawayItem(new Item.Properties().stacksTo(16), 20 * 20, 1));
    public static final RegistryObject<Item> RADAWAY_FLUSH = ITEMS.register("radaway_flush",
            () -> new RadawayItem(new Item.Properties().stacksTo(16), 50, 19));
    public static final RegistryObject<Item> RADX = ITEMS.register("radx",
            () -> new EffectPillItem(new Item.Properties().stacksTo(16), ModEffects.RADX, 3 * 60 * 20, 0, null, true));
    public static final RegistryObject<Item> CONTAINMENT_BOX = simpleStackOneItem("containment_box");
    public static final RegistryObject<Item> PLASTIC_BAG = simpleStackOneItem("plastic_bag");
    public static final RegistryObject<Item> TOOLBOX = registerLegacy("toolbox", () -> new ToolboxItem(new Item.Properties()));
    public static final RegistryObject<Item> CONVEYOR_WAND = registerLegacy("conveyor_wand",
            () -> new ConveyorWandItem(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> BATTERY_POTATO = registerLegacy("battery_potato",
            () -> new HbmBatteryItem(new Item.Properties(), 1_000L, 0L, 100L));
    public static final RegistryObject<Item> BATTERY_CREATIVE = registerLegacy("battery_creative",
            () -> new HbmCreativeBatteryItem(new Item.Properties()));
    public static final RegistryObject<Item> BATTERY_REDSTONE = batteryPack("battery_redstone", 0, 100L, false);
    public static final RegistryObject<Item> BATTERY_LEAD = batteryPack("battery_lead", 1, 1_000L, false);
    public static final RegistryObject<Item> BATTERY_LITHIUM = batteryPack("battery_lithium", 2, 10_000L, false);
    public static final RegistryObject<Item> BATTERY_SODIUM = batteryPack("battery_sodium", 3, 50_000L, false);
    public static final RegistryObject<Item> BATTERY_SCHRABIDIUM = batteryPack("battery_schrabidium", 4, 250_000L, false);
    public static final RegistryObject<Item> BATTERY_QUANTUM = batteryPack("battery_quantum", 5, 1_000_000L, 20L * 60L * 60L);
    public static final RegistryObject<Item> CAPACITOR_COPPER = batteryPack("capacitor_copper", 6, 1_000L, true);
    public static final RegistryObject<Item> CAPACITOR_GOLD = batteryPack("capacitor_gold", 7, 10_000L, true);
    public static final RegistryObject<Item> CAPACITOR_NIOBIUM = batteryPack("capacitor_niobium", 8, 100_000L, true);
    public static final RegistryObject<Item> CAPACITOR_TANTALUM = batteryPack("capacitor_tantalum", 9, 500_000L, true);
    public static final RegistryObject<Item> CAPACITOR_BISMUTH = batteryPack("capacitor_bismuth", 10, 2_500_000L, true);
    public static final RegistryObject<Item> CAPACITOR_SPARK = batteryPack("capacitor_spark", 11, 10_000_000L, true);
    public static final RegistryObject<Item> BATTERY_SC_EMPTY = selfChargingBattery("battery_sc.empty", 0, 0L);
    public static final RegistryObject<Item> BATTERY_SC_WASTE = selfChargingBattery("battery_sc.waste", 1, 150L);
    public static final RegistryObject<Item> BATTERY_SC_RA226 = selfChargingBattery("battery_sc.ra226", 2, 200L);
    public static final RegistryObject<Item> BATTERY_SC_TC99 = selfChargingBattery("battery_sc.tc99", 3, 500L);
    public static final RegistryObject<Item> BATTERY_SC_CO60 = selfChargingBattery("battery_sc.co60", 4, 750L);
    public static final RegistryObject<Item> BATTERY_SC_PU238 = selfChargingBattery("battery_sc.pu238", 5, 1_000L);
    public static final RegistryObject<Item> BATTERY_SC_PO210 = selfChargingBattery("battery_sc.po210", 6, 1_250L);
    public static final RegistryObject<Item> BATTERY_SC_AU198 = selfChargingBattery("battery_sc.au198", 7, 1_500L);
    public static final RegistryObject<Item> BATTERY_SC_PB209 = selfChargingBattery("battery_sc.pb209", 8, 2_000L);
    public static final RegistryObject<Item> BATTERY_SC_AM241 = selfChargingBattery("battery_sc.am241", 9, 2_500L);

    public static final List<RegistryObject<Item>> BATTERY_PACK_ITEMS = List.of(
            BATTERY_REDSTONE,
            BATTERY_LEAD,
            BATTERY_LITHIUM,
            BATTERY_SODIUM,
            BATTERY_SCHRABIDIUM,
            BATTERY_QUANTUM,
            CAPACITOR_COPPER,
            CAPACITOR_GOLD,
            CAPACITOR_NIOBIUM,
            CAPACITOR_TANTALUM,
            CAPACITOR_BISMUTH,
            CAPACITOR_SPARK
    );

    public static final List<RegistryObject<Item>> BATTERY_SC_ITEMS = List.of(
            BATTERY_SC_EMPTY,
            BATTERY_SC_WASTE,
            BATTERY_SC_RA226,
            BATTERY_SC_TC99,
            BATTERY_SC_CO60,
            BATTERY_SC_PU238,
            BATTERY_SC_PO210,
            BATTERY_SC_AU198,
            BATTERY_SC_PB209,
            BATTERY_SC_AM241
    );

    public static final List<RegistryObject<Item>> EXTRA_PARTS_TAB_ITEMS = simpleParts(
            "ingot_pu_mix",
            "ingot_am241",
            "ingot_am242",
            "ingot_am_mix",
            "ingot_technetium",
            "ingot_co60",
            "ingot_sr90",
            "ingot_au198",
            "ingot_pb209",
            "ingot_ra226",
            "ingot_boron",
            "ingot_graphite",
            "ingot_firebrick",
            "sulfur",
            "nitra",
            "nitra_small",
            "ingot_uranium_fuel",
            "ingot_plutonium_fuel",
            "ingot_neptunium_fuel",
            "ingot_mox_fuel",
            "ingot_americium_fuel",
            "ingot_schrabidium_fuel",
            "ingot_thorium_fuel",
            "nugget_uranium_fuel",
            "nugget_thorium_fuel",
            "nugget_plutonium_fuel",
            "nugget_neptunium_fuel",
            "nugget_mox_fuel",
            "nugget_americium_fuel",
            "nugget_schrabidium_fuel",
            "ingot_tcalloy",
            "ingot_cdalloy",
            "ingot_bismuth_bronze",
            "ingot_arsenic_bronze",
            "ingot_bscco",
            "ingot_bismuth",
            "ingot_niobium",
            "ingot_tantalium",
            "ingot_polymer",
            "ingot_bakelite",
            "ingot_pc",
            "ingot_pvc",
            "ingot_red_copper",
            "ingot_tungsten_carbide",
            "fluorite",
            "plate_dura_steel",
            "plate_gold",
            "plate_advanced_alloy",
            "lithium",
            "powder_lithium",
            "powder_cobalt",
            "powder_sodium",
            "powder_schrabidium",
            "powder_spark_mix",
            "ingot_zirconium",
            "ingot_phosphorus",
            "coil_advanced_alloy",
            "coil_advanced_torus",
            "ingot_magnetized_tungsten",
            "plate_mixed",
            "plate_cast_bismuth_bronze",
            "plate_cast_arsenic_bronze",
            "plate_cast_combine_steel",
            "wire_gold",
            "wire_dense_gold",
            "wire_dense_niobium",
            "wire_dense_bscco",
            "pellet_charged",
            "circuit_chip_quantum",
            "pipes_steel",
            "drill_titanium",
            "plate_dalekanium",
            "plate_polymer",
            "plate_kevlar",
            "plate_dineutronium",
            "plate_desh",
            "ingot_solinium",
            "nugget_solinium",
            "photo_panel",
            "sat_base",
            "thruster_nuclear",
            "safety_fuse",
            "billet_uranium",
            "billet_u233",
            "billet_u235",
            "billet_u238",
            "billet_uzh",
            "billet_th232",
            "billet_plutonium",
            "billet_pu238",
            "billet_pu239",
            "billet_pu240",
            "billet_pu241",
            "billet_pu_mix",
            "billet_am241",
            "billet_am242",
            "billet_am_mix",
            "billet_neptunium",
            "billet_polonium",
            "billet_technetium",
            "billet_cobalt",
            "billet_co60",
            "billet_sr90",
            "billet_au198",
            "billet_pb209",
            "billet_ra226",
            "billet_actinium",
            "billet_solinium",
            "billet_uranium_fuel",
            "billet_thorium_fuel",
            "billet_plutonium_fuel",
            "billet_neptunium_fuel",
            "billet_mox_fuel",
            "billet_americium_fuel",
            "billet_les",
            "billet_schrabidium_fuel",
            "billet_hes",
            "billet_po210be",
            "billet_ra226be",
            "billet_pu238be",
            "billet_beryllium",
            "billet_bismuth",
            "billet_zirconium",
            "billet_yharonite",
            "billet_zfb_bismuth",
            "billet_zfb_pu241",
            "billet_zfb_am_mix",
            "billet_nuclear_waste",
            "ingot_gunmetal",
            "plate_gunmetal",
            "ingot_calcium",
            "powder_calcium",
            "ingot_cadmium",
            "powder_cadmium",
            "powder_bismuth",
            "powder_yellowcake",
            "powder_caesium",
            "powder_coltan_ore",
            "ingot_mud",
            "ingot_cft",
            "fallout",
            "cell_empty",
            "cell_sas3",
            "nuclear_waste_long",
            "nuclear_waste_long_tiny",
            "nuclear_waste_short",
            "nuclear_waste_short_tiny",
            "nuclear_waste_long_depleted",
            "nuclear_waste_long_depleted_tiny",
            "nuclear_waste_short_depleted",
            "nuclear_waste_short_depleted_tiny",
            "nuclear_waste",
            "nuclear_waste_tiny",
            "nuclear_waste_vitrified",
            "nuclear_waste_vitrified_tiny",
            "waste_natural_uranium",
            "waste_uranium",
            "waste_thorium",
            "waste_mox",
            "waste_plutonium",
            "waste_u233",
            "waste_u235",
            "waste_schrabidium",
            "waste_zfb_mox",
            "waste_plate_u233",
            "waste_plate_u235",
            "waste_plate_mox",
            "waste_plate_pu239",
            "waste_plate_sa326",
            "waste_plate_ra226be",
            "waste_plate_pu238be",
            "scrap_nuclear",
            "trinitite",
            "gem_rad",
            "crystal_uranium",
            "crystal_thorium",
            "crystal_plutonium",
            "crystal_schraranium",
            "crystal_schrabidium",
            "crystal_phosphorus",
            "crystal_lithium",
            "crystal_trixite",
            "plate_armor_titanium",
            "plate_armor_ajr",
            "plate_armor_hev",
            "plate_armor_lunar",
            "plate_armor_fau",
            "plate_armor_dnt",
            "solid_fuel",
            "solid_fuel_presto",
            "solid_fuel_presto_triplet",
            "solid_fuel_bf",
            "solid_fuel_presto_bf",
            "solid_fuel_presto_triplet_bf",
            "rocket_fuel",
            "lignite",
            "powder_lignite",
            "coal_infernal",
            "cinnebar",
            "powder_limestone",
            "nugget_th232",
            "nugget_uranium",
            "nugget_u233",
            "nugget_u235",
            "nugget_u238",
            "nugget_plutonium",
            "nugget_pu238",
            "nugget_pu239",
            "nugget_pu240",
            "nugget_pu241",
            "nugget_pu_mix",
            "nugget_am241",
            "nugget_am242",
            "nugget_am_mix",
            "nugget_neptunium",
            "nugget_polonium",
            "nugget_technetium",
            "nugget_cobalt",
            "nugget_co60",
            "nugget_sr90",
            "nugget_au198",
            "nugget_pb209",
            "nugget_ra226"
    );

    public static final List<RegistryObject<Item>> NUKE_TAB_ITEMS = simpleParts(
            "boy_propellant",
            "gadget_core",
            "boy_target",
            "boy_bullet",
            "man_core",
            "mike_core",
            "tsar_core",
            "fleija_propellant",
            "fleija_core",
            "solinium_propellant",
            "solinium_core"
    );

    private static final List<RegistryObject<Item>> CONTROL_BATTERY_ITEMS = Stream.concat(Stream.of(
            BATTERY_POTATO,
            BATTERY_CREATIVE
    ), Stream.concat(BATTERY_PACK_ITEMS.stream(), BATTERY_SC_ITEMS.stream())).toList();

    public static final List<RegistryObject<Item>> CONTROL_TAB_ITEMS = Stream.concat(Stream.concat(simpleParts(
            "pile_rod_uranium",
            "pile_rod_pu239",
            "pile_rod_plutonium",
            "pile_rod_source",
            "pile_rod_boron",
            "pile_rod_lithium",
            "pile_rod_detector",
            "pellet_rtg_strontium",
            "pellet_rtg_cobalt",
            "pellet_rtg_actinium",
            "pellet_rtg_lead"
    ).stream(), simpleStackOneItems(
            "plate_fuel_u233",
            "plate_fuel_u235",
            "plate_fuel_mox",
            "plate_fuel_pu239",
            "plate_fuel_sa326",
            "plate_fuel_ra226be",
            "plate_fuel_pu238be",
            "pellet_rtg_radium",
            "pellet_rtg_weak",
            "pellet_rtg",
            "pellet_rtg_polonium",
            "pellet_rtg_americium",
            "pellet_rtg_gold"
    ).stream()), CONTROL_BATTERY_ITEMS.stream()).toList();

    public static final List<RegistryObject<Item>> PARTS_TAB_ITEMS = Stream.concat(Stream.of(
            URANIUM_INGOT,
            URANIUM_233_INGOT,
            URANIUM_235_INGOT,
            URANIUM_238_INGOT,
            PLUTONIUM_INGOT,
            PLUTONIUM_238_INGOT,
            PLUTONIUM_239_INGOT,
            PLUTONIUM_240_INGOT,
            PLUTONIUM_241_INGOT,
            NEPTUNIUM_INGOT,
            POLONIUM_INGOT,
            THORIUM_232_INGOT,
            TITANIUM_INGOT,
            TUNGSTEN_INGOT,
            COPPER_INGOT,
            LEAD_INGOT,
            STEEL_INGOT,
            COBALT_INGOT,
            ALUMINIUM_INGOT,
            BERYLLIUM_INGOT,
            SCHRABIDIUM_INGOT,
            ADVANCED_ALLOY_INGOT,
            STEEL_PLATE,
            IRON_PLATE,
            COPPER_PLATE,
            LEAD_PLATE,
            TITANIUM_PLATE,
            ALUMINIUM_PLATE,
            URANIUM_POWDER,
            PLUTONIUM_POWDER,
            THORIUM_POWDER,
            TITANIUM_POWDER,
            TUNGSTEN_POWDER,
            COPPER_POWDER,
            IRON_POWDER,
            STEEL_POWDER,
            LEAD_POWDER,
            COPPER_COIL,
            TUNGSTEN_COIL,
            GOLD_COIL,
            MOTOR,
            IRON_PLATE_STAMP,
            IRON_FLAT_STAMP,
            IRON_WIRE_STAMP,
            IRON_CIRCUIT_STAMP
    ), EXTRA_PARTS_TAB_ITEMS.stream()).toList();

    public static final List<RegistryObject<Item>> CONSUMABLE_TAB_ITEMS = Stream.of(
            GEIGER_COUNTER,
            DIGAMMA_DIAGNOSTIC,
            RADAWAY,
            RADAWAY_STRONG,
            RADAWAY_FLUSH,
            RADX,
            CONTAINMENT_BOX,
            PLASTIC_BAG,
            TOOLBOX,
            CONVEYOR_WAND
    ).toList();

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static RegistryObject<Item> legacyItem(String name) {
        return ITEMS_BY_LEGACY_NAME.get(name);
    }

    private static RegistryObject<Item> ingot(String name) {
        return simpleItem(name);
    }

    private static RegistryObject<Item> part(String name) {
        return simpleItem(name);
    }

    private static List<RegistryObject<Item>> simpleParts(String... names) {
        return Stream.of(names).map(ModItems::simpleItem).toList();
    }

    private static List<RegistryObject<Item>> simpleStackOneItems(String... names) {
        return Stream.of(names).map(ModItems::simpleStackOneItem).toList();
    }

    private static RegistryObject<Item> registerLegacy(String name, java.util.function.Supplier<Item> supplier) {
        RegistryObject<Item> item = ITEMS.register(name, supplier);
        ITEMS_BY_LEGACY_NAME.put(name, item);
        return item;
    }

    private static RegistryObject<Item> batteryPack(String name, int legacyMeta, long dischargeRate, boolean capacitor) {
        long maxCharge = capacitor ? dischargeRate * 20L * 30L : dischargeRate * 20L * 60L * 15L;
        long chargeRate = capacitor ? dischargeRate : dischargeRate * 10L;
        return registerLegacy(name, () -> new HbmBatteryPackItem(
                new Item.Properties(),
                maxCharge,
                chargeRate,
                dischargeRate,
                name,
                legacyMeta,
                capacitor));
    }

    private static RegistryObject<Item> batteryPack(String name, int legacyMeta, long dischargeRate, long duration) {
        return registerLegacy(name, () -> new HbmBatteryPackItem(
                new Item.Properties(),
                dischargeRate * duration,
                dischargeRate * 10L,
                dischargeRate,
                name,
                legacyMeta,
                false));
    }

    private static RegistryObject<Item> selfChargingBattery(String name, int legacyMeta, long power) {
        return registerLegacy(name, () -> new HbmSelfChargingBatteryItem(new Item.Properties(), power, name, legacyMeta));
    }

    private static RegistryObject<Item> simpleItem(String name) {
        RegistryObject<Item> item = ITEMS.register(name, () -> createSimpleItem(name));
        ITEMS_BY_LEGACY_NAME.put(name, item);
        return item;
    }

    private static Item createSimpleItem(String name) {
        if (isLegacyDepletedFuel(name)) {
            return new DepletedFuelItem(new Item.Properties());
        }
        return new Item(new Item.Properties());
    }

    private static RegistryObject<Item> simpleStackOneItem(String name) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new Item(new Item.Properties().stacksTo(1)));
        ITEMS_BY_LEGACY_NAME.put(name, item);
        return item;
    }

    private static boolean isLegacyDepletedFuel(String name) {
        return switch (name) {
            case "waste_natural_uranium",
                 "waste_uranium",
                 "waste_thorium",
                 "waste_mox",
                 "waste_plutonium",
                 "waste_u233",
                 "waste_u235",
                 "waste_schrabidium",
                 "waste_zfb_mox",
                 "waste_plate_u233",
                 "waste_plate_u235",
                 "waste_plate_mox",
                 "waste_plate_pu239",
                 "waste_plate_sa326",
                 "waste_plate_ra226be",
                 "waste_plate_pu238be" -> true;
            default -> false;
        };
    }

    private ModItems() {
    }
}
