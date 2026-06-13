package com.hbm.ntm.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.damage.DamageResistanceConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Legacy-name damage resistance facade.
 */
@Deprecated(forRemoval = false)
public class DamageResistanceHandler {
    public static float currentPDT = 0.0F;
    public static float currentPDR = 0.0F;

    public static final String CATEGORY_EXPLOSION = com.hbm.ntm.damage.DamageResistanceHandler.CATEGORY_EXPLOSION;
    public static final String CATEGORY_FIRE = com.hbm.ntm.damage.DamageResistanceHandler.CATEGORY_FIRE;
    public static final String CATEGORY_PHYSICAL = com.hbm.ntm.damage.DamageResistanceHandler.CATEGORY_PHYSICAL;
    public static final String CATEGORY_ENERGY = com.hbm.ntm.damage.DamageResistanceHandler.CATEGORY_ENERGY;

    public static final HashMap<Item, ResistanceStats> itemStats = new ItemStatsMap();
    public static final HashMap<Tuple.Quartet<Item, Item, Item, Item>, ResistanceStats> setStats = new SetStatsMap();
    public static final HashMap<Class<? extends Entity>, ResistanceStats> entityStats = new EntityStatsMap();
    public static final HashMap<Item, List<Tuple.Quartet<Item, Item, Item, Item>>> itemInfoSet = new HashMap<>();

    protected DamageResistanceHandler() {
    }

    public static DamageResistanceConfig.LoadReport init() {
        return DamageResistanceConfig.initialize(FMLPaths.CONFIGDIR.get());
    }

    public static void clearSystem() {
        itemStats.clear();
        setStats.clear();
        entityStats.clear();
        itemInfoSet.clear();
        com.hbm.ntm.damage.DamageResistanceHandler.clear();
    }

    public static void registerItem(Item item, ResistanceStats stats) {
        if (item != null && stats != null) {
            itemStats.put(item, stats);
        }
    }

    public static void registerSet(Item helmet, Item plate, Item legs, Item boots, ResistanceStats stats) {
        if (stats == null) {
            return;
        }
        setStats.put(new Tuple.Quartet<>(helmet, plate, legs, boots), stats);
    }

    public static void registerEntity(Class<? extends Entity> entityClass, ResistanceStats stats) {
        if (entityClass != null && stats != null) {
            entityStats.put(entityClass, stats);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void addToListInHashMap(Object key, HashMap map, Object listElement) {
        List list = (List) map.get(key);
        if (list == null) {
            list = new ArrayList();
            map.put(key, list);
        }
        list.add(listElement);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void addInfo(ItemStack stack, List desc) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        com.hbm.ntm.damage.DamageResistanceTooltipUtil.addResistanceInformation(stack, desc);
    }

    public static void setup(float dt, float dr) {
        currentPDT = dt;
        currentPDR = dr;
        com.hbm.ntm.damage.DamageResistanceHandler.setup(dt, dr);
    }

    public static void reset() {
        currentPDT = 0.0F;
        currentPDR = 0.0F;
        com.hbm.ntm.damage.DamageResistanceHandler.reset();
    }

    public static float currentPierceDt() {
        return com.hbm.ntm.damage.DamageResistanceHandler.currentPierceDt();
    }

    public static float currentPierceDr() {
        return com.hbm.ntm.damage.DamageResistanceHandler.currentPierceDr();
    }

    public static String typeToCategory(DamageSource source) {
        return com.hbm.ntm.damage.DamageResistanceHandler.typeToCategory(source);
    }

    public static String typeToCategory(ResourceKey<DamageType> type) {
        return com.hbm.ntm.damage.DamageResistanceHandler.typeToCategory(type);
    }

    public static String typeToCategory(String legacyTypeOrId) {
        return com.hbm.ntm.damage.DamageResistanceHandler.typeToCategory(legacyTypeOrId);
    }

    public static String typeToCategory(DamageClass damageClass) {
        return com.hbm.ntm.damage.DamageResistanceHandler.typeToCategory(damageClass.modern());
    }

    public static String categoryKey(String category) {
        return com.hbm.ntm.damage.DamageResistanceHandler.categoryKey(category);
    }

    public static String categoryKey(DamageClass category) {
        return com.hbm.ntm.damage.DamageResistanceHandler.categoryKey(category.modern());
    }

    public static String exactTypeKey(DamageSource source) {
        return com.hbm.ntm.damage.DamageResistanceHandler.exactTypeKey(source);
    }

    public static String exactTypeKey(String type) {
        return com.hbm.ntm.damage.DamageResistanceHandler.exactTypeKey(type);
    }

    public static String exactTypeKey(ResourceKey<DamageType> type) {
        return com.hbm.ntm.damage.DamageResistanceHandler.exactTypeKey(type);
    }

    public static String exactTypeKey(DamageClass damageClass) {
        return com.hbm.ntm.damage.DamageResistanceHandler.exactTypeKey(damageClass.modern());
    }

    public static boolean isAbsolute(DamageSource source) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isAbsolute(source);
    }

    public static boolean isAbsolute(ResourceKey<DamageType> type) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isAbsolute(type);
    }

