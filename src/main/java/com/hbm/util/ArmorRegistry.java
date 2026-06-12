package com.hbm.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Legacy package facade for the 1.7.10 hazard-class armor registry.
 */
@Deprecated(forRemoval = false)
public final class ArmorRegistry {
    public static final Map<Item, ArrayList<com.hbm.ntm.api.item.HazardClass>> hazardClasses =
            com.hbm.ntm.radiation.ArmorRegistry.hazardClasses;
    public static final String FILTER_KEY = com.hbm.ntm.radiation.ArmorRegistry.FILTER_KEY;
    public static final String FILTERK_KEY = com.hbm.ntm.radiation.ArmorRegistry.FILTERK_KEY;
    public static final com.hbm.ntm.api.item.HazardClass[] FULL_NO_LIGHT =
            com.hbm.ntm.radiation.ArmorRegistry.FULL_NO_LIGHT;
    public static final com.hbm.ntm.api.item.HazardClass[] FULL_PACKAGE =
            com.hbm.ntm.radiation.ArmorRegistry.FULL_PACKAGE;
    public static final int ASH_EXPOSURE_LIMIT_ASH_GLASSES =
            com.hbm.ntm.radiation.ArmorRegistry.ASH_EXPOSURE_LIMIT_ASH_GLASSES;
    public static final int ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT =
            com.hbm.ntm.radiation.ArmorRegistry.ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT;
    public static final int ASH_EXPOSURE_LIMIT_UNPROTECTED =
            com.hbm.ntm.radiation.ArmorRegistry.ASH_EXPOSURE_LIMIT_UNPROTECTED;

    public static void registerHazard(Item item, com.hbm.ntm.api.item.HazardClass... hazards) {
        com.hbm.ntm.radiation.ArmorRegistry.registerHazard(item, hazards);
    }

    public static void registerProtection(Item item, com.hbm.ntm.api.item.HazardClass... hazards) {
        com.hbm.ntm.radiation.ArmorRegistry.registerProtection(item, hazards);
    }

    public static boolean registerHazard(ResourceLocation itemId, com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerHazard(itemId, hazards);
    }

    public static boolean registerProtection(ResourceLocation itemId,
                                             com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerProtection(itemId, hazards);
    }

    public static boolean registerHazard(String itemId, com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerHazard(itemId, hazards);
    }

    public static boolean registerProtection(String itemId, com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerProtection(itemId, hazards);
    }

    public static void registerExternalHazard(Item item, com.hbm.ntm.api.item.HazardClass... hazards) {
        com.hbm.ntm.radiation.ArmorRegistry.registerExternalHazard(item, hazards);
    }

    public static void registerExternalProtection(Item item, com.hbm.ntm.api.item.HazardClass... hazards) {
        com.hbm.ntm.radiation.ArmorRegistry.registerExternalProtection(item, hazards);
    }

    public static boolean registerExternalHazard(ResourceLocation itemId,
                                                 com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerExternalHazard(itemId, hazards);
    }

    public static boolean registerExternalProtection(ResourceLocation itemId,
                                                     com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerExternalProtection(itemId, hazards);
    }

    public static boolean registerExternalHazard(String itemId, com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerExternalHazard(itemId, hazards);
    }

    public static boolean registerExternalProtection(String itemId, com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerExternalProtection(itemId, hazards);
    }

