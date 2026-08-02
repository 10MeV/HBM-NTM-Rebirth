package com.hbm.ntm.block;

import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Exact modern equivalent of the 1.7.10 Spikes collision contract. */
@SuppressWarnings("deprecation")
public class LegacySpikesBlock extends Block {
    public LegacySpikesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity
                && entity.getDeltaMovement().y < -0.1D
                && entity.hurt(ModDamageSources.source(level, ModDamageSources.SPIKES), 100.0F)) {
            level.playSound(null, pos, ModSounds.ENTITY_SLICER.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }
}
