package com.hbm.ntm.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Legacy-name crafting advancement facade.
 */
@Deprecated(forRemoval = false)
public final class AchievementHandler {
    private AchievementHandler() {
    }

    public static void register() {
        // Concrete legacy achievement mappings are registered by content slices when their advancements exist.
    }

    public static void registerCraftingAchievement(ItemLike output, ResourceLocation advancementId) {
        HbmCraftingAdvancementUtil.registerCraftingAdvancement(output, advancementId);
    }

    public static boolean fire(Player player, ItemStack stack) {
        return player instanceof ServerPlayer serverPlayer
                && HbmCraftingAdvancementUtil.fireCraftingAdvancement(serverPlayer, stack);
    }
}
