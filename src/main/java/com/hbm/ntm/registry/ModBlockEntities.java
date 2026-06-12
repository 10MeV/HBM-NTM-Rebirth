package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ArcWelderBlockEntity;
import com.hbm.ntm.blockentity.AshpitBlockEntity;
import com.hbm.ntm.blockentity.AssemblyFactoryBlockEntity;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.blockentity.Bat9000BlockEntity;
import com.hbm.ntm.blockentity.BasicMachineBlockEntity;
import com.hbm.ntm.blockentity.BigAssTankBlockEntity;
import com.hbm.ntm.blockentity.BoilerBlockEntity;
import com.hbm.ntm.blockentity.BombMultiBlockEntity;
import com.hbm.ntm.blockentity.CableDiodeBlockEntity;
import com.hbm.ntm.blockentity.CatalyticCrackerBlockEntity;
import com.hbm.ntm.blockentity.CatalyticReformerBlockEntity;
import com.hbm.ntm.blockentity.ChemicalFactoryBlockEntity;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.blockentity.ChimneyBlockEntity;
import com.hbm.ntm.blockentity.CompressorBlockEntity;
import com.hbm.ntm.blockentity.ConnectorBlockEntity;
import com.hbm.ntm.blockentity.CokerBlockEntity;
import com.hbm.ntm.blockentity.CustomNukeBlockEntity;
import com.hbm.ntm.blockentity.DeconBlockEntity;
import com.hbm.ntm.blockentity.ElectricPressBlockEntity;
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
import com.hbm.ntm.blockentity.FensuBlockEntity;
import com.hbm.ntm.blockentity.FractionTowerBlockEntity;
import com.hbm.ntm.blockentity.GasFlareBlockEntity;
import com.hbm.ntm.blockentity.HydrotreaterBlockEntity;
import com.hbm.ntm.blockentity.IndustrialSteamTurbineBlockEntity;
import com.hbm.ntm.blockentity.LargePylonBlockEntity;
import com.hbm.ntm.blockentity.LargeCoolingTowerBlockEntity;
import com.hbm.ntm.blockentity.LegacyChargeBlockEntity;
import com.hbm.ntm.blockentity.LegacyDemonLampBlockEntity;
import com.hbm.ntm.blockentity.LegacyLanternBlockEntity;
import com.hbm.ntm.blockentity.LegacyLightBlockEntity;
import com.hbm.ntm.blockentity.LegacyVolcanoCoreBlockEntity;
import com.hbm.ntm.blockentity.LegacyVisibleMachineBlockEntity;
import com.hbm.ntm.blockentity.LaunchPadBlockEntity;
import com.hbm.ntm.blockentity.LiquefactorBlockEntity;
import com.hbm.ntm.blockentity.MachineBatteryBlockEntity;
import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.blockentity.MachineLpw2BlockEntity;
import com.hbm.ntm.blockentity.MediumPylonBlockEntity;
import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.blockentity.NuclearDeviceBlockEntity;
import com.hbm.ntm.blockentity.OilDrillBlockEntity;
import com.hbm.ntm.blockentity.PileBreedingFuelBlockEntity;
import com.hbm.ntm.blockentity.PileFuelBlockEntity;
import com.hbm.ntm.blockentity.PileNeutronDetectorBlockEntity;
import com.hbm.ntm.blockentity.PileSourceBlockEntity;
import com.hbm.ntm.blockentity.PneumaticTubeBlockEntity;
import com.hbm.ntm.blockentity.PoweredRedCableBlockEntity;
import com.hbm.ntm.blockentity.PyroOvenBlockEntity;
import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.blockentity.RadarLargeBlockEntity;
import com.hbm.ntm.blockentity.RadarScreenBlockEntity;
import com.hbm.ntm.blockentity.RadioAutocalBlockEntity;
import com.hbm.ntm.blockentity.RadioTelexBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchControllerBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchCounterBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchLogicBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchReaderBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchReceiverBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchSenderBlockEntity;
import com.hbm.ntm.blockentity.RBMKPanelBlockEntity;
import com.hbm.ntm.blockentity.RedCableBlockEntity;
import com.hbm.ntm.blockentity.RedCableGaugeBlockEntity;
import com.hbm.ntm.blockentity.RefineryBlockEntity;
import com.hbm.ntm.blockentity.SatelliteDockBlockEntity;
import com.hbm.ntm.blockentity.SatelliteLinkerBlockEntity;
import com.hbm.ntm.blockentity.SolarBoilerBlockEntity;
import com.hbm.ntm.blockentity.SolidifierBlockEntity;
import com.hbm.ntm.blockentity.SmallCoolingTowerBlockEntity;
import com.hbm.ntm.blockentity.SmallPylonBlockEntity;
import com.hbm.ntm.blockentity.SoyuzCapsuleBlockEntity;
import com.hbm.ntm.blockentity.SoyuzLauncherBlockEntity;
import com.hbm.ntm.blockentity.SteamTurbineBlockEntity;
import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.blockentity.StorageCrateBlockEntity;
import com.hbm.ntm.blockentity.SubstationBlockEntity;
import com.hbm.ntm.blockentity.TrinketBlockEntity;
import com.hbm.ntm.blockentity.VacuumDistillBlockEntity;
import com.hbm.ntm.blockentity.VendingMachineBlockEntity;
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

    public static final RegistryObject<BlockEntityType<ElectricPressBlockEntity>> ELECTRIC_PRESS =
            BLOCK_ENTITIES.register("electric_press", () ->
                    BlockEntityType.Builder.of(ElectricPressBlockEntity::new, ModBlocks.MACHINE_EPRESS.get()).build(null));

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

    public static final RegistryObject<BlockEntityType<ConnectorBlockEntity>> RED_CONNECTOR =
            BLOCK_ENTITIES.register("red_connector", () ->
                    BlockEntityType.Builder.of(ConnectorBlockEntity::new,
                            ModBlocks.RED_CONNECTOR.get(),
                            ModBlocks.RED_CONNECTOR_SUPER.get()).build(null));

    public static final RegistryObject<BlockEntityType<SmallPylonBlockEntity>> RED_PYLON =
            BLOCK_ENTITIES.register("red_pylon", () ->
                    BlockEntityType.Builder.of(SmallPylonBlockEntity::new,
                            ModBlocks.RED_PYLON.get()).build(null));

    public static final RegistryObject<BlockEntityType<MediumPylonBlockEntity>> RED_PYLON_MEDIUM =
            BLOCK_ENTITIES.register("red_pylon_medium", () ->
                    BlockEntityType.Builder.of(MediumPylonBlockEntity::new,
                            ModBlocks.RED_PYLON_MEDIUM_WOOD.get(),
                            ModBlocks.RED_PYLON_MEDIUM_WOOD_TRANSFORMER.get(),
                            ModBlocks.RED_PYLON_MEDIUM_STEEL.get(),
                            ModBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER.get()).build(null));

    public static final RegistryObject<BlockEntityType<LargePylonBlockEntity>> RED_PYLON_LARGE =
            BLOCK_ENTITIES.register("red_pylon_large", () ->
                    BlockEntityType.Builder.of(LargePylonBlockEntity::new,
                            ModBlocks.RED_PYLON_LARGE.get()).build(null));

    public static final RegistryObject<BlockEntityType<SubstationBlockEntity>> SUBSTATION =
            BLOCK_ENTITIES.register("substation", () ->
                    BlockEntityType.Builder.of(SubstationBlockEntity::new,
                            ModBlocks.SUBSTATION.get()).build(null));

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

    public static final RegistryObject<BlockEntityType<RadioAutocalBlockEntity>> RADIO_AUTOCAL =
            BLOCK_ENTITIES.register("radio_autocal", () ->
                    BlockEntityType.Builder.of(RadioAutocalBlockEntity::new,
                            ModBlocks.RADIO_AUTOCAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadioTelexBlockEntity>> RADIO_TELEX =
            BLOCK_ENTITIES.register("radio_telex", () ->
                    BlockEntityType.Builder.of(RadioTelexBlockEntity::new,
                            ModBlocks.RADIO_TELEX.get()).build(null));

    public static final RegistryObject<BlockEntityType<RBMKPanelBlockEntity>> RBMK_PANEL =
            BLOCK_ENTITIES.register("rbmk_panel", () ->
                    BlockEntityType.Builder.of(RBMKPanelBlockEntity::new,
                            ModBlocks.RBMK_GAUGE.get(),
                            ModBlocks.RBMK_GRAPH.get(),
                            ModBlocks.RBMK_INDICATOR.get(),
                            ModBlocks.RBMK_KEY_PAD.get(),
                            ModBlocks.RBMK_LEVER.get(),
                            ModBlocks.RBMK_NUMITRON.get()).build(null));

    public static final RegistryObject<BlockEntityType<PileFuelBlockEntity>> PILE_FUEL =
            BLOCK_ENTITIES.register("pile_fuel", () ->
                    BlockEntityType.Builder.of(PileFuelBlockEntity::new,
                            ModBlocks.BLOCK_GRAPHITE_FUEL.get()).build(null));

    public static final RegistryObject<BlockEntityType<PileSourceBlockEntity>> PILE_SOURCE =
            BLOCK_ENTITIES.register("pile_source", () ->
                    BlockEntityType.Builder.of(PileSourceBlockEntity::new,
                            ModBlocks.BLOCK_GRAPHITE_PLUTONIUM.get(),
                            ModBlocks.BLOCK_GRAPHITE_SOURCE.get()).build(null));

    public static final RegistryObject<BlockEntityType<PileBreedingFuelBlockEntity>> PILE_BREEDING_FUEL =
            BLOCK_ENTITIES.register("pile_breeding_fuel", () ->
                    BlockEntityType.Builder.of(PileBreedingFuelBlockEntity::new,
                            ModBlocks.BLOCK_GRAPHITE_LITHIUM.get()).build(null));

    public static final RegistryObject<BlockEntityType<PileNeutronDetectorBlockEntity>> PILE_NEUTRON_DETECTOR =
            BLOCK_ENTITIES.register("pile_neutron_detector", () ->
                    BlockEntityType.Builder.of(PileNeutronDetectorBlockEntity::new,
                            ModBlocks.BLOCK_GRAPHITE_DETECTOR.get()).build(null));

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

    public static final RegistryObject<BlockEntityType<FensuBlockEntity>> MACHINE_FENSU =
            BLOCK_ENTITIES.register("machine_fensu", () ->
                    BlockEntityType.Builder.of(FensuBlockEntity::new, ModBlocks.MACHINE_FENSU.get()).build(null));

    public static final RegistryObject<BlockEntityType<MachineBatterySocketBlockEntity>> MACHINE_BATTERY_SOCKET =
            BLOCK_ENTITIES.register("machine_battery_socket", () ->
                    BlockEntityType.Builder.of(MachineBatterySocketBlockEntity::new, ModBlocks.MACHINE_BATTERY_SOCKET.get()).build(null));

    public static final RegistryObject<BlockEntityType<StorageCrateBlockEntity>> STORAGE_CRATE =
            BLOCK_ENTITIES.register("storage_crate", () ->
                    BlockEntityType.Builder.of(StorageCrateBlockEntity::new,
                            ModBlocks.CRATE_IRON.get(),
                            ModBlocks.CRATE_STEEL.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretChekhovBlockEntity>> TURRET_CHEKHOV =
            BLOCK_ENTITIES.register("turret_chekhov", () ->
                    BlockEntityType.Builder.of(TurretChekhovBlockEntity::new, ModBlocks.TURRET_CHEKHOV.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretFriendlyBlockEntity>> TURRET_FRIENDLY =
            BLOCK_ENTITIES.register("turret_friendly", () ->
                    BlockEntityType.Builder.of(TurretFriendlyBlockEntity::new, ModBlocks.TURRET_FRIENDLY.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretJeremyBlockEntity>> TURRET_JEREMY =
            BLOCK_ENTITIES.register("turret_jeremy", () ->
                    BlockEntityType.Builder.of(TurretJeremyBlockEntity::new, ModBlocks.TURRET_JEREMY.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretRichardBlockEntity>> TURRET_RICHARD =
            BLOCK_ENTITIES.register("turret_richard", () ->
                    BlockEntityType.Builder.of(TurretRichardBlockEntity::new, ModBlocks.TURRET_RICHARD.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretTauonBlockEntity>> TURRET_TAUON =
            BLOCK_ENTITIES.register("turret_tauon", () ->
                    BlockEntityType.Builder.of(TurretTauonBlockEntity::new, ModBlocks.TURRET_TAUON.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretHowardBlockEntity>> TURRET_HOWARD =
            BLOCK_ENTITIES.register("turret_howard", () ->
                    BlockEntityType.Builder.of(TurretHowardBlockEntity::new, ModBlocks.TURRET_HOWARD.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretSentryBlockEntity>> TURRET_SENTRY =
            BLOCK_ENTITIES.register("turret_sentry", () ->
                    BlockEntityType.Builder.of(TurretSentryBlockEntity::new, ModBlocks.TURRET_SENTRY.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretHowardDamagedBlockEntity>> TURRET_HOWARD_DAMAGED =
            BLOCK_ENTITIES.register("turret_howard_damaged", () ->
                    BlockEntityType.Builder.of(TurretHowardDamagedBlockEntity::new,
                            ModBlocks.TURRET_HOWARD_DAMAGED.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretSentryDamagedBlockEntity>> TURRET_SENTRY_DAMAGED =
            BLOCK_ENTITIES.register("turret_sentry_damaged", () ->
                    BlockEntityType.Builder.of(TurretSentryDamagedBlockEntity::new,
                            ModBlocks.TURRET_SENTRY_DAMAGED.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretMaxwellBlockEntity>> TURRET_MAXWELL =
            BLOCK_ENTITIES.register("turret_maxwell", () ->
                    BlockEntityType.Builder.of(TurretMaxwellBlockEntity::new,
                            ModBlocks.TURRET_MAXWELL.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretArtyBlockEntity>> TURRET_ARTY =
            BLOCK_ENTITIES.register("turret_arty", () ->
                    BlockEntityType.Builder.of(TurretArtyBlockEntity::new,
                            ModBlocks.TURRET_ARTY.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretHimarsBlockEntity>> TURRET_HIMARS =
            BLOCK_ENTITIES.register("turret_himars", () ->
                    BlockEntityType.Builder.of(TurretHimarsBlockEntity::new,
                            ModBlocks.TURRET_HIMARS.get()).build(null));

    public static final RegistryObject<BlockEntityType<TurretFritzBlockEntity>> TURRET_FRITZ =
            BLOCK_ENTITIES.register("turret_fritz", () ->
                    BlockEntityType.Builder.of(TurretFritzBlockEntity::new,
                            ModBlocks.TURRET_FRITZ.get()).build(null));

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

    public static final RegistryObject<BlockEntityType<LaunchPadBlockEntity>> LAUNCH_PAD =
            BLOCK_ENTITIES.register("launch_pad", () ->
                    BlockEntityType.Builder.of(LaunchPadBlockEntity::new,
                            ModBlocks.LAUNCH_PAD.get()).build(null));

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

    public static final RegistryObject<BlockEntityType<ChimneyBlockEntity>> CHIMNEY =
            BLOCK_ENTITIES.register("chimney", () ->
                    BlockEntityType.Builder.of(ChimneyBlockEntity::new,
                            ModBlocks.CHIMNEY_BRICK.get(),
                            ModBlocks.CHIMNEY_INDUSTRIAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<AshpitBlockEntity>> ASHPIT =
            BLOCK_ENTITIES.register("ashpit", () ->
                    BlockEntityType.Builder.of(AshpitBlockEntity::new,
                            ModBlocks.MACHINE_ASHPIT.get()).build(null));

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

    public static final RegistryObject<BlockEntityType<ArcWelderBlockEntity>> ARC_WELDER =
            BLOCK_ENTITIES.register("arc_welder", () ->
                    BlockEntityType.Builder.of(ArcWelderBlockEntity::new,
                            ModBlocks.MACHINE_ARC_WELDER.get()).build(null));

    public static final RegistryObject<BlockEntityType<MachineLpw2BlockEntity>> MACHINE_LPW2 =
            BLOCK_ENTITIES.register("machine_lpw2", () ->
                    BlockEntityType.Builder.of(MachineLpw2BlockEntity::new,
                            ModBlocks.MACHINE_LPW2.get()).build(null));

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
                            ModBlocks.MACHINE_BATTERY_REDD.get(),
                            ModBlocks.MACHINE_CENTRIFUGE.get(),
                            ModBlocks.MACHINE_GASCENT.get(),
                            ModBlocks.MACHINE_ORE_SLOPPER.get(),
                            ModBlocks.MACHINE_SAWMILL.get(),
                            ModBlocks.MACHINE_CRUCIBLE.get(),
                            ModBlocks.MACHINE_INTAKE.get(),
                            ModBlocks.MACHINE_DRAIN.get(),
                            ModBlocks.MACHINE_CHUNGUS.get(),
                            ModBlocks.MACHINE_HEPHAESTUS.get(),
                            ModBlocks.MACHINE_BOILER.get(),
                            ModBlocks.MACHINE_INDUSTRIAL_BOILER.get(),
                            ModBlocks.MACHINE_COMBUSTION_ENGINE.get(),
                            ModBlocks.PUMP_STEAM.get(),
                            ModBlocks.PUMP_ELECTRIC.get(),
                            ModBlocks.HEATER_HEATEX.get(),
                            ModBlocks.HEATER_FIREBOX.get(),
                            ModBlocks.HEATER_OVEN.get(),
                            ModBlocks.HEATER_OILBURNER.get(),
                            ModBlocks.HEATER_ELECTRIC.get(),
                            ModBlocks.MACHINE_CONDENSER_POWERED.get(),
                            ModBlocks.MACHINE_COMPRESSOR_COMPACT.get(),
                            ModBlocks.MACHINE_PUREX.get(),
                            ModBlocks.MACHINE_SILEX.get(),
                            ModBlocks.MACHINE_EXPOSURE_CHAMBER.get(),
                            ModBlocks.MACHINE_CYCLOTRON.get(),
                            ModBlocks.MACHINE_CRYSTALLIZER.get(),
                            ModBlocks.MACHINE_ELECTROLYSER.get(),
                            ModBlocks.MACHINE_SOLDERING_STATION.get(),
                            ModBlocks.MACHINE_MIXER.get(),
                            ModBlocks.MACHINE_RADIOLYSIS.get(),
                            ModBlocks.MACHINE_RADGEN.get(),
                            ModBlocks.MACHINE_ROTARY_FURNACE.get(),
                            ModBlocks.MACHINE_TURBOFAN.get(),
                            ModBlocks.MACHINE_TURBINEGAS.get(),
                            ModBlocks.MACHINE_AMMO_PRESS.get(),
                            ModBlocks.FURNACE_IRON.get(),
                            ModBlocks.FURNACE_STEEL.get(),
                            ModBlocks.FURNACE_COMBINATION.get(),
                            ModBlocks.MACHINE_BLAST_FURNACE.get(),
                            ModBlocks.MACHINE_ARC_FURNACE.get(),
                            ModBlocks.MACHINE_ANNIHILATOR.get(),
                            ModBlocks.MACHINE_FEL.get(),
                            ModBlocks.MACHINE_ORBUS.get(),
                            ModBlocks.MACHINE_MINING_LASER.get(),
                            ModBlocks.MACHINE_STRAND_CASTER.get(),
                            ModBlocks.MACHINE_WOOD_BURNER.get(),
                            ModBlocks.MACHINE_STIRLING.get(),
                            ModBlocks.MACHINE_STIRLING_STEEL.get(),
                            ModBlocks.MACHINE_STIRLING_CREATIVE.get(),
                            ModBlocks.MACHINE_DEUTERIUM_TOWER.get(),
                            ModBlocks.FRACTION_SPACER.get()).build(null));

    public static final RegistryObject<BlockEntityType<VendingMachineBlockEntity>> VENDING_MACHINE =
            BLOCK_ENTITIES.register("vending_machine", () ->
                    BlockEntityType.Builder.of(VendingMachineBlockEntity::new,
                            ModBlocks.VENDING_MACHINE.get()).build(null));

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

    public static final RegistryObject<BlockEntityType<BombMultiBlockEntity>> BOMB_MULTI =
            BLOCK_ENTITIES.register("bomb_multi", () ->
                    BlockEntityType.Builder.of(BombMultiBlockEntity::new, ModBlocks.BOMB_MULTI.get()).build(null));

    public static final RegistryObject<BlockEntityType<LegacyChargeBlockEntity>> LEGACY_CHARGE =
            BLOCK_ENTITIES.register("legacy_charge", () ->
                    BlockEntityType.Builder.of(LegacyChargeBlockEntity::new,
                            ModBlocks.CHARGE_DYNAMITE.get(),
                            ModBlocks.CHARGE_MINER.get(),
                            ModBlocks.CHARGE_C4.get(),
                            ModBlocks.CHARGE_SEMTEX.get()).build(null));

    public static final RegistryObject<BlockEntityType<LegacyVolcanoCoreBlockEntity>> LEGACY_VOLCANO_CORE =
            BLOCK_ENTITIES.register("legacy_volcano_core", () ->
                    BlockEntityType.Builder.of(LegacyVolcanoCoreBlockEntity::new,
                            ModBlocks.VOLCANO_CORE.get(),
                            ModBlocks.VOLCANO_RAD_CORE.get()).build(null));

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    private ModBlockEntities() {
    }
}
