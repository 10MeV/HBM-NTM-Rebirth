package com.hbm.util;

import com.hbm.ntm.api.item.HazardClass;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Legacy package facade for the 1.7.10 armor protection helpers.
 */
@Deprecated(forRemoval = false)
public final class ArmorUtil {
    public static final List<Tuple.Pair<Item, HazardClass[]>> external = new LegacyExternalProtectionList();
    public static final HazardClass[] FULL_NO_LIGHT = com.hbm.ntm.radiation.ArmorUtil.FULL_NO_LIGHT;
    public static final HazardClass[] FULL_PACKAGE = com.hbm.ntm.radiation.ArmorUtil.FULL_PACKAGE;
    public static final String[] metals = com.hbm.ntm.radiation.ArmorUtil.metals;
    public static final String FILTER_KEY = com.hbm.ntm.radiation.ArmorUtil.FILTER_KEY;
    public static final String FILTERK_KEY = com.hbm.ntm.radiation.ArmorUtil.FILTERK_KEY;
    public static final int ASH_EXPOSURE_LIMIT_ASH_GLASSES =
            com.hbm.ntm.radiation.ArmorUtil.ASH_EXPOSURE_LIMIT_ASH_GLASSES;
    public static final int ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT =
            com.hbm.ntm.radiation.ArmorUtil.ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT;
    public static final int ASH_EXPOSURE_LIMIT_UNPROTECTED =
            com.hbm.ntm.radiation.ArmorUtil.ASH_EXPOSURE_LIMIT_UNPROTECTED;

    public static void register() {
        com.hbm.ntm.radiation.ArmorUtil.register();
    }

    public static void registerProtection(Item item, HazardClass... hazardClasses) {
        com.hbm.ntm.radiation.ArmorUtil.registerProtection(item, hazardClasses);
    }

    public static boolean registerProtection(ResourceLocation itemId, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.registerProtection(itemId, hazardClasses);
    }

    public static boolean registerProtection(String itemId, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.registerProtection(itemId, hazardClasses);
    }

    public static void registerExternalProtection(Item item, HazardClass... hazardClasses) {
        com.hbm.ntm.radiation.ArmorUtil.registerExternalProtection(item, hazardClasses);
    }

    public static boolean registerExternalProtection(ResourceLocation itemId, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.registerExternalProtection(itemId, hazardClasses);
    }

    public static boolean registerExternalProtection(String itemId, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.registerExternalProtection(itemId, hazardClasses);
    }

    public static EnumSet<HazardClass> removeProtection(Item item) {
        return com.hbm.ntm.radiation.ArmorUtil.removeProtection(item);
    }

    public static boolean removeProtection(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.ArmorUtil.removeProtection(itemId);
    }

    public static boolean removeProtection(String itemId) {
        return com.hbm.ntm.radiation.ArmorUtil.removeProtection(itemId);
    }

    public static EnumSet<HazardClass> removeExternalProtection(Item item) {
        return com.hbm.ntm.radiation.ArmorUtil.removeExternalProtection(item);
    }

    public static boolean removeExternalProtection(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.ArmorUtil.removeExternalProtection(itemId);
    }

    public static boolean removeExternalProtection(String itemId) {
        return com.hbm.ntm.radiation.ArmorUtil.removeExternalProtection(itemId);
    }

    public static void clearProtections() {
        com.hbm.ntm.radiation.ArmorUtil.clearProtections();
    }

    public static void clearExternalProtections() {
        com.hbm.ntm.radiation.ArmorUtil.clearExternalProtections();
    }

    public static Set<HazardClass> getProtection(ItemStack stack) {
        return com.hbm.ntm.radiation.ArmorUtil.getProtection(stack);
    }

