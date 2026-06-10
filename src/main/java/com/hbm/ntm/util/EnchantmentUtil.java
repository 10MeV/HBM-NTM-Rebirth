package com.hbm.ntm.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Legacy-name enchantment/experience utility facade.
 */
@Deprecated(forRemoval = false)
public final class EnchantmentUtil {
    private EnchantmentUtil() {
    }

    public static void addEnchantment(ItemStack stack, Enchantment enchantment, int level) {
        HbmEnchantmentUtil.addEnchantment(stack, enchantment, level);
    }

    public static void removeEnchantment(ItemStack stack, Enchantment enchantment) {
        HbmEnchantmentUtil.removeEnchantment(stack, enchantment);
    }

    public static int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        return HbmEnchantmentUtil.getEnchantmentLevel(stack, enchantment);
    }

    public static int xpBarCap(int level) {
        return HbmEnchantmentUtil.xpBarCap(level);
    }

    public static int getLevelForExperience(int xp) {
        return HbmEnchantmentUtil.getLevelForExperience(xp);
    }

    public static void addExperience(Player player, int xp, boolean silent) {
        HbmEnchantmentUtil.addExperience(player, xp, silent);
    }

    public static void setExperience(Player player, int xp) {
        HbmEnchantmentUtil.setExperience(player, xp);
    }

    public static void addExperienceLevelSilent(Player player, int level) {
        HbmEnchantmentUtil.addExperienceLevelSilent(player, level);
    }

    public static int getTotalExperience(Player player) {
        return HbmEnchantmentUtil.getTotalExperience(player);
    }
}
