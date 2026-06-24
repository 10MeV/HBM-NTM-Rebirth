package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyModelReloadListener;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.particle.AshesParticle;
import com.hbm.ntm.client.particle.BlackPowderSmokeParticle;
import com.hbm.ntm.client.particle.BlackPowderSparkParticle;
import com.hbm.ntm.client.particle.ChaosCloudParticle;
import com.hbm.ntm.client.particle.CoolingTowerParticle;
import com.hbm.ntm.client.particle.DeadLeafParticle;
import com.hbm.ntm.client.particle.DigammaSmokeParticle;
import com.hbm.ntm.client.particle.ExplosionSmallParticle;
import com.hbm.ntm.client.particle.FlamethrowerParticle;
import com.hbm.ntm.client.particle.FluidFillParticle;
import com.hbm.ntm.client.particle.FoamParticle;
import com.hbm.ntm.client.particle.GasFlameParticle;
import com.hbm.ntm.client.particle.GibletParticle;
import com.hbm.ntm.client.particle.HadronParticle;
import com.hbm.ntm.client.particle.HazeParticle;
import com.hbm.ntm.client.particle.HbmSmokeParticle;
import com.hbm.ntm.client.particle.LargeExplodeParticle;
import com.hbm.ntm.client.particle.LegacyContrailParticle;
import com.hbm.ntm.client.particle.LegacySplashParticle;
import com.hbm.ntm.client.particle.MukeWaveParticle;
import com.hbm.ntm.client.particle.NetworkDebugParticle;
import com.hbm.ntm.client.particle.PlasmaBlastParticle;
import com.hbm.ntm.client.particle.RadiationFogParticle;
import com.hbm.ntm.client.particle.RbmkAnimatedParticle;
import com.hbm.ntm.client.particle.SchrabFogParticle;
import com.hbm.ntm.client.particle.SmokePlumeParticle;
import com.hbm.ntm.client.particle.TownAuraParticle;
import com.hbm.ntm.client.render.HbmBlackHoleEffects;
import com.hbm.ntm.client.render.HbmRenderEffects;
import com.hbm.ntm.client.particle.RocketFlameParticle;
import com.hbm.ntm.client.renderer.AssemblyFactoryRenderer;
import com.hbm.ntm.client.renderer.AssemblyMachineRenderer;
import com.hbm.ntm.client.renderer.AntiBallisticMissileRenderer;
import com.hbm.ntm.client.renderer.ArtilleryRocketRenderer;
import com.hbm.ntm.client.renderer.ArtilleryShellRenderer;
import com.hbm.ntm.client.renderer.ArcFurnaceRenderer;
import com.hbm.ntm.client.renderer.AutosawRenderer;
import com.hbm.ntm.client.renderer.BalefireBombRenderer;
import com.hbm.ntm.client.renderer.BasicMachineRenderer;
import com.hbm.ntm.client.renderer.BombMultiRenderer;
import com.hbm.ntm.client.renderer.BreedingReactorRenderer;
import com.hbm.ntm.client.renderer.BulletProjectileRenderer;
import com.hbm.ntm.client.renderer.CableDiodeRenderer;
import com.hbm.ntm.client.renderer.CargoElevatorRenderer;
import com.hbm.ntm.client.renderer.ChemicalFactoryRenderer;
import com.hbm.ntm.client.renderer.ChemicalPlantRenderer;
import com.hbm.ntm.client.renderer.ChemicalProjectileRenderer;
import com.hbm.ntm.client.renderer.ChargerRenderer;
import com.hbm.ntm.client.renderer.ChungusRenderer;
import com.hbm.ntm.client.renderer.CloudFleijaRenderer;
import com.hbm.ntm.client.renderer.CloudFleijaRainbowRenderer;
import com.hbm.ntm.client.renderer.CloudSoliniumRenderer;
import com.hbm.ntm.client.renderer.CoinRenderer;
import com.hbm.ntm.client.renderer.CogRenderer;
import com.hbm.ntm.client.renderer.CraneSplitterRenderer;
import com.hbm.ntm.client.renderer.CustomNukeRenderer;
import com.hbm.ntm.client.renderer.CustomMissileRenderer;
import com.hbm.ntm.client.renderer.CustomMissileLauncherRenderer;
import com.hbm.ntm.client.renderer.DeathBlastRenderer;
import com.hbm.ntm.client.renderer.DigammaSpearRenderer;
import com.hbm.ntm.client.renderer.EmpBlastRenderer;
import com.hbm.ntm.client.renderer.ElectricPressRenderer;
import com.hbm.ntm.client.renderer.ExposureChamberRenderer;
import com.hbm.ntm.client.renderer.FalloutRainRenderer;
import com.hbm.ntm.client.renderer.FallingNukeRenderer;
import com.hbm.ntm.client.renderer.FensuRenderer;
import com.hbm.ntm.client.renderer.ForceFieldRenderer;
import com.hbm.ntm.client.renderer.FluidDuctBoxRenderer;
import com.hbm.ntm.client.renderer.FluidDuctPaintableRenderer;
import com.hbm.ntm.client.renderer.FluidBarrelRenderer;
import com.hbm.ntm.client.renderer.FluidPipeRenderer;
import com.hbm.ntm.client.renderer.FluidPipeAnchorRenderer;
import com.hbm.ntm.client.renderer.FluidTankRenderer;
import com.hbm.ntm.client.renderer.FusionBoilerRenderer;
import com.hbm.ntm.client.renderer.FusionBreederRenderer;
import com.hbm.ntm.client.renderer.FusionCollectorRenderer;
import com.hbm.ntm.client.renderer.FusionCouplerRenderer;
import com.hbm.ntm.client.renderer.FusionKlystronRenderer;
import com.hbm.ntm.client.renderer.FusionKlystronCreativeRenderer;
import com.hbm.ntm.client.renderer.FusionMHDTRenderer;
import com.hbm.ntm.client.renderer.FusionPlasmaForgeRenderer;
import com.hbm.ntm.client.renderer.FusionTorusStructCoreRenderer;
import com.hbm.ntm.client.renderer.FusionTorusRenderer;
import com.hbm.ntm.client.renderer.GeigerRenderer;
import com.hbm.ntm.client.renderer.HexafluorideTankRenderer;
import com.hbm.ntm.client.renderer.IndustrialSteamTurbineRenderer;
import com.hbm.ntm.client.renderer.ICFControllerRenderer;
import com.hbm.ntm.client.renderer.ICFReactorRenderer;
import com.hbm.ntm.client.renderer.ICFStructCoreRenderer;
import com.hbm.ntm.client.renderer.LegacyChargeBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyDemonLampBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyEmitterBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyFanRenderer;
import com.hbm.ntm.client.renderer.LegacyFileCabinetRenderer;
import com.hbm.ntm.client.renderer.LegacyLanternBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyLargeTurbineRenderer;
import com.hbm.ntm.client.renderer.LegacyLightBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyPylonRenderer;
import com.hbm.ntm.client.renderer.LegacyPrimedExplosiveRenderer;
import com.hbm.ntm.client.renderer.LegacyVisibleMachineRenderer;
import com.hbm.ntm.client.renderer.LaunchPadRenderer;
import com.hbm.ntm.client.renderer.LargeLaunchPadRenderer;
import com.hbm.ntm.client.renderer.LiquefactorRenderer;
import com.hbm.ntm.client.renderer.MachineBatterySocketRenderer;
import com.hbm.ntm.client.renderer.MachineLpw2Renderer;
import com.hbm.ntm.client.renderer.MicrowaveRenderer;
import com.hbm.ntm.client.renderer.MiningLaserRenderer;
import com.hbm.ntm.client.renderer.MinerRocketRenderer;
import com.hbm.ntm.client.renderer.MissileRenderer;
import com.hbm.ntm.client.renderer.MissileAssemblyRenderer;
import com.hbm.ntm.client.renderer.MultiblockDummyRenderer;
import com.hbm.ntm.client.renderer.MovingPackageRenderer;
import com.hbm.ntm.client.renderer.MovingItemRenderer;
import com.hbm.ntm.client.renderer.NuclearDeviceRenderer;
import com.hbm.ntm.client.renderer.NukeTorexRenderer;
import com.hbm.ntm.client.renderer.OilDrillRenderer;
import com.hbm.ntm.client.renderer.OreSlopperRenderer;
import com.hbm.ntm.client.renderer.ParticleAcceleratorRenderer;
import com.hbm.ntm.client.renderer.PyroOvenRenderer;
import com.hbm.ntm.client.renderer.RadarRenderer;
import com.hbm.ntm.client.renderer.RadarScreenRenderer;
import com.hbm.ntm.client.renderer.ResearchReactorRenderer;
import com.hbm.ntm.client.renderer.RadioDecoRenderer;
import com.hbm.ntm.client.renderer.RadioAutocalRenderer;
import com.hbm.ntm.client.renderer.RadioTelexRenderer;
import com.hbm.ntm.client.renderer.RadioTorchRenderer;
import com.hbm.ntm.client.renderer.RBMKAutoloaderRenderer;
import com.hbm.ntm.client.renderer.RBMKColumnRenderer;
import com.hbm.ntm.client.renderer.RBMKConsoleRenderer;
import com.hbm.ntm.client.renderer.RBMKCraneConsoleRenderer;
import com.hbm.ntm.client.renderer.RBMKDebrisRenderer;
import com.hbm.ntm.client.renderer.RBMKPanelRenderer;
import com.hbm.ntm.client.renderer.RedCableRenderer;
import com.hbm.ntm.client.renderer.RefuelerRenderer;
import com.hbm.ntm.client.renderer.RubbleRenderer;
import com.hbm.ntm.client.renderer.RustedLaunchPadRenderer;
import com.hbm.ntm.client.renderer.SawbladeRenderer;
import com.hbm.ntm.client.renderer.ShrapnelRenderer;
import com.hbm.ntm.client.renderer.SolidifierRenderer;
import com.hbm.ntm.client.renderer.SolderingStationRenderer;
import com.hbm.ntm.client.renderer.SoyuzCapsuleBlockEntityRenderer;
import com.hbm.ntm.client.renderer.SoyuzCapsuleRenderer;
import com.hbm.ntm.client.renderer.SoyuzLauncherRenderer;
import com.hbm.ntm.client.renderer.SoyuzMultiblockGhostRenderer;
import com.hbm.ntm.client.renderer.SoyuzRenderer;
import com.hbm.ntm.client.renderer.SolarMirrorRenderer;
import com.hbm.ntm.client.renderer.SteamEngineRenderer;
import com.hbm.ntm.client.renderer.StorageDrumRenderer;
import com.hbm.ntm.client.renderer.TeslaRenderer;
import com.hbm.ntm.client.renderer.ThresherRenderer;
import com.hbm.ntm.client.renderer.TrinketBlockEntityRenderer;
import com.hbm.ntm.client.renderer.TurbofanRenderer;
import com.hbm.ntm.client.renderer.TurbineGasRenderer;
import com.hbm.ntm.client.renderer.WatzReactorRenderer;
import com.hbm.ntm.client.renderer.WatzStructCoreRenderer;
import com.hbm.ntm.client.renderer.ZirnoxDestroyedRenderer;
import com.hbm.ntm.client.renderer.ZirnoxDebrisRenderer;
import com.hbm.ntm.client.renderer.ZirnoxReactorRenderer;
import com.hbm.ntm.client.renderer.TurretBlockEntityRenderer;
import com.hbm.ntm.client.renderer.VendingMachineRenderer;
import com.hbm.ntm.client.screen.AnnihilatorScreen;
import com.hbm.ntm.client.screen.AnvilScreen;
import com.hbm.ntm.client.screen.AutocrafterScreen;
import com.hbm.ntm.client.screen.ArmorTableScreen;
import com.hbm.ntm.client.screen.AmmoBagScreen;
import com.hbm.ntm.client.screen.AmmoPressScreen;
import com.hbm.ntm.client.screen.ArcFurnaceScreen;
import com.hbm.ntm.client.screen.ArcWelderScreen;
import com.hbm.ntm.client.screen.AshpitScreen;
import com.hbm.ntm.client.screen.BalefireBombScreen;
import com.hbm.ntm.client.screen.BatteryReddScreen;
import com.hbm.ntm.client.screen.BasicMachineScreen;
import com.hbm.ntm.client.screen.BlastFurnaceScreen;
import com.hbm.ntm.client.screen.BrickFurnaceScreen;
import com.hbm.ntm.client.screen.BreedingReactorScreen;
import com.hbm.ntm.client.screen.BombMultiScreen;
import com.hbm.ntm.client.screen.AssemblyFactoryScreen;
import com.hbm.ntm.client.screen.AssemblyMachineScreen;
import com.hbm.ntm.client.screen.ChemicalFactoryScreen;
import com.hbm.ntm.client.screen.ChemicalPlantScreen;
import com.hbm.ntm.client.screen.CasingBagScreen;
import com.hbm.ntm.client.screen.CompactLauncherScreen;
import com.hbm.ntm.client.screen.CompressorScreen;
import com.hbm.ntm.client.screen.CombustionEngineScreen;
import com.hbm.ntm.client.screen.CombinationOvenScreen;
import com.hbm.ntm.client.screen.CraneLogisticsScreen;
import com.hbm.ntm.client.screen.CrateScreen;
import com.hbm.ntm.client.screen.CrucibleScreen;
import com.hbm.ntm.client.screen.CustomNukeScreen;
import com.hbm.ntm.client.screen.CyclotronScreen;
import com.hbm.ntm.client.screen.DiFurnaceScreen;
import com.hbm.ntm.client.screen.DiFurnaceRtgScreen;
import com.hbm.ntm.client.screen.DieselGeneratorScreen;
import com.hbm.ntm.client.screen.ElectrolyserScreen;
import com.hbm.ntm.client.screen.ElectricFurnaceScreen;
import com.hbm.ntm.client.screen.ElectricPressScreen;
import com.hbm.ntm.client.screen.ExposureChamberScreen;
import com.hbm.ntm.client.screen.FireboxHeaterScreen;
import com.hbm.ntm.client.screen.FelScreen;
import com.hbm.ntm.client.screen.FileCabinetScreen;
import com.hbm.ntm.client.screen.ForceFieldScreen;
import com.hbm.ntm.client.screen.FunnelScreen;
import com.hbm.ntm.client.screen.FluidTankScreen;
import com.hbm.ntm.client.screen.FluidPumpScreen;
import com.hbm.ntm.client.renderer.FoundryRenderer;
import com.hbm.ntm.client.screen.FusionBreederScreen;
import com.hbm.ntm.client.screen.FusionKlystronScreen;
import com.hbm.ntm.client.screen.FusionPlasmaForgeScreen;
import com.hbm.ntm.client.screen.FusionTorusScreen;
import com.hbm.ntm.client.screen.GasCentScreen;
import com.hbm.ntm.client.screen.GasFlareScreen;
import com.hbm.ntm.client.screen.HeaterHeatexScreen;
import com.hbm.ntm.client.screen.ICFPressScreen;
import com.hbm.ntm.client.screen.ICFReactorScreen;
import com.hbm.ntm.client.screen.KeyForgeScreen;
import com.hbm.ntm.client.screen.LaunchPadScreen;
import com.hbm.ntm.client.screen.LaunchTableScreen;
import com.hbm.ntm.client.screen.LegacyFurnaceScreen;
import com.hbm.ntm.client.screen.LiquefactorScreen;
import com.hbm.ntm.client.screen.MachineBatteryScreen;
import com.hbm.ntm.client.screen.MachineBatterySocketScreen;
import com.hbm.ntm.client.screen.MassStorageScreen;
import com.hbm.ntm.client.screen.MicrowaveScreen;
import com.hbm.ntm.client.screen.MissileAssemblyScreen;
import com.hbm.ntm.client.screen.MiningLaserScreen;
import com.hbm.ntm.client.screen.MixerScreen;
import com.hbm.ntm.client.screen.NuclearDeviceScreen;
import com.hbm.ntm.client.screen.OilDrillScreen;
import com.hbm.ntm.client.screen.OilburnerScreen;
import com.hbm.ntm.client.screen.OreSlopperScreen;
import com.hbm.ntm.client.screen.ParticleAcceleratorScreen;
import com.hbm.ntm.client.screen.PrecassScreen;
import com.hbm.ntm.client.screen.PyroOvenScreen;
import com.hbm.ntm.client.screen.ProcessingMachineScreen;
import com.hbm.ntm.client.screen.PurexScreen;
import com.hbm.ntm.client.screen.PWRScreen;
import com.hbm.ntm.client.screen.RadGenScreen;
import com.hbm.ntm.client.screen.ReactorControlScreen;
import com.hbm.ntm.client.screen.ResearchReactorScreen;
import com.hbm.ntm.client.screen.RadioAutocalScreen;
import com.hbm.ntm.client.screen.RadioReceiverScreen;
import com.hbm.ntm.client.screen.RadioTelexScreen;
import com.hbm.ntm.client.screen.RadioTorchScreen;
import com.hbm.ntm.client.screen.RBMKAutoloaderScreen;
import com.hbm.ntm.client.screen.RBMKBoilerScreen;
import com.hbm.ntm.client.screen.RBMKControlAutoScreen;
import com.hbm.ntm.client.screen.RBMKControlScreen;
import com.hbm.ntm.client.screen.RBMKConsoleScreen;
import com.hbm.ntm.client.screen.RBMKHeaterScreen;
import com.hbm.ntm.client.screen.RBMKOutgasserScreen;
import com.hbm.ntm.client.screen.RBMKPanelScreen;
import com.hbm.ntm.client.screen.RBMKRodScreen;
import com.hbm.ntm.client.screen.RBMKStorageScreen;
import com.hbm.ntm.client.screen.RadarScreen;
import com.hbm.ntm.client.screen.RadiolysisScreen;
import com.hbm.ntm.client.screen.RefineryScreen;
import com.hbm.ntm.client.screen.RemoteFluidMachineScreen;
import com.hbm.ntm.client.screen.RotaryFurnaceScreen;
import com.hbm.ntm.client.screen.RtgScreen;
import com.hbm.ntm.client.screen.RtgFurnaceScreen;
import com.hbm.ntm.client.screen.RustedLaunchPadScreen;
import com.hbm.ntm.client.screen.SatelliteDockScreen;
import com.hbm.ntm.client.screen.SatelliteLinkerScreen;
import com.hbm.ntm.client.screen.SolidifierScreen;
import com.hbm.ntm.client.screen.SilexScreen;
import com.hbm.ntm.client.screen.ShredderScreen;
import com.hbm.ntm.client.screen.SirenScreen;
import com.hbm.ntm.client.screen.SolderingStationScreen;
import com.hbm.ntm.client.screen.SoyuzCapsuleScreen;
import com.hbm.ntm.client.screen.SoyuzLauncherScreen;
import com.hbm.ntm.client.screen.StrandCasterScreen;
import com.hbm.ntm.client.screen.SteamTurbineScreen;
import com.hbm.ntm.client.screen.LegacyLargeTurbineScreen;
import com.hbm.ntm.client.screen.StorageDrumScreen;
import com.hbm.ntm.client.screen.ToolAbilityScreen;
import com.hbm.ntm.client.screen.TurbofanScreen;
import com.hbm.ntm.client.screen.TurbineGasScreen;
import com.hbm.ntm.client.screen.TurretScreen;
import com.hbm.ntm.client.screen.WeaponTableScreen;
import com.hbm.ntm.client.screen.WoodBurnerScreen;
import com.hbm.ntm.client.screen.WasteDrumScreen;
import com.hbm.ntm.client.screen.WatzReactorScreen;
import com.hbm.ntm.client.screen.ZirnoxReactorScreen;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.block.LegacySellafieldSlakedBlock;
import com.hbm.ntm.item.BedrockOreFragmentItem;
import com.hbm.ntm.item.BedrockOreItem;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.ICFPelletItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.MarshmallowItem;
import com.hbm.ntm.item.OreByproductItem;
import com.hbm.ntm.item.RBMKPelletItem;
import com.hbm.ntm.item.SirenCassetteItem;
import com.hbm.ntm.neutron.RBMKItemPlanner;
import com.hbm.ntm.radiation.CraterRadiationData;
import com.hbm.ntm.radiation.CraterBiomeUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.io.IOException;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private static final PerlinSimplexNoise CRATER_PLANT_NOISE =
            new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(2345L)), List.of(0));

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.BASIC_MACHINE.get(), BasicMachineScreen::new);
            MenuScreens.register(ModMenuTypes.ANVIL.get(), AnvilScreen::new);
            MenuScreens.register(ModMenuTypes.ELECTRIC_PRESS.get(), ElectricPressScreen::new);
            MenuScreens.register(ModMenuTypes.AMMO_BAG.get(), AmmoBagScreen::new);
            MenuScreens.register(ModMenuTypes.CASING_BAG.get(), CasingBagScreen::new);
            MenuScreens.register(ModMenuTypes.ARMOR_TABLE.get(), ArmorTableScreen::new);
            MenuScreens.register(ModMenuTypes.WEAPON_TABLE.get(), WeaponTableScreen::new);
            MenuScreens.register(ModMenuTypes.SIREN.get(), SirenScreen::new);
            MenuScreens.register(ModMenuTypes.ASSEMBLY_MACHINE.get(), AssemblyMachineScreen::new);
            MenuScreens.register(ModMenuTypes.CHEMICAL_PLANT.get(), ChemicalPlantScreen::new);
            MenuScreens.register(ModMenuTypes.ASSEMBLY_FACTORY.get(), AssemblyFactoryScreen::new);
            MenuScreens.register(ModMenuTypes.CHEMICAL_FACTORY.get(), ChemicalFactoryScreen::new);
            MenuScreens.register(ModMenuTypes.COMPRESSOR.get(), CompressorScreen::new);
            MenuScreens.register(ModMenuTypes.COMBUSTION_ENGINE.get(), CombustionEngineScreen::new);
            MenuScreens.register(ModMenuTypes.DIESEL_GENERATOR.get(), DieselGeneratorScreen::new);
            MenuScreens.register(ModMenuTypes.ARC_WELDER.get(), ArcWelderScreen::new);
            MenuScreens.register(ModMenuTypes.ARC_FURNACE.get(), ArcFurnaceScreen::new);
            MenuScreens.register(ModMenuTypes.MINING_LASER.get(), MiningLaserScreen::new);
            MenuScreens.register(ModMenuTypes.PROCESSING_MACHINE.get(), ProcessingMachineScreen::new);
            MenuScreens.register(ModMenuTypes.SHREDDER.get(), ShredderScreen::new);
            MenuScreens.register(ModMenuTypes.AUTOCRAFTER.get(), AutocrafterScreen::new);
            MenuScreens.register(ModMenuTypes.MICROWAVE.get(), MicrowaveScreen::new);
            MenuScreens.register(ModMenuTypes.FUNNEL.get(), FunnelScreen::new);
            MenuScreens.register(ModMenuTypes.KEY_FORGE.get(), KeyForgeScreen::new);
            MenuScreens.register(ModMenuTypes.ELECTRIC_FURNACE.get(), ElectricFurnaceScreen::new);
            MenuScreens.register(ModMenuTypes.DIFURNACE.get(), DiFurnaceScreen::new);
            MenuScreens.register(ModMenuTypes.BRICK_FURNACE.get(), BrickFurnaceScreen::new);
            MenuScreens.register(ModMenuTypes.RTG_FURNACE.get(), RtgFurnaceScreen::new);
            MenuScreens.register(ModMenuTypes.DIFURNACE_RTG.get(), DiFurnaceRtgScreen::new);
            MenuScreens.register(ModMenuTypes.LEGACY_FURNACE.get(), LegacyFurnaceScreen::new);
            MenuScreens.register(ModMenuTypes.AMMO_PRESS.get(), AmmoPressScreen::new);
            MenuScreens.register(ModMenuTypes.RADGEN.get(), RadGenScreen::new);
            MenuScreens.register(ModMenuTypes.RESEARCH_REACTOR.get(), ResearchReactorScreen::new);
            MenuScreens.register(ModMenuTypes.REACTOR_CONTROL.get(), ReactorControlScreen::new);
            MenuScreens.register(ModMenuTypes.BREEDING_REACTOR.get(), BreedingReactorScreen::new);
            MenuScreens.register(ModMenuTypes.ZIRNOX_REACTOR.get(), ZirnoxReactorScreen::new);
            MenuScreens.register(ModMenuTypes.WATZ_REACTOR.get(), WatzReactorScreen::new);
            MenuScreens.register(ModMenuTypes.ICF_REACTOR.get(), ICFReactorScreen::new);
            MenuScreens.register(ModMenuTypes.FUSION_TORUS.get(), FusionTorusScreen::new);
            MenuScreens.register(ModMenuTypes.FUSION_KLYSTRON.get(), FusionKlystronScreen::new);
            MenuScreens.register(ModMenuTypes.FUSION_BREEDER.get(), FusionBreederScreen::new);
            MenuScreens.register(ModMenuTypes.FUSION_PLASMA_FORGE.get(), FusionPlasmaForgeScreen::new);
            MenuScreens.register(ModMenuTypes.ICF_PRESS.get(), ICFPressScreen::new);
            MenuScreens.register(ModMenuTypes.PWR.get(), PWRScreen::new);
            MenuScreens.register(ModMenuTypes.WASTE_DRUM.get(), WasteDrumScreen::new);
            MenuScreens.register(ModMenuTypes.STORAGE_DRUM.get(), StorageDrumScreen::new);
            MenuScreens.register(ModMenuTypes.WOOD_BURNER.get(), WoodBurnerScreen::new);
            MenuScreens.register(ModMenuTypes.TURBOFAN.get(), TurbofanScreen::new);
            MenuScreens.register(ModMenuTypes.TURBINE_GAS.get(), TurbineGasScreen::new);
            MenuScreens.register(ModMenuTypes.STEAM_TURBINE.get(), SteamTurbineScreen::new);
            MenuScreens.register(ModMenuTypes.LEGACY_LARGE_TURBINE.get(), LegacyLargeTurbineScreen::new);
            MenuScreens.register(ModMenuTypes.COMBINATION_OVEN.get(), CombinationOvenScreen::new);
            MenuScreens.register(ModMenuTypes.BLAST_FURNACE.get(), BlastFurnaceScreen::new);
            MenuScreens.register(ModMenuTypes.RADIOLYSIS.get(), RadiolysisScreen::new);
            MenuScreens.register(ModMenuTypes.RTG.get(), RtgScreen::new);
            MenuScreens.register(ModMenuTypes.ROTARY_FURNACE.get(), RotaryFurnaceScreen::new);
            MenuScreens.register(ModMenuTypes.STRAND_CASTER.get(), StrandCasterScreen::new);
            MenuScreens.register(ModMenuTypes.CRUCIBLE.get(), CrucibleScreen::new);
            MenuScreens.register(ModMenuTypes.EXPOSURE_CHAMBER.get(), ExposureChamberScreen::new);
            MenuScreens.register(ModMenuTypes.SOLDERING_STATION.get(), SolderingStationScreen::new);
            MenuScreens.register(ModMenuTypes.GAS_CENT.get(), GasCentScreen::new);
            MenuScreens.register(ModMenuTypes.ORE_SLOPPER.get(), OreSlopperScreen::new);
            MenuScreens.register(ModMenuTypes.SILEX.get(), SilexScreen::new);
            MenuScreens.register(ModMenuTypes.CYCLOTRON.get(), CyclotronScreen::new);
            MenuScreens.register(ModMenuTypes.FEL.get(), FelScreen::new);
            MenuScreens.register(ModMenuTypes.FORCE_FIELD.get(), ForceFieldScreen::new);
            MenuScreens.register(ModMenuTypes.BATTERY_REDD.get(), BatteryReddScreen::new);
            MenuScreens.register(ModMenuTypes.ANNIHILATOR.get(), AnnihilatorScreen::new);
            MenuScreens.register(ModMenuTypes.MIXER.get(), MixerScreen::new);
            MenuScreens.register(ModMenuTypes.ELECTROLYSER.get(), ElectrolyserScreen::new);
            MenuScreens.register(ModMenuTypes.PRECASS.get(), PrecassScreen::new);
            MenuScreens.register(ModMenuTypes.PUREX.get(), PurexScreen::new);
            MenuScreens.register(ModMenuTypes.LIQUEFACTOR.get(), LiquefactorScreen::new);
            MenuScreens.register(ModMenuTypes.REFINERY.get(), RefineryScreen::new);
            MenuScreens.register(ModMenuTypes.SOLIDIFIER.get(), SolidifierScreen::new);
            MenuScreens.register(ModMenuTypes.OIL_DRILL.get(), OilDrillScreen::new);
            MenuScreens.register(ModMenuTypes.GAS_FLARE.get(), GasFlareScreen::new);
            MenuScreens.register(ModMenuTypes.PYRO_OVEN.get(), PyroOvenScreen::new);
            MenuScreens.register(ModMenuTypes.ASHPIT.get(), AshpitScreen::new);
            MenuScreens.register(ModMenuTypes.FIREBOX_HEATER.get(), FireboxHeaterScreen::new);
            MenuScreens.register(ModMenuTypes.OILBURNER.get(), OilburnerScreen::new);
            MenuScreens.register(ModMenuTypes.HEATER_HEATEX.get(), HeaterHeatexScreen::new);
            MenuScreens.register(ModMenuTypes.FLUID_TANK.get(), FluidTankScreen::new);
            MenuScreens.register(ModMenuTypes.FLUID_PUMP.get(), FluidPumpScreen::new);
            MenuScreens.register(ModMenuTypes.REMOTE_FLUID_MACHINE.get(), RemoteFluidMachineScreen::new);
            MenuScreens.register(ModMenuTypes.MACHINE_BATTERY.get(), MachineBatteryScreen::new);
            MenuScreens.register(ModMenuTypes.MACHINE_BATTERY_SOCKET.get(), MachineBatterySocketScreen::new);
            MenuScreens.register(ModMenuTypes.STORAGE_CRATE.get(), CrateScreen::new);
            MenuScreens.register(ModMenuTypes.FILE_CABINET.get(), FileCabinetScreen::new);
            MenuScreens.register(ModMenuTypes.MASS_STORAGE.get(), MassStorageScreen::new);
            MenuScreens.register(ModMenuTypes.CRANE_LOGISTICS.get(), CraneLogisticsScreen::new);
            MenuScreens.register(ModMenuTypes.TURRET.get(), TurretScreen::new);
            MenuScreens.register(ModMenuTypes.NUCLEAR_DEVICE.get(), NuclearDeviceScreen::new);
            MenuScreens.register(ModMenuTypes.CUSTOM_NUKE.get(), CustomNukeScreen::new);
            MenuScreens.register(ModMenuTypes.BOMB_MULTI.get(), BombMultiScreen::new);
            MenuScreens.register(ModMenuTypes.BALEFIRE_BOMB.get(), BalefireBombScreen::new);
            MenuScreens.register(ModMenuTypes.RADAR.get(), RadarScreen::new);
            MenuScreens.register(ModMenuTypes.SATELLITE_LINKER.get(), SatelliteLinkerScreen::new);
            MenuScreens.register(ModMenuTypes.SATELLITE_DOCK.get(), SatelliteDockScreen::new);
            MenuScreens.register(ModMenuTypes.SOYUZ_CAPSULE.get(), SoyuzCapsuleScreen::new);
            MenuScreens.register(ModMenuTypes.SOYUZ_LAUNCHER.get(), SoyuzLauncherScreen::new);
            MenuScreens.register(ModMenuTypes.LAUNCH_PAD.get(), LaunchPadScreen::new);
            MenuScreens.register(ModMenuTypes.LAUNCH_PAD_RUSTED.get(), RustedLaunchPadScreen::new);
            MenuScreens.register(ModMenuTypes.LAUNCH_TABLE.get(), LaunchTableScreen::new);
            MenuScreens.register(ModMenuTypes.COMPACT_LAUNCHER.get(), CompactLauncherScreen::new);
            MenuScreens.register(ModMenuTypes.MISSILE_ASSEMBLY.get(), MissileAssemblyScreen::new);
            MenuScreens.register(ModMenuTypes.PARTICLE_ACCELERATOR.get(), ParticleAcceleratorScreen::new);
            MenuScreens.register(ModMenuTypes.TOOL_ABILITY.get(), ToolAbilityScreen::new);
            MenuScreens.register(ModMenuTypes.RADIO_TORCH.get(), RadioTorchScreen::new);
            MenuScreens.register(ModMenuTypes.RADIO_AUTOCAL.get(), RadioAutocalScreen::new);
            MenuScreens.register(ModMenuTypes.RADIO_TELEX.get(), RadioTelexScreen::new);
            MenuScreens.register(ModMenuTypes.RADIO_RECEIVER.get(), RadioReceiverScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_PANEL.get(), RBMKPanelScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_CONSOLE.get(), RBMKConsoleScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_ROD.get(), RBMKRodScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_CONTROL.get(), RBMKControlScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_CONTROL_AUTO.get(), RBMKControlAutoScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_HEATER.get(), RBMKHeaterScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_BOILER.get(), RBMKBoilerScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_STORAGE.get(), RBMKStorageScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_OUTGASSER.get(), RBMKOutgasserScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_AUTOLOADER.get(), RBMKAutoloaderScreen::new);
            ItemProperties.register(ModItems.MARSHMALLOW.get(), new ResourceLocation(HbmNtm.MOD_ID, "roasted"),
                    (stack, level, entity, seed) -> MarshmallowItem.isRoasted(stack) ? 1.0F : 0.0F);
            ModItems.RBMK_PELLET_ITEMS.stream()
                    .map(RegistryObject::get)
                    .filter(RBMKPelletItem.class::isInstance)
                    .forEach(item -> ItemProperties.register(item, RBMKPelletItem.META_PROPERTY,
                            (stack, level, entity, seed) -> RBMKItemPlanner.rectifyPelletMeta(stack.getDamageValue())));
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BASIC_MACHINE.get(), BasicMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ELECTRIC_PRESS.get(), ElectricPressRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_CABLE.get(), RedCableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CABLE_DIODE.get(), CableDiodeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_CONNECTOR.get(), LegacyPylonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_PYLON.get(), LegacyPylonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_PYLON_MEDIUM.get(), LegacyPylonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_PYLON_LARGE.get(), LegacyPylonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SUBSTATION.get(), LegacyPylonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_SENDER.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_RECEIVER.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_LOGIC.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_READER.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_CONTROLLER.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_COUNTER.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MULTIBLOCK_DUMMY.get(), MultiblockDummyRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_AUTOCAL.get(), RadioAutocalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TELEX.get(), RadioTelexRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RBMK_PANEL.get(), RBMKPanelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RBMK_COLUMN.get(), RBMKColumnRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RBMK_AUTOLOADER.get(), RBMKAutoloaderRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RBMK_CONSOLE.get(), RBMKConsoleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RBMK_CRANE_CONSOLE.get(), RBMKCraneConsoleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_PIPE.get(), FluidPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_DUCT_BOX.get(), FluidDuctBoxRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_DUCT_EXHAUST.get(), FluidDuctBoxRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_DUCT_PAINTABLE.get(), FluidDuctPaintableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_DUCT_PAINTABLE_EXHAUST.get(),
                FluidDuctPaintableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_PIPE_ANCHOR.get(), FluidPipeAnchorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_FENSU.get(), FensuRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_BATTERY_SOCKET.get(), MachineBatterySocketRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_MACHINE.get(), AssemblyMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHEMICAL_PLANT.get(), ChemicalPlantRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_FACTORY.get(), AssemblyFactoryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHEMICAL_FACTORY.get(), ChemicalFactoryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRANE_SPLITTER.get(), CraneSplitterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LIQUEFACTOR.get(), LiquefactorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REFINERY.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_TANK.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEXAFLUORIDE_TANK.get(), HexafluorideTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BAT9000.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BIG_ASS_TANK.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ORBUS.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_BARREL.get(), FluidBarrelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.OIL_DRILL.get(), OilDrillRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BOILER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PROCESSING_MACHINE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHARGER.get(), ChargerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REFUELER.get(), RefuelerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIOBOX.get(), RadioDecoRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIOREC.get(), RadioDecoRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TESLA.get(), TeslaRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MICROWAVE.get(), MicrowaveRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AUTOSAW.get(), AutosawRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.THRESHER.get(), ThresherRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_FURNACE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AMMO_PRESS.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADGEN.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WOOD_BURNER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COMBINATION_OVEN.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BLAST_FURNACE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIOLYSIS.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RTG.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ROTARY_FURNACE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.EXPOSURE_CHAMBER.get(), ExposureChamberRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLDERING_STATION.get(), SolderingStationRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ANNIHILATOR.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DEUTERIUM_TOWER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GAS_CENT.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ORE_SLOPPER.get(), OreSlopperRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SILEX.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CYCLOTRON.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FEL.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FORCE_FIELD.get(), ForceFieldRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BATTERY_REDD.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MIXER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ELECTROLYSER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SAWMILL.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHUNGUS.get(), ChungusRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURBOFAN.get(), TurbofanRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURBINE_GAS.get(), TurbineGasRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARC_FURNACE.get(), ArcFurnaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MINING_LASER.get(), MiningLaserRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GEIGER.get(), GeigerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_GENERIC_SELECTOR_MACHINE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEPHAESTUS.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FRACTION_SPACER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STRAND_CASTER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GAS_FLARE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHIMNEY.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASHPIT.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CATALYTIC_CRACKER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CATALYTIC_REFORMER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VACUUM_DISTILL.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FRACTION_TOWER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HYDROTREATER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COKER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLIDIFIER.get(), SolidifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PYRO_OVEN.get(), PyroOvenRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_RADAR.get(), RadarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_RADAR_LARGE.get(), RadarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_RADAR_SCREEN.get(), RadarScreenRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VENDING_MACHINE.get(), VendingMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SAT_DOCK.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOYUZ_CAPSULE.get(), SoyuzCapsuleBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOYUZ_LAUNCHER.get(), SoyuzLauncherRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOYUZ_STRUCT.get(), SoyuzMultiblockGhostRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAUNCH_PAD.get(), LaunchPadRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAUNCH_PAD_LARGE.get(), LargeLaunchPadRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAUNCH_PAD_RUSTED.get(), RustedLaunchPadRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAUNCH_TABLE.get(), CustomMissileLauncherRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COMPACT_LAUNCHER.get(), CustomMissileLauncherRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MISSILE_ASSEMBLY.get(), MissileAssemblyRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PA_SOURCE.get(), ParticleAcceleratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PA_BEAMLINE.get(), ParticleAcceleratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PA_RFC.get(), ParticleAcceleratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PA_QUADRUPOLE.get(), ParticleAcceleratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PA_DIPOLE.get(), ParticleAcceleratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PA_DETECTOR.get(), ParticleAcceleratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COMPRESSOR.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COMBUSTION_ENGINE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DIESEL_GENERATOR.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ELECTRIC_HEATER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FIREBOX_HEATER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.OILBURNER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WATER_PUMP.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.POWERED_CONDENSER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARC_WELDER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_LPW2.get(), MachineLpw2Renderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RESEARCH_REACTOR.get(), ResearchReactorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BREEDING_REACTOR.get(), BreedingReactorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ZIRNOX_REACTOR.get(), ZirnoxReactorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ZIRNOX_DESTROYED.get(), ZirnoxDestroyedRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WATZ_REACTOR.get(), WatzReactorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WATZ_STRUCT_CORE.get(), WatzStructCoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STORAGE_DRUM.get(), StorageDrumRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WATZ_PUMP.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ICF_REACTOR.get(), ICFReactorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ICF_CONTROLLER.get(), ICFControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ICF_STRUCT_CORE.get(), ICFStructCoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_TORUS.get(), FusionTorusRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_KLYSTRON.get(), FusionKlystronRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_KLYSTRON_CREATIVE.get(), FusionKlystronCreativeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_BREEDER.get(), FusionBreederRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_COLLECTOR.get(), FusionCollectorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_BOILER.get(), FusionBoilerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_COUPLER.get(), FusionCouplerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_MHDT.get(), FusionMHDTRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_PLASMA_FORGE.get(), FusionPlasmaForgeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_TORUS_STRUCT_CORE.get(), FusionTorusStructCoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CARGO_ELEVATOR.get(), CargoElevatorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STEAM_ENGINE.get(), SteamEngineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INDUSTRIAL_STEAM_TURBINE.get(), IndustrialSteamTurbineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LARGE_TURBINE.get(), LegacyLargeTurbineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLAR_BOILER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLAR_MIRROR.get(), SolarMirrorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SMALL_COOLING_TOWER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LARGE_COOLING_TOWER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INTAKE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DRAIN.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STIRLING.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TRINKET.get(), TrinketBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LIGHT.get(), LegacyLightBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_DEMON_LAMP.get(), LegacyDemonLampBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_MOLD.get(), FoundryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_BASIN.get(), FoundryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_CHANNEL.get(), FoundryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_TANK.get(), FoundryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_OUTLET.get(), FoundryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_SLAGTAP.get(), FoundryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_SLAG.get(), FoundryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_EMITTER.get(), LegacyEmitterBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_FAN.get(), LegacyFanRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_FILE_CABINET.get(), LegacyFileCabinetRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LANTERN.get(), LegacyLanternBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUCLEAR_DEVICE.get(), NuclearDeviceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CUSTOM_NUKE.get(), CustomNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BOMB_MULTI.get(), BombMultiRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BALEFIRE_BOMB.get(), BalefireBombRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_CHARGE.get(), LegacyChargeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_CHEKHOV.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_FRIENDLY.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_JEREMY.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_RICHARD.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_TAUON.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_HOWARD.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_SENTRY.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_HOWARD_DAMAGED.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_SENTRY_DAMAGED.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_MAXWELL.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_ARTY.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_HIMARS.get(), TurretBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TURRET_FRITZ.get(), TurretBlockEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MOVING_ITEM.get(), MovingItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MOVING_PACKAGE.get(), MovingPackageRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NUKE_EXPLOSION_MK5.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NUKE_EXPLOSION_MK3.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.TOM_BLAST.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NUKE_TOREX.get(), NukeTorexRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BLACK_HOLE.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VORTEX.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RAGING_VORTEX.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.QUASAR.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.DIGAMMA_SPEAR.get(), DigammaSpearRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FALLOUT_RAIN.get(), FalloutRainRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLOUD_FLEIJA.get(), CloudFleijaRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLOUD_SOLINIUM.get(), CloudSoliniumRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLOUD_FLEIJA_RAINBOW.get(), CloudFleijaRainbowRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.EMP_BLAST.get(), EmpBlastRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FIRE_LINGERING.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MIST.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.EMP_LOGIC.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BALEFIRE_EXPLOSION.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.DEATH_BLAST.get(), DeathBlastRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FALLING_NUKE.get(), FallingNukeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LEGACY_PRIMED_EXPLOSIVE.get(), LegacyPrimedExplosiveRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_GENERIC.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_DECOY.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_INCENDIARY.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_CLUSTER.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_BUSTER.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_STRONG.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_INCENDIARY_STRONG.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_CLUSTER_STRONG.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_BUSTER_STRONG.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_EMP_STRONG.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_BURST.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_INFERNO.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_RAIN.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_DRILL.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_STEALTH.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_EMP.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_MICRO.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_SCHRABIDIUM.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_BHOLE.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_TAINT.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_NUCLEAR.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_NUCLEAR_CLUSTER.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_VOLCANO.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_DOOMSDAY.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_DOOMSDAY_RUSTED.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_ANTI_BALLISTIC.get(), AntiBallisticMissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_CUSTOM.get(), CustomMissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MINER_ROCKET.get(), MinerRocketRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SOYUZ.get(), SoyuzRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SOYUZ_CAPSULE.get(), SoyuzCapsuleRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SHRAPNEL.get(), ShrapnelRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RUBBLE.get(), RubbleRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RBMK_DEBRIS.get(), RBMKDebrisRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ZIRNOX_DEBRIS.get(), ZirnoxDebrisRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BULLET_PROJECTILE.get(), BulletProjectileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ARTILLERY_SHELL.get(), ArtilleryShellRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ARTILLERY_ROCKET.get(), ArtilleryRocketRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CHEMICAL_PROJECTILE.get(), ChemicalProjectileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.DYNAMITE_STICK.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.COIN.get(), CoinRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SAWBLADE.get(), SawbladeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.COG.get(), CogRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        TrinketBlockEntityRenderer.registerAdditionalModels();
        ObjModelLibrary.registerAdditionalModels(event);
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new LegacyModelReloadListener());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ModItems.PARTS_TAB_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(DepletedFuelItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) ->
                        DepletedFuelItem.isHot(stack) ? DepletedFuelItem.HOT_TINT : 0xFFFFFF, item));
        ModItems.CONSUMABLE_TAB_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(HbmFluidContainerItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) -> ((HbmFluidContainerItem) item).getTintColor(stack, tintIndex), item));
        ModItems.CONTROL_FLUID_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(HbmFluidContainerItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) -> ((HbmFluidContainerItem) item).getTintColor(stack, tintIndex), item));
        event.register(FluidIconItem::getTintColor, ModItems.FLUID_ICON.get());
        ModItems.CONTROL_FLUID_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(FluidIdentifierItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) -> ((FluidIdentifierItem) item).getTintColor(stack, tintIndex), item));
        ModItems.ORE_BYPRODUCT_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(OreByproductItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) -> ((OreByproductItem) item).getTintColor(tintIndex), item));
        event.register(BedrockOreItem::tint, ModItems.BEDROCK_ORE.get());
        event.register(BedrockOreFragmentItem::tint, ModItems.BEDROCK_ORE_FRAGMENT.get());
        event.register(ICFPelletItem::tint, ModItems.ICF_PELLET.get());
        event.register((stack, tintIndex) -> stack.getItem() instanceof SirenCassetteItem cassette
                ? cassette.tint(stack, tintIndex)
                : 0xFFFFFF, ModItems.SIREN_TRACK.get());
        event.register((stack, tintIndex) -> {
            if (stack.getItem() instanceof FluidPipeBlockItem pipe) {
                return pipe.getTintColor(stack, tintIndex);
            }
            return 0xFFFFFF;
        }, ModItems.FLUID_DUCT.get());
        event.register((stack, tintIndex) -> {
            if (tintIndex == 0 && stack.getItem() instanceof LegacyStateBlockItem item) {
                return sellafieldLevelTint(item.getVariant(stack));
            }
            return 0xFFFFFF;
        }, ModBlocks.SELLAFIELD_SLAKED.get().asItem(), ModBlocks.SELLAFIELD_BEDROCK.get().asItem(),
                ModBlocks.ORE_SELLAFIELD_DIAMOND.get().asItem(), ModBlocks.ORE_SELLAFIELD_EMERALD.get().asItem(),
                ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED.get().asItem(), ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM.get().asItem(),
                ModBlocks.ORE_SELLAFIELD_RADGEM.get().asItem());
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex == 1 && level != null && pos != null
                    && level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe) {
                return pipe.getFluidType().getColor();
            }
            return 0xFFFFFF;
        }, ModBlocks.FLUID_DUCT_NEO.get(),
                ModBlocks.FLUID_DUCT_BOX.get(),
                ModBlocks.FLUID_DUCT_GAUGE.get(),
                ModBlocks.FLUID_DUCT_PAINTABLE.get(),
                ModBlocks.PIPE_ANCHOR.get(),
                ModBlocks.FLUID_VALVE.get(),
                ModBlocks.FLUID_SWITCH.get(),
                ModBlocks.FLUID_COUNTER_VALVE.get());
        event.register((state, level, pos, tintIndex) -> {
            int crater = craterGrassColor(level, pos);
            return crater >= 0 ? crater : fallbackGrassColor(level, pos);
        }, Blocks.GRASS_BLOCK, Blocks.GRASS, Blocks.TALL_GRASS, Blocks.FERN, Blocks.LARGE_FERN, Blocks.VINE,
                Blocks.SUGAR_CANE);
        event.register((state, level, pos, tintIndex) -> {
            int crater = craterFoliageColor(level, pos);
            return crater >= 0 ? crater : fallbackFoliageColor(level, pos);
        }, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES,
                Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES);
        event.register((state, level, pos, tintIndex) ->
                tintIndex == 0 && state.hasProperty(LegacySellafieldSlakedBlock.LEVEL)
                        ? sellafieldLevelTint(state.getValue(LegacySellafieldSlakedBlock.LEVEL))
                        : 0xFFFFFF,
                ModBlocks.SELLAFIELD_SLAKED.get(), ModBlocks.SELLAFIELD_BEDROCK.get(),
                ModBlocks.ORE_SELLAFIELD_DIAMOND.get(), ModBlocks.ORE_SELLAFIELD_EMERALD.get(),
                ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED.get(), ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM.get(),
                ModBlocks.ORE_SELLAFIELD_RADGEM.get());
    }

    private static int sellafieldLevelTint(int level) {
        int clamped = Math.max(0, Math.min(15, level));
        int shade = Math.max(0, Math.min(255, Math.round((1.0F - clamped / 15.0F) * 255.0F)));
        return shade << 16 | shade << 8 | shade;
    }

    private static int craterGrassColor(BlockAndTintGetter level, BlockPos pos) {
        CraterRadiationData.CraterZone zone = craterZone(level, pos);
        double noise = legacyPlantNoise(pos);
        return switch (zone) {
            case OUTER -> noise < -0.1D ? 0x776F59 : 0x6F6752;
            case CRATER -> noise < -0.1D ? 0x606060 : 0x505050;
            case INNER -> noise < -0.1D ? 0x404040 : 0x303030;
            case NONE -> -1;
        };
    }

    private static int craterFoliageColor(BlockAndTintGetter level, BlockPos pos) {
        return craterZone(level, pos) != CraterRadiationData.CraterZone.NONE ? 0x6A7039 : -1;
    }

    private static int fallbackGrassColor(BlockAndTintGetter level, BlockPos pos) {
        return level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.getDefaultColor();
    }

    private static int fallbackFoliageColor(BlockAndTintGetter level, BlockPos pos) {
        return level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : FoliageColor.getDefaultColor();
    }

    private static CraterRadiationData.CraterZone craterZone(BlockAndTintGetter level, BlockPos pos) {
        ResourceKey<Biome> key = biomeKey(level, pos);
        if (CraterBiomeUtil.CRATER_OUTER.equals(key)) {
            return CraterRadiationData.CraterZone.OUTER;
        }
        if (CraterBiomeUtil.CRATER.equals(key)) {
            return CraterRadiationData.CraterZone.CRATER;
        }
        if (CraterBiomeUtil.CRATER_INNER.equals(key)) {
            return CraterRadiationData.CraterZone.INNER;
        }
        return CraterRadiationData.CraterZone.NONE;
    }

    private static ResourceKey<Biome> biomeKey(BlockAndTintGetter level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        if (level instanceof LevelReader levelReader) {
            return levelReader.getBiome(pos).unwrapKey().orElse(null);
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.getBiome(pos).unwrapKey().orElse(null);
        }
        return null;
    }

    private static double legacyPlantNoise(BlockPos pos) {
        if (pos == null) {
            return 0.0D;
        }
        return CRATER_PLANT_NOISE.getValue(pos.getX() * 0.225D, pos.getZ() * 0.225D, false);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        HbmClientKeybinds.register(event);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        HbmRenderEffects.registerShaders(event);
        HbmBlackHoleEffects.registerShaders(event);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.RADIATION_FOG.get(), RadiationFogParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ROCKET_FLAME.get(), RocketFlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GAS_FLAME.get(), GasFlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.CONTRAIL.get(), LegacyContrailParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SMOKE_PLUME.get(), SmokePlumeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.LAUNCH_SMOKE.get(), HbmSmokeParticle.LaunchSmokeProvider::new);
        event.registerSpriteSet(ModParticleTypes.EX_SMOKE.get(), HbmSmokeParticle.ExSmokeProvider::new);
        event.registerSpriteSet(ModParticleTypes.DIGAMMA_SMOKE.get(), DigammaSmokeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FOAM.get(), FoamParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER.get(), FlamethrowerParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER_BALEFIRE.get(),
                sprites -> new FlamethrowerParticle.Provider(sprites, FlamethrowerParticle.META_BALEFIRE));
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER_DIGAMMA.get(),
                sprites -> new FlamethrowerParticle.Provider(sprites, FlamethrowerParticle.META_DIGAMMA));
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER_OXY.get(),
                sprites -> new FlamethrowerParticle.Provider(sprites, FlamethrowerParticle.META_OXY));
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER_BLACK.get(),
                sprites -> new FlamethrowerParticle.Provider(sprites, FlamethrowerParticle.META_BLACK));
        event.registerSpriteSet(ModParticleTypes.BLACK_POWDER_SPARK.get(), BlackPowderSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.LARGE_EXPLODE.get(), LargeExplodeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.EXPLOSION_SMALL.get(), ExplosionSmallParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.BLACK_POWDER_SMOKE.get(), BlackPowderSmokeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ASHES.get(), AshesParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.MUKE_WAVE.get(), MukeWaveParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.TOWN_AURA.get(), TownAuraParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SCHRAB_FOG.get(), SchrabFogParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.DEAD_LEAF.get(), DeadLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GIBLET_MEAT.get(), sprites -> new GibletParticle.Provider(sprites, GibletParticle.TYPE_MEAT));
        event.registerSpriteSet(ModParticleTypes.GIBLET_SLIME.get(), sprites -> new GibletParticle.Provider(sprites, GibletParticle.TYPE_SLIME));
        event.registerSpriteSet(ModParticleTypes.GIBLET_METAL.get(), sprites -> new GibletParticle.Provider(sprites, GibletParticle.TYPE_METAL));
        event.registerSpriteSet(ModParticleTypes.HADRON.get(), HadronParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.RBMK_FLAME.get(), RbmkAnimatedParticle.FlameProvider::new);
        event.registerSpriteSet(ModParticleTypes.RBMK_STEAM.get(), RbmkAnimatedParticle.SteamProvider::new);
        event.registerSpriteSet(ModParticleTypes.RBMK_MUSH.get(), RbmkAnimatedParticle.MushProvider::new);
        event.registerSpriteSet(ModParticleTypes.COOLING_TOWER.get(), CoolingTowerParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SPLASH.get(), LegacySplashParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FLUID_FILL.get(), FluidFillParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.NETWORK_POWER.get(), NetworkDebugParticle.PowerProvider::new);
        event.registerSpriteSet(ModParticleTypes.NETWORK_FLUID.get(), NetworkDebugParticle.FluidProvider::new);
        event.registerSpriteSet(ModParticleTypes.PLASMA_BLAST.get(), PlasmaBlastParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.HAZE.get(), HazeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.CHAOS_CLOUD_ORANGE.get(),
                sprites -> new ChaosCloudParticle.Provider(sprites, ChaosCloudParticle.Mode.ORANGE));
        event.registerSpriteSet(ModParticleTypes.CHAOS_CLOUD_GREEN.get(),
                sprites -> new ChaosCloudParticle.Provider(sprites, ChaosCloudParticle.Mode.GREEN));
        event.registerSpriteSet(ModParticleTypes.CHAOS_CLOUD_PINK.get(),
                sprites -> new ChaosCloudParticle.Provider(sprites, ChaosCloudParticle.Mode.PINK));
    }

    private ClientModEvents() {
    }
}
