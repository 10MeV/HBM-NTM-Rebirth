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
    public static final ResourceLocation STRATUM = new ResourceLocation(HbmNtm.MOD_ID, "stratum");
    public static final ResourceLocation SLIMEBALL = new ResourceLocation(HbmNtm.MOD_ID, "slimeball");
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
    public static final ResourceLocation BURNER_PRESS = new ResourceLocation(HbmNtm.MOD_ID, "burner_press");
    public static final ResourceLocation SELENIUM = new ResourceLocation(HbmNtm.MOD_ID, "selenium");
    public static final ResourceLocation CONCRETE = new ResourceLocation(HbmNtm.MOD_ID, "concrete");
    public static final ResourceLocation POLYMER = new ResourceLocation(HbmNtm.MOD_ID, "polymer");
    public static final ResourceLocation TANTALUM = new ResourceLocation(HbmNtm.MOD_ID, "tantalum");
    public static final ResourceLocation GAS_CENT = new ResourceLocation(HbmNtm.MOD_ID, "gas_cent");
    public static final ResourceLocation CENTRIFUGE = new ResourceLocation(HbmNtm.MOD_ID, "centrifuge");
    public static final ResourceLocation SPACE = new ResourceLocation(HbmNtm.MOD_ID, "space");
    public static final ResourceLocation SCHRAB = new ResourceLocation(HbmNtm.MOD_ID, "schrab");
    public static final ResourceLocation ACIDIZER = new ResourceLocation(HbmNtm.MOD_ID, "acidizer");
    public static final ResourceLocation SILEX = new ResourceLocation(HbmNtm.MOD_ID, "silex");
    public static final ResourceLocation WATZ = new ResourceLocation(HbmNtm.MOD_ID, "watz");
    public static final ResourceLocation RBMK = new ResourceLocation(HbmNtm.MOD_ID, "rbmk");
    public static final ResourceLocation BISMUTH = new ResourceLocation(HbmNtm.MOD_ID, "bismuth");
    public static final ResourceLocation BREEDING = new ResourceLocation(HbmNtm.MOD_ID, "breeding");
    public static final ResourceLocation FUSION = new ResourceLocation(HbmNtm.MOD_ID, "fusion");
    public static final ResourceLocation RED_BALLOONS = new ResourceLocation(HbmNtm.MOD_ID, "red_balloons");
    public static final ResourceLocation FIEND = new ResourceLocation(HbmNtm.MOD_ID, "fiend");
    public static final ResourceLocation FIEND2 = new ResourceLocation(HbmNtm.MOD_ID, "fiend2");
    public static final ResourceLocation OMEGA12 = new ResourceLocation(HbmNtm.MOD_ID, "omega12");
    public static final ResourceLocation INFERNO = new ResourceLocation(HbmNtm.MOD_ID, "inferno");

    private AchievementHandler() {
    }

    public static void register() {
        registerCraftingAchievement(ModItems.legacyItem("piston_selenium").get(), SELENIUM);
        registerCraftingAchievement(ModItems.legacyItem("battery_potatos").get(), new ResourceLocation(HbmNtm.MOD_ID, "potato"));
        registerCraftingAchievement(ModBlocks.MACHINE_PRESS.get(), BURNER_PRESS);
        registerCraftingAchievement(ModItems.legacyItem("rbmk_fuel_empty").get(), RBMK);
        registerCraftingAchievement(ModBlocks.MACHINE_BLAST_FURNACE.get(), BLAST_FURNACE);
        registerCraftingAchievement(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get(), ASSEMBLY);
        registerCraftingAchievement(ModBlocks.MACHINE_CHEMICAL_PLANT.get(), CHEMPLANT);
        registerCraftingAchievement(ModBlocks.CONCRETE_SMOOTH.get(), CONCRETE);
        registerCraftingAchievement(ModBlocks.CONCRETE_ASBESTOS.get(), CONCRETE);
        registerCraftingAchievement(ModItems.legacyItem("ingot_polymer").get(), POLYMER);
        registerCraftingAchievement(ModItems.legacyItem("ingot_desh").get(), DESH);
        registerCraftingAchievement(ModItems.legacyItem("gem_tantalium").get(), TANTALUM);
        registerCraftingAchievement(ModBlocks.MACHINE_GASCENT.get(), GAS_CENT);
        registerCraftingAchievement(ModBlocks.MACHINE_CENTRIFUGE.get(), CENTRIFUGE);
        registerCraftingAchievement(ModItems.legacyItem("ingot_schrabidium").get(), SCHRAB);
        registerCraftingAchievement(ModItems.legacyItem("nugget_schrabidium").get(), SCHRAB);
        registerCraftingAchievement(ModBlocks.MACHINE_CRYSTALLIZER.get(), ACIDIZER);
        registerCraftingAchievement(ModBlocks.MACHINE_SILEX.get(), SILEX);
        registerCraftingAchievement(ModItems.legacyItem("nugget_technetium").get(), TECHNETIUM);
        registerCraftingAchievement(ModBlocks.STRUCT_WATZ_CORE.get(), WATZ);
        registerCraftingAchievement(ModItems.legacyItem("nugget_bismuth").get(), BISMUTH);
        registerCraftingAchievement(ModItems.legacyItem("nugget_am241").get(), BREEDING);
        registerCraftingAchievement(ModItems.legacyItem("nugget_am242").get(), BREEDING);
        registerCraftingAchievement(ModItems.legacyItem("missile_nuclear").get(), RED_BALLOONS);
        registerCraftingAchievement(ModItems.legacyItem("missile_nuclear_cluster").get(), RED_BALLOONS);
        registerCraftingAchievement(ModItems.legacyItem("missile_doomsday").get(), RED_BALLOONS);
        registerCraftingAchievement(ModItems.legacyItem("mp_warhead_10_nuclear").get(), RED_BALLOONS);
        registerCraftingAchievement(ModItems.legacyItem("mp_warhead_10_nuclear_large").get(), RED_BALLOONS);
        registerCraftingAchievement(ModItems.legacyItem("mp_warhead_15_nuclear").get(), RED_BALLOONS);
        registerCraftingAchievement(ModItems.legacyItem("mp_warhead_15_nuclear_shark").get(), RED_BALLOONS);
        registerCraftingAchievement(ModItems.legacyItem("mp_warhead_15_boxcar").get(), RED_BALLOONS);
        registerCraftingAchievement(ModBlocks.FUSION_TORUS.get(), FUSION);
        registerCraftingAchievement(ModItems.legacyItem("particle_digamma").get(), new ResourceLocation(HbmNtm.MOD_ID, "omega12"));
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
