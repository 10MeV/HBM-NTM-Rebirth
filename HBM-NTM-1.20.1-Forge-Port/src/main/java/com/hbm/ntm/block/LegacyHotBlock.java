package com.hbm.ntm.block;

import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyHotBlock extends Block {
    private final float radiation;

    public LegacyHotBlock(Properties properties) {
        this(properties, 0.0F);
    }

    public LegacyHotBlock(Properties properties, float radiation) {
        super(properties);
        this.radiation = radiation;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.hurt(level.damageSources().hotFloor(), 4.0F);
        if (radiation > 0.0F && entity instanceof LivingEntity living) {
            RadiationUtil.contaminate(living, radiation, true);
        }
    }
}
