package com.hbm.ntm.api.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface ControlReceiver {
    boolean hasPermission(Player player);

    void receiveControl(CompoundTag data);

    default void receiveControl(Player player, CompoundTag data) {
    }
}
