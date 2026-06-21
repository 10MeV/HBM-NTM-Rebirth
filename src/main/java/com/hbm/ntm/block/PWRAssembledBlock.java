package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.PWRAssembledBlockEntity;
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
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PWRAssembledBlockEntity assembled) {
            assembled.invalidateController();
            assembled.restoreOriginalBlock();
        }
        super.onRemove(state, level, pos, newState, moving);
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
