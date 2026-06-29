package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import com.hbm.ntm.armor.ArmorModGasMaskItem;
import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.ability.ToolAreaAbilities;
import com.hbm.ntm.ability.ToolHarvestAbilities;
import com.hbm.ntm.ability.WeaponAbilities;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmCreativeBatteryItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.item.AmmoBagItem;
import com.hbm.ntm.item.ArcElectrodeItem;
import com.hbm.ntm.item.AmsCatalystItem;
import com.hbm.ntm.item.AmsCoreItem;
import com.hbm.ntm.item.AmsLensItem;
import com.hbm.ntm.item.ArtilleryDesignatorItem;
import com.hbm.ntm.item.AntimatterCellItem;
import com.hbm.ntm.item.BedrockOreBaseItem;
import com.hbm.ntm.item.BedrockOreFragmentItem;
import com.hbm.ntm.item.BedrockOreItem;
import com.hbm.ntm.item.BjJetpackArmorItem;
import com.hbm.ntm.item.ChargeThrowerItem;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.DetonatorItem;
import com.hbm.ntm.item.DemonCoreItem;
import com.hbm.ntm.item.DieselSuitArmorItem;
import com.hbm.ntm.item.DnsArmorItem;
import com.hbm.ntm.item.DosimeterItem;
import com.hbm.ntm.item.DynamiteStickItem;
import com.hbm.ntm.item.AntimatterClusterItem;
import com.hbm.ntm.item.BjArmorItem;
import com.hbm.ntm.item.CasingBagItem;
import com.hbm.ntm.item.ChemthrowerItem;
import com.hbm.ntm.item.ChocolateItem;
import com.hbm.ntm.item.ColtanCompassItem;
import com.hbm.ntm.item.DigammaParticleItem;
import com.hbm.ntm.item.DigammaDiagnosticItem;
import com.hbm.ntm.item.DrillGunItem;
import com.hbm.ntm.item.DrillbitItem;
import com.hbm.ntm.item.EffectPillItem;
import com.hbm.ntm.item.ExpensiveModeItem;
import com.hbm.ntm.item.EnvSuitArmorItem;
import com.hbm.ntm.item.EuphemiumArmorItem;
import com.hbm.ntm.item.FabulousHatArmorItem;
import com.hbm.ntm.item.FiveHtpItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.FluidPipetteItem;
import com.hbm.ntm.item.FluidSiphonItem;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.item.FmnItem;
import com.hbm.ntm.item.FollyGunItem;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.item.FsbArmorItem;
import com.hbm.ntm.item.FsbFueledArmorItem;
import com.hbm.ntm.item.FsbPoweredArmorItem;
import com.hbm.ntm.item.GeigerCounterItem;
import com.hbm.ntm.item.GasMaskArmorItem;
import com.hbm.ntm.item.GasMaskFilterItem;
import com.hbm.ntm.item.GunRepairKitItem;
import com.hbm.ntm.item.GuideBookItem;
import com.hbm.ntm.item.HbmAbilitySwordItem;
import com.hbm.ntm.item.HbmAbilityToolItem;
import com.hbm.ntm.item.HazmatArmorItem;
import com.hbm.ntm.item.HazmatMaskArmorItem;
import com.hbm.ntm.item.HbmArmorMaterials;
import com.hbm.ntm.item.HbmFueledAbilityToolItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
import com.hbm.ntm.item.HbmPoweredAbilitySwordItem;
import com.hbm.ntm.item.HbmPoweredAbilityToolItem;
import com.hbm.ntm.item.HbmRagItem;
import com.hbm.ntm.item.HbmToolTiers;
import com.hbm.ntm.item.HerbalPasteItem;
import com.hbm.ntm.item.ICFPelletItem;
import com.hbm.ntm.item.IodinePillItem;
import com.hbm.ntm.item.ItemBlueprints;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.item.JetpackTankItem;
import com.hbm.ntm.item.KeyPinItem;
import com.hbm.ntm.item.LegacyToolItem;
import com.hbm.ntm.item.LegacyArtilleryAmmoItem;
import com.hbm.ntm.item.LegacyLoreItem;
import com.hbm.ntm.item.LegacySyringeItem;
import com.hbm.ntm.item.LegacyWiringItem;
import com.hbm.ntm.item.LiquidatorArmorItem;
import com.hbm.ntm.item.LiquidatorMaskArmorItem;
import com.hbm.ntm.item.MarshmallowItem;
import com.hbm.ntm.item.MeteoriteSwordItem;
import com.hbm.ntm.item.MirrorToolItem;
import com.hbm.ntm.item.PadlockItem;
import com.hbm.ntm.item.MissileLauncherGunItem;
import com.hbm.ntm.item.MissileDesignatorItem;
import com.hbm.ntm.item.RangefinderItem;
import com.hbm.ntm.item.Ni4NiGunItem;
import com.hbm.ntm.item.NcrpaArmorItem;
import com.hbm.ntm.item.No9ArmorItem;
import com.hbm.ntm.item.ObjArmorItem;
import com.hbm.ntm.item.OreByproductItem;
import com.hbm.ntm.item.OilDetectorItem;
import com.hbm.ntm.item.OreDensityScannerItem;
import com.hbm.ntm.item.PACoilItem;
import com.hbm.ntm.item.PollutionDetectorItem;
import com.hbm.ntm.item.PlanCItem;
import com.hbm.ntm.item.PlasticScrapItem;
import com.hbm.ntm.item.PWRFuelItem;
import com.hbm.ntm.item.PWRPrinterItem;
import com.hbm.ntm.item.RadawayItem;
import com.hbm.ntm.item.RadarLinkerItem;
import com.hbm.ntm.item.PistonSetItem;
import com.hbm.ntm.item.RedPillItem;
import com.hbm.ntm.item.ReactorSensorItem;
import com.hbm.ntm.item.RTTYPagerItem;
import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.item.RBMKPelletItem;
import com.hbm.ntm.item.RBMKToolItem;
import com.hbm.ntm.item.SettingsToolItem;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.item.ShredderBladeItem;
import com.hbm.ntm.item.SingularityItem;
import com.hbm.ntm.item.SioxItem;
import com.hbm.ntm.item.SirenCassetteItem;
import com.hbm.ntm.item.SteamsuitArmorItem;
import com.hbm.ntm.item.StingerGunItem;
import com.hbm.ntm.item.SurveyScannerItem;
import com.hbm.ntm.item.TauCannonItem;
import com.hbm.ntm.item.TeleLinkItem;
import com.hbm.ntm.item.ToolboxItem;
import com.hbm.ntm.item.TrenchmasterArmorItem;
import com.hbm.ntm.item.TurretChipItem;
import com.hbm.ntm.item.VodkaCanteenItem;
import com.hbm.ntm.item.WeaponModItem;
import com.hbm.ntm.item.WatzPelletItem;
import com.hbm.ntm.item.XanaxItem;
import com.hbm.ntm.item.ZirnoxRodItem;
import com.hbm.ntm.item.missile.CustomMissileItem;
import com.hbm.ntm.item.missile.MissileItem;
import com.hbm.ntm.item.missile.MissilePartItem;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidContainerRules;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.bullet.LegacySednaGunConfigs;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.neutron.RBMKFuelRodRegistry;
import com.hbm.ntm.recipe.WatzFuelRuntime;
import com.hbm.ntm.recipe.PWRFuelRuntime;
import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.satellite.SatelliteChipItem;
import com.hbm.ntm.satellite.SatelliteDesignatorItem;
import com.hbm.ntm.satellite.SatelliteInterfaceItem;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import com.hbm.ntm.item.AmmoContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
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
    public static final RegistryObject<Item> BORON_POWDER = part("powder_boron");
    public static final RegistryObject<Item> LEAD_POWDER = part("powder_lead");
    public static final RegistryObject<Item> POWDER_POWER = part("powder_power");
    public static final RegistryObject<Item> POWDER_SAWDUST = part("powder_sawdust");
    public static final RegistryObject<Item> SCRAP = part("scrap");
    public static final RegistryObject<Item> BEDROCK_ORE_BASE = registerLegacy("bedrock_ore_base",
            () -> new BedrockOreBaseItem(new Item.Properties()));
    public static final RegistryObject<Item> BEDROCK_ORE = registerLegacy("bedrock_ore",
            () -> new BedrockOreItem(new Item.Properties()));
    public static final RegistryObject<Item> BEDROCK_ORE_FRAGMENT = registerLegacy("bedrock_ore_fragment",
            () -> new BedrockOreFragmentItem(new Item.Properties()));
    public static final RegistryObject<Item> SCRAP_PLASTIC = registerLegacy("scrap_plastic",
            () -> new PlasticScrapItem(new Item.Properties()));
    public static final RegistryObject<Item> MOLD = registerLegacy("mold",
            () -> new FoundryMoldItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FOUNDRY_SCRAPS = registerLegacy("scraps",
            () -> new FoundryScrapsItem(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_COIL = part("coil_copper");
    public static final RegistryObject<Item> TUNGSTEN_COIL = part("coil_tungsten");
    public static final RegistryObject<Item> GOLD_COIL = part("coil_gold");
    public static final RegistryObject<Item> MOTOR = part("motor");
    public static final RegistryObject<Item> GEAR_LARGE = part("gear_large");
    public static final RegistryObject<Item> GEAR_LARGE_STEEL = part("gear_large_steel");
    public static final RegistryObject<Item> SAWBLADE = part("sawblade");
    public static final RegistryObject<Item> SHREDDER_BLADES_STEEL = shredderBlade("blades_steel", 400);
    public static final RegistryObject<Item> SHREDDER_BLADES_TITANIUM = shredderBlade("blades_titanium", 500);
    public static final RegistryObject<Item> SHREDDER_BLADES_DESH = shredderBlade("blades_desh", 0);
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
    public static final RegistryObject<Item> UPGRADE_FORTUNE_1 = machineUpgrade("upgrade_fortune_1", UpgradeType.FORTUNE, 1);
    public static final RegistryObject<Item> UPGRADE_FORTUNE_2 = machineUpgrade("upgrade_fortune_2", UpgradeType.FORTUNE, 2);
    public static final RegistryObject<Item> UPGRADE_FORTUNE_3 = machineUpgrade("upgrade_fortune_3", UpgradeType.FORTUNE, 3);
    public static final RegistryObject<Item> UPGRADE_SMELTER = machineUpgrade("upgrade_smelter", UpgradeType.SMELTER, 1);
    public static final RegistryObject<Item> UPGRADE_NULLIFIER = machineUpgrade("upgrade_nullifier", UpgradeType.NULLIFIER, 1);
    public static final RegistryObject<Item> UPGRADE_SHREDDER = machineUpgrade("upgrade_shredder", UpgradeType.SHREDDER, 1);
    public static final RegistryObject<Item> UPGRADE_CENTRIFUGE = machineUpgrade("upgrade_centrifuge", UpgradeType.CENTRIFUGE, 1);
    public static final RegistryObject<Item> UPGRADE_CRYSTALLIZER = machineUpgrade("upgrade_crystallizer", UpgradeType.CRYSTALLIZER, 1);
    public static final RegistryObject<Item> UPGRADE_SCREM = simpleStackOneItem("upgrade_screm");
    public static final RegistryObject<Item> UPGRADE_RADIUS = simpleStackSizeItem("upgrade_radius", 16);
    public static final RegistryObject<Item> UPGRADE_HEALTH = simpleStackSizeItem("upgrade_health", 16);
    public static final RegistryObject<Item> TEMPLATE_FOLDER = registerLegacy("template_folder",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TURRET_CHIP = registerLegacy("turret_chip",
            () -> new TurretChipItem(new Item.Properties()));
    public static final RegistryObject<Item> WIRING_RED_COPPER = registerLegacy("wiring_red_copper",
            () -> new LegacyWiringItem(new Item.Properties().stacksTo(1)));

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
    public static final RegistryObject<Item> AMMO_BAG = registerLegacy("ammo_bag",
            () -> new AmmoBagItem(new Item.Properties(), false));
    public static final RegistryObject<Item> AMMO_BAG_INFINITE = registerLegacy("ammo_bag_infinite",
            () -> new AmmoBagItem(new Item.Properties(), true));
    public static final RegistryObject<Item> CASING_BAG = registerLegacy("casing_bag",
            () -> new CasingBagItem(new Item.Properties()));
    public static final RegistryObject<Item> GUN_KIT_1 = registerLegacy("gun_kit_1",
            () -> new GunRepairKitItem(new Item.Properties(), 10));
    public static final RegistryObject<Item> GUN_KIT_2 = registerLegacy("gun_kit_2",
            () -> new GunRepairKitItem(new Item.Properties(), 100));
    public static final RegistryObject<Item> AMMO_CONTAINER = registerLegacy("ammo_container",
            () -> new AmmoContainerItem(new Item.Properties(), false));
    public static final RegistryObject<Item> AMMO_CONTAINER_ALT = registerLegacy("ammo_container_alt",
            () -> new AmmoContainerItem(new Item.Properties(), true));
    public static final RegistryObject<Item> AMMO_STANDARD_STONE = simpleItem("ammo_standard_stone");
    public static final RegistryObject<Item> AMMO_STANDARD_STONE_AP = simpleItem("ammo_standard_stone_ap");
    public static final RegistryObject<Item> AMMO_STANDARD_STONE_IRON = simpleItem("ammo_standard_stone_iron");
    public static final RegistryObject<Item> AMMO_STANDARD_STONE_SHOT = simpleItem("ammo_standard_stone_shot");
    public static final RegistryObject<Item> AMMO_STANDARD_G12_BP = simpleItem("ammo_standard_g12_bp");
    public static final RegistryObject<Item> AMMO_STANDARD_G12_BP_MAGNUM = simpleItem("ammo_standard_g12_bp_magnum");
    public static final RegistryObject<Item> AMMO_STANDARD_G12_BP_SLUG = simpleItem("ammo_standard_g12_bp_slug");
    public static final RegistryObject<Item> AMMO_STANDARD_G12 = simpleItem("ammo_standard_g12");
    public static final RegistryObject<Item> AMMO_STANDARD_G12_SLUG = simpleItem("ammo_standard_g12_slug");
    public static final RegistryObject<Item> AMMO_STANDARD_G12_FLECHETTE = simpleItem("ammo_standard_g12_flechette");
    public static final RegistryObject<Item> AMMO_STANDARD_G12_MAGNUM = simpleItem("ammo_standard_g12_magnum");
    public static final RegistryObject<Item> AMMO_STANDARD_G12_EXPLOSIVE = simpleItem("ammo_standard_g12_explosive");
    public static final RegistryObject<Item> AMMO_STANDARD_G12_PHOSPHORUS = simpleItem("ammo_standard_g12_phosphorus");
    public static final RegistryObject<Item> AMMO_STANDARD_G10 = simpleItem("ammo_standard_g10");
    public static final RegistryObject<Item> AMMO_STANDARD_G10_SHRAPNEL = simpleItem("ammo_standard_g10_shrapnel");
    public static final RegistryObject<Item> AMMO_STANDARD_G10_DU = simpleItem("ammo_standard_g10_du");
    public static final RegistryObject<Item> AMMO_STANDARD_G10_SLUG = simpleItem("ammo_standard_g10_slug");
    public static final RegistryObject<Item> AMMO_STANDARD_G10_EXPLOSIVE = simpleItem("ammo_standard_g10_explosive");
    public static final RegistryObject<Item> AMMO_STANDARD_P22_SP = simpleItem("ammo_standard_p22_sp");
    public static final RegistryObject<Item> AMMO_STANDARD_P22_FMJ = simpleItem("ammo_standard_p22_fmj");
    public static final RegistryObject<Item> AMMO_STANDARD_P22_JHP = simpleItem("ammo_standard_p22_jhp");
    public static final RegistryObject<Item> AMMO_STANDARD_P22_AP = simpleItem("ammo_standard_p22_ap");
    public static final RegistryObject<Item> AMMO_STANDARD_P9_SP = simpleItem("ammo_standard_p9_sp");
    public static final RegistryObject<Item> AMMO_STANDARD_P9_FMJ = simpleItem("ammo_standard_p9_fmj");
    public static final RegistryObject<Item> AMMO_STANDARD_P9_JHP = simpleItem("ammo_standard_p9_jhp");
    public static final RegistryObject<Item> AMMO_STANDARD_P9_AP = simpleItem("ammo_standard_p9_ap");
    public static final RegistryObject<Item> AMMO_STANDARD_P45_SP = simpleItem("ammo_standard_p45_sp");
    public static final RegistryObject<Item> AMMO_STANDARD_P45_FMJ = simpleItem("ammo_standard_p45_fmj");
    public static final RegistryObject<Item> AMMO_STANDARD_P45_JHP = simpleItem("ammo_standard_p45_jhp");
    public static final RegistryObject<Item> AMMO_STANDARD_P45_AP = simpleItem("ammo_standard_p45_ap");
    public static final RegistryObject<Item> AMMO_STANDARD_P45_DU = simpleItem("ammo_standard_p45_du");
    public static final RegistryObject<Item> AMMO_STANDARD_R556_SP = simpleItem("ammo_standard_r556_sp");
    public static final RegistryObject<Item> AMMO_STANDARD_R556_FMJ = simpleItem("ammo_standard_r556_fmj");
    public static final RegistryObject<Item> AMMO_STANDARD_R556_JHP = simpleItem("ammo_standard_r556_jhp");
    public static final RegistryObject<Item> AMMO_STANDARD_R556_AP = simpleItem("ammo_standard_r556_ap");
    public static final RegistryObject<Item> AMMO_STANDARD_M44_BP = simpleItem("ammo_standard_m44_bp");
    public static final RegistryObject<Item> AMMO_STANDARD_M44_SP = simpleItem("ammo_standard_m44_sp");
    public static final RegistryObject<Item> AMMO_STANDARD_M44_FMJ = simpleItem("ammo_standard_m44_fmj");
    public static final RegistryObject<Item> AMMO_STANDARD_M44_JHP = simpleItem("ammo_standard_m44_jhp");
    public static final RegistryObject<Item> AMMO_STANDARD_M44_AP = simpleItem("ammo_standard_m44_ap");
    public static final RegistryObject<Item> AMMO_STANDARD_M44_EXPRESS = simpleItem("ammo_standard_m44_express");
    public static final RegistryObject<Item> AMMO_STANDARD_M357_BP = simpleItem("ammo_standard_m357_bp");
    public static final RegistryObject<Item> AMMO_STANDARD_M357_SP = simpleItem("ammo_standard_m357_sp");
    public static final RegistryObject<Item> AMMO_STANDARD_M357_FMJ = simpleItem("ammo_standard_m357_fmj");
    public static final RegistryObject<Item> AMMO_STANDARD_M357_JHP = simpleItem("ammo_standard_m357_jhp");
    public static final RegistryObject<Item> AMMO_STANDARD_M357_AP = simpleItem("ammo_standard_m357_ap");
    public static final RegistryObject<Item> AMMO_STANDARD_M357_EXPRESS = simpleItem("ammo_standard_m357_express");
    public static final RegistryObject<Item> AMMO_STANDARD_R762_SP = simpleItem("ammo_standard_r762_sp");
    public static final RegistryObject<Item> AMMO_STANDARD_R762_FMJ = simpleItem("ammo_standard_r762_fmj");
    public static final RegistryObject<Item> AMMO_STANDARD_R762_JHP = simpleItem("ammo_standard_r762_jhp");
    public static final RegistryObject<Item> AMMO_STANDARD_R762_AP = simpleItem("ammo_standard_r762_ap");
    public static final RegistryObject<Item> AMMO_STANDARD_R762_DU = simpleItem("ammo_standard_r762_du");
    public static final RegistryObject<Item> AMMO_STANDARD_R762_HE = simpleItem("ammo_standard_r762_he");
    public static final RegistryObject<Item> AMMO_STANDARD_BMG50_SP = simpleItem("ammo_standard_bmg50_sp");
    public static final RegistryObject<Item> AMMO_STANDARD_BMG50_FMJ = simpleItem("ammo_standard_bmg50_fmj");
    public static final RegistryObject<Item> AMMO_STANDARD_BMG50_JHP = simpleItem("ammo_standard_bmg50_jhp");
    public static final RegistryObject<Item> AMMO_STANDARD_BMG50_AP = simpleItem("ammo_standard_bmg50_ap");
    public static final RegistryObject<Item> AMMO_STANDARD_BMG50_DU = simpleItem("ammo_standard_bmg50_du");
    public static final RegistryObject<Item> AMMO_STANDARD_BMG50_HE = simpleItem("ammo_standard_bmg50_he");
    public static final RegistryObject<Item> AMMO_STANDARD_BMG50_SM = simpleItem("ammo_standard_bmg50_sm");
    public static final RegistryObject<Item> AMMO_STANDARD_B75 = simpleItem("ammo_standard_b75");
    public static final RegistryObject<Item> AMMO_STANDARD_B75_INC = simpleItem("ammo_standard_b75_inc");
    public static final RegistryObject<Item> AMMO_STANDARD_B75_EXP = simpleItem("ammo_standard_b75_exp");
    public static final RegistryObject<Item> AMMO_STANDARD_G26_FLARE = simpleItem("ammo_standard_g26_flare");
    public static final RegistryObject<Item> AMMO_STANDARD_G26_FLARE_SUPPLY = simpleItem(
            "ammo_standard_g26_flare_supply");
    public static final RegistryObject<Item> AMMO_STANDARD_G26_FLARE_WEAPON = simpleItem(
            "ammo_standard_g26_flare_weapon");
    public static final RegistryObject<Item> AMMO_STANDARD_G40_HE = simpleItem("ammo_standard_g40_he");
    public static final RegistryObject<Item> AMMO_STANDARD_G40_HEAT = simpleItem("ammo_standard_g40_heat");
    public static final RegistryObject<Item> AMMO_STANDARD_G40_DEMO = simpleItem("ammo_standard_g40_demo");
    public static final RegistryObject<Item> AMMO_STANDARD_G40_INC = simpleItem("ammo_standard_g40_inc");
    public static final RegistryObject<Item> AMMO_STANDARD_G40_PHOSPHORUS = simpleItem(
            "ammo_standard_g40_phosphorus");
    public static final RegistryObject<Item> AMMO_STANDARD_ROCKET_HE = simpleItem("ammo_standard_rocket_he");
    public static final RegistryObject<Item> AMMO_STANDARD_ROCKET_HEAT = simpleItem("ammo_standard_rocket_heat");
    public static final RegistryObject<Item> AMMO_STANDARD_ROCKET_DEMO = simpleItem("ammo_standard_rocket_demo");
    public static final RegistryObject<Item> AMMO_STANDARD_ROCKET_INC = simpleItem("ammo_standard_rocket_inc");
    public static final RegistryObject<Item> AMMO_STANDARD_ROCKET_PHOSPHORUS = simpleItem(
            "ammo_standard_rocket_phosphorus");
    public static final RegistryObject<Item> AMMO_STANDARD_CAPACITOR = simpleItem("ammo_standard_capacitor");
    public static final RegistryObject<Item> AMMO_STANDARD_CAPACITOR_OVERCHARGE = simpleItem(
            "ammo_standard_capacitor_overcharge");
    public static final RegistryObject<Item> AMMO_STANDARD_CAPACITOR_IR = simpleItem("ammo_standard_capacitor_ir");
    public static final RegistryObject<Item> AMMO_STANDARD_COIL_TUNGSTEN = simpleItem("ammo_standard_coil_tungsten");
    public static final RegistryObject<Item> AMMO_STANDARD_COIL_FERROURANIUM = simpleItem(
            "ammo_standard_coil_ferrouranium");
    public static final RegistryObject<Item> AMMO_STANDARD_FLAME_DIESEL = simpleItem("ammo_standard_flame_diesel");
    public static final RegistryObject<Item> AMMO_STANDARD_FLAME_GAS = simpleItem("ammo_standard_flame_gas");
    public static final RegistryObject<Item> AMMO_STANDARD_FLAME_NAPALM = simpleItem("ammo_standard_flame_napalm");
    public static final RegistryObject<Item> AMMO_STANDARD_FLAME_BALEFIRE = simpleItem("ammo_standard_flame_balefire");
    public static final RegistryObject<Item> AMMO_STANDARD_NUKE_STANDARD = simpleItem("ammo_standard_nuke_standard");
    public static final RegistryObject<Item> AMMO_STANDARD_NUKE_DEMO = simpleItem("ammo_standard_nuke_demo");
    public static final RegistryObject<Item> AMMO_STANDARD_NUKE_HIGH = simpleItem("ammo_standard_nuke_high");
    public static final RegistryObject<Item> AMMO_STANDARD_NUKE_TOTS = simpleItem("ammo_standard_nuke_tots");
    public static final RegistryObject<Item> AMMO_STANDARD_NUKE_HIVE = simpleItem("ammo_standard_nuke_hive");
    public static final RegistryObject<Item> AMMO_STANDARD_NUKE_BALEFIRE = simpleItem("ammo_standard_nuke_balefire");
    public static final RegistryObject<Item> AMMO_FIREEXT_0 = simpleItem("ammo_fireext_0");
    public static final RegistryObject<Item> AMMO_FIREEXT_1 = simpleItem("ammo_fireext_1");
    public static final RegistryObject<Item> AMMO_FIREEXT_2 = simpleItem("ammo_fireext_2");
    public static final RegistryObject<Item> AMMO_SECRET_FOLLY_SM = simpleItem("ammo_secret_folly_sm");
    public static final RegistryObject<Item> AMMO_SECRET_FOLLY_NUKE = simpleItem("ammo_secret_folly_nuke");
    public static final RegistryObject<Item> AMMO_SECRET_M44_EQUESTRIAN = simpleItem("ammo_secret_m44_equestrian");
    public static final RegistryObject<Item> AMMO_SECRET_G12_EQUESTRIAN = simpleItem("ammo_secret_g12_equestrian");
    public static final RegistryObject<Item> AMMO_SECRET_BMG50_EQUESTRIAN = simpleItem("ammo_secret_bmg50_equestrian");
    public static final RegistryObject<Item> AMMO_SECRET_P35_800 = simpleItem("ammo_secret_p35_800");
    public static final RegistryObject<Item> AMMO_SECRET_BMG50_BLACK = simpleItem("ammo_secret_bmg50_black");
    public static final RegistryObject<Item> AMMO_SECRET_P35_800_BL = simpleItem("ammo_secret_p35_800_bl");
    public static final RegistryObject<Item> AMMO_STANDARD_TAU_URANIUM = simpleItem("ammo_standard_tau_uranium");
    public static final RegistryObject<Item> AMMO_STANDARD_CT_HOOK = simpleItem("ammo_standard_ct_hook");
    public static final RegistryObject<Item> AMMO_STANDARD_CT_MORTAR = simpleItem("ammo_standard_ct_mortar");
    public static final RegistryObject<Item> AMMO_STANDARD_CT_MORTAR_CHARGE = simpleItem(
            "ammo_standard_ct_mortar_charge");
    private static final List<WeaponModItem.Spec> WEAPON_MOD_SPECS = List.of(
            WeaponModItem.Spec.test("firerate", 0),
            WeaponModItem.Spec.test("damage", 1),
            WeaponModItem.Spec.test("multi", 2),
            WeaponModItem.Spec.test("override_2_5", 3),
            WeaponModItem.Spec.test("override_5", 4),
            WeaponModItem.Spec.test("override_7_5", 5),
            WeaponModItem.Spec.test("override_10", 6),
            WeaponModItem.Spec.test("override_12_5", 7),
            WeaponModItem.Spec.test("override_15", 8),
            WeaponModItem.Spec.test("override_20", 9),
            WeaponModItem.Spec.generic("iron_damage", 0),
            WeaponModItem.Spec.generic("iron_dura", 1),
            WeaponModItem.Spec.generic("steel_damage", 2),
            WeaponModItem.Spec.generic("steel_dura", 3),
            WeaponModItem.Spec.generic("dura_damage", 4),
            WeaponModItem.Spec.generic("dura_dura", 5),
            WeaponModItem.Spec.generic("desh_damage", 6),
            WeaponModItem.Spec.generic("desh_dura", 7),
            WeaponModItem.Spec.generic("wsteel_damage", 8),
            WeaponModItem.Spec.generic("wsteel_dura", 9),
            WeaponModItem.Spec.generic("ferro_damage", 10),
            WeaponModItem.Spec.generic("ferro_dura", 11),
            WeaponModItem.Spec.generic("tcalloy_damage", 12),
            WeaponModItem.Spec.generic("tcalloy_dura", 13),
            WeaponModItem.Spec.generic("bigmt_damage", 14),
            WeaponModItem.Spec.generic("bigmt_dura", 15),
            WeaponModItem.Spec.generic("bronze_damage", 16),
            WeaponModItem.Spec.generic("bronze_dura", 17),
            WeaponModItem.Spec.special("silencer", 0),
            WeaponModItem.Spec.special("scope", 1),
            WeaponModItem.Spec.special("saw", 2),
            WeaponModItem.Spec.special("greasegun", 3),
            WeaponModItem.Spec.special("slowdown", 4),
            WeaponModItem.Spec.special("speedup", 5),
            WeaponModItem.Spec.special("choke", 6),
            WeaponModItem.Spec.special("speedloader", 7),
            WeaponModItem.Spec.special("furniture_green", 8),
            WeaponModItem.Spec.special("furniture_black", 9),
            WeaponModItem.Spec.special("bayonet", 10),
            WeaponModItem.Spec.special("stack_mag", 11),
            WeaponModItem.Spec.special("skin_saturnite", 12),
            WeaponModItem.Spec.special("las_shotgun", 13),
            WeaponModItem.Spec.special("las_capacitor", 14),
            WeaponModItem.Spec.special("las_auto", 15),
            WeaponModItem.Spec.special("nickel", 16),
            WeaponModItem.Spec.special("doubloons", 17),
            WeaponModItem.Spec.special("drill_hss", 18),
            WeaponModItem.Spec.special("drill_weaponsteel", 19),
            WeaponModItem.Spec.special("drill_tcalloy", 20),
            WeaponModItem.Spec.special("drill_saturnite", 21),
            WeaponModItem.Spec.special("engine_diesel", 22),
            WeaponModItem.Spec.special("engine_aviation", 23),
            WeaponModItem.Spec.special("engine_electric", 24),
            WeaponModItem.Spec.special("engine_turbo", 25),
            WeaponModItem.Spec.special("magnet", 26),
            WeaponModItem.Spec.special("sifter", 27),
            WeaponModItem.Spec.special("canisters", 28),
            WeaponModItem.Spec.caliber("p9", 0),
            WeaponModItem.Spec.caliber("p45", 1),
            WeaponModItem.Spec.caliber("p22", 2),
            WeaponModItem.Spec.caliber("m357", 3),
            WeaponModItem.Spec.caliber("m44", 4),
            WeaponModItem.Spec.caliber("r556", 5),
            WeaponModItem.Spec.caliber("r762", 6),
            WeaponModItem.Spec.caliber("bmg50", 7)
    );
    public static final List<RegistryObject<Item>> WEAPON_MOD_ITEMS = WEAPON_MOD_SPECS.stream()
            .map(ModItems::weaponMod)
            .toList();
    public static final List<RegistryObject<Item>> WEAPON_MOD_CREATIVE_ITEMS = WEAPON_MOD_SPECS.stream()
            .filter(WeaponModItem.Spec::creativeTab)
            .map(spec -> ITEMS_BY_LEGACY_NAME.get(spec.modernName()))
            .toList();
    public static final List<RegistryObject<Item>> WEAPON_MOD_TEST_ITEMS = WEAPON_MOD_SPECS.stream()
            .filter(spec -> !spec.creativeTab())
            .map(spec -> ITEMS_BY_LEGACY_NAME.get(spec.modernName()))
            .toList();
    public static final List<RegistryObject<Item>> SEDNA_GUN_PART_ITEMS = simpleParts(
            "barrel_light_steel",
            "barrel_light_gunmetal",
            "barrel_light_dura_steel",
            "barrel_light_desh",
            "barrel_light_weaponsteel",
            "barrel_light_saturnite",
            "barrel_light_bismuth_bronze",
            "barrel_light_arsenic_bronze",
            "barrel_light_tcalloy",
            "barrel_light_cdalloy",
            "barrel_heavy_steel",
            "barrel_heavy_gunmetal",
            "barrel_heavy_dura_steel",
            "barrel_heavy_desh",
            "barrel_heavy_weaponsteel",
            "barrel_heavy_saturnite",
            "barrel_heavy_ferrouranium",
            "barrel_heavy_tcalloy",
            "barrel_heavy_cdalloy",
            "receiver_light_steel",
            "receiver_light_gunmetal",
            "receiver_light_dura_steel",
            "receiver_light_desh",
            "receiver_light_weaponsteel",
            "receiver_light_saturnite",
            "receiver_light_bismuth_bronze",
            "receiver_light_arsenic_bronze",
            "receiver_light_tcalloy",
            "receiver_light_cdalloy",
            "receiver_heavy_dura_steel",
            "receiver_heavy_gunmetal",
            "receiver_heavy_weaponsteel",
            "receiver_heavy_saturnite",
            "receiver_heavy_ferrouranium",
            "receiver_heavy_tcalloy",
            "receiver_heavy_cdalloy",
            "receiver_heavy_bismuth_bronze",
            "receiver_heavy_arsenic_bronze",
            "stock_wood",
            "stock_polymer",
            "stock_bakelite",
            "stock_pc",
            "stock_pvc",
            "stock_desh",
            "stock_gunmetal",
            "stock_weaponsteel",
            "stock_saturnite",
            "grip_wood",
            "grip_ivory",
            "grip_steel",
            "grip_dura_steel",
            "grip_desh",
            "grip_gunmetal",
            "grip_weaponsteel",
            "grip_saturnite",
            "grip_polymer",
            "grip_bakelite",
            "grip_pc",
            "grip_pvc",
            "grip_rubber",
            "plate_cast_desh",
            "plate_cast_weaponsteel",
            "plate_cast_saturnite",
            "plate_cast_tcalloy",
            "plate_cast_cdalloy");
    public static final RegistryObject<Item> AMMO_DGK = simpleItem("ammo_dgk");
    public static final RegistryObject<Item> AMMO_SHELL_STOCK = simpleItem("ammo_shell_stock");
    public static final RegistryObject<Item> AMMO_SHELL_EXPLOSIVE = simpleItem("ammo_shell_explosive");
    public static final RegistryObject<Item> AMMO_SHELL_APFSDS_T = simpleItem("ammo_shell_apfsds_t");
    public static final RegistryObject<Item> AMMO_SHELL_APFSDS_DU = simpleItem("ammo_shell_apfsds_du");
    public static final RegistryObject<Item> AMMO_SHELL_W9 = simpleItem("ammo_shell_w9");
    public static final RegistryObject<Item> AMMO_ARTY = artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY);
    public static final RegistryObject<Item> AMMO_ARTY_CLASSIC =
            artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_CLASSIC);
    public static final RegistryObject<Item> AMMO_ARTY_HE = artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_HE);
    public static final RegistryObject<Item> AMMO_ARTY_PHOSPHORUS =
            artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_PHOSPHORUS);
    public static final RegistryObject<Item> AMMO_ARTY_PHOSPHORUS_MULTI =
            artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_PHOSPHORUS_MULTI);
    public static final RegistryObject<Item> AMMO_ARTY_MINI_NUKE =
            artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_MINI_NUKE);
    public static final RegistryObject<Item> AMMO_ARTY_MINI_NUKE_MULTI =
            artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_MINI_NUKE_MULTI);
    public static final RegistryObject<Item> AMMO_ARTY_NUKE = artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_NUKE);
    public static final RegistryObject<Item> AMMO_ARTY_CARGO =
            artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_CARGO);
    public static final RegistryObject<Item> AMMO_ARTY_CHLORINE =
            artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_CHLORINE);
    public static final RegistryObject<Item> AMMO_ARTY_PHOSGENE =
            artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_PHOSGENE);
    public static final RegistryObject<Item> AMMO_ARTY_MUSTARD_GAS =
            artilleryAmmo(LegacyArtilleryAmmoCatalog.AMMO_ARTY_MUSTARD_GAS);
    public static final RegistryObject<Item> AMMO_HIMARS_STANDARD =
            himarsAmmo(LegacyArtilleryAmmoCatalog.AMMO_HIMARS_STANDARD);
    public static final RegistryObject<Item> AMMO_HIMARS_STANDARD_HE =
            himarsAmmo(LegacyArtilleryAmmoCatalog.AMMO_HIMARS_STANDARD_HE);
    public static final RegistryObject<Item> AMMO_HIMARS_STANDARD_WP =
            himarsAmmo(LegacyArtilleryAmmoCatalog.AMMO_HIMARS_STANDARD_WP);
    public static final RegistryObject<Item> AMMO_HIMARS_STANDARD_TB =
            himarsAmmo(LegacyArtilleryAmmoCatalog.AMMO_HIMARS_STANDARD_TB);
    public static final RegistryObject<Item> AMMO_HIMARS_STANDARD_LAVA =
            himarsAmmo(LegacyArtilleryAmmoCatalog.AMMO_HIMARS_STANDARD_LAVA);
    public static final RegistryObject<Item> AMMO_HIMARS_STANDARD_MINI_NUKE =
            himarsAmmo(LegacyArtilleryAmmoCatalog.AMMO_HIMARS_STANDARD_MINI_NUKE);
    public static final RegistryObject<Item> AMMO_HIMARS_SINGLE =
            himarsAmmo(LegacyArtilleryAmmoCatalog.AMMO_HIMARS_SINGLE);
    public static final RegistryObject<Item> AMMO_HIMARS_SINGLE_TB =
            himarsAmmo(LegacyArtilleryAmmoCatalog.AMMO_HIMARS_SINGLE_TB);
    public static final RegistryObject<Item> GUN_PEPPERBOX = sednaGun(LegacySednaGunConfigs.GUN_PEPPERBOX);
    public static final RegistryObject<Item> GUN_MARESLEG = sednaGun(LegacySednaGunConfigs.GUN_MARESLEG);
    public static final RegistryObject<Item> GUN_MARESLEG_AKIMBO = sednaGun(
            LegacySednaGunConfigs.GUN_MARESLEG_AKIMBO);
    public static final RegistryObject<Item> GUN_MARESLEG_BROKEN = sednaGun(LegacySednaGunConfigs.GUN_MARESLEG_BROKEN);
    public static final RegistryObject<Item> GUN_LIBERATOR = sednaGun(LegacySednaGunConfigs.GUN_LIBERATOR);
    public static final RegistryObject<Item> GUN_SPAS12 = sednaGun(LegacySednaGunConfigs.GUN_SPAS12);
    public static final RegistryObject<Item> GUN_AUTOSHOTGUN = sednaGun(LegacySednaGunConfigs.GUN_AUTOSHOTGUN);
    public static final RegistryObject<Item> GUN_AUTOSHOTGUN_SHREDDER = sednaGun(
            LegacySednaGunConfigs.GUN_AUTOSHOTGUN_SHREDDER);
    public static final RegistryObject<Item> GUN_AUTOSHOTGUN_SEXY = sednaGun(LegacySednaGunConfigs.GUN_AUTOSHOTGUN_SEXY);
    public static final RegistryObject<Item> GUN_DOUBLE_BARREL = sednaGun(LegacySednaGunConfigs.GUN_DOUBLE_BARREL);
    public static final RegistryObject<Item> GUN_DOUBLE_BARREL_SACRED_DRAGON = sednaGun(
            LegacySednaGunConfigs.GUN_DOUBLE_BARREL_SACRED_DRAGON);
    public static final RegistryObject<Item> GUN_AUTOSHOTGUN_HERETIC = sednaGun(
            LegacySednaGunConfigs.GUN_AUTOSHOTGUN_HERETIC);
    public static final RegistryObject<Item> GUN_LIGHT_REVOLVER = sednaGun(
            LegacySednaGunConfigs.GUN_LIGHT_REVOLVER);
    public static final RegistryObject<Item> GUN_LIGHT_REVOLVER_ATLAS = sednaGun(
            LegacySednaGunConfigs.GUN_LIGHT_REVOLVER_ATLAS);
    public static final RegistryObject<Item> GUN_LIGHT_REVOLVER_DANI = sednaGun(
            LegacySednaGunConfigs.GUN_LIGHT_REVOLVER_DANI);
    public static final RegistryObject<Item> GUN_HENRY = sednaGun(LegacySednaGunConfigs.GUN_HENRY);
    public static final RegistryObject<Item> GUN_HENRY_LINCOLN = sednaGun(
            LegacySednaGunConfigs.GUN_HENRY_LINCOLN);
    public static final RegistryObject<Item> GUN_HEAVY_REVOLVER = sednaGun(
            LegacySednaGunConfigs.GUN_HEAVY_REVOLVER);
    public static final RegistryObject<Item> GUN_HEAVY_REVOLVER_LILMAC = sednaGun(
            LegacySednaGunConfigs.GUN_HEAVY_REVOLVER_LILMAC);
    public static final RegistryObject<Item> GUN_HEAVY_REVOLVER_PROTEGE = sednaGun(
            LegacySednaGunConfigs.GUN_HEAVY_REVOLVER_PROTEGE);
    public static final RegistryObject<Item> GUN_HANGMAN = sednaGun(LegacySednaGunConfigs.GUN_HANGMAN);
    public static final RegistryObject<Item> GUN_GREASEGUN = sednaGun(LegacySednaGunConfigs.GUN_GREASEGUN);
    public static final RegistryObject<Item> GUN_LAG = sednaGun(LegacySednaGunConfigs.GUN_LAG);
    public static final RegistryObject<Item> GUN_UZI = sednaGun(LegacySednaGunConfigs.GUN_UZI);
    public static final RegistryObject<Item> GUN_UZI_AKIMBO = sednaGun(LegacySednaGunConfigs.GUN_UZI_AKIMBO);
    public static final RegistryObject<Item> GUN_AM180 = sednaGun(LegacySednaGunConfigs.GUN_AM180);
    public static final RegistryObject<Item> GUN_STAR_F = sednaGun(LegacySednaGunConfigs.GUN_STAR_F);
    public static final RegistryObject<Item> GUN_STAR_F_AKIMBO = sednaGun(
            LegacySednaGunConfigs.GUN_STAR_F_AKIMBO);
    public static final RegistryObject<Item> GUN_G3 = sednaGun(LegacySednaGunConfigs.GUN_G3);
    public static final RegistryObject<Item> GUN_G3_ZEBRA = sednaGun(LegacySednaGunConfigs.GUN_G3_ZEBRA);
    public static final RegistryObject<Item> GUN_STG77 = sednaGun(LegacySednaGunConfigs.GUN_STG77);
    public static final RegistryObject<Item> GUN_CARBINE = sednaGun(LegacySednaGunConfigs.GUN_CARBINE);
    public static final RegistryObject<Item> GUN_MINIGUN = sednaGun(LegacySednaGunConfigs.GUN_MINIGUN);
    public static final RegistryObject<Item> GUN_MINIGUN_LACUNAE = sednaGun(
            LegacySednaGunConfigs.GUN_MINIGUN_LACUNAE);
    public static final RegistryObject<Item> GUN_MINIGUN_DUAL = sednaGun(LegacySednaGunConfigs.GUN_MINIGUN_DUAL);
    public static final RegistryObject<Item> GUN_MAS36 = sednaGun(LegacySednaGunConfigs.GUN_MAS36);
    public static final RegistryObject<Item> GUN_FLAREGUN = sednaGun(LegacySednaGunConfigs.GUN_FLAREGUN);
    public static final RegistryObject<Item> GUN_CONGOLAKE = sednaGun(LegacySednaGunConfigs.GUN_CONGOLAKE);
    public static final RegistryObject<Item> GUN_MK108 = sednaGun(LegacySednaGunConfigs.GUN_MK108);
    public static final RegistryObject<Item> GUN_AMAT = sednaGun(LegacySednaGunConfigs.GUN_AMAT);
    public static final RegistryObject<Item> GUN_AMAT_SUBTLETY = sednaGun(
            LegacySednaGunConfigs.GUN_AMAT_SUBTLETY);
    public static final RegistryObject<Item> GUN_AMAT_PENANCE = sednaGun(
            LegacySednaGunConfigs.GUN_AMAT_PENANCE);
    public static final RegistryObject<Item> GUN_M2 = sednaGun(LegacySednaGunConfigs.GUN_M2);
    public static final RegistryObject<Item> GUN_BOLTER = sednaGun(LegacySednaGunConfigs.GUN_BOLTER);
    public static final RegistryObject<Item> GUN_ABERRATOR = sednaGun(LegacySednaGunConfigs.GUN_ABERRATOR);
    public static final RegistryObject<Item> GUN_ABERRATOR_EOTT = sednaGun(
            LegacySednaGunConfigs.GUN_ABERRATOR_EOTT);
    public static final RegistryObject<Item> GUN_PANZERSCHRECK = sednaGun(
            LegacySednaGunConfigs.GUN_PANZERSCHRECK);
    public static final RegistryObject<Item> GUN_STINGER = registerLegacy("gun_stinger",
            () -> new StingerGunItem(new Item.Properties(), LegacySednaGunConfigs.GUN_STINGER));
    public static final RegistryObject<Item> GUN_QUADRO = sednaGun(LegacySednaGunConfigs.GUN_QUADRO);
    public static final RegistryObject<Item> GUN_MISSILE_LAUNCHER = registerLegacy("gun_missile_launcher",
            () -> new MissileLauncherGunItem(new Item.Properties(), LegacySednaGunConfigs.GUN_MISSILE_LAUNCHER));
    public static final RegistryObject<Item> GUN_LASER_PISTOL = sednaGun(LegacySednaGunConfigs.GUN_LASER_PISTOL);
    public static final RegistryObject<Item> GUN_LASER_PISTOL_PEW_PEW = sednaGun(
            LegacySednaGunConfigs.GUN_LASER_PISTOL_PEW_PEW);
    public static final RegistryObject<Item> GUN_LASER_PISTOL_MORNING_GLORY = sednaGun(
            LegacySednaGunConfigs.GUN_LASER_PISTOL_MORNING_GLORY);
    public static final RegistryObject<Item> GUN_LASRIFLE = sednaGun(LegacySednaGunConfigs.GUN_LASRIFLE);
    public static final RegistryObject<Item> GUN_TAU = registerLegacy("gun_tau",
            () -> new TauCannonItem(new Item.Properties(), LegacySednaGunConfigs.GUN_TAU));
    public static final RegistryObject<Item> GUN_COILGUN = sednaGun(LegacySednaGunConfigs.GUN_COILGUN);
    public static final RegistryObject<Item> GUN_FLAMER = sednaGun(LegacySednaGunConfigs.GUN_FLAMER);
    public static final RegistryObject<Item> GUN_FLAMER_TOPAZ = sednaGun(LegacySednaGunConfigs.GUN_FLAMER_TOPAZ);
    public static final RegistryObject<Item> GUN_FLAMER_DAYBREAKER = sednaGun(
            LegacySednaGunConfigs.GUN_FLAMER_DAYBREAKER);
    public static final RegistryObject<Item> GUN_CHEMTHROWER = registerLegacy("gun_chemthrower",
            () -> new ChemthrowerItem(new Item.Properties(), LegacySednaGunConfigs.GUN_CHEMTHROWER));
    public static final RegistryObject<Item> GUN_TESLA_CANNON = sednaGun(LegacySednaGunConfigs.GUN_TESLA_CANNON);
    public static final RegistryObject<Item> GUN_FATMAN = sednaGun(LegacySednaGunConfigs.GUN_FATMAN);
    public static final RegistryObject<Item> GUN_FOLLY = registerLegacy("gun_folly",
            () -> new FollyGunItem(new Item.Properties(), LegacySednaGunConfigs.GUN_FOLLY));
    public static final RegistryObject<Item> GUN_FIREEXT = sednaGun(LegacySednaGunConfigs.GUN_FIREEXT);
    public static final RegistryObject<Item> GUN_CHARGE_THROWER = registerLegacy("gun_charge_thrower",
            () -> new ChargeThrowerItem(new Item.Properties(), LegacySednaGunConfigs.GUN_CHARGE_THROWER));
    public static final RegistryObject<Item> GUN_NI4NI = registerLegacy("gun_n_i_4_n_i",
            () -> new Ni4NiGunItem(new Item.Properties(), LegacySednaGunConfigs.GUN_NI4NI));
    public static final RegistryObject<Item> GUN_DRILL = registerLegacy("gun_drill",
            () -> new DrillGunItem(new Item.Properties(), LegacySednaGunConfigs.GUN_DRILL));

    public static final RegistryObject<Item> GEIGER_COUNTER = ITEMS.register("geiger_counter",
            () -> new GeigerCounterItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DOSIMETER = registerLegacy("dosimeter",
            () -> new DosimeterItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> OIL_DETECTOR = registerLegacy("oil_detector",
            () -> new OilDetectorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COLTAN_TOOL = registerLegacy("coltan_tool",
            () -> new ColtanCompassItem(new Item.Properties().stacksTo(1)));
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
    public static final RegistryObject<Item> FIVE_HTP = registerLegacy("five_htp",
            () -> new FiveHtpItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> XANAX = registerLegacy("xanax",
            () -> new XanaxItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> PILL_IODINE = registerLegacy("pill_iodine",
            () -> new IodinePillItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SIOX = registerLegacy("siox",
            () -> new SioxItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> PILL_HERBAL = registerLegacy("pill_herbal",
            () -> new HerbalPasteItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> FMN = registerLegacy("fmn",
            () -> new FmnItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> PLAN_C = registerLegacy("plan_c",
            () -> new PlanCItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> PILL_RED = registerLegacy("pill_red",
            () -> new RedPillItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SYRINGE_METAL_EMPTY = registerLegacy("syringe_metal_empty",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SYRINGE_METAL_STIMPAK = registerLegacy("syringe_metal_stimpak",
            () -> new LegacySyringeItem(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.STIMPAK));
    public static final RegistryObject<Item> SYRINGE_METAL_MEDX = registerLegacy("syringe_metal_medx",
            () -> new LegacySyringeItem(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.MEDX));
    public static final RegistryObject<Item> SYRINGE_METAL_PSYCHO = registerLegacy("syringe_metal_psycho",
            () -> new LegacySyringeItem(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.PSYCHO));
    public static final RegistryObject<Item> SYRINGE_METAL_SUPER = registerLegacy("syringe_metal_super",
            () -> new LegacySyringeItem(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.SUPER));
    public static final RegistryObject<Item> SYRINGE_TAINT = registerLegacy("syringe_taint",
            () -> new LegacySyringeItem(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.TAINT));
    public static final RegistryObject<Item> SYRINGE_MKUNICORN = registerLegacy("syringe_mkunicorn",
            () -> new LegacySyringeItem(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.MKUNICORN));
    public static final RegistryObject<Item> BOTTLE2_EMPTY = registerLegacy("bottle2_empty",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> COIN_TOKEN = registerLegacy("coin_token",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAP_NUKA = registerLegacy("cap_nuka",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAP_QUANTUM = registerLegacy("cap_quantum",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAP_SPARKLE = registerLegacy("cap_sparkle",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAP_RAD = registerLegacy("cap_rad",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAP_KORL = registerLegacy("cap_korl",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAP_FRITZ = registerLegacy("cap_fritz",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BOTTLE_NUKA = registerLegacy("bottle_nuka",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BOTTLE_CHERRY = registerLegacy("bottle_cherry",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BOTTLE_QUANTUM = registerLegacy("bottle_quantum",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAN_BEPIS = registerLegacy("can_bepis",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAN_LUNA = registerLegacy("can_luna",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAN_MUG = registerLegacy("can_mug",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAN_BREEN = registerLegacy("can_breen",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DEFINITELYFOOD = registerLegacy("definitelyfood",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationMod(0.5F)
                    .build())));
    public static final RegistryObject<Item> TWINKIE = registerLegacy("twinkie",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationMod(0.25F)
                    .build())));
    public static final RegistryObject<Item> CHOCOLATE = registerLegacy("chocolate",
            () -> new ChocolateItem(new Item.Properties()));
    public static final RegistryObject<Item> CANTEEN_VODKA = registerLegacy("canteen_vodka",
            () -> new VodkaCanteenItem(new Item.Properties().durability(3 * 60)));
    public static final RegistryObject<Item> GLYPHID_MEAT = registerLegacy("glyphid_meat",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationMod(0.5F)
                    .meat()
                    .build())));
    public static final RegistryObject<Item> GLYPHID_MEAT_GRILLED = registerLegacy("glyphid_meat_grilled",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8)
                    .saturationMod(0.75F)
                    .meat()
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 180, 1), 1.0F)
                    .build())));
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
    public static final RegistryObject<Item> JETPACK_TANK = registerLegacy("jetpack_tank",
            () -> new JetpackTankItem(new Item.Properties()));
    public static final RegistryObject<Item> ATTACHMENT_MASK = registerLegacy("attachment_mask",
            () -> new ArmorModGasMaskItem(new Item.Properties(), false));
    public static final RegistryObject<Item> ATTACHMENT_MASK_MONO = registerLegacy("attachment_mask_mono",
            () -> new ArmorModGasMaskItem(new Item.Properties(), true));
    public static final RegistryObject<Item> GOGGLES = objIronHeadArmor("goggles");
    public static final RegistryObject<Item> ASHGLASSES = objIronHeadArmor("ashglasses");
    public static final RegistryObject<Item> HAT = registerLegacy("nossy_hat",
            () -> new FabulousHatArmorItem(HbmArmorMaterials.ALLOY, new Item.Properties()));
    public static final RegistryObject<Item> NO9 = registerLegacy("no9",
            () -> new No9ArmorItem(new Item.Properties()));
    public static final RegistryObject<Item> GAS_MASK = gasMaskArmor("gas_mask", false);
    public static final RegistryObject<Item> GAS_MASK_M65 = gasMaskArmor("gas_mask_m65", false);
    public static final RegistryObject<Item> GAS_MASK_MONO = gasMaskArmor("gas_mask_mono", true);
    public static final RegistryObject<Item> GAS_MASK_OLDE = gasMaskArmor("gas_mask_olde", false);
    public static final RegistryObject<Item> MASK_OF_INFAMY = armor("mask_of_infamy",
            HbmArmorMaterials.MASK_OF_INFAMY, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> MASK_RAG = ragsHeadArmor("mask_rag");
    public static final RegistryObject<Item> MASK_PISS = ragsHeadArmor("mask_piss");
    public static final RegistryObject<Item> STEEL_HELMET = fsbArmor("steel_helmet", HbmArmorMaterials.STEEL,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> STEEL_CHESTPLATE = fsbArmor("steel_plate", HbmArmorMaterials.STEEL,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> STEEL_LEGS = fsbArmor("steel_legs", HbmArmorMaterials.STEEL,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> STEEL_BOOTS = fsbArmor("steel_boots", HbmArmorMaterials.STEEL,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> TITANIUM_HELMET = fsbArmor("titanium_helmet", HbmArmorMaterials.TITANIUM,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> TITANIUM_CHESTPLATE = fsbArmor("titanium_plate", HbmArmorMaterials.TITANIUM,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> TITANIUM_LEGS = fsbArmor("titanium_legs", HbmArmorMaterials.TITANIUM,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> TITANIUM_BOOTS = fsbArmor("titanium_boots", HbmArmorMaterials.TITANIUM,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ALLOY_HELMET = fsbArmor("alloy_helmet", HbmArmorMaterials.ALLOY,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ALLOY_PLATE = fsbArmor("alloy_plate", HbmArmorMaterials.ALLOY,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ALLOY_LEGS = fsbArmor("alloy_legs", HbmArmorMaterials.ALLOY,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ALLOY_BOOTS = fsbArmor("alloy_boots", HbmArmorMaterials.ALLOY,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> COBALT_HELMET = fsbArmor("cobalt_helmet", HbmArmorMaterials.COBALT,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> COBALT_PLATE = fsbArmor("cobalt_plate", HbmArmorMaterials.COBALT,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> COBALT_LEGS = fsbArmor("cobalt_legs", HbmArmorMaterials.COBALT,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> COBALT_BOOTS = fsbArmor("cobalt_boots", HbmArmorMaterials.COBALT,
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
    public static final RegistryObject<Item> CMB_HELMET = fsbArmor("cmb_helmet", HbmArmorMaterials.CMB,
            ArmorItem.Type.HELMET, cmbEffects());
    public static final RegistryObject<Item> CMB_PLATE = fsbArmor("cmb_plate", HbmArmorMaterials.CMB,
            ArmorItem.Type.CHESTPLATE, cmbEffects());
    public static final RegistryObject<Item> CMB_LEGS = fsbArmor("cmb_legs", HbmArmorMaterials.CMB,
            ArmorItem.Type.LEGGINGS, cmbEffects());
    public static final RegistryObject<Item> CMB_BOOTS = fsbArmor("cmb_boots", HbmArmorMaterials.CMB,
            ArmorItem.Type.BOOTS, cmbEffects());
    public static final RegistryObject<Item> PAA_PLATE = fsbArmor("paa_plate", HbmArmorMaterials.PAA,
            ArmorItem.Type.CHESTPLATE, paaEffects(), true, 0);
    public static final RegistryObject<Item> PAA_LEGS = fsbArmor("paa_legs", HbmArmorMaterials.PAA,
            ArmorItem.Type.LEGGINGS, paaEffects(), true, 0);
    public static final RegistryObject<Item> PAA_BOOTS = fsbArmor("paa_boots", HbmArmorMaterials.PAA,
            ArmorItem.Type.BOOTS, paaEffects(), true, 0);
    public static final RegistryObject<Item> JACKET = armor("jackt", HbmArmorMaterials.JACKET,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> JACKET2 = armor("jackt2", HbmArmorMaterials.JACKET2,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SECURITY_HELMET = fsbArmor("security_helmet", HbmArmorMaterials.SECURITY,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SECURITY_PLATE = fsbArmor("security_plate", HbmArmorMaterials.SECURITY,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SECURITY_LEGS = fsbArmor("security_legs", HbmArmorMaterials.SECURITY,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SECURITY_BOOTS = fsbArmor("security_boots", HbmArmorMaterials.SECURITY,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> STARMETAL_HELMET = fsbArmor("starmetal_helmet", HbmArmorMaterials.STARMETAL,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> STARMETAL_PLATE = fsbArmor("starmetal_plate", HbmArmorMaterials.STARMETAL,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> STARMETAL_LEGS = fsbArmor("starmetal_legs", HbmArmorMaterials.STARMETAL,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> STARMETAL_BOOTS = fsbArmor("starmetal_boots", HbmArmorMaterials.STARMETAL,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ROBES_HELMET = fsbArmor("robes_helmet", HbmArmorMaterials.ROBES,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ROBES_PLATE = fsbArmor("robes_plate", HbmArmorMaterials.ROBES,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ROBES_LEGS = fsbArmor("robes_legs", HbmArmorMaterials.ROBES,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ROBES_BOOTS = fsbArmor("robes_boots", HbmArmorMaterials.ROBES,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ZIRCONIUM_LEGS = fsbArmor("zirconium_legs", HbmArmorMaterials.ZIRCONIUM,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> DNT_HELMET = fsbArmor("dnt_helmet", HbmArmorMaterials.DNT,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> DNT_PLATE = fsbArmor("dnt_plate", HbmArmorMaterials.DNT,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> DNT_LEGS = fsbArmor("dnt_legs", HbmArmorMaterials.DNT,
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> DNT_BOOTS = fsbArmor("dnt_boots", HbmArmorMaterials.DNT,
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> HAZMAT_PAA_HELMET = fullHoodGasMaskArmor("hazmat_paa_helmet",
            HbmArmorMaterials.HAZMAT_PAA);
    public static final RegistryObject<Item> HAZMAT_PAA_PLATE = armor("hazmat_paa_plate",
            HbmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> HAZMAT_PAA_LEGS = armor("hazmat_paa_legs",
            HbmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> HAZMAT_PAA_BOOTS = armor("hazmat_paa_boots",
            HbmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> LIQUIDATOR_HELMET = liquidatorMaskArmor("liquidator_helmet");
    public static final RegistryObject<Item> LIQUIDATOR_PLATE = liquidatorArmor("liquidator_plate",
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> LIQUIDATOR_LEGS = liquidatorArmor("liquidator_legs",
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> LIQUIDATOR_BOOTS = liquidatorArmor("liquidator_boots",
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SCHRABIDIUM_HELMET = fsbArmor("schrabidium_helmet",
            HbmArmorMaterials.SCHRABIDIUM, ArmorItem.Type.HELMET, schrabidiumEffects());
    public static final RegistryObject<Item> SCHRABIDIUM_PLATE = fsbArmor("schrabidium_plate",
            HbmArmorMaterials.SCHRABIDIUM, ArmorItem.Type.CHESTPLATE, schrabidiumEffects());
    public static final RegistryObject<Item> SCHRABIDIUM_LEGS = fsbArmor("schrabidium_legs",
            HbmArmorMaterials.SCHRABIDIUM, ArmorItem.Type.LEGGINGS, schrabidiumEffects());
    public static final RegistryObject<Item> SCHRABIDIUM_BOOTS = fsbArmor("schrabidium_boots",
            HbmArmorMaterials.SCHRABIDIUM, ArmorItem.Type.BOOTS, schrabidiumEffects());
    public static final RegistryObject<Item> EUPHEMIUM_HELMET = euphemiumArmor("euphemium_helmet",
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> EUPHEMIUM_PLATE = euphemiumArmor("euphemium_plate",
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> EUPHEMIUM_LEGS = euphemiumArmor("euphemium_legs",
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> EUPHEMIUM_BOOTS = euphemiumArmor("euphemium_boots",
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> BISMUTH_HELMET = fsbArmor("bismuth_helmet", HbmArmorMaterials.BISMUTH,
            ArmorItem.Type.HELMET, bismuthEffects(), false, 3);
    public static final RegistryObject<Item> BISMUTH_PLATE = fsbArmor("bismuth_plate", HbmArmorMaterials.BISMUTH,
            ArmorItem.Type.CHESTPLATE, bismuthEffects(), false, 3);
    public static final RegistryObject<Item> BISMUTH_LEGS = fsbArmor("bismuth_legs", HbmArmorMaterials.BISMUTH,
            ArmorItem.Type.LEGGINGS, bismuthEffects(), false, 3);
    public static final RegistryObject<Item> BISMUTH_BOOTS = fsbArmor("bismuth_boots", HbmArmorMaterials.BISMUTH,
            ArmorItem.Type.BOOTS, bismuthEffects(), false, 3);
    public static final RegistryObject<Item> T51_HELMET = fsbPoweredArmor("t51_helmet", HbmArmorMaterials.T51,
            ArmorItem.Type.HELMET, t51Effects(), 1_000_000L, 10_000L, 1_000L, 5L, t51Traits());
    public static final RegistryObject<Item> T51_PLATE = fsbPoweredArmor("t51_plate", HbmArmorMaterials.T51,
            ArmorItem.Type.CHESTPLATE, t51Effects(), 1_000_000L, 10_000L, 1_000L, 5L, t51Traits());
    public static final RegistryObject<Item> T51_LEGS = fsbPoweredArmor("t51_legs", HbmArmorMaterials.T51,
            ArmorItem.Type.LEGGINGS, t51Effects(), 1_000_000L, 10_000L, 1_000L, 5L, t51Traits());
    public static final RegistryObject<Item> T51_BOOTS = fsbPoweredArmor("t51_boots", HbmArmorMaterials.T51,
            ArmorItem.Type.BOOTS, t51Effects(), 1_000_000L, 10_000L, 1_000L, 5L, t51Traits());
    public static final RegistryObject<Item> STEAMSUIT_HELMET = steamsuitArmor("steamsuit_helmet",
            HbmArmorMaterials.DESH_POWERED, ArmorItem.Type.HELMET, steamsuitEffects(), HbmFluids.STEAM,
            64_000, 500, 50, 1, hardLandingTraits());
    public static final RegistryObject<Item> STEAMSUIT_PLATE = steamsuitArmor("steamsuit_plate",
            HbmArmorMaterials.DESH_POWERED, ArmorItem.Type.CHESTPLATE, steamsuitEffects(), HbmFluids.STEAM,
            64_000, 500, 50, 1, hardLandingTraits());
    public static final RegistryObject<Item> STEAMSUIT_LEGS = steamsuitArmor("steamsuit_legs",
            HbmArmorMaterials.DESH_POWERED, ArmorItem.Type.LEGGINGS, steamsuitEffects(), HbmFluids.STEAM,
            64_000, 500, 50, 1, hardLandingTraits());
    public static final RegistryObject<Item> STEAMSUIT_BOOTS = steamsuitArmor("steamsuit_boots",
            HbmArmorMaterials.DESH_POWERED, ArmorItem.Type.BOOTS, steamsuitEffects(), HbmFluids.STEAM,
            64_000, 500, 50, 1, hardLandingTraits());
    public static final RegistryObject<Item> DIESELSUIT_HELMET = dieselSuitArmor("dieselsuit_helmet",
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> DIESELSUIT_PLATE = dieselSuitArmor("dieselsuit_plate",
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> DIESELSUIT_LEGS = dieselSuitArmor("dieselsuit_legs",
            ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> DIESELSUIT_BOOTS = dieselSuitArmor("dieselsuit_boots",
            ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> AJR_HELMET = fsbPoweredArmor("ajr_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJR_PLATE = fsbPoweredArmor("ajr_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJR_LEGS = fsbPoweredArmor("ajr_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJR_BOOTS = fsbPoweredArmor("ajr_boots", HbmArmorMaterials.AJR,
            ArmorItem.Type.BOOTS, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJRO_HELMET = fsbPoweredArmor("ajro_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJRO_PLATE = fsbPoweredArmor("ajro_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJRO_LEGS = fsbPoweredArmor("ajro_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJRO_BOOTS = fsbPoweredArmor("ajro_boots", HbmArmorMaterials.AJR,
            ArmorItem.Type.BOOTS, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> RPA_HELMET = fsbPoweredArmor("rpa_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> RPA_PLATE = fsbPoweredArmor("rpa_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> RPA_LEGS = fsbPoweredArmor("rpa_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> RPA_BOOTS = fsbPoweredArmor("rpa_boots", HbmArmorMaterials.AJR,
            ArmorItem.Type.BOOTS, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> NCRPA_HELMET = ncrpaArmor("ncrpa_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> NCRPA_PLATE = ncrpaArmor("ncrpa_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> NCRPA_LEGS = ncrpaArmor("ncrpa_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> NCRPA_BOOTS = ncrpaArmor("ncrpa_boots", HbmArmorMaterials.AJR,
            ArmorItem.Type.BOOTS, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> BJ_HELMET = bjArmor("bj_helmet", HbmArmorMaterials.BJ,
            ArmorItem.Type.HELMET, bjEffects(), 10_000_000L, 10_000L, 1_000L, 100L, bjTraits());
    public static final RegistryObject<Item> BJ_PLATE = bjArmor("bj_plate", HbmArmorMaterials.BJ,
            ArmorItem.Type.CHESTPLATE, bjEffects(), 10_000_000L, 10_000L, 1_000L, 100L, bjTraits());
    public static final RegistryObject<Item> BJ_PLATE_JETPACK = bjJetpackArmor("bj_plate_jetpack", HbmArmorMaterials.BJ,
            ArmorItem.Type.CHESTPLATE, bjEffects(), 10_000_000L, 10_000L, 1_000L, 100L, bjTraits());
    public static final RegistryObject<Item> BJ_LEGS = bjArmor("bj_legs", HbmArmorMaterials.BJ,
            ArmorItem.Type.LEGGINGS, bjEffects(), 10_000_000L, 10_000L, 1_000L, 100L, bjTraits());
    public static final RegistryObject<Item> BJ_BOOTS = bjArmor("bj_boots", HbmArmorMaterials.BJ,
            ArmorItem.Type.BOOTS, bjEffects(), 10_000_000L, 10_000L, 1_000L, 100L, bjTraits());
    public static final RegistryObject<Item> ENVSUIT_HELMET = envSuitArmor("envsuit_helmet", HbmArmorMaterials.ENV,
            ArmorItem.Type.HELMET, envEffects(), 100_000L, 1_000L, 250L, 0L);
    public static final RegistryObject<Item> ENVSUIT_PLATE = envSuitArmor("envsuit_plate", HbmArmorMaterials.ENV,
            ArmorItem.Type.CHESTPLATE, envEffects(), 100_000L, 1_000L, 250L, 0L);
    public static final RegistryObject<Item> ENVSUIT_LEGS = envSuitArmor("envsuit_legs", HbmArmorMaterials.ENV,
            ArmorItem.Type.LEGGINGS, envEffects(), 100_000L, 1_000L, 250L, 0L);
    public static final RegistryObject<Item> ENVSUIT_BOOTS = envSuitArmor("envsuit_boots", HbmArmorMaterials.ENV,
            ArmorItem.Type.BOOTS, envEffects(), 100_000L, 1_000L, 250L, 0L);
    public static final RegistryObject<Item> HEV_HELMET = fsbPoweredArmor("hev_helmet", HbmArmorMaterials.HEV,
            ArmorItem.Type.HELMET, envEffects(), 1_000_000L, 10_000L, 2_500L, 0L, hevTraits());
    public static final RegistryObject<Item> HEV_PLATE = fsbPoweredArmor("hev_plate", HbmArmorMaterials.HEV,
            ArmorItem.Type.CHESTPLATE, envEffects(), 1_000_000L, 10_000L, 2_500L, 0L, hevTraits());
    public static final RegistryObject<Item> HEV_LEGS = fsbPoweredArmor("hev_legs", HbmArmorMaterials.HEV,
            ArmorItem.Type.LEGGINGS, envEffects(), 1_000_000L, 10_000L, 2_500L, 0L, hevTraits());
    public static final RegistryObject<Item> HEV_BOOTS = fsbPoweredArmor("hev_boots", HbmArmorMaterials.HEV,
            ArmorItem.Type.BOOTS, envEffects(), 1_000_000L, 10_000L, 2_500L, 0L, hevTraits());
    public static final RegistryObject<Item> FAU_HELMET = fsbPoweredArmor("fau_helmet", HbmArmorMaterials.FAU,
            ArmorItem.Type.HELMET, fauEffects(), 10_000_000L, 10_000L, 2_500L, 0L, fauTraits());
    public static final RegistryObject<Item> FAU_PLATE = fsbPoweredArmor("fau_plate", HbmArmorMaterials.FAU,
            ArmorItem.Type.CHESTPLATE, fauEffects(), 10_000_000L, 10_000L, 2_500L, 0L, fauTraits());
    public static final RegistryObject<Item> FAU_LEGS = fsbPoweredArmor("fau_legs", HbmArmorMaterials.FAU,
            ArmorItem.Type.LEGGINGS, fauEffects(), 10_000_000L, 10_000L, 2_500L, 0L, fauTraits());
    public static final RegistryObject<Item> FAU_BOOTS = fsbPoweredArmor("fau_boots", HbmArmorMaterials.FAU,
            ArmorItem.Type.BOOTS, fauEffects(), 10_000_000L, 10_000L, 2_500L, 0L, fauTraits());
    public static final RegistryObject<Item> DNS_HELMET = dnsArmor("dns_helmet", HbmArmorMaterials.DNS,
            ArmorItem.Type.HELMET, dnsEffects(), 1_000_000_000L, 1_000_000L, 100_000L, 115L, dnsTraits());
    public static final RegistryObject<Item> DNS_PLATE = dnsArmor("dns_plate", HbmArmorMaterials.DNS,
            ArmorItem.Type.CHESTPLATE, dnsEffects(), 1_000_000_000L, 1_000_000L, 100_000L, 115L, dnsTraits());
    public static final RegistryObject<Item> DNS_LEGS = dnsArmor("dns_legs", HbmArmorMaterials.DNS,
            ArmorItem.Type.LEGGINGS, dnsEffects(), 1_000_000_000L, 1_000_000L, 100_000L, 115L, dnsTraits());
    public static final RegistryObject<Item> DNS_BOOTS = dnsArmor("dns_boots", HbmArmorMaterials.DNS,
            ArmorItem.Type.BOOTS, dnsEffects(), 1_000_000_000L, 1_000_000L, 100_000L, 115L, dnsTraits());
    public static final RegistryObject<Item> TAURUN_HELMET = fsbArmor("taurun_helmet", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.HELMET, taurunEffects(), false, 0, stepSizeTraits());
    public static final RegistryObject<Item> TAURUN_PLATE = fsbArmor("taurun_plate", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.CHESTPLATE, taurunEffects(), false, 0, stepSizeTraits());
    public static final RegistryObject<Item> TAURUN_LEGS = fsbArmor("taurun_legs", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.LEGGINGS, taurunEffects(), false, 0, stepSizeTraits());
    public static final RegistryObject<Item> TAURUN_BOOTS = fsbArmor("taurun_boots", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.BOOTS, taurunEffects(), false, 0, stepSizeTraits());
    public static final RegistryObject<Item> TRENCHMASTER_HELMET = trenchmasterArmor("trenchmaster_helmet",
            HbmArmorMaterials.TRENCHMASTER, ArmorItem.Type.HELMET, trenchmasterEffects(), false, 0, trenchTraits());
    public static final RegistryObject<Item> TRENCHMASTER_PLATE = trenchmasterArmor("trenchmaster_plate",
            HbmArmorMaterials.TRENCHMASTER, ArmorItem.Type.CHESTPLATE, trenchmasterEffects(), false, 0, trenchTraits());
    public static final RegistryObject<Item> TRENCHMASTER_LEGS = trenchmasterArmor("trenchmaster_legs",
            HbmArmorMaterials.TRENCHMASTER, ArmorItem.Type.LEGGINGS, trenchmasterEffects(), false, 0, trenchTraits());
    public static final RegistryObject<Item> TRENCHMASTER_BOOTS = trenchmasterArmor("trenchmaster_boots",
            HbmArmorMaterials.TRENCHMASTER, ArmorItem.Type.BOOTS, trenchmasterEffects(), false, 0, trenchTraits());
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
    public static final RegistryObject<Item> ITEM_SECRET_SELENIUM_STEEL = simpleItem("item_secret_selenium_steel");
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
    public static final RegistryObject<Item> WINGS_LIMP = registerLegacy("wings_limp",
            () -> new ArmorModItems.Wings(new Item.Properties(), false));
    public static final RegistryObject<Item> WINGS_MURK = registerLegacy("wings_murk",
            () -> new ArmorModItems.Wings(new Item.Properties(), true));
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
            () -> new ArmorModItems.Charm(new Item.Properties()));
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
    public static final RegistryObject<Item> SIREN_TRACK = registerLegacy("siren_track",
            () -> new SirenCassetteItem(new Item.Properties()));
    public static final RegistryObject<Item> SETTINGS_TOOL = registerLegacy("settings_tool",
            () -> new SettingsToolItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MIRROR_TOOL = registerLegacy("mirror_tool",
            () -> new MirrorToolItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SCREWDRIVER = registerLegacy("screwdriver",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(100), Toolable.ToolType.SCREWDRIVER));
    public static final RegistryObject<Item> HAND_DRILL = registerLegacy("hand_drill",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(100), Toolable.ToolType.HAND_DRILL));
    public static final RegistryObject<Item> WRENCH = registerLegacy("wrench",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(750), Toolable.ToolType.WRENCH));
    public static final RegistryObject<Item> BLOWTORCH = registerLegacy("blowtorch",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(4000), Toolable.ToolType.TORCH));
    public static final RegistryObject<Item> BOLTGUN = registerLegacy("boltgun",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(750), Toolable.ToolType.BOLT));
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
    public static final RegistryObject<Item> PISTON_SELENIUM = simpleStackOneItem("piston_selenium");
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
    public static final RegistryObject<Item> METEORITE_SWORD = meteoriteSword("meteorite_sword", 9.0F, "base");
    public static final RegistryObject<Item> METEORITE_SWORD_SEARED = meteoriteSword("meteorite_sword_seared", 10.0F, "seared");
    public static final RegistryObject<Item> METEORITE_SWORD_REFORGED = meteoriteSword("meteorite_sword_reforged", 12.5F, "reforged");
    public static final RegistryObject<Item> METEORITE_SWORD_HARDENED = meteoriteSword("meteorite_sword_hardened", 15.0F, "hardened");
    public static final RegistryObject<Item> METEORITE_SWORD_ALLOYED = meteoriteSword("meteorite_sword_alloyed", 17.5F, "alloyed");
    public static final RegistryObject<Item> METEORITE_SWORD_MACHINED = meteoriteSword("meteorite_sword_machined", 20.0F, "machined");
    public static final RegistryObject<Item> METEORITE_SWORD_TREATED = meteoriteSword("meteorite_sword_treated", 22.5F, "treated");
    public static final RegistryObject<Item> METEORITE_SWORD_ETCHED = meteoriteSword("meteorite_sword_etched", 25.0F, "etched");
    public static final RegistryObject<Item> METEORITE_SWORD_BRED = meteoriteSword("meteorite_sword_bred", 30.0F, "bred");
    public static final RegistryObject<Item> METEORITE_SWORD_IRRADIATED = meteoriteSword("meteorite_sword_irradiated", 35.0F, "irradiated");
    public static final RegistryObject<Item> METEORITE_SWORD_FUSED = meteoriteSword("meteorite_sword_fused", 50.0F, "fused");
    public static final RegistryObject<Item> METEORITE_SWORD_BALEFUL = meteoriteSword("meteorite_sword_baleful", 75.0F, "baleful");
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
                    .addAbility(WeaponAbilities.BEHEADER, 0)
                    .playGavelHitSound());
    public static final RegistryObject<Item> CHAINSAW = fueledAbilityAxe("chainsaw", 25.0F, -0.05D, HbmToolTiers.ELEC,
            5_000, 1, 250, item -> item.addAbility(ToolAreaAbilities.RECURSION, 2)
                    .addAbility(ToolHarvestAbilities.SILK, 0)
                    .addAbility(WeaponAbilities.CHAINSAW, 1)
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
    public static final RegistryObject<Item> BUCKET_MUD = registerLegacy("bucket_mud",
            () -> new BucketItem(ModFluids.MUD_FLUID, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final RegistryObject<Item> BUCKET_ACID = registerLegacy("bucket_acid",
            () -> new BucketItem(ModFluids.PEROXIDE.source(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final RegistryObject<Item> BUCKET_TOXIC = registerLegacy("bucket_toxic",
            () -> new BucketItem(ModFluids.TOXIC_FLUID, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final RegistryObject<Item> BUCKET_SCHRABIDIC_ACID = registerLegacy("bucket_schrabidic_acid",
            () -> new BucketItem(ModFluids.SCHRABIDIC.source(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final RegistryObject<Item> BUCKET_SULFURIC_ACID = registerLegacy("bucket_sulfuric_acid",
            () -> new BucketItem(ModFluids.SULFURIC_ACID.source(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    static {
        ModFluids.mudProperties().bucket(BUCKET_MUD);
        ModFluids.PEROXIDE.properties().bucket(BUCKET_ACID);
        ModFluids.toxicProperties().bucket(BUCKET_TOXIC);
        ModFluids.SCHRABIDIC.properties().bucket(BUCKET_SCHRABIDIC_ACID);
        ModFluids.SULFURIC_ACID.properties().bucket(BUCKET_SULFURIC_ACID);
    }
    public static final RegistryObject<Item> BIOMASS = registerLegacy("biomass",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BIOMASS_COMPRESSED = registerLegacy("biomass_compressed",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BURNT_BARK = registerLegacy("burnt_bark",
            () -> new LegacyLoreItem(new Item.Properties()));
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
    public static final RegistryObject<Item> SIPHON = registerLegacy("siphon",
            () -> new FluidSiphonItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PIPETTE = registerLegacy("pipette",
            () -> new FluidPipetteItem(new Item.Properties().stacksTo(1), FluidPipetteItem.Kind.NORMAL));
    public static final RegistryObject<Item> PIPETTE_BORON = registerLegacy("pipette_boron",
            () -> new FluidPipetteItem(new Item.Properties().stacksTo(1), FluidPipetteItem.Kind.BORON));
    public static final RegistryObject<Item> PIPETTE_LABORATORY = registerLegacy("pipette_laboratory",
            () -> new FluidPipetteItem(new Item.Properties().stacksTo(1), FluidPipetteItem.Kind.LABORATORY));
    public static final RegistryObject<Item> FLUID_DUCT = registerLegacy("fluid_duct",
            () -> new FluidPipeBlockItem(ModBlocks.FLUID_DUCT_NEO.get(), new Item.Properties()));
    public static final RegistryObject<Item> RTTY_PAGER = registerLegacy("rtty_pager",
            () -> new RTTYPagerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DETONATOR = registerLegacy("detonator",
            () -> new DetonatorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LINKER = registerLegacy("linker",
            () -> new TeleLinkItem(new Item.Properties().stacksTo(1)));
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
    public static final RegistryObject<Item> PARTICLE_EMPTY = simpleItem("particle_empty");
    public static final RegistryObject<Item> PARTICLE_HYDROGEN = particleCapsule("particle_hydrogen");
    public static final RegistryObject<Item> PARTICLE_COPPER = particleCapsule("particle_copper");
    public static final RegistryObject<Item> PARTICLE_LEAD = particleCapsule("particle_lead");
    public static final RegistryObject<Item> PARTICLE_AMAT = particleCapsule("particle_amat");
    public static final RegistryObject<Item> PARTICLE_ASCHRAB = particleCapsule("particle_aschrab");
    public static final RegistryObject<Item> PARTICLE_HIGGS = registerLegacy("particle_higgs",
            () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final RegistryObject<Item> PARTICLE_DARK = registerLegacy("particle_dark",
            () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final RegistryObject<Item> PARTICLE_TACHYON = particleCapsule("particle_tachyon");
    public static final RegistryObject<Item> PARTICLE_STRANGE = particleCapsule("particle_strange");
    public static final RegistryObject<Item> PARTICLE_SPARKTICLE = registerLegacy("particle_sparkticle",
            () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final RegistryObject<Item> PARTICLE_DIGAMMA = registerLegacy("particle_digamma",
            () -> new DigammaParticleItem(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get()), 60));
    public static final RegistryObject<Item> CELL_EMPTY = simpleItem("cell_empty");
    public static final RegistryObject<Item> CELL_ANTIMATTER = registerLegacy("cell_antimatter",
            () -> new AntimatterCellItem(new Item.Properties().craftRemainder(CELL_EMPTY.get()),
                    "item.hbm_ntm_rebirth.cell_antimatter"));
    public static final RegistryObject<Item> CELL_ANTI_SCHRABIDIUM = registerLegacy("cell_anti_schrabidium",
            () -> new AntimatterCellItem(new Item.Properties().craftRemainder(CELL_EMPTY.get()),
                    "item.hbm_ntm_rebirth.cell_anti_schrabidium"));
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
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_RESTORED = registerLegacy("holotape_image_restored",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HOLOTAPE_DAMAGED = registerLegacy("holotape_damaged",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLUID_ICON = registerLegacy("fluid_icon",
            () -> new FluidIconItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WATCH = registerLegacy("watch",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RANGEFINDER = registerLegacy("rangefinder",
            () -> new RangefinderItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SURVEY_SCANNER = registerLegacy("survey_scanner",
            () -> new SurveyScannerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ORE_DENSITY_SCANNER = registerLegacy("ore_density_scanner",
            () -> new OreDensityScannerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> REACTOR_SENSOR = registerLegacy("reactor_sensor",
            () -> new ReactorSensorItem(new Item.Properties().stacksTo(1)));

    public static final List<RegistryObject<Item>> HIDDEN_RECIPE_ITEMS = Stream.concat(
            Stream.concat(Stream.concat(STAMP_BOOK_ITEMS.stream(), PAGE_OF_ITEMS.stream()), WEAPON_MOD_TEST_ITEMS.stream()),
            Stream.of(TEMPLATE_FOLDER, HOLOTAPE_IMAGE_RESTORED, HOLOTAPE_DAMAGED, FLUID_ICON, WATCH, BURNT_BARK)).toList();

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
    public static final RegistryObject<Item> DESIGNATOR_ARTY_RANGE = registerLegacy("designator_arty_range",
            () -> new ArtilleryDesignatorItem(new Item.Properties().stacksTo(1)));

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
    public static final RegistryObject<Item> LAUNCH_CODE_PIECE = simpleStackOneItem("launch_code_piece");
    public static final RegistryObject<Item> LAUNCH_CODE = simpleStackOneItem("launch_code");
    public static final RegistryObject<Item> LAUNCH_KEY = simpleStackOneItem("launch_key");
    public static final RegistryObject<Item> KEY = registerLegacy("key",
            () -> new KeyPinItem(new Item.Properties().stacksTo(1), true));
    public static final RegistryObject<Item> KEY_KIT = registerLegacy("key_kit",
            () -> new LegacyLoreItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> KEY_FAKE = registerLegacy("key_fake",
            () -> new KeyPinItem(new Item.Properties().stacksTo(1), false));
    public static final RegistryObject<Item> PIN = registerLegacy("pin",
            () -> new LegacyLoreItem(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> PADLOCK_RUSTY = padlock("padlock_rusty", 1.0D);
    public static final RegistryObject<Item> PADLOCK = padlock("padlock", 0.1D);
    public static final RegistryObject<Item> PADLOCK_REINFORCED = padlock("padlock_reinforced", 0.02D);
    public static final RegistryObject<Item> PADLOCK_UNBREAKABLE = padlock("padlock_unbreakable", 0.0D);
    public static final RegistryObject<Item> MISSILE_SOYUZ = registerLegacy("missile_soyuz",
            () -> new SoyuzRocketItem(new Item.Properties()));
    public static final RegistryObject<Item> MISSILE_SOYUZ_LANDER = simpleStackOneItem("missile_soyuz_lander");

    public static final RegistryObject<Item> MISSILE_ASSEMBLY = missile("missile_assembly",
            MissileItem.FormFactor.OTHER, MissileItem.Tier.TIER1, false);
    public static final RegistryObject<Item> MISSILE_GENERIC = missile("missile_generic",
            MissileItem.FormFactor.V2, MissileItem.Tier.TIER1);
    public static final RegistryObject<Item> MISSILE_ANTI_BALLISTIC = missile("missile_anti_ballistic",
            MissileItem.FormFactor.ABM, MissileItem.Tier.TIER1, MissileItem.Fuel.SOLID);
    public static final RegistryObject<Item> MISSILE_INCENDIARY = missile("missile_incendiary",
            MissileItem.FormFactor.V2, MissileItem.Tier.TIER1);
    public static final RegistryObject<Item> MISSILE_CLUSTER = missile("missile_cluster",
            MissileItem.FormFactor.V2, MissileItem.Tier.TIER1);
    public static final RegistryObject<Item> MISSILE_BUSTER = missile("missile_buster",
            MissileItem.FormFactor.V2, MissileItem.Tier.TIER1);
    public static final RegistryObject<Item> MISSILE_DECOY = missile("missile_decoy",
            MissileItem.FormFactor.V2, MissileItem.Tier.TIER4);
    public static final RegistryObject<Item> MISSILE_STRONG = missile("missile_strong",
            MissileItem.FormFactor.STRONG, MissileItem.Tier.TIER2);
    public static final RegistryObject<Item> MISSILE_INCENDIARY_STRONG = missile("missile_incendiary_strong",
            MissileItem.FormFactor.STRONG, MissileItem.Tier.TIER2);
    public static final RegistryObject<Item> MISSILE_CLUSTER_STRONG = missile("missile_cluster_strong",
            MissileItem.FormFactor.STRONG, MissileItem.Tier.TIER2);
    public static final RegistryObject<Item> MISSILE_BUSTER_STRONG = missile("missile_buster_strong",
            MissileItem.FormFactor.STRONG, MissileItem.Tier.TIER2);
    public static final RegistryObject<Item> MISSILE_EMP_STRONG = missile("missile_emp_strong",
            MissileItem.FormFactor.STRONG, MissileItem.Tier.TIER2);
    public static final RegistryObject<Item> MISSILE_BURST = missile("missile_burst",
            MissileItem.FormFactor.HUGE, MissileItem.Tier.TIER3);
    public static final RegistryObject<Item> MISSILE_INFERNO = missile("missile_inferno",
            MissileItem.FormFactor.HUGE, MissileItem.Tier.TIER3);
    public static final RegistryObject<Item> MISSILE_RAIN = missile("missile_rain",
            MissileItem.FormFactor.HUGE, MissileItem.Tier.TIER3);
    public static final RegistryObject<Item> MISSILE_DRILL = missile("missile_drill",
            MissileItem.FormFactor.HUGE, MissileItem.Tier.TIER3);
    public static final RegistryObject<Item> MISSILE_NUCLEAR = missile("missile_nuclear",
            MissileItem.FormFactor.ATLAS, MissileItem.Tier.TIER4);
    public static final RegistryObject<Item> MISSILE_NUCLEAR_CLUSTER = missile("missile_nuclear_cluster",
            MissileItem.FormFactor.ATLAS, MissileItem.Tier.TIER4);
    public static final RegistryObject<Item> MISSILE_DOOMSDAY = missile("missile_doomsday",
            MissileItem.FormFactor.ATLAS, MissileItem.Tier.TIER4);
    public static final RegistryObject<Item> MISSILE_DOOMSDAY_RUSTED = missile("missile_doomsday_rusted",
            MissileItem.FormFactor.ATLAS, MissileItem.Tier.TIER4, false);
    public static final RegistryObject<Item> MISSILE_VOLCANO = missile("missile_volcano",
            MissileItem.FormFactor.ATLAS, MissileItem.Tier.TIER4);
    public static final RegistryObject<Item> MISSILE_MICRO = missile("missile_micro",
            MissileItem.FormFactor.MICRO, MissileItem.Tier.TIER0, MissileItem.Fuel.SOLID);
    public static final RegistryObject<Item> MISSILE_TAINT = missile("missile_taint",
            MissileItem.FormFactor.MICRO, MissileItem.Tier.TIER0, MissileItem.Fuel.SOLID);
    public static final RegistryObject<Item> MISSILE_BHOLE = missile("missile_bhole",
            MissileItem.FormFactor.MICRO, MissileItem.Tier.TIER0, MissileItem.Fuel.SOLID);
    public static final RegistryObject<Item> MISSILE_SCHRABIDIUM = missile("missile_schrabidium",
            MissileItem.FormFactor.MICRO, MissileItem.Tier.TIER0, MissileItem.Fuel.SOLID);
    public static final RegistryObject<Item> MISSILE_EMP = missile("missile_emp",
            MissileItem.FormFactor.MICRO, MissileItem.Tier.TIER0, MissileItem.Fuel.SOLID);
    public static final RegistryObject<Item> MISSILE_SHUTTLE = missile("missile_shuttle",
            MissileItem.FormFactor.OTHER, MissileItem.Tier.TIER3, MissileItem.Fuel.JETFUEL_LOXY);
    public static final RegistryObject<Item> MISSILE_STEALTH = missile("missile_stealth",
            MissileItem.FormFactor.V2, MissileItem.Tier.TIER1);
    public static final RegistryObject<Item> MISSILE_TEST = missile("missile_test",
            MissileItem.FormFactor.MICRO, MissileItem.Tier.TIER0, MissileItem.Fuel.SOLID);
    public static final RegistryObject<Item> MISSILE_CUSTOM = registerLegacy("missile_custom",
            () -> new CustomMissileItem(new Item.Properties().stacksTo(1)));

    public static final List<RegistryObject<Item>> MISSILE_PART_ITEMS = Stream.of(
            missileParts(MissilePartItem.PartType.CHIP,
                    "mp_chip_1", "mp_chip_2", "mp_chip_3", "mp_chip_4", "mp_chip_5").stream(),
            missileParts(MissilePartItem.PartType.THRUSTER,
                    "mp_thruster_10_kerosene", "mp_thruster_10_solid", "mp_thruster_10_xenon",
                    "mp_thruster_15_kerosene", "mp_thruster_15_kerosene_dual",
                    "mp_thruster_15_kerosene_triple", "mp_thruster_15_solid",
                    "mp_thruster_15_solid_hexdecuple", "mp_thruster_15_hydrogen",
                    "mp_thruster_15_hydrogen_dual", "mp_thruster_15_balefire_short",
                    "mp_thruster_15_balefire", "mp_thruster_15_balefire_large",
                    "mp_thruster_15_balefire_large_rad", "mp_thruster_20_kerosene",
                    "mp_thruster_20_kerosene_dual", "mp_thruster_20_kerosene_triple",
                    "mp_thruster_20_solid", "mp_thruster_20_solid_multi",
                    "mp_thruster_20_solid_multier").stream(),
            missileParts(MissilePartItem.PartType.FINS,
                    "mp_stability_10_flat", "mp_stability_10_cruise", "mp_stability_10_space",
                    "mp_stability_15_flat", "mp_stability_15_thin", "mp_stability_15_soyuz",
                    "mp_stability_20_flat").stream(),
            missileParts(MissilePartItem.PartType.FUSELAGE,
                    "mp_fuselage_10_kerosene", "mp_fuselage_10_kerosene_camo",
                    "mp_fuselage_10_kerosene_desert", "mp_fuselage_10_kerosene_sky",
                    "mp_fuselage_10_kerosene_insulation", "mp_fuselage_10_kerosene_flames",
                    "mp_fuselage_10_kerosene_sleek", "mp_fuselage_10_kerosene_metal",
                    "mp_fuselage_10_kerosene_taint", "mp_fuselage_10_solid",
                    "mp_fuselage_10_solid_flames", "mp_fuselage_10_solid_insulation",
                    "mp_fuselage_10_solid_sleek", "mp_fuselage_10_solid_soviet_glory",
                    "mp_fuselage_10_solid_cathedral", "mp_fuselage_10_solid_moonlit",
                    "mp_fuselage_10_solid_battery", "mp_fuselage_10_solid_duracell",
                    "mp_fuselage_10_xenon", "mp_fuselage_10_xenon_bhole",
                    "mp_fuselage_10_long_kerosene", "mp_fuselage_10_long_kerosene_camo",
                    "mp_fuselage_10_long_kerosene_desert", "mp_fuselage_10_long_kerosene_sky",
                    "mp_fuselage_10_long_kerosene_flames", "mp_fuselage_10_long_kerosene_insulation",
                    "mp_fuselage_10_long_kerosene_sleek", "mp_fuselage_10_long_kerosene_metal",
                    "mp_fuselage_10_long_kerosene_dash", "mp_fuselage_10_long_kerosene_taint",
                    "mp_fuselage_10_long_kerosene_vap", "mp_fuselage_10_long_solid",
                    "mp_fuselage_10_long_solid_flames", "mp_fuselage_10_long_solid_insulation",
                    "mp_fuselage_10_long_solid_sleek", "mp_fuselage_10_long_solid_soviet_glory",
                    "mp_fuselage_10_long_solid_bullet", "mp_fuselage_10_long_solid_silvermoonlight",
                    "mp_fuselage_10_15_kerosene", "mp_fuselage_10_15_solid",
                    "mp_fuselage_10_15_hydrogen", "mp_fuselage_10_15_balefire",
                    "mp_fuselage_15_kerosene", "mp_fuselage_15_kerosene_camo",
                    "mp_fuselage_15_kerosene_desert", "mp_fuselage_15_kerosene_sky",
                    "mp_fuselage_15_kerosene_insulation", "mp_fuselage_15_kerosene_metal",
                    "mp_fuselage_15_kerosene_decorated", "mp_fuselage_15_kerosene_steampunk",
                    "mp_fuselage_15_kerosene_polite", "mp_fuselage_15_kerosene_blackjack",
                    "mp_fuselage_15_kerosene_lambda", "mp_fuselage_15_kerosene_minuteman",
                    "mp_fuselage_15_kerosene_pip", "mp_fuselage_15_kerosene_taint",
                    "mp_fuselage_15_kerosene_yuck", "mp_fuselage_15_solid",
                    "mp_fuselage_15_solid_insulation", "mp_fuselage_15_solid_desh",
                    "mp_fuselage_15_solid_soviet_glory", "mp_fuselage_15_solid_soviet_stank",
                    "mp_fuselage_15_solid_faust", "mp_fuselage_15_solid_silvermoonlight",
                    "mp_fuselage_15_solid_snowy", "mp_fuselage_15_solid_panorama",
                    "mp_fuselage_15_solid_roses", "mp_fuselage_15_solid_mimi",
                    "mp_fuselage_15_hydrogen", "mp_fuselage_15_hydrogen_cathedral",
                    "mp_fuselage_15_balefire", "mp_fuselage_15_20_kerosene",
                    "mp_fuselage_15_20_kerosene_magnusson", "mp_fuselage_15_20_solid").stream(),
            missileParts(MissilePartItem.PartType.WARHEAD,
                    "mp_warhead_10_he", "mp_warhead_10_incendiary", "mp_warhead_10_buster",
                    "mp_warhead_10_nuclear", "mp_warhead_10_nuclear_large", "mp_warhead_10_taint",
                    "mp_warhead_10_cloud", "mp_warhead_15_he", "mp_warhead_15_incendiary",
                    "mp_warhead_15_nuclear", "mp_warhead_15_nuclear_shark",
                    "mp_warhead_15_nuclear_mimi", "mp_warhead_15_boxcar", "mp_warhead_15_n2",
                    "mp_warhead_15_balefire", "mp_warhead_15_turbine").stream()
    ).flatMap(stream -> stream).toList();

    public static final List<RegistryObject<Item>> MISSILE_TAB_ITEMS = Stream.concat(Stream.of(
            MISSILE_ASSEMBLY,
            MISSILE_GENERIC,
            MISSILE_ANTI_BALLISTIC,
            MISSILE_INCENDIARY,
            MISSILE_CLUSTER,
            MISSILE_BUSTER,
            MISSILE_DECOY,
            MISSILE_STRONG,
            MISSILE_INCENDIARY_STRONG,
            MISSILE_CLUSTER_STRONG,
            MISSILE_BUSTER_STRONG,
            MISSILE_EMP_STRONG,
            MISSILE_BURST,
            MISSILE_INFERNO,
            MISSILE_RAIN,
            MISSILE_DRILL,
            MISSILE_NUCLEAR,
            MISSILE_NUCLEAR_CLUSTER,
            MISSILE_DOOMSDAY,
            MISSILE_DOOMSDAY_RUSTED,
            MISSILE_VOLCANO,
            MISSILE_MICRO,
            MISSILE_TAINT,
            MISSILE_BHOLE,
            MISSILE_SCHRABIDIUM,
            MISSILE_EMP,
            MISSILE_SHUTTLE,
            MISSILE_STEALTH,
            MISSILE_TEST,
            MISSILE_CUSTOM
    ), MISSILE_PART_ITEMS.stream()).toList();

    public static final List<RegistryObject<Item>> SATELLITE_PART_ITEMS = List.of(
            SAT_HEAD_MAPPER,
            SAT_HEAD_SCANNER,
            SAT_HEAD_RADAR,
            SAT_HEAD_LASER,
            SAT_HEAD_RESONATOR
    );

    public static final List<RegistryObject<Item>> SATELLITE_TAB_ITEMS = List.of(
            RANGEFINDER,
            DESIGNATOR,
            DESIGNATOR_RANGE,
            DESIGNATOR_MANUAL,
            DESIGNATOR_ARTY_RANGE,
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

    public static final RegistryObject<Item> INGOT_C4 = registerLegacy("ingot_c4",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationMod(5.0F)
                    .meat()
                    .build())));
    public static final RegistryObject<Item> INGOT_SEMTEX = registerLegacy("ingot_semtex",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationMod(5.0F)
                    .meat()
                    .build())));
    public static final RegistryObject<Item> STICK_DYNAMITE = registerLegacy("stick_dynamite",
            () -> new DynamiteStickItem(new Item.Properties()));
    public static final RegistryObject<Item> BOTTLE_MERCURY = registerLegacy("bottle_mercury",
            () -> new Item(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE)));
    public static final RegistryObject<Item> CAN_EMPTY = registerLegacy("can_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLOWING_STEW = registerLegacy("glowing_stew",
            () -> new BowlFoodItem(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationMod(0.6F)
                    .build())));
    public static final RegistryObject<Item> MARSHMALLOW = registerLegacy("marshmallow",
            () -> new MarshmallowItem(new Item.Properties()));
    public static final RegistryObject<Item> BOOK_GUIDE = registerLegacy("book_guide",
            () -> new GuideBookItem(new Item.Properties()));
    public static final RegistryObject<Item> RAG = registerLegacy("rag",
            () -> new HbmRagItem(new Item.Properties()));
    public static final RegistryObject<Item> LASER_CRYSTAL_CO2 = simpleStackOneItem("laser_crystal_co2");
    public static final RegistryObject<Item> LASER_CRYSTAL_BISMUTH = simpleStackOneItem("laser_crystal_bismuth");
    public static final RegistryObject<Item> LASER_CRYSTAL_CMB = simpleStackOneItem("laser_crystal_cmb");
    public static final RegistryObject<Item> LASER_CRYSTAL_DNT = simpleStackOneItem("laser_crystal_dnt");
    public static final RegistryObject<Item> LASER_CRYSTAL_DIGAMMA = simpleStackOneItem("laser_crystal_digamma");
    public static final RegistryObject<Item> ICF_PELLET_EMPTY = simpleStackOneItem("icf_pellet_empty");
    public static final RegistryObject<Item> ICF_PELLET = registerLegacy("icf_pellet",
            () -> new ICFPelletItem(new Item.Properties()));
    public static final RegistryObject<Item> ICF_PELLET_DEPLETED = simpleStackOneItem("icf_pellet_depleted");
    public static final RegistryObject<Item> PARTICLE_MUON = registerLegacy("particle_muon",
            () -> new Item(new Item.Properties().stacksTo(1).craftRemainder(PARTICLE_EMPTY.get())));

    public static final List<RegistryObject<Item>> EXTRA_PARTS_TAB_ITEMS = Stream.concat(Stream.concat(Stream.concat(Stream.concat(CIRCUIT_ITEMS.stream(), EXPENSIVE_MODE_ITEMS.stream()), ORE_BYPRODUCT_ITEMS.stream()), simpleParts(
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
            "ingot_fiberglass",
            "ingot_euphemium",
            "ingot_mercury",
            "ingot_gh336",
            "ingot_starmetal",
            "ingot_chainsteel",
            "ingot_dineutronium",
            "ingot_graphite",
            "ingot_firebrick",
            "sulfur",
            "niter",
            "nitra",
            "nitra_small",
            "cordite",
            "ballistite",
            "ball_dynamite",
            "ball_tnt",
            "ball_tatb",
            "pellet_cluster",
            "pellet_buckshot",
            "ingot_uranium_fuel",
            "ingot_plutonium_fuel",
            "ingot_neptunium_fuel",
            "ingot_mox_fuel",
            "ingot_americium_fuel",
            "ingot_schrabidium_fuel",
            "ingot_thorium_fuel",
            "ingot_hes",
            "ingot_les",
            "ingot_schrabidate",
            "ingot_schraranium",
            "nugget_uranium_fuel",
            "nugget_thorium_fuel",
            "nugget_plutonium_fuel",
            "nugget_neptunium_fuel",
            "nugget_mox_fuel",
            "nugget_americium_fuel",
            "nugget_schrabidium_fuel",
            "nugget_hes",
            "nugget_les",
            "nugget_schrabidium",
            "ingot_tcalloy",
            "ingot_cdalloy",
            "ingot_bismuth_bronze",
            "ingot_arsenic_bronze",
            "ingot_bscco",
            "ingot_bismuth",
            "nugget_bismuth",
            "nugget_lead",
            "nugget_euphemium",
            "nugget_gh336",
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
            "plate_paa",
            "plate_euphemium",
            "lithium",
            "powder_lithium",
            "powder_lithium_tiny",
            "powder_ice",
            "powder_neodymium_tiny",
            "powder_boron_tiny",
            "powder_niobium_tiny",
            "powder_cerium_tiny",
            "powder_lanthanium_tiny",
            "powder_paleogenite_tiny",
            "powder_beryllium",
            "powder_aluminium",
            "powder_cobalt",
            "powder_cobalt_tiny",
            "powder_neptunium",
            "powder_sodium",
            "powder_schrabidium",
            "powder_gold",
            "powder_niobium",
            "powder_astatine",
            "powder_asbestos",
            "powder_molysite",
            "powder_ferrouranium",
            "powder_iodine",
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
            "powder_actinium",
            "powder_actinium_tiny",
            "powder_quartz",
            "powder_lapis",
            "powder_diamond",
            "powder_emerald",
            "powder_meteorite",
            "powder_meteorite_tiny",
            "powder_steel_tiny",
            "powder_poison",
            "powder_red_copper",
            "powder_magnetized_tungsten",
            "powder_magic",
            "powder_polymer",
            "powder_bakelite",
            "powder_spark_mix",
            "powder_desh_mix",
            "powder_nitan_mix",
            "powder_chlorophyte",
            "powder_fire",
            "gem_alexandrite",
            "gem_sodalite",
            "ball_resin",
            "ingot_zirconium",
            "ingot_phosphorus",
            "ingot_magnetized_tungsten",
            "assembly_nuke",
            "neutron_reflector",
            "plate_mixed",
            "plate_bismuth",
            "plate_cast_iron",
            "plate_cast_steel",
            "plate_cast_lead",
            "plate_cast_copper",
            "plate_cast_titanium",
            "plate_cast_aluminium",
            "plate_cast_dura_steel",
            "plate_cast_bismuth_bronze",
            "plate_cast_arsenic_bronze",
            "plate_cast_combine_steel",
            "plate_cast_ferrouranium",
            "plate_cast_tungsten",
            "plate_cast_starmetal",
            "plate_welded_steel",
            "plate_welded_copper",
            "plate_welded_zirconium",
            "plate_welded_tcalloy",
            "plate_welded_cdalloy",
            "plate_welded_osmiridium",
            "wire_gold",
            "wire_fine_aluminium",
            "wire_fine_copper",
            "wire_fine_mingrade",
            "wire_fine_tungsten",
            "wire_fine_schrabidium",
            "wire_fine_magnetized_tungsten",
            "wire_fine_lead",
            "wire_fine_zirconium",
            "wire_dense_gold",
            "wire_dense_copper",
            "wire_dense_titanium",
            "wire_dense_niobium",
            "wire_dense_mingrade",
            "wire_dense_bscco",
            "wire_dense_neodymium",
            "wire_dense_dineutronium",
            "wire_dense_starmetal",
            "fins_flat",
            "fins_small_steel",
            "fins_big_steel",
            "fins_tri_steel",
            "fins_quad_titanium",
            "bolt_lead",
            "bolt_steel",
            "bolt_tungsten",
            "bolt_dura_steel",
            "shell_aluminium",
            "shell_copper",
            "shell_steel",
            "shell_titanium",
            "shell_weaponsteel",
            "shell_saturnite",
            "mechanism_gunmetal",
            "mechanism_weaponsteel",
            "mechanism_saturnite",
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
            "warhead_generic_small",
            "warhead_generic_medium",
            "warhead_generic_large",
            "warhead_incendiary_small",
            "warhead_incendiary_medium",
            "warhead_incendiary_large",
            "warhead_cluster_small",
            "warhead_cluster_medium",
            "warhead_cluster_large",
            "warhead_buster_small",
            "warhead_buster_medium",
            "warhead_buster_large",
            "warhead_nuclear",
            "warhead_mirv",
            "warhead_volcano",
            "seg_10",
            "seg_15",
            "seg_20",
            "crystal_diamond",
            "crystal_rare",
            "safety_fuse",
            "hazmat_cloth",
            "hazmat_cloth_red",
            "hazmat_cloth_grey",
            "asbestos_cloth",
            "rag_damp",
            "rag_piss",
            "filter_coal",
            "motor_desh",
            "motor_bismuth",
            "centrifuge_element",
            "reactor_core",
            "thermo_element",
            "rtg_unit",
            "magnetron",
            "crt_display",
            "sphere_steel",
            "pedestal_steel",
            "blade_titanium",
            "turbine_titanium",
            "blade_tungsten",
            "turbine_tungsten",
            "flywheel_beryllium",
            "entanglement_kit",
            "dysfunctional_reactor",
            "coil_copper_torus",
            "coil_gold_torus",
            "coil_magnetized_tungsten",
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
            "billet_schrabidium",
            "billet_uranium_fuel",
            "billet_thorium_fuel",
            "billet_plutonium_fuel",
            "billet_neptunium_fuel",
            "billet_mox_fuel",
            "billet_americium_fuel",
            "billet_les",
            "billet_schrabidium_fuel",
            "billet_hes",
            "billet_australium_lesser",
            "billet_australium_greater",
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
            "billet_gh336",
            "ingot_gunmetal",
            "plate_gunmetal",
            "ingot_weaponsteel",
            "plate_weaponsteel",
            "ingot_combine_steel",
            "powder_combine_steel",
            "plate_combine_steel",
            "ingot_saturnite",
            "plate_saturnite",
            "ingot_australium",
            "nugget_australium",
            "nugget_australium_lesser",
            "nugget_australium_greater",
            "plate_schrabidium",
            "ingot_calcium",
            "powder_calcium",
            "ingot_cadmium",
            "powder_cadmium",
            "powder_bismuth",
            "powder_euphemium",
            "powder_yellowcake",
            "powder_schrabidate",
            "powder_caesium",
            "dust",
            "dust_tiny",
            "fragment_coltan",
            "powder_coltan_ore",
            "powder_coltan",
            "powder_tantalium",
            "gem_tantalium",
            "nugget_tantalium",
            "powder_cement",
            "powder_paleogenite",
            "powder_australium",
            "powder_tennessine",
            "powder_flux",
            "powder_borax",
            "powder_balefire",
            "powder_semtex_mix",
            "powder_desh_ready",
            "crystal_starmetal",
            "gem_volcanic",
            "fragment_neodymium",
            "fragment_cobalt",
            "fragment_niobium",
            "fragment_cerium",
            "fragment_lanthanium",
            "fragment_actinium",
            "fragment_boron",
            "fragment_meteorite",
            "scrap_oil",
            "ring_starmetal",
            "ingot_mud",
            "ingot_cft",
            "fallout",
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
            "crystal_coal",
            "crystal_iron",
            "crystal_gold",
            "crystal_redstone",
            "crystal_lapis",
            "crystal_uranium",
            "crystal_thorium",
            "crystal_plutonium",
            "crystal_sulfur",
            "crystal_niter",
            "crystal_copper",
            "crystal_titanium",
            "crystal_tungsten",
            "crystal_aluminium",
            "crystal_fluorite",
            "crystal_beryllium",
            "crystal_lead",
            "crystal_schraranium",
            "crystal_schrabidium",
            "crystal_phosphorus",
            "crystal_lithium",
            "crystal_trixite",
            "crystal_cobalt",
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
            "nugget_beryllium",
            "nugget_polonium",
            "nugget_technetium",
            "nugget_cobalt",
            "nugget_silicon",
            "nugget_co60",
            "nugget_sr90",
            "nugget_au198",
            "nugget_pb209",
            "nugget_ra226",
            "nugget_actinium",
            "nugget_zirconium"
    ).stream()), Stream.of(INGOT_C4)).toList();

    public static final RegistryObject<Item> DEMON_CORE_OPEN = registerLegacy("demon_core_open",
            () -> new DemonCoreItem(new Item.Properties()));
    public static final RegistryObject<Item> DEMON_CORE_CLOSED = simpleItem("demon_core_closed");
    public static final RegistryObject<Item> BATTERY_SPARK = simpleStackOneItem("battery_spark");
    public static final RegistryObject<Item> BATTERY_TRIXITE = simpleStackOneItem("battery_trixite");

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
            DEMON_CORE_OPEN,
            DEMON_CORE_CLOSED,
            simpleStackSizeItem("egg_balefire_shard", 16),
            simpleStackOneItem("egg_balefire"),
            BATTERY_SPARK,
            BATTERY_TRIXITE,
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
            PARTICLE_EMPTY,
            PARTICLE_HYDROGEN,
            PARTICLE_COPPER,
            PARTICLE_LEAD,
            PARTICLE_AMAT,
            PARTICLE_ASCHRAB,
            PARTICLE_HIGGS,
            PARTICLE_DARK,
            PARTICLE_TACHYON,
            PARTICLE_STRANGE,
            PARTICLE_SPARKTICLE,
            PARTICLE_DIGAMMA,
            CELL_EMPTY,
            CELL_ANTIMATTER,
            CELL_ANTI_SCHRABIDIUM,
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
            UPGRADE_AFTERBURN_3,
            UPGRADE_FORTUNE_1,
            UPGRADE_FORTUNE_2,
            UPGRADE_FORTUNE_3,
            UPGRADE_SMELTER,
            UPGRADE_NULLIFIER,
            UPGRADE_SHREDDER,
            UPGRADE_CENTRIFUGE,
            UPGRADE_CRYSTALLIZER,
            UPGRADE_SCREM,
            UPGRADE_RADIUS,
            UPGRADE_HEALTH
    );

    private static final List<RegistryObject<Item>> LEGACY_TOOL_ITEMS = List.of(
            SCREWDRIVER,
            HAND_DRILL,
            WRENCH,
            BLOWTORCH,
            BOLTGUN,
            DEFUSER,
            COLTAN_TOOL
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
            PISTON_SELENIUM,
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
            METEORITE_SWORD,
            METEORITE_SWORD_SEARED,
            METEORITE_SWORD_REFORGED,
            METEORITE_SWORD_HARDENED,
            METEORITE_SWORD_ALLOYED,
            METEORITE_SWORD_MACHINED,
            METEORITE_SWORD_TREATED,
            METEORITE_SWORD_ETCHED,
            METEORITE_SWORD_BRED,
            METEORITE_SWORD_IRRADIATED,
            METEORITE_SWORD_FUSED,
            METEORITE_SWORD_BALEFUL,
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

    public static final List<RegistryObject<Item>> ZIRNOX_ROD_ITEMS = List.of(
            zirnoxRod("rod_zirnox_natural_uranium_fuel", 250_000, 30, false),
            zirnoxRod("rod_zirnox_uranium_fuel", 200_000, 50, false),
            zirnoxRod("rod_zirnox_th232", 20_000, 0, true),
            zirnoxRod("rod_zirnox_thorium_fuel", 200_000, 40, false),
            zirnoxRod("rod_zirnox_mox_fuel", 165_000, 75, false),
            zirnoxRod("rod_zirnox_plutonium_fuel", 175_000, 65, false),
            zirnoxRod("rod_zirnox_u233_fuel", 150_000, 100, false),
            zirnoxRod("rod_zirnox_u235_fuel", 165_000, 85, false),
            zirnoxRod("rod_zirnox_les_fuel", 150_000, 150, false),
            zirnoxRod("rod_zirnox_lithium", 20_000, 0, true),
            zirnoxRod("rod_zirnox_zfb_mox", 50_000, 35, false));

    public static final List<RegistryObject<Item>> PWR_FUEL_ITEMS = pwrFuels();

    public static final List<RegistryObject<Item>> PWR_FUEL_HOT_ITEMS = simpleParts(
            "pwr_fuel_hot_meu",
            "pwr_fuel_hot_heu233",
            "pwr_fuel_hot_heu235",
            "pwr_fuel_hot_men",
            "pwr_fuel_hot_hen237",
            "pwr_fuel_hot_mox",
            "pwr_fuel_hot_mep",
            "pwr_fuel_hot_hep239",
            "pwr_fuel_hot_hep241",
            "pwr_fuel_hot_mea",
            "pwr_fuel_hot_hea242",
            "pwr_fuel_hot_hes326",
            "pwr_fuel_hot_hes327",
            "pwr_fuel_hot_bfb_am_mix",
            "pwr_fuel_hot_bfb_pu241");

    public static final List<RegistryObject<Item>> PWR_FUEL_DEPLETED_ITEMS = simpleParts(
            "pwr_fuel_depleted_meu",
            "pwr_fuel_depleted_heu233",
            "pwr_fuel_depleted_heu235",
            "pwr_fuel_depleted_men",
            "pwr_fuel_depleted_hen237",
            "pwr_fuel_depleted_mox",
            "pwr_fuel_depleted_mep",
            "pwr_fuel_depleted_hep239",
            "pwr_fuel_depleted_hep241",
            "pwr_fuel_depleted_mea",
            "pwr_fuel_depleted_hea242",
            "pwr_fuel_depleted_hes326",
            "pwr_fuel_depleted_hes327",
            "pwr_fuel_depleted_bfb_am_mix",
            "pwr_fuel_depleted_bfb_pu241");
    public static final RegistryObject<Item> PWR_PRINTER = registerLegacy("pwr_printer",
            () -> new PWRPrinterItem(new Item.Properties().stacksTo(1)));

    public static final List<RegistryObject<Item>> WATZ_PELLET_ITEMS = watzPellets(false);
    public static final List<RegistryObject<Item>> WATZ_PELLET_DEPLETED_ITEMS = watzPellets(true);

    public static final RegistryObject<Item> RBMK_LID = simpleItem("rbmk_lid");
    public static final RegistryObject<Item> RBMK_LID_GLASS = simpleItem("rbmk_lid_glass");
    public static final RegistryObject<Item> RBMK_FUEL_EMPTY = simpleItem("rbmk_fuel_empty");
    public static final RegistryObject<Item> RBMK_TOOL = registerLegacy("rbmk_tool",
            () -> new RBMKToolItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> AMS_CATALYST_BLANK = simpleStackOneItem("ams_catalyst_blank");
    public static final RegistryObject<Item> AMS_CATALYST_ALUMINIUM =
            amsCatalyst("ams_catalyst_aluminium", 0xCCCCCC, 1_000_000L, 1.15F, 0.85F, 1.15F);
    public static final RegistryObject<Item> AMS_CATALYST_BERYLLIUM =
            amsCatalyst("ams_catalyst_beryllium", 0x97978B, 0L, 1.25F, 0.95F, 1.05F);
    public static final RegistryObject<Item> AMS_CATALYST_CAESIUM =
            amsCatalyst("ams_catalyst_caesium", 0x6400FF, 2_500_000L, 1.00F, 0.85F, 1.15F);
    public static final RegistryObject<Item> AMS_CATALYST_CERIUM =
            amsCatalyst("ams_catalyst_cerium", 0x1D3FFF, 1_000_000L, 1.15F, 1.15F, 0.85F);
    public static final RegistryObject<Item> AMS_CATALYST_COBALT =
            amsCatalyst("ams_catalyst_cobalt", 0x789BBE, 0L, 1.25F, 1.05F, 0.95F);
    public static final RegistryObject<Item> AMS_CATALYST_COPPER =
            amsCatalyst("ams_catalyst_copper", 0xAADE29, 0L, 1.25F, 1.00F, 1.00F);
    public static final RegistryObject<Item> AMS_CATALYST_DINEUTRONIUM =
            amsCatalyst("ams_catalyst_dineutronium", 0x334077, 2_500_000L, 1.00F, 1.15F, 0.85F);
    public static final RegistryObject<Item> AMS_CATALYST_EUPHEMIUM =
            amsCatalyst("ams_catalyst_euphemium", 0xFF9CD2, 2_500_000L, 1.00F, 1.00F, 1.00F);
    public static final RegistryObject<Item> AMS_CATALYST_IRON =
            amsCatalyst("ams_catalyst_iron", 0xFF7E22, 1_000_000L, 1.15F, 0.95F, 1.05F);
    public static final RegistryObject<Item> AMS_CATALYST_LITHIUM =
            amsCatalyst("ams_catalyst_lithium", 0xFF2727, 0L, 1.25F, 0.85F, 1.15F);
    public static final RegistryObject<Item> AMS_CATALYST_NIOBIUM =
            amsCatalyst("ams_catalyst_niobium", 0x3BF1B6, 1_000_000L, 1.15F, 1.05F, 0.95F);
    public static final RegistryObject<Item> AMS_CATALYST_SCHRABIDIUM =
            amsCatalyst("ams_catalyst_schrabidium", 0x32FFFF, 2_500_000L, 1.00F, 1.05F, 0.95F);
    public static final RegistryObject<Item> AMS_CATALYST_STRONTIUM =
            amsCatalyst("ams_catalyst_strontium", 0xDD0D35, 1_000_000L, 1.15F, 1.00F, 1.00F);
    public static final RegistryObject<Item> AMS_CATALYST_THORIUM =
            amsCatalyst("ams_catalyst_thorium", 0x653B22, 2_500_000L, 1.00F, 0.95F, 1.05F);
    public static final RegistryObject<Item> AMS_CATALYST_TUNGSTEN =
            amsCatalyst("ams_catalyst_tungsten", 0xF5FF48, 0L, 1.25F, 1.15F, 0.85F);
    public static final RegistryObject<Item> AMS_LENS = registerLegacy("ams_lens",
            () -> new AmsLensItem(new Item.Properties().stacksTo(1), AmsLensItem.LEGACY_MAX_DAMAGE));
    public static final RegistryObject<Item> AMS_CORE_SING =
            amsCore("ams_core_sing", 1_000_000_000L, 200, 10, 500);
    public static final RegistryObject<Item> AMS_CORE_WORMHOLE =
            amsCore("ams_core_wormhole", 1_500_000_000L, 200, 15, 650);
    public static final RegistryObject<Item> AMS_CORE_EYEOFHARMONY =
            amsCore("ams_core_eyeofharmony", 2_500_000_000L, 300, 10, 800);
    public static final RegistryObject<Item> AMS_CORE_THINGY =
            amsCore("ams_core_thingy", 5_000_000_000L, 250, 5, 2500);

    public static final List<RegistryObject<Item>> AMS_CATALYST_ITEMS = List.of(
            AMS_CATALYST_BLANK,
            AMS_CATALYST_ALUMINIUM,
            AMS_CATALYST_BERYLLIUM,
            AMS_CATALYST_CAESIUM,
            AMS_CATALYST_CERIUM,
            AMS_CATALYST_COBALT,
            AMS_CATALYST_COPPER,
            AMS_CATALYST_DINEUTRONIUM,
            AMS_CATALYST_EUPHEMIUM,
            AMS_CATALYST_IRON,
            AMS_CATALYST_LITHIUM,
            AMS_CATALYST_NIOBIUM,
            AMS_CATALYST_SCHRABIDIUM,
            AMS_CATALYST_STRONTIUM,
            AMS_CATALYST_THORIUM,
            AMS_CATALYST_TUNGSTEN);

    public static final List<RegistryObject<Item>> RBMK_FUEL_ROD_ITEMS = List.of(
            rbmkFuelRod("rbmk_fuel_ueu"),
            rbmkFuelRod("rbmk_fuel_meu"),
            rbmkFuelRod("rbmk_fuel_heu233"),
            rbmkFuelRod("rbmk_fuel_heu235"),
            rbmkFuelRod("rbmk_fuel_uzh"),
            rbmkFuelRod("rbmk_fuel_thmeu"),
            rbmkFuelRod("rbmk_fuel_lep"),
            rbmkFuelRod("rbmk_fuel_mep"),
            rbmkFuelRod("rbmk_fuel_hep239"),
            rbmkFuelRod("rbmk_fuel_hep241"),
            rbmkFuelRod("rbmk_fuel_lea"),
            rbmkFuelRod("rbmk_fuel_mea"),
            rbmkFuelRod("rbmk_fuel_hea241"),
            rbmkFuelRod("rbmk_fuel_hea242"),
            rbmkFuelRod("rbmk_fuel_men"),
            rbmkFuelRod("rbmk_fuel_hen"),
            rbmkFuelRod("rbmk_fuel_mox"),
            rbmkFuelRod("rbmk_fuel_les"),
            rbmkFuelRod("rbmk_fuel_mes"),
            rbmkFuelRod("rbmk_fuel_hes"),
            rbmkFuelRod("rbmk_fuel_leaus"),
            rbmkFuelRod("rbmk_fuel_heaus"),
            rbmkFuelRod("rbmk_fuel_po210be"),
            rbmkFuelRod("rbmk_fuel_ra226be"),
            rbmkFuelRod("rbmk_fuel_pu238be"),
            rbmkFuelRod("rbmk_fuel_balefire_gold"),
            rbmkFuelRod("rbmk_fuel_flashlead"),
            rbmkFuelRod("rbmk_fuel_balefire"),
            rbmkFuelRod("rbmk_fuel_zfb_bismuth"),
            rbmkFuelRod("rbmk_fuel_zfb_pu241"),
            rbmkFuelRod("rbmk_fuel_zfb_am_mix"),
            rbmkFuelRod("rbmk_fuel_drx"),
            rbmkFuelRod("rbmk_fuel_test"));

    public static final List<RegistryObject<Item>> RBMK_PELLET_ITEMS = List.of(
            rbmkPellet("rbmk_pellet_ueu"),
            rbmkPellet("rbmk_pellet_meu"),
            rbmkPellet("rbmk_pellet_heu233"),
            rbmkPellet("rbmk_pellet_heu235"),
            rbmkPellet("rbmk_pellet_uzh"),
            rbmkPellet("rbmk_pellet_thmeu"),
            rbmkPellet("rbmk_pellet_lep"),
            rbmkPellet("rbmk_pellet_mep"),
            rbmkPellet("rbmk_pellet_hep239"),
            rbmkPellet("rbmk_pellet_hep241"),
            rbmkPellet("rbmk_pellet_lea"),
            rbmkPellet("rbmk_pellet_mea"),
            rbmkPellet("rbmk_pellet_hea241"),
            rbmkPellet("rbmk_pellet_hea242"),
            rbmkPellet("rbmk_pellet_men"),
            rbmkPellet("rbmk_pellet_hen"),
            rbmkPellet("rbmk_pellet_mox"),
            rbmkPellet("rbmk_pellet_les"),
            rbmkPellet("rbmk_pellet_mes"),
            rbmkPellet("rbmk_pellet_hes"),
            rbmkPellet("rbmk_pellet_leaus"),
            rbmkPellet("rbmk_pellet_heaus"),
            rbmkPellet("rbmk_pellet_po210be"),
            rbmkPellet("rbmk_pellet_ra226be"),
            rbmkPellet("rbmk_pellet_pu238be"),
            rbmkPellet("rbmk_pellet_balefire_gold"),
            rbmkPellet("rbmk_pellet_flashlead"),
            rbmkPellet("rbmk_pellet_balefire"),
            rbmkPellet("rbmk_pellet_zfb_bismuth"),
            rbmkPellet("rbmk_pellet_zfb_pu241"),
            rbmkPellet("rbmk_pellet_zfb_am_mix"),
            rbmkPellet("rbmk_pellet_drx"));

    public static final List<RegistryObject<Item>> CONTROL_TAB_ITEMS = Stream.<List<RegistryObject<Item>>>of(simpleParts(
            "pile_rod_uranium",
            "pile_rod_pu239",
            "pile_rod_plutonium",
            "pile_rod_source",
            "pile_rod_boron",
            "pile_rod_lithium",
            "pile_rod_detector",
            "rod_empty",
            "rod_lithium",
            "rod_tritium",
            "rod_co",
            "rod_co60",
            "rod_th232",
            "rod_thf",
            "rod_u235",
            "rod_np237",
            "rod_u238",
            "rod_pu238",
            "rod_pu239",
            "rod_rgp",
            "rod_waste",
            "rod_lead",
            "rod_uranium",
            "rod_ra226",
            "rod_ac227",
            "rod_dual_empty",
            "rod_quad_empty",
            "rod_dual_lithium",
            "rod_dual_tritium",
            "rod_dual_co",
            "rod_dual_co60",
            "rod_dual_th232",
            "rod_dual_thf",
            "rod_dual_u235",
            "rod_dual_np237",
            "rod_dual_u238",
            "rod_dual_pu238",
            "rod_dual_pu239",
            "rod_dual_rgp",
            "rod_dual_waste",
            "rod_dual_lead",
            "rod_dual_uranium",
            "rod_dual_ra226",
            "rod_dual_ac227",
            "rod_quad_lithium",
            "rod_quad_tritium",
            "rod_quad_co",
            "rod_quad_co60",
            "rod_quad_th232",
            "rod_quad_thf",
            "rod_quad_u235",
            "rod_quad_np237",
            "rod_quad_u238",
            "rod_quad_pu238",
            "rod_quad_pu239",
            "rod_quad_rgp",
            "rod_quad_waste",
            "rod_quad_lead",
            "rod_quad_uranium",
            "rod_quad_ra226",
            "rod_quad_ac227",
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
            "pellet_rtg_strontium",
            "pellet_rtg_cobalt",
            "pellet_rtg_actinium",
            "pellet_rtg_polonium",
            "pellet_rtg_americium",
            "pellet_rtg_gold",
            "pellet_rtg_lead",
            "reacher",
            "pellet_rtg_depleted_bismuth",
            "pellet_rtg_depleted_mercury",
            "pellet_rtg_depleted_neptunium",
            "pellet_rtg_depleted_lead",
            "pellet_rtg_depleted_zirconium",
            "pellet_rtg_depleted_nickel",
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
    ), ZIRNOX_ROD_ITEMS, PWR_FUEL_ITEMS, PWR_FUEL_HOT_ITEMS, PWR_FUEL_DEPLETED_ITEMS, List.of(PWR_PRINTER), WATZ_PELLET_ITEMS, WATZ_PELLET_DEPLETED_ITEMS, List.of(ICF_PELLET_EMPTY, ICF_PELLET, ICF_PELLET_DEPLETED, PARTICLE_MUON), List.of(RBMK_LID, RBMK_LID_GLASS, RBMK_FUEL_EMPTY), RBMK_FUEL_ROD_ITEMS, RBMK_PELLET_ITEMS, AMS_CATALYST_ITEMS, List.of(AMS_LENS, AMS_CORE_SING, AMS_CORE_WORMHOLE, AMS_CORE_EYEOFHARMONY), MACHINE_UPGRADE_ITEMS, LEGACY_TOOL_ITEMS, DRILLBIT_ITEMS, PISTON_SET_ITEMS, ARC_ELECTRODE_ITEMS, PA_COIL_ITEMS, ABILITY_TOOL_ITEMS, List.<RegistryObject<Item>>of(CATALYTIC_CONVERTER, SHREDDER_BLADES_STEEL, SHREDDER_BLADES_TITANIUM, SHREDDER_BLADES_DESH, SIREN_TRACK), SINGULARITY_FAMILY_ITEMS, CONTROL_BATTERY_ITEMS)
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
            RAG,
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
            POWDER_POWER,
            POWDER_SAWDUST,
            SCRAP,
            BEDROCK_ORE_BASE,
            BEDROCK_ORE,
            BEDROCK_ORE_FRAGMENT,
            COPPER_COIL,
            TUNGSTEN_COIL,
            GOLD_COIL,
            MOTOR,
            GEAR_LARGE,
            GEAR_LARGE_STEEL,
            SAWBLADE,
            UPGRADE_TEMPLATE,
            IRON_PLATE_STAMP,
            IRON_FLAT_STAMP,
            IRON_WIRE_STAMP,
            IRON_CIRCUIT_STAMP,
            STAMP_357,
            STAMP_44,
            STAMP_9,
            STAMP_50,
            LAUNCH_CODE_PIECE,
            LAUNCH_CODE,
            LAUNCH_KEY,
            WIRING_RED_COPPER,
            LASER_CRYSTAL_CO2,
            LASER_CRYSTAL_BISMUTH,
            LASER_CRYSTAL_CMB,
            LASER_CRYSTAL_DNT,
            LASER_CRYSTAL_DIGAMMA,
            INGOT_SEMTEX,
            BOTTLE_MERCURY
    ), Stream.concat(SEDNA_GUN_PART_ITEMS.stream(),
            Stream.concat(SATELLITE_PART_ITEMS.stream(), EXTRA_PARTS_TAB_ITEMS.stream()))).toList();

    public static final List<RegistryObject<Item>> WEAPON_TAB_ITEMS = Stream.concat(Stream.of(
            AMMO_STANDARD_G12_BP,
            AMMO_STANDARD_G12_BP_MAGNUM,
            AMMO_STANDARD_G12_BP_SLUG,
            AMMO_STANDARD_G12,
            AMMO_STANDARD_G12_SLUG,
            AMMO_STANDARD_G12_FLECHETTE,
            AMMO_STANDARD_G12_MAGNUM,
            AMMO_STANDARD_G12_EXPLOSIVE,
            AMMO_STANDARD_G12_PHOSPHORUS,
            AMMO_STANDARD_G10,
            AMMO_STANDARD_G10_SHRAPNEL,
            AMMO_STANDARD_G10_DU,
            AMMO_STANDARD_G10_SLUG,
            AMMO_STANDARD_G10_EXPLOSIVE,
            AMMO_STANDARD_P22_SP,
            AMMO_STANDARD_P22_FMJ,
            AMMO_STANDARD_P22_JHP,
            AMMO_STANDARD_P22_AP,
            AMMO_STANDARD_P9_SP,
            AMMO_STANDARD_P9_FMJ,
            AMMO_STANDARD_P9_JHP,
            AMMO_STANDARD_P9_AP,
            AMMO_STANDARD_P45_SP,
            AMMO_STANDARD_P45_FMJ,
            AMMO_STANDARD_P45_JHP,
            AMMO_STANDARD_P45_AP,
            AMMO_STANDARD_P45_DU,
            AMMO_STANDARD_R556_SP,
            AMMO_STANDARD_R556_FMJ,
            AMMO_STANDARD_R556_JHP,
            AMMO_STANDARD_R556_AP,
            AMMO_STANDARD_M44_BP,
            AMMO_STANDARD_M44_SP,
            AMMO_STANDARD_M44_FMJ,
            AMMO_STANDARD_M44_JHP,
            AMMO_STANDARD_M44_AP,
            AMMO_STANDARD_M44_EXPRESS,
            AMMO_STANDARD_M357_BP,
            AMMO_STANDARD_M357_SP,
            AMMO_STANDARD_M357_FMJ,
            AMMO_STANDARD_M357_JHP,
            AMMO_STANDARD_M357_AP,
            AMMO_STANDARD_M357_EXPRESS,
            AMMO_STANDARD_R762_SP,
            AMMO_STANDARD_R762_FMJ,
            AMMO_STANDARD_R762_JHP,
            AMMO_STANDARD_R762_AP,
            AMMO_STANDARD_R762_DU,
            AMMO_STANDARD_R762_HE,
            AMMO_STANDARD_BMG50_SP,
            AMMO_STANDARD_BMG50_FMJ,
            AMMO_STANDARD_BMG50_JHP,
            AMMO_STANDARD_BMG50_AP,
            AMMO_STANDARD_BMG50_DU,
            AMMO_STANDARD_BMG50_HE,
            AMMO_STANDARD_BMG50_SM,
            AMMO_STANDARD_B75,
            AMMO_STANDARD_B75_INC,
            AMMO_STANDARD_B75_EXP,
            AMMO_STANDARD_G26_FLARE,
            AMMO_STANDARD_G26_FLARE_SUPPLY,
            AMMO_STANDARD_G26_FLARE_WEAPON,
            AMMO_STANDARD_G40_HE,
            AMMO_STANDARD_G40_HEAT,
            AMMO_STANDARD_G40_DEMO,
            AMMO_STANDARD_G40_INC,
            AMMO_STANDARD_G40_PHOSPHORUS,
            AMMO_STANDARD_ROCKET_HE,
            AMMO_STANDARD_ROCKET_HEAT,
            AMMO_STANDARD_ROCKET_DEMO,
            AMMO_STANDARD_ROCKET_INC,
            AMMO_STANDARD_ROCKET_PHOSPHORUS,
            STICK_DYNAMITE,
            AMMO_STANDARD_CAPACITOR,
            AMMO_STANDARD_CAPACITOR_OVERCHARGE,
            AMMO_STANDARD_CAPACITOR_IR,
            AMMO_STANDARD_COIL_TUNGSTEN,
            AMMO_STANDARD_COIL_FERROURANIUM,
            AMMO_STANDARD_FLAME_DIESEL,
            AMMO_STANDARD_FLAME_GAS,
            AMMO_STANDARD_FLAME_NAPALM,
            AMMO_STANDARD_FLAME_BALEFIRE,
            AMMO_STANDARD_NUKE_STANDARD,
            AMMO_STANDARD_NUKE_DEMO,
            AMMO_STANDARD_NUKE_HIGH,
            AMMO_STANDARD_NUKE_TOTS,
            AMMO_STANDARD_NUKE_HIVE,
            AMMO_STANDARD_NUKE_BALEFIRE,
            AMMO_FIREEXT_0,
            AMMO_FIREEXT_1,
            AMMO_FIREEXT_2,
            AMMO_STANDARD_TAU_URANIUM,
            AMMO_STANDARD_CT_HOOK,
            AMMO_STANDARD_CT_MORTAR,
            AMMO_STANDARD_CT_MORTAR_CHARGE,
            AMMO_STANDARD_STONE,
            AMMO_STANDARD_STONE_AP,
            AMMO_STANDARD_STONE_IRON,
            AMMO_STANDARD_STONE_SHOT,
            AMMO_SHELL_STOCK,
            AMMO_SHELL_EXPLOSIVE,
            AMMO_SHELL_APFSDS_T,
            AMMO_SHELL_APFSDS_DU,
            AMMO_SHELL_W9,
            AMMO_ARTY,
            AMMO_ARTY_CLASSIC,
            AMMO_ARTY_HE,
            AMMO_ARTY_PHOSPHORUS,
            AMMO_ARTY_PHOSPHORUS_MULTI,
            AMMO_ARTY_MINI_NUKE,
            AMMO_ARTY_MINI_NUKE_MULTI,
            AMMO_ARTY_NUKE,
            AMMO_ARTY_CARGO,
            AMMO_ARTY_CHLORINE,
            AMMO_ARTY_PHOSGENE,
            AMMO_ARTY_MUSTARD_GAS,
            AMMO_HIMARS_STANDARD,
            AMMO_HIMARS_STANDARD_HE,
            AMMO_HIMARS_STANDARD_WP,
            AMMO_HIMARS_STANDARD_TB,
            AMMO_HIMARS_STANDARD_LAVA,
            AMMO_HIMARS_STANDARD_MINI_NUKE,
            AMMO_HIMARS_SINGLE,
            AMMO_HIMARS_SINGLE_TB,
            AMMO_CONTAINER,
            AMMO_CONTAINER_ALT,
            TURRET_CHIP,
            GUN_PEPPERBOX,
            GUN_MARESLEG,
            GUN_MARESLEG_AKIMBO,
            GUN_MARESLEG_BROKEN,
            GUN_LIBERATOR,
            GUN_SPAS12,
            GUN_AUTOSHOTGUN,
            GUN_AUTOSHOTGUN_SEXY,
            GUN_DOUBLE_BARREL,
            GUN_DOUBLE_BARREL_SACRED_DRAGON,
            GUN_AUTOSHOTGUN_SHREDDER,
            GUN_AUTOSHOTGUN_HERETIC,
            GUN_LIGHT_REVOLVER,
            GUN_LIGHT_REVOLVER_ATLAS,
            GUN_LIGHT_REVOLVER_DANI,
            GUN_HENRY,
            GUN_HENRY_LINCOLN,
            GUN_HEAVY_REVOLVER,
            GUN_HEAVY_REVOLVER_LILMAC,
            GUN_HEAVY_REVOLVER_PROTEGE,
            GUN_HANGMAN,
            GUN_GREASEGUN,
            GUN_LAG,
            GUN_UZI,
            GUN_UZI_AKIMBO,
            GUN_AM180,
            GUN_STAR_F,
            GUN_STAR_F_AKIMBO,
            GUN_G3,
            GUN_G3_ZEBRA,
            GUN_STG77,
            GUN_CARBINE,
            GUN_MINIGUN,
            GUN_MINIGUN_LACUNAE,
            GUN_MINIGUN_DUAL,
            GUN_FLAREGUN,
            GUN_CONGOLAKE,
            GUN_MK108,
            GUN_AMAT,
            GUN_AMAT_SUBTLETY,
            GUN_AMAT_PENANCE,
            GUN_M2,
            GUN_BOLTER,
            GUN_ABERRATOR,
            GUN_ABERRATOR_EOTT,
            GUN_PANZERSCHRECK,
            GUN_STINGER,
            GUN_QUADRO,
            GUN_MISSILE_LAUNCHER,
            GUN_LASER_PISTOL,
            GUN_LASER_PISTOL_PEW_PEW,
            GUN_LASER_PISTOL_MORNING_GLORY,
            GUN_LASRIFLE,
            GUN_TAU,
            GUN_COILGUN,
            GUN_FLAMER,
            GUN_FLAMER_TOPAZ,
            GUN_FLAMER_DAYBREAKER,
            GUN_CHEMTHROWER,
            GUN_TESLA_CANNON,
            GUN_FATMAN,
            GUN_FOLLY,
            GUN_FIREEXT,
            GUN_CHARGE_THROWER,
            GUN_NI4NI,
            GUN_DRILL
    ), WEAPON_MOD_CREATIVE_ITEMS.stream()).toList();

    public static final List<RegistryObject<Item>> CONSUMABLE_TAB_ITEMS = Stream.of(
            RBMK_TOOL,
            REACTOR_SENSOR,
            OIL_DETECTOR,
            SURVEY_SCANNER,
            ORE_DENSITY_SCANNER,
            GEIGER_COUNTER,
            DOSIMETER,
            DIGAMMA_DIAGNOSTIC,
            POLLUTION_DETECTOR,
            MIRROR_TOOL,
            LINKER,
            AMMO_BAG,
            AMMO_BAG_INFINITE,
            CASING_BAG,
            GUN_KIT_1,
            GUN_KIT_2,
            RADAWAY,
            RADAWAY_STRONG,
            RADAWAY_FLUSH,
            RADX,
            FIVE_HTP,
            XANAX,
            PILL_IODINE,
            SIOX,
            PILL_HERBAL,
            FMN,
            PLAN_C,
            PILL_RED,
            SYRINGE_METAL_EMPTY,
            SYRINGE_METAL_STIMPAK,
            SYRINGE_METAL_MEDX,
            SYRINGE_METAL_PSYCHO,
            SYRINGE_METAL_SUPER,
            SYRINGE_TAINT,
            BOTTLE2_EMPTY,
            COIN_TOKEN,
            CAP_NUKA,
            CAP_QUANTUM,
            CAP_SPARKLE,
            CAP_RAD,
            CAP_KORL,
            CAP_FRITZ,
            BOTTLE_NUKA,
            BOTTLE_CHERRY,
            BOTTLE_QUANTUM,
            CAN_EMPTY,
            CAN_BEPIS,
            CAN_LUNA,
            CAN_MUG,
            CAN_BREEN,
            DEFINITELYFOOD,
            TWINKIE,
            CHOCOLATE,
            GLOWING_STEW,
            MARSHMALLOW,
            BOOK_GUIDE,
            CANTEEN_VODKA,
            GLYPHID_MEAT,
            GLYPHID_MEAT_GRILLED,
            GAS_MASK_FILTER,
            GAS_MASK_FILTER_MONO,
            GAS_MASK_FILTER_COMBO,
            GAS_MASK_FILTER_RAG,
            GAS_MASK_FILTER_PISS,
            JETPACK_TANK,
            ATTACHMENT_MASK,
            ATTACHMENT_MASK_MONO,
            GOGGLES,
            ASHGLASSES,
            HAT,
            NO9,
            GAS_MASK,
            GAS_MASK_M65,
            GAS_MASK_MONO,
            GAS_MASK_OLDE,
            MASK_OF_INFAMY,
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
            JACKET,
            JACKET2,
            SECURITY_HELMET,
            SECURITY_PLATE,
            SECURITY_LEGS,
            SECURITY_BOOTS,
            STARMETAL_HELMET,
            STARMETAL_PLATE,
            STARMETAL_LEGS,
            STARMETAL_BOOTS,
            ROBES_HELMET,
            ROBES_PLATE,
            ROBES_LEGS,
            ROBES_BOOTS,
            ZIRCONIUM_LEGS,
            DNT_HELMET,
            DNT_PLATE,
            DNT_LEGS,
            DNT_BOOTS,
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
            BISMUTH_HELMET,
            BISMUTH_PLATE,
            BISMUTH_LEGS,
            BISMUTH_BOOTS,
            T51_HELMET,
            T51_PLATE,
            T51_LEGS,
            T51_BOOTS,
            STEAMSUIT_HELMET,
            STEAMSUIT_PLATE,
            STEAMSUIT_LEGS,
            STEAMSUIT_BOOTS,
            DIESELSUIT_HELMET,
            DIESELSUIT_PLATE,
            DIESELSUIT_LEGS,
            DIESELSUIT_BOOTS,
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
            BJ_PLATE_JETPACK,
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
            WINGS_LIMP,
            WINGS_MURK,
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
            KEY,
            KEY_KIT,
            KEY_FAKE,
            PIN,
            PADLOCK_RUSTY,
            PADLOCK,
            PADLOCK_REINFORCED,
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
            BUCKET_MUD,
            BUCKET_ACID,
            BUCKET_TOXIC,
            BUCKET_SCHRABIDIC_ACID,
            BUCKET_SULFURIC_ACID,
            DISPERSER_CANISTER_EMPTY,
            DISPERSER_CANISTER,
            GLYPHID_GLAND_EMPTY,
            GLYPHID_GLAND,
            INF_WATER,
            INF_WATER_MK2,
            CHLORINE_PINWHEEL,
            FLUID_IDENTIFIER_MULTI,
            SIPHON,
            PIPETTE,
            PIPETTE_BORON,
            PIPETTE_LABORATORY,
            FLUID_DUCT
    ).toList();

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static void registerToolStacks() {
        Toolable.ToolType.SCREWDRIVER.register(new ItemStack(SCREWDRIVER.get()));
        Toolable.ToolType.HAND_DRILL.register(new ItemStack(HAND_DRILL.get()));
        Toolable.ToolType.WRENCH.register(new ItemStack(WRENCH.get()));
        Toolable.ToolType.TORCH.register(new ItemStack(BLOWTORCH.get()));
        Toolable.ToolType.BOLT.register(new ItemStack(BOLTGUN.get()));
        Toolable.ToolType.DEFUSER.register(new ItemStack(DEFUSER.get()));
    }

    public static RegistryObject<Item> legacyItem(String name) {
        return ITEMS_BY_LEGACY_NAME.get(name);
    }

    static RegistryObject<Item> registerBlockItem(String name, java.util.function.Supplier<Item> supplier) {
        RegistryObject<Item> item = ITEMS.register(name, supplier);
        ITEMS_BY_LEGACY_NAME.put(name, item);
        return item;
    }

    private static RegistryObject<Item> ingot(String name) {
        return simpleItem(name);
    }

    private static RegistryObject<Item> part(String name) {
        return simpleItem(name);
    }

    private static RegistryObject<Item> shredderBlade(String name, int durability) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new ShredderBladeItem(durability));
        ITEMS_BY_LEGACY_NAME.put(name, item);
        return item;
    }

    private static RegistryObject<Item> missile(String name, MissileItem.FormFactor formFactor, MissileItem.Tier tier) {
        return missile(name, formFactor, tier, formFactor.defaultFuel(), true);
    }

    private static RegistryObject<Item> missile(String name, MissileItem.FormFactor formFactor, MissileItem.Tier tier,
                                                boolean launchable) {
        return missile(name, formFactor, tier, formFactor.defaultFuel(), launchable);
    }

    private static RegistryObject<Item> missile(String name, MissileItem.FormFactor formFactor, MissileItem.Tier tier,
                                                MissileItem.Fuel fuel) {
        return missile(name, formFactor, tier, fuel, true);
    }

    private static RegistryObject<Item> missile(String name, MissileItem.FormFactor formFactor, MissileItem.Tier tier,
                                                MissileItem.Fuel fuel, boolean launchable) {
        return registerLegacy(name, () -> new MissileItem(new Item.Properties().stacksTo(1), formFactor, tier,
                fuel, fuel.defaultCap(), launchable));
    }

    private static List<RegistryObject<Item>> missileParts(MissilePartItem.PartType type, String... names) {
        return Stream.of(names)
                .map(name -> registerLegacy(name, () -> new MissilePartItem(new Item.Properties().stacksTo(1), type, name)))
                .toList();
    }

    private static List<RegistryObject<Item>> simpleParts(String... names) {
        return Stream.of(names).map(ModItems::simpleItem).toList();
    }

    private static List<RegistryObject<Item>> simpleStackOneItems(String... names) {
        return Stream.of(names).map(ModItems::simpleStackOneItem).toList();
    }

    private static List<RegistryObject<Item>> stackSizeItems(int stackSize, String... names) {
        return Stream.of(names).map(name -> registerLegacy(name, () -> new Item(new Item.Properties().stacksTo(stackSize)))).toList();
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

    private static RegistryObject<Item> particleCapsule(String name) {
        return registerLegacy(name, () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    }

    private static RegistryObject<Item> zirnoxRod(String name, int maxLife, int heat, boolean breeding) {
        return registerLegacy(name, () -> new ZirnoxRodItem(new Item.Properties().stacksTo(1).durability(maxLife), heat, breeding));
    }

    private static List<RegistryObject<Item>> pwrFuels() {
        return Stream.of(PWRFuelRuntime.Type.values())
                .map(type -> registerLegacy("pwr_fuel_" + type.suffix(),
                        () -> new PWRFuelItem(new Item.Properties(), type)))
                .toList();
    }

    private static List<RegistryObject<Item>> watzPellets(boolean depleted) {
        return Stream.of(WatzFuelRuntime.Type.values())
                .map(type -> watzPellet((depleted ? "watz_pellet_depleted_" : "watz_pellet_") + type.suffix(),
                        type, depleted))
                .toList();
    }

    private static RegistryObject<Item> watzPellet(String name, WatzFuelRuntime.Type type, boolean depleted) {
        return registerLegacy(name, () -> new WatzPelletItem(new Item.Properties().stacksTo(16), type, depleted));
    }

    private static RegistryObject<Item> rbmkFuelRod(String name) {
        return registerLegacy(name, () -> new RBMKFuelRodItem(new Item.Properties(),
                RBMKFuelRodRegistry.find(name).orElseThrow(() -> new IllegalArgumentException("Unknown RBMK fuel rod: " + name))));
    }

    private static RegistryObject<Item> rbmkPellet(String name) {
        return registerLegacy(name, () -> new RBMKPelletItem(new Item.Properties(),
                RBMKFuelRodRegistry.all().stream()
                        .filter(entry -> name.equals(entry.legacyPelletId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown RBMK pellet: " + name))));
    }

    private static RegistryObject<Item> amsCatalyst(String name, int color, long powerAbs,
            float powerMod, float heatMod, float fuelMod) {
        return registerLegacy(name, () -> new AmsCatalystItem(new Item.Properties().stacksTo(1),
                color, powerAbs, powerMod, heatMod, fuelMod));
    }

    private static RegistryObject<Item> amsCore(String name, long powerBase, int heatBase,
            int fuelBase, int dfcMultiplier) {
        return registerLegacy(name, () -> new AmsCoreItem(new Item.Properties().stacksTo(1),
                powerBase, heatBase, fuelBase, dfcMultiplier));
    }

    private static RegistryObject<Item> padlock(String name, double lockMod) {
        return registerLegacy(name, () -> new PadlockItem(new Item.Properties().stacksTo(1), lockMod));
    }

    private static RegistryObject<Item> registerLegacy(String name, java.util.function.Supplier<Item> supplier) {
        RegistryObject<Item> item = ITEMS.register(name, supplier);
        ITEMS_BY_LEGACY_NAME.put(name, item);
        return item;
    }

    private static RegistryObject<Item> hazmatArmor(String name, HbmArmorMaterials material, ArmorItem.Type type) {
        return registerLegacy(name, () -> type == ArmorItem.Type.HELMET
                ? new HazmatMaskArmorItem(material, new Item.Properties())
                : new HazmatArmorItem(material, type, new Item.Properties()));
    }

    private static RegistryObject<Item> asbestosArmor(String name, ArmorItem.Type type) {
        return registerLegacy(name, () -> new ArmorItem(HbmArmorMaterials.ASBESTOS, type, new Item.Properties()));
    }

    private static RegistryObject<Item> armor(String name, HbmArmorMaterials material, ArmorItem.Type type) {
        return registerLegacy(name, () -> new ArmorItem(material, type, new Item.Properties()));
    }

    private static RegistryObject<Item> euphemiumArmor(String name, ArmorItem.Type type) {
        return registerLegacy(name, () -> new EuphemiumArmorItem(type, new Item.Properties()));
    }

    private static RegistryObject<Item> fsbArmor(String name, HbmArmorMaterials material, ArmorItem.Type type) {
        return fsbArmor(name, material, type, List.of());
    }

    private static RegistryObject<Item> fsbArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects) {
        return fsbArmor(name, material, type, effects, false, 0);
    }

    private static RegistryObject<Item> fsbArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, boolean noHelmet, int dashCount) {
        return fsbArmor(name, material, type, effects, noHelmet, dashCount, FsbArmorItem.FullSetTraits.NONE);
    }

    private static RegistryObject<Item> fsbArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, boolean noHelmet, int dashCount,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new FsbArmorItem(material, type, new Item.Properties(), effects,
                noHelmet, dashCount, traits));
    }

    private static RegistryObject<Item> fsbPoweredArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain) {
        return fsbPoweredArmor(name, material, type, effects, maxCharge, chargeRate, consumption, drain,
                FsbArmorItem.FullSetTraits.NONE);
    }

    private static RegistryObject<Item> fsbPoweredArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new FsbPoweredArmorItem(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> ncrpaArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new NcrpaArmorItem(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> envSuitArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain) {
        return registerLegacy(name, () -> new EnvSuitArmorItem(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain));
    }

    private static RegistryObject<Item> bjArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new BjArmorItem(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> bjJetpackArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain) {
        return bjJetpackArmor(name, material, type, effects, maxCharge, chargeRate, consumption, drain,
                FsbArmorItem.FullSetTraits.NONE);
    }

    private static RegistryObject<Item> bjJetpackArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new BjJetpackArmorItem(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> dnsArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new DnsArmorItem(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> trenchmasterArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, boolean noHelmet, int dashCount,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new TrenchmasterArmorItem(material, type, new Item.Properties(), effects,
                noHelmet, dashCount, traits));
    }

    private static RegistryObject<Item> fsbFueledArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, FluidType fuel, int maxFuel, int fillRate, int consumption,
            int drain) {
        return fsbFueledArmor(name, material, type, effects, fuel, maxFuel, fillRate, consumption, drain,
                FsbArmorItem.FullSetTraits.NONE);
    }

    private static RegistryObject<Item> fsbFueledArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, FluidType fuel, int maxFuel, int fillRate, int consumption,
            int drain, FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new FsbFueledArmorItem(material, type, new Item.Properties(), effects,
                fuel, maxFuel, fillRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> steamsuitArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, FluidType fuel, int maxFuel, int fillRate, int consumption,
            int drain, FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new SteamsuitArmorItem(material, type, new Item.Properties(), effects,
                fuel, maxFuel, fillRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> fsbFueledArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, int maxFuel, int fillRate, int consumption, int drain,
            FluidType... fuels) {
        return registerLegacy(name, () -> new FsbFueledArmorItem(material, type, new Item.Properties(), effects,
                maxFuel, fillRate, consumption, drain, fuels));
    }

    private static RegistryObject<Item> dieselSuitArmor(String name, ArmorItem.Type type) {
        return registerLegacy(name, () -> new DieselSuitArmorItem(HbmArmorMaterials.DIESEL, type, new Item.Properties(),
                dieselEffects(), 64_000, 500, 50, 1, dieselTraits(), HbmFluids.DIESEL, HbmFluids.DIESEL_CRACK));
    }

    private static List<FsbArmorItem.FullSetEffect> t51Effects() {
        return List.of(FsbArmorItem.effect(MobEffects.DAMAGE_BOOST, 20, 0, "Strength I"));
    }

    private static List<FsbArmorItem.FullSetEffect> steamsuitEffects() {
        return List.of(FsbArmorItem.effect(MobEffects.DIG_SPEED, 20, 4, "Haste V"));
    }

    private static List<FsbArmorItem.FullSetEffect> dieselEffects() {
        return List.of(
                FsbArmorItem.effect(MobEffects.MOVEMENT_SPEED, 20, 2, "Speed III"),
                FsbArmorItem.effect(MobEffects.JUMP, 20, 2, "Jump Boost III"));
    }

    private static List<FsbArmorItem.FullSetEffect> schrabidiumEffects() {
        return List.of(
                FsbArmorItem.effect(MobEffects.DIG_SPEED, 20, 2, "Haste III"),
                FsbArmorItem.effect(MobEffects.DAMAGE_BOOST, 20, 2, "Strength III"),
                FsbArmorItem.effect(MobEffects.JUMP, 20, 1, "Jump Boost II"),
                FsbArmorItem.effect(MobEffects.MOVEMENT_SPEED, 20, 2, "Speed III"));
    }

    private static List<FsbArmorItem.FullSetEffect> cmbEffects() {
        return List.of(
                FsbArmorItem.effect(MobEffects.MOVEMENT_SPEED, 20, 2, "Speed III"),
                FsbArmorItem.effect(MobEffects.DIG_SPEED, 20, 2, "Haste III"),
                FsbArmorItem.effect(MobEffects.DAMAGE_BOOST, 20, 4, "Strength V"));
    }

    private static List<FsbArmorItem.FullSetEffect> paaEffects() {
        return List.of(FsbArmorItem.effect(MobEffects.DIG_SPEED, 20, 0, "Haste I"));
    }

    private static List<FsbArmorItem.FullSetEffect> bismuthEffects() {
        return List.of(
                FsbArmorItem.effect(MobEffects.JUMP, 20, 6, "Jump Boost VII"),
                FsbArmorItem.effect(MobEffects.MOVEMENT_SPEED, 20, 6, "Speed VII"),
                FsbArmorItem.effect(MobEffects.REGENERATION, 20, 1, "Regeneration II"),
                FsbArmorItem.effect(MobEffects.NIGHT_VISION, 15 * 20, 0, "Night Vision"));
    }

    private static List<FsbArmorItem.FullSetEffect> ajrEffects() {
        return List.of(
                FsbArmorItem.effect(MobEffects.JUMP, 20, 0, "Jump Boost I"),
                FsbArmorItem.effect(MobEffects.DAMAGE_BOOST, 20, 0, "Strength I"));
    }

    private static List<FsbArmorItem.FullSetEffect> rpaEffects() {
        return List.of(FsbArmorItem.effect(MobEffects.DAMAGE_BOOST, 20, 3, "Strength IV"));
    }

    private static List<FsbArmorItem.FullSetEffect> bjEffects() {
        return List.of(
                FsbArmorItem.effect(MobEffects.MOVEMENT_SPEED, 20, 1, "Speed II"),
                FsbArmorItem.effect(MobEffects.JUMP, 20, 0, "Jump Boost I"),
                FsbArmorItem.effect(MobEffects.ABSORPTION, 20, 0, "Absorption I"),
                FsbArmorItem.effect(ModEffects.RADX::get, 20, 0, "Rad-X I"));
    }

    private static List<FsbArmorItem.FullSetEffect> envEffects() {
        return List.of(
                FsbArmorItem.effect(MobEffects.MOVEMENT_SPEED, 20, 1, "Speed II"),
                FsbArmorItem.effect(MobEffects.JUMP, 20, 0, "Jump Boost I"));
    }

    private static List<FsbArmorItem.FullSetEffect> fauEffects() {
        return List.of(FsbArmorItem.effect(MobEffects.JUMP, 20, 1, "Jump Boost II"));
    }

    private static List<FsbArmorItem.FullSetEffect> dnsEffects() {
        return List.of(
                FsbArmorItem.effect(MobEffects.DAMAGE_BOOST, 20, 9, "Strength X"),
                FsbArmorItem.effect(MobEffects.DIG_SPEED, 20, 7, "Haste VIII"),
                FsbArmorItem.effect(MobEffects.JUMP, 20, 2, "Jump Boost III"));
    }

    private static List<FsbArmorItem.FullSetEffect> taurunEffects() {
        return List.of(FsbArmorItem.effect(MobEffects.DAMAGE_BOOST, 20, 0, "Strength I"));
    }

    private static List<FsbArmorItem.FullSetEffect> trenchmasterEffects() {
        return List.of(
                FsbArmorItem.effect(MobEffects.DAMAGE_BOOST, 20, 2, "Strength III"),
                FsbArmorItem.effect(MobEffects.DIG_SPEED, 20, 1, "Haste II"),
                FsbArmorItem.effect(MobEffects.JUMP, 20, 1, "Jump Boost II"),
                FsbArmorItem.effect(MobEffects.MOVEMENT_SPEED, 20, 0, "Speed I"));
    }

    private static FsbArmorItem.FullSetTraits hardLandingTraits() {
        return FsbArmorItem.FullSetTraits.builder()
                .hardLanding()
                .build();
    }

    private static FsbArmorItem.FullSetTraits t51Traits() {
        return FsbArmorItem.FullSetTraits.builder()
                .vats()
                .geigerSound()
                .hardLanding()
                .step("hbm:step.metal")
                .jump("hbm:step.iron_jump")
                .fall("hbm:step.iron_land")
                .build();
    }

    private static FsbArmorItem.FullSetTraits dieselTraits() {
        return FsbArmorItem.FullSetTraits.builder()
                .vats()
                .thermal()
                .build();
    }

    private static FsbArmorItem.FullSetTraits poweredStepTraits() {
        return FsbArmorItem.FullSetTraits.builder()
                .vats()
                .geigerSound()
                .hardLanding()
                .step("hbm:step.powered")
                .jump("hbm:step.powered")
                .fall("hbm:step.powered")
                .build();
    }

    private static FsbArmorItem.FullSetTraits bjTraits() {
        return FsbArmorItem.FullSetTraits.builder()
                .vats()
                .thermal()
                .geigerSound()
                .hardLanding()
                .step("hbm:step.metal")
                .jump("hbm:step.iron_jump")
                .fall("hbm:step.iron_land")
                .build();
    }

    private static FsbArmorItem.FullSetTraits hevTraits() {
        return FsbArmorItem.FullSetTraits.builder()
                .geigerSound()
                .customGeiger()
                .build();
    }

    private static FsbArmorItem.FullSetTraits fauTraits() {
        return FsbArmorItem.FullSetTraits.builder()
                .thermal()
                .geigerSound()
                .hardLanding()
                .step("hbm:step.metal")
                .jump("hbm:step.iron_jump")
                .fall("hbm:step.iron_land")
                .build();
    }

    private static FsbArmorItem.FullSetTraits dnsTraits() {
        return FsbArmorItem.FullSetTraits.builder()
                .vats()
                .thermal()
                .geigerSound()
                .hardLanding()
                .step("hbm:step.metal")
                .jump("hbm:step.iron_jump")
                .fall("hbm:step.iron_land")
                .build();
    }

    private static FsbArmorItem.FullSetTraits stepSizeTraits() {
        return FsbArmorItem.FullSetTraits.builder()
                .stepSize(1)
                .build();
    }

    private static FsbArmorItem.FullSetTraits trenchTraits() {
        return FsbArmorItem.FullSetTraits.builder()
                .vats()
                .stepSize(1)
                .build();
    }

    private static RegistryObject<Item> fullHoodGasMaskArmor(String name, HbmArmorMaterials material) {
        return registerLegacy(name, () -> new GasMaskArmorItem(material, new Item.Properties(), List.of()));
    }

    private static RegistryObject<Item> liquidatorMaskArmor(String name) {
        return registerLegacy(name, () -> new LiquidatorMaskArmorItem(new Item.Properties()));
    }

    private static RegistryObject<Item> liquidatorArmor(String name, ArmorItem.Type type) {
        return registerLegacy(name, () -> new LiquidatorArmorItem(type, new Item.Properties()));
    }

    private static RegistryObject<Item> gasMaskArmor(String name, boolean mono) {
        return registerLegacy(name, () -> new GasMaskArmorItem(ArmorMaterials.IRON, new Item.Properties(), mono));
    }

    private static RegistryObject<Item> ironHeadArmor(String name) {
        return registerLegacy(name, () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    }

    private static RegistryObject<Item> objIronHeadArmor(String name) {
        return registerLegacy(name, () -> new ObjArmorItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
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

    private static RegistryObject<Item> meteoriteSword(String name, float damage, String legacyStageKey) {
        return registerLegacy(name, () -> new MeteoriteSwordItem(damage, legacyStageKey,
                toolProperties(HbmToolTiers.METEORITE, false)));
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

    private static RegistryObject<Item> artilleryAmmo(LegacyArtilleryAmmoCatalog.AmmoType type) {
        return registerLegacy(type.legacyName(), () -> new LegacyArtilleryAmmoItem(new Item.Properties(), type));
    }

    private static RegistryObject<Item> himarsAmmo(LegacyArtilleryAmmoCatalog.AmmoType type) {
        return registerLegacy(type.legacyName(),
                () -> new LegacyArtilleryAmmoItem(new Item.Properties().stacksTo(1), type));
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
        RegistryObject<Item> item = ITEMS.register(name,
                () -> new Item(simpleStackSizeProperties(name, maxStackSize)));
        ITEMS_BY_LEGACY_NAME.put(name, item);
        return item;
    }

    private static Item.Properties simpleStackSizeProperties(String name, int maxStackSize) {
        Item.Properties properties = new Item.Properties().stacksTo(maxStackSize);
        if (isZirnoxRodProduct(name)) {
            RegistryObject<Item> emptyRod = legacyItem("rod_zirnox_empty");
            if (emptyRod != null) {
                properties.craftRemainder(emptyRod.get());
            }
        }
        return properties;
    }

    private static boolean isZirnoxRodProduct(String name) {
        return "rod_zirnox_tritium".equals(name)
                || name.startsWith("rod_zirnox_") && name.endsWith("_depleted");
    }

    private static RegistryObject<Item> sednaGun(SednaGunConfig config) {
        return registerLegacy(config.legacyName(), () -> new SednaGunItem(new Item.Properties(), config));
    }

    private static RegistryObject<Item> weaponMod(WeaponModItem.Spec spec) {
        return registerLegacy(spec.modernName(), () -> new WeaponModItem(new Item.Properties(), spec));
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
