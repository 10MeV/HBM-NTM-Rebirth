package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PhosphorVineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IForgeShearable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Source-backed equivalent of 1.7.10 {@code BlockHangingVine}.  The renderer-only block entity carries no
 * legacy state; it exists solely because the old two-pass crossed-square renderer cannot be represented by a
 * single vanilla baked block model without losing its full-bright spots pass.
 */
@SuppressWarnings("deprecation")
public final class PhosphorVineBlock extends BaseEntityBlock implements IForgeShearable {
    public PhosphorVineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos supportPos = pos.above();
        BlockState support = level.getBlockState(supportPos);
        return support.is(this) || support.isFaceSturdy(level, supportPos, Direction.UP);
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
        return direction == Direction.UP && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5D));
        entity.fallDistance = 0.0F;
    }

    @Override
    public boolean isShearable(ItemStack item, Level level, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(@Nullable net.minecraft.world.entity.player.Player player, ItemStack item,
            Level level, BlockPos pos, int fortune) {
        return List.of(new ItemStack(this));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PhosphorVineBlockEntity(pos, state);
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
