package com.hbm.ntm.radiation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Set;

public final class ArmorUtil {
    private static final Set<String> FARADAY_KEYWORDS = Set.of(
            "chainmail", "iron", "gold", "netherite", "steel", "titanium", "alloy",
            "lead", "copper", "hazmat", "rubber", "schrabidium");

    public static boolean checkForHazmat(LivingEntity entity) {
        return getWornPieces(entity) == 4 && HazmatRegistry.getResistance(entity) >= 0.6F;
    }

    public static boolean checkForHaz2(LivingEntity entity) {
        return getWornPieces(entity) == 4 && HazmatRegistry.getResistance(entity) >= 1.7F;
    }

    public static boolean checkForFaraday(Player player) {
        int pieces = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty() && isFaradayArmor(stack)) {
                pieces++;
            }
        }
        return pieces == 4;
    }

    public static boolean isFaradayArmor(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (HazmatRegistry.getResistance(stack) > 0.0D) {
            return true;
        }
        String key = stack.getItem().getDescriptionId().toLowerCase(Locale.US);
        for (String metal : FARADAY_KEYWORDS) {
            if (key.contains(metal)) {
                return true;
            }
        }
        return false;
    }

    private static int getWornPieces(LivingEntity entity) {
        int pieces = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (!stack.isEmpty()) {
                pieces++;
            }
        }
        return pieces;
    }

    private ArmorUtil() {
    }
}
