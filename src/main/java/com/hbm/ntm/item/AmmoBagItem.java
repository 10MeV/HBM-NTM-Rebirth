package com.hbm.ntm.item;

import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.ntm.menu.AmmoBagMenu;
import net.minecraft.core.NonNullList;
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

public class AmmoBagItem extends Item {
    public static final int SLOT_COUNT = 8;

    private final boolean infinite;

    public AmmoBagItem(Properties properties, boolean infinite) {
        super(properties.stacksTo(1));
        this.infinite = infinite;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Component title = stack.hasCustomHoverName() ? stack.getHoverName() : Component.translatable("container.ammoBag");
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((containerId, inventory, owner) ->
                            new AmmoBagMenu(containerId, inventory, hand), title),
                    buffer -> buffer.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !infinite && (!stack.hasTag() || durabilityForDisplay(stack) != 0.0D);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * (float) (1.0D - durabilityForDisplay(stack)));
    }

    private static double durabilityForDisplay(ItemStack stack) {
        if (!stack.hasTag()) {
            return 1.0D;
        }
        NonNullList<ItemStack> slots = HbmItemStackUtil.readStacksFromNbt(stack, SLOT_COUNT);
        int capacity = 0;
        int bullets = 0;
        for (ItemStack slot : slots) {
            if (slot.isEmpty()) {
                capacity += 64;
            } else {
                capacity += slot.getMaxStackSize();
                bullets += slot.getCount();
            }
        }
        return capacity <= 0 ? 1.0D : 1.0D - (double) bullets / (double) capacity;
    }
}
