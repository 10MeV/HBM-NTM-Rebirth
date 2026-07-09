package com.hbm.ntm.util;

import com.hbm.ntm.util.ArmorRegistry.HazardClass;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static List<Tuple.Pair<Item, HazardClass[]>> external = new ExternalProtectionList();
    public static HazardClass[] FULL_NO_LIGHT = new HazardClass[] {
            HazardClass.PARTICLE_COARSE,
            HazardClass.PARTICLE_FINE,
            HazardClass.GAS_LUNG,
            HazardClass.BACTERIA,
            HazardClass.GAS_BLISTERING,
            HazardClass.GAS_MONOXIDE,
            HazardClass.SAND
    };
    public static HazardClass[] FULL_PACKAGE = new HazardClass[] {
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
        syncExternalToModern();
        com.hbm.ntm.radiation.ArmorUtil.register();
    }

    public static void registerDefaultProtections() {
        syncExternalToModern();
        com.hbm.ntm.radiation.ArmorUtil.registerDefaultProtections();
    }

    public static void registerProtection(Item item, HazardClass... hazards) {
        ArmorRegistry.registerProtection(item, hazards);
    }

    public static boolean registerProtection(ResourceLocation itemId, HazardClass... hazards) {
        return ArmorRegistry.registerProtection(itemId, hazards);
    }

    public static boolean registerProtection(String itemId, HazardClass... hazards) {
        return ArmorRegistry.registerProtection(itemId, hazards);
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
        return ArmorRegistry.removeProtection(item);
    }

    public static boolean removeProtection(ResourceLocation itemId) {
        return ArmorRegistry.removeProtection(itemId);
    }

    public static boolean removeProtection(String itemId) {
        return ArmorRegistry.removeProtection(itemId);
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
        ArmorRegistry.clearProtections();
    }

    public static void clearExternalProtections() {
        com.hbm.ntm.radiation.ArmorUtil.clearExternalProtections();
    }

    public static void replaceProtections(Map<Item, ? extends Collection<HazardClass>> protections) {
        ArmorRegistry.replaceProtections(protections);
    }

    public static void replaceExternalProtections(Map<Item, ? extends Collection<HazardClass>> protections) {
        com.hbm.ntm.radiation.ArmorUtil.replaceExternalProtections(ArmorRegistry.modernProtectionMap(protections));
    }

    public static ArrayList<HazardClass> getProtection(ItemStack stack) {
        return ArmorRegistry.getProtection(stack);
    }

    public static ArrayList<HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity) {
        return ArmorRegistry.getProtectionFromItem(stack, entity);
    }

    public static Map<Item, EnumSet<HazardClass>> protectionSnapshot() {
        return ArmorRegistry.protectionSnapshot();
    }

    public static Map<Item, EnumSet<HazardClass>> externalProtectionDefaultsSnapshot() {
        Map<Item, EnumSet<HazardClass>> snapshot = new LinkedHashMap<>();
        for (Map.Entry<Item, EnumSet<com.hbm.ntm.api.item.HazardClass>> entry :
                com.hbm.ntm.radiation.ArmorUtil.externalProtectionDefaultsSnapshot().entrySet()) {
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
        if (player instanceof ServerPlayer serverPlayer) {
            ObfuscationReflectionHelper.setPrivateValue(ServerGamePacketListenerImpl.class,
                    serverPlayer.connection, 0, "f_9737_");
        }
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
        return ArmorRegistry.checkForMkuProtection(entity);
    }

    public static boolean hasPollutionPoisonProtection(LivingEntity entity) {
        return ArmorRegistry.hasPollutionPoisonProtection(entity);
    }

    public static boolean hasPollutionLeadProtection(LivingEntity entity) {
        return ArmorRegistry.hasPollutionLeadProtection(entity);
    }

    public static boolean hasSootLungProtection(LivingEntity entity) {
        return ArmorRegistry.hasSootLungProtection(entity);
    }

    public static boolean hasBlindingProtection(LivingEntity entity) {
        return ArmorRegistry.hasBlindingProtection(entity);
    }

    public static boolean hasAshVisionPartialProtection(LivingEntity entity) {
        return ArmorRegistry.hasAshVisionPartialProtection(entity);
    }

    public static boolean checkForAshGlasses(LivingEntity entity) {
        return ArmorRegistry.checkForAshGlasses(entity);
    }

    public static int getAshExposureLimit(LivingEntity entity) {
        return ArmorRegistry.getAshExposureLimit(entity);
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
        return ArmorRegistry.hasProtection(entity, hazard);
    }

    public static boolean hasAnyProtection(LivingEntity entity, HazardClass... hazards) {
        return ArmorRegistry.hasAnyProtection(entity, hazards);
    }

    public static boolean hasAllProtection(LivingEntity entity, HazardClass... hazards) {
        return ArmorRegistry.hasAllProtection(entity, hazards);
    }

    public static boolean hasProtection(LivingEntity entity, int slot, HazardClass hazard) {
        return ArmorRegistry.hasProtection(entity, slot, hazard);
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot, HazardClass hazard) {
        return ArmorRegistry.hasProtection(entity, slot, hazard);
    }

    public static boolean hasAnyProtection(LivingEntity entity, int slot, HazardClass... hazards) {
        return ArmorRegistry.hasAnyProtection(entity, slot, hazards);
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return ArmorRegistry.hasAnyProtection(entity, slot, hazards);
    }

    public static boolean hasAllProtection(LivingEntity entity, int slot, HazardClass... hazards) {
        return ArmorRegistry.hasAllProtection(entity, slot, hazards);
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot, HazardClass... hazards) {
        return ArmorRegistry.hasAllProtection(entity, slot, hazards);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, HazardClass hazard,
            int filterDamage) {
        return ArmorRegistry.hasProtectionAndDamageFilter(entity, hazard, filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, int slot, HazardClass hazard,
            int filterDamage) {
        return ArmorRegistry.hasProtectionAndDamageFilter(entity, slot, hazard, filterDamage);
    }

    public static boolean hasProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, HazardClass hazard,
            int filterDamage) {
        return ArmorRegistry.hasProtectionAndDamageFilter(entity, slot, hazard, filterDamage);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
            HazardClass... hazards) {
        return ArmorRegistry.hasAnyProtectionAndDamageFilter(entity, filterDamage, hazards);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
            HazardClass... hazards) {
        return ArmorRegistry.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasAnyProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
            HazardClass... hazards) {
        return ArmorRegistry.hasAnyProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static EquipmentSlot legacyEquipmentSlot(int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorUtil.legacyEquipmentSlot(legacyArmorSlot);
    }

    public static EquipmentSlot tryLegacyEquipmentSlot(int legacyArmorSlot) {
        return com.hbm.ntm.radiation.ArmorUtil.tryLegacyEquipmentSlot(legacyArmorSlot);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int filterDamage,
            HazardClass... hazards) {
        return ArmorRegistry.hasAllProtectionAndDamageFilter(entity, filterDamage, hazards);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, int slot, int filterDamage,
            HazardClass... hazards) {
        return ArmorRegistry.hasAllProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasAllProtectionAndDamageFilter(LivingEntity entity, EquipmentSlot slot, int filterDamage,
            HazardClass... hazards) {
        return ArmorRegistry.hasAllProtectionAndDamageFilter(entity, slot, filterDamage, hazards);
    }

    public static boolean hasFineParticleProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorRegistry.hasFineParticleProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasCoarseParticleProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorRegistry.hasCoarseParticleProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasMonoxideGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorRegistry.hasMonoxideGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasLungGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorRegistry.hasLungGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasBacteriaProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorRegistry.hasBacteriaProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasBlisteringGasProtectionAndDamageFilter(LivingEntity entity, int filterDamage) {
        return ArmorRegistry.hasBlisteringGasProtectionAndDamageFilter(entity, filterDamage);
    }

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazard,
            boolean requiresFullBodyProtection, boolean apply) {
        return ArmorRegistry.hasToxinProtection(entity, hazard, requiresFullBodyProtection, apply);
    }

    public static boolean hasToxinProtection(LivingEntity entity, HazardClass hazard,
            boolean requiresFullBodyProtection, int filterDamage) {
        return ArmorRegistry.hasToxinProtection(entity, hazard, requiresFullBodyProtection, filterDamage);
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

    private static void syncExternalToModern() {
        if (external instanceof ExternalProtectionList) {
            return;
        }
        List<HbmTuple.Pair<Item, com.hbm.ntm.api.item.HazardClass[]>> converted = new ArrayList<>();
        if (external != null) {
            for (Tuple.Pair<Item, HazardClass[]> entry : external) {
                if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                    converted.add(new LegacyProtectionPairAdapter(entry));
                }
            }
        }
        com.hbm.ntm.radiation.ArmorUtil.external = converted;
    }

    private static final class ExternalProtectionList extends AbstractList<Tuple.Pair<Item, HazardClass[]>> {
        @Override
        public Tuple.Pair<Item, HazardClass[]> get(int index) {
            HbmTuple.Pair<Item, com.hbm.ntm.api.item.HazardClass[]> entry =
                    com.hbm.ntm.radiation.ArmorUtil.external.get(index);
            return legacyPair(entry);
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
                    new LegacyProtectionPairAdapter(element));
        }

        @Override
        public Tuple.Pair<Item, HazardClass[]> set(int index, Tuple.Pair<Item, HazardClass[]> element) {
            Tuple.Pair<Item, HazardClass[]> previous = get(index);
            if (element == null || element.getKey() == null || element.getValue() == null) {
                return previous;
            }
            com.hbm.ntm.radiation.ArmorUtil.external.set(index,
                    new LegacyProtectionPairAdapter(element));
            return previous;
        }

        @Override
        public Tuple.Pair<Item, HazardClass[]> remove(int index) {
            Tuple.Pair<Item, HazardClass[]> previous = get(index);
            com.hbm.ntm.radiation.ArmorUtil.external.remove(index);
            return previous;
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
            com.hbm.ntm.radiation.ArmorUtil.external.clear();
        }

        private boolean matches(Tuple.Pair<Item, HazardClass[]> entry, HbmTuple.Pair<?, ?> candidate) {
            return entry.getKey() == candidate.getKey()
                    && candidate.getValue() instanceof HazardClass[] hazards
                    && Arrays.equals(entry.getValue(), hazards);
        }

        private Tuple.Pair<Item, HazardClass[]> legacyPair(
                HbmTuple.Pair<Item, com.hbm.ntm.api.item.HazardClass[]> entry) {
            if (entry instanceof LegacyProtectionPairAdapter adapter) {
                return adapter.legacyPair();
            }
            return new Tuple.Pair<>(entry.getKey(), ArmorRegistry.legacy(entry.getValue()));
        }
    }

    private static final class LegacyProtectionPairAdapter
            extends HbmTuple.Pair<Item, com.hbm.ntm.api.item.HazardClass[]> {
        private final Tuple.Pair<Item, HazardClass[]> legacy;

        private LegacyProtectionPairAdapter(Tuple.Pair<Item, HazardClass[]> legacy) {
            super(legacy.getKey(), ArmorRegistry.modern(legacy.getValue()));
            this.legacy = legacy;
        }

        @Override
        public Item getKey() {
            return legacy.getKey();
        }

        @Override
        public com.hbm.ntm.api.item.HazardClass[] getValue() {
            return ArmorRegistry.modern(legacy.getValue());
        }

        private Tuple.Pair<Item, HazardClass[]> legacyPair() {
            return legacy;
        }
    }
}
