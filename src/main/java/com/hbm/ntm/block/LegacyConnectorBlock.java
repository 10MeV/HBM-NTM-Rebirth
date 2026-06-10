package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ConnectorBlockEntity;
import com.hbm.ntm.blockentity.HbmLegacyWireNodeBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.ColorUtil;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LegacyConnectorBlock extends HbmLegacyWireNodeBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final double MIN = 5.0D;
    private static final double MAX = 11.0D;

    private final Kind kind;

    public LegacyConnectorBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConnectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.RED_CONNECTOR.get()) {
            return null;
        }
        return level.isClientSide ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        HbmLegacyWireNodeBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (HbmLegacyWireNodeBlockEntity) blockEntity);
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
        if (!(player instanceof ServerPlayer) || !(level.getBlockEntity(pos) instanceof HbmLegacyWireNodeBlockEntity connector)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        int color = ColorUtil.getColorFromDye(stack);
        if (!connector.setWireColor(color)) {
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForFacing(state.getValue(FACING), kind);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    public static VoxelShape shapeForFacing(Direction facing, Kind kind) {
        Direction dir = facing.getOpposite();
        double minX = dir == Direction.WEST ? 0.0D : kind == Kind.SUPER && dir == Direction.EAST ? 0.0D : MIN;
        double maxX = dir == Direction.EAST ? 16.0D : kind == Kind.SUPER && dir == Direction.WEST ? 16.0D : MAX;
        double minY = dir == Direction.DOWN ? 0.0D : kind == Kind.SUPER && dir == Direction.UP ? 0.0D : MIN;
        double maxY = dir == Direction.UP ? 16.0D : kind == Kind.SUPER && dir == Direction.DOWN ? 16.0D : MAX;
        double minZ = dir == Direction.NORTH ? 0.0D : kind == Kind.SUPER && dir == Direction.SOUTH ? 0.0D : MIN;
        double maxZ = dir == Direction.SOUTH ? 16.0D : kind == Kind.SUPER && dir == Direction.NORTH ? 16.0D : MAX;
        return box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public enum Kind {
        NORMAL,
        SUPER
    }
}
