package com.hbm.lib;

import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.damage.DamageClass;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Legacy 1.7.10 package bridge for HBM damage source names.
 *
 * <p>Modern damage sources require a level registry lookup, so the old static fields are represented as
 * {@link ResourceKey} values and converted to real {@link DamageSource} instances through the factory methods.</p>
 */
@Deprecated(forRemoval = false)
public final class ModDamageSource {
    public static final ResourceKey<DamageType> nuclearBlast = ModDamageSources.NUCLEAR_BLAST;
    public static final ResourceKey<DamageType> mudPoisoning = ModDamageSources.MUD_POISONING;
    public static final ResourceKey<DamageType> acid = ModDamageSources.ACID;
    public static final ResourceKey<DamageType> euthanizedSelf = ModDamageSources.EUTHANIZED_SELF;
    public static final ResourceKey<DamageType> euthanizedSelf2 = ModDamageSources.EUTHANIZED_SELF2;
    public static final ResourceKey<DamageType> tauBlast = ModDamageSources.TAU_BLAST;
    public static final ResourceKey<DamageType> radiation = ModDamageSources.RADIATION;
    public static final ResourceKey<DamageType> digamma = ModDamageSources.DIGAMMA;
    public static final ResourceKey<DamageType> suicide = ModDamageSources.SUICIDE;
    public static final ResourceKey<DamageType> rubble = ModDamageSources.RUBBLE;
    public static final ResourceKey<DamageType> shrapnel = ModDamageSources.SHRAPNEL;
    public static final ResourceKey<DamageType> blackhole = ModDamageSources.BLACKHOLE;
    public static final ResourceKey<DamageType> turbofan = ModDamageSources.BLENDER;
    public static final ResourceKey<DamageType> meteorite = ModDamageSources.METEORITE;
    public static final ResourceKey<DamageType> boxcar = ModDamageSources.BOXCAR;
    public static final ResourceKey<DamageType> boat = ModDamageSources.BOAT;
    public static final ResourceKey<DamageType> building = ModDamageSources.BUILDING;
    public static final ResourceKey<DamageType> taint = ModDamageSources.TAINT;
    public static final ResourceKey<DamageType> ams = ModDamageSources.AMS;
    public static final ResourceKey<DamageType> amsCore = ModDamageSources.AMS_CORE;
    public static final ResourceKey<DamageType> broadcast = ModDamageSources.BROADCAST;
    public static final ResourceKey<DamageType> bang = ModDamageSources.BANG;
    public static final ResourceKey<DamageType> pc = ModDamageSources.PC;
    public static final ResourceKey<DamageType> cloud = ModDamageSources.CLOUD;
    public static final ResourceKey<DamageType> lead = ModDamageSources.LEAD;
    public static final ResourceKey<DamageType> enervation = ModDamageSources.ENERVATION;
    public static final ResourceKey<DamageType> electricity = ModDamageSources.ELECTRICITY;
    public static final ResourceKey<DamageType> exhaust = ModDamageSources.EXHAUST;
    public static final ResourceKey<DamageType> spikes = ModDamageSources.SPIKES;
    public static final ResourceKey<DamageType> lunar = ModDamageSources.LUNAR;
    public static final ResourceKey<DamageType> monoxide = ModDamageSources.MONOXIDE;
    public static final ResourceKey<DamageType> asbestos = ModDamageSources.ASBESTOS;
    public static final ResourceKey<DamageType> blacklung = ModDamageSources.BLACK_LUNG;
    public static final ResourceKey<DamageType> mku = ModDamageSources.MKU;
    public static final ResourceKey<DamageType> vacuum = ModDamageSources.VACUUM;
    public static final ResourceKey<DamageType> overdose = ModDamageSources.OVERDOSE;
    public static final ResourceKey<DamageType> microwave = ModDamageSources.MICROWAVE;
    public static final ResourceKey<DamageType> nitan = ModDamageSources.NITAN;

