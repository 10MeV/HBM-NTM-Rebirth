package com.hbm.ntm.util;

import com.google.gson.Gson;
import com.hbm.ntm.radiation.HazmatResistanceConfig;
import com.hbm.ntm.util.ArmorRegistry.HazardClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.AbstractList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clean-port internal legacy-name facade for the hazmat radiation/protection registry.
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
    public static final List<Tuple.Pair<Item, Double>> external = new ExternalHazmatList();
    public static final Gson gson = com.hbm.ntm.radiation.HazmatRegistry.gson;

    private HazmatRegistry() {
    }

    public static void initDefault() {
        registerDefaults();
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

    public static void registerDefaultProtections() {
        com.hbm.ntm.radiation.HazmatRegistry.registerDefaultProtections();
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
        com.hbm.ntm.radiation.HazmatRegistry.registerProtection(item, ArmorRegistry.modern(protections));
    }

    public static boolean registerProtection(ResourceLocation itemId, HazardClass... protections) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerProtection(itemId, ArmorRegistry.modern(protections));
    }

    public static boolean registerProtection(String itemId, HazardClass... protections) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerProtection(itemId, ArmorRegistry.modern(protections));
    }

    public static void registerExternalProtection(Item item, HazardClass... protections) {
        com.hbm.ntm.radiation.HazmatRegistry.registerExternalProtection(item, ArmorRegistry.modern(protections));
    }

    public static boolean registerExternalProtection(ResourceLocation itemId, HazardClass... protections) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerExternalProtection(itemId,
                ArmorRegistry.modern(protections));
    }

    public static boolean registerExternalProtection(String itemId, HazardClass... protections) {
        return com.hbm.ntm.radiation.HazmatRegistry.registerExternalProtection(itemId,
                ArmorRegistry.modern(protections));
    }

    public static EnumSet<HazardClass> removeProtection(Item item) {
        return legacyNullableSet(com.hbm.ntm.radiation.HazmatRegistry.removeProtection(item));
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
        return legacyNullableSet(com.hbm.ntm.radiation.HazmatRegistry.removeExternalProtection(item));
    }

    public static boolean removeExternalProtection(ResourceLocation itemId) {
        Item item = resolveItem(itemId);
        return item != null && removeExternalProtection(item) != null;
    }

    public static boolean removeExternalProtection(String itemId) {
        Item item = resolveItem(itemId);
        return item != null && removeExternalProtection(item) != null;
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

    public static void replaceHazmats(Map<Item, Double> resistances) {
        com.hbm.ntm.radiation.HazmatRegistry.replaceHazmats(resistances);
    }

    public static void replaceProtections(Map<Item, ? extends Collection<HazardClass>> protections) {
        com.hbm.ntm.radiation.HazmatRegistry.replaceProtections(ArmorRegistry.modernProtectionMap(protections));
    }

    public static void replaceExternalHazmats(Map<Item, Double> resistances) {
        com.hbm.ntm.radiation.HazmatRegistry.replaceExternalHazmats(resistances);
    }

    public static void replaceExternalResistances(Map<Item, Double> resistances) {
        com.hbm.ntm.radiation.HazmatRegistry.replaceExternalResistances(resistances);
    }

    public static void replaceExternalProtections(Map<Item, ? extends Collection<HazardClass>> protections) {
        com.hbm.ntm.radiation.HazmatRegistry.replaceExternalProtections(
                ArmorRegistry.modernProtectionMap(protections));
    }

    public static Map<Item, Double> resistanceSnapshot() {
        return com.hbm.ntm.radiation.HazmatRegistry.resistanceSnapshot();
    }

    public static Map<Item, Double> externalResistanceDefaultsSnapshot() {
        return com.hbm.ntm.radiation.HazmatRegistry.externalResistanceDefaultsSnapshot();
    }

    public static Map<Item, EnumSet<HazardClass>> protectionSnapshot() {
        Map<Item, EnumSet<HazardClass>> snapshot = new IdentityHashMap<>();
        for (Map.Entry<Item, EnumSet<com.hbm.ntm.api.item.HazardClass>> entry :
                com.hbm.ntm.radiation.HazmatRegistry.protectionSnapshot().entrySet()) {
            snapshot.put(entry.getKey(), legacySet(entry.getValue()));
        }
        return Map.copyOf(snapshot);
    }

    public static Map<Item, EnumSet<HazardClass>> externalProtectionDefaultsSnapshot() {
        Map<Item, EnumSet<HazardClass>> snapshot = new IdentityHashMap<>();
        for (Map.Entry<Item, EnumSet<com.hbm.ntm.api.item.HazardClass>> entry :
                com.hbm.ntm.radiation.HazmatRegistry.externalProtectionDefaultsSnapshot().entrySet()) {
            snapshot.put(entry.getKey(), legacySet(entry.getValue()));
        }
        return Map.copyOf(snapshot);
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
        return legacySet(com.hbm.ntm.radiation.HazmatRegistry.getProtection(stack));
    }

    public static Set<HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity) {
        return legacySet(com.hbm.ntm.radiation.HazmatRegistry.getProtectionFromItem(stack, entity));
    }

    public static Set<HazardClass> getProtection(LivingEntity entity, EquipmentSlot slot) {
        return legacySet(com.hbm.ntm.radiation.HazmatRegistry.getProtection(entity, slot));
    }

    public static Set<HazardClass> getProtection(LivingEntity entity, int legacyArmorSlot) {
        EquipmentSlot slot = ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot == null ? Set.of() : getProtection(entity, slot);
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot, HazardClass hazard) {
        return hazard != null && com.hbm.ntm.radiation.HazmatRegistry.hasProtection(entity, slot, hazard.modern());
    }

    public static boolean hasProtection(LivingEntity entity, int legacyArmorSlot, HazardClass hazard) {
        EquipmentSlot slot = ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasProtection(entity, slot, hazard);
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return com.hbm.ntm.radiation.HazmatRegistry.hasAllProtection(entity, slot, ArmorRegistry.modern(hazards));
    }

    public static boolean hasAllProtection(LivingEntity entity, int legacyArmorSlot, HazardClass... hazards) {
        EquipmentSlot slot = ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasAllProtection(entity, slot, hazards);
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return com.hbm.ntm.radiation.HazmatRegistry.hasAnyProtection(entity, slot, ArmorRegistry.modern(hazards));
    }

    public static boolean hasAnyProtection(LivingEntity entity, int legacyArmorSlot, HazardClass... hazards) {
        EquipmentSlot slot = ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
        return slot != null && hasAnyProtection(entity, slot, hazards);
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

    private static EnumSet<HazardClass> legacySet(Set<com.hbm.ntm.api.item.HazardClass> hazards) {
        if (hazards == null || hazards.isEmpty()) {
            return EnumSet.noneOf(HazardClass.class);
        }
        EnumSet<HazardClass> mapped = EnumSet.noneOf(HazardClass.class);
        for (com.hbm.ntm.api.item.HazardClass hazard : hazards) {
            if (hazard != null) {
                mapped.add(HazardClass.valueOf(hazard.name()));
            }
        }
        return mapped;
    }

    private static EnumSet<HazardClass> legacyNullableSet(Set<com.hbm.ntm.api.item.HazardClass> hazards) {
        return hazards == null ? null : legacySet(hazards);
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

    private static final class ExternalHazmatList extends AbstractList<Tuple.Pair<Item, Double>> {
        @Override
        public Tuple.Pair<Item, Double> get(int index) {
            HbmTuple.Pair<Item, Double> entry = com.hbm.ntm.radiation.HazmatRegistry.external.get(index);
            return new Tuple.Pair<>(entry.getKey(), entry.getValue());
        }

        @Override
        public int size() {
            return com.hbm.ntm.radiation.HazmatRegistry.external.size();
        }

        @Override
        public void add(int index, Tuple.Pair<Item, Double> element) {
            if (element == null || element.getKey() == null || element.getValue() == null) {
                return;
            }
            com.hbm.ntm.radiation.HazmatRegistry.external.add(index, element);
        }

        @Override
        public Tuple.Pair<Item, Double> set(int index, Tuple.Pair<Item, Double> element) {
            Tuple.Pair<Item, Double> previous = get(index);
            if (element == null || element.getKey() == null || element.getValue() == null) {
                return previous;
            }
            HbmTuple.Pair<Item, Double> replaced =
                    com.hbm.ntm.radiation.HazmatRegistry.external.set(index, element);
            return new Tuple.Pair<>(replaced.getKey(), replaced.getValue());
        }

        @Override
        public Tuple.Pair<Item, Double> remove(int index) {
            HbmTuple.Pair<Item, Double> previous = com.hbm.ntm.radiation.HazmatRegistry.external.remove(index);
            return new Tuple.Pair<>(previous.getKey(), previous.getValue());
        }

        @Override
        public boolean contains(Object object) {
            return indexOf(object) >= 0;
        }

        @Override
        public int indexOf(Object object) {
            if (!(object instanceof HbmTuple.Pair<?, ?> pair)) {
                return -1;
            }
            for (int index = 0; index < size(); index++) {
                if (matches(get(index), pair)) {
                    return index;
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object object) {
            if (!(object instanceof HbmTuple.Pair<?, ?> pair)) {
                return -1;
            }
            for (int index = size() - 1; index >= 0; index--) {
                if (matches(get(index), pair)) {
                    return index;
                }
            }
            return -1;
        }

        @Override
        public boolean remove(Object object) {
            int index = indexOf(object);
            if (index < 0) {
                return false;
            }
            remove(index);
            return true;
        }

        @Override
        public void clear() {
            com.hbm.ntm.radiation.HazmatRegistry.external.clear();
        }

        private boolean matches(Tuple.Pair<Item, Double> entry, HbmTuple.Pair<?, ?> candidate) {
            return entry.getKey() == candidate.getKey()
                    && candidate.getValue() instanceof Number resistance
                    && Double.compare(entry.getValue(), resistance.doubleValue()) == 0;
        }
    }
}
