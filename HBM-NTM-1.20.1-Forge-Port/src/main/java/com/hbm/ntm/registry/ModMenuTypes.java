package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.BasicMachineMenu;
import com.hbm.ntm.menu.MachineBatteryMenu;
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

    public static final RegistryObject<MenuType<MachineBatteryMenu>> MACHINE_BATTERY =
            MENUS.register("machine_battery", () -> IForgeMenuType.create(MachineBatteryMenu::new));

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    private ModMenuTypes() {
    }
}
