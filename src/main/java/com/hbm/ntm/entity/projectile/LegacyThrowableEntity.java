package com.hbm.ntm.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.UUID;

public abstract class LegacyThrowableEntity extends Entity {
    protected int ticksInAir;
    protected int ticksInGround;
    protected boolean inGround;
    private UUID ownerUuid;

    protected LegacyThrowableEntity(EntityType<?> type, Level level) {
        super(type, level);
        setNoGravity(false);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() && clientUsesServerInterpolationOnly()) {
            tickClientServerInterpolationOnly();
            return;
        }
        if (inGround) {
            ticksInGround++;
            if (groundDespawn() > 0 && ticksInGround == groundDespawn()) {
                discard();
            }
            return;
        }

        ticksInAir++;
        Vec3 start = position();
        Vec3 motion = getDeltaMovement();
        Vec3 end = start.add(motion.scale(motionMultiplier()));
        HitResult hit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.MISS) {
            end = hit.getLocation();
        }

        if (!level().isClientSide() && impactsEntities()) {
            EntityHitResult entityHit = findEntityHit(start, end);
            if (entityHit != null) {
                hit = entityHit;
            }
        }

        if (hit.getType() != HitResult.Type.MISS) {
            onImpact(hit);
        }

        setPos(end.x, end.y, end.z);
        Vec3 nextMotion = getDeltaMovement().scale(isInWater() ? getWaterDrag() : getAirDrag()).add(0.0D, -getGravityVelocity(), 0.0D);
        setDeltaMovement(nextMotion);
    }

    private EntityHitResult findEntityHit(Vec3 start, Vec3 end) {
        AABB sweep = getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D);
        List<Entity> entities = level().getEntities(this, sweep, entity -> entity.isAlive() && entity.isPickable());
        Entity closest = null;
        Vec3 closestHit = null;
        double closestDistance = Double.MAX_VALUE;
        for (Entity entity : entities) {
            if (entity == getOwner() && ticksInAir < selfDamageDelay()) {
                continue;
            }
            AABB bounds = entity.getBoundingBox().inflate(0.3D);
            Vec3 hit = bounds.clip(start, end).orElse(null);
            if (hit == null) {
                continue;
            }
            double distance = start.distanceToSqr(hit);
            if (distance < closestDistance) {
                closest = entity;
                closestHit = hit;
                closestDistance = distance;
            }
        }
        return closest == null ? null : new EntityHitResult(closest, closestHit);
    }

    protected Entity getOwner() {
        if (ownerUuid == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getEntity(ownerUuid);
    }

    public void setOwner(Entity owner) {
        ownerUuid = owner == null ? null : owner.getUUID();
    }

    protected boolean impactsEntities() {
        return true;
    }

    protected int selfDamageDelay() {
        return 5;
    }

    protected double motionMultiplier() {
        return 1.0D;
    }

    protected float getAirDrag() {
        return 0.99F;
    }

    protected float getWaterDrag() {
        return 0.8F;
    }

    protected double getGravityVelocity() {
        return 0.03D;
    }

    protected int groundDespawn() {
        return 1200;
    }

    protected boolean clientUsesServerInterpolationOnly() {
        return false;
    }

    protected void tickClientServerInterpolationOnly() {
    }

    protected abstract void onImpact(HitResult hit);

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ticksInAir = tag.getInt("ticksInAir");
        ticksInGround = tag.getInt("ticksInGround");
        inGround = tag.getBoolean("inGround");
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ticksInAir", ticksInAir);
        tag.putInt("ticksInGround", ticksInGround);
        tag.putBoolean("inGround", inGround);
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
    }

    protected static int hitBlockX(HitResult hit) {
        return hit instanceof BlockHitResult blockHit ? blockHit.getBlockPos().getX() : (int) Math.floor(hit.getLocation().x);
    }

    protected static int hitBlockY(HitResult hit) {
        return hit instanceof BlockHitResult blockHit ? blockHit.getBlockPos().getY() : (int) Math.floor(hit.getLocation().y);
    }

    protected static int hitBlockZ(HitResult hit) {
        return hit instanceof BlockHitResult blockHit ? blockHit.getBlockPos().getZ() : (int) Math.floor(hit.getLocation().z);
    }
}
