package com.hbm.ntm.radiation;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
        return indirect(level, SUBATOMIC, direct, cause);
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

    public static boolean isTau(DamageSource source) {
        return source.is(TAU);
    }

    public static boolean isSubatomic(DamageSource source) {
        return source.is(SUBATOMIC) || normalizeAlias(source.getMsgId()).startsWith("subatomic");
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

        expectLegacy(problems, NUCLEAR_BLAST, false, true, false, false, false, false);
        expectLegacy(problems, DIGAMMA, false, false, false, true, true, true);
        expectLegacy(problems, BLACKHOLE, false, false, false, true, true, false);
        expectLegacy(problems, ELECTRIC, false, false, false, false, false, false);
        expectLegacy(problems, ELECTRICITY, false, false, false, true, true, false);
        expectLegacy(problems, SHRAPNEL, true, false, false, false, false, false);
        expectLegacy(problems, TAU, true, false, false, true, false, false);
        expectLegacy(problems, SUBATOMIC, true, false, false, true, false, false);
        expectLegacy(problems, PLASMA, false, false, true, false, false, false);
        expectLegacy(problems, FLAMETHROWER, false, false, true, false, false, false);

        return new DamageAliasAudit(List.copyOf(problems), LEGACY_DAMAGE_TYPES.size(), LEGACY_DAMAGE_KEYS.size());
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, name));
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
        registerAlias(aliases, SUBATOMIC, "subAtomic", "subAtomic1", "subAtomic2", "subAtomic3", "subAtomic4", "subAtomic5");
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
        aliases.put(SUBATOMIC, List.of("subAtomic", "subAtomic1..5"));
        aliases.put(ACID_PLAYER, List.of("acidPlayer", "s_acid"));
        aliases.put(FLAMETHROWER, List.of("s_flamethrower"));
        aliases.put(PLASMA, List.of("immolator", "s_immolator"));
        aliases.put(ICE, List.of("cryolator", "s_cryolator"));
        aliases.put(LASER, List.of("s_laser"));
        aliases.put(BOIL, List.of("s_boil"));
        aliases.put(EUTHANIZED, List.of("s_euthanized"));
        aliases.put(TAU, List.of("s_tau"));
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

    public record LegacyDamageType(ResourceKey<DamageType> key, boolean projectile, boolean explosion, boolean fire,
                                   boolean bypassesArmor, boolean absolute, boolean creativeAllowed) {
        public ResourceLocation location() {
            return key.location();
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
