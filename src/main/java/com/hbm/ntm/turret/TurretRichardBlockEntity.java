package com.hbm.ntm.turret;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TurretRichardBlockEntity extends TurretBlockEntityBase {
    private static final String TAG_LOADED = "loaded";
    private static final List<BulletConfig> CONFIGS = List.of(
            LegacySednaRuntimeBulletConfigs.ROCKET_ML_HE,
            LegacySednaRuntimeBulletConfigs.ROCKET_ML_HEAT,
            LegacySednaRuntimeBulletConfigs.ROCKET_ML_DEMO,
            LegacySednaRuntimeBulletConfigs.ROCKET_ML_INC,
            LegacySednaRuntimeBulletConfigs.ROCKET_ML_PHOSPHORUS);

    private int timer;
    private int loaded;
    private int reload;

    public TurretRichardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_RICHARD.get(), pos, state, 10_000L);
    }

    public int getLoaded() {
        return loaded;
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_richard";
    }

    @Override
    protected String getGuiTextureFile() {
        return "gui_turret_richard.png";
    }

    @Override
    protected List<BulletConfig> getAmmoConfigs() {
        return CONFIGS;
    }

    @Override
    protected double getDetectorGrace() {
        return 8.0D;
    }

    @Override
    protected double getDetectorRange() {
        return 64.0D;
    }

    @Override
    protected double getTurretDepression() {
        return 25.0D;
    }

    @Override
    protected double getTurretElevation() {
        return 25.0D;
    }

    @Override
    protected double getBarrelLength() {
        return 1.25D;
    }

    @Override
    protected void updateServerTickAfterTargeting() {
        if (reload > 0) {
            reload--;
            if (reload == 0) {
                loaded = 17;
            }
        }
        if (getFirstConfigLoaded() == null) {
            loaded = 0;
        } else if (loaded <= 0 && reload <= 0) {
            reload = 100;
        }
    }

    @Override
    protected void updateFiringTick() {
        if (reload > 0) {
            return;
        }
        timer++;
        if (timer % 10 != 0) {
            return;
        }
        BulletConfig config = getFirstConfigLoaded();
        if (config == null || !hasAmmo(config)) {
            loaded = 0;
            return;
        }
        if (!spawnBullet(config, 30.0F, getTarget())) {
            return;
        }
        consumeAmmo(config);
        playTurretSound("hbm:turret.richard_fire", 2.0F, 1.0F);
        loaded--;
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
