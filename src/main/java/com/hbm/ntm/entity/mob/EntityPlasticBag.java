package com.hbm.ntm.entity.mob;

import com.hbm.ntm.entity.item.BuoyantItemEntity;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

/** Direct 1.20.1 migration of 1.7.10's squid-derived plastic bag entity. */
public final class EntityPlasticBag extends PathfinderMob {
    private float rotation;
    private float previousRotation;
    private float randomMotionSpeed;
    private float rotationVelocity;
    private float randomMotionX;
    private float randomMotionY;
    private float randomMotionZ;

    public EntityPlasticBag(EntityType<? extends EntityPlasticBag> type, Level level) {
        super(type, level);
        refreshDimensions();
        rotationVelocity = 1.0F / (random.nextFloat() + 1.0F) * 0.2F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide()) {
            discard();
            BuoyantItemEntity item = new BuoyantItemEntity(level(), getX(), getY(), getZ(),
                    new ItemStack(ModItems.PLASTIC_BAG.get()));
            item.setPickUpDelay(10);
            level().addFreshEntity(item);
        }
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        previousRotation = rotation;
        rotation += rotationVelocity;
        if (rotation > Mth.TWO_PI) {
            rotation -= Mth.TWO_PI;
            if (random.nextInt(10) == 0) {
                rotationVelocity = 1.0F / (random.nextFloat() + 1.0F) * 0.2F;
            }
        }

        if (isInWater()) {
            if (rotation < Mth.PI && rotation / Mth.PI > 0.75F) {
                randomMotionSpeed = 0.1F;
            } else if (rotation >= Mth.PI) {
                randomMotionSpeed *= 0.999F;
            }
            if (!level().isClientSide()) {
                setDeltaMovement(randomMotionX * randomMotionSpeed, randomMotionY * randomMotionSpeed,
                        randomMotionZ * randomMotionSpeed);
            }
            Vec3 movement = getDeltaMovement();
            float horizontal = Mth.sqrt((float) (movement.x * movement.x + movement.z * movement.z));
            yBodyRot += (-Mth.atan2((float) movement.x, (float) movement.z) * Mth.RAD_TO_DEG - yBodyRot) * 0.1F;
            setYRot(yBodyRot);
            setXRot((float) (Mth.atan2((float) movement.y, horizontal) * Mth.RAD_TO_DEG));
        } else if (!level().isClientSide()) {
            setDeltaMovement(0.0D, (getDeltaMovement().y - 0.08D) * 0.98D, 0.0D);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        // 1.7.10's entityAge is the resettable idle/despawn age, not the entity's total lifetime.
        if (getNoActionTime() > 100) {
            randomMotionX = randomMotionY = randomMotionZ = 0.0F;
        } else if (random.nextInt(50) == 0 || !isInWater()
                || randomMotionX == 0.0F && randomMotionY == 0.0F && randomMotionZ == 0.0F) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            randomMotionX = Mth.cos(angle) * 0.2F;
            randomMotionY = -0.1F + random.nextFloat() * 0.2F;
            randomMotionZ = Mth.sin(angle) * 0.2F;
        }
    }

    @Override
    public void travel(Vec3 ignored) {
        move(MoverType.SELF, getDeltaMovement());
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return super.checkSpawnRules(level, spawnType) && getY() > 45.0D && getY() < 63.0D && random.nextInt(10) == 0;
    }

    @Override public boolean isInWater() { return level().getFluidState(blockPosition()).is(FluidTags.WATER); }
    @Override public boolean isPushedByFluid() { return false; }
    @Override public boolean canBreatheUnderwater() { return true; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance); }
}