    public static final String s_bullet = "revolverBullet";
    public static final String s_emplacer = "chopperBullet";
    public static final String s_tau = "tau";
    public static final String s_combineball = "cmb";
    public static final String s_zomg_prefix = "subAtomic";
    public static final String s_euthanized = "euthanized";
    public static final String s_emp = "electrified";
    public static final String s_flamethrower = "flamethrower";
    public static final String s_immolator = "plasma";
    public static final String s_cryolator = "ice";
    public static final String s_laser = "laser";
    public static final String s_boil = "boil";
    public static final String s_acid = "acidPlayer";

    private ModDamageSource() {
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return ModDamageSources.source(level, type);
    }

    public static DamageSource source(Level level, String legacyTypeOrId) {
        return ModDamageSources.source(level, legacyTypeOrId);
    }

    public static DamageSource source(Level level, DamageClass damageClass) {
        return ModDamageSources.source(level, damageClass);
    }

    public static DamageSource source(Level level, com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.source(level, modern(damageClass));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, @Nullable Entity source) {
        return ModDamageSources.source(level, type, source);
    }

    public static DamageSource source(Level level, String legacyTypeOrId, @Nullable Entity source) {
        return ModDamageSources.source(level, legacyTypeOrId, source);
    }

    public static DamageSource source(Level level, DamageClass damageClass, @Nullable Entity source) {
        return ModDamageSources.source(level, damageClass, source);
    }

    public static DamageSource source(Level level, com.hbm.util.DamageResistanceHandler.DamageClass damageClass,
            @Nullable Entity source) {
        return ModDamageSources.source(level, modern(damageClass), source);
    }

    public static DamageSource source(Entity target, ResourceKey<DamageType> type) {
        return ModDamageSources.source(level(target, null), type);
    }

    public static DamageSource source(Entity target, String legacyTypeOrId) {
        return ModDamageSources.source(level(target, null), legacyTypeOrId);
    }

    public static DamageSource source(Entity target, DamageClass damageClass) {
        return ModDamageSources.source(level(target, null), damageClass);
    }

    public static DamageSource source(Entity target, com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.source(level(target, null), modern(damageClass));
    }

    public static DamageSource source(Entity target, ResourceKey<DamageType> type, @Nullable Entity cause) {
        return ModDamageSources.source(level(target, cause), type, cause);
    }

    public static DamageSource source(Entity target, String legacyTypeOrId, @Nullable Entity cause) {
        return ModDamageSources.source(level(target, cause), legacyTypeOrId, cause);
    }

    public static DamageSource source(Entity target, DamageClass damageClass, @Nullable Entity cause) {
        return ModDamageSources.source(level(target, cause), damageClass, cause);
    }

    public static DamageSource source(Entity target, com.hbm.util.DamageResistanceHandler.DamageClass damageClass,
            @Nullable Entity cause) {
        return ModDamageSources.source(level(target, cause), modern(damageClass), cause);
    }

    public static DamageSource indirect(Level level, ResourceKey<DamageType> type, Entity direct,
            @Nullable Entity cause) {
        return ModDamageSources.indirect(level, type, direct, cause);
    }

    public static DamageSource indirect(Level level, String legacyTypeOrId, Entity direct,
            @Nullable Entity cause) {
        return ModDamageSources.indirect(level, legacyTypeOrId, direct, cause);
    }

    public static DamageSource indirect(Level level, DamageClass damageClass, Entity direct, @Nullable Entity cause) {
        return ModDamageSources.indirect(level, damageClass, direct, cause);
    }

    public static DamageSource indirect(Level level, com.hbm.util.DamageResistanceHandler.DamageClass damageClass,
            Entity direct, @Nullable Entity cause) {
        return ModDamageSources.indirect(level, modern(damageClass), direct, cause);
    }

    public static DamageSource indirect(ResourceKey<DamageType> type, Entity direct, @Nullable Entity cause) {
        return ModDamageSources.indirect(level(direct, cause), type, direct, cause);
    }

    public static DamageSource indirect(String legacyTypeOrId, Entity direct, @Nullable Entity cause) {
        return ModDamageSources.indirect(level(direct, cause), legacyTypeOrId, direct, cause);
    }

