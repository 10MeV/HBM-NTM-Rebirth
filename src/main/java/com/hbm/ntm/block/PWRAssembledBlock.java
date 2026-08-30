package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PWRAssembledBlockEntity;
import com.hbm.ntm.client.ClientGeometryInvalidationBridge;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class PWRAssembledBlock extends Block implements EntityBlock {
    public static final BooleanProperty PORT = BooleanProperty.create("port");

    public PWRAssembledBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PORT, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PWRAssembledBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PORT);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (level.isClientSide && !state.is(newState.getBlock())) {
            refreshConnectedTextureNeighborhood(level, pos);
        }
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PWRAssembledBlockEntity assembled) {
            assembled.invalidateController();
            assembled.restoreOriginalBlock();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (level.isClientSide && !state.is(oldState.getBlock())) {
            refreshConnectedTextureNeighborhood(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, moving);
        if (level.isClientSide) {
            refreshConnectedTextureNeighborhood(level, pos);
        }
    }

    /** CTContext reads diagonal cells, so refresh the complete 3x3x3 dependency neighborhood. */
    public static void refreshConnectedTextureNeighborhood(Level level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof PWRAssembledBlockEntity) {
                entity.requestModelDataUpdate();
            }
            ClientGeometryInvalidationBridge.schedule(pos);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.PWR_BLOCK.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        PWRAssembledBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (PWRAssembledBlockEntity) blockEntity);
    }
}
