package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyFissureBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

/** Source-backed 1.7.10 {@code BlockFissure} / {@code ore_volcano}. */
public final class LegacyFissureBlock extends BaseEntityBlock {
    /** Old metadata zero emits ordinary volcanic lava; every other value emits rad lava. */
    public static final BooleanProperty RADIOACTIVE = BooleanProperty.create("radioactive");

    public LegacyFissureBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(RADIOACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RADIOACTIVE);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (level.getBlockState(above).canBeReplaced()) {
            level.setBlock(above, (state.getValue(RADIOACTIVE)
                    ? ModBlocks.RAD_LAVA_BLOCK.get()
                    : ModBlocks.VOLCANIC_LAVA_BLOCK.get()).defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyFissureBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.LEGACY_FISSURE.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                LegacyFissureBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (LegacyFissureBlockEntity) blockEntity);
    }
}
