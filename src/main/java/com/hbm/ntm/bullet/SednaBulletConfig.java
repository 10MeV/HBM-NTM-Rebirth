package com.hbm.ntm.bullet;

import com.hbm.ntm.damage.DamageClass;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public record SednaBulletConfig(
        String legacyName,
        AmmoKind ammoKind,
        String ammoName,
        String casingItemName,
        int casingItemStackSize,
        int casingItemAmount,
        int ammoReloadCount,
        float velocity,
        float spread,
        float wear,
        int projectilesMin,
        int projectilesMax,
        ProjectileType projectileType,
        float damageMultiplier,
        float armorThresholdNegation,
        float armorPiercingPercent,
        float knockbackMultiplier,
        float headshotMultiplier,
        DamageClass damageClass,
        float ricochetAngle,
        int maxRicochetCount,
        boolean damageFalloffByPenetration,
        double gravity,
        int expires,
        boolean impactsEntities,
        boolean penetrates,
        boolean spectral,
        int selfDamageDelay,
        boolean blackPowder,
        boolean renderRotations,
        String spentCasingName,
        Set<SednaBehaviorTag> behaviors) {

    public SednaBulletConfig {
        legacyName = legacyName == null ? "" : legacyName;
        ammoKind = ammoKind == null ? AmmoKind.STANDARD : ammoKind;
        ammoName = ammoName == null ? "" : ammoName;
        casingItemName = casingItemName == null ? "" : casingItemName;
        casingItemStackSize = casingItemStackSize <= 0 ? 1 : casingItemStackSize;
        casingItemAmount = Math.max(0, casingItemAmount);
        projectileType = projectileType == null ? ProjectileType.BULLET : projectileType;
        damageClass = damageClass == null ? DamageClass.PHYSICAL : damageClass;
        spentCasingName = spentCasingName == null ? "" : spentCasingName;
        if (behaviors == null || behaviors.isEmpty()) {
            behaviors = Collections.emptySet();
        } else {
            behaviors = Collections.unmodifiableSet(EnumSet.copyOf(behaviors));
        }
    }

    public boolean hasBehavior(SednaBehaviorTag behavior) {
        return behaviors.contains(behavior);
    }

    public Builder toBuilder(String legacyName) {
        return new Builder(legacyName == null || legacyName.isBlank() ? this.legacyName : legacyName, this);
    }

    public static Builder builder(String legacyName) {
        return new Builder(legacyName);
    }

    public enum AmmoKind {
        STANDARD,
        SECRET,
        ITEM
    }

    public enum ProjectileType {
        BULLET,
        BULLET_CHUNKLOADING,
        BEAM
    }

    public static final class Builder {
        private final String legacyName;
        private AmmoKind ammoKind = AmmoKind.STANDARD;
        private String ammoName = "";
        private String casingItemName = "";
        private int casingItemStackSize = 1;
        private int casingItemAmount;
        private int ammoReloadCount = 1;
        private float velocity = 10.0F;
        private float spread = 0.0F;
        private float wear = 1.0F;
        private int projectilesMin = 1;
        private int projectilesMax = 1;
        private ProjectileType projectileType = ProjectileType.BULLET;
        private float damageMultiplier = 1.0F;
        private float armorThresholdNegation = 0.0F;
        private float armorPiercingPercent = 0.0F;
        private float knockbackMultiplier = 0.1F;
        private float headshotMultiplier = 1.25F;
        private DamageClass damageClass = DamageClass.PHYSICAL;
        private float ricochetAngle = 5.0F;
        private int maxRicochetCount = 2;
        private boolean damageFalloffByPenetration = true;
        private double gravity = 0.0D;
        private int expires = 30;
        private boolean impactsEntities = true;
        private boolean penetrates = false;
        private boolean spectral = false;
        private int selfDamageDelay = 2;
        private boolean blackPowder = false;
        private boolean renderRotations = true;
        private String spentCasingName = "";
        private final EnumSet<SednaBehaviorTag> behaviors = EnumSet.of(
                SednaBehaviorTag.STANDARD_RICOCHET, SednaBehaviorTag.STANDARD_ENTITY_HIT);

        private Builder(String legacyName) {
            this.legacyName = legacyName == null ? "" : legacyName;
        }

        private Builder(String legacyName, SednaBulletConfig config) {
            this(legacyName);
            this.ammoKind = config.ammoKind;
            this.ammoName = config.ammoName;
            this.casingItemName = config.casingItemName;
            this.casingItemStackSize = config.casingItemStackSize;
            this.casingItemAmount = config.casingItemAmount;
            this.ammoReloadCount = config.ammoReloadCount;
            this.velocity = config.velocity;
            this.spread = config.spread;
            this.wear = config.wear;
            this.projectilesMin = config.projectilesMin;
            this.projectilesMax = config.projectilesMax;
            this.projectileType = config.projectileType;
            this.damageMultiplier = config.damageMultiplier;
            this.armorThresholdNegation = config.armorThresholdNegation;
            this.armorPiercingPercent = config.armorPiercingPercent;
            this.knockbackMultiplier = config.knockbackMultiplier;
            this.headshotMultiplier = config.headshotMultiplier;
            this.damageClass = config.damageClass;
            this.ricochetAngle = config.ricochetAngle;
            this.maxRicochetCount = config.maxRicochetCount;
            this.damageFalloffByPenetration = config.damageFalloffByPenetration;
            this.gravity = config.gravity;
            this.expires = config.expires;
            this.impactsEntities = config.impactsEntities;
            this.penetrates = config.penetrates;
            this.spectral = config.spectral;
            this.selfDamageDelay = config.selfDamageDelay;
            this.blackPowder = config.blackPowder;
            this.renderRotations = config.renderRotations;
            this.spentCasingName = config.spentCasingName;
            this.behaviors.clear();
            this.behaviors.addAll(config.behaviors);
        }

        public Builder standardAmmo(String ammoName) {
            this.ammoKind = AmmoKind.STANDARD;
            this.ammoName = ammoName;
            return this;
        }

        public Builder secretAmmo(String ammoName) {
            this.ammoKind = AmmoKind.SECRET;
            this.ammoName = ammoName;
            return this;
        }

        public Builder itemAmmo(String itemName) {
            this.ammoKind = AmmoKind.ITEM;
            this.ammoName = itemName;
            return this;
        }

        public Builder casingItem(String casingItemName, int amount) {
            return casingItem(casingItemName, 1, amount);
        }

        public Builder casingItem(String casingItemName, int stackSize, int amount) {
            this.casingItemName = casingItemName;
            this.casingItemStackSize = stackSize;
            this.casingItemAmount = amount;
            return this;
        }

        public Builder reloadCount(int ammoReloadCount) {
            this.ammoReloadCount = ammoReloadCount;
            return this;
        }

        public Builder ballistics(float velocity, float spread, float wear, int projectilesMin, int projectilesMax) {
            this.velocity = velocity;
            this.spread = spread;
            this.wear = wear;
            this.projectilesMin = projectilesMin;
            this.projectilesMax = projectilesMax;
            return this;
        }

        public Builder spread(float spread) {
            this.spread = spread;
            return this;
        }

        public Builder wear(float wear) {
            this.wear = wear;
            return this;
        }

        public Builder projectiles(int amount) {
            this.projectilesMin = amount;
            this.projectilesMax = amount;
            return this;
        }

        public Builder projectiles(int min, int max) {
            this.projectilesMin = min;
            this.projectilesMax = max;
            return this;
        }

        public Builder damage(float damageMultiplier) {
            this.damageMultiplier = damageMultiplier;
            return this;
        }

        public Builder armor(float thresholdNegation, float piercingPercent) {
            this.armorThresholdNegation = thresholdNegation;
            this.armorPiercingPercent = piercingPercent;
            return this;
        }

        public Builder knockback(float knockbackMultiplier) {
            this.knockbackMultiplier = knockbackMultiplier;
            return this;
        }

        public Builder headshot(float headshotMultiplier) {
            this.headshotMultiplier = headshotMultiplier;
            return this;
        }

        public Builder damageClass(DamageClass damageClass) {
            this.damageClass = damageClass;
            return this;
        }

        public Builder ricochet(float angle, int maxRicochetCount) {
            this.ricochetAngle = angle;
            this.maxRicochetCount = maxRicochetCount;
            return this;
        }

        public Builder damageFalloffByPenetration(boolean damageFalloffByPenetration) {
            this.damageFalloffByPenetration = damageFalloffByPenetration;
            return this;
        }

        public Builder physics(double gravity, int expires) {
            this.gravity = gravity;
            this.expires = expires;
            return this;
        }

        public Builder impactsEntities(boolean impactsEntities) {
            this.impactsEntities = impactsEntities;
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

        public Builder selfDamageDelay(int selfDamageDelay) {
            this.selfDamageDelay = selfDamageDelay;
            return this;
        }

        public Builder blackPowder(boolean blackPowder) {
            this.blackPowder = blackPowder;
            return this;
        }

        public Builder renderRotations(boolean renderRotations) {
            this.renderRotations = renderRotations;
            return this;
        }

        public Builder projectileType(ProjectileType projectileType) {
            this.projectileType = projectileType;
            return this;
        }

        public Builder beam() {
            this.projectileType = ProjectileType.BEAM;
            return this;
        }

        public Builder chunkloadingBullet() {
            this.projectileType = ProjectileType.BULLET_CHUNKLOADING;
            return this;
        }

        public Builder spentCasing(String spentCasingName) {
            this.spentCasingName = spentCasingName;
            return this;
        }

        public Builder behavior(SednaBehaviorTag behavior) {
            this.behaviors.add(behavior);
            return this;
        }

        public Builder clearBehavior(SednaBehaviorTag behavior) {
            this.behaviors.remove(behavior);
            return this;
        }

        public SednaBulletConfig build() {
            return new SednaBulletConfig(legacyName, ammoKind, ammoName, casingItemName, casingItemStackSize, casingItemAmount,
                    ammoReloadCount, velocity, spread, wear, projectilesMin, projectilesMax, projectileType,
                    damageMultiplier, armorThresholdNegation, armorPiercingPercent, knockbackMultiplier,
                    headshotMultiplier, damageClass, ricochetAngle, maxRicochetCount,
                    damageFalloffByPenetration, gravity, expires, impactsEntities, penetrates, spectral,
                    selfDamageDelay, blackPowder, renderRotations, spentCasingName, behaviors);
        }
    }
}
