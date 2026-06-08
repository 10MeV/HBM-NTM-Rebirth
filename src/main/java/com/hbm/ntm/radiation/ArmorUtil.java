package com.hbm.ntm.radiation;

import com.hbm.ntm.api.item.GasMask;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Set;

public final class ArmorUtil {
    public static final String FILTER_KEY = "hfrFilter";

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
        return HazmatRegistry.getProtectionFromItem(getHelmet(entity), entity).contains(hazardClass);
    }

    public static boolean hasAllProtection(LivingEntity entity, HazardClass... hazardClasses) {
        Set<HazardClass> protections = HazmatRegistry.getProtectionFromItem(getHelmet(entity), entity);
        for (HazardClass hazardClass : hazardClasses) {
            if (!protections.contains(hazardClass)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasAnyProtection(LivingEntity entity, HazardClass... hazardClasses) {
        Set<HazardClass> protections = HazmatRegistry.getProtectionFromItem(getHelmet(entity), entity);
        for (HazardClass hazardClass : hazardClasses) {
            if (protections.contains(hazardClass)) {
                return true;
            }
        }
        return false;
    }

    public static void damageGasMaskFilter(LivingEntity entity, int damage) {
        ItemStack helmet = getHelmet(entity);
        if (helmet.isEmpty()) {
            return;
        }
        ItemStack maskStack = helmet;
        boolean attachedMask = false;
        if (!(maskStack.getItem() instanceof GasMask) && ArmorModHandler.hasMods(helmet)) {
            ItemStack mod = ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only);
            if (!mod.isEmpty() && mod.getItem() instanceof GasMask) {
                maskStack = mod;
                attachedMask = true;
            }
        }
        if (maskStack.getItem() instanceof GasMask mask) {
            mask.damageFilter(maskStack, entity, Math.max(0, damage));
            if (attachedMask) {
                ArmorModHandler.applyMod(helmet, maskStack);
            }
        }
    }

    public static ItemStack getGasMaskFilterRecursively(ItemStack mask, LivingEntity entity) {
        ItemStack filter = getGasMaskFilter(mask);
        if (filter.isEmpty() && ArmorModHandler.hasMods(mask)) {
            ItemStack mod = ArmorModHandler.pryMod(mask, ArmorModHandler.helmet_only);
            if (!mod.isEmpty() && mod.getItem() instanceof GasMask gasMask) {
                filter = gasMask.getFilter(mod, entity);
            }
        }
        return filter;
    }

    public static ItemStack getGasMaskFilter(ItemStack mask) {
        if (mask == null || mask.isEmpty() || !mask.hasTag()) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = mask.getTag();
        if (tag == null || !tag.contains(FILTER_KEY, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound(FILTER_KEY));
    }

    public static void installGasMaskFilter(ItemStack mask, ItemStack filter) {
        if (mask == null || mask.isEmpty() || filter == null || filter.isEmpty()) {
            return;
        }
        mask.getOrCreateTag().put(FILTER_KEY, filter.copyWithCount(1).save(new CompoundTag()));
    }

    public static void removeFilter(ItemStack mask) {
        if (mask == null || mask.isEmpty() || !mask.hasTag()) {
            return;
        }
        mask.getOrCreateTag().remove(FILTER_KEY);
    }

    public static void damageGasMaskFilter(ItemStack mask, int damage) {
        ItemStack filter = getGasMaskFilter(mask);
        if (filter.isEmpty() || !filter.isDamageableItem()) {
            return;
        }
        filter.setDamageValue(filter.getDamageValue() + Math.max(0, damage));
        if (filter.getDamageValue() > filter.getMaxDamage()) {
            removeFilter(mask);
        } else {
            installGasMaskFilter(mask, filter);
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
