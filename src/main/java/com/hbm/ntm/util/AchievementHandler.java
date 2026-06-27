package com.hbm.ntm.util;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

/**
 * Legacy-name crafting advancement facade.
 */
@Deprecated(forRemoval = false)
public final class AchievementHandler {
    public static final ResourceLocation MANHATTAN = new ResourceLocation(HbmNtm.MOD_ID, "manhattan");
    public static final ResourceLocation RBMK_BOOM = new ResourceLocation(HbmNtm.MOD_ID, "rbmk_boom");
    public static final ResourceLocation ZIRNOX_BOOM = new ResourceLocation(HbmNtm.MOD_ID, "zirnox_boom");
    public static final ResourceLocation WATZ_BOOM = new ResourceLocation(HbmNtm.MOD_ID, "watz_boom");
    public static final ResourceLocation SULFURIC = new ResourceLocation(HbmNtm.MOD_ID, "sulfuric");
    public static final ResourceLocation RAD_POISON = new ResourceLocation(HbmNtm.MOD_ID, "rad_poison");
    public static final ResourceLocation RAD_DEATH = new ResourceLocation(HbmNtm.MOD_ID, "rad_death");
    public static final ResourceLocation DIGAMMA_SEE =
            new ResourceLocation(HbmNtm.MOD_ID, "digamma_see");
    public static final ResourceLocation DIGAMMA_FEEL =
            new ResourceLocation(HbmNtm.MOD_ID, "digamma_feel");
    public static final ResourceLocation DIGAMMA_KNOW =
            new ResourceLocation(HbmNtm.MOD_ID, "digamma_know");
    public static final ResourceLocation DIGAMMA_KAUAI_MOHO =
            new ResourceLocation(HbmNtm.MOD_ID, "digamma_kauai_moho");

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

    public static boolean award(Player player, ResourceLocation advancementId) {
        return player instanceof ServerPlayer serverPlayer
                && HbmCraftingAdvancementUtil.awardAdvancement(serverPlayer, advancementId);
    }

    public static void fireManhattan(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (ServerPlayer player : serverLevel.players()) {
            HbmCraftingAdvancementUtil.awardAdvancement(player, MANHATTAN);
        }
    }
}
