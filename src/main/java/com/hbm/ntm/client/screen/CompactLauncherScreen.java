package com.hbm.ntm.client.screen;

import com.hbm.ntm.menu.CompactLauncherMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompactLauncherScreen extends CustomMissileLauncherScreen<CompactLauncherMenu> {
    public CompactLauncherScreen(CompactLauncherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "gui_launch_table_small", false);
    }
}
