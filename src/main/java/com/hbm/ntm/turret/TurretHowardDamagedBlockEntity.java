package com.hbm.ntm.turret;

import com.hbm.ntm.bullet.BulletCasingEjectUtil;
import com.hbm.ntm.config.WeaponConfig;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.LegacyCasingEjectors;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TurretHowardDamagedBlockEntity extends DamagedTurretBlockEntityBase {
    private int timer;

    public TurretHowardDamagedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_HOWARD_DAMAGED.get(), pos, state);
    }

    @Override
    protected double getDetectorRange() {
        return 16.0D;
    }

    @Override
    protected double getDetectorGrace() {
        return 5.0D;
    }

    @Override
    protected double getTurretYawSpeed() {
        return 3.0D;
    }

    @Override
    protected double getTurretPitchSpeed() {
        return 2.0D;
    }

    @Override
    protected double getTurretDepression() {
        return 50.0D;
    }

    @Override
    protected double getTurretElevation() {
        return 90.0D;
    }

    @Override
    protected double getHeightOffset() {
        return 2.25D;
    }

    @Override
    protected double getBarrelLength() {
        return 3.25D;
    }

    @Override
    protected boolean hasThermalVision() {
        return false;
    }

    @Override
    protected void tickServerSpecificAnimations() {
        if (getTargetPos() != null) {
            triggerBarrelSpin(45.0F);
        }
    }

    @Override
    protected void tickClientSpecificAnimations() {
        if (getTargetPos() != null) {
            triggerClientBarrelSpin(45.0F);
        }
    }

    @Override
    protected void updateFiringTick() {
        if (level == null) {
            return;
        }
        timer++;
        if (timer % 4 != 0) {
            return;
        }
        spawnMuzzleLargeExplodeAt(getMuzzlePos().add(rotateLegacyLocal(new Vec3(0.0D, 0.25D, 0.0D))), 1.5F, 1);
        spawnHowardCasing();
        playTurretSound(ModSounds.TURRET_HOWARD_FIRE.get(), 4.0F, 0.7F + level.random.nextFloat() * 0.3F);
        Entity target = getTarget();
        if (target != null && level.random.nextInt(100) + 1 <= WeaponConfig.ciwsHitrate() * 0.5D) {
            EntityDamageUtil.attackEntityFromIgnoreIFrame(target, ModDamageSources.shrapnel(level),
                    2.0F + level.random.nextInt(2));
        }
    }

    private void spawnHowardCasing() {
        if (level == null || level.isClientSide) {
            return;
        }
        BulletCasingEjectUtil.execute(level, BulletCasingEjectUtil.legacyEjectorRequest(getCasingSpawnPos(),
                LegacyCasingEjectors.TURRET_HOWARD, "DGK", (float) -getRotationPitch(), (float) getRotationYaw(),
                false));
    }

    @Override
    protected Vec3 getCasingSpawnPos() {
        Vec3 pos = getTurretPos();
        Vec3 vec = new Vec3(-0.875D, 0.2D, -0.125D)
                .zRot((float) -getRotationPitch())
                .yRot((float) -(getRotationYaw() + Math.PI * 0.5D));
        return pos.add(vec);
    }
}
