package com.hbm.ntm.block;

import com.hbm.ntm.api.block.IBomb;
import com.hbm.ntm.blockentity.SealHatchBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 1.20.1 carrier for the old BlockSeal controller.  The sealed area is a
 * horizontal square whose centre lies behind the controller facing.
 */
@SuppressWarnings("deprecation")
public class SealControllerBlock extends Block implements IBomb {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    private static final int MAX_FRAME_RADIUS_EXCLUSIVE = 7;

    public SealControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        // Old yaw mapping 0/1/2/3 -> north/east/south/west is exactly the
        // opposite of the player's horizontal facing.
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            toggleIfValid(level, pos, state);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
            if (powered) {
                toggleIfValid(level, pos, state);
            }
        }
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level == null || pos == null || level.isClientSide || !level.getBlockState(pos).is(this)) {
            return BombReturnCode.UNDEFINED;
        }
        return toggleIfValid(level, pos, level.getBlockState(pos))
                ? BombReturnCode.TRIGGERED
                : BombReturnCode.ERROR_INCOMPATIBLE;
    }

    public static int getFrameSize(Level level, BlockPos controller) {
        BlockState controllerState = level.getBlockState(controller);
        if (!controllerState.is(ModBlocks.SEAL_CONTROLLER.get())) {
            return 0;
        }
        Direction facing = controllerState.getValue(FACING);
        for (int radius = 1; radius < MAX_FRAME_RADIUS_EXCLUSIVE; radius++) {
            BlockPos centre = controller.relative(facing.getOpposite(), radius);
            if (hasFrame(level, centre, radius)) {
                return radius;
            }
        }
        return 0;
    }

    public static boolean isSealClosed(Level level, BlockPos controller, int radius) {
        BlockPos centre = getCentre(level, controller, radius);
        for (int x = -radius + 1; x < radius; x++) {
            for (int z = -radius + 1; z < radius; z++) {
                if (level.getBlockState(centre.offset(x, 0, z)).is(ModBlocks.SEAL_HATCH.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void closeSeal(Level level, BlockPos controller, int radius) {
        BlockPos centre = getCentre(level, controller, radius);
        for (int x = -radius + 1; x < radius; x++) {
            for (int z = -radius + 1; z < radius; z++) {
                BlockPos target = centre.offset(x, 0, z);
                if (level.isEmptyBlock(target)) {
                    level.setBlock(target, ModBlocks.SEAL_HATCH.get().defaultBlockState(), Block.UPDATE_ALL);
                    if (level.getBlockEntity(target) instanceof SealHatchBlockEntity hatch) {
                        hatch.setControllerPos(controller);
                    }
                }
            }
        }
    }

    public static void openSeal(Level level, BlockPos controller, int radius) {
        BlockPos centre = getCentre(level, controller, radius);
        for (int x = -radius + 1; x < radius; x++) {
            for (int z = -radius + 1; z < radius; z++) {
                BlockPos target = centre.offset(x, 0, z);
                if (level.getBlockState(target).is(ModBlocks.SEAL_HATCH.get())) {
                    level.removeBlock(target, false);
                }
            }
        }
    }

    private static boolean toggleIfValid(Level level, BlockPos controller, BlockState state) {
        int radius = getFrameSize(level, controller);
        if (radius == 0) {
            return false;
        }
        if (isSealClosed(level, controller, radius)) {
            openSeal(level, controller, radius);
        } else {
            closeSeal(level, controller, radius);
        }
        return true;
    }

    private static boolean hasFrame(Level level, BlockPos centre, int radius) {
        for (int x = -radius; x <= radius; x++) {
            if (!isFrameMember(level.getBlockState(centre.offset(x, 0, -radius)))
                    || !isFrameMember(level.getBlockState(centre.offset(x, 0, radius)))) {
                return false;
            }
        }
        for (int z = -radius; z <= radius; z++) {
            if (!isFrameMember(level.getBlockState(centre.offset(-radius, 0, z)))
                    || !isFrameMember(level.getBlockState(centre.offset(radius, 0, z)))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFrameMember(BlockState state) {
        return state.is(ModBlocks.SEAL_FRAME.get()) || state.is(ModBlocks.SEAL_CONTROLLER.get());
    }

    private static BlockPos getCentre(Level level, BlockPos controller, int radius) {
        return controller.relative(level.getBlockState(controller).getValue(FACING).getOpposite(), radius);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }
}
