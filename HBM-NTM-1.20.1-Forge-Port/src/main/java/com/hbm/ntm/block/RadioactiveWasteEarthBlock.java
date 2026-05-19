package com.hbm.ntm.block;

import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class RadioactiveWasteEarthBlock extends Block {
    private final boolean mycelium;
    private final float chunkRadiation;

    public RadioactiveWasteEarthBlock(Properties properties, boolean mycelium, float chunkRadiation) {
        super(properties.randomTicks());
        this.mycelium = mycelium;
        this.chunkRadiation = chunkRadiation;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide && !oldState.is(state.getBlock())) {
            ChunkRadiationManager.incrementRadiation(level, pos, chunkRadiation);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !newState.is(state.getBlock())) {
            ChunkRadiationManager.decrementRadiation(level, pos, chunkRadiation);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (mycelium && entity instanceof LivingEntity living) {
            RadiationUtil.addRadiationPoisoning(living, 30 * 20, 3);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (mycelium) {
            level.addParticle(ParticleTypes.MYCELIUM,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + 1.1D,
                    pos.getZ() + random.nextFloat(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (mycelium) {
            for (BlockPos target : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                BlockPos above = target.above();
                BlockState targetState = level.getBlockState(target);
                if (!level.getBlockState(above).isSolidRender(level, above)
                        && (targetState.is(Blocks.DIRT) || targetState.is(Blocks.GRASS_BLOCK) || targetState.is(Blocks.MYCELIUM))) {
                    level.setBlock(target, state, 2);
                }
            }
        } else if (level.getBlockState(pos.above()).is(Blocks.BROWN_MUSHROOM)
                || level.getBlockState(pos.above()).is(Blocks.RED_MUSHROOM)) {
            level.destroyBlock(pos.above(), false);
        }
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction facing, net.minecraftforge.common.IPlantable plantable) {
        return plantable.getPlantType((LevelReader) level, pos.relative(facing)) == net.minecraftforge.common.PlantType.CAVE;
    }
}
