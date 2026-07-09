package com.hbm.ntm.item;

import com.hbm.ntm.menu.ContainmentBoxMenu;
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

public class ContainmentBoxItem extends Item {
    public static final int SLOT_COUNT = 20;
    public static final int SLOT_STACK_LIMIT = 1;

    public ContainmentBoxItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Component title = stack.getHoverName();
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((containerId, inventory, owner) ->
                            new ContainmentBoxMenu(containerId, inventory, hand), title),
                    buffer -> buffer.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
