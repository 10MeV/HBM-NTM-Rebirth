package com.hbm.ntm.util;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
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
    public static final ResourceLocation RADIUM = new ResourceLocation(HbmNtm.MOD_ID, "radium");
    public static final ResourceLocation NO9 = new ResourceLocation(HbmNtm.MOD_ID, "no9");
    public static final ResourceLocation SOME_WOUNDS = new ResourceLocation(HbmNtm.MOD_ID, "some_wounds");
    public static final ResourceLocation GO_FISH = new ResourceLocation(HbmNtm.MOD_ID, "go_fish");
    public static final ResourceLocation DIGAMMA_SEE =
            new ResourceLocation(HbmNtm.MOD_ID, "digamma_see");
    public static final ResourceLocation DIGAMMA_FEEL =
            new ResourceLocation(HbmNtm.MOD_ID, "digamma_feel");
    public static final ResourceLocation DIGAMMA_KNOW =
            new ResourceLocation(HbmNtm.MOD_ID, "digamma_know");
    public static final ResourceLocation DIGAMMA_KAUAI_MOHO =
            new ResourceLocation(HbmNtm.MOD_ID, "digamma_kauai_moho");
    public static final ResourceLocation HIDDEN = new ResourceLocation(HbmNtm.MOD_ID, "hidden");
    public static final ResourceLocation BLAST_FURNACE = new ResourceLocation(HbmNtm.MOD_ID, "blast_furnace");
    public static final ResourceLocation ASSEMBLY = new ResourceLocation(HbmNtm.MOD_ID, "assembly");
    public static final ResourceLocation CHEMPLANT = new ResourceLocation(HbmNtm.MOD_ID, "chemplant");
    public static final ResourceLocation DESH = new ResourceLocation(HbmNtm.MOD_ID, "desh");
    public static final ResourceLocation TECHNETIUM = new ResourceLocation(HbmNtm.MOD_ID, "technetium");
    public static final ResourceLocation FOEQ = new ResourceLocation(HbmNtm.MOD_ID, "foeq");
    public static final ResourceLocation HORIZONS_START = new ResourceLocation(HbmNtm.MOD_ID, "horizons_start");
    public static final ResourceLocation HORIZONS_END = new ResourceLocation(HbmNtm.MOD_ID, "horizons_end");
    public static final ResourceLocation HORIZONS_BONUS = new ResourceLocation(HbmNtm.MOD_ID, "horizons_bonus");
    public static final ResourceLocation SOYUZ = new ResourceLocation(HbmNtm.MOD_ID, "soyuz");

    private AchievementHandler() {
    }

    public static void register() {
        registerCraftingAchievement(ModBlocks.MACHINE_BLAST_FURNACE.get(), BLAST_FURNACE);
        registerCraftingAchievement(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get(), ASSEMBLY);
        registerCraftingAchievement(ModBlocks.MACHINE_CHEMICAL_PLANT.get(), CHEMPLANT);
        registerCraftingAchievement(ModItems.legacyItem("ingot_desh").get(), DESH);
        registerCraftingAchievement(ModItems.legacyItem("nugget_technetium").get(), TECHNETIUM);
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

    public static boolean has(Player player, ResourceLocation advancementId) {
        return advancementId == null || player instanceof ServerPlayer serverPlayer
                && HbmCraftingAdvancementUtil.hasAdvancement(serverPlayer, advancementId);
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
