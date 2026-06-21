package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ElectricPressBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ElectricPressBlock extends LegacyXrMultiblockBlock implements EntityBlock {
    private static final int[] LEGACY_DIMENSIONS = { 2, 0, 0, 0, 0, 0 };

    public ElectricPressBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return LEGACY_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return 0;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return LegacyMultiblockLayout.ofLegacyXrChecked(LEGACY_DIMENSIONS, state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricPressBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getMultiblockShape(state, level, pos, context);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof ElectricPressBlockEntity machine) {
            NetworkHooks.openScreen(serverPlayer, machine, machine.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.ELECTRIC_PRESS.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        ElectricPressBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (ElectricPressBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        ElectricPressBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (ElectricPressBlockEntity) blockEntity);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && resolveCoreBlockEntity(level, pos) instanceof ElectricPressBlockEntity machine) {
            for (ItemStack stack : machine.getDrops()) {
                Block.popResource(level, machine.getBlockPos(), stack);
            }
            level.updateNeighbourForOutputSignal(machine.getBlockPos(), this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
