package com.hbm.handler;

import com.google.gson.Gson;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.radiation.HazmatResistanceConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.AbstractList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Legacy package facade for the 1.7.10 hazmat radiation-resistance registry.
 */
@Deprecated(forRemoval = false)
public final class HazmatRegistry {
    public static final double HELMET = com.hbm.ntm.radiation.HazmatRegistry.HELMET;
    public static final double CHEST = com.hbm.ntm.radiation.HazmatRegistry.CHEST;
    public static final double LEGS = com.hbm.ntm.radiation.HazmatRegistry.LEGS;
    public static final double BOOTS = com.hbm.ntm.radiation.HazmatRegistry.BOOTS;
    public static double helmet = com.hbm.ntm.radiation.HazmatRegistry.helmet;
    public static double chest = com.hbm.ntm.radiation.HazmatRegistry.chest;
    public static double legs = com.hbm.ntm.radiation.HazmatRegistry.legs;
    public static double boots = com.hbm.ntm.radiation.HazmatRegistry.boots;
    public static final List<com.hbm.util.Tuple.Pair<Item, Double>> external = new LegacyExternalHazmatList();
    public static final Gson gson = com.hbm.ntm.radiation.HazmatRegistry.gson;

    public static void initDefault() {
        syncWeightsToModern();
        com.hbm.ntm.radiation.HazmatRegistry.initDefault();
        syncWeightsFromModern();
    }

    public static HazmatResistanceConfig.LoadReport registerHazmats() {
        syncWeightsToModern();
        HazmatResistanceConfig.LoadReport report = com.hbm.ntm.radiation.HazmatRegistry.registerHazmats();
        syncWeightsFromModern();
        return report;
    }

    public static void registerDefaults() {
        syncWeightsToModern();
        com.hbm.ntm.radiation.HazmatRegistry.registerDefaults();
        syncWeightsFromModern();
    }

    public static void registerHazmat(Item item, double resistance) {
        com.hbm.ntm.radiation.HazmatRegistry.registerHazmat(item, resistance);
    }

