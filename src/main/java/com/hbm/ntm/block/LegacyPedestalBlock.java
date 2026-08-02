package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyPedestalBlockEntity;
import com.hbm.ntm.recipe.PedestalRecipeRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LegacyPedestalBlock extends BaseEntityBlock {
    private static final VoxelShape BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    private static final VoxelShape TOP = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape MIDDLE = Block.box(2.0D, 4.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    private static final VoxelShape SHAPE = Shapes.or(BASE, TOP, MIDDLE);

    public LegacyPedestalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof LegacyPedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (!pedestal.hasItem() && !held.isEmpty()) {
            if (!level.isClientSide) {
                pedestal.setItem(held.copy());
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (pedestal.hasItem() && held.isEmpty()) {
            if (!level.isClientSide) {
                player.setItemInHand(hand, pedestal.getItem());
                pedestal.clearItem();
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            PedestalRecipeRuntime.tryCraft(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof LegacyPedestalBlockEntity pedestal
                && pedestal.hasItem()) {
            ItemStack drop = pedestal.getItem();
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, drop);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyPedestalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.PEDESTAL.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                LegacyPedestalBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (LegacyPedestalBlockEntity) blockEntity);
    }
}
