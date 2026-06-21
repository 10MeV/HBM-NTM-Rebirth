package com.hbm.ntm.turret;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TurretJeremyBlockEntity extends TurretBlockEntityBase {
    private static final List<BulletConfig> CONFIGS = List.of(
            LegacySednaRuntimeBulletConfigs.SHELL_NORMAL,
            LegacySednaRuntimeBulletConfigs.SHELL_EXPLOSIVE,
            LegacySednaRuntimeBulletConfigs.SHELL_AP,
            LegacySednaRuntimeBulletConfigs.SHELL_DU,
            LegacySednaRuntimeBulletConfigs.SHELL_W9);

    private int timer;
    private int reload;

    public TurretJeremyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_JEREMY.get(), pos, state, 10_000L);
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_jeremy";
    }

    @Override
    protected String getGuiTextureFile() {
        return "gui_turret_cannon.png";
    }

    @Override
    protected List<BulletConfig> getAmmoConfigs() {
        return CONFIGS;
    }

    @Override
    protected double getDetectorGrace() {
        return 16.0D;
    }

    @Override
    protected double getDetectorRange() {
        return 80.0D;
    }

    @Override
    protected double getTurretDepression() {
        return 45.0D;
    }

    @Override
    protected double getBarrelLength() {
        return 4.25D;
    }

    @Override
    protected void updateServerTick() {
        if (reload > 0) {
            reload--;
            if (reload == 1) {
                playTurretSound("hbm:turret.jeremy_reload", 2.0F, 1.0F);
            }
        }
    }

    @Override
    protected void updateFiringTick() {
        timer++;
        if (timer % 40 != 0) {
            return;
        }
        BulletConfig config = getFirstConfigLoaded();
        if (config == null || !hasAmmo(config)) {
            return;
        }
        if (!spawnBullet(config, 50.0F)) {
            return;
        }
        scheduleCasing(config);
        consumeAmmo(config);
        playTurretSound("hbm:turret.jeremy_fire", 4.0F, 1.0F);
        reload = 20;
        spawnMuzzleLargeExplode(0.0F, 5);
    }

    @Override
    protected boolean usesCasings() {
        return true;
    }

    @Override
    protected Vec3 getCasingSpawnPos() {
        Vec3 pos = getTurretPos();
        Vec3 vec = new Vec3(-2.0D, 0.0D, 0.0D)
                .zRot((float) -getRotationPitch())
                .yRot((float) -(getRotationYaw() + Math.PI * 0.5D));
        return pos.add(vec);
    }

    @Override
    protected void spawnLegacyCasing(BulletConfig config) {
        spawnDirectCasing(getCasingSpawnPos(),
                (float) Math.toDegrees(getRotationYaw()),
                (float) -Math.toDegrees(getRotationPitch()),
                -0.2D, -0.2D, 0.0D, 0.01D, -5.0F, 0.0F,
                config.spentCasingName(), true, 100, 0.5D, 20);
    }

    @Override
    protected int casingDelay() {
        return 22;
    }
}