    public static DamageSource indirect(DamageClass damageClass, Entity direct, @Nullable Entity cause) {
        return ModDamageSources.indirect(level(direct, cause), damageClass, direct, cause);
    }

    public static DamageSource indirect(com.hbm.util.DamageResistanceHandler.DamageClass damageClass, Entity direct,
            @Nullable Entity cause) {
        return ModDamageSources.indirect(level(direct, cause), modern(damageClass), direct, cause);
    }

    public static DamageSource causeDamageClassDamage(Entity direct, @Nullable Entity cause, DamageClass damageClass) {
        return cause == null
                ? ModDamageSources.source(level(direct, null), damageClass)
                : ModDamageSources.indirect(level(direct, cause), damageClass, direct, cause);
    }

    public static DamageSource causeDamageClassDamage(Entity direct, @Nullable Entity cause,
            com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return causeDamageClassDamage(direct, cause, modern(damageClass));
    }

    public static DamageSource causeBulletDamage(Entity base, @Nullable Entity ent) {
        return ModDamageSources.bullet(level(base, ent), base, ent);
    }

    public static DamageSource causeDisplacementDamage(Entity ent, @Nullable Entity hit) {
        return ModDamageSources.displacement(level(ent, hit), ent, hit);
    }

    public static DamageSource causeTauDamage(Entity ent, @Nullable Entity hit) {
        return ModDamageSources.tau(level(ent, hit), ent, hit);
    }

    public static DamageSource causeCombineDamage(Entity ent, @Nullable Entity hit) {
        return ModDamageSources.combineBall(level(ent, hit), ent, hit);
    }

    public static DamageSource causeSubatomicDamage(Entity ent, @Nullable Entity hit) {
        return ModDamageSources.subatomic(level(ent, hit), ent, hit);
    }

    public static DamageSource euthanized(Entity ent, @Nullable Entity hit) {
        return ModDamageSources.euthanized(level(ent, hit), ent, hit);
    }

    public static boolean getIsTau(DamageSource source) {
        return ModDamageSources.isTau(source);
    }

    public static boolean getIsTau(ResourceKey<DamageType> type) {
        return ModDamageSources.isTau(type);
    }

    public static boolean getIsTau(String legacyTypeOrId) {
        return ModDamageSources.isTau(legacyTypeOrId);
    }

    public static boolean getIsTau(DamageClass damageClass) {
        return ModDamageSources.isTau(damageClass);
    }

    public static boolean getIsTau(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.isTau(modern(damageClass));
    }

    public static boolean getIsSubatomic(DamageSource source) {
        return ModDamageSources.isSubatomic(source);
    }

    public static boolean getIsSubatomic(ResourceKey<DamageType> type) {
        return ModDamageSources.isSubatomic(type);
    }

    public static boolean getIsSubatomic(String legacyTypeOrId) {
        return ModDamageSources.isSubatomic(legacyTypeOrId);
    }

    public static boolean getIsSubatomic(DamageClass damageClass) {
        return ModDamageSources.isSubatomic(damageClass);
    }

    public static boolean getIsSubatomic(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.isSubatomic(modern(damageClass));
    }

    public static boolean is(DamageSource source, ResourceKey<DamageType> type) {
        return ModDamageSources.is(source, type);
    }

    public static boolean is(DamageSource source, String legacyTypeOrId) {
        return ModDamageSources.is(source, legacyTypeOrId);
    }

    public static boolean is(DamageSource source, DamageClass damageClass) {
        return ModDamageSources.sourceMatches(source, damageClass);
    }

    public static boolean is(DamageSource source, com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.sourceMatches(source, modern(damageClass));
    }

    public static boolean sourceMatches(DamageSource source, ResourceKey<DamageType> expected) {
        return ModDamageSources.sourceMatches(source, expected);
    }

    public static boolean sourceMatches(DamageSource source, String expectedLegacyTypeOrId) {
        return ModDamageSources.sourceMatches(source, expectedLegacyTypeOrId);
    }

