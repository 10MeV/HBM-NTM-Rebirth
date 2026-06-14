package com.hbm.ntm.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Legacy-name hazard protection facade.
 */
@Deprecated(forRemoval = false)
public final class ArmorRegistry {
    public static final Map<Item, ArrayList<HazardClass>> hazardClasses = new HazardClassMap();
    public static final String FILTER_KEY = ArmorUtil.FILTER_KEY;
    public static final String FILTERK_KEY = ArmorUtil.FILTERK_KEY;
    public static final HazardClass[] FULL_NO_LIGHT = ArmorUtil.FULL_NO_LIGHT;
    public static final HazardClass[] FULL_PACKAGE = ArmorUtil.FULL_PACKAGE;
    public static final int ASH_EXPOSURE_LIMIT_ASH_GLASSES = ArmorUtil.ASH_EXPOSURE_LIMIT_ASH_GLASSES;
    public static final int ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT = ArmorUtil.ASH_EXPOSURE_LIMIT_SAND_OR_LIGHT;
    public static final int ASH_EXPOSURE_LIMIT_UNPROTECTED = ArmorUtil.ASH_EXPOSURE_LIMIT_UNPROTECTED;

    private ArmorRegistry() {
    }

    public static void registerHazard(Item item, HazardClass... hazards) {
        com.hbm.ntm.radiation.ArmorRegistry.registerHazard(item, modern(hazards));
    }

    public static void register() {
        ArmorUtil.register();
    }

    public static void registerDefaultProtections() {
        com.hbm.ntm.radiation.ArmorRegistry.registerDefaultProtections();
    }

    public static void registerProtection(Item item, HazardClass... hazards) {
        registerHazard(item, hazards);
    }

    public static boolean registerHazard(ResourceLocation itemId, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerHazard(itemId, modern(hazards));
    }

    public static boolean registerProtection(ResourceLocation itemId, HazardClass... hazards) {
        return registerHazard(itemId, hazards);
    }

    public static boolean registerHazard(String itemId, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerHazard(itemId, modern(hazards));
    }

    public static boolean registerProtection(String itemId, HazardClass... hazards) {
        return registerHazard(itemId, hazards);
    }

    public static void registerExternalHazard(Item item, HazardClass... hazards) {
        com.hbm.ntm.radiation.ArmorRegistry.registerExternalHazard(item, modern(hazards));
    }

    public static void registerExternalProtection(Item item, HazardClass... hazards) {
        registerExternalHazard(item, hazards);
    }

    public static boolean registerExternalHazard(ResourceLocation itemId, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerExternalHazard(itemId, modern(hazards));
    }

    public static boolean registerExternalProtection(ResourceLocation itemId, HazardClass... hazards) {
        return registerExternalHazard(itemId, hazards);
    }

    public static boolean registerExternalHazard(String itemId, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.registerExternalHazard(itemId, modern(hazards));
    }

    public static boolean registerExternalProtection(String itemId, HazardClass... hazards) {
        return registerExternalHazard(itemId, hazards);
    }

    public static ArrayList<HazardClass> removeHazard(Item item) {
        return legacy(com.hbm.ntm.radiation.ArmorRegistry.removeHazard(item));
    }

    public static ArrayList<HazardClass> removeProtection(Item item) {
        return removeHazard(item);
    }

    public static boolean removeHazard(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeHazard(itemId);
    }

    public static boolean removeProtection(ResourceLocation itemId) {
        return removeHazard(itemId);
    }

