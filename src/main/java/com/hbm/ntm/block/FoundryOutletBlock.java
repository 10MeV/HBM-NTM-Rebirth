package com.hbm.ntm.block;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.FoundryOutletBlockEntity;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FoundryOutletBlock extends Block implements EntityBlock, ICrucibleAcceptor, Toolable {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape EAST_SHAPE = box(10, 0, 5, 16, 8, 11);
    private static final VoxelShape WEST_SHAPE = box(0, 0, 5, 6, 8, 11);
    private static final VoxelShape SOUTH_SHAPE = box(5, 0, 10, 11, 8, 16);
    private static final VoxelShape NORTH_SHAPE = box(5, 0, 0, 11, 8, 6);

    private final boolean slagTap;

    public FoundryOutletBlock(Properties properties, boolean slagTap) {
        super(properties);
        this.slagTap = slagTap;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return slagTap ? FoundryOutletBlockEntity.slagTap(pos, state) : new FoundryOutletBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        BlockEntityType<?> expected = slagTap ? ModBlockEntities.FOUNDRY_SLAGTAP.get() : ModBlockEntities.FOUNDRY_OUTLET.get();
        return type == expected && !level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                FoundryOutletBlockEntity.serverTick(tickLevel, tickPos, tickState, (FoundryOutletBlockEntity) blockEntity)
                : null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            level.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()), Block.UPDATE_ALL);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FoundryOutletBlockEntity outlet)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            MaterialStack material = FoundryScrapsItem.getMaterial(held);
            if (held.is(ModItems.FOUNDRY_SCRAPS.get()) && material != null) {
                outlet.setFilter(material.material);
            } else {
                outlet.toggleInvertRedstone();
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (!(level.getBlockEntity(pos) instanceof FoundryOutletBlockEntity outlet)) {
            return false;
        }
        if (!level.isClientSide) {
            if (tool == ToolType.SCREWDRIVER) {
                outlet.clearFilter();
            } else if (tool == ToolType.HAND_DRILL) {
                outlet.toggleInvertFilter();
            } else {
                return false;
            }
        }
        return tool == ToolType.SCREWDRIVER || tool == ToolType.HAND_DRILL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return false;
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return stack;
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
}
