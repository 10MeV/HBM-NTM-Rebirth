package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LaunchPadBlockEntity;
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
public class LaunchPadBlock extends LegacyXrMultiblockBlock implements EntityBlock {
    private static final int[] LEGACY_XR_DIMENSIONS = new int[] { 0, 0, 1, 1, 1, 1 };
    private static final int LEGACY_OFFSET = 1;
    private static final VoxelShape SHAPE = Shapes.or(
            box(-24.0D, 0.0D, -24.0D, -8.0D, 16.0D, -8.0D),
            box(8.0D, 0.0D, -24.0D, 24.0D, 16.0D, -8.0D),
            box(-24.0D, 0.0D, 8.0D, -8.0D, 16.0D, 24.0D),
            box(8.0D, 0.0D, 8.0D, 24.0D, 16.0D, 24.0D),
            box(-8.0D, 8.0D, -24.0D, 8.0D, 16.0D, 24.0D),
            box(-24.0D, 8.0D, -8.0D, 24.0D, 16.0D, 8.0D)).optimize();

    public LaunchPadBlock(Properties properties) {
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
                .withExtraProxyOffsets(List.of(
                        new BlockPos(1, 0, 1),
                        new BlockPos(1, 0, -1),
                        new BlockPos(-1, 0, 1),
                        new BlockPos(-1, 0, -1)), LegacyProxyMode.combo(true, true, true));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaunchPadBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof LaunchPadBlockEntity launchPad) {
            NetworkHooks.openScreen(serverPlayer, launchPad, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.LAUNCH_PAD.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        LaunchPadBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (LaunchPadBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        LaunchPadBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (LaunchPadBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof LaunchPadBlockEntity launchPad) {
            for (ItemStack stack : launchPad.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
