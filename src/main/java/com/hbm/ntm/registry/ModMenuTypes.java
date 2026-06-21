package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.AnnihilatorMenu;
import com.hbm.ntm.menu.AnvilMenu;
import com.hbm.ntm.menu.ArcFurnaceMenu;
import com.hbm.ntm.menu.ArcWelderMenu;
import com.hbm.ntm.menu.ArmorTableMenu;
import com.hbm.ntm.menu.AshpitMenu;
import com.hbm.ntm.menu.BalefireBombMenu;
import com.hbm.ntm.menu.AssemblyFactoryMenu;
import com.hbm.ntm.menu.AssemblyMachineMenu;
import com.hbm.ntm.menu.AmmoBagMenu;
import com.hbm.ntm.menu.AmmoPressMenu;
import com.hbm.ntm.menu.BatteryReddMenu;
import com.hbm.ntm.menu.BasicMachineMenu;
import com.hbm.ntm.menu.BlastFurnaceMenu;
import com.hbm.ntm.menu.BrickFurnaceMenu;
import com.hbm.ntm.menu.BreedingReactorMenu;
import com.hbm.ntm.menu.BombMultiMenu;
import com.hbm.ntm.menu.CasingBagMenu;
import com.hbm.ntm.menu.ChemicalFactoryMenu;
import com.hbm.ntm.menu.ChemicalPlantMenu;
import com.hbm.ntm.menu.CompactLauncherMenu;
import com.hbm.ntm.menu.CompressorMenu;
import com.hbm.ntm.menu.CombustionEngineMenu;
import com.hbm.ntm.menu.CombinationOvenMenu;
import com.hbm.ntm.menu.CrucibleMenu;
import com.hbm.ntm.menu.CrateMenu;
import com.hbm.ntm.menu.CustomNukeMenu;
import com.hbm.ntm.menu.CyclotronMenu;
import com.hbm.ntm.menu.DiFurnaceMenu;
import com.hbm.ntm.menu.DiFurnaceRtgMenu;
import com.hbm.ntm.menu.DieselGeneratorMenu;
import com.hbm.ntm.menu.ElectrolyserMenu;
import com.hbm.ntm.menu.ElectricFurnaceMenu;
import com.hbm.ntm.menu.ElectricPressMenu;
import com.hbm.ntm.menu.ExposureChamberMenu;
import com.hbm.ntm.menu.FluidTankMenu;
import com.hbm.ntm.menu.FireboxHeaterMenu;
import com.hbm.ntm.menu.FelMenu;
import com.hbm.ntm.menu.ForceFieldMenu;
import com.hbm.ntm.menu.FusionBreederMenu;
import com.hbm.ntm.menu.FusionKlystronMenu;
import com.hbm.ntm.menu.FusionPlasmaForgeMenu;
import com.hbm.ntm.menu.FusionTorusMenu;
import com.hbm.ntm.menu.GasCentMenu;
import com.hbm.ntm.menu.GasFlareMenu;
import com.hbm.ntm.menu.HeaterHeatexMenu;
import com.hbm.ntm.menu.ICFPressMenu;
import com.hbm.ntm.menu.ICFReactorMenu;
import com.hbm.ntm.menu.LaunchPadMenu;
import com.hbm.ntm.menu.LaunchTableMenu;
import com.hbm.ntm.menu.LegacyFurnaceMenu;
import com.hbm.ntm.menu.LiquefactorMenu;
import com.hbm.ntm.menu.MachineBatteryMenu;
import com.hbm.ntm.menu.MachineBatterySocketMenu;
import com.hbm.ntm.menu.MassStorageMenu;
import com.hbm.ntm.menu.MissileAssemblyMenu;
import com.hbm.ntm.menu.MiningLaserMenu;
import com.hbm.ntm.menu.MixerMenu;
import com.hbm.ntm.menu.NuclearDeviceMenu;
import com.hbm.ntm.menu.OilDrillMenu;
import com.hbm.ntm.menu.OilburnerMenu;
import com.hbm.ntm.menu.OreSlopperMenu;
import com.hbm.ntm.menu.ParticleAcceleratorMenu;
import com.hbm.ntm.menu.PWRMenu;
import com.hbm.ntm.menu.PyroOvenMenu;
import com.hbm.ntm.menu.PrecassMenu;
import com.hbm.ntm.menu.ProcessingMachineMenu;
import com.hbm.ntm.menu.PurexMenu;
import com.hbm.ntm.menu.RadioAutocalMenu;
import com.hbm.ntm.menu.RadGenMenu;
import com.hbm.ntm.menu.ResearchReactorMenu;
import com.hbm.ntm.menu.RadioTelexMenu;
import com.hbm.ntm.menu.RadioTorchMenu;
import com.hbm.ntm.menu.RBMKAutoloaderMenu;
import com.hbm.ntm.menu.RBMKBoilerMenu;
import com.hbm.ntm.menu.RBMKControlAutoMenu;
import com.hbm.ntm.menu.RBMKControlMenu;
import com.hbm.ntm.menu.RBMKConsoleMenu;
import com.hbm.ntm.menu.RBMKHeaterMenu;
import com.hbm.ntm.menu.RBMKOutgasserMenu;
import com.hbm.ntm.menu.RBMKPanelMenu;
import com.hbm.ntm.menu.RBMKRodMenu;
import com.hbm.ntm.menu.RBMKStorageMenu;
import com.hbm.ntm.menu.RadarMenu;
import com.hbm.ntm.menu.RadiolysisMenu;
import com.hbm.ntm.menu.RemoteFluidMachineMenu;
import com.hbm.ntm.menu.RefineryMenu;
import com.hbm.ntm.menu.RotaryFurnaceMenu;
import com.hbm.ntm.menu.RtgMenu;
import com.hbm.ntm.menu.RtgFurnaceMenu;
import com.hbm.ntm.menu.RustedLaunchPadMenu;
import com.hbm.ntm.menu.SatelliteDockMenu;
import com.hbm.ntm.menu.SatelliteLinkerMenu;
import com.hbm.ntm.menu.SolidifierMenu;
import com.hbm.ntm.menu.SilexMenu;
import com.hbm.ntm.menu.ShredderMenu;
import com.hbm.ntm.menu.SirenMenu;
import com.hbm.ntm.menu.SolderingStationMenu;
import com.hbm.ntm.menu.SoyuzCapsuleMenu;
import com.hbm.ntm.menu.SoyuzLauncherMenu;
import com.hbm.ntm.menu.StrandCasterMenu;
import com.hbm.ntm.menu.ToolAbilityMenu;
import com.hbm.ntm.menu.TurbofanMenu;
import com.hbm.ntm.menu.TurbineGasMenu;
import com.hbm.ntm.menu.TurretMenu;
import com.hbm.ntm.menu.WeaponTableMenu;
import com.hbm.ntm.menu.WoodBurnerMenu;
import com.hbm.ntm.menu.WasteDrumMenu;
import com.hbm.ntm.menu.WatzReactorMenu;
import com.hbm.ntm.menu.ZirnoxReactorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, HbmNtm.MOD_ID);

    public static final RegistryObject<MenuType<BasicMachineMenu>> BASIC_MACHINE =
            MENUS.register("basic_machine", () -> IForgeMenuType.create(BasicMachineMenu::new));

    public static final RegistryObject<MenuType<AnvilMenu>> ANVIL =
            MENUS.register("anvil", () -> IForgeMenuType.create(AnvilMenu::new));

    public static final RegistryObject<MenuType<ElectricPressMenu>> ELECTRIC_PRESS =
            MENUS.register("electric_press", () -> IForgeMenuType.create(ElectricPressMenu::new));

    public static final RegistryObject<MenuType<AmmoBagMenu>> AMMO_BAG =
            MENUS.register("ammo_bag", () -> IForgeMenuType.create(AmmoBagMenu::new));

    public static final RegistryObject<MenuType<CasingBagMenu>> CASING_BAG =
            MENUS.register("casing_bag", () -> IForgeMenuType.create(CasingBagMenu::new));

    public static final RegistryObject<MenuType<ArmorTableMenu>> ARMOR_TABLE =
            MENUS.register("armor_table", () -> IForgeMenuType.create(ArmorTableMenu::new));

    public static final RegistryObject<MenuType<WeaponTableMenu>> WEAPON_TABLE =
            MENUS.register("weapon_table", () -> IForgeMenuType.create(WeaponTableMenu::new));

    public static final RegistryObject<MenuType<SirenMenu>> SIREN =
            MENUS.register("siren", () -> IForgeMenuType.create(SirenMenu::new));

    public static final RegistryObject<MenuType<AssemblyMachineMenu>> ASSEMBLY_MACHINE =
            MENUS.register("assembly_machine", () -> IForgeMenuType.create(AssemblyMachineMenu::new));

    public static final RegistryObject<MenuType<ChemicalPlantMenu>> CHEMICAL_PLANT =
            MENUS.register("chemical_plant", () -> IForgeMenuType.create(ChemicalPlantMenu::new));

    public static final RegistryObject<MenuType<AssemblyFactoryMenu>> ASSEMBLY_FACTORY =
            MENUS.register("assembly_factory", () -> IForgeMenuType.create(AssemblyFactoryMenu::new));

    public static final RegistryObject<MenuType<ChemicalFactoryMenu>> CHEMICAL_FACTORY =
            MENUS.register("chemical_factory", () -> IForgeMenuType.create(ChemicalFactoryMenu::new));

    public static final RegistryObject<MenuType<CompressorMenu>> COMPRESSOR =
            MENUS.register("compressor", () -> IForgeMenuType.create(CompressorMenu::new));

    public static final RegistryObject<MenuType<CombustionEngineMenu>> COMBUSTION_ENGINE =
            MENUS.register("combustion_engine", () -> IForgeMenuType.create(CombustionEngineMenu::new));

    public static final RegistryObject<MenuType<DieselGeneratorMenu>> DIESEL_GENERATOR =
            MENUS.register("diesel_generator", () -> IForgeMenuType.create(DieselGeneratorMenu::new));

    public static final RegistryObject<MenuType<ArcWelderMenu>> ARC_WELDER =
            MENUS.register("arc_welder", () -> IForgeMenuType.create(ArcWelderMenu::new));

    public static final RegistryObject<MenuType<ArcFurnaceMenu>> ARC_FURNACE =
            MENUS.register("arc_furnace", () -> IForgeMenuType.create(ArcFurnaceMenu::new));

    public static final RegistryObject<MenuType<MiningLaserMenu>> MINING_LASER =
            MENUS.register("mining_laser", () -> IForgeMenuType.create(MiningLaserMenu::new));

    public static final RegistryObject<MenuType<ProcessingMachineMenu>> PROCESSING_MACHINE =
            MENUS.register("processing_machine", () -> IForgeMenuType.create(ProcessingMachineMenu::new));

    public static final RegistryObject<MenuType<ShredderMenu>> SHREDDER =
            MENUS.register("shredder", () -> IForgeMenuType.create(ShredderMenu::new));

    public static final RegistryObject<MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE =
            MENUS.register("electric_furnace", () -> IForgeMenuType.create(ElectricFurnaceMenu::new));

    public static final RegistryObject<MenuType<DiFurnaceMenu>> DIFURNACE =
            MENUS.register("difurnace", () -> IForgeMenuType.create(DiFurnaceMenu::new));

    public static final RegistryObject<MenuType<BrickFurnaceMenu>> BRICK_FURNACE =
            MENUS.register("brick_furnace", () -> IForgeMenuType.create(BrickFurnaceMenu::new));

    public static final RegistryObject<MenuType<DiFurnaceRtgMenu>> DIFURNACE_RTG =
            MENUS.register("difurnace_rtg", () -> IForgeMenuType.create(DiFurnaceRtgMenu::new));

    public static final RegistryObject<MenuType<RtgFurnaceMenu>> RTG_FURNACE =
            MENUS.register("rtg_furnace", () -> IForgeMenuType.create(RtgFurnaceMenu::new));

    public static final RegistryObject<MenuType<LegacyFurnaceMenu>> LEGACY_FURNACE =
            MENUS.register("legacy_furnace", () -> IForgeMenuType.create(LegacyFurnaceMenu::new));

    public static final RegistryObject<MenuType<AmmoPressMenu>> AMMO_PRESS =
            MENUS.register("ammo_press", () -> IForgeMenuType.create(AmmoPressMenu::new));

    public static final RegistryObject<MenuType<RadGenMenu>> RADGEN =
            MENUS.register("radgen", () -> IForgeMenuType.create(RadGenMenu::new));

    public static final RegistryObject<MenuType<ResearchReactorMenu>> RESEARCH_REACTOR =
            MENUS.register("research_reactor", () -> IForgeMenuType.create(ResearchReactorMenu::new));

    public static final RegistryObject<MenuType<BreedingReactorMenu>> BREEDING_REACTOR =
            MENUS.register("breeding_reactor", () -> IForgeMenuType.create(BreedingReactorMenu::new));

    public static final RegistryObject<MenuType<ZirnoxReactorMenu>> ZIRNOX_REACTOR =
            MENUS.register("zirnox_reactor", () -> IForgeMenuType.create(ZirnoxReactorMenu::new));

    public static final RegistryObject<MenuType<WatzReactorMenu>> WATZ_REACTOR =
            MENUS.register("watz_reactor", () -> IForgeMenuType.create(WatzReactorMenu::new));

    public static final RegistryObject<MenuType<ICFReactorMenu>> ICF_REACTOR =
            MENUS.register("icf_reactor", () -> IForgeMenuType.create(ICFReactorMenu::new));

    public static final RegistryObject<MenuType<FusionTorusMenu>> FUSION_TORUS =
            MENUS.register("fusion_torus", () -> IForgeMenuType.create(FusionTorusMenu::new));

    public static final RegistryObject<MenuType<FusionKlystronMenu>> FUSION_KLYSTRON =
            MENUS.register("fusion_klystron", () -> IForgeMenuType.create(FusionKlystronMenu::new));

    public static final RegistryObject<MenuType<FusionBreederMenu>> FUSION_BREEDER =
            MENUS.register("fusion_breeder", () -> IForgeMenuType.create(FusionBreederMenu::new));

    public static final RegistryObject<MenuType<FusionPlasmaForgeMenu>> FUSION_PLASMA_FORGE =
            MENUS.register("fusion_plasma_forge", () -> IForgeMenuType.create(FusionPlasmaForgeMenu::new));

    public static final RegistryObject<MenuType<ICFPressMenu>> ICF_PRESS =
            MENUS.register("icf_press", () -> IForgeMenuType.create(ICFPressMenu::new));

    public static final RegistryObject<MenuType<PWRMenu>> PWR =
            MENUS.register("pwr", () -> IForgeMenuType.create(PWRMenu::new));

    public static final RegistryObject<MenuType<WasteDrumMenu>> WASTE_DRUM =
            MENUS.register("waste_drum", () -> IForgeMenuType.create(WasteDrumMenu::new));

    public static final RegistryObject<MenuType<WoodBurnerMenu>> WOOD_BURNER =
            MENUS.register("wood_burner", () -> IForgeMenuType.create(WoodBurnerMenu::new));

    public static final RegistryObject<MenuType<TurbofanMenu>> TURBOFAN =
            MENUS.register("turbofan", () -> IForgeMenuType.create(TurbofanMenu::new));

    public static final RegistryObject<MenuType<TurbineGasMenu>> TURBINE_GAS =
            MENUS.register("turbine_gas", () -> IForgeMenuType.create(TurbineGasMenu::new));

    public static final RegistryObject<MenuType<CombinationOvenMenu>> COMBINATION_OVEN =
            MENUS.register("combination_oven", () -> IForgeMenuType.create(CombinationOvenMenu::new));

    public static final RegistryObject<MenuType<BlastFurnaceMenu>> BLAST_FURNACE =
            MENUS.register("blast_furnace", () -> IForgeMenuType.create(BlastFurnaceMenu::new));

    public static final RegistryObject<MenuType<RadiolysisMenu>> RADIOLYSIS =
            MENUS.register("radiolysis", () -> IForgeMenuType.create(RadiolysisMenu::new));

    public static final RegistryObject<MenuType<RtgMenu>> RTG =
            MENUS.register("rtg", () -> IForgeMenuType.create(RtgMenu::new));

    public static final RegistryObject<MenuType<RotaryFurnaceMenu>> ROTARY_FURNACE =
            MENUS.register("rotary_furnace", () -> IForgeMenuType.create(RotaryFurnaceMenu::new));

    public static final RegistryObject<MenuType<StrandCasterMenu>> STRAND_CASTER =
            MENUS.register("strand_caster", () -> IForgeMenuType.create(StrandCasterMenu::new));

    public static final RegistryObject<MenuType<CrucibleMenu>> CRUCIBLE =
            MENUS.register("crucible", () -> IForgeMenuType.create(CrucibleMenu::new));

    public static final RegistryObject<MenuType<ExposureChamberMenu>> EXPOSURE_CHAMBER =
            MENUS.register("exposure_chamber", () -> IForgeMenuType.create(ExposureChamberMenu::new));

    public static final RegistryObject<MenuType<SolderingStationMenu>> SOLDERING_STATION =
            MENUS.register("soldering_station", () -> IForgeMenuType.create(SolderingStationMenu::new));

    public static final RegistryObject<MenuType<GasCentMenu>> GAS_CENT =
            MENUS.register("gas_cent", () -> IForgeMenuType.create(GasCentMenu::new));

    public static final RegistryObject<MenuType<OreSlopperMenu>> ORE_SLOPPER =
            MENUS.register("ore_slopper", () -> IForgeMenuType.create(OreSlopperMenu::new));

    public static final RegistryObject<MenuType<SilexMenu>> SILEX =
            MENUS.register("silex", () -> IForgeMenuType.create(SilexMenu::new));

    public static final RegistryObject<MenuType<CyclotronMenu>> CYCLOTRON =
            MENUS.register("cyclotron", () -> IForgeMenuType.create(CyclotronMenu::new));

    public static final RegistryObject<MenuType<FelMenu>> FEL =
            MENUS.register("fel", () -> IForgeMenuType.create(FelMenu::new));

    public static final RegistryObject<MenuType<ForceFieldMenu>> FORCE_FIELD =
            MENUS.register("force_field", () -> IForgeMenuType.create(ForceFieldMenu::new));

    public static final RegistryObject<MenuType<BatteryReddMenu>> BATTERY_REDD =
            MENUS.register("battery_redd", () -> IForgeMenuType.create(BatteryReddMenu::new));

    public static final RegistryObject<MenuType<AnnihilatorMenu>> ANNIHILATOR =
            MENUS.register("annihilator", () -> IForgeMenuType.create(AnnihilatorMenu::new));

    public static final RegistryObject<MenuType<MixerMenu>> MIXER =
            MENUS.register("mixer", () -> IForgeMenuType.create(MixerMenu::new));

    public static final RegistryObject<MenuType<ElectrolyserMenu>> ELECTROLYSER =
            MENUS.register("electrolyser", () -> IForgeMenuType.create(ElectrolyserMenu::new));

    public static final RegistryObject<MenuType<PrecassMenu>> PRECASS =
            MENUS.register("precass", () -> IForgeMenuType.create(PrecassMenu::new));

    public static final RegistryObject<MenuType<PurexMenu>> PUREX =
            MENUS.register("purex", () -> IForgeMenuType.create(PurexMenu::new));

    public static final RegistryObject<MenuType<LiquefactorMenu>> LIQUEFACTOR =
            MENUS.register("liquefactor", () -> IForgeMenuType.create(LiquefactorMenu::new));

    public static final RegistryObject<MenuType<RefineryMenu>> REFINERY =
            MENUS.register("refinery", () -> IForgeMenuType.create(RefineryMenu::new));

    public static final RegistryObject<MenuType<SolidifierMenu>> SOLIDIFIER =
            MENUS.register("solidifier", () -> IForgeMenuType.create(SolidifierMenu::new));

    public static final RegistryObject<MenuType<OilDrillMenu>> OIL_DRILL =
            MENUS.register("oil_drill", () -> IForgeMenuType.create(OilDrillMenu::new));

    public static final RegistryObject<MenuType<GasFlareMenu>> GAS_FLARE =
            MENUS.register("gas_flare", () -> IForgeMenuType.create(GasFlareMenu::new));

    public static final RegistryObject<MenuType<PyroOvenMenu>> PYRO_OVEN =
            MENUS.register("pyro_oven", () -> IForgeMenuType.create(PyroOvenMenu::new));

    public static final RegistryObject<MenuType<AshpitMenu>> ASHPIT =
            MENUS.register("ashpit", () -> IForgeMenuType.create(AshpitMenu::new));

    public static final RegistryObject<MenuType<FireboxHeaterMenu>> FIREBOX_HEATER =
            MENUS.register("firebox_heater", () -> IForgeMenuType.create(FireboxHeaterMenu::new));

    public static final RegistryObject<MenuType<OilburnerMenu>> OILBURNER =
            MENUS.register("oilburner", () -> IForgeMenuType.create(OilburnerMenu::new));

    public static final RegistryObject<MenuType<HeaterHeatexMenu>> HEATER_HEATEX =
            MENUS.register("heater_heatex", () -> IForgeMenuType.create(HeaterHeatexMenu::new));

    public static final RegistryObject<MenuType<FluidTankMenu>> FLUID_TANK =
            MENUS.register("fluid_tank", () -> IForgeMenuType.create(FluidTankMenu::new));

    public static final RegistryObject<MenuType<RemoteFluidMachineMenu>> REMOTE_FLUID_MACHINE =
            MENUS.register("remote_fluid_machine", () -> IForgeMenuType.create(RemoteFluidMachineMenu::new));

    public static final RegistryObject<MenuType<MachineBatteryMenu>> MACHINE_BATTERY =
            MENUS.register("machine_battery", () -> IForgeMenuType.create(MachineBatteryMenu::new));

    public static final RegistryObject<MenuType<MachineBatterySocketMenu>> MACHINE_BATTERY_SOCKET =
            MENUS.register("machine_battery_socket", () -> IForgeMenuType.create(MachineBatterySocketMenu::new));

    public static final RegistryObject<MenuType<CrateMenu>> STORAGE_CRATE =
            MENUS.register("storage_crate", () -> IForgeMenuType.create(CrateMenu::new));

    public static final RegistryObject<MenuType<MassStorageMenu>> MASS_STORAGE =
            MENUS.register("mass_storage", () -> IForgeMenuType.create(MassStorageMenu::new));

    public static final RegistryObject<MenuType<TurretMenu>> TURRET =
            MENUS.register("turret", () -> IForgeMenuType.create(TurretMenu::new));

    public static final RegistryObject<MenuType<NuclearDeviceMenu>> NUCLEAR_DEVICE =
            MENUS.register("nuclear_device", () -> IForgeMenuType.create(NuclearDeviceMenu::new));

    public static final RegistryObject<MenuType<CustomNukeMenu>> CUSTOM_NUKE =
            MENUS.register("custom_nuke", () -> IForgeMenuType.create(CustomNukeMenu::new));

    public static final RegistryObject<MenuType<BombMultiMenu>> BOMB_MULTI =
            MENUS.register("bomb_multi", () -> IForgeMenuType.create(BombMultiMenu::new));

    public static final RegistryObject<MenuType<BalefireBombMenu>> BALEFIRE_BOMB =
            MENUS.register("balefire_bomb", () -> IForgeMenuType.create(BalefireBombMenu::new));

    public static final RegistryObject<MenuType<RadarMenu>> RADAR =
            MENUS.register("radar", () -> IForgeMenuType.create(RadarMenu::new));

    public static final RegistryObject<MenuType<SatelliteLinkerMenu>> SATELLITE_LINKER =
            MENUS.register("satellite_linker", () -> IForgeMenuType.create(SatelliteLinkerMenu::new));

    public static final RegistryObject<MenuType<SatelliteDockMenu>> SATELLITE_DOCK =
            MENUS.register("satellite_dock", () -> IForgeMenuType.create(SatelliteDockMenu::new));

    public static final RegistryObject<MenuType<SoyuzCapsuleMenu>> SOYUZ_CAPSULE =
            MENUS.register("soyuz_capsule", () -> IForgeMenuType.create(SoyuzCapsuleMenu::new));

    public static final RegistryObject<MenuType<SoyuzLauncherMenu>> SOYUZ_LAUNCHER =
            MENUS.register("soyuz_launcher", () -> IForgeMenuType.create(SoyuzLauncherMenu::new));

    public static final RegistryObject<MenuType<LaunchPadMenu>> LAUNCH_PAD =
            MENUS.register("launch_pad", () -> IForgeMenuType.create(LaunchPadMenu::new));

    public static final RegistryObject<MenuType<RustedLaunchPadMenu>> LAUNCH_PAD_RUSTED =
            MENUS.register("launch_pad_rusted", () -> IForgeMenuType.create(RustedLaunchPadMenu::new));

    public static final RegistryObject<MenuType<LaunchTableMenu>> LAUNCH_TABLE =
            MENUS.register("launch_table", () -> IForgeMenuType.create(LaunchTableMenu::new));

    public static final RegistryObject<MenuType<CompactLauncherMenu>> COMPACT_LAUNCHER =
            MENUS.register("compact_launcher", () -> IForgeMenuType.create(CompactLauncherMenu::new));

    public static final RegistryObject<MenuType<MissileAssemblyMenu>> MISSILE_ASSEMBLY =
            MENUS.register("missile_assembly", () -> IForgeMenuType.create(MissileAssemblyMenu::new));

    public static final RegistryObject<MenuType<ParticleAcceleratorMenu>> PARTICLE_ACCELERATOR =
            MENUS.register("particle_accelerator", () -> IForgeMenuType.create(ParticleAcceleratorMenu::new));

    public static final RegistryObject<MenuType<ToolAbilityMenu>> TOOL_ABILITY =
            MENUS.register("tool_ability", () -> IForgeMenuType.create(ToolAbilityMenu::new));

    public static final RegistryObject<MenuType<RadioTorchMenu>> RADIO_TORCH =
            MENUS.register("radio_torch", () -> IForgeMenuType.create(RadioTorchMenu::new));

    public static final RegistryObject<MenuType<RadioAutocalMenu>> RADIO_AUTOCAL =
            MENUS.register("radio_autocal", () -> IForgeMenuType.create(RadioAutocalMenu::new));

    public static final RegistryObject<MenuType<RadioTelexMenu>> RADIO_TELEX =
            MENUS.register("radio_telex", () -> IForgeMenuType.create(RadioTelexMenu::new));

    public static final RegistryObject<MenuType<RBMKPanelMenu>> RBMK_PANEL =
            MENUS.register("rbmk_panel", () -> IForgeMenuType.create(RBMKPanelMenu::new));

    public static final RegistryObject<MenuType<RBMKConsoleMenu>> RBMK_CONSOLE =
            MENUS.register("rbmk_console", () -> IForgeMenuType.create(RBMKConsoleMenu::new));

    public static final RegistryObject<MenuType<RBMKRodMenu>> RBMK_ROD =
            MENUS.register("rbmk_rod", () -> IForgeMenuType.create(RBMKRodMenu::new));

    public static final RegistryObject<MenuType<RBMKControlMenu>> RBMK_CONTROL =
            MENUS.register("rbmk_control", () -> IForgeMenuType.create(RBMKControlMenu::new));

    public static final RegistryObject<MenuType<RBMKControlAutoMenu>> RBMK_CONTROL_AUTO =
            MENUS.register("rbmk_control_auto", () -> IForgeMenuType.create(RBMKControlAutoMenu::new));

    public static final RegistryObject<MenuType<RBMKHeaterMenu>> RBMK_HEATER =
            MENUS.register("rbmk_heater", () -> IForgeMenuType.create(RBMKHeaterMenu::new));

    public static final RegistryObject<MenuType<RBMKBoilerMenu>> RBMK_BOILER =
            MENUS.register("rbmk_boiler", () -> IForgeMenuType.create(RBMKBoilerMenu::new));

    public static final RegistryObject<MenuType<RBMKStorageMenu>> RBMK_STORAGE =
            MENUS.register("rbmk_storage", () -> IForgeMenuType.create(RBMKStorageMenu::new));

    public static final RegistryObject<MenuType<RBMKOutgasserMenu>> RBMK_OUTGASSER =
            MENUS.register("rbmk_outgasser", () -> IForgeMenuType.create(RBMKOutgasserMenu::new));

    public static final RegistryObject<MenuType<RBMKAutoloaderMenu>> RBMK_AUTOLOADER =
            MENUS.register("rbmk_autoloader", () -> IForgeMenuType.create(RBMKAutoloaderMenu::new));

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    private ModMenuTypes() {
    }
}
