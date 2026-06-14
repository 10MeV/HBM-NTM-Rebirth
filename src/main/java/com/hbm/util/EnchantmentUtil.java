package com.hbm.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Legacy 1.7.10 package bridge for enchantment and experience helpers.
 */
@Deprecated(forRemoval = false)
public final class EnchantmentUtil {
    private EnchantmentUtil() {
    }

    public static void addEnchantment(ItemStack stack, Enchantment enchantment, int level) {
        com.hbm.ntm.util.EnchantmentUtil.addEnchantment(stack, enchantment, level);
    }

    public static void removeEnchantment(ItemStack stack, Enchantment enchantment) {
        com.hbm.ntm.util.EnchantmentUtil.removeEnchantment(stack, enchantment);
    }

    public static int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        return com.hbm.ntm.util.EnchantmentUtil.getEnchantmentLevel(stack, enchantment);
    }

    public static int xpBarCap(int level) {
        return com.hbm.ntm.util.EnchantmentUtil.xpBarCap(level);
    }

    public static int getLevelForExperience(int xp) {
        return com.hbm.ntm.util.EnchantmentUtil.getLevelForExperience(xp);
    }

    public static void addExperience(Player player, int xp, boolean silent) {
        com.hbm.ntm.util.EnchantmentUtil.addExperience(player, xp, silent);
    }

    public static void setExperience(Player player, int xp) {
        com.hbm.ntm.util.EnchantmentUtil.setExperience(player, xp);
    }

    public static void addExperienceLevelSilent(Player player, int level) {
        com.hbm.ntm.util.EnchantmentUtil.addExperienceLevelSilent(player, level);
    }

    public static int getTotalExperience(Player player) {
        return com.hbm.ntm.util.EnchantmentUtil.getTotalExperience(player);
    }
}