    public static boolean sourceMatches(DamageSource source, DamageClass expectedDamageClass) {
        return ModDamageSources.sourceMatches(source, expectedDamageClass);
    }

    public static boolean sourceMatches(DamageSource source,
            com.hbm.util.DamageResistanceHandler.DamageClass expectedDamageClass) {
        return ModDamageSources.sourceMatches(source, modern(expectedDamageClass));
    }

    public static boolean matches(ResourceKey<DamageType> actual, ResourceKey<DamageType> expected) {
        return ModDamageSources.matches(actual, expected);
    }

    public static boolean matches(ResourceKey<DamageType> actual, String expectedLegacyTypeOrId) {
        return ModDamageSources.matches(actual, expectedLegacyTypeOrId);
    }

    public static boolean matches(ResourceKey<DamageType> actual, DamageClass expectedDamageClass) {
        return ModDamageSources.matches(actual, expectedDamageClass);
    }

    public static boolean matches(ResourceKey<DamageType> actual,
            com.hbm.util.DamageResistanceHandler.DamageClass expectedDamageClass) {
        return ModDamageSources.matches(actual, modern(expectedDamageClass));
    }

    public static boolean matches(String actualLegacyTypeOrId, ResourceKey<DamageType> expected) {
        return ModDamageSources.matches(actualLegacyTypeOrId, expected);
    }

    public static boolean matches(String actualLegacyTypeOrId, String expectedLegacyTypeOrId) {
        return ModDamageSources.matches(actualLegacyTypeOrId, expectedLegacyTypeOrId);
    }

    public static boolean matches(String actualLegacyTypeOrId, DamageClass expectedDamageClass) {
        return ModDamageSources.matches(actualLegacyTypeOrId, expectedDamageClass);
    }

    public static boolean matches(String actualLegacyTypeOrId,
            com.hbm.util.DamageResistanceHandler.DamageClass expectedDamageClass) {
        return ModDamageSources.matches(actualLegacyTypeOrId, modern(expectedDamageClass));
    }

    public static boolean matches(DamageClass actualDamageClass, ResourceKey<DamageType> expected) {
        return ModDamageSources.matches(actualDamageClass, expected);
    }

    public static boolean matches(DamageClass actualDamageClass, String expectedLegacyTypeOrId) {
        return ModDamageSources.matches(actualDamageClass, expectedLegacyTypeOrId);
    }

    public static boolean matches(DamageClass actualDamageClass, DamageClass expectedDamageClass) {
        return ModDamageSources.matches(actualDamageClass, expectedDamageClass);
    }

    public static boolean matches(DamageClass actualDamageClass,
            com.hbm.util.DamageResistanceHandler.DamageClass expectedDamageClass) {
        return ModDamageSources.matches(actualDamageClass, modern(expectedDamageClass));
    }

    public static boolean matches(com.hbm.util.DamageResistanceHandler.DamageClass actualDamageClass,
            ResourceKey<DamageType> expected) {
        return ModDamageSources.matches(modern(actualDamageClass), expected);
    }

    public static boolean matches(com.hbm.util.DamageResistanceHandler.DamageClass actualDamageClass,
            String expectedLegacyTypeOrId) {
        return ModDamageSources.matches(modern(actualDamageClass), expectedLegacyTypeOrId);
    }

    public static boolean matches(com.hbm.util.DamageResistanceHandler.DamageClass actualDamageClass,
            DamageClass expectedDamageClass) {
        return ModDamageSources.matches(modern(actualDamageClass), expectedDamageClass);
    }

    public static boolean matches(com.hbm.util.DamageResistanceHandler.DamageClass actualDamageClass,
            com.hbm.util.DamageResistanceHandler.DamageClass expectedDamageClass) {
        return ModDamageSources.matches(modern(actualDamageClass), modern(expectedDamageClass));
    }

    public static boolean isProjectile(DamageSource source) {
        return ModDamageSources.isProjectile(source);
    }

