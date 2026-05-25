package com.hbm.ntm.network;

import net.minecraft.world.item.ItemStack;

public interface HbmLegacyItemAnimationReceiver {
    void handleLegacyItemAnimation(ItemStack stack, int selectedSlot, short animationType, int receiverIndex, int itemIndex);
}