    public static boolean removeHazard(String itemId) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeHazard(itemId);
    }

    public static boolean removeProtection(String itemId) {
        return removeHazard(itemId);
    }

    public static ArrayList<HazardClass> removeExternalHazard(Item item) {
        return legacy(com.hbm.ntm.radiation.ArmorRegistry.removeExternalHazard(item));
    }

    public static ArrayList<HazardClass> removeExternalProtection(Item item) {
        return removeExternalHazard(item);
    }

    public static boolean removeExternalHazard(ResourceLocation itemId) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeExternalHazard(itemId);
    }

    public static boolean removeExternalProtection(ResourceLocation itemId) {
        return removeExternalHazard(itemId);
    }

    public static boolean removeExternalHazard(String itemId) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeExternalHazard(itemId);
    }

    public static boolean removeExternalProtection(String itemId) {
        return removeExternalHazard(itemId);
    }

    public static void clearHazards() {
        com.hbm.ntm.radiation.ArmorRegistry.clearHazards();
    }

    public static void clearProtections() {
        clearHazards();
    }

    public static void clearExternalHazards() {
        com.hbm.ntm.radiation.ArmorRegistry.clearExternalHazards();
    }

    public static void clearExternalProtections() {
        clearExternalHazards();
    }

    public static void replaceHazards(Map<Item, ? extends Collection<HazardClass>> hazards) {
        com.hbm.ntm.radiation.ArmorRegistry.replaceHazards(modernProtectionMap(hazards));
    }

    public static void replaceProtections(Map<Item, ? extends Collection<HazardClass>> hazards) {
        replaceHazards(hazards);
    }

    public static void replaceExternalHazards(Map<Item, ? extends Collection<HazardClass>> hazards) {
        com.hbm.ntm.radiation.ArmorRegistry.replaceExternalHazards(modernProtectionMap(hazards));
    }

    public static void replaceExternalProtections(Map<Item, ? extends Collection<HazardClass>> hazards) {
        replaceExternalHazards(hazards);
    }

    public static boolean hasAllProtection(LivingEntity entity, int slot, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtection(entity, slot, modern(hazards));
    }

    public static boolean hasAllProtection(LivingEntity entity, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtection(entity, modern(hazards));
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtection(entity, slot, modern(hazards));
    }

    public static boolean hasAnyProtection(LivingEntity entity, int slot, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtection(entity, slot, modern(hazards));
    }

    public static boolean hasAnyProtection(LivingEntity entity, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtection(entity, modern(hazards));
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtection(entity, slot, modern(hazards));
    }

    public static boolean hasProtection(LivingEntity entity, int slot, HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtection(entity, slot, hazard.modern());
    }

    public static boolean hasProtection(LivingEntity entity, HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtection(entity, hazard.modern());
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot, HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtection(entity, slot, hazard.modern());
    }

    public static ArrayList<HazardClass> getProtection(ItemStack stack) {
        return legacy(com.hbm.ntm.radiation.ArmorRegistry.getProtection(stack));
    }

    public static ArrayList<HazardClass> getProtection(LivingEntity entity, int slot) {
        return legacy(com.hbm.ntm.radiation.ArmorRegistry.getProtection(entity, slot));
    }

    public static ArrayList<HazardClass> getProtection(LivingEntity entity, EquipmentSlot slot) {
        return legacy(com.hbm.ntm.radiation.ArmorRegistry.getProtection(entity, slot));
    }

    public static ArrayList<HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity) {
        return legacy(com.hbm.ntm.radiation.ArmorRegistry.getProtectionFromItem(stack, entity));
    }

    public static Map<Item, EnumSet<HazardClass>> protectionSnapshot() {
        Map<Item, EnumSet<HazardClass>> snapshot = new LinkedHashMap<>();
        for (Map.Entry<Item, EnumSet<com.hbm.ntm.api.item.HazardClass>> entry :
                com.hbm.ntm.radiation.ArmorRegistry.protectionSnapshot().entrySet()) {
            snapshot.put(entry.getKey(), legacySet(entry.getValue()));
        }
        return snapshot;
    }

    public static Map<Item, EnumSet<HazardClass>> externalProtectionDefaultsSnapshot() {
        Map<Item, EnumSet<HazardClass>> snapshot = new LinkedHashMap<>();
        for (Map.Entry<Item, EnumSet<com.hbm.ntm.api.item.HazardClass>> entry :
                com.hbm.ntm.radiation.ArmorRegistry.externalProtectionDefaultsSnapshot().entrySet()) {
            snapshot.put(entry.getKey(), legacySet(entry.getValue()));
        }
        return snapshot;
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

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, HazardClass hazard, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtectionAndDamageFilter(entity, hazard.modern(),
                filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                       HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtectionAndDamageFilter(entity, slot, filterDamage,
                hazard.modern());
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, int slot, HazardClass hazard,
                                                       int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtectionAndDamageFilter(entity, slot, hazard.modern(),
                filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                       HazardClass hazard) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtectionAndDamageFilter(entity, slot, filterDamage,
                hazard.modern());
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, HazardClass hazard,
                                                       int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasProtectionAndDamageFilter(entity, slot, hazard.modern(),
                filterDamage);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
                                                          HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtectionAndDamageFilter(entity, filterDamage,
                modern(hazards));
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                          HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtectionAndDamageFilter(entity, slot, filterDamage,
                modern(hazards));
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                          HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAllProtectionAndDamageFilter(entity, slot, filterDamage,
                modern(hazards));
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
                                                          HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtectionAndDamageFilter(entity, filterDamage,
                modern(hazards));
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
                                                          HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage,
                modern(hazards));
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
                                                          HazardClass... hazards) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage,
                modern(hazards));
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

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazard,
                                             boolean requiresFullBodyProtection, boolean apply) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasToxinProtection(entity,
                hazard == null ? null : hazard.modern(), requiresFullBodyProtection, apply);
    }

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazard,
                                             boolean requiresFullBodyProtection, int filterDamage) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasToxinProtection(entity,
                hazard == null ? null : hazard.modern(), requiresFullBodyProtection, filterDamage);
    }

    public static boolean checkForMkuProtection(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.checkForMkuProtection(entity);
    }

    public static com.hbm.ntm.radiation.ArmorUtil.WornGasMask getWornGasMask(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.getWornGasMask(entity);
    }

    public static boolean hasWornGasMask(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasWornGasMask(entity);
    }

    public static ItemStack getWornGasMaskFilter(LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.getWornGasMaskFilter(entity);
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

    public static ItemStack getGasMaskFilter(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorRegistry.getGasMaskFilter(mask);
    }

    public static boolean hasGasMaskFilter(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorRegistry.hasGasMaskFilter(mask);
    }

    public static ItemStack getGasMaskFilterRecursively(ItemStack mask, LivingEntity entity) {
        return com.hbm.ntm.radiation.ArmorRegistry.getGasMaskFilterRecursively(mask, entity);
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

    public static ItemStack removeGasMaskFilterRecursively(ItemStack mask) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeGasMaskFilterRecursively(mask);
    }

    public static boolean removeGasMaskFilterToInventory(ItemStack mask, Player player) {
        return com.hbm.ntm.radiation.ArmorRegistry.removeGasMaskFilterToInventory(mask, player);
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

    public static EquipmentSlot tryLegacyEquipmentSlot(int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorRegistry.tryLegacyEquipmentSlot(legacyArmorSlot);
    }

    static com.hbm.ntm.api.item.HazardClass[] modern(HazardClass... hazards) {
        if (hazards == null) {
            return new com.hbm.ntm.api.item.HazardClass[0];
        }
        com.hbm.ntm.api.item.HazardClass[] mapped = new com.hbm.ntm.api.item.HazardClass[hazards.length];
        for (int i = 0; i < hazards.length; i++) {
            mapped[i] = hazards[i] == null ? null : hazards[i].modern();
        }
        return mapped;
    }

    static HazardClass[] legacy(com.hbm.ntm.api.item.HazardClass... hazards) {
        if (hazards == null) {
            return new HazardClass[0];
        }
        HazardClass[] mapped = new HazardClass[hazards.length];
        for (int i = 0; i < hazards.length; i++) {
            mapped[i] = hazards[i] == null ? null : HazardClass.valueOf(hazards[i].name());
        }
        return mapped;
    }

    static Map<Item, Collection<com.hbm.ntm.api.item.HazardClass>> modernProtectionMap(
            Map<Item, ? extends Collection<HazardClass>> hazards) {
        Map<Item, Collection<com.hbm.ntm.api.item.HazardClass>> mapped = new LinkedHashMap<>();
        if (hazards == null) {
            return mapped;
        }
        for (Entry<Item, ? extends Collection<HazardClass>> entry : hazards.entrySet()) {
            Collection<HazardClass> value = entry.getValue();
            List<com.hbm.ntm.api.item.HazardClass> converted = new ArrayList<>();
            if (value != null) {
                for (HazardClass hazard : value) {
                    if (hazard != null) {
                        converted.add(hazard.modern());
                    }
                }
            }
            mapped.put(entry.getKey(), converted);
        }
        return mapped;
    }

    static ArrayList<HazardClass> legacy(Collection<com.hbm.ntm.api.item.HazardClass> hazards) {
        ArrayList<HazardClass> mapped = new ArrayList<>();
        if (hazards != null) {
            for (com.hbm.ntm.api.item.HazardClass hazard : hazards) {
                if (hazard != null) {
                    mapped.add(HazardClass.valueOf(hazard.name()));
                }
            }
        }
        return mapped;
    }

    private static EnumSet<HazardClass> legacySet(Collection<com.hbm.ntm.api.item.HazardClass> hazards) {
        EnumSet<HazardClass> mapped = EnumSet.noneOf(HazardClass.class);
        if (hazards != null) {
            for (com.hbm.ntm.api.item.HazardClass hazard : hazards) {
                if (hazard != null) {
                    mapped.add(HazardClass.valueOf(hazard.name()));
                }
            }
        }
        return mapped;
    }

    public enum HazardClass {
        GAS_LUNG("hazard.gasChlorine"),
        GAS_MONOXIDE("hazard.gasMonoxide"),
        GAS_INERT("hazard.gasInert"),
        PARTICLE_COARSE("hazard.particleCoarse"),
        PARTICLE_FINE("hazard.particleFine"),
        BACTERIA("hazard.bacteria"),
        GAS_BLISTERING("hazard.corrosive"),
        SAND("hazard.sand"),
        LIGHT("hazard.light");

        public final String lang;

        HazardClass(String lang) {
            this.lang = lang;
        }

        public com.hbm.ntm.api.item.HazardClass modern() {
            return com.hbm.ntm.api.item.HazardClass.valueOf(name());
        }
    }

    private static final class HazardClassMap extends AbstractMap<Item, ArrayList<HazardClass>> {
        @Override
        public ArrayList<HazardClass> put(Item item, ArrayList<HazardClass> hazards) {
            ArrayList<HazardClass> previous = get(item);
            if (item != null) {
                if (hazards == null) {
                    com.hbm.ntm.radiation.ArmorRegistry.removeHazard(item);
                } else {
                    com.hbm.ntm.radiation.ArmorRegistry.registerHazard(item,
                            modern(hazards.toArray(HazardClass[]::new)));
                }
            }
            return previous;
        }

        @Override
        public ArrayList<HazardClass> get(Object key) {
            if (!(key instanceof Item item)) {
                return null;
            }
            ArrayList<com.hbm.ntm.api.item.HazardClass> modern =
                    com.hbm.ntm.radiation.ArmorRegistry.hazardClasses.get(item);
            return modern == null ? null : new BackedHazardClassList(item, legacy(modern));
        }

        @Override
        public boolean containsKey(Object key) {
            return key instanceof Item item
                    && com.hbm.ntm.radiation.ArmorRegistry.hazardClasses.containsKey(item);
        }

        @Override
        public int size() {
            return com.hbm.ntm.radiation.ArmorRegistry.hazardClasses.size();
        }

        @Override
        public boolean isEmpty() {
            return com.hbm.ntm.radiation.ArmorRegistry.hazardClasses.isEmpty();
        }

        @Override
        public ArrayList<HazardClass> computeIfAbsent(Item item,
                Function<? super Item, ? extends ArrayList<HazardClass>> mappingFunction) {
            ArrayList<HazardClass> existing = get(item);
            if (existing != null || item == null) {
                return existing;
            }
            ArrayList<HazardClass> created = mappingFunction.apply(item);
            if (created == null) {
                return null;
            }
            put(item, created);
            return get(item);
        }

        @Override
        public ArrayList<HazardClass> remove(Object key) {
            if (!(key instanceof Item item)) {
                return null;
            }
            ArrayList<HazardClass> previous = get(item);
            com.hbm.ntm.radiation.ArmorRegistry.removeHazard(item);
            return previous;
        }

        @Override
        public void clear() {
            com.hbm.ntm.radiation.ArmorRegistry.clearHazards();
        }

        @Override
        public boolean containsValue(Object value) {
            if (!(value instanceof Collection<?> collection)) {
                return false;
            }
            for (ArrayList<com.hbm.ntm.api.item.HazardClass> protections :
                    com.hbm.ntm.radiation.ArmorRegistry.hazardClasses.values()) {
                if (legacy(protections).equals(collection)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Collection<ArrayList<HazardClass>> values() {
            return new AbstractCollection<>() {
                @Override
                public Iterator<ArrayList<HazardClass>> iterator() {
                    Iterator<Entry<Item, ArrayList<com.hbm.ntm.api.item.HazardClass>>> iterator =
                            com.hbm.ntm.radiation.ArmorRegistry.hazardClasses.entrySet().iterator();
                    return new Iterator<>() {
                        @Nullable private Item current;

                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public ArrayList<HazardClass> next() {
                            Entry<Item, ArrayList<com.hbm.ntm.api.item.HazardClass>> entry = iterator.next();
                            current = entry.getKey();
                            return new BackedHazardClassList(entry.getKey(), legacy(entry.getValue()));
                        }

                        @Override
                        public void remove() {
                            if (current == null) {
                                throw new IllegalStateException();
                            }
                            iterator.remove();
                            current = null;
                        }
                    };
                }

                @Override
                public int size() {
                    return HazardClassMap.this.size();
                }

                @Override
                public boolean contains(Object object) {
                    return HazardClassMap.this.containsValue(object);
                }

                @Override
                public boolean remove(Object object) {
                    if (!(object instanceof Collection<?> collection)) {
                        return false;
                    }
                    Iterator<Entry<Item, ArrayList<com.hbm.ntm.api.item.HazardClass>>> iterator =
                            com.hbm.ntm.radiation.ArmorRegistry.hazardClasses.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry<Item, ArrayList<com.hbm.ntm.api.item.HazardClass>> entry = iterator.next();
                        if (legacy(entry.getValue()).equals(collection)) {
                            iterator.remove();
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void clear() {
                    HazardClassMap.this.clear();
                }
            };
        }

        @Override
        public Set<Entry<Item, ArrayList<HazardClass>>> entrySet() {
            return new AbstractSet<>() {
                @Override
                public Iterator<Entry<Item, ArrayList<HazardClass>>> iterator() {
                    Iterator<Entry<Item, ArrayList<com.hbm.ntm.api.item.HazardClass>>> iterator =
                            com.hbm.ntm.radiation.ArmorRegistry.hazardClasses.entrySet().iterator();
                    return new Iterator<>() {
                        @Nullable private Item current;

                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public Entry<Item, ArrayList<HazardClass>> next() {
                            Entry<Item, ArrayList<com.hbm.ntm.api.item.HazardClass>> entry = iterator.next();
                            current = entry.getKey();
                            return new BackedHazardClassEntry(entry.getKey());
                        }

                        @Override
                        public void remove() {
                            if (current == null) {
                                throw new IllegalStateException();
                            }
                            iterator.remove();
                            current = null;
                        }
                    };
                }

                @Override
                public int size() {
                    return HazardClassMap.this.size();
                }

                @Override
                public boolean contains(Object object) {
                    if (!(object instanceof Entry<?, ?> entry)) {
                        return false;
                    }
                    ArrayList<HazardClass> value = HazardClassMap.this.get(entry.getKey());
                    return value != null && value.equals(entry.getValue());
                }

                @Override
                public boolean remove(Object object) {
                    if (!contains(object) || !(object instanceof Entry<?, ?> entry)
                            || !(entry.getKey() instanceof Item item)) {
                        return false;
                    }
                    com.hbm.ntm.radiation.ArmorRegistry.removeHazard(item);
                    return true;
                }

                @Override
                public void clear() {
                    HazardClassMap.this.clear();
                }
            };
        }

        @Override
        public void putAll(Map<? extends Item, ? extends ArrayList<HazardClass>> map) {
            for (Entry<? extends Item, ? extends ArrayList<HazardClass>> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue() == null ? null : new ArrayList<>(entry.getValue()));
            }
        }
    }

    private static final class BackedHazardClassEntry implements Entry<Item, ArrayList<HazardClass>> {
        private final Item item;

        private BackedHazardClassEntry(Item item) {
            this.item = item;
        }

        @Override
        public Item getKey() {
            return item;
        }

        @Override
        public ArrayList<HazardClass> getValue() {
            return hazardClasses.get(item);
        }

        @Override
        public ArrayList<HazardClass> setValue(ArrayList<HazardClass> value) {
            ArrayList<HazardClass> previous = getValue();
            if (value == null) {
                com.hbm.ntm.radiation.ArmorRegistry.removeHazard(item);
            } else {
                com.hbm.ntm.radiation.ArmorRegistry.registerHazard(item,
                        modern(value.toArray(HazardClass[]::new)));
            }
            return previous;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Entry<?, ?> entry)) {
                return false;
            }
            return Objects.equals(item, entry.getKey()) && Objects.equals(getValue(), entry.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(item) ^ Objects.hashCode(getValue());
        }
    }

    private static final class BackedHazardClassList extends ArrayList<HazardClass> {
        private final Item item;

        private BackedHazardClassList(Item item, Collection<HazardClass> hazards) {
            super(hazards);
            this.item = item;
        }

        @Override
        public boolean add(HazardClass hazard) {
            boolean changed = super.add(hazard);
            sync();
            return changed;
        }

        @Override
        public void add(int index, HazardClass element) {
            super.add(index, element);
            sync();
        }

        @Override
        public boolean addAll(Collection<? extends HazardClass> collection) {
            boolean changed = super.addAll(collection);
            if (changed) {
                sync();
            }
            return changed;
        }

        @Override
        public boolean addAll(int index, Collection<? extends HazardClass> collection) {
            boolean changed = super.addAll(index, collection);
            if (changed) {
                sync();
            }
            return changed;
        }

        @Override
        public HazardClass set(int index, HazardClass element) {
            HazardClass previous = super.set(index, element);
            sync();
            return previous;
        }

        @Override
        public HazardClass remove(int index) {
            HazardClass previous = super.remove(index);
            sync();
            return previous;
        }

        @Override
        public boolean remove(Object object) {
            boolean changed = super.remove(object);
            if (changed) {
                sync();
            }
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            boolean changed = super.removeAll(collection);
            if (changed) {
                sync();
            }
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            boolean changed = super.retainAll(collection);
            if (changed) {
                sync();
            }
            return changed;
        }

        @Override
        public void clear() {
            if (!isEmpty()) {
                super.clear();
                sync();
            }
        }

        private void sync() {
            com.hbm.ntm.radiation.ArmorRegistry.registerHazard(item, modern(toArray(HazardClass[]::new)));
        }
    }
}