    public static boolean isProjectile(ResourceKey<DamageType> type) {
        return ModDamageSources.isProjectile(type);
    }

    public static boolean isProjectile(String legacyTypeOrId) {
        return ModDamageSources.isProjectile(legacyTypeOrId);
    }

    public static boolean isProjectile(DamageClass damageClass) {
        return ModDamageSources.isProjectile(damageClass);
    }

    public static boolean isProjectile(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.isProjectile(modern(damageClass));
    }

    public static boolean isExplosion(DamageSource source) {
        return ModDamageSources.isExplosion(source);
    }

    public static boolean isExplosion(ResourceKey<DamageType> type) {
        return ModDamageSources.isExplosion(type);
    }

    public static boolean isExplosion(String legacyTypeOrId) {
        return ModDamageSources.isExplosion(legacyTypeOrId);
    }

    public static boolean isExplosion(DamageClass damageClass) {
        return ModDamageSources.isExplosion(damageClass);
    }

    public static boolean isExplosion(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.isExplosion(modern(damageClass));
    }

    public static boolean isFireDamage(DamageSource source) {
        return ModDamageSources.isFireDamage(source);
    }

    public static boolean isFireDamage(ResourceKey<DamageType> type) {
        return ModDamageSources.isFireDamage(type);
    }

    public static boolean isFireDamage(String legacyTypeOrId) {
        return ModDamageSources.isFireDamage(legacyTypeOrId);
    }

    public static boolean isFireDamage(DamageClass damageClass) {
        return ModDamageSources.isFireDamage(damageClass);
    }

    public static boolean isFireDamage(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.isFireDamage(modern(damageClass));
    }

    public static boolean isUnblockable(DamageSource source) {
        return ModDamageSources.isUnblockable(source);
    }

    public static boolean isUnblockable(ResourceKey<DamageType> type) {
        return ModDamageSources.isUnblockable(type);
    }

    public static boolean isUnblockable(String legacyTypeOrId) {
        return ModDamageSources.isUnblockable(legacyTypeOrId);
    }

    public static boolean isUnblockable(DamageClass damageClass) {
        return ModDamageSources.isUnblockable(damageClass);
    }

    public static boolean isUnblockable(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.isUnblockable(modern(damageClass));
    }

    public static boolean isDamageAbsolute(DamageSource source) {
        return ModDamageSources.isDamageAbsolute(source);
    }

    public static boolean isDamageAbsolute(ResourceKey<DamageType> type) {
        return ModDamageSources.isDamageAbsolute(type);
    }

    public static boolean isDamageAbsolute(String legacyTypeOrId) {
        return ModDamageSources.isDamageAbsolute(legacyTypeOrId);
    }

    public static boolean isDamageAbsolute(DamageClass damageClass) {
        return ModDamageSources.isDamageAbsolute(damageClass);
    }

    public static boolean isDamageAbsolute(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.isDamageAbsolute(modern(damageClass));
    }

    public static boolean bypassesEffects(DamageSource source) {
        return ModDamageSources.bypassesEffects(source);
    }

    public static boolean bypassesEffects(ResourceKey<DamageType> type) {
        return ModDamageSources.bypassesEffects(type);
    }

    public static boolean bypassesEffects(String legacyTypeOrId) {
        return ModDamageSources.bypassesEffects(legacyTypeOrId);
    }

    public static boolean bypassesEffects(DamageClass damageClass) {
        return ModDamageSources.bypassesEffects(damageClass);
    }

    public static boolean bypassesEffects(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.bypassesEffects(modern(damageClass));
    }

    public static boolean isDamageAllowedInCreativeMode(DamageSource source) {
        return ModDamageSources.isDamageAllowedInCreativeMode(source);
    }

    public static boolean isDamageAllowedInCreativeMode(ResourceKey<DamageType> type) {
        return ModDamageSources.isDamageAllowedInCreativeMode(type);
    }

    public static boolean isDamageAllowedInCreativeMode(String legacyTypeOrId) {
        return ModDamageSources.isDamageAllowedInCreativeMode(legacyTypeOrId);
    }

