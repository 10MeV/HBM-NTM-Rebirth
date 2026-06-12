package com.hbm.lib;

import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

    public static DamageSource source(Level level, ResourceKey<DamageType> type, @Nullable Entity source) {
        return ModDamageSources.source(level, type, source);
    }

    public static DamageSource source(Level level, String legacyTypeOrId, @Nullable Entity source) {
        return ModDamageSources.source(level, legacyTypeOrId, source);
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

    public static boolean getIsSubatomic(DamageSource source) {
        return ModDamageSources.isSubatomic(source);
    }

    public static boolean is(DamageSource source, ResourceKey<DamageType> type) {
        return source != null && source.is(type);
    }

    public static boolean is(DamageSource source, String legacyTypeOrId) {
        return source != null && ModDamageSources.legacyKey(legacyTypeOrId)
                .filter(source::is)
                .isPresent();
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
        return new FacadeAudit(List.copyOf(problems));
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
