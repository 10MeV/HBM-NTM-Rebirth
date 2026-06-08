package com.hbm.ntm.blockentity;

import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlockEntities;
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeconBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        AABB area = new AABB(pos).inflate(0.5D, 0.0D, 0.5D).expandTowards(0.0D, 1.0D, 0.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity entity : entities) {
            RadiationUtil.applyRadaway(entity, 0.5F);
            HbmLivingProperties.setRadBuf(entity, 0.0F);
        }
    }
}
