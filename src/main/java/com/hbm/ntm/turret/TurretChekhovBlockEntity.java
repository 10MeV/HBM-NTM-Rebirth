package com.hbm.ntm.turret;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.particle.LegacyCasingEjectors;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TurretChekhovBlockEntity extends TurretBlockEntityBase {
    private static final List<BulletConfig> CONFIGS = List.of(
            LegacySednaRuntimeBulletConfigs.BMG50_SP,
            LegacySednaRuntimeBulletConfigs.BMG50_FMJ,
            LegacySednaRuntimeBulletConfigs.BMG50_JHP,
            LegacySednaRuntimeBulletConfigs.BMG50_AP,
            LegacySednaRuntimeBulletConfigs.BMG50_DU);

    private int timer;
    private float spinAcceleration;

    public TurretChekhovBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.TURRET_CHEKHOV.get(), pos, state);
    }

    protected TurretChekhovBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 10_000L);
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_chekhov";
    }

    @Override
    protected List<BulletConfig> getAmmoConfigs() {
        return CONFIGS;
    }

    @Override
    protected double getTurretElevation() {
        return 45.0D;
    }

    @Override
    protected double getAcceptableInaccuracy() {
        return 15.0D;
    }

    @Override
    protected double getBarrelLength() {
        return 3.5D;
    }

    @Override
    protected void updateServerTickAfterTargeting() {
        if (getTargetPos() == null) {
            timer = Math.max(0, Math.min(20, timer - 1));
        }
    }

    @Override
    protected void tickClientSpecificAnimations() {
        tickLegacySpin();
    }

    private void tickLegacySpin() {
        if (getTargetPos() != null) {
            spinAcceleration = Math.min(45.0F, spinAcceleration + 2.0F);
        } else {
            spinAcceleration = Math.max(0.0F, spinAcceleration - 2.0F);
        }
        triggerClientBarrelSpin(spinAcceleration);
    }

    @Override
    protected void updateFiringTick() {
        timer++;
        if (timer <= 20 || timer % getDelay() != 0) {
            return;
        }
        BulletConfig config = getFirstConfigLoaded();
        if (config == null || !consumeAmmo(config)) {
            return;
        }
        spawnBullet(config, 10.0F);
        spawnMuzzleLargeExplode(1.5F, 1);
        scheduleCasing(config);
        playTurretSound("hbm:turret.chekhov_fire", 2.0F, 1.0F);
    }

    @Override
    protected boolean usesCasings() {
        return true;
    }

    @Override
    protected int legacyCasingEjectorId() {
        return LegacyCasingEjectors.TURRET_CHEKHOV;
    }

    @Override
    protected Vec3 getCasingSpawnPos() {
        Vec3 pos = getTurretPos();
        Vec3 vec = new Vec3(-1.125D, 0.125D, 0.25D)
                .zRot((float) -getRotationPitch())
                .yRot((float) -(getRotationYaw() + Math.PI * 0.5D));
        return pos.add(vec);
    }

    protected int getDelay() {
        return 2;
    }
}