    public static ArrayList<com.hbm.ntm.api.item.HazardClass> removeHazard(Item item) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeHazard(item);
    }

    public static boolean removeHazard(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeHazard(itemId);
    }

    public static boolean removeHazard(String itemId) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeHazard(itemId);
    }

    public static EnumSet<com.hbm.ntm.api.item.HazardClass> removeExternalHazard(Item item) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeExternalHazard(item);
    }

    public static boolean removeExternalHazard(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeExternalHazard(itemId);
    }

    public static boolean removeExternalHazard(String itemId) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeExternalHazard(itemId);
    }

    public static void clearHazards() {
        com.hbm.ntm.radiation.ArmorRegistry.clearHazards();
    }

    public static void clearExternalHazards() {
        com.hbm.ntm.radiation.ArmorRegistry.clearExternalHazards();
    }

    public static ArrayList<com.hbm.ntm.api.item.HazardClass> getProtection(ItemStack stack) {
        return com.hbm.ntm.radiation.ArmorRegistry.getProtection(stack);
    }

    public static ArrayList<com.hbm.ntm.api.item.HazardClass> getProtection(LivingEntity entity, int slot) {
        return com.hbm.ntm.radiation.ArmorRegistry.getProtection(entity, slot);
    }

    public static ArrayList<com.hbm.ntm.api.item.HazardClass> getProtection(LivingEntity entity, EquipmentSlot slot) {
        return com.hbm.ntm.radiation.ArmorRegistry.getProtection(entity, slot);
    }

    public static boolean hasAllProtection(LivingEntity entity, int slot,
                                           com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtection(entity, slot, hazards);
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot,
                                           com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtection(entity, slot, hazards);
    }

    public static boolean hasAllProtection(LivingEntity entity,
                                           com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtection(entity, hazards);
    }

    public static boolean hasAnyProtection(LivingEntity entity, int slot,
                                           com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtection(entity, slot, hazards);
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot,
                                           com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtection(entity, slot, hazards);
    }

    public static boolean hasAnyProtection(LivingEntity entity,
                                           com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtection(entity, hazards);
    }

    public static boolean hasProtection(LivingEntity entity, int slot, com.hbm.ntm.api.item.HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtection(entity, slot, hazard);
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot,
                                        com.hbm.ntm.api.item.HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtection(entity, slot, hazard);
    }

    public static boolean hasProtection(LivingEntity entity, com.hbm.ntm.api.item.HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtection(entity, hazard);
    }

    public static List<com.hbm.ntm.api.item.HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.getProtectionFromItem(stack, entity);
    }

    public static Map<Item, EnumSet<com.hbm.ntm.api.item.HazardClass>> protectionSnapshot() {
        return com.hbm.ntm.radiation.ArmorRegistry.protectionSnapshot();
    }

    public static boolean hasFineParticleProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasFineParticleProtection(entity);
    }

    public static boolean hasCoarseParticleProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasCoarseParticleProtection(entity);
    }

    public static boolean hasMonoxideGasProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasMonoxideGasProtection(entity);
    }

    public static boolean hasLungGasProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasLungGasProtection(entity);
    }

    public static boolean hasBacteriaProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasBacteriaProtection(entity);
    }

    public static boolean hasBlisteringGasProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasBlisteringGasProtection(entity);
    }

    public static boolean hasLightProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasLightProtection(entity);
    }

    public static boolean hasSandProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasSandProtection(entity);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                       com.hbm.ntm.api.item.HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtectionAndDamageFilter(entity, slot, filterDamage, hazard);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, int slot,
                                                       com.hbm.ntm.api.item.HazardClass hazard, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtectionAndDamageFilter(entity, slot, hazard, filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity,
                                                       com.hbm.ntm.api.item.HazardClass hazard, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtectionAndDamageFilter(entity, hazard, filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                       com.hbm.ntm.api.item.HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtectionAndDamageFilter(entity, slot, filterDamage, hazard);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot,
                                                       com.hbm.ntm.api.item.HazardClass hazard, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtectionAndDamageFilter(entity, slot, hazard, filterDamage);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                          com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtectionAndDamageFilter(entity, slot, filterDamage,
                hazards);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
                                                          com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtectionAndDamageFilter(entity, filterDamage, hazards);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                          com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtectionAndDamageFilter(entity, slot, filterDamage,
                hazards);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                          com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage,
                hazards);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
                                                          com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtectionAndDamageFilter(entity, filterDamage, hazards);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                          com.hbm.ntm.api.item.HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage,
                hazards);
    }

    public static boolean hasFineParticleProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasFineParticleProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasCoarseParticleProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasCoarseParticleProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasMonoxideGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasMonoxideGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasLungGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasLungGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasBacteriaProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasBacteriaProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasBlisteringGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasBlisteringGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasToxinProtection(LivingEntity entity, com.hbm.ntm.api.item.HazardClass hazard,
                                             boolean requiresFullBodyProtection, boolean apply) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasToxinProtection(entity, hazard, requiresFullBodyProtection,
                apply);
    }

    public static boolean hasToxinProtection(LivingEntity entity, com.hbm.ntm.api.item.HazardClass hazard,
                                             boolean requiresFullBodyProtection, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasToxinProtection(entity, hazard, requiresFullBodyProtection,
                filterDamage);
    }

    public static boolean hasPollutionPoisonProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasPollutionPoisonProtection(entity);
    }

    public static boolean hasPollutionLeadProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasPollutionLeadProtection(entity);
    }

    public static boolean hasSootLungProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasSootLungProtection(entity);
    }

    public static boolean hasBlindingProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasBlindingProtection(entity);
    }

    public static boolean hasAshVisionPartialProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAshVisionPartialProtection(entity);
    }

    public static boolean checkForAshGlasses(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForAshGlasses(entity);
    }

    public static int getAshExposureLimit(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.getAshExposureLimit(entity);
    }

    public static boolean checkForMkuProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForMkuProtection(entity);
    }

    public static boolean checkForHazmat(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForHazmat(entity);
    }

    public static boolean checkForHaz2(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForHaz2(entity);
    }

    public static boolean checkForAsbestos(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForAsbestos(entity);
    }

    public static boolean checkForFaraday(Player player) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForFaraday(player);
    }

    public static boolean checkForDigamma(Player player) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForDigamma(player);
    }

    public static boolean checkForDigamma2(Player player) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForDigamma2(player);
    }

    public static boolean checkForFiend(Player player) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForFiend(player);
    }

    public static boolean checkForFiend2(Player player) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForFiend2(player);
    }

    public static boolean checkArmor(LivingEntity entity, Item... armor) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkArmor(entity, armor);
    }

    public static boolean checkArmorPiece(LivingEntity entity, Item armor, int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkArmorPiece(entity, armor, legacyArmorSlot);
    }

    public static boolean checkArmorNull(LivingEntity entity, int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkArmorNull(entity, legacyArmorSlot);
    }

    public static void damageSuit(LivingEntity entity, int legacyArmorSlot, int amount) {
        com.hbm.ntm.radiation.ArmorRegistry.damageSuit(entity, legacyArmorSlot, amount);
    }

    public static void damageSuit(LivingEntity entity, EquipmentSlot slot, int amount) {
        com.hbm.ntm.radiation.ArmorRegistry.damageSuit(entity, slot, amount);
    }

    public static void damageSuitAll(LivingEntity entity, int amount) {
        com.hbm.ntm.radiation.ArmorRegistry.damageSuitAll(entity, amount);
    }

    public static boolean isFaradayArmor(ItemStack stack) {
        return com.hbm.ntm.radiation.ArmorRegistry.isFaradayArmor(stack);
    }

    public static com.hbm.ntm.radiation.ArmorUtil.WornGasMask getWornGasMask(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.getWornGasMask(entity);
    }

    public static ItemStack getWornGasMaskFilter(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.getWornGasMaskFilter(entity);
    }

    public static boolean hasWornGasMask(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasWornGasMask(entity);
    }

    public static boolean hasWornGasMaskFilter(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasWornGasMaskFilter(entity);
    }

    public static boolean canInstallWornGasMaskFilter(LivingEntity entity, ItemStack filter) {
        return com.hbm.ntm.radiation.ArmorRegistry.canInstallWornGasMaskFilter(entity, filter);
    }

    public static boolean installWornGasMaskFilter(LivingEntity entity, ItemStack filter) {
        return com.hbm.ntm.radiation.ArmorRegistry.installWornGasMaskFilter(entity, filter);
    }

    public static com.hbm.ntm.radiation.ArmorUtil.GasMaskFilterInstallResult installWornGasMaskFilter(Player player,
                                                                                                       ItemStack filter) {
        return com.hbm.ntm.radiation.ArmorRegistry.installWornGasMaskFilter(player, filter);
    }

    public static ItemStack removeWornGasMaskFilter(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeWornGasMaskFilter(entity);
    }

    public static boolean removeWornGasMaskFilterToInventory(Player player) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeWornGasMaskFilterToInventory(player);
    }

    public static boolean removeGasMaskFilterToInventory(ItemStack mask, Player player) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeGasMaskFilterToInventory(mask, player);
    }

    public static ItemStack removeGasMaskFilterRecursively(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeGasMaskFilterRecursively(mask);
    }

    public static ItemStack getGasMaskFilterRecursively(ItemStack mask, LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.getGasMaskFilterRecursively(mask, entity);
    }

    public static ItemStack getGasMaskFilter(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorRegistry.getGasMaskFilter(mask);
    }

    public static boolean hasGasMaskFilter(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasGasMaskFilter(mask);
    }

    public static boolean hasGasMaskFilterRecursively(ItemStack mask, LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasGasMaskFilterRecursively(mask, entity);
    }

    public static boolean canInstallGasMaskFilter(ItemStack maskStack, LivingEntity entity, ItemStack filter) {
        return com.hbm.ntm.radiation.ArmorRegistry.canInstallGasMaskFilter(maskStack, entity, filter);
    }

    public static void installGasMaskFilter(ItemStack mask, ItemStack filter) {
        com.hbm.ntm.radiation.ArmorRegistry.installGasMaskFilter(mask, filter);
    }

    public static void removeFilter(ItemStack mask) {
        com.hbm.ntm.radiation.ArmorRegistry.removeFilter(mask);
    }

    public static void damageGasMaskFilter(LivingEntity entity, int damage) {
        com.hbm.ntm.radiation.ArmorRegistry.damageGasMaskFilter(entity, damage);
    }

    public static void damageGasMaskFilter(ItemStack mask, int damage) {
        com.hbm.ntm.radiation.ArmorRegistry.damageGasMaskFilter(mask, damage);
    }

    public static void addGasMaskTooltip(ItemStack maskStack, @Nullable LivingEntity entity,
                                         List<Component> tooltip, TooltipFlag flag) {
        com.hbm.ntm.radiation.ArmorRegistry.addGasMaskTooltip(maskStack, entity, tooltip, flag);
    }

    public static void addGasMaskTooltip(ItemStack maskStack, @Nullable LivingEntity entity,
                                         List<Component> tooltip, boolean advanced) {
        com.hbm.ntm.radiation.ArmorRegistry.addGasMaskTooltip(maskStack, entity, tooltip, advanced);
    }

    public static void addGasMaskBlacklistTooltip(ItemStack maskStack, @Nullable LivingEntity entity,
                                                  List<Component> tooltip) {
        com.hbm.ntm.radiation.ArmorRegistry.addGasMaskBlacklistTooltip(maskStack, entity, tooltip);
    }

    public static boolean isWearingEmptyMask(Player player) {
        return com.hbm.ntm.radiation.ArmorRegistry.isWearingEmptyMask(player);
    }

    public static EquipmentSlot legacyEquipmentSlot(int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorRegistry.legacyEquipmentSlot(legacyArmorSlot);
    }

    @Nullable
    public static EquipmentSlot tryLegacyEquipmentSlot(int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorRegistry.tryLegacyEquipmentSlot(legacyArmorSlot);
    }

    public static final class HazardClass {
        public static final com.hbm.ntm.api.item.HazardClass GAS_LUNG =
                com.hbm.ntm.api.item.HazardClass.GAS_LUNG;
        public static final com.hbm.ntm.api.item.HazardClass GAS_MONOXIDE =
                com.hbm.ntm.api.item.HazardClass.GAS_MONOXIDE;
        public static final com.hbm.ntm.api.item.HazardClass GAS_INERT =
                com.hbm.ntm.api.item.HazardClass.GAS_INERT;
        public static final com.hbm.ntm.api.item.HazardClass PARTICLE_COARSE =
                com.hbm.ntm.api.item.HazardClass.PARTICLE_COARSE;
        public static final com.hbm.ntm.api.item.HazardClass PARTICLE_FINE =
                com.hbm.ntm.api.item.HazardClass.PARTICLE_FINE;
        public static final com.hbm.ntm.api.item.HazardClass BACTERIA =
                com.hbm.ntm.api.item.HazardClass.BACTERIA;
        public static final com.hbm.ntm.api.item.HazardClass GAS_BLISTERING =
                com.hbm.ntm.api.item.HazardClass.GAS_BLISTERING;
        public static final com.hbm.ntm.api.item.HazardClass SAND =
                com.hbm.ntm.api.item.HazardClass.SAND;
        public static final com.hbm.ntm.api.item.HazardClass LIGHT =
                com.hbm.ntm.api.item.HazardClass.LIGHT;

        public static com.hbm.ntm.api.item.HazardClass[] values() {
            return com.hbm.ntm.api.item.HazardClass.values();
        }

        public static com.hbm.ntm.api.item.HazardClass valueOf(String name) {
            return com.hbm.ntm.api.item.HazardClass.valueOf(name);
        }

        private HazardClass() {
        }
    }

    private ArmorRegistry() {
    }
}
