package com.hbm.ntm.damage;

import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class EntityDamageUtil {
    public static final String OUTCOME_DAMAGED = "damaged";
    public static final String OUTCOME_FULLY_ABSORBED = "fully_absorbed";
    public static final String OUTCOME_CANCELED = "canceled";
    public static final String OUTCOME_NON_POSITIVE_AMOUNT = "non_positive_amount";
    public static final String OUTCOME_PVP_DENIED = "pvp_denied";
    public static final String OUTCOME_SKIPPED = "skipped";

    private static final ThreadLocal<Boolean> ALLOW_SPECIAL_CANCEL = ThreadLocal.withInitial(() -> Boolean.TRUE);

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount) {
        return attackEntityFromNt(entity, source, amount, 0.0F, 0.0F);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, type), amount);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, legacyTypeOrId), amount);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, source, amount, false, true, 0.0D, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            float pierceDt, float pierceDr) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, type), amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            float pierceDt, float pierceDr) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, legacyTypeOrId), amount, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame) {
        return attackEntityFromNt(entity, source, amount, ignoreIFrame, true, 0.0D, 0.0F, 0.0F);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, type), amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, legacyTypeOrId), amount, ignoreIFrame);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, source, amount, ignoreIFrame, true, 0.0D, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, float pierceDt, float pierceDr) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, type), amount, ignoreIFrame, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, float pierceDt, float pierceDr) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, legacyTypeOrId), amount, ignoreIFrame, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, source, amount, ignoreIFrame, true, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, type), amount, ignoreIFrame, knockbackMultiplier,
                pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, double knockbackMultiplier, float pierceDt, float pierceDr) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, legacyTypeOrId), amount, ignoreIFrame,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNtDetailed(entity, source, amount, ignoreIFrame, allowSpecialCancel, knockbackMultiplier,
                pierceDt, pierceDr).damaged();
    }

    public static boolean attackEntityFromNt(Entity entity, ResourceKey<DamageType> type, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, type), amount, ignoreIFrame, allowSpecialCancel,
                knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, String legacyTypeOrId, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromNt(entity, sourceFor(entity, legacyTypeOrId), amount, ignoreIFrame,
                allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static DamageApplication attackEntityFromNtDetailed(Entity entity, DamageSource source, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        if (amount <= 0.0F) {
            return DamageApplication.skipped(amount, 0.0F, OUTCOME_NON_POSITIVE_AMOUNT);
        }
        if (!(entity instanceof LivingEntity)) {
            boolean damaged = entity.hurt(source, amount);
            return new DamageApplication(damaged, false, amount, damaged ? amount : 0.0F,
                    outcome(damaged, false));
        }

        LivingEntity living = (LivingEntity) entity;
        if (!canDamagePlayer(living, source)) {
            return DamageApplication.skipped(amount, 0.0F, OUTCOME_PVP_DENIED);
        }

        int invulnerableTime = living.invulnerableTime;
        Vec3 previousMovement = living.getDeltaMovement();
        if (ignoreIFrame) {
            living.invulnerableTime = 0;
        }

        DamageResistanceHandler.ResistanceBreakdown breakdown =
                DamageResistanceHandler.breakdown(living, source, amount, pierceDt, pierceDr);
        DamageResistanceHandler.PierceState previousPiercing = DamageResistanceHandler.capturePiercing();
        DamageResistanceHandler.setup(pierceDt, pierceDr);
        boolean previousAllowSpecialCancel = ALLOW_SPECIAL_CANCEL.get();
        ALLOW_SPECIAL_CANCEL.set(allowSpecialCancel);
        try {
            boolean result = living.hurt(source, amount);
            if (!result && ignoreIFrame && invulnerableTime > 0) {
                living.invulnerableTime = 0;
                result = living.hurt(source, amount);
            }
            if (result && knockbackMultiplier > 0.0D) {
                living.setDeltaMovement(previousMovement);
                applyKnockback(living, source.getEntity(), knockbackMultiplier);
            }
            boolean fullyAbsorbed = breakdown.fullyAbsorbed(amount);
            return new DamageApplication(result, fullyAbsorbed, amount,
                    result ? breakdown.finalDamage() : 0.0F, outcome(result, fullyAbsorbed));
        } finally {
            ALLOW_SPECIAL_CANCEL.set(previousAllowSpecialCancel);
            if (ignoreIFrame) {
                living.invulnerableTime = Math.min(living.invulnerableTime, invulnerableTime);
            }
            DamageResistanceHandler.restorePiercing(previousPiercing);
        }
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity entity, DamageSource source, float amount) {
        return attackEntityFromNt(entity, source, amount, true, true, 0.0D, 0.0F, 0.0F);
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity entity, ResourceKey<DamageType> type, float amount) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromIgnoreIFrame(entity, sourceFor(entity, type), amount);
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity entity, String legacyTypeOrId, float amount) {
        if (amount <= 0.0F) {
            return false;
        }
        return attackEntityFromIgnoreIFrame(entity, sourceFor(entity, legacyTypeOrId), amount);
    }

    public static boolean allowSpecialCancel() {
        return ALLOW_SPECIAL_CANCEL.get();
    }

    public static ApplicationAudit applicationAudit() {
        List<String> problems = new ArrayList<>();
        expect(problems, "damaged outcome wins", OUTCOME_DAMAGED.equals(outcome(true, true)));
        expect(problems, "absorbed outcome", OUTCOME_FULLY_ABSORBED.equals(outcome(false, true)));
        expect(problems, "canceled outcome", OUTCOME_CANCELED.equals(outcome(false, false)));
        DamageApplication skipped = DamageApplication.skipped(-1.0F, 0.0F, OUTCOME_NON_POSITIVE_AMOUNT);
        expect(problems, "skipped keeps requested amount", nearly(skipped.requestedAmount(), -1.0F));
        expect(problems, "skipped final amount zero", nearly(skipped.finalAmount(), 0.0F));
        expect(problems, "skipped not damaged", !skipped.damaged() && !skipped.fullyAbsorbed());
        expect(problems, "skipped should not retry i-frame", !skipped.shouldRetryIgnoringIFrames());
        expect(problems, "absorbed should not retry i-frame",
                !new DamageApplication(false, true, 5.0F, 0.0F, OUTCOME_FULLY_ABSORBED).shouldRetryIgnoringIFrames());
        expect(problems, "canceled may retry i-frame",
                new DamageApplication(false, false, 5.0F, 0.0F, OUTCOME_CANCELED).shouldRetryIgnoringIFrames());
        expect(problems, "damaged should not retry i-frame",
                !new DamageApplication(true, false, 5.0F, 3.0F, OUTCOME_DAMAGED).shouldRetryIgnoringIFrames());
        expect(problems, "legacy util facade skips non-positive key",
                !com.hbm.util.EntityDamageUtil.attackEntityFromNT(null, com.hbm.lib.ModDamageSource.radiation, -1.0F));
        expect(problems, "legacy util facade skips non-positive string",
                !com.hbm.util.EntityDamageUtil.attackEntityFromIgnoreIFrame(null,
                        com.hbm.lib.ModDamageSource.s_emp, 0.0F));

        boolean previousAllowSpecialCancel = ALLOW_SPECIAL_CANCEL.get();
        try {
            ALLOW_SPECIAL_CANCEL.set(false);
            expect(problems, "allow special cancel false state", !allowSpecialCancel());
        } finally {
            ALLOW_SPECIAL_CANCEL.set(previousAllowSpecialCancel);
        }
        expect(problems, "allow special cancel restored", allowSpecialCancel() == previousAllowSpecialCancel);
        return new ApplicationAudit(List.copyOf(problems));
    }

    public record DamageApplication(boolean damaged, boolean fullyAbsorbed, float requestedAmount, float finalAmount,
                                    String outcome) {
        public boolean shouldRetryIgnoringIFrames() {
            return !damaged && !fullyAbsorbed && OUTCOME_CANCELED.equals(outcome);
        }

        private static DamageApplication skipped(float requestedAmount, float finalAmount, String outcome) {
            return new DamageApplication(false, false, requestedAmount, finalAmount, outcome);
        }
    }

    public record ApplicationAudit(List<String> problems) {
        public boolean passed() {
            return problems.isEmpty();
        }
    }

    private static String outcome(boolean damaged, boolean fullyAbsorbed) {
        if (damaged) {
            return OUTCOME_DAMAGED;
        }
        return fullyAbsorbed ? OUTCOME_FULLY_ABSORBED : OUTCOME_CANCELED;
    }

    private static boolean canDamagePlayer(LivingEntity target, DamageSource source) {
        if (!(target instanceof Player player) || !(source.getEntity() instanceof Player attacker)) {
            return true;
        }
        return player.canHarmPlayer(attacker);
    }

    private static DamageSource sourceFor(Entity entity, ResourceKey<DamageType> type) {
        if (entity == null) {
            throw new IllegalArgumentException("A target entity is required to create damage source " + type.location());
        }
        return ModDamageSources.source(entity.level(), type);
    }

    private static DamageSource sourceFor(Entity entity, String legacyTypeOrId) {
        if (entity == null) {
            throw new IllegalArgumentException("A target entity is required to create damage source " + legacyTypeOrId);
        }
        return ModDamageSources.source(entity.level(), legacyTypeOrId);
    }

    private static void applyKnockback(LivingEntity target, Entity attacker, double multiplier) {
        if (attacker == null) {
            return;
        }
        double resistance = target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        if (target.getRandom().nextDouble() < resistance) {
            return;
        }

        double deltaX = attacker.getX() - target.getX();
        double deltaZ = attacker.getZ() - target.getZ();
        while (deltaX * deltaX + deltaZ * deltaZ < 1.0E-4D) {
            deltaX = (target.getRandom().nextDouble() - target.getRandom().nextDouble()) * 0.01D;
            deltaZ = (target.getRandom().nextDouble() - target.getRandom().nextDouble()) * 0.01D;
        }
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double magnitude = 0.4D * multiplier;
        Vec3 movement = target.getDeltaMovement().scale(0.5D)
                .add(-deltaX / horizontal * magnitude, magnitude, -deltaZ / horizontal * magnitude);
        if (movement.y > 0.2D * multiplier) {
            movement = new Vec3(movement.x, 0.2D * multiplier, movement.z);
        }
        target.setDeltaMovement(movement);
        target.hurtMarked = true;
    }

    private static void expect(List<String> problems, String label, boolean ok) {
        if (!ok) {
            problems.add(label);
        }
    }

    private static boolean nearly(float actual, float expected) {
        return Math.abs(actual - expected) < 0.0001F;
    }

    private EntityDamageUtil() {
    }
}
