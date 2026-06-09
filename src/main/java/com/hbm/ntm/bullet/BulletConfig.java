package com.hbm.ntm.bullet;

import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record BulletConfig(
        String legacyName,
        BulletAmmo ammo,
        int ammoCount,
        float velocity,
        float spread,
        int wear,
        int bulletsMin,
        int bulletsMax,
        float damageMin,
        float damageMax,
        float headshotMultiplier,
        double gravity,
        int maxAge,
        boolean ricochets,
        double ricochetAngle,
        int lowerBoundRicochetChance,
        int higherBoundRicochetChance,
        double bounceModifier,
        int selfDamageDelay,
        boolean penetrates,
        boolean spectral,
        boolean breaksGlass,
        boolean liveAfterImpact,
        boolean blackPowder,
        int incendiaryTicks,
        int emp,
        boolean blockDamage,
        float explosive,
        double jolt,
        int rainbow,
        int nuke,
        int shrapnel,
        int chlorine,
        int leadChance,
        int caustic,
        List<MobEffectInstance> effects,
        boolean destroysBlocks,
        boolean instakill,
        BulletStyle style,
        int trail,
        BulletPlink plink,
        String vanillaParticle,
        String spentCasingName,
        int dischargePerShot,
        String modeName,
        String chatColorName,
        int firingRate,
        ResourceKey<DamageType> damageType,
        boolean damageProjectile,
        boolean damageFire,
        boolean damageExplosion,
        boolean damageBypass,
        Set<BulletBehaviorTag> behaviors) {

    public BulletConfig {
        ammo = ammo == null ? BulletAmmo.NOTHING : ammo;
        style = style == null ? BulletStyle.NONE : style;
        plink = plink == null ? BulletPlink.NONE : plink;
        vanillaParticle = vanillaParticle == null ? "" : vanillaParticle;
        spentCasingName = spentCasingName == null ? "" : spentCasingName;
        modeName = modeName == null ? "" : modeName;
        chatColorName = chatColorName == null ? "white" : chatColorName;
        damageType = damageType == null ? ModDamageSources.REVOLVER_BULLET : damageType;
        effects = copyEffects(effects);
        if (behaviors == null || behaviors.isEmpty()) {
            behaviors = Collections.emptySet();
        } else {
            behaviors = Collections.unmodifiableSet(EnumSet.copyOf(behaviors));
        }
    }

    public DamageSource damageSource(Level level, Entity direct, @Nullable Entity cause) {
        return direct == null
                ? ModDamageSources.source(level, damageType, cause)
                : ModDamageSources.indirect(level, damageType, direct, cause);
    }

    public boolean hasBehavior(BulletBehaviorTag behavior) {
        return behaviors.contains(behavior);
    }

    private static List<MobEffectInstance> copyEffects(List<MobEffectInstance> effects) {
        if (effects == null || effects.isEmpty()) {
            return Collections.emptyList();
        }
        List<MobEffectInstance> copy = new ArrayList<>(effects.size());
        for (MobEffectInstance effect : effects) {
            if (effect != null) {
                copy.add(new MobEffectInstance(effect));
            }
        }
        return copy.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(copy);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder(String legacyName) {
        return new Builder(legacyName);
    }

    public static final class Builder {
        private final String legacyName;
        private BulletAmmo ammo = BulletAmmo.NOTHING;
        private int ammoCount = 1;
        private float velocity;
        private float spread;
        private int wear;
        private int bulletsMin;
        private int bulletsMax;
        private float damageMin;
        private float damageMax;
        private float headshotMultiplier = 1.0F;
        private double gravity;
        private int maxAge;
        private boolean ricochets;
        private double ricochetAngle;
        private int lowerBoundRicochetChance;
        private int higherBoundRicochetChance;
        private double bounceModifier;
        private int selfDamageDelay = 5;
        private boolean penetrates;
        private boolean spectral;
        private boolean breaksGlass;
        private boolean liveAfterImpact;
        private boolean blackPowder;
        private int incendiaryTicks;
        private int emp;
        private boolean blockDamage = true;
        private float explosive;
        private double jolt;
        private int rainbow;
        private int nuke;
        private int shrapnel;
        private int chlorine;
        private int leadChance;
        private int caustic;
        private final List<MobEffectInstance> effects = new ArrayList<>();
        private boolean destroysBlocks;
        private boolean instakill;
        private BulletStyle style = BulletStyle.NONE;
        private int trail;
        private BulletPlink plink = BulletPlink.NONE;
        private String vanillaParticle = "";
        private String spentCasingName = "";
        private int dischargePerShot;
        private String modeName = "";
        private String chatColorName = "white";
        private int firingRate;
        private ResourceKey<DamageType> damageType = ModDamageSources.REVOLVER_BULLET;
        private boolean damageProjectile = true;
        private boolean damageFire;
        private boolean damageExplosion;
        private boolean damageBypass;
        private final EnumSet<BulletBehaviorTag> behaviors = EnumSet.noneOf(BulletBehaviorTag.class);

        private Builder(String legacyName) {
            this.legacyName = legacyName;
        }

        private Builder(BulletConfig config) {
            this(config.legacyName);
            this.ammo = config.ammo;
            this.ammoCount = config.ammoCount;
            this.velocity = config.velocity;
            this.spread = config.spread;
            this.wear = config.wear;
            this.bulletsMin = config.bulletsMin;
            this.bulletsMax = config.bulletsMax;
            this.damageMin = config.damageMin;
            this.damageMax = config.damageMax;
            this.headshotMultiplier = config.headshotMultiplier;
            this.gravity = config.gravity;
            this.maxAge = config.maxAge;
            this.ricochets = config.ricochets;
            this.ricochetAngle = config.ricochetAngle;
            this.lowerBoundRicochetChance = config.lowerBoundRicochetChance;
            this.higherBoundRicochetChance = config.higherBoundRicochetChance;
            this.bounceModifier = config.bounceModifier;
            this.selfDamageDelay = config.selfDamageDelay;
            this.penetrates = config.penetrates;
            this.spectral = config.spectral;
            this.breaksGlass = config.breaksGlass;
            this.liveAfterImpact = config.liveAfterImpact;
            this.blackPowder = config.blackPowder;
            this.incendiaryTicks = config.incendiaryTicks;
            this.emp = config.emp;
            this.blockDamage = config.blockDamage;
            this.explosive = config.explosive;
            this.jolt = config.jolt;
            this.rainbow = config.rainbow;
            this.nuke = config.nuke;
            this.shrapnel = config.shrapnel;
            this.chlorine = config.chlorine;
            this.leadChance = config.leadChance;
            this.caustic = config.caustic;
            this.effects.addAll(copyEffects(config.effects));
            this.destroysBlocks = config.destroysBlocks;
            this.instakill = config.instakill;
            this.style = config.style;
            this.trail = config.trail;
            this.plink = config.plink;
            this.vanillaParticle = config.vanillaParticle;
            this.spentCasingName = config.spentCasingName;
            this.dischargePerShot = config.dischargePerShot;
            this.modeName = config.modeName;
            this.chatColorName = config.chatColorName;
            this.firingRate = config.firingRate;
            this.damageType = config.damageType;
            this.damageProjectile = config.damageProjectile;
            this.damageFire = config.damageFire;
            this.damageExplosion = config.damageExplosion;
            this.damageBypass = config.damageBypass;
            this.behaviors.addAll(config.behaviors);
        }

        public Builder ammo(BulletAmmo ammo) {
            this.ammo = ammo;
            return this;
        }

        public Builder ammoCount(int ammoCount) {
            this.ammoCount = ammoCount;
            return this;
        }

        public Builder ballistics(float velocity, float spread, int wear, int bulletsMin, int bulletsMax) {
            this.velocity = velocity;
            this.spread = spread;
            this.wear = wear;
            this.bulletsMin = bulletsMin;
            this.bulletsMax = bulletsMax;
            return this;
        }

        public Builder damage(float min, float max) {
            this.damageMin = min;
            this.damageMax = max;
            return this;
        }

        public Builder headshotMultiplier(float headshotMultiplier) {
            this.headshotMultiplier = headshotMultiplier;
            return this;
        }

        public Builder physics(double gravity, int maxAge) {
            this.gravity = gravity;
            this.maxAge = maxAge;
            return this;
        }

        public Builder ricochet(boolean ricochets, double angle, int lowerChance, int higherChance, double bounceModifier) {
            this.ricochets = ricochets;
            this.ricochetAngle = angle;
            this.lowerBoundRicochetChance = lowerChance;
            this.higherBoundRicochetChance = higherChance;
            this.bounceModifier = bounceModifier;
            return this;
        }

        public Builder selfDamageDelay(int selfDamageDelay) {
            this.selfDamageDelay = selfDamageDelay;
            return this;
        }

        public Builder penetration(boolean penetrates) {
            this.penetrates = penetrates;
            return this;
        }

        public Builder spectral(boolean spectral) {
            this.spectral = spectral;
            return this;
        }

        public Builder breaksGlass(boolean breaksGlass) {
            this.breaksGlass = breaksGlass;
            return this;
        }

        public Builder liveAfterImpact(boolean liveAfterImpact) {
            this.liveAfterImpact = liveAfterImpact;
            return this;
        }

        public Builder blackPowder(boolean blackPowder) {
            this.blackPowder = blackPowder;
            return this;
        }

        public Builder incendiaryTicks(int incendiaryTicks) {
            this.incendiaryTicks = incendiaryTicks;
            return this;
        }

        public Builder emp(int emp) {
            this.emp = emp;
            return this;
        }

        public Builder blockDamage(boolean blockDamage) {
            this.blockDamage = blockDamage;
            return this;
        }

        public Builder explosive(float explosive) {
            this.explosive = explosive;
            return this;
        }

        public Builder jolt(double jolt) {
            this.jolt = jolt;
            return this;
        }

        public Builder rainbow(int rainbow) {
            this.rainbow = rainbow;
            return this;
        }

        public Builder nuke(int nuke) {
            this.nuke = nuke;
            return this;
        }

        public Builder shrapnel(int shrapnel) {
            this.shrapnel = shrapnel;
            return this;
        }

        public Builder chlorine(int chlorine) {
            this.chlorine = chlorine;
            return this;
        }

        public Builder leadChance(int leadChance) {
            this.leadChance = leadChance;
            return this;
        }

        public Builder caustic(int caustic) {
            this.caustic = caustic;
            return this;
        }

        public Builder effect(MobEffectInstance effect) {
            if (effect != null) {
                this.effects.add(new MobEffectInstance(effect));
            }
            return this;
        }

        public Builder effects(List<MobEffectInstance> effects) {
            this.effects.clear();
            this.effects.addAll(copyEffects(effects));
            return this;
        }

        public Builder destroysBlocks(boolean destroysBlocks) {
            this.destroysBlocks = destroysBlocks;
            return this;
        }

        public Builder instakill(boolean instakill) {
            this.instakill = instakill;
            return this;
        }

        public Builder appearance(BulletStyle style, int trail, BulletPlink plink, String vanillaParticle) {
            this.style = style;
            this.trail = trail;
            this.plink = plink;
            this.vanillaParticle = vanillaParticle;
            return this;
        }

        public Builder setToBolt(BulletTrail trail) {
            this.style = BulletStyle.BOLT;
            this.trail = trail.legacyId();
            return this;
        }

        public Builder spentCasingName(String spentCasingName) {
            this.spentCasingName = spentCasingName;
            return this;
        }

        public Builder energy(int dischargePerShot, String modeName, String chatColorName, int firingRate) {
            this.dischargePerShot = dischargePerShot;
            this.modeName = modeName;
            this.chatColorName = chatColorName;
            this.firingRate = firingRate;
            return this;
        }

        public Builder damageType(ResourceKey<DamageType> damageType) {
            this.damageType = damageType;
            return this;
        }

        public Builder damageFlags(boolean projectile, boolean fire, boolean explosion, boolean bypass) {
            this.damageProjectile = projectile;
            this.damageFire = fire;
            this.damageExplosion = explosion;
            this.damageBypass = bypass;
            return this;
        }

        public Builder behavior(BulletBehaviorTag behavior) {
            this.behaviors.add(behavior);
            return this;
        }

        public BulletConfig build() {
            return new BulletConfig(legacyName, ammo, ammoCount, velocity, spread, wear, bulletsMin, bulletsMax,
                    damageMin, damageMax, headshotMultiplier, gravity, maxAge, ricochets, ricochetAngle,
                    lowerBoundRicochetChance, higherBoundRicochetChance, bounceModifier, selfDamageDelay,
                    penetrates, spectral, breaksGlass, liveAfterImpact, blackPowder, incendiaryTicks, emp,
                    blockDamage, explosive, jolt, rainbow, nuke, shrapnel, chlorine, leadChance, caustic,
                    copyEffects(effects),
                    destroysBlocks, instakill, style, trail, plink, vanillaParticle, spentCasingName,
                    dischargePerShot, modeName, chatColorName, firingRate, damageType, damageProjectile,
                    damageFire, damageExplosion, damageBypass, behaviors);
        }
    }
}
