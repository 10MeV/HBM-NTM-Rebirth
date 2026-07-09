package com.hbm.ntm.blockentity;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmBlockStateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LegacyDemonLampBlockEntity extends BlockEntity {
    private static final float RADIATION = 100_000.0F;
    private static final double RANGE = 25.0D;
    private static final double AURA_RENDER_RADIUS = 15.0D;

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
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        AABB box = new AABB(centerX, centerY, centerZ, centerX, centerY, centerZ).inflate(RANGE);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box);
        DamageSources damageSources = level.damageSources();
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();

        for (LivingEntity entity : entities) {
            double deltaX = entity.getX() - centerX;
            double deltaY = entity.getEyeY() - centerY;
            double deltaZ = entity.getZ() - centerZ;
            double lengthSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
            if (lengthSquared <= 0.0D) {
                continue;
            }

            double length = Math.sqrt(lengthSquared);
            double invLength = 1.0D / length;
            double normalX = deltaX * invLength;
            double normalY = deltaY * invLength;
            double normalZ = deltaZ * invLength;
            float resistance = 0.0F;
            for (int i = 1; i < length; i++) {
                sample.set(
                        Mth.floor(centerX + normalX * i),
                        Mth.floor(centerY + normalY * i),
                        Mth.floor(centerZ + normalZ * i));
                resistance += HbmBlockStateUtil.explosionResistance(level.getBlockState(sample), level, sample);
            }
            if (resistance < 1.0F) {
                resistance = 1.0F;
            }

            float exposure = RADIATION / resistance / (float) lengthSquared;
            RadiationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, exposure);
            if (length < 2.0D) {
                EntityDamageUtil.attackEntityFromNt(entity, damageSources.inFire(), 100.0F);
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(AURA_RENDER_RADIUS);
    }
}
