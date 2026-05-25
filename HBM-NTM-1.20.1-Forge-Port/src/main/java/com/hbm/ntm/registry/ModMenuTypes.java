package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.AssemblyMachineMenu;
import com.hbm.ntm.menu.BasicMachineMenu;
import com.hbm.ntm.menu.ChemicalPlantMenu;
import com.hbm.ntm.menu.CustomNukeMenu;
import com.hbm.ntm.menu.FluidTankMenu;
import com.hbm.ntm.menu.LiquefactorMenu;
import com.hbm.ntm.menu.MachineBatteryMenu;
import com.hbm.ntm.menu.MachineBatterySocketMenu;
import com.hbm.ntm.menu.NuclearDeviceMenu;
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

    public static final RegistryObject<MenuType<AssemblyMachineMenu>> ASSEMBLY_MACHINE =
            MENUS.register("assembly_machine", () -> IForgeMenuType.create(AssemblyMachineMenu::new));

    public static final RegistryObject<MenuType<ChemicalPlantMenu>> CHEMICAL_PLANT =
            MENUS.register("chemical_plant", () -> IForgeMenuType.create(ChemicalPlantMenu::new));

    public static final RegistryObject<MenuType<LiquefactorMenu>> LIQUEFACTOR =
            MENUS.register("liquefactor", () -> IForgeMenuType.create(LiquefactorMenu::new));

    public static final RegistryObject<MenuType<FluidTankMenu>> FLUID_TANK =
            MENUS.register("fluid_tank", () -> IForgeMenuType.create(FluidTankMenu::new));

    public static final RegistryObject<MenuType<MachineBatteryMenu>> MACHINE_BATTERY =
            MENUS.register("machine_battery", () -> IForgeMenuType.create(MachineBatteryMenu::new));

    public static final RegistryObject<MenuType<MachineBatterySocketMenu>> MACHINE_BATTERY_SOCKET =
            MENUS.register("machine_battery_socket", () -> IForgeMenuType.create(MachineBatterySocketMenu::new));

    public static final RegistryObject<MenuType<NuclearDeviceMenu>> NUCLEAR_DEVICE =
            MENUS.register("nuclear_device", () -> IForgeMenuType.create(NuclearDeviceMenu::new));

    public static final RegistryObject<MenuType<CustomNukeMenu>> CUSTOM_NUKE =
            MENUS.register("custom_nuke", () -> IForgeMenuType.create(CustomNukeMenu::new));

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    private ModMenuTypes() {
    }
}
