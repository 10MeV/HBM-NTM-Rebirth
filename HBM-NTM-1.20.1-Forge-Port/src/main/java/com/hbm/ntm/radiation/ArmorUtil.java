package com.hbm.ntm.radiation;

import com.hbm.ntm.registry.ModEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Set;

public final class ArmorUtil {
    private static final Set<String> FARADAY_KEYWORDS = Set.of(
            "chainmail", "iron", "gold", "netherite", "steel", "titanium", "alloy",
            "lead", "copper", "hazmat", "rubber", "schrabidium", "silver",
            "platinum", "tin", "liquidator", "euphemium", "cmb", "bronze",
            "electrum", "t45", "t51", "bj", "starmetal", "hev", "ajr",
            "rpa", "spacesuit", "paa", "security", "cobalt");
    private static final Set<String> DIGAMMA_KEYWORDS = Set.of(
            "fau", "dns");
    private static final Set<String> DIGAMMA2_KEYWORDS = Set.of(
            "robe", "robes");
    private static final Set<String> BACTERIA_KEYWORDS = Set.of(
            "gas_mask", "gasmask", "filter", "hazmat", "paa", "liquidator",
            "schrabidium", "euphemium", "t51", "steamsuit", "ajr", "ajro",
            "rpa", "ncrpa", "envsuit", "hev", "fau", "dns", "taurun");

    public static boolean checkForHazmat(LivingEntity entity) {
        if (entity.hasEffect(ModEffects.MUTATION.get())) {
            return true;
        }
        return getWornPieces(entity) == 4 && HazmatRegistry.getResistance(entity) >= 0.6F;
    }

    public static boolean checkForHaz2(LivingEntity entity) {
        return getWornPieces(entity) == 4 && HazmatRegistry.getResistance(entity) >= 1.7F;
    }

    public static boolean hasFineParticleProtection(LivingEntity entity) {
        return checkForHaz2(entity);
    }

    public static boolean hasBacteriaProtection(LivingEntity entity) {
        ItemStack helmet = getHelmet(entity);
        return !helmet.isEmpty()
                && (containsAnyKeyword(helmet, BACTERIA_KEYWORDS)
                || HazmatRegistry.getResistance(helmet) >= 0.34D);
    }

    public static boolean checkForMkuProtection(LivingEntity entity) {
        return checkForHaz2(entity) && hasBacteriaProtection(entity);
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

    public static boolean checkForDigamma(Player player) {
        return player.hasEffect(ModEffects.STABILITY.get())
                || isWearingFullKeywordSet(player, DIGAMMA_KEYWORDS);
    }

    public static boolean checkForDigamma2(Player player) {
        return isWearingFullKeywordSet(player, DIGAMMA2_KEYWORDS)
                && player.hasEffect(ModEffects.STABILITY.get())
                && HazmatRegistry.getResistance(player) >= 0.4F
                && player.getMaxHealth() < 3.0F;
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

    private static ItemStack getHelmet(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD);
    }

    private static boolean isWearingFullKeywordSet(Player player, Set<String> keywords) {
        int pieces = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty() && containsAnyKeyword(stack, keywords)) {
                pieces++;
            }
        }
        return pieces == 4;
    }

    private static boolean containsAnyKeyword(ItemStack stack, Set<String> keywords) {
        String key = stack.getItem().getDescriptionId().toLowerCase(Locale.US);
        for (String keyword : keywords) {
            if (key.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private ArmorUtil() {
    }
}
