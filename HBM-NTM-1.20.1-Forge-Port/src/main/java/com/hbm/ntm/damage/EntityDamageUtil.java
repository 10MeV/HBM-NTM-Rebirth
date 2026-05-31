package com.hbm.ntm.damage;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class EntityDamageUtil {
    private static final ThreadLocal<Boolean> ALLOW_SPECIAL_CANCEL = ThreadLocal.withInitial(() -> Boolean.TRUE);

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount) {
        return attackEntityFromNt(entity, source, amount, 0.0F, 0.0F);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, source, amount, false, true, 0.0D, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame) {
        return attackEntityFromNt(entity, source, amount, ignoreIFrame, true, 0.0D, 0.0F, 0.0F);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, source, amount, ignoreIFrame, true, 0.0D, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNt(entity, source, amount, ignoreIFrame, true, knockbackMultiplier, pierceDt, pierceDr);
    }

    public static boolean attackEntityFromNt(Entity entity, DamageSource source, float amount, boolean ignoreIFrame,
            boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        return attackEntityFromNtDetailed(entity, source, amount, ignoreIFrame, allowSpecialCancel, knockbackMultiplier,
                pierceDt, pierceDr).damaged();
    }

    public static DamageApplication attackEntityFromNtDetailed(Entity entity, DamageSource source, float amount,
            boolean ignoreIFrame, boolean allowSpecialCancel, double knockbackMultiplier, float pierceDt, float pierceDr) {
        if (amount <= 0.0F) {
            return DamageApplication.skipped(0.0F, 0.0F, "non_positive_amount");
        }
        if (!(entity instanceof LivingEntity)) {
            boolean damaged = entity.hurt(source, amount);
            return new DamageApplication(damaged, false, amount, damaged ? amount : 0.0F, damaged ? "damaged" : "canceled");
        }

        LivingEntity living = (LivingEntity) entity;
        if (!canDamagePlayer(living, source)) {
            return DamageApplication.skipped(amount, 0.0F, "pvp_denied");
        }

        int invulnerableTime = living.invulnerableTime;
        Vec3 previousMovement = living.getDeltaMovement();
        if (ignoreIFrame) {
            living.invulnerableTime = 0;
        }

        DamageResistanceHandler.ResistanceBreakdown breakdown =
                DamageResistanceHandler.breakdown(living, source, amount, pierceDt, pierceDr);
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
            return new DamageApplication(result, breakdown.fullyAbsorbed(amount), amount,
                    result ? breakdown.finalDamage() : 0.0F, result ? "damaged" : "canceled");
        } finally {
            ALLOW_SPECIAL_CANCEL.set(previousAllowSpecialCancel);
            if (ignoreIFrame) {
                living.invulnerableTime = Math.min(living.invulnerableTime, invulnerableTime);
            }
            DamageResistanceHandler.reset();
        }
    }

    public static boolean attackEntityFromIgnoreIFrame(Entity entity, DamageSource source, float amount) {
        return attackEntityFromNt(entity, source, amount, true, true, 0.0D, 0.0F, 0.0F);
    }

    public static boolean allowSpecialCancel() {
        return ALLOW_SPECIAL_CANCEL.get();
    }

    public record DamageApplication(boolean damaged, boolean fullyAbsorbed, float requestedAmount, float finalAmount,
                                    String outcome) {
        private static DamageApplication skipped(float requestedAmount, float finalAmount, String outcome) {
            return new DamageApplication(false, false, requestedAmount, finalAmount, outcome);
        }
    }

    private static boolean canDamagePlayer(LivingEntity target, DamageSource source) {
        if (!(target instanceof Player player) || !(source.getEntity() instanceof Player attacker)) {
            return true;
        }
        return player.canHarmPlayer(attacker);
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

    private EntityDamageUtil() {
    }
}
