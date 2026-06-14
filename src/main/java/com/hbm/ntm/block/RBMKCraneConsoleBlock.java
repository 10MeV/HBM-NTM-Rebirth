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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RBMKCraneConsoleBlock extends LegacyXrMultiblockBlock implements EntityBlock, Toolable {
    private static final int[] LEGACY_DIMENSIONS = new int[] { 1, 0, 0, 0, 1, 1 };
    private static final int[] LEGACY_EXTRA_DIMENSIONS = new int[] { 0, 0, 0, 1, 1, 1 };
    private static final VoxelShape HALF_HEIGHT_SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);

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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ToolType tool = ToolType.getType(player.getItemInHand(hand));
        if (tool == ToolType.SCREWDRIVER) {
            return onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)
                    ? InteractionResult.sidedSuccess(level.isClientSide)
                    : InteractionResult.PASS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }
        if (!level.isClientSide) {
            MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
            if (core == null || !(level.getBlockEntity(core.pos()) instanceof RBMKCraneConsoleBlockEntity console)) {
                return false;
            }
            console.cycleCraneRotation();
        }
        return true;
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public VoxelShape getMultiblockDummyShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return isTopDummy(corePos, dummyPos) ? HALF_HEIGHT_SHAPE : Shapes.block();
    }

    @Override
    public VoxelShape getMultiblockDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return getMultiblockDummyShape(state, level, corePos, dummyPos, context);
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

    private static boolean isTopDummy(BlockPos corePos, BlockPos dummyPos) {
        return dummyPos.getY() > corePos.getY();
    }
}
