package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.RBMKCraneConsoleBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RBMKCraneConsoleBlock extends LegacyXrMultiblockBlock implements EntityBlock, Toolable {
    private static final int[] LEGACY_DIMENSIONS = new int[] { 1, 0, 0, 0, 1, 1 };
    private static final int[] LEGACY_EXTRA_DIMENSIONS = new int[] { 0, 0, 0, 1, 1, 1 };

    public RBMKCraneConsoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return LEGACY_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return 1;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
        BlockPos extraOrigin = LegacyMultiblockLayout.relative(facing);
        return super.getLayout(state)
                .withLegacyXrProxyFill(LEGACY_EXTRA_DIMENSIONS, facing, extraOrigin, LegacyProxyMode.passive())
                .withLegacyXrCheckOnly(LEGACY_EXTRA_DIMENSIONS, facing, extraOrigin)
                .withLegacyExtraOffsets(LegacyMultiblockLayout.legacyXrFillOffsets(
                        LEGACY_EXTRA_DIMENSIONS, facing, extraOrigin));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return localShape(BlockPos.ZERO);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return localShape(BlockPos.ZERO);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        for (BlockPos offset : getLayout(state).offsets()) {
            double height = isTopOffset(offset) ? 0.5D : 1.0D;
            shape = Shapes.or(shape, Shapes.box(
                    offset.getX(),
                    offset.getY(),
                    offset.getZ(),
                    offset.getX() + 1.0D,
                    offset.getY() + height,
                    offset.getZ() + 1.0D));
        }
        return shape.optimize();
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return getMultiblockShape(state, level, corePos, context);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(state.getBlock())) {
            fillConsoleLayout(level, pos, state);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ToolType tool = ToolType.getType(player.getItemInHand(hand));
        if (tool == ToolType.SCREWDRIVER) {
            return onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)
                    ? InteractionResult.sidedSuccess(level.isClientSide)
                    : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }
        if (level.isClientSide) {
            return MultiblockHelper.isOperationalCoreLayoutComplete(level, pos);
        }
        if (!level.isClientSide) {
            BlockEntity blockEntity = MultiblockHelper.resolveOperationalCoreBlockEntity(level, pos);
            if (!(blockEntity instanceof RBMKCraneConsoleBlockEntity console)) {
                return false;
            }
            console.cycleCraneRotation();
        }
        return true;
    }

    private boolean fillConsoleLayout(Level level, BlockPos corePos, BlockState state) {
        LegacyMultiblockLayout layout = getLayout(state);
        boolean filled = MultiblockHelper.fillLayout(level, corePos, layout);
        boolean complete = filled && MultiblockHelper.isLayoutComplete(level, corePos, layout);
        if (!complete) {
            onIncompleteLegacyLayout(level, corePos, state);
        }
        return complete;
    }

    @Override
    protected boolean requiresCompleteLegacyLayout(BlockState state) {
        return true;
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean usesLocalDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesLocalDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesMultiblockHighlightShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }

    @Override
    public boolean requiresCompleteOperationalLayout(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public VoxelShape getMultiblockDummyShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return localShape(dummyPos.subtract(corePos));
    }

    @Override
    public VoxelShape getMultiblockDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return localShape(dummyPos.subtract(corePos));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RBMKCraneConsoleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.RBMK_CRANE_CONSOLE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        RBMKCraneConsoleBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (RBMKCraneConsoleBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        RBMKCraneConsoleBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (RBMKCraneConsoleBlockEntity) blockEntity);
    }

    private static boolean isTopOffset(BlockPos offset) {
        return offset.getY() > 0;
    }

    private static VoxelShape localShape(BlockPos offset) {
        return Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, isTopOffset(offset) ? 0.5D : 1.0D, 1.0D);
    }
}
