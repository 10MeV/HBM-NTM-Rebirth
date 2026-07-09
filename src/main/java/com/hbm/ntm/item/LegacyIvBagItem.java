package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.EnchantmentUtil;
import com.hbm.ntm.util.InventoryUtil;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LegacyIvBagItem extends Item {
    private static final float BLOOD_HEALTH = 5.0F;
    private static final int XP_AMOUNT = 100;

    private final Kind kind;

    public LegacyIvBagItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && !apply(stack, player)) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private boolean apply(ItemStack stack, Player player) {
        switch (kind) {
            case EMPTY -> {
                finishUse(stack, player, new ItemStack(ModItems.IV_BLOOD.get()), "hbm:item.syringe");
                float health = Math.max(player.getHealth() - BLOOD_HEALTH, 0.0F);
                player.setHealth(health);
                if (health <= 0.0F) {
                    player.die(player.damageSources().magic());
                }
                return true;
            }
            case BLOOD -> {
                finishUse(stack, player, new ItemStack(ModItems.IV_EMPTY.get()), "hbm:item.radaway");
                player.heal(BLOOD_HEALTH);
                return true;
            }
            case XP_EMPTY -> {
                int totalXp = EnchantmentUtil.getTotalExperience(player);
                if (totalXp < XP_AMOUNT) {
                    return false;
                }
                finishUse(stack, player, new ItemStack(ModItems.IV_XP.get()), "hbm:item.syringe");
                EnchantmentUtil.setExperience(player, totalXp - XP_AMOUNT);
                return true;
            }
            case XP -> {
                finishUse(stack, player, new ItemStack(ModItems.IV_XP_EMPTY.get()), "random.orb");
                EnchantmentUtil.addExperience(player, XP_AMOUNT, false);
                return true;
            }
        }
        return false;
    }

    private static void finishUse(ItemStack stack, Player player, ItemStack container, String sound) {
        stack.shrink(1);
        LegacySoundPlayer.playSoundAtEntity(player, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        InventoryUtil.giveOrDrop(player, container);
    }

    public enum Kind {
        EMPTY,
        BLOOD,
        XP_EMPTY,
        XP
    }
}
