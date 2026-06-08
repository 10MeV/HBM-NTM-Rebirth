package com.hbm.ntm.block;

import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.blockentity.RadioTorchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public abstract class RadioTorchBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape DOWN_SHAPE = shape(Direction.DOWN);
    private static final VoxelShape UP_SHAPE = shape(Direction.UP);
    private static final VoxelShape NORTH_SHAPE = shape(Direction.NORTH);
    private static final VoxelShape SOUTH_SHAPE = shape(Direction.SOUTH);
    private static final VoxelShape WEST_SHAPE = shape(Direction.WEST);
    private static final VoxelShape EAST_SHAPE = shape(Direction.EAST);

    protected RadioTorchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof RadioTorchBlockEntity torch) {
            NetworkHooks.openScreen(serverPlayer, torch, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return canAttachTo(level, pos.relative(facing.getOpposite()), facing);
    }

    protected boolean canAttachTo(LevelReader level, BlockPos supportPos, Direction attachedSide) {
        BlockState support = level.getBlockState(supportPos);
        return support.isFaceSturdy(level, supportPos, attachedSide)
                || support.hasAnalogOutputSignal()
                || support.isSignalSource()
                || support.isRedstoneConductor(level, supportPos);
    }

    protected boolean hasAttachedRorValueProvider(LevelReader level, BlockPos pos, Direction facing) {
        BlockEntity blockEntity = level.getBlockEntity(pos.relative(facing.getOpposite()));
        return blockEntity instanceof RORValueProvider;
    }

    protected boolean hasAttachedRorInteractive(LevelReader level, BlockPos pos, Direction facing) {
        BlockEntity blockEntity = level.getBlockEntity(pos.relative(facing.getOpposite()));
        return blockEntity instanceof RORInteractive;
    }

    protected boolean hasAttachedItemHandler(LevelReader level, BlockPos pos, Direction facing) {
        BlockEntity blockEntity = level.getBlockEntity(pos.relative(facing.getOpposite()));
        return blockEntity != null && blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private static VoxelShape shapeFor(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN_SHAPE;
            case UP -> UP_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
        };
    }

    private static VoxelShape shape(Direction direction) {
        return Block.box(
                direction.getStepX() == 1 ? 0.0D : 6.0D,
                direction.getStepY() == 1 ? 0.0D : 6.0D,
                direction.getStepZ() == 1 ? 0.0D : 6.0D,
                direction.getStepX() == -1 ? 16.0D : 10.0D,
                direction.getStepY() == -1 ? 16.0D : 10.0D,
                direction.getStepZ() == -1 ? 16.0D : 10.0D);
    }
}
