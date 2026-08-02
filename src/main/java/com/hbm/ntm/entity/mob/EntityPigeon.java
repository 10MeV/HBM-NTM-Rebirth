package com.hbm.ntm.entity.mob;

import com.hbm.ntm.item.FertilizerItem;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.util.HbmModelRenderDistances;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

/** Direct runtime migration of 1.7.10's {@code EntityPigeon}. */
public final class EntityPigeon extends PathfinderMob {
    public static final int STATE_WALKING = 0;
    public static final int STATE_FLYING = 1;

    private static final EntityDataAccessor<Byte> FLYING_STATE =
            SynchedEntityData.defineId(EntityPigeon.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> FAT =
            SynchedEntityData.defineId(EntityPigeon.class, EntityDataSerializers.BYTE);

    public float fallTime;
    public float dest;
    public float prevDest;
    public float prevFallTime;
    public float offGroundTimer = 1.0F;

    public EntityPigeon(EntityType<? extends EntityPigeon> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // The legacy class did not override EntityCreature's base attributes.
        return PathfinderMob.createMobAttributes();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FLYING_STATE, (byte) STATE_WALKING);
        entityData.define(FAT, (byte) 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new StartFlyingGoal());
        goalSelector.addGoal(0, new StopFlyingGoal());
        goalSelector.addGoal(1, new ConditionalFloatGoal());
        goalSelector.addGoal(2, new EatBreadGoal());
        goalSelector.addGoal(5, new ConditionalWanderGoal());
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (getFlyingState() == STATE_FLYING) {
            int surfaceY = level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(getX()), Mth.floor(getZ()));
            boolean ceilingReached = getY() - surfaceY > 10.0D;
            double verticalSpeed = random.nextGaussian() * 0.05D + (ceilingReached ? 0.0D : 0.04D)
                    + (isInWater() ? 0.2D : 0.0D);
            if (onGround()) {
                verticalSpeed = Math.abs(verticalSpeed) + 0.1D;
            }
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x, verticalSpeed, motion.z);
            zza = 1.5F;
            if (random.nextInt(20) == 0) {
                setYRot(getYRot() + (float) (random.nextGaussian() * 30.0D));
            }
            if (isFat() && random.nextInt(50) == 0) {
                ParticleUtil.spawnSweat(this, Blocks.WHITE_WOOL, 0, 3, 50.0D);
                fertilizeBelow();
                if (random.nextInt(10) == 0) {
                    setFat(false);
                }
            }
        } else if (!onGround() && getDeltaMovement().y < 0.0D) {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x, motion.y * 0.8D, motion.z);
        }
    }

    private void fertilizeBelow() {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        int x = Mth.floor(getX());
        int y = Mth.floor(getY()) - 1;
        int z = Mth.floor(getZ());
        Player player = FakePlayerFactory.getMinecraft(server);
        for (int offset = 0; offset < 25; offset++) {
            BlockPos pos = new BlockPos(x, y - offset, z);
            if (FertilizerItem.fertilize(server, pos, player, true)) {
                server.levelEvent(2005, pos, 0);
                break;
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        prevFallTime = fallTime;
        prevDest = dest;
        dest += (onGround() ? -1.0F : 4.0F) * 0.3F;
        dest = Mth.clamp(dest, 0.0F, 1.0F);
        if (!onGround() && offGroundTimer < 1.0F) {
            offGroundTimer = 1.0F;
        }
        offGroundTimer *= 0.9F;
        if (!onGround() && getDeltaMovement().y < 0.0D) {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x, motion.y * 0.6D, motion.z);
        }
        fallTime += offGroundTimer * 2.0F;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (amount >= getMaxHealth() * 2.0F && !level().isClientSide()) {
            discard();
            for (int index = 0; index < 10; index++) {
                Vec3 direction = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize();
                ItemEntity feather = new ItemEntity(level(), getX() + direction.x, getY() + getBbHeight() / 2.0D + direction.y,
                        getZ() + direction.z, new ItemStack(Items.FEATHER));
                feather.setDeltaMovement(direction.scale(0.5D));
                level().addFreshEntity(feather);
            }
            return true;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        int feathers = random.nextInt(3) + random.nextInt(1 + looting);
        for (int index = 0; index < feathers; index++) {
            spawnAtLocation(Items.FEATHER);
        }
        spawnAtLocation(isOnFire() ? Items.COOKED_CHICKEN : Items.CHICKEN, isFat() ? 3 : 1);
    }

    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }

    @Override
    protected SoundEvent getDeathSound() { return null; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean isIgnoringBlockTriggers() { return true; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance); }

    public int getFlyingState() { return entityData.get(FLYING_STATE); }
    public void setFlyingState(int state) { entityData.set(FLYING_STATE, (byte) state); }
    public boolean isFat() { return entityData.get(FAT) == 1; }
    public void setFat(boolean fat) { entityData.set(FAT, (byte) (fat ? 1 : 0)); }

    private final class StartFlyingGoal extends Goal {
        @Override public boolean canUse() {
            return getFlyingState() == STATE_WALKING
                    && (getTarget() != null || isOnFire() || random.nextInt(600) == 0);
        }
        @Override public void start() { setFlyingState(STATE_FLYING); }
    }

    private final class StopFlyingGoal extends Goal {
        @Override public boolean canUse() { return getFlyingState() == STATE_FLYING && random.nextInt(200) == 0; }
        @Override public void start() { setFlyingState(STATE_WALKING); }
    }

    private final class ConditionalFloatGoal extends Goal {
        private ConditionalFloatGoal() { setFlags(EnumSet.of(Flag.JUMP)); }
        @Override public boolean canUse() { return getFlyingState() == STATE_WALKING && (isInWater() || isInLava()); }
        @Override public void tick() { if (random.nextFloat() < 0.8F) getJumpControl().jump(); }
    }

    private final class EatBreadGoal extends Goal {
        private ItemEntity item;

        private EatBreadGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }

        @Override
        public boolean canUse() {
            if (isFat() || getFlyingState() != STATE_WALKING) {
                return false;
            }
            for (ItemEntity candidate : level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(10.0D))) {
                if (candidate.getItem().is(Items.BREAD)) {
                    item = candidate;
                    return true;
                }
            }
            return false;
        }

        @Override public boolean canContinueToUse() { return item != null && item.isAlive() && canUse(); }

        @Override
        public void tick() {
            if (item == null) {
                return;
            }
            getLookControl().setLookAt(item, 30.0F, getMaxHeadXRot());
            if (distanceToSqr(item) > 1.0D) {
                getNavigation().moveTo(item, 0.4D);
                return;
            }
            if (random.nextInt(3) == 0) {
                ItemStack stack = item.getItem();
                if (stack.getCount() > 1) {
                    ItemStack remainder = stack.copy();
                    remainder.shrink(1);
                    level().addFreshEntity(new ItemEntity(level(), item.getX(), item.getY(), item.getZ(), remainder));
                }
                item.discard();
            }
            setFat(true);
            playSound(SoundEvents.GENERIC_EAT, 0.5F + 0.5F * random.nextInt(2),
                    (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
        }
    }

    private final class ConditionalWanderGoal extends RandomStrollGoal {
        private ConditionalWanderGoal() { super(EntityPigeon.this, 0.2D); }
        @Override public boolean canUse() {
            return getFlyingState() == STATE_WALKING && getNoActionTime() < 100 && super.canUse();
        }
        @Override public boolean canContinueToUse() {
            return getFlyingState() == STATE_WALKING && super.canContinueToUse();
        }
    }
}
