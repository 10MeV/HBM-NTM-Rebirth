package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyReedsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Source-backed equivalent of 1.7.10 {@code BlockReeds}.  Its block entity is renderer-only: the old
 * water-depth crossed-square stack cannot be represented by one baked model without losing per-layer light.
 */
@SuppressWarnings("deprecation")
public final class LegacyReedsBlock extends BaseEntityBlock {
    public LegacyReedsBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // In 1.7.10 this was exactly Blocks.water or Blocks.flowing_water.  Both modern flow states use WATER.
        return level.getBlockState(pos.below()).is(Blocks.WATER);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
            LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        // Legacy checkAndDropBlock replaced invalid reeds with air directly, with no item drop.
        return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighbourState, level, pos, neighbourPos)
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyReedsBlockEntity(pos, state);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
