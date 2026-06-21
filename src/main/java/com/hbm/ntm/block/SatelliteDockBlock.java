package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.SatelliteDockBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
public class SatelliteDockBlock extends LegacyVisibleMultiblockMachineBlock {
    private static final VoxelShape CORE_SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);
    private static final VoxelShape DUMMY_PLATE_SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);

    public SatelliteDockBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteDockBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof SatelliteDockBlockEntity dock) {
            NetworkHooks.openScreen(serverPlayer, dock, dock.getBlockPos());
        }
        return player.isShiftKeyDown() ? InteractionResult.PASS : InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.SAT_DOCK.get()
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                SatelliteDockBlockEntity.serverTick(tickLevel, tickPos, tickState, (SatelliteDockBlockEntity) blockEntity)
                : null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof SatelliteDockBlockEntity dock) {
            for (ItemStack stack : dock.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return CORE_SHAPE;
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return CORE_SHAPE;
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
    public VoxelShape getMultiblockDummyShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return DUMMY_PLATE_SHAPE;
    }

    @Override
    public VoxelShape getMultiblockDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return DUMMY_PLATE_SHAPE;
    }

    @Override
    public boolean usesMultiblockHighlightShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return false;
    }
}
