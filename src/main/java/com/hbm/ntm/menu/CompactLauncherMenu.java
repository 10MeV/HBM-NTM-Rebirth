package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.CompactLauncherBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class CompactLauncherMenu extends CustomMissileLauncherMenu {
    public CompactLauncherMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, (CompactLauncherBlockEntity) getLauncher(inventory, data.readBlockPos()));
    }

    public CompactLauncherMenu(int containerId, Inventory inventory, CompactLauncherBlockEntity blockEntity) {
        super(ModMenuTypes.COMPACT_LAUNCHER.get(), containerId, inventory, blockEntity);
    }
}
