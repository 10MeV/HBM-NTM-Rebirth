package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.ArcWelderMenu;
import com.hbm.ntm.menu.ArmorTableMenu;
import com.hbm.ntm.menu.AshpitMenu;
import com.hbm.ntm.menu.AssemblyFactoryMenu;
import com.hbm.ntm.menu.AssemblyMachineMenu;
import com.hbm.ntm.menu.AmmoBagMenu;
import com.hbm.ntm.menu.BasicMachineMenu;
import com.hbm.ntm.menu.BombMultiMenu;
import com.hbm.ntm.menu.CasingBagMenu;
import com.hbm.ntm.menu.ChemicalFactoryMenu;
import com.hbm.ntm.menu.ChemicalPlantMenu;
import com.hbm.ntm.menu.CompactLauncherMenu;
import com.hbm.ntm.menu.CompressorMenu;
import com.hbm.ntm.menu.CombustionEngineMenu;
import com.hbm.ntm.menu.CrateMenu;
import com.hbm.ntm.menu.CustomNukeMenu;
import com.hbm.ntm.menu.FluidTankMenu;
import com.hbm.ntm.menu.FireboxHeaterMenu;
import com.hbm.ntm.menu.GasFlareMenu;
import com.hbm.ntm.menu.HeaterHeatexMenu;
import com.hbm.ntm.menu.LaunchPadMenu;
import com.hbm.ntm.menu.LaunchTableMenu;
import com.hbm.ntm.menu.LiquefactorMenu;
import com.hbm.ntm.menu.MachineBatteryMenu;
import com.hbm.ntm.menu.MachineBatterySocketMenu;
import com.hbm.ntm.menu.MissileAssemblyMenu;
import com.hbm.ntm.menu.NuclearDeviceMenu;
import com.hbm.ntm.menu.OilDrillMenu;
import com.hbm.ntm.menu.OilburnerMenu;
import com.hbm.ntm.menu.PyroOvenMenu;
import com.hbm.ntm.menu.RadioAutocalMenu;
import com.hbm.ntm.menu.RadioTelexMenu;
import com.hbm.ntm.menu.RadioTorchMenu;
import com.hbm.ntm.menu.RBMKAutoloaderMenu;
import com.hbm.ntm.menu.RBMKOutgasserMenu;
import com.hbm.ntm.menu.RBMKPanelMenu;
import com.hbm.ntm.menu.RBMKStorageMenu;
import com.hbm.ntm.menu.RadarMenu;
import com.hbm.ntm.menu.RemoteFluidMachineMenu;
import com.hbm.ntm.menu.RefineryMenu;
import com.hbm.ntm.menu.RustedLaunchPadMenu;
import com.hbm.ntm.menu.SatelliteDockMenu;
import com.hbm.ntm.menu.SatelliteLinkerMenu;
import com.hbm.ntm.menu.SolidifierMenu;
import com.hbm.ntm.menu.SoyuzCapsuleMenu;
import com.hbm.ntm.menu.SoyuzLauncherMenu;
import com.hbm.ntm.menu.ToolAbilityMenu;
import com.hbm.ntm.menu.TurretMenu;
import com.hbm.ntm.menu.WeaponTableMenu;
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

    public static final RegistryObject<MenuType<AmmoBagMenu>> AMMO_BAG =
            MENUS.register("ammo_bag", () -> IForgeMenuType.create(AmmoBagMenu::new));

    public static final RegistryObject<MenuType<CasingBagMenu>> CASING_BAG =
            MENUS.register("casing_bag", () -> IForgeMenuType.create(CasingBagMenu::new));

    public static final RegistryObject<MenuType<ArmorTableMenu>> ARMOR_TABLE =
            MENUS.register("armor_table", () -> IForgeMenuType.create(ArmorTableMenu::new));

    public static final RegistryObject<MenuType<WeaponTableMenu>> WEAPON_TABLE =
            MENUS.register("weapon_table", () -> IForgeMenuType.create(WeaponTableMenu::new));

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

    public static final RegistryObject<MenuType<ArcWelderMenu>> ARC_WELDER =
            MENUS.register("arc_welder", () -> IForgeMenuType.create(ArcWelderMenu::new));

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

    public static final RegistryObject<MenuType<TurretMenu>> TURRET =
            MENUS.register("turret", () -> IForgeMenuType.create(TurretMenu::new));

    public static final RegistryObject<MenuType<NuclearDeviceMenu>> NUCLEAR_DEVICE =
            MENUS.register("nuclear_device", () -> IForgeMenuType.create(NuclearDeviceMenu::new));

    public static final RegistryObject<MenuType<CustomNukeMenu>> CUSTOM_NUKE =
            MENUS.register("custom_nuke", () -> IForgeMenuType.create(CustomNukeMenu::new));

    public static final RegistryObject<MenuType<BombMultiMenu>> BOMB_MULTI =
            MENUS.register("bomb_multi", () -> IForgeMenuType.create(BombMultiMenu::new));

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
