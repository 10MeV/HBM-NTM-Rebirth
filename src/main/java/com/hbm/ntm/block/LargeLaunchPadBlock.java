package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LargeLaunchPadBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LargeLaunchPadBlock extends LegacyXrMultiblockBlock implements EntityBlock, RemoteDetonatableBlock {
    private static final int[] LEGACY_XR_DIMENSIONS = new int[] { 0, 0, 4, 4, 4, 4 };
    private static final int LEGACY_OFFSET = 4;
    private static final List<BlockPos> EXTRA_PORTS = List.of(
            new BlockPos(4, 0, 2),
            new BlockPos(4, 0, -2),
            new BlockPos(-4, 0, 2),
            new BlockPos(-4, 0, -2),
            new BlockPos(2, 0, 4),
            new BlockPos(-2, 0, 4),
            new BlockPos(2, 0, -4),
            new BlockPos(-2, 0, -4));
    private static final VoxelShape SHAPE = Shapes.or(
            box(-72.0D, 0.0D, -72.0D, 72.0D, 16.0D, -8.0D),
            box(-72.0D, 0.0D, 8.0D, 72.0D, 16.0D, 72.0D),
            box(-72.0D, 14.0D, -8.0D, 72.0D, 16.0D, 8.0D)).optimize();

    public LargeLaunchPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return LEGACY_XR_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return LEGACY_OFFSET;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        Direction facing = state.getValue(FACING);
        return LegacyMultiblockLayout.ofLegacyXrChecked(LEGACY_XR_DIMENSIONS, facing)
                .withExtraProxyOffsets(EXTRA_PORTS, LegacyProxyMode.combo(true, true, true));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LargeLaunchPadBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof LargeLaunchPadBlockEntity launchPad) {
            NetworkHooks.openScreen(serverPlayer, launchPad, launchPad.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.LAUNCH_PAD_LARGE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        LargeLaunchPadBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (LargeLaunchPadBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        LargeLaunchPadBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (LargeLaunchPadBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof LargeLaunchPadBlockEntity launchPad) {
            for (ItemStack stack : launchPad.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    @Override
    public BombReturnCode detonateFromRemote(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        if (!(resolveCoreBlockEntity(level, pos) instanceof LargeLaunchPadBlockEntity launchPad)) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        return launchPad.launchFromDesignator() ? BombReturnCode.LAUNCHED : BombReturnCode.ERROR_MISSING_COMPONENT;
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesMultiblockHighlightShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
