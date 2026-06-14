package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.LaunchTableBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class LaunchTableMenu extends CustomMissileLauncherMenu {
    public LaunchTableMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, (LaunchTableBlockEntity) getLauncher(inventory, data.readBlockPos()));
    }

    public LaunchTableMenu(int containerId, Inventory inventory, LaunchTableBlockEntity blockEntity) {
        super(ModMenuTypes.LAUNCH_TABLE.get(), containerId, inventory, blockEntity);
    }
}
