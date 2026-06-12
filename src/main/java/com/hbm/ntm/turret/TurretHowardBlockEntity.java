package com.hbm.ntm.turret;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.BulletCasingEjectUtil;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.config.WeaponConfig;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.LegacyCasingEjectors;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TurretHowardBlockEntity extends TurretBlockEntityBase {
    private static final String TAG_LOADED = "loaded";
    private static final List<BulletConfig> CONFIGS = List.of(LegacySednaRuntimeBulletConfigs.DGK_NORMAL);
    private int loaded;
    private int timer;

    public TurretHowardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_HOWARD.get(), pos, state, 50_000L);
    }

    public int getLoaded() {
        return loaded;
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_howard";
    }

    @Override
    protected String getGuiTextureFile() {
        return "gui_turret_howard.png";
    }

    @Override
    protected List<BulletConfig> getAmmoConfigs() {
        return CONFIGS;
    }

    @Override
    protected long getConsumption() {
        return 500L;
    }

    @Override
    protected double getDetectorRange() {
        return 250.0D;
    }

    @Override
    protected double getTurretYawSpeed() {
        return 12.0D;
    }

    @Override
    protected double getTurretPitchSpeed() {
        return 8.0D;
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
    protected void updateServerTick() {
        if (loaded > 0) {
            return;
        }
        BulletConfig config = getFirstConfigLoaded();
        if (config != null && consumeAmmo(config)) {
            loaded = 200;
            playTurretSound(ModSounds.TURRET_HOWARD_RELOAD.get(), 4.0F, 1.0F);
        }
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
        if (loaded <= 0 || level == null || getTargetPos() == null) {
            return;
        }
        timer++;
        playTurretSound(ModSounds.TURRET_HOWARD_FIRE.get(), 4.0F, 0.9F + level.random.nextFloat() * 0.3F);
        playTurretSound(ModSounds.TURRET_HOWARD_FIRE.get(), 4.0F, 1.0F + level.random.nextFloat() * 0.3F);
        spawnHowardCasing();
        spawnHowardCasing();
        if (timer % 2 != 0) {
            return;
        }
        loaded--;
        Vec3 muzzle = getMuzzlePos();
        Vec3 offset = rotateLegacyLocal(new Vec3(0.0D, 0.25D, 0.0D));
        spawnMuzzleLargeExplodeAt(muzzle.add(offset), 1.5F, 1);
        spawnMuzzleLargeExplodeAt(muzzle.subtract(offset), 1.5F, 1);
        Entity target = getTarget();
        if (target != null && level.random.nextInt(100) + 1 <= WeaponConfig.ciwsHitrate()) {
            EntityDamageUtil.attackEntityFromIgnoreIFrame(target, ModDamageSources.shrapnel(level),
                    2.0F + level.random.nextInt(2));
        }
    }

    protected void spawnHowardCasing() {
        if (level == null || level.isClientSide) {
            return;
        }
        BulletCasingEjectUtil.execute(level, BulletCasingEjectUtil.legacyEjectorRequest(getCasingSpawnPos(),
                LegacyCasingEjectors.TURRET_HOWARD, "DGK", (float) -getRotationPitch(), (float) getRotationYaw(),
                false));
    }

    @Override
    protected boolean usesCasings() {
        return true;
    }

    @Override
    protected Vec3 getCasingSpawnPos() {
        Vec3 pos = getTurretPos();
        Vec3 vec = new Vec3(-0.875D, 0.2D, -0.125D)
                .zRot((float) -getRotationPitch())
                .yRot((float) -(getRotationYaw() + Math.PI * 0.5D));
        return pos.add(vec);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_LOADED, loaded);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loaded = tag.getInt(TAG_LOADED);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt(TAG_LOADED, loaded);
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt(TAG_LOADED, loaded);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_LOADED)) {
            loaded = tag.getInt(TAG_LOADED);
        }
    }
}
