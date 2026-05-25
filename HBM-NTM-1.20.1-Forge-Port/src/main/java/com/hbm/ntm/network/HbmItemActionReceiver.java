package com.hbm.ntm.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface HbmItemActionReceiver {
    default boolean canReceiveItemAction(ServerPlayer player, InteractionHand hand, ItemStack stack,
                                         ResourceLocation actionType, CompoundTag data) {
        return true;
    }

    void handleItemAction(ServerPlayer player, InteractionHand hand, ItemStack stack,
                          ResourceLocation actionType, CompoundTag data);
}
