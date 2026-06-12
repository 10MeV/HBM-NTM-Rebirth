package com.hbm.ntm.damage;

import com.hbm.ntm.api.entity.ResistanceProvider;
import com.hbm.ntm.HbmNtm;
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

    public static void registerSet(Item helmet, Item chest, Item legs, Item boots, DamageResistanceStats stats) {
        ArmorSet set = new ArmorSet(helmet, chest, legs, boots);
        SET_STATS.put(set, stats);
        addSetInfo(helmet, set);
        addSetInfo(chest, set);
        addSetInfo(legs, set);
        addSetInfo(boots, set);
    }

    public static void registerEntity(Class<? extends Entity> entityClass, DamageResistanceStats stats) {
        ENTITY_STATS.put(entityClass, stats);
    }

    public static void registerEntitySimpleName(String simpleName, DamageResistanceStats stats) {
        ENTITY_SIMPLE_NAME_STATS.put(simpleName, stats);
    }

    public static float calculateDamage(LivingEntity entity, DamageSource source, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return calculateDamage(entity, source, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float calculateDamage(LivingEntity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        return breakdown(entity, source, amount, pierceDt, pierceDr).finalDamage();
    }

    public static float[] getDtDr(LivingEntity entity, DamageSource source, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return getDtDr(entity, source, amount, piercing.pierceDt(), piercing.pierceDr());
    }

    public static float[] getDtDr(LivingEntity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        ResistanceContributionTotals totals = collectResistanceContributions(entity, source, amount, pierceDt, pierceDr);
        return new float[] { totals.threshold(), totals.resistance() };
    }

    public static List<ResistanceContribution> resistanceContributions(LivingEntity entity, DamageSource source, float amount) {
        PierceState piercing = CURRENT_PIERCING.get();
        return resistanceContributions(entity, source, amount, piercing.pierceDt(), piercing.pierceDr());
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
        expect(problems, "legacy util handler category bridge",
                com.hbm.util.DamageResistanceHandler.CATEGORY_ENERGY.equals(CATEGORY_ENERGY)
                        && com.hbm.util.DamageResistanceHandler.categoryKey(
                                com.hbm.util.DamageResistanceHandler.DamageClass.LASER).equals(CATEGORY_ENERGY));
        com.hbm.util.DamageResistanceHandler.ResistanceStats legacyStats =
                new com.hbm.util.DamageResistanceHandler.ResistanceStats()
                        .addExact("subAtomic2", 1.0F, 0.2F)
                        .addCategory(com.hbm.util.DamageResistanceHandler.DamageClass.ELECTRIC, 2.0F, 0.3F)
                        .setOther(3.0F, 0.4F);
        expectResistance(problems, "legacy util handler exact normalization",
                legacyStats.modern().exactResistances().get("subatomic"), 1.0F, 0.2F);
        expectResistance(problems, "legacy util handler category normalization",
                legacyStats.modern().categoryResistances().get(CATEGORY_ENERGY), 2.0F, 0.3F);
        expectResistance(problems, "legacy util handler other conversion",
                legacyStats.modern().otherResistance(), 3.0F, 0.4F);
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

    public static String categoryKey(String category) {
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
        return source.getMsgId().toLowerCase(Locale.US);
    }

    public static String exactTypeKey(DamageSource source) {
        return exactTypeKey(source.getMsgId());
    }

    public static String exactTypeKey(ResourceKey<DamageType> type) {
        return exactTypeKey(type.location().getPath());
    }

    @Nullable
    public static String registryTypeKey(DamageSource source) {
        return source.typeHolder().unwrapKey()
                .map(DamageResistanceHandler::exactTypeKey)
                .orElse(null);
    }

    public static String exactTypeKey(String type) {
        String normalized = normalizeExactType(type);
        return EXACT_ALIASES.getOrDefault(normalized, normalized);
    }

    public static boolean isAbsolute(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_RESISTANCE);
    }

    public static boolean isUnblockableForLegacyResistance(DamageSource source) {
        return source.is(DamageTypeTags.BYPASSES_ARMOR);
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
