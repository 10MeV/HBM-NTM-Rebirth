package com.hbm.ntm.util;

import com.hbm.ntm.util.ArmorRegistry.HazardClass;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Legacy-name armor protection facade.
 */
@Deprecated(forRemoval = false)
public final class ArmorUtil {
    public static final List<Tuple.Pair<Item, HazardClass[]>> external = new ExternalProtectionList();
    public static final HazardClass[] FULL_NO_LIGHT = new HazardClass[] {
            HazardClass.PARTICLE_COARSE,
            HazardClass.PARTICLE_FINE,
            HazardClass.GAS_LUNG,
            HazardClass.BACTERIA,
            HazardClass.GAS_BLISTERING,
            HazardClass.GAS_MONOXIDE,
            HazardClass.SAND
    };
    public static final HazardClass[] FULL_PACKAGE = new HazardClass[] {
            HazardClass.PARTICLE_COARSE,
            HazardClass.PARTICLE_FINE,
            HazardClass.GAS_LUNG,
            HazardClass.BACTERIA,
            HazardClass.GAS_BLISTERING,
            HazardClass.GAS_MONOXIDE,
            HazardClass.LIGHT,
            HazardClass.SAND
    };
    public static final String FILTERK_KEY = com.hbm.ntm.radiation.ArmorUtil.FILTERK_KEY;
    public static final String FILTER_KEY = com.hbm.ntm.radiation.ArmorUtil.FILTER_KEY;
    public static final int ASH_EXPOSURE_LIMIT_ASH_GLASSES =
            com.hbm.ntm.radiation.ArmorUtil.ASH_EXPOSURE_LIMIT_ASH_GLASSES;
    public static final int ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT =
            com.hbm.ntm.radiation.ArmorUtil.ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT;
    public static final int ASH_EXPOSURE_LIMIT_UNPROTECTED =
            com.hbm.ntm.radiation.ArmorUtil.ASH_EXPOSURE_LIMIT_UNPROTECTED;
    public static final String[] metals = com.hbm.ntm.radiation.ArmorUtil.metals;

    private ArmorUtil() {
    }

    public static void register() {
        com.hbm.ntm.radiation.HazmatRegistry.registerDefaults();
    }

    public static void registerProtection(Item item, HazardClass... hazards) {
        com.hbm.ntm.radiation.ArmorUtil.registerProtection(item, ArmorRegistry.modern(hazards));
    }

    public static boolean registerProtection(ResourceLocation itemId, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.registerProtection(itemId, ArmorRegistry.modern(hazards));
    }

    public static boolean registerProtection(String itemId, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.registerProtection(itemId, ArmorRegistry.modern(hazards));
    }

    public static void registerExternalProtection(Item item, HazardClass... hazards) {
        com.hbm.ntm.radiation.ArmorUtil.registerExternalProtection(item, ArmorRegistry.modern(hazards));
    }

    public static boolean registerExternalProtection(ResourceLocation itemId, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.registerExternalProtection(itemId, ArmorRegistry.modern(hazards));
    }

    public static boolean registerExternalProtection(String itemId, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.registerExternalProtection(itemId, ArmorRegistry.modern(hazards));
    }

    public static ArrayList<HazardClass> removeProtection(Item item) {
        return ArmorRegistry.legacy(com.hbm.ntm.radiation.ArmorUtil.removeProtection(item));
    }

    public static boolean removeProtection(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.ArmorUtil.removeProtection(itemId);
    }

    public static boolean removeProtection(String itemId) {
        return com.hbm.ntm.radiation.ArmorUtil.removeProtection(itemId);
    }

    public static ArrayList<HazardClass> removeExternalProtection(Item item) {
        return ArmorRegistry.legacy(com.hbm.ntm.radiation.ArmorUtil.removeExternalProtection(item));
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

    public static ArrayList<HazardClass> getProtection(ItemStack stack) {
        return ArmorRegistry.legacy(com.hbm.ntm.radiation.ArmorUtil.getProtection(stack));
    }

    public static ArrayList<HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity) {
        return ArmorRegistry.legacy(com.hbm.ntm.radiation.ArmorUtil.getProtectionFromItem(stack, entity));
    }

