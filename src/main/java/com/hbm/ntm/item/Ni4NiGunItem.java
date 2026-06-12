package com.hbm.ntm.item;

import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaWeaponModEvaluator;
import com.hbm.ntm.entity.projectile.CoinEntity;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Ni4NiGunItem extends SednaGunItem {
    public static final String KEY_COIN_COUNT = "coincount";
    public static final String KEY_COIN_CHARGE = "coincharge";
    public static final String KEY_COLORS = "colors";
    private static final int BASE_MAX_COINS = 4;
    private static final int COIN_CHARGE_TICKS = 80;

    public Ni4NiGunItem(Properties properties, SednaGunConfig gunConfig) {
        super(properties, gunConfig);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }

        int maxCoins = maxCoinCount(stack);
        if (getCoinCount(stack) >= maxCoins) {
            return;
        }

        int charge = getCoinCharge(stack) + 1;
        if (charge >= COIN_CHARGE_TICKS) {
            setCoinCharge(stack, 0);
            int newCount = Math.min(maxCoins, getCoinCount(stack) + 1);
            setCoinCount(stack, newCount);
            if (selected) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.ITEM_TECH_BOOP.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F + newCount / (float) maxCoins);
            }
        } else {
            setCoinCharge(stack, charge);
        }
    }

    @Override
    public void handleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind, boolean pressed) {
        if (keybind == HbmKeybind.GUN_SECONDARY && pressed) {
            throwCoin(player, stack);
            return;
        }
        super.handleKeybind(player, stack, keybind, pressed);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Now, don't get the wrong idea.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("I ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("fucking hate ").withStyle(ChatFormatting.RED))
                .append(Component.literal("this game.").withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.literal("I didn't do this for you, I did it for sea.").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private void throwCoin(ServerPlayer player, ItemStack stack) {
        int count = getCoinCount(stack);
        if (count <= 0) {
            return;
        }

        Level level = player.level();
        Vec3 look = player.getLookAngle();
        CoinEntity coin = new CoinEntity(level);
        coin.setOwner(player);
        coin.setPos(player.getX(), player.getY() + player.getEyeHeight() - 0.625D, player.getZ());
        coin.setYRot(player.getYRot());
        coin.yRotO = player.getYRot();
        coin.setDeltaMovement(look.scale(0.8D).add(0.0D, 0.5D, 0.0D));
        level.addFreshEntity(coin);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        setCoinCount(stack, count - 1);
    }

    private static int maxCoinCount(ItemStack stack) {
        int max = BASE_MAX_COINS;
        if (SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_NI4NI_NICKEL)) {
            max += 2;
        }
        if (SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_NI4NI_DOUBLOONS)) {
            max += 2;
        }
        return max;
    }

    public static int getCoinCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(KEY_COIN_COUNT);
    }

    public static void setCoinCount(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(KEY_COIN_COUNT, Math.max(0, value));
    }

    public static int getCoinCharge(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(KEY_COIN_CHARGE);
    }

    public static void setCoinCharge(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(KEY_COIN_CHARGE, Math.max(0, value));
    }

    @Nullable
    public static int[] getColors(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(KEY_COLORS)) {
            return null;
        }
        int[] colors = tag.getIntArray(KEY_COLORS);
        return colors.length == 3 ? colors : null;
    }

    public static void setColors(ItemStack stack, int dark, int light, int grip) {
        stack.getOrCreateTag().putIntArray(KEY_COLORS, new int[] { dark, light, grip });
    }

    public static void resetColors(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(KEY_COLORS);
        }
    }
}
