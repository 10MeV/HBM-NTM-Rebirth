package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PneumaticStorageExporterBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class PneumaticStorageExporterBlock extends BaseEntityBlock {
    public PneumaticStorageExporterBlock(Properties properties) { super(properties); }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PneumaticStorageExporterBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.PNEUMATIC_STORAGE_EXPORTER.get(), PneumaticStorageExporterBlockEntity::serverTick); }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) { if (!player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof PneumaticStorageExporterBlockEntity exporter) { if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) NetworkHooks.openScreen(serverPlayer, exporter, data -> data.writeBlockPos(pos)); return InteractionResult.sidedSuccess(level.isClientSide); } return InteractionResult.PASS; }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) { if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PneumaticStorageExporterBlockEntity exporter) for (int slot = 9; slot < 18; slot++) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), exporter.getItems().getStackInSlot(slot)); super.onRemove(state, level, pos, newState, movedByPiston); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
}
