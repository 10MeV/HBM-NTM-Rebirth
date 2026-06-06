package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmCreativeBatteryItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.DetonatorItem;
import com.hbm.ntm.item.AntimatterClusterItem;
import com.hbm.ntm.item.DigammaParticleItem;
import com.hbm.ntm.item.DigammaDiagnosticItem;
import com.hbm.ntm.item.EffectPillItem;
import com.hbm.ntm.item.ExpensiveModeItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.item.GeigerCounterItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
import com.hbm.ntm.item.ItemBlueprints;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.item.OreByproductItem;
import com.hbm.ntm.item.RadawayItem;
import com.hbm.ntm.item.SettingsToolItem;
import com.hbm.ntm.item.SingularityItem;
import com.hbm.ntm.item.ToolboxItem;
import com.hbm.ntm.fluid.HbmFluidContainerRules;
import com.hbm.ntm.fluid.HbmFluids;
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
    public static final RegistryObject<Item> UPGRADE_TEMPLATE = part("upgrade_template");
    public static final RegistryObject<Item> BLUEPRINTS = registerLegacy("blueprints",
            () -> new ItemBlueprints(new Item.Properties()));
    public static final RegistryObject<Item> UPGRADE_SPEED_1 = machineUpgrade("upgrade_speed_1", UpgradeType.SPEED, 1);
    public static final RegistryObject<Item> UPGRADE_SPEED_2 = machineUpgrade("upgrade_speed_2", UpgradeType.SPEED, 2);
    public static final RegistryObject<Item> UPGRADE_SPEED_3 = machineUpgrade("upgrade_speed_3", UpgradeType.SPEED, 3);
    public static final RegistryObject<Item> UPGRADE_EFFECT_1 = machineUpgrade("upgrade_effect_1", UpgradeType.EFFECT, 1);
    public static final RegistryObject<Item> UPGRADE_EFFECT_2 = machineUpgrade("upgrade_effect_2", UpgradeType.EFFECT, 2);
    public static final RegistryObject<Item> UPGRADE_EFFECT_3 = machineUpgrade("upgrade_effect_3", UpgradeType.EFFECT, 3);
    public static final RegistryObject<Item> UPGRADE_POWER_1 = machineUpgrade("upgrade_power_1", UpgradeType.POWER, 1);
    public static final RegistryObject<Item> UPGRADE_POWER_2 = machineUpgrade("upgrade_power_2", UpgradeType.POWER, 2);
    public static final RegistryObject<Item> UPGRADE_POWER_3 = machineUpgrade("upgrade_power_3", UpgradeType.POWER, 3);
    public static final RegistryObject<Item> UPGRADE_OVERDRIVE_1 = machineUpgrade("upgrade_overdrive_1", UpgradeType.OVERDRIVE, 1);
    public static final RegistryObject<Item> UPGRADE_OVERDRIVE_2 = machineUpgrade("upgrade_overdrive_2", UpgradeType.OVERDRIVE, 2);
    public static final RegistryObject<Item> UPGRADE_OVERDRIVE_3 = machineUpgrade("upgrade_overdrive_3", UpgradeType.OVERDRIVE, 3);
    public static final RegistryObject<Item> TEMPLATE_FOLDER = registerLegacy("template_folder",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_PLATE_STAMP = ITEMS.register("stamp_iron_plate",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.PLATE));
    public static final RegistryObject<Item> IRON_FLAT_STAMP = ITEMS.register("stamp_iron_flat",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.FLAT));
    public static final RegistryObject<Item> IRON_WIRE_STAMP = ITEMS.register("stamp_iron_wire",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.WIRE));
    public static final RegistryObject<Item> IRON_CIRCUIT_STAMP = ITEMS.register("stamp_iron_circuit",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.CIRCUIT));
    public static final RegistryObject<Item> STAMP_357 = registerLegacy("stamp_357",
            () -> new ItemPressStamp(new Item.Properties().durability(1_000), ItemPressStamp.StampType.C357));
    public static final RegistryObject<Item> STAMP_44 = registerLegacy("stamp_44",
            () -> new ItemPressStamp(new Item.Properties().durability(1_000), ItemPressStamp.StampType.C44));
    public static final RegistryObject<Item> STAMP_9 = registerLegacy("stamp_9",
            () -> new ItemPressStamp(new Item.Properties().durability(1_000), ItemPressStamp.StampType.C9));
    public static final RegistryObject<Item> STAMP_50 = registerLegacy("stamp_50",
            () -> new ItemPressStamp(new Item.Properties().durability(1_000), ItemPressStamp.StampType.C50));

    public static final RegistryObject<Item> GEIGER_COUNTER = ITEMS.register("geiger_counter",
            () -> new GeigerCounterItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIGAMMA_DIAGNOSTIC = ITEMS.register("digamma_diagnostic",
            () -> new DigammaDiagnosticItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RADAWAY = ITEMS.register("radaway",
            () -> new RadawayItem(new Item.Properties().stacksTo(16), 140, 0));
    public static final RegistryObject<Item> RADAWAY_STRONG = ITEMS.register("radaway_strong",
            () -> new RadawayItem(new Item.Properties().stacksTo(16), 350, 0));
    public static final RegistryObject<Item> RADAWAY_FLUSH = ITEMS.register("radaway_flush",
            () -> new RadawayItem(new Item.Properties().stacksTo(16), 500, 0));
    public static final RegistryObject<Item> RADX = ITEMS.register("radx",
            () -> new EffectPillItem(new Item.Properties().stacksTo(16), ModEffects.RADX, 3 * 60 * 20, 0, null, true));
    public static final RegistryObject<Item> CONTAINMENT_BOX = simpleStackOneItem("containment_box");
    public static final RegistryObject<Item> PLASTIC_BAG = simpleStackOneItem("plastic_bag");
    public static final RegistryObject<Item> TOOLBOX = registerLegacy("toolbox", () -> new ToolboxItem(new Item.Properties()));
    public static final RegistryObject<Item> SETTINGS_TOOL = registerLegacy("settings_tool",
            () -> new SettingsToolItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CONVEYOR_WAND = registerLegacy("conveyor_wand",
            () -> new ConveyorWandItem(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> CANISTER_EMPTY = registerLegacy("canister_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CANISTER_FULL = registerLegacy("canister_full",
            () -> new HbmFluidContainerItem(new Item.Properties().craftRemainder(CANISTER_EMPTY.get()), HbmFluidContainerRules.ContainerKind.CANISTER));
    public static final RegistryObject<Item> CANISTER_NAPALM = registerLegacy("canister_napalm",
            () -> new Item(new Item.Properties().craftRemainder(CANISTER_EMPTY.get())));
    public static final RegistryObject<Item> GAS_EMPTY = registerLegacy("gas_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GAS_FULL = registerLegacy("gas_full",
            () -> new HbmFluidContainerItem(new Item.Properties().craftRemainder(GAS_EMPTY.get()), HbmFluidContainerRules.ContainerKind.GAS_TANK));
    public static final RegistryObject<Item> FLUID_TANK_EMPTY = registerLegacy("fluid_tank_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLUID_TANK_FULL = registerLegacy("fluid_tank_full",
            () -> new HbmFluidContainerItem(new Item.Properties().craftRemainder(FLUID_TANK_EMPTY.get()), HbmFluidContainerRules.ContainerKind.FLUID_TANK));
    public static final RegistryObject<Item> FLUID_TANK_LEAD_EMPTY = registerLegacy("fluid_tank_lead_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLUID_TANK_LEAD_FULL = registerLegacy("fluid_tank_lead_full",
            () -> new HbmFluidContainerItem(new Item.Properties().craftRemainder(FLUID_TANK_LEAD_EMPTY.get()), HbmFluidContainerRules.ContainerKind.LEAD_FLUID_TANK));
    public static final RegistryObject<Item> FLUID_BARREL_EMPTY = registerLegacy("fluid_barrel_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLUID_BARREL_FULL = registerLegacy("fluid_barrel_full",
            () -> new HbmFluidContainerItem(new Item.Properties().craftRemainder(FLUID_BARREL_EMPTY.get()), HbmFluidContainerRules.ContainerKind.FLUID_BARREL, HbmFluidContainerRules.BARREL_CAPACITY));
    public static final RegistryObject<Item> FLUID_BARREL_INFINITE = registerLegacy("fluid_barrel_infinite",
            () -> new HbmInfiniteFluidItem(new Item.Properties(), null, 1_000_000_000, 1, "Infinite Fluid Barrel"));
    public static final RegistryObject<Item> FLUID_PACK_EMPTY = registerLegacy("fluid_pack_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLUID_PACK_FULL = registerLegacy("fluid_pack_full",
            () -> new HbmFluidContainerItem(new Item.Properties().craftRemainder(FLUID_PACK_EMPTY.get()), HbmFluidContainerRules.ContainerKind.FLUID_PACK, HbmFluidContainerRules.FLUID_PACK_CAPACITY));
    public static final RegistryObject<Item> BIOMASS = registerLegacy("biomass",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BIOMASS_COMPRESSED = registerLegacy("biomass_compressed",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CATALYTIC_CONVERTER = registerLegacy("catalytic_converter",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DISPERSER_CANISTER_EMPTY = registerLegacy("disperser_canister_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DISPERSER_CANISTER = registerLegacy("disperser_canister",
            () -> new HbmFluidContainerItem(new Item.Properties().craftRemainder(DISPERSER_CANISTER_EMPTY.get()), HbmFluidContainerRules.ContainerKind.DISPERSER_CANISTER, HbmFluidContainerRules.DISPERSER_CAPACITY));
    public static final RegistryObject<Item> GLYPHID_GLAND_EMPTY = registerLegacy("glyphid_gland_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLYPHID_GLAND = registerLegacy("glyphid_gland",
            () -> new HbmFluidContainerItem(new Item.Properties().craftRemainder(GLYPHID_GLAND_EMPTY.get()), HbmFluidContainerRules.ContainerKind.GLYPHID_GLAND, HbmFluidContainerRules.GLYPHID_GLAND_CAPACITY));
    public static final RegistryObject<Item> INF_WATER = registerLegacy("inf_water",
            () -> new HbmInfiniteFluidItem(new Item.Properties(), HbmFluids.WATER, 50, 1, "Infinite Water"));
    public static final RegistryObject<Item> INF_WATER_MK2 = registerLegacy("inf_water_mk2",
            () -> new HbmInfiniteFluidItem(new Item.Properties(), HbmFluids.WATER, 500, 1, "Infinite Water Mk2"));
    public static final RegistryObject<Item> CHLORINE_PINWHEEL = registerLegacy("chlorine_pinwheel",
            () -> new HbmInfiniteFluidItem(new Item.Properties(), HbmFluids.CHLORINE, 1, 2, "Chlorine Pinwheel"));
    public static final RegistryObject<Item> FLUID_IDENTIFIER_MULTI = registerLegacy("fluid_identifier_multi",
            () -> new FluidIdentifierItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLUID_DUCT = registerLegacy("fluid_duct",
            () -> new FluidPipeBlockItem(ModBlocks.FLUID_DUCT_NEO.get(), new Item.Properties()));
    public static final RegistryObject<Item> DETONATOR = registerLegacy("detonator",
            () -> new DetonatorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SINGULARITY = registerLegacy("singularity",
            () -> new SingularityItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SINGULARITY_COUNTER_RESONANT = registerLegacy("singularity_counter_resonant",
            () -> new SingularityItem(new Item.Properties().stacksTo(1), "singularity_counter_resonant", 3));
    public static final RegistryObject<Item> SINGULARITY_SUPER_HEATED = registerLegacy("singularity_super_heated",
            () -> new SingularityItem(new Item.Properties().stacksTo(1), "singularity_super_heated", 3));
    public static final RegistryObject<Item> SINGULARITY_SPARK = registerLegacy("singularity_spark",
            () -> new SingularityItem(new Item.Properties().stacksTo(1), "singularity_spark", 3));
    public static final RegistryObject<Item> BLACK_HOLE = registerLegacy("black_hole",
            () -> new SingularityItem(new Item.Properties().stacksTo(1), "black_hole", 3));
    public static final RegistryObject<Item> PARTICLE_DIGAMMA = registerLegacy("particle_digamma",
            () -> new DigammaParticleItem(new Item.Properties(), 60));
    public static final RegistryObject<Item> PELLET_ANTIMATTER = registerLegacy("pellet_antimatter",
            () -> new AntimatterClusterItem(new Item.Properties()));
    public static final RegistryObject<Item> CUSTOM_TNT = simpleItem("custom_tnt");
    public static final RegistryObject<Item> CUSTOM_NUKE = simpleItem("custom_nuke");
    public static final RegistryObject<Item> CUSTOM_HYDRO = simpleItem("custom_hydro");
    public static final RegistryObject<Item> CUSTOM_AMAT = simpleItem("custom_amat");
    public static final RegistryObject<Item> CUSTOM_DIRTY = simpleItem("custom_dirty");
    public static final RegistryObject<Item> CUSTOM_SCHRAB = simpleItem("custom_schrab");
    public static final RegistryObject<Item> CUSTOM_FALL = simpleStackOneItem("custom_fall");
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

    public static final List<RegistryObject<Item>> CIRCUIT_ITEMS = simpleParts(
            "circuit_vacuum_tube",
            "circuit_capacitor",
            "circuit_capacitor_tantalium",
            "circuit_pcb",
            "circuit_silicon",
            "circuit_chip",
            "circuit_chip_bismoid",
            "circuit_analog",
            "circuit_basic",
            "circuit_advanced",
            "circuit_capacitor_board",
            "circuit_bismoid",
            "circuit_controller_chassis",
            "circuit_controller",
            "circuit_controller_advanced",
            "circuit_quantum",
            "circuit_chip_quantum",
            "circuit_controller_quantum",
            "circuit_atomic_clock",
            "circuit_numitron"
    );

    public static final List<RegistryObject<Item>> EXPENSIVE_MODE_ITEMS = expensiveModeItems(
            "item_expensive_steel_plating",
            "item_expensive_heavy_frame",
            "item_expensive_circuit",
            "item_expensive_lead_plating",
            "item_expensive_ferro_plating",
            "item_expensive_computer",
            "item_expensive_bronze_tubes",
            "item_expensive_plastic",
            "item_expensive_gold_dust",
            "item_expensive_degenerate_matter"
    );

    public static final List<RegistryObject<Item>> ORE_BYPRODUCT_ITEMS = oreByproductItems(
            new OreByproductSpec("ore_byproduct_b_iron", 0xE2C0AA),
            new OreByproductSpec("ore_byproduct_b_copper", 0xEC9A63),
            new OreByproductSpec("ore_byproduct_b_lithium", 0xEDEDED),
            new OreByproductSpec("ore_byproduct_b_silicon", 0xFFFBD1),
            new OreByproductSpec("ore_byproduct_b_lead", 0x646470),
            new OreByproductSpec("ore_byproduct_b_titanium", 0xF2EFE2),
            new OreByproductSpec("ore_byproduct_b_aluminium", 0xE8F2F9),
            new OreByproductSpec("ore_byproduct_b_sulfur", 0xEAD377),
            new OreByproductSpec("ore_byproduct_b_calcium", 0xCFCFA6),
            new OreByproductSpec("ore_byproduct_b_bismuth", 0x8D8577),
            new OreByproductSpec("ore_byproduct_b_radium", 0xE9FAF6),
            new OreByproductSpec("ore_byproduct_b_technetium", 0xCADFDF),
            new OreByproductSpec("ore_byproduct_b_polonium", 0xCADFDF),
            new OreByproductSpec("ore_byproduct_b_uranium", 0x868D82)
    );

    public static final List<RegistryObject<Item>> STAMP_BOOK_ITEMS = pressStampItems(
            new PressStampSpec("stamp_book_printing1", ItemPressStamp.StampType.PRINTING1),
            new PressStampSpec("stamp_book_printing2", ItemPressStamp.StampType.PRINTING2),
            new PressStampSpec("stamp_book_printing3", ItemPressStamp.StampType.PRINTING3),
            new PressStampSpec("stamp_book_printing4", ItemPressStamp.StampType.PRINTING4),
            new PressStampSpec("stamp_book_printing5", ItemPressStamp.StampType.PRINTING5),
            new PressStampSpec("stamp_book_printing6", ItemPressStamp.StampType.PRINTING6),
            new PressStampSpec("stamp_book_printing7", ItemPressStamp.StampType.PRINTING7),
            new PressStampSpec("stamp_book_printing8", ItemPressStamp.StampType.PRINTING8)
    );

    public static final List<RegistryObject<Item>> PAGE_OF_ITEMS = simpleHiddenItems(
            "page_of_page1",
            "page_of_page2",
            "page_of_page3",
            "page_of_page4",
            "page_of_page5",
            "page_of_page6",
            "page_of_page7",
            "page_of_page8"
    );
    public static final RegistryObject<Item> FLUID_ICON = registerLegacy("fluid_icon",
            () -> new FluidIconItem(new Item.Properties().stacksTo(1)));

    public static final List<RegistryObject<Item>> HIDDEN_RECIPE_ITEMS = Stream.concat(
            Stream.concat(STAMP_BOOK_ITEMS.stream(), PAGE_OF_ITEMS.stream()),
            Stream.of(TEMPLATE_FOLDER, FLUID_ICON)).toList();

    public static final List<RegistryObject<Item>> EXTRA_PARTS_TAB_ITEMS = Stream.concat(Stream.concat(Stream.concat(CIRCUIT_ITEMS.stream(), EXPENSIVE_MODE_ITEMS.stream()), ORE_BYPRODUCT_ITEMS.stream()), simpleParts(
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
            "niter",
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
            "nugget_bismuth",
            "ingot_niobium",
            "ingot_tantalium",
            "ingot_silicon",
            "ingot_ferrouranium",
            "ingot_asbestos",
            "ingot_polymer",
            "ingot_bakelite",
            "ingot_rubber",
            "ingot_pc",
            "ingot_pvc",
            "ingot_red_copper",
            "ingot_tungsten_carbide",
            "fluorite",
            "plate_dura_steel",
            "ingot_dura_steel",
            "powder_dura_steel",
            "plate_gold",
            "lithium",
            "powder_lithium",
            "powder_beryllium",
            "powder_cobalt",
            "powder_sodium",
            "powder_schrabidium",
            "powder_gold",
            "powder_niobium",
            "powder_asbestos",
            "powder_ferrouranium",
            "powder_quartz",
            "powder_lapis",
            "powder_diamond",
            "powder_emerald",
            "powder_sawdust",
            "powder_polymer",
            "powder_bakelite",
            "powder_spark_mix",
            "ball_resin",
            "ingot_zirconium",
            "ingot_phosphorus",
            "ingot_magnetized_tungsten",
            "neutron_reflector",
            "plate_mixed",
            "plate_bismuth",
            "plate_cast_steel",
            "plate_cast_lead",
            "plate_cast_copper",
            "plate_cast_dura_steel",
            "plate_cast_bismuth_bronze",
            "plate_cast_arsenic_bronze",
            "plate_cast_combine_steel",
            "plate_cast_ferrouranium",
            "plate_welded_steel",
            "plate_welded_copper",
            "plate_welded_zirconium",
            "wire_gold",
            "wire_fine_mingrade",
            "wire_fine_tungsten",
            "wire_dense_gold",
            "wire_dense_niobium",
            "wire_dense_bscco",
            "bolt_steel",
            "bolt_tungsten",
            "bolt_dura_steel",
            "shell_steel",
            "shell_titanium",
            "pipes_copper",
            "pipes_rubber",
            "pipes_dura_steel",
            "pellet_charged",
            "pipes_steel",
            "drill_titanium",
            "plate_dalekanium",
            "plate_polymer",
            "plate_kevlar",
            "plate_dineutronium",
            "plate_desh",
            "ingot_desh",
            "ingot_solinium",
            "nugget_solinium",
            "photo_panel",
            "sat_base",
            "thruster_nuclear",
            "safety_fuse",
            "hazmat_cloth",
            "asbestos_cloth",
            "filter_coal",
            "motor_desh",
            "centrifuge_element",
            "reactor_core",
            "thermo_element",
            "rtg_unit",
            "magnetron",
            "entanglement_kit",
            "dysfunctional_reactor",
            "rod_quad_empty",
            "part_lithium",
            "part_beryllium",
            "part_carbon",
            "part_copper",
            "part_plutonium",
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
            "billet_silicon",
            "billet_zirconium",
            "billet_yharonite",
            "billet_zfb_bismuth",
            "billet_zfb_pu241",
            "billet_zfb_am_mix",
            "billet_nuclear_waste",
            "ingot_gunmetal",
            "plate_gunmetal",
            "ingot_weaponsteel",
            "plate_weaponsteel",
            "ingot_combine_steel",
            "powder_combine_steel",
            "plate_combine_steel",
            "ingot_saturnite",
            "plate_saturnite",
            "plate_schrabidium",
            "ingot_calcium",
            "powder_calcium",
            "ingot_cadmium",
            "powder_cadmium",
            "powder_bismuth",
            "powder_yellowcake",
            "powder_schrabidate",
            "powder_caesium",
            "powder_coltan_ore",
            "powder_cement",
            "powder_paleogenite",
            "ingot_mud",
            "ingot_cft",
            "fallout",
            "cell_empty",
            "cell_sas3",
            "tank_steel",
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
            "powder_tektite",
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
            "powder_coal",
            "powder_coal_tiny",
            "coke_coal",
            "coke_lignite",
            "coke_petroleum",
            "briquette_coal",
            "briquette_lignite",
            "briquette_wood",
            "oil_tar_crude",
            "oil_tar_crack",
            "oil_tar_coal",
            "oil_tar_wood",
            "oil_tar_wax",
            "oil_tar_paraffin",
            "powder_ash_wood",
            "powder_ash_coal",
            "powder_ash_misc",
            "powder_ash_fly",
            "powder_ash_soot",
            "powder_ash_fullerene",
            "chunk_ore_rare",
            "chunk_ore_malachite",
            "chunk_ore_cryolite",
            "chunk_ore_moonstone",
            "plant_item_tobacco",
            "plant_item_rope",
            "plant_item_mustardwillow",
            "parts_legendary_tier1",
            "parts_legendary_tier2",
            "parts_legendary_tier3",
            "part_generic_piston_pneumatic",
            "part_generic_piston_hydraulic",
            "part_generic_piston_electric",
            "part_generic_lde",
            "part_generic_hde",
            "part_generic_glass_polarized",
            "casing_small",
            "casing_large",
            "casing_small_steel",
            "casing_large_steel",
            "casing_shotshell",
            "casing_buckshot",
            "casing_buckshot_advanced",
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
            "nugget_silicon",
            "nugget_co60",
            "nugget_sr90",
            "nugget_au198",
            "nugget_pb209",
            "nugget_ra226"
    ).stream()).toList();

    public static final List<RegistryObject<Item>> NUKE_TAB_ITEMS = Stream.concat(Stream.concat(simpleParts(
            "early_explosive_lenses",
            "explosive_lenses"
    ).stream(), simpleStackOneItems(
            "gadget_wireing",
            "boy_propellant",
            "gadget_core",
            "boy_igniter",
            "boy_shielding",
            "boy_target",
            "boy_bullet",
            "igniter",
            "man_igniter",
            "man_core",
            "mike_core",
            "mike_deut",
            "mike_cooling_unit",
            "tsar_core",
            "fleija_igniter",
            "fleija_propellant",
            "fleija_core",
            "solinium_igniter",
            "solinium_propellant",
            "solinium_core",
            "n2_charge"
    ).stream()), Stream.of(
            DETONATOR,
            CUSTOM_TNT,
            CUSTOM_NUKE,
            CUSTOM_HYDRO,
            CUSTOM_AMAT,
            CUSTOM_DIRTY,
            CUSTOM_SCHRAB,
            CUSTOM_FALL
    )).toList();

    private static final List<RegistryObject<Item>> CONTROL_BATTERY_ITEMS = Stream.concat(Stream.of(
            BATTERY_POTATO,
            BATTERY_CREATIVE
    ), Stream.concat(BATTERY_PACK_ITEMS.stream(), BATTERY_SC_ITEMS.stream())).toList();

    private static final List<RegistryObject<Item>> SINGULARITY_FAMILY_ITEMS = List.of(
            SINGULARITY,
            SINGULARITY_COUNTER_RESONANT,
            SINGULARITY_SUPER_HEATED,
            SINGULARITY_SPARK,
            BLACK_HOLE,
            PARTICLE_DIGAMMA,
            PELLET_ANTIMATTER
    );

    private static final List<RegistryObject<Item>> MACHINE_UPGRADE_ITEMS = List.of(
            BLUEPRINTS,
            UPGRADE_SPEED_1,
            UPGRADE_SPEED_2,
            UPGRADE_SPEED_3,
            UPGRADE_EFFECT_1,
            UPGRADE_EFFECT_2,
            UPGRADE_EFFECT_3,
            UPGRADE_POWER_1,
            UPGRADE_POWER_2,
            UPGRADE_POWER_3,
            UPGRADE_OVERDRIVE_1,
            UPGRADE_OVERDRIVE_2,
            UPGRADE_OVERDRIVE_3
    );

    public static final List<RegistryObject<Item>> CONTROL_TAB_ITEMS = Stream.<List<RegistryObject<Item>>>of(simpleParts(
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
            "pellet_rtg_lead",
            "fuel_additive_antiknock",
            "fuel_additive_deicer"
    ), simpleStackOneItems(
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
    ), MACHINE_UPGRADE_ITEMS, List.<RegistryObject<Item>>of(CATALYTIC_CONVERTER), SINGULARITY_FAMILY_ITEMS, CONTROL_BATTERY_ITEMS)
            .flatMap(List::stream)
            .toList();

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
            STEEL_PLATE,
            IRON_PLATE,
            COPPER_PLATE,
            LEAD_PLATE,
            TITANIUM_PLATE,
            ALUMINIUM_PLATE,
            BIOMASS,
            BIOMASS_COMPRESSED,
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
            UPGRADE_TEMPLATE,
            IRON_PLATE_STAMP,
            IRON_FLAT_STAMP,
            IRON_WIRE_STAMP,
            IRON_CIRCUIT_STAMP,
            STAMP_357,
            STAMP_44,
            STAMP_9,
            STAMP_50
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
            SETTINGS_TOOL,
            CONVEYOR_WAND
    ).toList();

    public static final List<RegistryObject<Item>> CONTROL_FLUID_ITEMS = Stream.of(
            CANISTER_EMPTY,
            CANISTER_FULL,
            CANISTER_NAPALM,
            GAS_EMPTY,
            GAS_FULL,
            FLUID_TANK_EMPTY,
            FLUID_TANK_FULL,
            FLUID_TANK_LEAD_EMPTY,
            FLUID_TANK_LEAD_FULL,
            FLUID_BARREL_EMPTY,
            FLUID_BARREL_FULL,
            FLUID_BARREL_INFINITE,
            FLUID_PACK_EMPTY,
            FLUID_PACK_FULL,
            DISPERSER_CANISTER_EMPTY,
            DISPERSER_CANISTER,
            GLYPHID_GLAND_EMPTY,
            GLYPHID_GLAND,
            INF_WATER,
            INF_WATER_MK2,
            CHLORINE_PINWHEEL,
            FLUID_IDENTIFIER_MULTI,
            FLUID_DUCT
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

    private static List<RegistryObject<Item>> expensiveModeItems(String... names) {
        return Stream.of(names).map(ModItems::expensiveModeItem).toList();
    }

    private static List<RegistryObject<Item>> oreByproductItems(OreByproductSpec... specs) {
        return Stream.of(specs).map(spec -> oreByproductItem(spec.name(), spec.tintColor())).toList();
    }

    private static List<RegistryObject<Item>> pressStampItems(PressStampSpec... specs) {
        return Stream.of(specs).map(spec -> pressStampItem(spec.name(), spec.stampType())).toList();
    }

    private static List<RegistryObject<Item>> simpleHiddenItems(String... names) {
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

    private static RegistryObject<Item> machineUpgrade(String name, UpgradeType type, int tier) {
        return registerLegacy(name, () -> new ItemMachineUpgrade(new Item.Properties(), type, tier));
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

    private static RegistryObject<Item> expensiveModeItem(String name) {
        return registerLegacy(name, () -> new ExpensiveModeItem(new Item.Properties()));
    }

    private static RegistryObject<Item> oreByproductItem(String name, int tintColor) {
        return registerLegacy(name, () -> new OreByproductItem(new Item.Properties(), tintColor));
    }

    private static RegistryObject<Item> pressStampItem(String name, ItemPressStamp.StampType stampType) {
        return registerLegacy(name, () -> new ItemPressStamp(new Item.Properties().stacksTo(1), stampType));
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

    private record OreByproductSpec(String name, int tintColor) {
    }

    private record PressStampSpec(String name, ItemPressStamp.StampType stampType) {
    }

    private ModItems() {
    }
}
