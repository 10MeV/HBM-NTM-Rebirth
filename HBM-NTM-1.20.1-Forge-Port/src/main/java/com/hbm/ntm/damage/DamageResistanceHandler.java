package com.hbm.ntm.damage;

import com.hbm.ntm.api.entity.ResistanceProvider;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
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

    private static float currentPierceDt;
    private static float currentPierceDr;

    private static final Map<Item, DamageResistanceStats> ITEM_STATS = new HashMap<>();
    private static final Map<ArmorSet, DamageResistanceStats> SET_STATS = new HashMap<>();
    private static final Map<Class<? extends Entity>, DamageResistanceStats> ENTITY_STATS = new HashMap<>();
    private static final Map<String, DamageResistanceStats> ENTITY_SIMPLE_NAME_STATS = new HashMap<>();
    private static final Map<String, String> EXACT_ALIASES = createExactAliases();

    static {
        registerEntity(Creeper.class, new DamageResistanceStats().addCategory(CATEGORY_EXPLOSION, 2.0F, 0.25F));
    }

    public static void clear() {
        ITEM_STATS.clear();
        SET_STATS.clear();
        ENTITY_STATS.clear();
        ENTITY_SIMPLE_NAME_STATS.clear();
        registerEntity(Creeper.class, new DamageResistanceStats().addCategory(CATEGORY_EXPLOSION, 2.0F, 0.25F));
    }

    public static void setup(float pierceDt, float pierceDr) {
        currentPierceDt = pierceDt;
        currentPierceDr = pierceDr;
    }

    public static void reset() {
        currentPierceDt = 0.0F;
        currentPierceDr = 0.0F;
    }

    public static float currentPierceDt() {
        return currentPierceDt;
    }

    public static float currentPierceDr() {
        return currentPierceDr;
    }

    public static void registerItem(Item item, DamageResistanceStats stats) {
        ITEM_STATS.put(item, stats);
    }

    public static void registerSet(Item helmet, Item chest, Item legs, Item boots, DamageResistanceStats stats) {
        SET_STATS.put(new ArmorSet(helmet, chest, legs, boots), stats);
    }

    public static void registerEntity(Class<? extends Entity> entityClass, DamageResistanceStats stats) {
        ENTITY_STATS.put(entityClass, stats);
    }

    public static void registerEntitySimpleName(String simpleName, DamageResistanceStats stats) {
        ENTITY_SIMPLE_NAME_STATS.put(simpleName, stats);
    }

    public static float calculateDamage(LivingEntity entity, DamageSource source, float amount) {
        return calculateDamage(entity, source, amount, currentPierceDt, currentPierceDr);
    }

    public static float calculateDamage(LivingEntity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        return breakdown(entity, source, amount, pierceDt, pierceDr).finalDamage();
    }

    public static float[] getDtDr(LivingEntity entity, DamageSource source, float amount) {
        return getDtDr(entity, source, amount, currentPierceDt, currentPierceDr);
    }

    public static float[] getDtDr(LivingEntity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        ResistanceContributionTotals totals = collectResistanceContributions(entity, source, amount, pierceDt, pierceDr);
        return new float[] { totals.threshold(), totals.resistance() };
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity, DamageSource source, float amount) {
        return resistanceContributions(entity, source, amount, currentPierceDt, currentPierceDr);
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity, DamageSource source, float amount,
            float pierceDt, float pierceDr) {
        return collectResistanceContributions(entity, source, amount, pierceDt, pierceDr).contributions();
    }

    private static ResistanceContributionTotals collectResistanceContributions(LivingEntity entity, DamageSource source,
            float amount, float pierceDt, float pierceDr) {
        List<ResistanceContribution> contributions = new ArrayList<>();

        if (entity instanceof ResistanceProvider provider) {
            float[] provided = provider.getCurrentDtDr(source, amount, pierceDt, pierceDr);
            if (provided != null && provided.length >= 2) {
                addContribution(contributions, "provider", entity.getClass().getName(), "provided", "provider",
                        new DamageResistance(provided[0], provided[1]));
            }
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
        if (innate == null) {
            innate = ENTITY_SIMPLE_NAME_STATS.get(entity.getClass().getSimpleName());
            innateId = entity.getClass().getSimpleName();
        }
        if (innate != null) {
            DamageResistanceStats.ResistanceMatch match = innate.match(source);
            if (match != null) {
                addContribution(contributions, "entity", innateId, match.kind(), match.key(), match.resistance());
            }
        }

        return new ResistanceContributionTotals(List.copyOf(contributions));
    }

    private static void addContribution(List<ResistanceContribution> contributions, String source, String id,
            String matchKind, String matchKey, DamageResistance resistance) {
        if (resistance.threshold() == 0.0F && resistance.resistance() == 0.0F) {
            return;
        }
        contributions.add(new ResistanceContribution(source, id, matchKind, matchKey, resistance.threshold(), resistance.resistance()));
    }

    public static ResistanceBreakdown breakdown(LivingEntity entity, DamageSource source, float amount) {
        return breakdown(entity, source, amount, currentPierceDt, currentPierceDr);
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
        float effectiveDt = Math.max(0.0F, rawDt - pierceDt);
        float effectiveDr = Mth.clamp(rawDr * Mth.clamp(1.0F - pierceDr, 0.0F, 2.0F), 0.0F, 1.0F);
        float finalDamage = effectiveDt >= amount ? 0.0F : Math.max(0.0F, (amount - effectiveDt) * (1.0F - effectiveDr));
        return new ResistanceBreakdown(exactType, category, false, rawDt, rawDr, effectiveDt, effectiveDr, finalDamage);
    }

    public static String typeToCategory(DamageSource source) {
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

    public static DamageResistanceStats itemStats(Item item) {
        return ITEM_STATS.get(item);
    }

    public static DamageResistanceStats setStatsForItem(Item item) {
        for (Map.Entry<ArmorSet, DamageResistanceStats> entry : SET_STATS.entrySet()) {
            if (entry.getKey().contains(item)) {
                return entry.getValue();
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
        if (innate == null) {
            innate = ENTITY_SIMPLE_NAME_STATS.get(entity.getClass().getSimpleName());
            innateKey = entity.getClass().getSimpleName();
        }
        return new ArmorBreakdown(setResistance, List.copyOf(slots), innateKey, innate);
    }

    public static String exactType(DamageSource source) {
        return source.getMsgId().toLowerCase(Locale.US);
    }

    public static String exactTypeKey(DamageSource source) {
        return exactTypeKey(source.getMsgId());
    }

    public static String exactTypeKey(String type) {
        String normalized = normalizeExactType(type);
        return EXACT_ALIASES.getOrDefault(normalized, normalized);
    }

    public static boolean isAbsolute(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_RESISTANCE);
    }

    public static boolean isUnblockableForLegacyResistance(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    private static boolean isEnergy(DamageSource source) {
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

        private boolean contains(Item item) {
            return helmet == item || chest == item || legs == item || boots == item;
        }
    }

    public record ResistanceBreakdown(String exactType, String category, boolean absolute, float rawDt, float rawDr,
                                      float effectiveDt, float effectiveDr, float finalDamage) {
        public boolean fullyAbsorbed(float amount) {
            return !absolute && ((effectiveDt > 0.0F && effectiveDt >= amount) || effectiveDr >= 1.0F);
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

    public record ResistanceContribution(String source, String id, String matchKind, String matchKey,
                                         float threshold, float resistance) {
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
