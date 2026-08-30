package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.ability.ToolAreaAbilities;
import com.hbm.ntm.ability.ToolHarvestAbilities;
import com.hbm.ntm.ability.WeaponAbilities;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmCreativeBatteryItem;
import com.hbm.ntm.energy.HbmLegacyEnergyCoreItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.entity.cart.NtmMinecartBase;
import com.hbm.ntm.entity.cart.NtmMinecartType;
import com.hbm.ntm.item.AmmoBagItem;
import com.hbm.ntm.item.AnchorRemoteItem;
import com.hbm.ntm.item.AnalysisToolItem;
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
import com.hbm.ntm.item.BobmazonCatalogItem;
import com.hbm.ntm.item.BlueprintFolderItem;
import com.hbm.ntm.item.ChargeThrowerItem;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.item.DetonatorItem;
import com.hbm.ntm.item.LaserDetonatorItem;
import com.hbm.ntm.item.LemegetonItem;
import com.hbm.ntm.item.MultiDetonatorItem;
import com.hbm.ntm.item.DemonCoreItem;
import com.hbm.ntm.item.DieselSuitArmorItem;
import com.hbm.ntm.item.DroppedDetonatorItem;
import com.hbm.ntm.item.AntimatterClusterItem;
import com.hbm.ntm.item.ArmorCapeItem;
import com.hbm.ntm.item.BjArmorItem;
import com.hbm.ntm.item.CasingBagItem;
import com.hbm.ntm.item.CbtDeviceItem;
import com.hbm.ntm.item.ChemicalDyeItem;
import com.hbm.ntm.item.ChemthrowerItem;
import com.hbm.ntm.item.ChocolateItem;
import com.hbm.ntm.item.ColtanCompassItem;
import com.hbm.ntm.item.ContainmentBoxItem;
import com.hbm.ntm.item.CraftingDegradationItem;
import com.hbm.ntm.item.DigammaParticleItem;
import com.hbm.ntm.item.DrillGunItem;
import com.hbm.ntm.item.DrillbitItem;
import com.hbm.ntm.item.DroneLinkerItem;
import com.hbm.ntm.item.DroneItem;
import com.hbm.ntm.item.EffectPillItem;
import com.hbm.ntm.item.ExpensiveModeItem;
import com.hbm.ntm.item.EnvSuitArmorItem;
import com.hbm.ntm.item.EuphemiumArmorItem;
import com.hbm.ntm.item.FabulousHatArmorItem;
import com.hbm.ntm.item.FiveHtpItem;
import com.hbm.ntm.item.FertilizerItem;
import com.hbm.ntm.item.FlaskInfusionItem;
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
import com.hbm.ntm.item.GasMaskArmorItem;
import com.hbm.ntm.item.GasMaskFilterItem;
import com.hbm.ntm.item.GunRepairKitItem;
import com.hbm.ntm.item.HbmAbilitySwordItem;
import com.hbm.ntm.item.HbmAbilityToolItem;
import com.hbm.ntm.item.HazmatMaskArmorItem;
import com.hbm.ntm.item.HbmArmorMaterials;
import com.hbm.ntm.item.HbmFueledAbilityToolItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.DisperserCanisterItem;
import com.hbm.ntm.item.CrucibleWeaponItem;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
import com.hbm.ntm.item.HotDustedItem;
import com.hbm.ntm.item.HotItem;
import com.hbm.ntm.item.HbmPoweredAbilitySwordItem;
import com.hbm.ntm.item.HbmPoweredAbilityToolItem;
import com.hbm.ntm.item.HbmPotatosItem;
import com.hbm.ntm.item.HbmRagItem;
import com.hbm.ntm.item.HbmSuitBatteryItem;
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
import com.hbm.ntm.item.LegacyBombWaffleItem;
import com.hbm.ntm.item.LegacyBdclItem;
import com.hbm.ntm.item.LegacyBigSwordItem;
import com.hbm.ntm.item.LegacyBottleOpenerItem;
import com.hbm.ntm.item.LegacyBoltgunItem;
import com.hbm.ntm.item.LegacyCigaretteItem;
import com.hbm.ntm.item.LegacyChainsawItem;
import com.hbm.ntm.item.LegacyEnergyDrinkItem;
import com.hbm.ntm.item.LegacyFoodItem;
import com.hbm.ntm.item.Mk2PileRodItem;
import com.hbm.ntm.item.LegacyConserveItem;
import com.hbm.ntm.item.LegacyCrayonItem;
import com.hbm.ntm.item.LegacyCustomKitItem;
import com.hbm.ntm.item.LegacyDuckSpawnItem;
import com.hbm.ntm.item.LegacyGavelItem;
import com.hbm.ntm.item.LegacyLoreItem;
import com.hbm.ntm.item.LegacyMemeSpoonItem;
import com.hbm.ntm.item.LegacyMusicDiscItem;
import com.hbm.ntm.item.LegacyPancakeItem;
import com.hbm.ntm.item.LegacyPeasItem;
import com.hbm.ntm.item.LegacyPipeLeadItem;
import com.hbm.ntm.item.LegacyRadiationFoodItem;
import com.hbm.ntm.item.LegacyRedstoneSwordItem;
import com.hbm.ntm.item.LegacyReerGraarItem;
import com.hbm.ntm.item.LegacySyringeItem;
import com.hbm.ntm.item.LegacyShimmerWeaponItem;
import com.hbm.ntm.item.LegacySignWeaponItem;
import com.hbm.ntm.item.LegacyTemFlakesItem;
import com.hbm.ntm.item.LegacyTrainItem;
import com.hbm.ntm.item.LegacyUllapoolCaberItem;
import com.hbm.ntm.item.LegacyToolWeaponItem;
import com.hbm.ntm.item.LegacyWrenchFlippedItem;
import com.hbm.ntm.item.LegacyWiringItem;
import com.hbm.ntm.item.LiquidatorArmorItem;
import com.hbm.ntm.item.LiquidatorMaskArmorItem;
import com.hbm.ntm.item.MarshmallowItem;
import com.hbm.ntm.item.MatchstickItem;
import com.hbm.ntm.item.MeteoriteSwordItem;
import com.hbm.ntm.item.MeltdownToolItem;
import com.hbm.ntm.item.MirrorToolItem;
import com.hbm.ntm.item.MufflerItem;
import com.hbm.ntm.item.NtmMinecartItem;
import com.hbm.ntm.item.PadlockItem;
import com.hbm.ntm.item.MissileLauncherGunItem;
import com.hbm.ntm.item.MissileDesignatorItem;
import com.hbm.ntm.item.MissileStarterKitItem;
import com.hbm.ntm.item.RangefinderItem;
import com.hbm.ntm.item.BombCallerItem;
import com.hbm.ntm.item.RebarPlacerItem;
import com.hbm.ntm.item.ResearchReactorPlateFuelItem;
import com.hbm.ntm.item.RubberBoatItem;
import com.hbm.ntm.item.Ni4NiGunItem;
import com.hbm.ntm.item.NcrpaArmorItem;
import com.hbm.ntm.item.No9ArmorItem;
import com.hbm.ntm.item.NukeElectricStarterKitItem;
import com.hbm.ntm.item.ObjArmorItem;
import com.hbm.ntm.item.OreByproductItem;
import com.hbm.ntm.item.OilDetectorItem;
import com.hbm.ntm.item.OreDensityScannerItem;
import com.hbm.ntm.item.PACoilItem;
import com.hbm.ntm.item.PlanCItem;
import com.hbm.ntm.item.PlasticBagItem;
import com.hbm.ntm.item.PlasticScrapItem;
import com.hbm.ntm.item.PowerNetToolItem;
import com.hbm.ntm.item.PowerArmorWeaponItem;
import com.hbm.ntm.item.PWRPrinterItem;
import com.hbm.ntm.item.RadarLinkerItem;
import com.hbm.ntm.item.PistonSetItem;
import com.hbm.ntm.item.RedPillItem;
import com.hbm.ntm.item.ReactorSensorItem;
import com.hbm.items.ItemCustomLore;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.items.armor.ArmorAJR;
import com.hbm.items.armor.ArmorAJRO;
import com.hbm.items.armor.ArmorAshGlasses;
import com.hbm.items.armor.ArmorBismuth;
import com.hbm.items.armor.ArmorBJ;
import com.hbm.items.armor.ArmorDNT;
import com.hbm.items.armor.ArmorDigamma;
import com.hbm.items.armor.ArmorEnvsuit;
import com.hbm.items.armor.ArmorEuphemium;
import com.hbm.items.armor.ArmorHEV;
import com.hbm.items.armor.ArmorHat;
import com.hbm.items.armor.ArmorModel;
import com.hbm.items.armor.ArmorNCRPA;
import com.hbm.items.armor.ArmorNo9;
import com.hbm.items.armor.ArmorRPA;
import com.hbm.items.armor.ArmorT51;
import com.hbm.items.armor.ArmorTaurun;
import com.hbm.items.armor.ArmorTrenchmaster;
import com.hbm.items.armor.ArmorFSBFueled;
import com.hbm.items.armor.ArmorFSBPowered;
import com.hbm.items.armor.ArmorBJJetpack;
import com.hbm.items.armor.WingsMurk;
import com.hbm.items.armor.ArmorDesh;
import com.hbm.items.armor.ArmorDiesel;
import com.hbm.items.armor.ArmorGasMask;
import com.hbm.items.armor.ArmorHazmat;
import com.hbm.items.armor.ArmorHazmatMask;
import com.hbm.items.armor.ArmorLiquidator;
import com.hbm.items.armor.ArmorLiquidatorMask;
import com.hbm.items.armor.ItemModBandaid;
import com.hbm.items.armor.ItemModAuto;
import com.hbm.items.armor.ItemModBathwater;
import com.hbm.items.armor.ItemModBattery;
import com.hbm.items.armor.ItemModCladding;
import com.hbm.items.armor.ItemModCard;
import com.hbm.items.armor.ItemModCharm;
import com.hbm.items.armor.ItemModCloud;
import com.hbm.items.armor.ItemModDefuser;
import com.hbm.items.armor.ItemModGasmask;
import com.hbm.items.armor.ItemModHealth;
import com.hbm.items.armor.ItemModInk;
import com.hbm.items.armor.ItemModIron;
import com.hbm.items.armor.ItemModInsert;
import com.hbm.items.armor.ItemModKnife;
import com.hbm.items.armor.ItemModLens;
import com.hbm.items.armor.ItemModLodestone;
import com.hbm.items.armor.ItemModMedal;
import com.hbm.items.armor.ItemModMilk;
import com.hbm.items.armor.ItemModMorningGlory;
import com.hbm.items.armor.ItemModNightVision;
import com.hbm.items.armor.ItemModObsidian;
import com.hbm.items.armor.ItemModPads;
import com.hbm.items.armor.ItemModPolish;
import com.hbm.items.armor.ItemModQuartz;
import com.hbm.items.armor.ItemModRevive;
import com.hbm.items.armor.ItemModSerum;
import com.hbm.items.armor.ItemModSensor;
import com.hbm.items.armor.ItemModServos;
import com.hbm.items.armor.ItemModShackles;
import com.hbm.items.armor.ItemModShield;
import com.hbm.items.armor.ItemModTesla;
import com.hbm.items.armor.ItemModTwoKick;
import com.hbm.items.armor.ItemModWD40;
import com.hbm.items.armor.MaskOfInfamy;
import com.hbm.items.armor.ModArmor;
import com.hbm.items.bomb.ItemFleija;
import com.hbm.items.machine.ItemBreedingRod;
import com.hbm.items.machine.ItemBreedingRod.BreedingRodType;
import com.hbm.items.machine.ItemBreedingRod.RodFamily;
import com.hbm.items.machine.ItemDepletedFuel;
import com.hbm.items.machine.ItemPWRFuel;
import com.hbm.items.machine.ItemRBMKPellet;
import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.items.machine.ItemRTGPelletDepleted;
import com.hbm.items.machine.ItemZirnoxRod;
import com.hbm.items.machine.ItemZirnoxRod.EnumZirnoxType;
import com.hbm.items.special.ItemHolotapeImage;
import com.hbm.items.special.ItemSimpleConsumable;
import com.hbm.items.special.ItemSyringe;
import com.hbm.items.special.ItemNuclearWaste;
import com.hbm.items.special.ItemWasteLong;
import com.hbm.items.special.ItemWasteShort;
import com.hbm.items.tool.ItemDigammaDiagnostic;
import com.hbm.items.tool.ItemDosimeter;
import com.hbm.items.tool.ItemGeigerCounter;
import com.hbm.items.tool.ItemPollutionDetector;
import com.hbm.items.weapon.ItemGrenadeDynamite;
import com.hbm.items.weapon.ItemGrenadeFishing;
import com.hbm.ntm.item.RTTYPagerItem;
import com.hbm.ntm.item.RBMKToolItem;
import com.hbm.ntm.item.SchrabidiumHammerItem;
import com.hbm.ntm.item.SettingsToolItem;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.item.ShredderBladeItem;
import com.hbm.ntm.item.SingularityItem;
import com.hbm.ntm.item.SioxItem;
import com.hbm.ntm.item.SirenCassetteItem;
import com.hbm.ntm.item.SteamsuitArmorItem;
import com.hbm.ntm.item.StealthBoyItem;
import com.hbm.ntm.item.StingerGunItem;
import com.hbm.ntm.item.SurveyScannerItem;
import com.hbm.ntm.item.TauCannonItem;
import com.hbm.ntm.item.TeleLinkItem;
import com.hbm.ntm.item.ToolboxItem;
import com.hbm.ntm.item.TurretChipItem;
import com.hbm.ntm.item.UniversalGrenadeItem;
import com.hbm.ntm.item.VanishOnDropItem;
import com.hbm.ntm.item.VodkaCanteenItem;
import com.hbm.ntm.item.WeaponModItem;
import com.hbm.ntm.item.WatzPelletItem;
import com.hbm.ntm.item.XanaxItem;
import com.hbm.ntm.item.missile.CustomMissileItem;
import com.hbm.ntm.item.missile.MissileItem;
import com.hbm.ntm.item.missile.MissilePartItem;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidContainerRules;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.bullet.LegacySednaGunConfigs;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.neutron.RBMKFuelRodRegistry;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.recipe.WatzFuelRuntime;
import com.hbm.ntm.recipe.PWRFuelRuntime;
import com.hbm.ntm.satellite.SatelliteChipItem;
import com.hbm.ntm.satellite.SatelliteDesignatorItem;
import com.hbm.ntm.satellite.SatelliteInterfaceItem;
import com.hbm.ntm.satellite.SatelliteItem;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import com.hbm.ntm.item.AmmoContainerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HbmNtm.MOD_ID);
    private static final Map<String, RegistryObject<Item>> ITEMS_BY_LEGACY_NAME = new LinkedHashMap<>();

    // EntityMappings#addMob registered a standard spawn egg for every listed mob in 1.7.10.
    // Bosses (Hunter Chopper, Mask Man, Quackos) and the Test Dummy remain intentionally excluded.
    public static final RegistryObject<Item> NUCLEAR_CREEPER_SPAWN_EGG = mobSpawnEgg("entity_mob_nuclear_creeper_spawn_egg", ModEntityTypes.NUCLEAR_CREEPER, 0x204131, 0x75CE00);
    public static final RegistryObject<Item> TAINTED_CREEPER_SPAWN_EGG = mobSpawnEgg("entity_mob_tainted_creeper_spawn_egg", ModEntityTypes.TAINTED_CREEPER, 0x813B9B, 0xD71FDD);
    public static final RegistryObject<Item> PHOSGENE_CREEPER_SPAWN_EGG = mobSpawnEgg("entity_mob_phosgene_creeper_spawn_egg", ModEntityTypes.PHOSGENE_CREEPER, 0xE3D398, 0xB8A06B);
    public static final RegistryObject<Item> VOLATILE_CREEPER_SPAWN_EGG = mobSpawnEgg("entity_mob_volatile_creeper_spawn_egg", ModEntityTypes.VOLATILE_CREEPER, 0xC28153, 0x4D382C);
    public static final RegistryObject<Item> GOLD_CREEPER_SPAWN_EGG = mobSpawnEgg("entity_mob_gold_creeper_spawn_egg", ModEntityTypes.GOLD_CREEPER, 0xECC136, 0x9E8B3E);
    public static final RegistryObject<Item> CYBER_CRAB_SPAWN_EGG = mobSpawnEgg("entity_cyber_crab_spawn_egg", ModEntityTypes.CYBER_CRAB, 0xAAAAAA, 0x444444);
    public static final RegistryObject<Item> TESLA_CRAB_SPAWN_EGG = mobSpawnEgg("entity_tesla_crab_spawn_egg", ModEntityTypes.TESLA_CRAB, 0xAAAAAA, 0x440000);
    public static final RegistryObject<Item> TAINT_CRAB_SPAWN_EGG = mobSpawnEgg("entity_taint_crab_spawn_egg", ModEntityTypes.TAINT_CRAB, 0xAAAAAA, 0xFF00FF);
    public static final RegistryObject<Item> DUCK_SPAWN_EGG = mobSpawnEgg("entity_fucc_a_ducc_spawn_egg", ModEntityTypes.DUCK, 0xD0D0D0, 0xFFBF00);
    public static final RegistryObject<Item> PIGEON_SPAWN_EGG = mobSpawnEgg("entity_pigeon_spawn_egg", ModEntityTypes.PIGEON, 0xC8C9CD, 0x858894);
    public static final RegistryObject<Item> FBI_SPAWN_EGG = mobSpawnEgg("entity_ntm_fbi_spawn_egg", ModEntityTypes.FBI, 0x008000, 0x404040);
    public static final RegistryObject<Item> FBI_DRONE_SPAWN_EGG = mobSpawnEgg("entity_ntm_fbi_drone_spawn_egg", ModEntityTypes.FBI_DRONE, 0x008000, 0x404040);
    public static final RegistryObject<Item> RAD_BEAST_SPAWN_EGG = mobSpawnEgg("entity_ntm_radiation_blaze_spawn_egg", ModEntityTypes.RAD_BEAST, 0x303030, 0x008000);
    public static final RegistryObject<Item> GLYPHID_SPAWN_EGG = mobSpawnEgg("entity_glyphid_spawn_egg", ModEntityTypes.GLYPHID, 0x724A21, 0xD2BB72);
    public static final RegistryObject<Item> GLYPHID_BRAWLER_SPAWN_EGG = mobSpawnEgg("entity_glyphid_brawler_spawn_egg", ModEntityTypes.GLYPHID_BRAWLER, 0x273038, 0xD2BB72);
    public static final RegistryObject<Item> GLYPHID_BEHEMOTH_SPAWN_EGG = mobSpawnEgg("entity_glyphid_behemoth_spawn_egg", ModEntityTypes.GLYPHID_BEHEMOTH, 0x267F00, 0xD2BB72);
    public static final RegistryObject<Item> GLYPHID_BRENDA_SPAWN_EGG = mobSpawnEgg("entity_glyphid_brenda_spawn_egg", ModEntityTypes.GLYPHID_BRENDA, 0x4FC0C0, 0xA0A0A0);
    public static final RegistryObject<Item> GLYPHID_BOMBARDIER_SPAWN_EGG = mobSpawnEgg("entity_glyphid_bombardier_spawn_egg", ModEntityTypes.GLYPHID_BOMBARDIER, 0xDDD919, 0xDBB79D);
    public static final RegistryObject<Item> GLYPHID_BLASTER_SPAWN_EGG = mobSpawnEgg("entity_glyphid_blaster_spawn_egg", ModEntityTypes.GLYPHID_BLASTER, 0xD83737, 0xDBB79D);
    public static final RegistryObject<Item> GLYPHID_SCOUT_SPAWN_EGG = mobSpawnEgg("entity_glyphid_scout_spawn_egg", ModEntityTypes.GLYPHID_SCOUT, 0x273038, 0xB9E36B);
    public static final RegistryObject<Item> GLYPHID_NUCLEAR_SPAWN_EGG = mobSpawnEgg("entity_glyphid_nuclear_spawn_egg", ModEntityTypes.GLYPHID_NUCLEAR, 0x267F00, 0xA0A0A0);
    public static final RegistryObject<Item> GLYPHID_DIGGER_SPAWN_EGG = mobSpawnEgg("entity_glyphid_digger_spawn_egg", ModEntityTypes.GLYPHID_DIGGER, 0x273038, 0x724A21);
    public static final RegistryObject<Item> PLASTIC_BAG_SPAWN_EGG = mobSpawnEgg("entity_plastic_bag_spawn_egg", ModEntityTypes.PLASTIC_BAG, 0xD0D0D0, 0x808080);
    public static final RegistryObject<Item> PARASITE_MAGGOT_SPAWN_EGG = mobSpawnEgg("entity_parasite_maggot_spawn_egg", ModEntityTypes.PARASITE_MAGGOT, 0xD0D0D0, 0x808080);
    public static final RegistryObject<Item> UNDEAD_SOLDIER_SPAWN_EGG = mobSpawnEgg("entity_ntm_undead_soldier_spawn_egg", ModEntityTypes.UNDEAD_SOLDIER, 0x749F30, 0x6C5B44);

    public static final List<RegistryObject<Item>> MOB_SPAWN_EGGS = List.of(
            NUCLEAR_CREEPER_SPAWN_EGG, TAINTED_CREEPER_SPAWN_EGG, PHOSGENE_CREEPER_SPAWN_EGG,
            VOLATILE_CREEPER_SPAWN_EGG, GOLD_CREEPER_SPAWN_EGG, CYBER_CRAB_SPAWN_EGG, TESLA_CRAB_SPAWN_EGG,
            TAINT_CRAB_SPAWN_EGG, DUCK_SPAWN_EGG, PIGEON_SPAWN_EGG, FBI_SPAWN_EGG, FBI_DRONE_SPAWN_EGG,
            RAD_BEAST_SPAWN_EGG, GLYPHID_SPAWN_EGG, GLYPHID_BRAWLER_SPAWN_EGG, GLYPHID_BEHEMOTH_SPAWN_EGG,
            GLYPHID_BRENDA_SPAWN_EGG, GLYPHID_BOMBARDIER_SPAWN_EGG, GLYPHID_BLASTER_SPAWN_EGG,
            GLYPHID_SCOUT_SPAWN_EGG, GLYPHID_NUCLEAR_SPAWN_EGG, GLYPHID_DIGGER_SPAWN_EGG,
            PLASTIC_BAG_SPAWN_EGG, PARASITE_MAGGOT_SPAWN_EGG, UNDEAD_SOLDIER_SPAWN_EGG);

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
    public static final RegistryObject<Item> LEAD_INGOT = ingot("ingot_lead");
    public static final RegistryObject<Item> STEEL_INGOT = ingot("ingot_steel");
    public static final RegistryObject<Item> COBALT_INGOT = ingot("ingot_cobalt");
    public static final RegistryObject<Item> ALUMINIUM_INGOT = ingot("ingot_aluminium");
    public static final RegistryObject<Item> BERYLLIUM_INGOT = ingot("ingot_beryllium");
    public static final RegistryObject<Item> SCHRABIDIUM_INGOT = ingot("ingot_schrabidium");
    public static final RegistryObject<Item> MERCURY_TINY_DROP = part("nugget_mercury_tiny");
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
    public static final RegistryObject<Item> POWDER_FERTILIZER = registerLegacy("powder_fertilizer",
            () -> new FertilizerItem(new Item.Properties()));
    public static final RegistryObject<Item> POWDER_THERMITE = registerLegacy("powder_thermite",
            () -> new ItemCustomLore(new Item.Properties()));
    public static final RegistryObject<Item> PELLET_GAS = registerLegacy("pellet_gas",
            () -> new ItemCustomLore(new Item.Properties()));
    public static final RegistryObject<Item> SCRAP = part("scrap");
    public static final RegistryObject<Item> BEDROCK_ORE_BASE = registerLegacy("bedrock_ore_base",
            () -> new BedrockOreBaseItem(new Item.Properties()));
    public static final RegistryObject<Item> BEDROCK_ORE = registerLegacy("bedrock_ore",
            () -> new BedrockOreItem(new Item.Properties()));
    public static final RegistryObject<Item> BEDROCK_ORE_FRAGMENT = registerLegacy("bedrock_ore_fragment",
            () -> new BedrockOreFragmentItem(new Item.Properties()));
    public static final RegistryObject<Item> SCRAP_PLASTIC = registerLegacy("scrap_plastic",
            () -> new PlasticScrapItem(new Item.Properties()));
    public static final RegistryObject<Item> MOLD_BASE = registerLegacy("mold_base",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MOLD = registerLegacy("mold",
            () -> new FoundryMoldItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FOUNDRY_SCRAPS = registerLegacy("scraps",
            () -> new FoundryScrapsItem(new Item.Properties()));
    public static final List<RegistryObject<Item>> INGOT_STEEL_DUSTED_ITEMS = hotDustedSteelItems();
    public static final RegistryObject<Item> INGOT_STEEL_DUSTED = INGOT_STEEL_DUSTED_ITEMS.get(0);
    public static final RegistryObject<Item> INGOT_CHAINSTEEL = hotItem("ingot_chainsteel", 100);
    public static final RegistryObject<Item> INGOT_METEORITE = hotItem("ingot_meteorite", 200);
    public static final RegistryObject<Item> INGOT_METEORITE_FORGED = hotItem("ingot_meteorite_forged", 200);
    public static final RegistryObject<Item> BLADE_METEORITE = hotItem("blade_meteorite", 200);
    public static final List<RegistryObject<Item>> HOT_SMITHING_ITEMS = Stream.concat(
            INGOT_STEEL_DUSTED_ITEMS.stream(),
            Stream.of(INGOT_CHAINSTEEL, INGOT_METEORITE, INGOT_METEORITE_FORGED, BLADE_METEORITE)).toList();
    public static final List<RegistryObject<Item>> HOT_ITEMS = HOT_SMITHING_ITEMS;

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
    public static final RegistryObject<Item> UPGRADE_MUFFLER = registerLegacy("upgrade_muffler",
            () -> new MufflerItem(new Item.Properties()));
    public static final RegistryObject<Item> BLUEPRINTS = registerLegacy("blueprints",
            () -> new ItemBlueprints(new Item.Properties()));
    public static final RegistryObject<Item> BLUEPRINT_FOLDER = registerLegacy("blueprint_folder",
            () -> new BlueprintFolderItem(new Item.Properties(), BlueprintFolderItem.Kind.ALT));
    public static final RegistryObject<Item> BLUEPRINT_FOLDER_DISCOVER = registerLegacy("blueprint_folder_discover",
            () -> new BlueprintFolderItem(new Item.Properties(), BlueprintFolderItem.Kind.DISCOVER));
    public static final RegistryObject<Item> BLUEPRINT_FOLDER_SECRET = registerLegacy("blueprint_folder_secret",
            () -> new BlueprintFolderItem(new Item.Properties(), BlueprintFolderItem.Kind.SECRET));
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
    public static final RegistryObject<Item> UPGRADE_STACK_1 = machineUpgrade("upgrade_stack_1", UpgradeType.STACK, 1);
    public static final RegistryObject<Item> UPGRADE_STACK_2 = machineUpgrade("upgrade_stack_2", UpgradeType.STACK, 2);
    public static final RegistryObject<Item> UPGRADE_STACK_3 = machineUpgrade("upgrade_stack_3", UpgradeType.STACK, 3);
    public static final RegistryObject<Item> UPGRADE_EJECTOR_1 = machineUpgrade("upgrade_ejector_1", UpgradeType.EJECTOR, 1);
    public static final RegistryObject<Item> UPGRADE_EJECTOR_2 = machineUpgrade("upgrade_ejector_2", UpgradeType.EJECTOR, 2);
    public static final RegistryObject<Item> UPGRADE_EJECTOR_3 = machineUpgrade("upgrade_ejector_3", UpgradeType.EJECTOR, 3);
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
    public static final RegistryObject<Item> NOTHING = registerLegacy("nothing",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ACHIEVEMENT_ICON = registerLegacy("achievement_icon",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TURRET_CHIP = registerLegacy("turret_chip",
            () -> new TurretChipItem(new Item.Properties()));
    public static final RegistryObject<Item> WIRING_RED_COPPER = registerLegacy("wiring_red_copper",
            () -> new LegacyWiringItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> POWER_NET_TOOL = registerLegacy("power_net_tool",
            () -> new PowerNetToolItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ANALYSIS_TOOL = registerLegacy("analysis_tool",
            () -> new AnalysisToolItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> IRON_PLATE_STAMP = registerLegacy("stamp_iron_plate",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.PLATE));
    public static final RegistryObject<Item> IRON_FLAT_STAMP = registerLegacy("stamp_iron_flat",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.FLAT));
    public static final RegistryObject<Item> IRON_WIRE_STAMP = registerLegacy("stamp_iron_wire",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.WIRE));
    public static final RegistryObject<Item> IRON_CIRCUIT_STAMP = registerLegacy("stamp_iron_circuit",
            () -> new ItemPressStamp(new Item.Properties().durability(64), ItemPressStamp.StampType.CIRCUIT));
    public static final RegistryObject<Item> STONE_FLAT_STAMP = flatStamp("stamp_stone_flat", 32);
    public static final RegistryObject<Item> STONE_PLATE_STAMP = pressStamp("stamp_stone_plate", 32,
            ItemPressStamp.StampType.PLATE);
    public static final RegistryObject<Item> STONE_WIRE_STAMP = pressStamp("stamp_stone_wire", 32,
            ItemPressStamp.StampType.WIRE);
    public static final RegistryObject<Item> STONE_CIRCUIT_STAMP = pressStamp("stamp_stone_circuit", 32,
            ItemPressStamp.StampType.CIRCUIT);
    public static final RegistryObject<Item> STEEL_FLAT_STAMP = flatStamp("stamp_steel_flat", 192);
    public static final RegistryObject<Item> STEEL_PLATE_STAMP = pressStamp("stamp_steel_plate", 192,
            ItemPressStamp.StampType.PLATE);
    public static final RegistryObject<Item> STEEL_WIRE_STAMP = pressStamp("stamp_steel_wire", 192,
            ItemPressStamp.StampType.WIRE);
    public static final RegistryObject<Item> STEEL_CIRCUIT_STAMP = pressStamp("stamp_steel_circuit", 192,
            ItemPressStamp.StampType.CIRCUIT);
    public static final RegistryObject<Item> TITANIUM_FLAT_STAMP = flatStamp("stamp_titanium_flat", 256);
    public static final RegistryObject<Item> TITANIUM_PLATE_STAMP = pressStamp("stamp_titanium_plate", 256,
            ItemPressStamp.StampType.PLATE);
    public static final RegistryObject<Item> TITANIUM_WIRE_STAMP = pressStamp("stamp_titanium_wire", 256,
            ItemPressStamp.StampType.WIRE);
    public static final RegistryObject<Item> TITANIUM_CIRCUIT_STAMP = pressStamp("stamp_titanium_circuit", 256,
            ItemPressStamp.StampType.CIRCUIT);
    public static final RegistryObject<Item> OBSIDIAN_FLAT_STAMP = flatStamp("stamp_obsidian_flat", 512);
    public static final RegistryObject<Item> OBSIDIAN_PLATE_STAMP = pressStamp("stamp_obsidian_plate", 512,
            ItemPressStamp.StampType.PLATE);
    public static final RegistryObject<Item> OBSIDIAN_WIRE_STAMP = pressStamp("stamp_obsidian_wire", 512,
            ItemPressStamp.StampType.WIRE);
    public static final RegistryObject<Item> OBSIDIAN_CIRCUIT_STAMP = pressStamp("stamp_obsidian_circuit", 512,
            ItemPressStamp.StampType.CIRCUIT);
    public static final RegistryObject<Item> DESH_FLAT_STAMP = flatStamp("stamp_desh_flat", 0);
    public static final RegistryObject<Item> DESH_PLATE_STAMP = pressStamp("stamp_desh_plate", 0,
            ItemPressStamp.StampType.PLATE);
    public static final RegistryObject<Item> DESH_WIRE_STAMP = pressStamp("stamp_desh_wire", 0,
            ItemPressStamp.StampType.WIRE);
    public static final RegistryObject<Item> DESH_CIRCUIT_STAMP = pressStamp("stamp_desh_circuit", 0,
            ItemPressStamp.StampType.CIRCUIT);
    public static final RegistryObject<Item> STAMP_357 = registerLegacy("stamp_357",
            () -> new ItemPressStamp(new Item.Properties().durability(1_000), ItemPressStamp.StampType.C357));
    public static final RegistryObject<Item> STAMP_44 = registerLegacy("stamp_44",
            () -> new ItemPressStamp(new Item.Properties().durability(1_000), ItemPressStamp.StampType.C44));
    public static final RegistryObject<Item> STAMP_9 = registerLegacy("stamp_9",
            () -> new ItemPressStamp(new Item.Properties().durability(1_000), ItemPressStamp.StampType.C9));
    public static final RegistryObject<Item> STAMP_50 = registerLegacy("stamp_50",
            () -> new ItemPressStamp(new Item.Properties().durability(1_000), ItemPressStamp.StampType.C50));
    public static final RegistryObject<Item> DESH_STAMP_357 = pressStamp("stamp_desh_357", 0,
            ItemPressStamp.StampType.C357);
    public static final RegistryObject<Item> DESH_STAMP_44 = pressStamp("stamp_desh_44", 0,
            ItemPressStamp.StampType.C44);
    public static final RegistryObject<Item> DESH_STAMP_9 = pressStamp("stamp_desh_9", 0,
            ItemPressStamp.StampType.C9);
    public static final RegistryObject<Item> DESH_STAMP_50 = pressStamp("stamp_desh_50", 0,
            ItemPressStamp.StampType.C50);
    public static final List<RegistryObject<Item>> PRESS_STAMP_VARIANT_ITEMS = List.of(
            STONE_FLAT_STAMP,
            STONE_PLATE_STAMP,
            STONE_WIRE_STAMP,
            STONE_CIRCUIT_STAMP,
            STEEL_FLAT_STAMP,
            STEEL_PLATE_STAMP,
            STEEL_WIRE_STAMP,
            STEEL_CIRCUIT_STAMP,
            TITANIUM_FLAT_STAMP,
            TITANIUM_PLATE_STAMP,
            TITANIUM_WIRE_STAMP,
            TITANIUM_CIRCUIT_STAMP,
            OBSIDIAN_FLAT_STAMP,
            OBSIDIAN_PLATE_STAMP,
            OBSIDIAN_WIRE_STAMP,
            OBSIDIAN_CIRCUIT_STAMP,
            DESH_FLAT_STAMP,
            DESH_PLATE_STAMP,
            DESH_WIRE_STAMP,
            DESH_CIRCUIT_STAMP,
            DESH_STAMP_357,
            DESH_STAMP_44,
            DESH_STAMP_9,
            DESH_STAMP_50);
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
    // GunFactory registers this debug round outside the ordinary weapon-tab inventory.
    public static final RegistryObject<Item> AMMO_DEBUG = simpleItem("ammo_debug");
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
    // Source GunFactory registry content; deliberately omitted from WEAPON_TAB_ITEMS like the old debug entry.
    public static final RegistryObject<Item> GUN_DEBUG = sednaGun(LegacySednaGunConfigs.GUN_DEBUG);
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
    public static final RegistryObject<Item> GUN_PA_MELEE = registerLegacy("gun_pa_melee",
            () -> new PowerArmorWeaponItem(new Item.Properties(), PowerArmorWeaponItem.Kind.MELEE));
    public static final RegistryObject<Item> GUN_PA_RANGED = registerLegacy("gun_pa_ranged",
            () -> new PowerArmorWeaponItem(new Item.Properties(), PowerArmorWeaponItem.Kind.RANGED));
    public static final RegistryObject<Item> CRUCIBLE = registerLegacy("crucible",
            () -> new CrucibleWeaponItem(new Item.Properties()));
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

    public static final RegistryObject<Item> GEIGER_COUNTER = registerLegacy("geiger_counter",
            () -> new ItemGeigerCounter(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DOSIMETER = registerLegacy("dosimeter",
            () -> new ItemDosimeter(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> OIL_DETECTOR = registerLegacy("oil_detector",
            () -> new OilDetectorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> COLTAN_TOOL = registerLegacy("coltan_tool",
            () -> new ColtanCompassItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DIGAMMA_DIAGNOSTIC = registerLegacy("digamma_diagnostic",
            () -> new ItemDigammaDiagnostic(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> POLLUTION_DETECTOR = registerLegacy("pollution_detector",
            () -> new ItemPollutionDetector(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> IV_EMPTY = registerLegacy("iv_empty",
            () -> ItemSimpleConsumable.ivEmpty(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> IV_BLOOD = registerLegacy("iv_blood",
            () -> ItemSimpleConsumable.ivBlood(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> IV_XP_EMPTY = registerLegacy("iv_xp_empty",
            () -> ItemSimpleConsumable.ivXpEmpty(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> IV_XP = registerLegacy("iv_xp",
            () -> ItemSimpleConsumable.ivXp(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> RADAWAY = registerLegacy("radaway",
            () -> ItemSimpleConsumable.radaway(new Item.Properties().stacksTo(16), 140));
    public static final RegistryObject<Item> RADAWAY_STRONG = registerLegacy("radaway_strong",
            () -> ItemSimpleConsumable.radaway(new Item.Properties().stacksTo(16), 350));
    public static final RegistryObject<Item> RADAWAY_FLUSH = registerLegacy("radaway_flush",
            () -> ItemSimpleConsumable.radaway(new Item.Properties().stacksTo(16), 500));
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
    public static final RegistryObject<Item> STEALTH_BOY = registerLegacy("stealth_boy",
            () -> new StealthBoyItem(new Item.Properties()));
    public static final RegistryObject<Item> CIGARETTE = registerLegacy("cigarette",
            () -> new LegacyCigaretteItem(new Item.Properties().stacksTo(16),
                    LegacyCigaretteItem.Kind.CIGARETTE));
    public static final RegistryObject<Item> CRACKPIPE = registerLegacy("crackpipe",
            () -> new LegacyCigaretteItem(new Item.Properties().stacksTo(1),
                    LegacyCigaretteItem.Kind.CRACKPIPE));
    public static final RegistryObject<Item> SYRINGE_EMPTY = registerLegacy("syringe_empty",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SYRINGE_ANTIDOTE = registerLegacy("syringe_antidote",
            () -> ItemSimpleConsumable.syringeAntidote(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SYRINGE_POISON = registerLegacy("syringe_poison",
            () -> ItemSimpleConsumable.syringePoison(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SYRINGE_AWESOME = registerLegacy("syringe_awesome",
            () -> ItemSimpleConsumable.syringeAwesome(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MED_BAG = registerLegacy("med_bag",
            () -> new ItemSyringe(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.MED_BAG));
    public static final RegistryObject<Item> SYRINGE_METAL_EMPTY = registerLegacy("syringe_metal_empty",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SYRINGE_METAL_STIMPAK = registerLegacy("syringe_metal_stimpak",
            () -> new ItemSyringe(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.STIMPAK));
    public static final RegistryObject<Item> SYRINGE_METAL_MEDX = registerLegacy("syringe_metal_medx",
            () -> new ItemSyringe(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.MEDX));
    public static final RegistryObject<Item> SYRINGE_METAL_PSYCHO = registerLegacy("syringe_metal_psycho",
            () -> new ItemSyringe(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.PSYCHO));
    public static final RegistryObject<Item> SYRINGE_METAL_SUPER = registerLegacy("syringe_metal_super",
            () -> new ItemSyringe(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.SUPER));
    public static final RegistryObject<Item> SYRINGE_TAINT = registerLegacy("syringe_taint",
            () -> new ItemSyringe(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.TAINT));
    public static final RegistryObject<Item> SYRINGE_MKUNICORN = registerLegacy("syringe_mkunicorn",
            () -> new ItemSyringe(new Item.Properties().stacksTo(16), LegacySyringeItem.Kind.MKUNICORN));
    public static final RegistryObject<Item> BOTTLE_EMPTY = registerLegacy("bottle_empty",
            () -> new Item(new Item.Properties()));
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
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.NUKA,
                    BOTTLE_EMPTY, CAP_NUKA));
    public static final RegistryObject<Item> FLASK_INFUSION = registerLegacy("flask_infusion",
            () -> new FlaskInfusionItem(new Item.Properties()));
    public static final RegistryObject<Item> BOTTLE_CHERRY = registerLegacy("bottle_cherry",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.CHERRY,
                    BOTTLE_EMPTY, CAP_NUKA));
    public static final RegistryObject<Item> BOTTLE_QUANTUM = registerLegacy("bottle_quantum",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.QUANTUM,
                    BOTTLE_EMPTY, CAP_QUANTUM));
    public static final RegistryObject<Item> BOTTLE_SPARKLE = registerLegacy("bottle_sparkle",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.SPARKLE,
                    BOTTLE_EMPTY, CAP_SPARKLE));
    public static final RegistryObject<Item> BOTTLE_RAD = registerLegacy("bottle_rad",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.RAD,
                    BOTTLE_EMPTY, CAP_RAD));
    public static final RegistryObject<Item> BOTTLE2_KORL = registerLegacy("bottle2_korl",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.KORL,
                    BOTTLE2_EMPTY, CAP_KORL));
    public static final RegistryObject<Item> BOTTLE2_FRITZ = registerLegacy("bottle2_fritz",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.FRITZ,
                    BOTTLE2_EMPTY, CAP_FRITZ));
    public static final RegistryObject<Item> BOTTLE_OPENER = registerLegacy("bottle_opener",
            () -> new LegacyBottleOpenerItem(new Item.Properties().stacksTo(1).durability(250)));
    public static final RegistryObject<Item> CAN_EMPTY = registerLegacy("can_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RING_PULL = registerLegacy("ring_pull",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAN_SMART = registerLegacy("can_smart",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.SMART,
                    CAN_EMPTY, RING_PULL, false));
    public static final RegistryObject<Item> CAN_CREATURE = registerLegacy("can_creature",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.CREATURE,
                    CAN_EMPTY, RING_PULL, false));
    public static final RegistryObject<Item> CAN_REDBOMB = registerLegacy("can_redbomb",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.REDBOMB,
                    CAN_EMPTY, RING_PULL, false));
    public static final RegistryObject<Item> CAN_MRSUGAR = registerLegacy("can_mrsugar",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.MRSUGAR,
                    CAN_EMPTY, RING_PULL, false));
    public static final RegistryObject<Item> CAN_OVERCHARGE = registerLegacy("can_overcharge",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.OVERCHARGE,
                    CAN_EMPTY, RING_PULL, false));
    public static final RegistryObject<Item> CAN_BEPIS = registerLegacy("can_bepis",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.BEPIS,
                    CAN_EMPTY, RING_PULL, false));
    public static final RegistryObject<Item> CAN_LUNA = registerLegacy("can_luna",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.LUNA,
                    CAN_EMPTY, RING_PULL, false));
    public static final RegistryObject<Item> CAN_MUG = registerLegacy("can_mug",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.MUG,
                    CAN_EMPTY, RING_PULL, false));
    public static final RegistryObject<Item> CAN_BREEN = registerLegacy("can_breen",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.BREEN,
                    CAN_EMPTY, RING_PULL, false));
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
    public static final RegistryObject<Item> NUGGET = registerLegacy("nugget",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(200)
                    .saturationMod(1.0F)
                    .build())));
    public static final RegistryObject<Item> BOMB_WAFFLE = registerLegacy("bomb_waffle",
            () -> new LegacyBombWaffleItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(20)
                    .saturationMod(0.6F)
                    .build())));
    public static final RegistryObject<Item> PANCAKE = registerLegacy("pancake",
            () -> new LegacyPancakeItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(20)
                    .saturationMod(20.0F)
                    .alwaysEat()
                    .build())));
    public static final RegistryObject<Item> SCHNITZEL_VEGAN = registerLegacy("schnitzel_vegan",
            () -> new LegacyRadiationFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(0)
                    .saturationMod(0.6F)
                    .meat()
                    .build()), LegacyRadiationFoodItem.Kind.SCHNITZEL_VEGAN));
    public static final RegistryObject<Item> COTTON_CANDY = registerLegacy("cotton_candy",
            () -> new LegacyRadiationFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(5)
                    .saturationMod(0.6F)
                    .alwaysEat()
                    .build()), LegacyRadiationFoodItem.Kind.COTTON_CANDY));
    public static final RegistryObject<Item> APPLE_LEAD = registerLegacy("apple_lead",
            () -> new LegacyRadiationFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(5)
                    .saturationMod(0.0F)
                    .alwaysEat()
                    .build()), LegacyRadiationFoodItem.Kind.LEAD_NUGGET));
    public static final RegistryObject<Item> APPLE_LEAD_INGOT = registerLegacy("apple_lead_ingot",
            () -> new LegacyRadiationFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(5)
                    .saturationMod(0.0F)
                    .alwaysEat()
                    .build()), LegacyRadiationFoodItem.Kind.LEAD_INGOT));
    public static final RegistryObject<Item> APPLE_LEAD_BLOCK = registerLegacy("apple_lead_block",
            () -> new LegacyRadiationFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(5)
                    .saturationMod(0.0F)
                    .alwaysEat()
                    .build()), LegacyRadiationFoodItem.Kind.LEAD_BLOCK));
    public static final RegistryObject<Item> APPLE_SCHRABIDIUM = registerLegacy("apple_schrabidium",
            () -> new LegacyRadiationFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(20)
                    .saturationMod(100.0F)
                    .alwaysEat()
                    .build()), LegacyRadiationFoodItem.Kind.SCHRABIDIUM_NUGGET));
    public static final RegistryObject<Item> APPLE_SCHRABIDIUM_INGOT = registerLegacy("apple_schrabidium_ingot",
            () -> new LegacyRadiationFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(20)
                    .saturationMod(100.0F)
                    .alwaysEat()
                    .build()), LegacyRadiationFoodItem.Kind.SCHRABIDIUM_INGOT));
    public static final RegistryObject<Item> APPLE_SCHRABIDIUM_BLOCK = registerLegacy("apple_schrabidium_block",
            () -> new LegacyRadiationFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(20)
                    .saturationMod(100.0F)
                    .alwaysEat()
                    .build()), LegacyRadiationFoodItem.Kind.SCHRABIDIUM_BLOCK));
    public static final RegistryObject<Item> APPLE_EUPHEMIUM = registerLegacy("apple_euphemium",
            () -> new LegacyRadiationFoodItem(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder()
                    .nutrition(20)
                    .saturationMod(100.0F)
                    .alwaysEat()
                    .build()), LegacyRadiationFoodItem.Kind.EUPHEMIUM));
    public static final RegistryObject<Item> COFFEE = registerLegacy("coffee",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.COFFEE));
    public static final RegistryObject<Item> COFFEE_RADIUM = registerLegacy("coffee_radium",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.COFFEE_RADIUM));
    public static final RegistryObject<Item> CHOCOLATE_MILK = registerLegacy("chocolate_milk",
            () -> new LegacyEnergyDrinkItem(new Item.Properties(), LegacyEnergyDrinkItem.Kind.CHOCOLATE_MILK));
    public static final RegistryObject<Item> LEMON = registerLegacy("lemon",
            () -> new LegacyFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationMod(0.5F)
                    .build()), LegacyFoodItem.Kind.LEMON));
    public static final RegistryObject<Item> PUDDING = registerLegacy("pudding",
            () -> new LegacyFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationMod(1.0F)
                    .build()), LegacyFoodItem.Kind.PUDDING));
    public static final RegistryObject<Item> SPONGEBOB_MACARONI = registerLegacy("spongebob_macaroni",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(5)
                    .saturationMod(1.0F)
                    .build())));
    public static final RegistryObject<Item> STATIC_SANDWICH = registerLegacy("static_sandwich",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationMod(1.0F)
                    .build())));
    public static final RegistryObject<Item> CHEESE = registerLegacy("cheese",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(5)
                    .saturationMod(0.75F)
                    .build())));
    public static final RegistryObject<Item> QUESADILLA = registerLegacy("quesadilla",
            () -> new LegacyFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8)
                    .saturationMod(1.0F)
                    .build()), LegacyFoodItem.Kind.QUESADILLA));
    public static final RegistryObject<Item> MUCHO_MANGO = registerLegacy("mucho_mango",
            () -> new LegacyFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(10)
                    .saturationMod(0.6F)
                    .alwaysEat()
                    .meat()
                    .build()), LegacyFoodItem.Kind.MUCHO_MANGO));
    public static final RegistryObject<Item> MED_IPECAC = registerLegacy("med_ipecac",
            () -> new LegacyFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(0)
                    .saturationMod(0.0F)
                    .alwaysEat()
                    .build()), LegacyFoodItem.Kind.MED_IPECAC));
    public static final RegistryObject<Item> MED_PTSD = registerLegacy("med_ptsd",
            () -> new LegacyFoodItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(0)
                    .saturationMod(0.0F)
                    .alwaysEat()
                    .build()), LegacyFoodItem.Kind.MED_PTSD));
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
    public static final RegistryObject<Item> EGG_GLYPHID = registerLegacy("egg_glyphid",
            () -> new Item(new Item.Properties()));
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
            () -> new ItemModGasmask(false));
    public static final RegistryObject<Item> ATTACHMENT_MASK_MONO = registerLegacy("attachment_mask_mono",
            () -> new ItemModGasmask(true));
    public static final RegistryObject<Item> GOGGLES = objIronHeadArmor("goggles");
    public static final RegistryObject<Item> ASHGLASSES = ashGlassesArmor("ashglasses");
    public static final RegistryObject<Item> CAPE_RADIATION = registerLegacy("cape_radiation",
            () -> new ArmorCapeItem(ArmorMaterials.CHAIN, new Item.Properties()));
    public static final RegistryObject<Item> CAPE_GASMASK = registerLegacy("cape_gasmask",
            () -> new ArmorCapeItem(ArmorMaterials.CHAIN, new Item.Properties()));
    public static final RegistryObject<Item> CAPE_SCHRABIDIUM = registerLegacy("cape_schrabidium",
            () -> new ArmorCapeItem(HbmArmorMaterials.SCHRABIDIUM, new Item.Properties()));
    public static final RegistryObject<Item> CAPE_HIDDEN = registerLegacy("cape_hidden",
            () -> new ArmorCapeItem(ArmorMaterials.CHAIN, new Item.Properties()));
    public static final RegistryObject<Item> HAT = registerLegacy("nossy_hat",
            () -> new ArmorHat(HbmArmorMaterials.ALLOY, new Item.Properties()));
    public static final RegistryObject<Item> NO9 = registerLegacy("no9",
            () -> new ArmorNo9(new Item.Properties()));
    public static final RegistryObject<Item> BETA = registerLegacy("beta",
            () -> new VanishOnDropItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GAS_MASK = gasMaskArmor("gas_mask", false);
    public static final RegistryObject<Item> GAS_MASK_M65 = gasMaskArmor("gas_mask_m65", false);
    public static final RegistryObject<Item> GAS_MASK_MONO = gasMaskArmor("gas_mask_mono", true);
    public static final RegistryObject<Item> GAS_MASK_OLDE = gasMaskArmor("gas_mask_olde", false);
    public static final RegistryObject<Item> MASK_OF_INFAMY = registerLegacy("mask_of_infamy", MaskOfInfamy::new);
    public static final RegistryObject<Item> MASK_RAG = modArmor("mask_rag", HbmArmorMaterials.RAGS,
            ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> MASK_PISS = modArmor("mask_piss", HbmArmorMaterials.RAGS,
            ArmorItem.Type.HELMET);
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
    public static final RegistryObject<Item> JACKET = modArmor("jackt", HbmArmorMaterials.JACKET,
            ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> JACKET2 = modArmor("jackt2", HbmArmorMaterials.JACKET2,
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
    public static final RegistryObject<Item> HAZMAT_PAA_PLATE = hazmatArmor("hazmat_paa_plate",
            HbmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> HAZMAT_PAA_LEGS = hazmatArmor("hazmat_paa_legs",
            HbmArmorMaterials.HAZMAT_PAA, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> HAZMAT_PAA_BOOTS = hazmatArmor("hazmat_paa_boots",
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
    public static final RegistryObject<Item> BISMUTH_HELMET = bismuthArmor("bismuth_helmet", HbmArmorMaterials.BISMUTH,
            ArmorItem.Type.HELMET, bismuthEffects(), false, 3);
    public static final RegistryObject<Item> BISMUTH_PLATE = bismuthArmor("bismuth_plate", HbmArmorMaterials.BISMUTH,
            ArmorItem.Type.CHESTPLATE, bismuthEffects(), false, 3);
    public static final RegistryObject<Item> BISMUTH_LEGS = bismuthArmor("bismuth_legs", HbmArmorMaterials.BISMUTH,
            ArmorItem.Type.LEGGINGS, bismuthEffects(), false, 3);
    public static final RegistryObject<Item> BISMUTH_BOOTS = bismuthArmor("bismuth_boots", HbmArmorMaterials.BISMUTH,
            ArmorItem.Type.BOOTS, bismuthEffects(), false, 3);
    public static final RegistryObject<Item> T51_HELMET = t51Armor("t51_helmet", HbmArmorMaterials.T51,
            ArmorItem.Type.HELMET, t51Effects(), 1_000_000L, 10_000L, 1_000L, 5L, t51Traits());
    public static final RegistryObject<Item> T51_PLATE = t51Armor("t51_plate", HbmArmorMaterials.T51,
            ArmorItem.Type.CHESTPLATE, t51Effects(), 1_000_000L, 10_000L, 1_000L, 5L, t51Traits());
    public static final RegistryObject<Item> T51_LEGS = t51Armor("t51_legs", HbmArmorMaterials.T51,
            ArmorItem.Type.LEGGINGS, t51Effects(), 1_000_000L, 10_000L, 1_000L, 5L, t51Traits());
    public static final RegistryObject<Item> T51_BOOTS = t51Armor("t51_boots", HbmArmorMaterials.T51,
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
    public static final RegistryObject<Item> AJR_HELMET = ajrArmor("ajr_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJR_PLATE = ajrArmor("ajr_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJR_LEGS = ajrArmor("ajr_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJR_BOOTS = ajrArmor("ajr_boots", HbmArmorMaterials.AJR,
            ArmorItem.Type.BOOTS, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJRO_HELMET = ajroArmor("ajro_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJRO_PLATE = ajroArmor("ajro_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJRO_LEGS = ajroArmor("ajro_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> AJRO_BOOTS = ajroArmor("ajro_boots", HbmArmorMaterials.AJR,
            ArmorItem.Type.BOOTS, ajrEffects(), 2_500_000L, 10_000L, 2_000L, 25L, t51Traits());
    public static final RegistryObject<Item> RPA_HELMET = rpaArmor("rpa_helmet", HbmArmorMaterials.AJR,
            ArmorItem.Type.HELMET, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> RPA_PLATE = rpaArmor("rpa_plate", HbmArmorMaterials.AJR,
            ArmorItem.Type.CHESTPLATE, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> RPA_LEGS = rpaArmor("rpa_legs", HbmArmorMaterials.AJR,
            ArmorItem.Type.LEGGINGS, rpaEffects(), 2_500_000L, 10_000L, 2_000L, 25L, poweredStepTraits());
    public static final RegistryObject<Item> RPA_BOOTS = rpaArmor("rpa_boots", HbmArmorMaterials.AJR,
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
    public static final RegistryObject<Item> HEV_HELMET = hevArmor("hev_helmet", HbmArmorMaterials.HEV,
            ArmorItem.Type.HELMET, envEffects(), 1_000_000L, 10_000L, 2_500L, 0L, hevTraits());
    public static final RegistryObject<Item> HEV_PLATE = hevArmor("hev_plate", HbmArmorMaterials.HEV,
            ArmorItem.Type.CHESTPLATE, envEffects(), 1_000_000L, 10_000L, 2_500L, 0L, hevTraits());
    public static final RegistryObject<Item> HEV_LEGS = hevArmor("hev_legs", HbmArmorMaterials.HEV,
            ArmorItem.Type.LEGGINGS, envEffects(), 1_000_000L, 10_000L, 2_500L, 0L, hevTraits());
    public static final RegistryObject<Item> HEV_BOOTS = hevArmor("hev_boots", HbmArmorMaterials.HEV,
            ArmorItem.Type.BOOTS, envEffects(), 1_000_000L, 10_000L, 2_500L, 0L, hevTraits());
    public static final RegistryObject<Item> FAU_HELMET = fauArmor("fau_helmet", HbmArmorMaterials.FAU,
            ArmorItem.Type.HELMET, fauEffects(), 10_000_000L, 10_000L, 2_500L, 0L, fauTraits());
    public static final RegistryObject<Item> FAU_PLATE = fauArmor("fau_plate", HbmArmorMaterials.FAU,
            ArmorItem.Type.CHESTPLATE, fauEffects(), 10_000_000L, 10_000L, 2_500L, 0L, fauTraits());
    public static final RegistryObject<Item> FAU_LEGS = fauArmor("fau_legs", HbmArmorMaterials.FAU,
            ArmorItem.Type.LEGGINGS, fauEffects(), 10_000_000L, 10_000L, 2_500L, 0L, fauTraits());
    public static final RegistryObject<Item> FAU_BOOTS = fauArmor("fau_boots", HbmArmorMaterials.FAU,
            ArmorItem.Type.BOOTS, fauEffects(), 10_000_000L, 10_000L, 2_500L, 0L, fauTraits());
    public static final RegistryObject<Item> DNS_HELMET = dnsArmor("dns_helmet", HbmArmorMaterials.DNS,
            ArmorItem.Type.HELMET, dnsEffects(), 1_000_000_000L, 1_000_000L, 100_000L, 115L, dnsTraits());
    public static final RegistryObject<Item> DNS_PLATE = dnsArmor("dns_plate", HbmArmorMaterials.DNS,
            ArmorItem.Type.CHESTPLATE, dnsEffects(), 1_000_000_000L, 1_000_000L, 100_000L, 115L, dnsTraits());
    public static final RegistryObject<Item> DNS_LEGS = dnsArmor("dns_legs", HbmArmorMaterials.DNS,
            ArmorItem.Type.LEGGINGS, dnsEffects(), 1_000_000_000L, 1_000_000L, 100_000L, 115L, dnsTraits());
    public static final RegistryObject<Item> DNS_BOOTS = dnsArmor("dns_boots", HbmArmorMaterials.DNS,
            ArmorItem.Type.BOOTS, dnsEffects(), 1_000_000_000L, 1_000_000L, 100_000L, 115L, dnsTraits());
    public static final RegistryObject<Item> TAURUN_HELMET = taurunArmor("taurun_helmet", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.HELMET, taurunEffects(), false, 0, stepSizeTraits());
    public static final RegistryObject<Item> TAURUN_PLATE = taurunArmor("taurun_plate", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.CHESTPLATE, taurunEffects(), false, 0, stepSizeTraits());
    public static final RegistryObject<Item> TAURUN_LEGS = taurunArmor("taurun_legs", HbmArmorMaterials.TAURUN,
            ArmorItem.Type.LEGGINGS, taurunEffects(), false, 0, stepSizeTraits());
    public static final RegistryObject<Item> TAURUN_BOOTS = taurunArmor("taurun_boots", HbmArmorMaterials.TAURUN,
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
            () -> new ItemModPads(0.5F, false));
    public static final RegistryObject<Item> PADS_SLIME = registerLegacy("pads_slime",
            () -> new ItemModPads(0.25F, false));
    public static final RegistryObject<Item> PADS_STATIC = registerLegacy("pads_static",
            () -> new ItemModPads(0.75F, true));
    public static final RegistryObject<Item> CLADDING_PAINT = registerLegacy("cladding_paint",
            () -> new ItemModCladding(0.025D));
    public static final RegistryObject<Item> CLADDING_RUBBER = registerLegacy("cladding_rubber",
            () -> new ItemModCladding(0.005D));
    public static final RegistryObject<Item> CLADDING_LEAD = registerLegacy("cladding_lead",
            () -> new ItemModCladding(0.1D));
    public static final RegistryObject<Item> CLADDING_DESH = registerLegacy("cladding_desh",
            () -> new ItemModCladding(0.2D));
    public static final RegistryObject<Item> CLADDING_GHIORSIUM = registerLegacy("cladding_ghiorsium",
            () -> new ItemModCladding(0.5D));
    public static final RegistryObject<Item> CLADDING_IRON = registerLegacy("cladding_iron",
            ItemModIron::new);
    public static final RegistryObject<Item> CLADDING_OBSIDIAN = registerLegacy("cladding_obsidian",
            ItemModObsidian::new);
    public static final RegistryObject<Item> INSERT_KEVLAR = registerLegacy("insert_kevlar",
            () -> new ItemModInsert(1500, 1.0F, 0.9F, 1.0F, 1.0F));
    public static final RegistryObject<Item> INSERT_SAPI = registerLegacy("insert_sapi",
            () -> new ItemModInsert(1750, 1.0F, 0.85F, 1.0F, 1.0F));
    public static final RegistryObject<Item> INSERT_ESAPI = registerLegacy("insert_esapi",
            () -> new ItemModInsert(2000, 0.95F, 0.8F, 1.0F, 1.0F));
    public static final RegistryObject<Item> INSERT_XSAPI = registerLegacy("insert_xsapi",
            () -> new ItemModInsert(2500, 0.9F, 0.75F, 1.0F, 1.0F));
    public static final RegistryObject<Item> INSERT_STEEL = registerLegacy("insert_steel",
            () -> new ItemModInsert(1000, 1.0F, 0.95F, 0.75F, 0.95F));
    public static final RegistryObject<Item> INSERT_DU = registerLegacy("insert_du",
            () -> new ItemModInsert(1500, 0.9F, 0.85F, 0.5F, 0.9F));
    public static final RegistryObject<Item> INSERT_POLONIUM = registerLegacy("insert_polonium",
            () -> new ItemModInsert(500, 0.9F, 1.0F, 0.95F, 0.9F));
    public static final RegistryObject<Item> INSERT_GHIORSIUM = registerLegacy("insert_ghiorsium",
            () -> new ItemModInsert(2000, 0.8F, 0.75F, 0.35F, 0.9F));
    public static final RegistryObject<Item> INSERT_ERA = registerLegacy("insert_era",
            () -> new ItemModInsert(25, 0.5F, 1.0F, 0.25F, 1.0F));
    public static final RegistryObject<Item> INSERT_YHARONITE = registerLegacy("insert_yharonite",
            () -> new ItemModInsert(9999, 0.01F, 1.0F, 1.0F, 1.0F));
    public static final RegistryObject<Item> INSERT_DOXIUM = registerLegacy("insert_doxium",
            () -> new ItemModInsert(9999, 5.0F, 1.0F, 1.0F, 1.0F));
    public static final RegistryObject<Item> SERVO_SET = registerLegacy("servo_set",
            () -> new ItemModServos(false));
    public static final RegistryObject<Item> SERVO_SET_DESH = registerLegacy("servo_set_desh",
            () -> new ItemModServos(true));
    public static final RegistryObject<Item> HEART_PIECE = registerLegacy("heart_piece",
            () -> new ItemModHealth(5.0F));
    public static final RegistryObject<Item> HEART_CONTAINER = registerLegacy("heart_container",
            () -> new ItemModHealth(20.0F));
    public static final RegistryObject<Item> HEART_BOOSTER = registerLegacy("heart_booster",
            () -> new ItemModHealth(40.0F));
    public static final RegistryObject<Item> HEART_FAB = registerLegacy("heart_fab",
            () -> new ItemModHealth(60.0F));
    public static final RegistryObject<Item> BLACK_DIAMOND = registerLegacy("black_diamond",
            () -> new ItemModHealth(40.0F));
    public static final RegistryObject<Item> ITEM_SECRET_SELENIUM_STEEL = simpleItem("item_secret_selenium_steel");
    public static final RegistryObject<Item> WD40 = registerLegacy("wd40",
            ItemModWD40::new);
    public static final RegistryObject<Item> BOTTLED_CLOUD = registerLegacy("bottled_cloud",
            ItemModCloud::new);
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
            WingsMurk::new);
    public static final RegistryObject<Item> AUSTRALIUM_III = registerLegacy("australium_iii",
            () -> new ItemModShield(25.0F));
    public static final RegistryObject<Item> ARMOR_POLISH = registerLegacy("armor_polish",
            ItemModPolish::new);
    public static final RegistryObject<Item> BANDAID = registerLegacy("bandaid",
            ItemModBandaid::new);
    public static final RegistryObject<Item> SERUM = registerLegacy("serum",
            ItemModSerum::new);
    public static final RegistryObject<Item> QUARTZ_PLUTONIUM = registerLegacy("quartz_plutonium",
            ItemModQuartz::new);
    public static final RegistryObject<Item> MORNING_GLORY = registerLegacy("morning_glory",
            ItemModMorningGlory::new);
    public static final RegistryObject<Item> LODESTONE = registerLegacy("lodestone",
            () -> new ItemModLodestone(5));
    public static final RegistryObject<Item> HORSESHOE_MAGNET = registerLegacy("horseshoe_magnet",
            () -> new ItemModLodestone(8));
    public static final RegistryObject<Item> INDUSTRIAL_MAGNET = registerLegacy("industrial_magnet",
            () -> new ItemModLodestone(12));
    public static final RegistryObject<Item> BATHWATER = registerLegacy("bathwater",
            () -> new ItemModBathwater(false));
    public static final RegistryObject<Item> BATHWATER_MK2 = registerLegacy("bathwater_mk2",
            () -> new ItemModBathwater(true));
    public static final RegistryObject<Item> SPIDER_MILK = registerLegacy("spider_milk",
            ItemModMilk::new);
    public static final RegistryObject<Item> INK = registerLegacy("ink",
            ItemModInk::new);
    public static final RegistryObject<Item> INJECTOR_5HTP = registerLegacy("injector_5htp",
            ItemModAuto::new);
    public static final RegistryObject<Item> INJECTOR_KNIFE = registerLegacy("injector_knife",
            ItemModKnife::new);
    public static final RegistryObject<Item> DEFUSER_GOLD = registerLegacy("defuser_gold",
            ItemModDefuser::new);
    public static final RegistryObject<Item> NEUTRINO_LENS = registerLegacy("neutrino_lens",
            ItemModLens::new);
    public static final RegistryObject<Item> NIGHT_VISION = registerLegacy("night_vision",
            ItemModNightVision::new);
    public static final RegistryObject<Item> BACK_TESLA = registerLegacy("back_tesla", ItemModTesla::new);
    public static final RegistryObject<Item> MEDAL_LIQUIDATOR = registerLegacy("medal_liquidator",
            ItemModMedal::new);
    public static final RegistryObject<Item> BALLISTIC_GAUNTLET = registerLegacy("ballistic_gauntlet",
            ItemModTwoKick::new);
    public static final RegistryObject<Item> CARD_AOS = registerLegacy("card_aos",
            ItemModCard::new);
    public static final RegistryObject<Item> CARD_QOS = registerLegacy("card_qos",
            ItemModCard::new);
    public static final RegistryObject<Item> PROTECTION_CHARM = registerLegacy("protection_charm",
            ItemModCharm::new);
    public static final RegistryObject<Item> METEOR_CHARM = registerLegacy("meteor_charm",
            () -> new ItemModCharm(true));
    public static final RegistryObject<Item> GAS_TESTER = registerLegacy("gas_tester",
            ItemModSensor::new);
    public static final RegistryObject<Item> ARMOR_BATTERY = registerLegacy("armor_battery",
            () -> new ItemModBattery(1.25D));
    public static final RegistryObject<Item> ARMOR_BATTERY_MK2 = registerLegacy("armor_battery_mk2",
            () -> new ItemModBattery(1.5D));
    public static final RegistryObject<Item> ARMOR_BATTERY_MK3 = registerLegacy("armor_battery_mk3",
            () -> new ItemModBattery(2.0D));
    public static final RegistryObject<Item> SCRUMPY = registerLegacy("scrumpy",
            () -> new ItemModRevive(1));
    public static final RegistryObject<Item> WILD_P = registerLegacy("wild_p",
            () -> new ItemModRevive(3));
    public static final RegistryObject<Item> SHACKLES = registerLegacy("shackles",
            ItemModShackles::new);
    public static final RegistryObject<Item> CONTAINMENT_BOX = registerLegacy("containment_box",
            () -> new ContainmentBoxItem(new Item.Properties()));
    public static final RegistryObject<Item> PLASTIC_BAG = registerLegacy("plastic_bag",
            () -> new PlasticBagItem(new Item.Properties()));
    public static final RegistryObject<Item> KIT_CUSTOM = registerLegacy("kit_custom",
            () -> new LegacyCustomKitItem(new Item.Properties()));
    public static final RegistryObject<Item> TOOLBOX = registerLegacy("toolbox", () -> new ToolboxItem(new Item.Properties()));
    public static final RegistryObject<Item> SIREN_TRACK = registerLegacy("siren_track",
            () -> new SirenCassetteItem(new Item.Properties()));
    public static final RegistryObject<Item> SETTINGS_TOOL = registerLegacy("settings_tool",
            () -> new SettingsToolItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MIRROR_TOOL = registerLegacy("mirror_tool",
            () -> new MirrorToolItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MATCHSTICK = registerLegacy("matchstick",
            () -> new MatchstickItem(new Item.Properties()));
    public static final RegistryObject<Item> SCREWDRIVER = registerLegacy("screwdriver",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(100), Toolable.ToolType.SCREWDRIVER));
    public static final RegistryObject<Item> SCREWDRIVER_DESH = registerLegacy("screwdriver_desh",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1), Toolable.ToolType.SCREWDRIVER));
    public static final RegistryObject<Item> HAND_DRILL = registerLegacy("hand_drill",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(100), Toolable.ToolType.HAND_DRILL));
    public static final RegistryObject<Item> HAND_DRILL_DESH = registerLegacy("hand_drill_desh",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1), Toolable.ToolType.HAND_DRILL));
    public static final RegistryObject<Item> WRENCH = registerLegacy("wrench",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(750), Toolable.ToolType.WRENCH));
    public static final RegistryObject<Item> WRENCH_ARCHINEER = registerLegacy("wrench_archineer",
            () -> new LegacyToolWeaponItem(new Item.Properties().stacksTo(1).durability(1_000),
                    Toolable.ToolType.WRENCH, 12.0F));
    public static final RegistryObject<Item> BLOWTORCH = registerLegacy("blowtorch",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(4000), Toolable.ToolType.TORCH));
    public static final RegistryObject<Item> ACETYLENE_TORCH = registerLegacy("acetylene_torch",
            () -> new LegacyToolItem(new Item.Properties().stacksTo(1).durability(4000), Toolable.ToolType.TORCH));
    public static final RegistryObject<Item> BOLTGUN = registerLegacy("boltgun",
            () -> new LegacyBoltgunItem(new Item.Properties().stacksTo(1).durability(750)));
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
    public static final RegistryObject<Item> RUNE_BLANK = registerLegacy("rune_blank",
            () -> new ItemCustomLore(new Item.Properties().stacksTo(1)).setEffect());
    public static final RegistryObject<Item> RUNE_ISA = registerLegacy("rune_isa",
            () -> new ItemCustomLore(new Item.Properties().stacksTo(1)).setEffect());
    public static final RegistryObject<Item> RUNE_DAGAZ = registerLegacy("rune_dagaz",
            () -> new ItemCustomLore(new Item.Properties().stacksTo(1)).setEffect());
    public static final RegistryObject<Item> RUNE_HAGALAZ = registerLegacy("rune_hagalaz",
            () -> new ItemCustomLore(new Item.Properties().stacksTo(1)).setEffect());
    public static final RegistryObject<Item> RUNE_JERA = registerLegacy("rune_jera",
            () -> new ItemCustomLore(new Item.Properties().stacksTo(1)).setEffect());
    public static final RegistryObject<Item> RUNE_THURISAZ = registerLegacy("rune_thurisaz",
            () -> new ItemCustomLore(new Item.Properties().stacksTo(1)).setEffect());
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
    public static final RegistryObject<Item> SCHRABIDIUM_HOE = hoe("schrabidium_hoe", HbmToolTiers.SCHRABIDIUM);
    public static final RegistryObject<Item> SCHRABIDIUM_HAMMER = registerLegacy("schrabidium_hammer",
            () -> new SchrabidiumHammerItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SHIMMER_SLEDGE = registerLegacy("shimmer_sledge",
            () -> new LegacyShimmerWeaponItem(LegacyShimmerWeaponItem.Kind.SLEDGE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SHIMMER_AXE = registerLegacy("shimmer_axe",
            () -> new LegacyShimmerWeaponItem(LegacyShimmerWeaponItem.Kind.AXE,
                    new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> STOPSIGN = registerLegacy("stopsign",
            () -> new LegacySignWeaponItem(LegacySignWeaponItem.Variant.STOP,
                    new Item.Properties().stacksTo(1).durability(HbmToolTiers.ALLOY.getUses())));
    public static final RegistryObject<Item> SOPSIGN = registerLegacy("sopsign",
            () -> new LegacySignWeaponItem(LegacySignWeaponItem.Variant.SOP,
                    new Item.Properties().stacksTo(1).durability(HbmToolTiers.ALLOY.getUses())));
    public static final RegistryObject<Item> CHERNOBYLSIGN = registerLegacy("chernobylsign",
            () -> new LegacySignWeaponItem(LegacySignWeaponItem.Variant.CHERNOBYL,
                    new Item.Properties().stacksTo(1).durability(HbmToolTiers.ALLOY.getUses())));
    public static final RegistryObject<Item> TITANIUM_SWORD = abilitySword("titanium_sword", HbmToolTiers.TITANIUM, 6.5F, 0.0D, false, item -> { });
    public static final RegistryObject<Item> TITANIUM_PICKAXE = abilityPickaxe("titanium_pickaxe", 4.5F, 0.0D, HbmToolTiers.TITANIUM, false, item -> { });
    public static final RegistryObject<Item> TITANIUM_AXE = abilityAxe("titanium_axe", 5.5F, 0.0D, HbmToolTiers.TITANIUM, false,
            item -> item.addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> TITANIUM_SHOVEL = abilityShovel("titanium_shovel", 3.5F, 0.0D, HbmToolTiers.TITANIUM, false, item -> { });
    public static final RegistryObject<Item> TITANIUM_HOE = hoe("titanium_hoe", HbmToolTiers.TITANIUM);
    public static final RegistryObject<Item> STEEL_SWORD = abilitySword("steel_sword", HbmToolTiers.STEEL, 6.0F, 0.0D, false,
            item -> item.addAbility(WeaponAbilities.STUN, 0));
    public static final RegistryObject<Item> STEEL_PICKAXE = abilityPickaxe("steel_pickaxe", 4.0F, 0.0D, HbmToolTiers.STEEL, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 0));
    public static final RegistryObject<Item> STEEL_AXE = abilityAxe("steel_axe", 5.0F, 0.0D, HbmToolTiers.STEEL, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 0)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> STEEL_SHOVEL = abilityShovel("steel_shovel", 3.0F, 0.0D, HbmToolTiers.STEEL, false,
            item -> item.addAbility(ToolAreaAbilities.RECURSION, 0));
    public static final RegistryObject<Item> STEEL_HOE = hoe("steel_hoe", HbmToolTiers.STEEL);
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
    public static final RegistryObject<Item> CMB_HOE = hoe("cmb_hoe", HbmToolTiers.CMB);
    public static final RegistryObject<Item> DESH_SWORD = abilitySword("desh_sword", HbmToolTiers.DESH, 12.5F, -0.05D, false,
            item -> item.addAbility(WeaponAbilities.STUN, 0));
    public static final RegistryObject<Item> DESH_PICKAXE = abilityPickaxe("desh_pickaxe", 5.0F, -0.05D, HbmToolTiers.DESH, false,
            item -> deshToolAbilities(item));
    public static final RegistryObject<Item> DESH_AXE = abilityAxe("desh_axe", 7.5F, -0.05D, HbmToolTiers.DESH, false,
            item -> deshToolAbilities(item)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> DESH_SHOVEL = abilityShovel("desh_shovel", 4.0F, -0.05D, HbmToolTiers.DESH, false,
            item -> deshToolAbilities(item));
    public static final RegistryObject<Item> DESH_HOE = hoe("desh_hoe", HbmToolTiers.DESH);
    public static final RegistryObject<Item> COBALT_SWORD = abilitySword("cobalt_sword", HbmToolTiers.COBALT, 12.0F, 0.0D, false, item -> { });
    public static final RegistryObject<Item> COBALT_PICKAXE = abilityPickaxe("cobalt_pickaxe", 4.0F, 0.0D, HbmToolTiers.COBALT, false,
            item -> cobaltToolAbilities(item));
    public static final RegistryObject<Item> COBALT_AXE = abilityAxe("cobalt_axe", 6.0F, 0.0D, HbmToolTiers.COBALT, false,
            item -> cobaltToolAbilities(item)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> COBALT_SHOVEL = abilityShovel("cobalt_shovel", 3.5F, 0.0D, HbmToolTiers.COBALT, false,
            item -> cobaltToolAbilities(item));
    public static final RegistryObject<Item> COBALT_HOE = hoe("cobalt_hoe", HbmToolTiers.COBALT);
    public static final RegistryObject<Item> COBALT_DECORATED_SWORD = abilitySword("cobalt_decorated_sword", HbmToolTiers.COBALT_DECORATED,
            15.0F, 0.0D, false, item -> item.addAbility(WeaponAbilities.BOBBLE, 0));
    public static final RegistryObject<Item> COBALT_DECORATED_PICKAXE = abilityPickaxe("cobalt_decorated_pickaxe", 6.0F, 0.0D,
            HbmToolTiers.COBALT_DECORATED, false, item -> cobaltDecoratedToolAbilities(item));
    public static final RegistryObject<Item> COBALT_DECORATED_AXE = abilityAxe("cobalt_decorated_axe", 8.0F, 0.0D,
            HbmToolTiers.COBALT_DECORATED, false, item -> cobaltDecoratedToolAbilities(item)
                    .addAbility(WeaponAbilities.BEHEADER, 0));
    public static final RegistryObject<Item> COBALT_DECORATED_SHOVEL = abilityShovel("cobalt_decorated_shovel", 5.0F, 0.0D,
            HbmToolTiers.COBALT_DECORATED, false, item -> cobaltDecoratedToolAbilities(item));
    public static final RegistryObject<Item> COBALT_DECORATED_HOE = hoe("cobalt_decorated_hoe",
            HbmToolTiers.COBALT_DECORATED);
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
    public static final RegistryObject<Item> STARMETAL_HOE = hoe("starmetal_hoe", HbmToolTiers.STARMETAL);
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
    public static final RegistryObject<Item> CROWBAR = abilitySword("crowbar", HbmToolTiers.STEEL, 6.0F, 0.0D,
            false, item -> { });
    public static final RegistryObject<Item> MEMESPOON = registerLegacy("memespoon",
            () -> new LegacyMemeSpoonItem(toolProperties(HbmToolTiers.STEEL, false)));
    public static final RegistryObject<Item> WOOD_GAVEL = registerLegacy("wood_gavel",
            () -> new LegacyGavelItem(Tiers.WOOD, 4.0F, false, "item.hbm_ntm_rebirth.wood_gavel.desc",
                    new Item.Properties().stacksTo(1).durability(Tiers.WOOD.getUses())));
    public static final RegistryObject<Item> LEAD_GAVEL = registerLegacy("lead_gavel",
            () -> new LegacyGavelItem(HbmToolTiers.STEEL, 6.0F, true, "item.hbm_ntm_rebirth.lead_gavel.desc",
                    toolProperties(HbmToolTiers.STEEL, false)));
    public static final RegistryObject<Item> PIPE_LEAD = registerLegacy("pipe_lead",
            () -> new LegacyPipeLeadItem(new Item.Properties().stacksTo(1).durability(250)));
    public static final RegistryObject<Item> REER_GRAAR = registerLegacy("reer_graar",
            () -> new LegacyReerGraarItem(toolProperties(HbmToolTiers.TITANIUM, false)));
    public static final RegistryObject<Item> WRENCH_FLIPPED = registerLegacy("wrench_flipped",
            () -> new LegacyWrenchFlippedItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ULLAPOOL_CABER = registerLegacy("ullapool_caber",
            () -> new LegacyUllapoolCaberItem(toolProperties(HbmToolTiers.STEEL, false).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> BOBMAZON = registerLegacy("bobmazon",
            () -> new BobmazonCatalogItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BOAT_RUBBER = registerLegacy("boat_rubber",
            () -> new RubberBoatItem(new Item.Properties()));
    public static final RegistryObject<Item> REBAR_PLACER = registerLegacy("rebar_placer",
            () -> new RebarPlacerItem(new Item.Properties()));
    public static final RegistryObject<Item> CART_EMPTY_WOOD = registerLegacy("cart_empty_wood",
            () -> new NtmMinecartItem(NtmMinecartBase.WOOD, new Item.Properties()));
    public static final RegistryObject<Item> CART_EMPTY_STEEL = registerLegacy("cart_empty_steel",
            () -> new NtmMinecartItem(NtmMinecartBase.STEEL, new Item.Properties()));
    public static final RegistryObject<Item> CART_EMPTY_PAINTED = registerLegacy("cart_empty_painted",
            () -> new NtmMinecartItem(NtmMinecartBase.PAINTED, new Item.Properties()));
    public static final RegistryObject<Item> CART_CRATE = registerLegacy("cart_crate",
            () -> new NtmMinecartItem(NtmMinecartBase.VANILLA, NtmMinecartType.CRATE, new Item.Properties()));
    // 1.7.10 ItemTrain was one metadata item, not two independent registry IDs.
    public static final RegistryObject<Item> TRAIN = registerLegacy("train",
            () -> new LegacyTrainItem(new Item.Properties()));
    public static final RegistryObject<Item> CART_POWDER_WOOD = registerLegacy("cart_powder_wood",
            () -> new NtmMinecartItem(NtmMinecartBase.WOOD, NtmMinecartType.POWDER, new Item.Properties()));
    public static final RegistryObject<Item> CART_POWDER_STEEL = registerLegacy("cart_powder_steel",
            () -> new NtmMinecartItem(NtmMinecartBase.STEEL, NtmMinecartType.POWDER, new Item.Properties()));
    public static final RegistryObject<Item> CART_POWDER_PAINTED = registerLegacy("cart_powder_painted",
            () -> new NtmMinecartItem(NtmMinecartBase.PAINTED, NtmMinecartType.POWDER, new Item.Properties()));
    public static final RegistryObject<Item> CART_SEMTEX_WOOD = registerLegacy("cart_semtex_wood",
            () -> new NtmMinecartItem(NtmMinecartBase.WOOD, NtmMinecartType.SEMTEX, new Item.Properties()));
    public static final RegistryObject<Item> CART_SEMTEX_STEEL = registerLegacy("cart_semtex_steel",
            () -> new NtmMinecartItem(NtmMinecartBase.STEEL, NtmMinecartType.SEMTEX, new Item.Properties()));
    public static final RegistryObject<Item> CART_SEMTEX_PAINTED = registerLegacy("cart_semtex_painted",
            () -> new NtmMinecartItem(NtmMinecartBase.PAINTED, NtmMinecartType.SEMTEX, new Item.Properties()));
    public static final RegistryObject<Item> CART_DESTROYER_STEEL = registerLegacy("cart_destroyer_steel",
            () -> new NtmMinecartItem(NtmMinecartBase.STEEL, NtmMinecartType.DESTROYER, new Item.Properties()));
    public static final RegistryObject<Item> CART_DESTROYER_PAINTED = registerLegacy("cart_destroyer_painted",
            () -> new NtmMinecartItem(NtmMinecartBase.PAINTED, NtmMinecartType.DESTROYER, new Item.Properties()));
    public static final RegistryObject<Item> CHAINSAW = registerLegacy("chainsaw", () ->
            new LegacyChainsawItem(25.0F, -0.05D, HbmToolTiers.ELEC, new Item.Properties(), 5_000, 1, 250,
                    HbmFluids.DIESEL,
                    HbmFluids.DIESEL_CRACK,
                    HbmFluids.KEROSENE,
                    HbmFluids.BIOFUEL,
                    HbmFluids.GASOLINE,
                    HbmFluids.GASOLINE_LEADED,
                    HbmFluids.PETROIL,
                    HbmFluids.PETROIL_LEADED,
                    HbmFluids.COALGAS,
                    HbmFluids.COALGAS_LEADED)
            .addAbility(ToolAreaAbilities.RECURSION, 2)
                    .addAbility(ToolHarvestAbilities.SILK, 0)
                    .addAbility(WeaponAbilities.CHAINSAW, 1)
                    .addAbility(WeaponAbilities.BEHEADER, 0)
                    .setShears());
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
    public static final RegistryObject<Item> BIO_WAFER = registerLegacy("bio_wafer",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationMod(2.0F)
                    .build())));
    public static final RegistryObject<Item> BURNT_BARK = registerLegacy("burnt_bark",
            () -> new LegacyLoreItem(new Item.Properties()));
    public static final RegistryObject<Item> CATALYTIC_CONVERTER = registerLegacy("catalytic_converter",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CBT_DEVICE = registerLegacy("cbt_device",
            () -> new CbtDeviceItem(new Item.Properties()));
    public static final RegistryObject<Item> DISPERSER_CANISTER_EMPTY = registerLegacy("disperser_canister_empty",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DISPERSER_CANISTER = registerLegacy("disperser_canister",
            () -> new DisperserCanisterItem(new Item.Properties().craftRemainder(DISPERSER_CANISTER_EMPTY.get())));
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
    public static final RegistryObject<Item> CHEMISTRY_SET = registerLegacy("chemistry_set",
            () -> new CraftingDegradationItem(new Item.Properties().durability(100)));
    public static final RegistryObject<Item> CHEMISTRY_SET_BORON = registerLegacy("chemistry_set_boron",
            () -> new CraftingDegradationItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FLUID_DUCT = registerLegacy("fluid_duct",
            () -> new FluidPipeBlockItem(ModBlocks.FLUID_DUCT_NEO.get(), new Item.Properties()));
    public static final RegistryObject<Item> RTTY_PAGER = registerLegacy("rtty_pager",
            () -> new RTTYPagerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DETONATOR = registerLegacy("detonator",
            () -> new DetonatorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DETONATOR_MULTI = registerLegacy("detonator_multi",
            () -> new MultiDetonatorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DETONATOR_LASER = registerLegacy("detonator_laser",
            () -> new LaserDetonatorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DETONATOR_DEADMAN = registerLegacy("detonator_deadman",
            () -> new DroppedDetonatorItem(new Item.Properties().stacksTo(1),
                    DroppedDetonatorItem.Type.DEADMAN_DETONATOR));
    public static final RegistryObject<Item> DETONATOR_DE = registerLegacy("detonator_de",
            () -> new DroppedDetonatorItem(new Item.Properties().stacksTo(1),
                    DroppedDetonatorItem.Type.DEADMAN_EXPLOSIVE));
    public static final RegistryObject<Item> LINKER = registerLegacy("linker",
            () -> new TeleLinkItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DRONE_LINKER = registerLegacy("drone_linker",
            () -> new DroneLinkerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DRONE = registerLegacy("drone",
            () -> new DroneItem(new Item.Properties()));
    public static final RegistryObject<Item> RADAR_LINKER = registerLegacy("radar_linker",
            () -> new RadarLinkerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SINGULARITY = registerLegacy("singularity",
            () -> new SingularityItem(new Item.Properties().stacksTo(1), "singularity", 3,
                    legacyItemSupplier("nuclear_waste")));
    public static final RegistryObject<Item> SINGULARITY_COUNTER_RESONANT = registerLegacy("singularity_counter_resonant",
            () -> new SingularityItem(new Item.Properties().stacksTo(1), "singularity_counter_resonant", 3,
                    legacyItemSupplier("nuclear_waste")));
    public static final RegistryObject<Item> SINGULARITY_SUPER_HEATED = registerLegacy("singularity_super_heated",
            () -> new SingularityItem(new Item.Properties().stacksTo(1), "singularity_super_heated", 3,
                    legacyItemSupplier("nuclear_waste")));
    public static final RegistryObject<Item> SINGULARITY_SPARK = registerLegacy("singularity_spark",
            () -> new SingularityItem(new Item.Properties().stacksTo(1), "singularity_spark", 3,
                    legacyItemSupplier("nuclear_waste")));
    public static final RegistryObject<Item> BLACK_HOLE = registerLegacy("black_hole",
            () -> new SingularityItem(new Item.Properties().stacksTo(1), "black_hole", 3,
                    legacyItemSupplier("nuclear_waste")));
    public static final RegistryObject<Item> PARTICLE_EMPTY = simpleItem("particle_empty");
    public static final RegistryObject<Item> PARTICLE_HYDROGEN = particleCapsule("particle_hydrogen");
    public static final RegistryObject<Item> PARTICLE_COPPER = particleCapsule("particle_copper");
    public static final RegistryObject<Item> PARTICLE_LEAD = particleCapsule("particle_lead");
    public static final RegistryObject<Item> PARTICLE_AMAT = particleCapsule("particle_amat");
    public static final RegistryObject<Item> PARTICLE_ASCHRAB = particleCapsule("particle_aschrab");
    public static final RegistryObject<Item> PARTICLE_HIGGS = registerLegacy("particle_higgs",
            () -> new Item(new Item.Properties().craftRemainder(PARTICLE_EMPTY.get())));
    public static final RegistryObject<Item> PARTICLE_LUTECE = particleCapsule("particle_lutece");
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
            () -> new AntimatterClusterItem(new Item.Properties().craftRemainder(CELL_EMPTY.get())));
    public static final RegistryObject<Item> CUSTOM_TNT = simpleItem("custom_tnt");
    public static final RegistryObject<Item> CUSTOM_NUKE = simpleItem("custom_nuke");
    public static final RegistryObject<Item> CUSTOM_HYDRO = simpleItem("custom_hydro");
    public static final RegistryObject<Item> CUSTOM_AMAT = simpleItem("custom_amat");
    public static final RegistryObject<Item> CUSTOM_DIRTY = simpleItem("custom_dirty");
    public static final RegistryObject<Item> CUSTOM_SCHRAB = simpleItem("custom_schrab");
    public static final RegistryObject<Item> CUSTOM_FALL = simpleStackOneItem("custom_fall");
    public static final RegistryObject<Item> CUBE_POWER = registerLegacy("cube_power",
            () -> new HbmBatteryItem(new Item.Properties(), 1_000_000_000_000_000_000L,
                    1_000_000_000_000_000L, 1_000_000_000_000_000L));
    public static final RegistryObject<Item> MEMORY = registerLegacy("memory",
            () -> new HbmBatteryItem(new Item.Properties(), Long.MAX_VALUE / 100L,
                    100_000_000_000_000L, 100_000_000_000_000L));
    public static final RegistryObject<Item> BATTERY_POTATO = registerLegacy("battery_potato",
            () -> new HbmBatteryItem(new Item.Properties(), 1_000L, 0L, 100L));
    public static final RegistryObject<Item> BATTERY_POTATOS = registerLegacy("battery_potatos",
            () -> new HbmPotatosItem(new Item.Properties(), 500_000L, 0L, 100L));
    public static final RegistryObject<Item> HEV_BATTERY = registerLegacy("hev_battery",
            () -> new HbmSuitBatteryItem(new Item.Properties().stacksTo(4), 150_000L));
    public static final RegistryObject<Item> FUSION_CORE = registerLegacy("fusion_core",
            () -> new HbmSuitBatteryItem(new Item.Properties().stacksTo(1), 2_500_000L));
    public static final RegistryObject<Item> ENERGY_CORE = registerLegacy("energy_core",
            () -> new HbmLegacyEnergyCoreItem(new Item.Properties(), 10_000_000L, 0L, 1_000L));
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

    public static final List<RegistryObject<Item>> CIRCUIT_STAR_PIECE_ITEMS = simpleHiddenItems(
            "circuit_star_piece_board_blank",
            "circuit_star_piece_board_transistor",
            "circuit_star_piece_board_converter",
            "circuit_star_piece_bridge_north",
            "circuit_star_piece_bridge_south",
            "circuit_star_piece_bridge_io",
            "circuit_star_piece_bridge_bus",
            "circuit_star_piece_bridge_chipset",
            "circuit_star_piece_bridge_cmos",
            "circuit_star_piece_bridge_bios",
            "circuit_star_piece_cpu_register",
            "circuit_star_piece_cpu_clock",
            "circuit_star_piece_cpu_logic",
            "circuit_star_piece_cpu_cache",
            "circuit_star_piece_cpu_ext",
            "circuit_star_piece_cpu_socket",
            "circuit_star_piece_mem_socket",
            "circuit_star_piece_mem_16k_a",
            "circuit_star_piece_mem_16k_b",
            "circuit_star_piece_mem_16k_c",
            "circuit_star_piece_mem_16k_d",
            "circuit_star_piece_card_board",
            "circuit_star_piece_card_processor"
    );

    public static final List<RegistryObject<Item>> CIRCUIT_STAR_COMPONENT_ITEMS = simpleHiddenItems(
            "circuit_star_component_chipset",
            "circuit_star_component_cpu",
            "circuit_star_component_ram",
            "circuit_star_component_card"
    );
    // Legacy ItemCustomLore: hidden, uncommon, and assembled from the four StarControl components.
    public static final RegistryObject<Item> CIRCUIT_STAR = registerLegacy("circuit_star",
            () -> new ItemCustomLore(new Item.Properties()).setRarity(Rarity.UNCOMMON));

    public static final List<RegistryObject<Item>> INGOT_METAL_ITEMS = simpleHiddenItems(
            "ingot_metal_scrap",
            "ingot_metal_ingot",
            "ingot_metal_counter",
            "ingot_metal_key",
            "ingot_metal_beacon",
            "ingot_metal_casing",
            "ingot_metal_clockwork",
            "ingot_metal_bar",
            "ingot_metal_detector"
    );

    public static final List<RegistryObject<Item>> CHEMICAL_DYE_ITEMS = chemicalDyeItems(
            new ChemicalDyeSpec("chemical_dye_black", 1973019),
            new ChemicalDyeSpec("chemical_dye_red", 11743532),
            new ChemicalDyeSpec("chemical_dye_green", 3887386),
            new ChemicalDyeSpec("chemical_dye_brown", 5320730),
            new ChemicalDyeSpec("chemical_dye_blue", 2437522),
            new ChemicalDyeSpec("chemical_dye_purple", 8073150),
            new ChemicalDyeSpec("chemical_dye_cyan", 2651799),
            new ChemicalDyeSpec("chemical_dye_silver", 11250603),
            new ChemicalDyeSpec("chemical_dye_gray", 4408131),
            new ChemicalDyeSpec("chemical_dye_pink", 14188952),
            new ChemicalDyeSpec("chemical_dye_lime", 4312372),
            new ChemicalDyeSpec("chemical_dye_yellow", 14602026),
            new ChemicalDyeSpec("chemical_dye_lightblue", 6719955),
            new ChemicalDyeSpec("chemical_dye_magenta", 12801229),
            new ChemicalDyeSpec("chemical_dye_orange", 15435844),
            new ChemicalDyeSpec("chemical_dye_white", 15790320)
    );

    // 1.7.10 ItemCrayon metadata values, split into normal modern registry IDs.
    public static final List<RegistryObject<Item>> CRAYON_ITEMS = crayonItems(
            new CrayonSpec("crayon_black", 1973019),
            new CrayonSpec("crayon_red", 11743532),
            new CrayonSpec("crayon_green", 3887386),
            new CrayonSpec("crayon_brown", 5320730),
            new CrayonSpec("crayon_blue", 2437522),
            new CrayonSpec("crayon_purple", 8073150),
            new CrayonSpec("crayon_cyan", 2651799),
            new CrayonSpec("crayon_silver", 11250603),
            new CrayonSpec("crayon_gray", 4408131),
            new CrayonSpec("crayon_pink", 14188952),
            new CrayonSpec("crayon_lime", 4312372),
            new CrayonSpec("crayon_yellow", 14602026),
            new CrayonSpec("crayon_lightblue", 6719955),
            new CrayonSpec("crayon_magenta", 12801229),
            new CrayonSpec("crayon_orange", 15435844),
            new CrayonSpec("crayon_white", 15790320)
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
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_DIGAMMA = registerLegacy("holotape_image_digamma",
            () -> new ItemHolotapeImage(new Item.Properties(), ItemHolotapeImage.EnumHoloImage.HOLO_DIGAMMA));
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_RESTORED = registerLegacy("holotape_image_restored",
            () -> new ItemHolotapeImage(new Item.Properties(), ItemHolotapeImage.EnumHoloImage.HOLO_RESTORED));
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_FE_HALL = holotape("holotape_image_fe_hall",
            ItemHolotapeImage.EnumHoloImage.HOLO_FE_HALL);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_FE_CORRIDOR = holotape("holotape_image_fe_corridor",
            ItemHolotapeImage.EnumHoloImage.HOLO_FE_CORRIDOR);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_FE_SERVER = holotape("holotape_image_fe_server",
            ItemHolotapeImage.EnumHoloImage.HOLO_FE_SERVER);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_FEH_DOME = holotape("holotape_image_feh_dome",
            ItemHolotapeImage.EnumHoloImage.HOLO_FEH_DOME);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_FEH_BOAT = holotape("holotape_image_feh_boat",
            ItemHolotapeImage.EnumHoloImage.HOLO_FEH_BOAT);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_FEH_LSC = holotape("holotape_image_feh_lsc",
            ItemHolotapeImage.EnumHoloImage.HOLO_FEH_LSC);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_F3_RC = holotape("holotape_image_f3_rc",
            ItemHolotapeImage.EnumHoloImage.HOLO_F3_RC);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_F3_IV = holotape("holotape_image_f3_iv",
            ItemHolotapeImage.EnumHoloImage.HOLO_F3_IV);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_F3_WM = holotape("holotape_image_f3_wm",
            ItemHolotapeImage.EnumHoloImage.HOLO_F3_WM);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_NV_CRATER = holotape("holotape_image_nv_crater",
            ItemHolotapeImage.EnumHoloImage.HOLO_NV_CRATER);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_NV_DIVIDE = holotape("holotape_image_nv_divide",
            ItemHolotapeImage.EnumHoloImage.HOLO_NV_DIVIDE);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_NV_BM = holotape("holotape_image_nv_bm",
            ItemHolotapeImage.EnumHoloImage.HOLO_NV_BM);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_O_1 = holotape("holotape_image_o_1",
            ItemHolotapeImage.EnumHoloImage.HOLO_O_1);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_O_2 = holotape("holotape_image_o_2",
            ItemHolotapeImage.EnumHoloImage.HOLO_O_2);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_O_3 = holotape("holotape_image_o_3",
            ItemHolotapeImage.EnumHoloImage.HOLO_O_3);
    public static final RegistryObject<Item> HOLOTAPE_IMAGE_CHALLENGE = holotape("holotape_image_challenge",
            ItemHolotapeImage.EnumHoloImage.HOLO_CHALLENGE);
    public static final List<RegistryObject<Item>> HOLOTAPE_IMAGE_ITEMS = List.of(
            HOLOTAPE_IMAGE_DIGAMMA, HOLOTAPE_IMAGE_RESTORED, HOLOTAPE_IMAGE_FE_HALL, HOLOTAPE_IMAGE_FE_CORRIDOR,
            HOLOTAPE_IMAGE_FE_SERVER, HOLOTAPE_IMAGE_FEH_DOME, HOLOTAPE_IMAGE_FEH_BOAT, HOLOTAPE_IMAGE_FEH_LSC,
            HOLOTAPE_IMAGE_F3_RC, HOLOTAPE_IMAGE_F3_IV, HOLOTAPE_IMAGE_F3_WM, HOLOTAPE_IMAGE_NV_CRATER,
            HOLOTAPE_IMAGE_NV_DIVIDE, HOLOTAPE_IMAGE_NV_BM, HOLOTAPE_IMAGE_O_1, HOLOTAPE_IMAGE_O_2,
            HOLOTAPE_IMAGE_O_3, HOLOTAPE_IMAGE_CHALLENGE);
    public static final RegistryObject<Item> HOLOTAPE_DAMAGED = registerLegacy("holotape_damaged",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLUID_ICON = registerLegacy("fluid_icon",
            () -> new FluidIconItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WATCH = registerLegacy("watch",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RANGEFINDER = registerLegacy("rangefinder",
            () -> new RangefinderItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BOMB_CALLER = registerLegacy("bomb_caller",
            () -> new BombCallerItem(new Item.Properties(), 0));
    public static final RegistryObject<Item> BOMB_CALLER_NAPALM = registerLegacy("bomb_caller_napalm",
            () -> new BombCallerItem(new Item.Properties(), 1));
    public static final RegistryObject<Item> BOMB_CALLER_CHLORINE = registerLegacy("bomb_caller_chlorine",
            () -> new BombCallerItem(new Item.Properties(), 2));
    public static final RegistryObject<Item> BOMB_CALLER_ORANGE = registerLegacy("bomb_caller_orange",
            () -> new BombCallerItem(new Item.Properties(), 3));
    public static final RegistryObject<Item> BOMB_CALLER_ATOMIC = registerLegacy("bomb_caller_atomic",
            () -> new BombCallerItem(new Item.Properties(), 4));
    public static final RegistryObject<Item> ANCHOR_REMOTE = registerLegacy("anchor_remote",
            () -> new AnchorRemoteItem(new Item.Properties()));
    public static final RegistryObject<Item> BISMUTH_TOOL = registerLegacy("bismuth_tool",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MELTDOWN_TOOL = registerLegacy("meltdown_tool",
            () -> new MeltdownToolItem(new Item.Properties()));
    public static final RegistryObject<Item> SURVEY_SCANNER = registerLegacy("survey_scanner",
            () -> new SurveyScannerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ORE_DENSITY_SCANNER = registerLegacy("ore_density_scanner",
            () -> new OreDensityScannerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> REACTOR_SENSOR = registerLegacy("reactor_sensor",
            () -> new ReactorSensorItem(new Item.Properties().stacksTo(1)));

    public static final List<RegistryObject<Item>> HIDDEN_RECIPE_ITEMS = Stream.concat(
            Stream.concat(Stream.concat(Stream.concat(Stream.concat(STAMP_BOOK_ITEMS.stream(), PAGE_OF_ITEMS.stream()),
                    CIRCUIT_STAR_PIECE_ITEMS.stream()), INGOT_METAL_ITEMS.stream()), WEAPON_MOD_TEST_ITEMS.stream()),
            Stream.concat(Stream.concat(Stream.of(BLUEPRINT_FOLDER_SECRET, TEMPLATE_FOLDER, HOLOTAPE_DAMAGED, FLUID_ICON, WATCH,
                    BURNT_BARK, APPLE_EUPHEMIUM), HOLOTAPE_IMAGE_ITEMS.stream()), CIRCUIT_STAR_COMPONENT_ITEMS.stream())).toList();

    public static final RegistryObject<Item> DESIGNATOR = registerLegacy("designator",
            () -> new MissileDesignatorItem(new Item.Properties().stacksTo(1), MissileDesignatorItem.Mode.BLOCK));
    public static final RegistryObject<Item> DESIGNATOR_RANGE = registerLegacy("designator_range",
            () -> new MissileDesignatorItem(new Item.Properties().stacksTo(1), MissileDesignatorItem.Mode.RANGE));
    public static final RegistryObject<Item> DESIGNATOR_MANUAL = registerLegacy("designator_manual",
            () -> new MissileDesignatorItem(new Item.Properties().stacksTo(1), MissileDesignatorItem.Mode.MANUAL));
    public static final RegistryObject<Item> DESIGNATOR_ARTY_RANGE = registerLegacy("designator_arty_range",
            () -> new ArtilleryDesignatorItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SATELLITE = registerLegacy("satellite",
            () -> new SatelliteItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SAT_CHIP = registerLegacy("sat_chip",
            () -> new SatelliteChipItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SAT_INTERFACE = registerLegacy("sat_interface",
            () -> new SatelliteInterfaceItem(new Item.Properties().stacksTo(1), SatelliteInterfaceItem.Mode.PANEL));
    public static final RegistryObject<Item> SAT_COORD = registerLegacy("sat_coord",
            () -> new SatelliteInterfaceItem(new Item.Properties().stacksTo(1), SatelliteInterfaceItem.Mode.COORD));
    public static final RegistryObject<Item> SAT_DESIGNATOR = registerLegacy("sat_designator",
            () -> new SatelliteDesignatorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SAT_RELAY = registerLegacy("sat_relay",
            () -> new SatelliteChipItem(new Item.Properties().stacksTo(1)));
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

    public static final RegistryObject<Item> MISSILE_KIT = registerLegacy("missile_kit",
            () -> new MissileStarterKitItem(new Item.Properties()));
    public static final RegistryObject<Item> MISSILE_ASSEMBLY = simpleStackOneItem("missile_assembly");
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
            MissileItem.FormFactor.V2, MissileItem.Tier.TIER1);
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
            MissileItem.FormFactor.OTHER, MissileItem.Tier.TIER3, MissileItem.Fuel.KEROSENE_PEROXIDE);
    public static final RegistryObject<Item> MISSILE_STEALTH = missile("missile_stealth",
            MissileItem.FormFactor.STRONG, MissileItem.Tier.TIER1);
    public static final RegistryObject<Item> MISSILE_TEST = missile("missile_test",
            MissileItem.FormFactor.MICRO, MissileItem.Tier.TIER0, MissileItem.Fuel.SOLID);
    public static final RegistryObject<Item> MISSILE_CUSTOM = registerLegacy("missile_custom",
            () -> new CustomMissileItem(new Item.Properties().stacksTo(1)));

    public static final List<RegistryObject<Item>> MISSILE_PART_ITEMS = Stream.of(
            missileParts(MissilePartItem.PartType.CHIP,
                    "mp_c_1", "mp_c_2", "mp_c_3", "mp_c_4", "mp_c_5").stream(),
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
                    "mp_s_20").stream(),
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

    public static final List<RegistryObject<Item>> MISSILE_TAB_ITEMS = List.of(
            MISSILE_KIT,
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
            MISSILE_STEALTH
    );

    /**
     * The nine complete named custom-missile examples injected by the legacy
     * {@code MissileTab#displayAllReleventItems} implementation. They are not
     * registry entries: each is a normal custom missile with source-authored
     * part NBT and a coloured display name.
     */
    public static List<ItemStack> legacyMissileCreativePresets() {
        return List.of(
                namedCustomMissile("Lil Bub", ChatFormatting.DARK_PURPLE,
                        "mp_c_3", "mp_warhead_10_he", "mp_fuselage_10_kerosene",
                        "mp_stability_10_flat", "mp_thruster_10_kerosene"),
                namedCustomMissile("Long Boy", ChatFormatting.DARK_PURPLE,
                        "mp_c_3", "mp_warhead_10_incendiary", "mp_fuselage_10_long_solid",
                        "mp_stability_10_space", "mp_thruster_10_solid"),
                namedCustomMissile("Uncle Kim", ChatFormatting.DARK_PURPLE,
                        "mp_c_3", "mp_warhead_10_nuclear", "mp_fuselage_10_15_kerosene",
                        "mp_stability_15_flat", "mp_thruster_15_kerosene"),
                namedCustomMissile("Trotty's Toy Rocket", ChatFormatting.GREEN,
                        "mp_c_3", "mp_warhead_10_nuclear_large", "mp_fuselage_10_15_balefire",
                        "mp_stability_15_flat", "mp_thruster_15_balefire_large"),
                namedCustomMissile("Stealthy Shark", ChatFormatting.DARK_PURPLE,
                        "mp_c_3", "mp_warhead_15_nuclear_shark", "mp_fuselage_15_kerosene_camo",
                        "mp_stability_15_thin", "mp_thruster_15_kerosene_triple"),
                namedCustomMissile("Polite Lad", ChatFormatting.DARK_PURPLE,
                        "mp_c_3", "mp_warhead_15_he", "mp_fuselage_15_kerosene_polite",
                        "mp_stability_15_thin", "mp_thruster_15_kerosene_dual"),
                namedCustomMissile("NERV's Leftover Missile", ChatFormatting.DARK_PURPLE,
                        "mp_c_3", "mp_warhead_15_n2", "mp_fuselage_15_solid_desh",
                        "mp_stability_15_thin", "mp_thruster_15_solid_hexdecuple"),
                namedCustomMissile("Auntie Blackjack", ChatFormatting.RED,
                        "mp_c_5", "mp_warhead_15_boxcar", "mp_fuselage_15_kerosene_blackjack",
                        "mp_stability_15_thin", "mp_thruster_15_kerosene"),
                namedCustomMissile("Hightower Missile", ChatFormatting.GREEN,
                        "mp_c_4", "mp_warhead_15_balefire", "mp_fuselage_15_20_kerosene_magnusson",
                        null, "mp_thruster_20_kerosene"));
    }

    public static final List<RegistryObject<Item>> SATELLITE_TAB_ITEMS = List.of(
            RANGEFINDER,
            DESIGNATOR,
            DESIGNATOR_RANGE,
            DESIGNATOR_MANUAL,
            DESIGNATOR_ARTY_RANGE,
            SATELLITE,
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
    public static final RegistryObject<Item> INGOT_SMORE = registerLegacy("ingot_smore",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(10)
                    .saturationMod(20.0F)
                    .build())));
    public static final RegistryObject<Item> STICK_DYNAMITE = registerLegacy("stick_dynamite",
            () -> new ItemGrenadeDynamite(3, new Item.Properties()));
    public static final RegistryObject<Item> STICK_DYNAMITE_FISHING = registerLegacy("stick_dynamite_fishing",
            () -> new ItemGrenadeFishing(3, new Item.Properties()));
    public static final RegistryObject<Item> GRENADE_SHELL_FRAG = simpleItem("grenade_shell_frag");
    public static final RegistryObject<Item> GRENADE_SHELL_STICK = simpleItem("grenade_shell_stick");
    public static final RegistryObject<Item> GRENADE_SHELL_TECH = simpleItem("grenade_shell_tech");
    public static final RegistryObject<Item> GRENADE_SHELL_NUKE = simpleItem("grenade_shell_nuke");
    public static final RegistryObject<Item> GRENADE_FUZE_S3 = simpleItem("grenade_fuze_s3");
    public static final RegistryObject<Item> GRENADE_FUZE_S7 = simpleItem("grenade_fuze_s7");
    public static final RegistryObject<Item> GRENADE_FUZE_S15 = simpleItem("grenade_fuze_s15");
    public static final RegistryObject<Item> GRENADE_FUZE_IMPACT = simpleItem("grenade_fuze_impact");
    public static final RegistryObject<Item> GRENADE_FUZE_AIRBURST = simpleItem("grenade_fuze_airburst");
    public static final RegistryObject<Item> GRENADE_FILLING_POWDER = simpleItem("grenade_filling_powder");
    public static final RegistryObject<Item> GRENADE_FILLING_HE = simpleItem("grenade_filling_he");
    public static final RegistryObject<Item> GRENADE_FILLING_DEMO = simpleItem("grenade_filling_demo");
    public static final RegistryObject<Item> GRENADE_FILLING_INC = simpleItem("grenade_filling_inc");
    public static final RegistryObject<Item> GRENADE_FILLING_WP = simpleItem("grenade_filling_wp");
    public static final RegistryObject<Item> GRENADE_FILLING_CLUSTER = simpleItem("grenade_filling_cluster");
    public static final RegistryObject<Item> GRENADE_FILLING_EMP = simpleItem("grenade_filling_emp");
    public static final RegistryObject<Item> GRENADE_FILLING_PLASMA = simpleItem("grenade_filling_plasma");
    public static final RegistryObject<Item> GRENADE_FILLING_LASER = simpleItem("grenade_filling_laser");
    public static final RegistryObject<Item> GRENADE_FILLING_CLUSTER_HEAVY = simpleItem("grenade_filling_cluster_heavy");
    public static final RegistryObject<Item> GRENADE_FILLING_NUCLEAR = simpleItem("grenade_filling_nuclear");
    public static final RegistryObject<Item> GRENADE_FILLING_NUCLEAR_DEMO = simpleItem("grenade_filling_nuclear_demo");
    public static final RegistryObject<Item> GRENADE_FILLING_SCHRAB = simpleItem("grenade_filling_schrab");
    public static final RegistryObject<Item> GRENADE_EXTRA_GLUE = simpleItem("grenade_extra_glue");
    public static final RegistryObject<Item> GRENADE_EXTRA_PROXY_FUZE = simpleItem("grenade_extra_proxy_fuze");
    public static final RegistryObject<Item> GRENADE_EXTRA_FRAG_SLEEVE = simpleItem("grenade_extra_frag_sleeve");
    public static final RegistryObject<Item> GRENADE_EXTRA_TRIPLEX = simpleItem("grenade_extra_triplex");
    public static final RegistryObject<Item> GRENADE_UNIVERSAL = registerLegacy("grenade_universal",
            () -> new UniversalGrenadeItem(new Item.Properties()));
    public static final RegistryObject<Item> BOTTLE_MERCURY = registerLegacy("bottle_mercury",
            () -> new Item(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE)));
    public static final RegistryObject<Item> BOLT_SPIKE = registerLegacy("bolt_spike",
            () -> new LegacyLoreItem(new Item.Properties()));
    public static final RegistryObject<Item> CAN_KEY = registerLegacy("can_key",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CANNED_BEEF = conserve(LegacyConserveItem.Type.BEEF);
    public static final RegistryObject<Item> CANNED_TUNA = conserve(LegacyConserveItem.Type.TUNA);
    public static final RegistryObject<Item> CANNED_MYSTERY = conserve(LegacyConserveItem.Type.MYSTERY);
    public static final RegistryObject<Item> CANNED_PASHTET = conserve(LegacyConserveItem.Type.PASHTET);
    public static final RegistryObject<Item> CANNED_CHEESE = conserve(LegacyConserveItem.Type.CHEESE);
    public static final RegistryObject<Item> CANNED_SLIME = conserve(LegacyConserveItem.Type.SLIME);
    public static final RegistryObject<Item> CANNED_MILK = conserve(LegacyConserveItem.Type.MILK);
    public static final RegistryObject<Item> CANNED_ASS = conserve(LegacyConserveItem.Type.ASS);
    public static final RegistryObject<Item> CANNED_PIZZA = conserve(LegacyConserveItem.Type.PIZZA);
    public static final RegistryObject<Item> CANNED_TUBE = conserve(LegacyConserveItem.Type.TUBE);
    public static final RegistryObject<Item> CANNED_TOMATO = conserve(LegacyConserveItem.Type.TOMATO);
    public static final RegistryObject<Item> CANNED_ASBESTOS = conserve(LegacyConserveItem.Type.ASBESTOS);
    public static final RegistryObject<Item> CANNED_BHOLE = conserve(LegacyConserveItem.Type.BHOLE);
    public static final RegistryObject<Item> CANNED_HOTDOGS = conserve(LegacyConserveItem.Type.HOTDOGS);
    public static final RegistryObject<Item> CANNED_LEFTOVERS = conserve(LegacyConserveItem.Type.LEFTOVERS);
    public static final RegistryObject<Item> CANNED_YOGURT = conserve(LegacyConserveItem.Type.YOGURT);
    public static final RegistryObject<Item> CANNED_STEW = conserve(LegacyConserveItem.Type.STEW);
    public static final RegistryObject<Item> CANNED_CHINESE = conserve(LegacyConserveItem.Type.CHINESE);
    public static final RegistryObject<Item> CANNED_OIL = conserve(LegacyConserveItem.Type.OIL);
    public static final RegistryObject<Item> CANNED_FIST = conserve(LegacyConserveItem.Type.FIST);
    public static final RegistryObject<Item> CANNED_SPAM = conserve(LegacyConserveItem.Type.SPAM);
    public static final RegistryObject<Item> CANNED_FRIED = conserve(LegacyConserveItem.Type.FRIED);
    public static final RegistryObject<Item> CANNED_NAPALM = conserve(LegacyConserveItem.Type.NAPALM);
    public static final RegistryObject<Item> CANNED_DIESEL = conserve(LegacyConserveItem.Type.DIESEL);
    public static final RegistryObject<Item> CANNED_KEROSENE = conserve(LegacyConserveItem.Type.KEROSENE);
    public static final RegistryObject<Item> CANNED_RECURSION = conserve(LegacyConserveItem.Type.RECURSION);
    public static final RegistryObject<Item> CANNED_BARK = conserve(LegacyConserveItem.Type.BARK);
    public static final RegistryObject<Item> TEM_FLAKES = registerLegacy("tem_flakes",
            () -> new LegacyTemFlakesItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(0)
                    .saturationMod(0.0F)
                    .alwaysEat()
                    .build())));
    public static final RegistryObject<Item> GLOWING_STEW = registerLegacy("glowing_stew",
            () -> new BowlFoodItem(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationMod(0.6F)
                    .build())));
    public static final RegistryObject<Item> BALEFIRE_SCRAMBLED = registerLegacy("balefire_scrambled",
            () -> new BowlFoodItem(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationMod(0.6F)
                    .build())));
    public static final RegistryObject<Item> BALEFIRE_AND_HAM = registerLegacy("balefire_and_ham",
            () -> new BowlFoodItem(new Item.Properties().stacksTo(1).food(new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationMod(0.6F)
                    .build())));
    public static final RegistryObject<Item> PEAS = registerLegacy("peas",
            () -> new LegacyPeasItem(new Item.Properties()));
    public static final RegistryObject<Item> SPAWN_DUCK = registerLegacy("spawn_duck",
            () -> new LegacyDuckSpawnItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> MARSHMALLOW = registerLegacy("marshmallow",
            () -> new MarshmallowItem(new Item.Properties()));
    public static final RegistryObject<Item> RECORD_LC = registerLegacy("record_lc",
            () -> new LegacyMusicDiscItem(1, ModSounds.MUSIC_RECORD_LAMBDA_CORE.get(),
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 104));
    public static final RegistryObject<Item> RECORD_SS = registerLegacy("record_ss",
            () -> new LegacyMusicDiscItem(1, ModSounds.MUSIC_RECORD_SECTOR_SWEEP.get(),
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 166));
    public static final RegistryObject<Item> RECORD_VC = registerLegacy("record_vc",
            () -> new LegacyMusicDiscItem(1, ModSounds.MUSIC_RECORD_VORTAL_COMBAT.get(),
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 194));
    public static final RegistryObject<Item> BOOK_LEMEGETON = registerLegacy("book_lemegeton",
            () -> new LemegetonItem(new Item.Properties()));
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
    public static final RegistryObject<Item> BIG_SWORD = registerLegacy("big_sword",
            () -> new LegacyBigSwordItem(new Item.Properties()));
    public static final RegistryObject<Item> REDSTONE_SWORD = registerLegacy("redstone_sword",
            () -> new LegacyRedstoneSwordItem(new Item.Properties().stacksTo(1).durability(Tiers.STONE.getUses())));
    public static final RegistryObject<Item> BDCL = registerLegacy("bdcl", () -> new LegacyBdclItem(new Item.Properties()));
    public static final RegistryObject<Item> DEUTERIUM_FILTER = simpleItem("deuterium_filter");
    public static final RegistryObject<Item> COUPLING_TOOL = registerLegacy("coupling_tool",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CRYSTAL_HORN = registerLegacy("crystal_horn",
            () -> new LegacyLoreItem(new Item.Properties()));
    public static final RegistryObject<Item> CRYSTAL_CHARRED = registerLegacy("crystal_charred",
            () -> new LegacyLoreItem(new Item.Properties()));
    public static final RegistryObject<Item> FOODITEM = registerLegacy("fooditem",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(2)
                    .saturationMod(5.0F)
                    .build())));

    public static final List<RegistryObject<Item>> EXTRA_PARTS_TAB_ITEMS = Stream.concat(CHEMICAL_DYE_ITEMS.stream(),
            Stream.concat(Stream.concat(Stream.concat(Stream.concat(CIRCUIT_ITEMS.stream(), EXPENSIVE_MODE_ITEMS.stream()), ORE_BYPRODUCT_ITEMS.stream()), simpleParts(
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
            "ingot_actinium",
            "ingot_boron",
            "ingot_lanthanium",
            "ingot_fiberglass",
            "ingot_euphemium",
            "ingot_mercury",
            "ingot_gh336",
            "ingot_starmetal",
            "ingot_dineutronium",
            "nugget_dineutronium",
            "ingot_electronium",
            "ingot_osmiridium",
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
            "ball_fireclay",
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
            "ingot_arsenic",
            "nugget_bismuth",
            "nugget_arsenic",
            "nugget_lead",
            "nugget_euphemium",
            "nugget_osmiridium",
            "nugget_gh336",
            "ingot_niobium",
            "nugget_niobium",
            "ingot_tantalium",
            "ingot_silicon",
            "ingot_ferrouranium",
            "ingot_asbestos",
            "ingot_polymer",
            "ingot_bakelite",
            "ingot_biorubber",
            "ingot_rubber",
            "ingot_pc",
            "ingot_pvc",
            "ingot_redstone",
            "ingot_neodymium",
            "ingot_borax",
            "ingot_sodium",
            "ingot_strontium",
            "ingot_slag",
            "ingot_red_copper",
            "ingot_tungsten_carbide",
            "fluorite",
            "plate_dura_steel",
            "ingot_dura_steel",
            "powder_dura_steel",
            "plate_gold",
            "plate_paa",
            "shimmer_head",
            "shimmer_axe_head",
            "shimmer_handle",
            "plate_euphemium",
            "lithium",
            "powder_lithium",
            "powder_lithium_tiny",
            "powder_ice",
            "powder_neodymium",
            "powder_neodymium_tiny",
            "powder_boron_tiny",
            "powder_niobium_tiny",
            "powder_cerium",
            "powder_cerium_tiny",
            "powder_lanthanium",
            "powder_lanthanium_tiny",
            "powder_paleogenite_tiny",
            "powder_beryllium",
            "powder_aluminium",
            "powder_bromine",
            "powder_cobalt",
            "powder_cobalt_tiny",
            "powder_neptunium",
            "powder_sodium",
            "powder_impure_osmiridium",
            "powder_schrabidium",
            "powder_dineutronium",
            "powder_gold",
            "powder_niobium",
            "powder_astatine",
            "powder_asbestos",
            "powder_molysite",
            "powder_ferrouranium",
            "powder_iodine",
            "powder_strontium",
            "powder_polonium",
            "powder_ra226",
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
            "powder_zirconium",
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
            "plate_cast_gold",
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
            "plate_cast_zirconium",
            "plate_cast_starmetal",
            "plate_cast_osmiridium",
            "plate_cast_schrabidium",
            "plate_cast_schrabidate",
            "plate_cast_bismuth",
            "plate_welded_iron",
            "plate_welded_steel",
            "plate_welded_copper",
            "plate_welded_titanium",
            "plate_welded_zirconium",
            "plate_welded_aluminium",
            "plate_welded_tcalloy",
            "plate_welded_cdalloy",
            "plate_welded_tungsten",
            "plate_welded_combine_steel",
            "plate_welded_osmiridium",
            "wire_fine_carbon",
            "wire_gold",
            "wire_fine_aluminium",
            "wire_fine_copper",
            "wire_fine_mingrade",
            "wire_fine_tungsten",
            "wire_fine_schrabidium",
            "wire_fine_magnetized_tungsten",
            "wire_fine_lead",
            "wire_fine_zirconium",
            "wire_fine_steel",
            "wire_dense_gold",
            "wire_dense_copper",
            "wire_dense_titanium",
            "wire_dense_niobium",
            "wire_dense_mingrade",
            "wire_dense_bscco",
            "wire_dense_neodymium",
            "wire_dense_schrabidium",
            "wire_dense_schrabidate",
            "wire_dense_tungsten",
            "wire_dense_dineutronium",
            "wire_dense_magnetized_tungsten",
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
            "pipes_aluminium",
            "pipes_iron",
            "pipes_rubber",
            "pipes_dura_steel",
            "pipes_lead",
            "pellet_charged",
            "pipes_steel",
            "drill_titanium",
            "plate_dalekanium",
            "plate_polymer",
            "plate_kevlar",
            "plate_dineutronium",
            "plate_desh",
            "ingot_desh",
            "nugget_desh",
            "ingot_solinium",
            "nugget_solinium",
            "photo_panel",
            "thruster_nuclear",
            "thruster_small",
            "thruster_medium",
            "thruster_large",
            "fuel_tank_small",
            "fuel_tank_medium",
            "fuel_tank_large",
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
            "fuse",
            "safety_fuse",
            "hazmat_cloth",
            "hazmat_cloth_red",
            "hazmat_cloth_grey",
            "asbestos_cloth",
            "rag_damp",
            "rag_piss",
            "filter_coal",
            "catalyst_clay",
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
            "billet_australium",
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
            "powder_chlorocalcite",
            "powder_borax",
            "powder_balefire",
            "powder_semtex_mix",
            "powder_tcalloy",
            "powder_desh",
            "powder_desh_ready",
            "crystal_starmetal",
            "gem_volcanic",
            "undefined",
            "fragment_iron",
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
            "crystal_cinnebar",
            "crystal_osmiridium",
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
    ).stream()), Stream.of(INGOT_C4))).toList();

    public static final RegistryObject<Item> DEMON_CORE_OPEN = registerLegacy("demon_core_open",
            () -> new DemonCoreItem(new Item.Properties()));
    public static final RegistryObject<Item> DEMON_CORE_CLOSED = simpleItem("demon_core_closed");
    public static final RegistryObject<Item> BATTERY_SPARK = simpleStackOneItem("battery_spark");
    public static final RegistryObject<Item> BATTERY_TRIXITE = simpleStackOneItem("battery_trixite");
    public static final RegistryObject<Item> NUKE_ELECTRIC_KIT = registerLegacy("nuke_electric_kit",
            () -> new NukeElectricStarterKitItem(new Item.Properties()));

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
            DETONATOR_MULTI,
            DETONATOR_LASER,
            DETONATOR_DEADMAN,
            DETONATOR_DE,
            CUSTOM_TNT,
            CUSTOM_NUKE,
            CUSTOM_HYDRO,
            CUSTOM_AMAT,
            CUSTOM_DIRTY,
            CUSTOM_SCHRAB,
            CUSTOM_FALL
    )).toList();

    private static final List<RegistryObject<Item>> CONTROL_BATTERY_ITEMS = Stream.concat(Stream.of(
            CUBE_POWER,
            BATTERY_POTATO,
            BATTERY_POTATOS,
            HEV_BATTERY,
            FUSION_CORE,
            ENERGY_CORE,
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
            PARTICLE_LUTECE,
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
            BLUEPRINT_FOLDER,
            BLUEPRINT_FOLDER_DISCOVER,
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
            UPGRADE_STACK_1,
            UPGRADE_STACK_2,
            UPGRADE_STACK_3,
            UPGRADE_EJECTOR_1,
            UPGRADE_EJECTOR_2,
            UPGRADE_EJECTOR_3,
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
            SCREWDRIVER_DESH,
            HAND_DRILL,
            HAND_DRILL_DESH,
            WRENCH,
            WRENCH_ARCHINEER,
            BLOWTORCH,
            ACETYLENE_TORCH,
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
            SCHRABIDIUM_HOE,
            SCHRABIDIUM_HAMMER,
            SHIMMER_SLEDGE,
            SHIMMER_AXE,
            TITANIUM_SWORD,
            TITANIUM_PICKAXE,
            TITANIUM_AXE,
            TITANIUM_SHOVEL,
            TITANIUM_HOE,
            STEEL_SWORD,
            STEEL_PICKAXE,
            STEEL_AXE,
            STEEL_SHOVEL,
            STEEL_HOE,
            ALLOY_SWORD,
            ALLOY_PICKAXE,
            ALLOY_AXE,
            ALLOY_SHOVEL,
            CMB_SWORD,
            CMB_PICKAXE,
            CMB_AXE,
            CMB_SHOVEL,
            CMB_HOE,
            DESH_SWORD,
            DESH_PICKAXE,
            DESH_AXE,
            DESH_SHOVEL,
            DESH_HOE,
            COBALT_SWORD,
            COBALT_PICKAXE,
            COBALT_AXE,
            COBALT_SHOVEL,
            COBALT_HOE,
            COBALT_DECORATED_SWORD,
            COBALT_DECORATED_PICKAXE,
            COBALT_DECORATED_AXE,
            COBALT_DECORATED_SHOVEL,
            COBALT_DECORATED_HOE,
            STARMETAL_SWORD,
            STARMETAL_PICKAXE,
            STARMETAL_AXE,
            STARMETAL_SHOVEL,
            STARMETAL_HOE,
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
            CROWBAR,
            MEMESPOON,
            WOOD_GAVEL,
            LEAD_GAVEL,
            PIPE_LEAD,
            ULLAPOOL_CABER,
            CHAINSAW
    );

    public static final List<RegistryObject<Item>> ZIRNOX_ROD_ITEMS = List.of(
            zirnoxRod("rod_zirnox_natural_uranium_fuel", EnumZirnoxType.NATURAL_URANIUM_FUEL),
            zirnoxRod("rod_zirnox_uranium_fuel", EnumZirnoxType.URANIUM_FUEL),
            zirnoxRod("rod_zirnox_th232", EnumZirnoxType.TH232),
            zirnoxRod("rod_zirnox_thorium_fuel", EnumZirnoxType.THORIUM_FUEL),
            zirnoxRod("rod_zirnox_mox_fuel", EnumZirnoxType.MOX_FUEL),
            zirnoxRod("rod_zirnox_plutonium_fuel", EnumZirnoxType.PLUTONIUM_FUEL),
            zirnoxRod("rod_zirnox_u233_fuel", EnumZirnoxType.U233_FUEL),
            zirnoxRod("rod_zirnox_u235_fuel", EnumZirnoxType.U235_FUEL),
            zirnoxRod("rod_zirnox_les_fuel", EnumZirnoxType.LES_FUEL),
            zirnoxRod("rod_zirnox_lithium", EnumZirnoxType.LITHIUM),
            zirnoxRod("rod_zirnox_zfb_mox", EnumZirnoxType.ZFB_MOX));

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

    // 1.7.10 ItemPileRodMK2: not one of the deprecated graphite-pile rod IDs.
    public static final RegistryObject<Item> PILE_ROD = registerLegacy("pile_rod",
            () -> new Mk2PileRodItem(new Item.Properties()));

    public static final List<RegistryObject<Item>> CONTROL_TAB_ITEMS = Stream.<List<RegistryObject<Item>>>of(List.of(PILE_ROD), simpleParts(
            "rod_empty"
    ), breedingRodItems(RodFamily.SINGLE), simpleParts(
            "rod_dual_empty",
            "rod_quad_empty"
    ), breedingRodItems(RodFamily.DUAL), breedingRodItems(RodFamily.QUAD), simpleParts(
            "rod_zirnox_empty",
            "cell_deuterium",
            "cell_tritium",
            "cell_uf6",
            "cell_puf6",
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
    ), Stream.concat(Stream.of(MOLD_BASE, MOLD), PRESS_STAMP_VARIANT_ITEMS.stream()).toList(),
            simpleStackOneItems(
            "plate_fuel_u233",
            "plate_fuel_u235",
            "plate_fuel_mox",
            "plate_fuel_pu239",
            "plate_fuel_sa326",
            "plate_fuel_ra226be",
            "plate_fuel_pu238be"
    ), rtgPelletItems(
            "pellet_rtg_radium",
            "pellet_rtg_weak",
            "pellet_rtg",
            "pellet_rtg_strontium",
            "pellet_rtg_cobalt",
            "pellet_rtg_actinium",
            "pellet_rtg_polonium",
            "pellet_rtg_americium",
            "pellet_rtg_gold",
            "pellet_rtg_lead"
    ), rtgDepletedPelletItems(
            "pellet_rtg_depleted_bismuth",
            "pellet_rtg_depleted_mercury",
            "pellet_rtg_depleted_neptunium",
            "pellet_rtg_depleted_lead",
            "pellet_rtg_depleted_zirconium",
            "pellet_rtg_depleted_nickel"
    ), simpleStackOneItems(
            "reacher",
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
    ), ZIRNOX_ROD_ITEMS, PWR_FUEL_ITEMS, PWR_FUEL_HOT_ITEMS, PWR_FUEL_DEPLETED_ITEMS, List.of(PWR_PRINTER), WATZ_PELLET_ITEMS, WATZ_PELLET_DEPLETED_ITEMS, List.of(ICF_PELLET_EMPTY, ICF_PELLET, ICF_PELLET_DEPLETED, PARTICLE_MUON), List.of(RBMK_LID, RBMK_LID_GLASS, RBMK_FUEL_EMPTY), RBMK_FUEL_ROD_ITEMS, RBMK_PELLET_ITEMS, AMS_CATALYST_ITEMS, List.of(AMS_LENS, AMS_CORE_SING, AMS_CORE_WORMHOLE, AMS_CORE_EYEOFHARMONY), MACHINE_UPGRADE_ITEMS, LEGACY_TOOL_ITEMS, DRILLBIT_ITEMS, PISTON_SET_ITEMS, ARC_ELECTRODE_ITEMS, PA_COIL_ITEMS, ABILITY_TOOL_ITEMS, List.<RegistryObject<Item>>of(CATALYTIC_CONVERTER, SHREDDER_BLADES_STEEL, SHREDDER_BLADES_TITANIUM, SHREDDER_BLADES_DESH, SIREN_TRACK), SINGULARITY_FAMILY_ITEMS, CONTROL_BATTERY_ITEMS, List.of(MELTDOWN_TOOL))
            .flatMap(List::stream)
            .toList();

    static {
        registerControlTabLegacyMappings();
    }

    public static final List<RegistryObject<Item>> PARTS_TAB_ITEMS = Stream.concat(CRAYON_ITEMS.stream(), Stream.concat(Stream.of(
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
            LEAD_INGOT,
            STEEL_INGOT,
            COBALT_INGOT,
            ALUMINIUM_INGOT,
            BERYLLIUM_INGOT,
            SCHRABIDIUM_INGOT,
            MERCURY_TINY_DROP,
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
            BIO_WAFER,
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
            POWDER_FERTILIZER,
            POWDER_THERMITE,
            PELLET_GAS,
            SCRAP,
            FOUNDRY_SCRAPS,
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
            UPGRADE_MUFFLER,
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
            MISSILE_ASSEMBLY,
            WIRING_RED_COPPER,
            LASER_CRYSTAL_CO2,
            LASER_CRYSTAL_BISMUTH,
            LASER_CRYSTAL_CMB,
            LASER_CRYSTAL_DNT,
            LASER_CRYSTAL_DIGAMMA,
            INGOT_SEMTEX,
            INGOT_SMORE,
            BOTTLE_MERCURY,
            BOLT_SPIKE,
            DEUTERIUM_FILTER,
            RUNE_BLANK,
            RUNE_ISA,
            RUNE_DAGAZ,
            RUNE_HAGALAZ,
            RUNE_JERA,
            RUNE_THURISAZ,
            CRYSTAL_HORN,
            CRYSTAL_CHARRED
    ), Stream.concat(Stream.concat(SEDNA_GUN_PART_ITEMS.stream(),
            EXTRA_PARTS_TAB_ITEMS.stream()),
            HOT_SMITHING_ITEMS.stream()))).toList();

    public static final List<RegistryObject<Item>> WEAPON_TAB_ITEMS = Stream.concat(Stream.of(
            BIG_SWORD,
            REDSTONE_SWORD,
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
            STICK_DYNAMITE_FISHING,
            GRENADE_SHELL_FRAG,
            GRENADE_SHELL_STICK,
            GRENADE_SHELL_TECH,
            GRENADE_SHELL_NUKE,
            GRENADE_FUZE_S3,
            GRENADE_FUZE_S7,
            GRENADE_FUZE_S15,
            GRENADE_FUZE_IMPACT,
            GRENADE_FUZE_AIRBURST,
            GRENADE_FILLING_POWDER,
            GRENADE_FILLING_HE,
            GRENADE_FILLING_DEMO,
            GRENADE_FILLING_INC,
            GRENADE_FILLING_WP,
            GRENADE_FILLING_CLUSTER,
            GRENADE_FILLING_EMP,
            GRENADE_FILLING_PLASMA,
            GRENADE_FILLING_LASER,
            GRENADE_FILLING_CLUSTER_HEAVY,
            GRENADE_FILLING_NUCLEAR,
            GRENADE_FILLING_NUCLEAR_DEMO,
            GRENADE_FILLING_SCHRAB,
            GRENADE_EXTRA_GLUE,
            GRENADE_EXTRA_PROXY_FUZE,
            GRENADE_EXTRA_FRAG_SLEEVE,
            GRENADE_EXTRA_TRIPLEX,
            GRENADE_UNIVERSAL,
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
            GUN_PA_MELEE,
            GUN_PA_RANGED,
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
            POWER_NET_TOOL,
            ANALYSIS_TOOL,
            COUPLING_TOOL,
            BDCL,
            FOODITEM,
            BOMB_CALLER,
            BOMB_CALLER_NAPALM,
            BOMB_CALLER_CHLORINE,
            BOMB_CALLER_ORANGE,
            BOMB_CALLER_ATOMIC,
            ANCHOR_REMOTE,
            MATCHSTICK,
            BOBMAZON,
            BOAT_RUBBER,
            REBAR_PLACER,
            CART_EMPTY_WOOD,
            CART_EMPTY_STEEL,
            CART_EMPTY_PAINTED,
            CART_CRATE,
            CART_POWDER_WOOD,
            CART_POWDER_STEEL,
            CART_POWDER_PAINTED,
            CART_SEMTEX_WOOD,
            CART_SEMTEX_STEEL,
            CART_SEMTEX_PAINTED,
            CART_DESTROYER_STEEL,
            CART_DESTROYER_PAINTED,
            NUKE_ELECTRIC_KIT,
            LINKER,
            DRONE_LINKER,
            AMMO_BAG,
            AMMO_BAG_INFINITE,
            CASING_BAG,
            GUN_KIT_1,
            GUN_KIT_2,
            IV_EMPTY,
            IV_BLOOD,
            IV_XP_EMPTY,
            IV_XP,
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
            STEALTH_BOY,
            CIGARETTE,
            CRACKPIPE,
            MED_BAG,
            SYRINGE_EMPTY,
            SYRINGE_ANTIDOTE,
            SYRINGE_POISON,
            SYRINGE_AWESOME,
            SYRINGE_METAL_EMPTY,
            SYRINGE_METAL_STIMPAK,
            SYRINGE_METAL_MEDX,
            SYRINGE_METAL_PSYCHO,
            SYRINGE_METAL_SUPER,
            SYRINGE_TAINT,
            BOTTLE_EMPTY,
            BOTTLE2_EMPTY,
            COIN_TOKEN,
            CAP_NUKA,
            CAP_QUANTUM,
            CAP_SPARKLE,
            CAP_RAD,
            CAP_KORL,
            CAP_FRITZ,
            BOTTLE_NUKA,
            FLASK_INFUSION,
            BOTTLE_CHERRY,
            BOTTLE_QUANTUM,
            BOTTLE_SPARKLE,
            BOTTLE_RAD,
            BOTTLE2_KORL,
            BOTTLE2_FRITZ,
            BOTTLE_OPENER,
            RING_PULL,
            CAN_EMPTY,
            CAN_KEY,
            CAN_SMART,
            CAN_CREATURE,
            CAN_REDBOMB,
            CAN_MRSUGAR,
            CAN_OVERCHARGE,
            CAN_BEPIS,
            CAN_LUNA,
            CAN_MUG,
            CAN_BREEN,
            CANNED_BEEF,
            CANNED_TUNA,
            CANNED_MYSTERY,
            CANNED_PASHTET,
            CANNED_CHEESE,
            CANNED_SLIME,
            CANNED_MILK,
            CANNED_ASS,
            CANNED_PIZZA,
            CANNED_TUBE,
            CANNED_TOMATO,
            CANNED_ASBESTOS,
            CANNED_BHOLE,
            CANNED_HOTDOGS,
            CANNED_LEFTOVERS,
            CANNED_YOGURT,
            CANNED_STEW,
            CANNED_CHINESE,
            CANNED_OIL,
            CANNED_FIST,
            CANNED_SPAM,
            CANNED_FRIED,
            CANNED_NAPALM,
            CANNED_DIESEL,
            CANNED_KEROSENE,
            CANNED_RECURSION,
            CANNED_BARK,
            TEM_FLAKES,
            DEFINITELYFOOD,
            TWINKIE,
            NUGGET,
            BOMB_WAFFLE,
            PANCAKE,
            SCHNITZEL_VEGAN,
            COTTON_CANDY,
            APPLE_LEAD,
            APPLE_LEAD_INGOT,
            APPLE_LEAD_BLOCK,
            APPLE_SCHRABIDIUM,
            APPLE_SCHRABIDIUM_INGOT,
            APPLE_SCHRABIDIUM_BLOCK,
            COFFEE,
            COFFEE_RADIUM,
            CHOCOLATE_MILK,
            LEMON,
            PUDDING,
            SPONGEBOB_MACARONI,
            STATIC_SANDWICH,
            CHEESE,
            QUESADILLA,
            MUCHO_MANGO,
            MED_IPECAC,
            MED_PTSD,
            CHOCOLATE,
            GLOWING_STEW,
            BALEFIRE_SCRAMBLED,
            BALEFIRE_AND_HAM,
            PEAS,
            SPAWN_DUCK,
            MARSHMALLOW,
            RECORD_LC,
            RECORD_SS,
            RECORD_VC,
            CANTEEN_VODKA,
            GLYPHID_MEAT,
            GLYPHID_MEAT_GRILLED,
            EGG_GLYPHID,
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
            CHEMISTRY_SET,
            CHEMISTRY_SET_BORON,
            FLUID_DUCT
    ).toList();

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static void registerToolStacks() {
        Toolable.ToolType.SCREWDRIVER.register(new ItemStack(SCREWDRIVER.get()));
        Toolable.ToolType.SCREWDRIVER.register(new ItemStack(SCREWDRIVER_DESH.get()));
        Toolable.ToolType.HAND_DRILL.register(new ItemStack(HAND_DRILL.get()));
        Toolable.ToolType.HAND_DRILL.register(new ItemStack(HAND_DRILL_DESH.get()));
        Toolable.ToolType.WRENCH.register(new ItemStack(WRENCH.get()));
        Toolable.ToolType.WRENCH.register(new ItemStack(WRENCH_ARCHINEER.get()));
        Toolable.ToolType.TORCH.register(new ItemStack(BLOWTORCH.get()));
        Toolable.ToolType.TORCH.register(new ItemStack(ACETYLENE_TORCH.get()));
        Toolable.ToolType.BOLT.register(new ItemStack(BOLTGUN.get()));
        Toolable.ToolType.DEFUSER.register(new ItemStack(DEFUSER.get()));
    }

    public static RegistryObject<Item> legacyItem(String name) {
        return ITEMS_BY_LEGACY_NAME.get(name);
    }

    private static RegistryObject<Item> requireRegisteredLegacyItem(String name) {
        RegistryObject<Item> item = legacyItem(name);
        if (item == null) {
            throw new IllegalStateException("Missing modern item for legacy meta mapping: " + name);
        }
        return item;
    }

    private static Supplier<Item> legacyItemSupplier(String name) {
        return () -> requireRegisteredLegacyItem(name).get();
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

    private static ItemStack namedCustomMissile(String name, ChatFormatting nameColor,
            String chip, String warhead, String fuselage, String stability, String thruster) {
        ItemStack missile = CustomMissileItem.buildMissile(
                new ItemStack(requireRegisteredLegacyItem(chip).get()),
                new ItemStack(requireRegisteredLegacyItem(warhead).get()),
                new ItemStack(requireRegisteredLegacyItem(fuselage).get()),
                stability == null ? null : new ItemStack(requireRegisteredLegacyItem(stability).get()),
                new ItemStack(requireRegisteredLegacyItem(thruster).get()));
        missile.setHoverName(Component.literal(name).withStyle(nameColor));
        return missile;
    }

    private static List<RegistryObject<Item>> simpleParts(String... names) {
        return Stream.of(names).map(ModItems::simpleItem).toList();
    }

    private static List<RegistryObject<Item>> simpleStackOneItems(String... names) {
        return Stream.of(names).map(ModItems::simpleStackOneItem).toList();
    }

    private static List<RegistryObject<Item>> breedingRodItems(RodFamily family) {
        return Stream.of(BreedingRodType.values())
                .map(type -> registerLegacy(family.registryName(type),
                        () -> new ItemBreedingRod(new Item.Properties(), family, type)))
                .toList();
    }

    private static void registerControlTabLegacyMappings() {
        LegacyMetaItemMappings.registerSparse(LegacyMetaItemMappings.FUEL_ADDITIVE, Map.of(
                0, requireRegisteredLegacyItem("fuel_additive_antiknock"),
                1, requireRegisteredLegacyItem("fuel_additive_deicer")));
        LegacyMetaItemMappings.register(LegacyMetaItemMappings.PELLET_RTG_DEPLETED,
                requireRegisteredLegacyItem("pellet_rtg_depleted_bismuth"),
                requireRegisteredLegacyItem("pellet_rtg_depleted_mercury"),
                requireRegisteredLegacyItem("pellet_rtg_depleted_neptunium"),
                requireRegisteredLegacyItem("pellet_rtg_depleted_lead"),
                requireRegisteredLegacyItem("pellet_rtg_depleted_zirconium"),
                requireRegisteredLegacyItem("pellet_rtg_depleted_nickel"));
        LegacyMetaItemMappings.registerSparse(LegacyMetaItemMappings.ROD_QUAD_EMPTY,
                Map.of(-1, requireRegisteredLegacyItem("rod_quad_empty")));
        LegacyMetaItemMappings.registerSparse(LegacyMetaItemMappings.ROD, breedingRodLegacyMap(RodFamily.SINGLE));
        LegacyMetaItemMappings.registerSparse(LegacyMetaItemMappings.ROD_DUAL, breedingRodLegacyMap(RodFamily.DUAL));
        LegacyMetaItemMappings.registerSparse(LegacyMetaItemMappings.ROD_QUAD, breedingRodLegacyMap(RodFamily.QUAD));
    }

    private static Map<Integer, RegistryObject<Item>> breedingRodLegacyMap(RodFamily family) {
        Map<Integer, RegistryObject<Item>> variants = new LinkedHashMap<>();
        for (BreedingRodType type : BreedingRodType.values()) {
            variants.put(type.ordinal(), requireRegisteredLegacyItem(family.registryName(type)));
        }
        return variants;
    }

    private static List<RegistryObject<Item>> rtgPelletItems(String... names) {
        return Stream.of(names)
                .map(name -> registerLegacy(name, () -> new ItemRTGPellet(new Item.Properties())))
                .toList();
    }

    private static List<RegistryObject<Item>> rtgDepletedPelletItems(String... names) {
        return Stream.of(names)
                .map(name -> registerLegacy(name, () -> new ItemRTGPelletDepleted(new Item.Properties())))
                .toList();
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

    private static List<RegistryObject<Item>> chemicalDyeItems(ChemicalDyeSpec... specs) {
        return Stream.of(specs).map(spec -> chemicalDyeItem(spec.name(), spec.tintColor())).toList();
    }

    private static List<RegistryObject<Item>> crayonItems(CrayonSpec... specs) {
        return Stream.of(specs).map(spec -> crayonItem(spec.name(), spec.tintColor())).toList();
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

    private static RegistryObject<Item> zirnoxRod(String name, EnumZirnoxType type) {
        return registerLegacy(name,
                () -> new ItemZirnoxRod(new Item.Properties().stacksTo(1).durability(type.maxLife), type));
    }

    private static List<RegistryObject<Item>> pwrFuels() {
        return Stream.of(PWRFuelRuntime.Type.values())
                .map(type -> registerLegacy("pwr_fuel_" + type.suffix(),
                        () -> new ItemPWRFuel(new Item.Properties(), type)))
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
        return registerLegacy(name, () -> new ItemRBMKRod(new Item.Properties(),
                RBMKFuelRodRegistry.find(name).orElseThrow(() -> new IllegalArgumentException("Unknown RBMK fuel rod: " + name))));
    }

    private static RegistryObject<Item> rbmkPellet(String name) {
        return registerLegacy(name, () -> new ItemRBMKPellet(new Item.Properties(),
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

    private static RegistryObject<Item> mobSpawnEgg(String name,
                                                     Supplier<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> entityType,
                                                     int backgroundColor, int highlightColor) {
        return registerLegacy(name, () -> new ForgeSpawnEggItem(entityType, backgroundColor, highlightColor, new Item.Properties()));
    }

    private static RegistryObject<Item> holotape(String name, ItemHolotapeImage.EnumHoloImage type) {
        return registerLegacy(name, () -> new ItemHolotapeImage(new Item.Properties(), type));
    }

    private static RegistryObject<Item> hotItem(String name, int maxHeat) {
        return registerLegacy(name, () -> new HotItem(new Item.Properties(), maxHeat));
    }

    private static List<RegistryObject<Item>> hotDustedSteelItems() {
        List<RegistryObject<Item>> items = new ArrayList<>(10);
        for (int forged = 0; forged < 10; forged++) {
            int forgedCount = forged;
            String name = forged == 0 ? "ingot_steel_dusted" : "ingot_steel_dusted_" + forged;
            items.add(registerLegacy(name, () -> new HotDustedItem(new Item.Properties(), 200, forgedCount)));
        }
        return List.copyOf(items);
    }

    private static RegistryObject<Item> flatStamp(String name, int durability) {
        return pressStamp(name, durability, ItemPressStamp.StampType.FLAT);
    }

    private static RegistryObject<Item> pressStamp(String name, int durability, ItemPressStamp.StampType stampType) {
        return registerLegacy(name,
                () -> new ItemPressStamp(stampProperties(durability), stampType));
    }

    private static Item.Properties stampProperties(int durability) {
        return durability > 0 ? new Item.Properties().durability(durability) : new Item.Properties().stacksTo(1);
    }

    private static RegistryObject<Item> hazmatArmor(String name, HbmArmorMaterials material, ArmorItem.Type type) {
        return registerLegacy(name, () -> type == ArmorItem.Type.HELMET
                ? new ArmorHazmatMask(material)
                : new ArmorHazmat(material, type, new Item.Properties()));
    }

    private static RegistryObject<Item> asbestosArmor(String name, ArmorItem.Type type) {
        return registerLegacy(name, () -> new ArmorItem(HbmArmorMaterials.ASBESTOS, type, new Item.Properties()));
    }

    private static RegistryObject<Item> armor(String name, HbmArmorMaterials material, ArmorItem.Type type) {
        return registerLegacy(name, () -> new ArmorItem(material, type, new Item.Properties()));
    }

    private static RegistryObject<Item> modArmor(String name, HbmArmorMaterials material, ArmorItem.Type type) {
        return registerLegacy(name, () -> new ModArmor(material, type));
    }

    private static RegistryObject<Item> euphemiumArmor(String name, ArmorItem.Type type) {
        return registerLegacy(name, () -> new ArmorEuphemium(type, new Item.Properties()));
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
        return registerLegacy(name, () -> new ArmorFSB(material, type, new Item.Properties(), effects,
                noHelmet, dashCount, traits));
    }

    private static RegistryObject<Item> bismuthArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, boolean noHelmet, int dashCount) {
        return registerLegacy(name, () -> new ArmorBismuth(material, type, new Item.Properties(), effects,
                noHelmet, dashCount, FsbArmorItem.FullSetTraits.NONE));
    }

    private static RegistryObject<Item> fsbPoweredArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain) {
        return fsbPoweredArmor(name, material, type, effects, maxCharge, chargeRate, consumption, drain,
                FsbArmorItem.FullSetTraits.NONE);
    }

    private static RegistryObject<Item> fsbPoweredArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorFSBPowered(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> fauArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorDigamma(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> hevArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorHEV(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> taurunArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, boolean noHelmet, int dashCount,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorTaurun(material, type, new Item.Properties(), effects,
                noHelmet, dashCount, traits));
    }

    private static RegistryObject<Item> t51Armor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorT51(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> rpaArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorRPA(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> ajrArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorAJR(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> ajroArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorAJRO(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> ncrpaArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorNCRPA(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> envSuitArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain) {
        return registerLegacy(name, () -> new ArmorEnvsuit(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain));
    }

    private static RegistryObject<Item> bjArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorBJ(material, type, new Item.Properties(), effects,
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
        return registerLegacy(name, () -> new ArmorBJJetpack(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> dnsArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, long maxCharge, long chargeRate, long consumption, long drain,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorDNT(material, type, new Item.Properties(), effects,
                maxCharge, chargeRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> trenchmasterArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, boolean noHelmet, int dashCount,
            FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorTrenchmaster(material, type, new Item.Properties(), effects,
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
        return registerLegacy(name, () -> new ArmorFSBFueled(material, type, new Item.Properties(), effects,
                fuel, maxFuel, fillRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> steamsuitArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, FluidType fuel, int maxFuel, int fillRate, int consumption,
            int drain, FsbArmorItem.FullSetTraits traits) {
        return registerLegacy(name, () -> new ArmorDesh(material, type, new Item.Properties(), effects,
                fuel, maxFuel, fillRate, consumption, drain, traits));
    }

    private static RegistryObject<Item> fsbFueledArmor(String name, HbmArmorMaterials material, ArmorItem.Type type,
            List<FsbArmorItem.FullSetEffect> effects, int maxFuel, int fillRate, int consumption, int drain,
            FluidType... fuels) {
        return registerLegacy(name, () -> new ArmorFSBFueled(material, type, new Item.Properties(), effects,
                maxFuel, fillRate, consumption, drain, fuels));
    }

    private static RegistryObject<Item> dieselSuitArmor(String name, ArmorItem.Type type) {
        return registerLegacy(name, () -> new ArmorDiesel(HbmArmorMaterials.DIESEL, type, new Item.Properties(),
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
                FsbArmorItem.effect(MobEffects.SATURATION, 20, 0, "Saturation I"),
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
        return registerLegacy(name, () -> new ArmorHazmatMask(material));
    }

    private static RegistryObject<Item> liquidatorMaskArmor(String name) {
        return registerLegacy(name, ArmorLiquidatorMask::new);
    }

    private static RegistryObject<Item> liquidatorArmor(String name, ArmorItem.Type type) {
        return registerLegacy(name, () -> new ArmorLiquidator(type, new Item.Properties()));
    }

    private static RegistryObject<Item> gasMaskArmor(String name, boolean mono) {
        return registerLegacy(name, () -> new ArmorGasMask(mono));
    }

    private static RegistryObject<Item> ironHeadArmor(String name) {
        return registerLegacy(name, () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    }

    private static RegistryObject<Item> objIronHeadArmor(String name) {
        return registerLegacy(name, () -> new ArmorModel(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    }

    private static RegistryObject<Item> ashGlassesArmor(String name) {
        return registerLegacy(name, () -> new ArmorAshGlasses(ArmorMaterials.IRON, ArmorItem.Type.HELMET,
                new Item.Properties()));
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

    private static RegistryObject<Item> hoe(String name, HbmToolTiers tier) {
        return registerLegacy(name, () -> new HoeItem(tier, -3, 0.0F, toolProperties(tier, false)));
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

    private static RegistryObject<Item> conserve(LegacyConserveItem.Type type) {
        return registerLegacy(type.registryName(),
                () -> new LegacyConserveItem(new Item.Properties().food(type.foodProperties()), type));
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

    private static RegistryObject<Item> chemicalDyeItem(String name, int tintColor) {
        return registerLegacy(name, () -> new ChemicalDyeItem(new Item.Properties(), tintColor));
    }

    private static RegistryObject<Item> crayonItem(String name, int tintColor) {
        return registerLegacy(name, () -> new LegacyCrayonItem(new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(3)
                .saturationMod(0.6F)
                .alwaysEat()
                .build()), tintColor));
    }

    private static RegistryObject<Item> pressStampItem(String name, ItemPressStamp.StampType stampType) {
        return registerLegacy(name, () -> new ItemPressStamp(new Item.Properties().stacksTo(1), stampType));
    }

    private static Item createSimpleItem(String name) {
        if ("ingot_arsenic".equals(name)) {
            return new ItemCustomLore(new Item.Properties());
        }
        if (isLegacyUncommonLorePart(name)) {
            return new ItemCustomLore(new Item.Properties()).setRarity(Rarity.UNCOMMON);
        }
        if (hasCellEmptyCraftingRemainder(name)) {
            return new Item(new Item.Properties().craftRemainder(CELL_EMPTY.get()));
        }
        if ("mike_deut".equals(name)) {
            return new Item(new Item.Properties().stacksTo(1).craftRemainder(requireRegisteredLegacyItem("tank_steel").get()));
        }
        if (isLegacyDepletedFuel(name)) {
            return new ItemDepletedFuel(new Item.Properties());
        }
        if (isLegacyLongWaste(name)) {
            return new ItemWasteLong(new Item.Properties());
        }
        if (isLegacyShortWaste(name)) {
            return new ItemWasteShort(new Item.Properties());
        }
        if (isLegacySimpleNuclearWaste(name)) {
            return new ItemNuclearWaste(new Item.Properties());
        }
        return new Item(new Item.Properties());
    }

    private static boolean hasCellEmptyCraftingRemainder(String name) {
        return "cell_deuterium".equals(name)
                || "cell_tritium".equals(name)
                || "cell_uf6".equals(name)
                || "cell_puf6".equals(name)
                || "cell_balefire".equals(name);
    }

    private static Item createSimpleStackSizeItem(String name, int maxStackSize) {
        Item.Properties properties = simpleStackSizeProperties(name, maxStackSize);
        if (isLegacyResearchReactorPlateFuel(name)) {
            return new ResearchReactorPlateFuelItem(properties);
        }
        if (isLegacyFleijaPart(name)) {
            return new ItemFleija(properties, "fleija_propellant".equals(name));
        }
        if ("cell_sas3".equals(name)) {
            return new ItemCustomLore(properties.craftRemainder(CELL_EMPTY.get())).setRarity(Rarity.RARE);
        }
        return new Item(properties);
    }

    private static boolean isLegacyResearchReactorPlateFuel(String name) {
        return switch (name) {
            case "plate_fuel_u233",
                 "plate_fuel_u235",
                 "plate_fuel_mox",
                 "plate_fuel_pu239",
                 "plate_fuel_sa326",
                 "plate_fuel_ra226be",
                 "plate_fuel_pu238be" -> true;
            default -> false;
        };
    }

    private static boolean isLegacyUncommonLorePart(String name) {
        return "billet_australium".equals(name)
                || "billet_australium_lesser".equals(name)
                || "billet_australium_greater".equals(name)
                || "billet_balefire_gold".equals(name)
                || "billet_flashlead".equals(name);
    }

    private static boolean isLegacyFleijaPart(String name) {
        return "fleija_igniter".equals(name)
                || "fleija_propellant".equals(name)
                || "fleija_core".equals(name);
    }

    private static boolean isLegacyLongWaste(String name) {
        return "nuclear_waste_long".equals(name)
                || "nuclear_waste_long_tiny".equals(name)
                || "nuclear_waste_long_depleted".equals(name)
                || "nuclear_waste_long_depleted_tiny".equals(name);
    }

    private static boolean isLegacyShortWaste(String name) {
        return "nuclear_waste_short".equals(name)
                || "nuclear_waste_short_tiny".equals(name)
                || "nuclear_waste_short_depleted".equals(name)
                || "nuclear_waste_short_depleted_tiny".equals(name);
    }

    private static boolean isLegacySimpleNuclearWaste(String name) {
        return "trinitite".equals(name)
                || "nuclear_waste".equals(name)
                || "nuclear_waste_tiny".equals(name)
                || "nuclear_waste_vitrified".equals(name)
                || "nuclear_waste_vitrified_tiny".equals(name);
    }

    private static RegistryObject<Item> simpleStackOneItem(String name) {
        return simpleStackSizeItem(name, 1);
    }

    private static RegistryObject<Item> simpleStackSizeItem(String name, int maxStackSize) {
        RegistryObject<Item> item = ITEMS.register(name,
                () -> createSimpleStackSizeItem(name, maxStackSize));
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

    private record ChemicalDyeSpec(String name, int tintColor) {
    }

    private record CrayonSpec(String name, int tintColor) {
    }

    private record PressStampSpec(String name, ItemPressStamp.StampType stampType) {
    }

    private ModItems() {
    }
}