    public static Set<HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.getProtectionFromItem(stack, entity);
    }

    public static Map<Item, EnumSet<HazardClass>> protectionSnapshot() {
        return com.hbm.ntm.radiation.ArmorUtil.protectionSnapshot();
    }

    public static boolean checkArmor(LivingEntity entity, Item... armor) {
        return com.hbm.ntm.radiation.ArmorUtil.checkArmor(entity, armor);
    }

    public static boolean checkArmorPiece(LivingEntity entity, Item armor, int slot) {
        return com.hbm.ntm.radiation.ArmorUtil.checkArmorPiece(entity, armor, slot);
    }

    public static boolean checkArmorNull(LivingEntity entity, int slot) {
        return com.hbm.ntm.radiation.ArmorUtil.checkArmorNull(entity, slot);
    }

    public static void damageSuit(LivingEntity entity, int slot, int amount) {
        com.hbm.ntm.radiation.ArmorUtil.damageSuit(entity, slot, amount);
    }

    public static void damageSuit(LivingEntity entity, EquipmentSlot slot, int amount) {
        com.hbm.ntm.radiation.ArmorUtil.damageSuit(entity, slot, amount);
    }

    public static void damageSuitAll(LivingEntity entity, int amount) {
        com.hbm.ntm.radiation.ArmorUtil.damageSuitAll(entity, amount);
    }

    public static boolean checkForHazmat(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForHazmat(entity);
    }

    public static boolean checkForHaz2(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForHaz2(entity);
    }

    public static boolean checkForAsbestos(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForAsbestos(entity);
    }

    public static boolean checkForFaraday(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForFaraday(player);
    }

    public static boolean checkForDigamma(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForDigamma(player);
    }

    public static boolean checkForDigamma2(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForDigamma2(player);
    }

    public static boolean checkForFiend(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForFiend(player);
    }

    public static boolean checkForFiend2(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForFiend2(player);
    }

    public static boolean isFaradayArmor(ItemStack stack) {
        return com.hbm.ntm.radiation.ArmorUtil.isFaradayArmor(stack);
    }

    public static boolean hasProtection(LivingEntity entity, HazardClass hazardClass) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtection(entity, hazardClass);
    }

    public static boolean hasProtection(LivingEntity entity, int slot, HazardClass hazardClass) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtection(entity, slot, hazardClass);
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot, HazardClass hazardClass) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtection(entity, slot, hazardClass);
    }

    public static boolean hasAllProtection(LivingEntity entity, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtection(entity, hazardClasses);
    }

    public static boolean hasAllProtection(LivingEntity entity, int slot, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtection(entity, slot, hazardClasses);
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtection(entity, slot, hazardClasses);
    }

    public static boolean hasAnyProtection(LivingEntity entity, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtection(entity, hazardClasses);
    }

    public static boolean hasAnyProtection(LivingEntity entity, int slot, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtection(entity, slot, hazardClasses);
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtection(entity, slot, hazardClasses);
    }

    public static boolean hasFineParticleProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasFineParticleProtection(entity);
    }

    public static boolean hasCoarseParticleProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasCoarseParticleProtection(entity);
    }

    public static boolean hasMonoxideGasProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasMonoxideGasProtection(entity);
    }

    public static boolean hasLungGasProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasLungGasProtection(entity);
    }

    public static boolean hasBacteriaProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasBacteriaProtection(entity);
    }

    public static boolean hasBlisteringGasProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasBlisteringGasProtection(entity);
    }

    public static boolean hasLightProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasLightProtection(entity);
    }

    public static boolean hasSandProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasSandProtection(entity);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, HazardClass hazardClass,
                                                       int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtectionAndDamageFilter(entity, hazardClass, filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, int slot, HazardClass hazardClass,
                                                       int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtectionAndDamageFilter(entity, slot, hazardClass, filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot,
                                                       HazardClass hazardClass, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtectionAndDamageFilter(entity, slot, hazardClass, filterDamage);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
                                                          HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtectionAndDamageFilter(entity, filterDamage, hazardClasses);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                          HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtectionAndDamageFilter(entity, slot, filterDamage,
                hazardClasses);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                          HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtectionAndDamageFilter(entity, slot, filterDamage,
                hazardClasses);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
                                                          HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtectionAndDamageFilter(entity, filterDamage, hazardClasses);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                          HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage,
                hazardClasses);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                          HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage,
                hazardClasses);
    }

    public static boolean hasFineParticleProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasFineParticleProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasCoarseParticleProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasCoarseParticleProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasMonoxideGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasMonoxideGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasLungGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasLungGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasBacteriaProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasBacteriaProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasBlisteringGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasBlisteringGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazardClass,
                                             boolean requiresFullBodyProtection, boolean apply) {
        return com.hbm.ntm.radiation.ArmorUtil.hasToxinProtection(entity, hazardClass,
                requiresFullBodyProtection, apply);
    }

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazardClass,
                                             boolean requiresFullBodyProtection, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasToxinProtection(entity, hazardClass,
                requiresFullBodyProtection, filterDamage);
    }

    public static boolean hasPollutionPoisonProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasPollutionPoisonProtection(entity);
    }

    public static boolean hasPollutionLeadProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasPollutionLeadProtection(entity);
    }

    public static boolean hasSootLungProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasSootLungProtection(entity);
    }

    public static boolean hasBlindingProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasBlindingProtection(entity);
    }

    public static boolean hasAshVisionPartialProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAshVisionPartialProtection(entity);
    }

    public static boolean checkForAshGlasses(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForAshGlasses(entity);
    }

    public static int getAshExposureLimit(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.getAshExposureLimit(entity);
    }

    public static boolean checkForMkuProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForMkuProtection(entity);
    }

    public static com.hbm.ntm.radiation.ArmorUtil.WornGasMask getWornGasMask(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.getWornGasMask(entity);
    }

    public static ItemStack getWornGasMaskFilter(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.getWornGasMaskFilter(entity);
    }

    public static boolean hasWornGasMask(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasWornGasMask(entity);
    }

    public static boolean hasWornGasMaskFilter(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasWornGasMaskFilter(entity);
    }

    public static boolean canInstallWornGasMaskFilter(LivingEntity entity, ItemStack filter) {
        return com.hbm.ntm.radiation.ArmorUtil.canInstallWornGasMaskFilter(entity, filter);
    }

    public static boolean installWornGasMaskFilter(LivingEntity entity, ItemStack filter) {
        return com.hbm.ntm.radiation.ArmorUtil.installWornGasMaskFilter(entity, filter);
    }

    public static com.hbm.ntm.radiation.ArmorUtil.GasMaskFilterInstallResult installWornGasMaskFilter(Player player,
                                                                                                       ItemStack filter) {
        return com.hbm.ntm.radiation.ArmorUtil.installWornGasMaskFilter(player, filter);
    }

    public static ItemStack removeWornGasMaskFilter(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.removeWornGasMaskFilter(entity);
    }

    public static boolean removeWornGasMaskFilterToInventory(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.removeWornGasMaskFilterToInventory(player);
    }

    public static boolean removeGasMaskFilterToInventory(ItemStack mask, Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.removeGasMaskFilterToInventory(mask, player);
    }

    public static ItemStack removeGasMaskFilterRecursively(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorUtil.removeGasMaskFilterRecursively(mask);
    }

    public static void installGasMaskFilter(ItemStack mask, ItemStack filter) {
        com.hbm.ntm.radiation.ArmorUtil.installGasMaskFilter(mask, filter);
    }

    public static void removeFilter(ItemStack mask) {
        com.hbm.ntm.radiation.ArmorUtil.removeFilter(mask);
    }

    public static ItemStack getGasMaskFilter(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorUtil.getGasMaskFilter(mask);
    }

    public static ItemStack getGasMaskFilterRecursively(ItemStack mask, LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.getGasMaskFilterRecursively(mask, entity);
    }

    public static boolean hasGasMaskFilter(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorUtil.hasGasMaskFilter(mask);
    }

    public static boolean hasGasMaskFilterRecursively(ItemStack mask, LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasGasMaskFilterRecursively(mask, entity);
    }

    public static boolean canInstallGasMaskFilter(ItemStack maskStack, LivingEntity entity, ItemStack filter) {
        return com.hbm.ntm.radiation.ArmorUtil.canInstallGasMaskFilter(maskStack, entity, filter);
    }

    public static void damageGasMaskFilter(LivingEntity entity, int damage) {
        com.hbm.ntm.radiation.ArmorUtil.damageGasMaskFilter(entity, damage);
    }

    public static void damageGasMaskFilter(ItemStack mask, int damage) {
        com.hbm.ntm.radiation.ArmorUtil.damageGasMaskFilter(mask, damage);
    }

    public static void addGasMaskTooltip(ItemStack maskStack, @Nullable LivingEntity entity,
                                         List<Component> tooltip, TooltipFlag flag) {
        com.hbm.ntm.radiation.ArmorUtil.addGasMaskTooltip(maskStack, entity, tooltip, flag);
    }

    public static void addGasMaskTooltip(ItemStack maskStack, @Nullable LivingEntity entity,
                                         List<Component> tooltip, boolean advanced) {
        com.hbm.ntm.radiation.ArmorUtil.addGasMaskTooltip(maskStack, entity, tooltip, advanced);
    }

    public static void addGasMaskBlacklistTooltip(ItemStack maskStack, @Nullable LivingEntity entity,
                                                  List<Component> tooltip) {
        com.hbm.ntm.radiation.ArmorUtil.addGasMaskBlacklistTooltip(maskStack, entity, tooltip);
    }

    public static boolean isWearingEmptyMask(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.isWearingEmptyMask(player);
    }

    public static EquipmentSlot legacyEquipmentSlot(int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorUtil.legacyEquipmentSlot(legacyArmorSlot);
    }

    @Nullable
    public static EquipmentSlot tryLegacyEquipmentSlot(int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
    }

    private static final class LegacyExternalProtectionList extends AbstractList<Tuple.Pair<Item, HazardClass[]>> {
        @Override
        public Tuple.Pair<Item, HazardClass[]> get(int index) {
            com.hbm.ntm.util.HbmTuple.Pair<Item, HazardClass[]> entry =
                    com.hbm.ntm.radiation.ArmorUtil.external.get(index);
            return new Tuple.Pair<>(entry.getKey(), entry.getValue().clone());
        }

        @Override
        public int size() {
            return com.hbm.ntm.radiation.ArmorUtil.external.size();
        }

        @Override
        public void add(int index, Tuple.Pair<Item, HazardClass[]> element) {
            com.hbm.ntm.radiation.ArmorUtil.external.add(index, element);
        }

        @Override
        public Tuple.Pair<Item, HazardClass[]> set(int index, Tuple.Pair<Item, HazardClass[]> element) {
            com.hbm.ntm.util.HbmTuple.Pair<Item, HazardClass[]> previous =
                    com.hbm.ntm.radiation.ArmorUtil.external.set(index, element);
            return new Tuple.Pair<>(previous.getKey(), previous.getValue().clone());
        }

        @Override
        public Tuple.Pair<Item, HazardClass[]> remove(int index) {
            com.hbm.ntm.util.HbmTuple.Pair<Item, HazardClass[]> previous =
                    com.hbm.ntm.radiation.ArmorUtil.external.remove(index);
            return new Tuple.Pair<>(previous.getKey(), previous.getValue().clone());
        }

        @Override
        public boolean contains(Object object) {
            return com.hbm.ntm.radiation.ArmorUtil.external.contains(object);
        }

        @Override
        public int indexOf(Object object) {
            return com.hbm.ntm.radiation.ArmorUtil.external.indexOf(object);
        }

        @Override
        public int lastIndexOf(Object object) {
            return com.hbm.ntm.radiation.ArmorUtil.external.lastIndexOf(object);
        }

        @Override
        public boolean remove(Object object) {
            return com.hbm.ntm.radiation.ArmorUtil.external.remove(object);
        }

        @Override
        public void clear() {
            com.hbm.ntm.radiation.ArmorUtil.external.clear();
        }
    }

    private ArmorUtil() {
    }
}
