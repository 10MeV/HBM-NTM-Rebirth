package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class DynamiteStickEntity extends ThrowableItemProjectile {
    private static final int MAX_TIMER = 3 * 20;
    private static final double BOUNCE_MOD = 0.5D;
    private int timer;

    public DynamiteStickEntity(EntityType<? extends DynamiteStickEntity> type, Level level) {
        super(type, level);
    }

    public DynamiteStickEntity(Level level, LivingEntity thrower) {
        super(ModEntityTypes.DYNAMITE_STICK.get(), thrower, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && ++timer >= MAX_TIMER) {
            explode();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        Direction face = hit.getDirection();
        Vec3 motion = getDeltaMovement();
        double x = face.getAxis() == Direction.Axis.X ? -motion.x : motion.x;
        double y = face.getAxis() == Direction.Axis.Y ? -motion.y : motion.y;
        double z = face.getAxis() == Direction.Axis.Z ? -motion.z : motion.z;
        Vec3 bounced = new Vec3(x, y, z).scale(BOUNCE_MOD);
        setDeltaMovement(bounced);
        if (bounced.lengthSqr() > 0.05D * 0.05D) {
            LegacySoundPlayer.playSoundAtEntity(this, "hbm:weapon.gBounce", SoundSource.PLAYERS, 2.0F, 1.0F);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide && tickCount > 5) {
            explode();
        }
    }

    private void explode() {
        if (level() instanceof ServerLevel serverLevel) {
            WeaponExplosionUtil.smooth(serverLevel, getX(), getY(), getZ(), 5.0F, this, 15.0F, 1.0D, true)
                    .explode();
        }
        discard();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.STICK_DYNAMITE.get();
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        timer = tag.getInt("timer");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("timer", timer);
    }
}
