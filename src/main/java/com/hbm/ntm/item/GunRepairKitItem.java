package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GunRepairKitItem extends Item {
    public GunRepairKitItem(Properties properties, int legacyUses) {
        super(properties.stacksTo(1).durability(Math.max(1, legacyUses)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        boolean repaired = false;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack hotbarStack = player.getInventory().items.get(slot);
            if (hotbarStack.getItem() instanceof SednaGunItem gun) {
                repaired |= gun.repairLegacyWearQuarter(hotbarStack);
            }
        }

        if (repaired) {
            SoundEvent sound = stack.is(ModItems.GUN_KIT_1.get())
                    ? ModSounds.ITEM_SPRAY.get()
                    : ModSounds.ITEM_REPAIR.get();
            level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS,
                    1.0F, 1.0F);
            stack.hurtAndBreak(1, player, owner -> {
                if (owner instanceof ServerPlayer serverPlayer) {
                    serverPlayer.broadcastBreakEvent(hand);
                }
            });
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}
