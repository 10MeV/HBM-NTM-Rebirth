package com.hbm.ntm.client.screen;

import com.hbm.ntm.menu.LaunchTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LaunchTableScreen extends CustomMissileLauncherScreen<LaunchTableMenu> {
    public LaunchTableScreen(LaunchTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "gui_launch_table", true);
    }
}
