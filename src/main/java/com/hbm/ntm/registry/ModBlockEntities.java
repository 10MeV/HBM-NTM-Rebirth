package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.AssemblyFactoryBlockEntity;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.blockentity.Bat9000BlockEntity;
import com.hbm.ntm.blockentity.BasicMachineBlockEntity;
import com.hbm.ntm.blockentity.BigAssTankBlockEntity;
import com.hbm.ntm.blockentity.BoilerBlockEntity;
import com.hbm.ntm.blockentity.CableDiodeBlockEntity;
import com.hbm.ntm.blockentity.CatalyticCrackerBlockEntity;
import com.hbm.ntm.blockentity.CatalyticReformerBlockEntity;
import com.hbm.ntm.blockentity.ChemicalFactoryBlockEntity;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.blockentity.CompressorBlockEntity;
import com.hbm.ntm.blockentity.CokerBlockEntity;
import com.hbm.ntm.blockentity.CustomNukeBlockEntity;
import com.hbm.ntm.blockentity.DeconBlockEntity;
import com.hbm.ntm.blockentity.FluidDuctBoxBlockEntity;
import com.hbm.ntm.blockentity.FluidDuctExhaustBlockEntity;
import com.hbm.ntm.blockentity.FluidDuctGaugeBlockEntity;
import com.hbm.ntm.blockentity.FluidDuctPaintableBlockEntity;
import com.hbm.ntm.blockentity.FluidDuctPaintableExhaustBlockEntity;
import com.hbm.ntm.blockentity.FluidBarrelBlockEntity;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.blockentity.FluidPipeAnchorBlockEntity;
import com.hbm.ntm.blockentity.FluidPumpBlockEntity;
import com.hbm.ntm.blockentity.FluidCounterValveBlockEntity;
import com.hbm.ntm.blockentity.FluidTankBlockEntity;
import com.hbm.ntm.blockentity.FluidValveBlockEntity;
import com.hbm.ntm.blockentity.FractionTowerBlockEntity;
import com.hbm.ntm.blockentity.GasFlareBlockEntity;
import com.hbm.ntm.blockentity.HydrotreaterBlockEntity;
import com.hbm.ntm.blockentity.IndustrialSteamTurbineBlockEntity;
import com.hbm.ntm.blockentity.LargeCoolingTowerBlockEntity;
import com.hbm.ntm.blockentity.LegacyDemonLampBlockEntity;
import com.hbm.ntm.blockentity.LegacyLanternBlockEntity;
import com.hbm.ntm.blockentity.LegacyLightBlockEntity;
import com.hbm.ntm.blockentity.LegacyVisibleMachineBlockEntity;
import com.hbm.ntm.blockentity.LiquefactorBlockEntity;
import com.hbm.ntm.blockentity.MachineBatteryBlockEntity;
import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.blockentity.NuclearDeviceBlockEntity;
import com.hbm.ntm.blockentity.OilDrillBlockEntity;
import com.hbm.ntm.blockentity.PneumaticTubeBlockEntity;
import com.hbm.ntm.blockentity.PoweredRedCableBlockEntity;
import com.hbm.ntm.blockentity.PyroOvenBlockEntity;
import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.blockentity.RadarLargeBlockEntity;
import com.hbm.ntm.blockentity.RadarScreenBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchControllerBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchCounterBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchLogicBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchReaderBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchReceiverBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchSenderBlockEntity;
import com.hbm.ntm.blockentity.RedCableBlockEntity;
import com.hbm.ntm.blockentity.RedCableGaugeBlockEntity;
import com.hbm.ntm.blockentity.RefineryBlockEntity;
import com.hbm.ntm.blockentity.SatelliteDockBlockEntity;
import com.hbm.ntm.blockentity.SatelliteLinkerBlockEntity;
import com.hbm.ntm.blockentity.SolarBoilerBlockEntity;
import com.hbm.ntm.blockentity.SolidifierBlockEntity;
import com.hbm.ntm.blockentity.SmallCoolingTowerBlockEntity;
import com.hbm.ntm.blockentity.SoyuzCapsuleBlockEntity;
import com.hbm.ntm.blockentity.SoyuzLauncherBlockEntity;
import com.hbm.ntm.blockentity.SteamTurbineBlockEntity;
import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.blockentity.TrinketBlockEntity;
import com.hbm.ntm.blockentity.VacuumDistillBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HbmNtm.MOD_ID);

    public static final RegistryObject<BlockEntityType<BasicMachineBlockEntity>> BASIC_MACHINE =
            BLOCK_ENTITIES.register("basic_machine", () ->
                    BlockEntityType.Builder.of(BasicMachineBlockEntity::new, ModBlocks.MACHINE_PRESS.get()).build(null));

    public static final RegistryObject<BlockEntityType<BoilerBlockEntity>> BOILER =
            BLOCK_ENTITIES.register("boiler", () ->
                    BlockEntityType.Builder.of(BoilerBlockEntity::new, ModBlocks.MACHINE_BOILER_OFF.get()).build(null));

    public static final RegistryObject<BlockEntityType<SteamTurbineBlockEntity>> STEAM_TURBINE =
            BLOCK_ENTITIES.register("steam_turbine", () ->
                    BlockEntityType.Builder.of(SteamTurbineBlockEntity::new, ModBlocks.MACHINE_TURBINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<IndustrialSteamTurbineBlockEntity>> INDUSTRIAL_STEAM_TURBINE =
            BLOCK_ENTITIES.register("industrial_steam_turbine", () ->
                    BlockEntityType.Builder.of(IndustrialSteamTurbineBlockEntity::new,
                            ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<DeconBlockEntity>> DECON =
            BLOCK_ENTITIES.register("decon", () ->
                    BlockEntityType.Builder.of(DeconBlockEntity::new, ModBlocks.DECON.get()).build(null));

    public static final RegistryObject<BlockEntityType<RedCableBlockEntity>> RED_CABLE =
            BLOCK_ENTITIES.register("red_cable", () ->
                    BlockEntityType.Builder.of(RedCableBlockEntity::new, ModBlocks.RED_CABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<RedCableGaugeBlockEntity>> RED_CABLE_GAUGE =
            BLOCK_ENTITIES.register("red_cable_gauge", () ->
                    BlockEntityType.Builder.of(RedCableGaugeBlockEntity::new,
                            ModBlocks.RED_CABLE_GAUGE.get()).build(null));

    public static final RegistryObject<BlockEntityType<PoweredRedCableBlockEntity>> CABLE_SWITCH =
            BLOCK_ENTITIES.register("cable_switch", () ->
                    BlockEntityType.Builder.of(PoweredRedCableBlockEntity::switchEntity,
                            ModBlocks.CABLE_SWITCH.get()).build(null));

    public static final RegistryObject<BlockEntityType<PoweredRedCableBlockEntity>> CABLE_DETECTOR =
            BLOCK_ENTITIES.register("cable_detector", () ->
                    BlockEntityType.Builder.of(PoweredRedCableBlockEntity::detectorEntity,
                            ModBlocks.CABLE_DETECTOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<CableDiodeBlockEntity>> CABLE_DIODE =
            BLOCK_ENTITIES.register("cable_diode", () ->
                    BlockEntityType.Builder.of(CableDiodeBlockEntity::new, ModBlocks.CABLE_DIODE.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadioTorchSenderBlockEntity>> RADIO_TORCH_SENDER =
            BLOCK_ENTITIES.register("radio_torch_sender", () ->
                    BlockEntityType.Builder.of(RadioTorchSenderBlockEntity::new,
                            ModBlocks.RADIO_TORCH_SENDER.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadioTorchReceiverBlockEntity>> RADIO_TORCH_RECEIVER =
            BLOCK_ENTITIES.register("radio_torch_receiver", () ->
                    BlockEntityType.Builder.of(RadioTorchReceiverBlockEntity::new,
                            ModBlocks.RADIO_TORCH_RECEIVER.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadioTorchCounterBlockEntity>> RADIO_TORCH_COUNTER =
            BLOCK_ENTITIES.register("radio_torch_counter", () ->
                    BlockEntityType.Builder.of(RadioTorchCounterBlockEntity::new,
                            ModBlocks.RADIO_TORCH_COUNTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadioTorchLogicBlockEntity>> RADIO_TORCH_LOGIC =
            BLOCK_ENTITIES.register("radio_torch_logic", () ->
                    BlockEntityType.Builder.of(RadioTorchLogicBlockEntity::new,
                            ModBlocks.RADIO_TORCH_LOGIC.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadioTorchReaderBlockEntity>> RADIO_TORCH_READER =
            BLOCK_ENTITIES.register("radio_torch_reader", () ->
                    BlockEntityType.Builder.of(RadioTorchReaderBlockEntity::new,
                            ModBlocks.RADIO_TORCH_READER.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadioTorchControllerBlockEntity>> RADIO_TORCH_CONTROLLER =
            BLOCK_ENTITIES.register("radio_torch_controller", () ->
                    BlockEntityType.Builder.of(RadioTorchControllerBlockEntity::new,
                            ModBlocks.RADIO_TORCH_CONTROLLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE =
            BLOCK_ENTITIES.register("fluid_pipe", () ->
                    BlockEntityType.Builder.of(FluidPipeBlockEntity::new, ModBlocks.FLUID_DUCT_NEO.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidValveBlockEntity>> FLUID_VALVE =
            BLOCK_ENTITIES.register("fluid_valve", () ->
                    BlockEntityType.Builder.of(FluidValveBlockEntity::new,
                            ModBlocks.FLUID_VALVE.get(),
                            ModBlocks.FLUID_SWITCH.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidCounterValveBlockEntity>> FLUID_COUNTER_VALVE =
            BLOCK_ENTITIES.register("fluid_counter_valve", () ->
                    BlockEntityType.Builder.of(FluidCounterValveBlockEntity::new,
                            ModBlocks.FLUID_COUNTER_VALVE.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidPumpBlockEntity>> FLUID_PUMP =
            BLOCK_ENTITIES.register("fluid_pump", () ->
                    BlockEntityType.Builder.of(FluidPumpBlockEntity::new, ModBlocks.FLUID_PUMP.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidDuctBoxBlockEntity>> FLUID_DUCT_BOX =
            BLOCK_ENTITIES.register("fluid_duct_box", () ->
                    BlockEntityType.Builder.of(FluidDuctBoxBlockEntity::new,
                            ModBlocks.FLUID_DUCT_BOX.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidDuctGaugeBlockEntity>> FLUID_DUCT_GAUGE =
            BLOCK_ENTITIES.register("fluid_duct_gauge", () ->
                    BlockEntityType.Builder.of(FluidDuctGaugeBlockEntity::new,
                            ModBlocks.FLUID_DUCT_GAUGE.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidDuctExhaustBlockEntity>> FLUID_DUCT_EXHAUST =
            BLOCK_ENTITIES.register("fluid_duct_exhaust", () ->
                    BlockEntityType.Builder.of(FluidDuctExhaustBlockEntity::new,
                            ModBlocks.FLUID_DUCT_EXHAUST.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidDuctPaintableBlockEntity>> FLUID_DUCT_PAINTABLE =
            BLOCK_ENTITIES.register("fluid_duct_paintable", () ->
                    BlockEntityType.Builder.of(FluidDuctPaintableBlockEntity::new,
                            ModBlocks.FLUID_DUCT_PAINTABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidDuctPaintableExhaustBlockEntity>> FLUID_DUCT_PAINTABLE_EXHAUST =
            BLOCK_ENTITIES.register("fluid_duct_paintable_block_exhaust", () ->
                    BlockEntityType.Builder.of(FluidDuctPaintableExhaustBlockEntity::new,
                            ModBlocks.FLUID_DUCT_PAINTABLE_BLOCK_EXHAUST.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidPipeAnchorBlockEntity>> FLUID_PIPE_ANCHOR =
            BLOCK_ENTITIES.register("pipe_anchor", () ->
                    BlockEntityType.Builder.of(FluidPipeAnchorBlockEntity::new,
                            ModBlocks.PIPE_ANCHOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidBarrelBlockEntity>> FLUID_BARREL =
            BLOCK_ENTITIES.register("fluid_barrel", () ->
                    BlockEntityType.Builder.of(FluidBarrelBlockEntity::new,
                            ModBlocks.BARREL_PLASTIC.get(),
                            ModBlocks.BARREL_STEEL.get(),
                            ModBlocks.BARREL_TCALLOY.get(),
                            ModBlocks.BARREL_ANTIMATTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<PneumaticTubeBlockEntity>> PNEUMATIC_TUBE =
            BLOCK_ENTITIES.register("pneumatic_tube", () ->
                    BlockEntityType.Builder.of(PneumaticTubeBlockEntity::new, ModBlocks.PNEUMATIC_TUBE.get()).build(null));

    public static final RegistryObject<BlockEntityType<MachineBatteryBlockEntity>> MACHINE_BATTERY =
            BLOCK_ENTITIES.register("machine_battery", () ->
                    BlockEntityType.Builder.of(MachineBatteryBlockEntity::new, ModBlocks.MACHINE_BATTERY.get()).build(null));

    public static final RegistryObject<BlockEntityType<MachineBatterySocketBlockEntity>> MACHINE_BATTERY_SOCKET =
            BLOCK_ENTITIES.register("machine_battery_socket", () ->
                    BlockEntityType.Builder.of(MachineBatterySocketBlockEntity::new, ModBlocks.MACHINE_BATTERY_SOCKET.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadarBlockEntity>> MACHINE_RADAR =
            BLOCK_ENTITIES.register("machine_radar", () ->
                    BlockEntityType.Builder.of(RadarBlockEntity::new, ModBlocks.MACHINE_RADAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadarLargeBlockEntity>> MACHINE_RADAR_LARGE =
            BLOCK_ENTITIES.register("machine_radar_large", () ->
                    BlockEntityType.Builder.of(RadarLargeBlockEntity::new,
                            ModBlocks.MACHINE_RADAR_LARGE.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadarScreenBlockEntity>> MACHINE_RADAR_SCREEN =
            BLOCK_ENTITIES.register("radar_screen", () ->
                    BlockEntityType.Builder.of(RadarScreenBlockEntity::new,
                            ModBlocks.MACHINE_RADAR_SCREEN.get()).build(null));

    public static final RegistryObject<BlockEntityType<SatelliteLinkerBlockEntity>> MACHINE_SATLINKER =
            BLOCK_ENTITIES.register("machine_satlinker", () ->
                    BlockEntityType.Builder.of(SatelliteLinkerBlockEntity::new,
                            ModBlocks.MACHINE_SATLINKER.get()).build(null));

    public static final RegistryObject<BlockEntityType<SatelliteDockBlockEntity>> SAT_DOCK =
            BLOCK_ENTITIES.register("sat_dock", () ->
                    BlockEntityType.Builder.of(SatelliteDockBlockEntity::new,
                            ModBlocks.SAT_DOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<SoyuzCapsuleBlockEntity>> SOYUZ_CAPSULE =
            BLOCK_ENTITIES.register("soyuz_capsule", () ->
                    BlockEntityType.Builder.of(SoyuzCapsuleBlockEntity::new,
                            ModBlocks.SOYUZ_CAPSULE.get()).build(null));

    public static final RegistryObject<BlockEntityType<SoyuzLauncherBlockEntity>> SOYUZ_LAUNCHER =
            BLOCK_ENTITIES.register("soyuz_launcher", () ->
                    BlockEntityType.Builder.of(SoyuzLauncherBlockEntity::new,
                            ModBlocks.SOYUZ_LAUNCHER.get()).build(null));

    public static final RegistryObject<BlockEntityType<MultiblockDummyBlockEntity>> MULTIBLOCK_DUMMY =
            BLOCK_ENTITIES.register("multiblock_dummy", () ->
                    BlockEntityType.Builder.of(MultiblockDummyBlockEntity::new, ModBlocks.DUMMY_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<AssemblyMachineBlockEntity>> ASSEMBLY_MACHINE =
            BLOCK_ENTITIES.register("assembly_machine", () ->
                    BlockEntityType.Builder.of(AssemblyMachineBlockEntity::new, ModBlocks.MACHINE_ASSEMBLY_MACHINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ChemicalPlantBlockEntity>> CHEMICAL_PLANT =
            BLOCK_ENTITIES.register("chemical_plant", () ->
                    BlockEntityType.Builder.of(ChemicalPlantBlockEntity::new, ModBlocks.MACHINE_CHEMICAL_PLANT.get()).build(null));

    public static final RegistryObject<BlockEntityType<AssemblyFactoryBlockEntity>> ASSEMBLY_FACTORY =
            BLOCK_ENTITIES.register("assembly_factory", () ->
                    BlockEntityType.Builder.of(AssemblyFactoryBlockEntity::new, ModBlocks.MACHINE_ASSEMBLY_FACTORY.get()).build(null));

    public static final RegistryObject<BlockEntityType<ChemicalFactoryBlockEntity>> CHEMICAL_FACTORY =
            BLOCK_ENTITIES.register("chemical_factory", () ->
                    BlockEntityType.Builder.of(ChemicalFactoryBlockEntity::new, ModBlocks.MACHINE_CHEMICAL_FACTORY.get()).build(null));

    public static final RegistryObject<BlockEntityType<LiquefactorBlockEntity>> LIQUEFACTOR =
            BLOCK_ENTITIES.register("liquefactor", () ->
                    BlockEntityType.Builder.of(LiquefactorBlockEntity::new, ModBlocks.MACHINE_LIQUEFACTOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<RefineryBlockEntity>> REFINERY =
            BLOCK_ENTITIES.register("refinery", () ->
                    BlockEntityType.Builder.of(RefineryBlockEntity::new,
                            ModBlocks.MACHINE_REFINERY.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidTankBlockEntity>> FLUID_TANK =
            BLOCK_ENTITIES.register("fluid_tank", () ->
                    BlockEntityType.Builder.of(FluidTankBlockEntity::new, ModBlocks.MACHINE_FLUIDTANK.get()).build(null));

    public static final RegistryObject<BlockEntityType<Bat9000BlockEntity>> BAT9000 =
            BLOCK_ENTITIES.register("bat9000", () ->
                    BlockEntityType.Builder.of(Bat9000BlockEntity::new, ModBlocks.MACHINE_BAT9000.get()).build(null));

    public static final RegistryObject<BlockEntityType<BigAssTankBlockEntity>> BIG_ASS_TANK =
            BLOCK_ENTITIES.register("big_ass_tank", () ->
                    BlockEntityType.Builder.of(BigAssTankBlockEntity::new, ModBlocks.MACHINE_BIGASSTANK.get()).build(null));

    public static final RegistryObject<BlockEntityType<GasFlareBlockEntity>> GAS_FLARE =
            BLOCK_ENTITIES.register("gas_flare", () ->
                    BlockEntityType.Builder.of(GasFlareBlockEntity::new, ModBlocks.MACHINE_GASFLARE.get()).build(null));

    public static final RegistryObject<BlockEntityType<OilDrillBlockEntity>> OIL_DRILL =
            BLOCK_ENTITIES.register("oil_drill", () ->
                    BlockEntityType.Builder.of(OilDrillBlockEntity::new,
                            ModBlocks.MACHINE_WELL.get(),
                            ModBlocks.MACHINE_PUMPJACK.get(),
                            ModBlocks.MACHINE_FRACKING_TOWER.get()).build(null));

    public static final RegistryObject<BlockEntityType<CatalyticCrackerBlockEntity>> CATALYTIC_CRACKER =
            BLOCK_ENTITIES.register("catalytic_cracker", () ->
                    BlockEntityType.Builder.of(CatalyticCrackerBlockEntity::new,
                            ModBlocks.MACHINE_CATALYTIC_CRACKER.get()).build(null));

    public static final RegistryObject<BlockEntityType<CatalyticReformerBlockEntity>> CATALYTIC_REFORMER =
            BLOCK_ENTITIES.register("catalytic_reformer", () ->
                    BlockEntityType.Builder.of(CatalyticReformerBlockEntity::new,
                            ModBlocks.MACHINE_CATALYTIC_REFORMER.get()).build(null));

    public static final RegistryObject<BlockEntityType<VacuumDistillBlockEntity>> VACUUM_DISTILL =
            BLOCK_ENTITIES.register("vacuum_distill", () ->
                    BlockEntityType.Builder.of(VacuumDistillBlockEntity::new,
                            ModBlocks.MACHINE_VACUUM_DISTILL.get()).build(null));

    public static final RegistryObject<BlockEntityType<FractionTowerBlockEntity>> FRACTION_TOWER =
            BLOCK_ENTITIES.register("fraction_tower", () ->
                    BlockEntityType.Builder.of(FractionTowerBlockEntity::new,
                            ModBlocks.MACHINE_FRACTION_TOWER.get()).build(null));

    public static final RegistryObject<BlockEntityType<HydrotreaterBlockEntity>> HYDROTREATER =
            BLOCK_ENTITIES.register("hydrotreater", () ->
                    BlockEntityType.Builder.of(HydrotreaterBlockEntity::new,
                            ModBlocks.MACHINE_HYDROTREATER.get()).build(null));

    public static final RegistryObject<BlockEntityType<CokerBlockEntity>> COKER =
            BLOCK_ENTITIES.register("coker", () ->
                    BlockEntityType.Builder.of(CokerBlockEntity::new,
                            ModBlocks.MACHINE_COKER.get()).build(null));

    public static final RegistryObject<BlockEntityType<SolidifierBlockEntity>> SOLIDIFIER =
            BLOCK_ENTITIES.register("solidifier", () ->
                    BlockEntityType.Builder.of(SolidifierBlockEntity::new,
                            ModBlocks.MACHINE_SOLIDIFIER.get()).build(null));

    public static final RegistryObject<BlockEntityType<PyroOvenBlockEntity>> PYRO_OVEN =
            BLOCK_ENTITIES.register("pyro_oven", () ->
                    BlockEntityType.Builder.of(PyroOvenBlockEntity::new,
                            ModBlocks.MACHINE_PYROOVEN.get()).build(null));

    public static final RegistryObject<BlockEntityType<CompressorBlockEntity>> COMPRESSOR =
            BLOCK_ENTITIES.register("compressor", () ->
                    BlockEntityType.Builder.of(CompressorBlockEntity::new,
                            ModBlocks.MACHINE_COMPRESSOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE =
            BLOCK_ENTITIES.register("steam_engine", () ->
                    BlockEntityType.Builder.of(SteamEngineBlockEntity::new,
                            ModBlocks.MACHINE_STEAM_ENGINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<SolarBoilerBlockEntity>> SOLAR_BOILER =
            BLOCK_ENTITIES.register("solar_boiler", () ->
                    BlockEntityType.Builder.of(SolarBoilerBlockEntity::new,
                            ModBlocks.MACHINE_SOLAR_BOILER.get()).build(null));

    public static final RegistryObject<BlockEntityType<SmallCoolingTowerBlockEntity>> SMALL_COOLING_TOWER =
            BLOCK_ENTITIES.register("small_cooling_tower", () ->
                    BlockEntityType.Builder.of(SmallCoolingTowerBlockEntity::new,
                            ModBlocks.MACHINE_TOWER_SMALL.get()).build(null));

    public static final RegistryObject<BlockEntityType<LargeCoolingTowerBlockEntity>> LARGE_COOLING_TOWER =
            BLOCK_ENTITIES.register("large_cooling_tower", () ->
                    BlockEntityType.Builder.of(LargeCoolingTowerBlockEntity::new,
                            ModBlocks.MACHINE_TOWER_LARGE.get()).build(null));

    public static final RegistryObject<BlockEntityType<LegacyVisibleMachineBlockEntity>> LEGACY_VISIBLE_MACHINE =
            BLOCK_ENTITIES.register("legacy_visible_machine", () ->
                    BlockEntityType.Builder.of(
                            LegacyVisibleMachineBlockEntity::new,
                            ModBlocks.MACHINE_CENTRIFUGE.get(),
                            ModBlocks.MACHINE_GASCENT.get(),
                            ModBlocks.MACHINE_ORE_SLOPPER.get(),
                            ModBlocks.MACHINE_SAWMILL.get(),
                            ModBlocks.MACHINE_CRUCIBLE.get(),
                            ModBlocks.MACHINE_PUREX.get(),
                            ModBlocks.MACHINE_SILEX.get(),
                            ModBlocks.MACHINE_EXPOSURE_CHAMBER.get(),
                            ModBlocks.MACHINE_CYCLOTRON.get(),
                            ModBlocks.MACHINE_CRYSTALLIZER.get(),
                            ModBlocks.MACHINE_ELECTROLYSER.get(),
                            ModBlocks.MACHINE_ARC_WELDER.get(),
                            ModBlocks.MACHINE_SOLDERING_STATION.get(),
                            ModBlocks.MACHINE_MIXER.get(),
                            ModBlocks.MACHINE_RADIOLYSIS.get(),
                            ModBlocks.MACHINE_RADGEN.get(),
                            ModBlocks.MACHINE_ROTARY_FURNACE.get(),
                            ModBlocks.MACHINE_TURBOFAN.get(),
                            ModBlocks.MACHINE_TURBINEGAS.get()).build(null));

    public static final RegistryObject<BlockEntityType<TrinketBlockEntity>> TRINKET =
            BLOCK_ENTITIES.register("trinket", () ->
                    BlockEntityType.Builder.of(
                            TrinketBlockEntity::new,
                            ModBlocks.legacyBlock("bobblehead").get(),
                            ModBlocks.legacyBlock("snowglobe").get(),
                            ModBlocks.legacyBlock("plushie").get()).build(null));

    public static final RegistryObject<BlockEntityType<LegacyLightBlockEntity>> LEGACY_LIGHT =
            BLOCK_ENTITIES.register("legacy_light", () ->
                    BlockEntityType.Builder.of(
                            LegacyLightBlockEntity::new,
                            ModBlocks.legacyBlock("spotlight_incandescent").get(),
                            ModBlocks.legacyBlock("spotlight_fluoro").get(),
                            ModBlocks.legacyBlock("spotlight_halogen").get(),
                            ModBlocks.legacyBlock("floodlight").get()).build(null));

    public static final RegistryObject<BlockEntityType<LegacyDemonLampBlockEntity>> LEGACY_DEMON_LAMP =
            BLOCK_ENTITIES.register("legacy_demon_lamp", () ->
                    BlockEntityType.Builder.of(
                            LegacyDemonLampBlockEntity::new,
                            ModBlocks.legacyBlock("lamp_demon").get()).build(null));

    public static final RegistryObject<BlockEntityType<LegacyLanternBlockEntity>> LEGACY_LANTERN =
            BLOCK_ENTITIES.register("legacy_lantern", () ->
                    BlockEntityType.Builder.of(
                            LegacyLanternBlockEntity::new,
                            ModBlocks.legacyBlock("lantern").get()).build(null));

    public static final RegistryObject<BlockEntityType<NuclearDeviceBlockEntity>> NUCLEAR_DEVICE =
            BLOCK_ENTITIES.register("nuclear_device", () ->
                    BlockEntityType.Builder.of(
                            NuclearDeviceBlockEntity::new,
                            ModBlocks.NUKE_GADGET.get(),
                            ModBlocks.NUKE_BOY.get(),
                            ModBlocks.NUKE_MAN.get(),
                            ModBlocks.NUKE_TSAR.get(),
                            ModBlocks.NUKE_MIKE.get(),
                            ModBlocks.NUKE_PROTOTYPE.get(),
                            ModBlocks.NUKE_FLEIJA.get(),
                            ModBlocks.NUKE_SOLINIUM.get(),
                            ModBlocks.NUKE_N2.get()).build(null));

    public static final RegistryObject<BlockEntityType<CustomNukeBlockEntity>> CUSTOM_NUKE =
            BLOCK_ENTITIES.register("custom_nuke", () ->
                    BlockEntityType.Builder.of(CustomNukeBlockEntity::new, ModBlocks.NUKE_CUSTOM.get()).build(null));

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    private ModBlockEntities() {
    }
}
