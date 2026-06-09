package com.hbm.ntm.radiation;

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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compatibility facade for the 1.7.10 ArmorRegistry hazard-class API.
 */
public final class ArmorRegistry {
    public static final Map<Item, ArrayList<HazardClass>> hazardClasses = new LegacyHazardClassMap();
    public static final String FILTER_KEY = ArmorUtil.FILTER_KEY;
    public static final String FILTERK_KEY = ArmorUtil.FILTERK_KEY;
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

    public static boolean hasAllProtection(LivingEntity entity, HazardClass... hazards) {
        return ArmorUtil.hasAllProtection(entity, hazards);
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return HazmatRegistry.hasAllProtection(entity, slot, hazards);
    }

    public static boolean hasAnyProtection(LivingEntity entity, int slot, HazardClass... hazards) {
        EquipmentSlot equipmentSlot = tryLegacyEquipmentSlot(slot);
        return equipmentSlot != null && HazmatRegistry.hasAnyProtection(entity, equipmentSlot, hazards);
    }

    public static boolean hasAnyProtection(LivingEntity entity, HazardClass... hazards) {
        return ArmorUtil.hasAnyProtection(entity, hazards);
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return HazmatRegistry.hasAnyProtection(entity, slot, hazards);
    }

    public static boolean hasProtection(LivingEntity entity, int slot, HazardClass hazard) {
        EquipmentSlot equipmentSlot = tryLegacyEquipmentSlot(slot);
        return equipmentSlot != null && HazmatRegistry.hasProtection(entity, equipmentSlot, hazard);
    }

    public static boolean hasProtection(LivingEntity entity, HazardClass hazard) {
        return ArmorUtil.hasProtection(entity, hazard);
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot, HazardClass hazard) {
        return HazmatRegistry.hasProtection(entity, slot, hazard);
    }

    public static boolean hasFineParticleProtection(LivingEntity entity) {
        return ArmorUtil.hasFineParticleProtection(entity);
    }

    public static boolean hasCoarseParticleProtection(LivingEntity entity) {
        return ArmorUtil.hasCoarseParticleProtection(entity);
    }

    public static boolean hasMonoxideGasProtection(LivingEntity entity) {
        return ArmorUtil.hasMonoxideGasProtection(entity);
    }

    public static boolean hasLungGasProtection(LivingEntity entity) {
        return ArmorUtil.hasLungGasProtection(entity);
    }

    public static boolean hasBacteriaProtection(LivingEntity entity) {
        return ArmorUtil.hasBacteriaProtection(entity);
    }

    public static boolean hasBlisteringGasProtection(LivingEntity entity) {
        return ArmorUtil.hasBlisteringGasProtection(entity);
    }

    public static boolean hasLightProtection(LivingEntity entity) {
        return ArmorUtil.hasLightProtection(entity);
    }

