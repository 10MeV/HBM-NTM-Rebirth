package com.hbm.ntm.item;

import com.hbm.ntm.menu.LemegetonMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** Legacy portable material-upgrade processor, not a guide/manual book. */
public class LemegetonItem extends Item {
    public LemegetonItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((containerId, inventory, owner) ->
                    new LemegetonMenu(containerId, inventory),
                    Component.translatable("container.hbm_ntm_rebirth.lemegeton")));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
