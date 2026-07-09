package com.hbm.ntm.entity.projectile;

import com.hbm.items.weapon.ItemGenericGrenade;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
    private int timer;
    private boolean stuck;

    public DynamiteStickEntity(EntityType<? extends DynamiteStickEntity> type, Level level) {
        super(type, level);
    }

    public DynamiteStickEntity(Level level, LivingEntity thrower) {
        super(ModEntityTypes.DYNAMITE_STICK.get(), thrower, level);
    }

    @Override
    public void tick() {
        if (stuck) {
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
        }
        super.tick();
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
        double x = face.getAxis() == Direction.Axis.X ? -motion.x : motion.x;
        double y = face.getAxis() == Direction.Axis.Y ? -motion.y : motion.y;
        double z = face.getAxis() == Direction.Axis.Z ? -motion.z : motion.z;
        Vec3 bounced = new Vec3(x, y, z).scale(getBounceMod(stack));
        setDeltaMovement(bounced);
        if (bounced.lengthSqr() > 0.05D * 0.05D) {
            LegacySoundPlayer.playSoundAtEntity(this, "hbm:weapon.gBounce", SoundSource.PLAYERS, 2.0F, 1.0F);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        ItemGenericGrenade grenade = getGrenade();
        ItemStack stack = getItem();
        if (grenade != null && grenade.onGrenadeEntityHit(this, hit, stack, timer)) {
            return;
        }
        if (!level().isClientSide && tickCount > 5) {
            explode();
        }
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
