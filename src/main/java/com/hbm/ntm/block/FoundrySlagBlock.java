package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FoundrySlagBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FoundrySlagBlock extends Block implements EntityBlock {
    public FoundrySlagBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundrySlagBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof FoundrySlagBlockEntity slag) {
            return box(0, 0, 0, 16, Math.max(1.0D, slag.getFillLevel() * 16.0D), 16);
        }
        return super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        BlockEntity selfEntity = level.getBlockEntity(pos);
        if (!(selfEntity instanceof FoundrySlagBlockEntity self) || self.getMaterialType() == null) {
            level.removeBlock(pos, false);
            return;
        }
        BlockPos below = pos.below();
        if (!level.isOutsideBuildHeight(below) && level.getBlockState(below).canBeReplaced()) {
            level.setBlock(below, state, Block.UPDATE_ALL);
            if (level.getBlockEntity(below) instanceof FoundrySlagBlockEntity moved) {
                moved.addMaterial(self.getMaterialType(), self.getAmount());
            }
            level.removeBlock(pos, false);
            return;
        }
        BlockEntity belowEntity = level.getBlockEntity(below);
        if (belowEntity instanceof FoundrySlagBlockEntity belowSlag
                && belowSlag.getMaterialType() == self.getMaterialType()
                && belowSlag.getAmount() < FoundrySlagBlockEntity.MAX_AMOUNT) {
            int transfer = Math.min(FoundrySlagBlockEntity.MAX_AMOUNT - belowSlag.getAmount(), self.getAmount());
            belowSlag.addMaterial(self.getMaterialType(), transfer);
            self.consume(transfer);
            if (self.getAmount() <= 0) {
                level.removeBlock(pos, false);
            }
            level.scheduleTick(below, this, 1);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof FoundrySlagBlockEntity slag) {
            ItemStack scrap = slag.asScrap();
            if (!scrap.isEmpty()) {
                Block.popResource(level, pos, scrap);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
