package com.hbm.entity.mob.glyphid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Source-backed 1.7.10 Glyphid Brawler leap carrier. */
public class EntityGlyphidBrawler extends EntityGlyphid {
    private int timer;
    private Entity lastTarget;
    private double lastX;
    private double lastY;
    private double lastZ;

    public EntityGlyphidBrawler(EntityType<? extends EntityGlyphidBrawler> type, Level level) {
        super(type, level);
    }

    public EntityGlyphidBrawler(Level level) {
        this(ModEntityTypes.GLYPHID_BRAWLER.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid_brawler.png");
    }

    @Override
    public float getScale() {
        return 1.25F;
    }

    @Override
    public void tick() {
        super.tick();
        Entity target = getTarget();
        if (target == null || !isAlive()) {
            return;
        }
        lastX = target.getX();
        lastY = target.getY();
        lastZ = target.getZ();
        if (--timer <= 0) {
            leap();
            timer = 80 + random.nextInt(30);
        }
    }

    public void leap() {
        Entity target = getTarget();
        if (level().isClientSide() || target == null || distanceToSqr(target) >= 20.0D * 20.0D) {
            return;
        }
        double velocityX = target.getX() - lastX;
        double velocityY = target.getY() - lastY;
        double velocityZ = target.getZ() - lastZ;
        // EntityGlyphidBrawler never assigns lastTarget in 1.7.10.
        if (lastTarget != target) {
            velocityX = 0.0D;
            velocityY = 0.0D;
            velocityZ = 0.0D;
        }

        Vec3 delta = new Vec3(target.getX() - getX() + velocityX * 60.0D,
                target.getY() + target.getBbHeight() * 0.5D - (getY() + 1.0D) + velocityY * 60.0D,
                target.getZ() - getZ() + velocityZ * 60.0D);
        if (delta.length() < 3.0D) {
            return;
        }
        double yaw = -Math.atan2(delta.x, delta.z);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double velocity = 1.5D;
        double velocitySquared = velocity * velocity;
        double gravity = 0.01D;
        double pitch = Math.atan((velocitySquared + Math.sqrt(velocitySquared * velocitySquared
                - gravity * (gravity * horizontal * horizontal + 2.0D * delta.y * velocitySquared)))
                / (gravity * horizontal));
        if (Double.isNaN(pitch)) {
            return;
        }
        Vec3 launch = new Vec3(velocity, 0.0D, 0.0D).zRot((float) (-pitch / 3.5D))
                .yRot((float) -(yaw + Math.PI * 0.5D));
        setDeltaMovement(legacyThrowVelocity(launch, velocity, random.nextFloat()));
        Vec3 motion = getDeltaMovement();
        float horizontalMotion = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float motionYaw = (float) (Math.atan2(motion.x, motion.z) * 180.0D / Math.PI);
        float motionPitch = (float) (Math.atan2(motion.y, horizontalMotion) * 180.0D / Math.PI);
        setYRot(motionYaw);
        yRotO = motionYaw;
        setXRot(motionPitch);
        xRotO = motionPitch;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return source.is(DamageTypeTags.IS_FALL) && amount <= 10.0F ? false : super.hurt(source, amount);
    }

    @Override
    protected boolean isArmorBroken(float amount) {
        return random.nextInt(100) <= Math.min(Math.pow(amount * 0.25D, 2.0D), 100.0D);
    }

    @Override
    protected float getArmorThresholdMultiplier() {
        return 2.0F;
    }

    @Override
    protected float getArmorResistanceMultiplier() {
        return 0.15F;
    }

    private Vec3 legacyThrowVelocity(Vec3 direction, double velocity, float inaccuracy) {
        Vec3 normalized = direction.normalize().add(random.nextGaussian() * 0.0075D * inaccuracy,
                random.nextGaussian() * 0.0075D * inaccuracy, random.nextGaussian() * 0.0075D * inaccuracy);
        return normalized.scale(velocity);
    }
}
