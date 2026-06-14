package com.hbm.ntm.turret;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TurretTauonBlockEntity extends TurretBlockEntityBase {
    private static final String TAG_BEAM_SHOT = "BeamShot";
    private static final List<BulletConfig> CONFIGS = List.of(LegacySednaRuntimeBulletConfigs.TAU_URANIUM);
    private int timer;
    private boolean didJustShootBeam;

    public TurretTauonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_TAUON.get(), pos, state, 100_000L);
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_tauon";
    }

    @Override
    protected String getGuiTextureFile() {
        return "gui_turret_tau.png";
    }

    @Override
    protected List<BulletConfig> getAmmoConfigs() {
        return CONFIGS;
    }

    @Override
    protected long getConsumption() {
        return 1_000L;
    }

    @Override
    protected double getDetectorRange() {
        return 128.0D;
    }

    @Override
    protected double getTurretYawSpeed() {
        return 9.0D;
    }

    @Override
    protected double getTurretPitchSpeed() {
        return 6.0D;
    }

    @Override
    protected double getTurretDepression() {
        return 35.0D;
    }

    @Override
    protected double getTurretElevation() {
        return 35.0D;
    }

    @Override
    protected double getBarrelLength() {
        return 1.9375D;
    }

    @Override
    protected void tickClientSpecificAnimations() {
        updateClientBeamDistanceFromTarget();
        if (getTargetPos() != null) {
            triggerClientBarrelSpin(45.0F);
        }
    }

    @Override
    protected void updateFiringTick() {
        timer++;
        if (timer % 5 != 0 || level == null) {
            return;
        }
        BulletConfig config = getFirstConfigLoaded();
        Entity target = getTarget();
        if (config == null || target == null || !consumeAmmo(config)) {
            return;
        }
        EntityDamageUtil.attackEntityFromNt(target, ModDamageSources.source(level, ModDamageSources.ELECTRICITY),
                30.0F + level.random.nextInt(11));
        didJustShootBeam = true;
        spawnTauMuzzleParticles(5);
        playTurretSound("hbm:weapon.tauShoot", 4.0F, 0.9F + level.random.nextFloat() * 0.3F);
    }

    @Override
    protected boolean shouldSyncBeamState() {
        return false;
    }

    @Override
    public net.minecraft.nbt.CompoundTag getClientSyncTag() {
        net.minecraft.nbt.CompoundTag tag = super.getClientSyncTag();
        writeTauonBeamSync(tag);
        return tag;
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public void handleClientSyncTag(net.minecraft.nbt.CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.getBoolean(TAG_BEAM_SHOT)) {
            triggerClientBeamFromTarget(3);
        }
    }

    private void writeTauonBeamSync(net.minecraft.nbt.CompoundTag tag) {
        tag.putBoolean(TAG_BEAM_SHOT, didJustShootBeam);
        didJustShootBeam = false;
    }
}
