package com.hbm.ntm.entity.train;

import net.minecraft.server.level.ServerPlayer;

/** Exact modern replacement for the old rail-car {@code IGUIProvider} condition. */
public interface RailCarMenuAccess {
    void openInventory(ServerPlayer player);
}
