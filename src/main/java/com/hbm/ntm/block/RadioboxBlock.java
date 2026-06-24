package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.RadioboxBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RadioboxBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape NORTH_SHAPE = box(4.0D, 1.0D, 0.0D, 12.0D, 15.0D, 5.0D);
    private static final VoxelShape SOUTH_SHAPE = box(4.0D, 1.0D, 11.0D, 12.0D, 15.0D, 16.0D);
    private static final VoxelShape WEST_SHAPE = box(11.0D, 1.0D, 4.0D, 16.0D, 15.0D, 12.0D);
    private static final VoxelShape EAST_SHAPE = box(0.0D, 1.0D, 4.0D, 5.0D, 15.0D, 12.0D);

    public RadioboxBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(ACTIVE, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RadioboxBlockEntity box) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty() && stack.is(ModItems.BATTERY_SPARK.get()) && !box.isInfinite()) {
                if (!(player instanceof ServerPlayer serverPlayer) || !serverPlayer.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                LegacySoundPlayer.playSoundEffect(level, pos, "hbm:item.upgradePlug", 1.5F, 1.0F);
                box.setInfinite(true);
                return InteractionResult.CONSUME;
            }

            boolean active = !state.getValue(ACTIVE);
            level.setBlock(pos, state.setValue(ACTIVE, active), Block.UPDATE_CLIENTS);
            LegacySoundPlayer.playSoundEffect(level, pos, "hbm:block.reactorStart", 1.0F, active ? 1.0F : 0.85F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof RadioboxBlockEntity box
                && box.isInfinite()) {
            popResource(level, pos, new ItemStack(ModItems.BATTERY_SPARK.get()));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> SOUTH_SHAPE;
        };
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadioboxBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return type == ModBlockEntities.RADIOBOX.get() && !level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        RadioboxBlockEntity.serverTick(tickLevel, tickPos, tickState, (RadioboxBlockEntity) blockEntity)
                : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }
}
