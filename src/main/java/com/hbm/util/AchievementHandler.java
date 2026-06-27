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
    public static final ResourceLocation MANHATTAN = com.hbm.ntm.util.AchievementHandler.MANHATTAN;
    public static final ResourceLocation RBMK_BOOM = com.hbm.ntm.util.AchievementHandler.RBMK_BOOM;
    public static final ResourceLocation ZIRNOX_BOOM = com.hbm.ntm.util.AchievementHandler.ZIRNOX_BOOM;
    public static final ResourceLocation WATZ_BOOM = com.hbm.ntm.util.AchievementHandler.WATZ_BOOM;
    public static final ResourceLocation SULFURIC = com.hbm.ntm.util.AchievementHandler.SULFURIC;
    public static final ResourceLocation RAD_POISON = com.hbm.ntm.util.AchievementHandler.RAD_POISON;
    public static final ResourceLocation RAD_DEATH = com.hbm.ntm.util.AchievementHandler.RAD_DEATH;
    public static final ResourceLocation DIGAMMA_SEE = com.hbm.ntm.util.AchievementHandler.DIGAMMA_SEE;
    public static final ResourceLocation DIGAMMA_FEEL = com.hbm.ntm.util.AchievementHandler.DIGAMMA_FEEL;
    public static final ResourceLocation DIGAMMA_KNOW = com.hbm.ntm.util.AchievementHandler.DIGAMMA_KNOW;
    public static final ResourceLocation DIGAMMA_KAUAI_MOHO = com.hbm.ntm.util.AchievementHandler.DIGAMMA_KAUAI_MOHO;

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

    public static boolean award(Player player, ResourceLocation advancementId) {
        return com.hbm.ntm.util.AchievementHandler.award(player, advancementId);
    }
}
