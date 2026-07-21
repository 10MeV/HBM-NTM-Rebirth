package com.hbm.entity.mob.glyphid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.projectile.EntityAcidBomb;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Source-backed 1.7.10 Glyphid Bombardier acid-volley carrier. */
public class EntityGlyphidBombardier extends EntityGlyphid {
    private Entity lastTarget;
    private double lastX;
    private double lastY;
    private double lastZ;

    public EntityGlyphidBombardier(EntityType<? extends EntityGlyphidBombardier> type, Level level) {
        super(type, level);
    }

    public EntityGlyphidBombardier(Level level) {
        this(ModEntityTypes.GLYPHID_BOMBARDIER.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid_bombardier.png");
    }

    @Override
    public void tick() {
        super.tick();
        Entity target = getTarget();
        if (level().isClientSide() || !(target instanceof LivingEntity)) {
            return;
        }
        if (tickCount % 20 == 0) {
            lastTarget = target;
            lastX = target.getX();
            lastY = target.getY();
            lastZ = target.getZ();
        }
        if (tickCount % 60 == 1) {
            fireBombs(target);
        }
    }

    private void fireBombs(Entity target) {
        double velocityX = target.getX() - lastX;
        double velocityY = target.getY() - lastY;
        double velocityZ = target.getZ() - lastZ;
        if (lastTarget != target || new Vec3(velocityX, velocityY, velocityZ).length() > 30.0D) {
            velocityX = 0.0D;
            velocityY = 0.0D;
            velocityZ = 0.0D;
        }

        boolean topAttack = distanceTo(target) > 20.0F;
        int prediction = topAttack ? 60 : 20;
        Vec3 delta = new Vec3(target.getX() - getX() + velocityX * prediction,
                target.getY() + target.getBbHeight() * 0.5D - (getY() + 1.0D) + velocityY * prediction,
                target.getZ() - getZ() + velocityZ * prediction);
        if (delta.length() < 3.0D) {
            return;
        }

        double yaw = -Math.atan2(delta.x, delta.z);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double velocity = getV0();
        double velocitySquared = velocity * velocity;
        double gravity = 0.04D;
        double rootSign = topAttack ? 1.0D : -1.0D;
        double discriminant = velocitySquared * velocitySquared
                - gravity * (gravity * horizontal * horizontal + 2.0D * delta.y * velocitySquared);
        double pitch = Math.atan((velocitySquared + Math.sqrt(discriminant) * rootSign) / (gravity * horizontal));
        if (Double.isNaN(pitch)) {
            return;
        }

        Vec3 direction = new Vec3(velocity, 0.0D, 0.0D).zRot((float) -pitch)
                .yRot((float) -(yaw + Math.PI * 0.5D));
        for (int index = 0; index < getBombCount(); index++) {
            EntityAcidBomb bomb = new EntityAcidBomb(level(), getX(), getY() + 1.0D, getZ());
            bomb.setOwner(this);
            bomb.setDeltaMovement(legacyThrowVelocity(direction, velocity, index * getSpreadMult()));
            bomb.setDamage(getBombDamage());
            level().addFreshEntity(bomb);
        }
        swingGlyphid();
    }

    public float getBombDamage() {
        return 5.0F;
    }

    public int getBombCount() {
        return 5;
    }

    public float getSpreadMult() {
        return 1.0F;
    }

    public double getV0() {
        return 1.0D;
    }

    private Vec3 legacyThrowVelocity(Vec3 direction, double velocity, float inaccuracy) {
        Vec3 normalized = direction.normalize().add(random.nextGaussian() * 0.0075D * inaccuracy,
                random.nextGaussian() * 0.0075D * inaccuracy,
                random.nextGaussian() * 0.0075D * inaccuracy);
        return normalized.scale(velocity);
    }
}