    public static boolean registerHazmat(ResourceLocation itemId, double resistance) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerHazmat(itemId, resistance);
    }

    public static boolean registerHazmat(String itemId, double resistance) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerHazmat(itemId, resistance);
    }

    public static void registerExternalHazmat(Item item, double resistance) {
        com.hbm.ntm.radiation.HazmatRegistry.registerExternalHazmat(item, resistance);
    }

    public static boolean registerExternalHazmat(ResourceLocation itemId, double resistance) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerExternalHazmat(itemId, resistance);
    }

    public static boolean registerExternalHazmat(String itemId, double resistance) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerExternalHazmat(itemId, resistance);
    }

    public static Double removeHazmat(Item item) {
        return com.hbm.ntm.radiation.HazmatRegistry.removeHazmat(item);
    }

    public static boolean removeHazmat(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.HazmatRegistry.removeHazmat(itemId);
    }

    public static boolean removeHazmat(String itemId) {
        return com.hbm.ntm.radiation.HazmatRegistry.removeHazmat(itemId);
    }

    public static Double removeExternalHazmat(Item item) {
        return com.hbm.ntm.radiation.HazmatRegistry.removeExternalHazmat(item);
    }

    public static boolean removeExternalHazmat(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.HazmatRegistry.removeExternalHazmat(itemId);
    }

    public static boolean removeExternalHazmat(String itemId) {
        return com.hbm.ntm.radiation.HazmatRegistry.removeExternalHazmat(itemId);
    }

    public static void clearExternalHazmats() {
        com.hbm.ntm.radiation.HazmatRegistry.clearExternalHazmats();
    }

    public static void registerProtection(Item item, HazardClass... protections) {
        com.hbm.ntm.radiation.HazmatRegistry.registerProtection(item, protections);
    }

    public static boolean registerProtection(ResourceLocation itemId, HazardClass... protections) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerProtection(itemId, protections);
    }

    public static boolean registerProtection(String itemId, HazardClass... protections) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerProtection(itemId, protections);
    }

    public static void registerExternalProtection(Item item, HazardClass... protections) {
        com.hbm.ntm.radiation.HazmatRegistry.registerExternalProtection(item, protections);
    }

    public static boolean registerExternalProtection(ResourceLocation itemId, HazardClass... protections) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerExternalProtection(itemId, protections);
    }

    public static boolean registerExternalProtection(String itemId, HazardClass... protections) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerExternalProtection(itemId, protections);
    }

    public static EnumSet<HazardClass> removeProtection(Item item) {
        return com.hbm.ntm.radiation.HazmatRegistry.removeProtection(item);
    }

    public static boolean removeProtection(ResourceLocation itemId) {
        Item item = resolveItem(itemId);
        return item != null && removeProtection(item) != null;
    }

    public static boolean removeProtection(String itemId) {
        Item item = resolveItem(itemId);
        return item != null && removeProtection(item) != null;
    }

    public static EnumSet<HazardClass> removeExternalProtection(Item item) {
        return com.hbm.ntm.radiation.HazmatRegistry.removeExternalProtection(item);
    }

    public static boolean removeExternalProtection(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.HazmatRegistry.removeExternalProtection(itemId);
    }

    public static boolean removeExternalProtection(String itemId) {
        return com.hbm.ntm.radiation.HazmatRegistry.removeExternalProtection(itemId);
    }

    public static void clearExternalProtections() {
        com.hbm.ntm.radiation.HazmatRegistry.clearExternalProtections();
    }

    public static void clear() {
        com.hbm.ntm.radiation.HazmatRegistry.clear();
    }

    public static void clearResistances() {
        com.hbm.ntm.radiation.HazmatRegistry.clearResistances();
    }

    public static void clearProtections() {
        com.hbm.ntm.radiation.HazmatRegistry.clearProtections();
    }

    public static void replaceResistances(Map<Item, Double> resistances) {
        com.hbm.ntm.radiation.HazmatRegistry.replaceResistances(resistances);
    }

    public static Map<Item, Double> resistanceSnapshot() {
        return com.hbm.ntm.radiation.HazmatRegistry.resistanceSnapshot();
    }

    public static Map<Item, EnumSet<HazardClass>> protectionSnapshot() {
        return com.hbm.ntm.radiation.HazmatRegistry.protectionSnapshot();
    }

    public static com.hbm.ntm.radiation.HazmatRegistry.RegistrySnapshot registrySnapshot() {
        return com.hbm.ntm.radiation.HazmatRegistry.registrySnapshot();
    }

    public static double getResistance(ItemStack stack) {
        return com.hbm.ntm.radiation.HazmatRegistry.getResistance(stack);
    }

    public static double getCladding(ItemStack stack) {
        return com.hbm.ntm.radiation.HazmatRegistry.getCladding(stack);
    }

    public static float getResistance(LivingEntity entity) {
        return com.hbm.ntm.radiation.HazmatRegistry.getResistance(entity);
    }

    public static Set<HazardClass> getProtection(ItemStack stack) {
        return com.hbm.ntm.radiation.HazmatRegistry.getProtection(stack);
    }

    public static Set<HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity) {
        return com.hbm.ntm.radiation.HazmatRegistry.getProtectionFromItem(stack, entity);
    }

    public static Set<HazardClass> getProtection(LivingEntity entity, EquipmentSlot slot) {
        return com.hbm.ntm.radiation.HazmatRegistry.getProtection(entity, slot);
    }

    public static Set<HazardClass> getProtection(LivingEntity entity, int legacyArmorSlot) {
        EquipmentSlot slot = com.hbm.ntm.radiation.ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot == null ? Set.of() : getProtection(entity, slot);
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot, HazardClass hazardClass) {
        return com.hbm.ntm.radiation.HazmatRegistry.hasProtection(entity, slot, hazardClass);
    }

    public static boolean hasProtection(LivingEntity entity, int legacyArmorSlot, HazardClass hazardClass) {
        EquipmentSlot slot = com.hbm.ntm.radiation.ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasProtection(entity, slot, hazardClass);
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.HazmatRegistry.hasAllProtection(entity, slot, hazardClasses);
    }

    public static boolean hasAllProtection(LivingEntity entity, int legacyArmorSlot, HazardClass... hazardClasses) {
        EquipmentSlot slot = com.hbm.ntm.radiation.ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasAllProtection(entity, slot, hazardClasses);
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazardClasses) {
        return com.hbm.ntm.radiation.HazmatRegistry.hasAnyProtection(entity, slot, hazardClasses);
    }

    public static boolean hasAnyProtection(LivingEntity entity, int legacyArmorSlot, HazardClass... hazardClasses) {
        EquipmentSlot slot = com.hbm.ntm.radiation.ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasAnyProtection(entity, slot, hazardClasses);
    }

    public static float calculateRadiationMod(LivingEntity entity) {
        return com.hbm.ntm.radiation.HazmatRegistry.calculateRadiationMod(entity);
    }

    public static float calculateRadiationModifier(LivingEntity entity) {
        return com.hbm.ntm.radiation.HazmatRegistry.calculateRadiationModifier(entity);
    }

    public static void registerArmorSet(Item helmetItem, Item chestItem, Item legsItem, Item bootsItem,
                                        double material) {
        syncWeightsToModern();
        com.hbm.ntm.radiation.HazmatRegistry.registerArmorSet(helmetItem, chestItem, legsItem, bootsItem, material);
        syncWeightsFromModern();
    }

    public static int registerArmorSet(ResourceLocation helmetItem, ResourceLocation chestItem,
                                       ResourceLocation legsItem, ResourceLocation bootsItem, double material) {
        syncWeightsToModern();
        int count = com.hbm.ntm.radiation.HazmatRegistry.registerArmorSet(helmetItem, chestItem, legsItem,
                bootsItem, material);
        syncWeightsFromModern();
        return count;
    }

    public static int registerArmorSet(String helmetItem, String chestItem, String legsItem, String bootsItem,
                                       double material) {
        syncWeightsToModern();
        int count = com.hbm.ntm.radiation.HazmatRegistry.registerArmorSet(helmetItem, chestItem, legsItem,
                bootsItem, material);
        syncWeightsFromModern();
        return count;
    }

    public static int registerExternalArmorSet(Item helmetItem, Item chestItem, Item legsItem, Item bootsItem,
                                               double material) {
        syncWeightsToModern();
        int count = com.hbm.ntm.radiation.HazmatRegistry.registerExternalArmorSet(helmetItem, chestItem, legsItem,
                bootsItem, material);
        syncWeightsFromModern();
        return count;
    }

    public static int registerExternalArmorSet(ResourceLocation helmetItem, ResourceLocation chestItem,
                                               ResourceLocation legsItem, ResourceLocation bootsItem,
                                               double material) {
        syncWeightsToModern();
        int count = com.hbm.ntm.radiation.HazmatRegistry.registerExternalArmorSet(helmetItem, chestItem, legsItem,
                bootsItem, material);
        syncWeightsFromModern();
        return count;
    }

    public static int registerExternalArmorSet(String helmetItem, String chestItem, String legsItem, String bootsItem,
                                               double material) {
        syncWeightsToModern();
        int count = com.hbm.ntm.radiation.HazmatRegistry.registerExternalArmorSet(helmetItem, chestItem, legsItem,
                bootsItem, material);
        syncWeightsFromModern();
        return count;
    }

    public static Item resolveItem(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.HazmatRegistry.resolveItem(itemId);
    }

    public static Item resolveItem(String itemId) {
        return com.hbm.ntm.radiation.HazmatRegistry.resolveItem(itemId);
    }

    private static void syncWeightsToModern() {
        com.hbm.ntm.radiation.HazmatRegistry.helmet = helmet;
        com.hbm.ntm.radiation.HazmatRegistry.chest = chest;
        com.hbm.ntm.radiation.HazmatRegistry.legs = legs;
        com.hbm.ntm.radiation.HazmatRegistry.boots = boots;
    }

    private static void syncWeightsFromModern() {
        helmet = com.hbm.ntm.radiation.HazmatRegistry.helmet;
        chest = com.hbm.ntm.radiation.HazmatRegistry.chest;
        legs = com.hbm.ntm.radiation.HazmatRegistry.legs;
        boots = com.hbm.ntm.radiation.HazmatRegistry.boots;
    }

    private static final class LegacyExternalHazmatList extends AbstractList<com.hbm.util.Tuple.Pair<Item, Double>> {
        @Override
        public com.hbm.util.Tuple.Pair<Item, Double> get(int index) {
            com.hbm.ntm.util.HbmTuple.Pair<Item, Double> entry =
                    com.hbm.ntm.radiation.HazmatRegistry.external.get(index);
            return new com.hbm.util.Tuple.Pair<>(entry.getKey(), entry.getValue());
        }

        @Override
        public int size() {
            return com.hbm.ntm.radiation.HazmatRegistry.external.size();
        }

        @Override
        public void add(int index, com.hbm.util.Tuple.Pair<Item, Double> element) {
            com.hbm.ntm.radiation.HazmatRegistry.external.add(index, element);
        }

        @Override
        public com.hbm.util.Tuple.Pair<Item, Double> set(int index,
                com.hbm.util.Tuple.Pair<Item, Double> element) {
            com.hbm.ntm.util.HbmTuple.Pair<Item, Double> previous =
                    com.hbm.ntm.radiation.HazmatRegistry.external.set(index, element);
            return new com.hbm.util.Tuple.Pair<>(previous.getKey(), previous.getValue());
        }

        @Override
        public com.hbm.util.Tuple.Pair<Item, Double> remove(int index) {
            com.hbm.ntm.util.HbmTuple.Pair<Item, Double> previous =
                    com.hbm.ntm.radiation.HazmatRegistry.external.remove(index);
            return new com.hbm.util.Tuple.Pair<>(previous.getKey(), previous.getValue());
        }

        @Override
        public boolean contains(Object object) {
            return com.hbm.ntm.radiation.HazmatRegistry.external.contains(object);
        }

        @Override
        public int indexOf(Object object) {
            return com.hbm.ntm.radiation.HazmatRegistry.external.indexOf(object);
        }

        @Override
        public int lastIndexOf(Object object) {
            return com.hbm.ntm.radiation.HazmatRegistry.external.lastIndexOf(object);
        }

        @Override
        public boolean remove(Object object) {
            return com.hbm.ntm.radiation.HazmatRegistry.external.remove(object);
        }

        @Override
        public void clear() {
            com.hbm.ntm.radiation.HazmatRegistry.external.clear();
        }
    }

    private HazmatRegistry() {
    }
}
