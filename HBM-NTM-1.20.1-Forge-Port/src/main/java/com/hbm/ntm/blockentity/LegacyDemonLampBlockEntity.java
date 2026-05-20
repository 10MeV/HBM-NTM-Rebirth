package com.hbm.ntm.blockentity;

import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LegacyDemonLampBlockEntity extends BlockEntity {
    private static final float RADIATION = 100_000.0F;
    private static final double RANGE = 25.0D;
    private static final AABB INFINITE_RENDER_BOX = new AABB(
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY);

    public LegacyDemonLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_DEMON_LAMP.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LegacyDemonLampBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        radiate(level, pos);
    }

    private static void radiate(Level level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = new AABB(center, center).inflate(RANGE);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box);
        DamageSources damageSources = level.damageSources();

        for (LivingEntity entity : entities) {
            Vec3 eyes = entity.getEyePosition();
            Vec3 delta = eyes.subtract(center);
            double length = delta.length();
            if (length <= 0.0D) {
                continue;
            }

            Vec3 normal = delta.normalize();
            float resistance = 0.0F;
            for (int i = 1; i < length; i++) {
                BlockPos sample = BlockPos.containing(center.add(normal.scale(i)));
                resistance += level.getBlockState(sample).getBlock().getExplosionResistance();
            }
            if (resistance < 1.0F) {
                resistance = 1.0F;
            }

            float exposure = RADIATION / resistance / (float) (length * length);
            RadiationUtil.contaminate(entity, exposure, true);
            if (length < 2.0D) {
                entity.hurt(damageSources.inFire(), 100.0F);
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return INFINITE_RENDER_BOX;
    }
}
