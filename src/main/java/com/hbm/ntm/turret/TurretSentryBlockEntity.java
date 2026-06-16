package com.hbm.ntm.turret;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.particle.LegacyCasingEjectors;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TurretSentryBlockEntity extends TurretBlockEntityBase {
    private static final String TAG_RETRACTING_LEFT = "RetractingLeft";
    private static final String TAG_RETRACTING_RIGHT = "RetractingRight";
    private static final List<BulletConfig> CONFIGS = List.of(
            LegacySednaRuntimeBulletConfigs.P9_SP,
            LegacySednaRuntimeBulletConfigs.P9_FMJ,
            LegacySednaRuntimeBulletConfigs.P9_JHP,
            LegacySednaRuntimeBulletConfigs.P9_AP);

    private int timer;
    private boolean shotSide;
    private boolean didJustShootLeft;
    private boolean didJustShootRight;
    private boolean retractingLeft;
    private boolean retractingRight;

    public TurretSentryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_SENTRY.get(), pos, state, 1_000L);
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_sentry";
    }

    @Override
    protected String getGuiTextureFile() {
        return "gui_turret_sentry.png";
    }

    @Override
    protected List<BulletConfig> getAmmoConfigs() {
        return CONFIGS;
    }

    @Override
    protected long getConsumption() {
        return 5L;
    }

    @Override
    protected double getDetectorRange() {
        return 24.0D;
    }

    @Override
    protected double getDetectorGrace() {
        return 2.0D;
    }

    @Override
    protected double getTurretDepression() {
        return 20.0D;
    }

    @Override
    protected double getTurretElevation() {
        return 20.0D;
    }

    @Override
    protected double getAcceptableInaccuracy() {
        return 15.0D;
    }

    @Override
    protected double getBarrelLength() {
        return 1.25D;
    }

    @Override
    protected boolean hasThermalVision() {
        return false;
    }

    @Override
    protected Vec3 getHorizontalOffset() {
        return new Vec3(0.5D, 0.0D, 0.5D);
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of(EnergyPort.of(0, -1, 0, Direction.DOWN));
    }

    @Override
    protected void seekNewTarget() {
        Entity previous = getTarget();
        super.seekNewTarget();
        Entity current = getTarget();
        if (level != null && current != null && current != previous) {
            playTurretSoundAtEntity(current, "hbm:turret.sentry_lockon", 2.0F, 1.5F);
        }
    }

    @Override
    protected void updateFiringTick() {
        timer++;
        if (timer % 10 != 0) {
            return;
        }
        BulletConfig config = getFirstConfigLoaded();
        if (config == null || !hasAmmo(config)) {
            return;
        }
        if (!spawnBullet(config, 5.0F)) {
            return;
        }
        consumeAmmo(config);
        playTurretSound("hbm:turret.sentry_fire", 2.0F, 1.0F);
        spawnMuzzleLargeExplodeAt(getMuzzlePos().add(rotateLegacyYawOnly(
                new Vec3(0.125D * (shotSide ? 1.0D : -1.0D), 0.0D, 0.0D))), 1.0F, 1);
        scheduleCasing(config);
        if (shotSide) {
            triggerLeftBarrelRecoil();
        } else {
            triggerRightBarrelRecoil();
        }
        shotSide = !shotSide;
    }

    @Override
    protected void triggerLeftBarrelRecoil() {
        didJustShootLeft = true;
        syncSentryRecoil();
    }

    @Override
    protected void triggerRightBarrelRecoil() {
        didJustShootRight = true;
        syncSentryRecoil();
    }

    @Override
    protected void decayClientAnimations() {
        tickSentryRecoil();
    }

    @Override
    protected void decayServerAnimations() {
    }

    private void tickSentryRecoil() {
        if (retractingLeft) {
            barrelLeftPos += 0.5F;
            if (barrelLeftPos >= 1.0F) {
                barrelLeftPos = 1.0F;
                retractingLeft = false;
            }
        } else if (barrelLeftPos > 0.0F) {
            barrelLeftPos = Math.max(0.0F, barrelLeftPos - 0.25F);
        }

        if (retractingRight) {
            barrelRightPos += 0.5F;
            if (barrelRightPos >= 1.0F) {
                barrelRightPos = 1.0F;
                retractingRight = false;
            }
        } else if (barrelRightPos > 0.0F) {
            barrelRightPos = Math.max(0.0F, barrelRightPos - 0.25F);
        }
    }

    private void syncSentryRecoil() {
    }

    @Override
    protected boolean shouldSyncBarrelRecoilPositions() {
        return false;
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        writeSentryAnimationSync(tag);
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_RETRACTING_LEFT) && tag.contains(TAG_RETRACTING_RIGHT)) {
            retractingLeft = tag.getBoolean(TAG_RETRACTING_LEFT);
            retractingRight = tag.getBoolean(TAG_RETRACTING_RIGHT);
        }
    }

    private void writeSentryAnimationSync(CompoundTag tag) {
        tag.putBoolean(TAG_RETRACTING_LEFT, didJustShootLeft);
        tag.putBoolean(TAG_RETRACTING_RIGHT, didJustShootRight);
        didJustShootLeft = false;
        didJustShootRight = false;
    }

    @Override
    protected boolean usesCasings() {
        return true;
    }

    @Override
    protected int legacyCasingEjectorId() {
        return LegacyCasingEjectors.TURRET_SENTRY;
    }

    @Override
    protected Vec3 getCasingSpawnPos() {
        Vec3 pos = getTurretPos();
        Vec3 vec = new Vec3(0.0D, 0.25D, -0.125D)
                .zRot((float) -getRotationPitch())
                .yRot((float) -(getRotationYaw() + Math.PI * 0.5D));
        return pos.add(vec);
    }

    public boolean shotSide() {
        return shotSide;
    }
}
