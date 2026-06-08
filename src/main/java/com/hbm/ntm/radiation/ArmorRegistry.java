package com.hbm.ntm.radiation;

import com.hbm.ntm.api.item.HazardClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Compatibility facade for the 1.7.10 ArmorRegistry hazard-class API.
 */
public final class ArmorRegistry {
    public static final HazardClass[] FULL_NO_LIGHT = ArmorUtil.FULL_NO_LIGHT;
    public static final HazardClass[] FULL_PACKAGE = ArmorUtil.FULL_PACKAGE;
    public static final int ASH_EXPOSURE_LIMIT_ASH_GLASSES = ArmorUtil.ASH_EXPOSURE_LIMIT_ASH_GLASSES;
    public static final int ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT = ArmorUtil.ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT;
    public static final int ASH_EXPOSURE_LIMIT_UNPROTECTED = ArmorUtil.ASH_EXPOSURE_LIMIT_UNPROTECTED;

    public static void registerHazard(Item item, HazardClass... hazards) {
        HazmatRegistry.registerProtection(item, hazards);
    }

    public static boolean registerHazard(ResourceLocation itemId, HazardClass... hazards) {
        return HazmatRegistry.registerProtection(itemId, hazards);
    }

    public static boolean registerHazard(String itemId, HazardClass... hazards) {
        return HazmatRegistry.registerProtection(itemId, hazards);
    }

    public static void registerExternalHazard(Item item, HazardClass... hazards) {
        HazmatRegistry.registerExternalProtection(item, hazards);
    }

    public static boolean registerExternalHazard(ResourceLocation itemId, HazardClass... hazards) {
        return HazmatRegistry.registerExternalProtection(itemId, hazards);
    }

    public static boolean registerExternalHazard(String itemId, HazardClass... hazards) {
        return HazmatRegistry.registerExternalProtection(itemId, hazards);
    }

    public static boolean hasAllProtection(LivingEntity entity, int slot, HazardClass... hazards) {
        EquipmentSlot equipmentSlot = tryLegacyEquipmentSlot(slot);
        return equipmentSlot != null && HazmatRegistry.hasAllProtection(entity, equipmentSlot, hazards);
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return HazmatRegistry.hasAllProtection(entity, slot, hazards);
    }

    public static boolean hasAnyProtection(LivingEntity entity, int slot, HazardClass... hazards) {
        EquipmentSlot equipmentSlot = tryLegacyEquipmentSlot(slot);
        return equipmentSlot != null && HazmatRegistry.hasAnyProtection(entity, equipmentSlot, hazards);
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return HazmatRegistry.hasAnyProtection(entity, slot, hazards);
    }

    public static boolean hasProtection(LivingEntity entity, int slot, HazardClass hazard) {
        EquipmentSlot equipmentSlot = tryLegacyEquipmentSlot(slot);
        return equipmentSlot != null && HazmatRegistry.hasProtection(entity, equipmentSlot, hazard);
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot, HazardClass hazard) {
        return HazmatRegistry.hasProtection(entity, slot, hazard);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                       HazardClass hazard) {
        return ArmorUtil.hasProtectionAndDamageFilter(entity, slot, hazard, filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                       HazardClass hazard) {
        return ArmorUtil.hasProtectionAndDamageFilter(entity, slot, hazard, filterDamage);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                          HazardClass... hazards) {
        return ArmorUtil.hasAllProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                          HazardClass... hazards) {
        return ArmorUtil.hasAllProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                          HazardClass... hazards) {
        return ArmorUtil.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                          HazardClass... hazards) {
        return ArmorUtil.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazard, boolean requiresFullBodyProtection,
                                             boolean apply) {
        return ArmorUtil.hasToxinProtection(entity, hazard, requiresFullBodyProtection, apply);
    }

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazard, boolean requiresFullBodyProtection,
                                             int filterDamage) {
        return ArmorUtil.hasToxinProtection(entity, hazard, requiresFullBodyProtection, filterDamage);
    }

    public static boolean hasPollutionPoisonProtection(LivingEntity entity) {
        return ArmorUtil.hasPollutionPoisonProtection(entity);
    }

    public static boolean hasPollutionLeadProtection(LivingEntity entity) {
        return ArmorUtil.hasPollutionLeadProtection(entity);
    }

    public static boolean hasSootLungProtection(LivingEntity entity) {
        return ArmorUtil.hasSootLungProtection(entity);
    }

    public static boolean hasBlindingProtection(LivingEntity entity) {
        return ArmorUtil.hasBlindingProtection(entity);
    }

    public static boolean hasAshVisionPartialProtection(LivingEntity entity) {
        return ArmorUtil.hasAshVisionPartialProtection(entity);
    }

    public static boolean checkForAshGlasses(LivingEntity entity) {
        return ArmorUtil.checkForAshGlasses(entity);
    }

    public static int getAshExposureLimit(LivingEntity entity) {
        return ArmorUtil.getAshExposureLimit(entity);
    }

    public static boolean checkForMkuProtection(LivingEntity entity) {
        return ArmorUtil.checkForMkuProtection(entity);
    }

    public static void damageSuit(LivingEntity entity, int legacyArmorSlot, int amount) {
        ArmorUtil.damageSuit(entity, legacyArmorSlot, amount);
    }

    public static void damageSuit(LivingEntity entity, EquipmentSlot slot, int amount) {
        ArmorUtil.damageSuit(entity, slot, amount);
    }

    public static void damageSuitAll(LivingEntity entity, int amount) {
        ArmorUtil.damageSuitAll(entity, amount);
    }

    public static List<HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity) {
        return new ArrayList<>(HazmatRegistry.getProtectionFromItem(stack, entity));
    }

    public static Map<Item, EnumSet<HazardClass>> protectionSnapshot() {
        return HazmatRegistry.protectionSnapshot();
    }

    public static EquipmentSlot legacyEquipmentSlot(int legacyArmorSlot) {
        return ArmorUtil.legacyEquipmentSlot(legacyArmorSlot);
    }

    @Nullable
    public static EquipmentSlot tryLegacyEquipmentSlot(int legacyArmorSlot) {
        return ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
    }

    private ArmorRegistry() {
    }
}
