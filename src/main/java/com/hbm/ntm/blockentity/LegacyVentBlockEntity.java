package com.hbm.ntm.blockentity;

import com.hbm.entity.particle.EntityChlorineFX;
import com.hbm.entity.particle.EntityCloudFX;
import com.hbm.entity.particle.EntityPinkCloudFX;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LegacyVentBlockEntity extends BlockEntity {
    public LegacyVentBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.LEGACY_VENT.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyVentBlockEntity vent) {
        if (!level.hasNeighborSignal(pos)) return;
        double spread = state.is(ModBlocks.VENT_CHLORINE.get()) ? 1.5D
                : state.is(ModBlocks.VENT_CLOUD.get()) ? 1.75D : 2.0D;
        double x = vent.level.random.nextGaussian() * spread;
        double y = vent.level.random.nextGaussian() * spread;
        double z = vent.level.random.nextGaussian() * spread;
        BlockPos target = pos.offset((int) x, (int) y, (int) z);
        if (level.getBlockState(target).isCollisionShapeFullBlock(level, target)) return;
        if (state.is(ModBlocks.VENT_CHLORINE.get())) {
            level.addFreshEntity(new EntityChlorineFX(level, target.getX(), target.getY(), target.getZ(), x / 2.0D, y / 2.0D, z / 2.0D));
        } else if (state.is(ModBlocks.VENT_CLOUD.get())) {
            level.addFreshEntity(new EntityCloudFX(level, target.getX(), target.getY(), target.getZ(), x / 2.0D, y / 2.0D, z / 2.0D));
        } else if (state.is(ModBlocks.VENT_PINK_CLOUD.get())) {
            level.addFreshEntity(new EntityPinkCloudFX(level, target.getX(), target.getY(), target.getZ(), x / 2.0D, y / 2.0D, z / 2.0D));
        }
    }
}
