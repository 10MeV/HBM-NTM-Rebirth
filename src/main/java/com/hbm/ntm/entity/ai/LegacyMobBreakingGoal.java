package com.hbm.ntm.entity.ai;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Direct Forge 1.20.1 migration of {@code EntityAIBreaking}. */
public final class LegacyMobBreakingGoal extends Goal {
    private final Mob digger;
    @Nullable private LivingEntity target;
    @Nullable private BlockPos markedPos;
    private int digTick;
    private int scanTick;

    public LegacyMobBreakingGoal(Mob digger) {
        this.digger = digger;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        target = digger.getTarget();
        if (target == null || !digger.getNavigation().isDone() || digger.distanceTo(target) <= 1.0F
                || (!target.onGround() && digger.hasLineOfSight(target))) {
            return false;
        }

        BlockPos obstacle = nextObstacle();
        if (obstacle == null) {
            return false;
        }
        markedPos = obstacle;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (markedPos == null || !digger.isAlive()) {
            return false;
        }
        Vec3 vector = new Vec3(markedPos.getX() - digger.getX(),
                markedPos.getY() - (digger.getY() + digger.getEyeHeight()), markedPos.getZ() - digger.getZ());
        return vector.length() <= 4.0D;
    }

    @Override
    public void tick() {
        if (digger.tickCount % 10 == 0) {
            BlockPos obstacle = nextObstacle();
            if (obstacle != null) {
                markedPos = obstacle;
            }
        }
        if (markedPos == null || digger.level().isEmptyBlock(markedPos)) {
            digTick = 0;
            return;
        }

        BlockState state = digger.level().getBlockState(markedPos);
        float hardness = state.getDestroySpeed(digger.level(), markedPos);
        if (hardness < 0.0F) {
            markedPos = null;
            return;
        }

        digTick++;
        int health = ((int) hardness) / 3;
        float progress = digTick * 0.05F / health;
        if (progress >= 1.0F) {
            digTick = 0;
            digger.level().destroyBlock(markedPos, false);
            markedPos = null;
            if (target != null) {
                digger.getNavigation().moveTo(target, 1.0D);
            }
            return;
        }

        if (digTick % 5 == 0) {
            SoundType sound = state.getSoundType(digger.level(), markedPos, digger);
            digger.playSound(sound.getStepSound(), sound.getVolume() + 1.0F, sound.getPitch());
            digger.swing(InteractionHand.MAIN_HAND);
            digger.level().destroyBlockProgress(digger.getId(), markedPos, (int) (progress * 10.0F));
        }
    }

    @Override
    public void stop() {
        markedPos = null;
        digTick = 0;
    }

    @Nullable
    private BlockPos nextObstacle() {
        int width = Mth.ceil(digger.getBbWidth());
        int height = Mth.ceil(digger.getBbHeight());
        int passMax = width * width * height;
        int x = scanTick % width - width / 2;
        int y = scanTick / (width * width);
        int z = scanTick % (width * width) / width - width / 2;
        Vec3 origin = new Vec3(digger.getX() + x, digger.getY() + y, digger.getZ() + z);
        Vec3 destination = origin.add(digger.getViewVector(1.0F).scale(2.0D));
        BlockHitResult hit = digger.level().clip(new ClipContext(origin, destination,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, digger));
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            if (digger.level().getBlockState(pos).getDestroySpeed(digger.level(), pos) >= 0.0F) {
                scanTick = 0;
                return pos;
            }
        }
        scanTick = (scanTick + 1) % passMax;
        return null;
    }
}