    public static Map<Item, EnumSet<HazardClass>> protectionSnapshot() {
        Map<Item, EnumSet<HazardClass>> snapshot = new LinkedHashMap<>();
        for (Map.Entry<Item, EnumSet<com.hbm.ntm.api.item.HazardClass>> entry :
                com.hbm.ntm.radiation.ArmorUtil.protectionSnapshot().entrySet()) {
            snapshot.put(entry.getKey(), legacySet(entry.getValue()));
        }
        return snapshot;
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

    public static void resetFlightTime(Player player) {
        // 1.7.10 reset NetHandlerPlayServer floatingTickCount by reflection; modern handling is deferred.
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

    public static boolean checkForDigamma(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForDigamma(player);
    }

    public static boolean checkForDigamma2(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForDigamma2(player);
    }

    public static boolean checkForFaraday(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForFaraday(player);
    }

    public static boolean checkForFiend(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForFiend(player);
    }

    public static boolean checkForFiend2(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForFiend2(player);
    }

    public static boolean checkForMkuProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.checkForMkuProtection(entity);
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

    public static boolean isFaradayArmor(ItemStack stack) {
        return com.hbm.ntm.radiation.ArmorUtil.isFaradayArmor(stack);
    }

    public static com.hbm.ntm.radiation.ArmorUtil.WornGasMask getWornGasMask(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.getWornGasMask(entity);
    }

    public static boolean hasWornGasMask(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.hasWornGasMask(entity);
    }

    public static ItemStack getWornGasMaskFilter(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.getWornGasMaskFilter(entity);
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

    public static void installGasMaskFilter(ItemStack mask, ItemStack filter) {
        com.hbm.ntm.radiation.ArmorUtil.installGasMaskFilter(mask, filter);
    }

    public static void removeFilter(ItemStack mask) {
        com.hbm.ntm.radiation.ArmorUtil.removeFilter(mask);
    }

    public static ItemStack removeGasMaskFilterRecursively(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorUtil.removeGasMaskFilterRecursively(mask);
    }

    public static boolean removeGasMaskFilterToInventory(ItemStack mask, Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.removeGasMaskFilterToInventory(mask, player);
    }

    public static ItemStack getGasMaskFilterRecursively(ItemStack mask, LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorUtil.getGasMaskFilterRecursively(mask, entity);
    }

    public static ItemStack getGasMaskFilter(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorUtil.getGasMaskFilter(mask);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void addGasMaskTooltip(ItemStack mask, Player player, List list, boolean ext) {
        com.hbm.ntm.radiation.ArmorUtil.addGasMaskTooltip(mask, player, (List<Component>) list,
                TooltipFlag.Default.NORMAL);
    }

    public static void addGasMaskTooltip(ItemStack mask, LivingEntity entity, List<Component> list, TooltipFlag flag) {
        com.hbm.ntm.radiation.ArmorUtil.addGasMaskTooltip(mask, entity, list, flag);
    }

    public static boolean isWearingEmptyMask(Player player) {
        return com.hbm.ntm.radiation.ArmorUtil.isWearingEmptyMask(player);
    }

    public static boolean hasProtection(LivingEntity entity, HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtection(entity, hazard.modern());
    }

    public static boolean hasAnyProtection(LivingEntity entity, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtection(entity, ArmorRegistry.modern(hazards));
    }

    public static boolean hasAllProtection(LivingEntity entity, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtection(entity, ArmorRegistry.modern(hazards));
    }

    public static boolean hasProtection(LivingEntity entity, int slot, HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtection(entity, slot, hazard.modern());
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot, HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtection(entity, slot, hazard.modern());
    }

    public static boolean hasAnyProtection(LivingEntity entity, int slot, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtection(entity, slot, ArmorRegistry.modern(hazards));
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtection(entity, slot, ArmorRegistry.modern(hazards));
    }

    public static boolean hasAllProtection(LivingEntity entity, int slot, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtection(entity, slot, ArmorRegistry.modern(hazards));
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtection(entity, slot, ArmorRegistry.modern(hazards));
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, HazardClass hazard,
            int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtectionAndDamageFilter(entity, hazard.modern(),
                filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, int slot, HazardClass hazard,
            int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtectionAndDamageFilter(entity, slot, hazard.modern(),
                filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, HazardClass hazard,
            int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasProtectionAndDamageFilter(entity, slot, hazard.modern(),
                filterDamage);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
            HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtectionAndDamageFilter(entity, filterDamage,
                ArmorRegistry.modern(hazards));
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
            HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage,
                ArmorRegistry.modern(hazards));
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
            HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage,
                ArmorRegistry.modern(hazards));
    }

    public static EquipmentSlot legacyEquipmentSlot(int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorUtil.legacyEquipmentSlot(legacyArmorSlot);
    }

    public static EquipmentSlot tryLegacyEquipmentSlot(int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
            HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtectionAndDamageFilter(entity, filterDamage,
                ArmorRegistry.modern(hazards));
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
            HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtectionAndDamageFilter(entity, slot, filterDamage,
                ArmorRegistry.modern(hazards));
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
            HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorUtil.hasAllProtectionAndDamageFilter(entity, slot, filterDamage,
                ArmorRegistry.modern(hazards));
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

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazard,
            boolean requiresFullBodyProtection, boolean apply) {
        return com.hbm.ntm.radiation.ArmorUtil.hasToxinProtection(entity,
                hazard == null ? null : hazard.modern(), requiresFullBodyProtection, apply);
    }

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazard,
            boolean requiresFullBodyProtection, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorUtil.hasToxinProtection(entity,
                hazard == null ? null : hazard.modern(), requiresFullBodyProtection, filterDamage);
    }

    private static EnumSet<HazardClass> legacySet(
            Collection<com.hbm.ntm.api.item.HazardClass> hazards) {
        EnumSet<HazardClass> mapped = EnumSet.noneOf(HazardClass.class);
        for (com.hbm.ntm.api.item.HazardClass hazard : hazards) {
            if (hazard != null) {
                mapped.add(HazardClass.valueOf(hazard.name()));
            }
        }
        return mapped;
    }

    private static final class ExternalProtectionList extends AbstractList<Tuple.Pair<Item, HazardClass[]>> {
        @Override
        public Tuple.Pair<Item, HazardClass[]> get(int index) {
            HbmTuple.Pair<Item, com.hbm.ntm.api.item.HazardClass[]> entry =
                    com.hbm.ntm.radiation.ArmorUtil.external.get(index);
            return new Tuple.Pair<>(entry.getKey(), ArmorRegistry.legacy(entry.getValue()));
        }

        @Override
        public int size() {
            return com.hbm.ntm.radiation.ArmorUtil.external.size();
        }

        @Override
        public void add(int index, Tuple.Pair<Item, HazardClass[]> element) {
            if (element == null || element.getKey() == null || element.getValue() == null) {
                return;
            }
            com.hbm.ntm.radiation.ArmorUtil.external.add(index,
                    new HbmTuple.Pair<>(element.getKey(), ArmorRegistry.modern(element.getValue())));
        }

        @Override
        public Tuple.Pair<Item, HazardClass[]> set(int index, Tuple.Pair<Item, HazardClass[]> element) {
            Tuple.Pair<Item, HazardClass[]> previous = get(index);
            if (element == null || element.getKey() == null || element.getValue() == null) {
                return previous;
            }
            com.hbm.ntm.radiation.ArmorUtil.external.set(index,
                    new HbmTuple.Pair<>(element.getKey(), ArmorRegistry.modern(element.getValue())));
            return previous;
        }

        @Override
        public Tuple.Pair<Item, HazardClass[]> remove(int index) {
            Tuple.Pair<Item, HazardClass[]> previous = get(index);
            com.hbm.ntm.radiation.ArmorUtil.external.remove(index);
            return previous;
        }

        @Override
        public void clear() {
            com.hbm.ntm.radiation.ArmorUtil.external.clear();
        }
    }
}
