package com.hbm.ntm.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HbmEnchantmentUtil {
    private HbmEnchantmentUtil() {
    }

    public static void addEnchantment(ItemStack stack, Enchantment enchantment, int level) {
        if (stack == null || stack.isEmpty() || enchantment == null || level <= 0) {
            return;
        }
        stack.enchant(enchantment, level);
    }

    public static void removeEnchantment(ItemStack stack, Enchantment enchantment) {
        if (stack == null || stack.isEmpty() || enchantment == null) {
            return;
        }
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(stack));
        if (enchantments.remove(enchantment) != null) {
            EnchantmentHelper.setEnchantments(enchantments, stack);
        }
    }

    public static int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        if (stack == null || stack.isEmpty() || enchantment == null) {
            return 0;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }

    public static int xpBarCap(int level) {
        return level >= 30 ? 62 + (level - 30) * 7 : (level >= 15 ? 17 + (level - 15) * 3 : 17);
    }

    public static int getLevelForExperience(int xp) {
        int level = 0;
        int remaining = Math.max(0, xp);
        while (true) {
            int xpCap = xpBarCap(level);
            if (remaining < xpCap) {
                return level;
            }
            remaining -= xpCap;
            level++;
        }
    }

    public static void addExperience(Player player, int xp, boolean silent) {
        if (player == null || xp <= 0) {
            return;
        }
        int cappedXp = Math.min(xp, Integer.MAX_VALUE - player.totalExperience);
        player.experienceProgress += (float) cappedXp / (float) player.getXpNeededForNextLevel();

        for (player.totalExperience += cappedXp; player.experienceProgress >= 1.0F;
                player.experienceProgress /= (float) player.getXpNeededForNextLevel()) {
            player.experienceProgress = (player.experienceProgress - 1.0F)
                    * (float) player.getXpNeededForNextLevel();
            if (silent) {
                addExperienceLevelSilent(player, 1);
            } else {
                player.giveExperienceLevels(1);
            }
        }
    }

    public static void setExperience(Player player, int xp) {
        if (player == null) {
            return;
        }
        player.experienceLevel = 0;
        player.experienceProgress = 0.0F;
        player.totalExperience = 0;
        addExperience(player, Math.max(0, xp), true);
    }

    public static void addExperienceLevelSilent(Player player, int levels) {
        if (player == null) {
            return;
        }
        player.experienceLevel += levels;
        if (player.experienceLevel < 0) {
            player.experienceLevel = 0;
            player.experienceProgress = 0.0F;
            player.totalExperience = 0;
        }
    }

    public static int getTotalExperience(Player player) {
        if (player == null) {
            return 0;
        }
        int xp = 0;
        for (int level = 0; level < player.experienceLevel; level++) {
            xp += xpBarCap(level);
        }
        xp += Math.round(xpBarCap(player.experienceLevel) * player.experienceProgress);
        return xp;
    }
}
