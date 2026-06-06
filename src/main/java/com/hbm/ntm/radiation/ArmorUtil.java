package com.hbm.ntm.radiation;

import com.hbm.ntm.api.item.GasMask;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
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
    private static final Set<HazardClass> STANDARD_FILTER = EnumSet.of(
            HazardClass.PARTICLE_COARSE,
            HazardClass.PARTICLE_FINE,
            HazardClass.GAS_LUNG,
            HazardClass.GAS_BLISTERING,
            HazardClass.BACTERIA);
    private static final Set<HazardClass> MONOXIDE_FILTER = EnumSet.of(
            HazardClass.PARTICLE_COARSE,
            HazardClass.GAS_MONOXIDE);
    private static final Set<HazardClass> COMBO_FILTER = EnumSet.of(
            HazardClass.PARTICLE_COARSE,
            HazardClass.PARTICLE_FINE,
            HazardClass.GAS_LUNG,
            HazardClass.GAS_BLISTERING,
            HazardClass.BACTERIA,
            HazardClass.GAS_MONOXIDE);
    private static final Set<HazardClass> FULL_PACKAGE = EnumSet.of(
            HazardClass.PARTICLE_COARSE,
            HazardClass.PARTICLE_FINE,
            HazardClass.GAS_LUNG,
            HazardClass.BACTERIA,
            HazardClass.GAS_BLISTERING,
            HazardClass.GAS_MONOXIDE,
            HazardClass.LIGHT,
            HazardClass.SAND);

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
        return hasProtection(entity, HazardClass.PARTICLE_FINE);
    }

    public static boolean hasCoarseParticleProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.PARTICLE_COARSE);
    }

    public static boolean hasMonoxideGasProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.GAS_MONOXIDE);
    }

    public static boolean hasLungGasProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.GAS_LUNG);
    }

    public static boolean hasBacteriaProtection(LivingEntity entity) {
        return hasProtection(entity, HazardClass.BACTERIA);
    }

    public static boolean hasProtection(LivingEntity entity, HazardClass hazardClass) {
        return getProtectionFromItem(getHelmet(entity), entity).contains(hazardClass);
    }

    public static boolean hasAllProtection(LivingEntity entity, HazardClass... hazardClasses) {
        Set<HazardClass> protections = getProtectionFromItem(getHelmet(entity), entity);
        for (HazardClass hazardClass : hazardClasses) {
            if (!protections.contains(hazardClass)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasAnyProtection(LivingEntity entity, HazardClass... hazardClasses) {
        Set<HazardClass> protections = getProtectionFromItem(getHelmet(entity), entity);
        for (HazardClass hazardClass : hazardClasses) {
            if (protections.contains(hazardClass)) {
                return true;
            }
        }
        return false;
    }

    public static void damageGasMaskFilter(LivingEntity entity, int damage) {
        ItemStack helmet = getHelmet(entity);
        if (!helmet.isEmpty() && helmet.getItem() instanceof GasMask mask) {
            mask.damageFilter(helmet, entity, Math.max(0, damage));
        }
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

    private static Set<HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity) {
        Set<HazardClass> protections = EnumSet.noneOf(HazardClass.class);
        if (stack.isEmpty()) {
            return protections;
        }

        protections.addAll(getLegacyDirectProtection(stack));
        if (stack.getItem() instanceof GasMask mask) {
            ItemStack filter = mask.getFilter(stack, entity);
            if (!filter.isEmpty() && mask.isFilterApplicable(stack, entity, filter)) {
                Set<HazardClass> filterProtection = getLegacyDirectProtection(filter);
                for (HazardClass blacklisted : mask.getBlacklist(stack, entity)) {
                    filterProtection.remove(blacklisted);
                }
                protections.addAll(filterProtection);
            }
        }
        return protections;
    }

    private static Set<HazardClass> getLegacyDirectProtection(ItemStack stack) {
        Set<HazardClass> protections = EnumSet.noneOf(HazardClass.class);
        String key = getItemKey(stack);

        if (key.contains("gas_mask_filter_combo")) {
            protections.addAll(COMBO_FILTER);
        } else if (key.contains("gas_mask_filter_mono")) {
            protections.addAll(MONOXIDE_FILTER);
        } else if (key.contains("gas_mask_filter_rag")) {
            protections.add(HazardClass.PARTICLE_COARSE);
        } else if (key.contains("gas_mask_filter_piss")) {
            protections.add(HazardClass.PARTICLE_COARSE);
            protections.add(HazardClass.GAS_LUNG);
        } else if (key.contains("gas_mask_filter")) {
            protections.addAll(STANDARD_FILTER);
        }

        if (key.contains("gas_mask_m65")) {
            protections.add(HazardClass.SAND);
        } else if (key.contains("gas_mask") && !key.contains("gas_mask_filter")) {
            protections.add(HazardClass.SAND);
            protections.add(HazardClass.LIGHT);
        }

        if (key.contains("mask_rag")) {
            protections.add(HazardClass.PARTICLE_COARSE);
        } else if (key.contains("mask_piss")) {
            protections.add(HazardClass.PARTICLE_COARSE);
            protections.add(HazardClass.GAS_LUNG);
        }

        if (key.contains("goggles") || key.contains("ashglasses")
                || key.contains("asbestos_helmet")) {
            protections.add(HazardClass.LIGHT);
            protections.add(HazardClass.SAND);
        } else if (key.contains("attachment_mask")
                || key.contains("hazmat_helmet")
                || key.contains("hazmat_helmet_red")
                || key.contains("hazmat_helmet_grey")) {
            protections.add(HazardClass.SAND);
        } else if (key.contains("hazmat_paa_helmet")
                || key.contains("liquidator_helmet")) {
            protections.add(HazardClass.LIGHT);
            protections.add(HazardClass.SAND);
        }

        if (key.contains("schrabidium_helmet") || key.contains("euphemium_helmet")) {
            protections.addAll(FULL_PACKAGE);
        }

        return protections;
    }

    private static String getItemKey(ItemStack stack) {
        return (BuiltInRegistries.ITEM.getKey(stack.getItem()) + " " + stack.getItem().getDescriptionId())
                .toLowerCase(Locale.US);
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
