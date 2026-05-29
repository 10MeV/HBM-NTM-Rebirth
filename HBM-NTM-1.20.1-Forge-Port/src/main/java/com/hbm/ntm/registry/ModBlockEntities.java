package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.blockentity.BasicMachineBlockEntity;
import com.hbm.ntm.blockentity.BigAssTankBlockEntity;
import com.hbm.ntm.blockentity.BoilerBlockEntity;
import com.hbm.ntm.blockentity.CatalyticCrackerBlockEntity;
import com.hbm.ntm.blockentity.CatalyticReformerBlockEntity;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.blockentity.CokerBlockEntity;
import com.hbm.ntm.blockentity.CustomNukeBlockEntity;
import com.hbm.ntm.blockentity.DeconBlockEntity;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.blockentity.FluidTankBlockEntity;
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
import com.hbm.ntm.blockentity.PneumaticTubeBlockEntity;
import com.hbm.ntm.blockentity.RedCableBlockEntity;
import com.hbm.ntm.blockentity.SolarBoilerBlockEntity;
import com.hbm.ntm.blockentity.SmallCoolingTowerBlockEntity;
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

    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE =
            BLOCK_ENTITIES.register("fluid_pipe", () ->
                    BlockEntityType.Builder.of(FluidPipeBlockEntity::new, ModBlocks.FLUID_DUCT_NEO.get()).build(null));

    public static final RegistryObject<BlockEntityType<PneumaticTubeBlockEntity>> PNEUMATIC_TUBE =
            BLOCK_ENTITIES.register("pneumatic_tube", () ->
                    BlockEntityType.Builder.of(PneumaticTubeBlockEntity::new, ModBlocks.PNEUMATIC_TUBE.get()).build(null));

    public static final RegistryObject<BlockEntityType<MachineBatteryBlockEntity>> MACHINE_BATTERY =
            BLOCK_ENTITIES.register("machine_battery", () ->
                    BlockEntityType.Builder.of(MachineBatteryBlockEntity::new, ModBlocks.MACHINE_BATTERY.get()).build(null));

    public static final RegistryObject<BlockEntityType<MachineBatterySocketBlockEntity>> MACHINE_BATTERY_SOCKET =
            BLOCK_ENTITIES.register("machine_battery_socket", () ->
                    BlockEntityType.Builder.of(MachineBatterySocketBlockEntity::new, ModBlocks.MACHINE_BATTERY_SOCKET.get()).build(null));

    public static final RegistryObject<BlockEntityType<MultiblockDummyBlockEntity>> MULTIBLOCK_DUMMY =
            BLOCK_ENTITIES.register("multiblock_dummy", () ->
                    BlockEntityType.Builder.of(MultiblockDummyBlockEntity::new, ModBlocks.DUMMY_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<AssemblyMachineBlockEntity>> ASSEMBLY_MACHINE =
            BLOCK_ENTITIES.register("assembly_machine", () ->
                    BlockEntityType.Builder.of(AssemblyMachineBlockEntity::new, ModBlocks.MACHINE_ASSEMBLY_MACHINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ChemicalPlantBlockEntity>> CHEMICAL_PLANT =
            BLOCK_ENTITIES.register("chemical_plant", () ->
                    BlockEntityType.Builder.of(ChemicalPlantBlockEntity::new, ModBlocks.MACHINE_CHEMICAL_PLANT.get()).build(null));

    public static final RegistryObject<BlockEntityType<LiquefactorBlockEntity>> LIQUEFACTOR =
            BLOCK_ENTITIES.register("liquefactor", () ->
                    BlockEntityType.Builder.of(LiquefactorBlockEntity::new, ModBlocks.MACHINE_LIQUEFACTOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<FluidTankBlockEntity>> FLUID_TANK =
            BLOCK_ENTITIES.register("fluid_tank", () ->
                    BlockEntityType.Builder.of(FluidTankBlockEntity::new, ModBlocks.MACHINE_FLUIDTANK.get()).build(null));

    public static final RegistryObject<BlockEntityType<BigAssTankBlockEntity>> BIG_ASS_TANK =
            BLOCK_ENTITIES.register("big_ass_tank", () ->
                    BlockEntityType.Builder.of(BigAssTankBlockEntity::new, ModBlocks.MACHINE_BIGASSTANK.get()).build(null));

    public static final RegistryObject<BlockEntityType<GasFlareBlockEntity>> GAS_FLARE =
            BLOCK_ENTITIES.register("gas_flare", () ->
                    BlockEntityType.Builder.of(GasFlareBlockEntity::new, ModBlocks.MACHINE_GASFLARE.get()).build(null));

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
                            ModBlocks.MACHINE_CHEMICAL_FACTORY.get(),
                            ModBlocks.MACHINE_REFINERY.get(),
                            ModBlocks.MACHINE_PYROOVEN.get(),
                            ModBlocks.MACHINE_SOLIDIFIER.get(),
                            ModBlocks.MACHINE_COMPRESSOR.get(),
                            ModBlocks.MACHINE_PUMPJACK.get(),
                            ModBlocks.MACHINE_CENTRIFUGE.get(),
                            ModBlocks.MACHINE_ORE_SLOPPER.get(),
                            ModBlocks.MACHINE_ASSEMBLY_FACTORY.get(),
                            ModBlocks.MACHINE_PUREX.get(),
                            ModBlocks.MACHINE_SILEX.get(),
                            ModBlocks.MACHINE_EXPOSURE_CHAMBER.get(),
                            ModBlocks.MACHINE_CYCLOTRON.get(),
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
