package com.hbm.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Legacy 1.7.10 package bridge for crafting achievement triggers.
 */
@Deprecated(forRemoval = false)
public final class AchievementHandler {
    private AchievementHandler() {
    }

    public static void register() {
        com.hbm.ntm.util.AchievementHandler.register();
    }

    public static void registerCraftingAchievement(ItemLike output, ResourceLocation advancementId) {
        com.hbm.ntm.util.AchievementHandler.registerCraftingAchievement(output, advancementId);
    }

    public static boolean fire(Player player, ItemStack stack) {
        return com.hbm.ntm.util.AchievementHandler.fire(player, stack);
    }
}
