package com.hbm.ntm.entity.ai;

import com.hbm.ntm.item.SednaGunItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

/**
 * Forge 1.20.1 counterpart of 1.7.10 {@code EntityAIFireGun}.  It intentionally
 * owns only mob pursuit/burst timing; Sedna's state, reload and projectile work
 * remains in {@link SednaGunItem}.
 */
public final class SednaGunMobAttackGoal extends Goal {
    private final Mob host;
    private int attackTimer;
    private FireState state = FireState.IDLE;
    private int stateTimer;

    public double attackMoveSpeed = 1.0D;
    public double maxRange = 20.0D;
    public int burstTime = 10;
    public int minWait = 10;
    public int maxWait = 40;
    public boolean randomBurst = true;

    public SednaGunMobAttackGoal(Mob host) {
        this.host = host;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static void ensureAttached(Mob mob) {
        if (mob == null || mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getGoal() instanceof SednaGunMobAttackGoal)) {
            return;
        }
        mob.goalSelector.addGoal(3, new SednaGunMobAttackGoal(mob));
        mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    public boolean canUse() {
        return host.getTarget() != null && gun(host.getMainHandItem()) != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() && host.getTarget().isAlive();
    }

    @Override
    public void stop() {
        host.getNavigation().stop();
        attackTimer = 0;
        state = FireState.IDLE;
        stateTimer = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = host.getTarget();
        ItemStack stack = host.getMainHandItem();
        SednaGunItem gun = gun(stack);
        if (target == null || gun == null) {
            return;
        }

        double distanceSquared = host.distanceToSqr(target);
        boolean canSeeTarget = host.getSensing().hasLineOfSight(target);
        attackTimer = canSeeTarget ? attackTimer + 1 : 0;

        if (distanceSquared < maxRange * maxRange && attackTimer > 20) {
            host.getNavigation().stop();
        } else {
            host.getNavigation().moveTo(target, attackMoveSpeed);
        }
        host.getLookControl().setLookAt(target, 30.0F, 30.0F);

        gun.tickNpcGunRuntime(host, target, stack, state == FireState.FIRING);
        if (--stateTimer < 0) {
            stateTimer = 0;
            if (state == FireState.WAIT) {
                state = FireState.IDLE;
            } else if (state != FireState.IDLE) {
                state = FireState.WAIT;
                stateTimer = waitDuration();
            }
        }

        if (!canSeeTarget || distanceSquared >= maxRange * maxRange || state != FireState.IDLE) {
            return;
        }
        if (!gun.npcHasLoadedRound(stack)) {
            if (gun.npcBeginReload(stack)) {
                state = FireState.RELOADING;
                stateTimer = 20;
            }
            return;
        }
        if (gun.npcPressPrimary(host, target, stack)) {
            state = FireState.FIRING;
            stateTimer = randomBurst ? host.getRandom().nextInt(Math.max(1, burstTime)) : burstTime;
        }
    }

    private int waitDuration() {
        int min = Math.max(0, minWait);
        int max = Math.max(min + 1, maxWait);
        return host.getRandom().nextInt(max - min) + min;
    }

    private static SednaGunItem gun(ItemStack stack) {
        return stack.getItem() instanceof SednaGunItem gun && gun.supportsNpcGunRuntime(stack) ? gun : null;
    }

    private enum FireState {
        IDLE,
        WAIT,
        FIRING,
        RELOADING
    }
}
