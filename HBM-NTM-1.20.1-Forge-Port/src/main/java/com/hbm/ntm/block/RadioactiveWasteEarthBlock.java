package com.hbm.ntm.block;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class RadioactiveWasteEarthBlock extends Block {
    private final boolean mycelium;

    public RadioactiveWasteEarthBlock(Properties properties, boolean mycelium) {
        super(properties.randomTicks());
        this.mycelium = mycelium;
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
        if (mycelium && RadiationConfig.ENABLE_MYCELIUM_SPREAD.get()) {
            for (BlockPos target : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                BlockPos above = target.above();
                BlockState targetState = level.getBlockState(target);
                if (!level.getBlockState(above).isSolidRender(level, above)
                        && (targetState.is(Blocks.DIRT) || targetState.is(Blocks.GRASS_BLOCK)
                        || targetState.is(Blocks.MYCELIUM) || targetState.is(ModBlocks.WASTE_EARTH.get()))) {
                    level.setBlock(target, state, 2);
                }
            }
        }

        if (shouldDecayToDirt(level, pos)) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
        }

        if (level.getBlockState(pos.above()).is(Blocks.BROWN_MUSHROOM)
                || level.getBlockState(pos.above()).is(Blocks.RED_MUSHROOM)) {
            level.destroyBlock(pos.above(), false);
        }
    }

    private static boolean shouldDecayToDirt(ServerLevel level, BlockPos pos) {
        return RadiationConfig.CLEANUP_DEAD_DIRT.get()
                || (level.getRawBrightness(pos.above(), 0) < 4 && level.getBlockState(pos.above()).getLightBlock(level, pos.above()) > 2);
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.core.Direction facing, net.minecraftforge.common.IPlantable plantable) {
        return plantable.getPlantType((LevelReader) level, pos.relative(facing)) == net.minecraftforge.common.PlantType.CAVE;
    }
}

