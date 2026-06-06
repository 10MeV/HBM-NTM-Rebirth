package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmLegacyItemAnimationReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ClientLegacyItemAnimationHandler {
    public static void handle(short animationType, int receiverIndex, int itemIndex) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof HbmLegacyItemAnimationReceiver receiver) {
            receiver.handleLegacyItemAnimation(stack, player.getInventory().selected, animationType, receiverIndex, itemIndex);
        } else {
            HbmNtm.LOGGER.debug("Legacy item animation type {} had no HbmLegacyItemAnimationReceiver receiver.",
                    animationType);
        }
    }

    private ClientLegacyItemAnimationHandler() {
    }
}
