package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyFileCabinetBlockEntity;
import com.hbm.ntm.item.LegacyFileCabinetBlockItem;
import com.hbm.ntm.item.PadlockItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LegacyFileCabinetBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 1);

    private static final VoxelShape NORTH_SHAPE = Block.box(3.0D, 0.0D, 4.0D, 13.0D, 16.0D, 16.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 12.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(4.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(0.0D, 0.0D, 3.0D, 12.0D, 16.0D, 13.0D);

    public LegacyFileCabinetBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(VARIANT, 0));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int variant = 0;
        if (context.getItemInHand().getItem() instanceof LegacyFileCabinetBlockItem item) {
            variant = item.getVariant(context.getItemInHand());
        }
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(VARIANT, Math.max(0, Math.min(1, variant)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof LegacyFileCabinetBlockEntity cabinet) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof PadlockItem) {
                return cabinet.tryApplyPadlock(player, held) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            if (held.is(ModItems.KEY_KIT.get())) {
                return cabinet.tryCreateCounterfeitKeys(player, hand) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            if (cabinet.canAccess(player, held)) {
                NetworkHooks.openScreen(serverPlayer, cabinet, pos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LegacyFileCabinetBlockEntity cabinet) {
            cabinet.loadFromPlacedStack(stack);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof LegacyFileCabinetBlockEntity cabinet) {
            cabinet.getDrops().forEach(stack -> Block.popResource(level, pos, stack));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        ItemStack stack = new ItemStack(asItem());
        stack.getOrCreateTag().putInt(com.hbm.ntm.item.LegacyStateBlockItem.TAG_VARIANT,
                state.hasProperty(VARIANT) ? state.getValue(VARIANT) : 0);
        return java.util.List.of(stack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyFileCabinetBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.LEGACY_FILE_CABINET.get(),
                LegacyFileCabinetBlockEntity::tick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }

    private static VoxelShape shape(BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        return switch (facing) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }
}
