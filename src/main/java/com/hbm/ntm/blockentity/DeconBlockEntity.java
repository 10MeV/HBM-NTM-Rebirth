package com.hbm.ntm.blockentity;

import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class DeconBlockEntity extends BlockEntity {
    public DeconBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECON.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DeconBlockEntity blockEntity) {
        if (level.isClientSide()) {
            spawnAura(level, pos);
            return;
        }

        AABB area = new AABB(pos).inflate(0.5D, 0.0D, 0.5D).expandTowards(0.0D, 1.0D, 0.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity entity : entities) {
            RadiationUtil.applyRadaway(entity, 0.5F);
            entity.removeEffect(ModEffects.RADIATION.get());
            HbmLivingProperties.clearCont(entity);
            HbmLivingProperties.setRadBuf(entity, 0.0F);
        }
    }

    private static void spawnAura(Level level, BlockPos pos) {
        ParticleUtil.spawnVanillaExtTownAura(level,
                pos.getX() + 0.125D + level.random.nextDouble() * 0.75D,
                pos.getY() + 1.1D,
                pos.getZ() + 0.125D + level.random.nextDouble() * 0.75D,
                0.0D, 0.04D, 0.0D);
    }
}
