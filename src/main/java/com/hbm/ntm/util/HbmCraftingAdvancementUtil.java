package com.hbm.ntm.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.IdentityHashMap;
import java.util.Map;

public final class HbmCraftingAdvancementUtil {
    private static final Map<Item, ResourceLocation> CRAFTING_ADVANCEMENTS = new IdentityHashMap<>();

    private HbmCraftingAdvancementUtil() {
    }

    public static void registerCraftingAdvancement(ItemLike output, ResourceLocation advancementId) {
        if (output != null && advancementId != null) {
            CRAFTING_ADVANCEMENTS.put(output.asItem(), advancementId);
        }
    }

    public static void clearCraftingAdvancements() {
        CRAFTING_ADVANCEMENTS.clear();
    }

    public static ResourceLocation craftingAdvancementFor(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : CRAFTING_ADVANCEMENTS.get(stack.getItem());
    }

    public static boolean fireCraftingAdvancement(ServerPlayer player, ItemStack stack) {
        ResourceLocation advancementId = craftingAdvancementFor(stack);
        return advancementId != null && awardAdvancement(player, advancementId);
    }

    public static boolean awardAdvancement(ServerPlayer player, ResourceLocation advancementId) {
        if (player == null || advancementId == null) {
            return false;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        Advancement advancement = server.getAdvancements().getAdvancement(advancementId);
        if (advancement == null) {
            return false;
        }

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        boolean awarded = false;
        for (String criterion : progress.getRemainingCriteria()) {
            awarded |= player.getAdvancements().award(advancement, criterion);
        }
        return awarded;
    }
}
