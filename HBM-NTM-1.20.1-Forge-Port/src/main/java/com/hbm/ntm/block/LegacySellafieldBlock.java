package com.hbm.ntm.block;

import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@SuppressWarnings("deprecation")
public class LegacySellafieldBlock extends Block {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 5);

    public LegacySellafieldBlock(Properties properties) {
        super(properties.randomTicks());
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            int meta = state.getValue(LEVEL);
            RadiationUtil.addRadiationPoisoning(living, 30 * 20, meta < 5 ? meta : meta * 2);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        int meta = state.getValue(LEVEL);
        ChunkRadiationManager.incrementRadiation(level, pos, 0.5F * (meta + 1));

        if (random.nextInt(meta == 0 ? 25 : 15) == 0) {
            if (meta > 0) {
                level.setBlock(pos, state.setValue(LEVEL, meta - 1), 2);
            } else {
                level.setBlock(pos, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(), 2);
            }
        }
    }
}