    public static boolean isAbsolute(String legacyTypeOrId) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isAbsolute(legacyTypeOrId);
    }

    public static boolean isAbsolute(DamageClass damageClass) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isAbsolute(damageClass.modern());
    }

    public static boolean isUnblockableForLegacyResistance(DamageSource source) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isUnblockableForLegacyResistance(source);
    }

    public static boolean isUnblockableForLegacyResistance(ResourceKey<DamageType> type) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isUnblockableForLegacyResistance(type);
    }

    public static boolean isUnblockableForLegacyResistance(String legacyTypeOrId) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isUnblockableForLegacyResistance(legacyTypeOrId);
    }

    public static boolean isUnblockableForLegacyResistance(DamageClass damageClass) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isUnblockableForLegacyResistance(damageClass.modern());
    }

    public static float calculateDamage(LivingEntity entity, DamageSource damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.calculateDamage(entity, damage, amount);
    }

    public static float calculateDamage(LivingEntity entity, ResourceKey<DamageType> damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.calculateDamage(entity, damage, amount);
    }

    public static float calculateDamage(LivingEntity entity, DamageClass damageClass, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.calculateDamage(entity, damageClass.modern(), amount);
    }

    public static float calculateDamage(LivingEntity entity, String legacyTypeOrId, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.calculateDamage(entity, legacyTypeOrId, amount);
    }

    public static float calculateDamage(LivingEntity entity, DamageSource damage, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.calculateDamage(entity, damage, amount, pierceDT, pierce);
    }

    public static float calculateDamage(LivingEntity entity, ResourceKey<DamageType> damage, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.calculateDamage(entity, damage, amount, pierceDT, pierce);
    }

    public static float calculateDamage(LivingEntity entity, DamageClass damageClass, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.calculateDamage(entity, damageClass.modern(), amount,
                pierceDT, pierce);
    }

    public static float calculateDamage(LivingEntity entity, String legacyTypeOrId, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.calculateDamage(entity, legacyTypeOrId, amount, pierceDT, pierce);
    }

    public static float applyLegacyPreResistanceDamageModifiers(LivingEntity entity, DamageSource damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.applyLegacyPreResistanceDamageModifiers(entity, damage,
                amount);
    }

    public static float applyLegacyPreResistanceDamageModifiers(LivingEntity entity, ResourceKey<DamageType> damage,
            float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.applyLegacyPreResistanceDamageModifiers(entity, damage,
                amount);
    }

    public static float applyLegacyPreResistanceDamageModifiers(LivingEntity entity, DamageClass damageClass,
            float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.applyLegacyPreResistanceDamageModifiers(entity,
                damageClass.modern(), amount);
    }

    public static float applyLegacyPreResistanceDamageModifiers(LivingEntity entity, String legacyTypeOrId,
            float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.applyLegacyPreResistanceDamageModifiers(entity,
                legacyTypeOrId, amount);
    }

    public static boolean hasLegacyPoweredArmorElectricWeakness(LivingEntity entity, DamageSource damage) {
        return com.hbm.ntm.damage.DamageResistanceHandler.hasLegacyPoweredArmorElectricWeakness(entity, damage);
    }

    public static boolean hasLegacyPoweredArmorElectricWeakness(LivingEntity entity, ResourceKey<DamageType> damage) {
        return com.hbm.ntm.damage.DamageResistanceHandler.hasLegacyPoweredArmorElectricWeakness(entity, damage);
    }

    public static boolean hasLegacyPoweredArmorElectricWeakness(LivingEntity entity, DamageClass damageClass) {
        return com.hbm.ntm.damage.DamageResistanceHandler.hasLegacyPoweredArmorElectricWeakness(entity,
                damageClass.modern());
    }

    public static boolean hasLegacyPoweredArmorElectricWeakness(LivingEntity entity, String legacyTypeOrId) {
        return com.hbm.ntm.damage.DamageResistanceHandler.hasLegacyPoweredArmorElectricWeakness(entity,
                legacyTypeOrId);
    }

    public static boolean isLegacyDamageClassElectric(DamageSource damage) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isLegacyDamageClassElectric(damage);
    }

    public static boolean isLegacyDamageClassElectric(ResourceKey<DamageType> damage) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isLegacyDamageClassElectric(damage);
    }

    public static boolean isLegacyDamageClassElectric(DamageClass damageClass) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isLegacyDamageClassElectric(damageClass.modern());
    }

    public static boolean isLegacyDamageClassElectric(String legacyTypeOrId) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isLegacyDamageClassElectric(legacyTypeOrId);
    }

    public static float[] getDTDR(LivingEntity entity, DamageSource damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getDtDr(entity, damage, amount);
    }

    public static float[] getDTDR(LivingEntity entity, ResourceKey<DamageType> damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getDtDr(entity, damage, amount);
    }

    public static float[] getDTDR(LivingEntity entity, DamageClass damageClass, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getDtDr(entity, damageClass.modern(), amount);
    }

    public static float[] getDTDR(LivingEntity entity, String legacyTypeOrId, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getDtDr(entity, legacyTypeOrId, amount);
    }

    public static float[] getDTDR(LivingEntity entity, DamageSource damage, float amount, float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getDtDr(entity, damage, amount, pierceDT, pierce);
    }

    public static float[] getDTDR(LivingEntity entity, ResourceKey<DamageType> damage, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getDtDr(entity, damage, amount, pierceDT, pierce);
    }

    public static float[] getDTDR(LivingEntity entity, DamageClass damageClass, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getDtDr(entity, damageClass.modern(), amount, pierceDT,
                pierce);
    }

    public static float[] getDTDR(LivingEntity entity, String legacyTypeOrId, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getDtDr(entity, legacyTypeOrId, amount, pierceDT, pierce);
    }

    public static float[] getDtDr(LivingEntity entity, DamageSource damage, float amount) {
        return getDTDR(entity, damage, amount);
    }

    public static float[] getDtDr(LivingEntity entity, ResourceKey<DamageType> damage, float amount) {
        return getDTDR(entity, damage, amount);
    }

    public static float[] getDtDr(LivingEntity entity, DamageClass damageClass, float amount) {
        return getDTDR(entity, damageClass, amount);
    }

    public static float[] getDtDr(LivingEntity entity, String legacyTypeOrId, float amount) {
        return getDTDR(entity, legacyTypeOrId, amount);
    }

    public static float[] getDtDr(LivingEntity entity, DamageSource damage, float amount, float pierceDT, float pierce) {
        return getDTDR(entity, damage, amount, pierceDT, pierce);
    }

    public static float[] getDtDr(LivingEntity entity, ResourceKey<DamageType> damage, float amount,
            float pierceDT, float pierce) {
        return getDTDR(entity, damage, amount, pierceDT, pierce);
    }

    public static float[] getDtDr(LivingEntity entity, DamageClass damageClass, float amount,
            float pierceDT, float pierce) {
        return getDTDR(entity, damageClass, amount, pierceDT, pierce);
    }

    public static float[] getDtDr(LivingEntity entity, String legacyTypeOrId, float amount, float pierceDT, float pierce) {
        return getDTDR(entity, legacyTypeOrId, amount, pierceDT, pierce);
    }

    public static boolean isResistanceProvider(Entity entity) {
        return com.hbm.ntm.damage.DamageResistanceHandler.isResistanceProvider(entity);
    }

    public static Resistance getProviderResistance(LivingEntity entity, DamageSource damage, float amount) {
        return Resistance.fromModernNullable(
                com.hbm.ntm.damage.DamageResistanceHandler.providerResistance(entity, damage, amount));
    }

    public static Resistance getProviderResistance(LivingEntity entity, ResourceKey<DamageType> damage, float amount) {
        return Resistance.fromModernNullable(
                com.hbm.ntm.damage.DamageResistanceHandler.providerResistance(entity, damage, amount));
    }

    public static Resistance getProviderResistance(LivingEntity entity, DamageClass damageClass, float amount) {
        return Resistance.fromModernNullable(
                com.hbm.ntm.damage.DamageResistanceHandler.providerResistance(entity, damageClass.modern(), amount));
    }

    public static Resistance getProviderResistance(LivingEntity entity, String legacyTypeOrId, float amount) {
        return Resistance.fromModernNullable(
                com.hbm.ntm.damage.DamageResistanceHandler.providerResistance(entity, legacyTypeOrId, amount));
    }

    public static Resistance getProviderResistance(LivingEntity entity, DamageSource damage, float amount,
            float pierceDT, float pierce) {
        return Resistance.fromModernNullable(
                com.hbm.ntm.damage.DamageResistanceHandler.providerResistance(entity, damage, amount, pierceDT, pierce));
    }

    public static Resistance getProviderResistance(LivingEntity entity, ResourceKey<DamageType> damage, float amount,
            float pierceDT, float pierce) {
        return Resistance.fromModernNullable(
                com.hbm.ntm.damage.DamageResistanceHandler.providerResistance(entity, damage, amount, pierceDT, pierce));
    }

    public static Resistance getProviderResistance(LivingEntity entity, DamageClass damageClass, float amount,
            float pierceDT, float pierce) {
        return Resistance.fromModernNullable(
                com.hbm.ntm.damage.DamageResistanceHandler.providerResistance(entity, damageClass.modern(), amount,
                        pierceDT, pierce));
    }

    public static Resistance getProviderResistance(LivingEntity entity, String legacyTypeOrId, float amount,
            float pierceDT, float pierce) {
        return Resistance.fromModernNullable(
                com.hbm.ntm.damage.DamageResistanceHandler.providerResistance(entity, legacyTypeOrId, amount,
                        pierceDT, pierce));
    }

    public static float[] getProviderDTDR(LivingEntity entity, DamageSource damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getProviderDtDr(entity, damage, amount);
    }

    public static float[] getProviderDTDR(LivingEntity entity, ResourceKey<DamageType> damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getProviderDtDr(entity, damage, amount);
    }

    public static float[] getProviderDTDR(LivingEntity entity, DamageClass damageClass, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getProviderDtDr(entity, damageClass.modern(), amount);
    }

    public static float[] getProviderDTDR(LivingEntity entity, String legacyTypeOrId, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getProviderDtDr(entity, legacyTypeOrId, amount);
    }

    public static float[] getProviderDTDR(LivingEntity entity, DamageSource damage, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getProviderDtDr(entity, damage, amount, pierceDT, pierce);
    }

    public static float[] getProviderDTDR(LivingEntity entity, ResourceKey<DamageType> damage, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getProviderDtDr(entity, damage, amount, pierceDT, pierce);
    }

    public static float[] getProviderDTDR(LivingEntity entity, DamageClass damageClass, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getProviderDtDr(entity, damageClass.modern(), amount,
                pierceDT, pierce);
    }

    public static float[] getProviderDTDR(LivingEntity entity, String legacyTypeOrId, float amount,
            float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.getProviderDtDr(entity, legacyTypeOrId, amount, pierceDT,
                pierce);
    }

    public static float[] getProviderDtDr(LivingEntity entity, DamageSource damage, float amount) {
        return getProviderDTDR(entity, damage, amount);
    }

    public static float[] getProviderDtDr(LivingEntity entity, ResourceKey<DamageType> damage, float amount) {
        return getProviderDTDR(entity, damage, amount);
    }

    public static float[] getProviderDtDr(LivingEntity entity, DamageClass damageClass, float amount) {
        return getProviderDTDR(entity, damageClass, amount);
    }

    public static float[] getProviderDtDr(LivingEntity entity, String legacyTypeOrId, float amount) {
        return getProviderDTDR(entity, legacyTypeOrId, amount);
    }

    public static float[] getProviderDtDr(LivingEntity entity, DamageSource damage, float amount,
            float pierceDT, float pierce) {
        return getProviderDTDR(entity, damage, amount, pierceDT, pierce);
    }

    public static float[] getProviderDtDr(LivingEntity entity, ResourceKey<DamageType> damage, float amount,
            float pierceDT, float pierce) {
        return getProviderDTDR(entity, damage, amount, pierceDT, pierce);
    }

    public static float[] getProviderDtDr(LivingEntity entity, DamageClass damageClass, float amount,
            float pierceDT, float pierce) {
        return getProviderDTDR(entity, damageClass, amount, pierceDT, pierce);
    }

    public static float[] getProviderDtDr(LivingEntity entity, String legacyTypeOrId, float amount,
            float pierceDT, float pierce) {
        return getProviderDTDR(entity, legacyTypeOrId, amount, pierceDT, pierce);
    }

    public static boolean notifyDamageDealt(LivingEntity entity, DamageSource damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.notifyDamageDealt(entity, damage, amount);
    }

    public static com.hbm.ntm.damage.DamageResistanceHandler.ResistanceBreakdown breakdown(
            LivingEntity entity, DamageSource damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.breakdown(entity, damage, amount);
    }

    public static com.hbm.ntm.damage.DamageResistanceHandler.ResistanceBreakdown breakdown(
            LivingEntity entity, ResourceKey<DamageType> damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.breakdown(entity, damage, amount);
    }

    public static com.hbm.ntm.damage.DamageResistanceHandler.ResistanceBreakdown breakdown(
            LivingEntity entity, DamageClass damageClass, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.breakdown(entity, damageClass.modern(), amount);
    }

    public static com.hbm.ntm.damage.DamageResistanceHandler.ResistanceBreakdown breakdown(
            LivingEntity entity, String legacyTypeOrId, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.breakdown(entity, legacyTypeOrId, amount);
    }

    public static com.hbm.ntm.damage.DamageResistanceHandler.ResistanceBreakdown breakdown(
            LivingEntity entity, DamageSource damage, float amount, float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.breakdown(entity, damage, amount, pierceDT, pierce);
    }

    public static com.hbm.ntm.damage.DamageResistanceHandler.ResistanceBreakdown breakdown(
            LivingEntity entity, ResourceKey<DamageType> damage, float amount, float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.breakdown(entity, damage, amount, pierceDT, pierce);
    }

    public static com.hbm.ntm.damage.DamageResistanceHandler.ResistanceBreakdown breakdown(
            LivingEntity entity, DamageClass damageClass, float amount, float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.breakdown(entity, damageClass.modern(), amount, pierceDT,
                pierce);
    }

    public static com.hbm.ntm.damage.DamageResistanceHandler.ResistanceBreakdown breakdown(
            LivingEntity entity, String legacyTypeOrId, float amount, float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.breakdown(entity, legacyTypeOrId, amount, pierceDT, pierce);
    }

    public static List<com.hbm.ntm.damage.DamageResistanceHandler.ResistanceContribution> resistanceContributions(
            LivingEntity entity, DamageSource damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.resistanceContributions(entity, damage, amount);
    }

    public static List<com.hbm.ntm.damage.DamageResistanceHandler.ResistanceContribution> resistanceContributions(
            LivingEntity entity, ResourceKey<DamageType> damage, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.resistanceContributions(entity, damage, amount);
    }

    public static List<com.hbm.ntm.damage.DamageResistanceHandler.ResistanceContribution> resistanceContributions(
            LivingEntity entity, DamageClass damageClass, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.resistanceContributions(entity, damageClass.modern(), amount);
    }

    public static List<com.hbm.ntm.damage.DamageResistanceHandler.ResistanceContribution> resistanceContributions(
            LivingEntity entity, String legacyTypeOrId, float amount) {
        return com.hbm.ntm.damage.DamageResistanceHandler.resistanceContributions(entity, legacyTypeOrId, amount);
    }

    public static List<com.hbm.ntm.damage.DamageResistanceHandler.ResistanceContribution> resistanceContributions(
            LivingEntity entity, DamageSource damage, float amount, float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.resistanceContributions(entity, damage, amount, pierceDT,
                pierce);
    }

    public static List<com.hbm.ntm.damage.DamageResistanceHandler.ResistanceContribution> resistanceContributions(
            LivingEntity entity, ResourceKey<DamageType> damage, float amount, float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.resistanceContributions(entity, damage, amount, pierceDT,
                pierce);
    }

    public static List<com.hbm.ntm.damage.DamageResistanceHandler.ResistanceContribution> resistanceContributions(
            LivingEntity entity, DamageClass damageClass, float amount, float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.resistanceContributions(entity, damageClass.modern(), amount,
                pierceDT, pierce);
    }

    public static List<com.hbm.ntm.damage.DamageResistanceHandler.ResistanceContribution> resistanceContributions(
            LivingEntity entity, String legacyTypeOrId, float amount, float pierceDT, float pierce) {
        return com.hbm.ntm.damage.DamageResistanceHandler.resistanceContributions(entity, legacyTypeOrId, amount,
                pierceDT, pierce);
    }

    public static void serialize(JsonWriter writer) throws IOException {
        writer.name("itemStats").beginArray();
        for (Map.Entry<Item, ResistanceStats> entry : itemStats.entrySet()) {
            writer.beginArray().setIndent("");
            writeItemId(writer, entry.getKey());
            writer.setIndent("  ");
            writer.beginObject();
            entry.getValue().serialize(writer);
            writer.setIndent("");
            writer.endObject().endArray().setIndent("  ");
        }
        writer.endArray();

        writer.name("setStats").beginArray();
        for (Map.Entry<Tuple.Quartet<Item, Item, Item, Item>, ResistanceStats> entry : setStats.entrySet()) {
            Tuple.Quartet<Item, Item, Item, Item> set = entry.getKey();
            writer.beginArray().setIndent("");
            writeItemId(writer, set.getW());
            writeItemId(writer, set.getX());
            writeItemId(writer, set.getY());
            writeItemId(writer, set.getZ());
            writer.setIndent("  ");
            writer.beginObject();
            entry.getValue().serialize(writer);
            writer.setIndent("");
            writer.endObject().endArray().setIndent("  ");
        }
        writer.endArray();

        writer.name("entityStats").beginArray();
        for (Map.Entry<Class<? extends Entity>, ResistanceStats> entry : entityStats.entrySet()) {
            writer.beginArray().setIndent("");
            writer.value(entry.getKey().getName()).setIndent("  ");
            writer.beginObject();
            entry.getValue().serialize(writer);
            writer.setIndent("");
            writer.endObject().endArray().setIndent("  ");
        }
        writer.endArray();
    }

    public static void deserialize(JsonObject json) {
        clearSystem();
        for (JsonElement element : array(json, "itemStats")) {
            JsonArray statArray = element.getAsJsonArray();
            Item item = item(statArray.get(0));
            if (item != null) {
                registerItem(item, ResistanceStats.deserialize(statArray.get(1).getAsJsonObject()));
            }
        }
        for (JsonElement element : array(json, "setStats")) {
            JsonArray statArray = element.getAsJsonArray();
            registerSet(item(statArray.get(0)), item(statArray.get(1)), item(statArray.get(2)), item(statArray.get(3)),
                    ResistanceStats.deserialize(statArray.get(4).getAsJsonObject()));
        }
        for (JsonElement element : array(json, "entityStats")) {
            JsonArray statArray = element.getAsJsonArray();
            Class<? extends Entity> entityClass = entityClass(statArray.get(0).getAsString());
            if (entityClass != null) {
                registerEntity(entityClass, ResistanceStats.deserialize(statArray.get(1).getAsJsonObject()));
            }
        }
    }

    private static void writeItemId(JsonWriter writer, Item item) throws IOException {
        if (item == null) {
            writer.nullValue();
            return;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            writer.nullValue();
        } else {
            writer.value(id.toString());
        }
    }

    private static JsonArray array(JsonObject json, String name) {
        JsonElement element = json.get(name);
        return element == null || !element.isJsonArray() ? new JsonArray() : element.getAsJsonArray();
    }

    private static Item item(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
        return id == null ? null : ForgeRegistries.ITEMS.getValue(id);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Entity> entityClass(String name) {
        try {
            Class<?> type = Class.forName(name);
            return Entity.class.isAssignableFrom(type) ? (Class<? extends Entity>) type : null;
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private static void addSetInfo(Tuple.Quartet<Item, Item, Item, Item> set) {
        addSetInfo(set.getW(), set);
        addSetInfo(set.getX(), set);
        addSetInfo(set.getY(), set);
        addSetInfo(set.getZ(), set);
    }

    private static void addSetInfo(Item item, Tuple.Quartet<Item, Item, Item, Item> set) {
        if (item != null) {
            List<Tuple.Quartet<Item, Item, Item, Item>> sets = itemInfoSet.computeIfAbsent(item, ignored -> new ArrayList<>());
            if (!sets.contains(set)) {
                sets.add(set);
            }
        }
    }

    private static final class ItemStatsMap extends HashMap<Item, ResistanceStats> {
        @Override
        public ResistanceStats put(Item item, ResistanceStats stats) {
            ResistanceStats previous = super.put(item, stats);
            if (item != null && stats != null) {
                com.hbm.ntm.damage.DamageResistanceHandler.registerItem(item, stats.modern());
            }
            return previous;
        }
    }

    private static final class SetStatsMap extends HashMap<Tuple.Quartet<Item, Item, Item, Item>, ResistanceStats> {
        @Override
        public ResistanceStats put(Tuple.Quartet<Item, Item, Item, Item> set, ResistanceStats stats) {
            ResistanceStats previous = super.put(set, stats);
            if (set != null && stats != null) {
                addSetInfo(set);
                com.hbm.ntm.damage.DamageResistanceHandler.registerSet(set.getW(), set.getX(), set.getY(),
                        set.getZ(), stats.modern());
            }
            return previous;
        }
    }

    private static final class EntityStatsMap extends HashMap<Class<? extends Entity>, ResistanceStats> {
        @Override
        public ResistanceStats put(Class<? extends Entity> entityClass, ResistanceStats stats) {
            ResistanceStats previous = super.put(entityClass, stats);
            if (entityClass != null && stats != null) {
                com.hbm.ntm.damage.DamageResistanceHandler.registerEntity(entityClass, stats.modern());
            }
            return previous;
        }
    }

    public enum DamageClass {
        PHYSICAL,
        FIRE,
        EXPLOSIVE,
        ELECTRIC,
        PLASMA,
        LASER,
        MICROWAVE,
        SUBATOMIC,
        OTHER;

        public com.hbm.ntm.damage.DamageClass modern() {
            return com.hbm.ntm.damage.DamageClass.valueOf(name());
        }
    }

    public static class ResistanceStats {
        public HashMap<String, Resistance> exactResistances = new HashMap<>();
        public HashMap<String, Resistance> categoryResistances = new HashMap<>();
        public Resistance otherResistance;

        public Resistance getResistance(DamageSource source) {
            com.hbm.ntm.damage.DamageResistance resistance = modern().getResistance(source);
            return resistance == null ? null : Resistance.fromModern(resistance);
        }

        public Resistance getResistance(ResourceKey<DamageType> type) {
            com.hbm.ntm.damage.DamageResistance resistance = modern().getResistance(type);
            return resistance == null ? null : Resistance.fromModern(resistance);
        }

        public Resistance getResistance(String legacyTypeOrId) {
            com.hbm.ntm.damage.DamageResistance resistance = modern().getResistance(legacyTypeOrId);
            return resistance == null ? null : Resistance.fromModern(resistance);
        }

        public Resistance getResistance(DamageClass damageClass) {
            com.hbm.ntm.damage.DamageResistance resistance = modern().getResistance(damageClass.modern());
            return resistance == null ? null : Resistance.fromModern(resistance);
        }

        public com.hbm.ntm.damage.DamageResistanceStats.ResistanceMatch match(DamageSource source) {
            return modern().match(source);
        }

        public com.hbm.ntm.damage.DamageResistanceStats.ResistanceMatch match(ResourceKey<DamageType> type) {
            return modern().match(type);
        }

        public com.hbm.ntm.damage.DamageResistanceStats.ResistanceMatch match(String legacyTypeOrId) {
            return modern().match(legacyTypeOrId);
        }

        public com.hbm.ntm.damage.DamageResistanceStats.ResistanceMatch match(DamageClass damageClass) {
            return modern().match(damageClass.modern());
        }

        public ResistanceStats addExact(String type, float threshold, float resistance) {
            exactResistances.put(exactTypeKey(type), new Resistance(threshold, resistance));
            return this;
        }

        public ResistanceStats addExact(ResourceKey<DamageType> type, float threshold, float resistance) {
            exactResistances.put(exactTypeKey(type), new Resistance(threshold, resistance));
            return this;
        }

        public ResistanceStats addExact(DamageClass type, float threshold, float resistance) {
            exactResistances.put(exactTypeKey(type), new Resistance(threshold, resistance));
            return this;
        }

        public ResistanceStats addCategory(String type, float threshold, float resistance) {
            categoryResistances.put(categoryKey(type), new Resistance(threshold, resistance));
            return this;
        }

        public ResistanceStats addCategory(ResourceKey<DamageType> type, float threshold, float resistance) {
            categoryResistances.put(typeToCategory(type), new Resistance(threshold, resistance));
            return this;
        }

        public ResistanceStats addCategory(DamageClass type, float threshold, float resistance) {
            return addCategory(categoryKey(type), threshold, resistance);
        }

        public ResistanceStats setOther(float threshold, float resistance) {
            otherResistance = new Resistance(threshold, resistance);
            return this;
        }

        public com.hbm.ntm.damage.DamageResistanceStats modern() {
            com.hbm.ntm.damage.DamageResistanceStats stats = new com.hbm.ntm.damage.DamageResistanceStats();
            for (Map.Entry<String, Resistance> entry : exactResistances.entrySet()) {
                stats.addExact(entry.getKey(), entry.getValue().threshold, entry.getValue().resistance);
            }
            for (Map.Entry<String, Resistance> entry : categoryResistances.entrySet()) {
                stats.addCategory(entry.getKey(), entry.getValue().threshold, entry.getValue().resistance);
            }
            if (otherResistance != null) {
                stats.setOther(otherResistance.threshold, otherResistance.resistance);
            }
            return stats;
        }

        public static ResistanceStats fromModern(com.hbm.ntm.damage.DamageResistanceStats modern) {
            ResistanceStats stats = new ResistanceStats();
            for (Map.Entry<String, com.hbm.ntm.damage.DamageResistance> entry : modern.exactResistances().entrySet()) {
                stats.addExact(entry.getKey(), entry.getValue().threshold(), entry.getValue().resistance());
            }
            for (Map.Entry<String, com.hbm.ntm.damage.DamageResistance> entry : modern.categoryResistances().entrySet()) {
                stats.addCategory(entry.getKey(), entry.getValue().threshold(), entry.getValue().resistance());
            }
            if (modern.otherResistance() != null) {
                stats.otherResistance = Resistance.fromModern(modern.otherResistance());
            }
            return stats;
        }

        public void serialize(JsonWriter writer) throws IOException {
            JsonObject json = modern().toJson();
            JsonArray exact = array(json, "exact");
            if (exact.size() > 0) {
                writer.name("exact").beginArray();
                for (JsonElement element : exact) {
                    JsonArray entry = element.getAsJsonArray();
                    writer.beginArray().setIndent("");
                    writer.value(entry.get(0).getAsString()).value(entry.get(1).getAsFloat())
                            .value(entry.get(2).getAsFloat());
                    writer.endArray().setIndent("  ");
                }
                writer.endArray();
            }
            JsonArray category = array(json, "category");
            if (category.size() > 0) {
                writer.name("category").beginArray();
                for (JsonElement element : category) {
                    JsonArray entry = element.getAsJsonArray();
                    writer.beginArray().setIndent("");
                    writer.value(entry.get(0).getAsString()).value(entry.get(1).getAsFloat())
                            .value(entry.get(2).getAsFloat());
                    writer.endArray().setIndent("  ");
                }
                writer.endArray();
            }
            JsonArray other = array(json, "other");
            if (other.size() >= 2) {
                writer.name("other").beginArray().setIndent("");
                writer.value(other.get(0).getAsFloat()).value(other.get(1).getAsFloat());
                writer.endArray().setIndent("  ");
            }
        }

        public static ResistanceStats deserialize(JsonObject json) {
            return fromModern(com.hbm.ntm.damage.DamageResistanceStats.fromJson(json));
        }
    }

    public static class Resistance {
        public float threshold;
        public float resistance;

        public Resistance(float threshold, float resistance) {
            this.threshold = threshold;
            this.resistance = resistance;
        }

        public com.hbm.ntm.damage.DamageResistance modern() {
            return new com.hbm.ntm.damage.DamageResistance(threshold, resistance);
        }

        public static Resistance fromModern(com.hbm.ntm.damage.DamageResistance modern) {
            return new Resistance(modern.threshold(), modern.resistance());
        }

        public static Resistance fromModernNullable(com.hbm.ntm.damage.DamageResistance modern) {
            return modern == null ? null : fromModern(modern);
        }
    }
}
