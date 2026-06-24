package com.hbm.ntm.block;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.FoundryCastingBlockEntity;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FoundryCastingBlock extends Block implements EntityBlock, ICrucibleAcceptor, Toolable {
    private static final VoxelShape MOLD_SHAPE = box(0, 0, 0, 16, 8, 16);
    private static final VoxelShape BASIN_SHAPE = box(0, 0, 0, 16, 15.984, 16);
    private static final VoxelShape MOLD_COLLISION = Shapes.or(
            box(0, 0, 0, 16, 2, 16),
            box(0, 0, 0, 16, 8, 2),
            box(0, 0, 0, 2, 8, 16),
            box(14, 0, 0, 16, 8, 16),
            box(0, 0, 14, 16, 8, 16));
    private static final VoxelShape BASIN_COLLISION = Shapes.or(
            box(0, 0, 0, 16, 2, 16),
            box(0, 0, 0, 16, 16, 2),
            box(0, 0, 0, 2, 16, 16),
            box(14, 0, 0, 16, 16, 16),
            box(0, 0, 14, 16, 16, 16));

    private final int moldSize;

    public FoundryCastingBlock(Properties properties, int moldSize) {
        super(properties);
        this.moldSize = moldSize;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return moldSize == 0 ? FoundryCastingBlockEntity.mold(pos, state) : FoundryCastingBlockEntity.basin(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if ((moldSize == 0 && type != ModBlockEntities.FOUNDRY_MOLD.get())
                || (moldSize == 1 && type != ModBlockEntities.FOUNDRY_BASIN.get())) {
            return null;
        }
        return level.isClientSide ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                FoundryCastingBlockEntity.serverTick(tickLevel, tickPos, tickState, (FoundryCastingBlockEntity) blockEntity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FoundryCastingBlockEntity casting)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack output = casting.removeOutput();
        if (!output.isEmpty()) {
            giveOrDrop(player, output);
            return InteractionResult.CONSUME;
        }
        ItemStack held = player.getItemInHand(hand);
        FoundryMoldItem.Mold mold = FoundryMoldItem.getMold(held);
        if (mold != null && mold.size() == casting.getMoldSize() && casting.installMold(held)) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        if (isShovel(held)) {
            ItemStack scrap = casting.drainAsScrap();
            if (!scrap.isEmpty()) {
                giveOrDrop(player, scrap);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER || !(level.getBlockEntity(pos) instanceof FoundryCastingBlockEntity casting)) {
            return false;
        }
        if (!level.isClientSide) {
            ItemStack mold = casting.removeMold();
            if (!mold.isEmpty()) {
                giveOrDrop(player, mold);
            }
        }
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return moldSize == 0 ? MOLD_SHAPE : BASIN_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return moldSize == 0 ? MOLD_COLLISION : BASIN_COLLISION;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return false;
    }

    public boolean isFaceSturdy(BlockState state, BlockGetter level, BlockPos pos, Direction direction,
            SupportType supportType) {
        return moldSize == 0 ? direction == Direction.DOWN : direction != Direction.UP;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof FoundryCastingBlockEntity casting) {
            for (ItemStack stack : casting.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acceptor
                && acceptor.canAcceptPartialPour(level, pos, hit, side, stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acceptor
                ? acceptor.pour(level, pos, hit, side, stack)
                : stack;
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acceptor
                && acceptor.canAcceptPartialFlow(level, pos, side, stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return level.getBlockEntity(pos) instanceof ICrucibleAcceptor acceptor
                ? acceptor.flow(level, pos, side, stack)
                : stack;
    }

    private static boolean isShovel(ItemStack stack) {
        return stack.getItem() instanceof ShovelItem;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.addItem(stack.copy())) {
            player.drop(stack.copy(), false);
        }
    }
}
