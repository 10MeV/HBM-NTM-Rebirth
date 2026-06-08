package com.hbm.ntm.radiation;

import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.api.item.GasMask;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmShadyUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class HazmatRegistry {
    public static final double HELMET = 0.2D;
    public static final double CHEST = 0.4D;
    public static final double LEGS = 0.3D;
    public static final double BOOTS = 0.1D;

    private static final Map<Item, Double> RESISTANCE = new IdentityHashMap<>();
    private static final Map<Item, EnumSet<HazardClass>> PROTECTION = new IdentityHashMap<>();
    private static final Map<Item, Double> EXTERNAL_RESISTANCE_DEFAULTS = new IdentityHashMap<>();
    private static final Map<Item, EnumSet<HazardClass>> EXTERNAL_PROTECTION_DEFAULTS = new IdentityHashMap<>();
    private static final int MAX_ARMOR_MOD_PROTECTION_DEPTH = 8;

    public static void registerDefaults() {
        clear();
        replayExternalResistances();

        double iron = 0.0225D;
        double gold = 0.0225D;
        double diamond = 0.07D;
        double netherite = 0.125D;
        double chainmail = 0.0225D;
        double steel = 0.045D;
        double titanium = 0.045D;
        double alloy = 0.07D;
        double cobalt = 0.125D;
        double hazYellow = 0.6D;
        double hazRed = 1.0D;
        double hazGray = 2.0D;
        double paa = 1.7D;
        double liquidator = 2.4D;
        double security = 0.825D;
        double star = 1.0D;
        double cmb = 1.3D;
        double schrab = 3.0D;
        double euph = 10.0D;

        registerArmorSet(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, iron);
        registerArmorSet(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, gold);
        registerArmorSet(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, chainmail);
        registerArmorSet(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, diamond);
        registerArmorSet(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, netherite);

        registerLegacyArmorSet("steel", steel);
        registerLegacyArmorSet("titanium", titanium);
        registerLegacyArmorSet("alloy", alloy);
        registerLegacyArmorSet("cobalt", cobalt);
        registerLegacyArmorSet("hazmat", hazYellow);
        registerLegacyArmorSet("hazmat_helmet_red", "hazmat_plate_red", "hazmat_legs_red", "hazmat_boots_red",
                hazRed);
        registerLegacyArmorSet("hazmat_helmet_grey", "hazmat_plate_grey", "hazmat_legs_grey", "hazmat_boots_grey",
                hazGray);
        registerLegacyArmorSet("hazmat_paa", paa);
        registerLegacyArmorSet("liquidator", liquidator);
        registerLegacyArmorSet("security", security);
        registerLegacyArmorSet("starmetal", star);
        registerLegacyArmorSet("cmb", cmb);
        registerLegacyArmorSet("schrabidium", schrab);
        registerLegacyArmorSet("euphemium", euph);
        registerLegacyPiece("paa_plate", paa * CHEST);
        registerLegacyPiece("paa_legs", paa * LEGS);
        registerLegacyPiece("paa_boots", paa * BOOTS);
        registerLegacyPiece("jackt", 0.1D);
        registerLegacyPiece("jackt2", 0.1D);
        registerLegacyPiece("gas_mask", 0.07D);
        registerLegacyPiece("gas_mask_m65", 0.095D);

        registerLegacyFsbArmorSet("t51", 1.0D, ArmorUtil.FULL_NO_LIGHT);
        registerLegacyFsbArmorSet("steamsuit", 1.3D, ArmorUtil.FULL_PACKAGE);
        registerLegacyFsbArmorSet("ajr", 1.3D, ArmorUtil.FULL_PACKAGE);
        registerLegacyFsbArmorSet("ajro", 1.3D, ArmorUtil.FULL_PACKAGE);
        registerLegacyFsbArmorSet("rpa", 2.0D, ArmorUtil.FULL_PACKAGE);
        registerLegacyFsbArmorSet("ncrpa", 1.7D, ArmorUtil.FULL_PACKAGE);
        registerLegacyFsbArmorSet("bj", 1.0D);
        registerLegacyFsbArmorSet("envsuit", 1.0D, ArmorUtil.FULL_PACKAGE);
        registerLegacyFsbArmorSet("hev", 2.3D, ArmorUtil.FULL_PACKAGE);
        registerLegacyFsbArmorSet("fau", 4.0D, ArmorUtil.FULL_PACKAGE);
        registerLegacyFsbArmorSet("dns", 5.0D, ArmorUtil.FULL_PACKAGE);
        registerLegacyFsbArmorSet("taurun", 0.125D, ArmorUtil.FULL_PACKAGE);
        registerLegacyFsbArmorSet("trenchmaster", 1.0D, ArmorUtil.FULL_PACKAGE);

        registerDefaultProtection();
        replayExternalProtections();
    }

    public static void registerHazmat(Item item, double resistance) {
        if (item == null || item == Items.AIR) {
            return;
        }
        RESISTANCE.put(item, Math.max(0.0D, resistance));
        RadiationShieldingRegistry.register(item, (float) Math.max(0.0D, resistance));
    }

    public static boolean registerHazmat(ResourceLocation itemId, double resistance) {
        Item item = resolveItem(itemId);
        if (item == null) {
            return false;
        }
        registerHazmat(item, resistance);
        return true;
    }

    public static boolean registerHazmat(String itemId, double resistance) {
        Item item = resolveItem(itemId);
        if (item == null) {
            return false;
        }
        registerHazmat(item, resistance);
        return true;
    }

    public static void registerExternalHazmat(Item item, double resistance) {
        if (item == null || item == Items.AIR) {
            return;
        }
        EXTERNAL_RESISTANCE_DEFAULTS.put(item, Math.max(0.0D, resistance));
        registerHazmat(item, resistance);
    }

    public static boolean registerExternalHazmat(ResourceLocation itemId, double resistance) {
        Item item = resolveItem(itemId);
        if (item == null) {
            return false;
        }
        registerExternalHazmat(item, resistance);
        return true;
    }

    public static boolean registerExternalHazmat(String itemId, double resistance) {
        Item item = resolveItem(itemId);
        if (item == null) {
            return false;
        }
        registerExternalHazmat(item, resistance);
        return true;
    }

    public static void registerExternalProtection(Item item, HazardClass... protections) {
        if (item == null || item == Items.AIR) {
            return;
        }
        EXTERNAL_PROTECTION_DEFAULTS.put(item, protectionSet(protections));
        registerProtection(item, protections);
    }

    public static boolean registerExternalProtection(ResourceLocation itemId, HazardClass... protections) {
        Item item = resolveItem(itemId);
        if (item == null) {
            return false;
        }
        registerExternalProtection(item, protections);
        return true;
    }

    public static boolean registerExternalProtection(String itemId, HazardClass... protections) {
        Item item = resolveItem(itemId);
        if (item == null) {
            return false;
        }
        registerExternalProtection(item, protections);
        return true;
    }

    public static void clear() {
        RESISTANCE.clear();
        PROTECTION.clear();
        RadiationShieldingRegistry.clear();
    }

    public static void clearResistances() {
        RESISTANCE.clear();
        RadiationShieldingRegistry.clear();
    }

    public static void replaceResistances(Map<Item, Double> resistances) {
        clearResistances();
        for (Map.Entry<Item, Double> entry : resistances.entrySet()) {
            registerHazmat(entry.getKey(), entry.getValue());
        }
    }

    public static Map<Item, Double> resistanceSnapshot() {
        return Map.copyOf(RESISTANCE);
    }

    public static Map<Item, EnumSet<HazardClass>> protectionSnapshot() {
        Map<Item, EnumSet<HazardClass>> snapshot = new IdentityHashMap<>();
        for (Map.Entry<Item, EnumSet<HazardClass>> entry : PROTECTION.entrySet()) {
            snapshot.put(entry.getKey(), EnumSet.copyOf(entry.getValue()));
        }
        return Map.copyOf(snapshot);
    }

    public static RegistrySnapshot registrySnapshot() {
        return new RegistrySnapshot(RESISTANCE.size(), PROTECTION.size(),
                EXTERNAL_RESISTANCE_DEFAULTS.size(), EXTERNAL_PROTECTION_DEFAULTS.size());
    }

    public static void registerProtection(Item item, HazardClass... protections) {
        if (item == null || item == Items.AIR) {
            return;
        }
        PROTECTION.put(item, protectionSet(protections));
    }

    public static boolean registerProtection(ResourceLocation itemId, HazardClass... protections) {
        Item item = resolveItem(itemId);
        if (item == null) {
            return false;
        }
        registerProtection(item, protections);
        return true;
    }

    public static boolean registerProtection(String itemId, HazardClass... protections) {
        Item item = resolveItem(itemId);
        if (item == null) {
            return false;
        }
        registerProtection(item, protections);
        return true;
    }

    public static double getResistance(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0D;
        }
        return RESISTANCE.getOrDefault(stack.getItem(), 0.0D) + getCladding(stack);
    }

    public static double getCladding(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0.0D;
        }
        double cladding = 0.0D;
        if (stack.hasTag()) {
            cladding = stack.getTag().getFloat("hfr_cladding");
            if (cladding == 0.0D) {
                cladding = stack.getTag().getFloat("ntm_cladding");
            }
            if (cladding == 0.0D) {
                cladding = stack.getTag().getFloat("cladding");
            }
        }
        if (cladding == 0.0D && ArmorModHandler.hasMods(stack)) {
            ItemStack claddingMod = ArmorModHandler.pryMod(stack, ArmorModHandler.cladding);
            if (claddingMod.getItem() instanceof ArmorModItems.Cladding claddingItem) {
                cladding = claddingItem.radiationResistance();
            }
        }
        return cladding;
    }

    public static float getResistance(LivingEntity entity) {
        float resistance = 0.0F;
        if (entity instanceof Player player
                && HbmShadyUtil.PU_238.equals(player.getUUID().toString())) {
            resistance += 0.4F;
        }
        for (ItemStack stack : entity.getArmorSlots()) {
            resistance += (float) getResistance(stack);
        }
        if (entity.hasEffect(ModEffects.RADX.get())) {
            resistance += 0.2F;
        }
        return resistance;
    }

    public static Set<HazardClass> getProtection(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return EnumSet.noneOf(HazardClass.class);
        }
        EnumSet<HazardClass> protections = PROTECTION.get(stack.getItem());
        if (protections == null) {
            return EnumSet.noneOf(HazardClass.class);
        }
        return EnumSet.copyOf(protections);
    }

    public static Set<HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity) {
        return getProtectionFromItem(stack, entity, 0);
    }

    public static Set<HazardClass> getProtection(LivingEntity entity, EquipmentSlot slot) {
        if (entity == null || slot == null) {
            return EnumSet.noneOf(HazardClass.class);
        }
        return getProtectionFromItem(entity.getItemBySlot(slot), entity);
    }

    public static boolean hasProtection(LivingEntity entity, EquipmentSlot slot,
                                        HazardClass hazardClass) {
        return getProtection(entity, slot).contains(hazardClass);
    }

    public static boolean hasAllProtection(LivingEntity entity, EquipmentSlot slot,
                                           HazardClass... hazardClasses) {
        if (entity == null || slot == null || entity.getItemBySlot(slot).isEmpty()) {
            return false;
        }
        Set<HazardClass> protections = getProtection(entity, slot);
        for (HazardClass hazardClass : hazardClasses) {
            if (!protections.contains(hazardClass)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasAnyProtection(LivingEntity entity, EquipmentSlot slot,
                                           HazardClass... hazardClasses) {
        Set<HazardClass> protections = getProtection(entity, slot);
        for (HazardClass hazardClass : hazardClasses) {
            if (protections.contains(hazardClass)) {
                return true;
            }
        }
        return false;
    }

    private static Set<HazardClass> getProtectionFromItem(ItemStack stack, LivingEntity entity, int depth) {
        Set<HazardClass> protections = getProtection(stack);
        if (stack == null || stack.isEmpty()) {
            return protections;
        }
        if (stack.getItem() instanceof GasMask mask) {
            ItemStack filter = mask.getFilter(stack, entity);
            if (filter != null && !filter.isEmpty() && mask.isFilterApplicable(stack, entity, filter)) {
                Set<HazardClass> filterProtection = getProtection(filter);
                for (HazardClass blacklisted : mask.getBlacklist(stack, entity)) {
                    filterProtection.remove(blacklisted);
                }
                protections.addAll(filterProtection);
            }
        }
        if (depth < MAX_ARMOR_MOD_PROTECTION_DEPTH && ArmorModHandler.hasMods(stack)) {
            for (ItemStack mod : ArmorModHandler.pryMods(stack)) {
                if (!mod.isEmpty()) {
                    protections.addAll(getProtectionFromItem(mod, entity, depth + 1));
                }
            }
        }
        return protections;
    }

    public static float calculateRadiationModifier(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return 1.0F;
        }
        if (player.isCreative()) {
            return 0.0F;
        }
        return (float) Math.pow(10.0F, -getResistance(entity));
    }

    public static void registerArmorSet(Item helmet, Item chest, Item legs, Item boots, double material) {
        registerHazmat(helmet, material * HELMET);
        registerHazmat(chest, material * CHEST);
        registerHazmat(legs, material * LEGS);
        registerHazmat(boots, material * BOOTS);
    }

    public static int registerArmorSet(ResourceLocation helmet, ResourceLocation chest, ResourceLocation legs,
                                       ResourceLocation boots, double material) {
        return registerArmorSetCount(resolveItem(helmet), resolveItem(chest), resolveItem(legs), resolveItem(boots),
                material);
    }

    public static int registerArmorSet(String helmet, String chest, String legs, String boots, double material) {
        return registerArmorSetCount(resolveItem(helmet), resolveItem(chest), resolveItem(legs), resolveItem(boots),
                material);
    }

    private static int registerArmorSetCount(Item helmet, Item chest, Item legs, Item boots, double material) {
        int count = 0;
        if (helmet != null && helmet != Items.AIR) {
            registerHazmat(helmet, material * HELMET);
            count++;
        }
        if (chest != null && chest != Items.AIR) {
            registerHazmat(chest, material * CHEST);
            count++;
        }
        if (legs != null && legs != Items.AIR) {
            registerHazmat(legs, material * LEGS);
            count++;
        }
        if (boots != null && boots != Items.AIR) {
            registerHazmat(boots, material * BOOTS);
            count++;
        }
        return count;
    }

    public static int registerExternalArmorSet(Item helmet, Item chest, Item legs, Item boots, double material) {
        int count = 0;
        if (helmet != null && helmet != Items.AIR) {
            registerExternalHazmat(helmet, material * HELMET);
            count++;
        }
        if (chest != null && chest != Items.AIR) {
            registerExternalHazmat(chest, material * CHEST);
            count++;
        }
        if (legs != null && legs != Items.AIR) {
            registerExternalHazmat(legs, material * LEGS);
            count++;
        }
        if (boots != null && boots != Items.AIR) {
            registerExternalHazmat(boots, material * BOOTS);
            count++;
        }
        return count;
    }

    public static int registerExternalArmorSet(ResourceLocation helmet, ResourceLocation chest, ResourceLocation legs,
                                               ResourceLocation boots, double material) {
        return registerExternalArmorSet(resolveItem(helmet), resolveItem(chest), resolveItem(legs), resolveItem(boots),
                material);
    }

    public static int registerExternalArmorSet(String helmet, String chest, String legs, String boots,
                                               double material) {
        return registerExternalArmorSet(resolveItem(helmet), resolveItem(chest), resolveItem(legs), resolveItem(boots),
                material);
    }

    private static void registerLegacyArmorSet(String prefix, double material) {
        registerLegacyPiece(prefix + "_helmet", material * HELMET);
        registerLegacyPiece(prefix + "_plate", material * CHEST);
        registerLegacyPiece(prefix + "_legs", material * LEGS);
        registerLegacyPiece(prefix + "_boots", material * BOOTS);
    }

    private static void registerLegacyArmorSet(String helmet, String chest, String legs, String boots,
                                               double material) {
        registerLegacyPiece(helmet, material * HELMET);
        registerLegacyPiece(chest, material * CHEST);
        registerLegacyPiece(legs, material * LEGS);
        registerLegacyPiece(boots, material * BOOTS);
    }

    private static void registerLegacyFsbArmorSet(String prefix, double material, HazardClass... helmetProtections) {
        registerLegacyArmorSet(prefix, material);
        if (helmetProtections.length > 0) {
            registerLegacyProtection(prefix + "_helmet", helmetProtections);
        }
    }

    private static void registerLegacyPiece(String name, double resistance) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        if (item != null) {
            registerHazmat(item.get(), resistance);
        }
    }

    private static void registerDefaultProtection() {
        registerLegacyProtection("gas_mask_filter",
                HazardClass.PARTICLE_COARSE,
                HazardClass.PARTICLE_FINE,
                HazardClass.GAS_LUNG,
                HazardClass.GAS_BLISTERING,
                HazardClass.BACTERIA);
        registerLegacyProtection("gas_mask_filter_mono",
                HazardClass.PARTICLE_COARSE,
                HazardClass.GAS_MONOXIDE);
        registerLegacyProtection("gas_mask_filter_combo",
                HazardClass.PARTICLE_COARSE,
                HazardClass.PARTICLE_FINE,
                HazardClass.GAS_LUNG,
                HazardClass.GAS_BLISTERING,
                HazardClass.BACTERIA,
                HazardClass.GAS_MONOXIDE);
        registerLegacyProtection("gas_mask_filter_rag", HazardClass.PARTICLE_COARSE);
        registerLegacyProtection("gas_mask_filter_piss", HazardClass.PARTICLE_COARSE, HazardClass.GAS_LUNG);

        registerLegacyProtection("gas_mask", HazardClass.SAND, HazardClass.LIGHT);
        registerLegacyProtection("gas_mask_m65", HazardClass.SAND);
        registerLegacyProtection("mask_rag", HazardClass.PARTICLE_COARSE);
        registerLegacyProtection("mask_piss", HazardClass.PARTICLE_COARSE, HazardClass.GAS_LUNG);
        registerLegacyProtection("goggles", HazardClass.LIGHT, HazardClass.SAND);
        registerLegacyProtection("ashglasses", HazardClass.LIGHT, HazardClass.SAND);
        registerLegacyProtection("attachment_mask", HazardClass.SAND);
        registerLegacyProtection("asbestos_helmet", HazardClass.SAND, HazardClass.LIGHT);
        registerLegacyProtection("hazmat_helmet", HazardClass.SAND);
        registerLegacyProtection("hazmat_helmet_red", HazardClass.SAND);
        registerLegacyProtection("hazmat_helmet_grey", HazardClass.SAND);
        registerLegacyProtection("hazmat_paa_helmet", HazardClass.LIGHT, HazardClass.SAND);
        registerLegacyProtection("liquidator_helmet", HazardClass.LIGHT, HazardClass.SAND);
        registerLegacyProtection("schrabidium_helmet",
                HazardClass.PARTICLE_COARSE,
                HazardClass.PARTICLE_FINE,
                HazardClass.GAS_LUNG,
                HazardClass.BACTERIA,
                HazardClass.GAS_BLISTERING,
                HazardClass.GAS_MONOXIDE,
                HazardClass.LIGHT,
                HazardClass.SAND);
        registerLegacyProtection("euphemium_helmet",
                HazardClass.PARTICLE_COARSE,
                HazardClass.PARTICLE_FINE,
                HazardClass.GAS_LUNG,
                HazardClass.BACTERIA,
                HazardClass.GAS_BLISTERING,
                HazardClass.GAS_MONOXIDE,
                HazardClass.LIGHT,
                HazardClass.SAND);
    }

    private static void registerLegacyProtection(String name, HazardClass... protections) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        if (item != null) {
            registerProtection(item.get(), protections);
        }
    }

    private static EnumSet<HazardClass> protectionSet(HazardClass... protections) {
        EnumSet<HazardClass> registered = EnumSet.noneOf(HazardClass.class);
        for (HazardClass protection : protections) {
            if (protection != null) {
                registered.add(protection);
            }
        }
        return registered;
    }

    public static Item resolveItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        RegistryObject<Item> legacy = ModItems.legacyItem(itemId);
        if (legacy != null && legacy.isPresent()) {
            return legacy.get();
        }
        ResourceLocation parsed = ResourceLocation.tryParse(itemId);
        if (parsed == null) {
            return null;
        }
        Item direct = resolveItem(parsed);
        if (direct != null) {
            return direct;
        }
        if ("hbm".equals(parsed.getNamespace())) {
            legacy = ModItems.legacyItem(parsed.getPath());
            if (legacy != null && legacy.isPresent()) {
                return legacy.get();
            }
        }
        return null;
    }

    public static Item resolveItem(ResourceLocation itemId) {
        if (itemId == null) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        return item == null || item == Items.AIR ? null : item;
    }

    private static void replayExternalResistances() {
        for (Map.Entry<Item, Double> entry : EXTERNAL_RESISTANCE_DEFAULTS.entrySet()) {
            registerHazmat(entry.getKey(), entry.getValue());
        }
    }

    private static void replayExternalProtections() {
        for (Map.Entry<Item, EnumSet<HazardClass>> entry : EXTERNAL_PROTECTION_DEFAULTS.entrySet()) {
            registerProtection(entry.getKey(), entry.getValue().toArray(HazardClass[]::new));
        }
    }

    private HazmatRegistry() {
    }

    public record RegistrySnapshot(int resistanceEntries, int protectionEntries, int externalResistanceDefaults,
                                   int externalProtectionDefaults) {
    }
}
