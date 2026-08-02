package com.hbm.ntm.entity.projectile;

import com.hbm.items.weapon.ItemGenericGrenade;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.item.UniversalGrenadeItem;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class DynamiteStickEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> TRIPLEX_TRAIL =
            SynchedEntityData.defineId(DynamiteStickEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BOUNCES =
            SynchedEntityData.defineId(DynamiteStickEntity.class, EntityDataSerializers.INT);
    private int timer;
    private boolean stuck;
    private float previousGrenadeSpin;
    private float grenadeSpin;

    public DynamiteStickEntity(EntityType<? extends DynamiteStickEntity> type, Level level) {
        super(type, level);
    }

    public DynamiteStickEntity(Level level, LivingEntity thrower) {
        super(ModEntityTypes.DYNAMITE_STICK.get(), thrower, level);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    public void tick() {
        if (stuck) {
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
        }
        super.tick();
        if (level().isClientSide()) {
            updateLegacyClientSpin();
            if (hasTriplexTrail()) {
                // EntityGrenadeUniversal#onUpdate emitted this client-only flame for TRAIL_TRIPLET.
                com.hbm.ntm.particle.ParticleUtil.spawnVanillaExtFlame(level(), getX(), getY(), getZ());
            }
        }
        if (stuck) {
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
        }
        if (!level().isClientSide) {
            timer++;
            ItemGenericGrenade grenade = getGrenade();
            ItemStack stack = getItem();
            if (grenade != null) {
                grenade.onGrenadeTick(this, stack, timer);
            }
            if (!isRemoved() && timer >= getMaxTimer(stack)) {
                explode();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        ItemGenericGrenade grenade = getGrenade();
        ItemStack stack = getItem();
        if (grenade != null && grenade.onGrenadeBlockHit(this, hit, stack, timer)) {
            return;
        }
        if (stuck) {
            return;
        }
        Direction face = hit.getDirection();
        Vec3 motion = getDeltaMovement();
        Vec3 bounced;
        if (grenade instanceof UniversalGrenadeItem) {
            // EntityGrenadeUniversal#onImpact: the surface-normal component
            // uses the shell bounce factor while tangential components retain
            // 80 percent. EntityGrenadeBouncyBase has a different all-axis
            // damping contract, so it remains on the branch below.
            double bounce = getBounceMod(stack);
            bounced = new Vec3(
                    face.getAxis() == Direction.Axis.X ? -motion.x * bounce : motion.x * 0.8D,
                    face.getAxis() == Direction.Axis.Y ? -motion.y * bounce : motion.y * 0.8D,
                    face.getAxis() == Direction.Axis.Z ? -motion.z * bounce : motion.z * 0.8D);
            Vec3 hitLocation = hit.getLocation();
            setPos(hitLocation.x + face.getStepX() * 0.05D, hitLocation.y + face.getStepY() * 0.05D,
                    hitLocation.z + face.getStepZ() * 0.05D);
            if (motion.length() > 0.2D) {
                LegacySoundPlayer.playSoundAtEntity(this, "hbm:weapon.grenadeBounce", SoundSource.PLAYERS,
                        1.0F, 1.0F);
            }
        } else {
            double x = face.getAxis() == Direction.Axis.X ? -motion.x : motion.x;
            double y = face.getAxis() == Direction.Axis.Y ? -motion.y : motion.y;
            double z = face.getAxis() == Direction.Axis.Z ? -motion.z : motion.z;
            bounced = new Vec3(x, y, z).scale(getBounceMod(stack));
            if (bounced.lengthSqr() > 0.05D * 0.05D) {
                LegacySoundPlayer.playSoundAtEntity(this, "hbm:weapon.gBounce", SoundSource.PLAYERS, 2.0F, 1.0F);
            }
        }
        setDeltaMovement(bounced);
        entityData.set(BOUNCES, bounceCount() + 1);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        ItemGenericGrenade grenade = getGrenade();
        ItemStack stack = getItem();
        if (grenade != null && grenade.onGrenadeEntityHit(this, hit, stack, timer)) {
            return;
        }
        // EntityGrenadeBouncyBase only ray-traced blocks. Its generic carriers
        // (stick dynamite and fishing dynamite) therefore pass through entities
        // until their fuse expires; only a grenade-specific hook may opt into an
        // entity impact detonation (for example the universal impact fuze).
    }

    public void explode() {
        if (!level().isClientSide) {
            ItemGenericGrenade grenade = getGrenade();
            Entity owner = getOwner();
            LivingEntity thrower = owner instanceof LivingEntity living ? living : null;
            if (grenade != null) {
                grenade.explode(this, thrower, level(), getX(), getY(), getZ(), getItem());
            } else if (level() instanceof ServerLevel serverLevel) {
                WeaponExplosionUtil.smooth(serverLevel, getX(), getY(), getZ(), 5.0F, thrower, 15.0F, 1.0D, true)
                        .explode();
            }
        }
        discard();
    }

    public void stickTo(BlockHitResult hit) {
        stuck = true;
        setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
        setDeltaMovement(Vec3.ZERO);
        setNoGravity(true);
    }

    public int getLegacyTimer() {
        return timer;
    }

    public void setTriplexTrail() {
        entityData.set(TRIPLEX_TRAIL, true);
    }

    public boolean hasTriplexTrail() {
        return entityData.get(TRIPLEX_TRAIL);
    }

    public int bounceCount() {
        return entityData.get(BOUNCES);
    }

    public float grenadeSpin(float partialTick) {
        return previousGrenadeSpin + (grenadeSpin - previousGrenadeSpin) * partialTick;
    }

    private void updateLegacyClientSpin() {
        previousGrenadeSpin = grenadeSpin;
        if (bounceCount() <= 0) {
            grenadeSpin += 15.0F;
        } else {
            double deltaX = xo - getX();
            double deltaZ = zo - getZ();
            grenadeSpin += Math.min(15.0D, Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 50.0D);
        }
        if (grenadeSpin >= 360.0F) {
            previousGrenadeSpin -= 360.0F;
            grenadeSpin -= 360.0F;
        }
    }

    private ItemGenericGrenade getGrenade() {
        Item item = getItem().getItem();
        if (item instanceof ItemGenericGrenade grenade) {
            return grenade;
        }
        Item fallback = ModItems.STICK_DYNAMITE.get();
        return fallback instanceof ItemGenericGrenade grenade ? grenade : null;
    }

    private int getMaxTimer(ItemStack stack) {
        ItemGenericGrenade grenade = getGrenade();
        return grenade != null ? grenade.getMaxTimer(stack) : 3 * 20;
    }

    private double getBounceMod(ItemStack stack) {
        ItemGenericGrenade grenade = getGrenade();
        return grenade != null ? grenade.getBounceMod(stack) : 0.5D;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.STICK_DYNAMITE.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TRIPLEX_TRAIL, false);
        entityData.define(BOUNCES, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        timer = tag.getInt("timer");
        stuck = tag.getBoolean("stuck");
        if (stuck) {
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("timer", timer);
        tag.putBoolean("stuck", stuck);
    }
}