    public static boolean isDamageAllowedInCreativeMode(DamageClass damageClass) {
        return ModDamageSources.isDamageAllowedInCreativeMode(damageClass);
    }

    public static boolean isDamageAllowedInCreativeMode(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.isDamageAllowedInCreativeMode(modern(damageClass));
    }

    public static Optional<ModDamageSources.LegacyDamageType> legacyDamageType(DamageSource source) {
        return ModDamageSources.legacyDamageType(source);
    }

    public static Optional<ModDamageSources.LegacyDamageType> legacyDamageType(ResourceKey<DamageType> type) {
        return ModDamageSources.legacyDamageType(type);
    }

    public static Optional<ModDamageSources.LegacyDamageType> legacyDamageType(String legacyTypeOrId) {
        return ModDamageSources.legacyDamageType(legacyTypeOrId);
    }

    public static List<String> actualTagLabels(DamageSource source) {
        return ModDamageSources.actualTagLabels(source);
    }

    public static List<String> expectedTagLabels(ResourceKey<DamageType> type) {
        return ModDamageSources.expectedTagLabels(type);
    }

    public static List<String> expectedTagLabels(String legacyTypeOrId) {
        return ModDamageSources.expectedTagLabels(legacyTypeOrId);
    }

    public static List<String> expectedTagLabels(DamageClass damageClass) {
        return ModDamageSources.expectedTagLabels(damageClass);
    }

    public static List<String> expectedTagLabels(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.expectedTagLabels(modern(damageClass));
    }

    public static String getDamageType(DamageSource source) {
        return ModDamageSources.damageType(source);
    }

    public static String getDamageType(ResourceKey<DamageType> type) {
        return ModDamageSources.damageType(type);
    }

    public static String getDamageType(String legacyTypeOrId) {
        return ModDamageSources.damageType(legacyTypeOrId);
    }

    public static String getDamageType(DamageClass damageClass) {
        return ModDamageSources.damageType(damageClass);
    }

    public static String getDamageType(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return ModDamageSources.damageType(modern(damageClass));
    }

    @Nullable
    public static Entity getEntity(DamageSource source) {
        return ModDamageSources.getEntity(source);
    }

    @Nullable
    public static Entity getSourceOfDamage(DamageSource source) {
        return ModDamageSources.getSourceOfDamage(source);
    }