    public static boolean hasSandProtection(LivingEntity entity) {
        return ArmorUtil.hasSandProtection(entity);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                       HazardClass hazard) {
        return ArmorUtil.hasProtectionAndDamageFilter(entity, slot, hazard, filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, HazardClass hazard, int filterDamage) {
        return ArmorUtil.hasProtectionAndDamageFilter(entity, hazard, filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                       HazardClass hazard) {
        return ArmorUtil.hasProtectionAndDamageFilter(entity, slot, hazard, filterDamage);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                          HazardClass... hazards) {
        return ArmorUtil.hasAllProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
                                                          HazardClass... hazards) {
        return ArmorUtil.hasAllProtectionAndDamageFilter(entity, filterDamage, hazards);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                          HazardClass... hazards) {
        return ArmorUtil.hasAllProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                          HazardClass... hazards) {
        return ArmorUtil.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
                                                          HazardClass... hazards) {
        return ArmorUtil.hasAnyProtectionAndDamageFilter(entity, filterDamage, hazards);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                          HazardClass... hazards) {
        return ArmorUtil.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasFineParticleProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorUtil.hasFineParticleProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasCoarseParticleProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorUtil.hasCoarseParticleProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasMonoxideGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorUtil.hasMonoxideGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasLungGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorUtil.hasLungGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasBacteriaProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorUtil.hasBacteriaProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasBlisteringGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorUtil.hasBlisteringGasProtectionAndDamageFilter(entity, filterDamage);
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

    public static boolean checkForHazmat(LivingEntity entity) {
        return ArmorUtil.checkForHazmat(entity);
    }

    public static boolean checkForHaz2(LivingEntity entity) {
        return ArmorUtil.checkForHaz2(entity);
    }

    public static boolean checkForAsbestos(LivingEntity entity) {
        return ArmorUtil.checkForAsbestos(entity);
    }

    public static boolean checkForFaraday(Player player) {
        return ArmorUtil.checkForFaraday(player);
    }

    public static boolean checkForDigamma(Player player) {
        return ArmorUtil.checkForDigamma(player);
    }

    public static boolean checkForDigamma2(Player player) {
        return ArmorUtil.checkForDigamma2(player);
    }

    public static boolean checkForFiend(Player player) {
        return ArmorUtil.checkForFiend(player);
    }

    public static boolean checkForFiend2(Player player) {
        return ArmorUtil.checkForFiend2(player);
    }

    public static boolean checkArmor(LivingEntity entity, Item... armor) {
        return ArmorUtil.checkArmor(entity, armor);
    }

    public static boolean checkArmorPiece(LivingEntity entity, Item armor, int legacyArmorSlot) {
        return ArmorUtil.checkArmorPiece(entity, armor, legacyArmorSlot);
    }

    public static boolean checkArmorNull(LivingEntity entity, int legacyArmorSlot) {
        return ArmorUtil.checkArmorNull(entity, legacyArmorSlot);
    }

    public static boolean isFaradayArmor(ItemStack stack) {
        return ArmorUtil.isFaradayArmor(stack);
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

    public static ArmorUtil.WornGasMask getWornGasMask(LivingEntity entity) {
        return ArmorUtil.getWornGasMask(entity);
    }

    public static ItemStack getWornGasMaskFilter(LivingEntity entity) {
        return ArmorUtil.getWornGasMaskFilter(entity);
    }

    public static boolean installWornGasMaskFilter(LivingEntity entity, ItemStack filter) {
        return ArmorUtil.installWornGasMaskFilter(entity, filter);
    }

    public static ArmorUtil.GasMaskFilterInstallResult installWornGasMaskFilter(Player player,
                                                                                ItemStack heldFilter) {
        return ArmorUtil.installWornGasMaskFilter(player, heldFilter);
    }

    public static ItemStack removeWornGasMaskFilter(LivingEntity entity) {
        return ArmorUtil.removeWornGasMaskFilter(entity);
    }

    public static ItemStack getGasMaskFilterRecursively(ItemStack mask, LivingEntity entity) {
        return ArmorUtil.getGasMaskFilterRecursively(mask, entity);
    }

    public static ItemStack getGasMaskFilter(ItemStack mask) {
        return ArmorUtil.getGasMaskFilter(mask);
    }

    public static void installGasMaskFilter(ItemStack mask, ItemStack filter) {
        ArmorUtil.installGasMaskFilter(mask, filter);
    }

    public static void removeFilter(ItemStack mask) {
        ArmorUtil.removeFilter(mask);
    }

    public static void damageGasMaskFilter(LivingEntity entity, int damage) {
        ArmorUtil.damageGasMaskFilter(entity, damage);
    }

    public static void damageGasMaskFilter(ItemStack mask, int damage) {
        ArmorUtil.damageGasMaskFilter(mask, damage);
    }

    public static void addGasMaskTooltip(ItemStack maskStack, @Nullable LivingEntity entity,
                                         List<Component> tooltip, TooltipFlag flag) {
        ArmorUtil.addGasMaskTooltip(maskStack, entity, tooltip, flag);
    }

    public static void addGasMaskBlacklistTooltip(ItemStack maskStack, @Nullable LivingEntity entity,
                                                  List<Component> tooltip) {
        ArmorUtil.addGasMaskBlacklistTooltip(maskStack, entity, tooltip);
    }

    public static boolean isWearingEmptyMask(Player player) {
        return ArmorUtil.isWearingEmptyMask(player);
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

    private static final class LegacyHazardClassMap extends AbstractMap<Item, ArrayList<HazardClass>> {
        @Override
        public ArrayList<HazardClass> get(Object key) {
            if (!(key instanceof Item item)) {
                return null;
            }
            EnumSet<HazardClass> protections = HazmatRegistry.protectionSnapshot().get(item);
            return protections == null ? null : new ArrayList<>(protections);
        }

        @Override
        public boolean containsKey(Object key) {
            return key instanceof Item item && HazmatRegistry.protectionSnapshot().containsKey(item);
        }

        @Override
        public int size() {
            return HazmatRegistry.protectionSnapshot().size();
        }

        @Override
        public ArrayList<HazardClass> put(Item key, ArrayList<HazardClass> value) {
            ArrayList<HazardClass> previous = get(key);
            if (key != null && value != null) {
                HazmatRegistry.registerProtection(key, value.toArray(HazardClass[]::new));
            }
            return previous;
        }

        @Override
        public ArrayList<HazardClass> remove(Object key) {
            if (!(key instanceof Item item)) {
                return null;
            }
            EnumSet<HazardClass> previous = HazmatRegistry.removeProtection(item);
            return previous == null ? null : new ArrayList<>(previous);
        }

        @Override
        public void clear() {
            HazmatRegistry.clearProtections();
        }

        @Override
        public Collection<ArrayList<HazardClass>> values() {
            return HazmatRegistry.protectionSnapshot().values().stream()
                    .map(ArrayList::new)
                    .collect(Collectors.toUnmodifiableList());
        }

        @Override
        public Set<Entry<Item, ArrayList<HazardClass>>> entrySet() {
            return HazmatRegistry.protectionSnapshot().entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), new ArrayList<>(entry.getValue())))
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    private ArmorRegistry() {
    }
}
