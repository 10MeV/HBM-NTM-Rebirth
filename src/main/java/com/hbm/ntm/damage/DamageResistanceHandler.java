package com.hbm.ntm.damage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.entity.ResistanceProvider;
import com.hbm.ntm.armor.FsbPoweredArmor;
import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DamageResistanceHandler {
    public static final String CATEGORY_EXPLOSION = "EXPL";
    public static final String CATEGORY_FIRE = "FIRE";
    public static final String CATEGORY_PHYSICAL = "PHYS";
    public static final String CATEGORY_ENERGY = "EN";
    public static final float LEGACY_FSB_ELECTRIC_DAMAGE_MULTIPLIER = 5.0F;

    private static final ThreadLocal<PierceState> CURRENT_PIERCING = ThreadLocal.withInitial(() -> PierceState.NONE);

    private static final Map<Item, DamageResistanceStats> ITEM_STATS = new HashMap<>();
    private static final Map<ArmorSet, DamageResistanceStats> SET_STATS = new HashMap<>();
    private static final Map<Item, List<ArmorSet>> ITEM_INFO_SETS = new HashMap<>();
    private static final Map<Class<? extends Entity>, DamageResistanceStats> ENTITY_STATS = new HashMap<>();
    private static final Map<String, DamageResistanceStats> ENTITY_SIMPLE_NAME_STATS = new HashMap<>();
    private static final Map<String, String> EXACT_ALIASES = createExactAliases();

    static {
        registerEntity(Creeper.class, new DamageResistanceStats().addCategory(CATEGORY_EXPLOSION, 2.0F, 0.25F));
    }

    public static void clear() {
        ITEM_STATS.clear();
        SET_STATS.clear();
        ITEM_INFO_SETS.clear();
        ENTITY_STATS.clear();
        ENTITY_SIMPLE_NAME_STATS.clear();
        registerEntity(Creeper.class, new DamageResistanceStats().addCategory(CATEGORY_EXPLOSION, 2.0F, 0.25F));
    }

    public static void setup(float pierceDt, float pierceDr) {
        CURRENT_PIERCING.set(new PierceState(pierceDt, pierceDr));
    }

    public static void reset() {
        CURRENT_PIERCING.set(PierceState.NONE);
    }

    public static PierceState capturePiercing() {
        return CURRENT_PIERCING.get();
    }

    public static void restorePiercing(PierceState state) {
        CURRENT_PIERCING.set(state == null ? PierceState.NONE : state);
    }

    public static float currentPierceDt() {
        return CURRENT_PIERCING.get().pierceDt();
    }

    public static float currentPierceDr() {
        return CURRENT_PIERCING.get().pierceDr();
    }

    public static void registerItem(Item item, DamageResistanceStats stats) {
        ITEM_STATS.put(item, stats);
    }

    public static DamageResistanceStats removeItem(Item item) {
        return ITEM_STATS.remove(item);
    }

    public static void registerSet(Item helmet, Item chest, Item legs, Item boots, DamageResistanceStats stats) {
        ArmorSet set = new ArmorSet(helmet, chest, legs, boots);
        SET_STATS.put(set, stats);
        addSetInfo(helmet, set);
        addSetInfo(chest, set);
        addSetInfo(legs, set);
        addSetInfo(boots, set);
    }

    public static DamageResistanceStats removeSet(Item helmet, Item chest, Item legs, Item boots) {
        ArmorSet set = new ArmorSet(helmet, chest, legs, boots);
        DamageResistanceStats removed = SET_STATS.remove(set);
        if (removed != null) {
            removeSetInfo(helmet, set);
            removeSetInfo(chest, set);
            removeSetInfo(legs, set);
            removeSetInfo(boots, set);
        }
        return removed;
    }

    public static void registerEntity(Class<? extends Entity> entityClass, DamageResistanceStats stats) {
        ENTITY_STATS.put(entityClass, stats);
    }

    public static DamageResistanceStats removeEntity(Class<? extends Entity> entityClass) {
        return ENTITY_STATS.remove(entityClass);
    }

    public static void registerEntitySimpleName(String simpleName, DamageResistanceStats stats) {
        ENTITY_SIMPLE_NAME_STATS.put(simpleName, stats);
    }

    public static float calculateDamage(LivingEntity entity, DamageSource source, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return calculateDamage(entity, source, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float calculateDamage(LivingEntity entity, ResourceKey<DamageType> type, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return calculateDamage(entity, type, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float calculateDamage(LivingEntity entity, DamageClass damageClass, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return calculateDamage(entity, damageClass, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float calculateDamage(LivingEntity entity, String legacyTypeOrId, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return calculateDamage(entity, legacyTypeOrId, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float calculateDamage(LivingEntity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        return breakdown(entity, source, amount, pierceDt, pierceDr).finalDamage();
    }

    public static float applyLegacyPreResistanceDamageModifiers(@Nullable LivingEntity entity,
            @Nullable DamageSource source, float amount) {
        if (hasLegacyPoweredArmorElectricWeakness(entity, source)) {
            return amount * LEGACY_FSB_ELECTRIC_DAMAGE_MULTIPLIER;
        }
        return amount;
    }

    public static float applyLegacyPreResistanceDamageModifiers(@Nullable LivingEntity entity,
            @Nullable ResourceKey<DamageType> type, float amount) {
        if (hasLegacyPoweredArmorElectricWeakness(entity, type)) {
            return amount * LEGACY_FSB_ELECTRIC_DAMAGE_MULTIPLIER;
        }
        return amount;
    }

    public static float applyLegacyPreResistanceDamageModifiers(@Nullable LivingEntity entity,
            @Nullable DamageClass damageClass, float amount) {
        if (hasLegacyPoweredArmorElectricWeakness(entity, damageClass)) {
            return amount * LEGACY_FSB_ELECTRIC_DAMAGE_MULTIPLIER;
        }
        return amount;
    }

    public static float applyLegacyPreResistanceDamageModifiers(@Nullable LivingEntity entity,
            @Nullable String legacyTypeOrId, float amount) {
        if (hasLegacyPoweredArmorElectricWeakness(entity, legacyTypeOrId)) {
            return amount * LEGACY_FSB_ELECTRIC_DAMAGE_MULTIPLIER;
        }
        return amount;
    }

    public static boolean hasLegacyPoweredArmorElectricWeakness(@Nullable LivingEntity entity,
            @Nullable DamageSource source) {
        if (entity == null || source == null || !isLegacyDamageClassElectric(source)) {
            return false;
        }
        return hasPoweredChestplate(entity);
    }

    public static boolean hasLegacyPoweredArmorElectricWeakness(@Nullable LivingEntity entity,
            @Nullable ResourceKey<DamageType> type) {
        return entity != null && isLegacyDamageClassElectric(type) && hasPoweredChestplate(entity);
    }

    public static boolean hasLegacyPoweredArmorElectricWeakness(@Nullable LivingEntity entity,
            @Nullable DamageClass damageClass) {
        return entity != null && isLegacyDamageClassElectric(damageClass) && hasPoweredChestplate(entity);
    }

    public static boolean hasLegacyPoweredArmorElectricWeakness(@Nullable LivingEntity entity,
            @Nullable String legacyTypeOrId) {
        return entity != null && isLegacyDamageClassElectric(legacyTypeOrId) && hasPoweredChestplate(entity);
    }

    private static boolean hasPoweredChestplate(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return !chest.isEmpty() && chest.getItem() instanceof FsbPoweredArmor;
    }

    public static boolean isLegacyDamageClassElectric(@Nullable DamageSource source) {
        return source != null && exactTypeKey(DamageClass.ELECTRIC).equals(exactTypeKey(source));
    }

    public static boolean isLegacyDamageClassElectric(@Nullable ResourceKey<DamageType> type) {
        return ModDamageSources.matches(type, DamageClass.ELECTRIC);
    }

    public static boolean isLegacyDamageClassElectric(@Nullable DamageClass damageClass) {
        return damageClass == DamageClass.ELECTRIC;
    }

    public static boolean isLegacyDamageClassElectric(@Nullable String legacyTypeOrId) {
        return ModDamageSources.matches(legacyTypeOrId, DamageClass.ELECTRIC);
    }

    public static float calculateDamage(LivingEntity entity, ResourceKey<DamageType> type, float amount,
            float pierceDt, float pierceDr) {
        return calculateDamage(entity, sourceFor(entity, type), amount, pierceDt, pierceDr);
    }

    public static float calculateDamage(LivingEntity entity, DamageClass damageClass, float amount,
            float pierceDt, float pierceDr) {
        return calculateDamage(entity, sourceFor(entity, damageClass), amount, pierceDt, pierceDr);
    }

    public static float calculateDamage(LivingEntity entity, String legacyTypeOrId, float amount,
            float pierceDt, float pierceDr) {
        return calculateDamage(entity, sourceFor(entity, legacyTypeOrId), amount, pierceDt, pierceDr);
    }

    public static float[] getDtDr(LivingEntity entity, DamageSource source, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return getDtDr(entity, source, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float[] getDtDr(LivingEntity entity, ResourceKey<DamageType> type, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return getDtDr(entity, type, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float[] getDtDr(LivingEntity entity, DamageClass damageClass, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return getDtDr(entity, damageClass, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float[] getDtDr(LivingEntity entity, String legacyTypeOrId, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return getDtDr(entity, legacyTypeOrId, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float[] getDtDr(LivingEntity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        ResistanceContributionTotals totals = collectResistanceContributions(entity, source, amount, pierceDt, pierceDr);
        return new float[] { totals.threshold(), totals.resistance() };
    }

    public static float[] getDtDr(LivingEntity entity, ResourceKey<DamageType> type, float amount,
            float pierceDt, float pierceDr) {
        return getDtDr(entity, sourceFor(entity, type), amount, pierceDt, pierceDr);
    }

    public static float[] getDtDr(LivingEntity entity, DamageClass damageClass, float amount,
            float pierceDt, float pierceDr) {
        return getDtDr(entity, sourceFor(entity, damageClass), amount, pierceDt, pierceDr);
    }

    public static float[] getDtDr(LivingEntity entity, String legacyTypeOrId, float amount,
            float pierceDt, float pierceDr) {
        return getDtDr(entity, sourceFor(entity, legacyTypeOrId), amount, pierceDt, pierceDr);
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity, DamageSource source, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return resistanceContributions(entity, source, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity,
            ResourceKey<DamageType> type, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return resistanceContributions(entity, type, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity,
            DamageClass damageClass, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return resistanceContributions(entity, damageClass, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity,
            String legacyTypeOrId, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return resistanceContributions(entity, legacyTypeOrId, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity, DamageSource source, float amount,
            float pierceDt, float pierceDr) {
        return collectResistanceContributions(entity, source, amount, pierceDt, pierceDr).contributions();
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity,
            ResourceKey<DamageType> type, float amount, float pierceDt, float pierceDr) {
        return resistanceContributions(entity, sourceFor(entity, type), amount, pierceDt, pierceDr);
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity,
            DamageClass damageClass, float amount, float pierceDt, float pierceDr) {
        return resistanceContributions(entity, sourceFor(entity, damageClass), amount, pierceDt, pierceDr);
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity,
            String legacyTypeOrId, float amount, float pierceDt, float pierceDr) {
        return resistanceContributions(entity, sourceFor(entity, legacyTypeOrId), amount, pierceDt, pierceDr);
    }

    public static boolean isResistanceProvider(@Nullable Entity entity) {
        return entity instanceof ResistanceProvider;
    }

    public static DamageResistance providerResistance(@Nullable LivingEntity entity, DamageSource source, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return providerResistance(entity, source, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static DamageResistance providerResistance(@Nullable LivingEntity entity, ResourceKey<DamageType> type,
            float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return providerResistance(entity, type, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static DamageResistance providerResistance(@Nullable LivingEntity entity, DamageClass damageClass,
            float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return providerResistance(entity, damageClass, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static DamageResistance providerResistance(@Nullable LivingEntity entity, String legacyTypeOrId,
            float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return providerResistance(entity, legacyTypeOrId, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static DamageResistance providerResistance(@Nullable LivingEntity entity, DamageSource source,
            float amount, float pierceDt, float pierceDr) {
        if (!(entity instanceof ResistanceProvider provider)) {
            return null;
        }
        float[] provided = provider.getCurrentDtDr(source, amount, pierceDt, pierceDr);
        return resistanceFromProvider(provided);
    }

    public static DamageResistance providerResistance(@Nullable LivingEntity entity, ResourceKey<DamageType> type,
            float amount, float pierceDt, float pierceDr) {
        if (!(entity instanceof ResistanceProvider)) {
            return null;
        }
        return providerResistance(entity, sourceFor(entity, type), amount, pierceDt, pierceDr);
    }

    public static DamageResistance providerResistance(@Nullable LivingEntity entity, DamageClass damageClass,
            float amount, float pierceDt, float pierceDr) {
        if (!(entity instanceof ResistanceProvider)) {
            return null;
        }
        return providerResistance(entity, sourceFor(entity, damageClass), amount, pierceDt, pierceDr);
    }

    public static DamageResistance providerResistance(@Nullable LivingEntity entity, String legacyTypeOrId,
            float amount, float pierceDt, float pierceDr) {
        if (!(entity instanceof ResistanceProvider)) {
            return null;
        }
        return providerResistance(entity, sourceFor(entity, legacyTypeOrId), amount, pierceDt, pierceDr);
    }

    public static float[] getProviderDtDr(@Nullable LivingEntity entity, DamageSource source, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return getProviderDtDr(entity, source, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float[] getProviderDtDr(@Nullable LivingEntity entity, ResourceKey<DamageType> type, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return getProviderDtDr(entity, type, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float[] getProviderDtDr(@Nullable LivingEntity entity, DamageClass damageClass, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return getProviderDtDr(entity, damageClass, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float[] getProviderDtDr(@Nullable LivingEntity entity, String legacyTypeOrId, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return getProviderDtDr(entity, legacyTypeOrId, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float[] getProviderDtDr(@Nullable LivingEntity entity, DamageSource source, float amount,
            float pierceDt, float pierceDr) {
        DamageResistance resistance = providerResistance(entity, source, amount, pierceDt, pierceDr);
        return resistance == null ? new float[] { 0.0F, 0.0F }
                : new float[] { resistance.threshold(), resistance.resistance() };
    }

    public static float[] getProviderDtDr(@Nullable LivingEntity entity, ResourceKey<DamageType> type, float amount,
            float pierceDt, float pierceDr) {
        DamageResistance resistance = providerResistance(entity, type, amount, pierceDt, pierceDr);
        return resistance == null ? new float[] { 0.0F, 0.0F }
                : new float[] { resistance.threshold(), resistance.resistance() };
    }

    public static float[] getProviderDtDr(@Nullable LivingEntity entity, DamageClass damageClass, float amount,
            float pierceDt, float pierceDr) {
        DamageResistance resistance = providerResistance(entity, damageClass, amount, pierceDt, pierceDr);
        return resistance == null ? new float[] { 0.0F, 0.0F }
                : new float[] { resistance.threshold(), resistance.resistance() };
    }

    public static float[] getProviderDtDr(@Nullable LivingEntity entity, String legacyTypeOrId, float amount,
            float pierceDt, float pierceDr) {
        DamageResistance resistance = providerResistance(entity, legacyTypeOrId, amount, pierceDt, pierceDr);
        return resistance == null ? new float[] { 0.0F, 0.0F }
                : new float[] { resistance.threshold(), resistance.resistance() };
    }

    public static boolean notifyDamageDealt(@Nullable LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ResistanceProvider provider)) {
            return false;
        }
        provider.onDamageDealt(source, amount);
        return true;
    }

    private static ResistanceContributionTotals collectResistanceContributions(LivingEntity entity, DamageSource source,
            float amount, float pierceDt, float pierceDr) {
        List<ResistanceContribution> contributions = new ArrayList<>();

        DamageResistance provided = providerResistance(entity, source, amount, pierceDt, pierceDr);
        if (provided != null) {
            addContribution(contributions, "provider", entity.getClass().getName(), "provided", "provider", provided);
        }

        DamageResistanceStats setResistance = SET_STATS.get(ArmorSet.of(entity));
        if (setResistance != null) {
            DamageResistanceStats.ResistanceMatch match = setResistance.match(source);
            if (match != null) {
                addContribution(contributions, "set", describeArmorSet(ArmorSet.of(entity)), match.kind(), match.key(), match.resistance());
            }
        }

        for (EquipmentSlot slot : ArmorSet.ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            DamageResistanceStats stats = ITEM_STATS.get(stack.getItem());
            if (stats == null) {
                continue;
            }
            DamageResistanceStats.ResistanceMatch match = stats.match(source);
            if (match != null) {
                addContribution(contributions, "item", slot.getName() + "=" + itemId(stack.getItem()), match.kind(), match.key(), match.resistance());
            }
        }

        DamageResistanceStats innate = ENTITY_STATS.get(entity.getClass());
        String innateId = entity.getClass().getName();
        String innateMatchKind = "class";
        if (innate == null) {
            EntityStatsMatch match = entityStatsMatch(entity);
            innate = match == null ? null : match.stats();
            innateId = match == null ? entity.getClass().getName() : match.id();
            innateMatchKind = match == null ? "class" : match.kind();
        }
        if (innate != null) {
            DamageResistanceStats.ResistanceMatch match = innate.match(source);
            if (match != null) {
                addContribution(contributions, "entity", innateMatchKind + ":" + innateId, match.kind(), match.key(), match.resistance());
            }
        }

        return new ResistanceContributionTotals(List.copyOf(contributions));
    }

    @Nullable
    private static DamageResistance resistanceFromProvider(@Nullable float[] provided) {
        if (provided == null || provided.length < 2) {
            return null;
        }
        return new DamageResistance(provided[0], provided[1]);
    }

    private static void addContribution(List<ResistanceContribution> contributions, String source, String id,
            String matchKind, String matchKey, DamageResistance resistance) {
        if (resistance.threshold() == 0.0F && resistance.resistance() == 0.0F) {
            return;
        }
        contributions.add(new ResistanceContribution(source, id, matchKind, matchKey, resistance.threshold(), resistance.resistance()));
    }

    public static ResistanceBreakdown breakdown(LivingEntity entity, DamageSource source, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return breakdown(entity, source, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static ResistanceBreakdown breakdown(LivingEntity entity, ResourceKey<DamageType> type, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return breakdown(entity, type, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static ResistanceBreakdown breakdown(LivingEntity entity, DamageClass damageClass, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return breakdown(entity, damageClass, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static ResistanceBreakdown breakdown(LivingEntity entity, String legacyTypeOrId, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return breakdown(entity, legacyTypeOrId, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static ResistanceBreakdown breakdown(LivingEntity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        String exactType = exactTypeKey(source);
        String category = typeToCategory(source);
        if (isAbsolute(source)) {
            return new ResistanceBreakdown(exactType, category, true, 0.0F, 0.0F, 0.0F, 0.0F, amount);
        }

        float[] vals = getDtDr(entity, source, amount, pierceDt, pierceDr);
        float rawDt = vals[0];
        float rawDr = vals[1];
        DamageFormula formula = reduceDamage(amount, rawDt, rawDr, pierceDt, pierceDr);
        return new ResistanceBreakdown(exactType, category, false, rawDt, rawDr,
                formula.effectiveDt(), formula.effectiveDr(), formula.finalDamage());
    }

    public static ResistanceBreakdown breakdown(LivingEntity entity, ResourceKey<DamageType> type, float amount,
            float pierceDt, float pierceDr) {
        return breakdown(entity, sourceFor(entity, type), amount, pierceDt, pierceDr);
    }

    public static ResistanceBreakdown breakdown(LivingEntity entity, DamageClass damageClass, float amount,
            float pierceDt, float pierceDr) {
        return breakdown(entity, sourceFor(entity, damageClass), amount, pierceDt, pierceDr);
    }

    public static ResistanceBreakdown breakdown(LivingEntity entity, String legacyTypeOrId, float amount,
            float pierceDt, float pierceDr) {
        return breakdown(entity, sourceFor(entity, legacyTypeOrId), amount, pierceDt, pierceDr);
    }

    public static DamageFormula reduceDamage(float amount, float rawDt, float rawDr, float pierceDt, float pierceDr) {
        float effectiveDt = Math.max(0.0F, rawDt - pierceDt);
        float effectiveDr = Mth.clamp(rawDr * Mth.clamp(1.0F - pierceDr, 0.0F, 2.0F), 0.0F, 1.0F);
        float finalDamage = effectiveDt >= amount ? 0.0F : Math.max(0.0F, (amount - effectiveDt) * (1.0F - effectiveDr));
        return new DamageFormula(effectiveDt, effectiveDr, finalDamage);
    }

    public static CoreAudit coreAudit() {
        List<String> problems = new ArrayList<>();
        expect(problems, "physical category", CATEGORY_PHYSICAL.equals(categoryKey(DamageClass.PHYSICAL)));
        expect(problems, "explosive category", CATEGORY_EXPLOSION.equals(categoryKey(DamageClass.EXPLOSIVE)));
        expect(problems, "energy category", CATEGORY_ENERGY.equals(categoryKey(DamageClass.LASER)));
        expect(problems, "combine exact alias", "cmb".equals(exactTypeKey("combine_ball")));
        expect(problems, "subatomic exact alias", "subatomic".equals(exactTypeKey("subAtomic3")));
        expect(problems, "registry exact alias", "cmb".equals(exactTypeKey(ModDamageSources.COMBINE_BALL)));
        expect(problems, "registry projectile category", CATEGORY_PHYSICAL.equals(typeToCategory(ModDamageSources.SHRAPNEL)));
        expect(problems, "registry explosion category", CATEGORY_EXPLOSION.equals(typeToCategory(ModDamageSources.NUCLEAR_BLAST)));
        expect(problems, "legacy string energy category", CATEGORY_ENERGY.equals(typeToCategory(com.hbm.lib.ModDamageSource.s_emp)));
        expect(problems, "vanilla entity attack string physical category",
                CATEGORY_PHYSICAL.equals(typeToCategory("minecraft:player_attack"))
                        && CATEGORY_PHYSICAL.equals(typeToCategory("minecraft:mob_attack"))
                        && CATEGORY_PHYSICAL.equals(typeToCategory("minecraft:arrow")));
        expect(problems, "null damage helper paths empty",
                exactType((DamageSource) null).isEmpty()
                        && exactTypeKey((DamageSource) null).isEmpty()
                        && exactTypeKey((ResourceKey<DamageType>) null).isEmpty()
                        && exactTypeKey((String) null).isEmpty()
                        && typeToCategory((DamageSource) null).isEmpty()
                        && typeToCategory((ResourceKey<DamageType>) null).isEmpty()
                        && typeToCategory((String) null).isEmpty()
                        && registryTypeKey(null) == null
                        && !isAbsolute((DamageSource) null)
                        && !isUnblockableForLegacyResistance((DamageSource) null));
        expect(problems, "damage class category physical/fire/explosive",
                CATEGORY_PHYSICAL.equals(typeToCategory(DamageClass.PHYSICAL))
                        && CATEGORY_FIRE.equals(typeToCategory(DamageClass.FIRE))
                        && CATEGORY_EXPLOSION.equals(typeToCategory(DamageClass.EXPLOSIVE)));
        expect(problems, "damage class energy category",
                CATEGORY_ENERGY.equals(typeToCategory(DamageClass.LASER))
                        && CATEGORY_ENERGY.equals(typeToCategory(DamageClass.PLASMA))
                        && CATEGORY_ENERGY.equals(typeToCategory(DamageClass.SUBATOMIC)));
        expect(problems, "damage class exact key",
                "revolverbullet".equals(exactTypeKey(DamageClass.PHYSICAL))
                        && "subatomic".equals(exactTypeKey(DamageClass.SUBATOMIC)));
        expect(problems, "registry absolute metadata", isAbsolute(ModDamageSources.DIGAMMA));
        expect(problems, "legacy string unblockable metadata", isUnblockableForLegacyResistance("subAtomic4"));
        expect(problems, "damage class unblockable metadata",
                isUnblockableForLegacyResistance(DamageClass.SUBATOMIC)
                        && !isUnblockableForLegacyResistance(DamageClass.LASER));
        DamageResistanceStats stats = new DamageResistanceStats()
                .addExact("subAtomic4", 1.0F, 0.2F)
                .addExact("on_fire", 2.0F, 0.3F)
                .addCategory("laser", 3.0F, 0.4F)
                .addCategory(DamageClass.ELECTRIC, 4.0F, 0.5F)
                .setOther(5.0F, 0.6F);
        expectResistance(problems, "stats exact normalizes subAtomic", stats.exactResistances().get("subatomic"), 1.0F, 0.2F);
        expectResistance(problems, "stats exact normalizes on_fire", stats.exactResistances().get("onfire"), 2.0F, 0.3F);
        expectResistance(problems, "stats category normalizes energy", stats.categoryResistances().get(CATEGORY_ENERGY), 4.0F, 0.5F);
        expectResistance(problems, "stats other preserved", stats.otherResistance(), 5.0F, 0.6F);
        DamageResistanceStats roundTrip = DamageResistanceStats.fromJson(stats.toJson());
        expectResistance(problems, "stats json exact roundtrip", roundTrip.exactResistances().get("subatomic"), 1.0F, 0.2F);
        expectResistance(problems, "stats json category roundtrip", roundTrip.categoryResistances().get(CATEGORY_ENERGY), 4.0F, 0.5F);
        expectResistance(problems, "stats json other roundtrip", roundTrip.otherResistance(), 5.0F, 0.6F);
        JsonObject invalidStatsJson = new JsonObject();
        JsonArray invalidExact = new JsonArray();
        JsonArray invalidExactEntry = new JsonArray();
        invalidExactEntry.add("laser");
        invalidExactEntry.add("not a number");
        invalidExactEntry.add(0.5F);
        invalidExact.add(invalidExactEntry);
        invalidStatsJson.add("exact", invalidExact);
        DamageResistanceStats.JsonParseResult invalidStats = DamageResistanceStats.parseJson(invalidStatsJson, "coreAudit");
        expect(problems, "stats json invalid warning",
                invalidStats.stats() != null
                        && invalidStats.warnings().stream().anyMatch(warning -> warning.contains("invalid exact resistance values")));
        DamageResistanceStats precedence = new DamageResistanceStats()
                .addExact("laser", 7.0F, 0.7F)
                .addCategory(CATEGORY_ENERGY, 8.0F, 0.8F)
                .setOther(9.0F, 0.9F);
        expectMatch(problems, "stats exact beats category",
                precedence.matchKeys("laser", null, CATEGORY_ENERGY, false), "exact", "laser", 7.0F, 0.7F);
        expectMatch(problems, "stats registry exact fallback",
                precedence.matchKeys("custommessage", "laser", CATEGORY_PHYSICAL, false), "exact", "laser", 7.0F, 0.7F);
        expectMatch(problems, "stats category beats other",
                precedence.matchKeys("missing", null, CATEGORY_ENERGY, false), "category", CATEGORY_ENERGY, 8.0F, 0.8F);
        expectMatch(problems, "stats other fallback",
                precedence.matchKeys("missing", null, "unknown", false), "other", "other", 9.0F, 0.9F);
        expect(problems, "stats bypass armor skips other",
                precedence.matchKeys("missing", null, "unknown", true) == null);
        DamageResistanceStats keyMatching = new DamageResistanceStats()
                .addExact("cmb", 1.0F, 0.1F)
                .addCategory(CATEGORY_ENERGY, 2.0F, 0.2F)
                .setOther(3.0F, 0.3F);
        expectMatch(problems, "stats match registry key exact",
                keyMatching.match(ModDamageSources.COMBINE_BALL), "exact", "cmb", 1.0F, 0.1F);
        expectMatch(problems, "stats match namespaced string exact",
                keyMatching.match("hbm_ntm_rebirth:combine_ball"), "exact", "cmb", 1.0F, 0.1F);
        expectMatch(problems, "stats match legacy string category",
                keyMatching.match(com.hbm.lib.ModDamageSource.s_emp), "category", CATEGORY_ENERGY, 2.0F, 0.2F);
        expectMatch(problems, "stats match damage class exact",
                new DamageResistanceStats().addExact(DamageClass.LASER, 4.0F, 0.4F)
                        .match(DamageClass.LASER), "exact", "laser", 4.0F, 0.4F);
        expectMatch(problems, "stats match damage class category",
                new DamageResistanceStats().addCategory(DamageClass.ELECTRIC, 5.0F, 0.5F)
                        .match(DamageClass.ELECTRIC), "category", CATEGORY_ENERGY, 5.0F, 0.5F);
        expectMatch(problems, "stats add exact registry key",
                new DamageResistanceStats().addExact(ModDamageSources.COMBINE_BALL, 6.0F, 0.6F)
                        .match("cmb"), "exact", "cmb", 6.0F, 0.6F);
        expectMatch(problems, "stats add category registry key",
                new DamageResistanceStats().addCategory(ModDamageSources.SHRAPNEL, 6.0F, 0.6F)
                        .match(ModDamageSources.REVOLVER_BULLET), "category", CATEGORY_PHYSICAL, 6.0F, 0.6F);
        expect(problems, "stats match string bypass skips other",
                new DamageResistanceStats().setOther(3.0F, 0.3F).match("subAtomic4") == null);
        expect(problems, "stats null source match missing",
                new DamageResistanceStats().setOther(3.0F, 0.3F).match((DamageSource) null) == null);
        PierceState previous = capturePiercing();
        try {
            setup(7.0F, 0.3F);
            expect(problems, "thread local pierce DT", nearly(currentPierceDt(), 7.0F));
            expect(problems, "thread local pierce DR", nearly(currentPierceDr(), 0.3F));
            reset();
            expect(problems, "thread local pierce reset", nearly(currentPierceDt(), 0.0F) && nearly(currentPierceDr(), 0.0F));
        } finally {
            restorePiercing(previous);
        }
        DamageFormula formula = reduceDamage(10.0F, 3.0F, 0.5F, 1.0F, 0.2F);
        expect(problems, "pierce formula DT", nearly(formula.effectiveDt(), 2.0F));
        expect(problems, "pierce formula DR", nearly(formula.effectiveDr(), 0.4F));
        expect(problems, "pierce formula final", nearly(formula.finalDamage(), 4.8F));
        DamageFormula overPierce = reduceDamage(10.0F, 3.0F, 0.25F, 0.0F, -1.0F);
        expect(problems, "negative pierce doubles DR cap path", nearly(overPierce.effectiveDr(), 0.5F));
        DamageFormula fullAbsorb = reduceDamage(10.0F, 12.0F, 0.0F, 0.0F, 0.0F);
        expect(problems, "DT full absorb", nearly(fullAbsorb.finalDamage(), 0.0F));
        api.hbm.entity.IResistanceProvider legacyProvider = new api.hbm.entity.IResistanceProvider() {
            @Override
            public float[] getCurrentDTDR(DamageSource damage, float amount, float pierceDT, float pierce) {
                return new float[] { amount + pierceDT, pierce };
            }
        };
        expect(problems, "legacy api resistance provider is modern provider", legacyProvider instanceof ResistanceProvider);
        float[] legacyProvided = legacyProvider.getCurrentDtDr(null, 2.0F, 3.0F, 0.4F);
        expect(problems, "legacy api resistance provider delegates",
                legacyProvided.length >= 2 && nearly(legacyProvided[0], 5.0F) && nearly(legacyProvided[1], 0.4F));
        expect(problems, "null entity is not resistance provider", !isResistanceProvider(null));
        expect(problems, "null provider resistance missing",
                providerResistance(null, (DamageSource) null, 1.0F) == null);
        float[] noProvider = getProviderDtDr(null, (DamageSource) null, 1.0F);
        expect(problems, "null provider DTDR zero",
                noProvider.length >= 2 && nearly(noProvider[0], 0.0F) && nearly(noProvider[1], 0.0F));
        float[] noProviderDamageClass = getProviderDtDr(null, DamageClass.LASER, 1.0F);
        expect(problems, "null provider damage class DTDR zero",
                noProviderDamageClass.length >= 2 && nearly(noProviderDamageClass[0], 0.0F)
                        && nearly(noProviderDamageClass[1], 0.0F));
        expect(problems, "null provider damage dealt skip",
                !notifyDamageDealt(null, null, 1.0F));
        expect(problems, "legacy FSB electric modifier null path",
                nearly(applyLegacyPreResistanceDamageModifiers(null, (DamageSource) null, 2.0F), 2.0F)
                        && !hasLegacyPoweredArmorElectricWeakness(null, (DamageSource) null)
                        && !isLegacyDamageClassElectric((DamageSource) null));
        expect(problems, "legacy FSB electric key discrimination",
                isLegacyDamageClassElectric(ModDamageSources.ELECTRIC)
                        && isLegacyDamageClassElectric(DamageClass.ELECTRIC)
                        && isLegacyDamageClassElectric("ELECTRIC")
                        && isLegacyDamageClassElectric("hbm_ntm_rebirth:electric")
                        && !isLegacyDamageClassElectric(ModDamageSources.ELECTRICITY)
                        && !isLegacyDamageClassElectric("electricity")
                        && !isLegacyDamageClassElectric(DamageClass.LASER));
        expect(problems, "legacy FSB electric key null modifier path",
                nearly(applyLegacyPreResistanceDamageModifiers(null, ModDamageSources.ELECTRIC, 3.0F), 3.0F)
                        && nearly(applyLegacyPreResistanceDamageModifiers(null, DamageClass.ELECTRIC, 3.0F), 3.0F)
                        && nearly(applyLegacyPreResistanceDamageModifiers(null, "ELECTRIC", 3.0F), 3.0F));
        expect(problems, "legacy util handler category bridge",
                com.hbm.util.DamageResistanceHandler.CATEGORY_ENERGY.equals(CATEGORY_ENERGY)
                        && com.hbm.util.DamageResistanceHandler.categoryKey(
                                com.hbm.util.DamageResistanceHandler.DamageClass.LASER).equals(CATEGORY_ENERGY));
        expect(problems, "legacy util handler null provider bridge",
                com.hbm.util.DamageResistanceHandler.getProviderResistance(null, (DamageSource) null, 1.0F) == null);
        expect(problems, "legacy util handler null damage class provider bridge",
                com.hbm.util.DamageResistanceHandler.getProviderResistance(null,
                        com.hbm.util.DamageResistanceHandler.DamageClass.LASER, 1.0F) == null);
        float[] legacyNoProvider = com.hbm.util.DamageResistanceHandler.getProviderDTDR(null, (DamageSource) null, 1.0F);
        expect(problems, "legacy util handler provider DTDR zero",
                legacyNoProvider.length >= 2 && nearly(legacyNoProvider[0], 0.0F)
                        && nearly(legacyNoProvider[1], 0.0F));
        com.hbm.util.DamageResistanceHandler.ResistanceStats legacyStats =
                new com.hbm.util.DamageResistanceHandler.ResistanceStats()
                        .addExact("subAtomic2", 1.0F, 0.2F)
                        .addExact("combine_ball", 1.5F, 0.25F)
                        .addCategory(com.hbm.util.DamageResistanceHandler.DamageClass.ELECTRIC, 2.0F, 0.3F)
                        .setOther(3.0F, 0.4F);
        expectLegacyResistance(problems, "legacy util handler public exact normalization",
                legacyStats.exactResistances.get("subatomic"), 1.0F, 0.2F);
        expectLegacyResistance(problems, "legacy util handler public exact alias normalization",
                legacyStats.exactResistances.get("cmb"), 1.5F, 0.25F);
        expectLegacyResistance(problems, "legacy util handler public category normalization",
                legacyStats.categoryResistances.get(CATEGORY_ENERGY), 2.0F, 0.3F);
        expectResistance(problems, "legacy util handler other conversion",
                legacyStats.modern().otherResistance(), 3.0F, 0.4F);
        expectLegacyResistance(problems, "legacy util handler key match",
                legacyStats.getResistance(ModDamageSources.SUBATOMIC), 1.0F, 0.2F);
        expectLegacyResistance(problems, "legacy util handler string match",
                legacyStats.getResistance("hbm_ntm_rebirth:subatomic"), 1.0F, 0.2F);
        expectLegacyResistance(problems, "legacy util handler damage class match",
                new com.hbm.util.DamageResistanceHandler.ResistanceStats()
                        .addExact(com.hbm.util.DamageResistanceHandler.DamageClass.LASER, 3.0F, 0.3F)
                        .getResistance(com.hbm.util.DamageResistanceHandler.DamageClass.LASER), 3.0F, 0.3F);
        Item facadeItem = net.minecraft.world.item.Items.LEATHER_HELMET;
        com.hbm.util.DamageResistanceHandler.ResistanceStats facadeItemStats =
                new com.hbm.util.DamageResistanceHandler.ResistanceStats().setOther(1.0F, 0.1F);
        com.hbm.util.DamageResistanceHandler.itemStats.put(facadeItem, facadeItemStats);
        expectResistance(problems, "legacy util handler item map put sync",
                itemStats(facadeItem).otherResistance(), 1.0F, 0.1F);
        com.hbm.util.DamageResistanceHandler.itemStats.remove(facadeItem);
        expect(problems, "legacy util handler item map remove sync", itemStats(facadeItem) == null);
        com.hbm.util.DamageResistanceHandler.itemStats.put(facadeItem, facadeItemStats);
        com.hbm.util.DamageResistanceHandler.itemStats.clear();
        expect(problems, "legacy util handler item map clear sync", itemStats(facadeItem) == null);
        com.hbm.util.Tuple.Quartet<Item, Item, Item, Item> facadeSet = new com.hbm.util.Tuple.Quartet<>(
                net.minecraft.world.item.Items.LEATHER_HELMET,
                net.minecraft.world.item.Items.LEATHER_CHESTPLATE,
                net.minecraft.world.item.Items.LEATHER_LEGGINGS,
                net.minecraft.world.item.Items.LEATHER_BOOTS);
        com.hbm.util.DamageResistanceHandler.ResistanceStats facadeSetStats =
                new com.hbm.util.DamageResistanceHandler.ResistanceStats().setOther(2.0F, 0.2F);
        com.hbm.util.DamageResistanceHandler.setStats.put(facadeSet, facadeSetStats);
        expect(problems, "legacy util handler set map put sync",
                setStatsForItem(facadeSet.getW()) != null
                        && setStatsForItem(facadeSet.getW()).otherResistance() != null
                        && nearly(setStatsForItem(facadeSet.getW()).otherResistance().threshold(), 2.0F));
        com.hbm.util.DamageResistanceHandler.setStats.remove(facadeSet);
        expect(problems, "legacy util handler set map remove sync", setStatsForItem(facadeSet.getW()) == null);
        JsonObject legacyConfigStatsJson = new JsonObject();
        JsonArray legacyConfigExact = new JsonArray();
        JsonArray legacyConfigExactEntry = new JsonArray();
        legacyConfigExactEntry.add("subAtomic5");
        legacyConfigExactEntry.add(4.0F);
        legacyConfigExactEntry.add(0.45F);
        legacyConfigExact.add(legacyConfigExactEntry);
        legacyConfigStatsJson.add("exact", legacyConfigExact);
        JsonArray legacyConfigCategory = new JsonArray();
        JsonArray legacyConfigCategoryEntry = new JsonArray();
        legacyConfigCategoryEntry.add("laser");
        legacyConfigCategoryEntry.add(5.0F);
        legacyConfigCategoryEntry.add(0.55F);
        legacyConfigCategory.add(legacyConfigCategoryEntry);
        legacyConfigStatsJson.add("category", legacyConfigCategory);
        JsonArray legacyConfigOther = new JsonArray();
        legacyConfigOther.add(6.0F);
        legacyConfigOther.add(0.65F);
        legacyConfigStatsJson.add("other", legacyConfigOther);
        com.hbm.util.DamageResistanceHandler.ResistanceStats legacyConfigStats =
                com.hbm.util.DamageResistanceHandler.ResistanceStats.deserialize(legacyConfigStatsJson);
        expectLegacyResistance(problems, "legacy util handler deserialize exact normalization",
                legacyConfigStats.exactResistances.get("subatomic"), 4.0F, 0.45F);
        expectLegacyResistance(problems, "legacy util handler deserialize category normalization",
                legacyConfigStats.categoryResistances.get(CATEGORY_ENERGY), 5.0F, 0.55F);
        expectLegacyResistance(problems, "legacy util handler deserialize other",
                legacyConfigStats.otherResistance, 6.0F, 0.65F);
        DamageResistanceStats firstSet = new DamageResistanceStats().setOther(1.0F, 0.1F);
        DamageResistanceStats secondSet = new DamageResistanceStats().setOther(2.0F, 0.2F);
        Item shared = net.minecraft.world.item.Items.IRON_HELMET;
        Item chest = net.minecraft.world.item.Items.IRON_CHESTPLATE;
        Item legs = net.minecraft.world.item.Items.IRON_LEGGINGS;
        Item boots = net.minecraft.world.item.Items.IRON_BOOTS;
        Map<ArmorSet, DamageResistanceStats> auditSetStats = new HashMap<>();
        Map<Item, List<ArmorSet>> auditItemInfoSets = new HashMap<>();
        ArmorSet first = new ArmorSet(shared, chest, legs, boots);
        ArmorSet second = new ArmorSet(shared, net.minecraft.world.item.Items.DIAMOND_CHESTPLATE, legs, boots);
        auditSetStats.put(first, firstSet);
        auditSetStats.put(second, secondSet);
        addSetInfo(auditItemInfoSets, shared, first);
        addSetInfo(auditItemInfoSets, shared, second);
        expect(problems, "item set info keeps registration order",
                setStatsForItem(shared, auditItemInfoSets, auditSetStats) == firstSet);
        ArmorSet partial = new ArmorSet(shared, null, legs, boots);
        auditSetStats.clear();
        auditItemInfoSets.clear();
        auditSetStats.put(partial, firstSet);
        addSetInfo(auditItemInfoSets, shared, partial);
        ArmorSetInfo partialInfo = setInfoForItem(shared, auditItemInfoSets, auditSetStats);
        expect(problems, "partial set info keeps null slot",
                partialInfo != null && partialInfo.chest() == null && partialInfo.legs() == legs);
        return new CoreAudit(List.copyOf(problems));
    }

    public static String typeToCategory(DamageSource source) {
        if (source == null) {
            return "";
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            return CATEGORY_EXPLOSION;
        }
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return CATEGORY_FIRE;
        }
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            return CATEGORY_PHYSICAL;
        }
        if (isEnergy(source)) {
            return CATEGORY_ENERGY;
        }
        if (source.is(DamageTypes.CACTUS) || source.is(ModDamageSources.SPIKES)) {
            return CATEGORY_PHYSICAL;
        }
        if (source.getEntity() != null) {
            return CATEGORY_PHYSICAL;
        }
        return exactTypeKey(source);
    }

    public static String typeToCategory(ResourceKey<DamageType> type) {
        if (type == null) {
            return "";
        }
        if (isExplosion(type)) {
            return CATEGORY_EXPLOSION;
        }
        if (isFireDamage(type)) {
            return CATEGORY_FIRE;
        }
        if (isProjectile(type)) {
            return CATEGORY_PHYSICAL;
        }
        if (isEnergy(type)) {
            return CATEGORY_ENERGY;
        }
        if (type.equals(DamageTypes.CACTUS) || type.equals(ModDamageSources.SPIKES)) {
            return CATEGORY_PHYSICAL;
        }
        if (isPhysicalEntityDamageType(type)) {
            return CATEGORY_PHYSICAL;
        }
        return exactTypeKey(type);
    }

    public static String typeToCategory(String legacyTypeOrId) {
        return ModDamageSources.legacyKey(legacyTypeOrId)
                .map(DamageResistanceHandler::typeToCategory)
                .orElseGet(() -> categoryKey(legacyTypeOrId));
    }

    public static String typeToCategory(DamageClass damageClass) {
        return typeToCategory(ModDamageSources.damageClassKey(damageClass));
    }

    public static String categoryKey(String category) {
        if (category == null || category.isBlank()) {
            return "";
        }
        String normalized = exactTypeKey(category);
        if (normalized.equals("phys") || normalized.equals("physical")) {
            return CATEGORY_PHYSICAL;
        }
        if (normalized.equals("expl") || normalized.equals("explosion") || normalized.equals("explosive")) {
            return CATEGORY_EXPLOSION;
        }
        if (normalized.equals("fire")) {
            return CATEGORY_FIRE;
        }
        if (normalized.equals("en")
                || normalized.equals("energy")
                || normalized.equals("electric")
                || normalized.equals("electricity")
                || normalized.equals("laser")
                || normalized.equals("plasma")
                || normalized.equals("microwave")
                || normalized.equals("subatomic")) {
            return CATEGORY_ENERGY;
        }
        return category;
    }

    public static String categoryKey(DamageClass category) {
        return switch (category) {
            case PHYSICAL -> CATEGORY_PHYSICAL;
            case FIRE -> CATEGORY_FIRE;
            case EXPLOSIVE -> CATEGORY_EXPLOSION;
            case ELECTRIC, PLASMA, LASER, MICROWAVE, SUBATOMIC -> CATEGORY_ENERGY;
            case OTHER -> DamageClass.OTHER.name();
        };
    }

    public static DamageResistanceStats itemStats(Item item) {
        return ITEM_STATS.get(item);
    }

    public static DamageResistanceStats setStatsForItem(Item item) {
        return setStatsForItem(item, ITEM_INFO_SETS, SET_STATS);
    }

    private static DamageResistanceStats setStatsForItem(Item item, Map<Item, List<ArmorSet>> itemInfoSets,
            Map<ArmorSet, DamageResistanceStats> setStats) {
        ArmorSetInfo info = setInfoForItem(item, itemInfoSets, setStats);
        return info == null ? null : info.stats();
    }

    public static ArmorSetInfo setInfoForItem(Item item) {
        return setInfoForItem(item, ITEM_INFO_SETS, SET_STATS);
    }

    private static ArmorSetInfo setInfoForItem(Item item, Map<Item, List<ArmorSet>> itemInfoSets,
            Map<ArmorSet, DamageResistanceStats> setStats) {
        List<ArmorSet> sets = itemInfoSets.get(item);
        if (sets == null) {
            return null;
        }
        for (ArmorSet set : sets) {
            DamageResistanceStats stats = setStats.get(set);
            if (stats != null) {
                return new ArmorSetInfo(set.helmet(), set.chest(), set.legs(), set.boots(), stats);
            }
        }
        return null;
    }

    public static RegistrySnapshot registrySnapshot() {
        return new RegistrySnapshot(ITEM_STATS.size(), SET_STATS.size(), ENTITY_STATS.size(), ENTITY_SIMPLE_NAME_STATS.size());
    }

    public static ArmorBreakdown armorBreakdown(LivingEntity entity) {
        DamageResistanceStats setResistance = SET_STATS.get(ArmorSet.of(entity));
        List<ArmorSlotBreakdown> slots = new ArrayList<>();
        for (EquipmentSlot slot : ArmorSet.ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            slots.add(new ArmorSlotBreakdown(slot, stack.copy(), stack.isEmpty() ? null : ITEM_STATS.get(stack.getItem())));
        }

        DamageResistanceStats innate = ENTITY_STATS.get(entity.getClass());
        String innateKey = entity.getClass().getName();
        EntityStatsMatch match = entityStatsMatch(entity);
        if (match != null) {
            innate = match.stats();
            innateKey = match.kind() + ":" + match.id();
        }
        return new ArmorBreakdown(setResistance, List.copyOf(slots), innateKey, innate);
    }

    public static String exactType(DamageSource source) {
        return source == null ? "" : source.getMsgId().toLowerCase(Locale.US);
    }

    public static String exactTypeKey(DamageSource source) {
        return source == null ? "" : exactTypeKey(source.getMsgId());
    }

    public static String exactTypeKey(ResourceKey<DamageType> type) {
        return type == null ? "" : exactTypeKey(type.location().getPath());
    }

    public static String exactTypeKey(DamageClass damageClass) {
        return exactTypeKey(ModDamageSources.damageType(damageClass));
    }

    @Nullable
    public static String registryTypeKey(DamageSource source) {
        if (source == null) {
            return null;
        }
        return source.typeHolder().unwrapKey()
                .map(DamageResistanceHandler::exactTypeKey)
                .orElse(null);
    }

    public static String exactTypeKey(String type) {
        if (type == null || type.isBlank()) {
            return "";
        }
        String normalized = normalizeExactType(type);
        return EXACT_ALIASES.getOrDefault(normalized, normalized);
    }

    public static boolean isAbsolute(DamageSource source) {
        return source != null && source.is(DamageTypeTags.BYPASSES_RESISTANCE);
    }

    public static boolean isAbsolute(ResourceKey<DamageType> type) {
        return ModDamageSources.legacyDamageType(type)
                .map(ModDamageSources.LegacyDamageType::absolute)
                .orElse(false);
    }

    public static boolean isAbsolute(String legacyTypeOrId) {
        return ModDamageSources.legacyKey(legacyTypeOrId)
                .map(DamageResistanceHandler::isAbsolute)
                .orElse(false);
    }

    public static boolean isAbsolute(DamageClass damageClass) {
        return isAbsolute(ModDamageSources.damageClassKey(damageClass));
    }

    public static boolean isUnblockableForLegacyResistance(DamageSource source) {
        return source != null && source.is(DamageTypeTags.BYPASSES_ARMOR);
    }

    public static boolean isUnblockableForLegacyResistance(ResourceKey<DamageType> type) {
        return ModDamageSources.legacyDamageType(type)
                .map(ModDamageSources.LegacyDamageType::bypassesArmor)
                .orElse(false);
    }

    public static boolean isUnblockableForLegacyResistance(String legacyTypeOrId) {
        return ModDamageSources.legacyKey(legacyTypeOrId)
                .map(DamageResistanceHandler::isUnblockableForLegacyResistance)
                .orElse(false);
    }

    public static boolean isUnblockableForLegacyResistance(DamageClass damageClass) {
        return isUnblockableForLegacyResistance(ModDamageSources.damageClassKey(damageClass));
    }

    private static boolean isEnergy(DamageSource source) {
        if (source == null) {
            return false;
        }
        String type = exactTypeKey(source);
        return type.equals(DamageClass.LASER.name().toLowerCase(Locale.US))
                || type.equals(DamageClass.PLASMA.name().toLowerCase(Locale.US))
                || type.equals(DamageClass.MICROWAVE.name().toLowerCase(Locale.US))
                || type.equals(DamageClass.SUBATOMIC.name().toLowerCase(Locale.US))
                || type.equals(DamageClass.ELECTRIC.name().toLowerCase(Locale.US))
                || source.is(ModDamageSources.ELECTRIC)
                || source.is(ModDamageSources.ELECTRICITY)
                || source.is(ModDamageSources.MICROWAVE)
                || source.is(ModDamageSources.LASER)
                || source.is(ModDamageSources.PLASMA)
                || source.is(ModDamageSources.SUBATOMIC);
    }

    private static boolean isEnergy(ResourceKey<DamageType> type) {
        String exact = exactTypeKey(type);
        return exact.equals(DamageClass.LASER.name().toLowerCase(Locale.US))
                || exact.equals(DamageClass.PLASMA.name().toLowerCase(Locale.US))
                || exact.equals(DamageClass.MICROWAVE.name().toLowerCase(Locale.US))
                || exact.equals(DamageClass.SUBATOMIC.name().toLowerCase(Locale.US))
                || exact.equals(DamageClass.ELECTRIC.name().toLowerCase(Locale.US))
                || type.equals(ModDamageSources.ELECTRIC)
                || type.equals(ModDamageSources.ELECTRICITY)
                || type.equals(ModDamageSources.MICROWAVE)
                || type.equals(ModDamageSources.LASER)
                || type.equals(ModDamageSources.PLASMA)
                || type.equals(ModDamageSources.SUBATOMIC);
    }

    private static boolean isProjectile(ResourceKey<DamageType> type) {
        return ModDamageSources.legacyDamageType(type)
                .map(ModDamageSources.LegacyDamageType::projectile)
                .orElse(false);
    }

    private static boolean isExplosion(ResourceKey<DamageType> type) {
        return ModDamageSources.legacyDamageType(type)
                .map(ModDamageSources.LegacyDamageType::explosion)
                .orElse(false);
    }

    private static boolean isFireDamage(ResourceKey<DamageType> type) {
        return ModDamageSources.legacyDamageType(type)
                .map(ModDamageSources.LegacyDamageType::fire)
                .orElse(false);
    }

    private static boolean isPhysicalEntityDamageType(ResourceKey<DamageType> type) {
        if (type == null || !"minecraft".equals(type.location().getNamespace())) {
            return false;
        }
        return switch (type.location().getPath()) {
            case "player_attack", "mob_attack", "mob_attack_no_aggro", "arrow", "trident", "thrown",
                    "mob_projectile", "sting" -> true;
            default -> false;
        };
    }

    private static DamageSource sourceFor(LivingEntity entity, ResourceKey<DamageType> type) {
        if (entity == null) {
            throw new IllegalArgumentException("A living entity is required to create damage source " + type.location());
        }
        return ModDamageSources.source(entity.level(), type);
    }

    private static DamageSource sourceFor(LivingEntity entity, DamageClass damageClass) {
        if (entity == null) {
            throw new IllegalArgumentException("A living entity is required to create damage source " + damageClass);
        }
        return ModDamageSources.source(entity.level(), damageClass);
    }

    private static DamageSource sourceFor(LivingEntity entity, String legacyTypeOrId) {
        if (entity == null) {
            throw new IllegalArgumentException("A living entity is required to create damage source " + legacyTypeOrId);
        }
        return ModDamageSources.source(entity.level(), legacyTypeOrId);
    }

    private static String normalizeExactType(String type) {
        String lower = type.toLowerCase(Locale.US);
        StringBuilder builder = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (c != '_' && c != '-' && c != '.') {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static Map<String, String> createExactAliases() {
        Map<String, String> aliases = new HashMap<>();
        alias(aliases, "onfire", "onfire");
        alias(aliases, "on_fire", "onfire");
        alias(aliases, "infire", "infire");
        alias(aliases, "in_fire", "infire");
        alias(aliases, "acidPlayer", "acidplayer");
        alias(aliases, "acid_player", "acidplayer");
        alias(aliases, "tauBlast", "taublast");
        alias(aliases, "tau_blast", "taublast");
        alias(aliases, "euthanizedSelf", "euthanizedself");
        alias(aliases, "euthanized_self", "euthanizedself");
        alias(aliases, "euthanizedSelf2", "euthanizedself2");
        alias(aliases, "euthanized_self2", "euthanizedself2");
        alias(aliases, "revolverBullet", "revolverbullet");
        alias(aliases, "revolver_bullet", "revolverbullet");
        alias(aliases, "chopperBullet", "chopperbullet");
        alias(aliases, "chopper_bullet", "chopperbullet");
        alias(aliases, "combineBall", "cmb");
        alias(aliases, "combine_ball", "cmb");
        alias(aliases, "cmb", "cmb");
        alias(aliases, "nuclearBlast", "nuclearblast");
        alias(aliases, "nuclear_blast", "nuclearblast");
        alias(aliases, "mudPoisoning", "mudpoisoning");
        alias(aliases, "mud_poisoning", "mudpoisoning");
        alias(aliases, "amsCore", "amscore");
        alias(aliases, "ams_core", "amscore");
        alias(aliases, "subAtomic", "subatomic");
        alias(aliases, "sub_atomic", "subatomic");
        alias(aliases, "subAtomic1", "subatomic");
        alias(aliases, "subAtomic2", "subatomic");
        alias(aliases, "subAtomic3", "subatomic");
        alias(aliases, "subAtomic4", "subatomic");
        alias(aliases, "subAtomic5", "subatomic");
        alias(aliases, "electrified", "electricity");
        alias(aliases, "euthanized", "euthanized");
        return aliases;
    }

    private static void alias(Map<String, String> aliases, String key, String canonical) {
        aliases.put(normalizeExactType(key), normalizeExactType(canonical));
    }

    private static void expect(List<String> problems, String label, boolean ok) {
        if (!ok) {
            problems.add(label);
        }
    }

    private static void expectResistance(List<String> problems, String label, @Nullable DamageResistance actual,
            float threshold, float resistance) {
        expect(problems, label, actual != null
                && nearly(actual.threshold(), threshold)
                && nearly(actual.resistance(), resistance));
    }

    private static void expectLegacyResistance(List<String> problems, String label,
            @Nullable com.hbm.util.DamageResistanceHandler.Resistance actual, float threshold, float resistance) {
        expect(problems, label, actual != null
                && nearly(actual.threshold, threshold)
                && nearly(actual.resistance, resistance));
    }

    private static void expectMatch(List<String> problems, String label, @Nullable DamageResistanceStats.ResistanceMatch actual,
            String kind, String key, float threshold, float resistance) {
        expect(problems, label, actual != null
                && actual.kind().equals(kind)
                && actual.key().equals(key)
                && nearly(actual.resistance().threshold(), threshold)
                && nearly(actual.resistance().resistance(), resistance));
    }

    private static boolean nearly(float actual, float expected) {
        return Math.abs(actual - expected) < 0.0001F;
    }

    private static String itemId(@Nullable Item item) {
        if (item == null) {
            return "empty";
        }
        var key = ForgeRegistries.ITEMS.getKey(item);
        return key == null ? HbmNtm.MOD_ID + ":unknown" : key.toString();
    }

    private static String describeArmorSet(ArmorSet set) {
        return itemId(set.helmet()) + ", " + itemId(set.chest()) + ", " + itemId(set.legs()) + ", " + itemId(set.boots());
    }

    private static void addSetInfo(@Nullable Item item, ArmorSet set) {
        addSetInfo(ITEM_INFO_SETS, item, set);
    }

    private static void addSetInfo(Map<Item, List<ArmorSet>> itemInfoSets, @Nullable Item item, ArmorSet set) {
        if (item != null) {
            itemInfoSets.computeIfAbsent(item, ignored -> new ArrayList<>()).add(set);
        }
    }

    private static void removeSetInfo(@Nullable Item item, ArmorSet set) {
        if (item == null) {
            return;
        }
        List<ArmorSet> sets = ITEM_INFO_SETS.get(item);
        if (sets == null) {
            return;
        }
        sets.remove(set);
        if (sets.isEmpty()) {
            ITEM_INFO_SETS.remove(item);
        }
    }

    private static EntityStatsMatch entityStatsMatch(LivingEntity entity) {
        DamageResistanceStats exact = ENTITY_STATS.get(entity.getClass());
        if (exact != null) {
            return new EntityStatsMatch("class", entity.getClass().getName(), exact);
        }
        DamageResistanceStats simple = ENTITY_SIMPLE_NAME_STATS.get(entity.getClass().getSimpleName());
        if (simple != null) {
            return new EntityStatsMatch("simpleName", entity.getClass().getSimpleName(), simple);
        }
        EntityStatsMatch best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Map.Entry<Class<? extends Entity>, DamageResistanceStats> entry : ENTITY_STATS.entrySet()) {
            if (entry.getKey().isAssignableFrom(entity.getClass())) {
                int distance = inheritanceDistance(entity.getClass(), entry.getKey());
                if (distance < bestDistance
                        || (distance == bestDistance && best != null && entry.getKey().getName().compareTo(best.id()) < 0)) {
                    bestDistance = distance;
                    best = new EntityStatsMatch("assignable", entry.getKey().getName(), entry.getValue());
                }
            }
        }
        return best;
    }

    private static int inheritanceDistance(Class<?> actual, Class<?> registered) {
        if (actual.equals(registered)) {
            return 0;
        }
        int distance = 0;
        Class<?> current = actual;
        while (current != null) {
            if (current.equals(registered)) {
                return distance;
            }
            current = current.getSuperclass();
            distance++;
        }
        return registered.isInterface() && registered.isAssignableFrom(actual) ? distance + 100 : Integer.MAX_VALUE;
    }

    private record ArmorSet(Item helmet, Item chest, Item legs, Item boots) {
        private static final EquipmentSlot[] ARMOR_SLOTS = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        private static ArmorSet of(LivingEntity entity) {
            return new ArmorSet(
                    item(entity, EquipmentSlot.HEAD),
                    item(entity, EquipmentSlot.CHEST),
                    item(entity, EquipmentSlot.LEGS),
                    item(entity, EquipmentSlot.FEET));
        }

        private static Item item(LivingEntity entity, EquipmentSlot slot) {
            ItemStack stack = entity.getItemBySlot(slot);
            return stack.isEmpty() ? null : stack.getItem();
        }

    }

    public record ResistanceBreakdown(String exactType, String category, boolean absolute, float rawDt, float rawDr,
                                      float effectiveDt, float effectiveDr, float finalDamage) {
        public boolean fullyAbsorbed(float amount) {
            return !absolute && ((effectiveDt > 0.0F && effectiveDt >= amount) || effectiveDr >= 1.0F);
        }
    }

    public record DamageFormula(float effectiveDt, float effectiveDr, float finalDamage) {
    }

    public record CoreAudit(List<String> problems) {
        public boolean passed() {
            return problems.isEmpty();
        }
    }

    public record RegistrySnapshot(int itemStats, int setStats, int entityClassStats, int entitySimpleNameStats) {
        public int entityStats() {
            return entityClassStats + entitySimpleNameStats;
        }
    }

    public record ArmorBreakdown(DamageResistanceStats setStats, List<ArmorSlotBreakdown> slots,
                                 String innateKey, DamageResistanceStats innateStats) {
    }

    public record ArmorSlotBreakdown(EquipmentSlot slot, ItemStack stack, DamageResistanceStats itemStats) {
    }

    public record ArmorSetInfo(@Nullable Item helmet, @Nullable Item chest, @Nullable Item legs, @Nullable Item boots,
                               DamageResistanceStats stats) {
        public List<Item> nonNullItems() {
            List<Item> items = new ArrayList<>(4);
            if (helmet != null) {
                items.add(helmet);
            }
            if (chest != null) {
                items.add(chest);
            }
            if (legs != null) {
                items.add(legs);
            }
            if (boots != null) {
                items.add(boots);
            }
            return List.copyOf(items);
        }
    }

    public record ResistanceContribution(String source, String id, String matchKind, String matchKey,
                                         float threshold, float resistance) {
    }

    public record PierceState(float pierceDt, float pierceDr) {
        public static final PierceState NONE = new PierceState(0.0F, 0.0F);
    }

    private record EntityStatsMatch(String kind, String id, DamageResistanceStats stats) {
    }

    private record ResistanceContributionTotals(List<ResistanceContribution> contributions) {
        private float threshold() {
            float value = 0.0F;
            for (ResistanceContribution contribution : contributions) {
                value += contribution.threshold();
            }
            return value;
        }

        private float resistance() {
            float value = 0.0F;
            for (ResistanceContribution contribution : contributions) {
                value += contribution.resistance();
            }
            return value;
        }
    }

    private DamageResistanceHandler() {
    }
}