    public static FacadeAudit facadeAudit() {
        List<String> problems = new ArrayList<>();
        expect(problems, "radiation key", radiation.equals(ModDamageSources.RADIATION));
        expect(problems, "digamma key", digamma.equals(ModDamageSources.DIGAMMA));
        expect(problems, "electricity key", electricity.equals(ModDamageSources.ELECTRICITY));
        expect(problems, "turbofan key maps blender", turbofan.equals(ModDamageSources.BLENDER));
        expect(problems, "blacklung key", blacklung.equals(ModDamageSources.BLACK_LUNG));
        expect(problems, "bullet string resolves", ModDamageSources.legacyKey(s_bullet)
                .filter(ModDamageSources.REVOLVER_BULLET::equals).isPresent());
        expect(problems, "emplacer string resolves", ModDamageSources.legacyKey(s_emplacer)
                .filter(ModDamageSources.CHOPPER_BULLET::equals).isPresent());
        expect(problems, "subatomic prefix resolves", ModDamageSources.legacyKey(s_zomg_prefix + "3")
                .filter(ModDamageSources.SUBATOMIC::equals).isPresent());
        expect(problems, "emp string resolves", ModDamageSources.legacyKey(s_emp)
                .filter(ModDamageSources.ELECTRICITY::equals).isPresent());
        expect(problems, "null key compare false", !is(null, radiation));
        expect(problems, "null string compare false", !is(null, s_laser));
        expect(problems, "null projectile false", !isProjectile((DamageSource) null));
        expect(problems, "null unblockable false", !isUnblockable((DamageSource) null));
        expect(problems, "null damage type empty", getDamageType((DamageSource) null).isEmpty());
        expect(problems, "null entity helpers missing",
                getEntity(null) == null && getSourceOfDamage(null) == null);
        expect(problems, "null legacy type missing", legacyDamageType((DamageSource) null).isEmpty());
        expect(problems, "null actual tags empty", actualTagLabels(null).isEmpty());
        expect(problems, "indirect key alias", ModDamageSources.legacyKey(s_tau)
                .filter(ModDamageSources.TAU::equals).isPresent());
        expect(problems, "tau string projectile", isProjectile(s_tau));
        expect(problems, "tau string unblockable", isUnblockable(s_tau));
        expect(problems, "tau helper overloads",
                getIsTau(s_tau) && getIsTau(ModDamageSources.TAU)
                        && !getIsTau(com.hbm.util.DamageResistanceHandler.DamageClass.SUBATOMIC));
        expect(problems, "subatomic helper overloads",
                getIsSubatomic(s_zomg_prefix + "5") && getIsSubatomic(ModDamageSources.SUBATOMIC)
                        && getIsSubatomic(com.hbm.util.DamageResistanceHandler.DamageClass.SUBATOMIC)
                        && !getIsSubatomic(s_tau));
        expect(problems, "source match overloads",
                matches(s_combineball, ModDamageSources.COMBINE_BALL)
                        && matches(ModDamageSources.COMBINE_BALL, s_combineball)
                        && matches(s_combineball, "combineBall")
                        && !matches(ModDamageSources.TAU, s_combineball));
        expect(problems, "damage class match overloads",
                matches(com.hbm.util.DamageResistanceHandler.DamageClass.PHYSICAL, ModDamageSources.REVOLVER_BULLET)
                        && matches(DamageClass.SUBATOMIC, s_zomg_prefix + "2")
                        && !matches(com.hbm.util.DamageResistanceHandler.DamageClass.LASER, DamageClass.PLASMA));
        expect(problems, "digamma string absolute", isDamageAbsolute("digamma") && bypassesEffects("digamma"));
        expect(problems, "nitan string creative", isDamageAllowedInCreativeMode("nitan"));
        expect(problems, "subatomic expected damage type", getDamageType(s_zomg_prefix + "4").equals("subAtomic"));
        expect(problems, "combine expected tags", expectedTagLabels(s_combineball)
                .equals(List.of("projectile", "bypassesArmor")));
        expect(problems, "damage class physical projectile",
                getDamageType(DamageClass.PHYSICAL).equals("revolverBullet") && isProjectile(DamageClass.PHYSICAL));
        expect(problems, "damage class fire tags",
                getDamageType(DamageClass.FIRE).equals("flamethrower") && isFireDamage(DamageClass.FIRE));
        expect(problems, "damage class explosive tags",
                getDamageType(DamageClass.EXPLOSIVE).equals("explosion") && isExplosion(DamageClass.EXPLOSIVE));
        expect(problems, "legacy damage class subatomic bridge",
                getDamageType(com.hbm.util.DamageResistanceHandler.DamageClass.SUBATOMIC).equals("subAtomic")
                        && isUnblockable(com.hbm.util.DamageResistanceHandler.DamageClass.SUBATOMIC));
        return new FacadeAudit(List.copyOf(problems));
    }

    private static DamageClass modern(com.hbm.util.DamageResistanceHandler.DamageClass damageClass) {
        return damageClass == null ? DamageClass.OTHER : damageClass.modern();
    }

    private static Level level(Entity direct, @Nullable Entity cause) {
        if (direct != null) {
            return direct.level();
        }
        if (cause != null) {
            return cause.level();
        }
        throw new IllegalArgumentException("A direct or causing entity is required to create an indirect damage source");
    }

    private static void expect(List<String> problems, String label, boolean ok) {
        if (!ok) {
            problems.add(label);
        }
    }

    public record FacadeAudit(List<String> problems) {
        public boolean passed() {
            return problems.isEmpty();
        }
    }
}
