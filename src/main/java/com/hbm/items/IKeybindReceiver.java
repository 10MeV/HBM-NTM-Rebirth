package com.hbm.items;

import com.hbm.handler.HbmKeybinds;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmKeybindReceiver;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy keybind receiver facade. Implementing this old interface also makes
 * the item visible to the modern server keybind dispatcher.
 */
public interface IKeybindReceiver extends HbmKeybindReceiver {
    boolean canHandleKeybind(ServerPlayer player, ItemStack stack, EnumKeybind keybind);

    void handleKeybind(ServerPlayer player, ItemStack stack, EnumKeybind keybind, boolean state);

    default void handleKeybindClient(Player player, ItemStack stack, EnumKeybind keybind, boolean state) {
    }

    @Override
    default boolean canHandleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind) {
        return canHandleKeybind(player, stack, HbmKeybinds.fromModern(keybind));
    }

    @Override
    default void handleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind, boolean pressed) {
        handleKeybind(player, stack, HbmKeybinds.fromModern(keybind), pressed);
    }
}
