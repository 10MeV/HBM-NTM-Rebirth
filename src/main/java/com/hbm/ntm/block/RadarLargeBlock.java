package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.blockentity.RadarLargeBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RadarLargeBlock extends LegacyVisibleMultiblockMachineBlock {
    public RadarLargeBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadarLargeBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        return RadarBlockSupport.useRadarCore(level, pos, player);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.MACHINE_RADAR_LARGE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                RadarBlockEntity.clientTick(tickLevel, tickPos, tickState, (RadarLargeBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                RadarBlockEntity.serverTick(tickLevel, tickPos, tickState, (RadarLargeBlockEntity) blockEntity);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return RadarBlockSupport.redstoneOutput(level, pos);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        RadarBlockSupport.refreshEnergyConnections(level, pos);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        RadarBlockSupport.dropInventory(level, pos);
    }
}
