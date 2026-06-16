package com.hbm.ntm.radiation;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.damage.DamageClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ModDamageSources {
    public static final ResourceKey<DamageType> NUCLEAR_BLAST = key("nuclear_blast");
    public static final ResourceKey<DamageType> MUD_POISONING = key("mud_poisoning");
    public static final ResourceKey<DamageType> ACID = key("acid");
    public static final ResourceKey<DamageType> EUTHANIZED = key("euthanized");
    public static final ResourceKey<DamageType> EUTHANIZED_SELF = key("euthanized_self");
    public static final ResourceKey<DamageType> EUTHANIZED_SELF2 = key("euthanized_self2");
    public static final ResourceKey<DamageType> TAU_BLAST = key("tau_blast");
    public static final ResourceKey<DamageType> RADIATION = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "radiation"));
    public static final ResourceKey<DamageType> DIGAMMA = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "digamma"));
    public static final ResourceKey<DamageType> MKU = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "mku"));
    public static final ResourceKey<DamageType> ASBESTOS = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "asbestos"));
    public static final ResourceKey<DamageType> BLACK_LUNG = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "blacklung"));
    public static final ResourceKey<DamageType> SUICIDE = key("suicide");
    public static final ResourceKey<DamageType> EXPLOSION = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "explosion"));
    public static final ResourceKey<DamageType> BLENDER = key("blender");
    public static final ResourceKey<DamageType> METEORITE = key("meteorite");
    public static final ResourceKey<DamageType> BOXCAR = key("boxcar");
    public static final ResourceKey<DamageType> BOAT = key("boat");
    public static final ResourceKey<DamageType> BUILDING = key("building");
    public static final ResourceKey<DamageType> MONOXIDE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "monoxide"));
    public static final ResourceKey<DamageType> PC = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "pc"));
    public static final ResourceKey<DamageType> CLOUD = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "cloud"));
    public static final ResourceKey<DamageType> TAINT = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "taint"));
    public static final ResourceKey<DamageType> AMS = key("ams");
    public static final ResourceKey<DamageType> AMS_CORE = key("ams_core");
    public static final ResourceKey<DamageType> BROADCAST = key("broadcast");
    public static final ResourceKey<DamageType> BANG = key("bang");
    public static final ResourceKey<DamageType> LEAD = key("lead");
    public static final ResourceKey<DamageType> ENERVATION = key("enervation");
    public static final ResourceKey<DamageType> ELECTRIC = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "electric"));
    public static final ResourceKey<DamageType> ELECTRICITY = key("electricity");
    public static final ResourceKey<DamageType> EXHAUST = key("exhaust");
    public static final ResourceKey<DamageType> SPIKES = key("spikes");
    public static final ResourceKey<DamageType> LUNAR = key("lunar");
    public static final ResourceKey<DamageType> SHRAPNEL = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "shrapnel"));
    public static final ResourceKey<DamageType> RUBBLE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "rubble"));
    public static final ResourceKey<DamageType> BLACKHOLE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "blackhole"));
    public static final ResourceKey<DamageType> VACUUM = key("vacuum");
    public static final ResourceKey<DamageType> OVERDOSE = key("overdose");
    public static final ResourceKey<DamageType> MICROWAVE = key("microwave");
    public static final ResourceKey<DamageType> NITAN = key("nitan");
    public static final ResourceKey<DamageType> LASER = key("laser");
    public static final ResourceKey<DamageType> PLASMA = key("plasma");
    public static final ResourceKey<DamageType> SUBATOMIC = key("subatomic");
    public static final ResourceKey<DamageType> SUBATOMIC_1 = key("subatomic_1");
    public static final ResourceKey<DamageType> SUBATOMIC_2 = key("subatomic_2");
    public static final ResourceKey<DamageType> SUBATOMIC_3 = key("subatomic_3");
    public static final ResourceKey<DamageType> SUBATOMIC_4 = key("subatomic_4");
    public static final ResourceKey<DamageType> SUBATOMIC_5 = key("subatomic_5");
    public static final ResourceKey<DamageType> REVOLVER_BULLET = key("revolver_bullet");
    public static final ResourceKey<DamageType> CHOPPER_BULLET = key("chopper_bullet");
    public static final ResourceKey<DamageType> TAU = key("tau");
    public static final ResourceKey<DamageType> COMBINE_BALL = key("combine_ball");
    public static final ResourceKey<DamageType> ACID_PLAYER = key("acid_player");
    public static final ResourceKey<DamageType> BOIL = key("boil");
    public static final ResourceKey<DamageType> ICE = key("ice");
    public static final ResourceKey<DamageType> FLAMETHROWER = key("flamethrower");

    private static final List<LegacyDamageType> LEGACY_DAMAGE_TYPES = List.of(
            legacy(NUCLEAR_BLAST, false, true, false, false, false, false),
            legacy(MUD_POISONING, false, false, false, true, false, false),
            legacy(ACID, false, false, false, false, false, false),
            legacy(EUTHANIZED, false, false, false, true, false, false),
            legacy(EUTHANIZED_SELF, false, false, false, true, false, false),
            legacy(EUTHANIZED_SELF2, false, false, false, true, false, false),
            legacy(TAU_BLAST, false, true, false, true, false, false),
            legacy(RADIATION, false, false, false, true, false, false),
            legacy(DIGAMMA, false, false, false, true, true, true),
            legacy(MKU, false, false, false, true, true, false),
            legacy(ASBESTOS, false, false, false, true, true, false),
            legacy(BLACK_LUNG, false, false, false, true, true, false),
            legacy(SUICIDE, true, false, false, false, false, false),
            legacy(EXPLOSION, false, true, false, false, false, false),
            legacy(BLENDER, false, false, false, true, true, false),
            legacy(METEORITE, false, false, false, true, true, false),
            legacy(BOXCAR, false, false, false, true, true, false),
            legacy(BOAT, false, false, false, true, true, false),
            legacy(BUILDING, false, false, false, true, true, false),
            legacy(MONOXIDE, false, false, false, true, true, false),
            legacy(PC, false, false, false, true, true, false),
            legacy(CLOUD, false, false, false, true, true, false),
            legacy(TAINT, false, false, false, true, true, false),
            legacy(AMS, false, false, false, true, true, false),
            legacy(AMS_CORE, false, false, false, true, true, false),
            legacy(BROADCAST, false, false, false, true, true, false),
            legacy(BANG, false, false, false, true, true, false),
            legacy(LEAD, false, false, false, true, true, false),
            legacy(ENERVATION, false, false, false, true, true, false),
            legacy(ELECTRIC, false, false, false, false, false, false),
            legacy(ELECTRICITY, false, false, false, true, true, false),
            legacy(EXHAUST, false, false, false, true, true, false),
            legacy(SPIKES, false, false, false, true, false, false),
            legacy(LUNAR, false, false, false, true, true, false),
            legacy(SHRAPNEL, true, false, false, false, false, false),
            legacy(RUBBLE, true, false, false, false, false, false),
            legacy(BLACKHOLE, false, false, false, true, true, false),
            legacy(VACUUM, false, false, false, true, true, false),
            legacy(OVERDOSE, false, false, false, true, true, false),
            legacy(MICROWAVE, false, false, false, true, true, false),
            legacy(NITAN, false, false, false, true, true, true),
            legacy(LASER, false, false, false, false, false, false),
            legacy(PLASMA, false, false, true, false, false, false),
            legacy(SUBATOMIC, true, false, false, true, false, false),
            legacy(SUBATOMIC_1, true, false, false, true, false, false),
            legacy(SUBATOMIC_2, true, false, false, true, false, false),
            legacy(SUBATOMIC_3, true, false, false, true, false, false),
            legacy(SUBATOMIC_4, true, false, false, true, false, false),
            legacy(SUBATOMIC_5, true, false, false, true, false, false),
            legacy(REVOLVER_BULLET, true, false, false, false, false, false),
            legacy(CHOPPER_BULLET, true, false, false, false, false, false),
            legacy(TAU, true, false, false, true, false, false),
            legacy(COMBINE_BALL, true, false, false, true, false, false),
            legacy(ACID_PLAYER, false, false, false, false, false, false),
            legacy(BOIL, false, false, true, false, false, false),
            legacy(ICE, false, false, false, false, false, false),
            legacy(FLAMETHROWER, false, false, true, false, false, false)
    );
    private static final Map<String, ResourceKey<DamageType>> LEGACY_DAMAGE_KEYS = createLegacyDamageKeys();
    private static final Map<ResourceKey<DamageType>, List<String>> LEGACY_DAMAGE_ALIASES = createLegacyDamageAliases();

    public static DamageSource radiation(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RADIATION));
    }

    public static DamageSource digamma(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DIGAMMA));
    }

    public static DamageSource mku(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MKU));
    }

    public static DamageSource asbestos(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ASBESTOS));
    }

    public static DamageSource blackLung(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BLACK_LUNG));
    }

    public static DamageSource monoxide(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MONOXIDE));
    }

    public static DamageSource pc(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PC));
    }

    public static DamageSource cloud(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CLOUD));
    }

    public static DamageSource taint(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TAINT));
    }

    public static DamageSource electric(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ELECTRIC));
    }

    public static DamageSource shrapnel(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SHRAPNEL));
    }

    public static DamageSource rubble(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RUBBLE));
    }

    public static DamageSource bullet(Level level, Entity direct, @Nullable Entity cause) {
        return indirect(level, REVOLVER_BULLET, direct, cause);
    }

    public static DamageSource displacement(Level level, Entity direct, @Nullable Entity cause) {
        return indirect(level, CHOPPER_BULLET, direct, cause);
    }

    public static DamageSource tau(Level level, Entity direct, @Nullable Entity cause) {
        return indirect(level, TAU, direct, cause);
    }

    public static DamageSource combineBall(Level level, Entity direct, @Nullable Entity cause) {
        return indirect(level, COMBINE_BALL, direct, cause);
    }

    public static DamageSource subatomic(Level level, Entity direct, @Nullable Entity cause) {
        return indirect(level, randomSubatomic(level), direct, cause);
    }

    public static DamageSource euthanized(Level level, Entity direct, @Nullable Entity cause) {
        return indirect(level, EUTHANIZED, direct, cause);
    }

    public static DamageSource blackhole(Level level, @Nullable Entity source) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BLACKHOLE), source);
    }

    public static DamageSource explosion(Level level, @Nullable Entity source) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(EXPLOSION), source);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
    }

    public static DamageSource source(Level level, String legacyTypeOrId) {
        ResourceKey<DamageType> type = legacyKey(legacyTypeOrId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown damage type: " + legacyTypeOrId));
        return source(level, type);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, @Nullable Entity source) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), source);
    }

    public static DamageSource source(Level level, String legacyTypeOrId, @Nullable Entity source) {
        ResourceKey<DamageType> type = legacyKey(legacyTypeOrId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown damage type: " + legacyTypeOrId));
        return source(level, type, source);
    }

    public static DamageSource indirect(Level level, ResourceKey<DamageType> type, Entity direct, @Nullable Entity cause) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), direct, cause);
    }

    public static DamageSource indirect(Level level, String legacyTypeOrId, Entity direct, @Nullable Entity cause) {
        ResourceKey<DamageType> type = legacyKey(legacyTypeOrId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown damage type: " + legacyTypeOrId));
        return indirect(level, type, direct, cause);
    }

    public static List<LegacyDamageType> legacyDamageTypes() {
        return LEGACY_DAMAGE_TYPES;
    }

    public static Optional<ResourceKey<DamageType>> legacyKey(String legacyTypeOrId) {
        if (legacyTypeOrId == null || legacyTypeOrId.isBlank()) {
            return Optional.empty();
        }

        String trimmed = legacyTypeOrId.trim();
        if (trimmed.contains(":")) {
            String[] parts = trimmed.split(":", 2);
            String namespace = parts[0].toLowerCase(Locale.ROOT);
            String path = parts.length > 1 ? parts[1] : "";
            ResourceKey<DamageType> key = LEGACY_DAMAGE_KEYS.get(normalizeAlias(path));
            if (key != null && key.location().getNamespace().equals(namespace)) {
                return Optional.of(key);
            }
            return parseDamageTypeId(namespace + ":" + path.toLowerCase(Locale.ROOT));
        }

        ResourceKey<DamageType> legacyKey = LEGACY_DAMAGE_KEYS.get(normalizeAlias(trimmed));
        if (legacyKey != null) {
            return Optional.of(legacyKey);
        }
        return parseDamageTypeId(trimmed.toLowerCase(Locale.ROOT));
    }

    public static List<String> legacyAliases(ResourceKey<DamageType> key) {
        return LEGACY_DAMAGE_ALIASES.getOrDefault(key, List.of());
    }

    public static Optional<LegacyDamageType> legacyDamageType(ResourceKey<DamageType> key) {
        for (LegacyDamageType legacy : LEGACY_DAMAGE_TYPES) {
            if (legacy.key().equals(key)) {
                return Optional.of(legacy);
            }
        }
        return Optional.empty();
    }

    public static Optional<LegacyDamageType> legacyDamageType(String legacyTypeOrId) {
        return legacyKey(legacyTypeOrId).flatMap(ModDamageSources::legacyDamageType);
    }

    public static Optional<LegacyDamageType> legacyDamageType(DamageSource source) {
        if (source == null) {
            return Optional.empty();
        }
        for (LegacyDamageType legacy : LEGACY_DAMAGE_TYPES) {
            if (source.is(legacy.key())) {
                return Optional.of(legacy);
            }
        }
        return legacyKey(source.getMsgId()).flatMap(ModDamageSources::legacyDamageType);
    }

    public static List<String> expectedTagLabels(ResourceKey<DamageType> key) {
        return legacyDamageType(key)
                .map(LegacyDamageType::expectedTagLabels)
                .orElse(List.of());
    }

    public static List<String> expectedTagLabels(String legacyTypeOrId) {
        return legacyDamageType(legacyTypeOrId)
                .map(LegacyDamageType::expectedTagLabels)
                .orElse(List.of());
    }

    public static ResourceKey<DamageType> damageClassKey(DamageClass damageClass) {
        return switch (damageClass == null ? DamageClass.OTHER : damageClass) {
            case PHYSICAL -> REVOLVER_BULLET;
            case FIRE -> FLAMETHROWER;
            case EXPLOSIVE -> EXPLOSION;
            case ELECTRIC -> ELECTRIC;
            case PLASMA -> PLASMA;
            case LASER -> LASER;
            case MICROWAVE -> MICROWAVE;
            case SUBATOMIC -> SUBATOMIC;
            case OTHER -> minecraft("generic");
        };
    }

    public static DamageSource source(Level level, DamageClass damageClass) {
        return source(level, damageClassKey(damageClass));
    }

    public static DamageSource source(Level level, DamageClass damageClass, @Nullable Entity source) {
        return source(level, damageClassKey(damageClass), source);
    }

    public static DamageSource indirect(Level level, DamageClass damageClass, Entity direct, @Nullable Entity cause) {
        return indirect(level, damageClassKey(damageClass), direct, cause);
    }

    public static List<String> expectedTagLabels(DamageClass damageClass) {
        return expectedTagLabels(damageClassKey(damageClass));
    }

    public static List<String> actualTagLabels(DamageSource source) {
        if (source == null) {
            return List.of();
        }
        List<String> labels = new ArrayList<>(7);
        if (isProjectile(source)) {
            labels.add("projectile");
        }
        if (isExplosion(source)) {
            labels.add("explosion");
        }
        if (isFireDamage(source)) {
            labels.add("fire");
        }
        if (isUnblockable(source)) {
            labels.add("bypassesArmor");
        }
        if (isDamageAbsolute(source)) {
            labels.add("absolute");
        }
        if (bypassesEffects(source)) {
            labels.add("effects");
        }
        if (isDamageAllowedInCreativeMode(source)) {
            labels.add("creativeAllowed");
        }
        return List.copyOf(labels);
    }

    public static boolean isTau(DamageSource source) {
        return source != null && source.is(TAU);
    }

    public static boolean isTau(ResourceKey<DamageType> type) {
        return matches(type, TAU);
    }

    public static boolean isTau(String legacyTypeOrId) {
        return matches(legacyTypeOrId, TAU);
    }

    public static boolean isTau(DamageClass damageClass) {
        return matches(damageClass, TAU);
    }

    public static boolean isSubatomic(DamageSource source) {
        return source != null && (isSubatomicSource(source) || normalizeAlias(source.getMsgId()).startsWith("subatomic"));
    }

    public static boolean isSubatomic(ResourceKey<DamageType> type) {
        return isSubatomicKey(type);
    }

    public static boolean isSubatomic(String legacyTypeOrId) {
        return legacyKey(legacyTypeOrId)
                .filter(SUBATOMIC::equals)
                .isPresent()
                || (legacyTypeOrId != null && normalizeAlias(legacyTypeOrId).startsWith("subatomic"));
    }

    public static boolean isSubatomic(DamageClass damageClass) {
        return matches(damageClass, SUBATOMIC);
    }

    public static boolean is(DamageSource source, ResourceKey<DamageType> type) {
        return source != null && type != null
                && (source.is(type) || (isSubatomicSource(source) && isSubatomicKey(type)));
    }

    public static boolean is(DamageSource source, String legacyTypeOrId) {
        return source != null && legacyKey(legacyTypeOrId).filter(type -> is(source, type)).isPresent();
    }

    public static boolean is(DamageSource source, DamageClass damageClass) {
        return sourceMatches(source, damageClass);
    }

    public static boolean sourceMatches(DamageSource source, ResourceKey<DamageType> expected) {
        return is(source, expected);
    }

    public static boolean sourceMatches(DamageSource source, String expectedLegacyTypeOrId) {
        return is(source, expectedLegacyTypeOrId);
    }

    public static boolean sourceMatches(DamageSource source, DamageClass expectedDamageClass) {
        return is(source, damageClassKey(expectedDamageClass));
    }

    public static boolean matches(ResourceKey<DamageType> actual, ResourceKey<DamageType> expected) {
        return actual != null && expected != null
                && (actual.equals(expected) || (isSubatomicKey(actual) && isSubatomicKey(expected)));
    }

    public static boolean matches(ResourceKey<DamageType> actual, String expectedLegacyTypeOrId) {
        return actual != null && legacyKey(expectedLegacyTypeOrId).filter(expected -> matches(actual, expected)).isPresent();
    }

    public static boolean matches(ResourceKey<DamageType> actual, DamageClass expectedDamageClass) {
        return matches(actual, damageClassKey(expectedDamageClass));
    }

    public static boolean matches(String actualLegacyTypeOrId, ResourceKey<DamageType> expected) {
        return expected != null && legacyKey(actualLegacyTypeOrId).filter(actual -> matches(actual, expected)).isPresent();
    }

    public static boolean matches(String actualLegacyTypeOrId, String expectedLegacyTypeOrId) {
        Optional<ResourceKey<DamageType>> actual = legacyKey(actualLegacyTypeOrId);
        Optional<ResourceKey<DamageType>> expected = legacyKey(expectedLegacyTypeOrId);
        return actual.isPresent() && expected.isPresent() && matches(actual.get(), expected.get());
    }

    public static boolean matches(String actualLegacyTypeOrId, DamageClass expectedDamageClass) {
        return matches(actualLegacyTypeOrId, damageClassKey(expectedDamageClass));
    }

    public static boolean matches(DamageClass actualDamageClass, ResourceKey<DamageType> expected) {
        return matches(damageClassKey(actualDamageClass), expected);
    }

    public static boolean matches(DamageClass actualDamageClass, String expectedLegacyTypeOrId) {
        return matches(damageClassKey(actualDamageClass), expectedLegacyTypeOrId);
    }

    public static boolean matches(DamageClass actualDamageClass, DamageClass expectedDamageClass) {
        return damageClassKey(actualDamageClass).equals(damageClassKey(expectedDamageClass));
    }

    public static boolean isProjectile(DamageSource source) {
        return source != null && source.is(DamageTypeTags.IS_PROJECTILE);
    }

    public static boolean isProjectile(ResourceKey<DamageType> type) {
        return legacyDamageType(type)
                .map(LegacyDamageType::projectile)
                .orElse(false);
    }

    public static boolean isProjectile(String legacyTypeOrId) {
        return legacyDamageType(legacyTypeOrId)
                .map(LegacyDamageType::projectile)
                .orElse(false);
    }

    public static boolean isProjectile(DamageClass damageClass) {
        return isProjectile(damageClassKey(damageClass));
    }

    public static boolean isExplosion(DamageSource source) {
        return source != null && source.is(DamageTypeTags.IS_EXPLOSION);
    }

    public static boolean isExplosion(ResourceKey<DamageType> type) {
        return legacyDamageType(type)
                .map(LegacyDamageType::explosion)
                .orElse(false);
    }

    public static boolean isExplosion(String legacyTypeOrId) {
        return legacyDamageType(legacyTypeOrId)
                .map(LegacyDamageType::explosion)
                .orElse(false);
    }

    public static boolean isExplosion(DamageClass damageClass) {
        return isExplosion(damageClassKey(damageClass));
    }

    public static boolean isFireDamage(DamageSource source) {
        return source != null && source.is(DamageTypeTags.IS_FIRE);
    }

    public static boolean isFireDamage(ResourceKey<DamageType> type) {
        return legacyDamageType(type)
                .map(LegacyDamageType::fire)
                .orElse(false);
    }

    public static boolean isFireDamage(String legacyTypeOrId) {
        return legacyDamageType(legacyTypeOrId)
                .map(LegacyDamageType::fire)
                .orElse(false);
    }

    public static boolean isFireDamage(DamageClass damageClass) {
        return isFireDamage(damageClassKey(damageClass));
    }

    public static boolean isUnblockable(DamageSource source) {
        return source != null && source.is(DamageTypeTags.BYPASSES_ARMOR);
    }

    public static boolean isUnblockable(ResourceKey<DamageType> type) {
        return legacyDamageType(type)
                .map(LegacyDamageType::bypassesArmor)
                .orElse(false);
    }

    public static boolean isUnblockable(String legacyTypeOrId) {
        return legacyDamageType(legacyTypeOrId)
                .map(LegacyDamageType::bypassesArmor)
                .orElse(false);
    }

    public static boolean isUnblockable(DamageClass damageClass) {
        return isUnblockable(damageClassKey(damageClass));
    }

    public static boolean isDamageAbsolute(DamageSource source) {
        return source != null && source.is(DamageTypeTags.BYPASSES_RESISTANCE);
    }

    public static boolean isDamageAbsolute(ResourceKey<DamageType> type) {
        return legacyDamageType(type)
                .map(LegacyDamageType::absolute)
                .orElse(false);
    }

    public static boolean isDamageAbsolute(String legacyTypeOrId) {
        return legacyDamageType(legacyTypeOrId)
                .map(LegacyDamageType::absolute)
                .orElse(false);
    }

    public static boolean isDamageAbsolute(DamageClass damageClass) {
        return isDamageAbsolute(damageClassKey(damageClass));
    }

    public static boolean bypassesEffects(DamageSource source) {
        return source != null && source.is(DamageTypeTags.BYPASSES_EFFECTS);
    }

    public static boolean bypassesEffects(ResourceKey<DamageType> type) {
        return legacyDamageType(type)
                .map(LegacyDamageType::bypassesEffects)
                .orElse(false);
    }

    public static boolean bypassesEffects(String legacyTypeOrId) {
        return legacyDamageType(legacyTypeOrId)
                .map(LegacyDamageType::bypassesEffects)
                .orElse(false);
    }

    public static boolean bypassesEffects(DamageClass damageClass) {
        return bypassesEffects(damageClassKey(damageClass));
    }

    public static boolean isDamageAllowedInCreativeMode(DamageSource source) {
        return source != null && source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    public static boolean isDamageAllowedInCreativeMode(ResourceKey<DamageType> type) {
        return legacyDamageType(type)
                .map(LegacyDamageType::creativeAllowed)
                .orElse(false);
    }

    public static boolean isDamageAllowedInCreativeMode(String legacyTypeOrId) {
        return legacyDamageType(legacyTypeOrId)
                .map(LegacyDamageType::creativeAllowed)
                .orElse(false);
    }

    public static boolean isDamageAllowedInCreativeMode(DamageClass damageClass) {
        return isDamageAllowedInCreativeMode(damageClassKey(damageClass));
    }

    public static String damageType(DamageSource source) {
        return source == null ? "" : source.getMsgId();
    }

    public static String damageType(ResourceKey<DamageType> type) {
        if (type == null) {
            return "";
        }
        return legacyDamageType(type)
                .map(LegacyDamageType::expectedMessageId)
                .orElseGet(() -> type.location().getPath());
    }

    public static String damageType(String legacyTypeOrId) {
        if (legacyTypeOrId == null || legacyTypeOrId.isBlank()) {
            return "";
        }
        return legacyKey(legacyTypeOrId)
                .map(ModDamageSources::damageType)
                .orElse(legacyTypeOrId);
    }

    public static String damageType(DamageClass damageClass) {
        return damageType(damageClassKey(damageClass));
    }

    @Nullable
    public static Entity getEntity(DamageSource source) {
        return source == null ? null : source.getEntity();
    }

    @Nullable
    public static Entity getSourceOfDamage(DamageSource source) {
        return source == null ? null : source.getDirectEntity();
    }

    public static DamageAliasAudit aliasAudit() {
        List<String> problems = new ArrayList<>();
        for (LegacyDamageType legacy : LEGACY_DAMAGE_TYPES) {
            expectResolve(problems, legacy.location().getPath(), legacy.key());
            expectResolve(problems, legacy.location().toString(), legacy.key());
            for (String alias : legacyAliases(legacy.key())) {
                if (alias.endsWith("..5")) {
                    continue;
                }
                expectResolve(problems, alias, legacy.key());
            }
        }

        expectResolve(problems, "nuclearBlast", NUCLEAR_BLAST);
        expectResolve(problems, "mudPoisoning", MUD_POISONING);
        expectResolve(problems, "tauBlast", TAU_BLAST);
        expectResolve(problems, "blackLung", BLACK_LUNG);
        expectResolve(problems, "amsCore", AMS_CORE);
        expectResolve(problems, "electrified", ELECTRICITY);
        expectResolve(problems, "s_emp", ELECTRICITY);
        expectResolve(problems, "revolverBullet", REVOLVER_BULLET);
        expectResolve(problems, "s_bullet", REVOLVER_BULLET);
        expectResolve(problems, "PHYSICAL", REVOLVER_BULLET);
        expectResolve(problems, "FIRE", FLAMETHROWER);
        expectResolve(problems, "EXPLOSIVE", EXPLOSION);
        expectResolve(problems, "ELECTRIC", ELECTRIC);
        expectResolve(problems, "PLASMA", PLASMA);
        expectResolve(problems, "LASER", LASER);
        expectResolve(problems, "MICROWAVE", MICROWAVE);
        expectResolve(problems, "SUBATOMIC", SUBATOMIC);
        expectResolve(problems, "subAtomic1", SUBATOMIC_1);
        expectResolve(problems, "subAtomic2", SUBATOMIC_2);
        expectResolve(problems, "subAtomic3", SUBATOMIC_3);
        expectResolve(problems, "subAtomic4", SUBATOMIC_4);
        expectResolve(problems, "subAtomic5", SUBATOMIC_5);
        expectResolve(problems, "OTHER", minecraft("generic"));
        expectResolve(problems, "chopperBullet", CHOPPER_BULLET);
        expectResolve(problems, "s_emplacer", CHOPPER_BULLET);
        expectResolve(problems, "cmb", COMBINE_BALL);
        expectResolve(problems, "s_combineball", COMBINE_BALL);
        for (int i = 1; i <= 5; i++) {
            expectResolve(problems, "subAtomic" + i, SUBATOMIC);
        }
        expectResolve(problems, "onFire", minecraft("on_fire"));
        expectResolve(problems, "inFire", minecraft("in_fire"));
        expectResolve(problems, "hotFloor", minecraft("hot_floor"));
        expectResolve(problems, "frozen", minecraft("freeze"));
        expectResolve(problems, "playerAttack", minecraft("player_attack"));
        expectResolve(problems, "mobAttack", minecraft("mob_attack"));
        expect(problems, "null damage type empty", damageType((DamageSource) null).isEmpty());
        expect(problems, "null entity source missing", getEntity(null) == null && getSourceOfDamage(null) == null);
        expect(problems, "null projectile false", !isProjectile((DamageSource) null));
        expect(problems, "null absolute false", !isDamageAbsolute((DamageSource) null));
        expect(problems, "null legacy source missing", legacyDamageType((DamageSource) null).isEmpty());
        expect(problems, "null actual tags empty", actualTagLabels(null).isEmpty());

        expectLegacy(problems, NUCLEAR_BLAST, false, true, false, false, false, false);
        expectLegacy(problems, DIGAMMA, false, false, false, true, true, true);
        expectLegacy(problems, BLACKHOLE, false, false, false, true, true, false);
        expectLegacy(problems, ELECTRIC, false, false, false, false, false, false);
        expectLegacy(problems, ELECTRICITY, false, false, false, true, true, false);
        expectLegacy(problems, SHRAPNEL, true, false, false, false, false, false);
        expectLegacy(problems, TAU, true, false, false, true, false, false);
        expectLegacy(problems, SUBATOMIC, true, false, false, true, false, false);
        expectLegacy(problems, SUBATOMIC_3, true, false, false, true, false, false);
        expectLegacy(problems, PLASMA, false, false, true, false, false, false);
        expectLegacy(problems, FLAMETHROWER, false, false, true, false, false, false);
        expect(problems, "tau expected tags", legacyDamageType(TAU)
                .map(LegacyDamageType::expectedTagLabels)
                .filter(tags -> tags.equals(List.of("projectile", "bypassesArmor")))
                .isPresent());
        expect(problems, "digamma expected tags", legacyDamageType(DIGAMMA)
                .map(LegacyDamageType::expectedTagLabels)
                .filter(tags -> tags.equals(List.of("bypassesArmor", "absolute", "effects", "creativeAllowed")))
                .isPresent());
        expect(problems, "electric has no expected tags", legacyDamageType(ELECTRIC)
                .map(LegacyDamageType::expectedTagLabels)
                .filter(List::isEmpty)
                .isPresent());
        expect(problems, "blackhole expected modern effects tag", legacyDamageType(BLACKHOLE)
                .map(LegacyDamageType::expectedTagLabels)
                .filter(tags -> tags.equals(List.of("bypassesArmor", "absolute", "effects")))
                .isPresent());
        expect(problems, "key projectile metadata", isProjectile(TAU) && isProjectile("tau"));
        expect(problems, "key explosion metadata", isExplosion(TAU_BLAST) && isExplosion("tauBlast"));
        expect(problems, "key fire metadata", isFireDamage(FLAMETHROWER) && isFireDamage("flamethrower"));
        expect(problems, "key bypass metadata", isUnblockable(SUBATOMIC) && isUnblockable("subAtomic4"));
        expect(problems, "tau helper metadata",
                isTau(TAU) && isTau("tau") && isTau("s_tau") && !isTau(SUBATOMIC));
        expect(problems, "subatomic helper metadata",
                isSubatomic(SUBATOMIC) && isSubatomic("subAtomic4") && isSubatomic(DamageClass.SUBATOMIC)
                        && !isSubatomic(TAU));
        expect(problems, "source key matches without world",
                matches(COMBINE_BALL, "cmb") && matches("combineBall", COMBINE_BALL)
                        && matches("s_combineball", "cmb") && !matches(TAU, COMBINE_BALL));
        expect(problems, "damage class matches without world",
                matches(DamageClass.PHYSICAL, REVOLVER_BULLET)
                        && matches(DamageClass.SUBATOMIC, "subAtomic3")
                        && !matches(DamageClass.LASER, DamageClass.PLASMA));
        expect(problems, "key absolute metadata", isDamageAbsolute(DIGAMMA) && isDamageAbsolute("digamma"));
        expect(problems, "key effects metadata", bypassesEffects(BLACKHOLE) && bypassesEffects("blackhole"));
        expect(problems, "key creative metadata", isDamageAllowedInCreativeMode(NITAN)
                && isDamageAllowedInCreativeMode("nitan"));
        expect(problems, "key expected labels", expectedTagLabels(COMBINE_BALL)
                .equals(List.of("projectile", "bypassesArmor")));
        expect(problems, "string expected labels", expectedTagLabels("subAtomic2")
                .equals(List.of("projectile", "bypassesArmor")));
        expect(problems, "damage class physical maps projectile",
                damageClassKey(DamageClass.PHYSICAL).equals(REVOLVER_BULLET)
                        && isProjectile(DamageClass.PHYSICAL));
        expect(problems, "damage class fire maps flamethrower",
                damageClassKey(DamageClass.FIRE).equals(FLAMETHROWER)
                        && isFireDamage(DamageClass.FIRE));
        expect(problems, "damage class explosive maps explosion",
                damageClassKey(DamageClass.EXPLOSIVE).equals(EXPLOSION)
                        && isExplosion(DamageClass.EXPLOSIVE));
        expect(problems, "damage class electric maps electric",
                damageClassKey(DamageClass.ELECTRIC).equals(ELECTRIC)
                        && damageType(DamageClass.ELECTRIC).equals("electric"));
        expect(problems, "damage class subatomic bypass",
                damageClassKey(DamageClass.SUBATOMIC).equals(SUBATOMIC)
                        && isProjectile(DamageClass.SUBATOMIC)
                        && isUnblockable(DamageClass.SUBATOMIC));
        expect(problems, "damage class other maps generic",
                damageClassKey(DamageClass.OTHER).equals(minecraft("generic"))
                        && expectedTagLabels(DamageClass.OTHER).isEmpty());
        expect(problems, "key damage type expected message id", damageType(COMBINE_BALL).equals("cmb"));
        expect(problems, "string damage type expected message id", damageType("subAtomic4").equals("subAtomic4"));
        expect(problems, "unknown string metadata false", !isProjectile("not_a_real_damage_type")
                && expectedTagLabels("not_a_real_damage_type").isEmpty());
        expectMessageId(problems, COMBINE_BALL, "cmb");
        expectMessageId(problems, SUBATOMIC, "subAtomic");
        expectMessageId(problems, SUBATOMIC_1, "subAtomic1");
        expectMessageId(problems, SUBATOMIC_2, "subAtomic2");
        expectMessageId(problems, SUBATOMIC_3, "subAtomic3");
        expectMessageId(problems, SUBATOMIC_4, "subAtomic4");
        expectMessageId(problems, SUBATOMIC_5, "subAtomic5");
        expectMessageId(problems, NUCLEAR_BLAST, "nuclearBlast");
        expectMessageId(problems, MUD_POISONING, "mudPoisoning");
        expectMessageId(problems, TAU_BLAST, "tauBlast");
        expectMessageId(problems, BLACK_LUNG, "blacklung");
        expectMessageId(problems, ACID_PLAYER, "acidPlayer");
        expectMessageId(problems, ELECTRICITY, "electricity");
        expectMessageId(problems, REVOLVER_BULLET, "revolverBullet");
        expectMessageId(problems, CHOPPER_BULLET, "chopperBullet");
        com.hbm.lib.ModDamageSource.FacadeAudit facadeAudit = com.hbm.lib.ModDamageSource.facadeAudit();
        if (!facadeAudit.passed()) {
            problems.addAll(facadeAudit.problems().stream()
                    .map(problem -> "ModDamageSource facade " + problem)
                    .toList());
        }

        return new DamageAliasAudit(List.copyOf(problems), LEGACY_DAMAGE_TYPES.size(), LEGACY_DAMAGE_KEYS.size());
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, name));
    }

    private static ResourceKey<DamageType> randomSubatomic(Level level) {
        return switch (level.random.nextInt(5)) {
            case 0 -> SUBATOMIC_1;
            case 1 -> SUBATOMIC_2;
            case 2 -> SUBATOMIC_3;
            case 3 -> SUBATOMIC_4;
            default -> SUBATOMIC_5;
        };
    }

    private static boolean isSubatomicSource(DamageSource source) {
        return source.is(SUBATOMIC)
                || source.is(SUBATOMIC_1)
                || source.is(SUBATOMIC_2)
                || source.is(SUBATOMIC_3)
                || source.is(SUBATOMIC_4)
                || source.is(SUBATOMIC_5);
    }

    private static boolean isSubatomicKey(ResourceKey<DamageType> key) {
        return SUBATOMIC.equals(key)
                || SUBATOMIC_1.equals(key)
                || SUBATOMIC_2.equals(key)
                || SUBATOMIC_3.equals(key)
                || SUBATOMIC_4.equals(key)
                || SUBATOMIC_5.equals(key);
    }

    private static LegacyDamageType legacy(ResourceKey<DamageType> key, boolean projectile, boolean explosion, boolean fire,
            boolean bypassesArmor, boolean absolute, boolean creativeAllowed) {
        return new LegacyDamageType(key, projectile, explosion, fire, bypassesArmor, absolute, creativeAllowed);
    }

    private static Optional<ResourceKey<DamageType>> parseDamageTypeId(String id) {
        try {
            return Optional.of(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(id)));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static Map<String, ResourceKey<DamageType>> createLegacyDamageKeys() {
        Map<String, ResourceKey<DamageType>> aliases = new HashMap<>();
        for (LegacyDamageType legacy : LEGACY_DAMAGE_TYPES) {
            registerAlias(aliases, legacy.key(), legacy.location().getPath());
        }
        registerAlias(aliases, NUCLEAR_BLAST, "nuclearBlast");
        registerAlias(aliases, MUD_POISONING, "mudPoisoning");
        registerAlias(aliases, EUTHANIZED_SELF, "euthanizedSelf");
        registerAlias(aliases, EUTHANIZED_SELF2, "euthanizedSelf2");
        registerAlias(aliases, TAU_BLAST, "tauBlast");
        registerAlias(aliases, BLACK_LUNG, "blackLung");
        registerAlias(aliases, AMS_CORE, "amsCore");
        registerAlias(aliases, ELECTRICITY, "electrified");
        registerAlias(aliases, REVOLVER_BULLET, "revolverBullet", "bullet", "s_bullet");
        registerAlias(aliases, CHOPPER_BULLET, "chopperBullet", "emplacer", "displacement", "s_emplacer");
        registerAlias(aliases, COMBINE_BALL, "cmb", "combineBall", "combineball", "s_combineball");
        registerAlias(aliases, SUBATOMIC, "subAtomic");
        registerAlias(aliases, SUBATOMIC_1, "subAtomic1");
        registerAlias(aliases, SUBATOMIC_2, "subAtomic2");
        registerAlias(aliases, SUBATOMIC_3, "subAtomic3");
        registerAlias(aliases, SUBATOMIC_4, "subAtomic4");
        registerAlias(aliases, SUBATOMIC_5, "subAtomic5");
        registerAlias(aliases, ACID_PLAYER, "acidPlayer");
        registerAlias(aliases, FLAMETHROWER, "s_flamethrower");
        registerAlias(aliases, PLASMA, "immolator", "s_immolator");
        registerAlias(aliases, ICE, "cryolator", "s_cryolator");
        registerAlias(aliases, LASER, "s_laser");
        registerAlias(aliases, BOIL, "s_boil");
        registerAlias(aliases, ACID_PLAYER, "s_acid");
        registerAlias(aliases, EUTHANIZED, "s_euthanized");
        registerAlias(aliases, TAU, "s_tau");
        registerAlias(aliases, ELECTRICITY, "s_emp");
        registerAlias(aliases, REVOLVER_BULLET, "PHYSICAL", "physical");
        registerAlias(aliases, FLAMETHROWER, "FIRE", "fire");
        registerAlias(aliases, EXPLOSION, "EXPLOSIVE", "explosive");
        registerAlias(aliases, ELECTRIC, "ELECTRIC", "electric");
        registerAlias(aliases, PLASMA, "PLASMA", "plasma");
        registerAlias(aliases, LASER, "LASER", "laser");
        registerAlias(aliases, MICROWAVE, "MICROWAVE", "microwave");
        registerAlias(aliases, SUBATOMIC, "SUBATOMIC", "subatomic");
        registerAlias(aliases, minecraft("generic"), "OTHER", "other");
        registerAlias(aliases, minecraft("on_fire"), "onFire", "on_fire");
        registerAlias(aliases, minecraft("in_fire"), "inFire", "in_fire");
        registerAlias(aliases, minecraft("hot_floor"), "hotFloor", "hot_floor");
        registerAlias(aliases, minecraft("freeze"), "freeze", "frozen");
        registerAlias(aliases, minecraft("fall"), "fall");
        registerAlias(aliases, minecraft("drown"), "drown");
        registerAlias(aliases, minecraft("generic"), "generic");
        registerAlias(aliases, minecraft("player_attack"), "player", "playerAttack", "player_attack");
        registerAlias(aliases, minecraft("mob_attack"), "mob", "mobAttack", "mob_attack");
        return Collections.unmodifiableMap(aliases);
    }

    private static Map<ResourceKey<DamageType>, List<String>> createLegacyDamageAliases() {
        Map<ResourceKey<DamageType>, List<String>> aliases = new HashMap<>();
        aliases.put(NUCLEAR_BLAST, List.of("nuclearBlast"));
        aliases.put(MUD_POISONING, List.of("mudPoisoning"));
        aliases.put(EUTHANIZED_SELF, List.of("euthanizedSelf"));
        aliases.put(EUTHANIZED_SELF2, List.of("euthanizedSelf2"));
        aliases.put(TAU_BLAST, List.of("tauBlast"));
        aliases.put(BLACK_LUNG, List.of("blackLung"));
        aliases.put(AMS_CORE, List.of("amsCore"));
        aliases.put(ELECTRICITY, List.of("electrified", "s_emp"));
        aliases.put(REVOLVER_BULLET, List.of("revolverBullet", "bullet", "s_bullet"));
        aliases.put(CHOPPER_BULLET, List.of("chopperBullet", "emplacer", "displacement", "s_emplacer"));
        aliases.put(COMBINE_BALL, List.of("cmb", "combineBall", "s_combineball"));
        aliases.put(SUBATOMIC, List.of("subAtomic"));
        aliases.put(SUBATOMIC_1, List.of("subAtomic1"));
        aliases.put(SUBATOMIC_2, List.of("subAtomic2"));
        aliases.put(SUBATOMIC_3, List.of("subAtomic3"));
        aliases.put(SUBATOMIC_4, List.of("subAtomic4"));
        aliases.put(SUBATOMIC_5, List.of("subAtomic5"));
        aliases.put(ACID_PLAYER, List.of("acidPlayer", "s_acid"));
        aliases.put(FLAMETHROWER, List.of("s_flamethrower"));
        aliases.put(PLASMA, List.of("immolator", "s_immolator"));
        aliases.put(ICE, List.of("cryolator", "s_cryolator"));
        aliases.put(LASER, List.of("s_laser"));
        aliases.put(BOIL, List.of("s_boil"));
        aliases.put(EUTHANIZED, List.of("s_euthanized"));
        aliases.put(TAU, List.of("s_tau"));
        aliases.put(minecraft("generic"), List.of("OTHER"));
        aliases.put(minecraft("on_fire"), List.of("onFire"));
        aliases.put(minecraft("in_fire"), List.of("inFire"));
        aliases.put(minecraft("hot_floor"), List.of("hotFloor"));
        aliases.put(minecraft("freeze"), List.of("frozen"));
        aliases.put(minecraft("player_attack"), List.of("playerAttack"));
        aliases.put(minecraft("mob_attack"), List.of("mobAttack"));
        return Collections.unmodifiableMap(aliases);
    }

    private static void registerAlias(Map<String, ResourceKey<DamageType>> aliases, ResourceKey<DamageType> key, String... names) {
        for (String name : names) {
            aliases.put(normalizeAlias(name), key);
        }
    }

    private static String normalizeAlias(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (c != '_' && c != '-' && c != '.' && c != ' ') {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static ResourceKey<DamageType> minecraft(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("minecraft", name));
    }

    private static void expectResolve(List<String> problems, String alias, ResourceKey<DamageType> expected) {
        Optional<ResourceKey<DamageType>> actual = legacyKey(alias);
        if (actual.isEmpty() || !actual.get().equals(expected)) {
            problems.add("alias " + alias + " -> " + actual.map(key -> key.location().toString()).orElse("<missing>")
                    + ", expected " + expected.location());
        }
    }

    private static void expect(List<String> problems, String label, boolean ok) {
        if (!ok) {
            problems.add(label);
        }
    }

    private static void expectLegacy(List<String> problems, ResourceKey<DamageType> key, boolean projectile,
            boolean explosion, boolean fire, boolean bypassesArmor, boolean absolute, boolean creativeAllowed) {
        LegacyDamageType actual = null;
        for (LegacyDamageType legacy : LEGACY_DAMAGE_TYPES) {
            if (legacy.key().equals(key)) {
                actual = legacy;
                break;
            }
        }
        if (actual == null) {
            problems.add("missing legacy metadata for " + key.location());
            return;
        }
        if (actual.projectile() != projectile
                || actual.explosion() != explosion
                || actual.fire() != fire
                || actual.bypassesArmor() != bypassesArmor
                || actual.absolute() != absolute
                || actual.creativeAllowed() != creativeAllowed) {
            problems.add("legacy metadata mismatch for " + key.location());
        }
    }

    private static void expectMessageId(List<String> problems, ResourceKey<DamageType> key, String expected) {
        String actual = legacyDamageType(key)
                .map(LegacyDamageType::expectedMessageId)
                .orElse("<missing>");
        if (!expected.equals(actual)) {
            problems.add("message id " + key.location() + " -> " + actual + ", expected " + expected);
        }
    }

    public record LegacyDamageType(ResourceKey<DamageType> key, boolean projectile, boolean explosion, boolean fire,
                                   boolean bypassesArmor, boolean absolute, boolean creativeAllowed) {
        public ResourceLocation location() {
            return key.location();
        }

        public List<String> expectedTagLabels() {
            List<String> labels = new ArrayList<>(7);
            if (projectile) {
                labels.add("projectile");
            }
            if (explosion) {
                labels.add("explosion");
            }
            if (fire) {
                labels.add("fire");
            }
            if (bypassesArmor) {
                labels.add("bypassesArmor");
            }
            if (absolute) {
                labels.add("absolute");
            }
            if (bypassesEffects()) {
                labels.add("effects");
            }
            if (creativeAllowed) {
                labels.add("creativeAllowed");
            }
            return List.copyOf(labels);
        }

        public boolean bypassesEffects() {
            return absolute;
        }

        public String expectedMessageId() {
            return switch (location().getPath()) {
                case "acid_player" -> "acidPlayer";
                case "ams_core" -> "amsCore";
                case "black_lung", "blacklung" -> "blacklung";
                case "chopper_bullet" -> "chopperBullet";
                case "combine_ball" -> "cmb";
                case "euthanized_self" -> "euthanizedSelf";
                case "euthanized_self2" -> "euthanizedSelf2";
                case "mud_poisoning" -> "mudPoisoning";
                case "nuclear_blast" -> "nuclearBlast";
                case "revolver_bullet" -> "revolverBullet";
                case "subatomic" -> "subAtomic";
                case "subatomic_1" -> "subAtomic1";
                case "subatomic_2" -> "subAtomic2";
                case "subatomic_3" -> "subAtomic3";
                case "subatomic_4" -> "subAtomic4";
                case "subatomic_5" -> "subAtomic5";
                case "tau_blast" -> "tauBlast";
                default -> location().getPath();
            };
        }
    }

    public record DamageAliasAudit(List<String> problems, int legacyTypes, int aliases) {
        public boolean passed() {
            return problems.isEmpty();
        }
    }

    private ModDamageSources() {
    }
}
