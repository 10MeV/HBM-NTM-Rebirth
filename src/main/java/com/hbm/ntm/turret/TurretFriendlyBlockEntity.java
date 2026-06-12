package com.hbm.ntm.turret;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.particle.LegacyCasingEjectors;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TurretFriendlyBlockEntity extends TurretChekhovBlockEntity {
    private static final List<BulletConfig> CONFIGS = List.of(
            LegacySednaRuntimeBulletConfigs.R556_SP,
            LegacySednaRuntimeBulletConfigs.R556_FMJ,
            LegacySednaRuntimeBulletConfigs.R556_JHP,
            LegacySednaRuntimeBulletConfigs.R556_AP);

    public TurretFriendlyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_FRIENDLY.get(), pos, state);
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_friendly";
    }

    @Override
    protected String getGuiTextureFile() {
        return "gui_turret_friendly.png";
    }

    @Override
    protected List<BulletConfig> getAmmoConfigs() {
        return CONFIGS;
    }

    @Override
    protected int getDelay() {
        return 5;
    }

    @Override
    protected int legacyCasingEjectorId() {
        return LegacyCasingEjectors.TURRET_FRIENDLY;
    }
}
