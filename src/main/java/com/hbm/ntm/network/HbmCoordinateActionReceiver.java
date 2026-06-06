package com.hbm.ntm.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface HbmCoordinateActionReceiver {
    default boolean canReceiveCoordinateAction(ServerPlayer player, ItemStack stack, BlockPos pos,
                                               int action, int value, int frequency, CompoundTag data) {
        return true;
    }

    void handleCoordinateAction(ServerPlayer player, ItemStack stack, BlockPos pos,
                                int action, int value, int frequency, CompoundTag data);
}
