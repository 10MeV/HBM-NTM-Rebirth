package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.AnnihilatorBlock;
import com.hbm.ntm.block.AutocrafterBlock;
import com.hbm.ntm.block.ArmorTableBlock;
import com.hbm.ntm.block.ArcFurnaceBlock;
import com.hbm.ntm.block.ArcWelderBlock;
import com.hbm.ntm.block.AshpitBlock;
import com.hbm.ntm.block.AutosawBlock;
import com.hbm.ntm.block.AssemblyFactoryBlock;
import com.hbm.ntm.block.AssemblyMachineBlock;
import com.hbm.ntm.block.AmmoPressBlock;
import com.hbm.ntm.block.BalefireBombBlock;
import com.hbm.ntm.block.BalefireBlock;
import com.hbm.ntm.block.Bat9000Block;
import com.hbm.ntm.block.BatteryReddBlock;
import com.hbm.ntm.block.BigAssTankBlock;
import com.hbm.ntm.block.BlastFurnaceBlock;
import com.hbm.ntm.block.BedrockOreDepositBlock;
import com.hbm.ntm.block.BreedingReactorBlock;
import com.hbm.ntm.block.BrickFurnaceBlock;
import com.hbm.ntm.block.BombMultiBlock;
import com.hbm.ntm.block.BoilerBlock;
import com.hbm.ntm.block.CapacitorBusBlock;
import com.hbm.ntm.block.CableDiodeBlock;
import com.hbm.ntm.block.CargoElevatorBlock;
import com.hbm.ntm.block.ChemicalFactoryBlock;
import com.hbm.ntm.block.ChemicalPlantBlock;
import com.hbm.ntm.block.ChimneyBlock;
import com.hbm.ntm.block.ChargerBlock;
import com.hbm.ntm.block.ChungusBlock;
import com.hbm.ntm.block.CompressorBlock;
import com.hbm.ntm.block.CombustionEngineBlock;
import com.hbm.ntm.block.CombinationOvenBlock;
import com.hbm.ntm.block.CondenserBlock;
import com.hbm.ntm.block.CoolingTowerBlock;
import com.hbm.ntm.block.CraneLogisticsBlock;
import com.hbm.ntm.block.CraneSplitterBlock;
import com.hbm.ntm.block.CrateBlock;
import com.hbm.ntm.block.CrucibleBlock;
import com.hbm.ntm.block.CustomMissileLauncherBlock;
import com.hbm.ntm.block.CustomNukeBlock;
import com.hbm.ntm.block.CyclotronBlock;
import com.hbm.ntm.block.DeconBlock;
import com.hbm.ntm.block.DeuteriumExtractorBlock;
import com.hbm.ntm.block.DeuteriumTowerBlock;
import com.hbm.ntm.block.DfcMachineBlock;
import com.hbm.ntm.block.DiFurnaceBlock;
import com.hbm.ntm.block.DiFurnaceExtensionBlock;
import com.hbm.ntm.block.DiFurnaceRtgBlock;
import com.hbm.ntm.block.DieselGeneratorBlock;
import com.hbm.ntm.block.DrainBlock;
import com.hbm.ntm.block.ElectricHeaterBlock;
import com.hbm.ntm.block.ElectricFurnaceBlock;
import com.hbm.ntm.block.ElectricPressBlock;
import com.hbm.ntm.block.ElectrolyserBlock;
import com.hbm.ntm.block.ExposureChamberBlock;
import com.hbm.ntm.block.FieldDisturberBlock;
import com.hbm.ntm.block.FelBlock;
import com.hbm.ntm.block.FloodlightBeamBlock;
import com.hbm.ntm.block.ForceFieldBlock;
import com.hbm.ntm.block.FractionSpacerBlock;
import com.hbm.ntm.block.FusionMachineBlock;
import com.hbm.ntm.block.FusionStructureComponentBlock;
import com.hbm.ntm.block.FusionTorusStructCoreBlock;
import com.hbm.ntm.block.GlowingMushBlock;
import com.hbm.ntm.block.LegacyChargeBlock;
import com.hbm.ntm.block.LegacyDetCordBlock;
import com.hbm.ntm.block.LegacyExplosiveChargeBlock;
import com.hbm.ntm.block.LegacyFurnaceBlock;
import com.hbm.ntm.block.DigammaFlameBlock;
import com.hbm.ntm.block.FalloutLayerBlock;
import com.hbm.ntm.block.FensuBlock;
import com.hbm.ntm.block.FireboxHeaterBlock;
import com.hbm.ntm.block.FluidDuctBoxBlock;
import com.hbm.ntm.block.FluidDuctExhaustBlock;
import com.hbm.ntm.block.FluidDuctGaugeBlock;
import com.hbm.ntm.block.FluidDuctPaintableBlock;
import com.hbm.ntm.block.FluidDuctPaintableExhaustBlock;
import com.hbm.ntm.block.FluidBarrelBlock;
import com.hbm.ntm.block.FluidPipeBlock;
import com.hbm.ntm.block.FluidPipeAnchorBlock;
import com.hbm.ntm.block.FluidPumpBlock;
import com.hbm.ntm.block.FluidTankBlock;
import com.hbm.ntm.block.FluidValveBlock;
import com.hbm.ntm.block.FoundryCastingBlock;
import com.hbm.ntm.block.FoundryChannelBlock;
import com.hbm.ntm.block.FoundryOutletBlock;
import com.hbm.ntm.block.FoundrySlagBlock;
import com.hbm.ntm.block.FoundryTankBlock;
import com.hbm.ntm.block.FunnelMachineBlock;
import com.hbm.ntm.block.GasCentBlock;
import com.hbm.ntm.block.GasFlareBlock;
import com.hbm.ntm.block.GeigerBlock;
import com.hbm.ntm.block.HeatBoilerBlock;
import com.hbm.ntm.block.HeaterHeatexBlock;
import com.hbm.ntm.block.HevBatteryBlock;
import com.hbm.ntm.block.HephaestusBlock;
import com.hbm.ntm.block.HexafluorideTankBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.ICFControllerBlock;
import com.hbm.ntm.block.ICFAssembledBlock;
import com.hbm.ntm.block.ICFPressBlock;
import com.hbm.ntm.block.ICFReactorBlock;
import com.hbm.ntm.block.ICFStructCoreBlock;
import com.hbm.ntm.block.ICFStructureComponentBlock;
import com.hbm.ntm.block.IntakeBlock;
import com.hbm.ntm.block.KeyForgeBlock;
import com.hbm.ntm.block.LargeLaunchPadBlock;
import com.hbm.ntm.block.LegacyChainBlock;
import com.hbm.ntm.block.LegacyComplexShapeBlock;
import com.hbm.ntm.block.LegacyConnectorBlock;
import com.hbm.ntm.block.LegacyCoriumFiniteBlock;
import com.hbm.ntm.block.LegacyCaveSpikeBlock;
import com.hbm.ntm.block.LegacyCrystalVirusBlock;
import com.hbm.ntm.block.LegacyDemonLampBlock;
import com.hbm.ntm.block.LegacyDepthBlock;
import com.hbm.ntm.block.LegacyEmitterBlock;
import com.hbm.ntm.block.LegacyFanBlock;
import com.hbm.ntm.block.LegacyFileCabinetBlock;
import com.hbm.ntm.block.LegacyExplosiveBarrelBlock;
import com.hbm.ntm.block.LegacyGasMeltdownBlock;
import com.hbm.ntm.block.LegacyGasRadonBlock;
import com.hbm.ntm.block.LegacyGlyphidSpawnerBlock;
import com.hbm.ntm.block.LegacyGrateBlock;
import com.hbm.ntm.block.LegacyHugeMushBlock;
import com.hbm.ntm.block.LegacyBasaltOreBlock;
import com.hbm.ntm.block.LegacyNuclearWasteBlock;
import com.hbm.ntm.block.LegacyLargePylonBlock;
import com.hbm.ntm.block.LegacyMediumPylonBlock;
import com.hbm.ntm.block.LegacyNtmFlowerBlock;
import com.hbm.ntm.block.LegacyNetherCoalOreBlock;
import com.hbm.ntm.block.LegacyOreBlock;
import com.hbm.ntm.block.LegacyOutgasBlock;
import com.hbm.ntm.block.OldBoilerBlock;
import com.hbm.ntm.block.LegacyHotBlock;
import com.hbm.ntm.block.LegacyLeavesLayerBlock;
import com.hbm.ntm.block.LegacyPlasticExplosiveBlock;
import com.hbm.ntm.block.LegacyPoleSatelliteReceiverBlock;
import com.hbm.ntm.block.LegacySmallPylonBlock;
import com.hbm.ntm.block.LegacySpotlightBlock;
import com.hbm.ntm.block.LegacyWasteLeavesBlock;
import com.hbm.ntm.block.LegacyWasteLogBlock;
import com.hbm.ntm.block.LegacySubstationBlock;
import com.hbm.ntm.block.LegacyTapeRecorderBlock;
import com.hbm.ntm.block.LegacyVolcanicLavaBlock;
import com.hbm.ntm.block.MachineBlockEntityBlock;
import com.hbm.ntm.block.MachineLpw2Block;
import com.hbm.ntm.block.MassStorageBlock;
import com.hbm.ntm.block.MiniRtgBlock;
import com.hbm.ntm.block.MicrowaveBlock;
import com.hbm.ntm.block.MiningLaserBlock;
import com.hbm.ntm.block.ExcavatorBlock;
import com.hbm.ntm.block.MissileAssemblyBlock;
import com.hbm.ntm.block.NTMAnvilBlock;
import com.hbm.ntm.block.OreSlopperBlock;
import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.block.LegacyLanternBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachinePartRenderProperties;
import com.hbm.ntm.block.LegacyMachinePartRenderMode;
import com.hbm.ntm.block.LegacyMachineRenderProfile;
import com.hbm.ntm.block.LegacyMudBlock;
import com.hbm.ntm.block.LegacyNtmGlassBlock;
import com.hbm.ntm.block.LegacyNtmGlassPaneBlock;
import com.hbm.ntm.block.LegacyGenericSelectorMachineBlock;
import com.hbm.ntm.block.LegacyLargeTurbineBlock;
import com.hbm.ntm.block.MachineBatteryBlock;
import com.hbm.ntm.block.LegacyHazardSourceBlock;
import com.hbm.ntm.block.LegacyRadAbsorberBlock;
import com.hbm.ntm.block.LegacyRadiationBarrelBlock;
import com.hbm.ntm.block.LegacySellafieldBlock;
import com.hbm.ntm.block.LegacySellafieldOreBlock;
import com.hbm.ntm.block.LegacySellafieldSlakedBlock;
import com.hbm.ntm.block.LegacyFrozenEarthBlock;
import com.hbm.ntm.block.LegacyReactiveLiquidBlock;
import com.hbm.ntm.block.LegacyTaintBlock;
import com.hbm.ntm.block.LegacyTntBaseBlock;
import com.hbm.ntm.block.LegacyToxicGasBlock;
import com.hbm.ntm.block.LegacyTektiteOreBlock;
import com.hbm.ntm.block.LegacyTrinititeOreBlock;
import com.hbm.ntm.block.LegacyVolcanoCoreBlock;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.block.LaunchPadBlock;
import com.hbm.ntm.block.LiquefactorBlock;
import com.hbm.ntm.block.NuclearDeviceBlock;
import com.hbm.ntm.block.OilburnerBlock;
import com.hbm.ntm.block.OilSpillBlock;
import com.hbm.ntm.block.OrbusBlock;
import com.hbm.ntm.block.PileGraphiteBlock;
import com.hbm.ntm.block.PileGraphiteBreedingFuelBlock;
import com.hbm.ntm.block.PileGraphiteBreedingProductBlock;
import com.hbm.ntm.block.PileGraphiteDrilledBlock;
import com.hbm.ntm.block.PileGraphiteFuelBlock;
import com.hbm.ntm.block.PileGraphiteNeutronDetectorBlock;
import com.hbm.ntm.block.PileGraphiteRodBlock;
import com.hbm.ntm.block.PileGraphiteSourceBlock;
import com.hbm.ntm.block.PneumaticTubeBlock;
import com.hbm.ntm.block.PressMachineBlock;
import com.hbm.ntm.block.ProcessingMachineBlock;
import com.hbm.ntm.block.PowerDetectorBlock;
import com.hbm.ntm.block.PoweredRedCableBlock;
import com.hbm.ntm.block.PoweredCondenserBlock;
import com.hbm.ntm.block.PWRAssembledBlock;
import com.hbm.ntm.block.PWRComponentBlock;
import com.hbm.ntm.block.PWRControllerBlock;
import com.hbm.ntm.block.PyroOvenBlock;
import com.hbm.ntm.block.RadiolysisBlock;
import com.hbm.ntm.block.RadiatingFallingBlock;
import com.hbm.ntm.block.RadiatingHazardBlock;
import com.hbm.ntm.block.RadarBlock;
import com.hbm.ntm.block.RadGenBlock;
import com.hbm.ntm.block.RadioboxBlock;
import com.hbm.ntm.block.RadioReceiverBlock;
import com.hbm.ntm.block.RefuelerBlock;
import com.hbm.ntm.block.ResearchReactorBlock;
import com.hbm.ntm.block.RadarLargeBlock;
import com.hbm.ntm.block.RadarScreenBlock;
import com.hbm.ntm.block.RadioAutocalBlock;
import com.hbm.ntm.block.RadioTelexBlock;
import com.hbm.ntm.block.RadioactiveWasteEarthBlock;
import com.hbm.ntm.block.RadioTorchControllerDeviceBlock;
import com.hbm.ntm.block.RadioTorchCounterDeviceBlock;
import com.hbm.ntm.block.RadioTorchLogicBlock;
import com.hbm.ntm.block.RadioTorchReaderDeviceBlock;
import com.hbm.ntm.block.RadioTorchReceiverBlock;
import com.hbm.ntm.block.RadioTorchSenderBlock;
import com.hbm.ntm.block.RustedLaunchPadBlock;
import com.hbm.ntm.block.RtgBlock;
import com.hbm.ntm.block.RtgFurnaceBlock;
import com.hbm.ntm.block.ReactorControlBlock;
import com.hbm.ntm.block.SafeBlock;
import com.hbm.ntm.block.SawmillBlock;
import com.hbm.ntm.block.ShredderBlock;
import com.hbm.ntm.block.SilexBlock;
import com.hbm.ntm.block.SirenBlock;
import com.hbm.ntm.block.StrandCasterBlock;
import com.hbm.ntm.block.RBMKDisplayBlankBlock;
import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.block.RBMKConsoleBlock;
import com.hbm.ntm.block.RBMKAutoloaderBlock;
import com.hbm.ntm.block.RBMKCraneConsoleBlock;
import com.hbm.ntm.block.RBMKDebrisBlock;
import com.hbm.ntm.block.RBMKPanelBlock;
import com.hbm.ntm.block.RBMKRadiatingDebrisBlock;
import com.hbm.ntm.block.RBMKUtilityBlock;
import com.hbm.ntm.block.SatelliteDockBlock;
import com.hbm.ntm.block.SatelliteLinkerBlock;
import com.hbm.ntm.block.SoyuzCapsuleBlock;
import com.hbm.ntm.block.SoyuzLauncherBlock;
import com.hbm.ntm.block.SoyuzStructBlock;
import com.hbm.ntm.block.SpotlightBeamBlock;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.block.MixerBlock;
import com.hbm.ntm.block.RedCableBoxBlock;
import com.hbm.ntm.block.RedCableBlock;
import com.hbm.ntm.block.RedCableClassicBlock;
import com.hbm.ntm.block.RedCableGaugeBlock;
import com.hbm.ntm.block.RedWireCoatedBlock;
import com.hbm.ntm.block.RemoteFluidMachineBlock;
import com.hbm.ntm.block.RotaryFurnaceBlock;
import com.hbm.ntm.block.OilDrillBlock;
import com.hbm.ntm.block.RefineryBlock;
import com.hbm.ntm.block.SolarBoilerBlock;
import com.hbm.ntm.block.SolarMirrorBlock;
import com.hbm.ntm.block.SolidifierBlock;
import com.hbm.ntm.block.SolderingStationBlock;
import com.hbm.ntm.block.SteamEngineBlock;
import com.hbm.ntm.block.StaticLegacyMultiblockMachineBlock;
import com.hbm.ntm.block.SteelScaffoldBlock;
import com.hbm.ntm.block.SteamTurbineBlock;
import com.hbm.ntm.block.SteamTurbineMultiblockBlock;
import com.hbm.ntm.block.StirlingBlock;
import com.hbm.ntm.block.StorageDrumBlock;
import com.hbm.ntm.block.SulfuricAcidLiquidBlock;
import com.hbm.ntm.block.SingleTurretBlock;
import com.hbm.ntm.block.TeslaBlock;
import com.hbm.ntm.block.TeleporterBlock;
import com.hbm.ntm.block.ThresherBlock;
import com.hbm.ntm.block.TrinketBlock;
import com.hbm.ntm.block.TrinketVariant;
import com.hbm.ntm.block.TurbofanBlock;
import com.hbm.ntm.block.TurbineGasBlock;
import com.hbm.ntm.block.TurretBaseBlock;
import com.hbm.ntm.block.VendingMachineBlock;
import com.hbm.ntm.block.WaterPumpBlock;
import com.hbm.ntm.block.WeaponTableBlock;
import com.hbm.ntm.block.WoodBurnerBlock;
import com.hbm.ntm.block.WasteDrumBlock;
import com.hbm.ntm.block.WatzEndBlock;
import com.hbm.ntm.block.WatzPumpBlock;
import com.hbm.ntm.block.WatzReactorBlock;
import com.hbm.ntm.block.WatzStructCoreBlock;
import com.hbm.ntm.block.ZirnoxDestroyedBlock;
import com.hbm.ntm.block.ZirnoxReactorBlock;
import com.hbm.ntm.block.conveyor.ChuteConveyorBlock;
import com.hbm.ntm.block.conveyor.ConveyorBlock;
import com.hbm.ntm.block.conveyor.DoubleConveyorBlock;
import com.hbm.ntm.block.conveyor.ExpressConveyorBlock;
import com.hbm.ntm.block.conveyor.LiftConveyorBlock;
import com.hbm.ntm.block.conveyor.TripleConveyorBlock;
import com.hbm.ntm.item.FluidDuctVariantBlockItem;
import com.hbm.ntm.item.FluidPipeStyleBlockItem;
import com.hbm.ntm.item.BalefireBombBlockItem;
import com.hbm.ntm.item.BombMultiBlockItem;
import com.hbm.ntm.item.GeigerBlockItem;
import com.hbm.ntm.item.HexafluorideTankBlockItem;
import com.hbm.ntm.item.LegacyDemonLampBlockItem;
import com.hbm.ntm.item.LegacyFanBlockItem;
import com.hbm.ntm.item.LegacyFileCabinetBlockItem;
import com.hbm.ntm.item.LegacyFloodlightBlockItem;
import com.hbm.ntm.item.LegacyLanternBlockItem;
import com.hbm.ntm.item.LegacyLoreBlockItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.MassStorageBlockItem;
import com.hbm.ntm.item.LegacyStateMultiblockBlockItem;
import com.hbm.ntm.item.MultiblockBlockItem;
import com.hbm.ntm.item.NuclearDeviceBlockItem;
import com.hbm.ntm.item.ObjMachineBlockItem;
import com.hbm.ntm.blockentity.CraneLogisticsBlockEntity;
import com.hbm.ntm.blockentity.LegacyFurnaceBlockEntity;
import com.hbm.ntm.blockentity.FusionTorusStructCoreBlockEntity;
import com.hbm.ntm.blockentity.ICFStructCoreBlockEntity;
import com.hbm.ntm.blockentity.LegacyGenericSelectorMachineBlockEntity;
import com.hbm.ntm.blockentity.ProcessingMachineBlockEntity;
import com.hbm.ntm.item.CableDiodeBlockItem;
import com.hbm.ntm.item.CrateBlockItem;
import com.hbm.ntm.item.RedCableBlockItem;
import com.hbm.ntm.item.TrinketBlockItem;
import com.hbm.ntm.item.TurretBlockItem;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.DummyBlock;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import com.hbm.ntm.neutron.PileGraphiteInsertionPlanner;
import com.hbm.ntm.radiation.RadiationConstants;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.blockentity.StorageCrateBlockEntity;
import com.hbm.ntm.turret.TurretArtyBlockEntity;
import com.hbm.ntm.turret.TurretChekhovBlockEntity;
import com.hbm.ntm.turret.TurretFriendlyBlockEntity;
import com.hbm.ntm.turret.TurretFritzBlockEntity;
import com.hbm.ntm.turret.TurretHimarsBlockEntity;
import com.hbm.ntm.turret.TurretHowardBlockEntity;
import com.hbm.ntm.turret.TurretHowardDamagedBlockEntity;
import com.hbm.ntm.turret.TurretJeremyBlockEntity;
import com.hbm.ntm.turret.TurretMaxwellBlockEntity;
import com.hbm.ntm.turret.TurretRichardBlockEntity;
import com.hbm.ntm.turret.TurretSentryBlockEntity;
import com.hbm.ntm.turret.TurretSentryDamagedBlockEntity;
import com.hbm.ntm.turret.TurretTauonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HbmNtm.MOD_ID);
    private static final Map<String, RegistryObject<? extends Block>> BLOCKS_BY_LEGACY_NAME = new LinkedHashMap<>();
    private static final SoundType PIPE_SOUND = new ForgeSoundType(
            0.85F,
            0.85F,
            () -> LegacySoundPlayer.resolveEvent("hbm:block.pipePlaced"),
            () -> SoundType.METAL.getStepSound(),
            () -> LegacySoundPlayer.resolveEvent("hbm:block.pipePlaced"),
            () -> SoundType.METAL.getHitSound(),
            () -> SoundType.METAL.getFallSound());

    private static final Set<String> ROOT_DIRECT_MACHINE_MODELS = Set.of(
            "fluidtank", "radiolysis", "refinery");
    private static final Set<String> MACHINE_DIRECT_MODELS = Set.of(
            "acidizer", "ammo_press", "annihilator", "arc_furnace", "arc_welder",
            "assembly_factory", "bat9000", "bigasstank", "boiler", "catalytic_cracker",
            "catalytic_reformer", "centrifuge", "chemical_factory", "chemical_plant",
            "chimney_brick", "chimney_industrial", "chungus", "coker", "combination_oven",
            "combustion_engine", "compressor", "condenser", "crucible", "cyclotron", "derrick",
            "dieselgen", "drain", "electric_heater", "electrolyser", "elevator",
            "exposure_chamber", "fan", "fel", "fensu2", "firebox", "flare_stack", "fracking_tower",
            "fraction_spacer", "fraction_tower", "furnace_iron", "furnace_steel", "gascent",
            "heatex", "heating_oven", "hephaestus", "hydrotreater", "industrial_boiler",
            "industrial_turbine", "intake", "liquefactor", "machine_deuterium_tower", "mixer",
            "mining_laser", "orbus", "ore_slopper", "pump", "pumpjack", "purex", "pyrooven",
            "radar_large", "radar_screen", "radgen", "rotary_furnace", "rtg", "sawmill",
            "silex", "solar_boiler", "soldering_station", "solidifier", "steam_engine",
            "strand_caster", "thresher", "tower_large", "tower_small", "turbinegas", "turbofan",
            "vacuum_distill", "vending_machine", "wood_burner");
    private static final Set<String> DIRECT_MACHINE_MODEL_TEXTURES = Set.of(
            "acidizer", "ammo_press", "annihilator", "arc_furnace", "arc_welder",
            "assembly_factory", "assembly_machine", "bat9000", "battery_socket", "bigasstank",
            "blast_furnace", "boiler", "catalytic_cracker", "catalytic_reformer", "centrifuge",
            "chemical_factory", "chemical_plant", "chimney_brick", "chimney_industrial", "chungus",
            "coker", "combination_oven", "combustion_engine", "compressor", "compressor_compact",
            "condenser", "crucible_heat", "cyclotron", "derrick", "dieselgen",
            "drain", "drum_gray", "electric_heater", "electrolyser", "elevator",
            "exposure_chamber", "fan", "fel", "fensu", "fensu2", "firebox",
            "flare_stack", "fracking_tower", "fraction_spacer", "fraction_tower", "furnace_iron",
            "furnace_steel", "gascent", "heater_heatex", "heating_oven", "hephaestus",
            "hydrotreater", "industrial_boiler", "industrial_turbine", "intake", "liquefactor",
            "machine_deuterium_tower", "mining_laser_base", "mining_laser_laser", "mining_laser_pivot",
            "mixer", "oilburner", "orbus", "ore_slopper", "pump_electric", "pump_steam",
            "pumpjack",
            "purex", "pyrooven", "radar_large", "radar_screen", "radgen",
            "rotary_furnace", "rtg", "sawmill", "silex", "solar_boiler",
            "solar_mirror", "soldering_station", "solidifier", "steam_engine", "strand_caster",
            "thresher", "tower_large", "tower_small", "turbinegas", "turbofan", "vacuum_distill", "vending_machine", "wood_burner");

    // Legacy 1.7.10 machine IDs. The press has the first BlockEntity scaffold so far.
    public static final RegistryObject<Block> MACHINE_PRESS = pressMachine("machine_press");
    public static final RegistryObject<Block> PRESS_PREHEATER = pressPreheater("press_preheater");
    public static final RegistryObject<Block> MACHINE_EPRESS = electricPressMachine("machine_epress");
    public static final RegistryObject<Block> MACHINE_DIFURNACE_OFF = diFurnaceMachine("machine_difurnace_off");
    public static final RegistryObject<Block> MACHINE_DIFURNACE_EXTENSION =
            diFurnaceExtension("machine_difurnace_extension");
    public static final RegistryObject<Block> MACHINE_ELECTRIC_FURNACE_OFF = electricFurnaceMachine("machine_electric_furnace_off");
    public static final RegistryObject<Block> MACHINE_BOILER_OFF = boilerMachine("machine_boiler_off");
    public static final RegistryObject<Block> MACHINE_SHREDDER = shredderMachine("machine_shredder");
    public static final RegistryObject<Block> MACHINE_AUTOCRAFTER = autocrafterMachine("machine_autocrafter");
    public static final RegistryObject<Block> MACHINE_DETECTOR = powerDetector("machine_detector");
    public static final RegistryObject<Block> CHARGER = chargerMachine("charger");
    public static final RegistryObject<Block> REFUELER = refuelerMachine("refueler");
    public static final RegistryObject<Block> RADIOBOX = radioDecoMachine("radiobox", true);
    public static final RegistryObject<Block> RADIOREC = radioDecoMachine("radiorec", false);
    public static final RegistryObject<Block> TESLA = teslaMachine("tesla");
    public static final RegistryObject<Block> MACHINE_MICROWAVE = microwaveMachine("machine_microwave");
    public static final RegistryObject<Block> MACHINE_FUNNEL = funnelMachine("machine_funnel");
    public static final RegistryObject<Block> MACHINE_KEYFORGE = keyForgeMachine("machine_keyforge");
    public static final RegistryObject<Block> MACHINE_AUTOSAW = autosawMachine("machine_autosaw");
    public static final RegistryObject<Block> MACHINE_THRESHER = thresherMachine("machine_thresher");
    public static final RegistryObject<Block> MACHINE_TURBINE = steamTurbineMachine("machine_turbine");
    public static final RegistryObject<Block> MACHINE_INDUSTRIAL_TURBINE = steamTurbineMultiblockMachine(
            "machine_industrial_turbine", industrialTurbineDefinition(), SteamTurbineMultiblockBlock.Kind.INDUSTRIAL);
    public static final RegistryObject<Block> MACHINE_LARGE_TURBINE = legacyLargeTurbineMachine(
            "machine_large_turbine", legacyLargeTurbineDefinition());
    public static final RegistryObject<Block> MACHINE_CONDENSER = condenserMachine("machine_condenser");
    public static final RegistryObject<Block> DECON = decon("decon");
    public static final RegistryObject<Block> MACHINE_ARMOR_TABLE = armorTable("machine_armor_table");
    public static final RegistryObject<Block> MACHINE_WEAPON_TABLE = weaponTable("machine_weapon_table");
    public static final RegistryObject<Block> MACHINE_SIREN = siren("machine_siren");
    public static final RegistryObject<Block> FAN = fan("fan");
    public static final RegistryObject<Block> FILING_CABINET = filingCabinet("filing_cabinet");
    public static final RegistryObject<Block> ANVIL_IRON = anvil("anvil_iron", NTMAnvilBlock.TIER_IRON);
    public static final RegistryObject<Block> ANVIL_LEAD = anvil("anvil_lead", NTMAnvilBlock.TIER_IRON);
    public static final RegistryObject<Block> ANVIL_STEEL = anvil("anvil_steel", NTMAnvilBlock.TIER_STEEL);
    public static final RegistryObject<Block> ANVIL_DESH = anvil("anvil_desh", NTMAnvilBlock.TIER_OIL);
    public static final RegistryObject<Block> ANVIL_FERROURANIUM =
            anvil("anvil_ferrouranium", NTMAnvilBlock.TIER_NUCLEAR);
    public static final RegistryObject<Block> ANVIL_SATURNITE = anvil("anvil_saturnite", NTMAnvilBlock.TIER_RBMK);
    public static final RegistryObject<Block> ANVIL_BISMUTH_BRONZE =
            anvil("anvil_bismuth_bronze", NTMAnvilBlock.TIER_RBMK);
    public static final RegistryObject<Block> ANVIL_ARSENIC_BRONZE =
            anvil("anvil_arsenic_bronze", NTMAnvilBlock.TIER_RBMK);
    public static final RegistryObject<Block> ANVIL_SCHRABIDATE =
            anvil("anvil_schrabidate", NTMAnvilBlock.TIER_FUSION);
    public static final RegistryObject<Block> ANVIL_DNT = anvil("anvil_dnt", NTMAnvilBlock.TIER_PARTICLE);
    public static final RegistryObject<Block> ANVIL_OSMIRIDIUM =
            anvil("anvil_osmiridium", NTMAnvilBlock.TIER_GERALD);
    public static final RegistryObject<Block> ANVIL_MURKY = anvil("anvil_murky", 1_916_169);
    public static final RegistryObject<Block> RED_CABLE = redCable("red_cable");
    public static final RegistryObject<Block> RED_CABLE_CLASSIC = redCableClassic("red_cable_classic");
    public static final RegistryObject<Block> RED_WIRE_COATED = redWireCoated("red_wire_coated");
    public static final RegistryObject<Block> RED_CABLE_BOX = redCableBox("red_cable_box");
    public static final RegistryObject<Block> RED_CABLE_GAUGE = redCableGauge("red_cable_gauge");
    public static final RegistryObject<Block> CABLE_SWITCH = poweredRedCable("cable_switch",
            PoweredRedCableBlock.Kind.SWITCH);
    public static final RegistryObject<Block> CABLE_DETECTOR = poweredRedCable("cable_detector",
            PoweredRedCableBlock.Kind.DETECTOR);
    public static final RegistryObject<Block> CABLE_DIODE = cableDiode("cable_diode");
    public static final RegistryObject<Block> RED_CONNECTOR =
            redConnector("red_connector", LegacyConnectorBlock.Kind.NORMAL);
    public static final RegistryObject<Block> RED_CONNECTOR_SUPER =
            redConnector("red_connector_super", LegacyConnectorBlock.Kind.SUPER);
    public static final RegistryObject<Block> RED_PYLON = smallPylon("red_pylon");
    public static final RegistryObject<Block> RED_PYLON_MEDIUM_WOOD =
            mediumPylon("red_pylon_medium_wood", LegacyMediumPylonBlock.Kind.WOOD);
    public static final RegistryObject<Block> RED_PYLON_MEDIUM_WOOD_TRANSFORMER =
            mediumPylon("red_pylon_medium_wood_transformer", LegacyMediumPylonBlock.Kind.WOOD_TRANSFORMER);
    public static final RegistryObject<Block> RED_PYLON_MEDIUM_STEEL =
            mediumPylon("red_pylon_medium_steel", LegacyMediumPylonBlock.Kind.STEEL);
    public static final RegistryObject<Block> RED_PYLON_MEDIUM_STEEL_TRANSFORMER =
            mediumPylon("red_pylon_medium_steel_transformer", LegacyMediumPylonBlock.Kind.STEEL_TRANSFORMER);
    public static final RegistryObject<Block> RED_PYLON_LARGE = largePylon("red_pylon_large");
    public static final RegistryObject<Block> SUBSTATION = substation("substation");
    public static final RegistryObject<Block> RADIO_TORCH_SENDER = radioTorchSender("radio_torch_sender");
    public static final RegistryObject<Block> RADIO_TORCH_RECEIVER = radioTorchReceiver("radio_torch_receiver");
    public static final RegistryObject<Block> RADIO_TORCH_COUNTER = radioTorchCounter("radio_torch_counter");
    public static final RegistryObject<Block> RADIO_TORCH_LOGIC = radioTorchLogic("radio_torch_logic");
    public static final RegistryObject<Block> RADIO_TORCH_READER = radioTorchReader("radio_torch_reader");
    public static final RegistryObject<Block> RADIO_TORCH_CONTROLLER = radioTorchController("radio_torch_controller");
    public static final RegistryObject<Block> RADIO_AUTOCAL = radioAutocal("radio_autocal");
    public static final RegistryObject<Block> RADIO_TELEX = radioTelex("radio_telex");
    public static final RegistryObject<Block> RBMK_DISPLAY_BLANK = rbmkDisplayBlank("rbmk_display_blank");
    public static final RegistryObject<Block> RBMK_DISPLAY = rbmkPanel("rbmk_display", RBMKPanelPlanner.PanelType.DISPLAY);
    public static final RegistryObject<Block> RBMK_GAUGE = rbmkPanel("rbmk_gauge", RBMKPanelPlanner.PanelType.GAUGE);
    public static final RegistryObject<Block> RBMK_GRAPH = rbmkPanel("rbmk_graph", RBMKPanelPlanner.PanelType.GRAPH);
    public static final RegistryObject<Block> RBMK_INDICATOR = rbmkPanel("rbmk_indicator", RBMKPanelPlanner.PanelType.INDICATOR);
    public static final RegistryObject<Block> RBMK_KEY_PAD = rbmkPanel("rbmk_key_pad", RBMKPanelPlanner.PanelType.KEYPAD);
    public static final RegistryObject<Block> RBMK_LEVER = rbmkPanel("rbmk_lever", RBMKPanelPlanner.PanelType.LEVER);
    public static final RegistryObject<Block> RBMK_NUMITRON = rbmkPanel("rbmk_numitron", RBMKPanelPlanner.PanelType.NUMITRON);
    public static final RegistryObject<Block> RBMK_TERMINAL = rbmkPanel("rbmk_terminal", RBMKPanelPlanner.PanelType.TERMINAL);
    public static final RegistryObject<Block> RBMK_BLANK = rbmkColumn("rbmk_blank", RBMKColumnBlock.Kind.BLANK);
    public static final RegistryObject<Block> RBMK_MODERATOR = rbmkColumn("rbmk_moderator", RBMKColumnBlock.Kind.MODERATOR);
    public static final RegistryObject<Block> RBMK_REFLECTOR = rbmkColumn("rbmk_reflector", RBMKColumnBlock.Kind.REFLECTOR);
    public static final RegistryObject<Block> RBMK_ABSORBER = rbmkColumn("rbmk_absorber", RBMKColumnBlock.Kind.ABSORBER);
    public static final RegistryObject<Block> RBMK_ROD = rbmkColumn("rbmk_rod", RBMKColumnBlock.Kind.ROD);
    public static final RegistryObject<Block> RBMK_ROD_MOD = rbmkColumn("rbmk_rod_mod", RBMKColumnBlock.Kind.ROD_MOD);
    public static final RegistryObject<Block> RBMK_ROD_REASIM = rbmkColumn("rbmk_rod_reasim", RBMKColumnBlock.Kind.ROD_REASIM);
    public static final RegistryObject<Block> RBMK_ROD_REASIM_MOD =
            rbmkColumn("rbmk_rod_reasim_mod", RBMKColumnBlock.Kind.ROD_REASIM_MOD);
    public static final RegistryObject<Block> RBMK_BOILER = rbmkColumn("rbmk_boiler", RBMKColumnBlock.Kind.BOILER);
    public static final RegistryObject<Block> RBMK_HEATER = rbmkColumn("rbmk_heater", RBMKColumnBlock.Kind.HEATER);
    public static final RegistryObject<Block> RBMK_COOLER = rbmkColumn("rbmk_cooler", RBMKColumnBlock.Kind.COOLER);
    public static final RegistryObject<Block> RBMK_OUTGASSER =
            rbmkColumn("rbmk_outgasser", RBMKColumnBlock.Kind.OUTGASSER);
    public static final RegistryObject<Block> RBMK_STORAGE = rbmkColumn("rbmk_storage", RBMKColumnBlock.Kind.STORAGE);
    public static final RegistryObject<Block> RBMK_CONTROL = rbmkColumn("rbmk_control", RBMKColumnBlock.Kind.CONTROL);
    public static final RegistryObject<Block> RBMK_CONTROL_MOD = rbmkColumn("rbmk_control_mod", RBMKColumnBlock.Kind.CONTROL_MOD);
    public static final RegistryObject<Block> RBMK_CONTROL_AUTO = rbmkColumn("rbmk_control_auto", RBMKColumnBlock.Kind.CONTROL_AUTO);
    public static final RegistryObject<Block> RBMK_CONTROL_REASIM =
            rbmkColumn("rbmk_control_reasim", RBMKColumnBlock.Kind.CONTROL_REASIM);
    public static final RegistryObject<Block> RBMK_CONTROL_REASIM_AUTO =
            rbmkColumn("rbmk_control_reasim_auto", RBMKColumnBlock.Kind.CONTROL_REASIM_AUTO);
    public static final RegistryObject<Block> RBMK_AUTOLOADER = rbmkAutoloader("rbmk_autoloader");
    public static final RegistryObject<Block> RBMK_CONSOLE = rbmkConsole("rbmk_console");
    public static final RegistryObject<Block> RBMK_CRANE_CONSOLE = rbmkCraneConsole("rbmk_crane_console");
    public static final RegistryObject<Block> RBMK_LOADER = rbmkUtilityBlock("rbmk_loader", RBMKUtilityBlock.Kind.LOADER);
    public static final RegistryObject<Block> RBMK_STEAM_INLET =
            rbmkUtilityBlock("rbmk_steam_inlet", RBMKUtilityBlock.Kind.STEAM_INLET);
    public static final RegistryObject<Block> RBMK_STEAM_OUTLET =
            rbmkUtilityBlock("rbmk_steam_outlet", RBMKUtilityBlock.Kind.STEAM_OUTLET);
    public static final RegistryObject<Block> BLOCK_GRAPHITE = pileGraphite("block_graphite");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_DRILLED = pileGraphiteDrilled("block_graphite_drilled");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_FUEL = pileGraphiteFuel("block_graphite_fuel");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_PLUTONIUM = pileGraphiteSource(
            "block_graphite_plutonium",
            PileGraphiteInsertionPlanner.GraphiteBlockKind.PLUTONIUM);
    public static final RegistryObject<Block> BLOCK_GRAPHITE_ROD = pileGraphiteRod("block_graphite_rod");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_SOURCE = pileGraphiteSource(
            "block_graphite_source",
            PileGraphiteInsertionPlanner.GraphiteBlockKind.SOURCE);
    public static final RegistryObject<Block> BLOCK_GRAPHITE_LITHIUM = pileGraphiteBreedingFuel("block_graphite_lithium");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_TRITIUM = pileGraphiteBreedingProduct("block_graphite_tritium");
    public static final RegistryObject<Block> BLOCK_GRAPHITE_DETECTOR =
            pileGraphiteNeutronDetector("block_graphite_detector");
    public static final RegistryObject<Block> FLUID_DUCT_NEO = fluidPipe("fluid_duct_neo");
    public static final RegistryObject<Block> FLUID_VALVE = fluidValve("fluid_valve", FluidValveBlock.Kind.VALVE);
    public static final RegistryObject<Block> FLUID_SWITCH = fluidValve("fluid_switch", FluidValveBlock.Kind.SWITCH);
    public static final RegistryObject<Block> FLUID_COUNTER_VALVE =
            fluidValve("fluid_counter_valve", FluidValveBlock.Kind.COUNTER);
    public static final RegistryObject<Block> FLUID_PUMP = fluidPump("fluid_pump");
    public static final RegistryObject<Block> FLUID_DUCT_BOX = fluidDuctBox("fluid_duct_box");
    public static final RegistryObject<Block> FLUID_DUCT_GAUGE = fluidDuctGauge("fluid_duct_gauge");
    public static final RegistryObject<Block> FLUID_DUCT_EXHAUST = fluidDuctExhaust("fluid_duct_exhaust");
    public static final RegistryObject<Block> FLUID_DUCT_PAINTABLE = fluidDuctPaintable("fluid_duct_paintable");
    public static final RegistryObject<Block> FLUID_DUCT_PAINTABLE_BLOCK_EXHAUST =
            fluidDuctPaintableExhaust("fluid_duct_paintable_block_exhaust");
    public static final RegistryObject<Block> PIPE_ANCHOR = fluidPipeAnchor("pipe_anchor");
    public static final RegistryObject<Block> BARREL_PLASTIC =
            fluidBarrel("barrel_plastic", FluidBarrelBlock.Variant.PLASTIC);
    public static final RegistryObject<Block> BARREL_CORRODED =
            fluidBarrel("barrel_corroded", FluidBarrelBlock.Variant.CORRODED);
    public static final RegistryObject<Block> BARREL_STEEL =
            fluidBarrel("barrel_steel", FluidBarrelBlock.Variant.STEEL);
    public static final RegistryObject<Block> BARREL_TCALLOY =
            fluidBarrel("barrel_tcalloy", FluidBarrelBlock.Variant.TCALLOY);
    public static final RegistryObject<Block> BARREL_ANTIMATTER =
            fluidBarrel("barrel_antimatter", FluidBarrelBlock.Variant.ANTIMATTER);
    public static final RegistryObject<Block> PNEUMATIC_TUBE = pneumaticTube("pneumatic_tube");
    public static final RegistryObject<Block> CONVEYOR = conveyor("conveyor", ConveyorBlock::new);
    public static final RegistryObject<Block> CONVEYOR_EXPRESS = conveyor("conveyor_express", ExpressConveyorBlock::new);
    public static final RegistryObject<Block> CONVEYOR_DOUBLE = conveyor("conveyor_double", DoubleConveyorBlock::new);
    public static final RegistryObject<Block> CONVEYOR_TRIPLE = conveyor("conveyor_triple", TripleConveyorBlock::new);
    public static final RegistryObject<Block> CONVEYOR_LIFT = conveyor("conveyor_lift", LiftConveyorBlock::new);
    public static final RegistryObject<Block> CONVEYOR_CHUTE = conveyor("conveyor_chute", ChuteConveyorBlock::new);
    public static final RegistryObject<Block> CRANE_EXTRACTOR =
            craneLogistics("crane_extractor", CraneLogisticsBlockEntity.Kind.EXTRACTOR);
    public static final RegistryObject<Block> CRANE_INSERTER =
            craneLogistics("crane_inserter", CraneLogisticsBlockEntity.Kind.INSERTER);
    public static final RegistryObject<Block> CRANE_GRABBER =
            craneLogistics("crane_grabber", CraneLogisticsBlockEntity.Kind.GRABBER);
    public static final RegistryObject<Block> CRANE_ROUTER =
            craneLogistics("crane_router", CraneLogisticsBlockEntity.Kind.ROUTER);
    public static final RegistryObject<Block> CRANE_BOXER =
            craneLogistics("crane_boxer", CraneLogisticsBlockEntity.Kind.BOXER);
    public static final RegistryObject<Block> CRANE_UNBOXER =
            craneLogistics("crane_unboxer", CraneLogisticsBlockEntity.Kind.UNBOXER);
    public static final RegistryObject<Block> CRANE_PARTITIONER =
            craneLogistics("crane_partitioner", CraneLogisticsBlockEntity.Kind.PARTITIONER);
    public static final RegistryObject<Block> CRANE_SPLITTER = craneSplitter("crane_splitter");
    public static final RegistryObject<Block> MACHINE_BATTERY = machineBattery("machine_battery");
    public static final RegistryObject<Block> MACHINE_BATTERY_POTATO =
            machineBattery("machine_battery_potato", 10_000L);
    public static final RegistryObject<Block> MACHINE_LITHIUM_BATTERY =
            machineBattery("machine_lithium_battery", 50_000_000L);
    public static final RegistryObject<Block> MACHINE_SCHRABIDIUM_BATTERY =
            machineBattery("machine_schrabidium_battery", 25_000_000_000L);
    public static final RegistryObject<Block> MACHINE_DINEUTRONIUM_BATTERY =
            machineBattery("machine_dineutronium_battery", 1_000_000_000_000L);
    public static final RegistryObject<Block> HEV_BATTERY =
            registerBlockWithItemName("hev_battery", "hev_battery_block",
                    () -> new HevBatteryBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5F, 0.25F)
                            .sound(SoundType.METAL)
                            .lightLevel(state -> 10)
                            .noOcclusion()
                            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> MACHINE_FENSU = machineFensu("machine_fensu");
    public static final RegistryObject<Block> MACHINE_BATTERY_REDD = batteryReddMachine("machine_battery_redd",
            batteryReddDefinition());
    public static final RegistryObject<Block> MACHINE_BATTERY_SOCKET = machineBatterySocket("machine_battery_socket");
    public static final RegistryObject<Block> CRATE_IRON =
            storageCrate("crate_iron", StorageCrateBlockEntity.Kind.IRON);
    public static final RegistryObject<Block> CRATE_STEEL =
            storageCrate("crate_steel", StorageCrateBlockEntity.Kind.STEEL);
    public static final RegistryObject<Block> CRATE_DESH =
            storageCrate("crate_desh", StorageCrateBlockEntity.Kind.DESH);
    public static final RegistryObject<Block> CRATE_TUNGSTEN =
            storageCrate("crate_tungsten", StorageCrateBlockEntity.Kind.TUNGSTEN);
    public static final RegistryObject<Block> SAFE = safe("safe");
    public static final RegistryObject<Block> MASS_STORAGE = massStorage("mass_storage");
    public static final RegistryObject<Block> TURRET_CHEKHOV = turretChekhov("turret_chekhov");
    public static final RegistryObject<Block> TURRET_FRIENDLY = turretFriendly("turret_friendly");
    public static final RegistryObject<Block> TURRET_JEREMY = turretJeremy("turret_jeremy");
    public static final RegistryObject<Block> TURRET_RICHARD = turretRichard("turret_richard");
    public static final RegistryObject<Block> TURRET_TAUON = turretTauon("turret_tauon");
    public static final RegistryObject<Block> TURRET_HOWARD = turretHoward("turret_howard");
    public static final RegistryObject<Block> TURRET_SENTRY = turretSentry("turret_sentry");
    public static final RegistryObject<Block> TURRET_HOWARD_DAMAGED = turretHowardDamaged("turret_howard_damaged");
    public static final RegistryObject<Block> TURRET_SENTRY_DAMAGED = turretSentryDamaged("turret_sentry_damaged");
    public static final RegistryObject<Block> TURRET_MAXWELL = turretMaxwell("turret_maxwell");
    public static final RegistryObject<Block> TURRET_ARTY = turretArty("turret_arty");
    public static final RegistryObject<Block> TURRET_HIMARS = turretHimars("turret_himars");
    public static final RegistryObject<Block> TURRET_FRITZ = turretFritz("turret_fritz");
    public static final RegistryObject<Block> MACHINE_RADAR = machineRadar("machine_radar");
    public static final RegistryObject<Block> MACHINE_RADAR_LARGE = machineRadarLarge("machine_radar_large",
            radarLargeDefinition());
    public static final RegistryObject<Block> MACHINE_RADAR_SCREEN = machineRadarScreen("radar_screen",
            radarScreenDefinition());
    public static final RegistryObject<Block> MACHINE_SATLINKER = satelliteLinker("machine_satlinker");
    public static final RegistryObject<Block> SAT_DOCK = satelliteDock("sat_dock", satDockDefinition());
    public static final RegistryObject<Block> SOYUZ_CAPSULE = soyuzCapsule("soyuz_capsule");
    public static final RegistryObject<Block> SOYUZ_LAUNCHER = soyuzLauncher("soyuz_launcher",
            soyuzLauncherDefinition());
    public static final RegistryObject<Block> STRUCT_LAUNCHER = launchStructureBlock("struct_launcher");
    public static final RegistryObject<Block> STRUCT_SCAFFOLD = launchStructureBlock("struct_scaffold");
    public static final RegistryObject<Block> STRUCT_SOYUZ_CORE = soyuzStructCoreBlock("struct_soyuz_core");
    public static final RegistryObject<Block> LAUNCH_PAD = launchPad("launch_pad");
    public static final RegistryObject<Block> LAUNCH_PAD_LARGE = largeLaunchPad("launch_pad_large");
    public static final RegistryObject<Block> LAUNCH_PAD_RUSTED = rustedLaunchPad("launch_pad_rusted");
    public static final RegistryObject<Block> LAUNCH_TABLE = customMissileLauncher("launch_table",
            CustomMissileLauncherBlock.Kind.LAUNCH_TABLE);
    public static final RegistryObject<Block> COMPACT_LAUNCHER = customMissileLauncher("compact_launcher",
            CustomMissileLauncherBlock.Kind.COMPACT_LAUNCHER);
    public static final RegistryObject<Block> MACHINE_MISSILE_ASSEMBLY = missileAssembly("machine_missile_assembly");
    public static final RegistryObject<Block> PA_SOURCE =
            particleAccelerator("pa_source", ParticleAcceleratorBlock.Variant.SOURCE);
    public static final RegistryObject<Block> PA_BEAMLINE =
            particleAccelerator("pa_beamline", ParticleAcceleratorBlock.Variant.BEAMLINE);
    public static final RegistryObject<Block> PA_RFC =
            particleAccelerator("pa_rfc", ParticleAcceleratorBlock.Variant.RFC);
    public static final RegistryObject<Block> PA_QUADRUPOLE =
            particleAccelerator("pa_quadrupole", ParticleAcceleratorBlock.Variant.QUADRUPOLE);
    public static final RegistryObject<Block> PA_DIPOLE =
            particleAccelerator("pa_dipole", ParticleAcceleratorBlock.Variant.DIPOLE);
    public static final RegistryObject<Block> PA_DETECTOR =
            particleAccelerator("pa_detector", ParticleAcceleratorBlock.Variant.DETECTOR);
    public static final RegistryObject<Block> GAS_RADON = gasRadon("gas_radon", LegacyGasRadonBlock.Kind.NORMAL);
    public static final RegistryObject<Block> GAS_RADON_DENSE = gasRadon("gas_radon_dense", LegacyGasRadonBlock.Kind.DENSE);
    public static final RegistryObject<Block> GAS_RADON_TOMB = gasRadon("gas_radon_tomb", LegacyGasRadonBlock.Kind.TOMB);
    public static final RegistryObject<Block> GAS_MELTDOWN = gasMeltdown("gas_meltdown");
    public static final RegistryObject<Block> GAS_MONOXIDE = toxicGas("gas_monoxide", LegacyToxicGasBlock.Kind.MONOXIDE);
    public static final RegistryObject<Block> GAS_ASBESTOS = toxicGas("gas_asbestos", LegacyToxicGasBlock.Kind.ASBESTOS);
    public static final RegistryObject<Block> GAS_COAL = toxicGas("gas_coal", LegacyToxicGasBlock.Kind.COAL);
    public static final RegistryObject<Block> CHLORINE_GAS = toxicGas("chlorine_gas", LegacyToxicGasBlock.Kind.CHLORINE);
    public static final RegistryObject<Block> ACID_BLOCK = acidLiquidBlock("acid_block");
    public static final RegistryObject<Block> TOXIC_BLOCK = toxicLiquidBlock("toxic_block");
    public static final RegistryObject<Block> SCHRABIDIC_BLOCK = schrabidicLiquidBlock("schrabidic_block");
    public static final RegistryObject<Block> SULFURIC_ACID_BLOCK = registerBlockWithoutItem(
            "sulfuric_acid_block",
            () -> new SulfuricAcidLiquidBlock(ModFluids.SULFURIC_ACID.source(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .replaceable()
                    .noCollission()
                    .strength(100.0F)
                    .liquid()
                    .noLootTable()));
    static {
        ModFluids.PEROXIDE.properties().block(() -> (LiquidBlock) ACID_BLOCK.get());
        ModFluids.toxicProperties().block(() -> (LiquidBlock) TOXIC_BLOCK.get());
        ModFluids.SCHRABIDIC.properties().block(() -> (LiquidBlock) SCHRABIDIC_BLOCK.get());
        ModFluids.SULFURIC_ACID.properties().block(() -> (LiquidBlock) SULFURIC_ACID_BLOCK.get());
    }
    public static final RegistryObject<Block> RAD_ABSORBER = radAbsorber("rad_absorber");
    public static final RegistryObject<Block> GEIGER = geiger("geiger");
    public static final RegistryObject<Block> FLOODLIGHT_BEAM = BLOCKS.register("floodlight_beam",
            () -> new FloodlightBeamBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .lightLevel(state -> 15)
                    .strength(-1.0F, 1_000_000.0F)));
    public static final RegistryObject<Block> SPOTLIGHT_BEAM = hiddenBlock("spotlight_beam",
            () -> new SpotlightBeamBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .replaceable()
                    .noCollission()
                    .noOcclusion()
                    .lightLevel(state -> 15)
                    .strength(-1.0F, 1_000_000.0F)));
    public static final RegistryObject<Block> SPOTLIGHT_INCANDESCENT_OFF =
            hiddenBlock("spotlight_incandescent_off", () -> LegacySpotlightBlock.incandescent(
                    simpleResourceProperties("spotlight_incandescent_off", "cage_lamp_off").noOcclusion(), false));
    public static final RegistryObject<Block> SPOTLIGHT_FLUORO_OFF =
            hiddenBlock("spotlight_fluoro_off", () -> LegacySpotlightBlock.fluoro(
                    simpleResourceProperties("spotlight_fluoro_off", "fluorescent_lamp_off").noOcclusion(), false));
    public static final RegistryObject<Block> SPOTLIGHT_HALOGEN_OFF =
            hiddenBlock("spotlight_halogen_off", () -> LegacySpotlightBlock.halogen(
                    simpleResourceProperties("spotlight_halogen_off", "flood_lamp_off").noOcclusion(), false));
    public static final RegistryObject<Block> DUMMY_BLOCK = dummyBlock("dummy_block");
    public static final RegistryObject<Block> STEEL_SCAFFOLD = steelScaffold("steel_scaffold");
    public static final RegistryObject<Block> STEEL_BEAM = steelBeam("steel_beam");
    public static final RegistryObject<Block> STEEL_GRATE = steelGrate("steel_grate", false);
    public static final RegistryObject<Block> STEEL_GRATE_WIDE = steelGrate("steel_grate_wide", true);
    public static final RegistryObject<Block> CHAIN = chain("chain");
    public static final RegistryObject<Block> POLE_TOP = poleTop("pole_top");
    public static final RegistryObject<Block> POLE_SATELLITE_RECEIVER =
            poleSatelliteReceiver("pole_satellite_receiver");
    public static final RegistryObject<Block> VENDING_MACHINE = vendingMachine("vending_machine");
    public static final RegistryObject<Block> MACHINE_ASSEMBLY_MACHINE = assemblyMachine("machine_assembly_machine");
    public static final RegistryObject<Block> MACHINE_TELEPORTER = teleporterMachine("machine_teleporter");
    public static final RegistryObject<Block> MACHINE_CHEMICAL_PLANT = chemicalPlantMachine("machine_chemical_plant",
            chemicalPlantDefinition());
    public static final RegistryObject<Block> MACHINE_LIQUEFACTOR = liquefactorMachine("machine_liquefactor",
            liquefactorDefinition());
    public static final RegistryObject<Block> MACHINE_CHEMICAL_FACTORY = chemicalFactoryMachine("machine_chemical_factory",
            chemicalFactoryDefinition());
    public static final RegistryObject<Block> MACHINE_REFINERY = refineryMachine("machine_refinery",
            refineryDefinition());
    public static final RegistryObject<Block> MACHINE_CATALYTIC_CRACKER = remoteFluidMachine("machine_catalytic_cracker",
            catalyticCrackerDefinition(), RemoteFluidMachineBlock.Kind.CATALYTIC_CRACKER);
    public static final RegistryObject<Block> MACHINE_CATALYTIC_REFORMER = remoteFluidMachine("machine_catalytic_reformer",
            catalyticReformerDefinition(), RemoteFluidMachineBlock.Kind.CATALYTIC_REFORMER);
    public static final RegistryObject<Block> MACHINE_VACUUM_DISTILL = remoteFluidMachine("machine_vacuum_distill",
            vacuumDistillDefinition(), RemoteFluidMachineBlock.Kind.VACUUM_DISTILL);
    public static final RegistryObject<Block> MACHINE_FRACTION_TOWER = remoteFluidMachine("machine_fraction_tower",
            fractionTowerDefinition(), RemoteFluidMachineBlock.Kind.FRACTION_TOWER);
    public static final RegistryObject<Block> MACHINE_HYDROTREATER = remoteFluidMachine("machine_hydrotreater",
            hydrotreaterDefinition(), RemoteFluidMachineBlock.Kind.HYDROTREATER);
    public static final RegistryObject<Block> MACHINE_COKER = remoteFluidMachine("machine_coker",
            cokerDefinition(), RemoteFluidMachineBlock.Kind.COKER);
    public static final RegistryObject<Block> MACHINE_PYROOVEN = pyroOvenMachine("machine_pyrooven",
            pyroOvenDefinition());
    public static final RegistryObject<Block> MACHINE_SOLIDIFIER = solidifierMachine("machine_solidifier",
            solidifierDefinition());
    public static final RegistryObject<Block> MACHINE_COMPRESSOR = compressorMachine("machine_compressor",
            compressorDefinition());
    public static final RegistryObject<Block> MACHINE_COMPRESSOR_COMPACT = compressorMachine(
            "machine_compressor_compact", compressorCompactDefinition());
    public static final RegistryObject<Block> MACHINE_LPW2 = machineLpw2("machine_lpw2");
    public static final RegistryObject<Block> MACHINE_CONTROLLER = reactorControl("machine_controller");
    public static final RegistryObject<Block> REACTOR_RESEARCH = researchReactor("reactor_research",
            researchReactorDefinition());
    public static final RegistryObject<Block> MACHINE_REACTOR_BREEDING = breedingReactor("machine_reactor_breeding",
            breedingReactorDefinition());
    public static final RegistryObject<Block> REACTOR_ZIRNOX = zirnoxReactor("reactor_zirnox",
            zirnoxReactorDefinition());
    public static final RegistryObject<Block> ZIRNOX_DESTROYED = zirnoxDestroyed("zirnox_destroyed",
            zirnoxDestroyedDefinition());
    public static final RegistryObject<Block> WATZ = watzReactor("watz", watzDefinition());
    public static final RegistryObject<Block> WATZ_PUMP = watzPump("watz_pump",
            watzPumpDefinition());
    public static final RegistryObject<Block> STRUCT_WATZ_CORE = watzStructCoreBlock("struct_watz_core");
    public static final RegistryObject<Block> WATZ_ELEMENT = watzPillar("watz_element");
    public static final RegistryObject<Block> WATZ_COOLER = watzPillar("watz_cooler");
    public static final RegistryObject<Block> WATZ_END = watzEndBlock("watz_end");
    public static final RegistryObject<Block> DFC_CORE = dfcMachine("dfc_core", DfcMachineBlock.Kind.CORE);
    public static final RegistryObject<Block> DFC_EMITTER = dfcMachine("dfc_emitter", DfcMachineBlock.Kind.EMITTER);
    public static final RegistryObject<Block> DFC_RECEIVER = dfcMachine("dfc_receiver", DfcMachineBlock.Kind.RECEIVER);
    public static final RegistryObject<Block> DFC_INJECTOR = dfcMachine("dfc_injector", DfcMachineBlock.Kind.INJECTOR);
    public static final RegistryObject<Block> DFC_STABILIZER = dfcMachine("dfc_stabilizer", DfcMachineBlock.Kind.STABILIZER);
    public static final RegistryObject<Block> STRUCT_TORUS_CORE = fusionTorusStructCoreBlock("struct_torus_core");
    public static final RegistryObject<Block> FUSION_COMPONENT_BSCCO =
            fusionBsccoComponentBlock("fusion_component_bscco");
    public static final RegistryObject<Block> FUSION_COMPONENT_BSCCO_WELDED =
            fusionStructureBlock("fusion_component_bscco_welded");
    public static final RegistryObject<Block> FUSION_COMPONENT_BLANKET =
            fusionStructureBlock("fusion_component_blanket");
    public static final RegistryObject<Block> FUSION_COMPONENT_MOTOR =
            fusionStructureBlock("fusion_component_motor");
    public static final RegistryObject<Block> STRUCT_ICF_CORE = icfStructCoreBlock("struct_icf_core");
    public static final RegistryObject<Block> ICF_COMPONENT_SCAFFOLD =
            icfStructureBlock("icf_component_scaffold");
    public static final RegistryObject<Block> ICF_COMPONENT_VESSEL =
            icfVesselComponentBlock("icf_component_vessel");
    public static final RegistryObject<Block> ICF_COMPONENT_VESSEL_WELDED =
            icfStructureBlock("icf_component_vessel_welded");
    public static final RegistryObject<Block> ICF_COMPONENT_STRUCTURE =
            icfStructureComponentBlock("icf_component_structure");
    public static final RegistryObject<Block> ICF_COMPONENT_STRUCTURE_BOLTED =
            icfStructureBlock("icf_component_structure_bolted");
    public static final RegistryObject<Block> ICF = icfReactor("icf", icfDefinition());
    public static final RegistryObject<Block> ICF_BLOCK = icfAssembledBlock("icf_block");
    public static final RegistryObject<Block> FUSION_TORUS = fusionMachine("fusion_torus",
            fusionTorusDefinition(), FusionMachineBlock.Kind.TORUS);
    public static final RegistryObject<Block> FUSION_KLYSTRON = fusionMachine("fusion_klystron",
            fusionKlystronDefinition(), FusionMachineBlock.Kind.KLYSTRON);
    public static final RegistryObject<Block> FUSION_KLYSTRON_CREATIVE = fusionMachine("fusion_klystron_creative",
            fusionKlystronCreativeDefinition(), FusionMachineBlock.Kind.KLYSTRON_CREATIVE);
    public static final RegistryObject<Block> FUSION_BREEDER = fusionMachine("fusion_breeder",
            fusionBreederDefinition(), FusionMachineBlock.Kind.BREEDER);
    public static final RegistryObject<Block> FUSION_COLLECTOR = fusionMachine("fusion_collector",
            fusionCollectorDefinition(), FusionMachineBlock.Kind.COLLECTOR);
    public static final RegistryObject<Block> FUSION_BOILER = fusionMachine("fusion_boiler",
            fusionBoilerDefinition(), FusionMachineBlock.Kind.BOILER);
    public static final RegistryObject<Block> FUSION_COUPLER = fusionMachine("fusion_coupler",
            fusionCouplerDefinition(), FusionMachineBlock.Kind.COUPLER);
    public static final RegistryObject<Block> FUSION_MHDT = fusionMachine("fusion_mhdt",
            fusionMhdtDefinition(), FusionMachineBlock.Kind.MHDT);
    public static final RegistryObject<Block> FUSION_PLASMA_FORGE = fusionMachine("fusion_plasma_forge",
            fusionPlasmaForgeDefinition(), FusionMachineBlock.Kind.PLASMA_FORGE);
    public static final RegistryObject<Block> ICF_CONTROLLER = icfController("icf_controller");
    public static final RegistryObject<Block> MACHINE_ICF_PRESS = icfPress("machine_icf_press");
    public static final RegistryObject<Block> ICF_LASER_CASING = icfComponentBlock("icf_laser_casing", "icf_casing");
    public static final RegistryObject<Block> ICF_LASER_PORT = icfComponentBlock("icf_laser_port", "icf_port");
    public static final RegistryObject<Block> ICF_LASER_CELL = icfComponentBlock("icf_laser_cell", "icf_cell");
    public static final RegistryObject<Block> ICF_LASER_EMITTER = icfComponentBlock("icf_laser_emitter", "icf_emitter");
    public static final RegistryObject<Block> ICF_LASER_CAPACITOR = icfComponentBlock("icf_laser_capacitor", "icf_capacitor_top");
    public static final RegistryObject<Block> ICF_LASER_TURBO = icfComponentBlock("icf_laser_turbo", "icf_turbocharger");
    public static final RegistryObject<Block> PWR_FUEL = pwrPillar("pwr_fuel", PWRComponentBlock.PillarKind.FUEL);
    public static final RegistryObject<Block> PWR_CONTROL = pwrPillar("pwr_control", PWRComponentBlock.PillarKind.CONTROL);
    public static final RegistryObject<Block> PWR_CHANNEL = pwrPillar("pwr_channel", PWRComponentBlock.PillarKind.CHANNEL);
    public static final RegistryObject<Block> PWR_HEATEX = pwrComponent("pwr_heatex", PWRComponentBlock.Kind.HEATEX);
    public static final RegistryObject<Block> PWR_HEATSINK = pwrComponent("pwr_heatsink", PWRComponentBlock.Kind.HEATSINK);
    public static final RegistryObject<Block> PWR_NEUTRON_SOURCE = pwrComponent("pwr_neutron_source", PWRComponentBlock.Kind.NEUTRON_SOURCE);
    public static final RegistryObject<Block> PWR_REFLECTOR = pwrComponent("pwr_reflector", PWRComponentBlock.Kind.REFLECTOR);
    public static final RegistryObject<Block> PWR_CASING = pwrComponent("pwr_casing", PWRComponentBlock.Kind.CASING);
    public static final RegistryObject<Block> PWR_PORT = pwrComponent("pwr_port", PWRComponentBlock.Kind.PORT);
    public static final RegistryObject<Block> PWR_CONTROLLER = pwrController("pwr_controller");
    public static final RegistryObject<Block> PWR_BLOCK = pwrAssembledBlock("pwr_block");
    public static final RegistryObject<Block> MACHINE_WASTE_DRUM = wasteDrum("machine_waste_drum");
    public static final RegistryObject<Block> MACHINE_STORAGE_DRUM = storageDrum("machine_storage_drum");
    public static final RegistryObject<Block> CARGO_ELEVATOR = cargoElevator("cargo_elevator");
    public static final RegistryObject<Block> MACHINE_BAT9000 = bat9000Machine("machine_bat9000",
            bat9000Definition());
    public static final RegistryObject<Block> MACHINE_BIGASSTANK = bigAssTankMachine("machine_bigasstank",
            bigAssTankDefinition());
    public static final RegistryObject<Block> MACHINE_FLUIDTANK = fluidTankMachine("machine_fluidtank",
            fluidTankDefinition());
    public static final RegistryObject<Block> MACHINE_UF6_TANK =
            hexafluorideTank("machine_uf6_tank", HexafluorideTankBlock.Kind.UF6);
    public static final RegistryObject<Block> MACHINE_PUF6_TANK =
            hexafluorideTank("machine_puf6_tank", HexafluorideTankBlock.Kind.PUF6);
    public static final RegistryObject<Block> MACHINE_WELL = oilDrillMachine("machine_well",
            oilWellDefinition());
    public static final RegistryObject<Block> MACHINE_PUMPJACK = oilDrillMachine("machine_pumpjack",
            pumpjackDefinition());
    public static final RegistryObject<Block> MACHINE_FRACKING_TOWER = oilDrillMachine("machine_fracking_tower",
            frackingTowerDefinition());
    public static final RegistryObject<Block> OIL_PIPE = hiddenBlock("oil_pipe", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5.0F, 10.0F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> MACHINE_CENTRIFUGE = processingMachine("machine_centrifuge",
            centrifugeDefinition(), ProcessingMachineBlockEntity.Kind.CENTRIFUGE);
    public static final RegistryObject<Block> MACHINE_GASCENT = gasCentMachine("machine_gascent",
            gasCentDefinition());
    public static final RegistryObject<Block> MACHINE_ORE_SLOPPER = oreSlopperMachine("machine_ore_slopper",
            oreSlopperDefinition());
    public static final RegistryObject<Block> MACHINE_SAWMILL = sawmillMachine("machine_sawmill",
            sawmillDefinition());
    public static final RegistryObject<Block> MACHINE_CRUCIBLE = crucibleMachine("machine_crucible",
            crucibleDefinition());
    public static final RegistryObject<Block> MACHINE_GASFLARE = gasFlareMachine("machine_flare",
            gasFlareDefinition());
    public static final RegistryObject<Block> CHIMNEY_BRICK = chimneyMachine("chimney_brick",
            chimneyBrickDefinition());
    public static final RegistryObject<Block> CHIMNEY_INDUSTRIAL = chimneyMachine("chimney_industrial",
            chimneyIndustrialDefinition());
    public static final RegistryObject<Block> MACHINE_INTAKE = intakeMachine("machine_intake",
            intakeDefinition());
    public static final RegistryObject<Block> MACHINE_DRAIN = drainMachine("machine_drain",
            drainDefinition());
    public static final RegistryObject<Block> MACHINE_CHUNGUS = chungusMachine("machine_chungus",
            chungusDefinition());
    public static final RegistryObject<Block> MACHINE_HEPHAESTUS = hephaestusMachine("machine_hephaestus",
            hephaestusDefinition());
    public static final RegistryObject<Block> MACHINE_BOILER = heatBoilerMachine("machine_boiler",
            heatBoilerDefinition());
    public static final RegistryObject<Block> MACHINE_INDUSTRIAL_BOILER = heatBoilerMachine("machine_industrial_boiler",
            industrialBoilerDefinition());
    public static final RegistryObject<Block> MACHINE_COMBUSTION_ENGINE = combustionEngineMachine("machine_combustion_engine",
            combustionEngineDefinition());
    public static final RegistryObject<Block> MACHINE_DIESEL = dieselGeneratorMachine("machine_diesel",
            dieselGeneratorDefinition());
    public static final RegistryObject<Block> PUMP_STEAM = waterPumpMachine("pump_steam",
            pumpDefinition("pump", proxyFluid(), "block_copper"));
    public static final RegistryObject<Block> PUMP_ELECTRIC = waterPumpMachine("pump_electric",
            pumpDefinition("pump", "pump_electric", proxyPowerFluid(), "block_steel"));
    public static final RegistryObject<Block> HEATER_HEATEX = heaterHeatexMachine("heater_heatex",
            heaterHeatexDefinition());
    public static final RegistryObject<Block> HEATER_FIREBOX = fireboxHeaterMachine("heater_firebox",
            heaterFireboxDefinition());
    public static final RegistryObject<Block> HEATER_OVEN = fireboxHeaterMachine("heater_oven",
            heaterOvenDefinition());
    public static final RegistryObject<Block> MACHINE_ASHPIT = ashpitMachine("machine_ashpit",
            ashpitDefinition());
    public static final RegistryObject<Block> HEATER_OILBURNER = oilburnerMachine("heater_oilburner",
            heaterOilburnerDefinition());
    public static final RegistryObject<Block> HEATER_ELECTRIC = electricHeaterMachine("heater_electric",
            heaterElectricDefinition());
    public static final RegistryObject<Block> MACHINE_CONDENSER_POWERED = poweredCondenserMachine("machine_condenser_powered",
            condenserPoweredDefinition());
    public static final RegistryObject<Block> MACHINE_ASSEMBLY_FACTORY = assemblyFactoryMachine("machine_assembly_factory",
            assemblyFactoryDefinition());
    public static final RegistryObject<Block> MACHINE_PRECASS = legacyGenericSelectorMachine("machine_precass",
            precassDefinition(), LegacyGenericSelectorMachineBlockEntity.Kind.PRECASS);
    public static final RegistryObject<Block> MACHINE_PUREX = legacyGenericSelectorMachine("machine_purex",
            purexDefinition(), LegacyGenericSelectorMachineBlockEntity.Kind.PUREX);
    public static final RegistryObject<Block> MACHINE_SILEX = silexMachine("machine_silex",
            silexDefinition());
    public static final RegistryObject<Block> MACHINE_EXPOSURE_CHAMBER = exposureChamberMachine("machine_exposure_chamber",
            exposureChamberDefinition());
    public static final RegistryObject<Block> MACHINE_CYCLOTRON = cyclotronMachine("machine_cyclotron",
            cyclotronDefinition());
    public static final RegistryObject<Block> MACHINE_CRYSTALLIZER = processingMachine("machine_crystallizer",
            crystallizerDefinition(), ProcessingMachineBlockEntity.Kind.CRYSTALLIZER);
    public static final RegistryObject<Block> MACHINE_ELECTROLYSER = electrolyserMachine("machine_electrolyser",
            electrolyserDefinition());
    public static final RegistryObject<Block> MACHINE_ARC_WELDER = arcWelderMachine("machine_arc_welder",
            arcWelderDefinition());
    public static final RegistryObject<Block> MACHINE_SOLDERING_STATION = solderingStationMachine("machine_soldering_station",
            solderingStationDefinition());
    public static final RegistryObject<Block> MACHINE_MIXER = mixerMachine("machine_mixer",
            mixerDefinition());
    public static final RegistryObject<Block> MACHINE_RADIOLYSIS = radiolysisMachine("machine_radiolysis",
            radiolysisDefinition());
    public static final RegistryObject<Block> MACHINE_RTG_GREY = rtgMachine("machine_rtg_grey",
            rtgDefinition());
    public static final RegistryObject<Block> MACHINE_MINIRTG =
            miniRtgMachine("machine_minirtg", MiniRtgBlock.Kind.CELL);
    public static final RegistryObject<Block> MACHINE_POWERRTG =
            miniRtgMachine("machine_powerrtg", MiniRtgBlock.Kind.POLONIUM);
    public static final RegistryObject<Block> MACHINE_RADGEN = radGenMachine("machine_radgen",
            radGenDefinition());
    public static final RegistryObject<Block> MACHINE_ROTARY_FURNACE = rotaryFurnaceMachine("machine_rotary_furnace",
            rotaryFurnaceDefinition());
    public static final RegistryObject<Block> MACHINE_STEAM_ENGINE = steamEngineMachine("machine_steam_engine",
            steamEngineDefinition());
    public static final RegistryObject<Block> MACHINE_SOLAR_BOILER = solarBoilerMachine("machine_solar_boiler",
            solarBoilerDefinition());
    public static final RegistryObject<Block> SOLAR_MIRROR = solarMirror("solar_mirror");
    public static final RegistryObject<Block> MACHINE_TOWER_SMALL = coolingTowerMachine("machine_tower_small",
            towerSmallDefinition(), CoolingTowerBlock.Kind.SMALL);
    public static final RegistryObject<Block> MACHINE_TOWER_LARGE = coolingTowerMachine("machine_tower_large",
            towerLargeDefinition(), CoolingTowerBlock.Kind.LARGE);
    public static final RegistryObject<Block> MACHINE_TURBOFAN = turbofanMachine("machine_turbofan",
            turbofanDefinition());
    public static final RegistryObject<Block> MACHINE_TURBINEGAS = turbineGasMachine("machine_turbinegas",
            turbineGasDefinition());
    public static final RegistryObject<Block> MACHINE_AMMO_PRESS = ammoPressMachine("machine_ammo_press",
            ammoPressDefinition());
    public static final RegistryObject<Block> MACHINE_TRANSFORMER = registerBlockWithItem("machine_transformer",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> CAPACITOR_BUS = capacitorBus("capacitor_bus");
    public static final RegistryObject<Block> MACHINE_FURNACE_BRICK =
            brickFurnaceMachine("machine_furnace_brick_off");
    public static final RegistryObject<Block> MACHINE_RTG_FURNACE =
            rtgFurnaceMachine("machine_rtg_furnace_off");
    public static final RegistryObject<Block> MACHINE_DIFURNACE_RTG =
            diFurnaceRtgMachine("machine_difurnace_rtg_off");
    public static final RegistryObject<Block> FURNACE_IRON = legacyFurnaceMachine("furnace_iron",
            furnaceIronDefinition(), LegacyFurnaceBlockEntity.Kind.IRON);
    public static final RegistryObject<Block> FURNACE_STEEL = legacyFurnaceMachine("furnace_steel",
            furnaceSteelDefinition(), LegacyFurnaceBlockEntity.Kind.STEEL);
    public static final RegistryObject<Block> FURNACE_COMBINATION = combinationOvenMachine("furnace_combination",
            furnaceCombinationDefinition());
    public static final RegistryObject<Block> MACHINE_BLAST_FURNACE = blastFurnaceMachine("machine_blast_furnace",
            blastFurnaceDefinition());
    public static final RegistryObject<Block> MACHINE_ARC_FURNACE = arcFurnaceMachine("machine_arc_furnace",
            arcFurnaceDefinition());
    public static final RegistryObject<Block> MACHINE_ANNIHILATOR = annihilatorMachine("machine_annihilator",
            annihilatorDefinition());
    public static final RegistryObject<Block> MACHINE_FORCEFIELD = forceFieldMachine("machine_forcefield");
    public static final RegistryObject<Block> MACHINE_FEL = felMachine("machine_fel",
            felDefinition());
    public static final RegistryObject<Block> MACHINE_ORBUS = orbusMachine("machine_orbus",
            orbusDefinition());
    public static final RegistryObject<Block> MACHINE_MINING_LASER = miningLaserMachine("machine_mining_laser",
            miningLaserDefinition());
    public static final RegistryObject<Block> MACHINE_EXCAVATOR = excavatorMachine("machine_excavator",
            excavatorDefinition());
    public static final RegistryObject<Block> MACHINE_STRAND_CASTER = strandCasterMachine("machine_strand_caster",
            strandCasterDefinition());
    public static final RegistryObject<Block> FOUNDRY_MOLD = registerBlockWithItem("foundry_mold",
            () -> new FoundryCastingBlock(foundryProperties(), 0));
    public static final RegistryObject<Block> FOUNDRY_BASIN = registerBlockWithItem("foundry_basin",
            () -> new FoundryCastingBlock(foundryProperties(), 1));
    public static final RegistryObject<Block> FOUNDRY_CHANNEL = registerBlockWithItem("foundry_channel",
            () -> new FoundryChannelBlock(foundryProperties()));
    public static final RegistryObject<Block> FOUNDRY_TANK = registerBlockWithItem("foundry_tank",
            () -> new FoundryTankBlock(foundryProperties()));
    public static final RegistryObject<Block> FOUNDRY_OUTLET = registerBlockWithItem("foundry_outlet",
            () -> new FoundryOutletBlock(foundryProperties(), false));
    public static final RegistryObject<Block> FOUNDRY_SLAGTAP = registerBlockWithItem("foundry_slagtap",
            () -> new FoundryOutletBlock(foundryProperties(), true));
    public static final RegistryObject<Block> FOUNDRY_SLAG = hiddenBlock("slag",
            () -> new FoundrySlagBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> MACHINE_WOOD_BURNER = woodBurnerMachine("machine_wood_burner",
            woodBurnerDefinition());
    public static final RegistryObject<Block> MACHINE_STIRLING = stirlingMachine("machine_stirling",
            stirlingDefinition("stirling"), StirlingBlock.Kind.NORMAL);
    public static final RegistryObject<Block> MACHINE_STIRLING_STEEL = stirlingMachine("machine_stirling_steel",
            stirlingDefinition("stirling_steel"), StirlingBlock.Kind.STEEL);
    public static final RegistryObject<Block> MACHINE_STIRLING_CREATIVE = stirlingMachine("machine_stirling_creative",
            stirlingDefinition("stirling_creative"), StirlingBlock.Kind.CREATIVE);
    public static final RegistryObject<Block> MACHINE_DEUTERIUM_TOWER = deuteriumTowerMachine("machine_deuterium_tower",
            deuteriumTowerDefinition());
    public static final RegistryObject<Block> MACHINE_DEUTERIUM_EXTRACTOR =
            deuteriumExtractor("machine_deuterium_extractor");
    public static final RegistryObject<Block> FRACTION_SPACER = fractionSpacerMachine("fraction_spacer",
            fractionSpacerDefinition());
    public static final RegistryObject<Block> TELEANCHOR = teleanchor("teleanchor");
    public static final RegistryObject<Block> FIELD_DISTURBER = fieldDisturber("field_disturber");
    public static final RegistryObject<Block> GLASS_BORON = legacyGlass("glass_boron");
    public static final RegistryObject<Block> GLASS_LEAD = legacyGlass("glass_lead");
    public static final RegistryObject<Block> GLASS_URANIUM = luminousLegacyGlass("glass_uranium");
    public static final RegistryObject<Block> GLASS_POLONIUM = luminousLegacyGlass("glass_polonium");
    public static final RegistryObject<Block> GLASS_QUARTZ = quartzGlass("glass_quartz");
    public static final RegistryObject<Block> SAND_BORON = legacySandMix("sand_boron");
    public static final RegistryObject<Block> SAND_LEAD = legacySandMix("sand_lead");
    public static final RegistryObject<Block> SAND_URANIUM = legacySandMix("sand_uranium");
    public static final RegistryObject<Block> SAND_POLONIUM = legacySandMix("sand_polonium");
    public static final RegistryObject<Block> SAND_QUARTZ = legacySandMix("sand_quartz");
    public static final RegistryObject<Block> MOON_TURF = moonTurf("moon_turf");
    public static final RegistryObject<Block> REINFORCED_LAMINATE = reinforcedLaminate("reinforced_laminate");
    public static final RegistryObject<Block> REINFORCED_LAMINATE_PANE =
            reinforcedLaminatePane("reinforced_laminate_pane");
    public static final RegistryObject<Block> BLOCK_CAP_NUKA = capBlock("block_cap_nuka");
    public static final RegistryObject<Block> BLOCK_CAP_QUANTUM = capBlock("block_cap_quantum");
    public static final RegistryObject<Block> BLOCK_CAP_SPARKLE = capBlock("block_cap_sparkle");
    public static final RegistryObject<Block> BLOCK_CAP_RAD = capBlock("block_cap_rad");
    public static final RegistryObject<Block> BLOCK_CAP_KORL = capBlock("block_cap_korl");
    public static final RegistryObject<Block> BLOCK_CAP_FRITZ = capBlock("block_cap_fritz");

    // Legacy 1.7.10 blockTab entries used as an early chunk-radiation test bed.
    public static final RegistryObject<Block> WASTE_EARTH = wasteEarth("waste_earth", false);
    public static final RegistryObject<Block> WASTE_MYCELIUM = wasteEarth("waste_mycelium", true);
    public static final RegistryObject<Block> MUSH = glowingMush("mush");
    public static final RegistryObject<Block> MUSH_BLOCK = hugeMush("mush_block");
    public static final RegistryObject<Block> MUSH_BLOCK_STEM = hugeMush("mush_block_stem");
    public static final RegistryObject<Block> PLANT_FLOWER_FOXGLOVE =
            ntmFlower("plant_flower_foxglove", LegacyNtmFlowerBlock.Kind.FOXGLOVE);
    public static final RegistryObject<Block> PLANT_FLOWER_TOBACCO =
            ntmFlower("plant_flower_tobacco", LegacyNtmFlowerBlock.Kind.TOBACCO);
    public static final RegistryObject<Block> PLANT_FLOWER_NIGHTSHADE =
            ntmFlower("plant_flower_nightshade", LegacyNtmFlowerBlock.Kind.NIGHTSHADE);
    public static final RegistryObject<Block> PLANT_FLOWER_WEED =
            ntmFlower("plant_flower_weed", LegacyNtmFlowerBlock.Kind.WEED);
    public static final RegistryObject<Block> PLANT_FLOWER_CD0 =
            ntmFlower("plant_flower_cd0", LegacyNtmFlowerBlock.Kind.CD0);
    public static final RegistryObject<Block> PLANT_FLOWER_CD1 =
            ntmFlower("plant_flower_cd1", LegacyNtmFlowerBlock.Kind.CD1);
    public static final List<RegistryObject<Block>> PLANT_FLOWER_BLOCKS = List.of(
            PLANT_FLOWER_FOXGLOVE,
            PLANT_FLOWER_TOBACCO,
            PLANT_FLOWER_NIGHTSHADE,
            PLANT_FLOWER_WEED,
            PLANT_FLOWER_CD0,
            PLANT_FLOWER_CD1);
    public static final RegistryObject<Block> WASTE_LEAVES = registerBlockWithItem("waste_leaves", () -> new LegacyWasteLeavesBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .strength(0.1F)
            .sound(SoundType.GRASS)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)));
    public static final RegistryObject<Block> WASTE_LOG = registerBlockWithItem("waste_log", () -> new LegacyWasteLogBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(5.0F, 2.5F)
            .sound(SoundType.WOOD)));
    public static final RegistryObject<Block> WASTE_PLANKS = simpleBlock("waste_planks", "waste_planks");
    public static final RegistryObject<Block> FROZEN_GRASS = frozenEarth("frozen_grass");
    public static final RegistryObject<Block> FROZEN_DIRT = frozenEarth("frozen_dirt");
    public static final RegistryObject<Block> FROZEN_LOG = frozenLog("frozen_log");
    public static final RegistryObject<Block> FROZEN_PLANKS = frozenBlock("frozen_planks");
    public static final RegistryObject<Block> LEAVES_LAYER = registerBlockWithItem("leaves_layer", () -> new LegacyLeavesLayerBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .strength(0.1F)
            .sound(SoundType.GRASS)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)));
    public static final RegistryObject<Block> FALLOUT = falloutLayer("fallout");
    public static final RegistryObject<Block> BARRICADE = barricade("barricade");
    public static final RegistryObject<Block> OIL_SPILL = oilSpill("oil_spill");
    public static final RegistryObject<Block> SELLAFIELD = sellafield("sellafield");
    public static final RegistryObject<Block> SELLAFIELD_SLAKED = sellafieldSlaked("sellafield_slaked");
    public static final RegistryObject<Block> SELLAFIELD_BEDROCK = sellafieldBedrock("sellafield_bedrock");
    public static final RegistryObject<Block> ORE_BEDROCK = bedrockOreDeposit("ore_bedrock");
    public static final RegistryObject<Block> ORE_BEDROCK_COLTAN = bedrockOreDeposit("ore_bedrock_coltan");
    public static final RegistryObject<Block> ORE_SELLAFIELD_DIAMOND = sellafieldOre("ore_sellafield_diamond", LegacySellafieldOreBlock.Kind.DIAMOND);
    public static final RegistryObject<Block> ORE_SELLAFIELD_EMERALD = sellafieldOre("ore_sellafield_emerald", LegacySellafieldOreBlock.Kind.EMERALD);
    public static final RegistryObject<Block> ORE_SELLAFIELD_URANIUM_SCORCHED = sellafieldOre("ore_sellafield_uranium_scorched", LegacySellafieldOreBlock.Kind.URANIUM_SCORCHED);
    public static final RegistryObject<Block> ORE_SELLAFIELD_SCHRABIDIUM = sellafieldOre("ore_sellafield_schrabidium", LegacySellafieldOreBlock.Kind.SCHRABIDIUM);
    public static final RegistryObject<Block> ORE_SELLAFIELD_RADGEM = sellafieldOre("ore_sellafield_radgem", LegacySellafieldOreBlock.Kind.RADGEM);
    public static final RegistryObject<Block> WASTE_TRINITITE = trinititeOre("waste_trinitite");
    public static final RegistryObject<Block> WASTE_TRINITITE_RED = trinititeOre("waste_trinitite_red");
    public static final RegistryObject<Block> GLASS_TRINITITE = trinititeGlass("glass_trinitite");
    public static final RegistryObject<Block> ASH_DIGAMMA = ashDigamma("ash_digamma");
    public static final RegistryObject<Block> FIRE_DIGAMMA = fireDigamma("fire_digamma");
    public static final RegistryObject<Block> BALEFIRE = balefire("balefire");
    public static final RegistryObject<Block> CORIUM_BLOCK = coriumBlock("corium_block");
    public static final RegistryObject<Block> PRIBRIS = pribrisDebris(
            "pribris", MapColor.COLOR_GRAY, RBMKDebrisBlock.Kind.INERT);
    public static final RegistryObject<Block> PRIBRIS_BURNING = pribrisDebris(
            "pribris_burning", MapColor.COLOR_ORANGE, RBMKDebrisBlock.Kind.BURNING);
    public static final RegistryObject<Block> PRIBRIS_RADIATING = pribrisDebris(
            "pribris_radiating", MapColor.COLOR_GREEN, RBMKDebrisBlock.Kind.RADIATING);
    public static final RegistryObject<Block> PRIBRIS_DIGAMMA = pribrisDebris(
            "pribris_digamma", MapColor.COLOR_BLACK, RBMKDebrisBlock.Kind.DIGAMMA);
    public static final RegistryObject<Block> ORE_BASALT = basaltOre("ore_basalt");
    public static final RegistryObject<Block> VOLCANIC_LAVA_BLOCK = volcanicLavaBlock("volcanic_lava_block", false);
    public static final RegistryObject<Block> RAD_LAVA_BLOCK = volcanicLavaBlock("rad_lava_block", true);
    static {
        ModFluids.volcanicLavaProperties().block(() -> (LiquidBlock) VOLCANIC_LAVA_BLOCK.get());
        ModFluids.radLavaProperties().block(() -> (LiquidBlock) RAD_LAVA_BLOCK.get());
    }
    public static final RegistryObject<Block> VOLCANO_CORE = volcanoCore("volcano_core");
    public static final RegistryObject<Block> VOLCANO_RAD_CORE = volcanoCore("volcano_rad_core");
    public static final RegistryObject<Block> MUD_BLOCK = mudLiquidBlock("mud_block");
    static {
        ModFluids.mudProperties().block(() -> (LiquidBlock) MUD_BLOCK.get());
    }
    public static final RegistryObject<Block> TAINT = taint("taint");
    public static final RegistryObject<Block> TEKTITE = tektite("tektite");
    public static final RegistryObject<Block> ORE_TEKTITE_OSMIRIDIUM = tektite("ore_tektite_osmiridium");
    public static final RegistryObject<Block> BLOCK_SEMTEX = plasticExplosive("block_semtex");
    public static final RegistryObject<Block> BLOCK_C4 = plasticExplosive("block_c4");

    // Legacy 1.7.10 nuclear device IDs. Inventory readiness is wired in the later block entity pass.
    public static final RegistryObject<Block> NUKE_GADGET = nuclearDevice("nuke_gadget", NuclearDeviceBlock.Kind.GADGET);
    public static final RegistryObject<Block> NUKE_BOY = nuclearDevice("nuke_boy", NuclearDeviceBlock.Kind.BOY);
    public static final RegistryObject<Block> NUKE_MAN = nuclearDevice("nuke_man", NuclearDeviceBlock.Kind.MAN);
    public static final RegistryObject<Block> NUKE_TSAR = nuclearDevice("nuke_tsar", NuclearDeviceBlock.Kind.TSAR);
    public static final RegistryObject<Block> NUKE_MIKE = nuclearDevice("nuke_mike", NuclearDeviceBlock.Kind.MIKE);
    public static final RegistryObject<Block> NUKE_PROTOTYPE = nuclearDevice("nuke_prototype", NuclearDeviceBlock.Kind.PROTOTYPE);
    public static final RegistryObject<Block> NUKE_FLEIJA = nuclearDevice("nuke_fleija", NuclearDeviceBlock.Kind.FLEIJA);
    public static final RegistryObject<Block> NUKE_SOLINIUM = nuclearDevice("nuke_solinium", NuclearDeviceBlock.Kind.SOLINIUM);
    public static final RegistryObject<Block> NUKE_N2 = nuclearDevice("nuke_n2", NuclearDeviceBlock.Kind.N2);
    public static final RegistryObject<Block> NUKE_CUSTOM = customNuke("nuke_custom");
    public static final RegistryObject<Block> NUKE_FSTBMB = balefireBomb("nuke_fstbmb");
    public static final RegistryObject<Block> BOMB_MULTI = bombMulti("bomb_multi");
    public static final RegistryObject<Block> DYNAMITE = legacyTntBase("dynamite", LegacyTntBaseBlock.Kind.DYNAMITE);
    public static final RegistryObject<Block> TNT_NTM = legacyTntBase("tnt_ntm", LegacyTntBaseBlock.Kind.TNT);
    public static final RegistryObject<Block> SEMTEX = legacyTntBase("semtex", LegacyTntBaseBlock.Kind.SEMTEX);
    public static final RegistryObject<Block> C4 = legacyTntBase("c4", LegacyTntBaseBlock.Kind.C4);
    public static final RegistryObject<Block> DET_CORD = detCord("det_cord");
    public static final RegistryObject<Block> DET_CHARGE = explosiveCharge("det_charge", LegacyExplosiveChargeBlock.Kind.CONVENTIONAL);
    public static final RegistryObject<Block> DET_NUKE = explosiveCharge("det_nuke", LegacyExplosiveChargeBlock.Kind.NUCLEAR);
    public static final RegistryObject<Block> CHARGE_DYNAMITE = legacyCharge("charge_dynamite", LegacyChargeBlock.Kind.DYNAMITE);
    public static final RegistryObject<Block> CHARGE_MINER = legacyCharge("charge_miner", LegacyChargeBlock.Kind.MINER);
    public static final RegistryObject<Block> CHARGE_C4 = legacyCharge("charge_c4", LegacyChargeBlock.Kind.C4);
    public static final RegistryObject<Block> CHARGE_SEMTEX = legacyCharge("charge_semtex", LegacyChargeBlock.Kind.SEMTEX);
    public static final RegistryObject<Block> RED_BARREL = explosiveBarrel("red_barrel", LegacyExplosiveBarrelBlock.Kind.RED);
    public static final RegistryObject<Block> PINK_BARREL = explosiveBarrel("pink_barrel", LegacyExplosiveBarrelBlock.Kind.PINK);
    public static final RegistryObject<Block> LOX_BARREL = explosiveBarrel("lox_barrel", LegacyExplosiveBarrelBlock.Kind.LOX);
    public static final RegistryObject<Block> TAINT_BARREL = explosiveBarrel("taint_barrel", LegacyExplosiveBarrelBlock.Kind.TAINT);
    public static final RegistryObject<Block> YELLOW_BARREL = radiationBarrel("yellow_barrel", 5.0F);
    public static final RegistryObject<Block> VITRIFIED_BARREL = radiationBarrel("vitrified_barrel", 0.5F);

    public static final List<RegistryObject<Block>> CONVEYOR_BLOCKS = List.of(
            CONVEYOR,
            CONVEYOR_EXPRESS,
            CONVEYOR_DOUBLE,
            CONVEYOR_TRIPLE,
            CONVEYOR_LIFT,
            CONVEYOR_CHUTE
    );

    public static final List<RegistryObject<Block>> PYLON_BLOCKS = List.of(
            RED_CONNECTOR,
            RED_CONNECTOR_SUPER,
            RED_PYLON,
            RED_PYLON_MEDIUM_WOOD,
            RED_PYLON_MEDIUM_WOOD_TRANSFORMER,
            RED_PYLON_MEDIUM_STEEL,
            RED_PYLON_MEDIUM_STEEL_TRANSFORMER,
            RED_PYLON_LARGE,
            SUBSTATION
    );

    public static final List<RegistryObject<Block>> MACHINE_TAB_BLOCKS = List.of(
            MACHINE_PRESS,
            PRESS_PREHEATER,
            MACHINE_EPRESS,
            MACHINE_DIFURNACE_OFF,
            MACHINE_ELECTRIC_FURNACE_OFF,
            MACHINE_DIFURNACE_EXTENSION,
            MACHINE_BOILER_OFF,
            MACHINE_SHREDDER,
            MACHINE_AUTOCRAFTER,
            MACHINE_DETECTOR,
            CHARGER,
            REFUELER,
            RADIOBOX,
            RADIOREC,
            TESLA,
            MACHINE_MICROWAVE,
            MACHINE_FUNNEL,
            MACHINE_KEYFORGE,
            MACHINE_AUTOSAW,
            MACHINE_THRESHER,
            MACHINE_TURBINE,
            MACHINE_INDUSTRIAL_TURBINE,
            MACHINE_LARGE_TURBINE,
            DECON,
            MACHINE_ARMOR_TABLE,
            MACHINE_WEAPON_TABLE,
            MACHINE_SIREN,
            FAN,
            FILING_CABINET,
            ANVIL_IRON,
            ANVIL_LEAD,
            ANVIL_STEEL,
            ANVIL_DESH,
            ANVIL_FERROURANIUM,
            ANVIL_SATURNITE,
            ANVIL_BISMUTH_BRONZE,
            ANVIL_ARSENIC_BRONZE,
            ANVIL_SCHRABIDATE,
            ANVIL_DNT,
            ANVIL_OSMIRIDIUM,
            ANVIL_MURKY,
            RED_CABLE,
            RED_CABLE_CLASSIC,
            RED_WIRE_COATED,
            RED_CABLE_BOX,
            RED_CABLE_GAUGE,
            CABLE_SWITCH,
            CABLE_DETECTOR,
            CABLE_DIODE,
            RADIO_TORCH_SENDER,
            RADIO_TORCH_RECEIVER,
            RADIO_TORCH_COUNTER,
            RADIO_TORCH_LOGIC,
            RADIO_TORCH_READER,
            RADIO_TORCH_CONTROLLER,
            RADIO_AUTOCAL,
            RADIO_TELEX,
            RBMK_DISPLAY_BLANK,
            RBMK_DISPLAY,
            RBMK_GAUGE,
            RBMK_GRAPH,
            RBMK_INDICATOR,
            RBMK_KEY_PAD,
            RBMK_LEVER,
            RBMK_NUMITRON,
            RBMK_TERMINAL,
            RBMK_BLANK,
            RBMK_MODERATOR,
            RBMK_REFLECTOR,
            RBMK_ABSORBER,
            RBMK_ROD,
            RBMK_ROD_MOD,
            RBMK_ROD_REASIM,
            RBMK_ROD_REASIM_MOD,
            RBMK_BOILER,
            RBMK_HEATER,
            RBMK_COOLER,
            RBMK_OUTGASSER,
            RBMK_STORAGE,
            RBMK_CONTROL,
            RBMK_CONTROL_MOD,
            RBMK_CONTROL_AUTO,
            RBMK_CONTROL_REASIM,
            RBMK_CONTROL_REASIM_AUTO,
            RBMK_AUTOLOADER,
            RBMK_CONSOLE,
            RBMK_CRANE_CONSOLE,
            RBMK_LOADER,
            RBMK_STEAM_INLET,
            RBMK_STEAM_OUTLET,
            FLUID_DUCT_NEO,
            FLUID_VALVE,
            FLUID_SWITCH,
            FLUID_COUNTER_VALVE,
            FLUID_PUMP,
            FLUID_DUCT_BOX,
            FLUID_DUCT_GAUGE,
            FLUID_DUCT_EXHAUST,
            FLUID_DUCT_PAINTABLE,
            FLUID_DUCT_PAINTABLE_BLOCK_EXHAUST,
            PIPE_ANCHOR,
            BARREL_PLASTIC,
            BARREL_STEEL,
            BARREL_TCALLOY,
            BARREL_ANTIMATTER,
            PNEUMATIC_TUBE,
            CRANE_SPLITTER,
            MACHINE_BATTERY_REDD,
            MACHINE_BATTERY_SOCKET,
            HEV_BATTERY,
            CRATE_IRON,
            CRATE_STEEL,
            CRATE_DESH,
            CRATE_TUNGSTEN,
            SAFE,
            MASS_STORAGE,
            MACHINE_RADAR,
            MACHINE_RADAR_LARGE,
            MACHINE_RADAR_SCREEN,
            VENDING_MACHINE,
            MACHINE_ASSEMBLY_MACHINE,
            MACHINE_TELEPORTER,
            MACHINE_CHEMICAL_PLANT,
            MACHINE_LIQUEFACTOR,
            MACHINE_CHEMICAL_FACTORY,
            MACHINE_REFINERY,
            MACHINE_CATALYTIC_CRACKER,
            MACHINE_CATALYTIC_REFORMER,
            MACHINE_VACUUM_DISTILL,
            MACHINE_FRACTION_TOWER,
            MACHINE_HYDROTREATER,
            MACHINE_COKER,
            MACHINE_PYROOVEN,
            MACHINE_SOLIDIFIER,
            MACHINE_COMPRESSOR,
            MACHINE_COMPRESSOR_COMPACT,
            MACHINE_LPW2,
            MACHINE_CONTROLLER,
            REACTOR_RESEARCH,
            MACHINE_REACTOR_BREEDING,
            REACTOR_ZIRNOX,
            WATZ,
            WATZ_PUMP,
            STRUCT_WATZ_CORE,
            WATZ_ELEMENT,
            WATZ_COOLER,
            WATZ_END,
            DFC_CORE,
            DFC_EMITTER,
            DFC_RECEIVER,
            DFC_INJECTOR,
            DFC_STABILIZER,
            STRUCT_TORUS_CORE,
            FUSION_COMPONENT_BSCCO,
            FUSION_COMPONENT_BSCCO_WELDED,
            FUSION_COMPONENT_BLANKET,
            FUSION_COMPONENT_MOTOR,
            STRUCT_ICF_CORE,
            ICF_COMPONENT_SCAFFOLD,
            ICF_COMPONENT_VESSEL,
            ICF_COMPONENT_VESSEL_WELDED,
            ICF_COMPONENT_STRUCTURE,
            ICF_COMPONENT_STRUCTURE_BOLTED,
            ICF,
            ICF_BLOCK,
            FUSION_TORUS,
            FUSION_KLYSTRON,
            FUSION_KLYSTRON_CREATIVE,
            FUSION_BREEDER,
            FUSION_COLLECTOR,
            FUSION_BOILER,
            FUSION_COUPLER,
            FUSION_MHDT,
            FUSION_PLASMA_FORGE,
            ICF_CONTROLLER,
            MACHINE_ICF_PRESS,
            ICF_LASER_CASING,
            ICF_LASER_PORT,
            ICF_LASER_CELL,
            ICF_LASER_EMITTER,
            ICF_LASER_CAPACITOR,
            ICF_LASER_TURBO,
            PWR_FUEL,
            PWR_CONTROL,
            PWR_CHANNEL,
            PWR_HEATEX,
            PWR_HEATSINK,
            PWR_NEUTRON_SOURCE,
            PWR_REFLECTOR,
            PWR_CASING,
            PWR_PORT,
            PWR_CONTROLLER,
            MACHINE_WASTE_DRUM,
            MACHINE_STORAGE_DRUM,
            CARGO_ELEVATOR,
            MACHINE_BAT9000,
            MACHINE_BIGASSTANK,
            MACHINE_FLUIDTANK,
            MACHINE_WELL,
            MACHINE_PUMPJACK,
            MACHINE_FRACKING_TOWER,
            MACHINE_CENTRIFUGE,
            MACHINE_GASCENT,
            MACHINE_ORE_SLOPPER,
            MACHINE_SAWMILL,
            MACHINE_CRUCIBLE,
            MACHINE_GASFLARE,
            CHIMNEY_BRICK,
            CHIMNEY_INDUSTRIAL,
            MACHINE_INTAKE,
            MACHINE_DRAIN,
            MACHINE_CHUNGUS,
            MACHINE_HEPHAESTUS,
            MACHINE_BOILER,
            MACHINE_INDUSTRIAL_BOILER,
            MACHINE_COMBUSTION_ENGINE,
            MACHINE_DIESEL,
            PUMP_STEAM,
            PUMP_ELECTRIC,
            HEATER_HEATEX,
            HEATER_FIREBOX,
            HEATER_OVEN,
            MACHINE_ASHPIT,
            HEATER_OILBURNER,
            HEATER_ELECTRIC,
            MACHINE_CONDENSER,
            MACHINE_CONDENSER_POWERED,
            MACHINE_ASSEMBLY_FACTORY,
            MACHINE_PRECASS,
            MACHINE_PUREX,
            MACHINE_SILEX,
            MACHINE_EXPOSURE_CHAMBER,
            MACHINE_CYCLOTRON,
            PA_SOURCE,
            PA_BEAMLINE,
            PA_RFC,
            PA_QUADRUPOLE,
            PA_DIPOLE,
            PA_DETECTOR,
            MACHINE_CRYSTALLIZER,
            MACHINE_ELECTROLYSER,
            MACHINE_ARC_WELDER,
            MACHINE_SOLDERING_STATION,
            MACHINE_MIXER,
            MACHINE_RADIOLYSIS,
            MACHINE_RTG_GREY,
            MACHINE_RADGEN,
            MACHINE_ROTARY_FURNACE,
            MACHINE_STEAM_ENGINE,
            MACHINE_SOLAR_BOILER,
            SOLAR_MIRROR,
            MACHINE_TOWER_SMALL,
            MACHINE_TOWER_LARGE,
            MACHINE_TURBOFAN,
            MACHINE_TURBINEGAS,
            MACHINE_AMMO_PRESS,
            MACHINE_TRANSFORMER,
            MACHINE_FURNACE_BRICK,
            MACHINE_DIFURNACE_RTG,
            FURNACE_IRON,
            FURNACE_STEEL,
            FURNACE_COMBINATION,
            MACHINE_BLAST_FURNACE,
            MACHINE_ARC_FURNACE,
            MACHINE_ANNIHILATOR,
            MACHINE_FORCEFIELD,
            MACHINE_FEL,
            MACHINE_ORBUS,
            MACHINE_MINING_LASER,
            MACHINE_EXCAVATOR,
            MACHINE_STRAND_CASTER,
            MACHINE_WOOD_BURNER,
            MACHINE_STIRLING,
            MACHINE_STIRLING_STEEL,
            MACHINE_STIRLING_CREATIVE,
            MACHINE_DEUTERIUM_TOWER,
            MACHINE_DEUTERIUM_EXTRACTOR,
            FRACTION_SPACER,
            TELEANCHOR,
            GAS_RADON,
            GAS_RADON_DENSE,
            GAS_RADON_TOMB,
            GAS_MELTDOWN,
            GAS_MONOXIDE,
            GAS_ASBESTOS,
            GAS_COAL,
            CHLORINE_GAS,
            RAD_ABSORBER,
            GEIGER
    );

    public static final List<RegistryObject<Block>> TURRET_TAB_BLOCKS = List.of(
            TURRET_CHEKHOV,
            TURRET_FRIENDLY,
            TURRET_JEREMY,
            TURRET_RICHARD,
            TURRET_TAUON,
            TURRET_HOWARD,
            TURRET_SENTRY,
            TURRET_HOWARD_DAMAGED,
            TURRET_SENTRY_DAMAGED,
            TURRET_MAXWELL,
            TURRET_ARTY,
            TURRET_HIMARS,
            TURRET_FRITZ
    );

    public static final List<RegistryObject<Block>> MACHINE_TAB_EXTRA_BLOCKS = List.of(
            GLASS_BORON,
            GLASS_LEAD,
            GLASS_URANIUM,
            GLASS_POLONIUM,
            SAND_BORON,
            SAND_LEAD,
            SAND_URANIUM,
            SAND_POLONIUM,
            SAND_QUARTZ,
            RED_CONNECTOR,
            RED_CONNECTOR_SUPER,
            RED_PYLON,
            RED_PYLON_MEDIUM_WOOD,
            RED_PYLON_MEDIUM_WOOD_TRANSFORMER,
            RED_PYLON_MEDIUM_STEEL,
            RED_PYLON_MEDIUM_STEEL_TRANSFORMER,
            RED_PYLON_LARGE,
            SUBSTATION
    );

    public static final List<RegistryObject<Block>> SATELLITE_TAB_BLOCKS = List.of(
            LAUNCH_PAD,
            LAUNCH_PAD_LARGE,
            LAUNCH_PAD_RUSTED,
            MACHINE_SATLINKER,
            SAT_DOCK,
            SOYUZ_CAPSULE,
            SOYUZ_LAUNCHER,
            STRUCT_LAUNCHER,
            STRUCT_SCAFFOLD,
            STRUCT_SOYUZ_CORE,
            LAUNCH_TABLE,
            COMPACT_LAUNCHER,
            MACHINE_MISSILE_ASSEMBLY
    );

    public static final List<RegistryObject<Block>> HIDDEN_MACHINE_BLOCKS = List.of(
            BARREL_CORRODED,
            MACHINE_UF6_TANK,
            MACHINE_PUF6_TANK,
            MACHINE_FENSU,
            MACHINE_MINIRTG,
            MACHINE_POWERRTG,
            FIELD_DISTURBER,
            CAPACITOR_BUS,
            MACHINE_RTG_FURNACE,
            MACHINE_BATTERY,
            MACHINE_BATTERY_POTATO,
            MACHINE_LITHIUM_BATTERY,
            MACHINE_SCHRABIDIUM_BATTERY,
            MACHINE_DINEUTRONIUM_BATTERY
    );

    public static final List<RegistryObject<Block>> CAP_BLOCKS = List.of(
            BLOCK_CAP_NUKA,
            BLOCK_CAP_QUANTUM,
            BLOCK_CAP_SPARKLE,
            BLOCK_CAP_RAD,
            BLOCK_CAP_KORL,
            BLOCK_CAP_FRITZ
    );

    public static final RegistryObject<Block> BLOCK_FOAM = simpleBlock("block_foam", "foam");
    public static final RegistryObject<Block> BLOCK_SLAG_BROKEN =
            hiddenBlock("block_slag_broken", () -> new Block(simpleResourceProperties("block_slag_broken",
                    "block_slag_broken")));

    public static final RegistryObject<Block> STALACTITE_SULFUR =
            caveSpike("stalactite_sulfur", "stalactite.sulfur", LegacyCaveSpikeBlock.Kind.STALACTITE);
    public static final RegistryObject<Block> STALACTITE_ASBESTOS =
            caveSpike("stalactite_asbestos", "stalactite.asbestos", LegacyCaveSpikeBlock.Kind.STALACTITE);
    public static final RegistryObject<Block> STALAGMITE_SULFUR =
            caveSpike("stalagmite_sulfur", "stalagmite.sulfur", LegacyCaveSpikeBlock.Kind.STALAGMITE);
    public static final RegistryObject<Block> STALAGMITE_ASBESTOS =
            caveSpike("stalagmite_asbestos", "stalagmite.asbestos", LegacyCaveSpikeBlock.Kind.STALAGMITE);

    public static final List<RegistryObject<Block>> EXTRA_BLOCK_TAB_BLOCKS = simpleResourceBlocks(
            "ore_uranium:ore_uranium",
            "ore_uranium_scorched:ore_uranium_scorched",
            "deepslate_ore_uranium:ore_uranium_deepslate",
            "ore_titanium:ore_titanium",
            "deepslate_ore_titanium:titanium_ore_deepslate",
            "ore_sulfur:ore_sulfur",
            "deepslate_ore_sulfur:deepslate_ore_sulfur",
            "ore_thorium:ore_thorium",
            "deepslate_ore_thorium:thorium_ore_deepslate",
            "ore_niter:ore_niter",
            "deepslate_ore_niter:deepslate_ore_niter",
            "ore_copper:ore_copper",
            "ore_tungsten:ore_tungsten",
            "deepslate_ore_tungsten:deepslate_ore_tungsten",
            "ore_aluminium:ore_aluminium",
            "deepslate_ore_aluminium:aluminum_ore_deepslate",
            "ore_fluorite:ore_fluorite",
            "deepslate_ore_fluorite:deepslate_ore_fluorite",
            "ore_lead:ore_lead",
            "deepslate_ore_lead:lead_ore_deepslate",
            "ore_schrabidium:ore_schrabidium",
            "ore_beryllium:ore_beryllium",
            "deepslate_ore_beryllium:beryllium_ore_deepslate",
            "ore_lignite:ore_lignite",
            "deepslate_ore_lignite:deepslate_browncoal_ore",
            "ore_asbestos:ore_asbestos",
            "deepslate_ore_asbestos:deepslate_ore_asbestos",
            "cluster_iron:cluster_iron",
            "cluster_titanium:cluster_titanium",
            "cluster_aluminium:cluster_aluminium",
            "cluster_copper:cluster_copper",
            "ore_nether_coal:ore_nether_coal",
            "ore_nether_smoldering:ore_nether_smoldering",
            "ore_nether_uranium:ore_nether_uranium",
            "ore_nether_uranium_scorched:ore_nether_uranium_scorched",
            "ore_nether_plutonium:ore_nether_plutonium",
            "ore_nether_tungsten:ore_nether_tungsten",
            "ore_nether_sulfur:ore_nether_sulfur",
            "ore_nether_fire:ore_nether_fire",
            "ore_nether_cobalt:ore_nether_cobalt",
            "ore_nether_schrabidium:ore_nether_schrabidium",
            "stone_gneiss:stone_gneiss_var",
            "ore_gneiss_iron:ore_gneiss_iron",
            "ore_gneiss_gold:ore_gneiss_gold",
            "ore_gneiss_uranium:ore_gneiss_uranium",
            "ore_gneiss_uranium_scorched:ore_gneiss_uranium_scorched",
            "ore_gneiss_copper:ore_gneiss_copper",
            "ore_gneiss_asbestos:ore_gneiss_asbestos",
            "ore_gneiss_lithium:ore_gneiss_lithium",
            "ore_gneiss_schrabidium:ore_gneiss_schrabidium",
            "ore_gneiss_rare:ore_gneiss_rare",
            "ore_gneiss_gas:ore_gneiss_gas",
            "stone_resource_sulfur:stone_resource_sulfur",
            "stone_resource_asbestos:stone_resource_asbestos",
            "stone_resource_hematite:stone_resource_hematite",
            "stone_resource_malachite:stone_resource_malachite",
            "stone_resource_limestone:stone_resource_limestone",
            "stone_resource_bauxite:stone_resource_bauxite",
            "gneiss_brick:gneiss_brick",
            "gneiss_tile:gneiss_tile",
            "gneiss_chiseled:gneiss_chiseled",
            "stone_depth:stone_depth",
            "ore_depth_cinnebar:ore_depth_cinnebar",
            "ore_depth_zirconium:ore_depth_zirconium",
            "ore_depth_borax:ore_depth_borax",
            "cluster_depth_iron:cluster_depth_iron",
            "cluster_depth_titanium:cluster_depth_titanium",
            "cluster_depth_tungsten:cluster_depth_tungsten",
            "ore_alexandrite:ore_alexandrite",
            "deepslate_ore_alexandrite:deepslate_ore_alexandrite",
            "depth_brick:depth_brick",
            "depth_tiles:depth_tiles",
            "depth_nether_brick:depth_nether_brick",
            "depth_nether_tiles:depth_nether_tiles",
            "depth_dnt:depth_dnt",
            "stone_depth_nether:stone_depth_nether",
            "ore_depth_nether_neodymium:ore_depth_nether_neodymium",
            "stone_porous:stone_porous",
            "basalt:basalt",
            "basalt_smooth:basalt_smooth",
            "basalt_brick:basalt_brick",
            "basalt_polished:basalt_polished",
            "basalt_tiles:basalt_tiles",
            "ore_australium:ore_australium",
            "ore_rare:ore_rare",
            "deepslate_ore_rare:ore_rare_deepslate",
            "ore_cobalt:ore_cobalt",
            "deepslate_ore_cobalt:cobalt_ore_deepslate",
            "ore_cinnebar:ore_cinnebar",
            "deepslate_ore_cinnebar:cinnabar_ore_deepslate",
            "ore_coltan:ore_coltan",
            "deepslate_ore_coltan:deepslate_ore_coltan",
            "ore_oil:ore_oil",
            "ore_oil_empty:ore_oil_empty",
            "ore_oil_sand:ore_oil_sand_alt",
            "ore_bedrock_oil:ore_bedrock_oil",
            "dirt_dead:dirt_dead",
            "dirt_oily:dirt_oily",
            "sand_dirty:sand_dirty",
            "sand_dirty_red:sand_dirty_red",
            "stone_cracked:stone_cracked",
            "ore_tikite:ore_tikite_alt",
            "block_uranium:block_uranium",
            "block_u233:block_u233",
            "block_u235:block_u235",
            "block_u238:block_u238",
            "block_uranium_fuel:block_uranium_fuel",
            "block_thorium:block_thorium",
            "block_thorium_fuel:block_thorium_fuel",
            "block_neptunium:block_neptunium",
            "block_polonium:block_polonium",
            "block_mox_fuel:block_mox_fuel",
            "block_plutonium:block_plutonium",
            "block_pu238:block_pu238",
            "block_pu239:block_pu239",
            "block_pu240:block_pu240",
            "block_pu_mix:block_pu_mix",
            "block_plutonium_fuel:block_plutonium_fuel",
            "block_titanium:block_titanium",
            "block_sulfur:block_sulfur",
            "block_niter:block_niter",
            "block_copper:block_copper",
            "block_red_copper:block_red_copper",
            "block_tungsten:block_tungsten",
            "block_aluminium:block_aluminium",
            "block_fluorite:block_fluorite",
            "block_steel:block_steel",
            "block_tcalloy:block_tcalloy",
            "block_cdalloy:block_cdalloy",
            "block_lead:block_lead",
            "block_bismuth:block_bismuth",
            "block_cadmium:block_cadmium",
            "block_coltan:block_coltan",
            "block_tantalium:block_tantalium",
            "block_niobium:block_niobium",
            "block_trinitite:block_trinitite",
            "block_waste:block_waste",
            "block_waste_painted:block_waste_painted",
            "block_waste_vitrified:block_waste_vitrified",
            "ancient_scrap:ancient_scrap",
            "block_corium:block_corium",
            "block_corium_cobble:block_corium_cobble",
            "crystal_virus:legacy_blocks/crystal_virus",
            "crystal_hardened:legacy_blocks/crystal_hardened",
            "block_scrap:block_scrap",
            "block_electrical_scrap:electrical_scrap",
            "block_beryllium:block_beryllium",
            "block_schraranium:block_schraranium",
            "block_schrabidium:block_schrabidium",
            "block_schrabidate:block_schrabidate",
            "block_solinium:block_solinium",
            "block_schrabidium_fuel:block_schrabidium_fuel",
            "block_euphemium:block_euphemium",
            "block_dineutronium:block_dineutronium",
            "block_schrabidium_cluster:block_schrabidium_cluster_side",
            "block_euphemium_cluster:block_euphemium_cluster_side",
            "block_magnetized_tungsten:block_magnetized_tungsten",
            "block_combine_steel:block_combine_steel",
            "block_desh:block_desh",
            "block_dura_steel:block_dura_steel",
            "block_starmetal:block_starmetal",
            "block_polymer:block_polymer",
            "block_bakelite:block_bakelite",
            "block_rubber:block_rubber",
            "block_yellowcake:block_yellowcake",
            "block_insulator:block_insulator_side",
            "block_fiberglass:block_fiberglass_side",
            "block_asbestos:block_asbestos",
            "block_cobalt:block_cobalt",
            "block_lithium:block_lithium",
            "block_zirconium:block_zirconium",
            "block_white_phosphorus:block_white_phosphorus",
            "block_red_phosphorus:block_red_phosphorus",
            "block_fallout:ash",
            "block_boron:block_boron",
            "block_lanthanium:block_lanthanium",
            "block_ra226:block_ra226",
            "block_actinium:block_actinium",
            "block_tritium:block_tritium_side",
            "block_smore:block_smore_side",
            "block_slag:block_slag",
            "block_australium:block_australium",
            "deco_titanium:deco_titanium",
            "deco_red_copper:deco_red_copper",
            "deco_tungsten:deco_tungsten",
            "deco_aluminium:deco_aluminium",
            "deco_steel:deco_steel",
            "deco_rusty_steel:deco_rusty_steel",
            "deco_lead:deco_lead",
            "deco_beryllium:deco_beryllium",
            "deco_asbestos:deco_asbestos",
            "deco_rbmk:rbmk/rbmk_top",
            "deco_rbmk_smooth:rbmk/rbmk_blank_top",
            "deco_emitter:emitter",
            "part_emitter:part_top",
            "tape_recorder:legacy_blocks/deco_tape_recorder",
            "bobblehead:block_steel",
            "snowglobe:glass_boron",
            "plushie:block_fiberglass_side",
            "gravel_obsidian:gravel_obsidian",
            "gravel_diamond:gravel_diamond",
            "asphalt:asphalt",
            "asphalt_light:asphalt_light",
            "sandbags:sandbags",
            "wood_barrier:wood_barrier",
            "wood_structure:wood_barrier",
            "reinforced_brick:reinforced_brick",
            "reinforced_light:reinforced_light",
            "reinforced_sand:reinforced_sand",
            "reinforced_lamp_off:reinforced_lamp_off",
            "lamp_tritium_green_off:lamp_tritium_green_off",
            "lamp_tritium_blue_off:lamp_tritium_blue_off",
            "lamp_demon:lamp_demon",
            "lantern:block_steel",
            "spotlight_incandescent:cage_lamp",
            "spotlight_fluoro:fluorescent_lamp",
            "spotlight_halogen:flood_lamp",
            "floodlight:block_steel",
            "rebar:rebar",
            "reinforced_stone:reinforced_stone",
            "concrete_smooth:concrete",
            "concrete_colored:concrete",
            "concrete:concrete_tile",
            "concrete_asbestos:concrete_asbestos",
            "concrete_rebar:concrete_rebar",
            "concrete_super_broken:concrete_super_broken",
            "concrete_pillar:concrete_pillar_side",
            "brick_concrete:brick_concrete",
            "brick_concrete_mossy:brick_concrete_mossy",
            "brick_concrete_cracked:brick_concrete_cracked",
            "brick_concrete_broken:brick_concrete_broken",
            "brick_concrete_marked:brick_concrete_marked",
            "brick_obsidian:brick_obsidian",
            "brick_light:brick_light",
            "brick_compound:brick_compound",
            "cmb_brick:cmb_brick",
            "cmb_brick_reinforced:cmb_brick_reinforced",
            "brick_asbestos:brick_asbestos",
            "brick_fire:brick_fire",
            "glyphid_spawner:glyphid_eggs_alt",
            "ducrete_smooth:ducrete",
            "ducrete:ducrete_tile",
            "brick_ducrete:brick_ducrete",
            "reinforced_ducrete:reinforced_ducrete",
            "tile_lab:tile_lab",
            "tile_lab_cracked:tile_lab_cracked",
            "tile_lab_broken:tile_lab_broken",
            "block_meteor:meteor",
            "block_meteor_cobble:meteor_cobble",
            "__end__:__end__"
    );

    public static final List<RegistryObject<Block>> BLOCK_TAB_BLOCKS = Stream.concat(
            Stream.of(WASTE_EARTH, WASTE_MYCELIUM, WASTE_LEAVES, WASTE_LOG, WASTE_PLANKS, FROZEN_GRASS, FROZEN_DIRT,
                    FROZEN_LOG, FROZEN_PLANKS, LEAVES_LAYER, OIL_SPILL, SELLAFIELD, SELLAFIELD_SLAKED,
            SELLAFIELD_BEDROCK, ORE_SELLAFIELD_DIAMOND, ORE_SELLAFIELD_EMERALD, ORE_SELLAFIELD_URANIUM_SCORCHED,
            ORE_SELLAFIELD_SCHRABIDIUM, ORE_SELLAFIELD_RADGEM, WASTE_TRINITITE, WASTE_TRINITITE_RED, GLASS_TRINITITE,
            MOON_TURF, REINFORCED_LAMINATE, REINFORCED_LAMINATE_PANE, MUSH,
            PLANT_FLOWER_FOXGLOVE, PLANT_FLOWER_TOBACCO, PLANT_FLOWER_NIGHTSHADE, PLANT_FLOWER_WEED,
            PLANT_FLOWER_CD0, PLANT_FLOWER_CD1,
            BLOCK_CAP_NUKA, BLOCK_CAP_QUANTUM, BLOCK_CAP_SPARKLE, BLOCK_CAP_RAD, BLOCK_CAP_KORL, BLOCK_CAP_FRITZ,
            ASH_DIGAMMA, BALEFIRE, PRIBRIS, PRIBRIS_BURNING, PRIBRIS_RADIATING, PRIBRIS_DIGAMMA,
            ORE_BASALT, VOLCANIC_LAVA_BLOCK, RAD_LAVA_BLOCK, BLOCK_FOAM, MUD_BLOCK,
                    STEEL_SCAFFOLD, STEEL_BEAM, STEEL_GRATE, STEEL_GRATE_WIDE, CHAIN,
                    POLE_TOP, POLE_SATELLITE_RECEIVER, TEKTITE,
                    ORE_TEKTITE_OSMIRIDIUM, BLOCK_GRAPHITE, BLOCK_SEMTEX, BLOCK_C4,
                    STALACTITE_SULFUR, STALACTITE_ASBESTOS, STALAGMITE_SULFUR, STALAGMITE_ASBESTOS),
            EXTRA_BLOCK_TAB_BLOCKS.stream()).toList();

    public static final List<RegistryObject<Block>> NUKE_TAB_BLOCKS = List.of(
            NUKE_GADGET,
            NUKE_BOY,
            NUKE_MAN,
            NUKE_TSAR,
            NUKE_MIKE,
            NUKE_PROTOTYPE,
            NUKE_FLEIJA,
            NUKE_SOLINIUM,
            NUKE_N2,
            NUKE_CUSTOM,
            NUKE_FSTBMB,
            BOMB_MULTI,
            DYNAMITE,
            TNT_NTM,
            SEMTEX,
            C4,
            DET_CORD,
            DET_CHARGE,
            DET_NUKE,
            VOLCANO_CORE,
            VOLCANO_RAD_CORE,
            CHARGE_DYNAMITE,
            CHARGE_MINER,
            CHARGE_C4,
            CHARGE_SEMTEX,
            RED_BARREL,
            PINK_BARREL,
            LOX_BARREL,
            TAINT_BARREL,
            YELLOW_BARREL,
            VITRIFIED_BARREL
    );

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    public static RegistryObject<? extends Block> legacyBlock(String name) {
        return BLOCKS_BY_LEGACY_NAME.get(name);
    }

    private static RegistryObject<Block> machine(String name) {
        return registerBlockWithItem(name, () -> new HorizontalMachineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> reactorControl(String name) {
        return registerBlockWithItem(name, () -> new ReactorControlBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> hexafluorideTank(String name, HexafluorideTankBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new HexafluorideTankBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), kind),
                block -> new HexafluorideTankBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> deuteriumExtractor(String name) {
        return registerBlockWithItem(name, () -> new DeuteriumExtractorBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> teleanchor(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> fieldDisturber(String name) {
        return registerBlockWithItem(name, () -> new FieldDisturberBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 200.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> siren(String name) {
        return registerBlockWithItem(name, () -> new SirenBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> anvil(String name, int tier) {
        return registerBlockWithItem(
                name,
                () -> new NTMAnvilBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 100.0F)
                        .sound(SoundType.ANVIL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), tier),
                block -> NTMAnvilBlock.item((NTMAnvilBlock) block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> shredderMachine(String name) {
        return registerBlockWithItem(name, () -> new ShredderBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> autocrafterMachine(String name) {
        return registerBlockWithItem(name, () -> new AutocrafterBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> powerDetector(String name) {
        return registerBlockWithItem(name, () -> new PowerDetectorBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> geiger(String name) {
        return registerBlockWithItem(name, () -> new GeigerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(15.0F, 0.25F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new GeigerBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> chargerMachine(String name) {
        return registerBlockWithItem(name, () -> new ChargerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> refuelerMachine(String name) {
        return registerBlockWithItem(name, () -> new RefuelerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new ObjMachineBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> radioDecoMachine(String name, boolean broadcaster) {
        return registerBlockWithItem(name, () -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion();
            return broadcaster ? new RadioboxBlock(properties) : new RadioReceiverBlock(properties);
        }, block -> new ObjMachineBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> teslaMachine(String name) {
        return registerBlockWithItem(name, () -> new TeslaBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new ObjMachineBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> microwaveMachine(String name) {
        return registerBlockWithItem(name, () -> new MicrowaveBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> funnelMachine(String name) {
        return registerBlockWithItem(name, () -> new FunnelMachineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(10.0F, 20.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> keyForgeMachine(String name) {
        return registerBlockWithItem(name, () -> new KeyForgeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> autosawMachine(String name) {
        return registerBlockWithItem(name, () -> new AutosawBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> thresherMachine(String name) {
        return registerBlockWithItem(name, () -> new ThresherBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> pressPreheater(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> forceFieldMachine(String name) {
        return registerBlockWithItem(name, () -> new ForceFieldBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 100.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> electricFurnaceMachine(String name) {
        return registerBlockWithItem(name, () -> new ElectricFurnaceBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> diFurnaceMachine(String name) {
        return registerBlockWithItem(name, () -> new DiFurnaceBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> diFurnaceExtension(String name) {
        return registerBlockWithItem(name, () -> new DiFurnaceExtensionBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> brickFurnaceMachine(String name) {
        return registerBlockWithItem(name, () -> new BrickFurnaceBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> rtgFurnaceMachine(String name) {
        return registerBlockWithItem(name, () -> new RtgFurnaceBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> diFurnaceRtgMachine(String name) {
        return registerBlockWithItem(name, () -> new DiFurnaceRtgBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> capacitorBus(String name) {
        return registerBlockWithItem(name, () -> new CapacitorBusBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()),
                block -> new LegacyLoreBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> nonOccludingMachine(String name) {
        return registerBlockWithItem(name, () -> new HorizontalMachineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), false));
    }

    private static RegistryObject<Block> fan(String name) {
        return registerBlockWithItem(name, () -> new LegacyFanBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new LegacyFanBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> filingCabinet(String name) {
        return registerBlockWithItem(name, () -> new LegacyFileCabinetBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(10.0F, 15.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new LegacyFileCabinetBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> balefireBomb(String name) {
        return registerBlockWithItem(name, () -> new BalefireBombBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 200.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new BalefireBombBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> nuclearDevice(String name, NuclearDeviceBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new NuclearDeviceBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), kind),
                block -> new NuclearDeviceBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> customNuke(String name) {
        return registerBlockWithItem(name, () -> new CustomNukeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new NuclearDeviceBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> bombMulti(String name) {
        return registerBlockWithItem(name, () -> new BombMultiBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new BombMultiBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> basicMachine(String name) {
        return registerBlockWithItem(name, () -> new MachineBlockEntityBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), false));
    }

    private static RegistryObject<Block> pressMachine(String name) {
        return registerBlockWithItem(
                name,
                () -> new PressMachineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> electricPressMachine(String name) {
        return registerBlockWithItem(
                name,
                () -> new ElectricPressBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> boilerMachine(String name) {
        return registerBlockWithItem(name, () -> new OldBoilerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> condenserMachine(String name) {
        return registerBlockWithItem(name, () -> new CondenserBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> steamTurbineMachine(String name) {
        return registerBlockWithItem(name, () -> new SteamTurbineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> decon(String name) {
        return registerBlockWithItem(name, () -> new DeconBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> armorTable(String name) {
        return registerBlockWithItem(name, () -> new ArmorTableBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> weaponTable(String name) {
        return registerBlockWithItem(name, () -> new WeaponTableBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> redCable(String name) {
        return registerBlockWithItem(
                name,
                () -> new RedCableBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new RedCableBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> redCableClassic(String name) {
        return registerBlockWithItem(name, () -> new RedCableClassicBlock(redCableProperties()));
    }

    private static RegistryObject<Block> redWireCoated(String name) {
        return registerBlockWithItem(name, () -> new RedWireCoatedBlock(redCableProperties()));
    }

    private static RegistryObject<Block> redCableBox(String name) {
        return registerBlockWithItem(
                name,
                () -> new RedCableBoxBlock(redCableProperties()),
                block -> new LegacyStateBlockItem(block.get(), new Item.Properties(), RedCableBoxBlock.SIZE, 5,
                        variant -> Component.translatable("block." + HbmNtm.MOD_ID + "." + name)));
    }

    private static BlockBehaviour.Properties redCableProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }

    private static RegistryObject<Block> redCableGauge(String name) {
        return registerBlockWithItem(name, () -> new RedCableGaugeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> poweredRedCable(String name, PoweredRedCableBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new PoweredRedCableBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(), kind));
    }

    private static RegistryObject<Block> cableDiode(String name) {
        return registerBlockWithItem(
                name,
                () -> new CableDiodeBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new CableDiodeBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> radioTorchSender(String name) {
        return registerBlockWithItem(name, () -> new RadioTorchSenderBlock(radioTorchProperties()));
    }

    private static RegistryObject<Block> radioTorchReceiver(String name) {
        return registerBlockWithItem(name, () -> new RadioTorchReceiverBlock(radioTorchProperties()));
    }

    private static RegistryObject<Block> radioTorchCounter(String name) {
        return registerBlockWithItem(name, () -> new RadioTorchCounterDeviceBlock(radioTorchProperties()));
    }

    private static RegistryObject<Block> radioTorchLogic(String name) {
        return registerBlockWithItem(name, () -> new RadioTorchLogicBlock(radioTorchProperties()));
    }

    private static RegistryObject<Block> radioTorchReader(String name) {
        return registerBlockWithItem(name, () -> new RadioTorchReaderDeviceBlock(radioTorchProperties()));
    }

    private static RegistryObject<Block> radioTorchController(String name) {
        return registerBlockWithItem(name, () -> new RadioTorchControllerDeviceBlock(radioTorchProperties()));
    }

    private static RegistryObject<Block> radioAutocal(String name) {
        return registerBlockWithItem(
                name,
                () -> new RadioAutocalBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> radioTelex(String name) {
        return registerBlockWithItem(
                name,
                () -> new RadioTelexBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WOOD)
                        .strength(3.0F, 10.0F)
                        .sound(SoundType.WOOD)
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> rbmkPanel(String name, RBMKPanelPlanner.PanelType panelType) {
        return registerBlockWithItem(name, () -> new RBMKPanelBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), panelType));
    }

    private static RegistryObject<Block> rbmkDisplayBlank(String name) {
        return registerBlockWithItem(name, () -> new RBMKDisplayBlankBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> rbmkColumn(String name, RBMKColumnBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new RBMKColumnBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> rbmkAutoloader(String name) {
        return registerBlockWithItem(
                name,
                () -> new RBMKAutoloaderBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(50.0F, 60.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> rbmkCraneConsole(String name) {
        return registerBlockWithItem(
                name,
                () -> new RBMKCraneConsoleBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> rbmkConsole(String name) {
        return registerBlockWithItem(
                name,
                () -> new RBMKConsoleBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> rbmkUtilityBlock(String name, RBMKUtilityBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new RBMKUtilityBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(50.0F, 60.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(), kind));
    }

    private static RegistryObject<Block> pileGraphite(String name) {
        return registerBlockWithItem(name, () -> new PileGraphiteBlock(pileGraphiteProperties()));
    }

    private static RegistryObject<Block> pileGraphiteDrilled(String name) {
        return registerBlockWithItem(name, () -> new PileGraphiteDrilledBlock(pileGraphiteProperties()));
    }

    private static RegistryObject<Block> pileGraphiteFuel(String name) {
        return registerBlockWithItem(name, () -> new PileGraphiteFuelBlock(pileGraphiteProperties()));
    }

    private static RegistryObject<Block> pileGraphiteSource(
            String name,
            PileGraphiteInsertionPlanner.GraphiteBlockKind graphiteKind) {
        return registerBlockWithItem(name, () -> new PileGraphiteSourceBlock(pileGraphiteProperties(), graphiteKind));
    }

    private static RegistryObject<Block> pileGraphiteRod(String name) {
        return registerBlockWithItem(name, () -> new PileGraphiteRodBlock(pileGraphiteProperties()));
    }

    private static RegistryObject<Block> pileGraphiteBreedingFuel(String name) {
        return registerBlockWithItem(name, () -> new PileGraphiteBreedingFuelBlock(pileGraphiteProperties()));
    }

    private static RegistryObject<Block> pileGraphiteBreedingProduct(String name) {
        return registerBlockWithItem(name, () -> new PileGraphiteBreedingProductBlock(pileGraphiteProperties()));
    }

    private static RegistryObject<Block> pileGraphiteNeutronDetector(String name) {
        return registerBlockWithItem(name, () -> new PileGraphiteNeutronDetectorBlock(pileGraphiteProperties()));
    }

    private static BlockBehaviour.Properties pileGraphiteProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties radioTorchProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.1F, 10.0F)
                .sound(SoundType.WOOD)
                .noCollission()
                .noOcclusion();
    }

    private static RegistryObject<Block> fluidPipe(String name) {
        return registerBlockWithItem(
                name,
                () -> new FluidPipeBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(PIPE_SOUND)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new FluidPipeStyleBlockItem(block.get(), new Item.Properties(), FluidPipeBlock.LEGACY_STYLE,
                        FluidPipeBlock.LEGACY_STYLE_COUNT,
                        variant -> Component.translatable("block." + HbmNtm.MOD_ID + "." + name)));
    }

    private static RegistryObject<Block> fluidValve(String name, FluidValveBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new FluidValveBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(PIPE_SOUND)
                        .requiresCorrectToolForDrops(), kind));
    }

    private static RegistryObject<Block> fluidPump(String name) {
        return registerBlockWithItem(name, () -> new FluidPumpBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> fluidDuctBox(String name) {
        return registerBlockWithItem(
                name,
                () -> new FluidDuctBoxBlock(fluidPipeProperties()),
                block -> new FluidDuctVariantBlockItem(block.get(), new Item.Properties(),
                        FluidDuctBoxBlock.boxCreativeMetadata()));
    }

    private static RegistryObject<Block> fluidDuctGauge(String name) {
        return registerBlockWithItem(
                name,
                () -> new FluidDuctGaugeBlock(fluidPipeProperties()));
    }

    private static RegistryObject<Block> fluidDuctExhaust(String name) {
        return registerBlockWithItem(
                name,
                () -> new FluidDuctExhaustBlock(fluidPipeProperties()),
                block -> new FluidDuctVariantBlockItem(block.get(), new Item.Properties(),
                        FluidDuctBoxBlock.exhaustCreativeMetadata()));
    }

    private static RegistryObject<Block> fluidDuctPaintable(String name) {
        return registerBlockWithItem(
                name,
                () -> new FluidDuctPaintableBlock(fluidPipeProperties()));
    }

    private static RegistryObject<Block> fluidDuctPaintableExhaust(String name) {
        return registerBlockWithItem(
                name,
                () -> new FluidDuctPaintableExhaustBlock(fluidPipeProperties()));
    }

    private static RegistryObject<Block> fluidPipeAnchor(String name) {
        return registerBlockWithItem(
                name,
                () -> new FluidPipeAnchorBlock(fluidPipeProperties()));
    }

    private static BlockBehaviour.Properties fluidPipeProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(PIPE_SOUND)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }

    private static RegistryObject<Block> fluidBarrel(String name, FluidBarrelBlock.Variant variant) {
        return registerBlockWithItem(name, () -> new FluidBarrelBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 5.0F)
                .sound(variant == FluidBarrelBlock.Variant.PLASTIC ? SoundType.STONE : SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), variant));
    }

    private static RegistryObject<Block> pneumaticTube(String name) {
        return registerBlockWithItem(name, () -> new PneumaticTubeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.1F, 10.0F)
                .sound(SoundType.METAL)
                .noOcclusion()));
    }

    private static <T extends Block> RegistryObject<T> conveyor(String name, Function<BlockBehaviour.Properties, T> factory) {
        return registerBlockWithItem(name, () -> factory.apply(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 2.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> craneSplitter(String name) {
        return registerBlockWithItem(
                name,
                () -> new CraneSplitterBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> craneLogistics(String name, CraneLogisticsBlockEntity.Kind kind) {
        return registerBlockWithItem(name, () -> new CraneLogisticsBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), kind));
    }

    private static RegistryObject<Block> gasMeltdown(String name) {
        return registerBlockWithItem(name, () -> new LegacyGasMeltdownBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.0F, 0.0F)
                .noCollission()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> gasRadon(String name, LegacyGasRadonBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new LegacyGasRadonBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.0F, 0.0F)
                .noCollission()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false), kind));
    }

    private static RegistryObject<Block> toxicGas(String name, LegacyToxicGasBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new LegacyToxicGasBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.0F, 0.0F)
                .noCollission()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false), kind));
    }

    private static RegistryObject<Block> acidLiquidBlock(String name) {
        RegistryObject<Block> block = registerBlockWithoutItem(name,
                () -> new LegacyReactiveLiquidBlock(ModFluids.PEROXIDE.source(), legacyLiquidProperties(
                        MapColor.COLOR_YELLOW, 5), LegacyReactiveLiquidBlock.Kind.ACID));
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static RegistryObject<Block> toxicLiquidBlock(String name) {
        RegistryObject<Block> block = registerBlockWithoutItem(name,
                () -> new LegacyReactiveLiquidBlock(ModFluids.TOXIC_FLUID, legacyLiquidProperties(
                        MapColor.COLOR_LIGHT_GREEN, 15), LegacyReactiveLiquidBlock.Kind.TOXIC));
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static RegistryObject<Block> schrabidicLiquidBlock(String name) {
        RegistryObject<Block> block = registerBlockWithoutItem(name,
                () -> new LegacyReactiveLiquidBlock(ModFluids.SCHRABIDIC.source(), legacyLiquidProperties(
                        MapColor.COLOR_CYAN, 0), LegacyReactiveLiquidBlock.Kind.SCHRABIDIC));
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static RegistryObject<Block> mudLiquidBlock(String name) {
        RegistryObject<Block> block = registerBlockWithoutItem(name,
                () -> new LegacyReactiveLiquidBlock(ModFluids.MUD_FLUID,
                        legacyLiquidProperties(MapColor.COLOR_BROWN, 5)
                                .sound(SoundType.SLIME_BLOCK),
                        LegacyReactiveLiquidBlock.Kind.MUD));
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static BlockBehaviour.Properties legacyLiquidProperties(MapColor color, int light) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(100.0F, 500.0F)
                .lightLevel(state -> light)
                .replaceable()
                .noCollission()
                .noOcclusion()
                .liquid()
                .noLootTable()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

    private static RegistryObject<Block> radAbsorber(String name) {
        return registerBlockWithItem(
                name,
                () -> new LegacyRadAbsorberBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()),
                block -> new LegacyStateBlockItem(block.get(), new Item.Properties(), LegacyRadAbsorberBlock.TIER, 4,
                        variant -> Component.translatable(variant == 0
                                ? "block.hbm_ntm_rebirth.rad_absorber"
                                : "block.hbm_ntm_rebirth.rad_absorber." + variant)));
    }

    private static RegistryObject<Block> dummyBlock(String name) {
        return registerBlockWithItem(name, () -> new DummyBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .randomTicks()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> steelScaffold(String name) {
        return registerBlockWithItem(name, () -> new SteelScaffoldBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 15.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> steelGrate(String name, boolean wide) {
        return registerBlockWithItem(name, () -> new LegacyGrateBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 5.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false), wide));
    }

    private static RegistryObject<Block> assemblyMachine(String name) {
        return registerBlockWithItem(
                name,
                () -> new AssemblyMachineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> teleporterMachine(String name) {
        return registerBlockWithItem(name, () -> new TeleporterBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> vendingMachine(String name) {
        return registerBlockWithItem(
                name,
                () -> new VendingMachineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new LegacyStateMultiblockBlockItem(block.get(), new Item.Properties(),
                        VendingMachineBlock.VARIANT, 2,
                        variant -> Component.translatable(variant == 0
                                ? "block.hbm_ntm_rebirth.vending_machine"
                                : "block.hbm_ntm_rebirth.vending_machine.snacks")));
    }

    private static RegistryObject<Block> visibleMultiblockMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new LegacyVisibleMultiblockMachineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> watzPump(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new WatzPumpBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> hephaestusMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new HephaestusBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> fractionSpacerMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new FractionSpacerBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> strandCasterMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new StrandCasterBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> crucibleMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new CrucibleBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> arcFurnaceMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ArcFurnaceBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> miningLaserMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new MiningLaserBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> excavatorMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ExcavatorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> rotaryFurnaceMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new RotaryFurnaceBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> gasCentMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new GasCentBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> oreSlopperMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new OreSlopperBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> batteryReddMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new BatteryReddBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> exposureChamberMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ExposureChamberBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> chungusMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ChungusBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> combinationOvenMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new CombinationOvenBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> blastFurnaceMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new BlastFurnaceBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> radiolysisMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new RadiolysisBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> rtgMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new RtgBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> legacyGenericSelectorMachine(String name, LegacyMachineDefinition definition,
            LegacyGenericSelectorMachineBlockEntity.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new LegacyGenericSelectorMachineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition, kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> intakeMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new IntakeBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> drainMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new DrainBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> stirlingMachine(String name, LegacyMachineDefinition definition,
            StirlingBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new StirlingBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition, kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> sawmillMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new SawmillBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> processingMachine(String name, LegacyMachineDefinition definition,
            ProcessingMachineBlockEntity.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new ProcessingMachineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition, kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> mixerMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new MixerBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> electrolyserMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ElectrolyserBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> legacyFurnaceMachine(String name, LegacyMachineDefinition definition,
            LegacyFurnaceBlockEntity.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new LegacyFurnaceBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition, kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> ammoPressMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new AmmoPressBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> radGenMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new RadGenBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> researchReactor(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ResearchReactorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> breedingReactor(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new BreedingReactorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> zirnoxReactor(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ZirnoxReactorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> zirnoxDestroyed(String name, LegacyMachineDefinition definition) {
        return registerBlockWithoutItem(
                name,
                () -> new ZirnoxDestroyedBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(100.0F, 800.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition));
    }

    private static RegistryObject<Block> watzReactor(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new WatzReactorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> icfReactor(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ICFReactorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> icfAssembledBlock(String name) {
        return registerBlockWithoutItem(name, () -> new ICFAssembledBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> fusionMachine(String name, LegacyMachineDefinition definition,
            FusionMachineBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new FusionMachineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition, kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> dfcMachine(String name, DfcMachineBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new DfcMachineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), kind));
    }

    private static RegistryObject<Block> icfController(String name) {
        return registerBlockWithItem(name, () -> new ICFControllerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> icfPress(String name) {
        return registerBlockWithItem(name, () -> new ICFPressBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()),
                block -> new LegacyLoreBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> icfComponentBlock(String name, String texture) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> fusionStructureBlock(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> fusionBsccoComponentBlock(String name) {
        return registerBlockWithItem(name, () -> new FusionStructureComponentBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(),
                FusionStructureComponentBlock.Conversion.bsccoCoil(FUSION_COMPONENT_BSCCO_WELDED::get)));
    }

    private static RegistryObject<Block> icfStructureBlock(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 60.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> icfVesselComponentBlock(String name) {
        return registerBlockWithItem(name, () -> new ICFStructureComponentBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 60.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(),
                ICFStructureComponentBlock.Conversion.vessel(ICF_COMPONENT_VESSEL_WELDED::get)));
    }

    private static RegistryObject<Block> icfStructureComponentBlock(String name) {
        return registerBlockWithItem(name, () -> new ICFStructureComponentBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 60.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(),
                ICFStructureComponentBlock.Conversion.structure(ICF_COMPONENT_STRUCTURE_BOLTED::get)));
    }

    private static RegistryObject<Block> icfStructCoreBlock(String name) {
        return registerBlockWithItem(name, () -> new ICFStructCoreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .lightLevel(state -> 15)
                .noOcclusion()
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> fusionTorusStructCoreBlock(String name) {
        return registerBlockWithItem(name, () -> new FusionTorusStructCoreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .lightLevel(state -> 15)
                .noOcclusion()
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> watzStructCoreBlock(String name) {
        return registerBlockWithItem(name, () -> new WatzStructCoreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .lightLevel(state -> 15)
                .noOcclusion()
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> watzPillar(String name) {
        return registerBlockWithItem(name, () -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> watzEndBlock(String name) {
        return registerBlockWithItem(name, () -> new WatzEndBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> pwrPillar(String name, PWRComponentBlock.PillarKind kind) {
        return registerBlockWithItem(name, () -> new PWRComponentBlock.Pillar(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(), kind));
    }

    private static RegistryObject<Block> pwrComponent(String name, PWRComponentBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new PWRComponentBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(), kind));
    }

    private static RegistryObject<Block> pwrController(String name) {
        return registerBlockWithItem(name, () -> new PWRControllerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> pwrAssembledBlock(String name) {
        return hiddenBlock(name, () -> new PWRAssembledBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> wasteDrum(String name) {
        return registerBlockWithItem(name, () -> new WasteDrumBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> storageDrum(String name) {
        return registerBlockWithItem(name, () -> new StorageDrumBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()),
                block -> new ObjMachineBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> woodBurnerMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new WoodBurnerBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> turbofanMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new TurbofanBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> turbineGasMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new TurbineGasBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> machineLpw2(String name) {
        return registerBlockWithItem(
                name,
                () -> new MachineLpw2Block(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> cargoElevator(String name) {
        return registerBlockWithItem(
                name,
                () -> new CargoElevatorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> particleAccelerator(String name, ParticleAcceleratorBlock.Variant variant) {
        return registerBlockWithItem(
                name,
                () -> new ParticleAcceleratorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), variant),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> launchPad(String name) {
        return registerBlockWithItem(
                name,
                () -> new LaunchPadBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> mediumPylon(String name, LegacyMediumPylonBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new LegacyMediumPylonBlock(BlockBehaviour.Properties.of()
                        .mapColor(kind.steel() ? MapColor.METAL : MapColor.WOOD)
                        .strength(5.0F, 10.0F)
                        .sound(kind.steel() ? SoundType.METAL : SoundType.WOOD)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> smallPylon(String name) {
        return registerBlockWithItem(
                name,
                () -> new LegacySmallPylonBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> redConnector(String name, LegacyConnectorBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new LegacyConnectorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> largePylon(String name) {
        return registerBlockWithItem(
                name,
                () -> new LegacyLargePylonBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> substation(String name) {
        return registerBlockWithItem(
                name,
                () -> new LegacySubstationBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> arcWelderMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ArcWelderBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> largeLaunchPad(String name) {
        return registerBlockWithItem(
                name,
                () -> new LargeLaunchPadBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> solderingStationMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new SolderingStationBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> silexMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new SilexBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> cyclotronMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new CyclotronBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> felMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new FelBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> annihilatorMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new AnnihilatorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> deuteriumTowerMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new DeuteriumTowerBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> assemblyFactoryMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new AssemblyFactoryBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> chemicalFactoryMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ChemicalFactoryBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> remoteFluidMachine(String name, LegacyMachineDefinition definition,
            RemoteFluidMachineBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new RemoteFluidMachineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition, kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> refineryMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new RefineryBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> solidifierMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new SolidifierBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> pyroOvenMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new PyroOvenBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> oilDrillMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new OilDrillBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> compressorMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new CompressorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> rustedLaunchPad(String name) {
        return registerBlockWithItem(
                name,
                () -> new RustedLaunchPadBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> customMissileLauncher(String name, CustomMissileLauncherBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new CustomMissileLauncherBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> combustionEngineMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new CombustionEngineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> dieselGeneratorMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new DieselGeneratorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> heatBoilerMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new HeatBoilerBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> electricHeaterMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ElectricHeaterBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> fireboxHeaterMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new FireboxHeaterBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> oilburnerMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new OilburnerBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> heaterHeatexMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new HeaterHeatexBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> waterPumpMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new WaterPumpBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> poweredCondenserMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new PoweredCondenserBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> steamTurbineMultiblockMachine(String name, LegacyMachineDefinition definition,
            SteamTurbineMultiblockBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new SteamTurbineMultiblockBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition, kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> legacyLargeTurbineMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new LegacyLargeTurbineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> gasFlareMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new GasFlareBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> chimneyMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ChimneyBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> ashpitMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new AshpitBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> steamEngineMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new SteamEngineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> solarBoilerMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new SolarBoilerBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> solarMirror(String name) {
        return registerBlockWithItem(name, () -> new SolarMirrorBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> coolingTowerMachine(String name, LegacyMachineDefinition definition,
            CoolingTowerBlock.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new CoolingTowerBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition, kind),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> fluidTankMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new FluidTankBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> bigAssTankMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new BigAssTankBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> bat9000Machine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new Bat9000Block(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> orbusMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new OrbusBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> chemicalPlantMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new ChemicalPlantBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> liquefactorMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new LiquefactorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(10.0F, 20.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static LegacyMachineDefinition chemicalPlantDefinition() {
        return LegacyMachineDefinition.builder(machineModel("chemical_plant"), machineTexture("chemical_plant"))
                .legacyXrDimensions(2, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.squarePerimeter(1),
                                proxyInventoryPowerFluid()))
                .renderParts("Base", "Frame", "Slider", "Spinner")
                .legacyItemScale(4.5D, 0.75D)
                .yRotation(facing -> 270.0F - facing.toYRot())
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 4, 3)))
                .build();
    }

    private static LegacyMachineDefinition chemicalFactoryDefinition() {
        return LegacyMachineDefinition.builder(machineModel("chemical_factory"), machineTexture("chemical_factory"))
                .legacyXrDimensions(2, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 2, 2, 2, 2 }, facing)
                        .withExtraProxyOffsets(assemblyFactoryProxyOffsets(facing), proxyInventoryPowerFluid()))
                .renderParts("Base", "Frame", "Fan1", "Fan2")
                .legacyItemScale(3.0D, 0.75D)
                .yRotation(facing -> 270.0F - facing.toYRot())
                .renderBoundingBox(pos -> new AABB(pos.offset(-8, 0, -8), pos.offset(18, 10, 18)))
                .build();
    }

    private static LegacyMachineDefinition liquefactorDefinition() {
        return LegacyMachineDefinition.builder(machineModel("liquefactor"), machineTexture("liquefactor"))
                .legacyXrDimensions(3, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(liquefactorProxyOffsets(facing), proxyInventoryPowerFluid()))
                .renderParts("Main", "Fluid", "Glass")
                .translucentPart("Glass", 0xBFFFFF, 38)
                .itemRenderParts("Main")
                .legacyItemScale(3.0F)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 4, 2)))
                .build();
    }

    private static LegacyMachineDefinition industrialTurbineDefinition() {
        return LegacyMachineDefinition.builder(machineModel("industrial_turbine"), machineTexture("industrial_turbine"))
                .legacyXrDimensions(2, 0, 3, 3, 1, 1)
                .legacyOffset(3)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 3, 3, 1, 1 }, facing)
                        .withExtraProxyOffsets(industrialTurbineProxyOffsets(facing), proxyPowerFluid()))
                .renderParts("Turbine", "Gauge", "Flywheel")
                .legacyItemScale(3.0D, 0.75D)
                .yRotation(facing -> 270.0F - facing.toYRot())
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .renderBoundingBox(pos -> new AABB(pos.offset(-5, 0, -5), pos.offset(6, 4, 6)))
                .renderProfile(LegacyMachineRenderProfile.INDUSTRIAL_TURBINE_ITEM_PREVIEW)
                .build();
    }

    private static LegacyMachineDefinition legacyLargeTurbineDefinition() {
        return LegacyMachineDefinition.builder(machineModel("turbine"), machineTexture("turbine"))
                .legacyXrDimensions(1, 0, 3, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 3, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(legacyLargeTurbineProxyOffsets(facing), proxyPowerFluid()))
                .renderParts("Body", "Blades")
                .partTextures(Map.of("Blades",
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/turbofan_blades.png")))
                .itemPartTextures(Map.of("Blades",
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/turbofan_blades.png")))
                .legacyItemScale(3.0D, 0.75D)
                .yRotation(facing -> 270.0F - facing.toYRot())
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_NO_CULL)
                .renderBoundingBox(pos -> new AABB(pos.offset(-5, 0, -5), pos.offset(6, 3, 6)))
                .renderProfile(LegacyMachineRenderProfile.LEGACY_LARGE_TURBINE_ITEM_PREVIEW)
                .build();
    }

    private static LegacyMachineDefinition refineryDefinition() {
        return LegacyMachineDefinition.builder(machineModel("refinery"), machineTexture("refinery"))
                .legacyXrDimensions(8, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 8, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(rearCornerProxyOffsets(facing), proxyInventoryPowerFluid()))
                .legacyItemScale(3.0D, 0.5D)
                .yRotation(facing -> 180.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, -1, -3), pos.offset(3, 11, 3)))
                .renderProfile(LegacyMachineRenderProfile.REFINERY_DAMAGE_STATE)
                .build();
    }

    private static LegacyMachineDefinition oilWellDefinition() {
        return LegacyMachineDefinition.builder(machineModel("derrick"), machineTexture("derrick"))
                .legacyXrDimensions(9, 0, 1, 1, 1, 1)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofOffsets(List.of(BlockPos.ZERO))
                        .withLegacyXrCheckedFill(new int[] { 1, -1, 0, 0, 0, 0 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 8, 0, 1, 1, 1, 1 }, facing, new BlockPos(0, 1, 0))
                        .withLegacyXrCheckedFill(new int[] { -1, 1, 0, 0, 0, 0 }, facing, new BlockPos(1, 1, 1))
                        .withLegacyXrCheckedFill(new int[] { -1, 1, 0, 0, 0, 0 }, facing, new BlockPos(1, 1, -1))
                        .withLegacyXrCheckedFill(new int[] { -1, 1, 0, 0, 0, 0 }, facing, new BlockPos(-1, 1, 1))
                        .withLegacyXrCheckedFill(new int[] { -1, 1, 0, 0, 0, 0 }, facing, new BlockPos(-1, 1, -1)))
                .legacyItemScale(3.0D, 0.5D)
                .yRotation(facing -> normalizeRotation(solidifierRotation(facing) + 90.0F))
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, -1, -2), pos.offset(11, 11, 3)))
                .build();
    }

    private static LegacyMachineDefinition catalyticCrackerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("catalytic_cracker"), machineTexture("catalytic_cracker"))
                .legacyXrDimensions(0, 0, 3, 3, 2, 3)
                .legacyOffset(3)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 3, 3, 2, 3 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 8, -1, 3, -1, 2, 0 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 13, 0, 0, 3, 2, 1 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 14, -13, -1, 2, 1, 0 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, -1, 2, 3, -1, 3 }, facing)
                        .withExtraProxyOffsets(catalyticCrackerProxyOffsets(facing), proxyFluid()))
                .legacyItemScale(1.8D, 0.5D)
                .yRotation(ModBlocks::catalyticRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, 0, -4), pos.offset(5, 17, 5)))
                .build();
    }

    private static LegacyMachineDefinition catalyticReformerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("catalytic_reformer"), machineTexture("catalytic_reformer"))
                .legacyXrDimensions(2, 0, 1, 1, 2, 2)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 2, 2 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, -3, 1, 0, -1, 2 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 6, -3, 1, 1, 2, 0 }, facing)
                        .withExtraProxyOffsets(catalyticReformerProxyOffsets(facing), proxyPowerFluid()))
                .legacyItemScale(3.5D, 0.5D)
                .yRotation(ModBlocks::catalyticRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-3, 0, -3), pos.offset(4, 8, 4)))
                .build();
    }

    private static LegacyMachineDefinition vacuumDistillDefinition() {
        return LegacyMachineDefinition.builder(machineModel("vacuum_distill"), machineTexture("vacuum_distill"))
                .legacyXrDimensions(8, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 8, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(rearCornerProxyOffsets(facing), proxyPowerFluid()))
                .legacyItemScale(3.0D, 0.5D)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 10, 3)))
                .build();
    }

    private static LegacyMachineDefinition fractionTowerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("fraction_tower"), machineTexture("fraction_tower"))
                .legacyXrDimensions(2, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(1), proxyFluid()))
                .legacyItemScale(3.25F)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 4, 3)))
                .build();
    }

    private static LegacyMachineDefinition hydrotreaterDefinition() {
        return LegacyMachineDefinition.builder(machineModel("hydrotreater"), machineTexture("hydrotreater"))
                .legacyXrDimensions(6, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 6, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(rearCornerProxyOffsets(facing), proxyPowerFluid()))
                .legacyItemScale(4.0D, 0.5D)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 8, 3)))
                .build();
    }

    private static LegacyMachineDefinition cokerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("coker"), machineTexture("coker"))
                .legacyXrDimensions(22, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 22, 0, 1, 1, 1, 1 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 5, 0, 2, 2, 2, 2 }, Direction.NORTH, new BlockPos(0, 1, 0))
                        .withLegacyXrCheckedFill(new int[] { 0, 1, 0, 0, 0, 0 }, Direction.NORTH, new BlockPos(2, 1, 2))
                        .withLegacyXrCheckedFill(new int[] { 0, 1, 0, 0, 0, 0 }, Direction.NORTH, new BlockPos(2, 1, -2))
                        .withLegacyXrCheckedFill(new int[] { 0, 1, 0, 0, 0, 0 }, Direction.NORTH, new BlockPos(-2, 1, 2))
                        .withLegacyXrCheckedFill(new int[] { 0, 1, 0, 0, 0, 0 }, Direction.NORTH, new BlockPos(-2, 1, -2))
                        .withExtraProxyOffsets(cokerProxyOffsets(), proxyInventoryFluid()))
                .legacyItemScale(2.75D, 0.25D)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-3, 0, -3), pos.offset(4, 24, 4)))
                .build();
    }

    private static LegacyMachineDefinition pyroOvenDefinition() {
        return LegacyMachineDefinition.builder(machineModel("pyrooven"), machineTexture("pyrooven"))
                .legacyXrDimensions(2, 0, 3, 3, 2, 2)
                .legacyOffset(3)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 3, 3, 2, 2 }, facing)
                        .withExtraProxyOffsets(pyroOvenProxyOffsets(facing), proxyInventoryPowerFluid()))
                .renderParts("Oven", "Slider", "Fan")
                .legacyItemScale(3.5D, 0.5D)
                .yRotation(ModBlocks::pyroOvenRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, 0, -4), pos.offset(5, 5, 5)))
                .build();
    }

    private static LegacyMachineDefinition solidifierDefinition() {
        return LegacyMachineDefinition.builder(machineModel("solidifier"), machineTexture("solidifier"))
                .legacyXrDimensions(3, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(solidifierProxyOffsets(), proxyInventoryPowerFluid()))
                .renderParts("Main", "Fluid", "Glass")
                .translucentPart("Glass", 0xBFFFFF, 38)
                .itemRenderParts("Main")
                .legacyItemScale(3.0F)
                .yRotation(ModBlocks::solidifierRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 5, 3)))
                .build();
    }

    private static LegacyMachineDefinition compressorDefinition() {
        return LegacyMachineDefinition.builder(machineModel("compressor"), machineTexture("compressor"))
                .legacyXrDimensions(2, 0, 1, 2, 1, 1)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 2, 1, 1 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, -3, 1, 1, 1, 1 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 8, -4, 0, 0, 1, 1 }, facing)
                        .withExtraProxyOffsets(compressorProxyOffsets(facing), proxyPowerFluid()))
                .renderParts("Compressor", "Pump", "Fan")
                .renderProfile(LegacyMachineRenderProfile.COMPRESSOR_RUNNING_PARTS)
                .legacyItemScale(3.0D, 0.5D)
                .yRotation(ModBlocks::solidifierRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-5, -4, -2), pos.offset(9, 10, 3)))
                .build();
    }

    private static LegacyMachineDefinition compressorCompactDefinition() {
        return LegacyMachineDefinition.builder(machineModel("condenser"), machineTexture("compressor_compact"))
                .legacyXrDimensions(2, 0, 1, 1, 3, 3)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 3, 3 }, facing)
                        .withExtraProxyOffsets(condenserPoweredProxyOffsets(facing), proxyPowerFluid()))
                .renderParts("Condenser", "Fan1", "Fan2")
                .renderProfile(LegacyMachineRenderProfile.COMPRESSOR_COMPACT_RUNNING_FANS)
                .legacyItemScale(2.75D, 0.75D)
                .yRotation(ModBlocks::eastZeroRotation)
                .particleState(legacyBlockParticleState("block_steel_machine"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, -1, -4), pos.offset(5, 4, 5)))
                .build();
    }

    private static LegacyMachineDefinition crystallizerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("acidizer"), machineTexture("acidizer"))
                .legacyXrDimensions(5, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 5, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.floorCorners(1), proxyInventoryPowerFluid()))
                .renderParts("Body", "Spinner")
                .renderProfile(LegacyMachineRenderProfile.CRYSTALLIZER_RUNNING_PARTS)
                .legacyItemScale(2.0F)
                .yRotation(ModBlocks::solidifierRotation)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 8, 3)))
                .build();
    }

    private static LegacyMachineDefinition electrolyserDefinition() {
        return LegacyMachineDefinition.builder(machineModel("electrolyser"), machineTexture("electrolyser"))
                .legacyXrDimensions(0, 0, 5, 5, 1, 3)
                .legacyOffset(5)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 5, 5, 1, 3 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 2, -1, 5, 5, 1, 1 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, -3, 5, 5, 0, 0 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, -1, 4, -4, -3, 3 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, -1, 2, -2, -3, 3 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, -1, 0, 0, -3, 3 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, -1, -2, 2, -3, 3 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, -1, -4, 4, -3, 3 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 0, 0, 0, 0, -1, 2 }, facing, electrolyserTopColumnOrigin(facing, 4))
                        .withLegacyXrCheckedFill(new int[] { 0, 0, 0, 0, -1, 2 }, facing, electrolyserTopColumnOrigin(facing, 2))
                        .withLegacyXrCheckedFill(new int[] { 0, 0, 0, 0, -1, 2 }, facing, electrolyserTopColumnOrigin(facing, 0))
                        .withLegacyXrCheckedFill(new int[] { 0, 0, 0, 0, -1, 2 }, facing, electrolyserTopColumnOrigin(facing, -2))
                        .withLegacyXrCheckedFill(new int[] { 0, 0, 0, 0, -1, 2 }, facing, electrolyserTopColumnOrigin(facing, -4))
                .withExtraProxyOffsets(electrolyserProxyOffsets(facing), proxyInventoryPowerFluid()))
                .legacyItemScale(2.5D, 0.5D)
                .yRotation(ModBlocks::pyroOvenRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-5, -3, -7), pos.offset(7, 5, 7)))
                .build();
    }

    private static LegacyMachineDefinition bigAssTankDefinition() {
        return LegacyMachineDefinition.builder(machineModel("bigasstank"), machineTexture("bigasstank"))
                .legacyXrDimensions(5, 0, 4, 4, 4, 4)
                .legacyOffset(6)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 5, 0, 4, 4, 4, 4 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 4, 0, 5, -4, 2, 2 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 4, 0, -4, 5, 2, 2 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 4, 0, 2, 2, 5, -4 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 4, 0, 2, 2, -4, 5 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, 0, 6, -5, 0, 0 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, 0, -5, 6, 0, 0 }, facing)
                        .withExtraProxyOffsets(bigAssTankProxyOffsets(facing), proxyFluid()))
                .legacyItemScale(2.5D, 0.5D)
                .yRotation(ModBlocks::bigAssTankRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-7, -1, -7), pos.offset(8, 7, 8)))
                .build();
    }

    private static LegacyMachineDefinition bat9000Definition() {
        return LegacyMachineDefinition.builder(machineModel("bat9000"), machineTexture("bat9000"))
                .legacyXrDimensions(4, 0, 2, 2, 1, 1)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 4, 0, 2, 2, 1, 1 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 4, 0, 1, 1, 2, -2 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 4, 0, 1, 1, -2, 2 }, facing)
                        .withExtraProxyOffsets(bat9000ProxyOffsets(), proxyFluid()))
                .legacyItemScale(2.0F)
                .yRotation(ModBlocks::bigAssTankRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-3, -1, -3), pos.offset(4, 6, 4)))
                .build();
    }

    private static LegacyMachineDefinition batteryReddDefinition() {
        return LegacyMachineDefinition.builder(machineModel("fensu2"), machineTexture("fensu2"))
                .legacyXrDimensions(9, 0, 2, 2, 4, 4)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 9, 0, 2, 2, 4, 4 }, facing)
                        .withExtraProxyOffsets(batteryReddProxyOffsets(facing), proxyPowerConductor()))
                .renderParts("Base", "Wheel", "Lights")
                .itemRenderParts("Base", "Wheel", "Lights")
                .legacyItemScale(2.5D, 0.5D)
                .yRotation(ModBlocks::batteryReddRotation)
                .renderProfile(LegacyMachineRenderProfile.BATTERY_REDD_STATIC_SPECIAL)
                .renderBoundingBox(pos -> new AABB(pos.offset(-5, 0, -5), pos.offset(6, 11, 6)))
                .build();
    }

    private static LegacyMachineDefinition fluidTankDefinition() {
        return LegacyMachineDefinition.builder(machineModel("fluidtank"), machineTexture("fluidtank"))
                .legacyXrDimensions(2, 0, 1, 1, 2, 2)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 2, 2 }, facing)
                .withExtraProxyOffsets(fluidTankProxyOffsets(facing), proxyFluid()))
                .legacyItemScale(3.5D, 0.75D)
                .yRotation(facing -> (360.0F - facing.toYRot()) % 360.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-6, -1, -6), pos.offset(7, 5, 7)))
                .build();
    }

    private static LegacyMachineDefinition pumpjackDefinition() {
        return LegacyMachineDefinition.builder(machineModel("pumpjack"), machineTexture("pumpjack"))
                .legacyXrDimensions(3, 0, 0, 0, 0, 6)
                .legacyOffset(0)
                .layout(facing -> {
                    Direction rot = facing.getCounterClockWise();
                    BlockPos offsetCore = new BlockPos(rot.getStepX() * 3, 0, rot.getStepZ() * 3);
                    return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 0, 0, 0, 6 }, facing)
                            .withLegacyXrFill(new int[] { 0, 0, -1, 1, 1, 1 }, facing, offsetCore)
                            .withLegacyXrFill(new int[] { 0, 0, 1, -1, 2, 2 }, facing, offsetCore)
                            .withExtraProxyOffsets(pumpjackCornerProxyOffsets(offsetCore), proxyPowerFluid())
                            .withLegacyXrCheckOnly(new int[] { 0, 0, -1, 1, -2, 4 }, facing, BlockPos.ZERO)
                            .withLegacyXrCheckOnly(new int[] { 0, 0, 1, -1, -1, 5 }, facing, BlockPos.ZERO);
                })
                .renderParts("Base", "Rotor", "Head", "Carriage")
                .legacyItemScale(4.0D, 0.5D)
                .yRotation(facing -> 270.0F - facing.toYRot())
                .renderBoundingBox(pos -> new AABB(pos.offset(-7, 0, -7), pos.offset(8, 6, 8)))
                .build();
    }

    private static LegacyMachineDefinition frackingTowerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("fracking_tower"), machineTexture("fracking_tower"))
                .legacyXrDimensions(3, 0, 0, 0, 0, 0)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 0, 0, 0, 0 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 1, 0, 3, 3, 3, 3 }, facing, new BlockPos(0, 2, 0))
                        .withLegacyXrCheckedFill(new int[] { -1, 2, 0, 1, 0, 1 }, Direction.NORTH, new BlockPos(-2, 2, -2))
                        .withLegacyXrCheckedFill(new int[] { -1, 2, 0, 1, 0, 1 }, Direction.NORTH, new BlockPos(-2, 2, 3))
                        .withLegacyXrCheckedFill(new int[] { -1, 2, 0, 1, 0, 1 }, Direction.NORTH, new BlockPos(3, 2, -2))
                        .withLegacyXrCheckedFill(new int[] { -1, 2, 0, 1, 0, 1 }, Direction.NORTH, new BlockPos(3, 2, 3))
                        .withLegacyXrCheckedFill(new int[] { 10, -4, 2, 2, 2, 2 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 24, -9, 1, 1, 1, 1 }, facing)
                        .withLegacyXrFill(new int[] { 1, 0, 1, 1, -2, 3 }, Direction.WEST, new BlockPos(0, 15, 0))
                        .withLegacyXrCheckOnly(new int[] { 1, 0, 1, 1, -2, 3 }, facing, new BlockPos(0, 15, 0)))
                .legacyItemScale(2.5D, 0.25D)
                .yRotation(facing -> 180.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-10, -9, -10), pos.offset(11, 26, 11)))
                .build();
    }

    private static LegacyMachineDefinition centrifugeDefinition() {
        return LegacyMachineDefinition.builder(machineModel("centrifuge"), machineTexture("centrifuge"))
                .legacyXrDimensions(3, 0, 0, 0, 0, 0)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 0, 0, 0, 0 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyPowerFluid()))
                .legacyItemScale(3.5F)
                .yRotation(ModBlocks::centrifugeRotation)
                .collisionShape(state -> legacyRotatedShape(state,
                        new AABB(-0.5D, 0.0D, -0.5D, 0.5D, 1.0D, 0.5D),
                        new AABB(-0.375D, 1.0D, -0.375D, 0.375D, 4.0D, 0.375D)))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 5, 2)))
                .build();
    }

    private static LegacyMachineDefinition gasCentDefinition() {
        return LegacyMachineDefinition.builder(machineModel("gascent"), machineTexture("gascent"))
                .legacyXrDimensions(3, 0, 0, 0, 0, 0)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 0, 0, 0, 0 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyPowerFluid()))
                .renderParts("Centrifuge", "Flag")
                .itemRenderParts("Centrifuge")
                .legacyItemScale(3.5F)
                .yRotation(facing -> normalizeRotation(centrifugeRotation(facing) + 180.0F))
                .collisionShape(state -> legacyRotatedShape(state,
                        new AABB(-0.5D, 0.0D, -0.5D, 0.5D, 1.0D, 0.5D),
                        new AABB(-0.4375D, 1.0D, -0.4375D, 0.4375D, 4.0D, 0.4375D)))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 5, 2)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition oreSlopperDefinition() {
        return LegacyMachineDefinition.builder(machineModel("ore_slopper"), machineTexture("ore_slopper"))
                .legacyXrDimensions(3, 0, 3, 3, 1, 1)
                .legacyOffset(3)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 3, 3, 1, 1 }, facing)
                        .withExtraProxyOffsets(oreSlopperProxyOffsets(facing), proxyInventoryPowerFluid()))
                .legacyItemScale(3.75D, 0.5D)
                .yRotation(ModBlocks::oreSlopperRotation)
                .collisionShape(state -> legacyRotatedShape(state,
                        new AABB(-3.5D, 0.0D, -1.5D, 3.5D, 1.0D, 1.5D),
                        new AABB(0.5D, 1.0D, -1.5D, 3.5D, 3.25D, 1.5D),
                        new AABB(-2.25D, 1.0D, -1.5D, 0.25D, 3.25D, -0.75D),
                        new AABB(-2.25D, 1.0D, 0.75D, 0.25D, 3.25D, 1.5D),
                        new AABB(-2.25D, 1.0D, -1.5D, -2.0D, 3.25D, 1.5D),
                        new AABB(0.0D, 1.0D, -1.5D, 0.25D, 3.25D, 1.5D),
                        new AABB(-2.0D, 1.0D, -0.75D, 0.0D, 2.0D, 0.75D),
                        new AABB(-3.25D, 1.0D, -1.0D, -2.25D, 3.0D, 1.0D)))
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, 0, -4), pos.offset(5, 7, 5)))
                .build();
    }

    private static LegacyMachineDefinition sawmillDefinition() {
        return LegacyMachineDefinition.builder(machineModel("sawmill"), machineTexture("sawmill"))
                .legacyXrDimensions(1, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(1), proxyInventory()))
                .renderParts("Main", "Blade", "GearLeft", "GearRight")
                .legacyItemScale(3.25F)
                .yRotation(ModBlocks::sawmillRotation)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .renderProfile(LegacyMachineRenderProfile.SAWMILL_RUNNING_PARTS)
                .collisionShape(state -> legacyRotatedShape(state,
                        new AABB(-1.5D, 0.0D, -1.5D, 1.5D, 1.0D, 1.5D),
                        new AABB(-1.25D, 1.0D, -0.5D, -0.625D, 1.875D, 0.5D),
                        new AABB(-0.625D, 1.0D, -1.0D, 1.375D, 2.0D, 1.0D)))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition crucibleDefinition() {
        return LegacyMachineDefinition.builder(machineModel("crucible"), machineTexture("crucible_heat"))
                .legacyXrDimensions(1, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 1, 1, 1, 1 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventory()))
                .legacyItemScale(3.25F)
                .yRotation(ModBlocks::crucibleRotation)
                .renderProfile(LegacyMachineRenderProfile.CRUCIBLE_MOLTEN)
                .collisionShape(state -> legacyRotatedShape(state,
                        new AABB(-1.5D, 0.0D, -1.5D, 1.5D, 0.5D, 1.5D),
                        new AABB(-1.25D, 0.5D, -1.25D, 1.25D, 1.5D, -1.0D),
                        new AABB(-1.25D, 0.5D, -1.25D, -1.0D, 1.5D, 1.25D),
                        new AABB(-1.25D, 0.5D, 1.0D, 1.25D, 1.5D, 1.25D),
                        new AABB(1.0D, 0.5D, -1.25D, 1.25D, 1.5D, 1.25D)))
                .highlightShape(state -> legacyUnrotatedShape(
                        new AABB(-1.5D, 0.0D, -1.5D, 1.5D, 0.5D, 1.5D),
                        new AABB(-1.25D, 0.5D, -1.25D, 1.25D, 1.5D, -1.0D),
                        new AABB(-1.25D, 0.5D, -1.25D, -1.0D, 1.5D, 1.25D),
                        new AABB(-1.25D, 0.5D, 1.0D, 1.25D, 1.5D, 1.25D),
                        new AABB(1.0D, 0.5D, -1.25D, 1.25D, 1.5D, 1.25D)))
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(2, 3, 2)))
                .particleState(legacyBlockParticleState("brick_fire"))
                .build();
    }

    private static LegacyMachineDefinition gasFlareDefinition() {
        return LegacyMachineDefinition.builder(machineModel("flare_stack"), machineTexture("flare_stack"))
                .legacyXrDimensions(11, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 11, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(1), proxyPowerFluid()))
                .legacyItemScale(2.25D, 0.5D)
                .yRotation(facing -> 180.0F)
                .collisionShape(state -> legacyRotatedShape(state,
                        new AABB(-1.5D, 0.0D, -1.5D, 1.5D, 3.875D, 1.5D),
                        new AABB(-0.75D, 3.875D, -0.75D, 0.75D, 9.0D, 0.75D),
                        new AABB(-1.5D, 9.0D, -1.5D, 1.5D, 9.375D, 1.5D),
                        new AABB(-0.75D, 9.375D, -0.75D, 0.75D, 12.0D, 0.75D)))
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 13, 3)))
                .renderProfile(LegacyMachineRenderProfile.GAS_FLARE_TILTED_STATE)
                .build();
    }

    private static LegacyMachineDefinition chimneyBrickDefinition() {
        return LegacyMachineDefinition.builder(machineModel("chimney_brick"), machineTexture("chimney_brick"))
                .legacyXrDimensions(12, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 12, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(1), proxyFluid()))
                .legacyItemScale(2.25D, 0.5D)
                .yRotation(facing -> 180.0F)
                .particleState(legacyBlockParticleState("brick_fire"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 14, 3)))
                .build();
    }

    private static LegacyMachineDefinition chimneyIndustrialDefinition() {
        return LegacyMachineDefinition.builder(machineModel("chimney_industrial"), machineTexture("chimney_industrial"))
                .legacyXrDimensions(22, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 22, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(1), proxyFluid()))
                .legacyItemScale(2.75D, 0.25D)
                .yRotation(facing -> 180.0F)
                .particleState(legacyBlockParticleState("concrete"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 24, 3)))
                .build();
    }

    private static LegacyMachineDefinition intakeDefinition() {
        return LegacyMachineDefinition.builder(machineModel("intake"), machineTexture("intake"))
                .legacyXrDimensions(0, 0, 1, 0, 1, 0)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 1, 0, 1, 0 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyPowerFluid()))
                .renderParts("Base", "Fan")
                .renderProfile(LegacyMachineRenderProfile.INTAKE_FAN)
                .legacyItemScale(5.0D, 1.0D)
                .yRotation(ModBlocks::solidifierRotation)
                .modelTranslation(-0.5D, 0.0D, 0.5D)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 1, 2)))
                .build();
    }

    private static LegacyMachineDefinition drainDefinition() {
        return LegacyMachineDefinition.builder(machineModel("drain"), machineTexture("drain"))
                .legacyXrDimensions(0, 0, 2, 0, 0, 0)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 2, 0, 0, 0 }, facing))
                .legacyItemScale(5.0D, 1.0D)
                .yRotation(ModBlocks::solidifierRotation)
                .particleState(legacyBlockParticleState("concrete"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-3, 0, -3), pos.offset(3, 2, 3)))
                .build();
    }

    private static LegacyMachineDefinition chungusDefinition() {
        return LegacyMachineDefinition.builder(machineModel("chungus"), machineTexture("chungus"))
                .legacyXrDimensions(3, 0, 0, 3, 2, 2)
                .legacyOffset(3)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 0, 3, 2, 2 }, facing)
                        .withLegacyXrFill(new int[] { 4, -4, 0, 3, 1, 1 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 3, 0, 6, -1, 1, 1 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 2, 0, 10, -7, 1, 1 }, facing)
                        .withExtraProxyOffsets(chungusProxyOffsets(facing), proxyPowerFluid()))
                .renderParts("Body", "Lever", "Blades")
                .legacyItemScale(2.5D, 0.5D)
                .modelTranslation(0.0D, 0.0D, -3.0D)
                .yRotation(facing -> normalizeRotation(eastZeroRotation(facing) + 90.0F))
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-11, -4, -11), pos.offset(12, 6, 12)))
                .build();
    }

    private static LegacyMachineDefinition hephaestusDefinition() {
        return LegacyMachineDefinition.builder(machineModel("hephaestus"), machineTexture("hephaestus"))
                .legacyXrDimensions(11, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 11, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(hephaestusProxyOffsets(), proxyFluid()))
                .renderParts("Main", "Rotor", "Core")
                .legacyItemScale(2.25D, 0.5D)
                .yRotation(facing -> 0.0F)
                .renderProfile(LegacyMachineRenderProfile.HEPHAESTUS_RUNNING_CORE)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-3, 0, -3), pos.offset(4, 13, 4)))
                .build();
    }

    private static LegacyMachineDefinition heatBoilerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("boiler"), machineTexture("boiler"))
                .legacyXrDimensions(3, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(heatBoilerProxyOffsets(facing), proxyFluid()))
                .renderParts("Plane")
                .legacyItemScale(3.0F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderProfile(LegacyMachineRenderProfile.HEAT_BOILER)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 4, 2)))
                .build();
    }

    private static LegacyMachineDefinition industrialBoilerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("industrial_boiler"), machineTexture("industrial_boiler"))
                .legacyXrDimensions(4, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 4, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(industrialBoilerProxyOffsets(facing), proxyFluid()))
                .legacyItemScale(2.5F)
                .yRotation(facing -> 0.0F)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 5, 2)))
                .build();
    }

    private static LegacyMachineDefinition combustionEngineDefinition() {
        return LegacyMachineDefinition.builder(machineModel("combustion_engine"), machineTexture("combustion_engine"))
                .legacyXrDimensions(1, 0, 1, 0, 3, 2)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 1, 0, 3, 2 }, facing)
                        .withExtraProxyOffsets(combustionEngineProxyOffsets(facing), proxyPowerFluid()))
                .renderParts("Engine", "Canister", "Hatch")
                .renderProfile(LegacyMachineRenderProfile.COMBUSTION_ENGINE_DOOR_CANISTER)
                .legacyItemScale(2.75F)
                .modelTranslation(-0.5D, 0.0D, 3.0D)
                .yRotation(ModBlocks::eastZeroRotation)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-3, 0, -3), pos.offset(4, 2, 4)))
                .build();
    }

    private static LegacyMachineDefinition dieselGeneratorDefinition() {
        return LegacyMachineDefinition.builder(machineModel("dieselgen"), machineTexture("dieselgen"))
                .renderParts("Generator", "Engine")
                .renderProfile(LegacyMachineRenderProfile.DIESEL_GENERATOR_RUNNING_PARTS)
                .legacyItemScale(5.0F)
                .yRotation(ModBlocks::eastZeroRotation)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 2, 2)))
                .build();
    }

    private static LegacyMachineDefinition pumpDefinition(String textureName, LegacyProxyMode extraProxyMode,
            String particleBlock) {
        return pumpDefinition(textureName, textureName, extraProxyMode, particleBlock);
    }

    private static LegacyMachineDefinition pumpDefinition(String modelName, String textureName,
            LegacyProxyMode extraProxyMode, String particleBlock) {
        return LegacyMachineDefinition.builder(machineModel(modelName), machineTexture(textureName))
                .legacyXrDimensions(3, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(1), extraProxyMode))
                .renderParts("Base", "Rotor", "Arms", "Piston")
                .legacyItemScale(2.5F)
                .yRotation(ModBlocks::pumpRotation)
                .renderProfile(LegacyMachineRenderProfile.PUMP_RUNNING_PARTS)
                .particleState(legacyBlockParticleState(particleBlock))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 5, 2)))
                .build();
    }

    private static LegacyMachineDefinition heaterHeatexDefinition() {
        return LegacyMachineDefinition.builder(machineModel("heatex"), machineTexture("heater_heatex"))
                .legacyXrDimensions(0, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.floorCorners(1), proxyFluid()))
                .legacyItemScale(3.25F)
                .yRotation(ModBlocks::eastZeroRotation)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 1, 2)))
                .build();
    }

    private static LegacyMachineDefinition heaterFireboxDefinition() {
        return LegacyMachineDefinition.builder(machineModel("firebox"), machineTexture("firebox"))
                .legacyXrDimensions(0, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 1, 1, 1, 1 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventoryFluid()))
                .renderParts("Main", "Door", "InnerEmpty")
                .itemRenderParts("Main", "Door")
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .partRenderProperty("InnerBurning", LegacyMachinePartRenderProperties
                        .mode(LegacyMachinePartRenderMode.CUTOUT_NO_CULL).asFullBright())
                .legacyItemScale(3.25F)
                .yRotation(ModBlocks::eastZeroRotation)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderProfile(LegacyMachineRenderProfile.FIREBOX_HEATER)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 1, 2)))
                .build();
    }

    private static LegacyMachineDefinition heaterOvenDefinition() {
        return LegacyMachineDefinition.builder(machineModel("heating_oven"), machineTexture("heating_oven"))
                .legacyXrDimensions(0, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 1, 1, 1, 1 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventoryFluid()))
                .renderParts("Main", "Door", "Inner")
                .itemRenderParts("Main", "Door")
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .partRenderProperty("InnerBurning", LegacyMachinePartRenderProperties
                        .mode(LegacyMachinePartRenderMode.CUTOUT_NO_CULL).asFullBright())
                .legacyItemScale(3.25F)
                .yRotation(ModBlocks::eastZeroRotation)
                .particleState(legacyBlockParticleState("brick_fire"))
                .renderProfile(LegacyMachineRenderProfile.FIREBOX_HEATER)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 1, 2)))
                .build();
    }

    private static LegacyMachineDefinition ashpitDefinition() {
        return LegacyMachineDefinition.builder(machineModel("heating_oven"), machineTexture("ashpit"))
                .legacyXrDimensions(0, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 1, 1, 1, 1 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventory()))
                .renderParts("Main", "Door", "Inner")
                .itemRenderParts("Main", "Door")
                .legacyItemScale(3.25F)
                .yRotation(ModBlocks::eastZeroRotation)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .renderProfile(LegacyMachineRenderProfile.ASHPIT_DOOR_INNER)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 1, 2)))
                .build();
    }

    private static LegacyMachineDefinition heaterOilburnerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("oilburner"), machineTexture("oilburner"))
                .legacyXrDimensions(1, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 1, 1, 1, 1 }, facing)
                        .withExtraOffsets(heaterOilburnerExtraOffsets(),
                                (Function<BlockPos, LegacyProxyMode>) ModBlocks::heaterOilburnerProxyMode)
                        .withLegacyExtraOffsets(heaterOilburnerExtraOffsets()))
                .legacyItemScale(3.25F)
                .yRotation(facing -> 0.0F)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 2, 2)))
                .build();
    }

    private static LegacyMachineDefinition heaterElectricDefinition() {
        return LegacyMachineDefinition.builder(machineModel("electric_heater"), machineTexture("electric_heater"))
                .legacyXrDimensions(0, 0, 1, 2, 1, 1)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 1, 2, 1, 1 }, facing)
                        .withLegacyExtraProxyOffsets(
                                List.of(LegacyMultiblockOffsets.relative(facing, 2, 0, 0)),
                                proxyPower()))
                .legacyItemScale(3.0F)
                .yRotation(ModBlocks::eastZeroRotation)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 1, 3)))
                .build();
    }

    private static LegacyMachineDefinition condenserPoweredDefinition() {
        return LegacyMachineDefinition.builder(machineModel("condenser"), machineTexture("condenser"))
                .legacyXrDimensions(2, 0, 1, 1, 3, 3)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 3, 3 }, facing)
                        .withExtraProxyOffsets(condenserPoweredProxyOffsets(facing), proxyPowerFluid()))
                .renderParts("Condenser", "Fan1", "Fan2")
                .renderProfile(LegacyMachineRenderProfile.POWERED_CONDENSER_FANS)
                .legacyItemScale(2.75D, 0.75D)
                .yRotation(ModBlocks::eastZeroRotation)
                .particleState(legacyBlockParticleState("block_steel_machine"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, -1, -4), pos.offset(5, 4, 5)))
                .build();
    }

    private static LegacyMachineDefinition assemblyFactoryDefinition() {
        return LegacyMachineDefinition.builder(machineModel("assembly_factory"), machineTexture("assembly_factory"))
                .legacyXrDimensions(2, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 2, 2, 2, 2 }, facing)
                        .withExtraProxyOffsets(assemblyFactoryProxyOffsets(facing), proxyInventoryPowerFluid()))
                .renderParts("Base", "Frame", "Slider1", "Slider2", "Slider3", "Slider4",
                        "ArmLower1", "ArmLower2", "ArmLower3", "ArmLower4",
                        "ArmUpper1", "ArmUpper2", "ArmUpper3", "ArmUpper4",
                        "Head1", "Head2", "Head3", "Head4",
                        "Striker1", "Striker2", "Striker3", "Striker4",
                        "Blade2", "Blade4")
                .legacyItemScale(3.0D, 0.75D)
                .yRotation(ModBlocks::assemblyFactoryRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 3, 3)))
                .build();
    }

    private static LegacyMachineDefinition precassDefinition() {
        return LegacyMachineDefinition.builder(machineModel("assembly_machine"),
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/precass.png"))
                .legacyXrDimensions(2, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.squarePerimeter(1),
                                proxyInventoryPowerFluid()))
                .renderParts("Base", "Frame", "Ring", "Ring2",
                        "ArmLower1", "ArmLower2", "ArmUpper1", "ArmUpper2",
                        "Head1", "Head2", "Spike1", "Spike2")
                .renderProfile(LegacyMachineRenderProfile.PRECASS_RUNNING_PARTS)
                .legacyItemScale(4.5D, 0.75D)
                .particleState(legacyBlockParticleState("block_steel"))
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2)))
                .build();
    }

    private static LegacyMachineDefinition purexDefinition() {
        return LegacyMachineDefinition.builder(machineModel("purex"), machineTexture("purex"))
                .legacyXrDimensions(4, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 4, 0, 2, 2, 2, 2 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.squarePerimeter(2), proxyInventoryPowerFluid()))
                .renderParts("Base", "Frame", "Fan", "Pump")
                .renderProfile(LegacyMachineRenderProfile.PUREX_RUNNING_PARTS)
                .legacyItemScale(2.5D, 0.75D)
                .yRotation(ModBlocks::assemblyFactoryRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 5, 3)))
                .build();
    }

    private static LegacyMachineDefinition silexDefinition() {
        return LegacyMachineDefinition.builder(machineModel("silex"), machineTexture("silex"))
                .legacyXrDimensions(2, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(silexProxyOffsets(facing), proxyInventoryFluid()))
                .legacyItemScale(3.25F)
                .yRotation(ModBlocks::solidifierRotation)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2)))
                .build();
    }

    private static LegacyMachineDefinition exposureChamberDefinition() {
        return LegacyMachineDefinition.builder(machineModel("exposure_chamber"), machineTexture("exposure_chamber"))
                .legacyXrDimensions(4, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .layout(facing -> {
                    Direction rot = facing.getCounterClockWise();
                    BlockPos lateralOrigin = new BlockPos(rot.getStepX() * 7, 0, rot.getStepZ() * 7);
                    return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 4, 0, 2, 2, 2, 2 }, facing)
                            .withLegacyXrFill(new int[] { 3, 0, 0, 0, -3, 8 }, facing)
                            .withLegacyXrCheckOnly(new int[] { 3, 0, 0, 0, -3, 8 }, facing, BlockPos.ZERO)
                            .withLegacyXrFill(new int[] { 0, 0, 1, -1, -3, 6 }, facing, new BlockPos(0, 2, 0))
                            .withLegacyXrCheckOnly(new int[] { 0, 0, 1, -1, -3, 6 }, facing, BlockPos.ZERO)
                            .withLegacyXrFill(new int[] { 0, 0, -1, 1, -3, 6 }, facing, new BlockPos(0, 2, 0))
                            .withLegacyXrCheckOnly(new int[] { 0, 0, -1, 1, -3, 6 }, facing, BlockPos.ZERO)
                            .withLegacyXrFill(new int[] { 3, 0, 1, -1, 0, 1 }, facing, lateralOrigin)
                            .withLegacyXrCheckOnly(new int[] { 3, 0, 1, -1, 0, 1 }, facing, lateralOrigin)
                            .withLegacyXrFill(new int[] { 3, 0, -1, 1, 0, 1 }, facing, lateralOrigin)
                            .withLegacyXrCheckOnly(new int[] { 3, 0, -1, 1, 0, 1 }, facing, lateralOrigin)
                            .withExtraProxyOffsets(exposureChamberProxyOffsets(facing, rot), proxyInventoryPower());
                })
                .renderParts("Chamber", "Magnets", "Core")
                .legacyItemScale(3.0D, 0.5D)
                .yRotation(ModBlocks::solidifierRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 5, 9)))
                .build();
    }

    private static LegacyMachineDefinition cyclotronDefinition() {
        return LegacyMachineDefinition.builder(machineModel("cyclotron"), machineTexture("cyclotron"))
                .legacyXrDimensions(2, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 2, 2, 2, 2 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.squareSidesWithoutCorners(2), proxyInventoryPowerFluid()))
                .renderParts("Body", "B1", "B2", "B3", "B4")
                .legacyItemScale(2.25F)
                .yRotation(facing -> 0.0F)
                .renderProfile(LegacyMachineRenderProfile.CYCLOTRON_PLUGS)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 4, 3)))
                .build();
    }

    private static LegacyMachineDefinition arcWelderDefinition() {
        return LegacyMachineDefinition.builder(machineModel("arc_welder"), machineTexture("arc_welder"))
                .legacyXrDimensions(1, 0, 1, 0, 1, 1)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 1, 0, 1, 1 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventoryPowerFluid()))
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::solidifierRotation)
                .modelTranslation(-0.5D, 0.0D, 0.0D)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .renderProfile(LegacyMachineRenderProfile.ARC_WELDER_DISPLAY_OUTPUT)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(2, 4, 2)))
                .build();
    }

    private static LegacyMachineDefinition solderingStationDefinition() {
        return LegacyMachineDefinition.builder(machineModel("soldering_station"), machineTexture("soldering_station"))
                .legacyXrDimensions(0, 0, 1, 0, 1, 0)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 1, 0, 1, 0 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventoryPowerFluid()))
                .legacyItemScale(5.0F)
                .yRotation(ModBlocks::solidifierRotation)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .modelTranslation(-0.5D, 0.0D, 0.5D)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 2, 2)))
                .build();
    }

    private static LegacyMachineDefinition mixerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("mixer"), machineTexture("mixer"))
                .legacyXrDimensions(2, 0, 0, 0, 0, 0)
                .legacyOffset(0)
                .renderParts("Main", "Mixer")
                .legacyItemScale(5.0F)
                .yRotation(facing -> 0.0F)
                .renderProfile(LegacyMachineRenderProfile.MIXER_RUNNING_PARTS)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2)))
                .build();
    }

    private static LegacyMachineDefinition radiolysisDefinition() {
        return LegacyMachineDefinition.builder(machineModel("radiolysis"), machineTexture("radiolysis"))
                .legacyXrDimensions(2, 0, 1, 1, 1, 1)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(1), proxyInventoryPowerFluid()))
                .legacyItemScale(3.0F)
                .yRotation(ModBlocks::radiolysisRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2)))
                .build();
    }

    private static LegacyMachineDefinition rtgDefinition() {
        return LegacyMachineDefinition.builder(machineModel("rtg"), machineTexture("rtg"))
                .legacyXrDimensions(0, 0, 0, 0, 0, 0)
                .legacyOffset(0)
                .renderParts("Gen")
                .renderProfile(LegacyMachineRenderProfile.RTG_CONNECTORS)
                .legacyItemScale(2.25F)
                .yRotation(facing -> 180.0F)
                .renderBoundingBox(pos -> new AABB(pos, pos.offset(1, 1, 1)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition radGenDefinition() {
        return LegacyMachineDefinition.builder(machineModel("radgen"), machineTexture("radgen"))
                .legacyXrDimensions(2, 0, 3, 2, 1, 1)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 3, 2, 1, 1 }, facing)
                        .withExtraProxyOffsets(radGenProxyOffsets(facing), proxyInventoryPower()))
                .renderParts("Base", "Rotor", "Light", "Glass")
                .translucentPart("Glass", 0x80BFFF, 77)
                .legacyItemScale(4.5D, 0.5D)
                .yRotation(ModBlocks::solidifierRotation)
                .renderProfile(LegacyMachineRenderProfile.RADGEN_STATIC_SPECIAL)
                .renderBoundingBox(pos -> new AABB(pos.offset(-5, 0, -3), pos.offset(5, 4, 3)))
                .build();
    }

    private static LegacyMachineDefinition researchReactorDefinition() {
        return LegacyMachineDefinition.builder(
                        new ResourceLocation(HbmNtm.MOD_ID, "models/reactors/reactor_small_base.obj"),
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/reactor_small_base.png"))
                .legacyXrDimensions(2, 0, 0, 0, 0, 0)
                .legacyOffset(0)
                .legacyItemScale(4.0F)
                .yRotation(facing -> 180.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, -1, -1), pos.offset(2, 5, 2)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition breedingReactorDefinition() {
        return LegacyMachineDefinition.builder(
                        new ResourceLocation(HbmNtm.MOD_ID, "models/reactors/breeder.obj"),
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/breeder.png"))
                .legacyXrDimensions(2, 0, 0, 0, 0, 0)
                .legacyOffset(0)
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::solidifierRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 4, 2)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition zirnoxDefinition() {
        return LegacyMachineDefinition.builder(
                        new ResourceLocation(HbmNtm.MOD_ID, "models/zirnox.obj"),
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/zirnox.png"))
                .legacyXrDimensions(1, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::solidifierRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-3, 0, -3), pos.offset(4, 5, 4)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition zirnoxReactorDefinition() {
        return LegacyMachineDefinition.builder(
                        new ResourceLocation(HbmNtm.MOD_ID, "models/zirnox.obj"),
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/zirnox.png"))
                .legacyXrDimensions(1, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .layout(ModBlocks::zirnoxLayout)
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::solidifierRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-3, 0, -3), pos.offset(4, 5, 4)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition zirnoxDestroyedDefinition() {
        return LegacyMachineDefinition.builder(
                        new ResourceLocation(HbmNtm.MOD_ID, "models/zirnox_destroyed.obj"),
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/zirnox_destroyed.png"))
                .legacyXrDimensions(1, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .layout(ModBlocks::zirnoxDestroyedLayout)
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::solidifierRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, 0, -4), pos.offset(5, 4, 5)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition watzDefinition() {
        return LegacyMachineDefinition.builder(
                        new ResourceLocation(HbmNtm.MOD_ID, "models/reactors/watz.obj"),
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/watz.png"))
                .legacyXrDimensions(2, 0, 3, 3, 1, 1)
                .legacyOffset(3)
                .layout(ModBlocks::watzLayout)
                .legacyItemScale(4.0F)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-3, 0, -3), pos.offset(4, 3, 4)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition watzPumpDefinition() {
        return LegacyMachineDefinition.builder(
                        new ResourceLocation(HbmNtm.MOD_ID, "models/machines/watz_pump.obj"),
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/watz_pump.png"))
                .legacyXrDimensions(1, 0, 0, 0, 0, 0)
                .legacyOffset(0)
                .legacyItemScale(5.0F)
                .legacyInventoryTranslation(0.0D, -1.5D, 0.0D)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 2, 2)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition icfDefinition() {
        return LegacyMachineDefinition.builder(
                        new ResourceLocation(HbmNtm.MOD_ID, "models/reactors/icf.obj"),
                        new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/icf.png"))
                .legacyXrDimensions(5, 0, 1, 1, 8, 8)
                .legacyOffset(1)
                .layout(ICFStructCoreBlockEntity::icfLayout)
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::eastZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-8, 0, -8), pos.offset(9, 7, 9)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition fusionTorusDefinition() {
        return fusionDefinition("fusion_torus", "torus")
                .legacyXrDimensions(4, 0, 7, 7, 7, 7)
                .legacyOffset(7)
                .layout(facing -> FusionTorusStructCoreBlockEntity.torusLayout())
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-8, 0, -8), pos.offset(9, 5, 9)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition fusionKlystronDefinition() {
        return fusionDefinition("fusion_klystron", "klystron")
                .legacyXrDimensions(3, 0, 4, 3, 2, 2)
                .legacyOffset(3)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 4, 3, 2, 2 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 4, -3, 4, 3, 1, 1 }, facing)
                        .withExtraProxyOffsets(fusionKlystronProxyOffsets(facing), proxyPowerFluid()))
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, 0, -4), pos.offset(5, 5, 5)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition fusionKlystronCreativeDefinition() {
        return fusionDefinition("fusion_klystron", "klystron_creative")
                .legacyXrDimensions(3, 0, 4, 3, 2, 2)
                .legacyOffset(3)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 4, 3, 2, 2 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 4, -3, 4, 3, 1, 1 }, facing))
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, 0, -4), pos.offset(5, 5, 5)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition fusionBreederDefinition() {
        return fusionDefinition("fusion_breeder", "breeder")
                .legacyXrDimensions(3, 0, 2, 2, 1, 1)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 2, 2, 1, 1 }, facing)
                        .withExtraProxyOffsets(fusionBreederProxyOffsets(facing), proxyInventoryFluid()))
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 4, 3)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition fusionCollectorDefinition() {
        return fusionDefinition("fusion_collector", "collector")
                .legacyXrDimensions(3, 0, 2, 1, 2, 2)
                .legacyOffset(1)
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 4, 3)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition fusionBoilerDefinition() {
        return fusionDefinition("fusion_boiler", "boiler")
                .legacyXrDimensions(3, 0, 4, 4, 1, 1)
                .legacyOffset(4)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 4, 4, 1, 1 }, facing)
                        .withExtraProxyOffsets(fusionBoilerProxyOffsets(facing), proxyFluid()))
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, 0, -4), pos.offset(5, 4, 5)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition fusionCouplerDefinition() {
        return fusionDefinition("fusion_coupler", "coupler")
                .legacyXrDimensions(3, 0, 1, 1, 1, 1)
                .legacyOffset(0)
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 4, 2)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition fusionMhdtDefinition() {
        return fusionDefinition("fusion_mhdt", "mhdt")
                .legacyXrDimensions(2, 0, 6, 7, 2, 2)
                .legacyOffset(7)
                .layout(ModBlocks::fusionMhdtLayout)
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-7, 0, -7), pos.offset(8, 4, 8)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMultiblockLayout fusionMhdtLayout(Direction facing) {
        return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 6, 7, 2, 2 }, facing)
                .withLegacyXrCheckedFill(new int[] { 3, -2, 6, 2, 1, 1 }, facing)
                .withLegacyXrCheckedFill(new int[] { 3, -2, -6, 7, 1, 1 }, facing)
                .withLegacyXrCheckedFill(new int[] { 3, -2, -3, 5, 2, 2 }, facing)
                .withLegacyXrCheckedFill(new int[] { 4, -3, -3, 5, 1, 1 }, facing)
                .withLegacyXrCheckedFill(new int[] { 1, 0, 0, 1, 3, 3 }, facing,
                        LegacyMultiblockOffsets.relative(facing, 3, 0))
                .withExtraProxyOffsets(fusionMhdtProxyOffsets(facing), proxyPowerFluid());
    }

    private static LegacyMachineDefinition fusionPlasmaForgeDefinition() {
        return fusionDefinition("fusion_plasma_forge", "plasma_forge")
                .legacyXrDimensions(2, 0, 2, 2, 5, 5)
                .legacyOffset(5)
                .layout(ModBlocks::fusionPlasmaForgeLayout)
                .legacyItemScale(4.0F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-5, 0, -5), pos.offset(5, 6, 6)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMultiblockLayout fusionPlasmaForgeLayout(Direction facing) {
        return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 2, 2, 5, 5 }, facing)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 3, -2, 4, 4 }, facing, BlockPos.ZERO)
                .withLegacyXrCheckedFill(new int[] { 2, 0, -2, 3, 4, 4 }, facing, BlockPos.ZERO)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 4, -3, 3, 3 }, facing, BlockPos.ZERO)
                .withLegacyXrCheckedFill(new int[] { 2, 0, -3, 4, 3, 3 }, facing, BlockPos.ZERO)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 5, -4, 2, 2 }, facing, BlockPos.ZERO)
                .withLegacyXrCheckedFill(new int[] { 2, 0, -4, 5, 2, 2 }, facing, BlockPos.ZERO)
                .withLegacyXrCheckedFill(new int[] { 3, -2, 1, 1, 5, 5 }, facing, BlockPos.ZERO)
                .withLegacyXrCheckedFill(new int[] { 4, -3, 0, 0, 4, 4 }, facing, BlockPos.ZERO)
                .withExtraProxyOffsets(fusionPlasmaForgeProxyOffsets(facing), proxyInventoryPowerFluid());
    }

    private static LegacyMachineDefinition.Builder fusionDefinition(String modelName, String textureName) {
        String legacyModelName = modelName.startsWith("fusion_") ? modelName.substring("fusion_".length()) : modelName;
        return LegacyMachineDefinition.builder(
                new ResourceLocation(HbmNtm.MOD_ID, "models/fusion/" + legacyModelName + ".obj"),
                new ResourceLocation(HbmNtm.MOD_ID, "textures/models/fusion/" + textureName + ".png"))
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL);
    }

    private static LegacyMachineDefinition rotaryFurnaceDefinition() {
        return LegacyMachineDefinition.builder(machineModel("rotary_furnace"), machineTexture("rotary_furnace"))
                .legacyXrDimensions(4, 0, 1, 1, 2, 2)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 4, 0, 1, 1, 2, 2 }, facing)
                        .withExtraProxyOffsets(rotaryFurnaceProxyOffsets(facing), proxyInventoryFluid()))
                .renderParts("Furnace", "Piston")
                .legacyItemScale(3.5D, 0.625D)
                .yRotation(ModBlocks::solidifierRotation)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .renderProfile(LegacyMachineRenderProfile.ROTARY_FURNACE_PISTON)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, 0, -4), pos.offset(4, 6, 4)))
                .build();
    }

    private static LegacyMachineDefinition steamEngineDefinition() {
        return LegacyMachineDefinition.builder(machineModel("steam_engine"), machineTexture("steam_engine"))
                .legacyXrDimensions(1, 0, 5, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 5, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(steamEngineProxyOffsets(facing), proxyPowerFluid()))
                .renderParts("Base", "Flywheel", "Shaft", "Transmission", "Piston")
                .legacyItemScale(2.0F)
                .yRotation(ModBlocks::steamEngineRotation)
                .modelTranslation(2.0D, 0.0D, 0.0D)
                .renderProfile(LegacyMachineRenderProfile.STEAM_ENGINE_ITEM_PREVIEW)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(6, 3, 3)))
                .build();
    }

    private static LegacyMachineDefinition solarBoilerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("solar_boiler"), machineTexture("solar_boiler"))
                .legacyXrDimensions(2, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(List.of(new BlockPos(0, 2, 0)), proxyFluid()))
                .renderParts("Base")
                .legacyItemScale(3.25F)
                .yRotation(ModBlocks::solidifierRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 3, 3)))
                .build();
    }

    private static LegacyMachineDefinition towerSmallDefinition() {
        return LegacyMachineDefinition.builder(machineModel("tower_small"), machineTexture("tower_small"))
                .legacyXrDimensions(18, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 18, 0, 2, 2, 2, 2 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(2), proxyFluid()))
                .legacyItemScale(3.0D, 0.25D)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 20, 3)))
                .build();
    }

    private static LegacyMachineDefinition towerLargeDefinition() {
        return LegacyMachineDefinition.builder(machineModel("tower_large"), machineTexture("tower_large"))
                .legacyXrDimensions(12, 0, 4, 4, 4, 4)
                .legacyOffset(4)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 12, 0, 4, 4, 4, 4 }, facing)
                        .withExtraProxyOffsets(towerLargeProxyOffsets(), proxyFluid()))
                .legacyItemScale(3.8D, 0.25D)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, 0, -4), pos.offset(5, 13, 5)))
                .build();
    }

    private static LegacyMachineDefinition turbofanDefinition() {
        return LegacyMachineDefinition.builder(machineModel("turbofan"), machineTexture("turbofan"))
                .legacyXrDimensions(2, 0, 1, 1, 3, 3)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 3, 3 }, facing)
                        .withExtraProxyOffsets(turbofanProxyOffsets(facing), proxyPowerFluid()))
                .renderParts("Body", "Blades", "Afterburner")
                .partTextures(Map.of("Afterburner", machineTexture("turbofan_back")))
                .itemPartTextures(Map.of("Afterburner", machineTexture("turbofan_back")))
                .legacyItemScale(2.25F)
                .yRotation(ModBlocks::solidifierRotation)
                .renderProfile(LegacyMachineRenderProfile.TURBOFAN_ITEM_PREVIEW)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, 0, -4), pos.offset(5, 3, 5)))
                .build();
    }

    private static LegacyMachineDefinition turbineGasDefinition() {
        return LegacyMachineDefinition.builder(machineModel("turbinegas"), machineTexture("turbinegas"))
                .legacyXrDimensions(2, 0, 1, 1, 4, 5)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 1, 1, 4, 5 }, facing)
                        .withExtraProxyOffsets(turbineGasProxyOffsets(facing), proxyPowerFluid()))
                .legacyItemScale(2.5D, 0.75D)
                .yRotation(ModBlocks::solidifierRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-5, 0, -5), pos.offset(6, 3, 6)))
                .build();
    }

    private static LegacyMachineDefinition ammoPressDefinition() {
        return LegacyMachineDefinition.builder(machineModel("ammo_press"), machineTexture("ammo_press"))
                .legacyXrDimensions(1, 0, 0, 0, 1, 1)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 0, 0, 1, 1 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventory()))
                .renderParts("Frame", "Press", "Shells", "Bullets")
                .legacyItemScale(5.0F)
                .yRotation(ModBlocks::eastZeroRotation)
                .renderProfile(LegacyMachineRenderProfile.AMMO_PRESS_RUNNING_PARTS)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(2, 2, 2)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition furnaceIronDefinition() {
        return LegacyMachineDefinition.builder(machineModel("furnace_iron"), machineTexture("furnace_iron"))
                .legacyXrDimensions(1, 0, 1, 0, 1, 0)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 1, 0, 1, 0 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventory()))
                .renderParts("Main", "Off")
                .legacyItemScale(5.0F)
                .modelTranslation(-0.5D, 0.0D, -0.5D)
                .yRotation(ModBlocks::southZeroRotation)
                .renderProfile(LegacyMachineRenderProfile.FURNACE_IRON_BURN_STATE)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(2, 2, 2)))
                .particleState(legacyBlockParticleState("block_aluminium"))
                .build();
    }

    private static LegacyMachineDefinition furnaceSteelDefinition() {
        return LegacyMachineDefinition.builder(machineModel("furnace_steel"), machineTexture("furnace_steel"))
                .legacyXrDimensions(1, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 1, 1, 1, 1 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventory()))
                .legacyItemScale(3.25F)
                .yRotation(facing -> southZeroRotation(facing) - 90.0F)
                .renderProfile(LegacyMachineRenderProfile.FURNACE_STEEL_FIRE)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 3, 2)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition furnaceCombinationDefinition() {
        return LegacyMachineDefinition.builder(machineModel("combination_oven"), machineTexture("combination_oven"))
                .legacyXrDimensions(1, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 1, 1, 1, 1 }, facing)
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventoryFluid()))
                .legacyItemScale(3.25F)
                .yRotation(facing -> 0.0F)
                .renderProfile(LegacyMachineRenderProfile.COMBINATION_OVEN_FIRE)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 5, 3)))
                .particleState(legacyBlockParticleState("brick_light_alt"))
                .build();
    }

    private static LegacyMachineDefinition blastFurnaceDefinition() {
        return LegacyMachineDefinition.builder(machineModel("blast_furnace"), machineTexture("blast_furnace"))
                .legacyXrDimensions(6, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> {
                    return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 6, 0, 1, 1, 1, 1 }, facing)
                            .withExtraProxyOffsets(blastFurnaceProxyOffsets(facing), proxyInventoryFluid());
                })
                .legacyItemScale(2.0F)
                .yRotation(ModBlocks::eastZeroRotation)
                .renderProfile(LegacyMachineRenderProfile.BLAST_FURNACE_TILTED_STATE)
                .renderBoundingBox(pos -> new AABB(pos.offset(-3, 0, -3), pos.offset(7, 8, 4)))
                .particleState(legacyBlockParticleState("brick_fire"))
                .build();
    }

    private static LegacyMachineDefinition arcFurnaceDefinition() {
        return LegacyMachineDefinition.builder(machineModel("arc_furnace"), machineTexture("arc_furnace"))
                .legacyXrDimensions(4, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .layout(facing -> {
                    Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
                    return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 4, 0, 2, 2, 2, 2 }, facing)
                            .withLegacyXrCheckedFill(new int[] { 4, 0, 3, -2, 1, 1 }, facing)
                            .withExtraProxyOffsets(arcFurnaceProxyOffsets(facing, rot), proxyInventoryPower());
                })
                .renderParts("Furnace", "Lid", "Ring1", "Ring2", "Ring3", "Electrode1", "Electrode2",
                        "Electrode3", "Cable1", "Cable2", "Cable3")
                .renderProfile(LegacyMachineRenderProfile.ARC_FURNACE_STATIC_PREVIEW)
                .legacyItemScale(1.75F)
                .yRotation(ModBlocks::eastZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, -2, -4), pos.offset(7, 9, 5)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition annihilatorDefinition() {
        return LegacyMachineDefinition.builder(machineModel("annihilator"), machineTexture("annihilator"))
                .legacyXrDimensions(2, 0, 4, 4, 1, 1)
                .legacyOffset(4)
                .layout(facing -> {
                    BlockPos rearOrigin = LegacyMultiblockOffsets.relative(facing, -3, 0, 0);
                    return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 4, 4, 1, 1 }, facing)
                            .withLegacyXrCheckedFill(new int[] { 8, -2, 1, 1, 1, 1 }, facing, rearOrigin)
                            .withExtraProxyOffsets(annihilatorProxyOffsets(facing), proxyInventoryFluid());
                })
                .renderParts("Annihilator", "Roller", "Belt")
                .partTextures(Map.of("Belt", machineTexture("annihilator_belt")))
                .renderProfile(LegacyMachineRenderProfile.ANNIHILATOR_UV_SCROLL)
                .legacyItemScale(2.75D, 0.5D)
                .yRotation(ModBlocks::assemblyFactoryRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-5, 0, -5), pos.offset(6, 9, 6)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition felDefinition() {
        return LegacyMachineDefinition.builder(machineModel("fel"), machineTexture("fel"))
                .legacyXrDimensions(2, 0, 4, 2, 1, 1)
                .legacyOffset(2)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 4, 2, 1, 1 }, facing)
                        .withExtraProxyOffsets(List.of(LegacyMultiblockOffsets.relative(facing, -4, 0, 1)),
                                proxyPower()))
                .legacyItemScale(2.0F)
                .yRotation(ModBlocks::northZeroRotation)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -5), pos.offset(3, 4, 3)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition orbusDefinition() {
        return LegacyMachineDefinition.builder(machineModel("orbus"), machineTexture("orbus"))
                .legacyXrDimensions(4, 0, 2, 1, 2, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 4, 0, 2, 1, 2, 1 }, facing)
                        .withExtraProxyOffsets(orbusProxyOffsets(facing), proxyFluid()))
                .legacyItemScale(2.0F)
                .yRotation(facing -> 0.0F)
                .modelTranslation(ModBlocks::orbusModelTranslation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 6, 3)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition miningLaserDefinition() {
        return LegacyMachineDefinition.builder(machineModel("mining_laser"), machineTexture("mining_laser_base"))
                .legacyXrDimensions(1, 1, 1, 1, 1, 1)
                .legacyOffset(0)
                .legacyHeightOffset(-1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 1, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(1), proxyInventoryFluid())
                        .withExtraProxyOffsets(List.of(new BlockPos(0, 1, 0)), proxyPower()))
                .renderParts("Base", "Pivot", "Laser")
                .partTextures(Map.of(
                        "Pivot", machineTexture("mining_laser_pivot"),
                        "Laser", machineTexture("mining_laser_laser")))
                .legacyItemScale(3.0F)
                .legacyInventoryTranslation(0.0D, -0.5D, 0.0D)
                .modelTranslation(0.0D, -1.0D, 0.0D)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, -4, -2), pos.offset(3, 3, 3)))
                .particleState(legacyBlockParticleState("machine_mining_laser"))
                .renderProfile(LegacyMachineRenderProfile.MINING_LASER_ITEM_PREVIEW)
                .build();
    }

    private static LegacyMachineDefinition excavatorDefinition() {
        return LegacyMachineDefinition.builder(machineModel("mining_drill"), machineTexture("mining_drill"))
                .legacyXrDimensions(3, 0, 3, 3, 3, 3)
                .legacyOffset(3)
                .legacyHeightOffset(3)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 3, 0, 3, 3, 3, 3 }, facing)
                        .withLegacyXrCheckedFill(new int[] { -1, 3, 3, -2, 3, -2 }, facing)
                        .withLegacyXrCheckedFill(new int[] { -1, 3, 3, -2, -2, 3 }, facing)
                        .withLegacyXrCheckedFill(new int[] { -1, 3, -2, 3, 3, 3 }, facing)
                        .withExtraProxyOffsets(excavatorProxyOffsets(facing), proxyPowerFluid()))
                .renderParts("Main", "Crusher1", "Crusher2", "Drillbit", "Shaft")
                .legacyItemScale(2.5F)
                .legacyInventoryTranslation(0.0D, -0.5D, 0.0D)
                .modelTranslation(0.0D, -3.0D, 0.0D)
                .yRotation(facing -> 90.0F + facing.toYRot())
                .renderBoundingBox(pos -> new AABB(pos.offset(-4, -256, -4), pos.offset(5, 5, 5)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition strandCasterDefinition() {
        return LegacyMachineDefinition.builder(machineModel("strand_caster"), machineTexture("strand_caster"))
                .legacyXrDimensions(0, 0, 6, 0, 1, 0)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 6, 0, 1, 0 }, facing)
                        .withLegacyXrCheckedFill(new int[] { 2, 0, 1, 0, 1, 0 }, facing)
                        .withExtraProxyOffsets(strandCasterProxyOffsets(facing), proxyInventoryFluidMolten()))
                .renderParts("caster")
                .itemRenderParts("caster", "plate")
                .renderProfile(LegacyMachineRenderProfile.STRAND_CASTER_MOLTEN)
                .legacyItemScale(2.0F)
                .modelTranslation(0.5D, 0.0D, 0.5D)
                .yRotation(ModBlocks::strandCasterBaseRotation)
                .postModelYRotation(180.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -7), pos.offset(3, 4, 2)))
                .particleState(legacyBlockParticleState("brick_fire"))
                .build();
    }

    private static LegacyMachineDefinition woodBurnerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("wood_burner"), machineTexture("wood_burner"))
                .legacyXrDimensions(1, 0, 1, 0, 1, 0)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 1, 0, 1, 0 }, facing)
                        .withExtraProxyOffsets(woodBurnerProxyOffsets(facing), proxyInventoryPowerFluid())
                        .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO), proxyInventory()))
                .legacyItemScale(3.5F)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .modelTranslation(-0.5D, 0.0D, -0.5D)
                .yRotation(ModBlocks::southZeroRotation)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(2, 3, 2)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition stirlingDefinition(String textureName) {
        return LegacyMachineDefinition.builder(machineModel("stirling"), machineTexture(textureName))
                .legacyXrDimensions(1, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(1), proxyPower()))
                .renderParts("Base", "Cog", "CogSmall", "Piston")
                .renderProfile(LegacyMachineRenderProfile.STIRLING_RUNNING_PARTS)
                .legacyItemScale(3.25F)
                .yRotation(ModBlocks::southZeroRotation)
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 3, 3)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static LegacyMachineDefinition deuteriumTowerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("machine_deuterium_tower"), machineTexture("machine_deuterium_tower"))
                .legacyXrDimensions(9, 0, 1, 0, 0, 1)
                .legacyOffset(0)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 9, 0, 1, 0, 0, 1 }, facing)
                        .withExtraProxyOffsets(deuteriumTowerProxyOffsets(facing), proxyPowerFluid()))
                .legacyItemScale(3.0D, 0.5D)
                .yRotation(ModBlocks::southZeroRotation)
                .modelTranslation(0.5D, 0.0D, -0.5D)
                .renderBoundingBox(pos -> new AABB(pos.offset(-2, 0, -2), pos.offset(3, 11, 3)))
                .particleState(legacyBlockParticleState("concrete"))
                .build();
    }

    private static LegacyMachineDefinition fractionSpacerDefinition() {
        return LegacyMachineDefinition.builder(machineModel("fraction_spacer"), machineTexture("fraction_spacer"))
                .legacyXrDimensions(0, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 0, 0, 1, 1, 1, 1 }, facing))
                .renderMode(LegacyMachinePartRenderMode.CUTOUT_CULL)
                .legacyItemScale(3.25F)
                .yRotation(facing -> 0.0F)
                .renderBoundingBox(pos -> new AABB(pos.offset(-1, 0, -1), pos.offset(2, 1, 2)))
                .particleState(legacyBlockParticleState("block_steel"))
                .build();
    }

    private static List<BlockPos> radGenProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, -3, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -1, 0));
    }

    public static LegacyMultiblockLayout zirnoxLayout(Direction facing) {
        Direction rot = facing.getClockWise();
        return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 2, 2, 2, 2 }, facing)
                .withLegacyXrCheckedFill(new int[] { 4, -2, 1, 1, 1, 1 }, facing)
                .withLegacyXrCheckedFill(new int[] { 4, -2, 0, 0, 2, -2 }, facing)
                .withLegacyXrCheckedFill(new int[] { 4, -2, 0, 0, -2, 2 }, facing)
                .withExtraProxyOffsets(List.of(
                        new BlockPos(rot.getStepX() * 2, 1, rot.getStepZ() * 2),
                        new BlockPos(rot.getStepX() * 2, 3, rot.getStepZ() * 2),
                        new BlockPos(rot.getStepX() * -2, 1, rot.getStepZ() * -2),
                        new BlockPos(rot.getStepX() * -2, 3, rot.getStepZ() * -2),
                        new BlockPos(0, 4, 0)),
                        proxyInventoryFluid());
    }

    public static LegacyMultiblockLayout zirnoxDestroyedLayout(Direction facing) {
        return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 1, 0, 2, 2, 2, 2 }, facing);
    }

    private static LegacyMultiblockLayout watzLayout(Direction facing) {
        return LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 2, 0, 3, 3, 1, 1 }, facing)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 2, 2, 2, -2 }, facing)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 2, 2, -2, 2 }, facing)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 1, 1, 3, -3 }, facing)
                .withLegacyXrCheckedFill(new int[] { 2, 0, 1, 1, -3, 3 }, facing)
                .withExtraProxyOffsets(List.of(
                        new BlockPos(2, 0, 0),
                        new BlockPos(-2, 0, 0),
                        new BlockPos(0, 0, 2),
                        new BlockPos(0, 0, -2),
                        new BlockPos(2, 2, 0),
                        new BlockPos(-2, 2, 0),
                        new BlockPos(0, 2, 2),
                        new BlockPos(0, 2, -2),
                        new BlockPos(0, 2, 0)),
                        proxyInventoryFluid());
    }

    private static List<BlockPos> rotaryFurnaceProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyDownSide(facing);
        return LegacyMultiblockOffsets.combine(
                LegacyMultiblockOffsets.lineAlongSide(facing, rot, -1, -2, 2, 0),
                List.of(
                        LegacyMultiblockOffsets.relative(facing, rot, 1, 2, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 4),
                        LegacyMultiblockOffsets.relative(facing, rot, 1, 1, 0)));
    }

    private static List<BlockPos> steamEngineProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 1, 1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 1));
    }

    private static List<BlockPos> towerLargeProxyOffsets() {
        return Stream.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
                .flatMap(direction -> {
                    Direction rot = LegacyMultiblockOffsets.legacyUpSide(direction);
                    return Stream.of(
                            LegacyMultiblockOffsets.relative(direction, rot, 4, 0, 0),
                            LegacyMultiblockOffsets.relative(direction, rot, 4, 3, 0),
                            LegacyMultiblockOffsets.relative(direction, rot, 4, -3, 0));
                })
                .toList();
    }

    private static List<BlockPos> turbofanProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -3, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -3, -1, 0));
    }

    private static List<BlockPos> turbineGasProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 1, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, -4, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 1, -4, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, 4, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -5, 1));
    }

    private static List<BlockPos> blastFurnaceProxyOffsets(Direction facing) {
        return List.of(
                new BlockPos(1, 0, 0),
                new BlockPos(-1, 0, 0),
                new BlockPos(0, 0, 1),
                new BlockPos(0, 0, -1),
                LegacyMultiblockOffsets.relative(facing, 1, 0, 3),
                LegacyMultiblockOffsets.relative(facing, 1, 0, 5),
                new BlockPos(0, 6, 0));
    }

    private static List<BlockPos> arcFurnaceProxyOffsets(Direction facing, Direction rot) {
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 2, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 2, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 1, 2, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 2, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 1, -2, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, -2, 0));
    }

    private static List<BlockPos> annihilatorProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 3, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 3, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 4, 0, 0));
    }

    private static List<BlockPos> orbusProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 0, 0, 4),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 4),
                LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 4),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 4));
    }

    private static List<BlockPos> strandCasterProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -5, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -5, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 2),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 2),
                LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 2),
                new BlockPos(0, 2, 0));
    }

    private static List<BlockPos> woodBurnerProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, -1, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 0));
    }

    private static List<BlockPos> deuteriumTowerProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, -1, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 0));
    }

    private static List<BlockPos> chungusProxyOffsets(Direction facing) {
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, -2, 0, 2),
                LegacyMultiblockOffsets.relative(facing, -10, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, 2, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, -2, 0));
    }

    private static List<BlockPos> hephaestusProxyOffsets() {
        return LegacyMultiblockOffsets.combine(
                LegacyMultiblockOffsets.cardinal(1),
                LegacyMultiblockOffsets.cardinal(1, 11));
    }

    private static List<BlockPos> heatBoilerProxyOffsets(Direction facing) {
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, side, 0, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, -1, 0),
                new BlockPos(0, 3, 0));
    }

    private static List<BlockPos> industrialBoilerProxyOffsets(Direction facing) {
        BlockPos rearOrigin = LegacyMultiblockOffsets.relative(facing, -2, 0);
        return LegacyMultiblockOffsets.combine(
                List.of(
                        rearOrigin.offset(1, 0, 0),
                        rearOrigin.offset(-1, 0, 0),
                        rearOrigin.offset(0, 0, 1),
                        rearOrigin.offset(0, 0, -1),
                        rearOrigin.offset(0, 4, 0)));
    }

    private static List<BlockPos> combustionEngineProxyOffsets(Direction facing) {
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, side, 0, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, -1, 0),
                LegacyMultiblockOffsets.relative(facing, side, -1, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, -1, -1, 0));
    }

    private static List<BlockPos> industrialTurbineProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 3, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 3, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 3, 0, 2),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 2),
                LegacyMultiblockOffsets.relative(facing, rot, -3, 0, 1));
    }

    private static List<BlockPos> legacyLargeTurbineProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, -3, 0, 0),
                LegacyMultiblockOffsets.relative(facing, 1, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -1, 0));
    }

    private static LegacyProxyMode proxyInventory() {
        return LegacyProxyMode.passive().inventoryProxy();
    }

    private static LegacyProxyMode proxyFluid() {
        return LegacyProxyMode.passive().fluidProxy();
    }

    private static LegacyProxyMode proxyPower() {
        return LegacyProxyMode.passive().powerProxy();
    }

    private static LegacyProxyMode proxyPowerConductor() {
        return LegacyProxyMode.passive().powerProxy().conductorProxy();
    }

    private static LegacyProxyMode proxyHeatSource() {
        return LegacyProxyMode.passive().heatProxy();
    }

    private static LegacyProxyMode proxyPowerFluid() {
        return LegacyProxyMode.combo(false, true, true);
    }

    private static LegacyProxyMode proxyInventoryPower() {
        return LegacyProxyMode.combo(true, true, false);
    }

    private static LegacyProxyMode proxyInventoryFluid() {
        return LegacyProxyMode.combo(true, false, true);
    }

    private static LegacyProxyMode proxyInventoryFluidMolten() {
        return LegacyProxyMode.combo(true, false, true).moltenMetalProxy();
    }

    private static LegacyProxyMode proxyInventoryPowerFluid() {
        return LegacyProxyMode.combo(true, true, true);
    }

    private static List<BlockPos> fusionBreederProxyOffsets(Direction facing) {
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, side, 0, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, -1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 1, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 1, -1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 2, 0, 2));
    }

    private static List<BlockPos> fusionBoilerProxyOffsets(Direction facing) {
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, side, -1, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, -1, -1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 2, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 2, -1, 0));
    }

    private static List<BlockPos> fusionKlystronProxyOffsets(Direction facing) {
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, side, 3, 0, 2),
                LegacyMultiblockOffsets.relative(facing, side, 0, 2, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, -2, 0));
    }

    private static List<BlockPos> fusionMhdtProxyOffsets(Direction facing) {
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, side, 4, 3, 0),
                LegacyMultiblockOffsets.relative(facing, side, 4, -3, 0),
                LegacyMultiblockOffsets.relative(facing, side, 7, 0, 1));
    }

    private static List<BlockPos> fusionPlasmaForgeProxyOffsets(Direction facing) {
        Direction side = facing.getClockWise();
        List<BlockPos> offsets = new ArrayList<>();
        for (int i = -2; i <= 2; i++) {
            offsets.add(new BlockPos(
                    facing.getStepX() * 5 + side.getStepX() * i,
                    0,
                    facing.getStepZ() * 5 + side.getStepZ() * i));
            offsets.add(new BlockPos(
                    -facing.getStepX() * 5 + side.getStepX() * i,
                    0,
                    -facing.getStepZ() * 5 + side.getStepZ() * i));
        }
        return List.copyOf(offsets);
    }

    private static List<BlockPos> heaterOilburnerExtraOffsets() {
        return LegacyMultiblockOffsets.combine(
                LegacyMultiblockOffsets.cardinal(1),
                List.of(new BlockPos(0, 1, 0)));
    }

    private static LegacyProxyMode heaterOilburnerProxyMode(BlockPos offset) {
        return offset.equals(new BlockPos(0, 1, 0)) ? proxyHeatSource() : proxyFluid();
    }

    private static List<BlockPos> condenserPoweredProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 0, 3, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -3, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 1, 1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 1, -1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, -1, -1, 1));
    }

    private static List<BlockPos> oreSlopperProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 3, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -3, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 2, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 2, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -2, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -2, -1, 0));
    }

    private static List<BlockPos> excavatorProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 3, 1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 3, -1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 0, 3, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -3, 1));
    }

    private static List<BlockPos> assemblyFactoryProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return LegacyMultiblockOffsets.combine(
                LegacyMultiblockOffsets.squarePerimeter(2),
                LegacyMultiblockOffsets.lineAlongFacing(facing, rot, -2, 2, 2, 2),
                LegacyMultiblockOffsets.lineAlongFacing(facing, rot, -2, 2, -2, 2));
    }

    private static List<BlockPos> silexProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -1, 1));
    }

    private static List<BlockPos> exposureChamberProxyOffsets(Direction facing, Direction rot) {
        return List.of(
                new BlockPos(rot.getStepX() * 7 + facing.getStepX(), 0, rot.getStepZ() * 7 + facing.getStepZ()),
                new BlockPos(rot.getStepX() * 7 - facing.getStepX(), 0, rot.getStepZ() * 7 - facing.getStepZ()),
                new BlockPos(rot.getStepX() * 8 + facing.getStepX(), 0, rot.getStepZ() * 8 + facing.getStepZ()),
                new BlockPos(rot.getStepX() * 8 - facing.getStepX(), 0, rot.getStepZ() * 8 - facing.getStepZ()),
                new BlockPos(rot.getStepX() * 8, 0, rot.getStepZ() * 8));
    }

    private static List<BlockPos> liquefactorProxyOffsets(Direction facing) {
        return LegacyMultiblockOffsets.combine(
                LegacyMultiblockOffsets.cardinal(1, 1),
                List.of(new BlockPos(0, 3, 0)));
    }

    private static List<BlockPos> pyroOvenProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyDownSide(facing);
        return LegacyMultiblockOffsets.combine(
                LegacyMultiblockOffsets.lineAlongFacing(facing, rot, -2, 2, 2, 0),
                List.of(LegacyMultiblockOffsets.relative(facing, rot, 0, -1, 2)));
    }

    private static List<BlockPos> solidifierProxyOffsets() {
        return List.of(
                new BlockPos(0, 3, 0),
                new BlockPos(1, 1, 0),
                new BlockPos(-1, 1, 0),
                new BlockPos(0, 1, 1),
                new BlockPos(0, 1, -1));
    }

    private static List<BlockPos> compressorProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -1, 0));
    }

    private static BlockPos electrolyserTopColumnOrigin(Direction facing, int forward) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return LegacyMultiblockOffsets.relative(facing, rot, forward, 0, 3);
    }

    private static List<BlockPos> electrolyserProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, -5, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -5, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -5, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 5, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 5, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 5, -1, 0));
    }

    private static List<BlockPos> bigAssTankProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 6, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -6, 0, 0));
    }

    private static List<BlockPos> fluidTankProxyOffsets(Direction facing) {
        return List.of(
                new BlockPos(1, 0, 1),
                new BlockPos(1, 0, -1),
                new BlockPos(-1, 0, 1),
                new BlockPos(-1, 0, -1));
    }

    private static List<BlockPos> rearCornerProxyOffsets(Direction facing) {
        int originX = -facing.getStepX() * 2;
        int originZ = -facing.getStepZ() * 2;
        return List.of(
                new BlockPos(originX + 1, 0, originZ + 1),
                new BlockPos(originX + 1, 0, originZ - 1),
                new BlockPos(originX - 1, 0, originZ + 1),
                new BlockPos(originX - 1, 0, originZ - 1));
    }

    private static List<BlockPos> bat9000ProxyOffsets() {
        return List.of(
                new BlockPos(1, 0, 2),
                new BlockPos(-1, 0, 2),
                new BlockPos(1, 0, -2),
                new BlockPos(-1, 0, -2),
                new BlockPos(2, 0, 1),
                new BlockPos(-2, 0, 1),
                new BlockPos(2, 0, -1),
                new BlockPos(-2, 0, -1));
    }

    private static List<BlockPos> batteryReddProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 2, 2, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 2, -2, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -2, 2, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -2, -2, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, 4, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -4, 0));
    }

    private static List<BlockPos> catalyticCrackerProxyOffsets(Direction facing) {
        return List.of(
                LegacyMultiblockOffsets.relative(facing, 3, 1),
                LegacyMultiblockOffsets.relative(facing, 3, -2),
                LegacyMultiblockOffsets.relative(facing, -3, 1),
                LegacyMultiblockOffsets.relative(facing, -3, -2),
                LegacyMultiblockOffsets.relative(facing, 2, 2),
                LegacyMultiblockOffsets.relative(facing, 2, -3),
                LegacyMultiblockOffsets.relative(facing, -2, 2),
                LegacyMultiblockOffsets.relative(facing, -2, -3));
    }

    private static List<BlockPos> catalyticReformerProxyOffsets(Direction facing) {
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return LegacyMultiblockOffsets.combine(
                rearCornerProxyOffsets(facing),
                List.of(
                        LegacyMultiblockOffsets.relative(facing, rot, -2, 2, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, -2, -2, 0)));
    }

    private static float catalyticRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float batteryReddRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 270.0F;
            case WEST -> 0.0F;
            case SOUTH -> 90.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

    private static float pyroOvenRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
    }

    private static float solidifierRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float radiolysisRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float steamEngineRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            case SOUTH -> 90.0F;
            default -> 0.0F;
        };
    }

    private static float pumpRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            case SOUTH -> 90.0F;
            default -> 0.0F;
        };
    }

    private static float bigAssTankRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            case SOUTH -> 90.0F;
            default -> 0.0F;
        };
    }

    private static float eastZeroRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }

    private static List<BlockPos> cokerProxyOffsets() {
        return List.of(
                new BlockPos(1, 0, 1),
                new BlockPos(1, 0, -1),
                new BlockPos(-1, 0, 1),
                new BlockPos(-1, 0, -1));
    }

    private static VoxelShape legacyRotatedShape(BlockState state, AABB... boxes) {
        Direction facing = state.getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        VoxelShape shape = Shapes.empty();
        for (AABB box : boxes) {
            AABB rotated = rotateLegacyBox(box, legacyDetailedHitboxRotation(facing));
            shape = Shapes.or(shape, Shapes.box(
                    rotated.minX + 0.5D,
                    rotated.minY,
                    rotated.minZ + 0.5D,
                    rotated.maxX + 0.5D,
                    rotated.maxY,
                    rotated.maxZ + 0.5D));
        }
        return shape;
    }

    private static VoxelShape legacyUnrotatedShape(AABB... boxes) {
        VoxelShape shape = Shapes.empty();
        for (AABB box : boxes) {
            shape = Shapes.or(shape, Shapes.box(
                    box.minX + 0.5D,
                    box.minY,
                    box.minZ + 0.5D,
                    box.maxX + 0.5D,
                    box.maxY,
                    box.maxZ + 0.5D));
        }
        return shape;
    }

    private static Direction legacyDetailedHitboxRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> Direction.SOUTH;
        };
    }

    private static AABB rotateLegacyBox(AABB box, Direction rotation) {
        return switch (rotation) {
            case EAST -> new AABB(-box.maxZ, box.minY, box.minX, -box.minZ, box.maxY, box.maxX);
            case SOUTH -> new AABB(-box.maxX, box.minY, -box.maxZ, -box.minX, box.maxY, -box.minZ);
            case WEST -> new AABB(box.minZ, box.minY, -box.maxX, box.maxZ, box.maxY, -box.minX);
            default -> box;
        };
    }

    private static float centrifugeRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float normalizeRotation(float rotation) {
        return (rotation % 360.0F + 360.0F) % 360.0F;
    }

    private static float northZeroRotation(Direction facing) {
        return switch (facing) {
            case WEST -> 90.0F;
            case SOUTH -> 180.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float sawmillRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
    }

    private static float crucibleRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float oreSlopperRotation(Direction facing) {
        return switch (facing) {
            case WEST -> 90.0F;
            case SOUTH -> 180.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float southZeroRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0.0F;
            case EAST -> 90.0F;
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float strandCasterBaseRotation(Direction facing) {
        return switch (facing) {
            case WEST -> 90.0F;
            case SOUTH -> 180.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static Vec3 orbusModelTranslation(Direction facing) {
        return switch (facing) {
            case NORTH -> new Vec3(0.5D, 0.0D, 0.5D);
            case WEST -> new Vec3(0.5D, 0.0D, -0.5D);
            case EAST -> new Vec3(-0.5D, 0.0D, 0.5D);
            default -> new Vec3(-0.5D, 0.0D, -0.5D);
        };
    }

    private static float assemblyFactoryRotation(Direction facing) {
        return switch (facing) {
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            case EAST -> 0.0F;
            default -> 90.0F;
        };
    }

    private static List<BlockPos> pumpjackCornerProxyOffsets(BlockPos offsetCore) {
        return List.of(
                offsetCore.offset(1, 0, 1),
                offsetCore.offset(1, 0, -1),
                offsetCore.offset(-1, 0, 1),
                offsetCore.offset(-1, 0, -1));
    }

    private static ResourceLocation machineModel(String name) {
        if (ROOT_DIRECT_MACHINE_MODELS.contains(name)) {
            return new ResourceLocation(HbmNtm.MOD_ID, "models/" + name + ".obj");
        }
        if (MACHINE_DIRECT_MODELS.contains(name)) {
            return new ResourceLocation(HbmNtm.MOD_ID, "models/machines/" + name + ".obj");
        }
        return new ResourceLocation(HbmNtm.MOD_ID, "models/block/machines/" + name + ".obj");
    }

    private static ResourceLocation machineTexture(String name) {
        switch (name) {
            case "radiolysis", "refinery", "tank" -> {
                return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + name + ".png");
            }
            case "fluidtank" -> {
                return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/tank.png");
            }
            default -> {
            }
        }
        if (DIRECT_MACHINE_MODEL_TEXTURES.contains(name)) {
            return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/" + name + ".png");
        }
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/machines/" + name + ".png");
    }

    private static ResourceLocation utilityModel(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "models/block/utility/" + name + ".obj");
    }

    private static ResourceLocation utilityTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/utility/" + name + ".png");
    }

    private static ResourceLocation launchTableModel(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "models/block/launch_table/" + name + ".obj");
    }

    private static ResourceLocation launchTableTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/launch_table/" + name + ".png");
    }

    private static Function<BlockState, BlockState> legacyBlockParticleState(String legacyName) {
        return state -> {
            RegistryObject<? extends Block> block = BLOCKS_BY_LEGACY_NAME.get(legacyName);
            if (block == null || !block.isPresent()) {
                return MultiblockHelper.steelParticleState();
            }
            BlockState particleState = block.get().defaultBlockState();
            return particleState.getBlock() == state.getBlock() ? MultiblockHelper.steelParticleState() : particleState;
        };
    }

    private static RegistryObject<Block> machineBattery(String name) {
        return machineBattery(name, MachineBatteryBlock.DEFAULT_MAX_POWER);
    }

    private static RegistryObject<Block> machineBattery(String name, long maxPower) {
        return registerBlockWithItem(name, () -> new MachineBatteryBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(), maxPower));
    }

    private static RegistryObject<Block> miniRtgMachine(String name, MiniRtgBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new MiniRtgBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), kind));
    }

    private static RegistryObject<Block> machineFensu(String name) {
        return registerBlockWithItem(
                name,
                () -> new FensuBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> machineBatterySocket(String name) {
        return registerBlockWithItem(
                name,
                () -> new MachineBatterySocketBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> storageCrate(String name, StorageCrateBlockEntity.Kind kind) {
        return registerBlockWithItem(name, () -> new CrateBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops(), kind),
                block -> new CrateBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> safe(String name) {
        return registerBlockWithItem(name, () -> new SafeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()),
                block -> new CrateBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> massStorage(String name) {
        return registerBlockWithItem(name, () -> new MassStorageBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()),
                block -> new MassStorageBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> turretChekhov(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretChekhovBlockEntity::new,
                ModBlockEntities.TURRET_CHEKHOV), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretFriendly(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretFriendlyBlockEntity::new,
                ModBlockEntities.TURRET_FRIENDLY), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretJeremy(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretJeremyBlockEntity::new,
                ModBlockEntities.TURRET_JEREMY), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretRichard(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretRichardBlockEntity::new,
                ModBlockEntities.TURRET_RICHARD), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretTauon(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretTauonBlockEntity::new,
                ModBlockEntities.TURRET_TAUON), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretHoward(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretHowardBlockEntity::new,
                ModBlockEntities.TURRET_HOWARD), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretSentry(String name) {
        return registerBlockWithItem(name, () -> new SingleTurretBlock(turretProperties(),
                TurretSentryBlockEntity::new,
                ModBlockEntities.TURRET_SENTRY,
                true,
                true,
                false), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretHowardDamaged(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretHowardDamagedBlockEntity::new,
                ModBlockEntities.TURRET_HOWARD_DAMAGED,
                TurretBaseBlock.STANDARD_DIMENSIONS,
                0,
                LegacyProxyMode.none(),
                false,
                false), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretSentryDamaged(String name) {
        return registerBlockWithItem(name, () -> new SingleTurretBlock(turretProperties(),
                TurretSentryDamagedBlockEntity::new,
                ModBlockEntities.TURRET_SENTRY_DAMAGED,
                false,
                false,
                false), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretMaxwell(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretMaxwellBlockEntity::new,
                ModBlockEntities.TURRET_MAXWELL), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretArty(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretArtyBlockEntity::new,
                ModBlockEntities.TURRET_ARTY,
                TurretBaseBlock.ARTILLERY_DIMENSIONS,
                1,
                LegacyProxyMode.combo(true, true, false)), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretHimars(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretHimarsBlockEntity::new,
                ModBlockEntities.TURRET_HIMARS,
                TurretBaseBlock.ARTILLERY_DIMENSIONS,
                1,
                LegacyProxyMode.combo(true, true, false)), ModBlocks::turretBlockItem);
    }

    private static RegistryObject<Block> turretFritz(String name) {
        return registerBlockWithItem(name, () -> new TurretBaseBlock(turretProperties(),
                TurretFritzBlockEntity::new,
                ModBlockEntities.TURRET_FRITZ,
                TurretBaseBlock.STANDARD_DIMENSIONS,
                0,
                LegacyProxyMode.combo(true, true, true)), ModBlocks::turretBlockItem);
    }

    private static Item turretBlockItem(RegistryObject<Block> block) {
        return new TurretBlockItem(block.get(), new Item.Properties());
    }

    private static BlockBehaviour.Properties turretProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private static RegistryObject<Block> machineRadar(String name) {
        return registerBlockWithItem(name, () -> new RadarBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> machineRadarLarge(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new RadarLargeBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> machineRadarScreen(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new RadarScreenBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> satelliteLinker(String name) {
        return registerBlockWithItem(name, () -> new SatelliteLinkerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> satelliteDock(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new SatelliteDockBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> soyuzCapsule(String name) {
        return registerBlockWithItem(name, () -> new SoyuzCapsuleBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> soyuzLauncher(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new SoyuzLauncherBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> missileAssembly(String name) {
        return registerBlockWithItem(
                name,
                () -> new MissileAssemblyBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> launchStructureBlock(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> soyuzStructCoreBlock(String name) {
        return registerBlockWithItem(name, () -> new SoyuzStructBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static LegacyMachineDefinition satDockDefinition() {
        return LegacyMachineDefinition.builder(utilityModel("sat_dock"), utilityTexture("sat_dock"))
                .layout(facing -> LegacyMultiblockLayout.ofOffsets(satDockOffsets()))
                .renderBoundingBox(pos -> new AABB(
                        pos.getX() - 1.0D,
                        pos.getY(),
                        pos.getZ() - 1.0D,
                        pos.getX() + 2.0D,
                        pos.getY() + 1.0D,
                        pos.getZ() + 2.0D))
                .collisionShape(state -> LegacyMultiblockLayout.ofOffsets(satDockOffsets()).shape(0.75D))
                .highlightShape(state -> LegacyMultiblockLayout.ofOffsets(satDockOffsets()).shape(0.75D))
                .itemFitSize(0.6F)
                .build();
    }

    private static List<BlockPos> satDockOffsets() {
        return List.of(
                new BlockPos(-1, 0, -1),
                new BlockPos(-1, 0, 0),
                new BlockPos(-1, 0, 1),
                new BlockPos(0, 0, -1),
                BlockPos.ZERO,
                new BlockPos(0, 0, 1),
                new BlockPos(1, 0, -1),
                new BlockPos(1, 0, 0),
                new BlockPos(1, 0, 1));
    }

    private static LegacyMachineDefinition soyuzLauncherDefinition() {
        return LegacyMachineDefinition.builder(launchTableModel("soyuz_launcher_table"),
                        launchTableTexture("soyuz_launcher_table"))
                .legacyHeightOffset(4)
                .placementFacing(facing -> Direction.EAST)
                .layout(facing -> soyuzLauncherLayout())
                .renderBoundingBox(pos -> new AABB(
                        pos.getX() - 8.0D,
                        pos.getY() - 4.0D,
                        pos.getZ() - 38.0D,
                        pos.getX() + 8.0D,
                        pos.getY() + 60.0D,
                        pos.getZ() + 49.0D))
                .itemFitSize(0.45F)
                .build();
    }

    private static LegacyMultiblockLayout soyuzLauncherLayout() {
        return SoyuzLauncherLayoutCache.LAYOUT;
    }

    private static final class SoyuzLauncherLayoutCache {
        private static final List<BlockPos> PROXY_OFFSETS = createSoyuzLauncherProxyOffsets();
        private static final LegacyMultiblockLayout LAYOUT = createSoyuzLauncherLayout();

        private SoyuzLauncherLayoutCache() {
        }
    }

    private static LegacyMultiblockLayout createSoyuzLauncherLayout() {
        return LegacyMultiblockLayout.ofOffsets(List.of(BlockPos.ZERO))
                .withLegacyXrCheckedFill(new int[] { 0, 1, 6, 6, 6, 6 }, Direction.EAST)
                .withLegacyXrCheckedFill(new int[] { -2, 4, -3, 6, -3, 6 }, Direction.EAST)
                .withLegacyXrCheckedFill(new int[] { -2, 4, 6, -3, -3, 6 }, Direction.EAST)
                .withLegacyXrCheckedFill(new int[] { -2, 4, 6, -3, 6, -3 }, Direction.EAST)
                .withLegacyXrCheckedFill(new int[] { -2, 4, -3, 6, 6, -3 }, Direction.EAST)
                .withLegacyXrCheckedFill(new int[] { 0, 4, 1, 1, -6, 8 }, Direction.EAST)
                .withLegacyXrCheckedFill(new int[] { 0, 4, 2, 2, 9, -5 }, Direction.EAST)
                .withExtraProxyOffsets(soyuzLauncherProxyOffsets(), LegacyProxyMode.combo(false, true, true));
    }

    private static List<BlockPos> soyuzLauncherProxyOffsets() {
        return SoyuzLauncherLayoutCache.PROXY_OFFSETS;
    }

    private static List<BlockPos> createSoyuzLauncherProxyOffsets() {
        Set<BlockPos> offsets = new LinkedHashSet<>();
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                if (Math.abs(x) == 6 || Math.abs(z) == 6) {
                    offsets.add(new BlockPos(x, 0, z));
                    offsets.add(new BlockPos(x, 1, z));
                }
            }
        }
        return List.copyOf(offsets);
    }

    private static LegacyMachineDefinition radarLargeDefinition() {
        return LegacyMachineDefinition.builder(machineModel("radar_large"), machineTexture("radar_large"))
                .legacyXrDimensions(4, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXrChecked(new int[] { 4, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(LegacyMultiblockOffsets.cardinal(1),
                                LegacyProxyMode.passive().powerProxy()))
                .yRotation(facing -> 180.0F)
                .renderBoundingBox(pos -> new AABB(
                        pos.getX() - 5.0D,
                        pos.getY(),
                        pos.getZ() - 5.0D,
                        pos.getX() + 6.0D,
                        pos.getY() + 10.0D,
                        pos.getZ() + 6.0D))
                .itemFitSize(0.72F)
                .build();
    }

    private static LegacyMachineDefinition radarScreenDefinition() {
        return LegacyMachineDefinition.builder(machineModel("radar_screen"), machineTexture("radar_screen"))
                .legacyXrDimensions(1, 0, 0, 0, 1, 0)
                .yRotation(ModBlocks::solidifierRotation)
                .renderBoundingBox(pos -> new AABB(
                        pos.getX() - 1.0D,
                        pos.getY() - 1.0D,
                        pos.getZ() - 1.0D,
                        pos.getX() + 2.0D,
                        pos.getY() + 2.0D,
                        pos.getZ() + 2.0D))
                .itemFitSize(0.7F)
                .build();
    }

    private static RegistryObject<Block> wasteEarth(String name, boolean mycelium) {
        return registerBlockWithItem(name, () -> new RadioactiveWasteEarthBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GRASS)
                .strength(0.6F)
                .sound(SoundType.GRASS), mycelium));
    }

    private static RegistryObject<Block> glowingMush(String name) {
        return registerBlockWithItem(name, () -> new GlowingMushBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.0F)
                .sound(SoundType.GRASS)
                .lightLevel(state -> 8)
                .noOcclusion()
                .noCollission()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> hugeMush(String name) {
        return registerBlockWithItem(name, () -> new LegacyHugeMushBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.2F)
                .sound(SoundType.GRASS)
                .lightLevel(state -> 15)));
    }

    private static RegistryObject<Block> ntmFlower(String name, LegacyNtmFlowerBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new LegacyNtmFlowerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.0F)
                .sound(SoundType.GRASS)
                .noOcclusion()
                .noCollission()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false),
                kind));
    }

    private static RegistryObject<Block> falloutLayer(String name) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new FalloutLayerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.1F)
                .sound(SoundType.GRAVEL)
                .noOcclusion()
                .noCollission()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static RegistryObject<Block> oilSpill(String name) {
        return registerBlockWithItem(name, () -> new OilSpillBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.1F)
                .sound(SoundType.SNOW)
                .noOcclusion()
                .noCollission()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> barricade(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(1.0F, 2.5F)
                .sound(SoundType.SAND)));
    }

    private static RegistryObject<Block> hiddenBlock(String name, Supplier<Block> blockSupplier) {
        RegistryObject<Block> block = BLOCKS.register(name, blockSupplier);
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static RegistryObject<Block> bedrockOreDeposit(String name) {
        return registerBlockWithoutItem(name, () -> new BedrockOreDepositBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3_600_000.0F)
                .sound(SoundType.STONE)
                .isValidSpawn((state, level, pos, type) -> false)));
    }

    private static RegistryObject<Block> sellafield(String name) {
        return registerBlockWithItem(
                name,
                () -> new LegacySellafieldBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()),
                block -> new LegacyStateBlockItem(block.get(), new Item.Properties(), LegacySellafieldBlock.LEVEL, 6,
                        variant -> Component.translatable(variant == 0
                                ? "block.hbm_ntm_rebirth.sellafield"
                                : "block.hbm_ntm_rebirth.sellafield." + variant)));
    }

    private static RegistryObject<Block> sellafieldSlaked(String name) {
        return registerSlakedStateBlock(name, () -> new LegacySellafieldSlakedBlock(sellafieldSlakedProperties()));
    }

    private static RegistryObject<Block> sellafieldBedrock(String name) {
        return registerSlakedStateBlock(name, () -> new LegacySellafieldSlakedBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3_600_000.0F)
                .sound(SoundType.STONE)
                .isValidSpawn((state, level, pos, type) -> false)));
    }

    private static RegistryObject<Block> sellafieldOre(String name, LegacySellafieldOreBlock.Kind kind) {
        return registerSlakedStateBlock(name, () -> new LegacySellafieldOreBlock(sellafieldSlakedProperties(), kind));
    }

    private static RegistryObject<Block> registerSlakedStateBlock(String name, Supplier<Block> blockFactory) {
        return registerBlockWithItem(
                name,
                blockFactory,
                block -> new LegacyStateBlockItem(block.get(), new Item.Properties(), LegacySellafieldSlakedBlock.LEVEL, 16,
                        variant -> Component.translatable("block.hbm_ntm_rebirth." + name)));
    }

    private static BlockBehaviour.Properties sellafieldSlakedProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static RegistryObject<Block> trinititeOre(String name) {
        return registerBlockWithItem(name, () -> new LegacyTrinititeOreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.5F, 2.5F)
                .sound(SoundType.SAND)));
    }

    private static RegistryObject<Block> frozenEarth(String name) {
        return registerBlockWithItem(name, () -> new LegacyFrozenEarthBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .strength(0.5F, 2.5F)
                .sound(SoundType.GLASS)));
    }

    private static RegistryObject<Block> frozenLog(String name) {
        return registerBlockWithItem(name, () -> new LegacyWasteLogBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .strength(0.5F, 2.5F)
                .sound(SoundType.GLASS)));
    }

    private static RegistryObject<Block> frozenBlock(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .strength(0.5F, 2.5F)
                .sound(SoundType.GLASS)));
    }

    private static RegistryObject<Block> tektite(String name) {
        return registerBlockWithItem(name, () -> new LegacyTektiteOreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.5F)
                .sound(SoundType.SAND)));
    }

    private static RegistryObject<Block> simpleBlock(String name, String textureName) {
        return registerBlockWithItem(name, () -> new Block(simpleResourceProperties(name, textureName)));
    }

    private static RegistryObject<Block> caveSpike(String name, String textureName, LegacyCaveSpikeBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new LegacyCaveSpikeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(0.5F, 2.0F)
                .sound(SoundType.STONE)
                .noCollission()
                .noOcclusion()
                .requiresCorrectToolForDrops(), kind));
    }

    private static RegistryObject<Block> chain(String name) {
        return registerBlockWithItem(name, () -> new LegacyChainBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.25F, 2.0F)
                .sound(SoundType.METAL)
                .noCollission()
                .noOcclusion()));
    }

    private static RegistryObject<Block> poleTop(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 15.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> poleSatelliteReceiver(String name) {
        return registerBlockWithItem(name, () -> new LegacyPoleSatelliteReceiverBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 15.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> steelBeam(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 15.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> ashDigamma(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.5F, 150.0F)
                .sound(SoundType.SAND)));
    }

    private static RegistryObject<Block> fireDigamma(String name) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new DigammaFlameBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(0.0F, 150.0F)
                .lightLevel(state -> 15)
                .noCollission()
                .noOcclusion()
                .replaceable()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static RegistryObject<Block> balefire(String name) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new BalefireBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GREEN)
                .strength(0.0F)
                .lightLevel(state -> 15)
                .noCollission()
                .noOcclusion()
                .replaceable()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static RegistryObject<Block> coriumBlock(String name) {
        return registerBlockWithoutItem(name, () -> new LegacyCoriumFiniteBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(100.0F, 500.0F)
                .lightLevel(state -> 10)
                .noCollission()
                .noOcclusion()
                .randomTicks()
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> pribrisDebris(String name, MapColor color, RBMKDebrisBlock.Kind kind) {
        return registerBlockWithItem(name, () -> {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(50.0F, 600.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion();
            if (kind == RBMKDebrisBlock.Kind.RADIATING) {
                return new RBMKRadiatingDebrisBlock(properties);
            }
            return new RBMKDebrisBlock(properties, kind);
        });
    }

    private static RegistryObject<Block> basaltOre(String name) {
        return registerBlockWithItem(
                name,
                () -> new LegacyBasaltOreBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()),
                block -> new LegacyStateBlockItem(block.get(), new Item.Properties(), LegacyBasaltOreBlock.VARIANT,
                        LegacyBasaltOreBlock.Variant.values().length,
                        variant -> Component.translatable("block." + HbmNtm.MOD_ID + ".ore_basalt_"
                                + LegacyBasaltOreBlock.Variant.byLegacyMeta(variant).getSerializedName())));
    }

    private static RegistryObject<Block> volcanicLavaBlock(String name, boolean radioactive) {
        RegistryObject<Block> block = registerBlockWithoutItem(name,
                () -> new LegacyVolcanicLavaBlock(
                        radioactive ? ModFluids.RAD_LAVA_FLUID : ModFluids.VOLCANIC_LAVA_FLUID,
                        legacyLiquidProperties(MapColor.COLOR_ORANGE, 15)
                                .randomTicks()
                                .sound(SoundType.STONE),
                        radioactive));
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static RegistryObject<Block> hotBlock(String name, float radiation) {
        return registerBlockWithItem(name, () -> new LegacyHotBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(100.0F, 500.0F)
                .lightLevel(state -> 15)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops(), radiation));
    }

    private static RegistryObject<Block> volcanoCore(String name) {
        return registerBlockWithItem(name, () -> new LegacyVolcanoCoreBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(-1.0F, 10000.0F)
                        .lightLevel(state -> 15)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()),
                block -> new LegacyStateBlockItem(block.get(), new Item.Properties(), LegacyVolcanoCoreBlock.MODE, 5,
                        variant -> Component.translatable("block." + HbmNtm.MOD_ID + "." + name)));
    }

    private static RegistryObject<Block> mudBlock(String name) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new LegacyMudBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .strength(100.0F, 500.0F)
                .lightLevel(state -> 5)
                .randomTicks()
                .noCollission()
                .noOcclusion()
                .replaceable()
                .sound(SoundType.SLIME_BLOCK)
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static RegistryObject<Block> radiationBarrel(String name, float chunkRadiationPerTick) {
        return registerBlockWithItem(name, () -> new LegacyRadiationBarrelBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5F, 2.5F)
                .sound(SoundType.METAL)
                .noOcclusion(),
                chunkRadiationPerTick));
    }

    private static RegistryObject<Block> explosiveBarrel(String name, LegacyExplosiveBarrelBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new LegacyExplosiveBarrelBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5F, 2.5F)
                .sound(SoundType.METAL)
                .noOcclusion(),
                kind));
    }

    private static RegistryObject<Block> taint(String name) {
        return registerBlockWithItem(name, () -> new LegacyTaintBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(15.0F, 10.0F)
                .sound(SoundType.GRAVEL)
                .noOcclusion()));
    }

    private static RegistryObject<Block> legacyTntBase(String name, LegacyTntBaseBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new LegacyTntBaseBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(0.0F)
                .sound(SoundType.GRASS),
                kind));
    }

    private static RegistryObject<Block> detCord(String name) {
        return registerBlockWithItem(name, () -> new LegacyDetCordBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(0.1F, 0.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> explosiveCharge(String name, LegacyExplosiveChargeBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new LegacyExplosiveChargeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.1F, 0.0F)
                .sound(SoundType.METAL),
                kind));
    }

    private static RegistryObject<Block> legacyCharge(String name, LegacyChargeBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new LegacyChargeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.1F, 1.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .noCollission()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false),
                kind));
    }

    private static RegistryObject<Block> plasticExplosive(String name) {
        return registerBlockWithItem(name, () -> new LegacyPlasticExplosiveBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(2.0F, 2.0F)
                .sound(SoundType.METAL)));
    }

    private static RegistryObject<Block> legacyGlass(String name) {
        return registerBlockWithItem(name, () -> new LegacyNtmGlassBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> luminousLegacyGlass(String name) {
        return registerBlockWithItem(name, () -> new LegacyNtmGlassBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(0.3F)
                .lightLevel(state -> 5)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> quartzGlass(String name) {
        return registerBlockWithItem(name, () -> new LegacyNtmGlassBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(1.0F, 40.0F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> legacySandMix(String name) {
        return registerBlockWithItem(name, () -> new FallingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.5F)
                .sound(SoundType.SAND)));
    }

    private static RegistryObject<Block> trinititeGlass(String name) {
        return registerBlockWithItem(name, () -> new LegacyNtmGlassBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(0.3F)
                .lightLevel(state -> 5)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> moonTurf(String name) {
        return registerBlockWithItem(name, () -> new FallingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.5F)
                .sound(SoundType.SAND)));
    }

    private static RegistryObject<Block> reinforcedLaminate(String name) {
        return registerBlockWithItem(name, () -> new LegacyNtmGlassBlock(reinforcedLaminateProperties()));
    }

    private static RegistryObject<Block> reinforcedLaminatePane(String name) {
        return registerBlockWithItem(name, () -> new LegacyNtmGlassPaneBlock(reinforcedLaminateProperties()
                .forceSolidOff()));
    }

    private static RegistryObject<Block> capBlock(String name) {
        return registerBlockWithItem(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static BlockBehaviour.Properties reinforcedLaminateProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(15.0F, 300.0F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .requiresCorrectToolForDrops()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

    private static List<RegistryObject<Block>> simpleResourceBlocks(String... specs) {
        return Arrays.stream(specs)
                .filter(spec -> !spec.startsWith("__end__"))
                .map(spec -> {
                    String[] parts = spec.split(":", 2);
                    return simpleResourceBlock(parts[0], parts[1]);
                })
                .toList();
    }

    private static RegistryObject<Block> simpleResourceBlock(String name, String textureName) {
        return switch (name) {
            case "bobblehead" -> trinketResourceBlock(name, textureName, TrinketVariant.Kind.BOBBLEHEAD);
            case "snowglobe" -> trinketResourceBlock(name, textureName, TrinketVariant.Kind.SNOWGLOBE);
            case "plushie" -> trinketResourceBlock(name, textureName, TrinketVariant.Kind.PLUSHIE);
            default -> simpleBlockResourceBlock(name, textureName);
        };
    }

    private static RegistryObject<Block> simpleBlockResourceBlock(String name, String textureName) {
        return registerBlockWithItem(name, () -> switch (name) {
            case "lamp_demon" -> new LegacyDemonLampBlock(simpleResourceProperties(name, textureName).noOcclusion().lightLevel(state -> 15));
            case "deco_emitter" -> new LegacyEmitterBlock(simpleResourceProperties(name, textureName).noOcclusion());
            case "tape_recorder" -> new LegacyTapeRecorderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 15.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops());
            case "lantern" -> new LegacyLanternBlock(simpleResourceProperties(name, textureName).noOcclusion().lightLevel(state -> 15));
            case "spotlight_incandescent" -> LegacySpotlightBlock.incandescent(simpleResourceProperties(name, textureName).noOcclusion().lightLevel(state -> 15), true);
            case "spotlight_fluoro" -> LegacySpotlightBlock.fluoro(simpleResourceProperties(name, textureName).noOcclusion().lightLevel(state -> 15), true);
            case "spotlight_halogen" -> LegacySpotlightBlock.halogen(simpleResourceProperties(name, textureName).noOcclusion().lightLevel(state -> 15), true);
            case "floodlight" -> LegacyComplexShapeBlock.floodlight(simpleResourceProperties(name, textureName).noOcclusion());
            case "rebar" -> LegacyComplexShapeBlock.rebar(simpleResourceProperties(name, textureName).noOcclusion());
            case "wood_barrier" -> LegacyComplexShapeBlock.woodBarrier(simpleResourceProperties(name, textureName).noOcclusion());
            case "sandbags" -> LegacyComplexShapeBlock.sandbags(simpleResourceProperties(name, textureName).noOcclusion());
            case "block_yellowcake" -> new RadiatingFallingBlock(simpleResourceProperties(name, textureName),
                    RadiationConstants.YELLOWCAKE * RadiationConstants.BLOCK * RadiationConstants.POWDER_MULTIPLIER * 0.1F);
            case "block_fallout" -> new RadiatingFallingBlock(simpleResourceProperties(name, textureName),
                    RadiationConstants.YELLOWCAKE * RadiationConstants.BLOCK * RadiationConstants.POWDER_MULTIPLIER * 0.1F);
            case "block_red_phosphorus" -> new FallingBlock(simpleResourceProperties(name, textureName));
            case "block_scrap", "block_electrical_scrap", "gravel_obsidian" ->
                    new FallingBlock(simpleResourceProperties(name, textureName));
            case "dirt_dead", "dirt_oily", "sand_dirty", "sand_dirty_red", "stone_cracked" ->
                    new FallingBlock(oilSpotSurfaceProperties(name));
            case "stone_depth", "ore_depth_cinnebar", "ore_depth_zirconium", "ore_depth_borax",
                    "cluster_depth_iron", "cluster_depth_titanium", "cluster_depth_tungsten",
                    "depth_brick", "depth_tiles", "depth_nether_brick", "depth_nether_tiles", "depth_dnt",
                    "stone_depth_nether", "ore_depth_nether_neodymium" ->
                    new LegacyDepthBlock(name, simpleResourceProperties(name, textureName));
            case "block_waste", "block_waste_painted", "block_waste_vitrified" -> new LegacyNuclearWasteBlock(name, simpleResourceProperties(name, textureName));
            case "ore_fluorite", "deepslate_ore_fluorite", "ore_niter", "deepslate_ore_niter",
                    "ore_sulfur", "deepslate_ore_sulfur", "ore_nether_sulfur", "ore_lignite",
                    "deepslate_ore_lignite", "ore_nether_fire", "ore_cobalt", "deepslate_ore_cobalt",
                    "ore_nether_cobalt", "ore_cinnebar", "deepslate_ore_cinnebar", "ore_coltan",
                    "deepslate_ore_coltan",
                    "ore_oil", "ore_oil_empty", "ore_oil_sand", "block_trinitite", "block_meteor",
                    "block_meteor_cobble" ->
                    new LegacyOreBlock(name, simpleResourceProperties(name, textureName));
            case "block_u233", "block_u235", "block_neptunium", "block_polonium", "block_mox_fuel",
                    "block_plutonium", "block_pu238", "block_pu239", "block_pu240", "block_pu_mix",
                    "block_plutonium_fuel" ->
                    new LegacyHazardSourceBlock(name, simpleResourceProperties(name, textureName), LegacyHazardSourceBlock.Effect.RADFOG);
            case "block_schraranium", "block_schrabidium", "block_schrabidate", "block_solinium", "block_schrabidium_fuel" ->
                    new LegacyHazardSourceBlock(name, simpleResourceProperties(name, textureName), LegacyHazardSourceBlock.Effect.SCHRAB);
            case "crystal_virus" ->
                    new LegacyCrystalVirusBlock(crystalVirusProperties());
            case "crystal_hardened" ->
                    new RadiatingHazardBlock(name, crystalVirusProperties());
            case "glyphid_spawner" ->
                    new LegacyGlyphidSpawnerBlock(simpleResourceProperties(name, textureName));
            case "ore_uranium", "deepslate_ore_uranium", "ore_uranium_scorched", "ore_gneiss_uranium", "ore_gneiss_uranium_scorched",
                    "ore_nether_uranium", "ore_nether_uranium_scorched" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName).randomTicks(), GAS_RADON::get, true, false);
            case "ancient_scrap" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName).randomTicks(), GAS_RADON_TOMB::get, true, true, false, true);
            case "block_corium_cobble" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName).randomTicks(), GAS_RADON::get, true, true);
            case "ore_nether_coal" ->
                    new LegacyNetherCoalOreBlock(name, simpleResourceProperties(name, textureName), GAS_MONOXIDE::get, true, false);
            case "ore_asbestos", "deepslate_ore_asbestos", "ore_gneiss_asbestos", "stone_resource_asbestos",
                    "block_asbestos", "deco_asbestos", "brick_asbestos", "tile_lab_broken" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName).randomTicks(), GAS_ASBESTOS::get, true, false, true);
            case "tile_lab", "tile_lab_cracked" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName), GAS_ASBESTOS::get, true, false);
            default -> new RadiatingHazardBlock(name, simpleResourceProperties(name, textureName));
        }, block -> switch (name) {
            case "floodlight" -> new LegacyFloodlightBlockItem(block.get(), new Item.Properties());
            case "lamp_demon" -> new LegacyDemonLampBlockItem(block.get(), new Item.Properties());
            case "lantern" -> new LegacyLanternBlockItem(block.get(), new Item.Properties());
            default -> new BlockItem(block.get(), new Item.Properties());
        });
    }

    private static BlockBehaviour.Properties crystalVirusProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(15.0F, 3_600_000.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties oilSpotSurfaceProperties(String name) {
        return switch (name) {
            case "dirt_dead", "dirt_oily" -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5F)
                    .sound(SoundType.GRAVEL);
            case "sand_dirty", "sand_dirty_red" -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.5F)
                    .sound(SoundType.SAND);
            case "stone_cracked" -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(5.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops();
            default -> simpleResourceProperties(name, name);
        };
    }

    private static RegistryObject<Block> trinketResourceBlock(String name, String textureName, TrinketVariant.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new TrinketBlock(simpleResourceProperties(name, textureName).noOcclusion(), kind),
                block -> new TrinketBlockItem(block.get(), new Item.Properties(), kind));
    }

    private static BlockBehaviour.Properties simpleResourceProperties(String name, String textureName) {
        BlockBehaviour.Properties legacyBlastProperties = legacyBlastProperties(name);
        if (legacyBlastProperties != null) {
            return legacyBlastProperties;
        }
        if (isDepthBlock(name)) {
            return BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1.0F, name.equals("depth_dnt") ? 60_000.0F : 10.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops();
        }
        if (name.contains("sand") || name.contains("gravel") || name.contains("fallout") || name.contains("yellowcake") || textureName.equals("ash")) {
            return BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.6F, 1.0F)
                    .sound(SoundType.GRAVEL)
                    .requiresCorrectToolForDrops();
        }
        if (name.startsWith("block_") || name.startsWith("deco_") || name.startsWith("part_") || name.contains("reinforced")) {
            return BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops();
        }
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static boolean isDepthBlock(String name) {
        return switch (name) {
            case "stone_depth", "ore_depth_cinnebar", "ore_depth_zirconium", "ore_depth_borax",
                    "cluster_depth_iron", "cluster_depth_titanium", "cluster_depth_tungsten",
                    "depth_brick", "depth_tiles", "depth_nether_brick", "depth_nether_tiles", "depth_dnt",
                    "stone_depth_nether", "ore_depth_nether_neodymium" -> true;
            default -> false;
        };
    }

    private static BlockBehaviour.Properties legacyBlastProperties(String name) {
        return switch (name) {
            case "block_scrap" -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(2.5F, 5.0F)
                    .sound(SoundType.GRAVEL)
                    .requiresCorrectToolForDrops();
            case "block_electrical_scrap" -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.5F, 5.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops();
            case "gravel_obsidian" -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 240.0F)
                    .sound(SoundType.GRAVEL)
                    .requiresCorrectToolForDrops();
            case "reinforced_brick" -> stoneBlast(15.0F, 300.0F);
            case "reinforced_light" -> stoneBlast(15.0F, 80.0F).lightLevel(state -> 15);
            case "reinforced_sand" -> stoneBlast(15.0F, 40.0F);
            case "brick_concrete", "brick_concrete_mossy", "brick_concrete_marked" -> stoneBlast(15.0F, 160.0F);
            case "brick_concrete_cracked" -> stoneBlast(15.0F, 60.0F);
            case "brick_concrete_broken" -> stoneBlast(15.0F, 45.0F);
            case "brick_obsidian" -> stoneBlast(15.0F, 120.0F);
            case "brick_light" -> stoneBlast(5.0F, 20.0F);
            case "brick_compound" -> stoneBlast(15.0F, 400.0F);
            case "cmb_brick" -> stoneBlast(25.0F, 5000.0F);
            case "cmb_brick_reinforced" -> stoneBlast(25.0F, 50000.0F);
            case "brick_asbestos" -> stoneBlast(5.0F, 1000.0F);
            case "brick_fire" -> stoneBlast(5.0F, 35.0F);
            default -> null;
        };
    }

    private static BlockBehaviour.Properties stoneBlast(float hardness, float resistance) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(hardness, resistance)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static BlockBehaviour.Properties foundryProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0F, 10.0F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .requiresCorrectToolForDrops();
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> blockSupplier) {
        return registerBlockWithItem(name, blockSupplier, block -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> blockSupplier, Function<RegistryObject<T>, Item> itemFactory) {
        RegistryObject<T> block = BLOCKS.register(name, blockSupplier);
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        ModItems.registerBlockItem(name, () -> itemFactory.apply(block));
        return block;
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithItemName(String blockName, String itemName,
            Supplier<T> blockSupplier) {
        RegistryObject<T> block = BLOCKS.register(blockName, blockSupplier);
        BLOCKS_BY_LEGACY_NAME.put(blockName, block);
        ModItems.registerBlockItem(itemName, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithoutItem(String name, Supplier<T> blockSupplier) {
        RegistryObject<T> block = BLOCKS.register(name, blockSupplier);
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private ModBlocks() {
    }
}

