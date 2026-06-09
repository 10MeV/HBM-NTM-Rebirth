package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.armor.ArmorModGasMaskItem;
import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.ability.ToolAreaAbilities;
import com.hbm.ntm.ability.ToolHarvestAbilities;
import com.hbm.ntm.ability.WeaponAbilities;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmCreativeBatteryItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.item.ArcElectrodeItem;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.DetonatorItem;
import com.hbm.ntm.item.DosimeterItem;
import com.hbm.ntm.item.AntimatterClusterItem;
import com.hbm.ntm.item.DigammaParticleItem;
import com.hbm.ntm.item.DigammaDiagnosticItem;
import com.hbm.ntm.item.DrillbitItem;
import com.hbm.ntm.item.EffectPillItem;
import com.hbm.ntm.item.ExpensiveModeItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.item.GeigerCounterItem;
import com.hbm.ntm.item.GasMaskArmorItem;
import com.hbm.ntm.item.GasMaskFilterItem;
import com.hbm.ntm.item.HbmAbilitySwordItem;
import com.hbm.ntm.item.HbmAbilityToolItem;
import com.hbm.ntm.item.HazmatArmorItem;
import com.hbm.ntm.item.HbmArmorMaterials;
import com.hbm.ntm.item.HbmFueledAbilityToolItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
import com.hbm.ntm.item.HbmPoweredAbilitySwordItem;
import com.hbm.ntm.item.HbmPoweredAbilityToolItem;
import com.hbm.ntm.item.HbmToolTiers;
import com.hbm.ntm.item.ItemBlueprints;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.item.LegacyToolItem;
import com.hbm.ntm.item.MissileDesignatorItem;
import com.hbm.ntm.item.OreByproductItem;
import com.hbm.ntm.item.PACoilItem;
import com.hbm.ntm.item.PollutionDetectorItem;
import com.hbm.ntm.item.RadawayItem;
import com.hbm.ntm.item.RadarLinkerItem;
import com.hbm.ntm.item.PistonSetItem;
import com.hbm.ntm.item.RTTYPagerItem;
import com.hbm.ntm.item.SettingsToolItem;
import com.hbm.ntm.item.SingularityItem;
import com.hbm.ntm.item.ToolboxItem;
import com.hbm.ntm.fluid.HbmFluidContainerRules;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.satellite.SatelliteChipItem;
import com.hbm.ntm.satellite.SatelliteDesignatorItem;
import com.hbm.ntm.satellite.SatelliteInterfaceItem;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
    public static final RegistryObject<Item> MERCURY_DROP = part("nugget_mercury");
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
    public static final RegistryObject<Item> UPGRADE_AFTERBURN_1 = machineUpgrade("upgrade_afterburn_1", UpgradeType.AFTERBURN, 1);
    public static final RegistryObject<Item> UPGRADE_AFTERBURN_2 = machineUpgrade("upgrade_afterburn_2", UpgradeType.AFTERBURN, 2);
    public static final RegistryObject<Item> UPGRADE_AFTERBURN_3 = machineUpgrade("upgrade_afterburn_3", UpgradeType.AFTERBURN, 3);
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
    public static final RegistryObject<Item> DOSIMETER = registerLegacy("dosimeter",
            () -> new DosimeterItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIGAMMA_DIAGNOSTIC = ITEMS.register("digamma_diagnostic",
            () -> new DigammaDiagnosticItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> POLLUTION_DETECTOR = registerLegacy("pollution_detector",
            () -> new PollutionDetectorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RADAWAY = ITEMS.register("radaway",
            () -> new RadawayItem(new Item.Properties().stacksTo(16), 140, 0));
    public static final RegistryObject<Item> RADAWAY_STRONG = ITEMS.register("radaway_strong",
            () -> new RadawayItem(new Item.Properties().stacksTo(16), 350, 0));
    public static final RegistryObject<Item> RADAWAY_FLUSH = ITEMS.register("radaway_flush",
            () -> new RadawayItem(new Item.Properties().stacksTo(16), 500, 0));
    public static final RegistryObject<Item> RADX = ITEMS.register("radx",
            () -> new EffectPillItem(new Item.Properties().stacksTo(16), ModEffects.RADX, 3 * 60 * 20, 0, null, true));
    public static final RegistryObject<Item> GAS_MASK_FILTER = registerLegacy("gas_mask_filter",
            () -> new GasMaskFilterItem(new Item.Properties()));
    public static final RegistryObject<Item> GAS_MASK_FILTER_MONO = registerLegacy("gas_mask_filter_mono",
            () -> new GasMaskFilterItem(new Item.Properties()));
    public static final RegistryObject<Item> GAS_MASK_FILTER_COMBO = registerLegacy("gas_mask_filter_combo",
            () -> new GasMaskFilterItem(new Item.Properties()));
    public static final RegistryObject<Item> GAS_MASK_FILTER_RAG = registerLegacy("gas_mask_filter_rag",
            () -> new GasMaskFilterItem(new Item.Properties()));
    public static final RegistryObject<Item> GAS_MASK_FILTER_PISS = registerLegacy("gas_mask_filter_piss",
            () -> new GasMaskFilterItem(new Item.Properties()));
    public static final RegistryObject<Item> ATTACHMENT_MASK = registerLegacy("attachment_mask",
            () -> new ArmorModGasMaskItem(new Item.Properties(), false));
    public static final RegistryObject<Item> ATTACHMENT_MASK_MONO = registerLegacy("attachment_mask_mono",
            () -> new ArmorModGasMaskItem(new Item.Properties(), true));
    public static final RegistryObject<Item> GOGGLES = ironHeadArmor("goggles");
    public static final RegistryObject<Item> ASHGLASSES = ironHeadArmor("ashglasses");
    public static final RegistryObject<Item> GAS_MASK = gasMaskArmor("gas_mask", false);
    public static final RegistryObject<Item> GAS_MASK_M65 = gasMaskArmor("gas_mask_m65", false);
    public static final RegistryObject<Item> GAS_MASK_MONO = gasMaskArmor("gas_mask_mono", true);
    public static final RegistryObject<Item> GAS_MASK_OLDE = gasMaskArmor("gas_mask_olde", false);
    public static final RegistryObject<Item> MASK_RAG = ragsHeadArmor("mask_rag");
    public static final RegistryObject<Item> MASK_PISS = ragsHeadArmor("mask_piss");
    public static final RegistryObject<Item> STEEL_HELMET = armor("steel_helmet", HbmArmorMaterials.STEEL,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> STEEL_CHESTPLATE = armor("steel_plate", HbmArmorMaterials.STEEL,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> STEEL_LEGS = armor("steel_legs", HbmArmorMaterials.STEEL,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> STEEL_BOOTS = armor("steel_boots", HbmArmorMaterials.STEEL,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> TITANIUM_HELMET = armor("titanium_helmet", HbmArmorMaterials.TITANIUM,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> TITANIUM_CHESTPLATE = armor("titanium_plate", HbmArmorMaterials.TITANIUM,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> TITANIUM_LEGS = armor("titanium_legs", HbmArmorMaterials.TITANIUM,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> TITANIUM_BOOTS = armor("titanium_boots", HbmArmorMaterials.TITANIUM,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ALLOY_HELMET = armor("alloy_helmet", HbmArmorMaterials.ALLOY,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ALLOY_PLATE = armor("alloy_plate", HbmArmorMaterials.ALLOY,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ALLOY_LEGS = armor("alloy_legs", HbmArmorMaterials.ALLOY,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ALLOY_BOOTS = armor("alloy_boots", HbmArmorMaterials.ALLOY,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> COBALT_HELMET = armor("cobalt_helmet", HbmArmorMaterials.COBALT,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> COBALT_PLATE = armor("cobalt_plate", HbmArmorMaterials.COBALT,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> COBALT_LEGS = armor("cobalt_legs", HbmArmorMaterials.COBALT,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> COBALT_BOOTS = armor("cobalt_boots", HbmArmorMaterials.COBALT,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> HAZMAT_HELMET = hazmatArmor("hazmat_helmet",
            HbmArmorMaterials.HAZMAT, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> HAZMAT_PLATE = hazmatArmor("hazmat_plate",
            HbmArmorMaterials.HAZMAT, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> HAZMAT_LEGS = hazmatArmor("hazmat_legs",
            HbmArmorMaterials.HAZMAT, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> HAZMAT_BOOTS = hazmatArmor("hazmat_boots",
            HbmArmorMaterials.HAZMAT, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> HAZMAT_HELMET_RED = hazmatArmor("hazmat_helmet_red",
            HbmArmorMaterials.HAZMAT_RED, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> HAZMAT_PLATE_RED = hazmatArmor("hazmat_plate_red",
            HbmArmorMaterials.HAZMAT_RED, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> HAZMAT_LEGS_RED = hazmatArmor("hazmat_legs_red",
            HbmArmorMaterials.HAZMAT_RED, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> HAZMAT_BOOTS_RED = hazmatArmor("hazmat_boots_red",
            HbmArmorMaterials.HAZMAT_RED, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> HAZMAT_HELMET_GREY = hazmatArmor("hazmat_helmet_grey",
            HbmArmorMaterials.HAZMAT_GREY, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> HAZMAT_PLATE_GREY = hazmatArmor("hazmat_plate_grey",
            HbmArmorMaterials.HAZMAT_GREY, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> HAZMAT_LEGS_GREY = hazmatArmor("hazmat_legs_grey",
            HbmArmorMaterials.HAZMAT_GREY, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> HAZMAT_BOOTS_GREY = hazmatArmor("hazmat_boots_grey",
            HbmArmorMaterials.HAZMAT_GREY, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ASBESTOS_HELMET = asbestosArmor("asbestos_helmet", ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ASBESTOS_PLATE = asbestosArmor("asbestos_plate", ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ASBESTOS_LEGS = asbestosArmor("asbestos_legs", ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ASBESTOS_BOOTS = asbestosArmor("asbestos_boots", ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> CMB_HELMET = armor("cmb_helmet", HbmArmorMaterials.CMB,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CMB_PLATE = armor("cmb_plate", HbmArmorMaterials.CMB,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> CMB_LEGS = armor("cmb_legs", HbmArmorMaterials.CMB,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> CMB_BOOTS = armor("cmb_boots", HbmArmorMaterials.CMB,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> PAA_PLATE = armor("paa_plate", HbmArmorMaterials.PAA,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> PAA_LEGS = armor("paa_legs", HbmArmorMaterials.PAA,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> PAA_BOOTS = armor("paa_boots", HbmArmorMaterials.PAA,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SECURITY_HELMET = armor("security_helmet", HbmArmorMaterials.SECURITY,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SECURITY_PLATE = armor("security_plate", HbmArmorMaterials.SECURITY,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SECURITY_LEGS = armor("security_legs", HbmArmorMaterials.SECURITY,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SECURITY_BOOTS = armor("security_boots", HbmArmorMaterials.SECURITY,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> STARMETAL_HELMET = armor("starmetal_helmet", HbmArmorMaterials.STARMETAL,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> STARMETAL_PLATE = armor("starmetal_plate", HbmArmorMaterials.STARMETAL,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> STARMETAL_LEGS = armor("starmetal_legs", HbmArmorMaterials.STARMETAL,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> STARMETAL_BOOTS = armor("starmetal_boots", HbmArmorMaterials.STARMETAL,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> HAZMAT_PAA_HELMET = fullHoodGasMaskArmor("hazmat_paa_helmet",
            HbmArmorMaterials.HAZMAT_PAA);
    public static final RegistryObject<Item> HAZMAT_PAA_PLATE = armor("hazmat_paa_plate",
            HbmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> HAZMAT_PAA_LEGS = armor("hazmat_paa_legs",
            HbmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> HAZMAT_PAA_BOOTS = armor("hazmat_paa_boots",
            HbmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> LIQUIDATOR_HELMET = fullHoodGasMaskArmor("liquidator_helmet",
            HbmArmorMaterials.LIQUIDATOR);
    public static final RegistryObject<Item> LIQUIDATOR_PLATE = armor("liquidator_plate",
            HbmArmorMaterials.LIQUIDATOR, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> LIQUIDATOR_LEGS = armor("liquidator_legs",
            HbmArmorMaterials.LIQUIDATOR, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> LIQUIDATOR_BOOTS = armor("liquidator_boots",
            HbmArmorMaterials.LIQUIDATOR, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SCHRABIDIUM_HELMET = armor("schrabidium_helmet",
            HbmArmorMaterials.SCHRABIDIUM, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SCHRABIDIUM_PLATE = armor("schrabidium_plate",
            HbmArmorMaterials.SCHRABIDIUM, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SCHRABIDIUM_LEGS = armor("schrabidium_legs",
            HbmArmorMaterials.SCHRABIDIUM, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SCHRABIDIUM_BOOTS = armor("schrabidium_boots",
            HbmArmorMaterials.SCHRABIDIUM, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> EUPHEMIUM_HELMET = armor("euphemium_helmet",
            HbmArmorMaterials.EUPHEMIUM, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> EUPHEMIUM_PLATE = armor("euphemium_plate",
            HbmArmorMaterials.EUPHEMIUM, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> EUPHEMIUM_LEGS = armor("euphemium_legs",
            HbmArmorMaterials.EUPHEMIUM, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> EUPHEMIUM_BOOTS = armor("euphemium_boots",
            HbmArmorMaterials.EUPHEMIUM, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> T51_HELMET = armor("t51_helmet", HbmArmorMaterials.T51,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> T51_PLATE = armor("t51_plate", HbmArmorMaterials.T51,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> T51_LEGS = armor("t51_legs", HbmArmorMaterials.T51,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> T51_BOOTS = armor("t51_boots", HbmArmorMaterials.T51,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> STEAMSUIT_HELMET = armor("steamsuit_helmet",
            HbmArmorMaterials.DESH_POWERED, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> STEAMSUIT_PLATE = armor("steamsuit_plate",
            HbmArmorMaterials.DESH_POWERED, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> STEAMSUIT_LEGS = armor("steamsuit_legs",
            HbmArmorMaterials.DESH_POWERED, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> STEAMSUIT_BOOTS = armor("steamsuit_boots",
            HbmArmorMaterials.DESH_POWERED, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> AJR_HELMET = armor("ajr_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> AJR_PLATE = armor("ajr_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> AJR_LEGS = armor("ajr_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> AJR_BOOTS = armor("ajr_boots", HbmArmorMaterials.AJR,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> AJRO_HELMET = armor("ajro_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> AJRO_PLATE = armor("ajro_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> AJRO_LEGS = armor("ajro_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> AJRO_BOOTS = armor("ajro_boots", HbmArmorMaterials.AJR,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> RPA_HELMET = armor("rpa_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> RPA_PLATE = armor("rpa_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> RPA_LEGS = armor("rpa_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> RPA_BOOTS = armor("rpa_boots", HbmArmorMaterials.AJR,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> NCRPA_HELMET = armor("ncrpa_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> NCRPA_PLATE = armor("ncrpa_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> NCRPA_LEGS = armor("ncrpa_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> NCRPA_BOOTS = armor("ncrpa_boots", HbmArmorMaterials.AJR,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> BJ_HELMET = armor("bj_helmet", HbmArmorMaterials.BJ,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> BJ_PLATE = armor("bj_plate", HbmArmorMaterials.BJ,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> BJ_LEGS = armor("bj_legs", HbmArmorMaterials.BJ,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BJ_BOOTS = armor("bj_boots", HbmArmorMaterials.BJ,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ENVSUIT_HELMET = armor("envsuit_helmet", HbmArmorMaterials.ENV,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ENVSUIT_PLATE = armor("envsuit_plate", HbmArmorMaterials.ENV,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ENVSUIT_LEGS = armor("envsuit_legs", HbmArmorMaterials.ENV,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ENVSUIT_BOOTS = armor("envsuit_boots", HbmArmorMaterials.ENV,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> HEV_HELMET = armor("hev_helmet", HbmArmorMaterials.HEV,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> HEV_PLATE = armor("hev_plate", HbmArmorMaterials.HEV,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> HEV_LEGS = armor("hev_legs", HbmArmorMaterials.HEV,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> HEV_BOOTS = armor("hev_boots", HbmArmorMaterials.HEV,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> FAU_HELMET = armor("fau_helmet", HbmArmorMaterials.FAU,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> FAU_PLATE = armor("fau_plate", HbmArmorMaterials.FAU,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> FAU_LEGS = armor("fau_legs", HbmArmorMaterials.FAU,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> FAU_BOOTS = armor("fau_boots", HbmArmorMaterials.FAU,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> DNS_HELMET = armor("dns_helmet", HbmArmorMaterials.DNS,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> DNS_PLATE = armor("dns_plate", HbmArmorMaterials.DNS,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> DNS_LEGS = armor("dns_legs", HbmArmorMaterials.DNS,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> DNS_BOOTS = armor("dns_boots", HbmArmorMaterials.DNS,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> TAURUN_HELMET = armor("taurun_helmet", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> TAURUN_PLATE = armor("taurun_plate", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> TAURUN_LEGS = armor("taurun_legs", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> TAURUN_BOOTS = armor("taurun_boots", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> TRENCHMASTER_HELMET = armor("trenchmaster_helmet",
            HbmArmorMaterials.TRENCHMASTER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> TRENCHMASTER_PLATE = armor("trenchmaster_plate",
            HbmArmorMaterials.TRENCHMASTER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> TRENCHMASTER_LEGS = armor("trenchmaster_legs",
            HbmArmorMaterials.TRENCHMASTER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> TRENCHMASTER_BOOTS = armor("trenchmaster_boots",
            HbmArmorMaterials.TRENCHMASTER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> PADS_RUBBER = registerLegacy("pads_rubber",
            () -> new ArmorModItems.Pads(new Item.Properties(), 0.5F, false));
    public static final RegistryObject<Item> PADS_SLIME = registerLegacy("pads_slime",
            () -> new ArmorModItems.Pads(new Item.Properties(), 0.25F, false));
    public static final RegistryObject<Item> PADS_STATIC = registerLegacy("pads_static",
            () -> new ArmorModItems.Pads(new Item.Properties(), 0.75F, true));
    public static final RegistryObject<Item> CLADDING_PAINT = registerLegacy("cladding_paint",
            () -> new ArmorModItems.Cladding(new Item.Properties(), 0.025D));
    public static final RegistryObject<Item> CLADDING_RUBBER = registerLegacy("cladding_rubber",
            () -> new ArmorModItems.Cladding(new Item.Properties(), 0.005D));
    public static final RegistryObject<Item> CLADDING_LEAD = registerLegacy("cladding_lead",
            () -> new ArmorModItems.Cladding(new Item.Properties(), 0.1D));
    public static final RegistryObject<Item> CLADDING_DESH = registerLegacy("cladding_desh",
            () -> new ArmorModItems.Cladding(new Item.Properties(), 0.2D));
    public static final RegistryObject<Item> CLADDING_GHIORSIUM = registerLegacy("cladding_ghiorsium",
            () -> new ArmorModItems.Cladding(new Item.Properties(), 0.5D));
    public static final RegistryObject<Item> CLADDING_IRON = registerLegacy("cladding_iron",
            () -> new ArmorModItems.IronCladding(new Item.Properties()));
    public static final RegistryObject<Item> CLADDING_OBSIDIAN = registerLegacy("cladding_obsidian",
            () -> new ArmorModItems.ObsidianCladding(new Item.Properties()));
    public static final RegistryObject<Item> INSERT_KEVLAR = registerLegacy("insert_kevlar",
            () -> new ArmorModItems.Insert(new Item.Properties(), 1500, 1.0F, 0.9F, 1.0F, 1.0F, false, false));
    public static final RegistryObject<Item> INSERT_SAPI = registerLegacy("insert_sapi",
            () -> new ArmorModItems.Insert(new Item.Properties(), 1750, 1.0F, 0.85F, 1.0F, 1.0F, false, false));
    public static final RegistryObject<Item> INSERT_ESAPI = registerLegacy("insert_esapi",
            () -> new ArmorModItems.Insert(new Item.Properties(), 2000, 0.95F, 0.8F, 1.0F, 1.0F, false, false));
    public static final RegistryObject<Item> INSERT_XSAPI = registerLegacy("insert_xsapi",
            () -> new ArmorModItems.Insert(new Item.Properties(), 2500, 0.9F, 0.75F, 1.0F, 1.0F, false, false));
    public static final RegistryObject<Item> INSERT_STEEL = registerLegacy("insert_steel",
            () -> new ArmorModItems.Insert(new Item.Properties(), 1000, 1.0F, 0.95F, 0.75F, 0.95F, false, false));
    public static final RegistryObject<Item> INSERT_DU = registerLegacy("insert_du",
            () -> new ArmorModItems.Insert(new Item.Properties(), 1500, 0.9F, 0.85F, 0.5F, 0.9F, false, false));
    public static final RegistryObject<Item> INSERT_POLONIUM = registerLegacy("insert_polonium",
            () -> new ArmorModItems.Insert(new Item.Properties(), 500, 0.9F, 1.0F, 0.95F, 0.9F, true, false));
    public static final RegistryObject<Item> INSERT_GHIORSIUM = registerLegacy("insert_ghiorsium",
            () -> new ArmorModItems.Insert(new Item.Properties(), 2000, 0.8F, 0.75F, 0.35F, 0.9F, false, false));
    public static final RegistryObject<Item> INSERT_ERA = registerLegacy("insert_era",
            () -> new ArmorModItems.Insert(new Item.Properties(), 25, 0.5F, 1.0F, 0.25F, 1.0F, false, true));
    public static final RegistryObject<Item> INSERT_YHARONITE = registerLegacy("insert_yharonite",
            () -> new ArmorModItems.Insert(new Item.Properties(), 9999, 0.01F, 1.0F, 1.0F, 1.0F, false, false));
    public static final RegistryObject<Item> INSERT_DOXIUM = registerLegacy("insert_doxium",
            () -> new ArmorModItems.Insert(new Item.Properties(), 9999, 5.0F, 1.0F, 1.0F, 1.0F, false, false));
    public static final RegistryObject<Item> SERVO_SET = registerLegacy("servo_set",
            () -> new ArmorModItems.Servos(new Item.Properties(), false));
    public static final RegistryObject<Item> SERVO_SET_DESH = registerLegacy("servo_set_desh",
            () -> new ArmorModItems.Servos(new Item.Properties(), true));
    public static final RegistryObject<Item> HEART_PIECE = registerLegacy("heart_piece",
            () -> new ArmorModItems.Health(new Item.Properties(), 5.0F, false));
    public static final RegistryObject<Item> HEART_CONTAINER = registerLegacy("heart_container",
            () -> new ArmorModItems.Health(new Item.Properties(), 20.0F, false));
    public static final RegistryObject<Item> HEART_BOOSTER = registerLegacy("heart_booster",
            () -> new ArmorModItems.Health(new Item.Properties(), 40.0F, false));
    public static final RegistryObject<Item> HEART_FAB = registerLegacy("heart_fab",
            () -> new ArmorModItems.Health(new Item.Properties(), 60.0F, false));
    public static final RegistryObject<Item> BLACK_DIAMOND = registerLegacy("black_diamond",
            () -> new ArmorModItems.Health(new Item.Properties(), 40.0F, true));
    public static final RegistryObject<Item> WD40 = registerLegacy("wd40",
            () -> new ArmorModItems.Wd40(new Item.Properties()));
    public static final RegistryObject<Item> BOTTLED_CLOUD = registerLegacy("bottled_cloud",
            () -> new ArmorModItems.BottledCloud(new Item.Properties()));
    public static final RegistryObject<Item> JETPACK_FLY = registerLegacy("jetpack_fly",
            () -> new ArmorModItems.Jetpack(new Item.Properties(), ArmorModItems.Jetpack.Type.REGULAR,
                    HbmFluids.KEROSENE, 12_000));
    public static final RegistryObject<Item> JETPACK_BREAK = registerLegacy("jetpack_break",
            () -> new ArmorModItems.Jetpack(new Item.Properties(), ArmorModItems.Jetpack.Type.HOVER,
                    HbmFluids.KEROSENE, 12_000));
    public static final RegistryObject<Item> JETPACK_VECTOR = registerLegacy("jetpack_vector",
            () -> new ArmorModItems.Jetpack(new Item.Properties(), ArmorModItems.Jetpack.Type.VECTORED,
                    HbmFluids.KEROSENE, 16_000));
    public static final RegistryObject<Item> JETPACK_BOOST = registerLegacy("jetpack_boost",
            () -> new ArmorModItems.Jetpack(new Item.Properties(), ArmorModItems.Jetpack.Type.BOOST,
                    HbmFluids.BALEFIRE, 32_000));
    public static final RegistryObject<Item> AUSTRALIUM_III = registerLegacy("australium_iii",
            () -> new ArmorModItems.Shield(new Item.Properties(), 25.0F));
    public static final RegistryObject<Item> ARMOR_POLISH = registerLegacy("armor_polish",
            () -> new ArmorModItems.Polish(new Item.Properties()));
    public static final RegistryObject<Item> BANDAID = registerLegacy("bandaid",
            () -> new ArmorModItems.Bandaid(new Item.Properties()));
    public static final RegistryObject<Item> SERUM = registerLegacy("serum",
            () -> new ArmorModItems.Serum(new Item.Properties()));
    public static final RegistryObject<Item> QUARTZ_PLUTONIUM = registerLegacy("quartz_plutonium",
            () -> new ArmorModItems.Quartz(new Item.Properties()));
    public static final RegistryObject<Item> MORNING_GLORY = registerLegacy("morning_glory",
            () -> new ArmorModItems.MorningGlory(new Item.Properties()));
    public static final RegistryObject<Item> LODESTONE = registerLegacy("lodestone",
            () -> new ArmorModItems.Lodestone(new Item.Properties(), 5));
    public static final RegistryObject<Item> HORSESHOE_MAGNET = registerLegacy("horseshoe_magnet",
            () -> new ArmorModItems.Lodestone(new Item.Properties(), 8));
    public static final RegistryObject<Item> INDUSTRIAL_MAGNET = registerLegacy("industrial_magnet",
            () -> new ArmorModItems.Lodestone(new Item.Properties(), 12));
    public static final RegistryObject<Item> BATHWATER = registerLegacy("bathwater",
            () -> new ArmorModItems.Bathwater(new Item.Properties(), false));
    public static final RegistryObject<Item> BATHWATER_MK2 = registerLegacy("bathwater_mk2",
            () -> new ArmorModItems.Bathwater(new Item.Properties(), true));
    public static final RegistryObject<Item> SPIDER_MILK = registerLegacy("spider_milk",
            () -> new ArmorModItems.Milk(new Item.Properties()));
    public static final RegistryObject<Item> INK = registerLegacy("ink",
            () -> new ArmorModItems.Ink(new Item.Properties()));
    public static final RegistryObject<Item> INJECTOR_5HTP = registerLegacy("injector_5htp",
            () -> new ArmorModItems.AutoInjector(new Item.Properties()));
    public static final RegistryObject<Item> INJECTOR_KNIFE = registerLegacy("injector_knife",
            () -> new ArmorModItems.InjectorKnife(new Item.Properties()));
    public static final RegistryObject<Item> DEFUSER_GOLD = registerLegacy("defuser_gold",
            () -> new ArmorModItems.Defuser(new Item.Properties()));
    public static final RegistryObject<Item> NEUTRINO_LENS = registerLegacy("neutrino_lens",
            () -> new ArmorModItems.NeutrinoLens(new Item.Properties()));
    public static final RegistryObject<Item> NIGHT_VISION = registerLegacy("night_vision",
            () -> new ArmorModItems.NightVision(new Item.Properties()));
    public static final RegistryObject<Item> BACK_TESLA = registerLegacy("back_tesla",
            () -> new ArmorModItems.BackTesla(new Item.Properties()));
    public static final RegistryObject<Item> MEDAL_LIQUIDATOR = registerLegacy("medal_liquidator",
            () -> new ArmorModItems.Medal(new Item.Properties()));
    public static final RegistryObject<Item> BALLISTIC_GAUNTLET = registerLegacy("ballistic_gauntlet",
            () -> new ArmorModItems.BallisticGauntlet(new Item.Properties()));
    public static final RegistryObject<Item> CARD_AOS = registerLegacy("card_aos",
            () -> new ArmorModItems.Card(new Item.Properties(), false));
    public static final RegistryObject<Item> CARD_QOS = registerLegacy("card_qos",
            () -> new ArmorModItems.Card(new Item.Properties(), true));
    public static final RegistryObject<Item> PROTECTION_CHARM = registerLegacy("protection_charm",
            () -> new ArmorModItems.Charm(new Item.Properties(), false));
    public static final RegistryObject<Item> METEOR_CHARM = registerLegacy("meteor_charm",
            () -> new ArmorModItems.Charm(new Item.Properties(), true));
    public static final RegistryObject<Item> GAS_TESTER = registerLegacy("gas_tester",
            () -> new ArmorModItems.GasSensor(new Item.Properties()));
    public static final RegistryObject<Item> ARMOR_BATTERY = registerLegacy("armor_battery",
            () -> new ArmorModItems.ArmorBattery(new Item.Properties(), 1.25D));
    public static final RegistryObject<Item> ARMOR_BATTERY_MK2 = registerLegacy("armor_battery_mk2",
            () -> new ArmorModItems.ArmorBattery(new Item.Properties(), 1.5D));
    public static final RegistryObject<Item> ARMOR_BATTERY_MK3 = registerLegacy("armor_battery_mk3",
            () -> new ArmorModItems.ArmorBattery(new Item.Properties(), 2.0D));
    public static final RegistryObject<Item> SCRUMPY = registerLegacy("scrumpy",
            () -> new ArmorModItems.Revive(new Item.Properties(), 1));
    public static final RegistryObject<Item> WILD_P = registerLegacy("wild_p",
            () -> new ArmorModItems.Revive(new Item.Properties(), 3));
    public static final RegistryObject<Item> SHACKLES = registerLegacy("shackles",
            () -> new ArmorModItems.Shackles(new Item.Properties()));
    public static final RegistryObject<Item> CONTAINMENT_BOX = simpleStackOneItem("containment_box");
    public static final RegistryObject<Item> PLASTIC_BAG = simpleStackOneItem("plastic_bag");
    public static final RegistryObject<Item> TOOLBOX = registerLegacy("toolbox", () -> new ToolboxItem(new Item.Properties()));
    public static final RegistryObject<Item> SETTINGS_TOOL = registerLegacy("settings_tool",
            () -> new SettingsToolItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SCREWDRIVER = registerLegacy("screwdriver",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(100), Toolable.ToolType.SCREWDRIVER));
    public static final RegistryObject<Item> HAND_DRILL = registerLegacy("hand_drill",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(100), Toolable.ToolType.HAND_DRILL));
    public static final RegistryObject<Item> DRILLBIT_STEEL = drillbit("drillbit_steel", DrillbitItem.Type.STEEL);
    public static final RegistryObject<Item> DRILLBIT_STEEL_DIAMOND = drillbit("drillbit_steel_diamond", DrillbitItem.Type.STEEL_DIAMOND);
    public static final RegistryObject<Item> DRILLBIT_HSS = drillbit("drillbit_hss", DrillbitItem.Type.HSS);
    public static final RegistryObject<Item> DRILLBIT_HSS_DIAMOND = drillbit("drillbit_hss_diamond", DrillbitItem.Type.HSS_DIAMOND);
    public static final RegistryObject<Item> DRILLBIT_DESH = drillbit("drillbit_desh", DrillbitItem.Type.DESH);
    public static final RegistryObject<Item> DRILLBIT_DESH_DIAMOND = drillbit("drillbit_desh_diamond", DrillbitItem.Type.DESH_DIAMOND);
    public static final RegistryObject<Item> DRILLBIT_TCALLOY = drillbit("drillbit_tcalloy", DrillbitItem.Type.TCALLOY);
    public static final RegistryObject<Item> DRILLBIT_TCALLOY_DIAMOND = drillbit("drillbit_tcalloy_diamond", DrillbitItem.Type.TCALLOY_DIAMOND);
    public static final RegistryObject<Item> DRILLBIT_FERRO = drillbit("drillbit_ferro", DrillbitItem.Type.FERRO);
    public static final RegistryObject<Item> DRILLBIT_FERRO_DIAMOND = drillbit("drillbit_ferro_diamond", DrillbitItem.Type.FERRO_DIAMOND);
    public static final RegistryObject<Item> PISTON_SET_STEEL = pistonSet("piston_set_steel", PistonSetItem.Type.STEEL);
    public static final RegistryObject<Item> PISTON_SET_DURA = pistonSet("piston_set_dura", PistonSetItem.Type.DURA);
    public static final RegistryObject<Item> PISTON_SET_DESH = pistonSet("piston_set_desh", PistonSetItem.Type.DESH);
    public static final RegistryObject<Item> PISTON_SET_STARMETAL = pistonSet("piston_set_starmetal", PistonSetItem.Type.STARMETAL);
    public static final RegistryObject<Item> ARC_ELECTRODE_GRAPHITE = arcElectrode("arc_electrode_graphite", ArcElectrodeItem.Type.GRAPHITE, false);
    public static final RegistryObject<Item> ARC_ELECTRODE_LANTHANIUM = arcElectrode("arc_electrode_lanthanium", ArcElectrodeItem.Type.LANTHANIUM, false);
    public static final RegistryObject<Item> ARC_ELECTRODE_DESH = arcElectrode("arc_electrode_desh", ArcElectrodeItem.Type.DESH, false);
    public static final RegistryObject<Item> ARC_ELECTRODE_SATURNITE = arcElectrode("arc_electrode_saturnite", ArcElectrodeItem.Type.SATURNITE, false);
    public static final RegistryObject<Item> ARC_ELECTRODE_BURNT_GRAPHITE = arcElectrode("arc_electrode_burnt_graphite", ArcElectrodeItem.Type.GRAPHITE, true);
    public static final RegistryObject<Item> ARC_ELECTRODE_BURNT_LANTHANIUM = arcElectrode("arc_electrode_burnt_lanthanium", ArcElectrodeItem.Type.LANTHANIUM, true);
    public static final RegistryObject<Item> ARC_ELECTRODE_BURNT_DESH = arcElectrode("arc_electrode_burnt_desh", ArcElectrodeItem.Type.DESH, true);
    public static final RegistryObject<Item> ARC_ELECTRODE_BURNT_SATURNITE = arcElectrode("arc_electrode_burnt_saturnite", ArcElectrodeItem.Type.SATURNITE, true);
    public static final RegistryObject<Item> PA_COIL_GOLD = paCoil("pa_coil_gold", PACoilItem.Type.GOLD);
    public static final RegistryObject<Item> PA_COIL_NIOBIUM = paCoil("pa_coil_niobium", PACoilItem.Type.NIOBIUM);
    public static final RegistryObject<Item> PA_COIL_BSCCO = paCoil("pa_coil_bscco", PACoilItem.Type.BSCCO);
    public static final RegistryObject<Item> PA_COIL_CHLOROPHYTE = paCoil("pa_coil_chlorophyte", PACoilItem.Type.CHLOROPHYTE);
    public static final RegistryObject<Item> DEFUSER = registerLegacy("defuser",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(100), Toolable.ToolType.DEFUSER));
    public static final RegistryObject<Item> CONVEYOR_WAND = registerLegacy("conveyor_wand",
            () -> new ConveyorWandItem(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> SCHRABIDIUM_SWORD = abilitySword("schrabidium_sword", HbmToolTiers.SCHRABIDIUM, 75.0F, 0.0D, true,
            item -> item.addAbility(WeaponAbilities.RADIATION, 1)
                    .addAbility(WeaponAbilities.VAMPIRE, 0));
    public static final RegistryObject<Item> SCHRABIDIUM_PICKAXE = abilityPickaxe("schrabidium_pickaxe", 20.0F, 0.0D, HbmToolTiers.SCHRABIDIUM, true,
            item -> schrabidiumToolAbilities(item));
    public static final RegistryObject<Item> SCHRABIDIUM_AXE = abilityAxe("schrabidium_axe", 25.0F, 0.0D, HbmToolTiers.SCHRABIDIUM, true,
            item -> schrabidiumToolAbilities(item)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> SCHRABIDIUM_SHOVEL = abilityShovel("schrabidium_shovel", 15.0F, 0.0D, HbmToolTiers.SCHRABIDIUM, true,
            item -> schrabidiumToolAbilities(item));
    public static final RegistryObject<Item> TITANIUM_SWORD = abilitySword("titanium_sword", HbmToolTiers.TITANIUM, 6.5F, 0.0D, false, item -> { });
    public static final RegistryObject<Item> TITANIUM_PICKAXE = abilityPickaxe("titanium_pickaxe", 4.5F, 0.0D, HbmToolTiers.TITANIUM, false, item -> { });
    public static final RegistryObject<Item> TITANIUM_AXE = abilityAxe("titanium_axe", 5.5F, 0.0D, HbmToolTiers.TITANIUM, false,
            item -> item.addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> TITANIUM_SHOVEL = abilityShovel("titanium_shovel", 3.5F, 0.0D, HbmToolTiers.TITANIUM, false, item -> { });
    public static final RegistryObject<Item> STEEL_SWORD = abilitySword("steel_sword", HbmToolTiers.STEEL, 6.0F, 0.0D, false,
            item -> item.addAbility(WeaponAbilities.STUN, 0));
    public static final RegistryObject<Item> STEEL_PICKAXE = abilityPickaxe("steel_pickaxe", 4.0F, 0.0D, HbmToolTiers.STEEL, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 0));
    public static final RegistryObject<Item> STEEL_AXE = abilityAxe("steel_axe", 5.0F, 0.0D, HbmToolTiers.STEEL, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 0)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> STEEL_SHOVEL = abilityShovel("steel_shovel", 3.0F, 0.0D, HbmToolTiers.STEEL, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 0));
    public static final RegistryObject<Item> ALLOY_SWORD = abilitySword("alloy_sword", HbmToolTiers.ALLOY, 8.0F, 0.0D, false,
            item -> item.addAbility(WeaponAbilities.STUN, 0));
    public static final RegistryObject<Item> ALLOY_PICKAXE = abilityPickaxe("alloy_pickaxe", 5.0F, 0.0D, HbmToolTiers.ALLOY, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 0));
    public static final RegistryObject<Item> ALLOY_AXE = abilityAxe("alloy_axe", 7.0F, 0.0D, HbmToolTiers.ALLOY, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 0)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> ALLOY_SHOVEL = abilityShovel("alloy_shovel", 4.0F, 0.0D, HbmToolTiers.ALLOY, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 0));
    public static final RegistryObject<Item> CMB_SWORD = abilitySword("cmb_sword", HbmToolTiers.CMB, 35.0F, 0.0D, false,
            item -> item.addAbility(WeaponAbilities.STUN, 0)
                    .addAbility(WeaponAbilities.VAMPIRE, 0));
    public static final RegistryObject<Item> CMB_PICKAXE = abilityPickaxe("cmb_pickaxe", 10.0F, 0.0D, HbmToolTiers.CMB, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 2)
                    .addAbility(ToolHarvestAbilities.SMELTER, 0)
                    .addAbility(ToolHarvestAbilities.SILK, 0)
                    .addAbility(ToolHarvestAbilities.LUCK, 2));
    public static final RegistryObject<Item> CMB_AXE = abilityAxe("cmb_axe", 30.0F, 0.0D, HbmToolTiers.CMB, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 2)
                    .addAbility(ToolHarvestAbilities.SMELTER, 0)
                    .addAbility(ToolHarvestAbilities.SILK, 0)
                    .addAbility(ToolHarvestAbilities.LUCK, 2)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> CMB_SHOVEL = abilityShovel("cmb_shovel", 8.0F, 0.0D, HbmToolTiers.CMB, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 2)
                    .addAbility(ToolHarvestAbilities.SMELTER, 0)
                    .addAbility(ToolHarvestAbilities.SILK, 0)
                    .addAbility(ToolHarvestAbilities.LUCK, 2));
    public static final RegistryObject<Item> DESH_SWORD = abilitySword("desh_sword", HbmToolTiers.DESH, 12.5F, -0.05D, false,
            item -> item.addAbility(WeaponAbilities.STUN, 0));
    public static final RegistryObject<Item> DESH_PICKAXE = abilityPickaxe("desh_pickaxe", 5.0F, -0.05D, HbmToolTiers.DESH, false,
            item -> deshToolAbilities(item));
    public static final RegistryObject<Item> DESH_AXE = abilityAxe("desh_axe", 7.5F, -0.05D, HbmToolTiers.DESH, false,
            item -> deshToolAbilities(item)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> DESH_SHOVEL = abilityShovel("desh_shovel", 4.0F, -0.05D, HbmToolTiers.DESH, false,
            item -> deshToolAbilities(item));
    public static final RegistryObject<Item> COBALT_SWORD = abilitySword("cobalt_sword", HbmToolTiers.COBALT, 12.0F, 0.0D, false, item -> { });
    public static final RegistryObject<Item> COBALT_PICKAXE = abilityPickaxe("cobalt_pickaxe", 4.0F, 0.0D, HbmToolTiers.COBALT, false,
            item -> cobaltToolAbilities(item));
    public static final RegistryObject<Item> COBALT_AXE = abilityAxe("cobalt_axe", 6.0F, 0.0D, HbmToolTiers.COBALT, false,
            item -> cobaltToolAbilities(item)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> COBALT_SHOVEL = abilityShovel("cobalt_shovel", 3.5F, 0.0D, HbmToolTiers.COBALT, false,
            item -> cobaltToolAbilities(item));
    public static final RegistryObject<Item> COBALT_DECORATED_SWORD = abilitySword("cobalt_decorated_sword", HbmToolTiers.COBALT_DECORATED,
            15.0F, 0.0D, false, item -> item.addAbility(WeaponAbilities.BOBBLE, 0));
    public static final RegistryObject<Item> COBALT_DECORATED_PICKAXE = abilityPickaxe("cobalt_decorated_pickaxe", 6.0F, 0.0D,
            HbmToolTiers.COBALT_DECORATED, false, item -> cobaltDecoratedToolAbilities(item));
    public static final RegistryObject<Item> COBALT_DECORATED_AXE = abilityAxe("cobalt_decorated_axe", 8.0F, 0.0D,
            HbmToolTiers.COBALT_DECORATED, false, item -> cobaltDecoratedToolAbilities(item)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> COBALT_DECORATED_SHOVEL = abilityShovel("cobalt_decorated_shovel", 5.0F, 0.0D,
            HbmToolTiers.COBALT_DECORATED, false, item -> cobaltDecoratedToolAbilities(item));
    public static final RegistryObject<Item> STARMETAL_SWORD = abilitySword("starmetal_sword", HbmToolTiers.STARMETAL, 25.0F, 0.0D, false,
            item -> item.addAbility(WeaponAbilities.BEHEADER, 0)
                    .addAbility(WeaponAbilities.STUN, 1)
                    .addAbility(WeaponAbilities.BOBBLE, 0));
    public static final RegistryObject<Item> STARMETAL_PICKAXE = abilityPickaxe("starmetal_pickaxe", 8.0F, 0.0D, HbmToolTiers.STARMETAL, false,
            item -> starmetalToolAbilities(item)
                    .addAbility(WeaponAbilities.STUN, 1));
    public static final RegistryObject<Item> STARMETAL_AXE = abilityAxe("starmetal_axe", 12.0F, 0.0D, HbmToolTiers.STARMETAL, false,
            item -> starmetalToolAbilities(item)
                    .addAbility(WeaponAbilities.BEHEADER, 0)
                    .addAbility(WeaponAbilities.STUN, 1));
    public static final RegistryObject<Item> STARMETAL_SHOVEL = abilityShovel("starmetal_shovel", 7.0F, 0.0D, HbmToolTiers.STARMETAL, false,
            item -> starmetalToolAbilities(item)
                    .addAbility(WeaponAbilities.STUN, 1));
    public static final RegistryObject<Item> CENTRI_STICK = abilityMiner("centri_stick", 3.0F, 0.0D, HbmToolTiers.ELEC, false, 50,
            item -> item.addAbility(ToolHarvestAbilities.CENTRIFUGE, 0));
    public static final RegistryObject<Item> SMASHING_HAMMER = abilityMiner("smashing_hammer", 12.0F, -0.1D, HbmToolTiers.STEEL, false, 2_500,
            item -> item.addAbility(ToolHarvestAbilities.SHREDDER, 0));
    public static final RegistryObject<Item> ELEC_SWORD = poweredAbilitySword("elec_sword", HbmToolTiers.ELEC, 12.5F, 0.0D,
            500_000L, 1_000L, 100L, item -> item.addAbility(WeaponAbilities.STUN, 2));
    public static final RegistryObject<Item> ELEC_PICKAXE = poweredAbilityPickaxe("elec_pickaxe", 6.0F, 0.0D, HbmToolTiers.ELEC,
            500_000L, 1_000L, 100L, item -> electricToolAbilities(item));
    public static final RegistryObject<Item> ELEC_AXE = poweredAbilityAxe("elec_axe", 10.0F, 0.0D, HbmToolTiers.ELEC,
            500_000L, 1_000L, 100L, item -> electricToolAbilities(item)
                    .addAbility(WeaponAbilities.CHAINSAW, 0)
                    .addAbility(WeaponAbilities.BEHEADER, 0)
                    .setShears());
    public static final RegistryObject<Item> ELEC_SHOVEL = poweredAbilityShovel("elec_shovel", 5.0F, 0.0D, HbmToolTiers.ELEC,
            500_000L, 1_000L, 100L, item -> electricToolAbilities(item));
    public static final RegistryObject<Item> DRX = poweredAbilityMiner("drax", 10.0F, -0.05D, HbmToolTiers.ELEC,
            500_000_000L, 100_000L, 5_000L, item -> item.addAbility(ToolHarvestAbilities.SMELTER, 0)
                    .addAbility(ToolHarvestAbilities.SHREDDER, 0)
                    .addAbility(ToolHarvestAbilities.LUCK, 1)
                    .addAbility(ToolAreaAbilities.HAMMER, 1)
                    .addAbility(ToolAreaAbilities.HAMMER_FLAT, 1)
                    .addAbility(ToolAreaAbilities.RECURSION, 2));
    public static final RegistryObject<Item> DRX_MK2 = poweredAbilityMiner("drax_mk2", 15.0F, -0.05D, HbmToolTiers.ELEC,
            1_000_000_000L, 250_000L, 7_500L, item -> item.addAbility(ToolHarvestAbilities.SMELTER, 0)
                    .addAbility(ToolHarvestAbilities.SHREDDER, 0)
                    .addAbility(ToolHarvestAbilities.CENTRIFUGE, 0)
                    .addAbility(ToolHarvestAbilities.LUCK, 2)
                    .addAbility(ToolAreaAbilities.HAMMER, 2)
                    .addAbility(ToolAreaAbilities.HAMMER_FLAT, 2)
                    .addAbility(ToolAreaAbilities.RECURSION, 4));
    public static final RegistryObject<Item> DRX_MK3 = poweredAbilityMiner("drax_mk3", 20.0F, -0.05D, HbmToolTiers.ELEC,
            2_500_000_000L, 500_000L, 10_000L, item -> item.addAbility(ToolHarvestAbilities.SMELTER, 0)
                    .addAbility(ToolHarvestAbilities.SHREDDER, 0)
                    .addAbility(ToolHarvestAbilities.CENTRIFUGE, 0)
                    .addAbility(ToolHarvestAbilities.CRYSTALLIZER, 0)
                    .addAbility(ToolHarvestAbilities.SILK, 0)
                    .addAbility(ToolHarvestAbilities.LUCK, 3)
                    .addAbility(ToolAreaAbilities.HAMMER, 3)
                    .addAbility(ToolAreaAbilities.HAMMER_FLAT, 3)
                    .addAbility(ToolAreaAbilities.RECURSION, 5));
    public static final RegistryObject<Item> BISMUTH_PICKAXE = abilityMiner("bismuth_pickaxe", 15.0F, 0.0D, HbmToolTiers.BISMUTH, false,
            item -> bismuthToolAbilities(item, 2, 0, true).setDepthRockBreaker());
    public static final RegistryObject<Item> BISMUTH_AXE = abilityAxe("bismuth_axe", 25.0F, 0.0D, HbmToolTiers.BISMUTH, false,
            item -> bismuthToolAbilities(item, 3, 1, true));
    public static final RegistryObject<Item> VOLCANIC_PICKAXE = abilityMiner("volcanic_pickaxe", 15.0F, 0.0D, HbmToolTiers.VOLCANIC, false,
            item -> volcanicToolAbilities(item, 0, 0, true).setDepthRockBreaker());
    public static final RegistryObject<Item> VOLCANIC_AXE = abilityAxe("volcanic_axe", 25.0F, 0.0D, HbmToolTiers.VOLCANIC, false,
            item -> volcanicToolAbilities(item, 1, 1, true));
    public static final RegistryObject<Item> CHLOROPHYTE_PICKAXE = abilityMiner("chlorophyte_pickaxe", 20.0F, 0.0D, HbmToolTiers.CHLOROPHYTE, false,
            item -> chlorophyteToolAbilities(item, 3, 2, true)
                    .addAbility(ToolHarvestAbilities.CENTRIFUGE, 0)
                    .addAbility(ToolHarvestAbilities.MERCURY, 0)
                    .setDepthRockBreaker());
    public static final RegistryObject<Item> CHLOROPHYTE_AXE = abilityAxe("chlorophyte_axe", 50.0F, 0.0D, HbmToolTiers.CHLOROPHYTE, false,
            item -> chlorophyteToolAbilities(item, 4, 3, true));
    public static final RegistryObject<Item> MESE_PICKAXE = abilityMiner("mese_pickaxe", 35.0F, 0.0D, HbmToolTiers.MESE, false,
            item -> meseToolAbilities(item, 3, 0)
                    .addAbility(ToolHarvestAbilities.CRYSTALLIZER, 0)
                    .setDepthRockBreaker());
    public static final RegistryObject<Item> MESE_AXE = abilityAxe("mese_axe", 75.0F, 0.0D, HbmToolTiers.MESE, false,
            item -> meseToolAbilities(item, 4, 1));
    public static final RegistryObject<Item> DNT_SWORD = abilitySword("dnt_sword", HbmToolTiers.MESE, 12.0F, 0.0D, false, item -> { });
    public static final RegistryObject<Item> DWARVEN_PICKAXE = abilityMiner("dwarven_pickaxe", 5.0F, -0.1D, HbmToolTiers.DWARVEN, false, 250,
            item -> item.addAbility(ToolAreaAbilities.HAMMER, 0)
                    .addAbility(ToolAreaAbilities.HAMMER_FLAT, 0));
    public static final RegistryObject<Item> MESE_GAVEL = abilitySword("mese_gavel", HbmToolTiers.MESE_GAVEL, 250.0F, 1.5D, false,
            item -> item.addAbility(WeaponAbilities.PHOSPHORUS, 0)
                    .addAbility(WeaponAbilities.RADIATION, 2)
                    .addAbility(WeaponAbilities.STUN, 3)
                    .addAbility(WeaponAbilities.VAMPIRE, 4)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> CHAINSAW = fueledAbilityAxe("chainsaw", 25.0F, -0.05D, HbmToolTiers.ELEC,
            5_000, 1, 250, item -> item.addAbility(ToolAreaAbilities.HAMMER, 0)
                    .addAbility(ToolAreaAbilities.HAMMER_FLAT, 0)
                    .addAbility(ToolAreaAbilities.RECURSION, 2)
                    .addAbility(ToolHarvestAbilities.SILK, 0)
                    .addAbility(ToolHarvestAbilities.LUCK, 1)
                    .addAbility(WeaponAbilities.CHAINSAW, 0)
                    .addAbility(WeaponAbilities.BEHEADER, 0)
                    .setShears(),
            HbmFluids.DIESEL,
            HbmFluids.DIESEL_CRACK,
            HbmFluids.KEROSENE,
            HbmFluids.BIOFUEL,
            HbmFluids.GASOLINE,
            HbmFluids.GASOLINE_LEADED,
            HbmFluids.PETROIL,
            HbmFluids.PETROIL_LEADED,
            HbmFluids.COALGAS,
            HbmFluids.COALGAS_LEADED);
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
    public static final RegistryObject<Item> RTTY_PAGER = registerLegacy("rtty_pager",
            () -> new RTTYPagerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DETONATOR = registerLegacy("detonator",
            () -> new DetonatorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RADAR_LINKER = registerLegacy("radar_linker",
            () -> new RadarLinkerItem(new Item.Properties().stacksTo(1)));
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

    public static final RegistryObject<Item> SAT_HEAD_MAPPER = part("sat_head_mapper");
    public static final RegistryObject<Item> SAT_HEAD_SCANNER = part("sat_head_scanner");
    public static final RegistryObject<Item> SAT_HEAD_RADAR = part("sat_head_radar");
    public static final RegistryObject<Item> SAT_HEAD_LASER = part("sat_head_laser");
    public static final RegistryObject<Item> SAT_HEAD_RESONATOR = part("sat_head_resonator");

    public static final RegistryObject<Item> DESIGNATOR = registerLegacy("designator",
            () -> new MissileDesignatorItem(new Item.Properties().stacksTo(1), MissileDesignatorItem.Mode.BLOCK));
    public static final RegistryObject<Item> DESIGNATOR_RANGE = registerLegacy("designator_range",
            () -> new MissileDesignatorItem(new Item.Properties().stacksTo(1), MissileDesignatorItem.Mode.RANGE));
    public static final RegistryObject<Item> DESIGNATOR_MANUAL = registerLegacy("designator_manual",
            () -> new MissileDesignatorItem(new Item.Properties().stacksTo(1), MissileDesignatorItem.Mode.MANUAL));

    public static final RegistryObject<Item> SAT_MAPPER = satelliteItem("sat_mapper", LegacySatelliteType.MAPPER, "satchip.mapper");
    public static final RegistryObject<Item> SAT_SCANNER = satelliteItem("sat_scanner", LegacySatelliteType.SCANNER, "satchip.scanner");
    public static final RegistryObject<Item> SAT_RADAR = satelliteItem("sat_radar", LegacySatelliteType.RADAR, "satchip.radar");
    public static final RegistryObject<Item> SAT_LASER = satelliteItem("sat_laser", LegacySatelliteType.LASER, "satchip.laser");
    public static final RegistryObject<Item> SAT_FOEQ = satelliteItem("sat_foeq", LegacySatelliteType.RELAY, "satchip.foeq");
    public static final RegistryObject<Item> SAT_RESONATOR = satelliteItem("sat_resonator", LegacySatelliteType.RESONATOR, "satchip.resonator");
    public static final RegistryObject<Item> SAT_MINER = satelliteItem("sat_miner", LegacySatelliteType.MINER, "satchip.miner");
    public static final RegistryObject<Item> SAT_LUNAR_MINER = satelliteItem("sat_lunar_miner", LegacySatelliteType.LUNAR_MINER, "satchip.lunar_miner");
    public static final RegistryObject<Item> SAT_GERALD = satelliteItem("sat_gerald", LegacySatelliteType.HORIZONS,
            "satchip.gerald.desc.0", "satchip.gerald.desc.1", "satchip.gerald.desc.2");
    public static final RegistryObject<Item> SAT_CHIP = registerLegacy("sat_chip",
            () -> new SatelliteChipItem(new Item.Properties()));
    public static final RegistryObject<Item> SAT_INTERFACE = registerLegacy("sat_interface",
            () -> new SatelliteInterfaceItem(new Item.Properties(), SatelliteInterfaceItem.Mode.PANEL));
    public static final RegistryObject<Item> SAT_COORD = registerLegacy("sat_coord",
            () -> new SatelliteInterfaceItem(new Item.Properties(), SatelliteInterfaceItem.Mode.COORD));
    public static final RegistryObject<Item> SAT_DESIGNATOR = registerLegacy("sat_designator",
            () -> new SatelliteDesignatorItem(new Item.Properties()));
    public static final RegistryObject<Item> SAT_RELAY = registerLegacy("sat_relay",
            () -> new SatelliteChipItem(new Item.Properties()));
    public static final RegistryObject<Item> MISSILE_SOYUZ = registerLegacy("missile_soyuz",
            () -> new SoyuzRocketItem(new Item.Properties()));
    public static final RegistryObject<Item> MISSILE_SOYUZ_LANDER = simpleStackOneItem("missile_soyuz_lander");

    public static final List<RegistryObject<Item>> SATELLITE_PART_ITEMS = List.of(
            SAT_HEAD_MAPPER,
            SAT_HEAD_SCANNER,
            SAT_HEAD_RADAR,
            SAT_HEAD_LASER,
            SAT_HEAD_RESONATOR
    );

    public static final List<RegistryObject<Item>> SATELLITE_TAB_ITEMS = List.of(
            DESIGNATOR,
            DESIGNATOR_RANGE,
            DESIGNATOR_MANUAL,
            SAT_MAPPER,
            SAT_SCANNER,
            SAT_RADAR,
            SAT_LASER,
            SAT_FOEQ,
            SAT_RESONATOR,
            SAT_MINER,
            SAT_LUNAR_MINER,
            SAT_GERALD,
            SAT_CHIP,
            SAT_INTERFACE,
            SAT_COORD,
            SAT_DESIGNATOR,
            SAT_RELAY,
            MISSILE_SOYUZ,
            MISSILE_SOYUZ_LANDER
    );

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
            "cordite",
            "ballistite",
            "ball_dynamite",
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
            "powder_neptunium",
            "powder_sodium",
            "powder_schrabidium",
            "powder_gold",
            "powder_niobium",
            "powder_asbestos",
            "powder_ferrouranium",
            "powder_strontium",
            "powder_polonium",
            "powder_co60",
            "powder_sr90",
            "powder_sr90_tiny",
            "powder_i131",
            "powder_i131_tiny",
            "powder_xe135",
            "powder_xe135_tiny",
            "powder_cs137",
            "powder_cs137_tiny",
            "powder_au198",
            "powder_at209",
            "powder_quartz",
            "powder_lapis",
            "powder_diamond",
            "powder_emerald",
            "powder_sawdust",
            "powder_polymer",
            "powder_bakelite",
            "powder_spark_mix",
            "powder_desh_mix",
            "powder_chlorophyte",
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
            "plate_cast_aluminium",
            "plate_cast_dura_steel",
            "plate_cast_bismuth_bronze",
            "plate_cast_arsenic_bronze",
            "plate_cast_combine_steel",
            "plate_cast_ferrouranium",
            "plate_welded_steel",
            "plate_welded_copper",
            "plate_welded_zirconium",
            "plate_welded_tcalloy",
            "plate_welded_cdalloy",
            "wire_gold",
            "wire_fine_copper",
            "wire_fine_mingrade",
            "wire_fine_tungsten",
            "wire_dense_gold",
            "wire_dense_copper",
            "wire_dense_titanium",
            "wire_dense_niobium",
            "wire_dense_bscco",
            "wire_dense_neodymium",
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
            "ingot_meteorite",
            "ingot_solinium",
            "nugget_solinium",
            "photo_panel",
            "sat_base",
            "thruster_nuclear",
            "thruster_small",
            "thruster_medium",
            "ducttape",
            "crystal_diamond",
            "safety_fuse",
            "hazmat_cloth",
            "hazmat_cloth_red",
            "hazmat_cloth_grey",
            "asbestos_cloth",
            "filter_coal",
            "motor_desh",
            "centrifuge_element",
            "reactor_core",
            "thermo_element",
            "rtg_unit",
            "magnetron",
            "crt_display",
            "sphere_steel",
            "blade_titanium",
            "turbine_titanium",
            "blade_tungsten",
            "turbine_tungsten",
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
            "billet_balefire_gold",
            "billet_flashlead",
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
            "powder_balefire",
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
            "explosive_lenses",
            "stick_tnt",
            "stick_semtex",
            "stick_c4"
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
            simpleStackSizeItem("egg_balefire_shard", 16),
            simpleStackOneItem("egg_balefire"),
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

    public static final List<RegistryObject<Item>> ARC_ELECTRODE_ITEMS = List.of(
            ARC_ELECTRODE_GRAPHITE,
            ARC_ELECTRODE_LANTHANIUM,
            ARC_ELECTRODE_DESH,
            ARC_ELECTRODE_SATURNITE,
            ARC_ELECTRODE_BURNT_GRAPHITE,
            ARC_ELECTRODE_BURNT_LANTHANIUM,
            ARC_ELECTRODE_BURNT_DESH,
            ARC_ELECTRODE_BURNT_SATURNITE
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
            UPGRADE_OVERDRIVE_3,
            UPGRADE_AFTERBURN_1,
            UPGRADE_AFTERBURN_2,
            UPGRADE_AFTERBURN_3
    );

    private static final List<RegistryObject<Item>> LEGACY_TOOL_ITEMS = List.of(
            SCREWDRIVER,
            HAND_DRILL,
            DEFUSER
    );

    public static final List<RegistryObject<Item>> DRILLBIT_ITEMS = List.of(
            DRILLBIT_STEEL,
            DRILLBIT_STEEL_DIAMOND,
            DRILLBIT_HSS,
            DRILLBIT_HSS_DIAMOND,
            DRILLBIT_DESH,
            DRILLBIT_DESH_DIAMOND,
            DRILLBIT_TCALLOY,
            DRILLBIT_TCALLOY_DIAMOND,
            DRILLBIT_FERRO,
            DRILLBIT_FERRO_DIAMOND
    );

    public static final List<RegistryObject<Item>> PISTON_SET_ITEMS = List.of(
            PISTON_SET_STEEL,
            PISTON_SET_DURA,
            PISTON_SET_DESH,
            PISTON_SET_STARMETAL
    );

    public static final List<RegistryObject<Item>> PA_COIL_ITEMS = List.of(
            PA_COIL_GOLD,
            PA_COIL_NIOBIUM,
            PA_COIL_BSCCO,
            PA_COIL_CHLOROPHYTE
    );

    private static final List<RegistryObject<Item>> ABILITY_TOOL_ITEMS = List.of(
            SCHRABIDIUM_SWORD,
            SCHRABIDIUM_PICKAXE,
            SCHRABIDIUM_AXE,
            SCHRABIDIUM_SHOVEL,
            TITANIUM_SWORD,
            TITANIUM_PICKAXE,
            TITANIUM_AXE,
            TITANIUM_SHOVEL,
            STEEL_SWORD,
            STEEL_PICKAXE,
            STEEL_AXE,
            STEEL_SHOVEL,
            ALLOY_SWORD,
            ALLOY_PICKAXE,
            ALLOY_AXE,
            ALLOY_SHOVEL,
            CMB_SWORD,
            CMB_PICKAXE,
            CMB_AXE,
            CMB_SHOVEL,
            DESH_SWORD,
            DESH_PICKAXE,
            DESH_AXE,
            DESH_SHOVEL,
            COBALT_SWORD,
            COBALT_PICKAXE,
            COBALT_AXE,
            COBALT_SHOVEL,
            COBALT_DECORATED_SWORD,
            COBALT_DECORATED_PICKAXE,
            COBALT_DECORATED_AXE,
            COBALT_DECORATED_SHOVEL,
            STARMETAL_SWORD,
            STARMETAL_PICKAXE,
            STARMETAL_AXE,
            STARMETAL_SHOVEL,
            CENTRI_STICK,
            SMASHING_HAMMER,
            ELEC_SWORD,
            ELEC_PICKAXE,
            ELEC_AXE,
            ELEC_SHOVEL,
            DRX,
            DRX_MK2,
            DRX_MK3,
            BISMUTH_PICKAXE,
            BISMUTH_AXE,
            VOLCANIC_PICKAXE,
            VOLCANIC_AXE,
            CHLOROPHYTE_PICKAXE,
            CHLOROPHYTE_AXE,
            MESE_PICKAXE,
            MESE_AXE,
            DNT_SWORD,
            DWARVEN_PICKAXE,
            MESE_GAVEL,
            CHAINSAW
    );

    public static final List<RegistryObject<Item>> CONTROL_TAB_ITEMS = Stream.<List<RegistryObject<Item>>>of(simpleParts(
            "pile_rod_uranium",
            "pile_rod_pu239",
            "pile_rod_plutonium",
            "pile_rod_source",
            "pile_rod_boron",
            "pile_rod_lithium",
            "pile_rod_detector",
            "rod_zirnox_empty",
            "cell_tritium",
            "cell_balefire",
            "debris_graphite",
            "debris_metal",
            "debris_fuel",
            "debris_concrete",
            "debris_exchanger",
            "debris_shrapnel",
            "debris_element",
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
            "pellet_rtg_gold",
            "rod_zirnox_tritium",
            "rod_zirnox_natural_uranium_fuel_depleted",
            "rod_zirnox_uranium_fuel_depleted",
            "rod_zirnox_thorium_fuel_depleted",
            "rod_zirnox_mox_fuel_depleted",
            "rod_zirnox_plutonium_fuel_depleted",
            "rod_zirnox_u233_fuel_depleted",
            "rod_zirnox_u235_fuel_depleted",
            "rod_zirnox_les_fuel_depleted",
            "rod_zirnox_zfb_mox_depleted",
            "crystal_xen"
    ), MACHINE_UPGRADE_ITEMS, LEGACY_TOOL_ITEMS, DRILLBIT_ITEMS, PISTON_SET_ITEMS, ARC_ELECTRODE_ITEMS, PA_COIL_ITEMS, ABILITY_TOOL_ITEMS, List.<RegistryObject<Item>>of(CATALYTIC_CONVERTER), SINGULARITY_FAMILY_ITEMS, CONTROL_BATTERY_ITEMS)
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
            MERCURY_DROP,
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
    ), Stream.concat(SATELLITE_PART_ITEMS.stream(), EXTRA_PARTS_TAB_ITEMS.stream())).toList();

    public static final List<RegistryObject<Item>> CONSUMABLE_TAB_ITEMS = Stream.of(
            GEIGER_COUNTER,
            DOSIMETER,
            DIGAMMA_DIAGNOSTIC,
            POLLUTION_DETECTOR,
            RADAWAY,
            RADAWAY_STRONG,
            RADAWAY_FLUSH,
            RADX,
            GAS_MASK_FILTER,
            GAS_MASK_FILTER_MONO,
            GAS_MASK_FILTER_COMBO,
            GAS_MASK_FILTER_RAG,
            GAS_MASK_FILTER_PISS,
            ATTACHMENT_MASK,
            ATTACHMENT_MASK_MONO,
            GOGGLES,
            ASHGLASSES,
            GAS_MASK,
            GAS_MASK_M65,
            GAS_MASK_MONO,
            GAS_MASK_OLDE,
            MASK_RAG,
            MASK_PISS,
            STEEL_HELMET,
            STEEL_CHESTPLATE,
            STEEL_LEGS,
            STEEL_BOOTS,
            TITANIUM_HELMET,
            TITANIUM_CHESTPLATE,
            TITANIUM_LEGS,
            TITANIUM_BOOTS,
            ALLOY_HELMET,
            ALLOY_PLATE,
            ALLOY_LEGS,
            ALLOY_BOOTS,
            COBALT_HELMET,
            COBALT_PLATE,
            COBALT_LEGS,
            COBALT_BOOTS,
            HAZMAT_HELMET,
            HAZMAT_PLATE,
            HAZMAT_LEGS,
            HAZMAT_BOOTS,
            HAZMAT_HELMET_RED,
            HAZMAT_PLATE_RED,
            HAZMAT_LEGS_RED,
            HAZMAT_BOOTS_RED,
            HAZMAT_HELMET_GREY,
            HAZMAT_PLATE_GREY,
            HAZMAT_LEGS_GREY,
            HAZMAT_BOOTS_GREY,
            ASBESTOS_HELMET,
            ASBESTOS_PLATE,
            ASBESTOS_LEGS,
            ASBESTOS_BOOTS,
            CMB_HELMET,
            CMB_PLATE,
            CMB_LEGS,
            CMB_BOOTS,
            PAA_PLATE,
            PAA_LEGS,
            PAA_BOOTS,
            SECURITY_HELMET,
            SECURITY_PLATE,
            SECURITY_LEGS,
            SECURITY_BOOTS,
            STARMETAL_HELMET,
            STARMETAL_PLATE,
            STARMETAL_LEGS,
            STARMETAL_BOOTS,
            HAZMAT_PAA_HELMET,
            HAZMAT_PAA_PLATE,
            HAZMAT_PAA_LEGS,
            HAZMAT_PAA_BOOTS,
            LIQUIDATOR_HELMET,
            LIQUIDATOR_PLATE,
            LIQUIDATOR_LEGS,
            LIQUIDATOR_BOOTS,
            SCHRABIDIUM_HELMET,
            SCHRABIDIUM_PLATE,
            SCHRABIDIUM_LEGS,
            SCHRABIDIUM_BOOTS,
            EUPHEMIUM_HELMET,
            EUPHEMIUM_PLATE,
            EUPHEMIUM_LEGS,
            EUPHEMIUM_BOOTS,
            T51_HELMET,
            T51_PLATE,
            T51_LEGS,
            T51_BOOTS,
            STEAMSUIT_HELMET,
            STEAMSUIT_PLATE,
            STEAMSUIT_LEGS,
            STEAMSUIT_BOOTS,
            AJR_HELMET,
            AJR_PLATE,
            AJR_LEGS,
            AJR_BOOTS,
            AJRO_HELMET,
            AJRO_PLATE,
            AJRO_LEGS,
            AJRO_BOOTS,
            RPA_HELMET,
            RPA_PLATE,
            RPA_LEGS,
            RPA_BOOTS,
            NCRPA_HELMET,
            NCRPA_PLATE,
            NCRPA_LEGS,
            NCRPA_BOOTS,
            BJ_HELMET,
            BJ_PLATE,
            BJ_LEGS,
            BJ_BOOTS,
            ENVSUIT_HELMET,
            ENVSUIT_PLATE,
            ENVSUIT_LEGS,
            ENVSUIT_BOOTS,
            HEV_HELMET,
            HEV_PLATE,
            HEV_LEGS,
            HEV_BOOTS,
            FAU_HELMET,
            FAU_PLATE,
            FAU_LEGS,
            FAU_BOOTS,
            DNS_HELMET,
            DNS_PLATE,
            DNS_LEGS,
            DNS_BOOTS,
            TAURUN_HELMET,
            TAURUN_PLATE,
            TAURUN_LEGS,
            TAURUN_BOOTS,
            TRENCHMASTER_HELMET,
            TRENCHMASTER_PLATE,
            TRENCHMASTER_LEGS,
            TRENCHMASTER_BOOTS,
            PADS_RUBBER,
            PADS_SLIME,
            PADS_STATIC,
            CLADDING_PAINT,
            CLADDING_RUBBER,
            CLADDING_LEAD,
            CLADDING_DESH,
            CLADDING_GHIORSIUM,
            CLADDING_IRON,
            CLADDING_OBSIDIAN,
            INSERT_KEVLAR,
            INSERT_SAPI,
            INSERT_ESAPI,
            INSERT_XSAPI,
            INSERT_STEEL,
            INSERT_DU,
            INSERT_POLONIUM,
            INSERT_GHIORSIUM,
            INSERT_ERA,
            INSERT_YHARONITE,
            INSERT_DOXIUM,
            SERVO_SET,
            SERVO_SET_DESH,
            RTTY_PAGER,
            RADAR_LINKER,
            HEART_PIECE,
            HEART_CONTAINER,
            HEART_BOOSTER,
            HEART_FAB,
            BLACK_DIAMOND,
            WD40,
            BOTTLED_CLOUD,
            JETPACK_FLY,
            JETPACK_BREAK,
            JETPACK_VECTOR,
            JETPACK_BOOST,
            AUSTRALIUM_III,
            ARMOR_POLISH,
            BANDAID,
            SERUM,
            QUARTZ_PLUTONIUM,
            MORNING_GLORY,
            LODESTONE,
            HORSESHOE_MAGNET,
            INDUSTRIAL_MAGNET,
            BATHWATER,
            BATHWATER_MK2,
            SPIDER_MILK,
            INK,
            INJECTOR_5HTP,
            INJECTOR_KNIFE,
            DEFUSER_GOLD,
            NEUTRINO_LENS,
            NIGHT_VISION,
            BACK_TESLA,
            MEDAL_LIQUIDATOR,
            BALLISTIC_GAUNTLET,
            PROTECTION_CHARM,
            METEOR_CHARM,
            GAS_TESTER,
            ARMOR_BATTERY,
            ARMOR_BATTERY_MK2,
            ARMOR_BATTERY_MK3,
            SCRUMPY,
            WILD_P,
            SHACKLES,
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

    private static RegistryObject<Item> drillbit(String name, DrillbitItem.Type type) {
        return registerLegacy(name, () -> new DrillbitItem(new Item.Properties().stacksTo(1), type));
    }

    private static RegistryObject<Item> pistonSet(String name, PistonSetItem.Type type) {
        return registerLegacy(name, () -> new PistonSetItem(new Item.Properties().stacksTo(1), type));
    }

    private static RegistryObject<Item> arcElectrode(String name, ArcElectrodeItem.Type type, boolean burnt) {
        Item.Properties properties = burnt ? new Item.Properties() : new Item.Properties().stacksTo(1);
        return registerLegacy(name, () -> new ArcElectrodeItem(properties, type, burnt));
    }

    private static RegistryObject<Item> paCoil(String name, PACoilItem.Type type) {
        return registerLegacy(name, () -> new PACoilItem(new Item.Properties().stacksTo(1), type));
    }

    private static RegistryObject<Item> registerLegacy(String name, java.util.function.Supplier<Item> supplier) {
        RegistryObject<Item> item = ITEMS.register(name, supplier);
        ITEMS_BY_LEGACY_NAME.put(name, item);
        return item;
    }

    private static RegistryObject<Item> hazmatArmor(String name, HbmArmorMaterials material, ArmorItem.Type type) {
        return registerLegacy(name, () -> new HazmatArmorItem(material, type, new Item.Properties()));
    }

    private static RegistryObject<Item> asbestosArmor(String name, ArmorItem.Type type) {
        return registerLegacy(name, () -> new ArmorItem(HbmArmorMaterials.ASBESTOS, type, new Item.Properties()));
    }

    private static RegistryObject<Item> armor(String name, HbmArmorMaterials material, ArmorItem.Type type) {
        return registerLegacy(name, () -> new ArmorItem(material, type, new Item.Properties()));
    }

    private static RegistryObject<Item> fullHoodGasMaskArmor(String name, HbmArmorMaterials material) {
        return registerLegacy(name, () -> new GasMaskArmorItem(material, new Item.Properties(), List.of()));
    }

    private static RegistryObject<Item> gasMaskArmor(String name, boolean mono) {
        return registerLegacy(name, () -> new GasMaskArmorItem(ArmorMaterials.IRON, new Item.Properties(), mono));
    }

    private static RegistryObject<Item> ironHeadArmor(String name) {
        return registerLegacy(name, () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    }

    private static RegistryObject<Item> ragsHeadArmor(String name) {
        return registerLegacy(name, () -> new ArmorItem(HbmArmorMaterials.RAGS, ArmorItem.Type.HELMET, new Item.Properties()));
    }

    private static RegistryObject<Item> abilitySword(String name, HbmToolTiers tier, float damage, double movement,
                                                     boolean rare, Consumer<HbmAbilitySwordItem> abilities) {
        return registerLegacy(name, () -> {
            HbmAbilitySwordItem item = new HbmAbilitySwordItem(tier, damage, movement, toolProperties(tier, rare));
            abilities.accept(item);
            return item;
        });
    }

    private static RegistryObject<Item> abilityPickaxe(String name, float damage, double movement, HbmToolTiers tier,
                                                       boolean rare, Consumer<HbmAbilityToolItem> abilities) {
        return registerLegacy(name, () -> {
            HbmAbilityToolItem item = HbmAbilityToolItem.pickaxe(damage, movement, tier, toolProperties(tier, rare));
            abilities.accept(item);
            return item;
        });
    }

    private static RegistryObject<Item> abilityAxe(String name, float damage, double movement, HbmToolTiers tier,
                                                   boolean rare, Consumer<HbmAbilityToolItem> abilities) {
        return registerLegacy(name, () -> {
            HbmAbilityToolItem item = HbmAbilityToolItem.axe(damage, movement, tier, toolProperties(tier, rare));
            abilities.accept(item);
            return item;
        });
    }

    private static RegistryObject<Item> abilityShovel(String name, float damage, double movement, HbmToolTiers tier,
                                                      boolean rare, Consumer<HbmAbilityToolItem> abilities) {
        return registerLegacy(name, () -> {
            HbmAbilityToolItem item = HbmAbilityToolItem.shovel(damage, movement, tier, toolProperties(tier, rare));
            abilities.accept(item);
            return item;
        });
    }

    private static RegistryObject<Item> abilityMiner(String name, float damage, double movement, HbmToolTiers tier,
                                                     boolean rare, Consumer<HbmAbilityToolItem> abilities) {
        return abilityMiner(name, damage, movement, tier, rare, -1, abilities);
    }

    private static RegistryObject<Item> abilityMiner(String name, float damage, double movement, HbmToolTiers tier,
                                                     boolean rare, int durability, Consumer<HbmAbilityToolItem> abilities) {
        return registerLegacy(name, () -> {
            HbmAbilityToolItem item = HbmAbilityToolItem.miner(damage, movement, tier, toolProperties(tier, rare, durability));
            abilities.accept(item);
            return item;
        });
    }

    private static RegistryObject<Item> poweredAbilitySword(String name, HbmToolTiers tier, float damage, double movement,
                                                            long maxCharge, long chargeRate, long consumption,
                                                            Consumer<HbmPoweredAbilitySwordItem> abilities) {
        return registerLegacy(name, () -> {
            HbmPoweredAbilitySwordItem item = new HbmPoweredAbilitySwordItem(tier, damage, movement,
                    toolProperties(tier, false), maxCharge, chargeRate, consumption);
            abilities.accept(item);
            return item;
        });
    }

    private static RegistryObject<Item> poweredAbilityPickaxe(String name, float damage, double movement, HbmToolTiers tier,
                                                              long maxCharge, long chargeRate, long consumption,
                                                              Consumer<HbmPoweredAbilityToolItem> abilities) {
        return registerLegacy(name, () -> {
            HbmPoweredAbilityToolItem item = HbmPoweredAbilityToolItem.pickaxe(damage, movement, tier,
                    toolProperties(tier, false), maxCharge, chargeRate, consumption);
            abilities.accept(item);
            return item;
        });
    }

    private static RegistryObject<Item> poweredAbilityAxe(String name, float damage, double movement, HbmToolTiers tier,
                                                          long maxCharge, long chargeRate, long consumption,
                                                          Consumer<HbmPoweredAbilityToolItem> abilities) {
        return registerLegacy(name, () -> {
            HbmPoweredAbilityToolItem item = HbmPoweredAbilityToolItem.axe(damage, movement, tier,
                    toolProperties(tier, false), maxCharge, chargeRate, consumption);
            abilities.accept(item);
            return item;
        });
    }

    private static RegistryObject<Item> poweredAbilityShovel(String name, float damage, double movement, HbmToolTiers tier,
                                                             long maxCharge, long chargeRate, long consumption,
                                                             Consumer<HbmPoweredAbilityToolItem> abilities) {
        return registerLegacy(name, () -> {
            HbmPoweredAbilityToolItem item = HbmPoweredAbilityToolItem.shovel(damage, movement, tier,
                    toolProperties(tier, false), maxCharge, chargeRate, consumption);
            abilities.accept(item);
            return item;
        });
    }

    private static RegistryObject<Item> poweredAbilityMiner(String name, float damage, double movement, HbmToolTiers tier,
                                                            long maxCharge, long chargeRate, long consumption,
                                                            Consumer<HbmPoweredAbilityToolItem> abilities) {
        return registerLegacy(name, () -> {
            HbmPoweredAbilityToolItem item = HbmPoweredAbilityToolItem.miner(damage, movement, tier,
                    toolProperties(tier, false), maxCharge, chargeRate, consumption);
            abilities.accept(item);
            return item;
        });
    }

    private static RegistryObject<Item> fueledAbilityAxe(String name, float damage, double movement, HbmToolTiers tier,
                                                         int maxFuel, int consumption, int fillRate,
                                                         Consumer<HbmFueledAbilityToolItem> abilities,
                                                         com.hbm.ntm.fluid.FluidType... acceptedFuels) {
        return registerLegacy(name, () -> {
            HbmFueledAbilityToolItem item = HbmFueledAbilityToolItem.axe(damage, movement, tier,
                    toolProperties(tier, false), maxFuel, consumption, fillRate, acceptedFuels);
            abilities.accept(item);
            return item;
        });
    }

    private static Item.Properties toolProperties(HbmToolTiers tier, boolean rare) {
        return toolProperties(tier, rare, -1);
    }

    private static Item.Properties toolProperties(HbmToolTiers tier, boolean rare, int durability) {
        Item.Properties properties = new Item.Properties().stacksTo(1);
        if (durability > 0) {
            properties.durability(durability);
        } else if (tier.getUses() > 0) {
            properties.durability(tier.getUses());
        }
        if (rare) {
            properties.rarity(Rarity.RARE);
        }
        return properties;
    }

    private static HbmAbilityToolItem schrabidiumToolAbilities(HbmAbilityToolItem item) {
        return item.addAbility(WeaponAbilities.RADIATION, 0)
                .addAbility(ToolAreaAbilities.HAMMER, 1)
                .addAbility(ToolAreaAbilities.HAMMER_FLAT, 1)
                .addAbility(ToolAreaAbilities.RECURSION, 6)
                .addAbility(ToolHarvestAbilities.SILK, 0)
                .addAbility(ToolHarvestAbilities.LUCK, 4)
                .addAbility(ToolHarvestAbilities.SMELTER, 0)
                .addAbility(ToolHarvestAbilities.SHREDDER, 0);
    }

    private static HbmAbilityToolItem deshToolAbilities(HbmAbilityToolItem item) {
        return item.addAbility(ToolAreaAbilities.HAMMER, 0)
                .addAbility(ToolAreaAbilities.HAMMER_FLAT, 0)
                .addAbility(ToolAreaAbilities.RECURSION, 0)
                .addAbility(ToolHarvestAbilities.SILK, 0)
                .addAbility(ToolHarvestAbilities.LUCK, 1);
    }

    private static HbmAbilityToolItem cobaltToolAbilities(HbmAbilityToolItem item) {
        return item.addAbility(ToolAreaAbilities.RECURSION, 1)
                .addAbility(ToolHarvestAbilities.SILK, 0)
                .addAbility(ToolHarvestAbilities.LUCK, 0);
    }

    private static HbmAbilityToolItem cobaltDecoratedToolAbilities(HbmAbilityToolItem item) {
        return item.addAbility(ToolAreaAbilities.RECURSION, 1)
                .addAbility(ToolAreaAbilities.HAMMER, 0)
                .addAbility(ToolAreaAbilities.HAMMER_FLAT, 0)
                .addAbility(ToolHarvestAbilities.SILK, 0)
                .addAbility(ToolHarvestAbilities.LUCK, 2);
    }

    private static HbmAbilityToolItem starmetalToolAbilities(HbmAbilityToolItem item) {
        return item.addAbility(ToolAreaAbilities.RECURSION, 3)
                .addAbility(ToolAreaAbilities.HAMMER, 1)
                .addAbility(ToolAreaAbilities.HAMMER_FLAT, 1)
                .addAbility(ToolHarvestAbilities.SILK, 0)
                .addAbility(ToolHarvestAbilities.LUCK, 4);
    }

    private static HbmAbilityToolItem electricToolAbilities(HbmAbilityToolItem item) {
        return item.addAbility(ToolAreaAbilities.HAMMER, 0)
                .addAbility(ToolAreaAbilities.HAMMER_FLAT, 0)
                .addAbility(ToolAreaAbilities.RECURSION, 2)
                .addAbility(ToolHarvestAbilities.SILK, 0)
                .addAbility(ToolHarvestAbilities.LUCK, 1);
    }

    private static HbmAbilityToolItem bismuthToolAbilities(HbmAbilityToolItem item, int stunLevel, int vampireLevel, boolean beheader) {
        item.addAbility(ToolAreaAbilities.HAMMER, 1)
                .addAbility(ToolAreaAbilities.HAMMER_FLAT, 1)
                .addAbility(ToolAreaAbilities.RECURSION, 1)
                .addAbility(ToolHarvestAbilities.SHREDDER, 0)
                .addAbility(ToolHarvestAbilities.LUCK, 1)
                .addAbility(ToolHarvestAbilities.SILK, 0)
                .addAbility(WeaponAbilities.STUN, stunLevel)
                .addAbility(WeaponAbilities.VAMPIRE, vampireLevel);
        if (beheader) {
            item.addAbility(WeaponAbilities.BEHEADER, 0);
        }
        return item;
    }

    private static HbmAbilityToolItem volcanicToolAbilities(HbmAbilityToolItem item, int fireLevel, int vampireLevel, boolean beheader) {
        item.addAbility(ToolAreaAbilities.HAMMER, 1)
                .addAbility(ToolAreaAbilities.HAMMER_FLAT, 1)
                .addAbility(ToolAreaAbilities.RECURSION, 1)
                .addAbility(ToolHarvestAbilities.SMELTER, 0)
                .addAbility(ToolHarvestAbilities.LUCK, 2)
                .addAbility(ToolHarvestAbilities.SILK, 0)
                .addAbility(WeaponAbilities.FIRE, fireLevel)
                .addAbility(WeaponAbilities.VAMPIRE, vampireLevel);
        if (beheader) {
            item.addAbility(WeaponAbilities.BEHEADER, 0);
        }
        return item;
    }

    private static HbmAbilityToolItem chlorophyteToolAbilities(HbmAbilityToolItem item, int stunLevel, int vampireLevel, boolean beheader) {
        item.addAbility(ToolAreaAbilities.HAMMER, 1)
                .addAbility(ToolAreaAbilities.HAMMER_FLAT, 1)
                .addAbility(ToolAreaAbilities.RECURSION, 1)
                .addAbility(ToolHarvestAbilities.LUCK, 3)
                .addAbility(WeaponAbilities.STUN, stunLevel)
                .addAbility(WeaponAbilities.VAMPIRE, vampireLevel);
        if (beheader) {
            item.addAbility(WeaponAbilities.BEHEADER, 0);
        }
        return item;
    }

    private static HbmAbilityToolItem meseToolAbilities(HbmAbilityToolItem item, int stunLevel, int phosphorusLevel) {
        return item.addAbility(ToolAreaAbilities.HAMMER, 2)
                .addAbility(ToolAreaAbilities.HAMMER_FLAT, 2)
                .addAbility(ToolAreaAbilities.RECURSION, 2)
                .addAbility(ToolHarvestAbilities.SILK, 0)
                .addAbility(ToolHarvestAbilities.LUCK, 5)
                .addAbility(ToolAreaAbilities.EXPLOSION, 3)
                .addAbility(WeaponAbilities.STUN, stunLevel)
                .addAbility(WeaponAbilities.PHOSPHORUS, phosphorusLevel)
                .addAbility(WeaponAbilities.BEHEADER, 0);
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

    private static RegistryObject<Item> satelliteItem(String name, LegacySatelliteType type, String... descriptionKeys) {
        return registerLegacy(name, () -> new SatelliteChipItem(new Item.Properties(), type, descriptionKeys));
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
        return simpleStackSizeItem(name, 1);
    }

    private static RegistryObject<Item> simpleStackSizeItem(String name, int maxStackSize) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new Item(new Item.Properties().stacksTo(maxStackSize)));
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
