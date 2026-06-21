package com.hbm.ntm.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class OilSpillBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public OilSpillBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return !below.is(Blocks.ICE)
                && !below.is(Blocks.PACKED_ICE)
                && (below.is(BlockTags.LEAVES)
                || below.getBlock() instanceof OilSpillBlock
                || (below.isSolidRender(level, belowPos) && below.blocksMotion()));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!state.canSurvive(level, pos)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }

    public boolean canBeReplacedByDrain(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().isEmpty()
                || state.getFluidState().getType() == Fluids.EMPTY
                || state.getBlock() instanceof OilSpillBlock;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return true;
    }
}
